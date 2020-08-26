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

package timers;

import auxiliar.tasks.INotificationTask;
import auxiliar.tasks.ITaskMonitor;
import stoppableThread.AbstractStoppableThread;

public class Timer extends AbstractStoppableThread implements ITimer, ITaskMonitor
{
	private ITimerMonitor monitor = null;
	
	//private List< Long > timerTimeList = new ArrayList< Long >();
	//private int timerIDx = 0;
	
	private long timerTime = 0L;
	private boolean stopTimer = false;
	private long timeSleep = 10L; //10 milisecond
	
	private long timerCount = 0;
	
	private boolean wait = true;
	private boolean interruption = false;
	private boolean isWorking = false;
			
	@Override
	public void setTimerMonitor( ITimerMonitor m ) 
	{
		this.monitor = m;
	}
	
	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#startUp()
	 */
	@Override
	protected void startUp() throws Exception
	{	
	}
	
	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#runInLoop()
	 */
	@Override
	protected void runInLoop() throws Exception  
	{	
		this.timerCount = System.currentTimeMillis();
		long delay = 0L;
		long tSleep = 0L;		
		this.isWorking = true;
		this.wait = true;
		
		//System.out.println((System.nanoTime()-coreControl.tiempo)+"-Timer.runInLoop() TIEMPO="+timerTime);
		while( (System.currentTimeMillis() - this.timerCount) < this.timerTime && !this.stopTimer && !this.interruption )
		{				
			tSleep = this.timeSleep - delay;
			if( tSleep > 0 )
			{
				super.sleep( tSleep );				
			}			
				
			delay = System.currentTimeMillis(); //To mesaure delay
							
			delay = System.currentTimeMillis() - delay;	
		}		
	}
	
	@Override
	protected void targetDone() throws Exception 
	{
		synchronized( super.stopWhenTaskDone )
		{
			if( super.stopWhenTaskDone.get() )
			{
				this.wait = false;
				super.stopThread = true;
			}
		}
		
		if( !this.stopTimer )
		{
			if( !this.interruption
					&& ( System.currentTimeMillis() - this.timerCount ) >= this.timerTime 
					&& this.timerTime > 0 )
			{
				
				this.monitor.timeOver( super.getName() );
			}
			
			this.interruption = false;
			
			if( this.wait )
			{
				this.isWorking = false;
				
				super.wait();
			}			
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#runExceptionManager(java.lang.Exception)
	 */
	@Override
	protected void runExceptionManager( Throwable e) 
	{
		if( !( e instanceof InterruptedException ) )
		{
			e.printStackTrace();
			super.stopThread = true;
		}
		else
		{
			//System.out.println((System.nanoTime()-coreControl.tiempo)+"-Timer.runExceptionManager()");
		}
	}
	
	public void interruptTimer()
	{
		if( this.isWorking )
		{
			this.interruption = true;
			super.interrupt();			
		}
	}

	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#preStopThread()
	 */
	@Override
	protected void preStopThread(int friendliness) 
	{	
		this.stopTimer = true;		
	}
	
	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#cleanUp()
	 */
	@Override
	protected void cleanUp()
	{
		this.isWorking = false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see Auxiliar.Tasks.ITaskMonitor#taskDone()
	 */
	@Override
	public void taskDone( INotificationTask t ) 
	{		
	}

	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#postStopThread()
	 */
	@Override
	protected void postStopThread(int friendliness) 
	{
		this.wait = false;
	}

	/*
	 * (non-Javadoc)
	 * @see Auxiliar.Timers.ITimer#resumeTimer()
	 */
	@Override
	public void restartTimer() 
	{			
		if( this.getState().equals( State.WAITING ) )
		{	
			synchronized( this )
			{				
				super.notify();
			}
		}
		else
		{
			super.interrupt();
		}		
	}
	
	/*
	 * (non-Javadoc)
	 * @see Auxiliar.Timers.ITimer#setTimerValue(int, long)
	 */
	@Override
	public void setTimerValue( long time) throws IllegalArgumentException 
	{
		this.timerTime = time;
	}
	
	/*
	 * (non-Javadoc)
	 * @see Auxiliar.Timers.ITimer#sumValueToTimer(int, long)
	 */
	@Override
	public void sumValueToTimer( long rise) throws IllegalArgumentException 
	{
		this.timerTime += rise;
		
		if( this.timerTime < 0 )
		{
			throw new IllegalArgumentException( "New timer time < 0." );
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see Auxiliar.Timers.ITimer#sumRelativeValueToTimer(int, long)
	 */
	@Override
	public void sumRelativeValueToTimer( float soMuchPerOne) throws IllegalArgumentException 
	{
		if( soMuchPerOne < 0 )
		{
			throw new IllegalArgumentException( "soMuchPerOne < 0." );
		}
		
		long aux = (long)( this.timerTime * soMuchPerOne );
		
		if( aux > 0 )
		{
			this.timerTime = aux;		
		}
	}
		
	@Override
	public String toString() 
	{
		return "[" + super.getName() + ": " + this.timerTime;
	}
}
