package lslrec.gui.miscellany;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public  class TableButtonCellRender extends DefaultTableCellRenderer
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -760365684571293966L;
	
	private JButton bt;
	
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
		
		this.bt.setBackground( com.getBackground() );
		
		return this.bt;
	}
	
	public JButton getButton()
	{
		return this.bt;
	}
}