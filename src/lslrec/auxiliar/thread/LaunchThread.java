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
 *
 */
package lslrec.auxiliar.thread;

import java.util.ArrayList;
import java.util.List;

import lslrec.auxiliar.task.INotificationTask;
import lslrec.auxiliar.task.ITaskMonitor;
import lslrec.control.message.EventInfo;
import lslrec.control.message.EventType;
import lslrec.stoppableThread.AbstractStoppableThread;

public class LaunchThread extends AbstractStoppableThread implements INotificationTask 
{
	private ITaskMonitor monitor;
	private List< EventInfo > events = null;
	private AbstractStoppableThread thr;	
	
	public LaunchThread( AbstractStoppableThread t ) 
	{
		this.events = new ArrayList<EventInfo>();
		this.thr = t;
	}
	
	@Override
	public void taskMonitor( ITaskMonitor m ) 
	{
		this.monitor = m;
	}

	@Override
	public List<EventInfo> getResult( boolean clear ) 
	{
		List< EventInfo > evs = new ArrayList< EventInfo >();
		
		synchronized ( this.events )
		{
			evs.addAll( this.events );
			
			if( clear )
			{
				this.events.clear();
			}
		}
		
		return evs;
	}

	@Override
	public void clearResult() 
	{
		synchronized ( this.events)
		{
			this.events.clear();
		}		
	}

	@Override
	public String getID() 
	{
		return super.getClass().getSimpleName();
	}

	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{		
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{	
	}
	
	@Override
	protected void runInLoop() throws Exception 
	{	
		if( this.thr != null && this.thr.getState().equals( Thread.State.NEW ) )
		{
			this.thr.startThread();		
		}
	}
	
	@Override
	protected void targetDone() throws Exception 
	{
		super.stopThread = true;
	}

	@Override
	protected void runExceptionManager( Throwable e ) 
	{
		EventInfo ev = new EventInfo( this.getID(), EventType.PROBLEM, e );
		
		this.events.add( ev );
		
		if( this.monitor != null )
		{
			try 
			{
				this.monitor.taskDone( this );
			}
			catch (Exception e1) 
			{
				e1.printStackTrace();
			}
		}
	}
}
