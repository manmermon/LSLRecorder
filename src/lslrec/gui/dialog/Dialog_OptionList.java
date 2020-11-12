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
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import lslrec.config.language.Language;
import lslrec.gui.KeyActions;

import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;

/**
 * @author Manuel Merino Monge
 *
 */
public class Dialog_OptionList extends JDialog 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4689155807850024810L;
	
	private JPanel contentPanel;
	private JPanel buttonPanel;
	
	private JButton okBt;
	private JButton cancelBt;
	private JTable optionsTable;

	/**
	 * Create the dialog.
	 */
	public Dialog_OptionList() 
	{
		super.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
		
		super.getRootPane().registerKeyboardAction( KeyActions.getEscapeCloseWindows( "EscapeCloseWindow"), 
				KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0), 
				JComponent.WHEN_IN_FOCUSED_WINDOW );
		
		super.getContentPane().setLayout( new BorderLayout() );
	
		super.getContentPane().add( this.getOptionListPanel(), BorderLayout.CENTER);
		super.getContentPane().add( this.getButtonPanel(), BorderLayout.SOUTH);
	}
	
	public void setOptions( List< String > opts )
	{
		JTable t = getOptionsTable();
		DefaultTableModel tm = (DefaultTableModel)t.getModel();
		
		for( int i = tm.getRowCount() - 1; i >= 0; i-- )
		{
			tm.removeRow( i );
		}
		
		for( String op : opts )
		{
			tm.addRow( new String[] { op } );
		}
		
		t.clearSelection();
	}
	
	private JPanel getOptionListPanel()
	{
		if( this.contentPanel == null )
		{
			this.contentPanel = new JPanel();
			this.contentPanel.setLayout( new BorderLayout() );
			this.contentPanel.setBorder( new EmptyBorder( 5, 5, 5, 5 ) );
			
			this.contentPanel.add( this.getOptionsTable(), BorderLayout.CENTER );
		}
		
		return this.contentPanel;
	}
	
	public int[] getSelectedIndex()
	{
		JTable t = this.getOptionsTable();
		
		return t.getSelectedRows();
	}
	
	public String[] getSelectedItems()
	{
		JTable t = this.getOptionsTable();
				
		int[] indexes = t.getSelectedRows();
		
		String[] items = new String[ indexes.length ];
		
		for( int i = 0; i < indexes.length; i++ )
		{
			int index = indexes[ i ];
			
			items[ i ] = t.getValueAt( index, 0 ).toString();
		}
		
		return items;
	}
	
	private JPanel getButtonPanel( )
	{
		if( this.buttonPanel == null )
		{
			this.buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
			
			this.buttonPanel.add( this.getCancelButton() );
			this.buttonPanel.add( this.getOkButton() );
		}
		
		return this.buttonPanel;
	}
	
	public JButton getOkButton()
	{
		if( this.okBt == null )
		{
			this.okBt = new JButton( Language.getLocalCaption( Language.OK_TEXT ) );
			
			this.okBt.addActionListener( new ActionListener()
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					dispose();
				}
			});
		}
		
		return this.okBt;
	}
	
	public JButton getCancelButton()
	{
		if( this.cancelBt == null )
		{
			this.cancelBt = new JButton( Language.getLocalCaption( Language.CANCEL_TEXT ) );
			
			this.cancelBt.addActionListener( new ActionListener()
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					getOptionsTable().clearSelection();
					
					dispose();
				}
			});
		}
		
		return this.cancelBt;
	}
	
	private JTable getOptionsTable() 
	{
		if ( this.optionsTable == null) 
		{
			this.optionsTable = this.getCreateJTable();
			this.optionsTable.setModel( this.createTablemodel( Language.OPTIONS_TEXT ) );
			
			this.optionsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		}
		
		return this.optionsTable;
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
												public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
											    {
											        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
											        	
											        if( !table.isCellEditable( row, column ) )
											        {	
											        	cellComponent.setBackground( new Color( 255, 255, 224 ) );
											        	
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
		
		table.getTableHeader().setReorderingAllowed( false );
		
		return table;
	}
	
	private TableModel createTablemodel( String tableHeaderID )
	{					
		TableModel tm =  new DefaultTableModel( null, new String[] { Language.getLocalCaption( tableHeaderID ) } )
							{
								private static final long serialVersionUID = 1L;
								
								Class[] columnTypes = new Class[]{ String.class };								
								boolean[] columnEditables = new boolean[] { false };
								
								public Class getColumnClass(int columnIndex) 
								{
									return columnTypes[columnIndex];
								}
																								
								public boolean isCellEditable(int row, int column) 
								{
									boolean editable = columnEditables[ column ];
									
									return editable;
								}
							};
		return tm;
	}
}
