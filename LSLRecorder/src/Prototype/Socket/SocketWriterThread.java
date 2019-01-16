package Prototype.Socket;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import Auxiliar.Tasks.ITaskMonitor;
import Controls.Messages.EventInfo;
import Controls.Messages.eventType;
import Sockets.SocketReadWriteThreadTemplate;
import StoppableThread.IStoppableThread;

public class SocketWriterThread extends SocketReadWriteThreadTemplate
{
	private PrintWriter writer;
	
	private List< String > outMessage;
	
	private Semaphore writeSemaphore;
	
	
	public SocketWriterThread( Socket socket, ITaskMonitor monitor ) throws IllegalArgumentException, IOException
	{
		super( socket, monitor );
		
		this.writer = new PrintWriter( super.SOCKET.getOutputStream(), true );
		
		super.setName( "writer:" + super.getName() );
		
		this.writeSemaphore = new Semaphore( 1, true );
		
		this.outMessage = new ArrayList< String >();
	}	

	@Override
	protected synchronized void runInLoop() throws Exception 
	{	
		if( this.writeSemaphore.tryAcquire( ) )
		{
			super.wait();
		}
		
		synchronized ( this.outMessage ) 
		{		
			//System.out.print("SocketWriterThread.runInLoop() " + this.getName() + " Inputs: "  + System.nanoTime() + " > " ); 
			for( String msg : this.outMessage )
			{
				//System.out.print( msg + " ");
				this.writer.println( msg );
			}
			//System.out.println();
			
			this.outMessage.clear();
		}
	}	
	
	@Override
	protected void targetDone() throws Exception 
	{
		super.targetDone();
				
		if( this.writeSemaphore.availablePermits() < 1 )
		{
			this.writeSemaphore.release( );
		}
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{
		//System.out.println("SocketWriterThread.cleanUp() " + super.getName() );
		super.cleanUp();
		
		synchronized ( super.events ) 
		{
			super.events.add( new EventInfo( eventType.SOCKET_CHANNEL_CLOSE, super.SOCKET ) );
		}
		
		synchronized ( this.notifier )
		{
			if( this.events.size() > 0 )
			{
				//System.out.println("SocketWriterThread.cleanUp() " + getName());
				this.notifier.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
				//this.notifier.notify();
				//this.notifier.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
			}
			else
			{
				//System.out.println("SocketWriterThread.cleanUp() NOTI FORCE STOP ");
				
				this.notifier.stopThread( IStoppableThread.FORCE_STOP );
			}
		}
		
		synchronized ( this.writer ) 
		{
			this.writer.close();
			this.writer = null;
		}
	}

	public synchronized void sendMessage( String msg )
	{
		synchronized ( this.outMessage )
		{	
			this.outMessage.add( msg.trim() );			
		}				
		
		if( !this.writeSemaphore.tryAcquire( ) )
		{
			this.notify();
		}
	}
	
	@Override
	public String getID() 
	{
		return this.getName();
	}
}
