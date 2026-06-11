/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2011-2018 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import lslrec.config.language.Language;
import lslrec.control.message.RegisterSyncMessages;
import lslrec.gui.miscellany.DisabledPanel;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.gui.miscellany.IPAddressCellEditor;
import lslrec.gui.miscellany.SpinnerNumberCellEditor;
import lslrec.config.ConfigApp;

public class SyncSocketSettingPanel extends JPanel
{	
	private static final long serialVersionUID = 2157401336834064879L;
		
	//PANELS
	private JPanel jPanelTCP_IP;
	private JPanel jPanelServerCommands;
	private JPanel jPanelServerSocket;
	//private JPanel jPanelServerControl;
	private JPanel jPanelTableServerSocket;
	private JPanel jPanelServerContentCommandsValues;
	private JPanel serverInputMsgPanel;
	private JPanel addInputMsgPanel;
		
	
	//SPLITSPANES
	//private JSplitPane jPanelCommands;
	private JPanel jPanelCommands;
	
	//CHECKBOXES
	//private JCheckBox checkServerSocket;	
	
	//TABLES
	private JTable jTableServerSocket;
	private JTable jTableServerCommandValue;
	
	//BUTTONS	
	private JButton addInMsgBtn;
	private JButton delInMsgBtn;
	
	//SCROLLPANES
	private JScrollPane jScrollPaneServerCommands;
	
	//Map
	//private Map< String, Component > parameters;
	
	//JFrame
	//private JFrame winOwner;
	
	// disabledPanel
	private DisabledPanel disPanel;
	
	// Auxiliar
	private Object[][] previousRowValues = null;
	
	public SyncSocketSettingPanel( )//JFrame owner )
	{
		//this.winOwner = owner;
		
		//this.parameters = new HashMap<String, Component>();
		
		this.init( );
				
		super.validate();
	}
	
	public void enableSettings( boolean enable )
	{
		this.getDisabledPanel().setEnabled( enable );
	}
		
	public void loadRegisteredSyncInputMessages()
	{
		JTable t = this.getJTableServerCommandValues();
		
		DefaultTableModel m = (DefaultTableModel)t.getModel();
		for( int i = t.getRowCount() - 1; i >= 0; i-- )
		{
			m.removeRow( i );
		}
		
		Map< String, Integer > SYNC = RegisterSyncMessages.getSyncMessagesAndMarks();
		for( String msg : SYNC.keySet() )
		{
			Integer mark = SYNC.get( msg );
			
			//this.updateRegisteredSynMessage( t, msg, mark );
			GuiManager.getInstance().updateRegisteredSynMessage( t, msg, mark );
		}
	}
	
	private void init( )
	{					
		super.setLayout( new BorderLayout() );
		super.add( this.getDisabledPanel(), BorderLayout.CENTER );
	}
	
	private DisabledPanel getDisabledPanel()
	{
		if( this.disPanel == null )
		{
			this.disPanel = new DisabledPanel( this.getJPanelTCP_IP() ); 
		}
		return this.disPanel;
	}
	
	private JPanel getJPanelTCP_IP() 
	{
		if (jPanelTCP_IP == null) 
		{
			jPanelTCP_IP = new JPanel();
			jPanelTCP_IP.setLayout(new BorderLayout(0, 0));
			
			jPanelTCP_IP.add( getJPanelCommands(), BorderLayout.CENTER);
		}
		return jPanelTCP_IP;
	}
		
	private JPanel getJPanelCommands() 
	{
		if ( this.jPanelCommands == null) 
		{	
			this.jPanelCommands = new JPanel( new BorderLayout() );
			
			this.jPanelCommands.add( this.getJPanelServerCommands(), BorderLayout.CENTER );			
		}
		
		return jPanelCommands;
	}
		
	private JPanel getJPanelServerCommands() 
	{
		if (jPanelServerCommands == null) 
		{
			jPanelServerCommands = new JPanel();
			
			TitledBorder border = BorderFactory.createTitledBorder( new LineBorder( SystemColor.inactiveCaption, 2)
																	, Language.getLocalCaption( Language.SETTING_SOCKET_TAB_INPUT_PANEL ) );
			
			GuiTextManager.addComponent( GuiTextManager.BORDER, Language.SETTING_SOCKET_TAB_INPUT_PANEL, border );
			
			jPanelServerCommands.setBorder( border );
			jPanelServerCommands.setLayout(new BorderLayout(0, 0));
			jPanelServerCommands.add( getJPanelTCP_Server(), BorderLayout.NORTH);
			jPanelServerCommands.add( getServerInputMsgPanel(), BorderLayout.CENTER);
		}
		return this.jPanelServerCommands;
	}
	
	private JPanel getServerInputMsgPanel()
	{
		if( this.serverInputMsgPanel == null )
		{
			this.serverInputMsgPanel = new JPanel( new BorderLayout() );
			
			this.serverInputMsgPanel.add( this.getAddInputMsgPanel(), BorderLayout.NORTH );
			this.serverInputMsgPanel.add( this.getJScrollPaneServerCommands(), BorderLayout.CENTER );
		}
		
		return this.serverInputMsgPanel;
	}
	
	private JPanel getAddInputMsgPanel()
	{
		if( this.addInputMsgPanel == null )
		{
			this.addInputMsgPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			
			this.addInputMsgPanel.add( this.getAddInMsgButton() );
			this.addInputMsgPanel.add( this.getDelInMsgButton() );
		}
		
		return this.addInputMsgPanel;
	}
	
	private JButton getAddInMsgButton()
	{
		if( this.addInMsgBtn == null )
		{
			this.addInMsgBtn = new JButton( );
			
			Icon ic = GeneralAppIcon.Add( 16, Color.BLACK );
			
			this.addInMsgBtn.setIcon( ic );
			if( ic == null )
			{
				this.addInMsgBtn.setText( Language.getLocalCaption( Language.INSERT_TEXT ) );
			}
			
			this.addInMsgBtn.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent arg0) 
				{					
					JTable t = getJTableServerCommandValues();
					
					GuiManager.getInstance().addNewSocketInputMessage( t );
				}
			});
		
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.INSERT_TEXT, this.addInMsgBtn );
		}
		
		return this.addInMsgBtn;
	}
				
	private JButton getDelInMsgButton()
	{
		if( this.delInMsgBtn == null )
		{
			this.delInMsgBtn = new JButton( );
			
			Icon ic = GeneralAppIcon.Trash( 16, Color.RED );
			
			this.delInMsgBtn.setIcon( ic );
			
			if( ic == null )
			{
				this.delInMsgBtn.setText( Language.getLocalCaption( Language.DELETE_TEXT ) );
			}
			
			this.delInMsgBtn.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent arg0) 
				{
					JTable t = getJTableServerCommandValues();
					
					GuiManager.getInstance().delSocketInputMessage( t );
				}
			});
		
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.DELETE_TEXT, this.delInMsgBtn );
		}
		
		return this.delInMsgBtn;
	}
	
	private JScrollPane getJScrollPaneServerCommands() 
	{
		if ( this.jScrollPaneServerCommands == null ) 
		{
			this.jScrollPaneServerCommands = new JScrollPane();
			this.jScrollPaneServerCommands.setViewportView( this.getJPanelServerContentCommandsValues() );
		}
		return this.jScrollPaneServerCommands;
	}
	
	private JPanel getJPanelServerContentCommandsValues() 
	{
		if ( this.jPanelServerContentCommandsValues == null ) 
		{
			this.jPanelServerContentCommandsValues = new JPanel();
			this.jPanelServerContentCommandsValues.setLayout( new BorderLayout( 0, 0 ) );
			
			this.jPanelServerContentCommandsValues.add( this.getJTableServerCommandValues().getTableHeader(), BorderLayout.NORTH );
			this.jPanelServerContentCommandsValues.add( this.getJTableServerCommandValues(), BorderLayout.CENTER );
		}
		return this.jPanelServerContentCommandsValues;
	}
		
	private JPanel getJPanelTCP_Server() 
	{
		if ( this.jPanelServerSocket == null) 
		{
			this.jPanelServerSocket = new JPanel();
			this.jPanelServerSocket.setLayout(new BoxLayout( this.jPanelServerSocket, BoxLayout.Y_AXIS));
			this.jPanelServerSocket.setBorder( BorderFactory.createEmptyBorder( 2, 0,  5,  0 ) );
			
			//this.jPanelServerSocket.add( this.getJPanelServerControl() );
			this.jPanelServerSocket.add( this.getPanelTableServerSocket() );
		}
		return this.jPanelServerSocket;
	}
	
	/*
	private JPanel getJPanelServerControl() 
	{
		if ( this.jPanelServerControl == null) 
		{
			this.jPanelServerControl = new JPanel();
			
			this.jPanelServerControl.setLayout( new BorderLayout() );
			//this.jPanelServerControl.add( this.getJCheckboxServerSocket(), BorderLayout.WEST );
			//this.jPanelServerControl.add( this.getJCheckActiveSpecialInputMsg(), BorderLayout.WEST );
			//this.jPanelServerControl.add( this.getJButtonInfo(), BorderLayout.EAST );						
		}
		
		return this.jPanelServerControl;
	}
	*/
	
	/*
	private JCheckBox getJCheckboxServerSocket()
	{
		if( this.checkServerSocket == null )
		{
			final String ID = ConfigApp.IS_SOCKET_SERVER_ACTIVE;
			
			checkServerSocket = new JCheckBox( "Active");
			checkServerSocket.setSelected( (Boolean)ConfigApp.getProperty( ID ) );
			
			//final Window owner = super.getOwner();
			checkServerSocket.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JCheckBox c = (JCheckBox)e.getSource();
					
					ConfigApp.setProperty( ID, c.isSelected() );					
				}
			});
			
			this.parameters.put( ID, checkServerSocket );
		}
		
		return this.checkServerSocket;
	}
	*/
	
	private JPanel getPanelTableServerSocket()
	{
		if( this.jPanelTableServerSocket == null )
		{
			this.jPanelTableServerSocket = new JPanel();
			this.jPanelTableServerSocket.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			
			this.jPanelTableServerSocket.setLayout( new BorderLayout() );
			
			this.jPanelTableServerSocket.add( this.getTableServerSockets().getTableHeader(), BorderLayout.NORTH );
			this.jPanelTableServerSocket.add( this.getTableServerSockets(), BorderLayout.CENTER );			
		}
		
		return this.jPanelTableServerSocket;
	}
	
	
	
	private JTable getTableServerSockets()
	{
		if( this.jTableServerSocket == null )
		{
			final String propertyID = ConfigApp.SERVER_SOCKET; 
			
			this.jTableServerSocket = this.getTableSockets( null ); 
			
			this.jTableServerSocket.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
						
			TableModel tm = this.jTableServerSocket.getModel();
			tm.addTableModelListener( new TableModelListener()
			{				
				@Override
				public void tableChanged( TableModelEvent e ) 
				{
					GuiManager.getInstance().updateSocket( e, propertyID, getJTableServerCommandValues() );
				}
			});			
			
			GuiManager.registerGUIComponent( propertyID, propertyID, this.jTableServerSocket );									
		}
		
		return this.jTableServerSocket;
	}
	
	private JTable getCreateJTable()
	{
		JTable t =  new JTable()
						{
							private static final long serialVersionUID = 1L;
			
							//Implement table cell tool tips.           
				            public String getToolTipText( MouseEvent e) 
				            {
				                String tip = null;
				                Point p = e.getPoint();
				                int rowIndex = rowAtPoint(p);
				                int colIndex = columnAtPoint(p);
				
				                try 
				                {
				                    tip = getValueAt(rowIndex, colIndex).toString();
				                }
				                catch ( RuntimeException e1 )
				                {
				                    //catch null pointer exception if mouse is over an empty line
				                }
				
				                return tip;
				            }				            
				        };
				        
		t.setDefaultRenderer( Object.class, new DefaultTableCellRenderer()
											{	
												public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
											    {
											        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
											        	
											        if( !table.isCellEditable( row, column ) )
											        {	
											        	cellComponent.setBackground( new Color( 255, 255, 224 ) );											        	
											        }
											        else
											        {
											        	if( isSelected )
											        	{
											        		cellComponent.setBackground( new Color( 0, 120, 215 ) );
											        	}
											        	else
											        	{
											        		cellComponent.setBackground( Color.WHITE );
											        	}
											        }
											        
											        cellComponent.setForeground( Color.BLACK );
											        
											        return cellComponent;
											    }
											});
		
		t.getTableHeader().setReorderingAllowed( false );
		
		return t;
	}
	
	private TableModel createClientSocketTableModel( Object[][] table)
	{
		TableModel tm = new DefaultTableModel( table,new String[] {"TCP/UDP"
															, Language.getLocalCaption( Language.SETTING_SOCKET_TAB_INPUT_IP_TABLE_ADDRESS )
															, Language.getLocalCaption( Language.SETTING_SOCKET_TAB_INPUT_IP_TABLE_PORT )} )
									{			
										Class[] columnTypes = new Class[] {	String.class, String.class, Integer.class };
										
										public Class getColumnClass(int columnIndex) 
										{
											return columnTypes[ columnIndex ];
										}
									};
		return tm;
	}
	
	private JTable getTableSockets( Object[][] t )
	{
		JTable table = this.getCreateJTable();
		table.putClientProperty("terminateEditOnFocusLost", true);
		
		table.setBackground( Color.WHITE );
        table.setRowSelectionAllowed( false );
        					
		table.setModel( this.createClientSocketTableModel( t ) );
		
		GuiTextManager.addComponent( GuiTextManager.TEXT, Language.SETTING_SOCKET_TAB_INPUT_IP_TABLE_ADDRESS
											, table.getColumnModel().getColumn( 1 ) );
		
		GuiTextManager.addComponent( GuiTextManager.TEXT, Language.SETTING_SOCKET_TAB_INPUT_IP_TABLE_PORT
											, table.getColumnModel().getColumn( 2 ) );
				
		table.getColumnModel().getColumn(0).setResizable(false);
		table.getColumnModel().getColumn(0).setPreferredWidth(60);
		table.getColumnModel().getColumn(0).setMaxWidth(60);
		table.getColumnModel().getColumn(0).setMinWidth(60);
		table.getColumnModel().getColumn(1).setPreferredWidth(125);
		
		TableColumn columnType = table.getColumnModel().getColumn( 0 );
		
		JComboBox< String > opts = new JComboBox<String>();
		opts.addItem( "TCP" );
		opts.addItem( "UDP" );
		columnType.setCellEditor( new DefaultCellEditor( opts ) );
				
		columnType = table.getColumnModel().getColumn( 1 );			
		JTextField ipEditor = new JTextField();
		columnType.setCellEditor( new IPAddressCellEditor( ipEditor ) );	
		ipEditor.addMouseWheelListener( new MouseWheelListener() 
		{	
			@Override
			public void mouseWheelMoved( MouseWheelEvent e ) 
			{
				GuiManager.getInstance().applyMouseWheelMoved2IPTextEditor( e, (JTextField)e.getSource() );
			}
		});		
		
		columnType = table.getColumnModel().getColumn( 2 );
		JSpinner portRange = new JSpinner( new SpinnerNumberModel( 1025, 1025, 65535, 1) );
		portRange.setEditor( new JSpinner.NumberEditor( portRange, "#") );		
		portRange.addMouseWheelListener( new MouseWheelListener() 
		{				
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) 
			{
				GuiManager.getInstance().applyMouseWheelMoved2Spinner( e, (JSpinner)e.getSource() );
			}
		});
				
		columnType.setCellEditor( new SpinnerNumberCellEditor( portRange ) );
		
		return table;
	}
			
	private TableModel createSocketCommandTablemodel( )
	{					
		TableModel tm =  new DefaultTableModel( null, new String[] { Language.getLocalCaption( Language.SETTING_LSL_MARK )
																	,  Language.getLocalCaption( Language.MSG_TEXT )} )
							{
								private static final long serialVersionUID = 1L;
								
								Class[] columnTypes = new Class[]{ Integer.class, String.class };								
								boolean[] columnEditables = new boolean[] { false, true };
								
								public Class getColumnClass(int columnIndex) 
								{
									return columnTypes[columnIndex];
								}
																								
								public boolean isCellEditable(int row, int column) 
								{
									boolean editable = columnEditables[ column ];
									
									if( editable && row >= 0 )
									{
										String val = (String)super.getValueAt( row, column );
										editable = !RegisterSyncMessages.isSpecialInputMessage( val );
									}
									
									return editable;
								}
							};
		return tm;
	}
	
	private JTable getJTableServerCommandValues()
	{
		if( this.jTableServerCommandValue == null )
		{	
			this.jTableServerCommandValue = this.getCreateJTable();
			this.jTableServerCommandValue.setModel( this.createSocketCommandTablemodel( ) );
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.SETTING_LSL_MARK
											, this.jTableServerCommandValue.getColumnModel().getColumn( 0 ) );

			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.MSG_TEXT
											, this.jTableServerCommandValue.getColumnModel().getColumn( 1 ) );
			
			FontMetrics fm = this.jTableServerCommandValue.getFontMetrics( this.jTableServerCommandValue.getFont() );			
			String hCol0 = this.jTableServerCommandValue.getColumnModel().getColumn( 0 ).getHeaderValue().toString();
			
			int s = fm.stringWidth( " " + hCol0 + " " ) * 2;
			this.jTableServerCommandValue.getColumnModel().getColumn( 0  ).setResizable( false );
			this.jTableServerCommandValue.getColumnModel().getColumn( 0 ).setPreferredWidth( s );
			this.jTableServerCommandValue.getColumnModel().getColumn( 0 ).setMaxWidth( s );
			
			this.jTableServerCommandValue.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
			
			this.jTableServerCommandValue.addFocusListener( new FocusListener() 
			{				
				@Override
				public void focusLost(FocusEvent e) 
				{}
				
				@Override
				public void focusGained(FocusEvent e) 
				{
					JTable t = (JTable)e.getSource();
										
					int r = t.getSelectedRow();
										
					if( r >= 0 )
					{
						int nRows = t.getRowCount(); 
						int nCols = t.getColumnCount();

						previousRowValues = new Object[ nRows ][ nCols ];
							
						for( int ir = 0; ir < nRows; ir++ )
						{
							for( int c = 0; c < nCols; c++ )
							{
								previousRowValues[ ir ][ c ] = t.getValueAt( ir, c );
							}
						}
					}
				}
			});
			
			this.jTableServerCommandValue.getModel().addTableModelListener( new TableModelListener()
			{				
				@Override
				public void tableChanged( TableModelEvent e ) 
				{
					DefaultTableModel tm = (DefaultTableModel)e.getSource();
					
					if( e.getType() == TableModelEvent.UPDATE )
					{
						int r = e.getFirstRow();
						int c = e.getColumn();
						
						Object o = tm.getValueAt( r, c );
						
						if( !RegisterSyncMessages.isSpecialInputMessage( o.toString() ) )
						{
							if( RegisterSyncMessages.getSyncMark( (String)o ).intValue() == RegisterSyncMessages.NO_MARK )
							{
								RegisterSyncMessages.updateSyncMessage( (String)previousRowValues[ r ][ c ], (String)o );
							}
							else if( !previousRowValues[ r ][ c ].equals( o ) )
							{
								tm.setValueAt( previousRowValues[ r ][ c ], r, c );
							}
						}
						else
						{
							tm.setValueAt( previousRowValues[ r ][ c ], r, c );
						}
					}
				}
			});
			
			this.jTableServerCommandValue.setPreferredScrollableViewportSize( this.jTableServerCommandValue.getPreferredSize() );
			this.jTableServerCommandValue.setFillsViewportHeight( true );
			
		}
		
		return this.jTableServerCommandValue;
	}	
	
	/*
	protected void updateTableSockets( JTable table, boolean add )
	{	
		DefaultTableModel m = (DefaultTableModel)table.getModel();
		
		if( add )
		{	
			int port = 5555;
			
			ServerSocket localmachine = null;
			try 
			{
				localmachine = new ServerSocket( 0 );
				localmachine.setReuseAddress( true );			
				port = localmachine.getLocalPort();
				localmachine.close();
			} 
			catch (IOException e1) {}
			finally
			{
				localmachine = null;
			}
			
			String ip = Inet4Address.getLoopbackAddress().getHostAddress();
			Object[] rt = new Object[]{"TCP", ip, port };
			
			m.addRow( rt );			
		}
		else
		{		
			int[] rs = table.getSelectedRows();
			Arrays.sort( rs );
			
			for( int i = rs.length - 1; i >= 0; i-- )
			{ 
				m.removeRow( rs[ i ] ); 
			}
			
			if( table.getRowCount() == 0 )
			{
				this.updateTableSockets( table, true );
			}
			else
			{
				table.setRowSelectionInterval( 0, 0 );
			}
		}
	}
	//*/
}
