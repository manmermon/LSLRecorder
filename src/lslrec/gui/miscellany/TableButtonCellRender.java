package lslrec.gui.miscellany;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

public  class TableButtonCellRender extends DefaultTableCellRenderer
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -760365684571293966L;
	
	private JButton bt;
	
	private boolean hover = false;
	
	public TableButtonCellRender() 
	{
		super();
		
		this.bt = new JButton();		
	}
	
	@Override
	public Component getTableCellRendererComponent(JTable table, Object value
													, boolean isSelected, boolean hasFocus
													, int row, int column) 
	{
		
		Component com = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		
		boolean editable = table.getModel().isCellEditable(row, column);
        
		if( !this.hover )
		{
			Color c = ( com != null ) ? com.getBackground() : null;
			
			this.bt.setBackground( c );
		}
		else
		{
			this.bt.setBackground( UIManager.getColor( "Table.gridColor" ) );
		}
		
		this.bt.setEnabled( editable );
				
		return this.bt;
	}
	
	public void setHover( boolean hover )
	{
		this.hover = hover;
	}
	
	public JButton getButton()
	{
		return this.bt;
	}
}