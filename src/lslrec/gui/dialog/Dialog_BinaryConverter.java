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
package lslrec.gui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lslrec.auxiliar.WarningMessage;
import lslrec.auxiliar.extra.ConvertTo;
import lslrec.auxiliar.extra.FileUtils;
import lslrec.auxiliar.extra.Tuple;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.config.SettingOptions;
import lslrec.config.language.Language;
import lslrec.dataStream.binary.input.writer.StreamBinaryHeader;
import lslrec.dataStream.binary.setting.BinaryFileStreamSetting;
import lslrec.dataStream.family.setting.IMutableStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting.StreamLibrary;
import lslrec.dataStream.family.setting.MutableStreamSetting;
import lslrec.dataStream.family.setting.SimpleMutableStreamSetting;
import lslrec.dataStream.family.setting.SimpleStreamSetting;
import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.sync.SyncMarkerCollectorWriter;
import lslrec.gui.GuiManager;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.gui.panel.plugin.item.CreatorDefaultSettingPanel;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.GridBagConstraints;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;

public class Dialog_BinaryConverter extends JDialog 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 299279362396885020L;
	
	// JPanel
	private JPanel contentPanel;
	private JPanel buttonPane;	
	private JPanel filesPanel;
	private JPanel dataFilePanel;
	//private JPanel timeStampPanel;
	private JPanel panelBtnAddData;
	//private JPanel panelBtnAddTime;
	private JPanel panelFilesInfo;
	private JPanel panelBinInfo;
	private JPanel panelOutPath;
	private JPanel panelSyncFile;
	
	// JTable
	private JTable tableFileData;
	//private JTable tableFileTime;
	private JLabel lblBinaryDataFiles;
	//private JLabel lblBinaryTimeFiles;
	
	
	// Labels
	private JLabel lblStreamName;
	private JLabel lblDataType;
	private JLabel lblNoChannels;
	private JLabel lblChunkSize;
	private JLabel lblSyncFile;
	private JLabel lblXmlDescr;
	private JLabel lblFile;
	private JLabel lblOutputFormat;
	private JLabel lblOutputPath;
	
	// JTextField
	private JTextField txtStreamName;
	private JTextField txtDataType;
	private JTextField txtNumChannels;
	private JTextField txtChunkSize;
	private JTextField txtXMLDesc;
	private JTextField txtFilePath;
	private JTextField txtOutFileFolder;
	private JTextField txtSyncMarkerFile;
		
	// Buttons
	private JButton btnDone;
	private JButton btnCancel;
	private JButton buttonAddData;
	private JButton btnOutFolder;
	private JButton btnSelectSyncFile;
	private JButton btnOutFormatOptions;
	
	// Combox
	private JComboBox< String > fileFormat;
	
	// ScrollPanel
	private JScrollPane scrollTableData;
	
	// Checkbox
	private JCheckBox chckbxDeleteBinaries;
	private JCheckBox chbxEncrypt;
	private JCheckBox chbParallelize;
	
	// JToggleButton
	private JToggleButton jtgBtnSortSyncFile;
	
	
	// Others variables
	private String currentFolderPath = System.getProperty( "user.dir" );
	
	private boolean clearBinaryFiles = true;
	
	private Map< String, IMutableStreamSetting > binaryDataFiles;	
	private IMutableStreamSetting currentBinFile = null;
		
	private OutputFileFormatParameters outFormat;
	
	private int FileTableColumn = 0;
	
	/**
	 * Create the dialog.
	 */
 	public Dialog_BinaryConverter( Frame owner, boolean modal ) 	
	{
		super( owner, modal );
		
		this.binaryDataFiles = new HashMap< String, IMutableStreamSetting >( );
		this.outFormat = DataFileFormat.getDefaultOutputFileFormatParameters();
		
		super.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
		
		JRootPane root = this.getRootPane( );

		ActionListener escListener = new ActionListener( ) 
		{
			@Override
			 public void actionPerformed( ActionEvent e ) 
			 {
			 	dispose( );
			 }
		};
		
		root.registerKeyboardAction( escListener, KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), JComponent.WHEN_IN_FOCUSED_WINDOW );
				
		super.setContentPane( this.getMainPanel( ) );
	}
	
	private void checkEncryptKey()
	{
		if( getChckboxEncrypt().isSelected() && !binaryDataFiles.isEmpty() )
		{	
			Dialog_Password dg = new Dialog_Password( GuiManager.getInstance().getAppUI()
												, Language.getLocalCaption( Language.ENCRYPT_KEY_TEXT ) );
			
			Dimension d = GuiManager.getInstance().getAppUI().getSize();
			Point l = GuiManager.getInstance().getAppUI().getLocation();
			
			Dimension dPass = dg.getSize();
			
			Point loc = dg.getLocation();
			loc.x = l.x + ( d.width - dPass.width ) / 2;
			loc.y = l.y + ( d.height- dPass.height ) / 2;
			dg.setLocation( loc );
			
			dg.setVisible( true );
			
			while( dg.getState() == Dialog_Password.PASSWORD_INCORRECT )
			{
				dg.setMessage( dg.getPasswordError() + Language.getLocalCaption( Language.REPEAT_TEXT ) + "." );
				dg.setVisible( true );
			}
			
			if( dg.getState() != Dialog_Password.PASSWORD_OK )
			{	
				binaryDataFiles.clear();
				
				JOptionPane.showMessageDialog( super.getOwner(), Language.getLocalCaption( Language.PROCESS_TEXT ) 
													+ " " + Language.getLocalCaption( Language.CANCEL_TEXT ) );
			}
			else
			{
				String key = dg.getPassword();
				
				this.outFormat.setParameter( OutputFileFormatParameters.ENCRYPT_KEY, key );
			}
		}
	}
	
	public JPanel getMainPanel( )	
	{
		if( this.contentPanel == null )	
		{
			this.contentPanel = new JPanel( );
			this.contentPanel.setLayout( new BorderLayout( ) );
			this.contentPanel.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
			this.contentPanel.add( this.getPanelBtnDone( ), BorderLayout.SOUTH );
			this.contentPanel.add( getPanelFilesInfo( ), BorderLayout.CENTER );
		}
		
		return this.contentPanel;
	}
	
	private JPanel getPanelBtnDone( )	
	{
		if( this.buttonPane == null )
		{
			this.buttonPane = new JPanel( );
			this.buttonPane.setLayout( new FlowLayout( FlowLayout.RIGHT ) );
			this.buttonPane.add( this.getChckbxDeleteBinaries() );
			this.buttonPane.add( this.getChckboxEncrypt() );
			
			this.buttonPane.add( this.getBtnDone( ) );
			this.buttonPane.add( this.getBtnCancel( ) );
		}
		
		return this.buttonPane;
	}
	
	public List< Tuple< Tuple< BinaryFileStreamSetting, OutputFileFormatParameters>, BinaryFileStreamSetting > > getBinaryFiles( ) throws Exception
	{	
		if( this.clearBinaryFiles )
	 	{
	 		this.binaryDataFiles.clear( );
	 	}
		
		this.checkEncryptKey();
		
		List<  Tuple< Tuple< BinaryFileStreamSetting, OutputFileFormatParameters>, BinaryFileStreamSetting > > binFiles = new ArrayList<  Tuple< Tuple< BinaryFileStreamSetting, OutputFileFormatParameters>, BinaryFileStreamSetting > >( );
		
		BinaryFileStreamSetting syncHeader = null;
		String syncFile = this.getTxtSyncFilePath().getText();
		if( !syncFile.isEmpty() )
		{			
			if( this.getJtgBtSyncFile().isSelected() )
			{
				String outFile = syncFile + "_sort.sync"; 
				SyncMarkerCollectorWriter.sortMarkers( syncFile, outFile, null, this.getChckbxDeleteBinaries().isSelected() );
				
				syncFile = outFile;
			}
			
			syncHeader = new BinaryFileStreamSetting( this.getBinaryFileInfo( syncFile ), new File( syncFile ) );
		}		
				
		for( String file : this.binaryDataFiles.keySet() )
		{
			BinaryFileStreamSetting datBin = new BinaryFileStreamSetting( this.binaryDataFiles.get( file ), new File( file ) );
			OutputFileFormatParameters format = this.outFormat.clone();
						
			binFiles.add( new Tuple< Tuple< BinaryFileStreamSetting, OutputFileFormatParameters>, BinaryFileStreamSetting >( new Tuple< BinaryFileStreamSetting, OutputFileFormatParameters >( datBin, format ), syncHeader ) );
		}
		
		return binFiles;
	}
	
	private JButton getBtnDone( )
	{
		if( this.btnDone == null )
		{
			this.btnDone = new JButton( Language.getLocalCaption( Language.OK_TEXT ) );
			this.btnDone.setActionCommand( "OK" );
					
			this.btnDone.addActionListener( new ActionListener( ) 
			{	
				@Override
				public void actionPerformed( ActionEvent e ) 
				{
					clearBinaryFiles = false;
						
					dispose();
				}
			} );
		}
		
		return this.btnDone;
	}
	
	private JButton getBtnCancel( )
	{
		if( this.btnCancel == null )
		{
			this.btnCancel = new JButton( Language.getLocalCaption( Language.CANCEL_TEXT ) );
			this.btnCancel.setActionCommand( "Cancel" );
						
			this.btnCancel.addActionListener( new ActionListener( ) 
			{	
				@Override
				public void actionPerformed( ActionEvent e ) 
				{
					clearBinaryFiles = true;
					
					dispose();
				}
			} );
		}
		
		return this.btnCancel;
	}
	
	private JPanel getFilesPanel( ) 
	{
		if ( filesPanel == null ) 
		{
			filesPanel = new JPanel( );
			filesPanel.setLayout( new BorderLayout() ); //new BoxLayout( filesPanel, BoxLayout.X_AXIS ) );
			filesPanel.add( this.getDataFilePanel( ), BorderLayout.CENTER );
			filesPanel.add( this.getSyncFilePanel(), BorderLayout.SOUTH );
			//filesPanel.add( getTimeStampPanel( ) );
		}

		return filesPanel;
	}

	private JPanel getSyncFilePanel()
	{
		if( this.panelSyncFile == null )
		{
			this.panelSyncFile = new JPanel();
			this.panelSyncFile.setLayout( new BoxLayout( this.panelSyncFile, BoxLayout.X_AXIS ) );
			this.panelSyncFile.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
			
			this.panelSyncFile.add( this.getJtgBtSyncFile() );
			this.panelSyncFile.add( Box.createRigidArea( new Dimension( 5, 0 ) ) );
			this.panelSyncFile.add( this.getLblSyncFile() );
			this.panelSyncFile.add( this.getTxtSyncFilePath() );
			this.panelSyncFile.add( this.getBtnSelectSyncFilePath() );
			
		}
		
		return this.panelSyncFile;
	}
	
	private JLabel getLblSyncFile()
	{
		if( this.lblSyncFile == null )
		{
			this.lblSyncFile = new JLabel( Language.getLocalCaption( Language.SETTING_LSL_SYNC ) + " " );
			
			this.lblSyncFile.setFont( new Font( "Tahoma", Font.BOLD, 11 ) );
		}
		
		return this.lblSyncFile;
	}
	
	private JTextField getTxtSyncFilePath()
	{
		if( this.txtSyncMarkerFile == null )
		{
			this.txtSyncMarkerFile = new JTextField();
		}
		
		return this.txtSyncMarkerFile;
	}
	
	private JButton getBtnSelectSyncFilePath()
	{
		if( this.btnSelectSyncFile == null )
		{
			this.btnSelectSyncFile = new JButton();
			
			try
			{
				this.btnSelectSyncFile.setIcon( GeneralAppIcon.Folder( 20, 16, Color.BLACK, Color.GREEN.brighter() ) );
			}
			catch ( Exception e ) 
			{
				this.btnSelectSyncFile.setText( Language.getLocalCaption( Language.SELECT_TEXT ) );
			}
			
			this.btnSelectSyncFile.addActionListener( new ActionListener( ) 
			{
				public void actionPerformed( ActionEvent e ) 
				{
					JTextField t = getTxtSyncFilePath();
					
					String[] FILES = FileUtils.selectUserFile( "", true, false, JFileChooser.FILES_ONLY, null, null, currentFolderPath );
					if( FILES != null )
					{
						for( String file : FILES )
						{
							t.setText( file );
						}
						
						currentFolderPath = (new File( FILES[ 0 ] )).getAbsolutePath();
					}
				}
			} );
			
			this.btnSelectSyncFile.setFont( new Font( "Tahoma", Font.BOLD, 11 ) );
			this.btnSelectSyncFile.setBorder( BorderFactory.createRaisedSoftBevelBorder( ) );
		}
		
		return this.btnSelectSyncFile;
	}
	
	private JToggleButton getJtgBtSyncFile()
	{
		if( this.jtgBtnSortSyncFile == null )
		{
			this.jtgBtnSortSyncFile = new JToggleButton( " " + Language.getLocalCaption( Language.SORT_TEXT ) + " " );
			this.jtgBtnSortSyncFile.setFont( new Font( "Tahoma", Font.BOLD, 11 ) );
			
			this.jtgBtnSortSyncFile.setHorizontalAlignment( SwingConstants.RIGHT );
			this.jtgBtnSortSyncFile.setBorder( BorderFactory.createBevelBorder( BevelBorder.RAISED ));
			
			this.jtgBtnSortSyncFile.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JToggleButton tgb = (JToggleButton)e.getSource();
					
					if( tgb.isSelected() )
					{
						tgb.setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ));
					}
					else
					{
						tgb.setBorder( BorderFactory.createBevelBorder( BevelBorder.RAISED ));
					}
				}
			});
		}
		
		return this.jtgBtnSortSyncFile;
	}
	
	private JPanel getDataFilePanel( ) 
	{
		if ( dataFilePanel == null )
		{
			dataFilePanel = new JPanel( );
			dataFilePanel.setForeground( Color.BLACK );
			dataFilePanel.setBorder( new EmptyBorder( 0, 0, 0, 2 ) );
			dataFilePanel.setLayout( new BorderLayout( 0, 0 ) );
			dataFilePanel.add( getPanelBtnAddData( ), BorderLayout.NORTH );
			dataFilePanel.add( this.getScrollTableData(), BorderLayout.CENTER );
		}
		
		return dataFilePanel;
	}

	private JScrollPane getScrollTableData()
	{
		if( this.scrollTableData == null )
		{
			this.scrollTableData = new JScrollPane( getTableFileData( ) );
		}
		
		return this.scrollTableData;
	}
	
	private JPanel getPanelBtnAddData( ) 
	{
		if ( panelBtnAddData == null )
		{
			panelBtnAddData = new JPanel( );
			FlowLayout flowLayout = ( FlowLayout ) panelBtnAddData.getLayout( );
			flowLayout.setAlignment( FlowLayout.LEFT );
			//panelBtnAddData.add( getBtnMoveUpDataFile( ) );
			//panelBtnAddData.add( getButtonMoveDownDataFile( ) );
			panelBtnAddData.add( getLblBinaryDataFiles( ) );
			panelBtnAddData.add( getButtonAddData( ) );
		}

		return panelBtnAddData;
	}

	private JButton getButtonAddData( ) 
	{
		if ( buttonAddData == null )
		{
			buttonAddData = new JButton( );
			
			try
			{
				buttonAddData.setIcon( GeneralAppIcon.Folder( 20, 16, Color.BLACK, Color.ORANGE ) );
			}
			catch ( Exception e ) 
			{
				buttonAddData.setText( Language.getLocalCaption( Language.SELECT_TEXT ) );
			}
			
			buttonAddData.addActionListener( new ActionListener( ) 
			{
				public void actionPerformed( ActionEvent e ) 
				{
					clearBinaryFiles( getTableFileData( ), binaryDataFiles );
					
					String[] FILES = FileUtils.selectUserFile( "", true, true, JFileChooser.FILES_ONLY, null, null, currentFolderPath );
					if( FILES != null )
					{
						for( String file : FILES )
						{	
							IMutableStreamSetting bh = getBinaryFileInfo( file );
							if( bh != null )
							{								
								insertBinaryFilesInTable( getTableFileData( ), file, bh.isInterleavedData() );
									
								binaryDataFiles.put( file, bh );
							}
						}
						
						currentFolderPath = (new File( FILES[ 0 ] ) ).getAbsolutePath();
					}
				}
			} );
			
			buttonAddData.setFont( new Font( "Tahoma", Font.BOLD, 11 ) );
			buttonAddData.setBorder( BorderFactory.createRaisedSoftBevelBorder( ) );
		}

		return buttonAddData;
	}

	
	private void clearBinaryFiles( JTable t, Map< String, IMutableStreamSetting > binaryFiles )	
	{
		this.clearInfoLabels( );
			
		binaryFiles.clear( );

		DefaultTableModel dm = ( DefaultTableModel )t.getModel( );
		while( dm.getRowCount( ) > 0 )
		{
			dm.removeRow( dm.getRowCount( ) - 1 );
		}
	}
	
	private JLabel getLblBinaryDataFiles( ) 
	{
		if ( lblBinaryDataFiles == null )
		{
			lblBinaryDataFiles = new JLabel(  Language.getLocalCaption( Language.LSL_BIN_DATA_FILES )  );
		}

		return lblBinaryDataFiles;
	}

	private JPanel getPanelFilesInfo( ) 
	{
		if ( panelFilesInfo == null )
		{
			panelFilesInfo = new JPanel( );
			panelFilesInfo.setLayout( new BorderLayout( 0, 0 ) );
			panelFilesInfo.add( this.getFilesPanel( ), BorderLayout.CENTER );
			panelFilesInfo.add( getPanelBinInfo( ), BorderLayout.SOUTH );
		}

		return panelFilesInfo;
	}

	private JPanel getPanelBinInfo( ) 
	{
		if ( panelBinInfo == null )
		{
			final int COLS = 2;
			
			panelBinInfo = new JPanel( );
			panelBinInfo.setBorder( new EmptyBorder( 4, 0, 0, 0 ) );
			
			GridBagLayout gbl_panelBinInfo = new GridBagLayout( );
			gbl_panelBinInfo.columnWidths = new int[]{0, 0, 0};
			gbl_panelBinInfo.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
			gbl_panelBinInfo.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_panelBinInfo.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
			panelBinInfo.setLayout( gbl_panelBinInfo );
			
			int colPadding = 0;
			
			GridBagConstraints gbc = new GridBagConstraints( );
			gbc.anchor = GridBagConstraints.EAST;
			gbc.insets = new Insets( 0, 0, 5, 5 );
			gbc.gridx = 0;
			gbc.gridy = ( panelBinInfo.getComponentCount()  + colPadding ) / COLS;
			panelBinInfo.add( getLblFile( ), gbc );
			
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 1;
			gbc.gridy = ( panelBinInfo.getComponentCount()  + colPadding ) / COLS;
			panelBinInfo.add( getTxtFilePath( ), gbc );

			gbc.fill = GridBagConstraints.NONE;
			gbc.gridx = 0;
			gbc.gridy = ( panelBinInfo.getComponentCount()  + colPadding ) / COLS;
			panelBinInfo.add( getLblStreamName( ), gbc );
			
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 1;
			gbc.gridy = ( panelBinInfo.getComponentCount()  + colPadding ) / COLS;
			panelBinInfo.add( getTxtStreamName( ), gbc );
			
			gbc.fill = GridBagConstraints.NONE;
			gbc.gridx = 0;
			gbc.gridy = ( panelBinInfo.getComponentCount()  + colPadding ) / COLS;
			panelBinInfo.add( getLblDataType( ), gbc );
			
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 1;
			gbc.gridy = ( panelBinInfo.getComponentCount()  + colPadding ) / COLS;
			panelBinInfo.add( getTxtDataType( ), gbc );
			
			gbc.fill = GridBagConstraints.NONE;
			gbc.gridx = 0;
			gbc.gridy = ( panelBinInfo.getComponentCount()  + colPadding ) / COLS;
			panelBinInfo.add( getLblNoChannels( ), gbc );
			
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 1;
			gbc.gridy = ( panelBinInfo.getComponentCount()  + colPadding ) / COLS;
			panelBinInfo.add( getTxtNumChannels( ), gbc );
			
			gbc.fill = GridBagConstraints.NONE;
			gbc.gridx = 0;
			gbc.gridy = ( panelBinInfo.getComponentCount()  + colPadding ) / COLS;
			panelBinInfo.add( getLblChunkSize(), gbc );
			
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 1;
			gbc.gridy = ( panelBinInfo.getComponentCount()  + colPadding ) / COLS;
			panelBinInfo.add( getTxtChunkSize( ), gbc );
			
			gbc.fill = GridBagConstraints.NONE;
			gbc.gridx = 0;
			gbc.gridy = ( panelBinInfo.getComponentCount()  + colPadding ) / COLS;
			panelBinInfo.add( getLblXmlDescr( ), gbc );
			
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 1;
			gbc.gridy = ( panelBinInfo.getComponentCount()  + colPadding ) / COLS;
			panelBinInfo.add( getTxtXMLDesc( ), gbc );
			
			gbc.fill = GridBagConstraints.NONE;
			gbc.gridx = 0;
			gbc.gridy = ( panelBinInfo.getComponentCount()  + colPadding ) / COLS;
			panelBinInfo.add( getLblOutputFormat( ), gbc );
			/*			gbc.fill = GridBagConstraints.HORIZONTAL;			
			gbc.gridx = 1;
			gbc.gridy = ( panelBinInfo.getComponentCount()  + colPadding ) / COLS;
			panelBinInfo.add( getComboBoxOutputFormat( ), gbc );
			*/
			
			JPanel panelAux = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			panelAux.add( getComboBoxOutputFormat( ) );
			//panelAux.add( new JLabel( Language.getLocalCaption( Language.OPTIONS_TEXT ) ) );
			panelAux.add( this.getOutputFormatOptsButton() );			
			//colPadding++;
			
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.anchor = GridBagConstraints.WEST;
			gbc.gridx = 1;
			gbc.gridy = ( panelBinInfo.getComponentCount()  + colPadding ) / COLS;			
			panelBinInfo.add( panelAux, gbc );
			
			gbc.fill = GridBagConstraints.NONE;
			gbc.anchor = GridBagConstraints.EAST;
			gbc.gridx = 0;
			gbc.gridy = ( panelBinInfo.getComponentCount() + colPadding ) / COLS;
			panelBinInfo.add( getLblOutputPath( ), gbc );
			
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 1;
			gbc.gridy = ( panelBinInfo.getComponentCount()  + colPadding ) / COLS;
			panelBinInfo.add( getPanelOutPath( ), gbc );
		}

		return panelBinInfo;
	}

	private JButton getOutputFormatOptsButton()
	{
		if( this.btnOutFormatOptions == null )
		{
			this.btnOutFormatOptions = new JButton();
			
			int s = getChbOutputParallelize().getPreferredSize().height;
			
			this.btnOutFormatOptions.setPreferredSize( new Dimension( s, s ) );
			
			s = (int)( s * 0.75D);
			
			this.btnOutFormatOptions.setIcon( new ImageIcon( GeneralAppIcon.Config2( Color.BLACK )
																		.getScaledInstance( s, s, Image.SCALE_SMOOTH ) ) ); // GeneralAppIcon.Pencil( s, Color.BLACK ) );
			this.btnOutFormatOptions.setBorder( BorderFactory.createEtchedBorder() );
			
			final JDialog ref = this;
			this.btnOutFormatOptions.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0) 
				{
					Object format = getComboBoxOutputFormat().getSelectedItem();
					if( format != null )
					{
						JDialog dial = new JDialog( ref );
						
						dial.setModal( true );
						dial.setLayout( new BorderLayout() );
						dial.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
						
						JPanel main = new JPanel( new BorderLayout() );
						main.setBackground( Color.green );
						
						
						dial.setTitle( format.toString() + " - " + Language.getLocalCaption( Language.SETTING_LSL_OUTPUT_FORMAT ) );

						
						Tuple< Encoder, WarningMessage > tenc = DataFileFormat.getDataFileEncoder( format.toString() );
						Encoder enc = tenc.t1;
						List< SettingOptions > opts = enc.getSettiongOptions();
						
						ParameterList encPars = enc.getParameters();
						
						for( SettingOptions opt : opts )
						{	
							Parameter p = outFormat.getParameter( opt.getIDReferenceParameter() );
							Parameter p2  = encPars.getParameter( opt.getIDReferenceParameter() );
							
							String id = opt.getIDReferenceParameter();
							Object val = null;
							String langID = null;
									
							if( p != null )
							{
								val = p.getValue();
								langID = p.getLangID();
							}
							
							if( val == null )
							{
								val = p2.getValue();
								langID = p2.getLangID();
							}
							
							if( langID == null || langID.isEmpty() )
							{
								langID = p2.getLangID();
							}
							
							outFormat.setParameter( id, val );							
							outFormat.getParameter( id ).setLangID( langID );
						}
						
						
						JScrollPane scr = new JScrollPane( CreatorDefaultSettingPanel.getSettingPanel( opts, outFormat.getAllParameters() ) );

						main.add( scr, BorderLayout.CENTER );
						
						dial.add( main );
						
						dial.setLocation( ref.getLocation() );					
						dial.pack();
						
						Dimension s = dial.getSize();
						FontMetrics fm = dial.getFontMetrics( dial.getFont() );
						
						int t = fm.stringWidth( dial.getTitle() ) * 2;
						if( t > s.width )
						{
							s.width = t;
						}
						s.height += 10;
						
						dial.setSize( s );
						
						dial.setVisible( true );
					}
				}
			});
		}
		
		return this.btnOutFormatOptions;
	}

	private JLabel getLblFile( ) 
	{
		if ( lblFile == null )
		{
			lblFile = new JLabel(  Language.getLocalCaption( Language.MENU_FILE ) );
			lblFile.setFont( new Font( "Tahoma", Font.BOLD, 12 ) );
		}

		return lblFile;
	}

	private JTextField getTxtFilePath( ) 
	{
		if ( txtFilePath == null )
		{
			txtFilePath = new JTextField( );
			txtFilePath.setEditable( false );
			txtFilePath.setColumns( 10 );
		}

		return txtFilePath;
	}

	private JLabel getLblStreamName( ) 
	{
		if ( lblStreamName == null )
		{
			lblStreamName = new JLabel(  Language.getLocalCaption( Language.SETTING_LSL_NAME ) );
			lblStreamName.setFont( new Font( "Tahoma", Font.BOLD, 12 ) );
		}

		return lblStreamName;
	}

	private JLabel getLblDataType( ) 
	{
		if ( lblDataType == null )
		{
			lblDataType = new JLabel(  Language.getLocalCaption( Language.LSL_DATA_TYPE )  );
			lblDataType.setFont( new Font( "Tahoma", Font.BOLD, 12 ) );
		}

		return lblDataType;
	}

	private JLabel getLblNoChannels( ) 
	{
		if ( lblNoChannels == null )
		{
			lblNoChannels = new JLabel(  Language.getLocalCaption( Language.LSL_CHANNELS )  );
			lblNoChannels.setFont( new Font( "Tahoma", Font.BOLD, 12 ) );
		}

		return lblNoChannels;
	}
	
	private JLabel getLblChunkSize( ) 
	{
		if ( this.lblChunkSize == null )
		{
			this.lblChunkSize = new JLabel(  Language.getLocalCaption( Language.SETTING_LSL_CHUNCK )  );
			this.lblChunkSize.setFont( new Font( "Tahoma", Font.BOLD, 12 ) );
		}

		return this.lblChunkSize;
	}
	

	private JLabel getLblXmlDescr( ) 
	{
		if ( lblXmlDescr == null )
		{
			lblXmlDescr = new JLabel(  Language.getLocalCaption( Language.LSL_XML_DESCRIPTION )  );
			lblXmlDescr.setFont( new Font( "Tahoma", Font.BOLD, 12 ) );
		}

		return lblXmlDescr;
	}
	
	private JTextField getTxtStreamName( )	
	{
		if ( txtStreamName == null ) 
		{
			txtStreamName = new JTextField( );
			txtStreamName.setColumns( 10 );
			txtStreamName.setEditable( false );
			
			txtStreamName.setDocument( new PlainDocument()
					{
						/**
						 * 
						 */
						private static final long serialVersionUID = 6140600460345989861L;
						private String text = "";
						@Override
						public void insertString(int offset, String txt, AttributeSet a) throws BadLocationException 
						{
							 try 
							 {
							        text = getText( 0, getLength() );
							        if ( ( text + txt ).matches( "[0-9a-zA-Z_-]+" ) ) 
							        {
							            super.insertString( offset, txt, a );
							        }
							 
							 }
							 catch (Exception ex) 
							 {
							 }
						}
					});
			
			txtStreamName.getDocument().addDocumentListener( new DocumentListener() 
			{				
				@Override
				public void removeUpdate( DocumentEvent e ) 
				{
					update( e );
				}
				
				@Override
				public void insertUpdate( DocumentEvent e ) 
				{
					update( e );
				}
				
				@Override
				public void changedUpdate( DocumentEvent e ) 
				{
					update( e );
				}
				
				private void update( DocumentEvent e )
				{
					if( currentBinFile != null )
					{
						try 
						{
							String nm = e.getDocument().getText( 0, e.getDocument().getLength() );
							if( !nm.isEmpty() )
							{
								IMutableStreamSetting prevInfo = currentBinFile;
								
								SimpleStreamSetting strSetting = new  SimpleStreamSetting( StreamLibrary.LSL
																						, nm
																						//, prevInfo.content_type()
																						, prevInfo.data_type()
																						, prevInfo.getTimestampDataType()
																						, prevInfo.getStringLegthDataType()
																						, prevInfo.channel_count()
																						, prevInfo.getChunkSize()
																						, prevInfo.sampling_rate()
																						, prevInfo.getRecordingCheckerTimer()
																						, prevInfo.source_id()
																						, prevInfo.uid()
																						//, prevInfo.hostname()
																						//, prevInfo.session_id()
																						//, prevInfo.version()
																						//, prevInfo.created_at()
																						//, prevInfo.description()
																						, prevInfo.getExtraInfo()																						
																						//, prevInfo.isInterleavedData()
																						//, prevInfo.isSelected()
																						//, prevInfo.isSynchronationStream() 
																						);
								
								/*
								List< String > parentNodes = new ArrayList< String >();
								parentNodes.add( "info" );
								parentNodes.add( LSLUtils.getAdditionalInformationLabelInXml() );
								
								addExtraStreamInfo( info.desc(), prevInfo.as_xml(), parentNodes );
								
								currentBinFile.setStreamInfo( info );
								*/
								
								currentBinFile = new MutableStreamSetting( strSetting );
								
								txtStreamName.setBorder( (new JTextField()).getBorder() );
							}
							else
							{								
								txtStreamName.setBorder( BorderFactory.createLineBorder( Color.RED ) );
							}
						} 
						catch ( BadLocationException e1 ) 
						{
						}
					}
				}
			});
		}

		return txtStreamName;
	}
	
	private JTextField getTxtDataType( ) 
	{
		if ( txtDataType == null )
		{
			txtDataType = new JTextField( );
			txtDataType.setEditable( false );
			txtDataType.setColumns( 10 );
		}

		return txtDataType;
	}

	private JTextField getTxtNumChannels( ) 
	{
		if ( txtNumChannels == null )
		{
			txtNumChannels = new JTextField( );
			txtNumChannels.setEditable( false );
			txtNumChannels.setColumns( 10 );
		}

		return txtNumChannels;
	}

	private JTextField getTxtChunkSize( ) 
	{
		if ( this.txtChunkSize == null )
		{
			this.txtChunkSize = new JTextField( );
			this.txtChunkSize.setEditable( false );
			this.txtChunkSize.setColumns( 10 );
		}

		return this.txtChunkSize;
	}
	
	private JTextField getTxtXMLDesc( ) 
	{
		if ( txtXMLDesc == null )
		{
			txtXMLDesc = new JTextField( );
			txtXMLDesc.setEditable( false );
			txtXMLDesc.setColumns( 10 );
		}

		return txtXMLDesc;
	}
		

	private JTable getTableFileData( )
	{
		if( this.tableFileData == null )
		{	
			this.tableFileData = this.getCreateJTable( );
			this.tableFileData.setModel( this.createBinFileTable( ) );
			this.tableFileData.getModel().addTableModelListener( new TableModelListener() 
			{				
				@Override
				public void tableChanged(TableModelEvent e) 
				{
					if( e.getType() == TableModelEvent.UPDATE )
					{
						int row = e.getFirstRow();
						int col = e.getColumn();
						
						if( row >= 0 && col >= 0 )
						{
							TableModel tm = (TableModel)e.getSource();							
							Object d = tm.getValueAt( row, col );
							
							String file = tm.getValueAt( row, FileTableColumn ).toString();
							
							updateDataStreamSetting( file, col, d );
						}
					}
				}
			});
			
			this.tableFileData.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
			
			this.tableFileData.setPreferredScrollableViewportSize( this.tableFileData.getPreferredSize( ) );
			this.tableFileData.setFillsViewportHeight( true );
			
			this.tableFileData.getColumnModel().getColumn( 0  ).setResizable( false );
			
			this.tableFileData.getColumnModel().getColumn( 1  ).setResizable( false );
			this.tableFileData.getColumnModel().getColumn( 1 ).setPreferredWidth( 75 );
			this.tableFileData.getColumnModel().getColumn( 1 ).setMaxWidth( 75 );
			
			this.tableFileData.getSelectionModel( ).addListSelectionListener( new ListSelectionListener( ) 
			{				
				@Override
				public void valueChanged( ListSelectionEvent e ) 
				{
					ListSelectionModel md = ( ListSelectionModel )e.getSource( );
					if( !md.isSelectionEmpty( ) && !e.getValueIsAdjusting( ) )
					{
						int r = tableFileData.getSelectedRow( );
						
						if( r < binaryDataFiles.size( ) )
						{
							String file = tableFileData.getValueAt( r, 0 ).toString();
							currentBinFile = binaryDataFiles.get( file );
							
							showBinaryFileInfo( file, currentBinFile );
						}
					}
				}
			} );		
		}
		
		return this.tableFileData;
	}	

	private JTable getCreateJTable( )
	{
		JTable t = new JTable( )
				{
					private static final long serialVersionUID = 1L;
			
					//Implement table cell tool tips. 
					public String getToolTipText( MouseEvent e ) 					 
					{
						String tip = null;
						Point p = e.getPoint( );
						int rowIndex = rowAtPoint( p );
						int colIndex = columnAtPoint( p );
						
						try 
						{
							tip = getValueAt( rowIndex, colIndex ).toString( );
						}
						catch ( RuntimeException e1 )
						{
							//catch null pointer exception if mouse is over an empty line
						}
				
						return tip;
					}				 
				 };
				 
		t.getTableHeader( ).setReorderingAllowed( false );
			
		/*
		t.addKeyListener( new KeyAdapter() 
		{				
			@Override
			public void keyPressed(KeyEvent e) 
			{
				if( e.isControlDown() )
				{
					if( e.getKeyCode() == KeyEvent.VK_UP )
					{
						moveBinaryFile( t, -1 );
					}
					else if( e.getKeyCode() == KeyEvent.VK_DOWN )
					{
						moveBinaryFile( t, +1 );
					}
						
				}
			}
		});
		*/
		
		return t;
	}
	
	private TableModel createBinFileTable( )
	{					
		TableModel tm = new DefaultTableModel( null, new String[] { Language.getLocalCaption( Language.MENU_FILE )
																	, Language.getLocalCaption( Language.SETTING_LSL_INTERLEAVED )} )
						{
							private static final long serialVersionUID = 1L;
								
							Class[] columnTypes = getColumnTableTypes();
							boolean[] columnEditables = new boolean[] { false, true };
								
							public Class getColumnClass( int columnIndex ) 
							{
								return columnTypes[columnIndex];
							}
																								
							public boolean isCellEditable( int row, int column ) 
							{
								boolean editable = columnEditables[column];
									
								return editable;
							}
						};		
						
		return tm;
	}
	
	private Class[] getColumnTableTypes()
	{
		this.FileTableColumn = 0;
		return new Class[]{ String.class, Boolean.class };
	}
		
	private void insertBinaryFilesInTable( JTable t, String file, boolean interleaved )
	{	
		Object[] vals = new Object[ t.getColumnCount( ) ];
		Class[] colTableTypes = this.getColumnTableTypes();
		
		for( int i = 0; i < vals.length; i++ )
		{			
			if( i < colTableTypes.length )
			{
				if( colTableTypes[ i ].equals( String.class ) )
				{
					vals[ i ] = file;
				}
				else if( colTableTypes[ i ].equals( Boolean.class ) )
				{
					vals[ i ] = interleaved;
				}
			}			
		}
		
		DefaultTableModel m = ( DefaultTableModel )t.getModel( );
		m.addRow( vals );
	}

	private void showBinaryFileInfo( String file, IMutableStreamSetting header )	
	{	
		this.clearInfoLabels( );		
	
		if( header != null )
		{
			this.getTxtFilePath( ).setText( file );
			this.getTxtStreamName( ).setText( header.name() );
			this.getTxtStreamName( ).setEditable( true );
			
			StreamDataType type = header.data_type();
			String t = type + "";
						
			this.getTxtDataType( ).setText( t );	
			this.getTxtNumChannels( ).setText( header.channel_count() + "" );
			this.getTxtXMLDesc( ).setText( header.description().replaceAll( "\\s+", "" ) );
			this.getTxtChunkSize( ).setText( header.getChunkSize() + "" );
		}
	}
		
	
	private void updateDataStreamSetting( String file, int fieldIndex, Object val )
	{
		if( file != null && val != null )
		{
			IMutableStreamSetting bh = this.binaryDataFiles.get( file );
			
			switch ( fieldIndex )
			{
				case 1:
				{
					try
					{
						bh.setInterleaveadData( (Boolean)val);
					}
					catch (Exception e) 
					{
					}
					
					break;
				}
				default:
					break;
			}
		}
	}
	
	private IMutableStreamSetting getBinaryFileInfo( String file )	
	{
		this.clearInfoLabels( );		
		
		IMutableStreamSetting bH = null;
		
		BufferedReader reader = null;
		
		try 
		{
			reader = new BufferedReader( new FileReader( new File( file ) ) );
			
			String binHeader = reader.readLine( );
		
			String binSplitChar = StreamBinaryHeader.HEADER_BINARY_SEPARATOR ;
			
			String[] parts = binHeader.split( binSplitChar );
					
			String name = "", type = "", timeType = ""
					, chs = "", chunk = "", xml = ""
					, interleaved = "", strLenType = "";
						
			for( int i = 0; i < parts.length; i++ )
			{
				if( i == 0 )
				{
					name = parts[ i ];
				}
				else if( i == 1 )
				{
					type = parts[ i ]; // data type
				}				
				else if( i == 2 )
				{
					chs = parts[ i ]; // channels
				}
				else if( i == 3 )
				{
					chunk = parts[ i ]; // chunk size
				}
				else if( i == 4 )
				{
					timeType = parts[ i ]; // time data type
				}
				else if( i == 5 )
				{ 
					strLenType = parts[ i ]; // string length data type
				}
				else if( i == 6 )
				{
					interleaved = parts[ i ]; // intereleavd
				}
				else
				{
					if( !xml.isEmpty() )
					{
						xml += binSplitChar;
					}
					
					xml += parts[ i ];
				}
			}
			
			xml = xml.replaceAll( "\\s+", " " );
			
			Map< String, String > fields = this.getStreamInfoFields( xml );
						
			String stType = fields.get( "type" );
			stType = ( stType == null ? "" : stType );
			
			String fr = fields.get( "nominal_srate" );			
			double frq = IStreamSetting.IRREGULAR_RATE;
			if( fr != null )
			{
				try
				{
					frq = new Double( fr );
				}
				catch (Exception e) 
				{
				}
			}
			
			String sid = fields.get( "source_id" );
			sid = ( sid == null ? "" : sid );
			
			//StreamInfo info = new StreamInfo( name, stType, new Integer( chs ), frq, new Integer( type ), sid );		
			
			SimpleMutableStreamSetting strSetting = new  SimpleMutableStreamSetting( StreamLibrary.LSL
																, name
																//, stType
																, StreamDataType.valueOf( type )
																, StreamDataType.valueOf( timeType )
																, StreamDataType.valueOf( strLenType )
																, Integer.parseInt( chs )
																, frq
																, 3
																, sid
																, System.nanoTime() + ""
																//, ""
																//, System.nanoTime() + ""
																//, 1
																//, System.nanoTime()
																//, xml
																, null
																, Integer.parseInt( chunk )
																//, Boolean.parseBoolean( interleaved )
																//, false
																//, false 
																);
			strSetting.setDescription( xml );
			strSetting.setInterleaveadData( Boolean.parseBoolean( interleaved ) );
			
			
			/*
			List< String > parentNodes = new ArrayList< String >();
			parentNodes.add( "info" );
			parentNodes.add( LSLUtils.getAdditionalInformationLabelInXml() );
			this.addExtraStreamInfo( info.desc(), xml, parentNodes);
						
			bH = new IMutableStreamSetting( info, new Integer( timeType ), new Integer( strLenType ), xml, new Integer( chunk ), new Boolean( interleave ), false  );
			*/
			
			bH = new MutableStreamSetting( strSetting );
			
		}
		catch ( Exception e ) 
		{
			bH = null;
		}
		finally 
		{
			if( reader != null )
			{
				try 
				{
					reader.close( );
				}
				catch ( IOException e ) 
				{
				}
			}
		}
		
		return bH;
	}
	
	private Map< String, String > getStreamInfoFields( String xml )
	{
		Map< String, String > nodes = new HashMap<String, String>();
		
		if( xml != null && !xml.isEmpty() ) 
		{
			Document doc = ConvertTo.Transform.xmlStringToXMLDocument( xml );
		
			Node n = doc.getFirstChild();
			NodeList nl = n.getChildNodes();
			Node an;
	
			for (int i=0; i < nl.getLength(); i++) 
			{
			    an = nl.item(i);
			    
			    if(an.getNodeType()==Node.ELEMENT_NODE) 
			    {
			    	nodes.put( an.getNodeName(), an.getTextContent() );
			    }
			}
		}
		
		return nodes;
	}
	
	/*
	private void addExtraStreamInfo( XMLElement desc, String xml, List< String > parentNodes )
	{
		if( xml != null && parentNodes != null && !parentNodes.isEmpty() )
		{
			Document doc = ConvertTo.Transform.xmlStringToXMLDocument( xml );
		
			Node root = doc.getFirstChild();
			
			Node aux = findNode( root, parentNodes, 0 );
			
			if( aux != null )
			{
				this.addExtraStreamInfoAux( desc, aux.getChildNodes() );
			}
		}
	}
	
	private void addExtraStreamInfoAux( XMLElement el, NodeList nl )
	{
		if( el != null && nl != null )
		{
			for (int i=0; i < nl.getLength(); i++) 
			{
				Node an = nl.item(i);
	
				if( an.getNodeType() == Node.ELEMENT_NODE) 
			    {
					String name = an.getNodeName();
					
					NodeList childNodes = an.getChildNodes();
					if( childNodes.getLength() == 1 )
					{
						String value = an.getTextContent();
						el.append_child_value( name, value);
					}
					else
					{
						this.addExtraStreamInfoAux( el.append_child( name ), childNodes );
					}
			    }
				else
				{
					this.addExtraStreamInfoAux( el.next_sibling(), an.getChildNodes() );
				}
			}
		}
	}
	
	private Node findNode( Node node, List< String > parentNodes, int parentNodeIndex )
	{
		Node res = null;
		
		if( node != null 
				&& parentNodeIndex < parentNodes.size() )
		{
			String parentNode = parentNodes.get( parentNodeIndex );
			
			if( node.getNodeName().toLowerCase().equals( parentNode.toLowerCase() ) )
			{
				parentNodeIndex++;
				
				if( parentNodeIndex >= parentNodes.size() )
				{
					res = node;
				}
				else
				{
					Node child = node.getFirstChild();
					
					res = this.findNode( child, parentNodes, parentNodeIndex );
					while( res == null )
					{
						child = child.getNextSibling();
						if( child != null )
						{
							res = this.findNode( child, parentNodes, parentNodeIndex );
						}
					}
					
				}
			}
		}
			
		return res;
	}
	*/
	
	private void clearInfoLabels( )
	{
		this.getTxtFilePath( ).setText( "" );
		
		this.getTxtStreamName( ).setText( "" );
		this.getTxtStreamName( ).setBorder( (new JTextField()).getBorder() );
		this.getTxtStreamName( ).setEditable( false );
		
		this.getTxtDataType( ).setText( "" );
		this.getTxtNumChannels( ).setText( "" );
		this.getTxtXMLDesc( ).setText( "" );	
	}
	
	/*
	private JButton getBtnMoveUpDataFile( ) 
	{
		if ( btnMoveUpDataFile == null )
		{
			btnMoveUpDataFile = new JButton( "" );
			btnMoveUpDataFile.setBorder( BorderFactory.createRaisedSoftBevelBorder( ) );
						
			try
			{
				btnMoveUpDataFile.setIcon( new ImageIcon( basicPainter2D.paintTriangle( 12, 1, Color.BLACK, Color.GRAY, basicPainter2D.NORTH ) ) );
			}
			catch( Exception e )
			{
				btnMoveUpDataFile.setText( Language.getLocalCaption( Language.UP_TEXT ) );
			}
			
			btnMoveUpDataFile.addActionListener( new ActionListener( ) 
			{				
				@Override
				public void actionPerformed( ActionEvent e ) 
				{
					moveBinaryFile( getTableFileData( ), -1 );
				}
			} );
		}

		return btnMoveUpDataFile;
	}
	*/
	/*
	private JButton getButtonMoveDownDataFile( ) 
	{
		if ( buttonMoveDownDataFile == null )
		{
			buttonMoveDownDataFile = new JButton( "" );
			buttonMoveDownDataFile.setBorder( BorderFactory.createRaisedSoftBevelBorder( ) );
			
			try
			{
				buttonMoveDownDataFile.setIcon( new ImageIcon( basicPainter2D.paintTriangle( 12, 1, Color.BLACK, Color.GRAY, basicPainter2D.SOUTH ) ) );
			}
			catch( Exception e )
			{
				buttonMoveDownDataFile.setText( Language.getLocalCaption( Language.DOWN_TEXT ) );
			}
			
			buttonMoveDownDataFile.addActionListener( new ActionListener( ) 
			{				
				@Override
				public void actionPerformed( ActionEvent e ) 
				{
					moveBinaryFile( getTableFileData( ), 1 );
				}
			} );
		}

		return buttonMoveDownDataFile;
	}
	*/
	/*
	private void moveBinaryFile( JTable list, int relativePos )	
	{
		int indexRow = list.getSelectedRow( );
		int newIndexRow = indexRow + relativePos;

		if ( ( indexRow > -1 ) && 
				( newIndexRow > -1 ) && 
				( newIndexRow < list.getRowCount( ) ) )
		{
			StreamBinaryHeader bh = this.binaryDataFiles.get( indexRow );
			StreamBinaryHeader bh2 = this.binaryDataFiles.get( newIndexRow );
			
			this.binaryDataFiles.set( newIndexRow, bh );
			this.binaryDataFiles.set( indexRow, bh2 );
			
			DefaultTableModel model = ( DefaultTableModel )list.getModel( );
			
			model.moveRow( indexRow, indexRow, newIndexRow );

			list.setRowSelectionInterval( newIndexRow, newIndexRow );
		}
	}
	*/

	private JLabel getLblOutputFormat( ) 
	{
		if ( lblOutputFormat == null )
		{
			lblOutputFormat = new JLabel( Language.getLocalCaption( Language.SETTING_LSL_OUTPUT_FORMAT ) + ":" );
			lblOutputFormat.setFont( new Font( "Tahoma", Font.BOLD, 12 ) );
		}

		return lblOutputFormat;
	}

	private JComboBox< String > getComboBoxOutputFormat( ) 
	{
		if ( fileFormat == null )
		{
			fileFormat = new JComboBox< String >( );
			
			String[] formats = DataFileFormat.getSupportedFileFormat( );
			for( int i = 0; i < formats.length; i++ )
			{
				this.fileFormat.addItem( formats[ i ] );
			}
			
			fileFormat.addItemListener( new ItemListener( ) 
			{				
				@Override
				public void itemStateChanged( ItemEvent e ) 
				{
					if( e.getStateChange( ) == ItemEvent.SELECTED )
					{
						JComboBox< String > cb = ( JComboBox<String> )e.getSource( );
						String f = cb.getSelectedItem( ).toString( );
						outFormat.setParameter( OutputFileFormatParameters.OUT_FILE_FORMAT, f );			
						
						Tuple< Encoder, WarningMessage > tenc = DataFileFormat.getDataFileEncoder( f );
						Encoder enc = tenc.t1;
						
						List< SettingOptions > opts = enc.getSettiongOptions();
						
						getOutputFormatOptsButton().setEnabled( opts != null && !opts.isEmpty() );
					}
				}
			} );
			
			fileFormat.setSelectedItem( this.outFormat.getParameter(OutputFileFormatParameters.OUT_FILE_FORMAT ).getValue() );
		}

		return fileFormat;
	}
	
	private JCheckBox getChbOutputParallelize( ) 
	{
		if ( this.chbParallelize == null )
		{
			this.chbParallelize = new JCheckBox( Language.getLocalCaption( Language.PARALLELIZE_TEXT ) );
			this.chbParallelize.setHorizontalTextPosition( SwingConstants.LEFT);
			
			this.chbParallelize.addItemListener( new ItemListener()
			{	
				@Override
				public void itemStateChanged(ItemEvent arg0) 
				{	
					JCheckBox c = (JCheckBox)arg0.getSource();
					
					outFormat.setParameter( OutputFileFormatParameters.PARALLELIZE, c.isSelected() );
				}
			}); 
			
			this.chbParallelize.setSelected( true );
		}

		return chbParallelize;
	}
	
	
	private JLabel getLblOutputPath( ) 
	{
		if ( lblOutputPath == null )
		{
			lblOutputPath = new JLabel( Language.getLocalCaption( Language.OUTPUT_TEXT ) + ":" );
			lblOutputPath.setFont( new Font( "Tahoma", Font.BOLD, 12 ) );
		}

		return lblOutputPath;
	}

	private JPanel getPanelOutPath( ) 
	{
		if ( panelOutPath == null )
		{
			panelOutPath = new JPanel( );
			panelOutPath.setLayout( new BorderLayout( 0, 0 ) );
			panelOutPath.add( getTxtOutFileFolder( ), BorderLayout.CENTER );
			panelOutPath.add( getBtnOutFolder( ), BorderLayout.EAST );
		}

		return panelOutPath;
	}

	private JTextField getTxtOutFileFolder( ) 
	{
		if ( txtOutFileFolder == null )
		{
			txtOutFileFolder = new JTextField( );			
			txtOutFileFolder.setColumns( 10 );
			
			txtOutFileFolder.getDocument( ).addDocumentListener( new DocumentListener( ) 
			{				
				@Override
				public void removeUpdate( DocumentEvent e ) 
				{
					update( e );					
				}
				
				@Override
				public void insertUpdate( DocumentEvent e ) 
				{
					update( e );
				}
				
				@Override
				public void changedUpdate( DocumentEvent e ) 
				{
					update( e );
				}
				
				private void update( DocumentEvent e )
				{
					try 
					{
						String folder = e.getDocument( ).getText( 0, e.getDocument( ).getLength( ) );
					
						outFormat.setParameter( OutputFileFormatParameters.OUT_FILE_NAME, folder );						
					}
					catch ( BadLocationException e1 ) 
					{
					}
				}
			} );
			
			txtOutFileFolder.setText( this.currentFolderPath );
		}
		
		return txtOutFileFolder;
	}

	private JButton getBtnOutFolder( ) 
	{
		if ( btnOutFolder == null )
		{
			btnOutFolder = new JButton( "" );
			btnOutFolder.setBorder( BorderFactory.createRaisedSoftBevelBorder( ) );
			
			try 
			{
				btnOutFolder.setIcon( GeneralAppIcon.Folder( 20, 16, Color.BLACK, Color.CYAN.darker( ) ) );
			}
			catch ( Exception e ) 
			{
				btnOutFolder.setText( "..." );
			}
			
			btnOutFolder.addActionListener( new ActionListener( ) 
			{				
				@Override
				public void actionPerformed( ActionEvent e ) 
				{
					String[] folder = FileUtils.selectUserFile( "", true, false, JFileChooser.DIRECTORIES_ONLY, null, null, currentFolderPath );
					
					if( folder != null && folder.length > 0 )
					{
						getTxtOutFileFolder( ).setText( folder[ 0 ] );
						
						currentFolderPath = folder[ 0 ];
					}
				}
			} );
		}

		return btnOutFolder;
	}
	
	
	private JCheckBox getChckbxDeleteBinaries() 
	{
		if ( this.chckbxDeleteBinaries == null ) 
		{
			this.chckbxDeleteBinaries = new JCheckBox( Language.getLocalCaption( Language.LSL_DEL_BINS ) );
			this.chckbxDeleteBinaries.addItemListener( new ItemListener()
			{				
				@Override
				public void itemStateChanged( ItemEvent e ) 
				{
					JCheckBox c = (JCheckBox)e.getSource();
					
					boolean del = c.isSelected();
					
					outFormat.setParameter( OutputFileFormatParameters.DELETE_BIN, del );
				}
			});
			
			this.outFormat.setParameter( OutputFileFormatParameters.DELETE_BIN, this.chckbxDeleteBinaries.isSelected() );
		}
		return chckbxDeleteBinaries;
	}		
	
	private JCheckBox getChckboxEncrypt() 
	{
		if ( this.chbxEncrypt == null ) 
		{
			this.chbxEncrypt = new JCheckBox( Language.getLocalCaption( Language.ENCRYPT_KEY_TEXT ) );			
		}
		
		return chbxEncrypt;
	}
}
