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
 */
package lslrec.plugin.impl.gui.trialStagesMarker;

import java.util.HashMap;
import java.util.Map;

public class TrialStage 
{
	public static final String EVENTS_SEPARATOR = ";";
	
	private String id;
	private int mark;
	private int time;
	private boolean auto;
	private Map< String, Integer > events;
	
	public TrialStage( String stageId, int mark, int time, boolean auto ) 
	{
		this.id = stageId;
		this.mark = mark;
		this.time = time;
		this.auto = auto;
		this.events = new HashMap< String, Integer >();
	}
	
	public void setSubstages( String eventId, int mark )
	{
		events.put( eventId, mark );
	}
	
	public Map< String, Integer > getEvents()
	{		
		return events;
	}
	
	public int getMark() 
	{
		return mark;
	}
	
	public String getId() 
	{
		return this.id;
	}
	
	public int getTime()
	{
		return this.time;
	}
	
	public boolean isAuto()
	{
		return this.auto;
	}
}
