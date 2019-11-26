/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.
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
package GUI;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import Auxiliar.Extra.Tuple;
import Config.Language.Language;
import DataStream.StreamHeader;
import DataStream.OutputDataFile.Format.DataFileFormat;
import GUI.Miscellany.GeneralAppIcon;
import GUI.Miscellany.imagenPoligono2D;
import edu.ucsd.sccn.LSL;

import java.awt.Color;
import javax.swing.JTable;
import javax.swing.JLabel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Point;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;

public class dialogConverBin2CLIS extends JDialog 
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
	private JPanel timeStampPanel;
	private JPanel panelBtnAddData;
	private JPanel panelBtnAddTime;
	private JPanel panelFilesInfo;
	private JPanel panelBinInfo;
	private JPanel panelOutPath;
	
	// JTable
	private JTable tableFileData;
	private JTable tableFileTime;
	private JLabel lblBinaryDataFiles;
	private JLabel lblBinaryTimeFiles;
	
	
	// Labels
	private JLabel lblStreamName;
	private JLabel lblDataType;
	private JLabel lblNoChannels;
	private JLabel lblXmlDescr;
	private JLabel lblFile;
	private JLabel lblOutputFormat;
	private JLabel lblOutputPath;
	
	// JTextField
	private JTextField txtStreamName;
	private JTextField txtDataType;
	private JTextField txtNumChannels;
	private JTextField txtXMLDesc;
	private JTextField txtFilePath;
	private JTextField txtOutFileFolder;
		
	// Buttons
	private JButton btnDone;
	private JButton btnCancel;
	private JButton buttonAddData;
	private JButton buttonAddTime;
	private JButton btnMoveUpDataFile;
	private JButton buttonMoveDownDataFile;
	private JButton btnMoveUpTimeFile;
	private JButton buttonMoveDownTimeFile;
	private JButton btnOutFolder;
	
	// Combox
	private JComboBox< String > fileFormat;
	
	// ScrollPanel
	private JScrollPane scrollTableData;
	private JScrollPane scrollTableTime;
	
	
	// Others variables	
	private List< StreamHeader > binaryDataFiles;
	private List< StreamHeader > binaryTimeFiles;
	
	private boolean clearBinaryFiles = true;
	private StreamHeader currentBinFile = null;
	private JCheckBox chckbxDeleteBinaries;
	
	
	/**
	 * Create the dialog.
	 */
	public dialogConverBin2CLIS( Frame owner, boolean modal ) 	
	{
		super( owner, modal );
		
		this.binaryDataFiles = new ArrayList< StreamHeader>( );
		this.binaryTimeFiles = new ArrayList< StreamHeader>( );
		
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
			this.buttonPane.add(getChckbxDeleteBinaries());
			
			this.buttonPane.add( this.getBtnDone( ) );
			this.buttonPane.add( this.getBtnCancel( ) );
		}
		
		return this.buttonPane;
	}
	
	public List< Tuple< StreamHeader, StreamHeader > > getBinaryFiles( )
	{
		List< Tuple<StreamHeader, StreamHeader> > binFiles = new ArrayList< Tuple<StreamHeader, StreamHeader> >( );
		
		if( this.clearBinaryFiles )
	 	{
	 		this.binaryDataFiles.clear( );
	 		this.binaryTimeFiles.clear( );
	 	}
		
		Iterator< StreamHeader > dataIT = binaryDataFiles.iterator( );
		Iterator< StreamHeader > timeIT = binaryTimeFiles.iterator( );
		
		while( dataIT.hasNext( ) && timeIT.hasNext( ) )
		{
			binFiles.add( new Tuple<StreamHeader, StreamHeader>( dataIT.next( ), timeIT.next( ) ) );
		}
		
		while( dataIT.hasNext( ) )
		{
			binFiles.add( new Tuple<StreamHeader, StreamHeader>( dataIT.next( ), null ) );
		}
		
		while( timeIT.hasNext( ) )
		{
			binFiles.add( new Tuple<StreamHeader, StreamHeader>( timeIT.next( ), null ) );
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
						
					/*
					try 
					{
						Robot r = new Robot( );
						r.keyPress( KeyEvent.VK_ESCAPE );
						r.keyRelease( KeyEvent.VK_ESCAPE );
					} 
					catch ( AWTException e1 ) 
					{
					}
					*/
					
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
					
					/*
					try 
					{
						Robot r = new Robot( );
						r.keyPress( KeyEvent.VK_ESCAPE );
						r.keyRelease( KeyEvent.VK_ESCAPE );
					} 
					catch ( AWTException e1 ) 
					{
					}
					*/
					
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
			filesPanel.setLayout( new BoxLayout( filesPanel, BoxLayout.X_AXIS ) );
			filesPanel.add( getDataFilePanel( ) );
			filesPanel.add( getTimeStampPanel( ) );
		}

		return filesPanel;
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
	
	private JPanel getTimeStampPanel( ) 
	{
		if ( timeStampPanel == null )
		{
			timeStampPanel = new JPanel( );
			timeStampPanel.setForeground( Color.BLACK );
			timeStampPanel.setBorder( new EmptyBorder( 0, 2, 0, 0 ) );
			timeStampPanel.setLayout( new BorderLayout( 0, 0 ) );
			timeStampPanel.add( getPanelBtnAddTime( ), BorderLayout.NORTH );
			timeStampPanel.add( getScrollTableTime( ), BorderLayout.CENTER );
		}

		return timeStampPanel;
	}
	
	private JScrollPane getScrollTableTime()
	{
		if( this.scrollTableTime == null )
		{
			this.scrollTableTime = new JScrollPane( this.getTableFileTime( ) );
		}
		
		return this.scrollTableTime;
	}

	private JPanel getPanelBtnAddData( ) 
	{
		if ( panelBtnAddData == null )
		{
			panelBtnAddData = new JPanel( );
			FlowLayout flowLayout = ( FlowLayout ) panelBtnAddData.getLayout( );
			flowLayout.setAlignment( FlowLayout.LEFT );
			panelBtnAddData.add( getBtnMoveUpDataFile( ) );
			panelBtnAddData.add( getButtonMoveDownDataFile( ) );
			panelBtnAddData.add( getLblBinaryDataFiles( ) );
			panelBtnAddData.add( getButtonAddData( ) );
		}

		return panelBtnAddData;
	}

	private JPanel getPanelBtnAddTime( ) 
	{
		if ( panelBtnAddTime == null )
		{
			panelBtnAddTime = new JPanel( );
			FlowLayout flowLayout = ( FlowLayout ) panelBtnAddTime.getLayout( );
			flowLayout.setAlignment( FlowLayout.LEFT );
			panelBtnAddTime.add( this.getBtnMoveUpTimeFile( ) );
			panelBtnAddTime.add( this.getButtonMoveDownTimeFile( ) );
			panelBtnAddTime.add( getLblBinaryTimeFiles( ) );
			panelBtnAddTime.add( getButtonAddTime( ) );
		}

		return panelBtnAddTime;
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
					
					String[] FILES = guiManager.getInstance( ).selectUserFile( "", true, true, JFileChooser.FILES_ONLY, null, null );
					if( FILES != null )
					{
						for( String file : FILES )
						{
							insertBinaryFiles( getTableFileData( ), file );
							
							StreamHeader bh = getBinaryFileInfo( file );
							if( bh != null )
							{
								binaryDataFiles.add( bh );
							}
						}
					}
				}
			} );
			
			buttonAddData.setFont( new Font( "Tahoma", Font.BOLD, 11 ) );
			buttonAddData.setBorder( BorderFactory.createRaisedSoftBevelBorder( ) );
		}

		return buttonAddData;
	}

	private JButton getButtonAddTime( ) 
	{
		if ( buttonAddTime == null )
		{
			buttonAddTime = new JButton( );
			
			try
			{
				buttonAddTime.setIcon( GeneralAppIcon.Folder( 20, 16, Color.BLACK, Color.ORANGE ) );
			}
			catch ( Exception e ) 
			{
				buttonAddTime.setText(  Language.getLocalCaption( Language.SELECT_TEXT ) );
			}
			
			buttonAddTime.setFont( new Font( "Tahoma", Font.BOLD, 11 ) );
			buttonAddTime.setBorder( BorderFactory.createRaisedSoftBevelBorder( ) );
			
			buttonAddTime.addActionListener( new ActionListener( ) 
			{
				public void actionPerformed( ActionEvent e ) 
				{
					clearBinaryFiles( getTableFileTime( ), binaryTimeFiles );
					
					String[] FILES = guiManager.getInstance( ).selectUserFile( "", true, true, JFileChooser.FILES_ONLY, null, null );
					if( FILES != null )
					{
						for( String file : FILES )
						{
							insertBinaryFiles( getTableFileTime( ), file );
							StreamHeader bh = getBinaryFileInfo( file );
							if( bh != null )
							{
								binaryTimeFiles.add( bh );
							}
						}
					}
				}
			} );
		}

		return buttonAddTime;
	}

	private void clearBinaryFiles( JTable t, List< StreamHeader > binaryFiles )	
	{
		this.clearInfoLabels( );
			
		binaryFiles.clear( );

		DefaultTableModel dm = ( DefaultTableModel )t.getModel( );
		while( dm.getRowCount( ) > 0 )
		{
			dm.removeRow( dm.getRowCount( ) - 1 );
		}
	}
	
	private JTable getTableFileTime( ) 
	{
		if ( tableFileTime == null ) 
		{
			this.tableFileTime = this.getCreateJTable( );
			this.tableFileTime.setModel( this.createBinFileTable( ) );
			
			this.tableFileTime.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
			
			this.tableFileTime.setPreferredScrollableViewportSize( this.tableFileTime.getPreferredSize( ) );
			this.tableFileTime.setFillsViewportHeight( true );
			
			this.tableFileTime.addFocusListener( new FocusListener( ) 
			{				
				@Override
				public void focusLost( FocusEvent e ) 
				{	
				}
				
				@Override
				public void focusGained( FocusEvent e ) 
				{
					JTable t = getTableFileData( );
					t.clearSelection( );
					clearInfoLabels( );
				}
			} );
			
			this.tableFileTime.getSelectionModel( ).addListSelectionListener( new ListSelectionListener( ) 
			{				
				@Override
				public void valueChanged( ListSelectionEvent e ) 
				{
					// TODO Auto-generated method stub
					ListSelectionModel md = ( ListSelectionModel )e.getSource( );
					if( !md.isSelectionEmpty( ) && !e.getValueIsAdjusting( ) )
					{
						//int c = tableFileTime.getSelectedColumn( );
						int r = tableFileTime.getSelectedRow( );
						
						if( r < binaryTimeFiles.size( ) )
						{
							currentBinFile = binaryTimeFiles.get( r );
							showBinaryFileInfo( currentBinFile );
						}
						/*
						if( c >= 0 && r >= 0 )
						{
							String file = tableFileTime.getValueAt( r, c ).toString( );
							getBinaryFileInfo( file );
						}
						*/
					}
				}
			} );		
		}

		return tableFileTime;
	}

	private JLabel getLblBinaryDataFiles( ) 
	{
		if ( lblBinaryDataFiles == null )
		{
			lblBinaryDataFiles = new JLabel(  Language.getLocalCaption( Language.LSL_BIN_DATA_FILES )  );
		}

		return lblBinaryDataFiles;
	}

	private JLabel getLblBinaryTimeFiles( ) 
	{
		if ( lblBinaryTimeFiles == null )
		{
			lblBinaryTimeFiles = new JLabel(  Language.getLocalCaption( Language.LSL_BIN_TIME_FILES )  );
		}

		return lblBinaryTimeFiles;
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
			panelBinInfo = new JPanel( );
			panelBinInfo.setBorder( new EmptyBorder( 4, 0, 0, 0 ) );
			
			GridBagLayout gbl_panelBinInfo = new GridBagLayout( );
			gbl_panelBinInfo.columnWidths = new int[]{0, 0, 0};
			gbl_panelBinInfo.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
			gbl_panelBinInfo.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
			gbl_panelBinInfo.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
			panelBinInfo.setLayout( gbl_panelBinInfo );
			
			GridBagConstraints gbc_lblFile = new GridBagConstraints( );
			gbc_lblFile.anchor = GridBagConstraints.EAST;
			gbc_lblFile.insets = new Insets( 0, 0, 5, 5 );
			gbc_lblFile.gridx = 0;
			gbc_lblFile.gridy = 0;
			panelBinInfo.add( getLblFile( ), gbc_lblFile );
			
			GridBagConstraints gbc_txtFilePath = new GridBagConstraints( );
			gbc_txtFilePath.insets = new Insets( 0, 0, 5, 0 );
			gbc_txtFilePath.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtFilePath.gridx = 1;
			gbc_txtFilePath.gridy = 0;
			panelBinInfo.add( getTxtFilePath( ), gbc_txtFilePath );
			
			GridBagConstraints gbc_lblStreamName = new GridBagConstraints( );
			gbc_lblStreamName.anchor = GridBagConstraints.EAST;
			gbc_lblStreamName.insets = new Insets( 0, 0, 5, 5 );
			gbc_lblStreamName.gridx = 0;
			gbc_lblStreamName.gridy = 1;
			panelBinInfo.add( getLblStreamName( ), gbc_lblStreamName );
			
			GridBagConstraints gbc_txtStreamName = new GridBagConstraints( );
			gbc_txtStreamName.insets = new Insets( 0, 0, 5, 0 );
			gbc_txtStreamName.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtStreamName.gridx = 1;
			gbc_txtStreamName.gridy = 1;
			panelBinInfo.add( getTxtStreamName( ), gbc_txtStreamName );
			
			GridBagConstraints gbc_lblDataType = new GridBagConstraints( );
			gbc_lblDataType.insets = new Insets( 0, 0, 5, 5 );
			gbc_lblDataType.anchor = GridBagConstraints.EAST;
			gbc_lblDataType.gridx = 0;
			gbc_lblDataType.gridy = 2;
			panelBinInfo.add( getLblDataType( ), gbc_lblDataType );
			
			GridBagConstraints gbc_txtDataType = new GridBagConstraints( );
			gbc_txtDataType.insets = new Insets( 0, 0, 5, 0 );
			gbc_txtDataType.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtDataType.gridx = 1;
			gbc_txtDataType.gridy = 2;
			panelBinInfo.add( getTxtDataType( ), gbc_txtDataType );
			
			GridBagConstraints gbc_lblNoChannels = new GridBagConstraints( );
			gbc_lblNoChannels.insets = new Insets( 0, 0, 5, 5 );
			gbc_lblNoChannels.anchor = GridBagConstraints.EAST;
			gbc_lblNoChannels.gridx = 0;
			gbc_lblNoChannels.gridy = 3;
			panelBinInfo.add( getLblNoChannels( ), gbc_lblNoChannels );
			
			GridBagConstraints gbc_txtNumChannels = new GridBagConstraints( );
			gbc_txtNumChannels.insets = new Insets( 0, 0, 5, 0 );
			gbc_txtNumChannels.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtNumChannels.gridx = 1;
			gbc_txtNumChannels.gridy = 3;
			panelBinInfo.add( getTxtNumChannels( ), gbc_txtNumChannels );
			
			GridBagConstraints gbc_lblXmlDescr = new GridBagConstraints( );
			gbc_lblXmlDescr.insets = new Insets( 0, 0, 5, 5 );
			gbc_lblXmlDescr.anchor = GridBagConstraints.EAST;
			gbc_lblXmlDescr.gridx = 0;
			gbc_lblXmlDescr.gridy = 4;
			panelBinInfo.add( getLblXmlDescr( ), gbc_lblXmlDescr );
			
			GridBagConstraints gbc_txtXMLDesc = new GridBagConstraints( );
			gbc_txtXMLDesc.insets = new Insets( 0, 0, 5, 0 );
			gbc_txtXMLDesc.fill = GridBagConstraints.HORIZONTAL;
			gbc_txtXMLDesc.gridx = 1;
			gbc_txtXMLDesc.gridy = 4;
			panelBinInfo.add( getTxtXMLDesc( ), gbc_txtXMLDesc );
			
			GridBagConstraints gbc_lblOutputFormat = new GridBagConstraints( );
			gbc_lblOutputFormat.anchor = GridBagConstraints.EAST;
			gbc_lblOutputFormat.insets = new Insets( 0, 0, 5, 5 );
			gbc_lblOutputFormat.gridx = 0;
			gbc_lblOutputFormat.gridy = 5;
			panelBinInfo.add( getLblOutputFormat( ), gbc_lblOutputFormat );
			
			GridBagConstraints gbc_comboBox = new GridBagConstraints( );
			gbc_comboBox.insets = new Insets( 0, 0, 5, 0 );
			gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
			gbc_comboBox.gridx = 1;
			gbc_comboBox.gridy = 5;
			panelBinInfo.add( getComboBoxOutputFormat( ), gbc_comboBox );
			
			GridBagConstraints gbc_lblOutputPath = new GridBagConstraints( );
			gbc_lblOutputPath.anchor = GridBagConstraints.EAST;
			gbc_lblOutputPath.insets = new Insets( 0, 0, 0, 5 );
			gbc_lblOutputPath.gridx = 0;
			gbc_lblOutputPath.gridy = 6;
			panelBinInfo.add( getLblOutputPath( ), gbc_lblOutputPath );
			
			GridBagConstraints gbc_panelOutPath = new GridBagConstraints( );
			gbc_panelOutPath.fill = GridBagConstraints.BOTH;
			gbc_panelOutPath.gridx = 1;
			gbc_panelOutPath.gridy = 6;
			panelBinInfo.add( getPanelOutPath( ), gbc_panelOutPath );
		}

		return panelBinInfo;
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
								currentBinFile.setName( nm );
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
			
			this.tableFileData.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
			
			this.tableFileData.setPreferredScrollableViewportSize( this.tableFileData.getPreferredSize( ) );
			this.tableFileData.setFillsViewportHeight( true );
			
			this.tableFileData.addFocusListener( new FocusListener( ) 
			{				
				@Override
				public void focusLost( FocusEvent e ) 
				{
				}
				
				@Override
				public void focusGained( FocusEvent e ) 
				{
					JTable t = getTableFileTime( );
					t.clearSelection( );
					clearInfoLabels( );
				}
			} );
			
			this.tableFileData.getSelectionModel( ).addListSelectionListener( new ListSelectionListener( ) 
			{				
				@Override
				public void valueChanged( ListSelectionEvent e ) 
				{
					// TODO Auto-generated method stub
					
					ListSelectionModel md = ( ListSelectionModel )e.getSource( );
					if( !md.isSelectionEmpty( ) && !e.getValueIsAdjusting( ) )
					{
						//int c = tableFileData.getSelectedColumn( );
						int r = tableFileData.getSelectedRow( );
						
						if( r < binaryDataFiles.size( ) )
						{
							currentBinFile = binaryDataFiles.get( r );
							showBinaryFileInfo( currentBinFile );
						}
						
						/*
						if( c >= 0 && r >= 0 )
						{
							String file = tableFileData.getValueAt( r, c ).toString( );
							getBinaryFileInfo( file );
						}
						*/
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
		
		return t;
	}
	
	private TableModel createBinFileTable( )
	{					
		TableModel tm = new DefaultTableModel( null, new String[] { Language.getLocalCaption( Language.MENU_FILE ) } )
						{
							private static final long serialVersionUID = 1L;
								
							Class[] columnTypes = new Class[]{ String.class };								
							boolean[] columnEditables = new boolean[] { false };
								
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
	
	private void insertBinaryFiles( JTable t, String file )
	{				
		Object[] vals = new Object[ t.getColumnCount( ) ];
		
		for( int i = 0; i < vals.length; i++ )
		{
			vals[ i ] = file;
		}
		
		DefaultTableModel m = ( DefaultTableModel )t.getModel( );
		m.addRow( vals );
	}

	private void showBinaryFileInfo( StreamHeader header )	
	{
		this.clearInfoLabels( );		
	
		if( header != null )
		{
			this.getTxtFilePath( ).setText( header.getFilePath( ) );
			this.getTxtStreamName( ).setText( header.getName( ) );
			this.getTxtStreamName( ).setEditable( true );
			
			int type = header.getType( ) - 1;
			String t = type + "";
			
			Field[] fields = LSL.ChannelFormat.class.getDeclaredFields( );
			
			if( type >= 0 && type < fields.length - 2 )
			{
				t = fields[ type ].getName( );
			}		
			else if( type < 0 )
			{
				int p = fields.length - 2;
				if( p >= 0 )
				{
					t = fields[ p ].getName();
				}
				else
				{
					t = "undefined";
				}
			}
			
			this.getTxtDataType( ).setText( t );	
			this.getTxtNumChannels( ).setText( header.getNumberOfChannels( ) + "" );
			this.getTxtXMLDesc( ).setText( header.getXMLDescription( ) );
			this.getComboBoxOutputFormat( ).setSelectedItem( header.getOutputFormat( ) );
		}
	}
		
	private StreamHeader getBinaryFileInfo( String file )	
	{
		this.clearInfoLabels( );		
		
		StreamHeader bH = null;
		
		BufferedReader reader = null;
		
		try 
		{
			reader = new BufferedReader( new FileReader( new File( file ) ) );
			
			String binHeader = reader.readLine( );
		
			String binSplitChar = ";" ;
			
			String[] parts = binHeader.split( binSplitChar );
					
			String name = "", type = "", timeType = "", chs = "", chunck = "", xml = "", interleave = "";
						
			for( int i = 0; i < parts.length; i++ )
			{
				if( i == 0 )
				{
					name = parts[ i ];
				}
				else if( i == 1 )
				{
					type = parts[ i ];
				}
				else if( i == 2 )
				{
					timeType = parts[ i ];
				}
				else if( i == 3 )
				{
					chs = parts[ i ];
				}
				else if( i == 4 )
				{
					chunck = parts[ i ];
				}
				else if( i == 5 )
				{
					interleave = parts[ i ];
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
			
			bH = new StreamHeader( file, name
									, new Integer( type )
									, new Integer( timeType )
									, new Integer( chs )
									, new Integer( chunck )
									, new Boolean( interleave )
									, xml
									, this.getComboBoxOutputFormat( ).getSelectedItem( ).toString( )
									, this.getTxtOutFileFolder( ).getText( )
									, this.getChckbxDeleteBinaries().isSelected() );
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
	
	private JButton getBtnMoveUpDataFile( ) 
	{
		if ( btnMoveUpDataFile == null )
		{
			btnMoveUpDataFile = new JButton( "" );
			btnMoveUpDataFile.setBorder( BorderFactory.createRaisedSoftBevelBorder( ) );
						
			try
			{
				btnMoveUpDataFile.setIcon( new ImageIcon( imagenPoligono2D.crearImagenTriangulo( 12, 1, Color.BLACK, Color.GRAY, imagenPoligono2D.NORTH ) ) );
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

	private JButton getButtonMoveDownDataFile( ) 
	{
		if ( buttonMoveDownDataFile == null )
		{
			buttonMoveDownDataFile = new JButton( "" );
			buttonMoveDownDataFile.setBorder( BorderFactory.createRaisedSoftBevelBorder( ) );
			
			try
			{
				buttonMoveDownDataFile.setIcon( new ImageIcon( imagenPoligono2D.crearImagenTriangulo( 12, 1, Color.BLACK, Color.GRAY, imagenPoligono2D.SOUTH ) ) );
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
	
	private JButton getBtnMoveUpTimeFile( ) 
	{
		if ( btnMoveUpTimeFile == null )
		{
			btnMoveUpTimeFile = new JButton( "" );
			btnMoveUpTimeFile.setBorder( BorderFactory.createRaisedSoftBevelBorder( ) );
						
			try
			{
				btnMoveUpTimeFile.setIcon( new ImageIcon( imagenPoligono2D.crearImagenTriangulo( 12, 1, Color.BLACK, Color.GRAY, imagenPoligono2D.NORTH ) ) );
			}
			catch( Exception e )
			{
				btnMoveUpTimeFile.setText( Language.getLocalCaption( Language.UP_TEXT ) );
			}
			
			btnMoveUpTimeFile.addActionListener( new ActionListener( ) 
			{				
				@Override
				public void actionPerformed( ActionEvent e ) 
				{
					moveBinaryFile( getTableFileTime( ), -1 );
				}
			} );
		}

		return btnMoveUpTimeFile;
	}

	private JButton getButtonMoveDownTimeFile( ) 
	{
		if ( buttonMoveDownTimeFile == null )
		{
			buttonMoveDownTimeFile = new JButton( "" );
			buttonMoveDownTimeFile.setBorder( BorderFactory.createRaisedSoftBevelBorder( ) );
			
			try
			{
				buttonMoveDownTimeFile.setIcon( new ImageIcon( imagenPoligono2D.crearImagenTriangulo( 12, 1, Color.BLACK, Color.GRAY, imagenPoligono2D.SOUTH ) ) );
			}
			catch( Exception e )
			{
				buttonMoveDownTimeFile.setText( Language.getLocalCaption( Language.DOWN_TEXT ) );
			}
			
			buttonMoveDownTimeFile.addActionListener( new ActionListener( ) 
			{				
				@Override
				public void actionPerformed( ActionEvent e ) 
				{
					moveBinaryFile( getTableFileTime( ), 1 );
				}
			} );
		}

		return buttonMoveDownTimeFile;
	}
	
	private void moveBinaryFile( JTable list, int relativePos )	
	{
		int indexRow = list.getSelectedRow( );
		int newIndexRow = indexRow + relativePos;

		if ( ( indexRow > -1 ) && 
				( newIndexRow > -1 ) && 
				( newIndexRow < list.getRowCount( ) ) )
		{
			StreamHeader bh = this.binaryDataFiles.get( indexRow );
			StreamHeader bh2 = this.binaryDataFiles.get( newIndexRow );
			
			this.binaryDataFiles.set( newIndexRow, bh );
			this.binaryDataFiles.set( indexRow, bh2 );
			
			DefaultTableModel model = ( DefaultTableModel )list.getModel( );
			
			model.moveRow( indexRow, indexRow, newIndexRow );

			list.setRowSelectionInterval( newIndexRow, newIndexRow );
		}
	}

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
						for( StreamHeader bH : binaryDataFiles )
						{
							bH.setOutputFormat( f );
						}
						
						for( StreamHeader bH : binaryTimeFiles )
						{
							bH.setOutputFormat( f );
						}
					}
				}
			} );
		}

		return fileFormat;
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
			txtOutFileFolder.setText( "./" );
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
						
						for( StreamHeader bh : binaryDataFiles )
						{
							bh.setOutputFolder( folder );
						}
						
						for( StreamHeader bh : binaryTimeFiles )
						{
							bh.setOutputFolder( folder );
						}
					}
					catch ( BadLocationException e1 ) 
					{
					}
				}
			} );
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
					String[] folder = guiManager.getInstance( ).selectUserFile( "", true, false, JFileChooser.DIRECTORIES_ONLY, null, null );
					
					if( folder != null && folder.length > 0 )
					{
						getTxtOutFileFolder( ).setText( folder[ 0 ] );
					}
				}
			} );
		}

		return btnOutFolder;
	}
	

	private JCheckBox getChckbxDeleteBinaries() 
	{
		if ( chckbxDeleteBinaries == null ) 
		{
			chckbxDeleteBinaries = new JCheckBox( Language.getLocalCaption( Language.LSL_DEL_BINS ) );
			this.chckbxDeleteBinaries.addItemListener( new ItemListener()
			{				
				@Override
				public void itemStateChanged( ItemEvent e ) 
				{
					JCheckBox c = (JCheckBox)e.getSource();
					
					boolean del = c.isSelected();
					
					for( StreamHeader bh : binaryDataFiles )
					{
						bh.setDeleteBinary( del );
					}
					
					for( StreamHeader bh : binaryTimeFiles )
					{
						bh.setDeleteBinary( del );
					}
				}
			});
		}
		return chckbxDeleteBinaries;
	}		
}
