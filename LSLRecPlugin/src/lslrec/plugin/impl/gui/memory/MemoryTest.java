/**
 * 
 */
package lslrec.plugin.impl.gui.memory;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
public class MemoryTest extends LSLRecPluginTrial
{
	public static final int INFINITY_REPETITIONS = -1;
	
	public static final int NUM_DIFFICULTY_LEVELS = 5;
	
	public static final int STAGE_MEMORY = 0x04;
	public static final int STAGE_ANSWER = STAGE_MEMORY << 1;
	
	public static final String DIFFICULTY = "difficulty"; 
	public static final String REPETITIONS = "repetitions";
	public static final String MEMORY_TIME = "memory_time";
	public static final String ANSWER_TIME = "answer_time";
	public static final String TASK_TIME = "task_time";
	
	private int stage = 0;

	private ParameterList parameters;
	

	private MemoryMatrix task = null;

	private int[][] answerLocationTable = null;

	private JPopupMenu answerOptionsListMenu = null;
	
	private JPanel container = null;
	
	private Timer task_timer;
	private Timer memory_timer;
	private Timer answer_timer;
	
	/**
	 * 
	 */
	public MemoryTest() 
	{
		this.parameters = new ParameterList();
		
		Parameter par = new Parameter< Integer >( REPETITIONS , INFINITY_REPETITIONS );
		this.parameters.addParameter( par );
		
		par  = new Parameter< Integer >( DIFFICULTY, 0 );
		this.parameters.addParameter( par );		
		
		par = new Parameter< Double >( MEMORY_TIME, 30D ); // 30 seconds
		this.parameters.addParameter( par );
		
		par = new Parameter< Double >( ANSWER_TIME, 30D ); // 30 seconds
		this.parameters.addParameter( par );
		
		par = new Parameter< Double >( TASK_TIME, 5 * 60D ); // 5 minutes
		this.parameters.addParameter( par );
		
		this.container = new JPanel();
	}
	
	@Override
	public String getID() 
	{
		return "Memory Test";
	}

	@Override
	public int getStageMark() 
	{
		return this.stage;
	}

	@Override
	public void loadSettings( List< Parameter< String > > arg0 )
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
							val = Integer.parseInt( par.getValue().toString() );
							break;
						}
						case TASK_TIME:						
						case MEMORY_TIME:						
						case ANSWER_TIME:
						{
							val = Double.parseDouble( par.getValue() );
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
	
	private ActionListener getActionMemoryTimer()
	{
		 return new ActionListener() 
					{
						
						@Override
						public void actionPerformed(ActionEvent e) 
						{
							stage = STAGE_ANSWER;
							
							if( answer_timer != null )
							{
								answer_timer.stop();
							}
							
							Parameter< Double > t = (Parameter< Double >)parameters.getParameter( ANSWER_TIME ); 
							answer_timer = new Timer( (int)( 1000 * t.getValue() ), getActionAnswerTimer() );
							
							hideFigure();
							
							LSLRecPluginSyncMethod syncMethod = getSyncMethod();
							
							if( syncMethod != null )
							{
								synchronized ( syncMethod )
								{
									syncMethod.notify();
								}			
							}
							
							answer_timer.setRepeats( false );
							answer_timer.start();
						}
					};
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
	
	private void stopTimers()
	{
		if( this.task_timer != null )
		{
			this.task_timer.stop();
		}
		
		if( this.memory_timer != null )
		{
			this.memory_timer.stop();
		}
		
		if( this.answer_timer != null )
		{
			this.answer_timer.stop();
		}
	}
	
	@Override
	public void setStage( JPanel panelTask ) 
	{
		Parameter< Integer > rep = this.parameters.getParameter( REPETITIONS );
		
		if( rep.getValue() == 0 )
		{
			this.stopTimers();
			
			this.stage = SyncMarker.STOP_MARK;
						
			super.stopThread( IStoppableThread.STOP_IN_NEXT_LOOP );
			
			return;
		}		
		
		rep.setValue( rep.getValue() - 1 );
		
		this.stage = STAGE_MEMORY;
		
		if( panelTask != null )
		{	
			Parameter< Double > t = (Parameter< Double >)this.parameters.getParameter( MEMORY_TIME ); 
			if( this.memory_timer != null )
			{
				this.memory_timer.stop();
			}
			
			this.memory_timer = new Timer( (int)( 1000 * t.getValue() ), this.getActionMemoryTimer() );
			this.memory_timer.setRepeats( false );
						
			panelTask.setVisible( false );
			this.container.setVisible( false );
			this.container.removeAll();
	
			Point mSize = this.task.getMatrixSize();			
			final int[][] memoryMatrix = this.task.getTask();			
			this.answerLocationTable = new int[ mSize.x ][ mSize.y ];
	
			this.container.setVisible( false );
			
			MemoryBoard.setMemoryPanel( memoryMatrix, this.container );
		
			panelTask.add( this.container, BorderLayout.CENTER );
			this.container.setVisible( true );
			panelTask.setVisible( true );
			
			this.memory_timer.start();
		}		
	}
	
	private void hideFigure()
	{
		this.container.setVisible( false );
		
		for( Component fig : this.container.getComponents() )
		{
			if( fig instanceof MemoryButton )
			{
				MemoryButton b = (MemoryButton) fig;
				
				b.setIcon( null );
				
				b.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						MemoryButton b = (MemoryButton)e.getSource();
						
						Point pos = MouseInfo.getPointerInfo().getLocation();
						
						if ( answerOptionsListMenu != null)
						{
							answerOptionsListMenu.setVisible(false);
						}
												
						answerOptionsListMenu = getJPopupMenuMemoryOptions( b );
						
						answerOptionsListMenu.setLocation( pos );
						answerOptionsListMenu.setVisible(true);
					}

				});
			}
		}
		
		this.container.setVisible( true );
	}
	
	/**
	 * This method initializes jPopupMenuOpcionesMemoria	
	 * 	
	 * @return javax.swing.JPopupMenu	
	 */
	private JPopupMenu getJPopupMenuMemoryOptions( final MemoryButton c )
	{
		final JPopupMenu jPopupMenuOptions = new JPopupMenu();
		
		Point loc = c.getLocationInMatrix();		

		int[] figs = MemoryMatrix.getMemorySet();

		int nc = 2;
		int nf = figs.length / nc;
		nf += figs.length - nf * nc;

		GridLayout ly = new GridLayout(nf, nc);
		jPopupMenuOptions.setLayout( ly );

		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
		int w = (int)( d.width * 0.04D );
		int h = (int)( d.height * 0.04D );

		for (int i = 0; i < figs.length; i++)
		{
			final MemoryButton b = new MemoryButton( loc.x, loc.y, figs[ i ] );
			b.setBackground( Color.WHITE );
			b.setPreferredSize( new Dimension( w, h ) );

			b.setName( figs[ i ] + "" );

			b.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					MemoryButton b = (MemoryButton) e.getSource();
					Point loc = b.getLocationInMatrix();
					
					c.setIcon( MemoryBoard.getFigure(c.getSize(), b.getFigureID() ) );
					c.setAnswerFigure( b.getFigureID() );

					answerLocationTable[ loc.x ][ loc.y ] = 1;
					checkAnswer();

					jPopupMenuOptions.setVisible( false );
				}

			});

			b.addComponentListener(new ComponentAdapter()
			{
				public void componentResized(ComponentEvent e)
				{
					MemoryButton mb = (MemoryButton) e.getSource();
					mb.setIcon( MemoryBoard.getFigure( mb.getSize(), mb.getFigureID() ) );
				}
			});
			
			jPopupMenuOptions.add( b );
		}

		return jPopupMenuOptions;
	}

	private void checkAnswer()
	{
		boolean answer = true;
		
		Point size = this.task.getMatrixSize();
		
		int nRes = 0;

		for (int i = 0; i < size.x; i++)
		{
			for( int j = 0; j < size.y; j++ )
			{
				nRes += this.answerLocationTable[ i ][ j ];
			}
		}

		if ( nRes == size.x * size.y )
		{
			Component[] matrix = this.container.getComponents();
			
			int[][] userAnswer = new int[ this.answerLocationTable.length ][ this.answerLocationTable[ 0 ].length ];
			for( Component c : matrix )
			{				
				if( c instanceof MemoryButton )
				{
					MemoryButton mb =  (MemoryButton)c;
					answer = answer && mb.isCorrectAnswer();
					
					Point loc = mb.getLocationInMatrix();
					
					userAnswer[ loc.x ][ loc.y ] = mb.getAnswer();
				}
			}

			int[] res = new int[ userAnswer.length * userAnswer[ 0 ].length ];
			int ir = 0;
			for( int i = 0; i < userAnswer.length; i++ )
			{
				for( int j = 0; j < userAnswer[ 0 ].length; j++ )
				{
					res[ ir ] = userAnswer[ i ][ j ];
					
					ir++;
				}
			}
			

			this.wakeUpTrial();
		}
	}
	
	public static Point getMatrixSize( int difficulty )
	{
		int f = 4;int c = 4;		
		
		switch ( difficulty )
		{
			case 0: 
			{
				f = 2;
				c = 2;
				break;
			}	
			case 1:
			{
				f = 2;
				c = 3;
				break;
			}	
			case 2:
			{
				f = 3;
				c = 3;
				break;
			}
			case 3:
			{
				f = 4;
				c = 3;
				break;
			}
		}
		
		return new Point( f, c );
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
		Point size = getMatrixSize( dif.getValue() );
		
		int f = size.x;		
		int c = size.y;

		this.task = new MemoryMatrix( f, c );
		
		GridLayout gb = new GridLayout();
		gb.setRows( f );
		gb.setColumns( c );

		this.container.setLayout( gb );
		
		Parameter< Double > t = (Parameter< Double >)this.parameters.getParameter( TASK_TIME );
		int v = (int)( 1000 * t.getValue() );
		if( v >= 0 )
		{
			this.task_timer = new Timer( v, new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					stopTimers();
					
					stage = SyncMarker.STOP_MARK;
					
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
			
			this.task_timer.setRepeats( false);			
			this.task_timer.start();
		}		
	}
	
	@Override
	protected void preStopThread(int arg0) throws Exception 
	{		
	}
	
	@Override
	protected void postStopThread(int arg0) throws Exception 
	{		
	}
}
