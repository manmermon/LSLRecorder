/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.openposePlotter;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import lslrec.auxiliar.WarningMessage;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.config.SettingOptions;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.gui.panel.plugin.item.CreatorDefaultSettingPanel;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.LSLRecConfigurablePluginAbstract;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.PluginDataProcessingSettings;

/**
 * @author Manuel Merino Monge
 *
 */
public class OpenposePlotterPlugin extends LSLRecConfigurablePluginAbstract implements ILSLRecPluginDataProcessing 
{
	public OpenposePlotterPlugin()
	{
		Parameter< String > par;
			
		/*
		par = new Parameter<String>( OpenposePlotter.BODY_POINTS, "25" );
		super.pars.put( par.getID(), par );
		
		par = new Parameter<String>( OpenposePlotter.OPP_X_RESOLUTION, "640" );
		super.pars.put( par.getID(), par );
		
		par = new Parameter<String>( OpenposePlotter.OPP_Y_RESOLUTION, "480" );
		super.pars.put( par.getID(), par );
		*/
				
	}
	
	@Override
	public WarningMessage checkSettings() 
	{
		WarningMessage wmsg = new WarningMessage();
				
		for( Parameter< String > par : super.pars.values() )
		{
			String id = par.getID();
			String val = par.getValue();
			
			int wType = WarningMessage.OK_MESSAGE;
			String msg = "";
			
			switch ( id )
			{
				case OpenposePlotter.BODY_POINTS:
				{					
					try
					{
						if( ((int)Double.parseDouble( val )) != 25 )
						{
							wType = WarningMessage.ERROR_MESSAGE;
							msg = "Body must be equal to 25.\n";
						}
					}
					catch (Exception e) 
					{	
						wType = WarningMessage.ERROR_MESSAGE;
						msg = e.getMessage();
					}
					
					break;
				}	
				/*
				case OpenposePlotter.OPP_X_RESOLUTION:
				case OpenposePlotter.OPP_Y_RESOLUTION:
				{
					try
					{
						if( ((int)Double.parseDouble( val )) <= 0 )
						{
							wType = WarningMessage.ERROR_MESSAGE;
							msg = "Openpose image resolution is zero or negatuve.\n";
						}
					}
					catch (Exception e) 
					{	
						wType = WarningMessage.ERROR_MESSAGE;
						msg = e.getMessage();
					}
					
					break;
				}
				*/
				default:
				{
					break;
				}
			}
			
			wmsg.addMessage( msg, wType );
		}		
		
		return wmsg;
	}

	@Override
	public PluginType getType() 
	{
		return PluginType.DATA_PROCESSING;
	}

	@Override
	public String getID() 
	{
		return "Openpose Plotter";
	}

	@Override
	public int compareTo(ILSLRecPlugin arg0) 
	{		
		return arg0.getID().compareTo( this.getID() );
	}

	@Override
	protected void setSettingPanel(JPanel arg0) 
	{	
		if( arg0 != null )
		{
			arg0.setLayout( new BorderLayout() );
			
			ParameterList parlist = new ParameterList();
			List< SettingOptions > opts = new ArrayList< SettingOptions >();
			
			for( Parameter< String > par : super.pars.values() )
			{			
				parlist.addParameter( par );
				
				switch ( par.getID() )
				{
				/*
					case OpenposePlotter.OPP_X_RESOLUTION:
					case OpenposePlotter.OPP_Y_RESOLUTION:
					{	
						SettingOptions opt = new SettingOptions( par.getID(), SettingOptions.Type.NUMBER, false, par.getID() );
						opt.addValue( par.getValue() );
						
						opts.add( opt );
						
						break;
					}
					
					*/
					default:
					{
						break;
					}
				}
			}
			
			if( !opts.isEmpty() )
			{
				JPanel p = CreatorDefaultSettingPanel.getSettingPanel( opts, parlist );
				arg0.add(  p, BorderLayout.NORTH );
			}
		}
	}

	@Override
	//public LSLRecPluginDataProcessing getProcessing(IStreamSetting arg0, ParameterList parlist, LSLRecPluginDataProcessing arg1)
	public LSLRecPluginDataProcessing getProcessing( PluginDataProcessingSettings settings, LSLRecPluginDataProcessing arg1)
	{
		OpenposePlotter disp = new OpenposePlotter( settings.getStreamSettings(), arg1 );
		
		List< Parameter< String > > pars = this.getSettings();
		
		/*
		String prevProc = "";
		if( arg1 != null )
		{
			prevProc = arg1.getID();
		}
		pars.add( new Parameter<String>( OpenposePlotter.WIN_TITLE, disp.getID() + ":" + prevProc + " - " + arg0.name() ) );
		//*/
		
		disp.loadProcessingSettings( pars );
		
		return disp;
	}

	@Override
	protected void postLoadSettings() 
	{	
	}

	@Override
	public ProcessingLocation getProcessingLocation() 
	{
		return ProcessingLocation.DURING;
	}
}
