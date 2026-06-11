package lslrec.plugin.impl.dataProcessing.locationVariability;

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
import lslrec.plugin.impl.dataProcessing.locationVariability.LocationVariabilityProcessing.CUT_TAILS_PERCENT;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.LSLRecConfigurablePluginAbstract;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.PluginDataProcessingSettings;

public class LocationVariabilityPlugin extends LSLRecConfigurablePluginAbstract implements ILSLRecPluginDataProcessing
{
	public LocationVariabilityPlugin() 
	{
		super();
		
		Parameter< String > par = new Parameter<String>( LocationVariabilityProcessing.BUFFER_LEN, "2" );
		super.pars.put( par.getID(), par );
		
		par = new Parameter<String>( LocationVariabilityProcessing.SHIFT_LEN, "1" );
		super.pars.put( par.getID(), par );
		
		par = new Parameter<String>( LocationVariabilityProcessing.FREQ, "1" );
		super.pars.put( par.getID(), par );
		
		par = new Parameter<String>( LocationVariabilityProcessing.CUT_TAILS, CUT_TAILS_PERCENT.P0.name() );
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
				case LocationVariabilityProcessing.BUFFER_LEN :
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
				case LocationVariabilityProcessing.SHIFT_LEN:
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
				case LocationVariabilityProcessing.FREQ:
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
				case LocationVariabilityProcessing.CUT_TAILS:
				{
					try
					{
						LocationVariabilityProcessing.CUT_TAILS_PERCENT.valueOf( par.getValue() );						
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
		return "LocationVariability";
	}

	@Override
	public int compareTo(ILSLRecPlugin o) 
	{
		return this.getID().compareTo( o.getID() );
	}

	@Override
	public LSLRecPluginDataProcessing getProcessing(PluginDataProcessingSettings setting, LSLRecPluginDataProcessing process) 
	{
		LocationVariabilityProcessing lvp = new LocationVariabilityProcessing( setting.getStreamSettings(), process );
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
					case  LocationVariabilityProcessing.BUFFER_LEN:
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
					case  LocationVariabilityProcessing.SHIFT_LEN:
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
					case LocationVariabilityProcessing.FREQ:
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
					case LocationVariabilityProcessing.CUT_TAILS:
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
			p2.add( new JLabel( "out=std( diff( inputs )/freq )" ), BorderLayout.NORTH );
			
			panel.add( p2, BorderLayout.NORTH );
			panel.setVisible( true );
		}	
	}

}
