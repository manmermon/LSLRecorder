/**
 * 
 */
package lslrec.plugin.impl.gui.alarm;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.Timer;

import lslrec.auxiliar.task.ITaskLog;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.dataStream.sync.SyncMarker;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.plugin.lslrecPlugin.sync.LSLRecPluginSyncMethod;
import lslrec.plugin.lslrecPlugin.trial.LSLRecPluginTrial;
import lslrec.stoppableThread.IStoppableThread;

/**
 * @author Manuel Merino Monge
 *
 */
public class AlarmTest extends LSLRecPluginTrial 
{
	public static final int ALARM = 4;
	
	public static final String FREQUENCY_TONE = "Frequency";
	public static final String TONE_DURATION = "Tone duration";
	public static final String VOLUMEN_TONE = "Volumen";
	public static final String BEEP_REP = "Tone repetition";
	public static final String BEEP_NUMBER = "Number of beeps";
	
	public static final String PRE_TEST_TIME = "Pre-test time";
	public static final String TIME_BETWEEN_BEEP = "Time between beeps";
	
	private int hz = 1500;
	private int msecs = 150;
	private int vol = 500;
	private int beep_rep = 1;
	private int beep_number =  10;
	
	private int time_between_beep = 1000;
	private int time_pretest = 1000;
	
	private int wait_between_tone = 300;
	
	private ParameterList parameters;

	private int stage = SyncMarker.NON_MARK;
	
	private Timer betweenTimer;
	private AlarmBeep beepAlarm;
	
	public AlarmTest() 
	{
		this.parameters = new ParameterList();
		
		Parameter< Integer > par = new Parameter<Integer>( FREQUENCY_TONE, 1500 );				
		this.parameters.addParameter( par );
		
		par = new Parameter<Integer>( TONE_DURATION, 150 );				
		this.parameters.addParameter( par );
		
		par = new Parameter<Integer>( VOLUMEN_TONE, 500 );				
		this.parameters.addParameter( par );
		
		par = new Parameter<Integer>( BEEP_REP, 1 );				
		this.parameters.addParameter( par );
		
		par = new Parameter<Integer>( BEEP_NUMBER, 10 );				
		this.parameters.addParameter( par );
		
		par = new Parameter<Integer>( TIME_BETWEEN_BEEP, 1000 );				
		this.parameters.addParameter( par );
		
		par = new Parameter<Integer>( PRE_TEST_TIME, 1000 );				
		this.parameters.addParameter( par );
	}
	
	@Override
	public String getID() 
	{
		return "Alarm";
	}

	@Override
	public int getStageMark() 
	{
		return this.stage;
	}

	
	@Override
	public void loadSettings(List<Parameter<String>> arg0) 
	{
		for( Parameter< String > par : arg0 )
		{
			String id = par.getID();
			String val = par.getValue();
			
			switch( id )
			{
				case FREQUENCY_TONE:
				case TONE_DURATION:
				case VOLUMEN_TONE:
				case BEEP_NUMBER:
				case BEEP_REP:
				case PRE_TEST_TIME:
				case TIME_BETWEEN_BEEP:
				{
					int value = Integer.parseInt( val );
					
					this.parameters.getParameter( id ).setValue( value );
					
					break;
				}
			}
		}
	}
	
	@Override
	protected void startUp() throws Exception 
	{
		super.startUp();
		
		for( String id : this.parameters.getParameterIDs() )
		{
			switch ( id )
			{
				case FREQUENCY_TONE:
				{
					this.hz = (Integer)this.parameters.getParameter( id ).getValue();
					break;
				}
				case VOLUMEN_TONE:
				{
					this.vol = (Integer)this.parameters.getParameter( id ).getValue();
					break;
				}
				case TONE_DURATION:
				{
					this.msecs = (Integer)this.parameters.getParameter( id ).getValue();
					break;
				}
				case BEEP_REP:
				{
					this.beep_rep = (Integer)this.parameters.getParameter( id ).getValue();
					break;
				}
				case BEEP_NUMBER:
				{
					this.beep_number = (Integer)this.parameters.getParameter( id ).getValue();
					break;
				}
				case PRE_TEST_TIME:
				{
					this.time_pretest = (Integer)this.parameters.getParameter( id ).getValue();
					break;
				}
				case TIME_BETWEEN_BEEP:
				{
					this.time_between_beep = (Integer)this.parameters.getParameter( id ).getValue();
					break;
				}
				default:
				{
					break;	
				}				
			}
		}
		
		this.stage = SyncMarker.START_MARK;
		
		LSLRecPluginSyncMethod sync = getSyncMethod();
		
		if( sync != null )
		{
			synchronized( sync )
			{
				sync.notify();
			}
		}
		
		try 
		{
			super.wait( this.time_pretest );
		}
		catch (Exception e) 
		{			
		}
		
		this.betweenTimer = new Timer( this.time_between_beep, this.getBetweenTimerAction() );
		this.betweenTimer.setRepeats( false );
		
		this.beepAlarm = new AlarmBeep( this.hz, this.msecs, this.vol, this.beep_rep, this.wait_between_tone, this.getBetweenTimerAction() );
		this.beepAlarm.startThread();
	}
	
	
	private ActionListener getBetweenTimerAction()
	{
		return new ActionListener() 
				{			
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						wakeUp();
					}
				};
	}
	
	private void wakeUp()
	{
		synchronized ( this )
		{
			super.notify();
		}
	}
	
	@Override
	public void setStage( JPanel pn )
	{	
		String t = "Alarm";
		ImageIcon ic  = null;
		
		switch ( this.stage ) 
		{
			case SyncMarker.NON_MARK:
			{
				this.stage = ALARM;		
				
				ic  = GeneralAppIcon.Sound( 128, 128, Color.BLACK, Color.WHITE );;
				t = "Alarm";
				
				break;
			}
			default:
			{
				this.stage = SyncMarker.NON_MARK;
				
				ic = new ImageIcon( GeneralAppIcon.Clock().getImage().getScaledInstance( 128, 128, Image.SCALE_SMOOTH ) );
				t = "None";
				
				break;
			}
		}
		
		if( pn != null )
		{			
			pn.setVisible( false );
			pn.removeAll();
			
			JButton b = new JButton();
			b.setBackground( Color.WHITE );
			
			b.setBorder( BorderFactory.createEtchedBorder() );
								
			if( ic != null )
			{
				b.setText( null );
				b.setIcon( ic );
			}
			else
			{
				b.setText( t );
			}
			
			pn.add( b );
			pn.setVisible( true );
		}
		
		if( this.stage == ALARM )
		{	
			synchronized( this.beepAlarm )
			{
				this.beepAlarm.notify();
			}
		}
		else
		{
			this.betweenTimer.restart();
		}
		
		if( this.stage == ALARM )
		{
			this.beep_number--;
		}
		
		if( this.stage == SyncMarker.NON_MARK )
		{	
			if( this.beep_number <= 0 )
			{			
				this.stage = SyncMarker.STOP_MARK;
				
				LSLRecPluginSyncMethod sync = getSyncMethod();
				
				if( sync != null )
				{
					synchronized( sync )
					{
						sync.notify();
					}
				}
				
				super.stopThread( IStoppableThread.STOP_IN_NEXT_LOOP );			
			}
		}
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
		
		if( this.beepAlarm != null )
		{
			this.beepAlarm.stopThread( IStoppableThread.FORCE_STOP );
		}
		
		if( this.betweenTimer != null )
		{
			this.betweenTimer.stop();
		}
	}
	
	@Override
	public void setTrialLogStream(ITaskLog arg0)
	{	
	}

	@Override
	protected void postStopThread(int arg0) throws Exception 
	{	
	}

	@Override
	protected void preStopThread(int arg0) throws Exception 
	{	
	}
}
