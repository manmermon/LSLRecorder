/*
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.testing.Socket;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import lslrec.auxiliar.tasks.INotificationTask;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.controls.messages.EventInfo;
import lslrec.controls.messages.EventType;
import lslrec.sockets.SocketReaderThread;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;


public class SocketActionManager extends AbstractStoppableThread implements ITaskMonitor, INotificationTask, Cloneable
{	
	private Socket socket;
	private ISocketAction socketAction;
	private String ID;
	
	private List< EventInfo > events;
	private List< EventInfo > stateEvent;
	
	private SocketReaderThread inputReader;
	private SocketWriterThread outputWriter;
	
	private ITaskMonitor monitor;
	
	private Semaphore semEvents;
	
	public SocketActionManager( Socket socket, ISocketAction action ) throws IllegalArgumentException, IOException  
	{		
		if( action == null )
		{
			throw new IllegalArgumentException( "Socket Action null" ); 
		}
		
		this.inputReader = new SocketReaderThread( socket, this );
		this.outputWriter = new SocketWriterThread( socket, this );
		
		this.socketAction = action;
		this.socket = socket;
		
		this.ID = this.socket.getLocalAddress().getHostAddress() + ":" + this.socket.getLocalPort()
					+ "<->" + this.socket.getInetAddress().getHostAddress() + ":" + this.socket.getPort();
		
		super.setName( this.ID );
				
		this.events = new ArrayList< EventInfo >();
		this.stateEvent = new ArrayList< EventInfo >();
		
		this.semEvents = new Semaphore( 1, true );
	}

	@Override
	protected void preStopThread( int friendliness ) throws Exception 
	{	
	}

	@Override
	protected void postStopThread( int friendliness ) throws Exception 
	{		
	}
	
	@Override
	protected void preStart() throws Exception 
	{
		super.preStart();
		
		this.outputWriter.startThread();
		this.inputReader.startThread();
	}

	@Override
	protected void runInLoop() throws Exception 
	{
		synchronized ( this )
		{	
			try
			{
				super.wait();
			}
			catch( InterruptedException e)
			{}
		}		
		
		try 
		{
			this.semEvents.acquire();
		} 
		catch (InterruptedException e) 
		{
		}
		
		for( EventInfo ev : this.events )
		{
			if( ev.getEventType().equals( EventType.SOCKET_INPUT_MSG ) )
			{
				String response = this.socketAction.prepareResponse( (String)ev.getEventInformation() );
				if( response != null )
				{
					this.outputWriter.sendMessage( response );
				}					
			}
			else if( ev.getEventType().equals( EventType.SOCKET_OUTPUT_MSG_OK ) )
			{

			}
			else if( ev.getEventType().equals( EventType.SOCKET_OUTPUT_MSG_SEND ) )
			{
				String outMsg = this.socketAction.prepareSending( (String)ev.getEventInformation() );
				if( outMsg != null )
				{
					this.outputWriter.sendMessage( outMsg );
				}
			}
			else if( ev.getEventType().equals( EventType.SOCKET_CHANNEL_CLOSE )
					|| ev.getEventType().equals( EventType.SOCKET_CONNECTION_PROBLEM ))
			{						
				this.stateEvent.add( new EventInfo( this.getID(), ev.getEventType(),  ev.getEventInformation() ) );

				this.inputReader.stopThread( IStoppableThread.FORCE_STOP );
				this.outputWriter.stopThread( IStoppableThread.FORCE_STOP );

				super.stopThread( IStoppableThread.FORCE_STOP );					
				break;
			}
		}
					
		this.events.clear();
		
		this.semEvents.release();
	}

	public synchronized void sendMessage( String msg ) 
	{
		try 
		{
			this.semEvents.acquire();
		} 
		catch (InterruptedException e) 
		{
		}
		//System.out.println("SocketActionManager.sendMessage() " + msg);
		this.events.add( new EventInfo( this.getID(), EventType.SOCKET_OUTPUT_MSG_SEND, msg ) );
				
		synchronized( this )
		{		
			this.notify();
		}
		
		this.semEvents.release();
	}
		
	@Override
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
		
		this.events.clear();
		
		this.inputReader.stopThread( IStoppableThread.FORCE_STOP );
		this.outputWriter.stopThread( IStoppableThread.FORCE_STOP );
		
		//System.out.println( "SocketActionManager.cleanUp()" );
		
		if( this.monitor != null )
		{
			synchronized( this.stateEvent )
			{			
				this.stateEvent.add( new EventInfo(this.getID(), EventType.THREAD_STOP, this.getID() ) );
				
				if( this.monitor != null )
				{
					try 
					{
						this.monitor.taskDone( this );
					}
					catch (Exception e) 
					{
						this.runExceptionManager( e );
					}
				}
			}
		}
		
		this.inputReader = null;
		this.outputWriter = null;
	}
	
	/**
	 * 
	 * @param task
	 * @throws Exception
	 */
	@Override
	public synchronized void taskDone( INotificationTask task ) throws Exception 
	{	
		List< EventInfo > events = task.getResult( true );
		
		synchronized ( events ) 
		{
			synchronized( this.events )
			{
				for( EventInfo ev : events )
				{
					this.events.add( new EventInfo(this.getID(), ev.getEventType(), ev.getEventInformation() ) );
				}

				super.notify();
			}
		}
		
		task.clearResult();
	}

	@Override
	public void taskMonitor(ITaskMonitor monitor) 
	{
		this.monitor = monitor;
	}

	@Override
	public List<EventInfo> getResult( boolean c ) 
	{
		return this.stateEvent;
	}

	@Override
	public void clearResult() 
	{
		this.stateEvent.clear();
	}
	
	@Override
	public String getID() 
	{
		return this.ID;
	}
	
	@Override
	protected Object clone() throws CloneNotSupportedException 
	{
		return super.clone();
	}
}
