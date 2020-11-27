/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.painter;

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

/**
 * @author Manuel Merino Monge
 *
 */
public class DataDisplayPlugin extends LSLRecConfigurablePluginAbstract implements ILSLRecPluginDataProcessing 
{
	public DataDisplayPlugin()
	{
		Parameter< String > par = new Parameter<String>( DataDisplay.DATA_LENGTH, "1000" );
		super.pars.put( par.getID(), par );
		
		par = new Parameter<String>( DataDisplay.VIEW_CHUNK_SIZE, "1" );
		super.pars.put( par.getID(), par );
		
		par = new Parameter<String>( DataDisplay.VIEW_MIN_Y, "0" );
		super.pars.put( par.getID(), par );
		
		par = new Parameter<String>( DataDisplay.VIEW_MAX_Y, "1" );
		super.pars.put( par.getID(), par );
	}
	
	@Override
	public WarningMessage checkSettings() 
	{
		WarningMessage wmsg = new WarningMessage();
		
		double minY = 0, maxY = 0;
		
		for( Parameter< String > par : super.pars.values() )
		{
			String id = par.getID();
			String val = par.getValue();
			
			int wType = WarningMessage.OK_MESSAGE;
			String msg = "";
			
			switch ( id )
			{
				case DataDisplay.DATA_LENGTH:
				{					
					try
					{
						if( ((int)Double.parseDouble( val )) <= 10 )
						{
							wType = WarningMessage.ERROR_MESSAGE;
							msg = "data length is lower than 10.\n";
						}
					}
					catch (Exception e) 
					{	
						wType = WarningMessage.ERROR_MESSAGE;
						msg = e.getMessage();
					}
					
					break;
				}	
				case DataDisplay.VIEW_CHUNK_SIZE:
				{
					try
					{
						if( ((int)Double.parseDouble( val )) <= 0 )
						{
							wType = WarningMessage.ERROR_MESSAGE;
							msg = "chunk size is lower than 1.\n";
						}
					}
					catch (Exception e) 
					{	
						wType = WarningMessage.ERROR_MESSAGE;
						msg = e.getMessage();
					}
					
					break;
				}
				case DataDisplay.VIEW_MAX_Y:
				{
					try
					{
						maxY = Double.parseDouble(  val );
					}
					catch (Exception e) 
					{	
						wType = WarningMessage.ERROR_MESSAGE;
						msg = e.getMessage();
					}
					
					break;
				}
				case DataDisplay.VIEW_MIN_Y:
				{
					try
					{
						minY = Double.parseDouble(  val );
					}
					catch (Exception e) 
					{	
						wType = WarningMessage.ERROR_MESSAGE;
						msg = e.getMessage();
					}
					
					break;
				}
				default:
				{
					break;
				}
			}
			
			wmsg.addMessage( msg, wType );
		}
		
		double span = maxY - minY ;
		if( span <= 0
				|| span == Double.NaN 
				|| span == Double.NEGATIVE_INFINITY
				|| span == Double.POSITIVE_INFINITY )
		{
			wmsg.addMessage( "y-axis span must be a non-negative, non-zero, finite number.\n", WarningMessage.ERROR_MESSAGE );
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
		return "Data display";
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
					case DataDisplay.DATA_LENGTH:
					case DataDisplay.VIEW_CHUNK_SIZE:
					case DataDisplay.VIEW_MIN_Y:
					case DataDisplay.VIEW_MAX_Y:
					{	
						SettingOptions opt = new SettingOptions( par.getID(), SettingOptions.Type.NUMBER, false, par.getID() );
						opt.addValue( par.getValue() );
						
						opts.add( opt );
						
						break;
					}
					default:
					{
						break;
					}
				}
			}
			
			JPanel p = CreatorDefaultSettingPanel.getSettingPanel( opts, parlist );
			arg0.add(  p, BorderLayout.NORTH );
		}
	}

	@Override
	public LSLRecPluginDataProcessing getProcessing(IStreamSetting arg0, LSLRecPluginDataProcessing arg1) 
	{
		DataDisplay disp = new DataDisplay( arg0, arg1 );
		
		List< Parameter< String > > pars = this.getSettings();
		
		String prevProc = "";
		if( arg1 != null )
		{
			prevProc = arg1.getID();
		}
		pars.add( new Parameter<String>( DataDisplay.WIN_TITLE, disp.getID() + ":" + prevProc + " - " + arg0.name() ) );
		
		disp.loadProcessingSettings( pars );
		
		return disp;
	}

}
