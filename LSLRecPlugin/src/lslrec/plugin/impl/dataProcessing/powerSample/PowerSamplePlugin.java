package lslrec.plugin.impl.dataProcessing.powerSample;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
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

public class PowerSamplePlugin extends LSLRecConfigurablePluginAbstract implements ILSLRecPluginDataProcessing 
{
	public PowerSamplePlugin() 
	{
		super.pars.put( PowerSampleProcessing.EXP
						, new Parameter<String>( PowerSampleProcessing.EXP, "2" ) );
	}
	
	@Override
	public WarningMessage checkSettings() 
	{
		WarningMessage msg = new WarningMessage();
		
		for( String id : super.pars.keySet() )
		{
			switch ( id ) 
			{
				case PowerSampleProcessing.EXP:
				{
					try 
					{
						Double.parseDouble( super.pars.get( id ).getValue() );
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
		return "PowerSample";
	}

	@Override
	public int compareTo(ILSLRecPlugin o) 
	{
		return o.getID().compareTo( this.getID() );
	}

	@Override
	public LSLRecPluginDataProcessing getProcessing(PluginDataProcessingSettings settings, LSLRecPluginDataProcessing process) 
	{
		PowerSampleProcessing squaredSample = new PowerSampleProcessing( settings.getStreamSettings(), process );
		squaredSample.loadProcessingSettings( this.getSettings() );
		
		return squaredSample;
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
					case  PowerSampleProcessing.EXP:
					{
						try
						{
							Double.parseDouble( val );
						}
						catch (Exception e) 
						{}
						
						if( super.pars.get( id ) == null )
						{
							super.pars.put( id, new Parameter<String>( id, val ) );
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
								
								try
								{
									Double.parseDouble( p.getValue() );
									
									pars.get( p.getID() ).setValue( p.getValue() + "" );
								}
								catch (Exception ex) 
								{}
							}
						});
						
						SettingOptions opt = new SettingOptions( id, SettingOptions.Type.NUMBER, false, null, id );
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
			
			JPanel p2 = new JPanel(new BorderLayout() );
			p2.add( p, BorderLayout.CENTER );
			p2.add( new JLabel( "y[n]=x[n]^("+PowerSampleProcessing.EXP+")" ), BorderLayout.NORTH );
			
			panel.add( p2, BorderLayout.NORTH );
			panel.setVisible( true );
		}
	}

}
