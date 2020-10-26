/**
 * 
 */
package lslrec.plugin.impl.gui.arithmetic;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.dataStream.sync.SyncMarker;
import lslrec.plugin.lslrecPlugin.sync.LSLRecPluginSyncMethod;
import lslrec.plugin.lslrecPlugin.trial.LSLRecPluginTrial;
import lslrec.stoppableThread.IStoppableThread;

/**
 * @author Manuel Merino Monge
 *
 */
public class ArithmeticTest  extends LSLRecPluginTrial
{
	public final static int STAGE_NEW = 4;
		
	private int stage = STAGE_NEW;
	
	public static final int INFINITY_REPETITIONS = -1;
	
	public static final String DIFFICULTY = "difficulty"; 
	public static final String REPETITIONS = "repetitions";
	public static final String ANSWER_TIME = "answer_time";
	public static final String TASK_TIME = "task_time";
	
	private ParameterList parameters;
	
	private JPanel container = null;
	private JPanel answerPanel = null;
	private JLabel taskText = null;	
	
	private Timer task_timer;
	private Timer answer_timer;
	
	private ArithmeticTask task = null;

	/**
	 * 
	 */
	public ArithmeticTest() 
	{
		this.parameters = new ParameterList();
		
		Parameter par = new Parameter< Integer >( REPETITIONS , INFINITY_REPETITIONS );
		this.parameters.addParameter( par );
		
		par  = new Parameter< Integer >( DIFFICULTY, 0 );
		this.parameters.addParameter( par );		
				
		par = new Parameter< Double >( ANSWER_TIME, 30D ); // 30 seconds
		this.parameters.addParameter( par );
		
		par = new Parameter< Double >( TASK_TIME, 5 * 60D ); // 5 minutes
		this.parameters.addParameter( par );
		
		this.container = new JPanel();
		this.container.setVisible(false);

		GridLayout gridLayout = new GridLayout( 2, 1 );

		this.container.setLayout(gridLayout);
		this.container.setBackground( Color.WHITE );
		this.container.add( this.getTaskText(), null);
		this.container.add( this.getJPanelResultado(), null);		
		this.container.setVisible(true);
	}
	
	@Override
	public String getID() 
	{
		return "Arithmetic Test";
	}

	@Override
	public int getStageMark() 
	{
		return this.stage;
	}

	@Override
	public void loadSettings(List< Parameter< String > > arg0) 
	{ 
		if( arg0 != null )
		{
			for( Parameter< String > par : arg0 )
			{
				Parameter p = this.parameters.getParameter( par.getID() );
				if( p != null )
				{				
					Number val = null;
					switch( par.getID() )
					{
						case REPETITIONS:
						case DIFFICULTY:
						{
							String v = par.getValue();
							val = Integer.parseInt( v );
							break;
						}
						case TASK_TIME:							
						case ANSWER_TIME:
						{
							val = Double.parseDouble( par.getValue() + "" );
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

	private ActionListener getActionAnswerTimer()
	{	
		 return new ActionListener() 
					{	
						@Override
						public void actionPerformed(ActionEvent e) 
						{
							wakeUpTrial();
						}
					};
	}
	
	private void wakeUpTrial()
	{
		synchronized( this )
		{
			super.notify();
		}
	}
	
	@Override
	public void setStage( JPanel panelTask )
	{
		Parameter< Integer > rep = this.parameters.getParameter( REPETITIONS );
		
		if( rep.getValue() == 0 )
		{
			if( this.task_timer != null )
			{
				this.task_timer.stop();
			}
			
			if( this.answer_timer != null )
			{
				
				this.answer_timer.stop();
			}
			
			this.stage = SyncMarker.STOP_MARK;
						
			super.stopThread( IStoppableThread.STOP_IN_NEXT_LOOP );
			
			return;
		}		
		
		rep.setValue( rep.getValue() - 1 );
		
		this.stage = STAGE_NEW;
		
		if( panelTask != null )
		{	
			Parameter< Double > t = (Parameter< Double >)this.parameters.getParameter( ANSWER_TIME );
			if( this.answer_timer != null )
			{
				this.answer_timer.stop();
			}
			
			this.answer_timer = new Timer( (int)( 1000 * t.getValue() ), this.getActionAnswerTimer() );
			this.answer_timer.setRepeats( false );
						
			panelTask.setVisible( false );
			
			this.container.setVisible( false );
			
			this.task.newArithmeticTask();
			
			getTaskText().setText( this.task.getOperation() );
			
			panelTask.add( this.container, BorderLayout.CENTER );
			
			this.container.setVisible( true );
			
			panelTask.setVisible( true );
			
			this.answer_timer.start();
		}	
	}

	/**
	 * This method initializes JLabel	
	 * 	
	 * @return JLabel	
	 */
	private JLabel getTaskText()
	{
		if( this.taskText == null )
		{
			this.taskText = new JLabel( "" );
		
			this.taskText.setBackground( Color.WHITE );
			this.taskText.setHorizontalAlignment( JLabel.CENTER );
			this.taskText.setComponentOrientation( ComponentOrientation.LEFT_TO_RIGHT );
	
			this.taskText.addComponentListener( new ComponentAdapter()
			{			
				@Override
				public void componentResized(ComponentEvent arg0)
				{
					JLabel lb = (JLabel)arg0.getSource();
					
					Dimension d = lb.getSize();
					
					Font f = new Font( Font.DIALOG, Font.PLAIN, d.height / 2);
					FontMetrics fm = lb.getFontMetrics( f );
					Insets pad = lb.getInsets();
	
					while ( fm.stringWidth("( 100 * ( 100 * ( 100 * 100 ) ) ) = ?") > d.width - pad.left - pad.right)
					{
						f = new Font(f.getName(), f.getStyle(), f.getSize() - 1);
						fm = lb.getFontMetrics(f);
					}
	
					while (fm.stringWidth("( 100 * ( 100 * ( 100 * 100 ) ) ) = ?") < d.width - pad.left - pad.right)
					{
						f = new Font(f.getName(), f.getStyle(), f.getSize() + 1);
						fm = lb.getFontMetrics(f);
					}
	
					if (fm.stringWidth("( 100 * ( 100 * ( 100 * 100 ) ) ) = ?") > d.width - pad.left - pad.right)
					{
						f = new Font(f.getName(), f.getStyle(), f.getSize() - 1);
					}
	
					lb.setFont( f );
					lb.setText( lb.getText() );
				}
			});
		}
		
		return this.taskText;
	}

	/**
	 * This method initializes answerPanel	
	 * 	
	 * @return JPanel	
	 */
	private JPanel getJPanelResultado()
	{
		if( this.answerPanel == null )
		{
			this.answerPanel = new JPanel();
			
			GridLayout gridLayout7 = new GridLayout();
			gridLayout7.setRows(1);
			
			this.answerPanel = new JPanel();
			this.answerPanel.setLayout( gridLayout7 );
			this.answerPanel.setBackground( Color.WHITE );
	
			for (int i = 0; i < 10; i++)
			{
				final JButton b = new JButton();
				b.setText( i + "");
	
				b.setFocusable( true );
				b.setBorder( BorderFactory.createEtchedBorder() );
				b.getInsets().left = 0;
				b.getInsets().right = 0;
	
				b.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						int target = task.getResult();
						String userAnswer = b.getText();
	
						boolean answer  = userAnswer.equals( target + "");
						
						wakeUpTrial();
					}        
				});
	
				b.addComponentListener(new ComponentAdapter()
				{
					public void componentResized(ComponentEvent arg0)
					{
						Dimension d = b.getSize();
						Font f = new Font( Font.DIALOG, Font.BOLD, d.height / 2);
						FontMetrics fm = b.getFontMetrics(f);
						Insets pad = b.getInsets();
	
						while (fm.stringWidth(b.getText()) > d.width - pad.left - pad.right)
						{
							f = new Font(f.getName(), f.getStyle(), f.getSize() - 1);
							fm = b.getFontMetrics(f);
						}
	
						while (fm.stringWidth(b.getText()) < d.width - pad.left - pad.right)
						{
							f = new Font(f.getName(), f.getStyle(), f.getSize() + 1);
							fm = b.getFontMetrics(f);
						}
	
						if (fm.stringWidth(b.getText()) > d.width - pad.left - pad.right)
						{
							f = new Font(f.getName(), f.getStyle(), f.getSize() - 1);
						}
	
						b.setFont( f );
					}
				});
				
				this.answerPanel.add( b );
			}
		}

		return this.answerPanel;
	}
	
	@Override
	protected void startUp() throws Exception 
	{
		super.startUp();
		
		this.stage = SyncMarker.START_MARK;
		
		LSLRecPluginSyncMethod sync = getSyncMethod();
		
		if( sync != null )
		{
			synchronized( sync )
			{
				sync.notify();
			}
		}
		
		super.setGUIPanel( this.container );
		
		Parameter< Integer > dif = this.parameters.getParameter( DIFFICULTY );
		this.task = new ArithmeticTask( dif.getValue() );
				
		Parameter< Double > t = (Parameter< Double >)this.parameters.getParameter( TASK_TIME );
		int v = (int)( 1000 * t.getValue() );
		if( v >= 0 )
		{
			this.task_timer = new Timer( v, new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					stage = SyncMarker.STOP_MARK;
					
					if( answer_timer != null )
					{
						answer_timer.stop();
					}
					
					LSLRecPluginSyncMethod sync = getSyncMethod();
										
					if( sync != null )
					{
						synchronized( sync )
						{
							sync.notify();
						}
					}
					
					synchronized( this )
					{
						while( !sync.getState().equals( State.WAITING ) )
						{
							try 
							{
								super.wait( 10L );
							}
							catch (InterruptedException e1) 
							{
							}
						}
					}
					
					stopThread( IStoppableThread.FORCE_STOP );
				}
			} );
			
			this.task_timer.setRepeats( false );			
			this.task_timer.start();
		}
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
