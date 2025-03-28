package lslrec.plugin.lslrecPlugin.processing;

import java.util.Set;

import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing.ProcessingLocation;

public class PluginDataProcessingSettings extends ParameterList 
{
	public static final String PAR_OUTPUT_FOLDER = "PAR_OUTPUT_FOLDER";
	public static final String PAR_PROCESSING_LOCATION = "PAR_PROCESSING_LOCATION";
	
	private IStreamSetting streamSettings;
	
	private ParameterList parlist;
	
	public PluginDataProcessingSettings( IStreamSetting streamSettings ) 
	{
		super();
		
		if( streamSettings == null )
		{
			throw new IllegalArgumentException( "IStreamSetting input null.");
		}
		
		this.streamSettings = streamSettings; 
		
		this.parlist = new ParameterList();
		
		this.parlist.addParameter( new Parameter< String >( PAR_OUTPUT_FOLDER, "./" ) );
		this.parlist.addParameter( new Parameter< ProcessingLocation >( PAR_PROCESSING_LOCATION, ProcessingLocation.DURING ) );
	}
	
	public IStreamSetting getStreamSettings() 
	{
		return streamSettings;
	}
	
	public Set< String > getParameterIDs()
	{
		return this.parlist.getParameterIDs();
	}
	
	public Parameter getParameter( String id )
	{
		return this.parlist.getParameter( id );
	}
	
	public void setParameter( String id, Object value )
	{
		this.parlist.addParameter( new Parameter( id, value ));
	}
}
