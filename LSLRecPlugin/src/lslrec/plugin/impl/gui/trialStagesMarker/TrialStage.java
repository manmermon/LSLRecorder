package lslrec.plugin.impl.gui.trialStagesMarker;

public class TrialStage 
{
	public static final String SUBSTAGE_SEPARATOR = ";";
	
	private String id;
	private int mark;
	private int time;
	private boolean auto;
	private String substageId;
	
	public TrialStage( String stageId, int mark, int time, boolean auto ) 
	{
		this.id = stageId;
		this.mark = mark;
		this.time = time;
		this.auto = auto;
		this.substageId = "";
	}
	
	public void setSubstages( String substages )
	{
		this.substageId = ( substages == null ? "" : substages.trim() );
	}
	
	public String[] getSubstages()
	{
		String[] substages = new String[0];
		
		if( !this.substageId.trim().isEmpty() )
		{
			substages = this.substageId.split( SUBSTAGE_SEPARATOR );
		}
		
		return substages;
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
