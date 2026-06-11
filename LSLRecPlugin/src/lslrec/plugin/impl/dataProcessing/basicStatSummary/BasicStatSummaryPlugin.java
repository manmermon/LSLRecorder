/* 
 * Copyright 2018-2024 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec. https://github.com/manmermon/LSLRecorder
 *
 *   LSLRec is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   LSLRec is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with LSLRec.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */
package lslrec.plugin.impl.dataProcessing.basicStatSummary;

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
import lslrec.gui.setting.CreatorDefaultSettingPanel;
import lslrec.gui.setting.SettingOptions;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.LSLRecConfigurablePluginAbstract;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.PluginDataProcessingSettings;

public class BasicStatSummaryPlugin  extends LSLRecConfigurablePluginAbstract implements ILSLRecPluginDataProcessing
{
	public BasicStatSummaryPlugin() 
	{
		super.pars.put( BasicStatSummaryProcessing.MARKER_WIN_SEGMENT_LEN, new Parameter<String>( BasicStatSummaryProcessing.MARKER_WIN_SEGMENT_LEN, "10" ) );
		super.pars.put( BasicStatSummaryProcessing.MARKER_ID_SEGMENTS, new Parameter<String>( BasicStatSummaryProcessing.MARKER_ID_SEGMENTS, "" ) );
	}
	
	@Override
	public WarningMessage checkSettings() 
	{
		WarningMessage warning = new WarningMessage();
		
		for( Parameter< String > par : super.getSettings() )
		{
			String id = par.getID();
			String val = par.getValue();
			
			switch ( id ) 
			{
				case BasicStatSummaryProcessing.MARKER_WIN_SEGMENT_LEN:
				{
					try
					{
						Integer.parseInt( val );
					}
					catch (Exception e)
					{
						warning.addMessage( e.getMessage(), WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
				case BasicStatSummaryProcessing.MARKER_ID_SEGMENTS:
				{
					try
					{
						
					}
					catch (Exception e) 
					{
					}
					
					break;
				}
				default:
				{
					break;
				}
			}
		}
		
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
	public LSLRecPluginDataProcessing getProcessing( PluginDataProcessingSettings settings, LSLRecPluginDataProcessing arg1)
	{		
		LSLRecPluginDataProcessing process = new BasicStatSummaryProcessing( settings.getStreamSettings(), arg1 );
		
		if( pars != null )
		{
			List< Parameter<String> > list = new ArrayList< Parameter<String> >();
			
			for( String id : settings.getParameterIDs() )
			{
				list.add( settings.getParameter( id ) );
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
	protected void setSettingPanel( JPanel arg0 ) 
	{
		if( arg0 != null )
		{
			arg0.setVisible( false );
			arg0.setLayout( new BorderLayout( ) );
			
			List< SettingOptions > opts = new  ArrayList< SettingOptions >();			
			ParameterList parList = new ParameterList();
			
			for( Parameter< String > par : super.getSettings() )
			{
				String id = par.getID();
				String val = par.getValue();
								
				switch ( id ) 
				{
					case BasicStatSummaryProcessing.MARKER_WIN_SEGMENT_LEN:
					{
						int dec = Integer.parseInt( val );

						if( pars.get( id ) == null )
						{
							pars.put( id, new Parameter<String>( id, val ) );
						}
						
						Parameter< Integer > p = new Parameter< Integer >( id, dec );
						p.setLangID( id );
						p.setDescriptorText( "window length (samples for irregular stream or seconds for regular)" );
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
						
						SettingOptions opt = new SettingOptions( id, SettingOptions.Type.NUMBER, false, new NumberRange(0, Double.POSITIVE_INFINITY ), id );
						opt.addValue( dec + "" );
						opts.add( opt );
						
						break;
					}
					case BasicStatSummaryProcessing.MARKER_ID_SEGMENTS:
					{
						if( pars.get( id ) == null )
						{
							pars.put( id, new Parameter<String>( id, val ) );
						}
						
						Parameter< String > p = new Parameter< String >( id, val );
						p.setLangID( id );
						p.setDescriptorText( "id of the segmentation markers separated by commas or semicolons" );
						parList.addParameter( p );
						
						p.addValueChangeListener( new ChangeListener() 
						{	
							@Override
							public void stateChanged(ChangeEvent e) 
							{
								Parameter< String > p = (Parameter<String>)e.getSource();
								
								pars.get( p.getID() ).setValue( p.getValue() );
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
			arg0.add( p, BorderLayout.NORTH );
			
			arg0.setVisible( true );
		}
	}

	@Override
	public ProcessingLocation getProcessingLocation() 
	{
		return ProcessingLocation.POST;
	}
}
