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

public class TimerParameters 
{
	private long time = 0L;
	
	private boolean activeBeep = false;
	
	private boolean activeTimeReport = false;

	/**
	 * Set timer properties.
	 * 
	 * @param t
	 * 	time value. If t < 0 then infinity timer.
	 * 
	 * @param beep
	 * 	Active beep sound.
	 * 
	 * @param report
	 * 	Active the report of time.
	 */
	public TimerParameters( long t, boolean beep, boolean report )
	{			
		this.time = t;
		this.activeBeep = beep;
		this.activeTimeReport = report;
	}
	
	public void setTime( long t )
	{
		this.time = t;
	}
		
	public long getTime( )
	{
		return this.time;
	}
	
	public void setEnableBeep( boolean beep )
	{
		this.activeBeep = beep;
	}
	
	public boolean isEnabledBeep()
	{
		return this.activeBeep;
	}
	
	public void setEnableTimeReport( boolean report )
	{
		this.activeTimeReport = report;
	}
	
	public boolean isEnableTimeReport()
	{
		return this.activeTimeReport;
	}
	
	@Override
	public String toString() 
	{
		return "Timer parameters: is beep active? " + this.activeBeep +
				", is time report active? " + this.activeTimeReport +
				", time = " + this.time;
	}
}
