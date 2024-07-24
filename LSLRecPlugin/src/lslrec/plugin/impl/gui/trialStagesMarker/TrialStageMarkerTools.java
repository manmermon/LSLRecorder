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
		TableModel tm =  new DefaultTableModel( null, new String[] { "Stage", "Mark", "Time (s)", "Auto" } )
							{
								private static final long serialVersionUID = 1L;
								
								Class[] columnTypes = new Class[]{ String.class, Integer.class, Integer.class, Boolean.class };								
								boolean[] columnEditables = new boolean[] { true, true, true, true };
								
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
