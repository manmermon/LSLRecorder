/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.plugin.impl.gui.memory;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lslrec.auxiliar.WarningMessage;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.trial.ILSLRecPluginTrial;
import lslrec.plugin.lslrecPlugin.trial.LSLRecPluginTrial;

/**
 * @author Manuel Merino Monge
 *
 */
public class PluginMemoryTest implements ILSLRecPluginTrial
{
	private ParameterList pars = new ParameterList();
		
	/**
	 * 
	 */
	public PluginMemoryTest() 
	{
		Parameter par = new Parameter( MemoryTest.REPETITIONS, 1 );
		this.pars.addParameter( par );
		
		par = new Parameter( MemoryTest.DIFFICULTY, 0 );
		this.pars.addParameter( par );

		par = new Parameter( MemoryTest.TASK_TIME, 5 * 60D );
		this.pars.addParameter( par );
		

		par = new Parameter( MemoryTest.MEMORY_TIME, 30D );
		this.pars.addParameter( par );

		par = new Parameter( MemoryTest.ANSWER_TIME, 30D );
		this.pars.addParameter( par );
	}
	
	@Override
	public JPanel getSettingPanel() 
	{
		JPanel container = new JPanel( new BorderLayout() );
		
		final JPanel previewPanel = new JPanel( new BorderLayout( )  );
		JPanel panel = new JPanel( new GridBagLayout() );
		
		container.add( panel, BorderLayout.NORTH );
		container.add( previewPanel, BorderLayout.CENTER );
		
		int cols = 2;
		
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.weightx = 0.5;
		gbc.anchor = GridBagConstraints.EAST;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets( 0, 0, 0, 0 );
		gbc.gridwidth = 1;				
		
		int c = -1;
		for( String idPar : this.pars.getParameterIDs() )
		{	
			JPanel parPanel = new JPanel( new BorderLayout() );
			parPanel.setBorder( BorderFactory.createTitledBorder( idPar ) );
			
			Component cmp = null;
			switch ( idPar )
			{
				case MemoryTest.REPETITIONS:
				{
					final int step = 1;
					
					JSpinner sp = new JSpinner( new SpinnerNumberModel( (Integer)this.pars.getParameter( idPar ).getValue() , -1, null, step ) );	
					sp.addMouseWheelListener( new MouseWheelListener()
					{
						@Override
						public void mouseWheelMoved(MouseWheelEvent e) 
						{
							JSpinner sp = (JSpinner)e.getSource();
							
							int update = 0;
							if( e.getWheelRotation() > 0 )
							{
								update = -step;
							}
							else if( e.getWheelRotation() < 0 )
							{
								update = step;
							}
							
							Integer v = (Integer)sp.getValue();
							sp.setValue( v + update );
						}
					});
					
					sp.addChangeListener( new ChangeListener() 
					{	
						@Override
						public void stateChanged(ChangeEvent e) 
						{
							JSpinner sp = (JSpinner)e.getSource();
							Integer v = (Integer)sp.getValue();
							
							pars.getParameter( idPar ).setValue( v );
						}
					});
					
					cmp = sp;
					
					break;
				}
				case MemoryTest.DIFFICULTY:
				{
					Integer[] dif = new Integer[ MemoryTest.NUM_DIFFICULTY_LEVELS ];
					
					for( int d = 0; d < dif.length; d++ )
					{
						dif[ d ] = d;
					}
					
					JComboBox< Integer > cb = new JComboBox< Integer >( dif );
					cb.setEditable( false );
					
					cb.addItemListener( new ItemListener() 
					{	
						@Override
						public void itemStateChanged(ItemEvent e) 
						{
							if( e.getStateChange() == ItemEvent.SELECTED )
							{
								Integer d = (Integer)e.getItem();
								
								if( d >= 0 )
								{
									pars.getParameter( idPar ).setValue( d );
									
									List< Parameter< String > > list = new ArrayList< Parameter< String > >();
									for( String id : pars.getParameterIDs() )
									{
										Parameter< String > p = new Parameter<String>( id, pars.getParameter( id ).getValue().toString() );
										list.add( p );
									}
									
									Point size = MemoryTest.getMatrixSize( d );
									MemoryMatrix m = new MemoryMatrix( size.x, size.y );
									
									previewPanel.setVisible( false );
									
									MemoryBoard.setMemoryPanel( m.getTask(), previewPanel );
									
									previewPanel.setVisible( true );
								}
							}
						}
					});
					
					cb.addMouseWheelListener( new MouseWheelListener() 
					{
						
						@Override
						public void mouseWheelMoved(MouseWheelEvent e) 
						{
							JComboBox< Integer > cb  = (JComboBox<Integer>)e.getSource();
							
							Integer ind = cb.getSelectedIndex() - e.getWheelRotation();
							Integer len = cb.getItemCount();
							
							if( ind >= 0 && ind < len)
							{
								cb.setSelectedIndex( ind );
							}
						}
					});
					
					cb.setSelectedIndex( -1 );
					//cb.setSelectedIndex( 0 );
					cb.setSelectedIndex( (Integer)this.pars.getParameter( idPar ).getValue() );
					
					cmp = cb;
					
					break;
				}
				case MemoryTest.TASK_TIME:
				case MemoryTest.MEMORY_TIME:
				case MemoryTest.ANSWER_TIME:
				{
					final double step = 1D;
					
					Number v = (Number)pars.getParameter( idPar ).getValue();
					JSpinner sp = new JSpinner( new SpinnerNumberModel( v.doubleValue(), 1D, null, step ) );	
					sp.addMouseWheelListener( new MouseWheelListener()
					{
						@Override
						public void mouseWheelMoved(MouseWheelEvent e) 
						{
							JSpinner sp = (JSpinner)e.getSource();
							
							double update = 0;
							if( e.getWheelRotation() > 0 )
							{
								update = -step;
							}
							else if( e.getWheelRotation() < 0 )
							{
								update = step;
							}
							
							double v = (Double)sp.getValue();
							sp.setValue( v + update );
						}
					});
					
					sp.addChangeListener( new ChangeListener() 
					{	
						@Override
						public void stateChanged(ChangeEvent e) 
						{
							JSpinner sp = (JSpinner)e.getSource();
							Double v = (Double)sp.getValue();
							
							pars.getParameter( idPar ).setValue( v );
						}
					});
					
					cmp = sp;
					
					break;
				}
				default:
				{
					break;
				}
			}
			
			if( cmp != null )
			{
				parPanel.add( cmp, BorderLayout.CENTER );
				
				c++;
				gbc.gridx = ( c % cols );
				gbc.gridy =  c / cols;
				
				panel.add( parPanel, gbc );
			}
		}
		
		return container;
	}

	@Override
	public List<Parameter<String>> getSettings() 
	{
		List< Parameter< String > > pars = new ArrayList<Parameter< String >>();
		for( String id : this.pars.getParameterIDs() )
		{
			Parameter p = this.pars.getParameter( id );
			
			pars.add( new Parameter<String>( id, p.getValue().toString() ) );
		}
		
		return pars;
	}

	@Override
	public void loadSettings( List< Parameter< String > > arg0 ) 
	{
		if( arg0 != null )
		{
			for( Parameter< String > par: arg0 )
			{				
				Parameter p = this.pars.getParameter( par.getID() );
				
				if( p != null )
				{				
					Number val = null;
					switch( par.getID() )
					{
						case MemoryTest.REPETITIONS:
						case MemoryTest.DIFFICULTY:
						{
							try
							{
								val = Integer.parseInt( par.getValue() );
							}
							catch (Exception e) 
							{
								e.printStackTrace();
							}
							
							break;
						}
						case MemoryTest.TASK_TIME:
						case MemoryTest.MEMORY_TIME:
						case MemoryTest.ANSWER_TIME:
						{
							try
							{
								val = Double.parseDouble( par.getValue() );
							}
							catch (Exception e) 
							{
								e.printStackTrace();
							}
							
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

	@Override
	public PluginType getType() 
	{
		return PluginType.TRIAL;
	}

	@Override
	public String getID() 
	{
		return "Memory Test";
	}

	@Override
	public LSLRecPluginTrial getGUIExperiment() 
	{
		MemoryTest t = new MemoryTest();
						
		t.loadSettings( this.getSettings() );
		
		return t;
	}

	@Override
	public int compareTo( ILSLRecPlugin o ) 
	{
		return o.getID().compareTo( this.getID() );
	}

	@Override
	public WarningMessage checkSettings() 
	{
		WarningMessage msg = new WarningMessage();
		
		for( String idPar : this.pars.getParameterIDs() )
		{
			Parameter p = this.pars.getParameter( idPar );
			
			switch ( idPar )
			{
				case MemoryTest.REPETITIONS:
				{
					if( ((Integer)p.getValue()) == 0 )
					{
						msg.addMessage( idPar + " must be non-zero.", WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
				case MemoryTest.MEMORY_TIME	:
				case MemoryTest.TASK_TIME:
				case MemoryTest.ANSWER_TIME:
				{
					if( ((Double)p.getValue()) < 0 )
					{
						msg.addMessage( idPar + " must be non-negative.", WarningMessage.ERROR_MESSAGE );
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
	public boolean hasTrialLog() 
	{
		return true;
	}

	@Override
	public String getLogDescription() 
	{
		String descr = ""; 
		
		descr += "Memory matrix prints by rows. ";
		
		descr += "4 Flag bits - [b3 b2 b1 b0]: "
				+ "b0 is the color (0 - white, 1 - black)"
				+ ", others bits contain the shape:"
				+ " b1 - circle, b2 - diamond, b3 - triangle."
				;
				
		
		return descr;
	}

	@Override
	public String getExtraInfo2Stream() 
	{
		return "";
	}
}
