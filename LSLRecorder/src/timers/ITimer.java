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

public interface ITimer 
{	
	public final static String EVENT_TIME_OVER = "time over";
	/**
	 * Adding or setting timer monitor.
	 * 
	 * @param monitor
	 */
	public void setTimerMonitor( ITimerMonitor monitor );
	
	/**
	 * 
	 * @return number of timers.
	 */
	//public int getNumberOfTimers();
	
	/**
	 * Adding a new timer time.
	 * 
	 * @param time
	 */
	//public void addTimerValue( long time );
	
	/**
	 * Set timer value in position pos to time.
	 *
	 * @param pos >= 0
	 * @param time >= 0
	 * 
	 * @throws IndexOutOfBoundsException
	 * 	If pos < 0 or pos >= number of timers then IndexOutOfBoundsException is thrown.
	 * 
	 * @throws IllegalArgumentException
	 * 	If time < 0
	 */
	//public void setTimerValue( int pos, long time ) throws IndexOutOfBoundsException, IllegalArgumentException;
	
	
	/**
	 * Set timer value to time.
	 *
	 * @param time >= 0
	 * 
	 * @throws IllegalArgumentException
	 * 	If time < 0
	 */
	public void setTimerValue( long time ) throws IllegalArgumentException;
	
	/**
	 *  Summing a new value to the timer time.
	 *
	 * @param pos >= 0
	 * @param rise
	 * 
	 * @throws IndexOutOfBoundsException
	 * 	If pos < 0 or pos >= number of timers then IndexOutOfBoundsException is thrown.
	 * 
	 * @throws IllegalArgumentException
	 * 	If ( rise + (timer time)) < 0
	 */
	//public void sumValueToTimer( int pos, long rise ) throws IndexOutOfBoundsException, IllegalArgumentException;;
	
	/**
	 *  Summing a new value to the timer time.
	 *
	 * @param rise
	 * 
	 * @throws IllegalArgumentException
	 * 	If ( rise + (timer time)) < 0
	 */
	public void sumValueToTimer( long rise ) throws IllegalArgumentException;;
	
	/**
	 *  (timer time) * soMuchPerOne.
	 *
	 * @param pos >= 0
	 * @param soMuchPerOne >= 0
	 * 
	 * @throws IndexOutOfBoundsException
	 * 	If pos < 0 or pos >= number of timers then IndexOutOfBoundsException is thrown.
	 * 
	 * @throws IllegalArgumentException
	 * 	If soMuchPerOne < 0
	 */
	//public void sumRelativeValueToTimer( int pos, float soMuchPerOne ) throws IndexOutOfBoundsException, IllegalArgumentException;
	
	/**
	 *  (timer time) * soMuchPerOne.
	 *
	 * @param soMuchPerOne >= 0
	 * 
	 * @throws IllegalArgumentException
	 * 	If soMuchPerOne < 0
	 */
	public void sumRelativeValueToTimer( float soMuchPerOne ) throws IllegalArgumentException;;
	
	/**
	 * Clear all timer times.
	 */
	//public void clearTimerValues();
	
	/**
	 * Restart timer activity.
	 */
	public void restartTimer();
	
	
}
