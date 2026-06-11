/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.  https://github.com/manmermon/LSLRecorder
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
package lslrec.plugin.impl.gui.trialStagesMarker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;

import lslrec.auxiliar.WarningMessage;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.trial.ILSLRecPluginTrial;
import lslrec.plugin.lslrecPlugin.trial.LSLRecPluginTrial;

public class TrialStageMarkerPlugin  implements ILSLRecPluginTrial 
{
	private ParameterList pars = new ParameterList();

	private JPanel settingPanel = null;
	
	public TrialStageMarkerPlugin() 
	{
		Parameter< String > par = new Parameter<String>( TrialStageMarker.STAGES, "stage1," + (TrialStageMarker.POSTRUN_MARK << 1) + ",60,false, " );
		this.pars.addParameter( par );
		
		par = new Parameter< String >( TrialStageMarker.PRE_RUN_TIME, "10" );
		this.pars.addParameter( par );
		
		par = new Parameter< String >( TrialStageMarker.POST_RUN_TIME, "10" );
		this.pars.addParameter( par );
		
		par = new Parameter< String >( TrialStageMarker.AUTO_FINISH, "false" );
		this.pars.addParameter( par );
		
		par = new Parameter< String >( TrialStageMarker.ENABLE_OUT_MARK_STREAM, "false" );
		this.pars.addParameter( par );
		
		par = new Parameter< String >( TrialStageMarker.STREAM_NAME, this.getID() );
		this.pars.addParameter( par );
		
		par = new Parameter< String >( TrialStageMarker.STREAM_SOURCE_ID, this.getID() );
		this.pars.addParameter( par );
		
		par = new Parameter< String >( TrialStageMarker.STREAM_TYPE, "marks" );
		this.pars.addParameter( par );
	}
	
	@Override
	public WarningMessage checkSettings() 
	{
		WarningMessage msg = new WarningMessage();
		
		for( String id : this.pars.getParameterIDs() )
		{
			String val = this.pars.getParameter( id ).getValue().toString();
			
			switch ( id ) 
			{
				case TrialStageMarker.STAGES:
				{
					String[] phases = val.split( TrialStageMarker.STAGE_SEPARATOR );
					for( String phase : phases )
					{
						String[] values = phase.split( "," );
						
						if( values.length < 4 || values.length > 5 )
						{
							msg.addMessage( "Error in plugin " + this.getID() + ": stages malformed. (" + phase + ")", WarningMessage.ERROR_MESSAGE );
						}
						else if( values[ 0 ].isEmpty() )
						{
							msg.addMessage( "Error in plugin " + this.getID() + ": any stage id empty.", WarningMessage.ERROR_MESSAGE );
						}
						else
						{
							try
							{
								int mark = Integer.parseInt( values[ 1 ] );
								
								if( mark <= 0 )
								{
									msg.addMessage( "Error in plugin " + this.getID() + ": mark must be >0.", WarningMessage.ERROR_MESSAGE );
								}
							}
							catch (Exception e) 
							{
								msg.addMessage( "Error in plugin " + this.getID() + ": mark value malformed (must be a integer).", WarningMessage.ERROR_MESSAGE );
							}
							
							try
							{
								int time = Integer.parseInt( values[ 2 ] );
								
								if( time <= 0 )
								{
									msg.addMessage( "Error in plugin " + this.getID() + ": time must be >0.", WarningMessage.ERROR_MESSAGE );
								}
							}
							catch (Exception e) 
							{
								msg.addMessage( "Error in plugin " + this.getID() + ": time value malformed.", WarningMessage.ERROR_MESSAGE );
							}
							
							try
							{
								Boolean.parseBoolean( values[ 3 ] );								
							}
							catch (Exception e) 
							{
								msg.addMessage( "Error in plugin " + this.getID() + ": autoadvancement malformed.", WarningMessage.ERROR_MESSAGE );
							}		
							
							try
							{
								if( values.length == 5 )
								{
									String events = values[ 4 ];
									String[] eventList = events.split( TrialStage.EVENTS_SEPARATOR );

									if( eventList.length > 0 )
									{
										for( String event : eventList )
										{
											String[] eventValues = event.split( "=" );

											if( eventValues.length != 2 )
											{
												throw new IllegalArgumentException( "event " + event + " malformed.");
											}
											else
											{
												String evId = eventValues[ 0 ];
												Integer.parseInt( eventValues[ 1 ] );

												if( evId == null || evId.trim().isEmpty() )
												{
													throw new IllegalArgumentException( "event  " + event + " malformed.");
												}
											}
										}
									}
								}								
							}
							catch (Exception e) 
							{
								msg.addMessage( "Error in plugin " + this.getID() + ": events malformed: idEvent=mark", WarningMessage.ERROR_MESSAGE );
							}
						}
					}
				
					break;
				}
				case( TrialStageMarker.PRE_RUN_TIME ):
				{
					try
					{
						int time = Integer.parseInt( val );
						
						if( time < 0 )
						{
							msg.addMessage( "Error in plugin " + this.getID() + ": pre-run time must be >=0.", WarningMessage.ERROR_MESSAGE );
						}
					}
					catch (Exception e) 
					{
						msg.addMessage( "Error in plugin " + this.getID() + ": pre-run time value malformed.", WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
				case( TrialStageMarker.POST_RUN_TIME ):
				{
					try
					{
						int time = Integer.parseInt( val );
						
						if( time < 0 )
						{
							msg.addMessage( "Error in plugin " + this.getID() + ": post-run time must be >=0.", WarningMessage.ERROR_MESSAGE );
						}
					}
					catch (Exception e) 
					{
						msg.addMessage( "Error in plugin " + this.getID() + ": post-run time value malformed.", WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
				case( TrialStageMarker.AUTO_FINISH ):
				{
					try
					{
						Boolean.parseBoolean( val );
					}
					catch (Exception e) 
					{
						msg.addMessage( "Error in plugin " + this.getID() + ": auto-finish value malformed.", WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
				case( TrialStageMarker.ENABLE_OUT_MARK_STREAM ):
				{
					try
					{
						Boolean.parseBoolean( val );
					}
					catch (Exception e) 
					{
						msg.addMessage( "Error in plugin " + this.getID() + ": " + id + " value malformed.", WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
				case( TrialStageMarker.STREAM_NAME):
				case( TrialStageMarker.STREAM_SOURCE_ID):
				case( TrialStageMarker.STREAM_TYPE):
				{
					
					if( val == null || val.trim().isEmpty() )
					{
						msg.addMessage( "Error in plugin " + this.getID() + ": " + id + " null, whitespaces or empty.", WarningMessage.ERROR_MESSAGE );
					}					
					
					break;
				}
				default:
				{
					break;
				}
			}
		}
		
		return msg;
	}

	@Override
	public JPanel getSettingPanel() 
	{
		if( this.settingPanel == null )
		{
			this.settingPanel = new JPanel( new BorderLayout() );

			this.setSettingComponent();
		}
		
		return this.settingPanel;
	}
	
	private void setSettingComponent( )
	{
		if( this.settingPanel == null )
		{
			this.getSettingPanel();
		}
		
		this.settingPanel.removeAll();
		
		//
		// stages table
		//

		JPanel stagePanel = new JPanel( new BorderLayout(5,5) ); 

		JTable table = TrialStageMarkerTools.createStageTrialTable();
		table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);

		JLabel markInfo = new JLabel( "Pre-Run sync mark = " + TrialStageMarker.PRERUN_MARK + "; Post-Run sync mark = " + TrialStageMarker.POSTRUN_MARK);
		markInfo.setBackground( Color.WHITE );
		markInfo.setForeground( Color.BLUE );

		stagePanel.add( table, BorderLayout.CENTER );
		stagePanel.add( table.getTableHeader(), BorderLayout.NORTH );
		stagePanel.add( markInfo, BorderLayout.SOUTH );		

		table.getModel().addTableModelListener( new TableModelListener()
		{				
			@Override
			public void tableChanged( TableModelEvent e ) 
			{
				DefaultTableModel tm = (DefaultTableModel)e.getSource();

				Parameter< String > par = (Parameter< String >)pars.getParameter( TrialStageMarker.STAGES );

				if( e.getType() == TableModelEvent.UPDATE || e.getType() == TableModelEvent.DELETE )
				{
					String stages = "";

					for( int r = 0; r < tm.getRowCount(); r++ )
					{
						String stg = "";
						for( int c = 0; c < 5; c++ )
						{
							Object o = tm.getValueAt( r, c );
							stg += o.toString() + ",";
						}

						stg = stg.substring( 0, stg.length() - 1 );

						stages += stg + TrialStageMarker.STAGE_SEPARATOR;
					}

					stages = stages.substring( 0, stages.length() - 1 );
					
					par.setValue( stages );					
				}
				else if( e.getType() == TableModelEvent.INSERT )
				{
					int r = e.getLastRow();

					String newStage = "";
					for( int c = 0; c < 5; c++ )
					{
						Object o = tm.getValueAt( r, c );
						newStage += o.toString() + ",";
					}

					newStage = newStage.substring( 0, newStage.length() - 1 );

					if( !par.getValue().contains( newStage ) )
					{
						par.setValue( par.getValue() + TrialStageMarker.STAGE_SEPARATOR + newStage );
					}
				}
			}
		});

		Parameter< String > par = this.pars.getParameter( TrialStageMarker.STAGES );
		if( par != null )
		{
			String stages = par.getValue();
			
			if( stages != null && !stages.isEmpty() )
			{
				String[] phases = stages.split( TrialStageMarker.STAGE_SEPARATOR );
				
				for( String phase : phases )
				{	
					String[] values = phase.split( "," );
					
					String substages = "";
					
					if( values.length >= 4 )
					{	
						if( values.length == 5 )
						{
							substages = values[ 4 ];
						}
						
						this.addNewStage( table, values[ 0 ], Integer.parseInt( values[ 1 ]),  Integer.parseInt( values[ 2 ])
												, Boolean.parseBoolean( values[ 3 ] ), substages );
					}
				}
				
			}
		}
		
		//
		// add/remove stage
		//

		JPanel controlStagesPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );

		JButton stageAddBt = new JButton();

		stageAddBt.setIcon( GeneralAppIcon.Add( 16, Color.BLACK ) );
		if( stageAddBt.getIcon() == null )
		{
			stageAddBt.setText( "add" );
		}

		stageAddBt.addActionListener( new ActionListener() 
		{			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				int nStages = table.getRowCount() + 1;
				String newStage = "stage" + nStages;
				int time = 60;
				int mark = TrialStageMarker.POSTRUN_MARK << nStages;
				boolean auto = false;
				
				addNewStage( table, newStage, mark, time, auto, "" );
			}
		});


		JButton stageDelBt = new JButton();
		stageDelBt.setIcon( GeneralAppIcon.Close( 16, Color.RED ) );
		if( stageDelBt.getIcon() == null )
		{
			stageDelBt.setText( "del" );
		}

		stageDelBt.addActionListener( new ActionListener() 
		{			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();

				if( row >= 0 )
				{
					DefaultTableModel m = (DefaultTableModel)table.getModel();
					m.removeRow( row );
				}
			}
		});

		controlStagesPanel.add( stageAddBt );
		controlStagesPanel.add( stageDelBt );


		//
		// Pre/post-run, autofinish and data summary
		//

		JPanel otherParametersPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) ); 

		JLabel prerunLb = new JLabel( TrialStageMarker.PRE_RUN_TIME + ": " );
		JSpinner prerunSp = new JSpinner( new SpinnerNumberModel( new Integer( 10 ), new Integer( 0 ),  new Integer( Integer.MAX_VALUE ), new Integer( 1 )) );
		prerunSp.addMouseWheelListener( new MouseWheelListener()
		{
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) 
			{
				JSpinner sp = (JSpinner)e.getSource();

				int update = 0;
				if( e.getWheelRotation() > 0 )
				{
					update = -1;
				}
				else if( e.getWheelRotation() < 0 )
				{
					update = 1;
				}


				Integer v = (Integer)sp.getValue() + update;

				if( ((SpinnerNumberModel)sp.getModel()).getMaximum().compareTo( v ) >= 0 
						&& ((SpinnerNumberModel)sp.getModel()).getMinimum().compareTo( v ) <= 0 )
				{
					sp.setValue( v );
				}
			}
		});

		prerunSp.addChangeListener( new ChangeListener() 
		{	
			@Override
			public void stateChanged(ChangeEvent e) 
			{
				JSpinner sp = (JSpinner)e.getSource();
				Integer v = (Integer)sp.getValue();

				pars.getParameter( TrialStageMarker.PRE_RUN_TIME ).setValue( v + "");
			}
		});
		
		prerunSp.setValue( Integer.parseInt( this.pars.getParameter( TrialStageMarker.PRE_RUN_TIME ).getValue().toString() ) );

		JLabel postrunLb = new JLabel( TrialStageMarker.POST_RUN_TIME + ": " );
		JSpinner postrunSp = new JSpinner( new SpinnerNumberModel( new Integer( 10 ), new Integer( 0 ), new Integer( Integer.MAX_VALUE ), new Integer( 1 )) );
		postrunSp.addMouseWheelListener( new MouseWheelListener()
		{
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) 
			{
				JSpinner sp = (JSpinner)e.getSource();

				int update = 0;
				if( e.getWheelRotation() > 0 )
				{
					update = -1;
				}
				else if( e.getWheelRotation() < 0 )
				{
					update = 1;
				}

				Integer v = (Integer)sp.getValue() + update;

				if( ((SpinnerNumberModel)sp.getModel()).getMaximum().compareTo( v ) >= 0 
						&& ((SpinnerNumberModel)sp.getModel()).getMinimum().compareTo( v ) <= 0 )
				{
					sp.setValue( v );
				}
			}
		});

		postrunSp.addChangeListener( new ChangeListener() 
		{	
			@Override
			public void stateChanged(ChangeEvent e) 
			{
				JSpinner sp = (JSpinner)e.getSource();
				Integer v = (Integer)sp.getValue();

				pars.getParameter( TrialStageMarker.POST_RUN_TIME ).setValue( v + "");
			}
		});
		
		postrunSp.setValue( Integer.parseInt( this.pars.getParameter( TrialStageMarker.POST_RUN_TIME ).getValue().toString() ) );

		JCheckBox autofinishChb = new JCheckBox( TrialStageMarker.AUTO_FINISH );
		autofinishChb.addItemListener( new ItemListener() 
		{	
			@Override
			public void itemStateChanged(ItemEvent e) 
			{
				JCheckBox ch = (JCheckBox)e.getSource();

				pars.getParameter( TrialStageMarker.AUTO_FINISH ).setValue( ch.isSelected() + "");
			}
		});
		autofinishChb.setSelected( Boolean.parseBoolean( this.pars.getParameter( TrialStageMarker.AUTO_FINISH ).getValue().toString() ) );
		
		otherParametersPanel.add( prerunLb );
		otherParametersPanel.add( prerunSp );
		otherParametersPanel.add( postrunLb );
		otherParametersPanel.add( postrunSp );
		otherParametersPanel.add( autofinishChb );

		JPanel southPanel = new JPanel( new BorderLayout() );
		southPanel.add( otherParametersPanel, BorderLayout.NORTH );

		JPanel outputStreamPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 2, 2 ) );
		JCheckBox enaStream = new JCheckBox( TrialStageMarker.ENABLE_OUT_MARK_STREAM );
		enaStream.setSelected( Boolean.parseBoolean( pars.getParameter( TrialStageMarker.ENABLE_OUT_MARK_STREAM ).getValue().toString() ) );
		enaStream.addItemListener( new ItemListener() 
		{			
			@Override
			public void itemStateChanged(ItemEvent e) 
			{
				JCheckBox ch = (JCheckBox)e.getSource();

				pars.getParameter( TrialStageMarker.ENABLE_OUT_MARK_STREAM ).setValue( ch.isSelected() + "");
			}
		});
		outputStreamPanel.add( enaStream );
		outputStreamPanel.add( new JLabel( "|" ) );
		
		outputStreamPanel.add( new JLabel( TrialStageMarker.STREAM_NAME ) );
		JTextField strname = new JTextField( 8 );
		strname.setText( pars.getParameter( TrialStageMarker.STREAM_NAME ).getValue().toString() );
		strname.getDocument().addDocumentListener( new DocumentListener() 
		{			
			@Override
			public void removeUpdate(DocumentEvent e) 
			{
				update( e );
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) 
			{
				update( e );
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) 
			{
				update( e );
			}
			
			private void update( DocumentEvent e )
			{
				try 
				{
					String t = e.getDocument().getText(0, e.getDocument().getLength() );
					
					pars.getParameter( TrialStageMarker.STREAM_NAME ).setValue( t );
				}
				catch (BadLocationException e1) 
				{
					e1.printStackTrace();
				}
				
			}
		});		
		outputStreamPanel.add( strname );
		
		outputStreamPanel.add( new JLabel( TrialStageMarker.STREAM_TYPE ) );
		JTextField strType = new JTextField( 8 );
		strType.setText( pars.getParameter( TrialStageMarker.STREAM_TYPE ).getValue().toString() );
		strType.getDocument().addDocumentListener( new DocumentListener() 
		{			
			@Override
			public void removeUpdate(DocumentEvent e) 
			{
				update( e );
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) 
			{
				update( e );
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) 
			{
				update( e );
			}
			
			private void update( DocumentEvent e )
			{
				try 
				{
					String t = e.getDocument().getText(0, e.getDocument().getLength() );
					
					pars.getParameter( TrialStageMarker.STREAM_TYPE ).setValue( t );
				}
				catch (BadLocationException e1) 
				{
					e1.printStackTrace();
				}
				
			}
		});		
		outputStreamPanel.add( strType );
		
		outputStreamPanel.add( new JLabel( TrialStageMarker.STREAM_SOURCE_ID ) );
		JTextField strSourceID = new JTextField( 8 );
		strSourceID.setText( pars.getParameter( TrialStageMarker.STREAM_SOURCE_ID ).getValue().toString() );
		strSourceID.getDocument().addDocumentListener( new DocumentListener() 
		{			
			@Override
			public void removeUpdate(DocumentEvent e) 
			{
				update( e );
			}
			
			@Override
			public void insertUpdate(DocumentEvent e) 
			{
				update( e );
			}
			
			@Override
			public void changedUpdate(DocumentEvent e) 
			{
				update( e );
			}
			
			private void update( DocumentEvent e )
			{
				try 
				{
					String t = e.getDocument().getText(0, e.getDocument().getLength() );
					
					pars.getParameter( TrialStageMarker.STREAM_SOURCE_ID ).setValue( t );
				}
				catch (BadLocationException e1) 
				{
					e1.printStackTrace();
				}
				
			}
		});	
		outputStreamPanel.add( strSourceID );
		
		southPanel.add( outputStreamPanel );
		//
		//
		//

		
		JPanel auxPanel = new JPanel( new BorderLayout() );
		auxPanel.add( controlStagesPanel, BorderLayout.NORTH );
		auxPanel.add( new JScrollPane( stagePanel ), BorderLayout.CENTER );
		auxPanel.add( new JScrollPane( southPanel ), BorderLayout.SOUTH );
		
		this.settingPanel.add( new JScrollPane( auxPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ), BorderLayout.CENTER );
	}
	
	private void addNewStage( JTable table, String id, int mark, int time, boolean auto, String substages )
	{
		if( table != null && id != null )
		{
			Object[] vals = new Object[ 5 ];							
			vals[ 0 ] = id;
			vals[ 1 ] = mark;
			vals[ 2 ] = time;
			vals[ 3 ] = auto;
			vals[ 4 ] = substages;
	
			DefaultTableModel m = (DefaultTableModel)table.getModel();
			m.addRow( vals );
		}
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
	public void loadSettings(List<Parameter<String>> arg0) 
	{
		if( arg0 != null )
		{
			for( Parameter< String > par : arg0 )
			{
				Parameter< String > p = (Parameter<String>)this.pars.getParameter( par.getID() );
				
				if( p != null )
				{				
					String val = par.getValue();
					switch( par.getID() )
					{
						case( TrialStageMarker.STAGES ):
						{
							p.setValue( val.toString() );
							
							break;
						}
						case( TrialStageMarker.PRE_RUN_TIME ):
						{
							p.setValue( Integer.parseInt( val ) + "" );
							
							break;
						}
						case( TrialStageMarker.POST_RUN_TIME ):
						{
							p.setValue( Integer.parseInt( val ) + "" );
							
							break;
						}
						case( TrialStageMarker.AUTO_FINISH ):
						{
							p.setValue( Boolean.parseBoolean( val ) + "" );
							
							break;
						}
						case( TrialStageMarker.ENABLE_OUT_MARK_STREAM ):
						{
							p.setValue( Boolean.parseBoolean( val ) + "" );
							break;
						}
						case( TrialStageMarker.STREAM_NAME ):
						case( TrialStageMarker.STREAM_TYPE ):
						case( TrialStageMarker.STREAM_SOURCE_ID ):
						{
							p.setValue( val );
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
	public PluginType getType() 
	{
		return PluginType.TRIAL;
	}

	@Override
	public String getID() 
	{
		return "Trial Stage Marker";
	}

	@Override
	public int compareTo(ILSLRecPlugin o) 
	{
		return o.getID().compareTo( this.getID() );
	}

	@Override
	public LSLRecPluginTrial getGUIExperiment() 
	{
		TrialStageMarker trials = new TrialStageMarker();
		trials.loadSettings( this.getSettings() );
		
		return trials;
	}

	@Override
	public String getLogDescription() 
	{
		return "Trial Stage Marker";
	}

	@Override
	public boolean hasTrialLog() 
	{
		return false;
	}


	@Override
	public String getExtraInfo2Stream() 
	{
		String extra = "sync-markers: " 
							+TrialStageMarker.PRERUN_MARK  + "=" + TrialStageMarker.PRE_RUN_TIME + ";"
							+ TrialStageMarker.POSTRUN_MARK + "=" + TrialStageMarker.POST_RUN_TIME + ";";
		
		Parameter< String > par = (Parameter< String >)pars.getParameter( TrialStageMarker.STAGES );
	
		String[] phases = par.getValue().split( TrialStageMarker.STAGE_SEPARATOR );
		//int numPrevSubstates = 0;		
		
		for( int iPhases = 0; iPhases < phases.length; iPhases++ )
		{
			String phase = phases[ iPhases ];
										
			String[] values = phase.split( "," );
			
			String id = values[ 0 ];
			//int mark = ( iPhases + 1 ) + TrialStageMarker.MARK_BIAS + numPrevSubstates;
			int mark = Integer.parseInt( values[ 1 ] );
			extra +=  + mark + "=" + id + ";";
			
			if( values.length == 5 )
			{
				String events = values[ 4 ];
				
				String[] eventList = events.split( TrialStage.EVENTS_SEPARATOR );
				
				for( String ev : eventList )
				{
					String[] eventValue = ev.split( "=" );
					if( eventValue.length != 2 )
					{
						throw new IllegalArgumentException( "Events in a stage is malformed." );
					}
					else
					{
						String evId = eventValue[ 0 ];
						String evMark = eventValue[ 1 ];
												
						extra += "" + evMark + "=" + evId + ";";
					}
				}
			}
		}
		
		return extra.substring( 0, extra.length() - 1 );
	}
}
