/**
 * 
 */
package lslrec.plugin.impl.gui.alarm;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import lslrec.auxiliar.WarningMessage;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.config.SettingOptions;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.gui.panel.plugin.item.CreatorDefaultSettingPanel;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.trial.ILSLRecPluginTrial;
import lslrec.plugin.lslrecPlugin.trial.LSLRecPluginTrial;
import lslrec.stoppableThread.IStoppableThread;

/**
 * @author Manuel Merino Monge
 *
 */
public class PluginAlarmTest  implements ILSLRecPluginTrial 
{
	private ParameterList pars = new ParameterList();
	
	private AlarmBeep alarmBeep;
	/**
	 * 
	 */
	public PluginAlarmTest() 
	{
		Parameter par = new Parameter( AlarmTest.BEEP_NUMBER, 10 );
		this.pars.addParameter( par );
		
		par = new Parameter( AlarmTest.BEEP_REP, 1 );
		this.pars.addParameter( par );

		par = new Parameter( AlarmTest.FREQUENCY_TONE, 1500 );
		this.pars.addParameter( par );
		
		par = new Parameter( AlarmTest.PRE_TEST_TIME, 1000 );
		this.pars.addParameter( par );
		
		par = new Parameter( AlarmTest.TIME_BETWEEN_BEEP, 1000 );
		this.pars.addParameter( par );
		
		par = new Parameter( AlarmTest.TONE_DURATION, 150 );
		this.pars.addParameter( par );
		
		par = new Parameter( AlarmTest.VOLUMEN_TONE, 500 );
		this.pars.addParameter( par );		
	}

	@Override
	public WarningMessage checkSettings() 
	{
		WarningMessage msg = new WarningMessage();
		
		for( String id : this.pars.getParameterIDs() )
		{
			Integer val = (Integer)this.pars.getParameter( id ).getValue();
			
			if( val <= 0 )
			{
				msg.addMessage( id + " must be non-zero.", WarningMessage.ERROR_MESSAGE );
			}
		}
		
		return msg;
	}

	@Override
	public JPanel getSettingPanel() 
	{
		List< SettingOptions > opts = new ArrayList< SettingOptions >();
		for( String id : this.pars.getParameterIDs() )
		{
			SettingOptions st = new SettingOptions( id, SettingOptions.Type.NUMBER, false, null, id );
			st.addValue( this.pars.getParameter( id ).getValue().toString() );
			
			opts.add( st );
		}
		
		JPanel p = CreatorDefaultSettingPanel.getSettingPanel( opts, this.pars );
		
		JPanel panel = new JPanel( new BorderLayout() );
		
		
		JButton b = new JButton( "Sound test" );
		b.setIcon( GeneralAppIcon.Sound( 20, 20, Color.BLACK, null ) );
		b.setBorder( BorderFactory.createEtchedBorder() );
		
		b.addActionListener( new ActionListener() 
		{	
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				int freq = 1500, vol = 500, msecs = 150, beep_rep = 1;
				
				for( String id : pars.getParameterIDs() )
				{
					switch ( id )
					{
						case AlarmTest.FREQUENCY_TONE:
						{
							freq = (Integer)pars.getParameter( id ).getValue();
							break;
						}
						case AlarmTest.VOLUMEN_TONE:
						{
							vol = (Integer)pars.getParameter( id ).getValue();
							break;
						}
						case AlarmTest.TONE_DURATION:
						{
							msecs = (Integer)pars.getParameter( id ).getValue();
							break;
						}
						case AlarmTest.BEEP_REP:
						{
							beep_rep = (Integer)pars.getParameter( id ).getValue();
							break;
						}
						default:
						{
							break;	
						}				
					}
				}
				
				if( alarmBeep != null )
				{
					alarmBeep.stopThread( IStoppableThread.FORCE_STOP );
				}
				
				alarmBeep = new AlarmBeep( freq, msecs, vol, beep_rep, 300, null );
				
				try 
				{
					alarmBeep.startThread();
					
					while( !alarmBeep.getState().equals( Thread.State.WAITING ) 
							&& !alarmBeep.getState().equals( Thread.State.TIMED_WAITING ) )
					{
						synchronized( this )
						{
							super.wait( 10L );
						}						
					}
					
					synchronized( alarmBeep )
					{
						alarmBeep.notify();
					}
				}
				catch (Exception e1) 
				{
					e1.printStackTrace();
				}
				
			}
		});
		
		panel.add( p, BorderLayout.CENTER );
		panel.add( b, BorderLayout.NORTH );
		
		JPanel outPanel = new JPanel( new BorderLayout() );
		outPanel.add( panel, BorderLayout.NORTH );
		
		return outPanel;
	}

	@Override
	public List<Parameter<String>> getSettings() 
	{
		List< Parameter< String > > parList = new ArrayList< Parameter< String > >();
		
		for( String id : this.pars.getParameterIDs() )
		{
			parList.add( new Parameter<String>( id, this.pars.getParameter( id ).getValue().toString() ) );
		}
		
		return parList;
	}

	@Override
	public void loadSettings( List< Parameter< String > > arg0) 
	{
		if( arg0 != null )
		{
			for( Parameter< String > par : arg0 )
			{
				String id = par.getID();
				
				Parameter p = this.pars.getParameter( par.getID() );
				
				if( p != null )
				{
					Object v = par.getValue();
					
					if( v != null )
					{						
						Number val = null;
						switch( id )
						{
							case AlarmTest.FREQUENCY_TONE:
							case AlarmTest.TONE_DURATION:
							case AlarmTest.VOLUMEN_TONE:
							case AlarmTest.BEEP_NUMBER:
							case AlarmTest.BEEP_REP:
							case AlarmTest.PRE_TEST_TIME:
							case AlarmTest.TIME_BETWEEN_BEEP:
							{
								val = Integer.parseInt( v.toString()  );
								
								break;
							}						
						}
						
						if( val != null )
						{
							p.setValue( val );
						}
					}
				}
			}
		}
	}

	@Override
	public PluginType getType() 
	{
		return PluginType.TRIAL;
	}

	@Override
	public String getID() 
	{
		return "Alarm";
	}

	@Override
	public int compareTo( ILSLRecPlugin o ) 
	{
		return o.getID().compareTo( this.getID() );
	}

	@Override
	public LSLRecPluginTrial getGUIExperiment() 
	{
		if( this.alarmBeep != null )
		{
			this.alarmBeep.stopThread( IStoppableThread.FORCE_STOP );
		}
		AlarmTest alarm = new AlarmTest();
	
		alarm.loadSettings( this.getSettings() );
	
		return alarm;
	}

	@Override
	public String getLogDescription() 
	{
		return "Alarm";
	}

	@Override
	public boolean hasTrialLog() 
	{
		return false;
	}

}
