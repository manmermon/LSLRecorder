package lslrec.plugin.impl.gui.trialStagesMarker;

public class TrialStage 
{
	private String id;
	private int mark;
	private int time;
	private boolean auto;
	
	public TrialStage( String stageId, int mark, int time, boolean auto ) 
	{
		this.id = stageId;
		this.mark = mark;
		this.time = time;
		this.auto = auto;
	}
	
	public String getId() 
	{
		return this.id;
	}
	
	public int getMark() 
	{
		return this.mark;
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
