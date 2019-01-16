package Sockets;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;

import Auxiliar.Tasks.ITaskMonitor;
import Controls.Messages.EventInfo;
import Controls.Messages.eventType;
import Sockets.Info.streamInputMessage;
import StoppableThread.IStoppableThread;

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
			
			streamInputMessage inMsg  = new streamInputMessage( in , this.remote, this.local );
			
			synchronized( super.events )
			{				
				super.events.add( new EventInfo( eventType.SOCKET_INPUT_MSG, inMsg ) );
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
			super.events.add( new EventInfo( eventType.SOCKET_CHANNEL_CLOSE, this.getID() ) );
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
