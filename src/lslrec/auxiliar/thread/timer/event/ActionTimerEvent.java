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
package lslrec.auxiliar.thread.timer.event;

import java.util.EventObject;

public class ActionTimerEvent extends EventObject
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public enum Type { DESTROY, START, RESTART, STOP, ACTION };
	
	private Type typeEvent;
	
	private long consumedTime;
	
	public ActionTimerEvent( Object source, Type type, long consumedTime ) 
	{
		super( source );
		
		this.typeEvent = type;
		
		this.consumedTime = consumedTime;
	}
	
	public Type getType()
	{
		return this.typeEvent;
	}
	
	public long getConsumedTime() 
	{
		return this.consumedTime;
	}
}
