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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import lslrec.auxiliar.tasks.INotificationTask;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.controls.messages.EventInfo;
import lslrec.controls.messages.EventType;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;


public class TCPClientSocketThread extends AbstractStoppableThread implements ITaskMonitor
{
	private Socket client;
	
	//private SocketWriterThread output;	
	//private SocketReaderThread input;
	
	private SocketActionManager inOutSocketManager;
	
	private List< EventInfo > responses;
	
	private int stopFriendliness;
	
	private Semaphore semStopSockAction;
	
	public TCPClientSocketThread( InetSocketAddress address ) throws Exception
	{
		this.client = new Socket( address.getAddress(), address.getPort() );	
		super.setName( "TCP-CLIENT>>" + client.getLocalAddress() + ":" + client.getLocalPort() );
		
		this.semStopSockAction = new Semaphore( 1, true );
	}
	
	public int getClientPort()
	{
		return this.client.getLocalPort();
	}
	
	public InetAddress getClientAddress()
	{
		return this.client.getLocalAddress();
	}
	
	public int getRemotePort()
	{
		return this.client.getPort();
	}
	
	public InetAddress getRemoteAddress()
	{
		return this.client.getInetAddress();
	}
	
	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{		
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{
		this.stopFriendliness = friendliness;
		//System.out.println("TCPClientSocketThread.postStopThread() " + getName() + "-" + System.nanoTime() + ">> " + friendliness);
		
		if( super.stopWhenTaskDone.get() )
		{
			//System.out.println("TCPClientSocketThread.postStopThread() "  + getName() );
			synchronized ( this )
			{
				this.notify();
			}
		}
	}

	@Override
	protected void preStart() throws Exception 
	{
		super.preStart();
		
		/*
		this.output = new SocketWriterThread( this.client, this );
		this.input = new SocketReaderThread( this.client, this );
		
		output.setName( "CLIENT--WRITER" );
		input.setName( "CLIENT--READER" );
		
		this.output.startThread();
		this.input.startThread();
		*/
		
		this.inOutSocketManager = new SocketActionManager( this.client, new ISocketAction() 
									{										
										@Override
										public String prepareSending(String outputMessage) 
										{							
											//System.out.println("TCPSeverSocketTheard.runInLoop() " + getName() + " - Send:" +outputMessage);
											return outputMessage;
										}
										
										@Override
										public String prepareResponse(String inputMessage) 
										{
											return null;
										}
									});
		
		this.responses = new ArrayList< EventInfo >();
				
		this.inOutSocketManager.taskMonitor( this );
		this.inOutSocketManager.startThread();
	}
	
	@Override
	protected void runInLoop() throws Exception 
	{	
		synchronized ( this ) 
		{			
			super.wait();
		}
		
		/*
		for( EventInfo ev : this.responses )
		{
			System.out.println("TCPClientSocketThread.runInLoop() RESPONSE " + ev );
		}
		*/	
	}
	
	@Override
	public void stopThread(int friendliness) 
	{	
		super.stopThread( friendliness );
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{
		//System.out.println("TCPClientSocketThread.cleanUp() " + getName() + "-" + System.nanoTime() + "> " + this.stopFriendliness );
		super.cleanUp();
				
		if( this.inOutSocketManager != null )
		{
			this.inOutSocketManager.stopThread( this.stopFriendliness );			
		}		
	}

	public void sendMessage( String msg )
	{
		//this.output.sendMessage( msg );
		try 
		{
			this.semStopSockAction.acquire();
		}
		catch (InterruptedException e) 
		{
		}
		
		if( this.inOutSocketManager != null )
		{			
			this.inOutSocketManager.sendMessage( msg );
		}
		
		if( this.semStopSockAction.availablePermits() < 1 )
		{
			this.semStopSockAction.release();
		}
	}
	
	@Override
	protected void runExceptionManager(Throwable e) 
	{
		if( !( e instanceof InterruptedException ) )
		{
			super.runExceptionManager( e );
		}
	}
	
	@Override
	public synchronized void taskDone( INotificationTask task ) throws Exception 
	{		
		List< EventInfo > events = task.getResult( true );		
		synchronized( events )
		{	
			synchronized( this.responses)
			{
				String ID = task.getID();
				
				for( EventInfo info : events )
				{		
					if( info.getEventType().equals( EventType.SOCKET_INPUT_MSG ) )
					{
						this.responses.add( new EventInfo( this.getName(),info.getEventType(), info.getEventInformation() ) );			
					}
					else if( info.getEventType().equals( EventType.SOCKET_CHANNEL_CLOSE )
							 || info.getEventType().equals( EventType.SOCKET_CONNECTION_PROBLEM ) 
							 || info.getEventType().equals( EventType.THREAD_STOP ) )
					{
						try
						{
							this.semStopSockAction.acquire();
						}
						catch( InterruptedException ex )
						{}
						
						//System.out.println("TCPClientSocketThread.taskDone() " + info.getEventType() + " > " + this.getName());
						
						if( this.inOutSocketManager != null )
						{
							this.inOutSocketManager.stopThread( IStoppableThread.FORCE_STOP );
							this.inOutSocketManager = null;
						}
						
						if( this.semStopSockAction.availablePermits() < 1 )
						{
							this.semStopSockAction.release();
						}
						
						if( this.client != null )
						{
							this.client.close();
							this.client = null;	
						}
						
						this.stopThread( IStoppableThread.FORCE_STOP );						
					}
				}
			}
			
			synchronized ( this ) 
			{
				super.notify();
			}
		}

		task.clearResult();		
	}
	
}
