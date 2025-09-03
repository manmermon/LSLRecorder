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
import javax.swing.border.EmptyBorder;
import javax.swing.BorderFactory;

import lslrec.auxiliar.extra.FileUtils;
import lslrec.config.language.Language;
import lslrec.dataStream.convertData.clis.ClisData;
import lslrec.dataStream.convertData.clis.MetadataVariableBlock;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.dataStream.sync.SyncMarker;
import lslrec.dataStream.sync.SyncMarkerCollectorWriter;
import lslrec.gui.miscellany.GeneralAppIcon;

import java.awt.Color;

import javax.swing.JLabel;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;

public class Dialog_ExportSyncMarkFromClisFile extends JDialog 
{	
	private static final long serialVersionUID = 9160005973731840486L;
	
	// JPanel
	private JPanel contentPanel;
	private JPanel buttonPane;	
	private JPanel filesPanel;
	private JPanel dataFilePanel;
	private JPanel panelBtnAddData;
	private JPanel panelFilesInfo;
	private JPanel panelSelVars;
	
	// JTable
	//private JTable tableFileData;
	
	//JTextField
	private JTextField clisFile;
			
	// Buttons
	private JButton btnDone;
	private JButton btnCancel;
	private JButton buttonAddData;
	
	private JComboBox< String > markVar;
	private JComboBox< Integer > markCol;
	private JComboBox< String > timeVar;
	private JComboBox< Integer > timeCol;
				
	// Others variables
	private String currentFolderPath = System.getProperty( "user.dir" );
	
	private ClisData clisDataFile;	
	private String syncOutFileName = null;
			
	public static void main(String[] args) 
	{
		try 
		{
			Dialog_ExportSyncMarkFromClisFile dialog = new Dialog_ExportSyncMarkFromClisFile( null, true, "D:\\Nextcloud\\WorkSpace\\GitHub\\LSLRecorder\\records\\data_Simulation.clis" );
			dialog.setBounds( 100, 100, 450, 210 );
			dialog.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );			
			dialog.setVisible(true);
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

 	public Dialog_ExportSyncMarkFromClisFile( Frame owner, boolean modal, String file ) 	
	{
		super( owner, modal );
				
		super.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
		
		JRootPane root = this.getRootPane( );

		ActionListener escListener = new ActionListener( ) 
		{
			@Override
			 public void actionPerformed( ActionEvent e ) 
			 {
				if( clisDataFile != null )
				{
					try 
					{
						clisDataFile.close();
					} 
					catch (IOException e1)
					{					
					}
					finally 
					{
						clisDataFile = null;
					}
				}
				
			 	dispose( );
			 }
		};
		
		root.registerKeyboardAction( escListener, KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ), JComponent.WHEN_IN_FOCUSED_WINDOW );
		
		super.setContentPane( this.getMainPanel( ) );
	
		if (file != null )
		{	
			loadClisFile2TxtField( file );
			
			getButtonAddData().setVisible( this.clisDataFile == null );
		}
	}
 	
 	public String getSyncFile()
 	{
 		return this.syncOutFileName;
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
			
			this.buttonPane.add( this.getBtnDone( ) );
			this.buttonPane.add( this.getBtnCancel( ) );
		}
		
		return this.buttonPane;
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
					if( clisDataFile != null )
					{	
						try 
						{
							SyncMarkerCollectorWriter syncCollectorWriter = new SyncMarkerCollectorWriter( getJTxClisFile().getText() );
							
							syncCollectorWriter.startThread();
							
							Map< String, Number[][] > variableData = clisDataFile.importAllData();
							Number[][] data = variableData.get( getCbMarkVariable().getSelectedItem().toString() );
							Number[][] time = variableData.get( getCbTimeVariable ().getSelectedItem().toString() );
							
							int markIndex = getCbMarkColumn().getSelectedIndex();
							int timeIndex = getCbTimeColumn().getSelectedIndex();
							
							for( int i = 0; i < data.length && i < time.length; i++ )
							{
								Number markValue = data[ i ][ markIndex ];
								Number timeValue = time[ i ][ timeIndex ];
								
								if( markValue.intValue() != SyncMarker.NON_MARK )
								{
									SyncMarker mark = new SyncMarker( markValue.intValue(), timeValue.doubleValue() );
									syncCollectorWriter.SaveSyncMarker( mark );
								}
							}
							
							syncCollectorWriter.stopThread( syncCollectorWriter.FORCE_STOP );

							syncOutFileName = syncCollectorWriter.getOutputFileName();
							
							clisDataFile.close();
						} 
						catch (Exception e1) 
						{
						}
					}
					
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
					if( clisDataFile != null )
					{
						try 
						{
							clisDataFile.close();
						} 
						catch (IOException e1)
						{					
						}
						finally 
						{
							clisDataFile = null;
						}
					}
					
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
		}

		return filesPanel;
	}
	
	private JPanel getSelVarPanel( ) 
	{
		if ( this.panelSelVars == null ) 
		{
			panelSelVars = new JPanel( );
			panelSelVars.setLayout( new BorderLayout() );
			
			GridBagLayout gbl_panelBinInfo = new GridBagLayout( );
			gbl_panelBinInfo.columnWidths = new int[]{0, 0, 0};
			gbl_panelBinInfo.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
			gbl_panelBinInfo.columnWeights = new double[]{0.0, 0.75, 0.0, 0.25, Double.MIN_VALUE};
			gbl_panelBinInfo.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
			panelSelVars.setLayout( gbl_panelBinInfo );
			
			int colPadding = 0;
			int COLS = 4;
			
			GridBagConstraints gbc = new GridBagConstraints( );
			gbc.anchor = GridBagConstraints.EAST;
			gbc.insets = new Insets( 0, 0, 5, 5 );
			gbc.gridx = 0;
			gbc.gridy = ( panelSelVars.getComponentCount()  + colPadding ) / COLS;
			
			JLabel lbSelVar = new JLabel( "Mark variable" );
			panelSelVars.add( lbSelVar, gbc );
			
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 1;
			gbc.gridy = ( panelSelVars.getComponentCount()  + colPadding ) / COLS;
			panelSelVars.add( this.getCbMarkVariable() , gbc );
			
			gbc = new GridBagConstraints( );
			gbc.anchor = GridBagConstraints.EAST;
			gbc.insets = new Insets( 0, 0, 5, 5 );
			gbc.gridx = 2;
			gbc.gridy = ( panelSelVars.getComponentCount()  + colPadding ) / COLS;
			JLabel lbSelCol = new JLabel( "Mark column" );
			panelSelVars.add( lbSelCol, gbc );
			
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 3;
			gbc.gridy = ( panelSelVars.getComponentCount()  + colPadding ) / COLS;
			panelSelVars.add( this.getCbMarkColumn() , gbc );
						
			gbc = new GridBagConstraints( );
			gbc.anchor = GridBagConstraints.EAST;
			gbc.insets = new Insets( 0, 0, 5, 5 );
			gbc.gridx = 0;
			gbc.gridy = ( panelSelVars.getComponentCount()  + colPadding ) / COLS;
			
			JLabel lbTimeVar = new JLabel( "Time variable" );
			panelSelVars.add( lbTimeVar, gbc );
			
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 1;
			gbc.gridy = ( panelSelVars.getComponentCount()  + colPadding ) / COLS;
			panelSelVars.add( this.getCbTimeVariable(), gbc );
			
			gbc = new GridBagConstraints( );
			gbc.anchor = GridBagConstraints.EAST;
			gbc.insets = new Insets( 0, 0, 5, 5 );
			gbc.gridx = 2;
			gbc.gridy = ( panelSelVars.getComponentCount()  + colPadding ) / COLS;
			JLabel lbSelTimeCol = new JLabel( "Time column" );
			panelSelVars.add( lbSelTimeCol, gbc );
			
			gbc.fill = GridBagConstraints.HORIZONTAL;
			gbc.gridx = 3;
			gbc.gridy = ( panelSelVars.getComponentCount()  + colPadding ) / COLS;
			panelSelVars.add( this.getCbTimeColumn() , gbc );
		}

		return panelSelVars;
	}
	
	private JComboBox< String > getCbMarkVariable()
	{
		if( this.markVar == null )
		{
			this.markVar = new  JComboBox<String>();
			
			this.markVar.addItemListener( new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					getCbMarkColumn().removeAllItems();
					
					if( clisDataFile != null )
					{
						int selIndex = markVar.getSelectedIndex();
						if( selIndex >= 0 )
						{
							MetadataVariableBlock var = clisDataFile.getVarInfo().get( selIndex );
							
							for( int i = 1; i <= var.getCols(); i++ )
							{
								getCbMarkColumn().addItem( i );
							}
							
							getCbMarkColumn().setSelectedIndex( var.getCols() - 1 );
						}
					}
				}
			});
		}
		return this.markVar;
	}
	
	private JComboBox< Integer > getCbMarkColumn()
	{
		if( this.markCol == null )
		{
			this.markCol = new  JComboBox< Integer >();
		}
		return this.markCol;
	}
	
	private JComboBox< String > getCbTimeVariable()
	{
		if( this.timeVar == null )
		{
			this.timeVar = new  JComboBox<String>();
			
			this.timeVar.addItemListener( new ItemListener() 
			{
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					getCbTimeColumn().removeAllItems();
					
					if( clisDataFile != null )
					{
						int selIndex = timeVar.getSelectedIndex();
						if( selIndex >= 0 )
						{
							MetadataVariableBlock var = clisDataFile.getVarInfo().get( selIndex );
							
							for( int i = 1; i <= var.getCols(); i++ )
							{
								getCbTimeColumn().addItem( i );
							}
							
							getCbTimeColumn().setSelectedIndex( var.getCols() - 1 );
						}
					}
				}
			});
		}
		return this.timeVar;
	}

	private JComboBox< Integer > getCbTimeColumn()
	{
		if( this.timeCol == null )
		{
			this.timeCol = new  JComboBox< Integer >();
		}
		return this.timeCol;
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
			//dataFilePanel.add( this.getScrollTableData(), BorderLayout.CENTER );
			dataFilePanel.add( new JLabel( Language.getLocalCaption( Language.FILENAME_TEXT ) ), BorderLayout.WEST );
			dataFilePanel.add( this.getJTxClisFile(), BorderLayout.CENTER );
		}
		
		return dataFilePanel;
	}

	/*
	private JScrollPane getScrollTableData()
	{
		if( this.scrollTableData == null )
		{
			this.scrollTableData = new JScrollPane( getTableFileData( ) );
		}
		
		return this.scrollTableData;
	}
	//*/
	
	private JTextField getJTxClisFile()
	{
		if( this.clisFile == null )
		{
			this.clisFile = new JTextField();
			this.clisFile.setEditable( false );
			this.clisFile.setColumns( 10 );
		}
		
		return this.clisFile;
	}
	
	private JPanel getPanelBtnAddData( ) 
	{
		if ( panelBtnAddData == null )
		{
			panelBtnAddData = new JPanel( new BorderLayout() );
			
			JPanel aux = new JPanel();
			FlowLayout flowLayout = ( FlowLayout ) aux.getLayout( );
			flowLayout.setAlignment( FlowLayout.LEFT );
			aux.add( this.getButtonAddData( ) );
			
			panelBtnAddData.add( aux, BorderLayout.CENTER );
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
					Map< String, String > exts = DataFileFormat.getSupportedFileExtension();
					
					String clisExt = exts.get( DataFileFormat.CLIS );
					int firstDot = clisExt.indexOf( "." );
					clisExt = ( firstDot > -1 ) ? clisExt.substring( firstDot + 1 ) : clisExt;
					
					String[] FILES = FileUtils.selectUserFile( "", true, false, JFileChooser.FILES_ONLY, DataFileFormat.CLIS, new String[] { clisExt }, currentFolderPath );
					if( FILES != null )
					{
						loadClisFile2TxtField( FILES[ 0 ] );
						
						currentFolderPath = (new File( FILES[ 0 ] ) ).getAbsolutePath();
					}
				}
			} );
			
			buttonAddData.setFont( new Font( "Tahoma", Font.BOLD, 11 ) );
			buttonAddData.setBorder( BorderFactory.createRaisedSoftBevelBorder( ) );
		}

		return buttonAddData;
	}
	
	private void loadClisFile2TxtField( String file )
	{
		if( file != null )
		{
			try
			{
				if( this.clisDataFile != null )
				{
					this.clisDataFile.close();
				}
			
				this.clisDataFile = new ClisData( file );
				
				JTextField t = getJTxClisFile();
				t.setText( file );
				t.setToolTipText( file );
			} 
			catch (Exception e) 
			{					
			}
			
			showSelectedVars();
		}
	}
	
	/*
	private void insertBinaryFilesInTable( JTable t, String file )
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
			}			
		}
		
		DefaultTableModel m = ( DefaultTableModel )t.getModel( );
		m.addRow( vals );
	}
	//*/
	
	private JPanel getPanelFilesInfo( ) 
	{
		if ( panelFilesInfo == null )
		{
			panelFilesInfo = new JPanel( );
			panelFilesInfo.setLayout( new BorderLayout() );
			
			panelFilesInfo.add( this.getFilesPanel( ), BorderLayout.NORTH );
			
			JScrollPane sp = new JScrollPane(this.getSelVarPanel()
												, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
												, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
			panelFilesInfo.add( sp, BorderLayout.CENTER );
		}

		return panelFilesInfo;
	}

	/*
	private JTable getTableFileData( )
	{
		if( this.tableFileData == null )
		{	
			this.tableFileData = this.getCreateJTable( );
			this.tableFileData.setModel( this.createBinFileTable( ) );
			
			this.tableFileData.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
			
			this.tableFileData.getModel().addTableModelListener( new TableModelListener() 
			{				
				@Override
				public void tableChanged(TableModelEvent e) 
				{
					if( e.getType() == TableModelEvent.UPDATE )
					{
						int row = e.getFirstRow();
						int col = e.getColumn();
						
						if( row >= 0 )
						{
							TableModel tm = (TableModel)e.getSource();							
							Object d = tm.getValueAt( row, col );
								
							currentClisData = clisDataFiles.get( d );		
							
							showSelectedVars();
						}
						else
						{
							currentClisData = null;
						}
					}
				}
			});
						
			this.tableFileData.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
			
			this.tableFileData.setPreferredScrollableViewportSize( this.tableFileData.getPreferredSize( ) );
			this.tableFileData.setFillsViewportHeight( true );
			
			TableColumnModel tcm = this.tableFileData.getColumnModel();
			
			tcm.getColumn( 0  ).setResizable( false );
			
			TableButtonCellRender btRender = new TableButtonCellRender();
			TableButtonCellEditor btEditor = new TableButtonCellEditor();
			
			JButton btR = btRender.getButton();
			JButton btEd = btEditor.getButton();
			
			btR.setIcon( GeneralAppIcon.Close( 12, Color.RED ) );
			btEd.setIcon( GeneralAppIcon.Close( 12, Color.RED ) );
			
			ActionListener actListener = new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JTable tb =  getTableFileData();
					tb.getCellEditor().stopCellEditing();
					
				}
			};
			btR.addActionListener(actListener );
			btEd.addActionListener(actListener );
			
			tcm.getColumn( 1 ).setCellRenderer( btRender );
			tcm.getColumn( 1 ).setCellEditor( btEditor );
			tcm.getColumn( 1  ).setResizable( false );
			tcm.getColumn( 1 ).setPreferredWidth( 25 );
			tcm.getColumn( 1 ).setMaxWidth( 25 );
						
			this.tableFileData.getSelectionModel( ).addListSelectionListener( new ListSelectionListener( ) 
			{				
				@Override
				public void valueChanged( ListSelectionEvent e ) 
				{
					ListSelectionModel md = ( ListSelectionModel )e.getSource( );
					if( !md.isSelectionEmpty( ) && !e.getValueIsAdjusting( ) )
					{
						int r = tableFileData.getSelectedRow( );
						
						if( r >= 0 )
						{					
							Object d = tableFileData.getValueAt( r, 0 );
								
							currentClisData = clisDataFiles.get( d );		
							
							showSelectedVars();
						}
						else
						{
							currentClisData = null;
						}
						
						showSelectedVars();
					}
					
					//getButtonTakeOffDataFile().setEnabled( !md.isSelectionEmpty( ) );
				}
			} );	
			
			this.tableFileData.addKeyListener( new KeyAdapter()
			{
				@Override
				public void keyReleased(KeyEvent e) 
				{
					if( e.getKeyCode() == KeyEvent.VK_DELETE )
					{
						JTable tb = (JTable)e.getSource();
						
						int cRow = tb.getSelectedRow();
						if( cRow >= 0 && cRow < tb.getRowCount() )
						{
						}
					}
				}
			});
		}
		
		return this.tableFileData;
	}	
	//*/

	/*
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
			
		
		return t;
	}
	
	private TableModel createBinFileTable( )
	{					
		TableModel tm = new DefaultTableModel( null, new String[] { Language.getLocalCaption( Language.MENU_FILE )
																	, "" } )
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
		return new Class[]{ String.class, Boolean.class };
	}		
	//*/
	
	private void showSelectedVars()
	{
		if( this.clisDataFile != null )
		{
			JComboBox< String > vars = getCbMarkVariable();
			JComboBox< String > time = getCbTimeVariable();
			JComboBox< Integer > cols = getCbMarkColumn();
			
			vars.removeAllItems();
			time.removeAllItems();
			cols.removeAllItems();
			
			for( MetadataVariableBlock var : this.clisDataFile.getVarInfo() )
			{
				vars.addItem( var.getName() );
				time.addItem( var.getName() );
			}
			
			if( this.clisDataFile.getVarInfo().size() > 0 )
			{
				vars.setSelectedIndex( 0 );
				time.setSelectedIndex( 0 );
			}
			
			if( this.clisDataFile.getVarInfo().size() > 1 )
			{
				time.setSelectedIndex( 1 );
			}
		}
	}
}
