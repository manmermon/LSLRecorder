package lslrec.plugin.impl.dataProcessing.medianFilter;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lslrec.auxiliar.WarningMessage;
import lslrec.auxiliar.extra.NumberRange;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.config.SettingOptions;
import lslrec.gui.panel.plugin.item.CreatorDefaultSettingPanel;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.LSLRecConfigurablePluginAbstract;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.PluginDataProcessingSettings;

public class MedianFilterPlugin extends LSLRecConfigurablePluginAbstract implements ILSLRecPluginDataProcessing 
{

	public MedianFilterPlugin() 
	{
		super();
		
		super.pars.put( MedianFilterProcessing.MEDIAN_FILTER_LENGTH, new Parameter<String>( MedianFilterProcessing.MEDIAN_FILTER_LENGTH, "5" ) );
	}
	
	@Override
	public WarningMessage checkSettings() 
	{
		WarningMessage msg = new WarningMessage();
		
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
		return "Median filter";
	}

	@Override
	public int compareTo(ILSLRecPlugin o) 
	{
		return o.getID().compareTo( this.getID() );
	}

	@Override
	public LSLRecPluginDataProcessing getProcessing(PluginDataProcessingSettings settings
														, LSLRecPluginDataProcessing process) 
	{
		MedianFilterProcessing medianFilter = new MedianFilterProcessing( settings.getStreamSettings(), process );
		medianFilter.loadProcessingSettings( this.getSettings() );
		
		return medianFilter;
	}

	@Override
	public ProcessingLocation getProcessingLocation() 
	{
		return ProcessingLocation.BOTH;
	}

	@Override
	protected void postLoadSettings() 
	{		
	}

	@Override
	protected void setSettingPanel(JPanel panel ) 
	{
		if( panel != null )
		{
			panel.setVisible( false );
			panel.setLayout( new BorderLayout( ) );
			
			List< SettingOptions > opts = new  ArrayList< SettingOptions >();			
			ParameterList parList = new ParameterList();
			
			for( Parameter< String > par : super.getSettings() )
			{
				String id = par.getID();
				String val = par.getValue();
								
				switch ( id ) 
				{
					case MedianFilterProcessing.MEDIAN_FILTER_LENGTH:
					{
						int dec = Integer.parseInt( val );

						if( pars.get( id ) == null )
						{
							pars.put( id, new Parameter<String>( id, val ) );
						}
						
						Parameter< Integer > p = new Parameter< Integer >( id, dec );
						p.setLangID( id );
						parList.addParameter( p );
						
						p.addValueChangeListener( new ChangeListener() 
						{	
							@Override
							public void stateChanged(ChangeEvent e) 
							{
								Parameter< Integer > p = (Parameter<Integer>)e.getSource();
								
								pars.get( p.getID() ).setValue( p.getValue() + "" );
							}
						});
						
						SettingOptions opt = new SettingOptions( id, SettingOptions.Type.NUMBER, false, new NumberRange(0, Integer.MAX_VALUE), id );
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
			
			JPanel p = CreatorDefaultSettingPanel.getSettingPanel( opts, parList );			
			
			panel.add( p, BorderLayout.NORTH );
			panel.setVisible( true );
		}
	}
}
