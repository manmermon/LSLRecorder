/**
 * 
 */
package lslrec.plugin.impl.gui.arithmetic;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
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
public class PluginArithmeticTest implements ILSLRecPluginTrial 
{
	private ParameterList pars = new ParameterList();

	/**
	 * 
	 */
	public PluginArithmeticTest() 
	{
		Parameter par = new Parameter( ArithmeticTest.REPETITIONS, 1 );
		this.pars.addParameter( par );
		
		par = new Parameter( ArithmeticTest.DIFFICULTY, 0 );
		this.pars.addParameter( par );

		par = new Parameter( ArithmeticTest.TASK_TIME, 5 * 60D );
		this.pars.addParameter( par );

		par = new Parameter( ArithmeticTest.ANSWER_TIME, 30D );
		this.pars.addParameter( par );
	}
	
	@Override
	public JPanel getSettingPanel() 
	{
		JPanel container = new JPanel( new BorderLayout() );
		
		final JPanel previewPanel = new JPanel( new BorderLayout( )  );
		previewPanel.setBorder( BorderFactory.createEtchedBorder());
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
				case ArithmeticTest.REPETITIONS:
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
				case ArithmeticTest.DIFFICULTY:
				{
					Integer[] dif = new Integer[ 5 ];
					
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
								pars.getParameter( idPar ).setValue( d );
								
								ArithmeticTask task = new ArithmeticTask( d );
								task.newArithmeticTask();
								
								previewPanel.setVisible( false );
								previewPanel.removeAll();
								
								JLabel oper = new JLabel( task.getOperation(), SwingConstants.CENTER );
								
								oper.addComponentListener( new ComponentAdapter() 
								{
									@Override
									public void componentResized(ComponentEvent e) 
									{
										JLabel lb = (JLabel)e.getSource();
										Dimension dim = lb.getSize();
										
										Font f = new Font( Font.DIALOG, Font.PLAIN, dim.height / 2);
										FontMetrics fm = lb.getFontMetrics( f );
										Insets pad = lb.getInsets();
						
										String ref = "( 99 * ( 99 * ( 99 * 99 ) ) ) = ?";
										while ( fm.stringWidth( ref ) > dim.width - pad.left - pad.right)
										{
											f = new Font( f.getName(), f.getStyle(), f.getSize() - 1 );
											fm = lb.getFontMetrics( f );
										}
						
										while ( fm.stringWidth( ref ) < dim.width - pad.left - pad.right)
										{
											f = new Font( f.getName(), f.getStyle(), f.getSize() + 1 );
											fm = lb.getFontMetrics( f );
										}
						
										if (fm.stringWidth( ref ) > dim.width - pad.left - pad.right)
										{
											f = new Font( f.getName(), f.getStyle(), f.getSize() - 1 );
										}
						
										lb.setFont( f );
										lb.setText( task.getOperation() );
									}
								});
								
								
								previewPanel.add( oper, BorderLayout.CENTER );
								
								previewPanel.setVisible( true );
							}
						}
					});
					
					cb.addMouseWheelListener( new MouseWheelListener()
					{
						@Override
						public void mouseWheelMoved(MouseWheelEvent e) 
						{
							JComboBox< Integer > sp = (JComboBox< Integer >)e.getSource();
							
							int index = sp.getSelectedIndex();
							
							if( e.getWheelRotation() > 0 )
							{
								index--;
							}
							else
							{
								index++;
							}
							
							if( index < 0 )
							{
								index = 0;
							}
							
							if( index >= sp.getItemCount() )
							{
								index = sp.getItemCount() -1;
							}
							
							sp.setSelectedIndex( index );
						}
					});
					
					cb.setSelectedIndex( -1 );
					cb.setSelectedItem( (Integer)this.pars.getParameter( idPar ).getValue() );
					
					//cb.setSelectedIndex( 0 );
					
					
					
					cmp = cb;
					
					break;
				}
				case ArithmeticTest.TASK_TIME:
				case ArithmeticTest.ANSWER_TIME:
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
	public List< Parameter< String > > getSettings() 
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
	public void loadSettings( List< Parameter< String > > arg0) 
	{	
		if( arg0 != null )
		{
			for( Parameter< String > par : arg0 )
			{
				Parameter p = this.pars.getParameter( par.getID() );
				
				if( p != null )
				{				
					Number val = null;
					switch( par.getID() )
					{
						case ArithmeticTest.REPETITIONS:
						case ArithmeticTest.DIFFICULTY:
						{
							val = Integer.parseInt( par.getValue().toString() );
							break;
						}
						case ArithmeticTest.TASK_TIME:							
						case ArithmeticTest.ANSWER_TIME:
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

	@Override
	public PluginType getType() 
	{
		return PluginType.TRIAL;
	}

	@Override
	public String getID() 
	{	
		return "Arithmetic Task";
	}

	@Override
	public LSLRecPluginTrial getGUIExperiment() 
	{
		ArithmeticTest at = new ArithmeticTest();
		
		at.loadSettings( this.getSettings() );
		
		return at;
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
				case ArithmeticTest.REPETITIONS:
				{
					if( ((Integer)p.getValue()) == 0 )
					{
						msg.addMessage( idPar + " must be non-zero.", WarningMessage.ERROR_MESSAGE );
					}
					
					break;
				}
				case ArithmeticTest.TASK_TIME:
				case ArithmeticTest.ANSWER_TIME:
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
}
