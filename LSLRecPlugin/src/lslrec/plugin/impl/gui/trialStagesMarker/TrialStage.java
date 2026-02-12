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
