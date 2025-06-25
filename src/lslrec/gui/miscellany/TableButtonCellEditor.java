package lslrec.gui.miscellany;

import java.awt.Component;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTable;

public class TableButtonCellEditor extends DefaultCellEditor
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8756012456698563372L;

	private JButton bt;
	
	public TableButtonCellEditor( ) 
	{
		super( new JCheckBox() );
		
		this.bt = new JButton();
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected
													, int row, int column) 
	{	
		this.bt.setBackground( table.getSelectionBackground() );
		
		return this.bt;
	}
	
	public JButton getButton()
	{
		return this.bt;
	}
}
