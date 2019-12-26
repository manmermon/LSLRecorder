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
package DataStream.OutputDataFile.Format;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskIdentity;
import Auxiliar.Tasks.ITaskMonitor;
import Auxiliar.Tasks.NotificationTask;
import Auxiliar.Tasks.BridgeNotifierThread;
import Auxiliar.Tasks.IMonitoredTask;
import Controls.Messages.EventInfo;
import Controls.Messages.EventType;
import DataStream.OutputDataFile.IOutputDataFileWriter;
import DataStream.OutputDataFile.DataBlock.DataBlock;
import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;
import edu.ucsd.sccn.LSL;

public abstract class OutputFileWriterTemplate extends AbstractStoppableThread implements IOutputDataFileWriter, IMonitoredTask, ITaskIdentity //INotificationTask//, ITaskMonitor
{	
	protected final static int BYTE_TYPE = LSL.ChannelFormat.int8;
	protected final static int SHORT_TYPE = LSL.ChannelFormat.int16;
	protected final static int INT_TYPE = LSL.ChannelFormat.int32;
	protected final static int LONG_TYPE = LSL.ChannelFormat.int64;
	protected final static int FLOAT_TYPE = LSL.ChannelFormat.float32;
	protected final static int DOUBLE_TYPE = LSL.ChannelFormat.double64;
	protected final static int STRING_TYPE = LSL.ChannelFormat.string;
	
	protected String fileName;
	
	protected RandomAccessFile fStream = null;
	
	protected ITaskMonitor monitor = null;
	
	private NotificationTask notifTask = null;
	
	//private List< EventInfo > events = null;
		
	//private BridgeNotifierThread notifier = null;
	
	private int maxNumProcessors = 1;
	
	private AtomicInteger counterProcessingDataBlocks = null;
	
	public OutputFileWriterTemplate( String file ) throws Exception 
	{	
		//this.events = new ArrayList< EventInfo >();
		
		this.fileName = file;
		this.fStream = new RandomAccessFile( new File( file ), "rw" );		
		
		this.maxNumProcessors = this.getMaxNumThreads();
		
		if( this.maxNumProcessors < 1 )
		{
			this.maxNumProcessors = 1;
		}
		
		this.counterProcessingDataBlocks = new AtomicInteger( this.maxNumProcessors );	
	}
	
	@Override
	public void taskMonitor( ITaskMonitor m )
	{
		/*
		if( this.getState().equals( Thread.State.NEW ) )
		{
			this.monitor = m;	
			
			this.notifier = new BridgeNotifierThread( this.monitor, this );
		}
		*/
		
		if( this.getState().equals( Thread.State.NEW ) && this.notifTask == null )
		{
			synchronized ( this ) 
			{
				this.monitor = m;
			}
		}
	}
	
	@Override
	protected void preStart() throws Exception 
	{
		super.preStart();
		
		/*
		if( this.notifier != null )
		{
			this.notifier.startThread();
		}
		*/
		
		synchronized ( this ) 
		{
			if( this.monitor != null )
			{
				this.notifTask = new NotificationTask();
				this.notifTask.taskMonitor( this.monitor );
				this.notifTask.setName( this.notifTask.getID() + "-" + this.getID() );
				this.notifTask.startThread();
			}
		}
		
	}
	
	/*
	@Override
	public List< EventInfo > getResult( boolean clear )
	{
		List< EventInfo > copy = new ArrayList< EventInfo>( );
		
		synchronized ( this.events )
		{
			copy.addAll( this.events );
			
			if( clear )
			{
				this.events.clear();
			}
		}
				
		return copy;
	}
		
	@Override
	public void clearResult()
	{
		synchronized ( this.events )
		{
			this.events.clear();
		}		
	}
	*/
		
	@Override
	public String getID()
	{
		return this.getName();
	}
		
	@Override
	final public boolean saveData( DataBlock data) throws Exception
	{	
		boolean add = false;
		
		synchronized ( this )
		{
			add = this.counterProcessingDataBlocks.get() > 0;
			
			if( add )
			{			
				add = add && this.DataBlockManager( data );
				
				this.counterProcessingDataBlocks.decrementAndGet();				
				
				super.notify();			
			}
		}
		
		return add;
	}
	
	@Override
	public boolean isReady() 
	{
		synchronized ( this.counterProcessingDataBlocks )
		{
			return this.counterProcessingDataBlocks.get() > 0 ;
		}		
	}
	
	@Override
	public String getFileName() 
	{
		return this.fileName;
	}

	/*
	@Override
	final synchronized public void closeWriter() throws Exception 
	{
		super.stopThread( STOP_WITH_TASKDONE );			
	}
	*/
		
	@Override
	protected void runInLoop() throws Exception 
	{	
		synchronized ( this )
		{
			if( !this.DataBlockAvailable() )
			{
				try
				{
					super.wait();			
				}
				catch( InterruptedException e)
				{					
				}
			}		
		}
						
		this.ProcessDataBlock();	
		
		//this.CleanDataBlock();		
	}
	
	@Override
	final protected void postStopThread(int friendliness) throws Exception 
	{
		/*
		synchronized ( this )
		{
			System.out.println("OutputFileWriterTemplate.postStopThread() " + super.stopThread + " - " + super.stopWhenTaskDone.get() );
			if( this.getState().equals( Thread.State.WAITING ) )
			{
				super.notify();
			}
		}
		*/
	}
	
	@Override
	protected void targetDone() throws Exception
	{	
		if( this.wasDataBlockProcessed() )
		{
			synchronized ( this.counterProcessingDataBlocks )
			{
				this.counterProcessingDataBlocks.incrementAndGet();
				
				if( this.counterProcessingDataBlocks.get() > this.maxNumProcessors )
				{
					this.counterProcessingDataBlocks.set( this.maxNumProcessors );
				}
			}
			
			if( this.counterProcessingDataBlocks.get() == 1 )
			{	
				boolean notify = true;
				
				String evType = EventType.OUTPUT_FILE_WRITER_READY;
				
				/*
				synchronized ( this.events )
				{					
					for( EventInfo ev : this.events )
					{
						if( ev.getEventType().equals( evType ) )
						{
							notify = false;
							break;
						}
					}
					
					if( notify )
					{					
						EventInfo e = new EventInfo( this.getID(), evType, null );
						
						this.events.add( e );
					}
				}
				
				if( notify )
				{
					this.Notifier();
				}
				*/
				
				if( this.notifTask != null )
				{
					synchronized ( this.notifTask )
					{					
						for( EventInfo ev : this.notifTask.getResult( false ) )
						{
							if( ev.getEventType().equals( evType ) )
							{
								notify = false;
								break;
							}
						}
						
						if( notify )
						{					
							EventInfo e = new EventInfo( this.getID(), evType, null );
							this.notifTask.addEvent( e );
						}
					}
					
					if( notify )
					{
						this.Notifier();
					}
				}
			}
		}		
				
		synchronized ( this )
		{
			if( super.stopWhenTaskDone.get() && this.finished() )
			{
				super.stopThread = true;
			}
		}
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{
		try
		{
			this.CloseWriterActions();
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
		finally 
		{			
			this.fStream.close();
			this.fStream = null;
			
			/*
			this.notifier.stopThread( IStoppableThread.STOP_IN_NEXT_LOOP );
			
			if( this.notifier != null )
			{
				synchronized ( this.events )
				{
					EventInfo e = new EventInfo( this.getID(), EventType.THREAD_STOP, null );
					this.events.add( e );
				}						

				this.Notifier();
			}
			*/
			
			if( this.notifTask != null )
			{
				EventInfo e = new EventInfo( this.getID(), EventType.THREAD_STOP, null );
				
				this.notifTask.addEvent( e );
				this.notifTask.stopThread( IStoppableThread.STOP_IN_NEXT_LOOP );
				
				this.Notifier();
			}
		}
	}

	private void Notifier()
	{
		Thread t = new Thread()
		{
			@Override
			public synchronized void run() 
			{
				if( notifTask != null )
				{
					synchronized ( notifTask )
					{
						notifTask.notify();
					}
				}
			}
		};
		
		t.setName( "AntiDeadlockNotifierThread" );
		
		t.start();
	}
	
	protected abstract int getMaxNumThreads();
	
	/**
	 * 
	 * @param data -> data block to store.
	 * @return true if data is queued. Otherwise, false
	 * @throws Exception
	 */
	protected abstract boolean DataBlockManager( DataBlock data ) throws Exception;
	
	protected abstract boolean DataBlockAvailable();
	
	protected abstract void ProcessDataBlock() throws Exception;
	
	protected abstract boolean wasDataBlockProcessed(); 
	
	//protected abstract void CleanDataBlock();
	
	protected abstract void CloseWriterActions()  throws Exception;
}
