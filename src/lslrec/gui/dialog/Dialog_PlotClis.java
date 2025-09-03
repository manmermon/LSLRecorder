/**
 * 
 */

package lslrec.gui.dialog;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
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
import javax.swing.JOptionPane;

import java.awt.Container;
import java.awt.Cursor;

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
	private static final long serialVersionUID = 2257212620236071644L;
	
	private JPanel centerPanel = null;
	//private JPanel southPanel;	
	private JPanel centerOutputFormatPanel;
	private JPanel loadFilePanel;
	private JPanel infoFilePanel;
	private JPanel loadFileBtnPanel;	
	private JPanel plotPanel;
	private JPanel panelMovePlotCtr;
	private JPanel panelPlotYAxis;
	private JPanel panelPlotCanva;
	private JPanel panelPlotCtr;
	private JPanel panelChannelsContainer;
	private JPanel panelChannelsChb;
	private JPanel panelxAxis;
	
	private JScrollPane scrChannelPanel;
	
	private JButton btnLoadFile;
	//private JButton btnClose;
	private JButton btnPrevious;
	private JButton btnNext;
	private JButton btnEnd;
	private JButton btnBegin;
	private JButton btnSaveImg;
	
	private JTextField clisFileText;
	
	private JLabel lblLoadFile;	
	private JLabel lblVariable;
	private JLabel lbXAxisMult;
	private JLabel lbXAxisOffset;
	//private JLabel lblChannel;
	private JLabel lblStep;
	private JLabel canva;
	
	private JLabel lblXAxis;
	
	private JComboBox<String> cbVariables;
	private JComboBox<String> cbXAxisVariables;
	private JComboBox< Integer > cbXAxisVarChannels;
	private JCheckBox chXAxisRelative;
	private JToggleButton jtbtDotPaint;
	private JToggleButton jtbtLinePaint;
	
	//private JSpinner spinnerChannel;
	private JSpinner spinnerStep;
	private JSpinner xAxisMultValue;
	private JSpinner xAxisOffsetValue;
	
	private String currentFolderPath;
	
	private ClisData currentClisFile = null;
	private Map< String, Number[][] > clisData = null;
	private int sampleIndex_A = 0;
	private MetadataVariableBlock currentVar = null; 
	private MetadataVariableBlock currentXAxisVar = null;
	
	private Object sync = new Object();
	private boolean[] selectedChannels = null;
	
	/**
	 * Launch the application.
	 */
	private static Dialog_PlotClis dgclis = null;
	//private JPanel panelPlotCtr;
	
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
		
		super.addWindowListener( new WindowAdapter() {
			
			@Override
			public void windowClosing(WindowEvent e) 
			{
				if( currentClisFile != null )
				{
					try 
					{
						currentClisFile.close();
					}
					catch (IOException e1) 
					{
						e1.printStackTrace();
					}
				}
				
				super.windowClosing(e);
			}
			
		});
		
		Container container = super.getContentPane();
		container.add( this.getCenterPanel(), BorderLayout.CENTER);
		//super.getContentPane().add( this.getSouthPanel(), BorderLayout.SOUTH);
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
		
	/*
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
	*/
	/*
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
	*/
	
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
						setClisFile( FILE[ 0 ] );
					}
				}
			} );
			
		}
		return btnLoadFile;
	}
	
	private void setClisFile( String FILE )
	{
		if( FILE != null )
		{	
			super.setCursor( Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR ) );
			
			this.getTxtClisFile().setText( "" );
			
			try 
			{
				this.currentClisFile = new ClisData( FILE );
				this.currentFolderPath = (new File( FILE )).getAbsolutePath();
				
				this.getTxtClisFile().setText( FILE );
			} 
			catch ( Exception e1) 
			{
				this.currentClisFile = null;
			}
			
			this.showBinaryFileInfo( );
			this.setClisDataPlotMetadata( );
			
			super.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
		}
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
			centerOutputFormatPanel.setBorder(new EmptyBorder(5, 0, 0, 5));
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
			
			JPanel xAxisPanel = new JPanel();
			xAxisPanel.setLayout( new BoxLayout( xAxisPanel, BoxLayout.Y_AXIS ) );			
			xAxisPanel.add( this.getPanelXAxis() );
			xAxisPanel.add( this.getPanelMovePlotCtr() );
			
			this.plotPanel.add( new JScrollPane( xAxisPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED  ) , BorderLayout.SOUTH);
			this.plotPanel.add( this.getPanelPlotYAxis(), BorderLayout.WEST);
			this.plotPanel.add( this.getPanelPlotCanva(), BorderLayout.CENTER);
			this.plotPanel.add( this.getPanelPlotCtr(), BorderLayout.NORTH);
			//this.plotPanel.add( this.getPanelPlotCtr(), BorderLayout.NORTH);
		}
		
		return this.plotPanel;
	}
	
	private JPanel getPanelXAxis( )
	{
		if( this.panelxAxis == null )
		{
			this.panelxAxis = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			
			this.panelxAxis.add( this.getlbXAxisVariable() );
			this.panelxAxis.add( this.getCbXAxisVariables() );
			this.panelxAxis.add( this.getCbXAxisVarChannels() );
			this.panelxAxis.add( this.getChbXAxisRelative() );
			this.panelxAxis.add( this.getJTBLinePaint() );
			this.panelxAxis.add( this.getJTBDotPaint() );
		}
		
		return this.panelxAxis;
	}
		
	private JLabel getlbXAxisVariable()
	{
		if( this.lblXAxis == null )
		{
			this.lblXAxis = new JLabel();
			String tx = Language.getLocalCaption( Language.XAXIS_TEXT ) + " - " + Language.getLocalCaption( Language.VARIABLE_TEXT ) + ":";
			this.lblXAxis.setText( tx );			
		}
		
		return this.lblXAxis;
	}
	
	private JComboBox<String> getCbXAxisVariables() 
	{
		if ( this.cbXAxisVariables == null) 
		{
			cbXAxisVariables = new JComboBox<String>();
			
			Font f = cbXAxisVariables.getFont();
			FontMetrics fm = cbXAxisVariables.getFontMetrics( f );
			int w = fm.stringWidth( StringUtils.repeat( "W", 10 ) );
			
			Dimension d = new Dimension( w, fm.getHeight() + 5 );
			cbXAxisVariables.setPreferredSize( d );
			
			cbXAxisVariables.addItemListener( new ItemListener() 
			{				
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					if( e.getStateChange() == ItemEvent.SELECTED )
					{
						JComboBox<String> cbXaxis = (JComboBox<String>)e.getSource();
						
						if( cbXaxis.getSelectedIndex() < 1 )
						{
							getCbXAxisVarChannels().setSelectedIndex( -1 );
							getCbXAxisVarChannels().removeAllItems();
							
							currentXAxisVar = null;
							
							drawData();
						}
						else
						{
							setXAxisData2Plot();
						}
					}
				}
			});
		}
		
		return cbXAxisVariables;
	}
	
	private JComboBox<Integer> getCbXAxisVarChannels() 
	{
		if ( this.cbXAxisVarChannels == null) 
		{
			cbXAxisVarChannels = new JComboBox< Integer >();
			
			Font f = cbXAxisVarChannels.getFont();
			FontMetrics fm = cbXAxisVarChannels.getFontMetrics( f );
			int w = fm.stringWidth( StringUtils.repeat( "W", 5 ) );
			
			Dimension d = new Dimension( w, fm.getHeight() + 5 );
			cbXAxisVarChannels.setPreferredSize( d );
			
			cbXAxisVarChannels.addItemListener( new ItemListener() 
			{				
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					if( e.getStateChange() == ItemEvent.SELECTED )
					{
						drawData();
					}
				}
			});
		}
		
		return cbXAxisVarChannels;
	}
	
	private JCheckBox getChbXAxisRelative()
	{
		if( this.chXAxisRelative == null )
		{
			this.chXAxisRelative = new JCheckBox( Language.getLocalCaption( Language.RELATIVE_TEXT ) );
			
			this.chXAxisRelative.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					drawData();
				}
			});
		}
	
		return this.chXAxisRelative;
	}
	
	private JToggleButton getJTBDotPaint()
	{
		if( this.jtbtDotPaint == null )
		{
			this.jtbtDotPaint = new JToggleButton();
			this.jtbtDotPaint.setIcon( new ImageIcon( BasicPainter2D.paintFillCircle(0, 0, 8, Color.BLACK, null )));
			this.jtbtDotPaint.setPreferredSize( this.getJTBLinePaint().getPreferredSize() );
			
			this.jtbtDotPaint.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JToggleButton jtb = (JToggleButton)e.getSource();
					if( !jtb.isSelected() && !getJTBLinePaint().isSelected() )
					{
						getJTBLinePaint().doClick();
					}
					else
					{
						drawData();
					}
				}
			});
		}
		
		return this.jtbtDotPaint;
	}
	
	private JToggleButton getJTBLinePaint()
	{
		if( this.jtbtLinePaint == null )
		{
			this.jtbtLinePaint = new JToggleButton();
			this.jtbtLinePaint.setPreferredSize( new Dimension( 32, 16 ) );
			this.jtbtLinePaint.setSelected( true );
			this.jtbtLinePaint.setIcon( new ImageIcon( BasicPainter2D.paintRectangle(16, 2, 1F, Color.BLACK, Color.BLACK ) ) );
			
			this.jtbtLinePaint.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JToggleButton jtb = (JToggleButton)e.getSource();
					if( !jtb.isSelected() && !getJTBDotPaint().isSelected() )
					{
						getJTBDotPaint().doClick();
					}
					else
					{
						drawData();
					}
				}
			});
		}
		
		return this.jtbtLinePaint;
	}
	
	private JPanel getPanelMovePlotCtr() 
	{
		if (this.panelMovePlotCtr == null) 
		{
			this.panelMovePlotCtr = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 5));
			
			this.panelMovePlotCtr.add( this.getLblStep() );
			this.panelMovePlotCtr.add( this.getSpinnerStep() );
			this.panelMovePlotCtr.add( this.getBtnBegin() );
			this.panelMovePlotCtr.add( this.getBtnPrevious() );
			this.panelMovePlotCtr.add( this.getBtnNext() );
			this.panelMovePlotCtr.add( this.getBtnEnd() );
			
			JSeparator sep = new JSeparator( JSeparator.VERTICAL );
			sep.setPreferredSize( new Dimension( 5, this.getBtnEnd().getPreferredSize().height ));
			this.panelMovePlotCtr.add( sep );
			
			JLabel lbXAxisTransform = new JLabel( );
			//lbXAxisTransform.setText( "X-Axis transform: ");
			lbXAxisTransform.setText( "x-axis = x*" +  this.getLbXAxisMult().getText() + " + " +  this.getLbXAxisoffset().getText() + ": ");
			
			this.panelMovePlotCtr.add( lbXAxisTransform );
			
			this.panelMovePlotCtr.add( this.getLbXAxisMult() );
			this.panelMovePlotCtr.add( this.getXAxisMultValue() );
			this.panelMovePlotCtr.add( this.getLbXAxisoffset() );
			this.panelMovePlotCtr.add( this.getXAxisOffsetValue() );			
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
			this.panelPlotCanva.setLayout(new BorderLayout(0, 0));
			this.panelPlotCanva.add( this.getCanva(), BorderLayout.CENTER );
			
			this.panelPlotCanva.setDropTarget( new DropTarget()
			{
				@Override
				public synchronized void drop(DropTargetDropEvent dtde) 
				{
					try 
					{
						dtde.acceptDrop(DnDConstants.ACTION_COPY);
						List<File> droppedFiles = (List<File>) dtde.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
						
						if( !droppedFiles.isEmpty() )
						{
							setClisFile( droppedFiles.get( 0 ).getCanonicalPath() );
						}
						
						dtde.dropComplete(true);
					} 
					catch (Exception ex) 
					{
						ex.printStackTrace();
					}
				}
			});
		}
		
		return this.panelPlotCanva;
	}
	
	private JPanel getPanelPlotCtr() 
	{
		if (panelPlotCtr == null) 
		{
			panelPlotCtr = new JPanel( new BorderLayout() );
			
			JPanel varPane = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			varPane.add( this.getLblVariable());
			varPane.add( this.getCbVariables());
			
			JPanel varSavePane = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			varSavePane.add( this.getBtnSaveImg() );
			
			JPanel panel = new JPanel( new BorderLayout() );
			panel.add( varPane, BorderLayout.NORTH );
			panel.add( varSavePane, BorderLayout.SOUTH );
			//panelPlotCtr.add(getLblChannel());
			//panelPlotCtr.add(getSpinnerChannel());
			
			this.panelPlotCtr.add( panel, BorderLayout.WEST );
			this.panelPlotCtr.add( this.getChannelsContainerPanel(), BorderLayout.CENTER );
		}
		
		return panelPlotCtr;
	}
	
	private JButton getBtnSaveImg()
	{
		if( this.btnSaveImg == null )
		{
			this.btnSaveImg = new JButton();
			ImageIcon ic = GeneralAppIcon.SaveFile2( 20, Color.BLACK, Color.GREEN.darker() );
			if( ic == null )
			{
				this.btnSaveImg.setText( Language.getLocalCaption( Language.MENU_SAVE ) );
			}
			else
			{
				this.btnSaveImg.setIcon( ic );
			}
			
			this.btnSaveImg.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed( ActionEvent e ) 
				{
					Image img = getDataImage( sampleIndex_A, sampleIndex_A + (int)getSpinnerStep().getValue(), 1024, 768 );
						
					if( img != null )
					{
						String ext = "png";

						String Filename = currentVar.getName();
						String suffix = "";
						for( int i = 0; i < selectedChannels.length; i++ )
						{
							suffix += ( selectedChannels[ i ] ) ? ("_" + (i+1)) : "";
						}
						
						Filename += suffix + "." + ext;
						
						String[] file = FileUtils.selectUserFile( Filename, false, false, JFileChooser.FILES_ONLY
																	, ext, new String[] { ext }, currentFolderPath );
						
						if( file != null && file.length > 0 )
						{
							boolean ok = !(new File( file[ 0 ] ) ).exists();
							if( !ok )
							{
								Component c = (Component)e.getSource();
								int selOpt = JOptionPane.showConfirmDialog( c, Language.getLocalCaption( Language.DIALOG_REPLACE_FILE_MESSAGE ) );
								
								ok = ( selOpt == JOptionPane.YES_OPTION );
							}

							if( ok )
							{
								try {
									ImageIO.write( (BufferedImage)img, ext, new File( file[ 0 ] ) );
								} catch (IOException e1) {
									// TODO Auto-generated catch block
									e1.printStackTrace();
								}
							}
						}
					}
				}
			});
		}
		
		return this.btnSaveImg;
	}
	
	private JLabel getLblVariable() 
	{
		if (lblVariable == null) 
		{
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
	
	private JLabel getLbXAxisMult()
	{
		if( this.lbXAxisMult == null )
		{
			this.lbXAxisMult = new JLabel( "m" );
		}
		return this.lbXAxisMult;
	}
	
	private JSpinner getXAxisMultValue()
	{
		if (this.xAxisMultValue == null) 
		{
			this.xAxisMultValue = new JSpinner();
			
			this.xAxisMultValue.setModel( new SpinnerNumberModel( 1D, null, null, 0.1D ) );
			
			Font f = this.xAxisMultValue.getFont();
			FontMetrics fm = this.xAxisMultValue.getFontMetrics( f );
			int w = fm.stringWidth( StringUtils.repeat( "9", 12 ) );
			
			Dimension d = new Dimension( w, fm.getHeight() + 5 );
			this.xAxisMultValue.setPreferredSize( d );
			
			this.xAxisMultValue.addMouseWheelListener( new MouseWheelListener() 
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
			
			this.xAxisMultValue.addChangeListener( new ChangeListener() 
			{
				@Override
				public void stateChanged(ChangeEvent e) 
				{
					drawData();
				}
			});
		}
		
		return this.xAxisMultValue;
	}
	
	private JLabel getLbXAxisoffset()
	{
		if( this.lbXAxisOffset == null )
		{
			this.lbXAxisOffset = new JLabel( "c" );
		}
		return this.lbXAxisOffset;
	}
	
	private JSpinner getXAxisOffsetValue()
	{
		if (this.xAxisOffsetValue == null) 
		{
			this.xAxisOffsetValue = new JSpinner();
			
			this.xAxisOffsetValue.setModel( new SpinnerNumberModel( 0D, null, null, 1D ) );
			
			Font f = this.xAxisOffsetValue.getFont();
			FontMetrics fm = this.xAxisOffsetValue.getFontMetrics( f );
			int w = fm.stringWidth( StringUtils.repeat( "9", 12 ) );
			
			Dimension d = new Dimension( w, fm.getHeight() + 5 );
			this.xAxisOffsetValue.setPreferredSize( d );
			
			this.xAxisOffsetValue.addMouseWheelListener( new MouseWheelListener() 
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
			
			this.xAxisOffsetValue.addChangeListener( new ChangeListener() 
			{
				@Override
				public void stateChanged(ChangeEvent e) 
				{
					drawData();
				}
			});
		}
		
		return this.xAxisOffsetValue;
	}
	
	private JPanel getChannelsContainerPanel()
	{
		if( this.panelChannelsContainer == null )
		{
			this.panelChannelsContainer = new JPanel( new BorderLayout() );
			
			this.panelChannelsContainer.setBorder( BorderFactory.createTitledBorder( Language.getLocalCaption( Language.LSL_CHANNELS ) ) );
			
			this.panelChannelsContainer.add( this.getChannelScrollPanel(), BorderLayout.CENTER );
		}
		
		return this.panelChannelsContainer;
	}
	
	private JScrollPane getChannelScrollPanel()
	{
		if( this.scrChannelPanel == null )
		{
			JPanel panel = this.getChannelsPanel();
			
			JPanel panel2 = new JPanel( new BorderLayout() );
			panel2.add( panel, BorderLayout.WEST );
			
			this.scrChannelPanel = new JScrollPane( panel2, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS );
			this.scrChannelPanel.setBorder( null );		
		}
			
		return this.scrChannelPanel;
	}
	
	private JPanel getChannelsPanel()
	{
		if( this.panelChannelsChb == null )
		{
			this.panelChannelsChb = new JPanel( new GridLayout( 2, 0 ) );	
			
			this.panelChannelsChb.setBorder( null );	
			this.setAllNoneChannelsOpt();
		}
		
		return this.panelChannelsChb;
	}
	
	private void setAllNoneChannelsOpt()
	{
		JPanel panel = this.getChannelsPanel();
		
		JRadioButton allBt = new JRadioButton( Language.getLocalCaption( Language.ALL_TEXT ) );
		allBt.setSelected( true );
		allBt.addActionListener( new ActionListener() 
		{			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				JRadioButton b = (JRadioButton)e.getSource();				
				b.setSelected( true );
				
				synchronized ( sync )
				{
					if( selectedChannels != null )
					{
						for( int ic = 0; ic < selectedChannels.length; ic++ )
						{
							selectedChannels[ ic ] = true;
						}
					}
				}
				
				setChannelCheckboxes();
				drawData();
			}
		});
		
		JRadioButton noneBt = new JRadioButton( Language.getLocalCaption( Language.NONE_TEXT) );
		noneBt.setSelected( true );
		noneBt.addActionListener( new ActionListener() 
		{			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				JRadioButton b = (JRadioButton)e.getSource();				
				b.setSelected( true );
				
				synchronized ( sync )
				{
					if( selectedChannels != null )
					{
						for( int ic = 0; ic < selectedChannels.length; ic++ )
						{
							selectedChannels[ ic ] = false;
						}
					}
				}
				
				setChannelCheckboxes();
				drawData();
			}
		});
		
		panel.add( allBt );
		panel.add( noneBt );
		
	}
	
	private void setChannelCheckboxes( )
	{
		JPanel channelPanel = this.getChannelsPanel();
		channelPanel.setVisible( false );
		channelPanel.removeAll();		

		this.setAllNoneChannelsOpt();
		
		synchronized( this.sync )
		{
			if( this.selectedChannels != null )
			{				
				int len = this.selectedChannels.length;
				for( int i = 0; i < len; i++ )
				{
					JCheckBox ch = new JCheckBox( "" + (i+1) );
					ch.setSelected( this.selectedChannels[ i ] );
					
					ch.addActionListener( new ActionListener() 
					{	
						@Override
						public void actionPerformed(ActionEvent e) 
						{
							JCheckBox ch = (JCheckBox)e.getSource();
							int ich = Integer.parseInt( ch.getText() ) - 1;
							
							if( ich < selectedChannels.length )
							{
								selectedChannels[ ich ] = ch.isSelected();
							}
							
							drawData();
						}
					});
					
					channelPanel.add( ch );					
				}
			}
		}
		
		channelPanel.setVisible( true );
	}
	
	private void drawData()
	{
		int step = (Integer)getSpinnerStep().getValue();
		drawDataPlot( sampleIndex_A, sampleIndex_A + step );
	}
	
	/*
	private JLabel getLblChannel() {
		if (lblChannel == null) {
			lblChannel = new JLabel( Language.getLocalCaption( Language.LSL_CHANNEL ) );
		}
		return lblChannel;
	}
	//*/
	
	
	/*
	private JSpinner getSpinnerChannel() {
		if (spinnerChannel == null) {
			spinnerChannel = new JSpinner();
			spinnerChannel.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
			
			Font f = spinnerChannel.getFont();
			FontMetrics fm = spinnerChannel.getFontMetrics( f );
			int w = fm.stringWidth( StringUtils.repeat( "9", 5 ) )*2;
			
			Dimension d = new Dimension( w, fm.getHeight() + 5 );
			spinnerChannel.setPreferredSize( d );
			spinnerChannel.setSize( d );
			
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
					sampleIndex_A = 0;
					drawData();
				}
			});
		}
		return spinnerChannel;
	}
	//*/
	
	private JLabel getLblStep() {
		if (lblStep == null) {
			lblStep = new JLabel( Language.getLocalCaption( Language.STEP_TEXT ) );
		}
		return lblStep;
	}
	
	private JSpinner getSpinnerStep() 
	{
		if (this.spinnerStep == null) 
		{
			this.spinnerStep = new JSpinner();
			
			this.spinnerStep.setModel(new SpinnerNumberModel(new Integer( 1000 ), new Integer(1), null, new Integer(100)));
			
			Font f = this.spinnerStep.getFont();
			FontMetrics fm = this.spinnerStep.getFontMetrics( f );
			int w = fm.stringWidth( StringUtils.repeat( "9", 12 ) );
			
			Dimension d = new Dimension( w, fm.getHeight() + 5 );
			this.spinnerStep.setPreferredSize( d );
			
			this.spinnerStep.addMouseWheelListener( new MouseWheelListener() 
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
			
			((JSpinner.DefaultEditor)this.spinnerStep.getEditor()).getTextField().addKeyListener( new KeyAdapter() 
			{
				@Override
				public void keyReleased(KeyEvent e) 
				{
					if( e.getKeyCode() == KeyEvent.VK_ENTER )
					{
						drawData(); 
					}
				}
			});
			
			((JSpinner.DefaultEditor)this.spinnerStep.getEditor()).getTextField().addFocusListener( new FocusAdapter() 
			{
				@Override
				public void focusLost(FocusEvent e) 
				{
					drawData();
				}
			}); 
		}
		return this.spinnerStep;
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
					drawData();
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
					drawData();
				}
			});
		}
		return btnNext;
	}
	
	private JButton getBtnBegin() 
	{
		if (btnBegin == null) {
			btnBegin = new JButton();
			
			btnBegin.setIcon(  GeneralAppIcon.toBegin( 10, 10, true, Color.BLACK, Color.GRAY, null ) );			
			
			btnBegin.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					updateSampleIndexes( true );
					sampleIndex_A = 0;
					drawData();
				}
			});
		}
		return btnBegin;
	}
	
	private JButton getBtnEnd() 
	{
		if (btnEnd == null) {
			btnEnd = new JButton();
			
			btnEnd.setIcon(  GeneralAppIcon.toEnd( 10, 10, true, Color.BLACK, Color.GRAY, null ) );			
			
			btnEnd.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					updateSampleIndexes( true );
					int step = (Integer)getSpinnerStep().getValue();
					sampleIndex_A = 0;
					
					if( currentVar != null && clisData != null )
					{
						Number[][] dat = clisData.get( currentVar.getName() ) ;
						
						if( dat != null )
						{
							sampleIndex_A = dat.length - step;
							
							if( sampleIndex_A < 0 )
							{
								sampleIndex_A = 0;
							}
						}
					}
					
					drawData();
				}
			});
		}
		return btnEnd;
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
			
			Number[][] dat = this.clisData.get( this.currentVar.getName() );
			
			int rows = dat.length;
			
			if( A >= rows )
			{
				A = rows - Math.abs( step );
			}
			
			if( A < 0 )
			{
				A = 0;
			}
						
			this.sampleIndex_A = A;
		}
	}
	
	private void setClisDataPlotMetadata( )
	{
		clearClisData();		
		
		if( this.currentClisFile != null )
		{
			List< MetadataVariableBlock > clisMeta = this.currentClisFile.getVarInfo();
			
			getCbXAxisVariables().addItem( Language.getLocalCaption( Language.NONE_TEXT ) );
			
			for( MetadataVariableBlock var : clisMeta )
			{
				getCbVariables().addItem( var.getName() );
				getCbXAxisVariables().addItem( var.getName() );
			}
			
			if( getCbVariables().getItemCount() > 0 )
			{
				getCbVariables().setSelectedIndex( 0 );
				getCbXAxisVariables().setSelectedIndex( 0 );
			}
		}
	}
	
	private void setData2Plot(  )
	{
		this.getPanelPlotCanva().setVisible( false );
		
		/*
		SpinnerNumberModel spnm = (SpinnerNumberModel)getSpinnerChannel().getModel();
		spnm.setMaximum( null );
		spnm.setValue( 1 );
		//*/
		
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
						//spnm.setMaximum( nch );
						
						this.selectedChannels = new boolean[ nch ];
						
						if( this.selectedChannels.length > 0 )
						{
							this.selectedChannels[ 0 ] = true;
						}
						
						this.setChannelCheckboxes();
						
						sampleIndex_A = 0;						
						drawData();
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
	
	private void setXAxisData2Plot(  )
	{
		this.getPanelPlotCanva().setVisible( false );
		
		/*
		SpinnerNumberModel spnm = (SpinnerNumberModel)getSpinnerChannel().getModel();
		spnm.setMaximum( null );
		spnm.setValue( 1 );
		//*/
		
		JComboBox< String > cbXAxisVar = getCbXAxisVariables();		
		Object varItem = cbXAxisVar.getSelectedItem();
		
		if( varItem != null )
		{	 
			String var = (String)varItem;
			
			if( this.currentXAxisVar == null || !this.currentXAxisVar.getName().equals( var ) )
			{			
				List< MetadataVariableBlock > clisMeta = this.currentClisFile.getVarInfo();
				this.currentXAxisVar = null;				
				
				for( MetadataVariableBlock m : clisMeta )
				{
					if( m.getName().equals( var ) )
					{
						this.currentXAxisVar = m;
						
						break;
					}
				}
				
				if( this.currentXAxisVar != null )
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
						getCbXAxisVarChannels().setVisible( false );
						
						getCbXAxisVarChannels().removeAllItems();
						
						int nch = this.currentXAxisVar.getCols();
						for( int i = 0; i < nch; i++ )
						{
							getCbXAxisVarChannels().addItem( i + 1 );
						}
						
						getCbXAxisVarChannels().setVisible( true );
					}
				}
			}
		}
		
		drawData();
		
		this.getPanelPlotCanva().setVisible( true );
	}	
	
	
	private void clearClisData()
	{	
		this.clisData = null;
		this.currentVar = null;
		this.sampleIndex_A = 0;
		
		getCbVariables().setSelectedIndex( -1 );
		getCbVariables().removeAllItems();
		
		getCbXAxisVariables().setSelectedIndex( -1 );
		getCbXAxisVariables().removeAllItems();
		
		getCbXAxisVarChannels().setSelectedIndex( -1 );
		getCbXAxisVarChannels().removeAllItems();
		
		/*
		SpinnerNumberModel spnm = (SpinnerNumberModel)getSpinnerChannel().getModel();
		spnm.setMaximum( null );
		spnm.setValue( 1 );
		//*/
		this.getChannelsPanel().removeAll();
		synchronized( sync )
		{
			this.selectedChannels = null;
		}
		
		this.getXAxisMultValue().setValue( 1D );
		this.getXAxisOffsetValue().setValue( 0D );
		
		this.getCanva().setIcon( null );
	}
	
	private Image getDataImage( int init, int end, int width, int height )
	{
		Image img = null;
		
		if( this.currentVar != null
				&& init >= 0
				&& ( end - init ) > 0 )
		{
			//int channel = ((Integer)getSpinnerChannel().getValue()) - 1;
			Number[][] dat = this.clisData.get( this.currentVar.getName() );
			
			if( dat != null )
			{
				int rows = dat.length;
				int cols = dat[ 0 ].length;
				
				if( end > rows )
				{
					end = rows;
				}
				
				if( end - init < 1 )
				{
					init = end - 1;
				}
				
				//if( channel < cols )
				
				DefaultXYDataset xyValues = new DefaultXYDataset();
				double minVal = Double.POSITIVE_INFINITY;
				double maxVal = Double.NEGATIVE_INFINITY;
				
				double mult = (Double)this.getXAxisMultValue().getValue();
				double offset = (Double)this.getXAxisOffsetValue().getValue();
				
				Number[][] xAxisDat = ( this.currentXAxisVar == null ) ? null : this.clisData.get( this.currentXAxisVar.getName() );
				
				int indxSelCh = this.getCbXAxisVarChannels().getSelectedIndex();
				double relativeXAxisValue = 0D;
				
				if( xAxisDat != null && this.getChbXAxisRelative().isSelected() )
				{	
					relativeXAxisValue = (indxSelCh >= 0 ) ? xAxisDat[0][ indxSelCh ].doubleValue() : relativeXAxisValue;
				}
				
				for( int ich = 0; ich < cols && ich < this.selectedChannels.length; ich++ )
				{
					if( this.selectedChannels[ ich ] )
					{						
						double[][] interval = new double[2][ end - init ];
						for( int i = init; i < end; i++ )
						{
							double xval = ( xAxisDat != null && indxSelCh >= 0 ) ? xAxisDat[ i ][ indxSelCh ].doubleValue() - relativeXAxisValue : i ;
							interval[0][ i - init ] = xval * mult + offset;
							
							double val = dat[ i ][ ich ].doubleValue();
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
						
						String serieName = Language.getLocalCaption( Language.LSL_CHANNEL ) + "[" + (ich+1)+"]";
						xyValues.addSeries( serieName, interval );						
					}
				}
				
				if( xyValues.getSeriesCount() > 0 )
				{
					final JFreeChart chart = ChartFactory.createXYLineChart( null, null, null, xyValues  );					
					chart.setAntiAlias( true );				
					chart.setBackgroundPaint( Color.WHITE );
					chart.getXYPlot().setBackgroundPaint( Color.WHITE );
					chart.getXYPlot().setRangeGridlinePaint( Color.BLACK );
					chart.getXYPlot().setDomainGridlinePaint( Color.BLACK );
					XYLineAndShapeRenderer render = new XYLineAndShapeRenderer();
					render.setDefaultShapesVisible( this.getJTBDotPaint().isSelected() );
					for( int iserie = 0; iserie < xyValues.getSeriesCount(); iserie++ )
					{
						render.setSeriesStroke( iserie, new BasicStroke( 3F ) );
						render.setSeriesLinesVisible(iserie, this.getJTBLinePaint().isSelected() );
					}
					
					chart.getXYPlot().setRenderer( render );
					chart.getXYPlot().getDomainAxis().setTickLabelFont( new Font( Font.DIALOG, Font.BOLD, 15 ) );
					chart.getXYPlot().getRangeAxis().setTickLabelFont( new Font( Font.DIALOG, Font.BOLD, 15 ) );
					ValueAxis yaxis = chart.getXYPlot().getRangeAxis();
					
					if( minVal == maxVal )
					{
						minVal -= 0.5;
						maxVal += 0.5; 
					}					
					
					minVal = ( minVal == Double.POSITIVE_INFINITY ) ? Double.NEGATIVE_INFINITY : minVal;
					maxVal = ( maxVal == Double.NEGATIVE_INFINITY ) ? Double.POSITIVE_INFINITY : maxVal;
					
					yaxis.setRange( minVal, maxVal );
					
					chart.getXYPlot().setRangeAxis( yaxis );				
					//chart.clearSubtitles();
										
					if ((width > 0) && (height > 0))
					{
						img = BasicPainter2D.createEmptyImage( width, height, null );
						
						chart.draw( (Graphics2D)img.getGraphics(), 
									new Rectangle2D.Double( 0, 0, img.getWidth( null ), img.getHeight( null ) ) );
					}
				}
			}
		}	
		
		return img;
	}	
	
	private void drawDataPlot( int init, int end )
	{	
		JLabel canva = this.getCanva();
		canva.setVisible( false );
		canva.setIcon( null );				
										
		Rectangle r = canva.getBounds();
		Border border = canva.getBorder();
		Insets pad = new Insets(0, 0, 0, 0);
		if( border != null )
		{
			pad = border.getBorderInsets(canva);
		}
		int w = r.width - pad.left - pad.right;
		int h = r.height - pad.top - pad.bottom;
		
		Image img = getDataImage(init, end, w, h );
		
		if( img != null )
		{
			canva.setIcon(new ImageIcon(img));
		}
		
		canva.setVisible( true );	
	}
		
	private JLabel getCanva()
	{
		if ( this.canva == null ) 
		{
			this.canva = new JLabel( );
			this.canva.setBackground( Color.WHITE);
			this.canva.setOpaque( true );
			
			/*
			Image imgCursor =  GeneralAppIcon.MagnifiyingGlass( 512, Color.BLACK ).getImage();
			Dimension bestDimensionCursor = Toolkit.getDefaultToolkit().getBestCursorSize( imgCursor.getWidth(null), imgCursor.getHeight(null) );
			int xCenterCursor = bestDimensionCursor.width / 2;
			int yCenterCursor = (int)(0.33 *  bestDimensionCursor.height );
			Cursor cursor = Toolkit.getDefaultToolkit().createCustomCursor( imgCursor, new Point(xCenterCursor,yCenterCursor), "magnifying glass" );
			//*/
			
			Cursor cursor = new Cursor( Cursor.CROSSHAIR_CURSOR ); 
			this.canva.setCursor( cursor );
			
			this.canva.addComponentListener( new ComponentAdapter() 
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
							
							drawData();
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
			
			final AtomicInteger pX1 = new AtomicInteger( -1 );
			BufferedImage plotImg[] =  new BufferedImage[ 1 ];
			this.canva.addMouseListener( new MouseAdapter() 
			{
				List< Integer > prevSteps = new ArrayList<Integer>();
				@Override
				public void mouseReleased(MouseEvent e) 
				{		
					if( clisData != null )
					{	
						if( e.getButton() == MouseEvent.BUTTON1 )
						{
							int prevStep = (int)getSpinnerStep().getValue();
							prevSteps.add( prevStep );
							
							JLabel cv = (JLabel)e.getSource();
							int leftMargin = 46;
							int rightMargin = 10;
	
							int pX2 = e.getX();
	
							int w = pX2 - pX1.get();
							if( w < 0 )
							{
								w = -w;
								pX1.set( pX2 );
								pX2 = pX1.get() + w; 
								
							}
							
							boolean inOKLeft  = pX2 > leftMargin;
							boolean inOKRight = pX1.get() < cv.getWidth() - rightMargin;
							
							w = ( pX1.get() < leftMargin) ? w - (leftMargin - pX1.get() ) : w;
							w = ( pX2 > cv.getWidth() - rightMargin ) ? w - ( pX2 - ( cv.getWidth() - 10) ) : w;
							
							if( w > 10  && inOKLeft && inOKRight)
							{
								int cvW = cv.getWidth() - leftMargin - rightMargin;
		
								double prop = w *1.0D / cvW;
		
								double sample = (pX1.get() - leftMargin * 1D) / cvW ;
		
								Number[][] dat = clisData.get( currentVar.getName() );
								
								int dataLen = dat.length;
								
								prevStep = (prevStep <= dataLen ) ? prevStep : dataLen;
								
								sampleIndex_A = (int)( sampleIndex_A +  prevStep * sample );
		
								sampleIndex_A = ( sampleIndex_A < 0 ) ? 0 : sampleIndex_A;
		
								int step = (int)( prevStep * prop );
								step = ( step < 1 ) ? 1 : step;
		
								drawDataPlot( sampleIndex_A, sampleIndex_A + step );
								
								getSpinnerStep().setValue( step );
							}
							else
							{
								cv.setIcon( new ImageIcon( plotImg[ 0 ] ) );
							}
							
							pX1.set( -1 );
							plotImg[ 0 ] = null;
						}	
						else if( e.getButton() == MouseEvent.BUTTON3 )
						{
							if( !prevSteps.isEmpty() )
							{
								int prevStep = prevSteps.get( prevSteps.size() - 1 );
								prevSteps.remove( prevSteps.size() - 1 );
								
								sampleIndex_A -= prevStep/2;
								sampleIndex_A = ( sampleIndex_A < 0 ) ? 0 : sampleIndex_A;
		
								drawDataPlot( sampleIndex_A, sampleIndex_A + prevStep );
								getSpinnerStep().setValue( prevStep );
							}
						}
					}
				}
				
				@Override
				public void mousePressed(MouseEvent e) 
				{
					if( clisData != null )
					{
						if( e.getButton() == MouseEvent.BUTTON1 )
						{
							JLabel cv = (JLabel)e.getSource();
							plotImg[ 0 ] = (BufferedImage)((ImageIcon)cv.getIcon()).getImage();
														
							pX1.set( e.getX() );
						}
					}
				}
			});

			this.canva.addMouseMotionListener( new MouseMotionAdapter() 
			{
				@Override
				public void mouseDragged(MouseEvent e) 
				{
					if( pX1.get() >= 0 && plotImg[ 0 ] != null )
					{
						if( clisData != null )
						{
							int w = e.getX() - pX1.get();
							
							int x = ( w < 0 ) ? e.getX() : pX1.get();
							
							w = ( w < 0 ) ? -w : w;
							w = ( w < 1) ? 1 : w;
							
							int h = plotImg[ 0 ].getHeight();
							
							Color c = new Color( 0.75F, 0.75F, 0.75F, 0.5F );
							BufferedImage selArea = (BufferedImage) BasicPainter2D.paintRectangle( w, h, 1F, Color.BLACK, c );
							
							
							BufferedImage plotImgArea = (BufferedImage)BasicPainter2D.copyImage( plotImg[ 0 ] );
							BasicPainter2D.compoundImages( plotImgArea, x, 0, selArea );
							
							JLabel cv = (JLabel)e.getSource();
							cv.setVisible( false );
							cv.setIcon( new ImageIcon( plotImgArea ) );
							cv.setVisible( true );
						}
					}
				}
			});
			
			this.canva.addMouseWheelListener( new MouseWheelListener() 
			{				
				@Override
				public void mouseWheelMoved(MouseWheelEvent e) 
				{
					if( clisData != null )
					{
						Number[][] dat = clisData.get( currentVar.getName() ) ;
						
						if( dat != null )
						{
							int step = (int)getSpinnerStep().getValue();
							
							step = ( step < dat.length ) ? step : dat.length;
							
							int mov = ( e.getWheelRotation() < 0  ) ? step : -step;
	
							sampleIndex_A -= mov;
							sampleIndex_A = ( sampleIndex_A < 0 ) ? 0 : sampleIndex_A;
	
							sampleIndex_A = ( sampleIndex_A + step >= dat.length ) ? dat.length - step : sampleIndex_A;
	
							drawDataPlot( sampleIndex_A, sampleIndex_A + step );
						}
					}
				}
			});
		}
		
		return this.canva;
	}
}
