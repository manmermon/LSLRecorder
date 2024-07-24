package lslrec.plugin.impl.gui.trialStagesMarker;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import lslrec.auxiliar.task.ITaskLog;
import lslrec.config.Parameter;
import lslrec.dataStream.sync.SyncMarker;
import lslrec.gui.miscellany.VerticalFlowLayout;
import lslrec.plugin.lslrecPlugin.sync.LSLRecPluginSyncMethod;
import lslrec.plugin.lslrecPlugin.trial.LSLRecPluginTrial;

public class TrialStageMarker extends LSLRecPluginTrial 
{	
	public static final String STAGES = "Stages";
	public static final String PRE_RUN_TIME = "Pre-run time";
	public static final String POST_RUN_TIME = "Post-run time";
	public static final String AUTO_FINISH = "Auto finish";
	
	public static final int PRERUN_MARK = 3;
	public static final int POSTRUN_MARK = 4;
	
	private int stageSyncMark = SyncMarker.NON_MARK;
		
	private List< TrialStage > stages = new ArrayList<TrialStage>();
	private int stageIndex = 0;
	
	private boolean autofinish = false;
	
	private JLabel remainingTimeInfo = new JLabel();
	private long refTimer = 0L;
	private Timer coundDownTimer = null; 
	private String timeoutMsg = "Tiempo agotado";		
	
	private Timer stageTimer = null;
	
	private boolean stageTimerStop = false;
	private boolean wakeupTrialCallByTimer = false;
	
	private Object sync = new Object();
	
	public TrialStageMarker() 
	{
	}
	
	@Override
	public String getID() 
	{
		return "Trial stage marker";
	}

	@Override
	public int getStageMark() 
	{
		return this.stageSyncMark;
	}

	@Override
	public void loadSettings(List<Parameter<String>> arg0) 
	{
		if( arg0 != null )
		{
			this.stages.clear();
			
			boolean stagesOk = false;
	
			for( Parameter< String > par : arg0 )
			{				
				String val = par.getValue();
				switch( par.getID() )
				{
					case( TrialStageMarker.STAGES ):
					{
						String[] phases = val.split( ";" );
												
						for( int iPhases = 0; iPhases < phases.length; iPhases++ )
						{
							String phase = phases[ iPhases ];
														
							String[] values = phase.split( "," );
							
							String id = values[ 0 ];
							int mark = Integer.parseInt( values[ 1 ] );
							int time = Integer.parseInt( values[ 2 ] );
							boolean auto = Boolean.parseBoolean( values[ 3 ] );
							
							this.stages.add( new TrialStage( id, mark, time, auto ) );
						}
						
						stagesOk = true;
						
						break;
					}
					default:
					{
						break;
					}
				}
			}
			
			if( stagesOk )
			{			
				for( Parameter< String > par : arg0 )
				{				
					String val = par.getValue();
					switch( par.getID() )
					{
						case( PRE_RUN_TIME ):
						{
							int preRunTime = Integer.parseInt( val );
							
							if( preRunTime > 0 )
							{
								this.stages.add( 0, new TrialStage( PRE_RUN_TIME, PRERUN_MARK, preRunTime, true ) );
							}
							
							break;
						}
						case( POST_RUN_TIME ):
						{
							int postRunTime = Integer.parseInt( val );
							
							if( postRunTime > 0 )
							{
								this.stages.add( new TrialStage( POST_RUN_TIME, POSTRUN_MARK, postRunTime, true ) );
							}
							
							break;
						}
						case( AUTO_FINISH ):
						{
							this.autofinish = Boolean.parseBoolean( val );
							
							break;
						}
						default:
						{
							break;
						}
					}
				}
			}
		}
	}

	@Override
	public void setStage( JPanel arg0 ) 
	{
		if( arg0 != null )
		{
			synchronized ( sync )
			{
				if( this.stageTimer != null )
				{
					this.stageTimer.stop();
					
					this.stageTimerStop = true;
				}
				
				if( this.coundDownTimer != null )
				{
					this.coundDownTimer.stop();
				}
			}			
			
			if( this.stageIndex < this.stages.size() )
			{
				arg0.setVisible( false );
				arg0.setLayout( new BorderLayout() );
				arg0.removeAll();

				TrialStage stage = this.stages.get( this.stageIndex );
				
				this.stageSyncMark = stage.getMark();
				
				this.remainingTimeInfo.setText( stage.getTime() + "");
				
				JPanel stagePanel = this.getPhasePanel( stage );
				this.stageTimer = this.getStageTimer( stage );		
				this.wakeupTrialCallByTimer = false;
				this.stageTimerStop = false;
				
				arg0.add( stagePanel, BorderLayout.CENTER );
				
				final long phaseTime = stage.getTime() * 1000;
	
				this.coundDownTimer = new Timer( 1000, new ActionListener() 
				{	
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						if( !stageTimerStop )
						{
							long elapsedTime = System.currentTimeMillis() - refTimer;
							
							long remainedTime = ( phaseTime - elapsedTime ) / 1000;
							if( remainedTime >= 0 )
							{
								synchronized ( sync )
								{
									if( remainingTimeInfo.getText().indexOf( timeoutMsg ) < 0 )
									{
										remainingTimeInfo.setVisible( false );
										remainingTimeInfo.setText( remainedTime + "" );
										remainingTimeInfo.setVisible( true );
									}
								}
							}
							else
							{
								Timer t = (Timer)e.getSource();
								t.stop();
							}
						}
					}
				});
				this.coundDownTimer.setRepeats( true );
				
				arg0.addComponentListener( new ComponentAdapter() 
				{
					@Override
					public void componentShown(ComponentEvent e) 
					{
						if( stageTimer != null )
						{
							refTimer = System.currentTimeMillis();
							stageTimer.start();
							
							coundDownTimer.start();
						}
					}
				});
				
				this.stageIndex++;
			}
			else if( this.autofinish )
			{
				this.stageSyncMark = SyncMarker.STOP_MARK;
				
				this.sendSyncMark();
			}
			else
			{
				if( this.stageSyncMark != POSTRUN_MARK && this.stageSyncMark != SyncMarker.STOP_MARK )
				{
					this.stageSyncMark = POSTRUN_MARK;
				}
				else
				{
					this.stageSyncMark = SyncMarker.STOP_MARK;
				}
				
				this.wakeupTrialCallByTimer = false;
				
				this.setTimeoutTimerMessage();
			}
			
			arg0.setVisible( true );
		}
	}
	
	private void sendSyncMark()
	{
		LSLRecPluginSyncMethod sync = getSyncMethod();
		
		if( sync != null )
		{
			synchronized( sync )
			{
				sync.notify();
			}
		}
	}
	
	private JPanel getPhasePanel( TrialStage stage  )
	{
		JPanel stagePanel = new JPanel( new BorderLayout( 5, 5 ) );
		
		if( stage != null )
		{			
			JPanel allStagePanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			
			for( TrialStage stg : this.stages )
			{
				String idStage = stg.getId( ) + "(" + stg.getMark() +")  ";
				
				if( stage.getId().equals( stg.getId() ) )
				{
					idStage = "<html><p style='color:orange'>" + idStage + "</p></html>";
				}
				
				allStagePanel.add( new JLabel( idStage ) );
			}
			
			stagePanel.add( new JScrollPane( allStagePanel ), BorderLayout.NORTH );
		
			JPanel panel = new JPanel( new VerticalFlowLayout( VerticalFlowLayout.CENTER ) );
			
			JButton bt = new JButton( "Siguiente fase");
			bt.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					synchronized ( sync )
					{
						stageTimerStop = true;
						
						if( !wakeupTrialCallByTimer )
						{					
							wakeUpTrial();
						}
					}					
				}
			});
			
			panel.add( bt );
			panel.add( new JLabel( "Tiempo restante:") );
			panel.add( this.remainingTimeInfo );	
			
			stagePanel.add( panel, BorderLayout.CENTER );
		}
		
		return stagePanel;
	}
	
	private Timer getStageTimer( TrialStage stage )
	{
		Timer timer = null;

		if( stage != null )
		{
			int time = stage.getTime();
			final boolean auto = stage.isAuto();
			
			timer = new Timer( time * 1000, new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					synchronized ( sync )
					{
						if( !stageTimerStop )
						{
							if( auto )
							{
								wakeupTrialCallByTimer = true;
								
								wakeUpTrial();
							}
							else
							{				
								sendSyncMark();
								
								setTimeoutTimerMessage();
							}
						}
					} 					
				}
			});
			
			timer.setRepeats( false );
		}
		
		return timer;
	}
	
	private void setTimeoutTimerMessage( )
	{
		remainingTimeInfo.setVisible( false );
		remainingTimeInfo.setText( "<html><h3 style='color:orange'>" + timeoutMsg + "</h3></html>" );
		remainingTimeInfo.setVisible( true );
	}
	
	private void wakeUpTrial()
	{
		synchronized( this )
		{
			super.notify();
		}
	}
	
	@Override
	protected void startUp() throws Exception 
	{
		super.startUp();
		
		this.stageSyncMark = SyncMarker.START_MARK;
		
		synchronized ( this ) 
		{
			super.wait( 1000L );
		}
		
		this.sendSyncMark();
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
