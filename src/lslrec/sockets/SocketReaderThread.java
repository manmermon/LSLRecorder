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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

import lslrec.auxiliar.task.ITaskMonitor;
import lslrec.control.message.EventInfo;
import lslrec.control.message.EventType;
import lslrec.sockets.info.StreamInputMessage;
import lslrec.stoppableThread.IStoppableThread;

public class SocketReaderThread extends SocketReadWriteThreadTemplate 
{	
	private BufferedReader readBuf;
		
	//private String inMsg = "";
	
	private InetSocketAddress local;
	private InetSocketAddress remote;
		
	public SocketReaderThread( Socket socket, ITaskMonitor monitor ) throws IllegalArgumentException, IOException
	{
		super( socket, monitor );
				
		this.readBuf = new BufferedReader( new InputStreamReader( super.SOCKET.getInputStream() ) );
		
		super.setName( "reader:" + super.getName() );
	}
	
	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{
		if( friendliness == IStoppableThread.FORCE_STOP )
		{
			this.SOCKET.close();		
			
		}
	}
	
	@Override
	protected void startUp() throws Exception 
	{
		super.startUp();
		
		this.local = new InetSocketAddress( super.SOCKET.getLocalAddress(), super.SOCKET.getLocalPort() );
		this.remote = new InetSocketAddress( super.SOCKET.getInetAddress(), super.SOCKET.getPort() );
	}
	
	@Override
	protected void runInLoop() throws Exception 
	{
		String in = readBuf.readLine();
		
		if( in != null )
		{
			//this.inMsg = in;
			//System.out.println("SocketReaderThread.runInLoop() " + this.getName() + " Inputs: "  + System.nanoTime() + " > " + inMsg );
			
			StreamInputMessage inMsg  = new StreamInputMessage( in , this.remote, this.local );
			
			synchronized( super.events )
			{				
				super.events.add( new EventInfo( this.getID(), EventType.SOCKET_INPUT_MSG, inMsg ) );
			}
		}	
		else
		{
			super.stopThread( IStoppableThread.STOP_IN_NEXT_LOOP );
		}
	}
	
	@Override
	protected void targetDone() throws Exception 
	{
		super.targetDone();
		
		synchronized ( this.notifier )
		{
			if( !super.stopThread )
			{
				if( this.events.size() > 0 )
				{				
					this.notifier.notify();				
				}
			}			
			else
			{
				if( this.events.size() > 0 )
				{				
					this.notifier.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
				}
				else
				{
					this.notifier.stopThread( IStoppableThread.FORCE_STOP );
				}
			}			
		}		
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
		
		synchronized( this.readBuf )
		{		
			this.readBuf.close();
			this.readBuf = null;
		}
				
		synchronized ( super.events ) 
		{			
			super.events.add( new EventInfo( this.getID(), EventType.SOCKET_CHANNEL_CLOSE, this.getID() ) );
		}
		
		synchronized ( this.notifier )
		{
			if( this.events.size() > 0 )
			{				
				//System.out.println("SocketReaderThread.cleanUp() notifier STOP_WITH_TASKDONE");
				this.notifier.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
			}
			else
			{
				//System.out.println("SocketReaderThread.cleanUp() notifier FORCE");
				this.notifier.stopThread( IStoppableThread.FORCE_STOP );
			}		
		}		
		
		this.monitor = null;
	}
	
	@Override
	public String getID() 
	{
		return this.getName();
	}
}
