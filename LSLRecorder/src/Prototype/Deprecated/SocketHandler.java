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

package Prototype.Deprecated;

import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskMonitor;
import Config.Parameter;
import Config.ParameterList;
import Controls.HandlerMinionTemplate;
import Controls.MinionParameters;
import Controls.Messages.EventInfo;
import Controls.Messages.EventType;
import Prototype.Discarded.Malfunction.Socket.streamClientSocket;
import Prototype.Discarded.Malfunction.Socket.streamServerSocket;
import Prototype.Discarded.Malfunction.Socket.streamingOutputMessage;
import Prototype.Discarded.Malfunction.Socket.Manager.ManagerInOutStreamSocket;
import Auxiliar.WarningMessage;
import Auxiliar.Extra.Tuple;
import Sockets.Info.SocketSetting;
import Sockets.Info.StreamSocketProblem;
import Sockets.Info.SocketParameters;
import StoppableThread.IStoppableThread;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class SocketHandler extends HandlerMinionTemplate implements ITaskMonitor
{
	public static final String ID = "SocketHandler";
	
	private static SocketHandler ctrStreaming = null;

	public static final String CLIENT_SOCKET_STREAMING = "client socket";
	public static final String SERVER_SOCKET_STREAMING = "server socket";
	
	private Map< InetSocketAddress, ManagerInOutStreamSocket > clients = null;
	private streamServerSocket server = null;

	private List< EventInfo > events = null;

	private List< INotificationTask > tasks = null;

	private Semaphore taskSem = null;
	private Semaphore sendMsgSemaphore = null;

	//private boolean stopRunLoop = false;

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

		this.clients = new HashMap< InetSocketAddress, ManagerInOutStreamSocket >();		
		this.events = new ArrayList< EventInfo >();
		this.tasks = new ArrayList< INotificationTask >();

		this.taskSem = new Semaphore(1, true);
		this.sendMsgSemaphore = new Semaphore(1, true);
	}

	/*
	 * (non-Javadoc)
	 * @see Controls.HandlerMinionTemplate#startWork(java.lang.Object)
	 */
	@Override
	protected void startWork( Object info ) throws Exception
	{
		try
		{
			this.sendMsgSemaphore.acquire();
		}
		catch (InterruptedException localInterruptedException) 
		{}

		Tuple< String, List< streamingOutputMessage > > in = (Auxiliar.Extra.Tuple< String, List< streamingOutputMessage > > )info;
		if( in.x.equals( CLIENT_SOCKET_STREAMING ) )
		{	
			for( streamingOutputMessage msg : in.y )
			{
				List< SocketSetting > dests = msg.getDestinations();

				for( SocketSetting sID : dests )
				{
					ManagerInOutStreamSocket ioss = this.clients.get( sID.getSocketAddress() );

					if( ioss != null )
					{
						ioss.sendMessage( msg );
					}
				}
			}
		}
		else if( in.x.equals( SERVER_SOCKET_STREAMING ) )
		{
			for( streamingOutputMessage msg : in.y )
			{
				ManagerInOutStreamSocket ioss = this.clients.get( msg.getOrigin().getSocketAddress() );

				if( ioss != null )
				{
					ioss.sendMessage( msg );
				}
			}
		}
		else
		{
			throw new IllegalArgumentException( "Tuple.x is unknown." );
		}

		if( this.sendMsgSemaphore.availablePermits() < 1 )
		{
			this.sendMsgSemaphore.release();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see Controls.HandlerMinionTemplate#createSubordinates(Controls.MinionParameters)
	 */
	@Override
	protected List<IStoppableThread> createSubordinates( MinionParameters minionPars) throws Exception
	{
		if ( minionPars == null )
		{
			throw new NullPointerException( "Parameters null." );
		}

		this.deletingSubordinates = false;
		
		List<IStoppableThread> clientList = new ArrayList< IStoppableThread >();

		ParameterList parameters = minionPars.getMinionParameters( ID );
		
		if( parameters == null )
		{
			throw new IllegalArgumentException( "Parameters of minion handler <" + ID + "> is not found." );
		}
				
		for (String parID : parameters.getParameterIDs() )
		{			
			Parameter parameter = parameters.getParameter( parID );
			
			List<SocketParameters> pars = (List< SocketParameters >)parameter.getValue();

			if (parID.equals( CLIENT_SOCKET_STREAMING ))
			{
				for (SocketParameters par : pars)
				{
					try
					{
						ManagerInOutStreamSocket client = streamClientSocket.createClientSocket( par );
						client.taskMonitor( this );
						client.startThread();

						this.clients.put( client.getRemoteSocketInfo().getSocketAddress(), client );

						clientList.add( client );
					}
					catch (Exception e)
					{
						StreamSocketProblem problem = new StreamSocketProblem(par.getSocketInfo().getSocketAddress(), e);
						this.events.add(new EventInfo( EventType.SOCKET_CONNECTION_PROBLEM, problem));
					}

				}
			} else if (parID.equals( SERVER_SOCKET_STREAMING ))
			{
				if (!pars.isEmpty())
				{
					SocketParameters par = (SocketParameters)pars.get(0);

					this.server = new streamServerSocket( par );
					this.server.taskMonitor( this );
					this.server.startThread();

					clientList.add( this.server );
				}
			}
		}

		this.checkEvents();

		return clientList;
	}

	/*
	 * (non-Javadoc)
	 * @see Auxiliar.Tasks.ITaskMonitor#taskDone(Auxiliar.Tasks.INotificationTask)
	 */
	@Override
	public void taskDone(INotificationTask t)
	{
		try
		{
			this.taskSem.acquire();
		}
		catch (InterruptedException localInterruptedException) 
		{}
				
		this.tasks.add( t );
		
		if (this.taskSem.availablePermits() < 1)
		{
			this.taskSem.release();
		}
				
		synchronized( this )
		{
			super.notify();
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


		try
		{
			this.taskSem.acquire();
		}
		catch (InterruptedException localInterruptedException1) 
		{}

		synchronized( this.tasks ) 
		{	
			for( INotificationTask task : this.tasks )
			{
				List< EventInfo > r = new ArrayList< EventInfo >( task.getResult() );          
				task.clearResult();

				for( EventInfo e : r )
				{
					if (e.getEventType().equals(  EventType.SOCKET_INOUT_CHANNEL_CREATED ) )
					{

						ManagerInOutStreamSocket c = (ManagerInOutStreamSocket)e.getEventInformation();
						c.taskMonitor(this);

						c.setReportClosedSocketByInterruptExceptionReader(false);
						c.setReportClosedSocketByInterruptExceptionWriter(true);

						try
						{
							c.startThread();

							SocketSetting clientInfo = c.getRemoteSocketInfo();

							if (clientInfo == null)
							{
								clientInfo = c.getLocalSocketInfo();
							}

							this.clients.put( clientInfo.getSocketAddress(), c );
							this.subordiateList.add(c);

						}
						catch (Exception ex)
						{
							StreamSocketProblem problem = new StreamSocketProblem(c.getRemoteSocketInfo().getSocketAddress(), ex);
							this.events.add(new EventInfo( EventType.SOCKET_CONNECTION_PROBLEM, problem));
						}
					}
					else if ( e.getEventType().equals( EventType.SOCKET_INPUT_MSG ) )
					{						
						this.events.add( e );							
					}
					else if (e.getEventType().equals( EventType.SOCKET_CONNECTION_PROBLEM ))
					{
						this.events.add( e );
					}
					else if( e.getEventType().equals( EventType.THREAD_STOP ) )
					{
						if( !this.deletingSubordinates )
						{
							Tuple< SocketSetting, SocketSetting > c = (Tuple<SocketSetting, SocketSetting>)e.getEventInformation();
							SocketSetting local = (SocketSetting)c.x;
							SocketSetting remote = (SocketSetting)c.y;

							SocketSetting id = remote;
							ManagerInOutStreamSocket ioss = null;

							if (id != null)
							{
								ioss = (ManagerInOutStreamSocket)this.clients.get( id.getSocketAddress() );
								if (ioss == null)
								{
									id = local;
									if (id != null)
									{
										ioss = (ManagerInOutStreamSocket)this.clients.get( id.getSocketAddress() );
									}
								}
							}
							else
							{
								id = local;
								ioss = (ManagerInOutStreamSocket)this.clients.get( id.getSocketAddress() );
							}

							if ( ioss != null )
							{
								this.clients.remove( id.getSocketAddress() );

								boolean clientClose = ( local != null ) 
										&& ( this.server != null ) 
										&& ( !this.server.getSocketParameters().getSocketInfo().equals( local ) );

								if( clientClose )
								{
									this.events.add(new EventInfo( EventType.SOCKET_OUTPUT_SOCKET_CLOSES
											, new StreamSocketProblem( id.getSocketAddress()
														, new Exception("The output socket " + id + " is closed."))));
								}

							}
						}
					}
					else if (e.getEventType().equals( EventType.SERVER_THREAD_STOP ))
					{
						this.server = null;
					}
				}
			}

			this.checkEvents();

			this.tasks.clear();
		}

		if (this.taskSem.availablePermits() < 1)
		{
			this.taskSem.release();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see Controls.HandlerMinionTemplate#deleteSubordinates(int)
	 */
	@Override
	public synchronized void deleteSubordinates(int friendliness)
	{
		this.deletingSubordinates = true;
	
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
					this.event = new EventInfo(  EventType.SOCKET_EVENTS, new ArrayList< EventInfo >( this.events ) );
					this.events.clear();
				}
			}
			else
			{
				super.event = new EventInfo( EventType.SOCKET_EVENTS, new ArrayList< EventInfo >( this.events ) );
				this.events.clear();
			}

			this.supervisor.eventNotification( this, super.event );
		}
	}

	/**
	 * Remove client socket (input streaming).
	 * 
	 * @param socketID 	-> Socket identifier  
	 */
	public void removeClientStreamSocket( InetSocketAddress socketID )
	{
		ManagerInOutStreamSocket client = (ManagerInOutStreamSocket)this.clients.get(socketID);

		if (client != null)
		{
			if ( client.isFinished() )
			{
				this.clients.remove( socketID );
			}
			else
			{
				client.stopThread(  IStoppableThread.STOP_IN_NEXT_LOOP  );
			}
		}
	}

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