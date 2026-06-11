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
package lslrec.plugin.impl.dataProcessing.locationMedian;

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
import lslrec.plugin.impl.dataProcessing.locationMedian.LocationMedianProcessing.CUT_TAILS_PERCENT;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.LSLRecConfigurablePluginAbstract;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.PluginDataProcessingSettings;

public class LocationMedianPlugin extends LSLRecConfigurablePluginAbstract implements ILSLRecPluginDataProcessing
{
	public LocationMedianPlugin() 
	{
		super();
		
		Parameter< String > par = new Parameter<String>( LocationMedianProcessing.BUFFER_LEN, "2" );
		super.pars.put( par.getID(), par );
		
		par = new Parameter<String>( LocationMedianProcessing.SHIFT_LEN, "1" );
		super.pars.put( par.getID(), par );
		
		par = new Parameter<String>( LocationMedianProcessing.FREQ, "1" );
		super.pars.put( par.getID(), par );
		
		par = new Parameter<String>( LocationMedianProcessing.CUT_TAILS, CUT_TAILS_PERCENT.P0.name() );
		super.pars.put( par.getID(), par );
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
				case LocationMedianProcessing.BUFFER_LEN :
				{
					try
					{
						int val = Integer.parseInt( par.getValue() );
						
						if( val < 2 )
						{
							warn.addMessage( id + " must be a integer greater or equal to 2.", WarningMessage.ERROR_MESSAGE );
						}
					}
					catch (Exception e) 
					{ 
						warn.addMessage( id + " is not a integer.", WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
				case LocationMedianProcessing.SHIFT_LEN:
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
				case LocationMedianProcessing.FREQ:
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
				case LocationMedianProcessing.CUT_TAILS:
				{
					try
					{
						LocationMedianProcessing.CUT_TAILS_PERCENT.valueOf( par.getValue() );						
					}
					catch (Exception e) 
					{ 
						warn.addMessage( id + " is not a correct value.", WarningMessage.ERROR_MESSAGE );
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
		return "DiffLocationMedian";
	}

	@Override
	public int compareTo(ILSLRecPlugin o) 
	{
		return this.getID().compareTo( o.getID() );
	}

	@Override
	public LSLRecPluginDataProcessing getProcessing(PluginDataProcessingSettings setting, LSLRecPluginDataProcessing process) 
	{
		LocationMedianProcessing lvp = new LocationMedianProcessing( setting.getStreamSettings(), process );
		lvp.loadProcessingSettings( super.getSettings() );
		
		return lvp;
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
					case  LocationMedianProcessing.BUFFER_LEN:
					{
						try
						{
							Integer.parseInt( val );
						}
						catch (Exception e) 
						{
							val = "2";
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
									String v = p.getValue();
									pars.get( p.getID() ).setValue( ((int)Double.parseDouble( v )) + "" );
								}
								catch (Exception ex) 
								{}
							}
						});
						
						SettingOptions opt = new SettingOptions( id, SettingOptions.Type.NUMBER, false, new NumberRange( 2, Integer.MAX_VALUE ), id );
						opt.addValue( val );
						opts.add( opt );
						
						break;
					}
					case  LocationMedianProcessing.SHIFT_LEN:
					{
						try
						{
							Integer.parseInt( val );
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
									String v = p.getValue();
									pars.get( p.getID() ).setValue( ((int)Double.parseDouble( v )) + "" );
								}
								catch (Exception ex) 
								{}
							}
						});
						
						SettingOptions opt = new SettingOptions( id, SettingOptions.Type.NUMBER, false, new NumberRange( 1, Integer.MAX_VALUE ), id );
						opt.addValue( val );
						opts.add( opt );
						
						break;
					}
					case LocationMedianProcessing.FREQ:
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
					case LocationMedianProcessing.CUT_TAILS:
					{
						try
						{
							CUT_TAILS_PERCENT.valueOf( val );
						}
						catch (Exception e) 
						{
							val = CUT_TAILS_PERCENT.P0.name();
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
									CUT_TAILS_PERCENT.valueOf( p.getValue() );
									
									pars.get( p.getID() ).setValue( p.getValue() );
								}
								catch (Exception ex) 
								{}
							}
						});
						
						SettingOptions opt = new SettingOptions( id, SettingOptions.Type.STRING, true, null, id );
						opt.addValue( CUT_TAILS_PERCENT.P0.name() );
						opt.addValue( CUT_TAILS_PERCENT.P5.name() );
						opt.addValue( CUT_TAILS_PERCENT.P10.name() );
						opt.addValue( CUT_TAILS_PERCENT.P15.name() );
						opt.addValue( CUT_TAILS_PERCENT.P20.name() );
						opt.addValue( CUT_TAILS_PERCENT.P25.name() );
						opt.addValue( CUT_TAILS_PERCENT.P50.name() );
						opt.setSelectedValue( 0 );
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
			p2.add( new JLabel( "out=median( diff( inputs )/freq )" ), BorderLayout.NORTH );
			
			panel.add( p2, BorderLayout.NORTH );
			panel.setVisible( true );
		}	
	}

}
