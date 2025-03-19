package lslrec.plugin.impl.gui.trialStagesMarker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
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
	
	public static final String STAGE_SEPARATOR = ":";
	
	public static final int PRERUN_MARK = 3;
	public static final int POSTRUN_MARK = 4;
	
	public static final int MARK_BIAS = Math.max( POSTRUN_MARK, PRERUN_MARK );
	
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
	
	private int subStageDelta = 0;
	
	public TrialStageMarker() 
	{
		remainingTimeInfo.setFont( this.getFont() );
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
	public void loadSettings(List<Parameter<String>> pars) 
	{
		if( pars != null )
		{
			this.stages.clear();
			
			boolean stagesOk = false;
	
			for( Parameter< String > par : pars )
			{				
				String val = par.getValue();
				switch( par.getID() )
				{
					case( TrialStageMarker.STAGES ):
					{
						String[] phases = val.split( STAGE_SEPARATOR );
											
						int numPrevSubStages = 0;
						for( int iPhases = 0; iPhases < phases.length; iPhases++ )
						{
							String phase = phases[ iPhases ];
														
							String[] values = phase.split( "," );
							
							String id = values[ 0 ];
							int time = Integer.parseInt( values[ 1 ] );
							boolean auto = Boolean.parseBoolean( values[ 2 ] );
							String substages = "";
							
							if( values.length == 4 )
							{
								substages = values[ 3 ];
							}
							
							int mark = this.MARK_BIAS + ( iPhases + 1 ) + numPrevSubStages;
							
							TrialStage tstage =  new TrialStage( id, mark, time, auto );
							tstage.setSubstages( substages );
							
							numPrevSubStages += tstage.getSubstages().length;
							
							this.stages.add( tstage );
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
				for( Parameter< String > par : pars )
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
	public void setStage( JPanel trialPanel ) 
	{
		if( trialPanel != null )
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
				trialPanel.setVisible( false );
				trialPanel.setLayout( new BorderLayout() );
				trialPanel.removeAll();

				TrialStage stage = this.stages.get( this.stageIndex );
				
				this.stageSyncMark = stage.getMark();
				
				this.remainingTimeInfo.setText( stage.getTime() + "");
				
				JPanel stagePanel = this.getPhasePanel( stage );
				this.stageTimer = this.getStageTimer( stage );		
				this.wakeupTrialCallByTimer = false;
				this.stageTimerStop = false;
				
				trialPanel.add( stagePanel, BorderLayout.CENTER );
				
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
				
				trialPanel.addComponentListener( new ComponentAdapter() 
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
				this.subStageDelta = 0;
				
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

				this.subStageDelta = 0;
				
				this.wakeupTrialCallByTimer = false;
				
				this.setTimeoutTimerMessage();
			}
			
			trialPanel.setVisible( true );
		}
	}
	
	private void sendSyncMark( )
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
			JPanel allStagePanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 2, 2 ) );
			
			for( TrialStage stg : this.stages )
			{
				int stageMark = stg.getMark();
				String idStage = stg.getId( ) + "(" + stageMark +")  ";
				
				if( stage.getId().equals( stg.getId() ) )
				{
					idStage = "<html><p style='color:orange'>" + idStage + "</p></html>";
				}
				
				JLabel lb = new JLabel( idStage );
				lb.setFont( this.getFont() );
				allStagePanel.add( lb );
								
				String[] subStages = stg.getSubstages();
				if( subStages != null )
				{
					stageMark += subStages.length; 
				}
			}
			allStagePanel.setBorder(BorderFactory.createEmptyBorder( 0, 0, 15, 0 ));
			
			stagePanel.add( new JScrollPane( allStagePanel ), BorderLayout.NORTH );
		
			JPanel panel = new JPanel( new VerticalFlowLayout( VerticalFlowLayout.CENTER, 5,5 ) );
		
			JLabel retime = new JLabel( "Tiempo restante:");
			retime.setFont( this.getFont() );
			
			JPanel ptime = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 5 ));					
			ptime.add( retime );
			ptime.add( this.remainingTimeInfo );
			
			panel.add( ptime );
			JPanel panelSig = new JPanel( new FlowLayout( FlowLayout.LEFT, 2,2) );
			
			JButton bt = new JButton( "Siguiente fase");
			bt.setFont( this.getFont() );
			bt.setForeground( Color.BLUE );
			
			JButton btSi = new JButton( "Sí" );
			btSi.setFont( this.getFont() );
			btSi.setEnabled( false );
			btSi.setForeground( Color.BLUE );
			
			JButton btNo = new JButton( "No" );
			btNo.setFont( this.getFont() );
			btNo.setEnabled( false );
			btNo.setForeground( Color.RED );
			
			panelSig.add( bt );
			panelSig.add( btSi );
			panelSig.add( btNo );
						
			bt.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					bt.setEnabled( false );
					btSi.setEnabled( true );
					btNo.setEnabled( true );
				}
			});
			
			btSi.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					synchronized ( sync )
					{
						stageTimerStop = true;
						
						stageSyncMark -= subStageDelta;
						subStageDelta = 0;
						
						if( !wakeupTrialCallByTimer )
						{					
							wakeUpTrial();
						}
					}					
				}
			});
			
			btNo.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					bt.setEnabled( true );
					btSi.setEnabled( false );
					btNo.setEnabled( false );
				}
			});
			
			
			panel.add( panelSig );
			panel.add( Box.createRigidArea( new Dimension( 5, 10 )) );
						
			String[] substages = stage.getSubstages();
			
			if( substages != null )
			{
				for( int isub = 0; isub < substages.length; isub++ )
				{
					String substage = substages[ isub ];
					
					JButton substageBt = new JButton( substage );
					substageBt.setFont( this.getFont() );
					final int delta = ( isub + 1 );
					substageBt.addActionListener( new ActionListener() 
					{	
						@Override
						public void actionPerformed(ActionEvent e) 
						{
							synchronized ( sync )
							{
								if( subStageDelta != delta )
								{
									stageSyncMark -= subStageDelta;
									
									stageSyncMark += delta;
									subStageDelta = delta;
								}
													
								sendSyncMark();
							}			
						}
					});
					
					panel.add( substageBt );
				}	
			}	
			
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
							stageSyncMark -= subStageDelta;
							subStageDelta = 0;
							
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
		remainingTimeInfo.setText( "<html><p style='color:orange'>" + timeoutMsg + "</p></html>" );
		remainingTimeInfo.setVisible( true );
	}
	
	private Font getFont()
	{
		return new Font( Font.DIALOG, Font.BOLD, 18 );
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
		this.subStageDelta = 0;
		
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
