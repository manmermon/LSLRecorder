/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.  https://github.com/manmermon/LSLRecorder
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
package lslrec.sockets;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.icmp4j.IcmpPingRequest;
import org.icmp4j.IcmpPingResponse;
import org.icmp4j.IcmpPingUtil;

import lslrec.auxiliar.task.ITaskMonitor;
import lslrec.auxiliar.task.NotificationTask;
import lslrec.control.message.EventInfo;
import lslrec.control.message.EventType;
import lslrec.dataStream.sync.SyncMarker;
import lslrec.sockets.info.StreamInputMessage;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;

public class SocketMessageDelayCalculator extends AbstractStoppableThread 
{
	public static final int DEFAULT_NUM_PINGS = 4;

	private int numberOfPings = 4;
	
	private ConcurrentLinkedQueue< StreamInputMessage > msgList = null;
	
	private StreamInputMessage currentMsg = null;
	
	private NotificationTask notifier = null;
	
	private ITaskMonitor monitor = null;
	
	private ConcurrentHashMap< String, Integer > regMsgs = null;
	
	public SocketMessageDelayCalculator( int numPings ) 
	{
		this.msgList = new ConcurrentLinkedQueue< StreamInputMessage >();
		
		if( numPings > 0 )
		{
			this.numberOfPings = numPings;
		}
				
		this.regMsgs = new ConcurrentHashMap< String, Integer >(); 
		super.setName( super.getClass().getSimpleName() );
	}
	
	public void CalculateMsgDelay( StreamInputMessage msg )
	{
		final SocketMessageDelayCalculator cal = this;
		Thread t = new Thread()
		{
			@Override
			public synchronized void run() 
			{
				if( regMsgs.containsKey( msg.getMessage() ) )
				{
					msgList.add( msg );
					
					synchronized ( cal )
					{
						cal.notify();
					}
				}
			}
		};
		t.setName( "CalculateMsgDelay-" +t.getId() );
		
		t.start();		
	}
	
	public void clearInputMessages()
	{
		this.regMsgs.clear();		
	}
	
	public void AddInputMessages( Map< String, Integer > msgs )
	{		
		if( msgs != null  )
		{
			this.regMsgs.putAll( msgs );
		}
	}
	
	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{	
	}

	@Override
	protected void preStart() throws Exception 
	{
		super.preStart();
		
		synchronized ( this )
		{
			if( this.monitor != null )
			{
				this.notifier = new NotificationTask( false );
				this.notifier.setID( this.notifier.getID() + "-" + this.getID() );
				this.notifier.setName( this.notifier.getID() );
				this.notifier.taskMonitor( this.monitor );
				this.notifier.startThread();
			}
		}		
	}
	
	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{	
		synchronized ( this )
		{
			if( friendliness != IStoppableThread.FORCE_STOP 
					&& super.getState().equals( Thread.State.WAITING ) )
			{
				this.notify();
			}
		}
	}

	@Override
	protected void runInLoop() throws Exception 
	{
		synchronized ( this )
		{
			if( this.msgList.isEmpty() )
			{
				try
				{
					this.wait();
				}
				catch (Exception e) 
				{					
				}
			}			
		}
		
		boolean continued = true;
				
		do
		{			
			this.currentMsg = this.msgList.poll();
			
			continued = ( this.currentMsg != null );
					
			if( continued )
			{				
				Integer Mark = this.regMsgs.get( this.currentMsg.getMessage() );
				
				double time = this.currentMsg.receivedTime();
			
				double rtt = this.pingDuration( this.currentMsg.getOrigin(), this.numberOfPings );

				if( rtt != Double.NaN 
						&& rtt != Double.POSITIVE_INFINITY
						&& rtt != Double.NEGATIVE_INFINITY )
				{
					time = time - rtt/ 2;
				}

				if( this.notifier != null )
				{
					EventInfo ev = new EventInfo( super.getName(), EventType.INPUT_MARK_READY, new SyncMarker( Mark, time ) );
					
					this.notifier.addEvent( ev );
					synchronized ( this.notifier )
					{
						this.notifier.notify();
					}
				}
			}
		}
		while( continued );
	}
		
	@Override
	protected void runExceptionManager( Throwable e) 
	{
		if( !( e instanceof InterruptedException ) )
		{
			e.printStackTrace();
						
			if( this.notifier != null && this.currentMsg != null)
			{
				double time = System.nanoTime() / 1e9D;
				Integer Mark = this.regMsgs.get( this.currentMsg.getMessage() );
				
				time = this.currentMsg.receivedTime();				
				EventInfo ev = new EventInfo( super.getName(), EventType.INPUT_MARK_READY, new SyncMarker( Mark, time ) );

				this.notifier.addEvent( ev );
				
				synchronized ( this.notifier ) 
				{
					this.notifier.notify();
				}				
			}
		}
	}

	@Override
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
				
		if( this.notifier != null )
		{			
			EventInfo ev = new EventInfo( super.getName(), EventType.SOCKET_PING_END, this );
			this.notifier.addEvent( ev );
			
			synchronized ( this.notifier )
			{
				this.notifier.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
				this.notifier.notify();
			}
			
		}
	}
	
	public boolean isCalculating()
	{
		return !this.msgList.isEmpty();
	}
	
	private double pingDuration( InetSocketAddress ipAddress, int numPings ) throws IOException
	{		
		IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest();
		request.setHost( ipAddress.getHostString() );
		
		double time = Double.POSITIVE_INFINITY;
				
		for( int i = 0; i < numPings; i++ )
		{			
			final IcmpPingResponse response = IcmpPingUtil.executePingRequest (request);
			
			if ( response.getSuccessFlag() )
			{
				double t = response.getDuration() / 1e9D;
				
				if( t < time )
				{
					time = t;
				}
			}
		}
		
		double rtt = Double.NaN;
		
		if( time < Double.POSITIVE_INFINITY )
		{
			rtt = time;
		}
		
		return rtt;
	}

	public void taskMonitor( ITaskMonitor m ) 
	{
		if( super.getState().equals( Thread.State.NEW ) && this.notifier == null )
		{
			synchronized ( this )
			{
				this.monitor = m;
			}			
		}
	}


	public String getID() 
	{
		return this.getClass().getName();
	}
}
