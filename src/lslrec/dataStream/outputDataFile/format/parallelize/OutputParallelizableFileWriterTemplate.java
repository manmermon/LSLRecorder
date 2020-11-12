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
package lslrec.dataStream.outputDataFile.format.parallelize;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import lslrec.auxiliar.tasks.IMonitoredTask;
import lslrec.auxiliar.tasks.ITaskIdentity;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.auxiliar.tasks.NotificationTask;
import lslrec.controls.messages.EventInfo;
import lslrec.controls.messages.EventType;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlock;
import lslrec.dataStream.outputDataFile.format.IOutputDataFileWriter;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;

public abstract class OutputParallelizableFileWriterTemplate extends AbstractStoppableThread implements IOutputDataFileWriter, IMonitoredTask, ITaskIdentity //INotificationTask//, ITaskMonitor
{		
	protected ITaskMonitor monitor = null;
	
	private NotificationTask notifTask = null;
	
	private int maxNumProcessors = 1;
	
	private AtomicInteger counterProcessingDataBlocks = null;
	
	private AtomicBoolean isOpen = null;
	
	private AtomicBoolean isProcessing = null;
		
	public OutputParallelizableFileWriterTemplate( ) throws Exception 
	{			
		this.maxNumProcessors = this.getMaxNumThreads();
		
		if( this.maxNumProcessors < 1 )
		{
			this.maxNumProcessors = 1;
		}
		
		this.counterProcessingDataBlocks = new AtomicInteger( this.maxNumProcessors );	
		
		this.isOpen = new AtomicBoolean( true );

		this.isProcessing = new AtomicBoolean( false );
		
		super.setName( this.getClass().getSimpleName() + "-" + super.getId()  );
	}
	
	@Override
	public void taskMonitor( ITaskMonitor m )
	{		
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
		
		synchronized ( this ) 
		{
			if( this.monitor != null )
			{
				this.notifTask = new NotificationTask( false );
				this.notifTask.taskMonitor( this.monitor );
				this.notifTask.setName( this.notifTask.getID() + "-" + this.getID() );
				this.notifTask.startThread();
			}
		}
		
	}
			
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
			synchronized ( this.isOpen )
			{
				if( !this.isOpen.get() )
				{
					throw new IOException( "Output file is closed. " );
				}
			}
			
			synchronized ( this.counterProcessingDataBlocks )
			{
				add = this.counterProcessingDataBlocks.get() > 0;
			}			
			
			if( add )
			{			
				add = add && this.DataBlockManager( data );
					
				if( add )
				{
					this.counterProcessingDataBlocks.decrementAndGet();
				}
				
				super.notify();			
			}
		}
		
		return add;
	}
			
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
		
		synchronized ( this.isProcessing )
		{
			this.isProcessing.set( true );
		}
		
		this.ProcessDataBlock();
		
		synchronized ( this.isProcessing )
		{
			this.isProcessing.set( false );
		}
	}
	
	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{
		if( friendliness == IStoppableThread.STOP_WITH_TASKDONE )
		{
			synchronized ( this )
			{
				if( super.getState().equals( Thread.State.WAITING )
						|| super.getState().equals( Thread.State.TIMED_WAITING ) )
				{
					super.notify();
				}
			}
		}
	}
	
	@Override
	protected void targetDone() throws Exception
	{	
		if( this.wasDataBlockProcessed() )
		{
			int preIncCounter = 0;
			
			synchronized ( this.counterProcessingDataBlocks )
			{
				preIncCounter = this.counterProcessingDataBlocks.get();
				this.counterProcessingDataBlocks.incrementAndGet();
				
				if( this.counterProcessingDataBlocks.get() > this.maxNumProcessors )
				{
					this.counterProcessingDataBlocks.set( this.maxNumProcessors );
				}
			}
			
			if( this.counterProcessingDataBlocks.get() == 1 && preIncCounter == 0 )
			{	
				boolean notify = true;
				
				String evType = EventType.OUTPUT_FILE_WRITER_READY;
							
				if( this.notifTask != null )
				{
					EventInfo e = new EventInfo( this.getID(), evType, null );
					notify = this.notifTask.addEvent( e, true );
					
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
			try
			{
				this.close();
			}
			catch( Exception ex )
			{
				ex.printStackTrace();
			}
						
			this.CloseWriterActions();
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
		finally 
		{	
			if( this.notifTask != null )
			{
				EventInfo e = new EventInfo( this.getID(), EventType.THREAD_STOP, null );
				
				this.notifTask.addEvent( e );
				this.notifTask.stopThread( IStoppableThread.STOP_IN_NEXT_LOOP );
				
				this.Notifier();
			}
		}
	}
	
	@Override
	public void close() throws Exception 
	{
		synchronized ( this.isOpen )
		{
			this.isOpen.set( false );
		}
		
		boolean processing = false;
		
		synchronized ( this.isProcessing )
		{
			processing = this.isProcessing.get();
		}
		
		if( this.DataBlockAvailable()
				|| !processing )
		{
			super.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
		}
		else
		{
			super.stopThread( IStoppableThread.FORCE_STOP );
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
		
		t.setName( "AntiDeadlockNotifierThread-Notify" );
		
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
	
	protected abstract void CloseWriterActions()  throws Exception;
}
