package lslrec.plugin.impl.dataProcessing.resender;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class LSLStreamResenderPlugin extends LSLRecConfigurablePluginAbstract implements ILSLRecPluginDataProcessing
{
	public LSLStreamResenderPlugin() 
	{
		super();
		
		super.pars.put( LSLStreamResender.STREAM_NAME, new Parameter<String>( LSLStreamResender.STREAM_NAME, "LSLStreamResender" ) );
		super.pars.put( LSLStreamResender.SELECTED_CHANNELS, new Parameter<String>( LSLStreamResender.SELECTED_CHANNELS, "0" ) );
		/*
		super.pars.put( LSLStreamResender.STREAM_CHUNK_SIZE, new Parameter<String>( LSLStreamResender.STREAM_CHUNK_SIZE, "1" ) );
		super.pars.put( LSLStreamResender.STREAM_DATA_TYPE, new Parameter<String>( LSLStreamResender.STREAM_DATA_TYPE, StreamDataType.float32.toString() ) );
		super.pars.put( LSLStreamResender.STREAM_NUMBER_CHANNELS, new Parameter<String>( LSLStreamResender.STREAM_NUMBER_CHANNELS, "1" ) );
		super.pars.put( LSLStreamResender.STREAM_SAMPLING_RATE, new Parameter<String>( LSLStreamResender.STREAM_SAMPLING_RATE, "0" ) );
		super.pars.put( LSLStreamResender.STREAM_TYPE, new Parameter<String>( LSLStreamResender.STREAM_TYPE, "Value" ) );
		*/
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
				case LSLStreamResender.STREAM_NAME:
				{
					if( val == null  || val.isEmpty() )
					{
						msg.addMessage( "Stream name of plugin " + this.getID() + " empty.", WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
				case LSLStreamResender.SELECTED_CHANNELS:
				{
					List< Integer > channels = LSLStreamResenderTools.convertIntegerStringList2IntArray( val );
										
					if( channels.isEmpty() )
					{
						msg.addMessage( "Selected channels  of plugin " + this.getID() + "  malformed.", WarningMessage.ERROR_MESSAGE );
					}
					else
					{
						Set< Integer > setChns = new HashSet<Integer>( channels );
						
						if( setChns.size() != channels.size() )
						{
							msg.addMessage( "Selected channels repeated in plugin " + this.getID() + ".", WarningMessage.ERROR_MESSAGE );
						}
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
		return "LSLStreamResender";
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
			arg0.removeAll();
			arg0.setLayout( new BorderLayout( ) );
			
			List< SettingOptions > opts = new  ArrayList< SettingOptions >();			
			ParameterList pars = new ParameterList();
			
			for( Parameter< String > par : super.getSettings() )
			{
				String id = par.getID();
				String val = par.getValue();
								
				switch ( id ) 
				{
					case LSLStreamResender.STREAM_NAME:
					{
						if( val == null || val.isEmpty() )
						{						
							val = "LSLStreamResender";
						}
						
						par.setValue( val );
						par.setID( id );
						
						pars.addParameter( par );
									
						SettingOptions opt = new SettingOptions( id, SettingOptions.Type.STRING, false, null, id );
						opt.addValue( val );
						opts.add( opt );
						
						break;
					}
					case LSLStreamResender.SELECTED_CHANNELS:
					{
						if( val == null || val.isEmpty() )
						{
							val = "0";
						}
					
						par.setValue( val );
						par.setLangID( id );
						par.setDescriptorText( LSLStreamResender.SELECTED_CHANNELS +  ": 1,2,...");
						pars.addParameter( par );
						
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
			
			JPanel p = CreatorDefaultSettingPanel.getSettingPanel( opts, pars );			
			//arg0.add( CreatorDefaultSettingPanel.getSettingPanel( opts, pars ), BorderLayout.NORTH );
			arg0.add( p, BorderLayout.NORTH );
			arg0.setVisible( true );
		}
	}

	@Override
	//public LSLRecPluginDataProcessing getProcessing( IStreamSetting arg0, ParameterList pars, LSLRecPluginDataProcessing arg1) 
	public LSLRecPluginDataProcessing getProcessing( PluginDataProcessingSettings settings, LSLRecPluginDataProcessing arg1)
	{
		LSLStreamResender resender = new LSLStreamResender( settings.getStreamSettings() , arg1 );			
		resender.loadProcessingSettings( new ArrayList< Parameter< String > >( super.pars.values() ) );
		
		return resender;
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
