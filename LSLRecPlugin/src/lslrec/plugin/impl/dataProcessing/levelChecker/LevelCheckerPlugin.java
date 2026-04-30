package lslrec.plugin.impl.dataProcessing.levelChecker;

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
import lslrec.gui.panel.plugin.item.CreatorDefaultSettingPanel;
import lslrec.gui.setting.SettingOptions;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.LSLRecConfigurablePluginAbstract;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.PluginDataProcessingSettings;

public class LevelCheckerPlugin extends LSLRecConfigurablePluginAbstract implements ILSLRecPluginDataProcessing 
{
	private ParameterList pars = null;
	/**
	 * 
	 */
	public LevelCheckerPlugin( ) 
	{
		this.pars = new ParameterList();
		
		Parameter par = new Parameter< NumberRange >( LevelCheckerProcessing.WARNING_RANGE, new NumberRange( 16, 30) );
		this.pars.addParameter( par );
		
		par = new Parameter< NumberRange >( LevelCheckerProcessing.ALERT_RANGE, new NumberRange( 0, 15 ) );
		this.pars.addParameter( par );
		
		par = new Parameter< Integer >( LevelCheckerProcessing.CHECK_TIMER, 10 );
		this.pars.addParameter( par );		
	}
	
	
	@Override
	public WarningMessage checkSettings() 
	{
		WarningMessage wm = new WarningMessage();
		
		for( String id : this.pars.getParameterIDs() )
		{
			Parameter par = this.pars.getParameter( id );
			switch ( id ) 
			{
				case LevelCheckerProcessing.CHECK_TIMER:
				{			
					
					if( (Integer)par.getValue() <= 0 )
					{
						wm.addMessage( "Check time must be greater than 0.", WarningMessage.ERROR_MESSAGE );
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
		return "LevelChecker";
	}

	@Override
	public int compareTo(ILSLRecPlugin o) 
	{
		return this.getID().compareTo( o.getID() );
	}

	@Override
	public LSLRecPluginDataProcessing getProcessing( PluginDataProcessingSettings settings, LSLRecPluginDataProcessing proc) 
	{
		LevelCheckerProcessing process = new LevelCheckerProcessing( settings.getStreamSettings(), proc );
		return process;
	}

	@Override
	public ProcessingLocation getProcessingLocation() 
	{
		return ProcessingLocation.DURING;
	}

	@Override
	public void loadSettings(List<Parameter<String>> pars) 
	{
		for( Parameter< String > par : pars )
		{	
			String id = par.getID();
			String val = par.getValue();
			
			switch( id ) 
			{
				case LevelCheckerProcessing.CHECK_TIMER:
				{
					this.pars.getParameter( id ).setValue( Integer.parseInt( val ) );
					
					break;
				}
				case LevelCheckerProcessing.ALERT_RANGE:
				case LevelCheckerProcessing.WARNING_RANGE:
				{
					this.pars.getParameter( id ).setValue( NumberRange.parseNumberRange( val ) );
					
					break;
				}
				default:
					break;
			}
		}
	}

	@Override
	protected void setSettingPanel( JPanel panel ) 
	{
		if( panel != null )
		{
			panel.removeAll();
			
			List< String > parIDs = new ArrayList<String>();
			parIDs.add( LevelCheckerProcessing.WARNING_RANGE );
			parIDs.add( LevelCheckerProcessing.ALERT_RANGE );
			parIDs.add( LevelCheckerProcessing.CHECK_TIMER );
			
			List< SettingOptions > opts = new  ArrayList< SettingOptions >();			
			ParameterList parList = new ParameterList();
			
			for( String id : parIDs )
			{				
				Parameter p = this.pars.getParameter( id) ;
				p.setLangID( id );
								
				Object val = p.getValue();
				
				switch( id ) 
				{
					case LevelCheckerProcessing.CHECK_TIMER:
					{
						SettingOptions opt = new SettingOptions( id, SettingOptions.Type.NUMBER, false, null, id );
						opt.addValue( p.getValue().toString() );
						opts.add( opt );
						
						parList.addParameter( p );
						
						break;
					}
					case LevelCheckerProcessing.ALERT_RANGE:
					case LevelCheckerProcessing.WARNING_RANGE:
					{
						NumberRange range = (NumberRange)p.getValue();
						
						double min = range.getMin();
						double max = range.getMax();
						
						Parameter< Double > pmin = new Parameter<Double>( id+"-min", min );
						pmin.addValueChangeListener( new ChangeListener() 
						{	
							@Override
							public void stateChanged(ChangeEvent e) 
							{
								Parameter< Double > p = (Parameter<Double>)e.getSource();
								
								Parameter par = pars.getParameter( id );
								
								NumberRange range = (NumberRange)par.getValue();
								
								NumberRange newRange = new NumberRange( p.getValue(), range.getMax() );
								par.setValue( newRange );
							}
						});
						parList.addParameter( pmin );
						
						Parameter< Double > pmax = new Parameter<Double>( id+"-max", max );
						pmax.addValueChangeListener( new ChangeListener() 
						{	
							@Override
							public void stateChanged(ChangeEvent e) 
							{
								Parameter< Double > p = (Parameter<Double>)e.getSource();
								
								Parameter par = pars.getParameter( id );
								
								NumberRange range = (NumberRange)par.getValue();
								
								NumberRange newRange = new NumberRange( range.getMin(), p.getValue());
								par.setValue( newRange );
							}
						});
						parList.addParameter( pmax );
						
						SettingOptions opt = new SettingOptions( pmin.getID(), SettingOptions.Type.NUMBER, false, null, pmin.getID() );
						opt.addValue( pmin.getValue().toString() );
						opts.add( opt );
						
						opt = new SettingOptions( pmax.getID(), SettingOptions.Type.NUMBER, false, null, pmax.getID() );
						opt.addValue( pmax.getValue().toString() );
						opts.add( opt );
						
						
						break;
					}
					default:
						break;
				}				
			}
			

			
			JPanel p = CreatorDefaultSettingPanel.getSettingPanel( opts, parList );			
			
			panel.setLayout( new BorderLayout() );
			panel.add( p, BorderLayout.NORTH );
			panel.setVisible( true );
		}
	}


	@Override
	protected void postLoadSettings() 
	{	
	}

}
