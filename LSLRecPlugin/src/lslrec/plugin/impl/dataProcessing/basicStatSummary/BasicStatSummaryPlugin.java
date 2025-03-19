package lslrec.plugin.impl.dataProcessing.basicStatSummary;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import lslrec.auxiliar.WarningMessage;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.LSLRecConfigurablePluginAbstract;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

public class BasicStatSummaryPlugin  extends LSLRecConfigurablePluginAbstract implements ILSLRecPluginDataProcessing
{
	@Override
	public WarningMessage checkSettings() 
	{
		WarningMessage warning = new WarningMessage();
		
		return warning;
	}

	@Override
	public PluginType getType() 
	{
		return PluginType.DATA_PROCESSING;
	}

	@Override
	public String getID() 
	{
		return "BasicStatSummary";
	}

	@Override
	public int compareTo( ILSLRecPlugin arg0 ) 
	{
		int eq = 0;
		
		if( arg0 != null  )
		{
			eq = arg0.getID().compareTo( this.getID() );
		}
		
		return eq;
	}

	@Override
	public LSLRecPluginDataProcessing getProcessing(IStreamSetting arg0, ParameterList pars, LSLRecPluginDataProcessing arg1) 
	{
		LSLRecPluginDataProcessing process = new BasicStatSummaryProcessing( arg0, arg1 );
		
		if( pars != null )
		{
			List< Parameter<String> > list = new ArrayList< Parameter<String> >();
			
			for( String id : pars.getParameterIDs() )
			{
				list.add( pars.getParameter( id ) );
			}
			
			super.getSettings().addAll( list );
			
			process.loadProcessingSettings( list );
		}
		
		return process; 
	}

	@Override
	protected void postLoadSettings() 
	{		
	}

	@Override
	protected void setSettingPanel(JPanel arg0) 
	{		
	}

	@Override
	public ProcessingLocation getProcessingLocation() 
	{
		return ProcessingLocation.POST;
	}
}
