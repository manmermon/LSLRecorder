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
package lslrec.plugin.impl.gui.trialStagesMarker;

import java.awt.Color;
import java.awt.Component;
import java.awt.FontMetrics;
import java.awt.Point;
import java.awt.event.MouseEvent;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class TrialStageMarkerTools 
{
	private static JTable getCreateJTable()
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

	private static TableModel createTrialStageTablemodel( )
	{					
		TableModel tm =  new DefaultTableModel( null, new String[] { "Stage", "Mark", "Time (s)", "Auto", "Events (id=mark"+TrialStage.EVENTS_SEPARATOR + "...)" } )
							{
								private static final long serialVersionUID = 1L;
								
								Class[] columnTypes = new Class[]{ String.class, Integer.class, Integer.class, Boolean.class, String.class };								
								boolean[] columnEditables = new boolean[] { true, true, true, true, true };
								
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
	
	public static JTable createStageTrialTable()
	{
		JTable table = getCreateJTable();
		
		table.setModel( createTrialStageTablemodel( ) );
		
		FontMetrics fm = table.getFontMetrics( table.getFont() );			
		String hCol0 = table.getColumnModel().getColumn( 0 ).getHeaderValue().toString();
			
		int s = fm.stringWidth( " " + hCol0 + " " ) * 3;
		table.getColumnModel().getColumn( 0  ).setResizable( false );
		table.getColumnModel().getColumn( 0 ).setPreferredWidth( s );
		table.getColumnModel().getColumn( 0 ).setMaxWidth( s );
			
		table.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
			
		table.setPreferredScrollableViewportSize( table.getPreferredSize() );
		table.setFillsViewportHeight( true );
		
		return table;
	}	
}
