package lslrec.plugin.impl.dataProcessing.envelopFilter;

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
import lslrec.plugin.impl.dataProcessing.envelopFilter.EnvelopFilterProcessing.Envelop_type;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.LSLRecConfigurablePluginAbstract;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.PluginDataProcessingSettings;

public class EnvelopFilterPlugin extends LSLRecConfigurablePluginAbstract implements ILSLRecPluginDataProcessing
{
	public EnvelopFilterPlugin() 
	{
		super();
		
		Parameter< String > par = new Parameter<String>( EnvelopFilterProcessing.ENVELOP_TYPE, Envelop_type.LOWER.name() );
		super.pars.put( par.getID(), par );
		
		par = new Parameter<String>( EnvelopFilterProcessing.PEAK_DIST, "0" );
		super.pars.put( par.getID(), par );
		
		par = new Parameter<String>( EnvelopFilterProcessing.BUFFER_LEN, "1" );
		super.pars.put( par.getID(), par );
		
		par = new Parameter<String>( EnvelopFilterProcessing.SHIFT_LEN, "1" );
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
				case EnvelopFilterProcessing.ENVELOP_TYPE :
				{
					try
					{
						Envelop_type.valueOf( par.getValue() );						
					}
					catch (Exception e) 
					{ 
						warn.addMessage( id + " is not a envelop type.", WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
				case EnvelopFilterProcessing.PEAK_DIST:
				{
					try
					{
						int val = (int)(Double.parseDouble( par.getValue() ) );
						
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
				case EnvelopFilterProcessing.BUFFER_LEN :
				case EnvelopFilterProcessing.SHIFT_LEN:
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
		return "EnvelopFilter";
	}

	@Override
	public int compareTo(ILSLRecPlugin o) 
	{
		return this.getID().compareTo( o.getID() );
	}

	@Override
	public LSLRecPluginDataProcessing getProcessing(PluginDataProcessingSettings arg0, LSLRecPluginDataProcessing arg1) 
	{
		EnvelopFilterProcessing ef = new EnvelopFilterProcessing( arg0.getStreamSettings(), arg1 );
		ef.loadProcessingSettings( super.getSettings());
		
		return ef;
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
					case  EnvelopFilterProcessing.PEAK_DIST:
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
									String v = p.getValue();
									pars.get( p.getID() ).setValue( ((int)Double.parseDouble( v )) + "" );
								}
								catch (Exception ex) 
								{}
							}
						});
						
						SettingOptions opt = new SettingOptions( id, SettingOptions.Type.NUMBER, false, new NumberRange( 0, Integer.MAX_VALUE ), id );
						opt.addValue( val );
						opts.add( opt );
						
						break;
					}
					case  EnvelopFilterProcessing.BUFFER_LEN:
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
					case  EnvelopFilterProcessing.SHIFT_LEN:
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
					case EnvelopFilterProcessing.ENVELOP_TYPE:
					{
						try
						{
							Envelop_type.valueOf( val );
						}
						catch (Exception e) 
						{
							val = Envelop_type.LOWER.name();
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
									Envelop_type.valueOf( p.getValue() );
									
									pars.get( p.getID() ).setValue( p.getValue() + "" );
								}
								catch (Exception ex) 
								{}
							}
						});
						
						SettingOptions opt = new SettingOptions( id, SettingOptions.Type.STRING, true, null, id );
						opt.addValue( Envelop_type.LOWER.name() );
						opt.addValue( Envelop_type.UPPER.name() );
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
			
			panel.add( p, BorderLayout.NORTH );
			panel.setVisible( true );
		}	
	}


}
