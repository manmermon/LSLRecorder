/* 
 * Copyright 2018-2019 by Manuel Merino Monge <manmermon@dte.us.es>
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


package lslrec.stoppableThread.events;

import java.lang.Thread.State;
import java.util.EventObject;

public class StoppableThreadEvent extends EventObject
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
		
	private State STATE; 
	
	private long threadID;
	
	public StoppableThreadEvent( Object arg0, long thrID ) 
	{
		this( arg0, thrID, Thread.State.NEW );
	} 
	
	public StoppableThreadEvent( Object arg0, long thrID, State threadState ) 
	{
		super( arg0 );
		
		this.threadID = thrID;
		
		this.STATE = threadState;
	} 
	
	public State GetState()
	{
		return this.STATE;
	}
	
	public long GetThreadID()
	{
		return this.threadID;
	}
}
