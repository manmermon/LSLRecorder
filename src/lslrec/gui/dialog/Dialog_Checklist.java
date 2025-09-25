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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
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
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import lslrec.auxiliar.extra.Tuple;
import lslrec.config.ConfigApp;
import lslrec.config.language.Caption;
import lslrec.config.language.Language;
import lslrec.gui.GuiTextManager;
import lslrec.gui.miscellany.BasicPainter2D;
import lslrec.gui.miscellany.GeneralAppIcon;

public class Dialog_Checklist extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private JPanel contentPanel;
	private JPanel panelButtons;
	private JPanel panelChecklist;
	
	private JButton btAddMsg;
	private JButton btnDelMsg;
	private JButton btUpMsg;
	private JButton btDonwMsg;
	
	private JTable tableChecklist;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) 
	{
		try {
			Dialog_Checklist dialog = new Dialog_Checklist();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public Dialog_Checklist( ) 
	{
		super.setBounds(100, 100, 450, 300);
		super.getContentPane().setLayout(new BorderLayout());
		
		super.getContentPane().add( this.getContentPanel(), BorderLayout.CENTER);
		
		super.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
		
		super.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e) 
			{
				saveChecklistMessages();
			}
		});
		
		List< Tuple< Boolean, String > > msgs = (List< Tuple< Boolean, String > >)ConfigApp.getProperty( ConfigApp.CHECKLIST_MSGS );
		for( Tuple< Boolean, String > msg : msgs )
		{
			boolean sel = ( msg.t1 == null ) ? true : msg.t1;
			String val = msg.t2;
			this.createNewMsg2Checklist( sel, val );
		}	
	}
	
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
	
	private JPanel getContentPanel()
	{
		if( this.contentPanel == null )
		{
			this.contentPanel = new JPanel();
			
			this.contentPanel.setLayout( new BorderLayout() );
			
			this.contentPanel.add( this.getButtonsPanel(), BorderLayout.NORTH );
			
			JScrollPane sc = new JScrollPane( this.getChecklistPanel(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
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
					createNewMsg2Checklist( true, "message");
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
			this.btnDelMsg.setFont( new Font( Font.DIALOG, Font.BOLD, 16) );
			
			Icon ic = GeneralAppIcon.Close( 16, Color.RED );
			this.btnDelMsg.setIcon( ic );
			if( ic == null )
			{
				this.btnDelMsg.setText( "X" );
				this.btnDelMsg.setForeground( Color.RED );
			}
			
			this.btnDelMsg.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e)
				{
					removeMsgsFromChecklist();
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
			
			//this.tableChecklist.setRowSelectionAllowed( false );
			//this.tableChecklist.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
			 
			this.tableChecklist.getColumnModel().getColumn(0).setResizable(false);
			this.tableChecklist.getColumnModel().getColumn(0).setPreferredWidth( fm.stringWidth( hCol0 ) );
			this.tableChecklist.getColumnModel().getColumn(0).setMaxWidth( fm.stringWidth( hCol0 ) * 2 );
			this.tableChecklist.getColumnModel().getColumn(0).setMinWidth( fm.stringWidth( hCol0 ) );
			//this.tableChecklist.getColumnModel().getColumn(1).setPreferredWidth(125);
			
			this.tableChecklist.addMouseListener( new MouseAdapter() 
			{
				@Override
				public void mouseClicked(MouseEvent e) 
				{
					JTable tb = (JTable)e.getSource();
					
					int row = tb.rowAtPoint( e.getPoint() );
					int col = tb.columnAtPoint( e.getPoint() );
					
					if( row == 1 && col == 1 )
					{
						changeCheckNumberSelectedStreams( Language.getLocalCaption( Language.CHECK_SELECTED_DATA_STREAMS_MSG ), row, col );
					}
					else if( row == 2 && col == 1 )
					{
						changeCheckNumberSelectedStreams( Language.getLocalCaption( Language.CHECK_SELECTED_SYNC_STREAMS_MSG ), row, col );
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
				                    tip = getValueAt(rowIndex, colIndex).toString();
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
												/**
												 * 
												 */
												private static final long serialVersionUID = 4144425414849985295L;

												public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
											    {	
											        Component cellComponent = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column);
											        
											        if( !table.isCellEditable( row, column ) )
											        {	
											        	cellComponent.setBackground( new Color( 255, 255, 150 ) );
											        }
											        else
											        {
											        	if( isSelected )
											        	{
											        		cellComponent.setBackground( new Color( 174, 214, 241 ) );											        		
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
		
		table.getTableHeader().setReorderingAllowed( false );
		
		return table;
	}
	
	private void changeCheckNumberSelectedStreams( String msg, int row, int col )
	{
		String errMsg = "";
		//String msg = Language.getLocalCaption( Language.CHECK_SELECTED_DATA_STREAMS_MSG );
		
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
		TableModel tm =  new DefaultTableModel( null, new String[] { Language.getLocalCaption( Language.SELECT_TEXT), Language.getLocalCaption( Language.CHECKLIST_TEXT )  } )
							{
								private static final long serialVersionUID = 1L;
								
								Class[] columnTypes = new Class[]{ Boolean.class, String.class };								
								boolean[] columnEditables = new boolean[] { true, true };
																
								public Class getColumnClass(int columnIndex) 
								{
									return columnTypes[ columnIndex ];
								}
																								
								public boolean isCellEditable(int row, int column) 
								{
									boolean editable = ( row > 2 || column < 1 ) ? columnEditables[ column ] : false;
									
									return editable;
								}
							};
		return tm;
	}
	
	private void createNewMsg2Checklist( boolean sel, String msg )
	{
		JTable chlistTb = this.getChecklistTable();
		
		chlistTb.setVisible( false );
		
		DefaultTableModel chlistTm = (DefaultTableModel)chlistTb.getModel();
		
		chlistTm.addRow( new Object[]{ sel, msg} );
		
		chlistTb.setVisible( true );
	}
	
	private void removeMsgsFromChecklist( )
	{
		JTable chlistTb = this.getChecklistTable();
		
		chlistTb.setVisible( false );
		
		DefaultTableModel chlistTm = (DefaultTableModel)chlistTb.getModel();		
		
		int[] index = chlistTb.getSelectedRows();
		
		Arrays.sort( index );
		
		for( int i = index.length - 1; i >= 0; i-- )
		{			
			int r = index[ i ];
				
			if( r > 0 )
			{
				chlistTm.removeRow( r );			
			}
		}
		
		chlistTb.setVisible( true );
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
					
					if( row > 2 && index > 2 )
					{
						tmSource.moveRow( index, index, row );
						
						if( i == 0 )
						{
							source.setRowSelectionInterval( row, row );
						}
					}
				}				
			}
		}
	}	
}
