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
package lslrec.plugin.lslrecPlugin.sync;

import java.util.List;
import java.util.Map;

import lslrec.auxiliar.tasks.IMonitoredTask;
import lslrec.auxiliar.tasks.ITaskIdentity;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.auxiliar.tasks.NotificationTask;
import lslrec.config.Parameter;
import lslrec.controls.messages.EventInfo;
import lslrec.controls.messages.EventType;
import lslrec.dataStream.sync.SyncMarker;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;

public abstract class LSLRecPluginSyncMethod extends AbstractStoppableThread
												implements IMonitoredTask, ITaskIdentity
{
	private ITaskMonitor monitor = null;
	private NotificationTask notifier = null;
	
	protected Map< String, Parameter< String > > pars;
		
	@Override
	public final void taskMonitor( ITaskMonitor monitor ) 
	{	
		this.monitor = monitor;
	}
	
	@Override
	protected final void preStart() throws Exception 
	{		
		super.preStart();
		
		if( this.monitor == null )
		{
			throw new NullPointerException( "Task monitor mull.");
		}
		
		this.notifier = new NotificationTask( false );
		this.notifier.taskMonitor( this.monitor );

		this.notifier.startThread();
	}
	
	@Override
	public final void start() 
	{
		super.start();
	}
	 
	@Override
	public final synchronized void startThread() throws Exception 
	{
		super.startThread();
	}
	
	@Override
	public final synchronized void run() 
	{
		super.run();
	}
	
	@Override
	protected final void runInLoop() throws Exception
	{
		synchronized ( this )
		{
			super.wait();
		}
		
		SyncMarker marker = this.getSyncMarker();
		
		if( this.notifier != null && marker != null )
		{
			this.notifier.addEvent( new EventInfo( this.getID(), EventType.INPUT_MARK_READY, marker ) );
			synchronized ( this.notifier )
			{
				this.notifier.notify();
			}			
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
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
		
		if( this.notifier != null )
		{
			this.notifier.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
			this.notifier = null;
		}
	}
	
	protected void wakeUpSyncMarkerReady()
	{
		synchronized ( this )
		{
			super.notify();
		}
	}
	
	public abstract void loadSyncSettings( List< Parameter< String > > pars );
	
	protected abstract SyncMarker getSyncMarker( );
}
