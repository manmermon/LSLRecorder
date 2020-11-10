/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.downSampling;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import lslrec.auxiliar.WarningMessage;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.config.SettingOptions;
import lslrec.dataStream.family.stream.lsl.DataStreamSetting;
import lslrec.gui.panel.plugin.item.CreatorDefaultSettingPanel;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.LSLRecConfigurablePluginAbstract;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

/**
 * @author Manuel Merino Monge
 *
 */
public class DownSamplingPlugin extends LSLRecConfigurablePluginAbstract implements ILSLRecPluginDataProcessing
{	
	/**
	 * 
	 */
	public DownSamplingPlugin() 
	{
		super();
		
		super.pars.put( DownSampling.DECIMATION, new Parameter<String>( DownSampling.DECIMATION, "1" ) );
	}
	
	@Override
	public WarningMessage checkSettings() 
	{
		WarningMessage msg = new WarningMessage();
		
		for( Parameter< String > par : super.getSettings() )
		{
			String id = par.getID();
			String val = par.getValue();
			
			switch ( id ) 
			{
				case DownSampling.DECIMATION:
				{
					try
					{
						Integer.parseInt( val );
					}
					catch (Exception e)
					{
						msg.addMessage( e.getMessage(), WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
				default:
				{
					break;
				}
			}
		}
		
		return msg;
	}

	@Override
	public PluginType getType() 
	{
		return PluginType.DATA_PROCESSING;
	}

	@Override
	public String getID() 
	{
		return "Downsampling";
	}

	@Override
	public int compareTo(ILSLRecPlugin arg0) 
	{
		int eq = 0;
		
		if( arg0 != null  )
		{
			eq = arg0.getID().compareTo( this.getID() );
		}
		
		return eq;
	}

	@Override
	protected void setSettingPanel( JPanel arg0 ) 
	{		
		if( arg0 != null )
		{
			arg0.setVisible( false );
			arg0.setLayout( new BorderLayout( ) );
			
			List< SettingOptions > opts = new  ArrayList< SettingOptions >();			
			ParameterList pars = new ParameterList();
			
			for( Parameter< String > par : super.getSettings() )
			{
				String id = par.getID();
				String val = par.getValue();
								
				switch ( id ) 
				{
					case DownSampling.DECIMATION:
					{
						int dec = Integer.parseInt( val );

						Parameter< Integer > p = new Parameter< Integer >( id, dec );
						p.setLangID( id );
						pars.addParameter( p );
												
						SettingOptions opt = new SettingOptions( id, SettingOptions.Type.NUMBER, false, id );
						opt.addValue( dec + "" );
						opts.add( opt );
						
						break;
					}
					default:
					{
						break;
					}
				}
			}
			
			JPanel p = CreatorDefaultSettingPanel.getSettingPanel( opts, pars );			
			//arg0.add( CreatorDefaultSettingPanel.getSettingPanel( opts, pars ), BorderLayout.NORTH );
			arg0.add( p, BorderLayout.NORTH );
			arg0.setVisible( true );
		}
	}

	@Override
	public LSLRecPluginDataProcessing getProcessing(DataStreamSetting arg0, LSLRecPluginDataProcessing arg1) 
	{
		DownSampling ds = new DownSampling( arg0 , arg1 );			
		ds.loadProcessingSettings( new ArrayList<Parameter<String>>( super.pars.values() ) );
		
		return ds;
	}

}
