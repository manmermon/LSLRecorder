/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2011-2019 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.  https://github.com/manmermon/LSLRecorder
 *
 *   CLIS is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CLIS is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CLIS.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */

package lslrec.gui.dataPlot;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import lslrec.auxiliar.extra.NumberRange;
import lslrec.config.language.Language;
import lslrec.gui.GuiTextManager;
import lslrec.gui.dialog.Dialog_Info;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.gui.miscellany.VerticalFlowLayout;
import lslrec.gui.miscellany.BasicPainter2D;
import lslrec.config.ConfigApp;

public class CanvasStreamDataPlot extends JPanel
{
	private static final long serialVersionUID = 9117600627145108594L;
		
	private int queueLength = 100;
	private List<Queue<Double>> xy = new ArrayList<Queue<Double>>();
	private List<String> plotNames = new ArrayList< String >();


	private List<Double> minY = new ArrayList< Double >();
	private List<Double> maxY = new ArrayList< Double >();

	private Set< String > hiddenPlots = null;

	private Semaphore sem = new Semaphore(1, true);
	
	private String plotName = "";
	private boolean newPlotName = false;


	private HashMap< String, NumberRange > filters = null;
	private HashMap< String, NumberRange > visRange = null;

	private boolean streamOn = true;
	
	//private boolean stopRun = false;

	// PANELS
	private JPanel panelPlots = null;
	private JPanel filterRangePanel = null;
	private JPanel containerPanel = null;
	private JPanel selChannelPlotPanel = null;

	// TEXTFIELD
	private JTextField filterRangeText = null;
	
	// BUTTONS
	private JButton applyFilters = null;
	private JButton infoFilters = null;
	private JButton btUndockPlot = null;
	
	// SCROLLPANEL
	private JScrollPane scrollpanel;

	// FRAME
	private JFrame jFramePlot = null;	
	
	//private List<List<Double>> XY;

	// LABELS
	private JLabel streamStateIco = null;
	
	/**
	 * Create the dialog.
	 */
	public CanvasStreamDataPlot(int dataLength)
	{
		super.setName( this.getClass().getName() );

		this.queueLength = dataLength;

		this.hiddenPlots = new HashSet< String >();

		super.setVisible(true);

		setBackground(Color.WHITE);
		setDoubleBuffered(true);

		setLayout(new BorderLayout(0, 0));
		
		this.SetContainerPanel();
		
		this.clearFilters();
	}

	private void SetContainerPanel()
	{
		this.setVisible( false );
		
		this.add(this.getContainerPanel(), BorderLayout.CENTER );
		
		this.setVisible( true );
	}
	
	private void RemoveContainerPanel()
	{
		this.setVisible( false );
		
		this.remove( this.getContainerPanel() );
		
		this.setVisible( true );
	}
	
	private JPanel getContainerPanel()
	{
		if( this.containerPanel == null )
		{
			this.containerPanel = new JPanel();
			this.containerPanel.setBackground(Color.WHITE);
			this.containerPanel.setDoubleBuffered(true);
			this.containerPanel.setLayout( new BorderLayout(0,0));
			
			this.containerPanel.add( this.getJPanelFilterRange(), BorderLayout.NORTH );
			this.containerPanel.add( this.getJScrollPanelPlot(), BorderLayout.CENTER );
			
			JPanel selPlotPlanelAux = new JPanel( new BorderLayout() );
			selPlotPlanelAux.add( this.getSelectionPlottedChannels(), BorderLayout.NORTH );
			
			JScrollPane selPlotChScr = new JScrollPane(  selPlotPlanelAux );
			selPlotChScr.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED );
			selPlotChScr.setHorizontalScrollBarPolicy( JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
			selPlotChScr.getVerticalScrollBar().setUnitIncrement( 10 );
			
			selPlotChScr.setBorder( BorderFactory.createTitledBorder( Language.getLocalCaption( Language.SETTING_LSL_PLOT ) ) );
			
			this.containerPanel.add( selPlotChScr, BorderLayout.WEST );
		}
		
		return this.containerPanel;
	}
	
	private JPanel getSelectionPlottedChannels()
	{
		if( this.selChannelPlotPanel == null )
		{
			this.selChannelPlotPanel = new JPanel( new GridLayout(0, 2, 0, 0 ));
			this.selChannelPlotPanel.setVisible( true );
		}
		
		return this.selChannelPlotPanel;
	}
	
	public void clearFilters()
	{
		if( this.filters == null )
		{
			this.filters = new HashMap< String, NumberRange >();
			this.visRange = new HashMap< String, NumberRange >();
		}
		else
		{
			this.filters.clear();
			this.visRange.clear();
			//this.ignoredCols.clear();
		}
	}
	
	public void setPlotName(String name)
	{
		if (name != null)
		{
			if (!this.newPlotName)
			{
				this.newPlotName = (!this.plotName.equals(name));
			}

			this.plotName = name;
		}
	}

	private JPanel getJPanelFilterRange()
	{
		if( this.filterRangePanel == null )
		{
			this.filterRangePanel = new JPanel();
			this.filterRangePanel.setLayout( new BoxLayout( this.filterRangePanel, BoxLayout.X_AXIS ) );
			
			JLabel lb = new JLabel(  Language.getLocalCaption( Language.LSL_PLOT_FILTERS ) );			
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.LSL_PLOT_FILTERS, lb );
			
			this.filterRangePanel.add( Box.createRigidArea( new Dimension(5, 0) ) );			
			this.filterRangePanel.add( this.getStreamStateIco() );			
			this.filterRangePanel.add( Box.createRigidArea( new Dimension(5, 0) ) );
			this.filterRangePanel.add( lb );
			this.filterRangePanel.add( Box.createRigidArea( new Dimension(2, 0) ) );
			this.filterRangePanel.add( this.getFilterRangeText() );
			this.filterRangePanel.add( Box.createRigidArea( new Dimension(2, 0) ) );
			this.filterRangePanel.add( this.getJButtonApplyFilters() );
			this.filterRangePanel.add( Box.createRigidArea( new Dimension(2, 0) ) );
			this.filterRangePanel.add( this.getJButtonInfoFilters() );
			this.filterRangePanel.add( Box.createRigidArea( new Dimension(2, 0) ) );
			this.filterRangePanel.add( this.getJButtonUndockPlot() );
			this.filterRangePanel.add( Box.createRigidArea( new Dimension(2, 0) ) );
		}
		
		return this.filterRangePanel;
	}
	
	private JLabel getStreamStateIco()
	{
		if( this.streamStateIco == null )
		{
			this.streamStateIco = new JLabel();
		}
		
		return this.streamStateIco;
	}
	
	public void setStreamState( boolean on )
	{
		Color color = ( on ) ? Color.GREEN.darker() : Color.RED;
		
		this.getStreamStateIco().setIcon( new ImageIcon( BasicPainter2D.paintFillCircle( 0, 0, 10, color, null) ) );
		
		this.streamOn = on;
	}
	
	private JTextField getFilterRangeText()
	{
		if( this.filterRangeText == null )
		{
			this.filterRangeText = new JTextField();
			
			this.filterRangeText.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JTextField jt = (JTextField)e.getSource();
					String filterText = jt.getText();
					
					defineFilters( filterText );
				}
			});
		}
		
		return this.filterRangeText;
	}
	
	private JButton getJButtonApplyFilters()
	{
		if( this.applyFilters == null )
		{
			this.applyFilters = new JButton( Language.getLocalCaption( Language.APPLY_TEXT ) );
			//this.applyFilters.setBorder( BorderFactory.createSoftBevelBorder( SoftBevelBorder.RAISED ) );
			
			this.applyFilters.addActionListener(new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{					
					String filterText = getFilterRangeText().getText();
					
					defineFilters( filterText );
				}
			});
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.APPLY_TEXT, this.applyFilters );
		}
		
		return this.applyFilters;
	}
	
	private JButton getJButtonUndockPlot()
	{
		if( this.btUndockPlot == null )
		{
			this.btUndockPlot = new JButton();
			
			try
			{
				this.btUndockPlot.setIcon( GeneralAppIcon.WindowMax( 16, Color.BLACK ) );
			}
			catch (Exception e) 
			{
				this.btUndockPlot.setText( "undock" );
			}
			
			this.btUndockPlot.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{	
					JButton b = (JButton)e.getSource();
					b.setVisible( false );
					
					RemoveContainerPanel();
					
					JFrame dialog = getDialogPlot();
					
					Toolkit t = Toolkit.getDefaultToolkit();
					Dimension dm = t.getScreenSize();

					int w = dm.width / 2;
					int h = dm.height / 2;
					
					dialog.setLocation( ( dm.width - w )/ 2, ( dm.height - h ) / 2 );
					dialog.setSize( w, h );
					
					dialog.setContentPane( getContainerPanel() );
					
					dialog.setVisible( true );
				}
			});
		}
		
		return this.btUndockPlot;
	}
	
	private JFrame getDialogPlot()
	{
		if( this.jFramePlot == null )
		{
			this.jFramePlot = new JFrame();
			
			this.jFramePlot.setLayout( new BorderLayout() );
			this.jFramePlot.setDefaultCloseOperation( JDialog.HIDE_ON_CLOSE );
			this.jFramePlot.setTitle( ConfigApp.fullNameApp + ": " + Language.getLocalCaption( Language.SETTING_LSL_PLOT ) );
			this.jFramePlot.setIconImage( GeneralAppIcon.getIconoAplicacion( 32, 32 ).getImage() );
			
			this.jFramePlot.addWindowListener( new WindowAdapter()
			{						
				@Override
				public void windowClosing(WindowEvent e) 
				{
					getJButtonUndockPlot().setVisible( true );
					SetContainerPanel();
				}				
			});
		}
		
		return this.jFramePlot;
	}
	
	public boolean isUndock()
	{
		return this.getDialogPlot() != null && this.getDialogPlot().isVisible(); 
	}
	
	public void disposeUndockWindow()
	{
		if( this.getDialogPlot() != null )
		{
			this.getDialogPlot().dispose();
		}
	}
	
	private void defineFilters( String filterText )
	{
		clearFilters();
		
		if( filterText != null && !filterText.isEmpty() )
		{
			filterText = filterText.trim().replace( " ", "" );
			String[] FILTERS = filterText.split( ";" );
			Arrays.sort( FILTERS );			
			
			for( String filter : FILTERS )
			{
				try
				{
					String[] filParts = filter.split( ":" );
					if( filParts.length == 2 )
					{
						int plotNo = new Integer( filParts[ 0 ] );						
						
						String datFilters = filParts[ 1 ];
						
						if( !datFilters.isEmpty() )
						{	
							String[] intervals = datFilters.split( "\\)\\[" );
							
							if( intervals.length == 2 )
							{
								this.setProcessingRange( plotNo, intervals[0]+")", '(', ')', this.filters );//, true );								
								this.setProcessingRange( plotNo, "["+intervals[1], '[', ']', this.visRange );//, false );
							}
							else if( intervals.length == 1 )
							{
								String interval = intervals[ 0 ];
								
								this.setProcessingRange( plotNo, interval, '(', ')', this.filters);//, true );																
								this.setProcessingRange( plotNo, interval, '[', ']', this.visRange);//, false );								
							}
						}
					}
				}
				catch( Exception ex )
				{		
					ex.printStackTrace();
				}
			}
		}
	}
	
	private void setProcessingRange( int plotNo, String interval, char delim1, char delim2,HashMap< String, NumberRange > process )//, boolean addIgnored )
	{
		if( process != null && interval != null )
		{
			NumberRange range = null;
			
			if( interval.charAt( 0 ) == delim1 && interval.charAt( interval.length() - 1 ) == delim2 )
			{
				String[] values = interval.replace( delim1+"", "" ).replace( delim2+"", "").split( "," );
				
				if( values.length == 2 )
				{
					range = this.getFilterRange( values[0], values[ 1 ] );
					
					/*
					if( addIgnored )
					{
						this.ignoredCols.add( plotNo - 1 );
					}
					//*/
				}									
			}
			
			if( range != null )
			{
				process.put( "" + plotNo, range );
			}
		}
	}
		
	private NumberRange getFilterRange( String a, String b )
	{
		NumberRange range = null;

		double min = Double.NEGATIVE_INFINITY;
		if( !a.toLowerCase().equals( "-inf" ) )
		{
			min = new Double( a );
		}

		double max = Double.POSITIVE_INFINITY;
		if( !b.toLowerCase().equals( "inf" ) )
		{
			max = new Double( b );
		}								

		if( !Double.isNaN( max ) && !Double.isNaN( min ) )
		{
			range = new NumberRange( min, max );
		}
		
		return range;
	}
	
	private JButton getJButtonInfoFilters()
	{
		if( this.infoFilters == null )
		{
			this.infoFilters = new JButton( "?" );
			this.infoFilters.setPreferredSize( new Dimension( 16, 16 ) );
			this.infoFilters.setBackground( Color.YELLOW.darker() );
			this.infoFilters.setBorder( BorderFactory.createLineBorder( Color.BLACK ) );
			
			this.infoFilters.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JButton b = (JButton)e.getSource();
					Window setTask = (Window) SwingUtilities.getWindowAncestor( b );
										
					Dialog_Info w = new Dialog_Info( setTask, getFilterTextInfo() );
					
					w.setSize( 300, 200 );
					//Dimension size = w.getSize();
					Point pos = b.getLocationOnScreen();
					
					Point loc = new Point( pos.x - w.getWidth() //- size.width
											, pos.y + b.getHeight()); 
					
					if( loc.x < 0 )
					{
						loc.x = pos.x + b.getWidth();
					}
					
					Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
					if( loc.y + w.getHeight() > screenSize.height )
					{
						loc.y = pos.y - w.getHeight();
					}
					
					w.setLocation( loc );
					w.toFront();
					w.setVisible( true );
				}
			});
		}			
		
		return this.infoFilters;
	}
	
	private String getFilterTextInfo()
	{
		return Language.getLocalCaption( Language.LSL_PLOT_FILTER_LEGEND );
	}
	
	private JScrollPane getJScrollPanelPlot()
	{
		if( this.scrollpanel == null )
		{
			//JPanel auxPlotPanel = new JPanel( new BorderLayout() );
			//auxPlotPanel.add( this.getJPanelPlots(), BorderLayout.NORTH );
			
			//this.scrollpanel = new JScrollPane( auxPlotPanel );
			this.scrollpanel = new JScrollPane( this.getJPanelPlots() );
			this.scrollpanel.setVisible( true );
			this.scrollpanel.getVerticalScrollBar().setUnitIncrement( 16 );
			
			this.scrollpanel.addComponentListener(new ComponentAdapter()
			{
				Timer controlTimer = null;
				int DELAY = 20;

				public void componentResized(ComponentEvent e)
				{
					/*
					JPanel p = (JPanel)e.getSource();
					Container c = p.getParent();
										
					if( c != null )
					{
						if( p.getSize().equals( c.getSize() ) )
						{
							p.setSize( c.getSize() );							
						}
						
						Component[] PLOTs = p.getComponents();
						
						Dimension plotDim = getDimensionPlots( p.getSize() );
						int aj = 10;
								
						if( PLOTs.length > 0 )
						{
							aj /= PLOTs.length;
						}
						
						if( aj < 1 )
						{
							aj = 1;
						}
						plotDim.width -= 10 ;
						plotDim.height -= aj;
						
						for( Component plot : PLOTs )
						{
							if( !hiddenPlots.contains( plot.getName() ) )
							{
								plot.setVisible( false );
								plot.setPreferredSize( plotDim );
								plot.setSize( plotDim );
								plot.setVisible( true );
							}
						}
					}
					//*/
					//setPlotPanelSize();
					update();
				}

				private void update()
				{
					if (this.controlTimer == null)
					{
						this.controlTimer = new Timer(this.DELAY, 
								new ActionListener()
						{

							public void actionPerformed(ActionEvent ac)
							{
								if (ac.getSource() == controlTimer)
								{
									controlTimer.stop();
									controlTimer = null;
									
									//setPlotPanelSize();
									//updatePlot();
								}								                  
							}
						});
						this.controlTimer.start();
					}
					else
					{
						this.controlTimer.restart();
					}
				}
			});
		}
		
		return this.scrollpanel;
	}
	
	
	private JPanel getJPanelPlots()
	{
		if (this.panelPlots == null)
		{
			this.panelPlots = new JPanel();
			//this.panelPlots.setPreferredSize( new Dimension(500,500));
			this.panelPlots.setVisible(true);
			this.panelPlots.setBorder(new LineBorder(new Color(0, 0, 0), 1, false));
			//this.panelPlots.setBackground( Color.RED );

			//this.panelPlots.setLayout(new GridLayout(0, 1, 0, 0));
			//this.panelPlots.setLayout( new BoxLayout( this.panelPlots, BoxLayout.Y_AXIS ) );
			//this.panelPlots.add( this.getJScrollPanelPlot(), BorderLayout.CENTER );
			this.panelPlots.setLayout( new VerticalFlowLayout( VerticalFlowLayout.TOP ) );
			
			
			//*
			this.panelPlots.addComponentListener(new ComponentAdapter()
			{
				public void componentResized(ComponentEvent e)
				{
					if( !streamOn )
					{
						setPlotPanelSize();
						updatePlot();
					}
				}
			});
			//*/
			/*
			this.panelPlots.addComponentListener(new ComponentAdapter()
			{
				Timer controlTimer = null;
				int DELAY = 20;

				public void componentResized(ComponentEvent e)
				{
					
					//setPlotPanelSize();
					update();
				}

				private void update()
				{
					if (this.controlTimer == null)
					{
						this.controlTimer = new Timer(this.DELAY, 
								new ActionListener()
						{

							public void actionPerformed(ActionEvent ac)
							{
								if (ac.getSource() == controlTimer)
								{
									controlTimer.stop();
									controlTimer = null;

									//updatePlot();
									setPlotPanelSize();
								}								                  
							}
						});
						this.controlTimer.start();
					}
					else
					{
						this.controlTimer.restart();
					}
				}
			});
			//*/
		}

		return this.panelPlots;
	}
	
	private Dimension getDimensionPlots( Dimension containerDim )
	{
		int plotWidth = 100;
		int plotHeigh= 100;
		
		if( !this.xy.isEmpty() )
		{
			plotWidth = containerDim.width;
			plotHeigh = containerDim.height / this.xy.size();
			
			if( plotHeigh < 100 )
			{
				plotHeigh = 100;
			}
		}
		
		return new Dimension( plotWidth, plotHeigh );
	}
	
	private void setPlotPanelSize()
	{	
		if( this.panelPlots.getComponentCount() > 0 )
		{		
			Dimension pSize = getJScrollPanelPlot().getSize();
			pSize.width -= ( getJScrollPanelPlot().getVerticalScrollBar().getWidth() );
			//pSize.height -= 1;
			
			Dimension plotDim = this.getDimensionPlots( new Dimension() );
			int minH = plotDim.height;
			
			int numVisPlot = 0;
			for( Component plot : this.panelPlots.getComponents() )
			{
				numVisPlot += ( plot.isVisible() ) ? 1 : 0;
			}

			if( numVisPlot > 0 )
			{
				if( plotDim.width < 1 )
				{
					plotDim.width = pSize.width;
				}

				int vDispSpace = pSize.height; 

				if( plotDim.height < vDispSpace / numVisPlot )
				{
					plotDim.height = vDispSpace / numVisPlot ;// - ( (this.panelPlots.getComponentCount() + 1) / numVisPlot );
					//plotDim.height -= 1;
				}
								
				if( numVisPlot == 1 )
				{
					plotDim.height -= 5;
				}
				
				//System.out.println("CanvasStreamDataPlot.setPlotPanelSize() " + pSize.height + " - " + plotDim.height*numVisPlot + " : " + hPad);
				
				//getJPanelPlots().setVisible( false );
				
				for( Component plot : this.panelPlots.getComponents() )
				{									
					if( !this.hiddenPlots.contains( plot.getName() ) )
					{
						//plot.setVisible( false );
	
						Dimension plSize = new Dimension( plotDim );
						
						Border b = ((PLOT)plot).getBorder();
						if( b != null )
						{
							Insets ins = b.getBorderInsets( plot );
							
							plSize.height -= ( ins.top - ins.bottom);
							//plSize.height += 5;
							plSize.width -= (ins.left + ins.right );
						}
						
						if( plSize.height < minH )
						{
							plSize.height = minH;
						}
						
						plot.setPreferredSize( plSize );
						plot.setSize( plSize );
	
						//plot.setVisible( true );
					}
				}
				
				//getJPanelPlots().setVisible( true );
			}
		}
	}
	
	private void updatePlot()
	{
		try 
		{	
			this.sem.acquire( this.sem.availablePermits() );
						
			if( !this.xy.isEmpty() )
			{				
				//Dimension panelPlotSize = this.getJPanelPlots().getSize();
				Dimension panelPlotSize = this.getJScrollPanelPlot().getSize();
				//panelPlotSize.width -= this.getJScrollPanelPlot().getVerticalScrollBar().getWidth();
				
				//Dimension plotDim = this.getDimensionPlots( panelPlotSize );
				
				//List< Queue< Double > > this.xy = new ArrayList< Queue < Double > >( this.xy );
				if( this.xy.size() != this.panelPlots.getComponentCount() )
				{
					this.panelPlots.setVisible( false );
					//this.panelPlots.removeAll();

					while( this.xy.size() < this.panelPlots.getComponentCount() )
					{
						this.panelPlots.remove( this.panelPlots.getComponentCount() - 1 );
					}

					int i = this.panelPlots.getComponentCount();
					
					while( this.xy.size() >  i )
					{			
						PLOT plot = new PLOT( );
						//plot.setPreferredSize( plotDim );
						
						plot.setBackground( Color.WHITE );						
						
						plot.setVisible( true );

						plot.setName( this.plotNames.get( i )  );
						//plot.setBorder( BorderFactory.createTitledBorder( BorderFactory.createLineBorder( Color.BLACK ), "Plot " + ( i + 1 ) ) );
						plot.setBorder( BorderFactory.createTitledBorder( BorderFactory.createLineBorder( Color.BLACK )
																		, Language.getLocalCaption( Language.SETTING_LSL_PLOT ) + " " + ( i + 1 ) + " - " + this.plotName ) );						
						i++;

						this.panelPlots.add( plot );
					}
					
					
					JPanel selPlotChPanel = this.getSelectionPlottedChannels();
					
					getContainerPanel().setVisible( false );
					selPlotChPanel.setVisible( false );
					selPlotChPanel.removeAll();
					
					List< JCheckBox > chboxes = new ArrayList<JCheckBox>();
					for( int ich = 0; ich < this.panelPlots.getComponentCount(); ich++ )
					{
						JCheckBox ch = new JCheckBox( this.panelPlots.getComponent( ich ).getName() );
						ch.setSelected( true );
						
						ch.addItemListener( new ItemListener() 
						{	
							@Override
							public void itemStateChanged(ItemEvent e) 
							{
								JCheckBox chb = (JCheckBox)e.getSource();
								
								if( e.getStateChange() == ItemEvent.SELECTED )
								{
									hiddenPlots.remove( chb.getText() );
								}
								else
								{
									hiddenPlots.add( chb.getText() );
								}
								
								if( !streamOn )
								{
									setPlotPanelSize();
									updatePlot();
								}
							}
						});
						
						chboxes.add( ch );						
						selPlotChPanel.add( ch );
					}
					
					JRadioButton allch = new JRadioButton( Language.getLocalCaption( Language.ALL_TEXT ) );
					allch.setSelected( true );
					allch.addActionListener( new ActionListener() 
					{						
						@Override
						public void actionPerformed(ActionEvent e) 
						{
							JRadioButton all = (JRadioButton)e.getSource();
							all.setSelected( true );
							
							for( JCheckBox ch : chboxes )
							{
								ch.setSelected( true );
							}
						}
					});
					
					JRadioButton nonech = new JRadioButton( Language.getLocalCaption( Language.NONE_TEXT ) );
					nonech.setSelected( true );
					nonech.addActionListener( new ActionListener() 
					{						
						@Override
						public void actionPerformed(ActionEvent e) 
						{
							JRadioButton all = (JRadioButton)e.getSource();
							all.setSelected( true );
							
							for( JCheckBox ch : chboxes )
							{
								ch.setSelected( false );
							}
						}
					});
					
					selPlotChPanel.add( nonech, 0 );
					selPlotChPanel.add( allch, 0 );
										
					selPlotChPanel.setVisible( true );
					getContainerPanel().setVisible( true );
					
					this.panelPlots.setVisible( true );
					
					//this.setPlotPanelSize();
				}

				if( this.newPlotName )
				{
					Component[] plots = this.panelPlots.getComponents();

					for( int i = 0; i < plots.length; i++ )
					{
						PLOT plot = (PLOT)plots[ i ];

						plot.setVisible( false );

						plot.setBorder( BorderFactory.createTitledBorder( BorderFactory.createLineBorder( Color.BLACK ), Language.getLocalCaption( Language.SETTING_LSL_PLOT ) + " " + ( i + 1 ) + " - " + this.plotName ) );

						plot.setVisible( true ); 
					}

					this.newPlotName = false;
				}
			
				/*
				int numVisPlot = panelPlots.getComponentCount() - this.hiddenPlots.size();
				int plotHeigh = panelPlotSize.height;
				int plotWidth = panelPlotSize.width;
				Dimension minPlotSize = getDimensionPlots( new Dimension() );
				if( numVisPlot > 0 )
				{
					plotHeigh /= numVisPlot; 
				}
				
				if( plotHeigh < minPlotSize.height )
				{
					plotHeigh = minPlotSize.height;
				}
				
				Dimension plotSize = new Dimension( plotWidth, plotHeigh );
				//*/ 
				
				this.setPlotPanelSize();
				
				for( int i = 0 ; i < this.xy.size(); i++ )
				{
					PLOT plot = (PLOT)this.panelPlots.getComponent( i );
					
					plot.setVisible( !this.hiddenPlots.contains( plot.getName() ) );
					
					Queue< Double > data = this.xy.get( i );
					
					double[][] d = new double[ 2 ][ data.size() ];

					int j = 0;
					for( Double value : data )
					{
						d[ 0 ][ j ] = j;
						d[ 1 ][ j ] = value;
						j++;
					}

					DefaultXYDataset xyValues = new DefaultXYDataset();
					xyValues.addSeries( plot.getName(), d );
					final JFreeChart chart = ChartFactory.createXYLineChart( null, null, null, xyValues  );
					chart.setAntiAlias( true );				
					chart.setBackgroundPaint( Color.WHITE );
					chart.getXYPlot().setBackgroundPaint( Color.WHITE );
					chart.getXYPlot().setRangeGridlinePaint( Color.BLACK );
					chart.getXYPlot().setDomainGridlinePaint( Color.BLACK );
					XYLineAndShapeRenderer render = new XYLineAndShapeRenderer();
					render.setDefaultShapesVisible( true );
					render.setSeriesStroke( 0, new BasicStroke( 3F ) );
					chart.getXYPlot().setRenderer( render );
					chart.getXYPlot().getDomainAxis().setTickLabelFont( new Font( Font.DIALOG, Font.BOLD, 15 ) );
					chart.getXYPlot().getRangeAxis().setTickLabelFont( new Font( Font.DIALOG, Font.BOLD, 15 ) );
					ValueAxis yaxis = chart.getXYPlot().getRangeAxis();

					try
					{
						double mean = ( this.maxY.get( i ) + this.minY.get( i ) ) / 2;
						double amp = ( this.maxY.get( i ) - this.minY.get( i ) ) / 2;
						if( amp == 0 )
						{
							amp = mean / 2;
						}

						if( amp < 1e-15 )
						{
							amp = 1e-15;		
						}

						yaxis.setRange( mean - amp, mean + amp );	
					}
					catch( Exception e )
					{
						e.printStackTrace();
					}

					chart.getXYPlot().setRangeAxis( yaxis );				
					chart.clearSubtitles();
					
					if( !this.hiddenPlots.contains( plot.getName() ) )
					{
						plot.setChart( chart );
						
						/*
						Dimension prefSize = plot.getPreferredSize();
						Dimension size = plot.getSize();
						
						if( !prefSize.equals( plotSize ) )
						{
							//plot.setPreferredSize( plotSize );
						}
						
						if( !size.equals( plot.getPreferredSize() ) )
						{
							plot.setSize( plot.getPreferredSize() );	
						}
						//*/
						
						if( !plot.getSize().equals( new Dimension() )  )
						{
							drawPlot( chart, plot );
						}					
					}
				}				
			}
			else
			{
				this.panelPlots.setVisible( false );
				this.panelPlots.removeAll();
				this.panelPlots.setVisible( true );
			}
		} 
		catch (InterruptedException e1) 
		{
			//e1.printStackTrace();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
		finally
		{	
			if( this.sem.availablePermits() < 1 )
			{
				this.sem.release( );
			}
		}
	}

	private void drawPlot(JFreeChart chart, JLabel canva)
	{
		if ((chart != null) && (canva != null))
		{
			Rectangle r = canva.getBounds();
			Insets pad = canva.getBorder().getBorderInsets(canva);
			int w = r.width - pad.left - pad.right;
			int h = r.height - pad.top - pad.bottom;
			if ((w > 0) && (h > 0))
			{
				Image img = BasicPainter2D.createEmptyImage( w, h, null );//chart.createBufferedImage(w, h);
				
				chart.draw( (Graphics2D)img.getGraphics(), 
							new Rectangle2D.Double( 0, 0, img.getWidth( null ), img.getHeight( null ) ) );
				
				canva.setIcon(new ImageIcon(img));
			}
		}
	}

	public boolean addXYData( final List<List<Double>> XY )
	{
		boolean adding = this.sem.tryAcquire(this.sem.availablePermits());
		
		if (adding)
		{
			Iterator<List<Double>> it = XY.iterator();

			this.plotNames.clear();
			//int inputDataIndex = 0;
			int plotIndex = 0;
			
			// General Filter 
			NumberRange genRng = this.filters.get( "" + ( plotIndex ) );
			NumberRange genVisRng = this.visRange.get( "" + ( plotIndex ) );
			
			while( it.hasNext() )
			{
				List<Double> xyValues = it.next();


				Queue<Double> queue = null;

				if ( plotIndex >= this.xy.size() )
				{
					queue = new LinkedList< Double >();
					this.minY.add( Double.MAX_VALUE );
					this.maxY.add( Double.MIN_VALUE );

					this.xy.add(queue);
				}
				else
				{
					queue = this.xy.get( plotIndex );
				}

				//this.plotNames.add( "" + inputDataIndex);
				this.plotNames.add( "" + (plotIndex+1) );

				queue.addAll( xyValues );
				if (this.queueLength > 0)
				{
					while ( queue.size() > this.queueLength )
					{
						queue.poll();
					}
				}

				NumberRange rng = this.filters.get( "" + ( plotIndex + 1 ) );

				RangeFilter filter = new RangeFilter();
				filter.addRange( rng );
				filter.addRange( genRng );

				Iterator< Double > itDouble = queue.iterator();

				while( itDouble.hasNext() )
				{
					double val = itDouble.next();

					if( filter.removeValue( val ) )
					{
						itDouble.remove();
					}
				}

				NumberRange visRange = this.visRange.get( "" + ( plotIndex + 1 )  );

				double min = Double.MAX_VALUE;
				double max = Double.MIN_VALUE;

				if( visRange != null )
				{
					min = visRange.getMin();
					max = visRange.getMax();
				}
				else if( genVisRng != null )
				{
					min = genVisRng.getMin();
					max = genVisRng.getMax();	
				}
				else
				{
					for (Double v : queue)
					{
						if ( (v.doubleValue() != Double.NaN )
								&& (v.doubleValue() > max))
						{
							max = v.doubleValue();
						}

						if ((v.doubleValue() != Double.NaN ) 
								&&  (v.doubleValue() < min) )
						{
							min = v.doubleValue();
						}
					}
				}

				if (plotIndex >= this.minY.size())
				{
					this.minY.add( min );
					this.maxY.add( max );
				}
				else
				{
					this.minY.set(plotIndex, min );
					this.maxY.set(plotIndex, max );
				}

				plotIndex++;	
				
				//inputDataIndex++;
				
			}

			/*
			while (this.xy.size() > inputDataIndex - this.ignoredCols.size())
			{
				this.xy.remove(this.xy.size() - 1);
			}
			//*/


			this.sem.release();

			this.updatePlot();
		}

		return adding;
	}

	public void setDataLength(int length)
	{
		this.queueLength = length;
	}

	public void clearData()
	{
		try
		{
			this.sem.acquire( this.sem.availablePermits() );
						
			this.xy.clear();
			
			JPanel p = this.getJPanelPlots();
			p.setVisible( false );
			p.removeAll();
			p.setVisible( true );
						
			if( this.sem.availablePermits() < 1 )
			{
				this.sem.release();
			}
		}
		catch( Exception ex )
		{		
			
		}
	}

	//////////////////////////////////////
	/////////////////////////////////////
	//////////////////////////////////////
	private class PLOT extends JLabel
	{
		private static final long serialVersionUID = 1L;

		private JFreeChart chart = null;

		public PLOT() 
		{
			super();
			
			super.addComponentListener( new ComponentAdapter()
			{				
				@Override
				public void componentResized(ComponentEvent e) 
				{
					/*
					JLabel canvas = (JLabel)e.getSource();	
					if( canvas.isVisible() )
					{
						JFreeChart jfChart = getChart();
						
						drawPlot( jfChart, canvas );
					}
					//*/
				}
			});
		}

		public JFreeChart getChart()
		{
			return this.chart;
		}

		public void setChart(JFreeChart c)
		{
			this.chart = c;
		}
	}
	
	
	//////////////////////////////////////
	/////////////////////////////////////
	//////////////////////////////////////
	private class RangeFilter
	{
		private List< NumberRange > ranges;
		
		public RangeFilter() 
		{
			this.ranges = new ArrayList<>();
		}
		
		public void addRange( NumberRange rng )
		{
			if( rng != null )
			{
				NumberRange addRng = null;
				
				if( this.ranges.isEmpty() )
				{
					addRng = rng;
				}
				else
				{				
					Iterator< NumberRange > it = this.ranges.iterator();
					
					while( it.hasNext() && addRng == null )
					{
						NumberRange r = it.next();
						
						if( r.overlap( rng ) )
						{
							if( !r.contain( rng ) )
							{
								double min = r.getMin() < rng.getMin() ? r.getMin() : rng.getMin();
								double max = r.getMax() > rng.getMax() ? r.getMax() : rng.getMax();
								
								addRng = new NumberRange( min, max );
								
								it.remove();
							}				
						}
					}
					
					if( addRng == null )
					{
						addRng = rng;
					}
				}
				
				if( addRng != null )
				{
					this.ranges.add( addRng );
				}
			}
		}
		
		public boolean removeValue( double value )
		{
			boolean remove = !this.ranges.isEmpty();
			
			if( remove )
			{
				for( NumberRange r : this.ranges )
				{
					remove = remove && !r.within( value );
				}
			}
			
			return remove;
		}
	}
}
