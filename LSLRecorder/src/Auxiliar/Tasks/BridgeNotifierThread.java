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

package Auxiliar.Tasks;

import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;

/**
 * To notifier events.
 * 
 * @author Manuel Merino Monge
 *
 */
public class BridgeNotifierThread extends AbstractStoppableThread 
{
	private ITaskMonitor Monitor;
	private INotificationTask Task;
	
	private boolean isWait = false;
	private boolean wakeUpToStop = false;
	
	private boolean isRunning = false;
		
	/**
	 * Set monitor and the notified task. 
	 *  
	 * @param monitor 	-> monitor
	 * @param task 		-> notified task
	 * 
	 * @throws IllegalArgumentException -> an input is null.
	 */
	public BridgeNotifierThread( ITaskMonitor monitor, INotificationTask task ) throws IllegalArgumentException 
	{
		if( monitor == null || task == null )
		{
			throw new IllegalArgumentException( "Null input(s)." );
		}
		
		this.Monitor = monitor;
		this.Task = task;
		
		super.setName( this.getClass() + "<" + this.Monitor + ", " + this.Task + ">" );
	}
	
	/**
	 * Set monitor.
	 * 
	 * @param m -> monitor
	 * 
	 * @throws IllegalStateException -> If the NotifierThread is running.
	 */
	public void setMonitor( ITaskMonitor m ) throws IllegalStateException
	{
		if( this.isRunning )
		{
			throw new IllegalStateException( "Notifier thread is run." );
		}
		
		this.Monitor = m;
	}
	
	/**
	 * Set notified task
	 * 
	 * @param t -> notified task
	   
	 * @throws IllegalStateException -> If the NotifierThread is running.
	 */
	public void setNotifiedTask( INotificationTask t ) throws IllegalStateException
	{
		if( this.isRunning )
		{
			throw new IllegalStateException( "Notifier thread is run." );
		}
		
		this.Task = t;
	}
	
	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#preStart()
	 */
	@Override
	protected void preStart() throws Exception 
	{
		super.preStart();
		
		this.isRunning = true;
	}
	
	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{	
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{	
		if( ( friendliness == IStoppableThread.STOP_IN_NEXT_LOOP 
				|| friendliness == IStoppableThread.STOP_WITH_TASKDONE )
				&& this.isWait )
		{	
			synchronized ( this )
			{				
				//this.wakeUpToStop = true;
				super.notify();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#runInLoop()
	 */
	@Override
	protected void runInLoop() throws Exception 
	{
		this.isWait = true;
			
		synchronized( this )
		{
			try
			{
				super.wait();
			}
			catch( InterruptedException ex )
			{}
		}
		
		this.isWait = false;
		
		//if( !this.wakeUpToStop )
		//{
			try
			{
				this.Monitor.taskDone( this.Task ); // Notify to the monitor
			}
			catch( InterruptedException e )
			{			
			}
		//}
	}
	
	/*
	@Override
	protected void targetDone() throws Exception 
	{		
		super.targetDone();		
	}
	*/
	
	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#runExceptionManager(java.lang.Exception)
	 */
	@Override
	protected void runExceptionManager( Throwable e ) 
	{
		super.stopThread = true;
		e.printStackTrace();
	}

	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#cleanUp()
	 */
	@Override
	protected void cleanUp() throws Exception 
	{
		this.Monitor = null;
		this.Task = null;		
	}
}
