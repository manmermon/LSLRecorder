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

import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.ArrayList;
import java.util.List;

import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskMonitor;
import Auxiliar.Tasks.NotifierThread;
import Controls.Messages.EventInfo;
import Controls.Messages.EventType;
import Prototype.Discarded.Malfunction.Socket.IClosedSocketMonitor;
import Prototype.Discarded.Malfunction.Socket.checkStreamTCPSocket;
import Sockets.Info.SocketSetting;
import Sockets.Info.StreamSocketProblem;
import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;

public abstract class TemplateReadWriteSocketThread  extends AbstractStoppableThread implements INotificationTask, IClosedSocketMonitor
{
	private ITaskMonitor monitor = null;
	protected NotifierThread monitorThread = null;
	
	protected List< EventInfo > events = null;
	
	protected AbstractSelectableChannel socket = null;
	
	protected SocketSetting localSocketInfo = null;
	protected SocketSetting remoteSocketInfo = null;

	protected checkStreamTCPSocket checkTCPConnectionState = null;
	
	protected int numEvents = 0;
	
	private boolean reportClosedByInterruptexception = true;
		
	public TemplateReadWriteSocketThread( AbstractSelectableChannel channel )  throws Exception 
	{
		if( channel == null )
		{
			throw new IllegalArgumentException( "Socket null." );
		}
				
		this.socket = channel;
		
		String localIP, remoteIP;
		int localPort, remotePort;
		int protocol = SocketSetting.TCP_PROTOCOL;
		
		if( channel instanceof SocketChannel )
		{
			localIP = ((SocketChannel) channel).socket().getLocalAddress().getHostAddress();
			remoteIP = ((SocketChannel) channel).socket().getInetAddress().getHostAddress();
			
			localPort = ((SocketChannel) channel).socket().getLocalPort();
			remotePort = ((SocketChannel) channel).socket().getPort();			
			
			this.checkTCPConnectionState = new checkStreamTCPSocket( ((SocketChannel)this.socket).socket(), this );
			
			this.remoteSocketInfo = new SocketSetting( protocol, remoteIP, remotePort );
		}
		else //if( channel instanceof DatagramChannel )
		{
			protocol = SocketSetting.UDP_PROTOCOL;
			localPort = ((DatagramChannel) channel).socket().getLocalPort();						
			localIP = ((DatagramChannel) channel).socket().getLocalAddress().getHostAddress();
			
			try
			{
				remotePort = ((DatagramChannel) channel).socket().getPort();
				remoteIP = ((DatagramChannel) channel).socket().getInetAddress().getHostAddress();
				
				this.remoteSocketInfo = new SocketSetting( protocol, remoteIP, remotePort );
			}
			catch( Exception e )
			{				
			}
		}
		
		this.localSocketInfo = new SocketSetting( protocol, localIP, localPort );		
		
		super.setName( this.localSocketInfo + "-" + this.remoteSocketInfo );
		
		this.events = new ArrayList<EventInfo>();
	}
		
	
	public SocketSetting getLocalSocketInfo()
	{
		return this.localSocketInfo;
	}
	
	public SocketSetting getRemoteSocketInfo()
	{
		return this.remoteSocketInfo;
	}
	
	/*
	 * (non-Javadoc)
	 * @see Auxiliar.Tasks.INotificationTask#taskMonitor(Auxiliar.Tasks.ITaskMonitor)
	 */
	@Override
	public void taskMonitor( ITaskMonitor m ) 
	{
		if( this.monitorThread != null )
		{
			throw new IllegalThreadStateException( "Thread is working." );
		}
			
		this.monitor = m;
	}
	
	public void setReportClosedSocketByInterruptException( boolean flag )
	{
		this.reportClosedByInterruptexception = flag;
	}
	
	/*
	 * (non-Javadoc)
	 * @see Auxiliar.Tasks.INotificationTask#getResult()
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

	/*
	 * (non-Javadoc)
	 * @see Auxiliar.Tasks.INotificationTask#clearResult()
	 */
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
			
			if( super.stopThread )
			{
				this.monitorThread.stopThread( IStoppableThread.STOP_IN_NEXT_LOOP );
			}
		}
	}
	
	@Override
	protected void preStart() throws Exception 
	{
		String suffix = "-" + this.getClass().getCanonicalName() + ": " + this.getName();
		if( this.checkTCPConnectionState != null )
		{
			this.checkTCPConnectionState.setName( this.checkTCPConnectionState.getClass().getCanonicalName() + suffix);
			this.checkTCPConnectionState.startThread();
		}
		
		this.monitorThread = new NotifierThread( this.monitor, this );
		this.monitorThread.setName( this.monitorThread.getClass().getCanonicalName() + suffix );
		this.monitorThread.startThread();
	}
	
	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{
		if( (friendliness == IStoppableThread.STOP_WITH_TASKDONE 
				|| friendliness == IStoppableThread.STOP_IN_NEXT_LOOP )
				&& checkIsWait() )
		{
			try
			{
				super.interrupt();
			}
			catch( SecurityException e )
			{		
				//e.printStackTrace();
			}
		}
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{
		synchronized ( this.events )
		{
			this.events.add( new EventInfo( EventType.THREAD_STOP, null ) );
		}
				
		synchronized ( this.monitorThread )
		{	
			if( this.events.size() > 0 )
			{
				this.monitorThread.notify();		
			}
		}
		
		//this.monitorThread.stopThread( IStoppableThread.StopInNextLoopInteraction );
		
		if( this.checkTCPConnectionState != null )
		{
			this.checkTCPConnectionState.stopThread( IStoppableThread.STOP_IN_NEXT_LOOP );
		}
		
		//this.socket.close();
		
		//this.monitorThread = null;
		this.checkTCPConnectionState = null;
		this.socket = null;
	}
	
	@Override
	public void closedConnection() 
	{
		this.stopThread( IStoppableThread.FORCE_STOP );
	}
	
	@Override
	protected void runExceptionManager(Exception e) 
	{	
		this.stopThread = true;
		
		boolean report = !(e instanceof InterruptedException ) 
							&&( !( e instanceof ClosedByInterruptException)
									|| this.reportClosedByInterruptexception );
		
		if( report )
		{			
			e.printStackTrace();
			synchronized ( this.events )
			{
				StreamSocketProblem problem = new StreamSocketProblem( null, e );
				if( this.remoteSocketInfo != null )
				{
					problem = new StreamSocketProblem( this.remoteSocketInfo.getSocketAddress(), e );
				}

				this.events.add( new EventInfo( EventType.SOCKET_CONNECTION_PROBLEM, problem ) );
			}
			
			/* It is not necessary. CleanUp() will report to monitor
			synchronized ( this.monitorThread )
			{
				if( this.events.size() > 0 )
				{
					this.monitorThread.notify();
				}
			}
			*/
		}	
	}
	
	protected abstract boolean checkIsWait();
	
	protected abstract void runInLoop() throws Exception;
}
