/**
 * 
 */
package lslrec.gui.dialog;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import lslrec.auxiliar.extra.FileUtils;
import lslrec.config.language.Language;
import lslrec.dataStream.convertData.clis.ClisData;
import lslrec.dataStream.convertData.clis.MetadataVariableBlock;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.gui.miscellany.BasicPainter2D;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.gui.miscellany.TextLineNumber;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;

import javax.swing.JLabel;
import java.awt.Container;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.commons.lang3.StringUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.DefaultXYDataset;

import javax.swing.JComboBox;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

/**
 * @author Manuel Merino Monge
 *
 */
public class Dialog_PlotClis extends JDialog 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2257212620236071644L;
	
	private JPanel centerPanel = null;
	private JPanel southPanel;	
	private JPanel centerOutputFormatPanel;
	private JPanel loadFilePanel;
	private JPanel infoFilePanel;
	private JPanel loadFileBtnPanel;	
	private JPanel plotPanel;
	
	private JButton btnLoadFile;
	private JButton btnClose;
		
	private JTextField clisFileText;
	
	private JLabel lblLoadFile;	
	
	private String currentFolderPath;
	
	private ClisData currentClisFile = null;
	private Map< String, Number[][] > clisData = null;
	private int sampleIndex_A = 0;
	private int sampleIndex_B = 1000;
	private MetadataVariableBlock currentVar = null;
	
	
	/**
	 * Launch the application.
	 */
	private static Dialog_PlotClis dgclis = null;
	//private JPanel panelPlotCtr;
	private JPanel panelMovePlotCtr;
	private JPanel panelPlotYAxis;
	private JPanel panelPlotCanva;
	private JPanel panelPlotCtr;
	private JLabel lblVariable;
	private JComboBox<String> cbVariables;
	private JLabel lblChannel;
	private JSpinner spinnerChannel;
	private JLabel lblStep;
	private JSpinner spinnerStep;
	private JButton btnPrevious;
	private JButton btnNext;
	private JLabel canva;
	
	public static void main(String[] args) {
		try {
					
			JFrame jf = new JFrame();
			
			JButton jb = new JButton( "show" );
			
			jb.addActionListener( new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					showJdialog(); 
				}
			});
			
			jf.getContentPane().add( jb );
			jf.setBounds( 100, 100 , 300, 100 );
			
			jf.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
			
			jf.addWindowListener( new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) 
				{
					if( dgclis != null )
					{
						dgclis.dispose();
					}
				}
			});
			
			jf.setVisible( true );
						
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void showJdialog()
	{
		if( dgclis != null )
		{
			dgclis.dispose();
		}
		
		dgclis = new Dialog_PlotClis();
		dgclis.setBounds( 200, 100, 600, 400 );
		
		dgclis.setVisible( true );
		
	}
		
	/**
	 * Create the dialog.
	 */
	public Dialog_PlotClis() 
	{	
		this.currentFolderPath = System.getProperty( "user.dir" );
		
		super.setBounds( 100, 100, 1000, 300 );
		
		super.getContentPane().setLayout( new BorderLayout() );
		
		super.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
		
		Container container = super.getContentPane();
		container.add( this.getCenterPanel(), BorderLayout.CENTER);
		super.getContentPane().add( this.getSouthPanel(), BorderLayout.SOUTH);
		super.getContentPane().add( this.getLoadFilePanel(), BorderLayout.NORTH);
	}
	
	private JPanel getCenterPanel()
	{
		if( this.centerPanel == null )
		{
			this.centerPanel = new JPanel();
			
			this.centerPanel.setLayout( new BorderLayout( 0, 5 ) );
			this.centerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			
			this.centerPanel.add( this.getInfoFilePanel(), BorderLayout.WEST );
			this.centerPanel.add( this.getPlotPanel(), BorderLayout.CENTER );
		}
		
		return this.centerPanel;
	}
		
	private JPanel getSouthPanel() 
	{
		if (southPanel == null) 
		{
			southPanel = new JPanel();
			southPanel.setLayout( new FlowLayout( FlowLayout.RIGHT ));
			
			southPanel.add( this.getBtnOk() );
		}
		return southPanel;
	}
	
	private JButton getBtnOk() 
	{
		if (btnClose == null) 
		{
			btnClose = new JButton( Language.getLocalCaption( Language.MENU_EXIT ) );
			
			final Dialog_PlotClis dcc = this;
			this.btnClose.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					dcc.dispose();
				}
			});
		}
		
		return btnClose;
	}
	
	private JPanel getLoadFilePanel() 
	{
		if ( this.loadFilePanel == null ) 
		{
			this.loadFilePanel = new JPanel( );
			loadFilePanel.setLayout( new BorderLayout(5, 0) );
			
			this.loadFilePanel.add( this.getCenterOutputFormatPanel(), BorderLayout.CENTER );
			this.loadFilePanel.add( this.getLoadFileBtnPanel(), BorderLayout.WEST);
		}
		return this.loadFilePanel;
	}
	
	private JButton getBtnLoadFile() 
	{
		if (btnLoadFile == null) {
			
			btnLoadFile = new JButton(  );
			
			this.btnLoadFile.setBorder( BorderFactory.createRaisedSoftBevelBorder( ) );
			
			ImageIcon ic = GeneralAppIcon.Folder( 24, 20, Color.BLACK, Color.ORANGE );
			
			if( ic != null )
			{
				this.btnLoadFile.setIcon( ic );
			}
			else
			{
				this.btnLoadFile.setText( Language.getLocalCaption( Language.LOAD_TEXT ) );
			}
			
			this.btnLoadFile.addActionListener( new ActionListener( ) 
			{
				public void actionPerformed( ActionEvent e ) 
				{	
					String idEncoder = DataFileFormat.CLIS;
					String ext = DataFileFormat.getSupportedFileExtension().get( idEncoder );
					
					String[] selExt = null;
					
					if( ext != null )
					{						
						if( ext.charAt( 0 ) == '.' )
						{
							ext = ext.substring( 1 );
						}
						selExt = new String[] { ext };
					}
					
					String[] FILE = FileUtils.selectUserFile( "", true, false, JFileChooser.FILES_ONLY, idEncoder, selExt, currentFolderPath );
					if( FILE != null && FILE.length > 0 )
					{						
						getTxtClisFile().setText( FILE[ 0 ] );
						
						try 
						{
							currentClisFile = new ClisData( FILE[ 0 ] );
						} 
						catch ( Exception e1) 
						{
							currentClisFile = null;
						}
						
						showBinaryFileInfo( );
						setClisDataPlotMetadata( );
					}
				}
			} );
			
		}
		return btnLoadFile;
	}
	
	private JLabel getLblLoadFile() 
	{
		if (lblLoadFile == null) 
		{
			lblLoadFile = new JLabel( Language.getLocalCaption( Language.LOAD_TEXT ) );
		}
		
		return lblLoadFile;
	}
		
	private JPanel getCenterOutputFormatPanel()
	{
		if( this.centerOutputFormatPanel == null )
		{
			this.centerOutputFormatPanel = new JPanel( );
			centerOutputFormatPanel.setBorder(new EmptyBorder(0, 0, 0, 5));
			BoxLayout bl = new BoxLayout( this.centerOutputFormatPanel, BoxLayout.X_AXIS );
			this.centerOutputFormatPanel.setLayout( bl );
			
			this.centerOutputFormatPanel.add( this.getTxtClisFile() );
		}
		
		return this.centerOutputFormatPanel;
	}
		
	private JTextField getTxtClisFile( ) 
	{
		if ( clisFileText == null )
		{
			clisFileText = new JTextField( );			
			clisFileText.setColumns( 10 );
			clisFileText.setEditable( false );
			
			Dimension s = new Dimension();
			
			FontMetrics fm = clisFileText.getFontMetrics( clisFileText.getFont() );
			s.width = fm.stringWidth( "W" ) * 30;
			
			clisFileText.setPreferredSize( s );
		}
		
		return clisFileText;
	}
	
	private void showBinaryFileInfo( )	
	{
		JPanel infoPanel = getInfoFilePanel();
		
		infoPanel.setVisible( false );
		
		infoPanel.removeAll();

		if( this.currentClisFile != null )
		{
			JTextPane jta = new JTextPane();
			jta.setEditable( false );
						
			List< MetadataVariableBlock > VARS = currentClisFile.getVarInfo();

			String tx = "";

			for( MetadataVariableBlock var : VARS )
			{
				tx += var.getName() + "," + var.getDataType().name() + "," + var.getCols() + "\n";
			}

			tx += currentClisFile.getHeader();

			tx = tx.replaceAll( "\t", "      " );
			
			jta.setText( tx );

			JScrollPane scrp = new JScrollPane( jta );
			TextLineNumber tln = new TextLineNumber( jta );
			scrp.setRowHeaderView( tln );
			
			scrp.getVerticalScrollBar().setPreferredSize( new Dimension( 10, 0) );
			scrp.getHorizontalScrollBar().setPreferredSize( new Dimension( 0, 10) );
			
			infoPanel.add( scrp , BorderLayout.CENTER );
		}
		
		infoPanel.setVisible( true );
	}
	
	private JPanel getInfoFilePanel()
	{
		if( this.infoFilePanel == null )
		{
			this.infoFilePanel = new JPanel( new BorderLayout( 0, 0 ) );
			this.infoFilePanel.setBorder( BorderFactory.createLineBorder( Color.BLACK ) );
			
			Dimension d = new Dimension( 250, 0 );
			this.infoFilePanel.setPreferredSize( d );
			this.infoFilePanel.setSize( d );
			this.infoFilePanel.setBackground( Color.WHITE );
		}
		
		return this.infoFilePanel;
	}
		
	private JPanel getLoadFileBtnPanel()
	{
		if ( this.loadFileBtnPanel == null) 
		{
			this.loadFileBtnPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			
			this.loadFileBtnPanel.add( this.getLblLoadFile() );
			this.loadFileBtnPanel.add( this.getBtnLoadFile() );
		}
		return this.loadFileBtnPanel;
	}
	
	private JPanel getPlotPanel()
	{
		if( this.plotPanel == null )
		{
			this.plotPanel = new JPanel();
			this.plotPanel.setBackground( Color.WHITE );
			this.plotPanel.setLayout(new BorderLayout(0, 0));
			
			this.plotPanel.add( this.getPanelMovePlotCtr(), BorderLayout.SOUTH);
			this.plotPanel.add( this.getPanelPlotYAxis(), BorderLayout.WEST);
			this.plotPanel.add( this.getPanelPlotCanva(), BorderLayout.CENTER);
			this.plotPanel.add( this.getPanelPlotCtr(), BorderLayout.NORTH);
			//this.plotPanel.add( this.getPanelPlotCtr(), BorderLayout.NORTH);
		}
		
		return this.plotPanel;
	}
	
	private JPanel getPanelMovePlotCtr() 
	{
		if (this.panelMovePlotCtr == null) 
		{
			this.panelMovePlotCtr = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 5));
			
			this.panelMovePlotCtr.add( this.getLblStep() );
			this.panelMovePlotCtr.add( this.getSpinnerStep() );
			this.panelMovePlotCtr.add( this.getBtnPrevious() );
			this.panelMovePlotCtr.add( this.getBtnNext() );
		}
		
		return this.panelMovePlotCtr;
	}
	
	private JPanel getPanelPlotYAxis() 
	{
		if (this.panelPlotYAxis == null) 
		{
			this.panelPlotYAxis = new JPanel();
		}
		
		return this.panelPlotYAxis;
	}
	
	private JPanel getPanelPlotCanva() 
	{
		if (this.panelPlotCanva == null) 
		{
			this.panelPlotCanva = new JPanel();
			this.panelPlotCanva.setBorder(new LineBorder(new Color(0, 0, 0)));
			this.panelPlotCanva.setBackground(Color.WHITE);
			panelPlotCanva.setLayout(new BorderLayout(0, 0));
			panelPlotCanva.add( this.getCanva(), BorderLayout.CENTER );
		}
		
		return this.panelPlotCanva;
	}
	private JPanel getPanelPlotCtr() {
		if (panelPlotCtr == null) {
			panelPlotCtr = new JPanel();
			FlowLayout flowLayout = (FlowLayout) panelPlotCtr.getLayout();
			flowLayout.setAlignment(FlowLayout.LEFT);
			panelPlotCtr.add(getLblVariable());
			panelPlotCtr.add(getCbVariables());
			panelPlotCtr.add(getLblChannel());
			panelPlotCtr.add(getSpinnerChannel());			
		}
		return panelPlotCtr;
	}
	private JLabel getLblVariable() {
		if (lblVariable == null) {
			lblVariable = new JLabel( Language.getLocalCaption( Language.VARIABLES_TEXT ));			
		}
		return lblVariable;
	}
	private JComboBox<String> getCbVariables() 
	{
		if (cbVariables == null) 
		{
			cbVariables = new JComboBox<String>();
			
			Font f = cbVariables.getFont();
			FontMetrics fm = cbVariables.getFontMetrics( f );
			int w = fm.stringWidth( StringUtils.repeat( "W", 10 ) );
			
			Dimension d = new Dimension( w, fm.getHeight() + 5 );
			cbVariables.setPreferredSize( d );
			
			cbVariables.addItemListener( new ItemListener() 
			{				
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					if( e.getStateChange() == ItemEvent.SELECTED )
					{
						setData2Plot( );						
					}
				}
			});
		}
		return cbVariables;
	}
	private JLabel getLblChannel() {
		if (lblChannel == null) {
			lblChannel = new JLabel( Language.getLocalCaption( Language.LSL_CHANNEL ) );
		}
		return lblChannel;
	}
	private JSpinner getSpinnerChannel() {
		if (spinnerChannel == null) {
			spinnerChannel = new JSpinner();
			spinnerChannel.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
			
			Font f = spinnerChannel.getFont();
			FontMetrics fm = spinnerChannel.getFontMetrics( f );
			int w = fm.stringWidth( StringUtils.repeat( "9", 5 ) );
			
			Dimension d = new Dimension( w, fm.getHeight() + 5 );
			spinnerChannel.setPreferredSize( d );
			
			spinnerChannel.addMouseWheelListener( new MouseWheelListener() 
			{				
				@Override
				public void mouseWheelMoved(MouseWheelEvent e) 
				{
					if( e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL )
					{
						try
						{	
							JSpinner sp = (JSpinner)e.getSource();

							int d = e.getWheelRotation();

							if( d > 0 )
							{
								sp.setValue( sp.getModel().getPreviousValue() );
							}
							else
							{
								sp.setValue( sp.getModel().getNextValue() );
							}	
						}
						catch( IllegalArgumentException ex )
						{												
						}
					}
				}
			});
			
			spinnerChannel.addChangeListener( new ChangeListener() 
			{				
				@Override
				public void stateChanged(ChangeEvent e) 
				{
					int step = (Integer)getSpinnerStep().getValue();
					sampleIndex_A = 0;
					sampleIndex_B = sampleIndex_A + step;
					
					drawDataPlot( sampleIndex_A, sampleIndex_B );
				}
			});
		}
		return spinnerChannel;
	}
	private JLabel getLblStep() {
		if (lblStep == null) {
			lblStep = new JLabel( Language.getLocalCaption( Language.STEP_TEXT ) );
		}
		return lblStep;
	}
	private JSpinner getSpinnerStep() {
		if (spinnerStep == null) {
			spinnerStep = new JSpinner();
			
			spinnerStep.setModel(new SpinnerNumberModel(new Integer( 1000 ), new Integer(1), null, new Integer(100)));
			
			Font f = spinnerStep.getFont();
			FontMetrics fm = spinnerStep.getFontMetrics( f );
			int w = fm.stringWidth( StringUtils.repeat( "9", 9 ) );
			
			Dimension d = new Dimension( w, fm.getHeight() + 5 );
			spinnerStep.setPreferredSize( d );
			
			spinnerStep.addMouseWheelListener( new MouseWheelListener() 
			{				
				@Override
				public void mouseWheelMoved(MouseWheelEvent e) 
				{
					if( e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL )
					{
						try
						{	
							JSpinner sp = (JSpinner)e.getSource();

							int d = e.getWheelRotation();

							if( d > 0 )
							{
								sp.setValue( sp.getModel().getPreviousValue() );
							}
							else
							{
								sp.setValue( sp.getModel().getNextValue() );
							}	
						}
						catch( IllegalArgumentException ex )
						{												
						}
					}
				}
			});
		}
		return spinnerStep;
	}
	private JButton getBtnPrevious() 
	{
		if (btnPrevious == null) {
			btnPrevious = new JButton();
			btnPrevious.setIcon( new ImageIcon( BasicPainter2D.paintTriangle( 10, 1F, Color.BLACK, Color.GRAY, BasicPainter2D.WEST ) ) );
			
			btnPrevious.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					updateSampleIndexes( false );
					drawDataPlot( sampleIndex_A, sampleIndex_B );
				}
			});
		}
		return btnPrevious;
	}
	private JButton getBtnNext() {
		if (btnNext == null) {
			btnNext = new JButton();
			btnNext.setIcon( new ImageIcon( BasicPainter2D.paintTriangle( 10, 1F, Color.BLACK, Color.GRAY, BasicPainter2D.EAST ) ) );			
			
			btnNext.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					updateSampleIndexes( true );
					drawDataPlot( sampleIndex_A, sampleIndex_B );
				}
			});
		}
		return btnNext;
	}
	
	private void updateSampleIndexes( boolean up )
	{
		if( this.clisData != null && this.currentVar != null )
		{
			int step = (Integer)getSpinnerStep().getValue();
			
			if( !up )
			{
				step *= -1;
			}
			
			int A = this.sampleIndex_A + step;
			int B = this.sampleIndex_B + step;
			
			if( A > B )
			{
				int aux = B;
				B = A;
				A = aux;
			}
			
			Number[][] dat = this.clisData.get( this.currentVar.getName() );
			
			int rows = dat.length;
			
			if( A >= rows )
			{
				A = rows - Math.abs( step );
			}
			else if( A < 0 )
			{
				A = 0;
			}
			
			if( B > rows )
			{
				B = rows;
				A = B - step;
			}
			else if( B < 0 )
			{
				B = A + Math.abs( step );
			}
			else if( ( B - A ) < Math.abs( step ) )
			{
				B = A + Math.abs( step );
			}
			
			this.sampleIndex_A = A;
			this.sampleIndex_B = B;
		}
	}
	
	private void setClisDataPlotMetadata( )
	{
		clearClisData();		
		
		if( this.currentClisFile != null )
		{
			List< MetadataVariableBlock > clisMeta = this.currentClisFile.getVarInfo();
			
			for( MetadataVariableBlock var : clisMeta )
			{
				getCbVariables().addItem( var.getName() );
			}
			
			if( getCbVariables().getItemCount() > 0 )
			{
				getCbVariables().setSelectedIndex( 0 );
			}
		}
	}
	
	private void setData2Plot(  )
	{
		this.getPanelPlotCanva().setVisible( false );
		
		SpinnerNumberModel spnm = (SpinnerNumberModel)getSpinnerChannel().getModel();
		spnm.setMaximum( null );
		spnm.setValue( 1 );
		
		JComboBox< String > cbVar = getCbVariables();		
		Object varItem = cbVar.getSelectedItem();
		
		if( varItem != null )
		{	 
			String var = (String)varItem;
			
			if( this.currentVar == null || !this.currentVar.getName().equals( var ) )
			{			
				List< MetadataVariableBlock > clisMeta = this.currentClisFile.getVarInfo();
				this.currentVar = null;				
				
				for( MetadataVariableBlock m : clisMeta )
				{
					if( m.getName().equals( var ) )
					{
						this.currentVar = m;
						
						break;
					}
				}
				
				if( this.currentVar != null )
				{	
					if( clisData == null )
					{
						try 
						{
							clisData = this.currentClisFile.importAllData();							
						}
						catch (Exception e) 
						{
							clearClisData();
						}
					}
					
					if( clisData != null )
					{
						int nch = this.currentVar.getCols();						
						spnm.setMaximum( nch );
						
						int step = (Integer)getSpinnerStep().getValue();
						sampleIndex_A = 0;
						sampleIndex_B = step;
						
						drawDataPlot( sampleIndex_A, sampleIndex_B );
					}
				}
				else
				{
					clearClisData();
				}
			}
		}
		else
		{
			clearClisData();
		}
		
		this.getPanelPlotCanva().setVisible( true );
	}	
	private void clearClisData()
	{	
		clisData = null;
		
		getCbVariables().setSelectedIndex( -1 );
		getCbVariables().removeAllItems();
		
		SpinnerNumberModel spnm = (SpinnerNumberModel)getSpinnerChannel().getModel();
		spnm.setMaximum( null );
		spnm.setValue( 1 );
	}
	
	private void drawDataPlot( int init, int end )
	{
		if( this.currentVar != null
				&& init >= 0
				&& ( end - init ) > 0 )
		{
			int channel = ((Integer)getSpinnerChannel().getValue()) - 1;
			Number[][] dat = this.clisData.get( this.currentVar.getName() );
			
			if( dat != null )
			{
				int rows = dat.length;
				int cols = dat[ 0 ].length;
				
				if( end > rows )
				{
					end = rows;
				}
				
				if( channel < cols )
				{
					double minVal = Double.POSITIVE_INFINITY;
					double maxVal = Double.NEGATIVE_INFINITY;
					double[][] interval = new double[2][ end - init ];
					for( int i = init; i < end; i++ )
					{
						interval[0][ i - init ] = i;
						
						double val = dat[ i ][ channel ].doubleValue();
						interval[1][ i - init ] = val;
						
						if( val < minVal )
						{
							minVal = val;
						}
						
						if( val > maxVal )
						{
							maxVal = val;
						}
					}
					
					DefaultXYDataset xyValues = new DefaultXYDataset();
					xyValues.addSeries( this.currentVar.getName(), interval );
					final JFreeChart chart = ChartFactory.createXYLineChart( null, null, null, xyValues  );
					chart.setAntiAlias( true );				
					chart.setBackgroundPaint( Color.WHITE );
					chart.getXYPlot().setBackgroundPaint( Color.WHITE );
					chart.getXYPlot().setRangeGridlinePaint( Color.BLACK );
					chart.getXYPlot().setDomainGridlinePaint( Color.BLACK );
					XYLineAndShapeRenderer render = new XYLineAndShapeRenderer();
					render.setDefaultShapesVisible( false );
					render.setSeriesStroke( 0, new BasicStroke( 3F ) );
					chart.getXYPlot().setRenderer( render );
					chart.getXYPlot().getDomainAxis().setTickLabelFont( new Font( Font.DIALOG, Font.BOLD, 18 ) );
					chart.getXYPlot().getRangeAxis().setTickLabelFont( new Font( Font.DIALOG, Font.BOLD, 18 ) );
					ValueAxis yaxis = chart.getXYPlot().getRangeAxis();
					
					if( minVal == maxVal )
					{
						minVal -= 0.5;
						maxVal += 0.5; 
					}					
					yaxis.setRange( minVal, maxVal );
					
					chart.getXYPlot().setRangeAxis( yaxis );				
					chart.clearSubtitles();
										
					JLabel canva = this.getCanva();
					canva.setVisible( false );
					Rectangle r = canva.getBounds();
					Border border = canva.getBorder();
					Insets pad = new Insets(0, 0, 0, 0);
					if( border != null )
					{
						pad = border.getBorderInsets(canva);
					}
					int w = r.width - pad.left - pad.right;
					int h = r.height - pad.top - pad.bottom;
					if ((w > 0) && (h > 0))
					{
						Image img = BasicPainter2D.createEmptyImage( w, h, null );
						
						chart.draw( (Graphics2D)img.getGraphics(), 
									new Rectangle2D.Double( 0, 0, img.getWidth( null ), img.getHeight( null ) ) );
						
						canva.setIcon(new ImageIcon(img));
					}
					canva.setVisible( true );
				}
			}
		}		
	}
	private JLabel getCanva() {
		if (canva == null) {
			canva = new JLabel( );
			canva.setBackground( Color.WHITE);
			canva.setOpaque( true );
			
			canva.addComponentListener( new ComponentAdapter() 
			{		
				AbstractStoppableThread  timer = null;
				Object sync = new Object();

				@Override
				public void componentResized(ComponentEvent e) 
				{
					if( timer != null )
					{
						timer.stopThread( IStoppableThread.FORCE_STOP );
					}
					
					timer = new AbstractStoppableThread() 
					{						
						@Override
						protected void runInLoop() throws Exception 
						{
							synchronized( this )
							{
								this.wait( 100L );
							}
							
							drawDataPlot( sampleIndex_A, sampleIndex_B);
						}
						
						protected void runExceptionManager(Throwable e) 
						{
						};
						
						@Override
						protected void preStopThread(int friendliness) throws Exception 
						{	
						}
						
						@Override
						protected void postStopThread(int friendliness) throws Exception 
						{	
						}
					};
					
					try 
					{
						timer.startThread();
						timer.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
					}
					catch (Exception e1) 
					{
					}
				}
			});
		}
		return canva;
	}
}
