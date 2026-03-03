/* 
 * Copyright 2018-2019 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.auxiliar.thread.timer;

import javax.swing.event.EventListenerList;

import lslrec.auxiliar.thread.timer.event.ActionTimerEvent;
import lslrec.auxiliar.thread.timer.event.ActionTimerEvent.Type;
import lslrec.auxiliar.thread.timer.event.IActionTimerEventListener;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;

/**
 * @author Manuel Merino Monge
 *
 */
public class Timer extends AbstractStoppableThread 
{
	private EventListenerList listenerList;

	private long time = 1000L;
	private ActionTimerThread action = null;
	
	private Object lock = new Object();
	
	private boolean pause = false;
	
	private boolean loop = true;
	
	private long refTime = 0L; 
	
	private Boolean running = false;
		
	/**
	 * 
	 */	
	public Timer( long time, boolean rep, ActionTimerThread act ) 
	{
		super();
		
		this.time = time;
		this.action = act;
		this.loop = rep;
		
		this.listenerList = new EventListenerList();
		
		super.setName( super.getName() + "-" + super.getId());
	}
	
	public long getTime()
	{
		return this.time;
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
	protected void preStart() throws Exception 
	{
		if( this.action != null )
		{
			this.action.startThread();
		}
	}
	
	@Override
	protected void startUp() throws Exception 
	{
		super.startUp();
		
		this.fireActionTimerEvent( ActionTimerEvent.Type.START, 0L );
	}
	
	@Override
	protected void runInLoop() throws Exception 
	{
		synchronized( this )
		{
			synchronized( this.running )
			{
				this.running = true;
			}
			
			this.updateTimeReference();
			this.wait( this.time );
		}
		
		if( this.action != null )
		{			
			this.action.execute();
		}
		
		this.fireActionTimerEvent( ActionTimerEvent.Type.ACTION, this.time );
	}
		
	@Override
	protected void targetDone() throws Exception 
	{
		super.targetDone();
		
		if( !this.loop )
		{
			//super.stopThread = true;
			this.pause = true;
		}
	}
	
	@Override
	protected void runExceptionManager(Throwable e) 
	{
		if( !(e instanceof InterruptedException ) )
		{
			super.runExceptionManager( e );
		}
	}
			
	@Override
	protected void finallyManager() 
	{
		if( this.pause )
		{
			synchronized( this )
			{
				synchronized( this.running )
				{
					this.running = false;
				}
				
				try 
				{
					super.wait();
				}
				catch (InterruptedException e) 
				{
				}
			}
		}
		else
		{
			super.finallyManager();
		}
	}
	
	public void restartTimer()
	{
		synchronized( this.lock )
		{
			long consumedTime = 0;
			
			if( this.getState().equals( Thread.State.NEW ) )
			{
				try 
				{
					super.startThread();
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
			else
			{
				consumedTime = this.getConsumedTime();
				this.pause = false;
				
				this.updateTimeReference();
				
				if( this.action != null )
				{
					this.action.interrupt();
				}
				super.interrupt();
			}
			
			this.fireActionTimerEvent( Type.RESTART, consumedTime );
		}		
	}
	
	public void stopTimer()
	{
		synchronized( this.lock )
		{
			this.pause = true;
			
			long consumedTime = this.getConsumedTime();
			this.updateTimeReference();
		
			super.interrupt();
			
			this.fireActionTimerEvent( Type.STOP, consumedTime );
		}
	}
	
	public boolean isRunning()
	{
		synchronized( this.running )
		{
			return this.running;
		}
	}
	
	private void updateTimeReference()
	{
		this.refTime = System.currentTimeMillis();
	}
	
	public void destroyTimer()
	{
		synchronized( this.lock )
		{
			super.stopThread( IStoppableThread.FORCE_STOP );
		}
	}
	
	public long getConsumedTime()
	{
		return (System.currentTimeMillis() - this.refTime);
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{	
		super.cleanUp();
		
		if( this.action != null )
		{
			this.action.stopThread( IStoppableThread.FORCE_STOP );
		}
		
		this.fireActionTimerEvent( Type.DESTROY, this.getConsumedTime() );
		this.updateTimeReference();
	}
	
	public void addActionTimerEventListener( IActionTimerEventListener listener) 
	{
		this.listenerList.add( IActionTimerEventListener.class, listener );
	}
	
	public void removeActionTimerEventListener( IActionTimerEventListener listener) 
	{
		this.listenerList.remove( IActionTimerEventListener.class, listener );
	}
	
	private synchronized void fireActionTimerEvent( ActionTimerEvent.Type typeEvent, long consumedTime )
	{
		ActionTimerEvent event = new ActionTimerEvent( this, typeEvent, consumedTime );

		IActionTimerEventListener[] listeners = this.listenerList.getListeners( IActionTimerEventListener.class );

		for (int i = 0; i < listeners.length; i++ ) 
		{
			listeners[ i ].StateChanged( event );
		}
	}
}
