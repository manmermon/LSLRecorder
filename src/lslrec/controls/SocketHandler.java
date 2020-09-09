/*
 * Work based on streamingControl class
 * of CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.
 *
 *   LSLRec is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   LSLRec is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with LSLRec.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */

package lslrec.controls;

import lslrec.controls.messages.EventInfo;
import lslrec.controls.messages.EventType;
import lslrec.sockets.info.SocketSetting;
import lslrec.sockets.info.StreamSocketProblem;
import lslrec.sockets.SocketReaderThread;
import lslrec.sockets.TCP_UDPServer;
import lslrec.sockets.info.SocketParameters;
import lslrec.stoppableThread.IStoppableThread;
import lslrec.auxiliar.WarningMessage;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import lslrec.auxiliar.tasks.INotificationTask;
import lslrec.auxiliar.tasks.ITaskMonitor;

public class SocketHandler extends HandlerMinionTemplate implements ITaskMonitor
{
	public static final String ID = "SocketHandler";
	
	private static SocketHandler ctrStreaming = null;

	public static final String SERVER_SOCKET_STREAMING = "server socket";
	public static final String INPUT_MSG = "input messages";
	
	private TCP_UDPServer server = null;

	private List< EventInfo > events = null;

	private Map< String, SocketReaderThread > tcpReader = null;
	private ConcurrentLinkedQueue< EventInfo > taskEvents = null;

	private boolean deletingSubordinates = false;
	
	/**
	 * Singleton class.
	 * 
	 * @return Instance of SocketHandler class
	 */
	public static SocketHandler getInstance()
	{
		if (ctrStreaming == null)
		{
			ctrStreaming = new SocketHandler();
		}

		return ctrStreaming;
	}

	/**
	 * Private constructor
	 */
	private SocketHandler()
	{
		super();

		super.setName( this.getClass().getSimpleName() );
		
		this.events = new ArrayList< EventInfo >();
		this.taskEvents = new ConcurrentLinkedQueue< EventInfo >();

		this.tcpReader = new HashMap< String, SocketReaderThread >();
	}

	/*
	 * (non-Javadoc)
	 * @see Controls.HandlerMinionTemplate#startWork(java.lang.Object)
	 */
	@Override
	protected void startWork( Object info ) throws Exception
	{	
	}
	
	/*
	 * (non-Javadoc)
	 * @see Controls.HandlerMinionTemplate#createSubordinates(Controls.MinionParameters)
	 */
	@Override
	protected List< IStoppableThread > createSubordinates( MinionParameters minionPars) throws Exception
	{
		if ( minionPars == null )
		{
			throw new NullPointerException( "Parameters null." );
		}

		this.deletingSubordinates = false;
		
		List< IStoppableThread > socketList = new ArrayList< IStoppableThread >();

		ParameterList parameters = minionPars.getMinionParameters( ID );
		
		if( parameters == null )
		{
			throw new IllegalArgumentException( "Parameters of minion handler <" + ID + "> is not found." );
		}
				
		for (String parID : parameters.getParameterIDs() )
		{			
			Parameter parameter = parameters.getParameter( parID );
			
			if (parID.equals( SERVER_SOCKET_STREAMING ))
			{
				List<SocketParameters> pars = (List< SocketParameters >)parameter.getValue();
				
				if ( !pars.isEmpty() )
				{
					SocketParameters par = (SocketParameters)pars.get(0);

					SocketSetting setting = par.getSocketInfo();
					
					this.server = new TCP_UDPServer( setting );
					this.server.taskMonitor( this );
					this.server.startThread();
					
					socketList.add( this.server );
				}
			}
			/*
			else if( parID.equals( INPUT_MSG ) )
			{
				if( this.delayCalculator == null )
				{
					this.delayCalculator = new SocketMessageDelayCalculator( ConfigApp.DEFAULT_NUM_SOCKET_PING );
					this.delayCalculator.taskMonitor( this );
				}
				
				this.delayCalculator.clearInputMessages();
				this.delayCalculator.AddInputMessages( (Map< String, Integer >)parameter.getValue() );				
			}
			*/
		}

		this.checkEvents();

		return socketList;
	}

	/*
	 * (non-Javadoc)
	 * @see Auxiliar.Tasks.ITaskMonitor#taskDone(Auxiliar.Tasks.INotificationTask)
	 */
	@Override
	public synchronized void taskDone( INotificationTask t )
	{
		/*
		try
		{
			this.taskSem.acquire();
		}
		catch (InterruptedException localInterruptedException) 
		{
			localInterruptedException.printStackTrace();
		}
		*/
		
		if( t != null )
		{
			List< EventInfo > evs = t.getResult( true );
			
			if( evs != null )
			{
				this.taskEvents.addAll( evs );
			}
			
				
			/*
			if (this.taskSem.availablePermits() < 1)
			{
				this.taskSem.release();
			}
			*/
					
			synchronized( this )
			{
				super.notify();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see Controls.HandlerMinionTemplate#cleanUpSubordinates()
	 */
	@Override
	protected void cleanUpSubordinates() 
	{
		
	}

	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#runInLoop()
	 */
	@Override
	protected void runInLoop() throws Exception 
	{
		try
		{
			super.wait();
		}
		catch (InterruptedException localInterruptedException) 
		{}

		boolean continued = true;
		
		do 
		{	
			EventInfo e = this.taskEvents.poll();

			continued = ( e != null );

			if( continued )
			{			

				if ( e.getEventType().equals( EventType.SOCKET_CONNECTION_DONE ) )
				{						
					Socket client = (Socket)e.getEventInformation();

					SocketReaderThread c = new SocketReaderThread( client, this );

					c.setName( "SERVER-" + c.getName() );

					this.tcpReader.put( c.getID(), c );

					c.startThread();						
				}
				else if ( e.getEventType().equals( EventType.SOCKET_INPUT_MSG ) )
				{	
					this.events.add( e );
				}
				/*
					if( this.delayCalculator != null )
					{
						this.delayCalculator.CalculateMsgDelay( (StreamInputMessage) e.getEventInformation() );
					}
				}
				else if( e.getEventType().equals( EventType.SOCKET_MSG_DELAY ) )
				{
					this.events.add( e );
				}
				else if( e.getEventType().equals( EventType.SOCKET_PING_END ) )
				{
					this.delayCalculator = null;
				}
				*/
				else if (e.getEventType().equals( EventType.SOCKET_CONNECTION_PROBLEM ))
				{
					this.events.add( e );
				}
				else if( e.getEventType().equals( EventType.THREAD_STOP ) )
				{						
					String id = (String)e.getEventInformation();
					SocketReaderThread reader = this.tcpReader.remove( id );

					if( reader != null && !this.deletingSubordinates )
					{										
						InetSocketAddress address = new InetSocketAddress( reader.getLocalAddress(), reader.getLocatPort() );
						this.events.add(new EventInfo( event.getIdSource(),  EventType.SOCKET_CONNECTION_PROBLEM
														, new StreamSocketProblem( address
																, new Exception("The output socket " 
																		+ id 
																		+ " is closed." ) ) ) ) ;
					}
				}
				else if( e.getEventType().equals( EventType.SOCKET_CHANNEL_CLOSE ) )
				{
					String id = (String)e.getEventInformation();
					SocketReaderThread reader = this.tcpReader.remove( id );

					if( reader != null && !this.deletingSubordinates )
					{
						InetSocketAddress address = new InetSocketAddress( reader.getLocalAddress(), reader.getLocatPort() );

						this.events.add(new EventInfo( event.getIdSource(),  EventType.SOCKET_CHANNEL_CLOSE
														, new StreamSocketProblem( address
																					, new SocketException("The output socket " 
																							+ id 
																							+ " is closed." ) ) ) ) ;
					}
					//System.out.println("SocketHandler2.runInLoop() SOCKET_CHANNEL_CLOSE " + this.tcpReader.remove( id ) );
				}
				else if ( e.getEventType().equals( EventType.SERVER_THREAD_STOP ))
				{
					//System.out.println("SocketHandler2.runInLoop() SERVER_THREAD_STOP");
					this.server = null;

					for( SocketReaderThread reader : this.tcpReader.values() )
					{
						reader.stopThread( IStoppableThread.FORCE_STOP );
					}
				}

			}

			this.checkEvents();
		}
		while( continued );

		/*
		if (this.taskSem.availablePermits() < 1)
		{
			this.taskSem.release();
		}
		*/
	}

	/*
	 * (non-Javadoc)
	 * @see Controls.HandlerMinionTemplate#deleteSubordinates(int)
	 */
	@Override
	public synchronized void deleteSubordinates( int friendliness )
	{
		this.deletingSubordinates = true;
	
		for( SocketReaderThread tcpClient :  this.tcpReader.values() )
		{		
			tcpClient.stopThread( IStoppableThread.FORCE_STOP );
		}
		
		super.deleteSubordinates( friendliness );		
	}
	
	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#preStopThread(int)
	 */
	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{		
	}

	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#postStopThread(int)
	 */
	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{		
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
		
		this.deleteSubordinates( IStoppableThread.FORCE_STOP );				
	}
	
	/**
	 * Notify to the supervisor if there are events. 
	 */
	private void checkEvents()
	{
		if ( !this.events.isEmpty() )
		{
			if ( super.event != null )
			{
				synchronized( super.event )
				{
					super.event = new EventInfo(  super.getName(), EventType.SOCKET_EVENTS, new ArrayList< EventInfo >( this.events ) );
					this.events.clear();
				}
			}
			else
			{
				super.event = new EventInfo( super.getName(), EventType.SOCKET_EVENTS, new ArrayList< EventInfo >( this.events ) );
				this.events.clear();
			}

			this.supervisor.eventNotification( this, super.event );
		}
	}

	/*
	public boolean isDelayCalculating()
	{
		return ( this.delayCalculator != null );
	}
	*/
	
	/*
	 * (non-Javadoc)
	 * @see Controls.IHandlerMinion#checkParameters()
	 */
	@Override
	public WarningMessage checkParameters()
	{
		return new WarningMessage();
	}
}