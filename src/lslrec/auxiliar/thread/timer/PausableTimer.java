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

package lslrec.auxiliar.thread.timer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import lslrec.stoppableThread.AbstractStoppableThread;

public class PausableTimer extends AbstractStoppableThread
{
	private Timer timer;
	private long startTime = -1;
	private ActionListener action;
	
	public PausableTimer( int delay, ActionListener act ) 
	{
		super();
		this.action = this.setAction( act ) ;
		this.timer = new Timer( delay, this.action );
		
		super.setName( this.getClass().getName() );
	}
	
	private ActionListener setAction( final ActionListener act )
	{
		return new ActionListener() 
		{	
			@Override
			public void actionPerformed(ActionEvent arg0) 
			{
				act.actionPerformed( arg0 );
				
				wakeup();
			}
		};
	}
	
	private void wakeup()
	{
		synchronized ( this )
		{
			super.notify();
		}
	}
	
	@Override
	public String toString() 
	{
		return super.getName() + ": " + this.timer;
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
		synchronized ( this )
		{
			this.startTime = System.nanoTime();
			
			this.timer.start();
			
			this.wait();
		}		
	}	
	
	@Override
	protected void targetDone() throws Exception 
	{
		super.targetDone();

		super.stopThread = true;
	}
		
	@Override
	protected void cleanUp() throws Exception 
	{
		synchronized ( this ) 
		{
			super.cleanUp();
			
			if( this.timer != null )
			{
				this.timer.stop();
				this.timer = null;
			}
		}		
	}	
	
	public int getRemainingTime()
	{
		int restTime = this.timer.getInitialDelay();
		if( this.startTime > 0 )
		{
			restTime = restTime - (int)( ( System.nanoTime() - this.startTime ) / 1e6D );
			restTime = ( restTime <= 0 ) ? 0 : restTime;
		}
		
		return restTime;		 
	}
	
	public void pauseTimer() 
	{
		synchronized ( this ) 
		{
			if( this.timer != null 
					&& this.timer.isRunning() )
			{
				this.timer.stop();
				int restTime = this.getRemainingTime();//this.timer.getInitialDelay() - (int)( ( System.nanoTime() - this.startTime ) / 1e6D );
				this.startTime = -1;
				this.timer = new Timer( restTime, this.action );
			}
		}
	}

	public void resumenTimer() 
	{
		synchronized ( this )
		{
			if( this.timer != null 
					&& !this.timer.isRunning() )
			{	
				this.startTime = System.nanoTime();				
				this.timer.restart();
			}
		}
	}
	
	public boolean isRunning() 
	{
		boolean run = false;
		
		synchronized ( this ) 
		{
			if( this.timer != null )
			{
				run = this.timer.isRunning(); 
			}
		}
		
		return run;
	}
}

