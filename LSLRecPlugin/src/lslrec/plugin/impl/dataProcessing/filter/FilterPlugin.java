package lslrec.plugin.impl.dataProcessing.filter;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lslrec.auxiliar.WarningMessage;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.gui.setting.CreatorDefaultSettingPanel;
import lslrec.gui.setting.SettingOptions;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.LSLRecConfigurablePluginAbstract;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.PluginDataProcessingSettings;

public class FilterPlugin extends LSLRecConfigurablePluginAbstract implements ILSLRecPluginDataProcessing  
{
	public FilterPlugin() 
	{
		super();
		
		super.pars.put( FilterProcessing.COEF_A, new Parameter<String>( FilterProcessing.COEF_A, "1" ) );
		super.pars.put( FilterProcessing.COEF_B, new Parameter<String>( FilterProcessing.COEF_B, "1" ) );
	}
	
	@Override
	public WarningMessage checkSettings() 
	{
		WarningMessage wm = new WarningMessage();
		
		for( String id : super.pars.keySet() )
		{
			Parameter< String > par = this.pars.get( id );
			switch ( id ) 
			{
				case FilterProcessing.COEF_A:
				case FilterProcessing.COEF_B:
				{					
					String val = par.getValue();
					
					List< Double > coef = FilterUtils.parseFilterCoef( val );
					if( coef.isEmpty() )
					{
						wm.addMessage( "Filter coefficients are not defined correctly. They must be a comma-separated list of numbers.", WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
				default:
				{
					break;
				}
			}
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
		return "Filter";
	}

	@Override
	public int compareTo(ILSLRecPlugin o) 
	{
		return o.getID().compareTo( this.getID() );
	}

	@Override
	public LSLRecPluginDataProcessing getProcessing(PluginDataProcessingSettings setting,  LSLRecPluginDataProcessing process) 
	{
		FilterProcessing filter = new FilterProcessing( setting.getStreamSettings(), process );
		filter.loadProcessingSettings( this.getSettings() );
		
		return filter;
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
					case FilterProcessing.COEF_A:					
					case FilterProcessing.COEF_B:
					{
						List< Double > coefs = FilterUtils.parseFilterCoef( val );
						
						if( coefs.isEmpty() )
						{
							coefs.add( 1D );
							val = "1";
						}
						
						if( pars.get( id ) == null )
						{
							pars.put( id, new Parameter<String>( id, val ) );
						}
						
						Parameter< String > p = new Parameter< String >( id, val );
						p.setLangID( id );
						parList.addParameter( p );
						
						p.addValueChangeListener( new ChangeListener() 
						{	
							@Override
							public void stateChanged(ChangeEvent e) 
							{
								Parameter< String > p = (Parameter< String >)e.getSource();
								
								List< Double > coef = FilterUtils.parseFilterCoef( p.getValue() );
								
								if( !coef.isEmpty() )
								{							
									pars.get( p.getID() ).setValue( p.getValue() + "" );
								}
							}
						});
						
						SettingOptions opt = new SettingOptions( id, SettingOptions.Type.STRING, false, null, id );
						opt.addValue( val );
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
