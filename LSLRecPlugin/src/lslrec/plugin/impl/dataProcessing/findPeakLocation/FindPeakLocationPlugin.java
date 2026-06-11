/* 
 * Copyright 2018-2026 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.plugin.impl.dataProcessing.findPeakLocation;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lslrec.auxiliar.WarningMessage;
import lslrec.auxiliar.extra.NumberRange;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.gui.setting.CreatorDefaultSettingPanel;
import lslrec.gui.setting.SettingOptions;
import lslrec.plugin.impl.dataProcessing.findPeakLocation.FindPeakLocationProcessing.Peak_type;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.LSLRecConfigurablePluginAbstract;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.PluginDataProcessingSettings;

public class FindPeakLocationPlugin extends LSLRecConfigurablePluginAbstract implements ILSLRecPluginDataProcessing 
{
	public FindPeakLocationPlugin() 
	{
		super();
		
		super.pars.put( FindPeakLocationProcessing.BUFFER_LEN, new Parameter<String>( FindPeakLocationProcessing.BUFFER_LEN, "1" ) );
		super.pars.put( FindPeakLocationProcessing.SHIFT_LEN,  new Parameter<String>( FindPeakLocationProcessing.SHIFT_LEN, "1") );
		super.pars.put( FindPeakLocationProcessing.MIN_DISTANCE, new Parameter<String>( FindPeakLocationProcessing.MIN_DISTANCE, "0") );
		super.pars.put( FindPeakLocationProcessing.MIN_PROMINENCE, new Parameter<String>( FindPeakLocationProcessing.MIN_PROMINENCE, "0") );
		super.pars.put( FindPeakLocationProcessing.PEAK_TYPE, new Parameter<String>( FindPeakLocationProcessing.PEAK_TYPE, Peak_type.MIN_MAX.toString() ));
		super.pars.put( FindPeakLocationProcessing.ADJUST_SHIFTING, new Parameter<String>( FindPeakLocationProcessing.ADJUST_SHIFTING,  "false" ));
	}
	
	@Override
	public WarningMessage checkSettings() 
	{
		WarningMessage warn = new WarningMessage();
		
		for( String id : super.pars.keySet() )
		{
			Parameter< String > par = super.pars.get( id );
			
			switch ( id ) 
			{
				case FindPeakLocationProcessing.MIN_DISTANCE:
				{
					try
					{
						int val = Integer.parseInt( par.getValue() );
						
						if( val < 0 )
						{
							warn.addMessage( id + " must be a integer greater or equal to 0.", WarningMessage.ERROR_MESSAGE );
						}
					}
					catch (Exception e) 
					{ 
						warn.addMessage( id + " is not a integer.", WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
				case FindPeakLocationProcessing.MIN_PROMINENCE:
				{
					try
					{
						double val = Double.parseDouble( par.getValue() );
						
						if( val < 0 )
						{
							warn.addMessage( id + " must be a double greater or equal to 0.", WarningMessage.ERROR_MESSAGE );
						}
					}
					catch (Exception e) 
					{ 
						warn.addMessage( id + " is not a double.", WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
				case FindPeakLocationProcessing.BUFFER_LEN :
				case FindPeakLocationProcessing.SHIFT_LEN:
				{
					try
					{
						int val = Integer.parseInt( par.getValue() );
						
						if( val < 1 )
						{
							warn.addMessage( id + " must be a integer greater or equal to 1.", WarningMessage.ERROR_MESSAGE );
						}
					}
					catch (Exception e) 
					{ 
						warn.addMessage( id + " is not a integer.", WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
				case FindPeakLocationProcessing.PEAK_TYPE:
				{
					try
					{
						Peak_type.valueOf( par.getValue() );
					}
					catch (Exception e) 
					{
						warn.addMessage( id + " is not correct.", WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
				case FindPeakLocationProcessing.ADJUST_SHIFTING:
				{
					try
					{
						Boolean.parseBoolean( par.getValue() );
					}
					catch (Exception e) 
					{
						warn.addMessage( id + " is not a boolean.", WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
				default:
					break;
			}
		}
		
		return warn;
	}

	@Override
	public PluginType getType() 
	{
		return PluginType.DATA_PROCESSING;		
	}

	@Override
	public String getID() 
	{
		return "findPeaks";
	}

	@Override
	public int compareTo(ILSLRecPlugin o) 
	{
		return this.getID().compareTo( o.getID() );
	}

	@Override
	public LSLRecPluginDataProcessing getProcessing(PluginDataProcessingSettings setting, LSLRecPluginDataProcessing process) 
	{
		FindPeakLocationProcessing fp = new FindPeakLocationProcessing( setting.getStreamSettings(), process );
		fp.loadProcessingSettings( super.getSettings() );
		
		return fp;
	}

	@Override
	public ProcessingLocation getProcessingLocation() 
	{
		return ProcessingLocation.DURING;
	}

	@Override
	protected void postLoadSettings() 
	{		
	}

	@Override
	protected void setSettingPanel(JPanel panel) 
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
					case  FindPeakLocationProcessing.SHIFT_LEN:
					case  FindPeakLocationProcessing.BUFFER_LEN:
					{
						try
						{
							Double.parseDouble( val );
						}
						catch (Exception e) 
						{
							val = "1";
						}
						
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
									pars.get( p.getID() ).setValue( ((int)Double.parseDouble( p.getValue() )) + "" );
								}
								catch (Exception ex) 
								{
									ex.printStackTrace();
								}
							}
						});
						
						SettingOptions opt = new SettingOptions( id, SettingOptions.Type.NUMBER, false, new NumberRange( 1D, Integer.MAX_VALUE ), id );
						opt.addValue( val );
						opts.add( opt );
						
						break;
					}
					case  FindPeakLocationProcessing.MIN_DISTANCE:
					{
						try
						{
							Double.parseDouble(val);
						}
						catch (Exception e) 
						{
							val = "1";
						}
						
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
									pars.get( p.getID() ).setValue( ((int)(Double.parseDouble( p.getValue() ) )) + "" );
								}
								catch (Exception ex) 
								{}
							}
						});
						
						SettingOptions opt = new SettingOptions( id, SettingOptions.Type.NUMBER, false, new NumberRange( 0D, Integer.MAX_VALUE ), id );
						opt.addValue( val );
						opts.add( opt );
						
						break;
					}
					case FindPeakLocationProcessing.MIN_PROMINENCE:
					{
						try
						{
							Double.parseDouble( val );
						}
						catch (Exception e) 
						{
							val = "0";
						}
						
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
						
						SettingOptions opt = new SettingOptions( id, SettingOptions.Type.NUMBER, false, new NumberRange( 0D, Double.MAX_VALUE ), id );
						opt.addValue( val );
						opts.add( opt );
						
						break;
					}
					case FindPeakLocationProcessing.PEAK_TYPE:
					{
						try
						{
							Peak_type.valueOf( val );
						}
						catch (Exception e) 
						{
							val = Peak_type.MIN_MAX.name();
						}
						
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
									pars.get( p.getID() ).setValue( Peak_type.valueOf( p.getValue() ).name() + "" );
								}
								catch (Exception ex) 
								{}
							}
						});
						
						SettingOptions opt = new SettingOptions( id, SettingOptions.Type.STRING, true, null, id );
						opt.addValue( Peak_type.MIN_MAX.name() );
						opt.addValue( Peak_type.MIN.name() );
						opt.addValue( Peak_type.MAX.name() );
						opt.setSelectedValue( 0 );
						opts.add( opt );
						
						break;
					}
					case FindPeakLocationProcessing.ADJUST_SHIFTING:
					{
						try
						{
							Boolean.parseBoolean( val );
						}
						catch (Exception e) 
						{
							val = "false";
						}
						
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
									pars.get( p.getID() ).setValue( Boolean.parseBoolean( p.getValue() ) + "" );
								}
								catch (Exception ex) 
								{}
							}
						});
						
						SettingOptions opt = new SettingOptions( id, SettingOptions.Type.BOOLEAN, false, null, id );
						opt.addValue( "false" );
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
			p2.add( new JLabel( "Peak detection in samples." ), BorderLayout.NORTH );
			
			panel.add( p2, BorderLayout.NORTH );
			panel.setVisible( true );
		}
	}

}
