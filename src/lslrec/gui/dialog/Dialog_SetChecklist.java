/* 
 * Copyright 2018-2026 by Manuel Merino Monge <manmermon@dte.us.es>
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import lslrec.config.ConfigApp;
import lslrec.config.language.Caption;
import lslrec.config.language.Language;
import lslrec.control.message.checklist.CheckMessage;
import lslrec.control.message.checklist.CheckMessagePartFromText;
import lslrec.gui.GuiTextManager;
import lslrec.gui.miscellany.BasicPainter2D;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.gui.miscellany.TableButtonCellEditor;
import lslrec.gui.miscellany.TableButtonCellRender;

public class Dialog_SetChecklist extends JDialog 
{
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPanel;
	private JPanel panelButtons;
	private JPanel panelChecklist;
	
	private JButton btAddMsg;
	private JButton btnDelMsg;
	private JButton btUpMsg;
	private JButton btDonwMsg;	
	
	private JTable tableChecklist;
	
	private List< CheckMessage > checkMessageList = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) 
	{
		try {
			Dialog_SetChecklist dialog = new Dialog_SetChecklist();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public Dialog_SetChecklist( ) 
	{
		super.setBounds(100, 100, 450, 300);
		super.getContentPane().setLayout(new BorderLayout());
		
		super.getContentPane().add( this.getContentPanel(), BorderLayout.CENTER);
		
		super.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
		
		/*
		super.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e) 
			{
				saveChecklistMessages();
			}
		});
		//*/
		
		this.checkMessageList = (List< CheckMessage >)ConfigApp.getProperty( ConfigApp.CHECKLIST_MSGS );
		for( CheckMessage msg : this.checkMessageList )
		{
			this.createNewMsg2Checklist( msg );
		}	
	}
	
	/*
	private void saveChecklistMessages()
	{
		String ID = ConfigApp.CHECKLIST_MSGS;
		
		JTable tb = this.getChecklistTable();
		TableModel tm = tb.getModel();
		
		int numMsgs = tb.getRowCount();
		List< Tuple< Boolean, String > > msgs = new ArrayList< Tuple< Boolean, String > >();
		for( int m = 0; m < numMsgs; m++ )
		{
			Boolean sel = (Boolean)tm.getValueAt( m, 0 );
			String msg = tm.getValueAt( m, 1 ).toString();
			
			Tuple< Boolean, String > tmsg = new Tuple<Boolean, String>( sel, msg );
			msgs.add( tmsg );
		}
		
		ConfigApp.setProperty( ID, msgs );
	}
	//*/
	
	private JPanel getContentPanel()
	{
		if( this.contentPanel == null )
		{
			this.contentPanel = new JPanel();
			
			this.contentPanel.setLayout( new BorderLayout() );
			
			this.contentPanel.add( this.getButtonsPanel(), BorderLayout.NORTH );
			
			JScrollPane sc = new JScrollPane( this.getChecklistPanel(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
			sc.getVerticalScrollBar().setUnitIncrement( 10 );
			this.contentPanel.add( sc, BorderLayout.CENTER );
		}
		return this.contentPanel;
	}
	
	private JPanel getButtonsPanel()
	{
		if( this.panelButtons == null )
		{
			this.panelButtons = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			
			this.panelButtons.add( this.getBtAddMsg() );
			this.panelButtons.add( this.getBtDelMsg() );
			this.panelButtons.add( this.getBtUpMsg() );
			this.panelButtons.add( this.getBtDonwMsg() );
		}
		return this.panelButtons;
	}

	private JPanel getChecklistPanel()
	{
		if( this.panelChecklist == null )
		{
			this.panelChecklist = new JPanel( new BorderLayout() );
			
			this.panelChecklist.add( this.getChecklistTable().getTableHeader(), BorderLayout.NORTH );
			this.panelChecklist.add(  this.getChecklistTable(), BorderLayout.CENTER );
		}
		return this.panelChecklist;
	}
	
	private JButton getBtAddMsg()
	{
		if( this.btAddMsg == null )
		{
			this.btAddMsg = new JButton();
			this.btAddMsg.setFont( new Font( Font.DIALOG, Font.BOLD, 16) );
			
			Icon ic = GeneralAppIcon.Add( 16, Color.BLACK );
			this.btAddMsg.setIcon( ic );
			if( ic == null )
			{
				this.btAddMsg.setText( "+" );
			}
			
			this.btAddMsg.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e)
				{
					SwingUtilities.invokeLater( () ->
					{
						CheckMessage msg = new CheckMessage( "userCheckMsg" + (getChecklistTable().getRowCount()+1)
															, CheckMessage.WARNING, CheckMessage.REMOVABLE_MESSAGE );
						msg.setEnable( true );
						
						msg.addMessagePart( new CheckMessagePartFromText( "message", "", true ));
											
						checkMessageList.add( msg );
						
						createNewMsg2Checklist( msg );						
						
						SwingUtilities.invokeLater( () ->
						{
							JTable table = getChecklistTable();

							int lastRow = table.getRowCount() - 1;						
							int column = 1;
							if (lastRow >= 0) 
							{	
								table.setRowSelectionInterval( lastRow, lastRow);							
								table.setColumnSelectionInterval( column, column );

								table.editCellAt(lastRow, column );

								Component editor = table.getEditorComponent();
								if (editor instanceof JButton) 
								{
									((JButton) editor).doClick();
								}
							}
						});
					});
				}
			});
			
		}
		
		return this.btAddMsg;
	}
	
	private JButton getBtDelMsg()
	{
		if( this.btnDelMsg == null )
		{
			this.btnDelMsg = new JButton();
			this.btnDelMsg.setEnabled( false );
			this.btnDelMsg.setFont( new Font( Font.DIALOG, Font.BOLD, 16) );
			
			//Icon ic = GeneralAppIcon.Close( 16, Color.RED );
			Icon ic = GeneralAppIcon.Trash( 16, Color.RED.darker() );
			this.btnDelMsg.setIcon( ic );
			if( ic == null )
			{
				this.btnDelMsg.setText( "delete" );
				this.btnDelMsg.setForeground( Color.RED );
			}
			
			this.btnDelMsg.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e)
				{
					removeMsgsFromChecklist();
					/*
					SwingUtilities.invokeLater(() ->
					{
						removeMsgsFromChecklist();
					});
					//*/
				}
			});
		}
		
		return this.btnDelMsg;
	}
	
	private JButton getBtUpMsg()
	{
		if( this.btUpMsg == null )
		{
			this.btUpMsg = new JButton();
			this.btUpMsg.setEnabled( false );
			
			try
			{
				ImageIcon icon = new ImageIcon( BasicPainter2D.paintTriangle(  16,  2, Color.BLACK
																				, Color.LIGHT_GRAY, BasicPainter2D.NORTH ) );
				
				Dimension d = new Dimension( icon.getIconWidth(), icon.getIconHeight() );
				d.width += 6;
				d.height += 6;
				this.btUpMsg.setPreferredSize( d );
				this.btUpMsg.setIcon( icon );
			}
			catch( Exception ex )
			{
				Caption cap = Language.getAllCaptions().get(  Language.UP_TEXT );
				this.btUpMsg.setText( cap.getCaption( Language.getCurrentLanguage() ) );
				
				GuiTextManager.addComponent( GuiTextManager.TEXT, Language.UP_TEXT, this.btUpMsg );
			}
			
			this.btUpMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			this.btUpMsg.addActionListener( new ActionListener()
			{				
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					reorderMsgs( -1 );
				}
			});
		}
		return this.btUpMsg;
	}
	
	private JButton getBtDonwMsg()
	{
		if( this.btDonwMsg == null )
		{
			this.btDonwMsg = new JButton();
			this.btDonwMsg.setEnabled( false );
			
			try
			{
				ImageIcon icon = new ImageIcon( BasicPainter2D.paintTriangle(  16,  2, Color.BLACK
																				, Color.LIGHT_GRAY, BasicPainter2D.SOUTH ) );
				
				Dimension d = new Dimension( icon.getIconWidth(), icon.getIconHeight() );
				d.width += 6;
				d.height += 6;
				this.btDonwMsg.setPreferredSize( d );
				this.btDonwMsg.setIcon( icon );
			}
			catch( Exception ex )
			{
				Caption cap = Language.getAllCaptions().get(  Language.DOWN_TEXT );
				this.btDonwMsg.setText( cap.getCaption( Language.getCurrentLanguage() ) );
				
				GuiTextManager.addComponent( GuiTextManager.TEXT, Language.DOWN_TEXT, this.btDonwMsg );
			}
			
			this.btDonwMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			this.btDonwMsg.addActionListener( new ActionListener()
			{				
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					reorderMsgs( 1 );
				}
			});
		}
		return this.btDonwMsg;
	}
		
	private JTable getChecklistTable()
	{
		if( this.tableChecklist == null )
		{
			this.tableChecklist = this.getCreateJTable();
			this.tableChecklist.setModel( this.createTablemodel() );
			
			String hCol0 = this.tableChecklist.getColumnModel().getColumn( 0 ).getHeaderValue().toString() + "   ";
			FontMetrics fm = this.tableChecklist.getFontMetrics( this.tableChecklist.getFont() );
						 
			this.tableChecklist.getColumnModel().getColumn(0).setResizable(false);
			this.tableChecklist.getColumnModel().getColumn(0).setPreferredWidth( fm.stringWidth( hCol0 ) );
			this.tableChecklist.getColumnModel().getColumn(0).setMaxWidth( fm.stringWidth( hCol0 ) * 2 );
			this.tableChecklist.getColumnModel().getColumn(0).setMinWidth( fm.stringWidth( hCol0 ) );
			
			hCol0 = this.tableChecklist.getColumnModel().getColumn( 1 ).getHeaderValue().toString() + "   ";
			this.tableChecklist.getColumnModel().getColumn(1).setResizable(false);
			this.tableChecklist.getColumnModel().getColumn(1).setPreferredWidth( fm.stringWidth( hCol0 ) );
			this.tableChecklist.getColumnModel().getColumn(1).setMaxWidth( fm.stringWidth( hCol0 ) * 2 );
			this.tableChecklist.getColumnModel().getColumn(1).setMinWidth( fm.stringWidth( hCol0 ) );
						
			
			TableButtonCellRender btRender = new TableButtonCellRender();
			TableButtonCellEditor btEditor = new TableButtonCellEditor();
			
			JButton btR = btRender.getButton();
			JButton btEd = btEditor.getButton();
			
			btR.setIcon( GeneralAppIcon.Pencil( 14, Color.BLACK ) );
			btEd.setIcon( GeneralAppIcon.Pencil( 14, Color.BLACK ) );
			
			ActionListener actListener = new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JButton bt = (JButton)e.getSource();
					
					JTable table = getChecklistTable();
					int r = table.getSelectedRow();
					
					if( r >= 0 && r < checkMessageList.size() )
					{
						CheckMessage msg = checkMessageList.get( r );

						Dialog_SetCheckMessagePart diag = new Dialog_SetCheckMessagePart( msg );
						diag.setModal( true );
						diag.setIconImage( GeneralAppIcon.Pencil( 16, Color.BLACK ).getImage() );
						diag.setTitle( Language.getLocalCaption( Language.CHECKLIST_TEXT )
										+ " - " + Language.getLocalCaption( Language.EDIT_TEXT ) );
						
						diag.setSize( new Dimension( 400, 200 ) );
						diag.setLocationRelativeTo( SwingUtilities.getWindowAncestor( bt ) );
						
						diag.setVisible( true );		
						
						SwingUtilities.invokeLater(() -> 
						{
							DefaultTableModel model = (DefaultTableModel) table.getModel();
							model.setValueAt( msg.getMessage(), r, 2 );
							model.fireTableCellUpdated( r, 2 );
						});
						
					}
				}
			};
			btR.addActionListener(actListener );
			btEd.addActionListener(actListener );
			
			this.tableChecklist.getColumnModel().getColumn( 1 ).setCellRenderer( btRender );
			this.tableChecklist.getColumnModel().getColumn( 1 ).setCellEditor( btEditor );			
						
			this.tableChecklist.getSelectionModel( ).addListSelectionListener( new ListSelectionListener( ) 
			{	
				@Override
				public void valueChanged( ListSelectionEvent e ) 
				{	
					if( !e.getValueIsAdjusting( ) )
					{
						JTable t = getChecklistTable();
						
						int selRow = t.getSelectedRow();
						int col = t.getSelectedColumn();
						
						if( selRow >= 0 && selRow < checkMessageList.size() )
						{
							CheckMessage msg = checkMessageList.get( selRow );
							
							boolean enable = msg.isRemovableMsg();
							
							getBtDelMsg().setEnabled( enable );
							getBtUpMsg().setEnabled( enable );
							getBtDonwMsg().setEnabled( enable );
						}
					}
				}
			} );	
			
			this.tableChecklist.getModel().addTableModelListener( new TableModelListener() 
			{				
				@Override
				public void tableChanged(TableModelEvent e) 
				{
					if (e.getType() == TableModelEvent.UPDATE) 
					{
				        int column = e.getColumn();
				        int row = e.getFirstRow();

				        if (column == 0)
				        { 
				        	CheckMessage msg = checkMessageList.get( row );
				        	
				        	// columna booleana
				            Object value = getChecklistTable().getModel().getValueAt( row, column );
				            boolean newValue = (Boolean) value;

							msg.setEnable( newValue );
				        }
				    }	
				}
			});
		}
		
		return this.tableChecklist;
	}
	
	private JTable getCreateJTable()
	{
		JTable table =  new JTable()
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
				                    tip = getValueAt( rowIndex, colIndex).toString();
				                }
				                catch ( RuntimeException e1 )
				                {
				                    //catch null pointer exception if mouse is over an empty line
				                }
				
				                return tip;
				            }				            
				        };
				        
		table.setDefaultRenderer( Object.class, new DefaultTableCellRenderer()
											{	
												private static final long serialVersionUID = 4144425414849985295L;

												public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
											    {	
											        Component cellComponent = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column);
											        
											        CheckMessage msg = checkMessageList.get( row );
											        
											        if( !msg.isRemovableMsg() )
											        {
											        	cellComponent.setBackground( new Color( 255, 255, 150 ) );
											        }											        
											        else
											        {
											        	cellComponent.setBackground( Color.WHITE );
											        }
											        
											        cellComponent.setForeground( Color.BLACK );
											        
											        return cellComponent;
											    }
											});
		
		table.getTableHeader().setReorderingAllowed( false );
		
		return table;
	}
	
	private void changeCheckNumberSelectedStreams( String msg, int row, int col )
	{
		String errMsg = "";		
		String val = "";
		
		while( val != null && val.isEmpty() )
		{
			val = JOptionPane.showInputDialog( this, errMsg + msg );
			
			if( val != null )
			{
				try 
				{				
					if( Integer.parseInt( val ) < 1 )
					{
						val = "";
						errMsg = "";
					}
				} 
				catch (Exception e) 
				{
					val = "";
					errMsg = e.getMessage() + "\n";
				}
			}
		}
		
		if( val != null )
		{
			tableChecklist.setValueAt( msg + val, row, col );
		}
	}
	
	private TableModel createTablemodel( )
	{	
		TableModel tm =  new DefaultTableModel( null, new String[] { Language.getLocalCaption( Language.SELECT_TEXT )
																		, Language.getLocalCaption( Language.EDIT_TEXT )
																		, Language.getLocalCaption( Language.CHECKLIST_TEXT )  } )
							{
								private static final long serialVersionUID = 1L;
								
								Class[] columnTypes = new Class[]{ Boolean.class, String.class, String.class };								
								boolean[] columnEditables = new boolean[] { true, true, false };
																
								public Class getColumnClass(int columnIndex) 
								{
									return columnTypes[ columnIndex ];
								}
																								
								public boolean isCellEditable(int row, int column) 
								{						
									//return columnEditables[ column ];
									boolean edit = columnEditables[ column ];
									
									if( column == 1 )
									{
										CheckMessage msg = checkMessageList.get( row );
										edit = msg.isEditableMessage();
									}
									
									return edit;
								}
							};
		return tm;
	}
	
	private void createNewMsg2Checklist( CheckMessage msg )
	{
		if( msg != null )
		{
			JTable chlistTb = this.getChecklistTable();

			chlistTb.setVisible( false );

			DefaultTableModel chlistTm = (DefaultTableModel)chlistTb.getModel();

			chlistTm.addRow( new Object[]{ msg.isEnable()
											, msg.isEditableMessage()
											, msg.getDescription() } );

			chlistTb.setVisible( true );
		}
	}
	
	private void removeMsgsFromChecklist( )
	{
		JTable chlistTb = this.getChecklistTable();
		
		//chlistTb.setVisible( false );
		
		DefaultTableModel chlistTm = (DefaultTableModel)chlistTb.getModel();		
		
		if( chlistTb.isEditing() )
		{
		    chlistTb.getCellEditor().stopCellEditing();
		}
		
		int[] index = chlistTb.getSelectedRows();
		
		Arrays.sort( index );
		
		for( int i = index.length - 1; i >= 0; i-- )
		{			
			//int r = index[ i ];
			
			int viewRow = index[i];
	        int r = chlistTb.convertRowIndexToModel(viewRow);

	        if (r < 0 || r >= chlistTm.getRowCount() || r >= checkMessageList.size() ) 
	        {
	        	continue;
	        }
			
			CheckMessage msg = checkMessageList.get( r );
			if( msg.isRemovableMsg() )
			{
				checkMessageList.remove( r );
								
				chlistTm.removeRow( r );
				
				int newRow = ( r < chlistTm.getRowCount() ) ? r : chlistTm.getRowCount()-1;
				
				if( newRow >= 0 )
				{
					int viewIndex = chlistTb.convertRowIndexToView(newRow);
					if (viewIndex >= 0)
					{
						chlistTb.setRowSelectionInterval(viewIndex, viewIndex);
					}
				}
			}
		}
		
		//chlistTb.setVisible( true );
		chlistTb.revalidate();
	    chlistTb.repaint();
	}
	
	
	private void reorderMsgs( int shift )
	{
		JTable source = this.getChecklistTable();
		DefaultTableModel tmSource = (DefaultTableModel)source.getModel();
				
		int dir = 1;
		if( shift < 0 )
		{
			dir = -1;
		}
		
		int[] selIndex = source.getSelectedRows();
		Arrays.sort( selIndex );
		
		int ref = 0;
		int from = 0; 
		int to = selIndex.length;
		
		if( dir < 0 )
		{
			for( int i = 0; i < selIndex.length; i++ )
			{
				if( ref == selIndex[ i ] )
				{
					from++;
					ref++;
				}
				else
				{
					break;
				}
			}
		}
		else
		{
			ref = source.getRowCount() -1;
			for( int i = selIndex.length - 1; i >= 0; i-- )
			{
				if( ref == selIndex[ i ] )
				{
					to--;
					ref--;
				}
				else
				{
					break;
				}
			}
		}		
		
		if( from < to )
		{		
			selIndex = Arrays.copyOfRange( selIndex, from, to );
			
			if( selIndex.length > 0 )
			{
				for( int i = selIndex.length - 1; i >= 0; i-- )
				{
					int index = selIndex[ i ];
					int row = index + dir;
					
					CheckMessage msg1 = checkMessageList.get( row );
					CheckMessage msg2 = checkMessageList.get( index );
					
					if( msg1.isRemovableMsg() && msg2.isRemovableMsg() )
					{
						tmSource.moveRow( index, index, row );
						
						if( i == 0 )
						{
							source.setRowSelectionInterval( row, row );
						}
						
						checkMessageList.remove( row );
						checkMessageList.add( row, msg2 );
						
						checkMessageList.remove( index );
						checkMessageList.add( index, msg1 );						
					}
				}				
			}
		}
	}	
}
