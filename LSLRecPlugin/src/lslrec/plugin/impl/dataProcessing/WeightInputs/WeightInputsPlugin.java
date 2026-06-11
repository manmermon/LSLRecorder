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
package lslrec.plugin.impl.dataProcessing.WeightInputs;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;

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

public class WeightInputsPlugin extends LSLRecConfigurablePluginAbstract implements ILSLRecPluginDataProcessing
{
	public WeightInputsPlugin() 
	{
		super();
		
		Parameter< String > par = new Parameter<String>( WeightInputsProcessing.MULT, "1" );
		super.pars.put( par.getID(), par );		
		
		par = new Parameter<String>( WeightInputsProcessing.DIV, "1" );
		super.pars.put( par.getID(), par );
		
		par = new Parameter<String>( WeightInputsProcessing.INV, "false" );
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
				case WeightInputsProcessing.MULT :
				{
					try
					{
						Double.parseDouble( par.getValue() );
					}
					catch (Exception e) 
					{ 
						warn.addMessage( id + " is not a double.", WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
				case WeightInputsProcessing.DIV :
				{
					try
					{
						double v = Double.parseDouble( par.getValue() );
						
						if( v == 0 )
						{
							warn.addMessage( id + " is equal to 0.", WarningMessage.ERROR_MESSAGE );
						}
					}
					catch (Exception e) 
					{ 
						warn.addMessage( id + " is not a double.", WarningMessage.ERROR_MESSAGE );
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
		return "WeigthInputs";
	}

	@Override
	public int compareTo(ILSLRecPlugin o) 
	{
		return this.getID().compareTo( o.getID() );
	}

	@Override
	public LSLRecPluginDataProcessing getProcessing(PluginDataProcessingSettings setting, LSLRecPluginDataProcessing process) 
	{
		WeightInputsProcessing lvp = new WeightInputsProcessing( setting.getStreamSettings(), process );
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
			
			JLabel descriptor = new JLabel( "out= in * mult / div" );
			
			List< SettingOptions > opts = new  ArrayList< SettingOptions >();			
			ParameterList parList = new ParameterList();
			
			for( Parameter< String > par : super.getSettings() )
			{
				String id = par.getID();
				String val = par.getValue();
								
				switch ( id ) 
				{	
					case  WeightInputsProcessing.MULT:
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
									String v = p.getValue();
									pars.get( p.getID() ).setValue( Double.parseDouble( v ) + "" );
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
					case  WeightInputsProcessing.DIV:
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
									String v = p.getValue();
									pars.get( p.getID() ).setValue( Double.parseDouble( v ) + "" );																		
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
					case  WeightInputsProcessing.INV:
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
									String v = p.getValue();
									boolean inv = Boolean.parseBoolean( v );
									pars.get( p.getID() ).setValue(  inv+ "" );
									
									if( !inv )
									{
										descriptor.setText( "out= in * mult / div" );
									}
									else
									{
										descriptor.setText( "out= (1/in) * mult / div" );
									}
								}
								catch (Exception ex) 
								{}
							}
						});
						
						SettingOptions opt = new SettingOptions( id, SettingOptions.Type.BOOLEAN, false, null, id );
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
			p2.add( descriptor, BorderLayout.NORTH );
			
			panel.add( p2, BorderLayout.NORTH );
			panel.setVisible( true );
		}	
	}

}
