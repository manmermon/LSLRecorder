/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2011-2019 by Manuel Merino Monge <manmermon@dte.us.es>
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

package lslrec.gui.panel.primary;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.SpinnerNumberModel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import javax.swing.border.BevelBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import lslrec.config.language.Language;
import lslrec.control.handler.CoreControl;
import lslrec.dataStream.family.DataStreamFactory;
import lslrec.dataStream.family.setting.IMutableStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.MutableStreamSetting;
import lslrec.dataStream.family.setting.StreamExtraLabels;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.dataStream.sync.SyncMethod;
import lslrec.dataStream.tools.StreamUtils;
import lslrec.dataStream.tools.StreamUtils.StreamDataType;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionDictionary;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.gui.GuiTextManager;
import lslrec.gui.GuiManager;
import lslrec.gui.dialog.Dialog_AdvancedOptions;
import lslrec.gui.miscellany.DisabledPanel;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.gui.miscellany.SelectedButtonGroup;
import lslrec.gui.miscellany.VerticalFlowLayout;
import lslrec.gui.panel.plugin.Panel_PluginSettings;
import lslrec.config.ConfigApp;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.config.SettingOptions;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lslrec.auxiliar.WarningMessage;
import lslrec.auxiliar.extra.FileUtils;
import lslrec.auxiliar.extra.Tuple;

public class Panel_StreamingSettings extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String STREAM_NAME = "LSL_STREAM_NAME";
	public static final String STREAM_SYNC = "LSL_STREAM_SYNC";
	
	// Tabs
	private JTabbedPane tabStreams;
	//private CanvasLSLDataPlot LSLplot;

	//JPANEL
	private JPanel contentPanel;
	private JPanel jPanelStreamInfo;	
	private JPanel jPanelGeneralAddInfoOutFile;
	private JPanel jOutFileFormat;
	private JPanel paneStreamInfo;
	private JPanel panelOutFileOption;
	private JPanel panelOutFileName;
	private JPanel panelDeviceAndSetting;
	private JPanel panelDataPlotter;
	private JPanel jOutFile;

	//JCOMBOX
	private JComboBox< String > fileFormat;

	//JTEXTFIELD
	//private JTextField filePath;
	private JTextField fileName;
	private JTextField generalDescrOutFile;

	// JCHECKBOX
	private JCheckBox encryptKeyActive;
	//private JCheckBox delBinaryFiles;
	//private JCheckBox parallelizeActive;
	
	//JBUTTON	
	private JButton jButtonSelectOutFile;
	private JButton btnOutFormatOptions;

	// JScrollPanel
	private JScrollPane scrollPanelSelectDevPanel;
	
	//ISTREAMSETTINGS
	private IStreamSetting[] deviceInfo;

	//JFrame
	private JFrame winOwner;

	private SelectedButtonGroup selectedDeviceGroup;
	private SelectedButtonGroup syncDeviceGroup;

	// JTree
	private JTree devInfoTree;
	
	//Map
	//private Map< String, Component > parameters;
	
	// SplitPanel
	private JSplitPane splitPanelDevices;
	
	// DisabledPanel
	private DisabledPanel disPanel;

	//Tuple
	//private Tuple< String, String > currentSelectedDev;
	//private LSLConfigParameters currentSelectedDev;

	/**
	 * Create the panel.
	 */
	public Panel_StreamingSettings( JFrame owner )  throws Exception
	{
		this.winOwner = owner;

		super.setLayout( new BorderLayout( 0, 0 ) );

		//this.parameters = new HashMap< String, Component >();

		this.selectedDeviceGroup = this.getRadioButtonGroup();
		this.syncDeviceGroup = this.getSynDeviceGroup();

		this.updateDeviceInfos();

		if( this.deviceInfo == null || this.deviceInfo.length < 1 )
		{
			ConfigApp.setProperty( ConfigApp.ID_STREAMS, new HashSet< IMutableStreamSetting >() );
		}

		super.add( this.getContentPanel(), BorderLayout.CENTER );
		super.setName( "Streaming Settings" );
		
		JComboBox< String > fileFormat = this.getJComboxFileFormat();
		
		fileFormat.setSelectedIndex( -1 );
		if( fileFormat.getItemCount() > 0 )
		{
			fileFormat.setSelectedIndex( 0 );
		}
	}

	public void enableSettings( boolean enable )
	{
		this.getDisabledPanel( ).setEnabled( enable );
		
		this.setEnablePluginSetting( enable );
	}
	
	private DisabledPanel getDisabledPanel( )
	{
		if( this.disPanel == null )
		{
			this.disPanel = new DisabledPanel( this.getPanelDeviceAndSetting() ); 
		}
		return this.disPanel;
	}
	
	public void deselectSyncDevices()
	{
		for( ButtonModel bm : this.syncDeviceGroup.getSelections() )
		{
			bm.setSelected( false );
		}
	}
	
	private void updateDeviceInfos() throws Exception
	{
		this.deviceInfo = null;

		try
		{
			//LSL lsl = new LSL();

			///IStreamSetting[] streams = lsl.resolve_streams( );
			//IStreamSetting[] streams = DataStreamFactory.getStreamSettings( (StreamLibrary)ConfigApp.getProperty( ConfigApp.STREAM_LIBRARY ) );
			IStreamSetting[] streams = DataStreamFactory.getStreamSettings( );
			
			Comparator< Tuple< String, Integer > > comp = new Comparator<Tuple<String,Integer>>() 
			{	
				@Override
				public int compare( Tuple<String, Integer> o1, Tuple<String, Integer> o2) 
				{
					int eq = 0;
					
					if( o1 != o2 )
					{
						if( o1 == null )
						{
							eq = 1;
						}
						else if( o2 == null )
						{
							eq = -1;
						}
						else
						{
							eq = o1.t1.compareTo( o2.t1 );
							
							if( eq == 0 )
							{
								eq = o1.t2 - o2.t2;
							}
						}
					}
					
					return eq;
				}
			};
			
			TreeSet< Tuple< String, Integer > > streamsNames = new TreeSet< Tuple< String, Integer > >( comp );
			
			for( int i = 0; i < streams.length; i++ )
			{	
				IStreamSetting st = streams[ i ];
								
				Tuple< String, Integer > t = new Tuple< String, Integer>( st.name() + st.source_id() + st.uid(), i );
				
				streamsNames.add( t );
			}
			
			this.deviceInfo = new IStreamSetting[ streams.length ];
			
			int index = 0;
			Iterator< Tuple< String, Integer> > itStreamNames = streamsNames.iterator();
			
			while( itStreamNames.hasNext() && index < this.deviceInfo.length )
			{
				Tuple< String, Integer > t = itStreamNames.next();
				
				this.deviceInfo[ index ] = streams[ t.t2 ]; 
						
				index++;
			}
		}
		catch( Exception e )
		{			
			e.printStackTrace();
		}		
	}

	private JPanel getContentPanel()
	{
		if( this.contentPanel == null )
		{
			this.contentPanel = new JPanel( new BorderLayout() );// JSplitPane();

			//this.contentPanel.add( new JScrollPane( this.getJPanelEvent() ), BorderLayout.WEST );
			this.contentPanel.add( this.getJPanelDeviceInfo(), BorderLayout.CENTER );			
		}

		return this.contentPanel;
	}
	
	private JPanel getJPanelDeviceInfo()
	{
		if( this.jPanelStreamInfo == null )
		{
			this.jPanelStreamInfo = new JPanel();
			this.jPanelStreamInfo.setLayout( new BorderLayout() );

			//this.jPanelDeviceInfo.add( this.getJPanelOutFileFormat(), BorderLayout.NORTH );
			this.jPanelStreamInfo.add( this.getJPaneDeviceInfo( ), BorderLayout.CENTER );
		}

		return this.jPanelStreamInfo;
	}

	/**
	 * Refresh and check LSL streamings.
	 * 
	 * @return True if a selected streaming  is in the new list of streamings. Otherwise, False.
	 */
	public boolean refreshDataStreams()
	{		
		Tuple< JPanel, JTree > update = this.getUpdateStreamPanel();
		
		JSplitPane splitPanel = this.getContentPanelStreamInfo();
		splitPanel.setVisible( false );
				
		JScrollPane scr = this.getScrollPanelSelectDevPanel();
		scr.setVisible( false );
		
		scr.setViewportView( update.t1 );
		
		boolean findDevice = false;

		try
		{
			//updateDeviceInfos();						
			//p.add( getContentPanelDeviceInfo() );

			HashSet< IMutableStreamSetting > devs = (HashSet< IMutableStreamSetting >) ConfigApp.getProperty( ConfigApp.ID_STREAMS );

			for( IMutableStreamSetting dev : devs )
			{
				boolean find = false;
				
				if( dev.isSelected() )
				{	
					find = this.searchButton( this.selectedDeviceGroup, dev.source_id() );					
					findDevice = findDevice || find;
					
					if( !find )
					{
						dev.setSelected( false );
					}
				}
				
				if( dev.isSynchronationStream() )
				{
					find = this.searchButton( this.syncDeviceGroup, dev.source_id() );					
					findDevice = findDevice || find;
					
					if( !find )
					{
						dev.setSelected( false );
					}
										
					if( !find )
					{
						dev.setSynchronizationStream( false );
					}
				}
			}
		}					
		catch( Exception ex )
		{						
		}		
		
		scr.setVisible( true );
		splitPanel.setVisible( true );
		
		GuiTextManager.updateSelectedStreamText();
		
		return findDevice;
	}
	
	private boolean searchButton( SelectedButtonGroup gr, String devID )
	{
		boolean find = false;
		Enumeration< AbstractButton > BTS = gr.getElements();

		while( BTS.hasMoreElements() && !find )
		{
			AbstractButton bt = BTS.nextElement();
			find = bt.getName().equals( devID );

			if( find )
			{
				bt.setSelected( true );
			}
		}
		
		return find;
	}
	
	private JPanel getJPanelOutFile()
	{
		if( this.jOutFile == null )
		{
			this.jOutFile = new JPanel( );
			this.jOutFile.setLayout( new BoxLayout( this.jOutFile, BoxLayout.Y_AXIS ) );

			//this.jOutFile.add( this.getPanelGeneralAddInfoOutFile() );		
			//this.jOutFile.add( this.getJPanelOutFileFormat() );
			JScrollPane p = new JScrollPane( this.getJPanelOutFileFormat() );
			
			TitledBorder tb = new TitledBorder( Language.getLocalCaption( Language.OUTPUT_TEXT ) );
			//this.jOutFile.setBorder( tb );
			p.setBorder( tb );
						
			this.jOutFile.add( p );
			
			GuiTextManager.addComponent( GuiTextManager.BORDER, Language.OUTPUT_TEXT, tb );
		}

		return this.jOutFile;
	}
	
	private JPanel getPanelGeneralAddInfoOutFile() 
	{
		if( this.jPanelGeneralAddInfoOutFile == null )
		{
			this.jPanelGeneralAddInfoOutFile = new JPanel();
			this.jPanelGeneralAddInfoOutFile.setLayout( new BoxLayout( this.jPanelGeneralAddInfoOutFile, BoxLayout.X_AXIS ) );
			
			JLabel lb = new JLabel( );
			lb.setText( Language.getLocalCaption( Language.DESCRIPTION_TEXT ) );
			
			this.jPanelGeneralAddInfoOutFile.add( lb );
			this.jPanelGeneralAddInfoOutFile.add( Box.createRigidArea( new Dimension( 5, 0 ) ) );
			this.jPanelGeneralAddInfoOutFile.add( this.getGeneralDescrOutFile() );
			this.jPanelGeneralAddInfoOutFile.add( Box.createRigidArea( new Dimension( 5, 0 ) ) );
			//this.jPanelGeneralAddInfoOutFile.add( this.getDeleteBinaryFiles() );
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.DESCRIPTION_TEXT, lb );			
		}
		
		return this.jPanelGeneralAddInfoOutFile;
	}
	
	private JTextField getGeneralDescrOutFile() 
	{
		if( this.generalDescrOutFile == null )
		{
			final String ID = ConfigApp.OUTPUT_FILE_DESCR;
			
			this.generalDescrOutFile = new JTextField();
			
			Dimension d = this.generalDescrOutFile.getSize();
			FontMetrics fm = this.fileName.getFontMetrics( this.fileName.getFont() );
			d.width = fm.stringWidth( "W" ) * 30;			
			this.generalDescrOutFile.setPreferredSize( d );
			
			this.generalDescrOutFile.getDocument().addDocumentListener( new DocumentListener() 
			{				
				@Override
				public void removeUpdate(DocumentEvent e) 
				{
					updateDoc( e );
				}

				@Override
				public void insertUpdate(DocumentEvent e) 
				{
					updateDoc( e );
				}

				@Override
				public void changedUpdate(DocumentEvent e) 
				{
					updateDoc( e );
				}

				private void updateDoc( DocumentEvent e )
				{
					try 
					{
						String desc = e.getDocument().getText( 0, e.getDocument().getLength() );
						ConfigApp.setProperty( ID, desc );
						
						generalDescrOutFile.setToolTipText( desc );
					}
					catch (BadLocationException e1) 
					{
						e1.printStackTrace();
					}
				}
			});

			GuiManager.setGUIComponent( ID, ID, this.generalDescrOutFile );
		}
		
		return this.generalDescrOutFile;
	}
	
	/*
	private JCheckBox getDeleteBinaryFiles()
	{
		if( this.delBinaryFiles == null )
		{
			final String ID = ConfigApp.DEL_BINARY_FILES;
			
			this.delBinaryFiles = new JCheckBox();
			
			this.delBinaryFiles.setText( Language.getLocalCaption( Language.DEL_BINARY_FILES ) );
			this.delBinaryFiles.setHorizontalTextPosition( JCheckBox.LEFT );
			
			this.delBinaryFiles.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JCheckBox ch = (JCheckBox)e.getSource();
					
					ConfigApp.setProperty( ID, ch.isSelected() );				
				}
			});
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.DEL_BINARY_FILES, this.delBinaryFiles );
			GuiManager.setGUIComponent( ID, ID, this.delBinaryFiles );
			
			//this.delBinaryFiles.setSelected( (Boolean)ConfigApp.getProperty( ID ) );
		}
		
		return this.delBinaryFiles;
	}
	//*/

	private JCheckBox getEncryptKeyActive()
	{
		if( this.encryptKeyActive == null )
		{
			final String ID = ConfigApp.OUTPUT_ENCRYPT_DATA;
			
			this.encryptKeyActive = new JCheckBox();
			this.encryptKeyActive.setText( Language.getLocalCaption( Language.ENCRYPT_KEY_TEXT ) );
			this.encryptKeyActive.setHorizontalTextPosition( JCheckBox.LEFT );
			
			this.encryptKeyActive.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JCheckBox ch = (JCheckBox)e.getSource();
					
					ConfigApp.setProperty( ID, ch.isSelected() );				
				}
			});
			
			this.encryptKeyActive.addPropertyChangeListener( new PropertyChangeListener() 
			{				
				@Override
				public void propertyChange(PropertyChangeEvent evt) 
				{
					if( evt.getPropertyName().equals( "enabled" ) )
					{
						JCheckBox chb = (JCheckBox)evt.getSource();
						boolean ena = (Boolean)evt.getNewValue();
						
						ConfigApp.setProperty( ID, ena );
						
						if( ena )
						{
							ConfigApp.setProperty( ID, chb.isSelected() );
						}
					}
				}
			});
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.ENCRYPT_KEY_TEXT, this.encryptKeyActive );
			GuiManager.setGUIComponent( ID, ID, this.encryptKeyActive );
									
		}
		
		return this.encryptKeyActive;
	}
	
	/*
	private JCheckBox getParallelizeActive()
	{
		if( this.parallelizeActive == null )
		{
			final String ID = ConfigApp.OUTPUT_PARALLELIZE;
			
			this.parallelizeActive = new JCheckBox();
			this.parallelizeActive.setText( Language.getLocalCaption( Language.PARALLELIZE_TEXT ) );
			this.parallelizeActive.setHorizontalTextPosition( JCheckBox.LEFT );
			
			this.parallelizeActive.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JCheckBox ch = (JCheckBox)e.getSource();
					
					ConfigApp.setProperty( ID, ch.isSelected() );				
				}
			});
						
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.PARALLELIZE_TEXT, this.parallelizeActive );
			this.parameters.put( ID, this.parallelizeActive );									
		}
		
		return this.parallelizeActive;
	}
	*/
	
	private JPanel getJPanelOutFileFormat()
	{
		if( this.jOutFileFormat == null )
		{
			this.jOutFileFormat = new JPanel( );
			BorderLayout ly = new BorderLayout( 2, 2 );
			this.jOutFileFormat.setLayout( ly );
			//this.jOutFileFormat.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
			
			//this.jOutFileFormat.setBorder( BorderFactory.createTitledBorder( Language.getLocalCaption( Language.OUTPUT_TEXT ) ) );
			
			this.jOutFileFormat.add( this.getPanelOutFileOption(), BorderLayout.EAST );
			this.jOutFileFormat.add( this.getPanelOutFileName(), BorderLayout.CENTER );			
			this.jOutFileFormat.add( this.getPanelGeneralAddInfoOutFile(), BorderLayout.SOUTH );
			
			//GuiLanguageManager.addComponent( GuiLanguageManager.BORDER, Language.OUTPUT_TEXT, this.jOutFileFormat.getBorder() );
		}

		return this.jOutFileFormat;
	}

	private JPanel getPanelOutFileName()
	{
		if( this.panelOutFileName == null )
		{
			this.panelOutFileName = new  JPanel( new BorderLayout() );

			this.panelOutFileName.add( this.getJButtonLogFilePath(), BorderLayout.WEST );
			
			this.panelOutFileName.add( this.getJTextFileName(), BorderLayout.CENTER );
		}

		return this.panelOutFileName;
	}

	private JButton getJButtonLogFilePath() 
	{
		if ( this.jButtonSelectOutFile == null) 
		{
			this.jButtonSelectOutFile = new JButton();
			
			this.jButtonSelectOutFile.setIcon( GeneralAppIcon.Folder( 32, 22, Color.BLACK, Color.ORANGE ) );
			this.jButtonSelectOutFile.setBorder( new LineBorder( Color.black ));
			
			this.jButtonSelectOutFile.addActionListener(new java.awt.event.ActionListener() 
			{
				public void actionPerformed(java.awt.event.ActionEvent e) 
				{
					String format = (String)getJComboxFileFormat().getSelectedItem();
					String[] filters = null; 
					
					try
					{
						filters = new String[] { "clis" };//DataFileFormat.getSupportedFileExtension( format ) };
					}
					catch( Exception ex )
					{}
					
					String path[] = FileUtils.selectUserFile( (String)ConfigApp.getProperty( ConfigApp.OUTPUT_FILE_NAME )
															, false, false, JFileChooser.FILES_ONLY, format
															, filters, System.getProperty("user.dir") );
					if( path != null )
					{
						getJTextFileName().setText( path[ 0 ] );
					}
				}
			});
		}
		
		return this.jButtonSelectOutFile;
	}
	
	private JTextField getJTextFileName()
	{
		if( this.fileName == null )
		{
			this.fileName = new JTextField();

			this.fileName.setText( ConfigApp.getProperty( ConfigApp.OUTPUT_FILE_NAME ).toString() );

			Dimension d = this.fileName.getPreferredSize();
			FontMetrics fm = this.fileName.getFontMetrics( this.fileName.getFont() );
			d.width = fm.stringWidth( "Z" ) * 20;
			
			this.fileName.setPreferredSize( d );
			
			final String ID = ConfigApp.OUTPUT_FILE_NAME;

			this.fileName.getDocument().addDocumentListener( new DocumentListener() 
			{				
				@Override
				public void removeUpdate(DocumentEvent e) 
				{
					updateDoc( e );
				}

				@Override
				public void insertUpdate(DocumentEvent e) 
				{
					updateDoc( e );
				}

				@Override
				public void changedUpdate(DocumentEvent e) 
				{
					updateDoc( e );
				}

				private void updateDoc( DocumentEvent e )
				{
					try 
					{
						String name = e.getDocument().getText( 0, e.getDocument().getLength() );
						ConfigApp.setProperty( ID, name );
						
						fileName.setToolTipText( name );
					}
					catch (BadLocationException e1) 
					{
						e1.printStackTrace();
					}
				}
			});

			this.fileName.addFocusListener( new FocusAdapter() 
			{	
				private String text = "";
				@Override
				public void focusGained(FocusEvent e) 
				{
					text = ((JTextField)e.getSource()).getText();
				}

				@Override		
				public void focusLost(FocusEvent e) 
				{
					JTextField jtxt = (JTextField)e.getSource();

					String txt = jtxt.getText();
					
					try
					{
						
						File f = new File( txt );
						f.getCanonicalPath();						
						txt = f.getPath();
						
						String fileName = f.getName();
						if( txt.matches( "[^\\.]+(\\.[\\\\/]).*")
								||fileName.startsWith( "." ) 
								|| fileName.endsWith( "." ))
						{
							txt = text;
						}								
					}
					catch ( IOException ex) 
					{
						txt = text;
					}
					finally 
					{
						jtxt.setText( txt );
					}
				}
			});

			GuiManager.setGUIComponent( ID, ID, this.fileName );
		}

		return this.fileName;
	}

	private JPanel getPanelOutFileOption()
	{
		if( this.panelOutFileOption == null )
		{
			this.panelOutFileOption = new JPanel();
			this.panelOutFileOption.setLayout( new FlowLayout( FlowLayout.LEFT ) );

			JLabel lb = new JLabel( Language.getLocalCaption( Language.SETTING_LSL_OUTPUT_FORMAT ) );
			
			this.panelOutFileOption.add( lb );
			this.panelOutFileOption.add( this.getJComboxFileFormat() );

			JSeparator separator = new JSeparator();
			separator.setBorder( BorderFactory.createBevelBorder(BevelBorder.RAISED ) );
			Dimension d = this.getJComboxFileFormat().getPreferredSize();			
			separator.setPreferredSize( new Dimension( 1, d.height ) );
			
			this.panelOutFileOption.add( this.getOutputFormatOptsButton() );
			this.panelOutFileOption.add( this.getEncryptKeyActive() );			
			//this.panelOutFileOption.add( this.getParallelizeActive() );
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.SETTING_LSL_OUTPUT_FORMAT, lb );
		}

		return this.panelOutFileOption;
	}
	
	private JComboBox< String > getJComboxFileFormat()
	{
		if( this.fileFormat == null )
		{
			this.fileFormat = new JComboBox< String >();
			this.fileFormat.setEditable( false );

			final String ID = ConfigApp.OUTPUT_FILE_FORMAT;
			String[] fileFormat = DataFileFormat.getSupportedFileFormat();
			for( int i = 0; i < fileFormat.length; i++ )
			{
				this.fileFormat.addItem( fileFormat[ i ] );
			}

			this.fileFormat.setSelectedItem( ConfigApp.getProperty( ID ).toString() );

			this.fileFormat.addActionListener( new ActionListener()
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JComboBox< String > c = (JComboBox<String>)e.getSource();
					
					Object selItem = c.getSelectedItem();
					
					if( selItem != null )
					{
						String format =  selItem.toString();
						
						ConfigApp.setProperty( ID , format );
						
						Tuple< Encoder, WarningMessage > tencoder = DataFileFormat.getDataFileEncoder( format );
						Encoder encorder = tencoder.t1;
								
						String ext = encorder.getOutputFileExtension();
						
						String nameFile = getJTextFileName().getText();
						
						int pos = nameFile.lastIndexOf( "." );
						if( pos >= 0 )
						{
							nameFile = nameFile.substring( 0 , pos );
						}
						
						nameFile += ext;
						
						getJTextFileName().setText( nameFile );			
						
						getEncryptKeyActive().setEnabled( encorder.isSupportedEncryption() );
						
						List< SettingOptions > opts = encorder.getSettiongOptions();
						
						getOutputFormatOptsButton().setEnabled( ( opts != null  && !opts.isEmpty() ) );
						
						/*
						JScrollPane encoderSetting = CreatorEncoderSettingPanel.getSettingPanel( encorder.getSettiongOptions() );
						
						JTabbedPane panel = getJPanelOutFile();
						
						if( panel.getTabCount() > 1 )
						{
							panel.removeTabAt( 1 );
						}
						
						if( encoderSetting != null  )
						{					
							panel.addTab( encorder.getID(), encoderSetting );
						}
						*/
					}
				}
			});
			
			GuiManager.setGUIComponent( ID, ID, this.fileFormat );
		}

		return this.fileFormat;
	}
	
	private JPanel getJPaneDeviceInfo( )
	{		
		if( this.paneStreamInfo == null )
		{
			this.paneStreamInfo = new JPanel();
			this.paneStreamInfo.setLayout( new BorderLayout() );

			this.paneStreamInfo.add( this.getContentPanelStreamInfo( ), BorderLayout.CENTER ); 
		}

		return this.paneStreamInfo;
	}
		
	private JSplitPane getContentPanelStreamInfo( )
	{		
		if( this.splitPanelDevices == null )
		{
			this.splitPanelDevices = new JSplitPane();
			this.splitPanelDevices.setResizeWeight( 0.5 );
			this.splitPanelDevices.setDividerLocation( 0.5 );			
			this.splitPanelDevices.setOrientation( JSplitPane.VERTICAL_SPLIT );
	
			this.splitPanelDevices.setBackground( Color.WHITE );
			this.splitPanelDevices.setFocusable( false );
			this.splitPanelDevices.setFocusCycleRoot( false );
		
			Tuple< JPanel, JTree > deviceInfo = this.getUpdateStreamPanel();
			
			JScrollPane scr = this.getScrollPanelSelectDevPanel();
			scr.setVisible( false );
			scr.setViewportView( deviceInfo.t1 );
			scr.setVisible( true );
						
			this.splitPanelDevices.setLeftComponent( this.getDisabledPanel( ) );
			this.splitPanelDevices.setRightComponent( this.getJTabDevice( deviceInfo.t2 ) );
			
		}

		return this.splitPanelDevices;
	}

	private JScrollPane getScrollPanelSelectDevPanel()
	{
		if( this.scrollPanelSelectDevPanel == null )
		{
			this.scrollPanelSelectDevPanel = new JScrollPane();
		}
		
		return this.scrollPanelSelectDevPanel;
	}
	
	private JPanel getPanelDeviceAndSetting( )
	{
		if( this.panelDeviceAndSetting == null )
		{
			this.panelDeviceAndSetting = new JPanel();
			this.panelDeviceAndSetting.setLayout( new BorderLayout() );
			
			this.panelDeviceAndSetting.add( this.getJPanelOutFile(), BorderLayout.NORTH );
			this.panelDeviceAndSetting.add( this.getScrollPanelSelectDevPanel(), BorderLayout.CENTER );
		}
			
		return this.panelDeviceAndSetting;
	}
	
	private Tuple< JPanel, JTree > getUpdateStreamPanel()
	{
		JPanel panelLSLSettings  = new JPanel();
		JTree tree = this.getDeviceInfoTree();
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		DefaultMutableTreeNode root = (DefaultMutableTreeNode)( model.getRoot() );
		if( root != null )
		{
			root.removeAllChildren();
			model.reload();
			model.setRoot( null );
		}
		
		this.selectedDeviceGroup.removeAllButtons();
		this.syncDeviceGroup.removeAllButtons();
		
		try 
		{
			updateDeviceInfos();
		}
		catch (Exception e2) 
		{
		}
				
		if( this.deviceInfo != null
				&& this.deviceInfo.length > 0 )
		{
			DefaultMutableTreeNode tmodel = new DefaultMutableTreeNode();
			tmodel.setUserObject( Language.getLocalCaption( Language.SETTING_LSL_DEVICES ) + " (" + this.deviceInfo.length + ")" );
			
			GuiTextManager.removeComponent( GuiTextManager.TEXT, Language.SETTING_LSL_DEVICES );
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.SETTING_LSL_DEVICES, tmodel );
	
			final HashSet< IMutableStreamSetting > deviceIDs = ( HashSet< IMutableStreamSetting > )ConfigApp.getProperty( ConfigApp.ID_STREAMS );
	
			//GridBagLayout bl = new GridBagLayout();			
			//panelLSLSettings.setLayout( bl );	
			//panelLSLSettings.setLayout( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
			panelLSLSettings.setLayout( new BorderLayout() );
			
			List< JPanel > devsPanel = new ArrayList<JPanel>();
			for( int i = 0; i < 6; i++ )
			{
				JPanel p = new JPanel();
				VerticalFlowLayout ly = new VerticalFlowLayout( VerticalFlowLayout.TOP, 0, 2 );
				p.setLayout( ly  );
				//p.setAlignmentX( Component.LEFT_ALIGNMENT );
				
				devsPanel.add( p );
			}
			
			int maxHeightComponent = Integer.MIN_VALUE;
			
			
			//Remove unplugged devices
			Iterator< IMutableStreamSetting > itLSL = deviceIDs.iterator();
			while ( itLSL.hasNext() )
			{
				IMutableStreamSetting lslcfg = itLSL.next();
				boolean enc = false;
				for ( int i = 0; i < this.deviceInfo.length && !enc; i++ )
				{
					IStreamSetting info = this.deviceInfo[ i ];
					String uid = info.uid();
	
					enc = uid.equals( lslcfg.uid() );
				}
	
				if( !enc )
				{
					itLSL.remove();
				}
			}				
	
			// Adding new devices
			int devLen = this.deviceInfo.length;
			for( int i = 0; i < devLen; i++ )
			{
				IStreamSetting info = this.deviceInfo[ i ];
				
				String deviceName = info.name();
				String deviceType = info.content_type();
				String uid = info.uid();
				String sourceID = info.source_id(); 
	
				itLSL = deviceIDs.iterator();
				boolean enc = false;
				while( itLSL.hasNext() && !enc )
				{
					IMutableStreamSetting lslCfg = itLSL.next();
					enc = lslCfg.uid().equals( uid );
				}
	
				if( !enc )
				{
					if( sourceID.isEmpty() )
					{
						sourceID = deviceName + deviceType;
					}
	
					MutableStreamSetting newLSL = new MutableStreamSetting( info  );
					
					deviceIDs.add( newLSL );
				}
			}
	
			ConfigApp.setProperty( ConfigApp.ID_STREAMS, deviceIDs  );
	
			GuiTextManager.removeTranslateToken( GuiTextManager.TEXT, Language.SETTING_LSL_EXTRA );
			GuiTextManager.removeTranslateToken( GuiTextManager.TEXT, Language.SETTING_LSL_STREAM_PLOT );
			GuiTextManager.removeTranslateToken( GuiTextManager.TEXT, Language.SETTING_LSL_SYNC );
			GuiTextManager.removeTranslateToken( GuiTextManager.TEXT, Language.SETTING_LSL_CHUNCK_TOOLTIP );
			GuiTextManager.removeTranslateToken( GuiTextManager.TEXT, Language.SETTING_LSL_INTERLEAVED_TOOLTIP );
			GuiTextManager.removeTranslateToken( GuiTextManager.TEXT, Language.SETTING_LSL_NAME );		
			
			GuiTextManager.removeTranslateToken( GuiTextManager.TOOLTIP, Language.SETTING_LSL_SYNC_TOOLTIP );
			GuiTextManager.removeTranslateToken( GuiTextManager.TOOLTIP, Language.SETTING_LSL_CHUNCK_TOOLTIP );
			GuiTextManager.removeTranslateToken( GuiTextManager.TOOLTIP, Language.SETTING_LSL_INTERLEAVED_TOOLTIP );
			GuiTextManager.removeTranslateToken( GuiTextManager.TOOLTIP, Language.SETTING_LSL_SYNC );
			GuiTextManager.removeTranslateToken( GuiTextManager.TOOLTIP, Language.SETTING_LSL_EXTRA );
			GuiTextManager.removeTranslateToken( GuiTextManager.TOOLTIP, Language.SETTING_LSL_STREAM_PLOT );
			GuiTextManager.removeTranslateToken( GuiTextManager.TOOLTIP, Language.SETTING_LSL_NAME );
			
			GuiTextManager.clearSelectedStreamComponent();
						
			for( int i = 0; i < this.deviceInfo.length; i++ )
			{
				final IStreamSetting info = this.deviceInfo[ i ];
	
				String uid = info.uid();
				String deviceName = info.name();
				String deviceType = info.content_type();
				String sourceID = info.source_id();
	
				itLSL = deviceIDs.iterator();
				boolean enc = false;
				IMutableStreamSetting auxDev = null;
				while( itLSL.hasNext() && !enc )
				{
					auxDev = itLSL.next();
	
					enc = auxDev.uid().equals( uid );
				}
	
				if( !enc )
				{
					if( sourceID.isEmpty() )
					{
						sourceID = deviceName + deviceType;
					}
	
					auxDev = new MutableStreamSetting( info );
					deviceIDs.add( auxDev );
				}
				
				final IMutableStreamSetting dev = auxDev;
				
				String idNode = deviceName + " (" + uid + ")";
				DefaultMutableTreeNode t = this.getDeviceInfo( info, dev );//, dev.getExtraInfo() );	
				if( idNode != null && t != null )
				{
					t.setUserObject( idNode );
					tmodel.insert( t, tmodel.getChildCount() );
				}				
	
				JCheckBox selDataStream = new JCheckBox( deviceName );
				JCheckBox Sync = new JCheckBox();
	
				if( !sourceID.isEmpty() )
				{	
					selDataStream.setName( sourceID );
				}
				else
				{
					selDataStream.setName( deviceName + deviceType );
				}
	
				selDataStream.setToolTipText( deviceName + "- uid: " + uid );
				GuiTextManager.addComponent( GuiTextManager.TEXT, Language.SETTING_LSL_NAME, selDataStream );
				
				selDataStream.setHorizontalTextPosition( JCheckBox.RIGHT );
	
				selDataStream.addItemListener( new ItemListener()
				{	
					@Override
					public void itemStateChanged(ItemEvent e) 
					{
						JCheckBox b = (JCheckBox)e.getSource();
	
						boolean sel = b.isSelected();
						dev.setSelected( sel );
	
						if( Sync.isEnabled() )
						{
							if(sel && Sync.isSelected() )
							{
								Sync.setSelected( false );
							}
						}
						
						GuiTextManager.updateSelectedStreamText();
					}
				});
	
				if( !sourceID.isEmpty() )
				{	
					Sync.setName( sourceID );
				}
				else
				{
					Sync.setName( deviceName + deviceType );
				}
	
				Sync.setToolTipText( Language.getLocalCaption( Language.SETTING_LSL_SYNC_TOOLTIP ) );				
				Sync.setEnabled( info.channel_count() == 1 && info.data_type() == StreamDataType.int32 && devLen > 1 );
				
				GuiTextManager.addComponent( GuiTextManager.TOOLTIP, Language.SETTING_LSL_SYNC_TOOLTIP, Sync );
	
				if( Sync.isEnabled() )
				{
					Sync.setEnabled( true );
	
					Sync.addItemListener( new ItemListener()
					{						
						@Override
						public void itemStateChanged(ItemEvent e) 
						{
							JCheckBox c = (JCheckBox)e.getSource();
	
							boolean sel = c.isSelected();
							dev.setSynchronizationStream( sel );
	
							if( sel && selDataStream.isSelected())
							{
								selDataStream.setSelected( false );
							}
						}
					});
				}
	
				Sync.addItemListener( new ItemListener()
				{					
					@Override
					public void itemStateChanged(ItemEvent e) 
					{
						JCheckBox c = (JCheckBox)e.getSource();
						
						if( e.getStateChange() == ItemEvent.SELECTED )
						{	
							if( !((String)ConfigApp.getProperty( ConfigApp.SELECTED_SYNC_METHOD )).equalsIgnoreCase( SyncMethod.SYNC_STREAM ) )
							{
								/*
								JOptionPane.showMessageDialog( winOwner
																, Language.getLocalCaption( Language.MSG_SELECTED_LSL_SYNC_STREAM_ERROR )
																, Language.getLocalCaption( Language.DIALOG_ERROR )
																, JOptionPane.ERROR_MESSAGE );
								*/
								
								Exception ex = new Exception( Language.getLocalCaption( Language.MSG_SELECTED_LSL_SYNC_STREAM_ERROR ) );
								ExceptionMessage msg = new ExceptionMessage( ex, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionDictionary.ERROR_MESSAGE );
								ExceptionDialog.showMessageDialog( msg, true, false );
								
								c.setSelected( false );
							}
						}
					}
				});
				
				JButton addInfo = new JButton();
				addInfo.setName( Language.getLocalCaption( Language.SETTING_LSL_EXTRA ) );
				addInfo.setBorder( BorderFactory.createEtchedBorder() );
				Dimension d = selDataStream.getPreferredSize();
				d.width = Math.min( d.width, d.height ) - 1;
				d.height = d.width;
				addInfo.setPreferredSize( d );
				
				ImageIcon ic = GeneralAppIcon.Pencil( (int)(addInfo.getPreferredSize().height * 0.75D), Color.BLACK );
				if( ic != null )
				{
					addInfo.setIcon( ic );
				}
				else
				{
					addInfo.setText( Language.getLocalCaption( Language.SETTING_LSL_EXTRA ) );
				}
				
				GuiTextManager.addComponent( GuiTextManager.TEXT, Language.SETTING_LSL_EXTRA, addInfo );
				
				addInfo.addActionListener( new ActionListener() 
				{	
					@Override
					public void actionPerformed(ActionEvent arg0) 
					{						
						Map< String, String > extInfo = dev.getExtraInfo();
						String textInfo = "";
						
						if( extInfo != null )
						{
							textInfo = extInfo.get( StreamExtraLabels.ID_EXTRA_INFO_LABEL );
						}
						
						if( textInfo == null )
						{
							textInfo = "";
						}
						
	
						String txInfo = JOptionPane.showInputDialog( deviceName + " (" + uid + ").\n" + Language.getLocalCaption( Language.SETTING_LSL_EXTRA_TOOLTIP ) + ":", textInfo );
						if( txInfo != null )
						{
							textInfo = txInfo;
						}
	
						dev.setAdditionalInfo( StreamExtraLabels.ID_EXTRA_INFO_LABEL, textInfo );
	
						/*
						info.desc().remove_child( dev.getExtraInfoLabel() );
						info.desc().append_child_value( dev.getExtraInfoLabel(), textInfo );
						*/
	
						getJTabDevice( null ).setVisible( false );
	
						int numDevices = tmodel.getRoot().getChildCount();
						boolean enc = false;
						for( int i =  0; i < numDevices; i++ )
						{
							DefaultMutableTreeNode dMT = (DefaultMutableTreeNode)tmodel.getChildAt( i );
							enc = dMT.getUserObject().equals( idNode );
							if( enc )
							{
								tmodel.remove( i );
	
								DefaultMutableTreeNode t = getDeviceInfo( info, dev );//, textInfo );
								t.setUserObject( idNode );
								tmodel.insert( t, i );								
							}
						}
	
						JTree tree = getDeviceInfoTree();						
						DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
						model.setRoot( tmodel );
						model.reload( );
	
						getJTabDevice( null ).setVisible( true );
					}
				});	
				
				JButton plot = new JButton();
				plot.setName( Language.getLocalCaption( Language.SETTING_LSL_STREAM_PLOT ) );
				plot.setBorder( BorderFactory.createEtchedBorder() );
				plot.setPreferredSize( d );
				
				ic = GeneralAppIcon.Plot( (int)(plot.getPreferredSize().width * 0.75D) , Color.BLACK );
				if( ic != null )
				{
					plot.setIcon( ic );
				}
				else
				{
					plot.setText( Language.getLocalCaption( Language.SETTING_LSL_STREAM_PLOT ) );
				}				
				
				GuiTextManager.addComponent( GuiTextManager.TEXT, Language.SETTING_LSL_STREAM_PLOT, plot );
				
				plot.addActionListener( new ActionListener() 
				{					
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						try 
						{
							tabStreams.setSelectedIndex( 1 );
	
							CoreControl.getInstance().createLSLDataPlot( getPanelPlot(), dev );
						}
						catch (Exception e1) 
						{
							e1.printStackTrace();
						}
					}
				});
	
	
				JSpinner chunckSize = new JSpinner();
				chunckSize.setToolTipText( Language.getLocalCaption( Language.SETTING_LSL_CHUNCK_TOOLTIP ) );
				
				GuiTextManager.addComponent( GuiTextManager.TOOLTIP, Language.SETTING_LSL_CHUNCK_TOOLTIP, chunckSize );
				
				FontMetrics fm = chunckSize.getFontMetrics( chunckSize.getFont() );
				
				Dimension dm = chunckSize.getPreferredSize();
				dm.width = (int)( fm.stringWidth( " 99999 ") * 1.5 );
				chunckSize.setPreferredSize( dm );
				chunckSize.setSize( dm );
	
				chunckSize.setName( Language.getLocalCaption( Language.SETTING_LSL_CHUNCK ) );
	
				chunckSize.setModel(new SpinnerNumberModel( new Integer( 1 ), new Integer( 1 ), null , new Integer( 1 ) ) );
				chunckSize.setValue( dev.getChunkSize() );
	
				chunckSize.addMouseWheelListener( new MouseWheelListener() 
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
	
				chunckSize.addChangeListener( new ChangeListener()
				{				
					@Override
					public void stateChanged(ChangeEvent e) 
					{
						try
						{
							JSpinner sp = (JSpinner)e.getSource();
	
							Integer v = (Integer)sp.getValue();
	
							dev.setChunckSize( v );
						}
						catch( IllegalArgumentException ex )
						{
	
						}			
					}
				});		
	
				JToggleButton interleaved = new JToggleButton();
				interleaved.setBorder( BorderFactory.createRaisedBevelBorder() );
				interleaved.setPreferredSize( d );
	
				interleaved.setToolTipText( Language.getLocalCaption( Language.SETTING_LSL_INTERLEAVED_TOOLTIP ) );
				interleaved.setName( Language.getLocalCaption( Language.SETTING_LSL_INTERLEAVED ) );
				GuiTextManager.addComponent( GuiTextManager.TOOLTIP, Language.SETTING_LSL_INTERLEAVED, interleaved );
	
				interleaved.setSelected( dev.isInterleavedData() );
				if( interleaved.isSelected() )
				{
					interleaved.setBorder( BorderFactory.createLoweredBevelBorder() );
				}
	
				ic = GeneralAppIcon.InterleavedIcon( d.width, d.height, Color.BLACK, null, null );
				if( ic == null )
				{
					interleaved.setText( Language.getLocalCaption( Language.SETTING_LSL_INTERLEAVED ) );
				}
				else
				{
					interleaved.setIcon( ic );
				}
				
				GuiTextManager.addComponent( GuiTextManager.TEXT, Language.SETTING_LSL_INTERLEAVED, interleaved );
	
				
				interleaved.addActionListener( new ActionListener() 
				{	
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						JToggleButton bt = (JToggleButton)e.getSource();
	
						if( bt.isSelected() )
						{
							bt.setBorder( BorderFactory.createLoweredBevelBorder() );						
						}
						else
						{
							bt.setBorder( BorderFactory.createRaisedBevelBorder() );
						}
	
						dev.setInterleaveadData( bt.isSelected() );
					}
				});
	
	
				/*
				JPanel deviceGroup = new JPanel();
				deviceGroup.setAlignmentX( Component.LEFT_ALIGNMENT );
								
				deviceGroup.setLayout( new BoxLayout( deviceGroup, BoxLayout.X_AXIS ) );
				deviceGroup.add( Box.createRigidArea( new Dimension( 5, 0 ) ) );
				deviceGroup.add( addInfo );
				deviceGroup.add( Box.createRigidArea( new Dimension( 5, 0 ) ) );
				deviceGroup.add( plot );
				deviceGroup.add( Box.createRigidArea( new Dimension( 5, 0 ) ) );
				deviceGroup.add( chunckSize );
				deviceGroup.add( Box.createRigidArea( new Dimension( 5, 0 ) ) );
				deviceGroup.add( interleaved );
				deviceGroup.add( Box.createRigidArea( new Dimension( 10, 0 ) ) );
				deviceGroup.add( Sync );
				deviceGroup.add( Box.createRigidArea( new Dimension( 10, 0 ) ) );
				deviceGroup.add( r );				
				*/
				
				devsPanel.get( 0 ).add( addInfo );				
				devsPanel.get( 1 ).add( plot );
				devsPanel.get( 2 ).add( chunckSize );
				devsPanel.get( 3 ).add( interleaved );
				devsPanel.get( 4 ).add( Sync );
				devsPanel.get( 5 ).add( selDataStream );
									
				this.selectedDeviceGroup.add( selDataStream );
				if( Sync.isEnabled() )
				{
					this.syncDeviceGroup.add( Sync );
				}
					
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.weightx = 1.0;
				gbc.anchor = GridBagConstraints.NORTHWEST;
				gbc.gridy = panelLSLSettings.getComponentCount() + 1;
	
				if( i == this.deviceInfo.length - 1 )
				{
					gbc.weighty = 1.0;
				}
	
				//panelLSLSettings.add( deviceGroup, gbc );
	
				if( dev.isSelected() )
				{
					selDataStream.setSelected( true );
				}
				else if( dev.isSynchronationStream() )
				{
					if( Sync.isEnabled() )
					{
						Sync.setSelected( true );
					}
					else
					{
						dev.setSynchronizationStream( false );
					}
				}
			}
				
			JPanel auxPanel = new JPanel();
			auxPanel.setLayout( new GridBagLayout() );
			
			for( int i = 0; i < devsPanel.size(); i++ )
			{
				JPanel panel = devsPanel.get( i );
				
				JPanel colPanel = new JPanel();				
				VerticalFlowLayout fly = new VerticalFlowLayout( VerticalFlowLayout.CENTER, 0, 0 );
								
				colPanel.setLayout( fly );
				
				GridBagConstraints gbc = new GridBagConstraints();
				gbc.fill = GridBagConstraints.NONE;
				gbc.ipady = 0;
				gbc.weighty = 0;
				gbc.weightx = 0;
				gbc.anchor = GridBagConstraints.NORTHWEST;		
				gbc.gridx = GridBagConstraints.RELATIVE;
				gbc.gridy = 0;
				
				Component headerAddAct = null;
								
				if( panel.getComponentCount() > 0 )
				{
					Component c = panel.getComponent( 0 );					
					String name = c.getName();		
															
					String idTransLang = GuiTextManager.getTranslateToken( c );				
					GuiTextManager.removeTranslateToken( GuiTextManager.TEXT, idTransLang );
					
					if( i == devsPanel.size() - 2 )
					{
						name = Language.getLocalCaption( Language.SETTING_LSL_SYNC );
						idTransLang = Language.SETTING_LSL_SYNC;
					}
					else if( i >= devsPanel.size() - 1 )
					{
						JCheckBox jchb = new JCheckBox( );
						
						//jchb.setBorder( BorderFactory.createEtchedBorder() );	
						jchb.setFocusable( false );
						jchb.setFocusCycleRoot( false );
						jchb.setFocusPainted( false );
						jchb.setSelected( true );
						jchb.setBackground( Color.WHITE );
						jchb.setAlignmentX( Component.CENTER_ALIGNMENT );
						jchb.setMargin( new Insets( 0, 0, 0, 0 ) );
						
						jchb.addItemListener( new ItemListener() 
						{							
							@Override
							public void itemStateChanged(ItemEvent e) 
							{
								if( e.getID() == ItemEvent.ITEM_STATE_CHANGED )
								{
									if( e.getStateChange() == ItemEvent.DESELECTED )
									{
										JCheckBox jch = (JCheckBox)e.getSource();
										
										for( Component comp : panel.getComponents() )
										{
											if( comp instanceof JToggleButton )
											{
												((JToggleButton)comp).setSelected( true );
											}
										}
																				
										jch.setSelected( true );
									}
								}
							}
						});
						
						name = Language.getLocalCaption( Language.SETTING_LSL_NAME ) ;
						idTransLang = Language.SETTING_LSL_NAME ;
						
						headerAddAct = jchb;
						
						colPanel.setLayout( new BorderLayout() );
						colPanel.setBorder( null );
						
						gbc.fill = GridBagConstraints.HORIZONTAL;
						gbc.weightx = 1.0;
					}
					
					if( name != null && !name.isEmpty() )
					{
						JLabel lb = new JLabel( name );
						lb.setBackground( Color.WHITE );
						lb.setOpaque( true );
						Font f = lb.getFont();
						lb.setFont( new Font( f.getName(), Font.BOLD, f.getSize() ) );
						lb.setBorder( null );
						
						JPanel headerPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 2, 2 ) );							
						headerPanel.add( lb );
						headerPanel.setBorder( BorderFactory.createMatteBorder( 0, 1, 1, 0, Color.BLACK ) );
						headerPanel.setBackground( lb.getBackground() );
						
						if( idTransLang != null )
						{
							GuiTextManager.addComponent( GuiTextManager.TEXT, idTransLang, lb );
						}
						
						if( headerAddAct != null )
						{							
							Dimension d = headerAddAct.getPreferredSize();
							d.height = lb.getPreferredSize().height;// - 2;// - lb.getInsets().top - lb.getInsets().bottom ;
							headerAddAct.setPreferredSize( d );
							headerAddAct.setSize( d );
							
							headerPanel.add( headerAddAct, 0 );
							
							GuiTextManager.addSelectedStreamComponent( lb, idTransLang );
							
							//lb.setBorder( BorderFactory.createEmptyBorder( 1, 0, 0, 0 ) );		
						}
						
						colPanel.add( headerPanel, BorderLayout.NORTH, 0 );
					}
					
					for( Component com : panel.getComponents() )
					{
						if( com.getPreferredSize().height > maxHeightComponent )
						{
							maxHeightComponent = com.getPreferredSize().height ;
						}
					}					
				}
				
				colPanel.add( panel, BorderLayout.CENTER );				
				
				auxPanel.add( colPanel, gbc );
				
				//panelLSLSettings.add( colPanel );
			}
			
			panelLSLSettings.add( auxPanel, BorderLayout.NORTH );
				
			for( JPanel panel : devsPanel )
			{			
				for( Component com : panel.getComponents() )
				{
					Dimension d = com.getPreferredSize();
					d.height = maxHeightComponent;
					com.setPreferredSize( d );
				}
			}
			
			/*
			if( panelLSLSettings.getComponentCount() > 0 )
			{
				JPanel dGr = (JPanel)panelLSLSettings.getComponent( 0 );
	
				JPanel header = new JPanel( );
				header.setBackground( header.getBackground().brighter() );
	
				BoxLayout ly = new BoxLayout( header, BoxLayout.LINE_AXIS );
				header.setLayout( ly );
				header.setBorder( BorderFactory.createMatteBorder( 0, 0, 1, 0, Color.BLACK ) );
	
				Insets pad = new Insets( 0, 0, 0, 0 );
				String lb = "";
				boolean prevPad = true;
				Component[] cs = dGr.getComponents();
	
				for( int iC = 0; iC < cs.length - 1; iC++ )
				{
					Component c = cs[ iC ];
					String name = c.getName();					
	
					if( name != null && !name.isEmpty() )
					{
						prevPad = false;
	
						if( !lb.isEmpty() )
						{
							Dimension dc = c.getPreferredSize();
							FontMetrics fm = c.getFontMetrics( c.getFont() );
	
							//while( fm.stringWidth( name ) > dc.width + pad.left + pad.right / 2 ) 
							//{
							//	name = name.substring( 0, name.length() - 1 );
							//}
	
							if( header.getComponentCount() > 0 )
							{
								JSeparator s = new JSeparator( JSeparator.VERTICAL );
								Dimension ds = new Dimension( s.getPreferredSize().width, s.getMaximumSize().height );
								s.setMaximumSize( ds );
								header.add( s );
							}
	
							header.add( new JLabel( lb ) );							
							header.add( Box.createRigidArea( new Dimension( 5, 0 ) )  );
	
							//prevPad = true;
							pad.left = (int)Math.ceil( pad.right / 2.0D );
							pad.right = 0;
						}
	
						lb = name;
					}
					else
					{
						if( prevPad )
						{
							pad.left += c.getPreferredSize().width;
						}
						else
						{
							pad.right += c.getPreferredSize().width;
						}
					}
				}
	
				if( header.getComponentCount() > 0 )
				{
					JSeparator s = new JSeparator( JSeparator.VERTICAL );
					Dimension ds = new Dimension( s.getPreferredSize().width, s.getMaximumSize().height );
					s.setMaximumSize( ds );
					header.add( s );
	
					header.add( new JLabel( "Sync" ) );
					header.add( Box.createRigidArea( new Dimension( 2, 0 ) )  );
	
					s = new JSeparator( JSeparator.VERTICAL );
					ds = new Dimension( s.getPreferredSize().width, s.getMaximumSize().height );
					s.setMaximumSize( ds );					
					header.add( s );
	
					header.add( new JLabel( "Stream's name" ) );
				}				
	
				GridBagConstraints gbcHeader = new GridBagConstraints();
				gbcHeader.weightx = 1.0;
				gbcHeader.anchor = GridBagConstraints.NORTHWEST;
				gbcHeader.fill = GridBagConstraints.BOTH;
				gbcHeader.gridy = 0;
	
				panelLSLSettings.add( header, gbcHeader, 0 );
			}
			*/
			
			model = (DefaultTreeModel)tree.getModel();
			model.setRoot( tmodel );
			model.reload( );
		}
		
		
		
		return new Tuple< JPanel, JTree >( panelLSLSettings, tree );
	}


	private JTree getDeviceInfoTree()
	{
		if( this.devInfoTree == null )
		{
			this.devInfoTree = new JTree( new DefaultMutableTreeNode() );			
		}
		
		return this.devInfoTree;
	}	
	
	private JTabbedPane getJTabDevice( JTree tree )
	{
		if( this.tabStreams == null )
		{
			this.tabStreams = new JTabbedPane( );

			this.tabStreams.addTab( Language.getLocalCaption( Language.SETTING_LSL_DEVICES ) 
									, new JScrollPane( tree ) );
			Component c = this.tabStreams.getComponentAt( 0 );
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.SETTING_LSL_DEVICES, c );
			
			//this.tabDevice.addTab( Language.getLocalCaption( Language.SETTING_LSL_PLOT ), this.getLSLPlot() );
			this.tabStreams.addTab( Language.getLocalCaption( Language.SETTING_LSL_PLOT ), this.getPanelPlot() );
			c = this.tabStreams.getComponentAt( 1 );
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.SETTING_LSL_PLOT, c );
			
			this.tabStreams.setTabLayoutPolicy( JTabbedPane.SCROLL_TAB_LAYOUT );
		}

		return this.tabStreams;
	}
	
	private JPanel getPanelPlot()
	{
		if( this.panelDataPlotter == null )
		{
			this.panelDataPlotter = new JPanel( new BorderLayout() );
		}

		return this.panelDataPlotter;
	}

	private SelectedButtonGroup getRadioButtonGroup()
	{
		if( this.selectedDeviceGroup == null )
		{
			this.selectedDeviceGroup = new SelectedButtonGroup();
			//this.selectedDeviceGroup.setLayout( new BoxLayout( this.selectedDeviceGroup, BoxLayout.Y_AXIS ) );

			GuiManager.setGUIComponent( STREAM_NAME, ConfigApp.ID_STREAMS, this.selectedDeviceGroup );
		}

		return this.selectedDeviceGroup;
	}

	private SelectedButtonGroup getSynDeviceGroup()
	{
		if( this.syncDeviceGroup == null )
		{
			this.syncDeviceGroup = new SelectedButtonGroup();

			GuiManager.setGUIComponent( STREAM_SYNC, ConfigApp.ID_STREAMS, this.syncDeviceGroup );
		}

		return this.syncDeviceGroup;
	}
	
	private DefaultMutableTreeNode getDeviceInfo( IStreamSetting inInfo, IMutableStreamSetting dev )//, String extra )  
	{		 	
		DefaultMutableTreeNode tree = null;

		if( inInfo != null )
		{
			try
			{  
				/*
				LSL.StreamInlet in = new LSL.StreamInlet( info );
				StreamInfo inInfo = in.info();
				*/
				
				/*
				while( this.hasDescLabelNode( inInfo.desc(), dev.getExtraInfoLabel() ) )
				{
					dev.increaseExtraCountLabel();
				}
				
				inInfo.desc().append_child_value( dev.getExtraInfoLabel(), extra );
				String xml = inInfo.as_xml();
				*/
				
				String xml = StreamUtils.getDeepXmlStreamDescription( inInfo );
				String rootNode = inInfo.getRootNode2ExtraInfoLabel();
				
				Map< String, String > extraInfo = dev.getExtraInfo();
				if( extraInfo != null )
				{
					for( String id : extraInfo.keySet() )
					{
						String value = extraInfo.get( id );
						xml = StreamUtils.addElementToXmlStreamDescription( xml, rootNode, id,  value );
					}
				}
								
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				
				ByteArrayInputStream bis = new ByteArrayInputStream( xml.getBytes( "UTF-8" ) );
				Document doc = db.parse( bis );
				Node root = (Node)doc.getDocumentElement();
				tree = this.builtTreeNode( root );				
			}
			catch( Exception e )
			{			 
			}
		}

		return tree;
	}

	/*
	private boolean hasDescLabelNode( XMLElement desc, String lab )
	{
		boolean has = false;
		
		XMLElement child = desc.first_child();
		if( child != null )
		{
			String name = child.name().toLowerCase();
			
			if( !name.isEmpty() )
			{
				has = name.equals( lab.toLowerCase() );
				
				if( !has )
				{
					has = hasDescLabelNode( child.next_sibling(), lab );
				}
			}
		}
		
		return has;
	}
	*/
	
	private DefaultMutableTreeNode builtTreeNode( Node root )
	{		 
		DefaultMutableTreeNode dmtNode = new DefaultMutableTreeNode( this.getXMLAttributes( root ) );
		NodeList nodeList = root.getChildNodes();
		for (int count = 0; count < nodeList.getLength(); count++)
		{
			Node tempNode = nodeList.item( count );
			// make sure it's element node.
			if (tempNode.getNodeType() == Node.ELEMENT_NODE)
			{
				if( tempNode.hasChildNodes( ) )
				{
					// loop again if has child nodes
					dmtNode.add( builtTreeNode( tempNode ) );
				}
				else
				{
					dmtNode.add( new DefaultMutableTreeNode( this.getXMLAttributes( tempNode ) ) );					 
				}
			}
			/*
			else if( tempNode.getNodeType() == Node.TEXT_NODE )
			{
				dmtNode.add( new DefaultMutableTreeNode( this.getXMLAttributes( tempNode ) ) );
			}
			*/
		}

		return dmtNode;
	}

	private String getXMLAttributes( Node node )
	{
		String nodeText = node.getNodeName();

		if( node.getPreviousSibling() != null )
		{
			nodeText += "="+ node.getTextContent();
		}

		String nodeAttributes = "";

		if( node.hasAttributes() )
		{
			nodeAttributes = "{";
			NamedNodeMap attrs = node.getAttributes();

			for( int i = 0; i < attrs.getLength(); i++ )
			{
				nodeAttributes += attrs.item( i ) + "; " ;
			}

			nodeAttributes = nodeAttributes.substring( 0, nodeAttributes.length() - 2 ) + "}";
		}

		if( !nodeAttributes.isEmpty() )
		{
			nodeText += ":" + nodeAttributes; 
		}

		return nodeText;
	}
		
	private JButton getOutputFormatOptsButton()
	{
		if( this.btnOutFormatOptions == null )
		{
			this.btnOutFormatOptions = new JButton();
			
			int s = getJComboxFileFormat().getPreferredSize().height;
			
			this.btnOutFormatOptions.setPreferredSize( new Dimension( s, s ) );
			
			s = (int)( s * 0.75D);
			
			this.btnOutFormatOptions.setIcon( new ImageIcon( GeneralAppIcon.Config2( Color.BLACK )
																		.getScaledInstance( s, s, Image.SCALE_SMOOTH ) ) ); // GeneralAppIcon.Pencil( s, Color.BLACK ) );
			
			//this.btnOutFormatOptions.setBorder( BorderFactory.createEtchedBorder() );
			this.btnOutFormatOptions.setFocusPainted( false );
			
			this.btnOutFormatOptions.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0) 
				{
					Object format = getJComboxFileFormat().getSelectedItem();
					if( format != null )
					{
						Tuple< Encoder, WarningMessage > tenc = DataFileFormat.getDataFileEncoder( format.toString() );
						Encoder enc = tenc.t1;
						
						List< SettingOptions > opts = enc.getSettiongOptions();
						ParameterList pars = enc.getParameters();
						
						for( String idPar : pars.getParameterIDs() )
						{
							Parameter par = pars.getParameter( idPar );
							
							Object val = ConfigApp.getProperty( par.getID() );
							
							if( val != null )
							{
								par.setValue( val );
							}
							
							par.addValueChangeListener( new ChangeListener() 
							{	
								@Override
								public void stateChanged(ChangeEvent e) 
								{
									Parameter par = (Parameter)e.getSource();
									
									ConfigApp.setProperty( par.getID(), par.getValue() );
								}
							});
						}
						
						Dialog_AdvancedOptions dial = new Dialog_AdvancedOptions( opts, pars );
						dial.setTitle( format.toString() + " - " + Language.getLocalCaption( Language.SETTING_LSL_OUTPUT_FORMAT ) );
						
						dial.setLocationRelativeTo( winOwner );
						dial.setResizable( false );
						dial.setIconImage( winOwner.getIconImage() );
						dial.setVisible( true );											
						dial.pack();
					}
				}
			});
		}
		
		return this.btnOutFormatOptions;
	}
	
	public void addSetting2TabbedPanel( String title, JPanel panel )
	{
		JTabbedPane t = this.getJTabDevice( null );
		
		t.addTab( title, panel );
	}
	
	private void setEnablePluginSetting( boolean ena )
	{
		JTabbedPane t = this.getJTabDevice( null );
		
		for( Component c : t.getComponents() )
		{
			if( c instanceof Panel_PluginSettings )
			{
				((Panel_PluginSettings) c ).enableSettings( ena ); 
			}
		}
	}
	
	public void refreshPlugins()
	{
		JTabbedPane t = this.getJTabDevice( null );
		
		for( Component c : t.getComponents() )
		{
			if( c instanceof Panel_PluginSettings )
			{
				((Panel_PluginSettings) c ).refreshSelectedPlugins(); 
			}
		}
	}
}
