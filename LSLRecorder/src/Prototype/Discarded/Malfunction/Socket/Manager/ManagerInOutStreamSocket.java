/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec and CLIS.
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

package Prototype.Discarded.Malfunction.Socket.Manager;

import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.ArrayList;
import java.util.List;

import Auxiliar.Extra.Tuple;
import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskMonitor;
import Controls.Messages.EventInfo;
import Controls.Messages.EventType;
import Prototype.Discarded.Malfunction.Socket.streamingOutputMessage;
import Sockets.Info.SocketSetting;
import Sockets.Info.SocketParameters;
import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;

public class ManagerInOutStreamSocket extends AbstractStoppableThread implements INotificationTask, ITaskMonitor 
{
	private boolean isRunning = false;
	private boolean wakeUpToStop = false;
	
	private ITaskMonitor monitor = null;
	//private NotifierThread monitorThread = null;
	
	private List< EventInfo > events = new ArrayList<EventInfo>();
	private int numEvents = 0;
	
	private readSocketThread readThread = null;
	
	private writeSocketThread writeThread = null;
	
	private SocketSetting localSocket = null;
	
	private SocketSetting remoteSocket = null;	
	
	private int counterThreads = 0;
		
	private AbstractSelectableChannel inOutChannel = null;

	private boolean reportCloseByInterrutpExceptionReader = true;
	private boolean reportCloseByInterrutpExceptionWriter = true;
	
	/**
	 * Create a read and write socket threads.
	 * Do it nothing if this one just exists.
	 * @throws Exception 
	 */
	public ManagerInOutStreamSocket( AbstractSelectableChannel channel, int direction ) throws Exception 
	{
		if( direction != SocketParameters.SOCKET_CHANNEL_IN 
				&& direction != SocketParameters.SOCKET_CHANNEL_INOUT 
				&& direction != SocketParameters.SOCKET_CHANNEL_OUT )
		{
			throw new IllegalArgumentException( "Socket streaming direction unknown. See streamingParameters class." );
		}
		
		this.inOutChannel = channel;
		
		if( this.readThread == null 
				&& 
				( direction == SocketParameters.SOCKET_CHANNEL_IN 
					|| direction == SocketParameters.SOCKET_CHANNEL_INOUT ) )
		{
			this.counterThreads++;
			
			this.readThread = new readSocketThread( channel );
			this.readThread.taskMonitor( this );
			
			this.localSocket = this.readThread.getLocalSocketInfo();
			this.remoteSocket = this.readThread.getRemoteSocketInfo();			
		}
		
		if( this.writeThread == null 
				&& 
				( direction == SocketParameters.SOCKET_CHANNEL_OUT 
					|| direction == SocketParameters.SOCKET_CHANNEL_INOUT ) )
		{
			this.counterThreads++;
			
			this.writeThread = new writeSocketThread( channel );
			this.writeThread.taskMonitor( this );
			
			this.localSocket = this.writeThread.getLocalSocketInfo();
			this.remoteSocket = this.writeThread.getRemoteSocketInfo();	
		}
		
		super.setName( super.getClass().getName() + "<" + this.localSocket + "-" + this.remoteSocket + ">");
	}
	
	@Override
	public synchronized void startThread() throws Exception 
	{
		super.startThread();
		
		if( this.readThread != null )
		{
			this.readThread.setReportClosedSocketByInterruptException( this.reportCloseByInterrutpExceptionReader );
			this.readThread.startThread();
		}
		
		if( this.writeThread != null )
		{
			this.writeThread.setReportClosedSocketByInterruptException( this.reportCloseByInterrutpExceptionWriter );
			this.writeThread.startThread();
		}
	}
	
	@Override
	public synchronized void taskDone( INotificationTask task)  throws Exception
	{
		synchronized ( this.events ) 
		{
			List< EventInfo > eventList = new ArrayList<EventInfo>( task.getResult() );
			task.clearResult();
			
			for( EventInfo e : eventList  )
			{	
				if( !e.getEventType().equals( EventType.THREAD_STOP ) )
				{
					this.events.add( e );
				}
				else
				{
					this.counterThreads--;
					
					if( this.readThread != null 
							&& this.readThread.equals( task ) )
					{
						this.readThread = null;
					}
					else
					{
						this.writeThread = null;
					}
					
					if( this.counterThreads == 0 )
					{
						this.events.add( new EventInfo( EventType.THREAD_STOP, new Tuple< SocketSetting, SocketSetting >( this.localSocket, this.remoteSocket ) ) );
					}
				}
			}
		}
		
		if( this.events.size() > 0 )
		{
			synchronized ( this ) 
			{
				super.notify();
			}
		}
	}
	@Override
	public void taskMonitor( ITaskMonitor m ) 
	{	
		if( this.isRunning )
		{
			throw new IllegalThreadStateException( "Manager of In-out stream socket is working");
		}
		
		this.monitor = m;
	}
	
	public void setReportClosedSocketByInterruptExceptionReader( boolean flag )
	{
		this.reportCloseByInterrutpExceptionReader = flag;
	}

	public void setReportClosedSocketByInterruptExceptionWriter( boolean flag )
	{
		this.reportCloseByInterrutpExceptionWriter = flag;
	}
	
	/**
	 * Return a eventInfo where eventInformation is a
	 * list of eventInfo.
	 */
	@Override
	public List< EventInfo > getResult() 
	{
		synchronized ( this.events )
		{
			this.numEvents = this.events.size();
			
			return this.events;
		}		
	}

	@Override
	public void clearResult() 
	{
		synchronized ( this.events ) 
		{
			while( this.numEvents > 0 )
			{
				this.events.remove( 0 );
				this.numEvents--;
			}
		}				
	}

	public boolean existReadSocketThread()
	{
		return this.readThread != null;
	}
	
	public boolean existWriteSocketThread()
	{
		return this.writeThread != null;
	}
	
	public SocketSetting getLocalSocketInfo()
	{				
		return this.localSocket;
	}
	
	public SocketSetting getRemoteSocketInfo()
	{	
		return this.remoteSocket;
	}
	
	/**
	 * Send message to remote socket. 
	 * @param msg: String data to send.
	 * @param destinations: remote socket list (only for UDP).
	 */
	public void sendMessage( final streamingOutputMessage msg ) throws NullPointerException
	{
		if( this.writeThread != null )
		{
			this.writeThread.sendMessage( msg );
		}
	}
	
	public boolean isFinished()
	{
		return this.counterThreads < 1;
		//return this.writeThread == null	&& this.readThread == null;
	}
	
	public String toString() 
	{
		return this.getClass() + " -> " + this.localSocket + " - " + this.remoteSocket;
	}

	@Override
	protected void preStopThread( int friendliness ) throws Exception 
	{
		if( this.counterThreads > 0 )
		{			
			if( this.readThread != null )
			{
				this.readThread.stopThread( friendliness );
			}
			
			if( this.writeThread != null )
			{				
				this.writeThread.stopThread( friendliness );
			}
		}
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{
		if( (friendliness == IStoppableThread.STOP_WITH_TASKDONE 
				|| friendliness == IStoppableThread.STOP_IN_NEXT_LOOP )
				&& this.getState().equals( State.WAITING ) 
				&& this.isFinished() )
		{
			try
			{
				synchronized ( this ) 
				{
					this.wakeUpToStop = true;
					super.notify();	
				}
			}
			catch( SecurityException e )
			{		
			}
		}
	}

	@Override
	protected void runInLoop() throws Exception 
	{
		super.wait();
		
		if( !this.wakeUpToStop )
		{
			try
			{
				if( this.events.size() > 0 )
				{
					this.monitor.taskDone( this );
				}
			}
			catch( InterruptedException e )
			{			
			}
		}
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
		
		if( this.inOutChannel != null )
		{
			this.inOutChannel.close();
		}
	}

	@Override
	public String getID() 
	{
		return super.getName();
	}
}
