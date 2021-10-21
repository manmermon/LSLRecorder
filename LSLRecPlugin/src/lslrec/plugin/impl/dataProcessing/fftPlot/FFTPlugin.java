/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.fftPlot;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import lslrec.auxiliar.WarningMessage;
import lslrec.auxiliar.extra.NumberRange;
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
public class FFTPlugin extends LSLRecConfigurablePluginAbstract implements ILSLRecPluginDataProcessing 
{

	/**
	 * 
	 */
	public FFTPlugin() 
	{
		//Parameter< String > par = new Parameter<String>( FFTDisplay.SAMPLING_RATE, "1" );
		//super.pars.put( par.getID(), par );
		
		Parameter< String > par = new Parameter<String>( FFTDisplay.TIME_WIN, "1" );
		super.pars.put( par.getID(), par );		
		
		par = new Parameter<String>( FFTDisplay.OVERLAP_WIN, "0" );
		super.pars.put( par.getID(), par );		
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
					//case FFTDisplay.SAMPLING_RATE:
					case FFTDisplay.OVERLAP_WIN:
					case FFTDisplay.TIME_WIN:
					{	
						SettingOptions opt = new SettingOptions( par.getID(), SettingOptions.Type.NUMBER, false, new NumberRange( 0.001, Double.POSITIVE_INFINITY ), par.getID() );
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
	public WarningMessage checkSettings() 
	{
		WarningMessage wm = new WarningMessage();
		
		for( Parameter< String > par : super.pars.values() )
		{
			String id = par.getID();
			String val = par.getValue();
			
			int wType = WarningMessage.OK_MESSAGE;
			String msg = "";
			
			switch ( id )
			{
				case FFTDisplay.TIME_WIN:
				{					
					try
					{
						if( Double.parseDouble( val ) <= 0 )
						{
							wType = WarningMessage.ERROR_MESSAGE;
							msg = "time windows must be > 0.\n";
						}
					}
					catch (Exception e) 
					{	
						wType = WarningMessage.ERROR_MESSAGE;
						msg = e.getMessage();
					}
					
					break;
				}	
				case FFTDisplay.OVERLAP_WIN:
				{					
					try
					{
						if( Double.parseDouble( val ) < 0 )
						{
							wType = WarningMessage.ERROR_MESSAGE;
							msg = "overlapment must be >= 0.\n";
						}
					}
					catch (Exception e) 
					{	
						wType = WarningMessage.ERROR_MESSAGE;
						msg = e.getMessage();
					}
					
					break;
				}	
				case FFTDisplay.SAMPLING_RATE:
				{					
					try
					{
						if( Double.parseDouble( val ) < 0 )
						{
							wType = WarningMessage.ERROR_MESSAGE;
							msg = "sampling rate must be >= 0.\n";
						}
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
			
			wm.addMessage( msg, wType );
		}
		
		return wm;
	}

	@Override
	public PluginType getType() 
	{
		return PluginType.DATA_PROCESSING;
	}

	@Override
	public String getID() 
	{
		return "DFT display";
	}

	@Override
	public int compareTo(ILSLRecPlugin o) 
	{
		return this.getID().compareTo( o.getID() );
	}

	@Override
	public LSLRecPluginDataProcessing getProcessing(IStreamSetting arg0, LSLRecPluginDataProcessing arg1) 
	{
		List< Parameter< String > > pars = this.getSettings();
		
		FFTDisplay disp = new FFTDisplay( arg0, arg1 );
				
		disp.loadProcessingSettings( pars );
		
		return disp;
	}
}
