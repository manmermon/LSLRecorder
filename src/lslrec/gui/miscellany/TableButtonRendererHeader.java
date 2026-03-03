package lslrec.gui.miscellany;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

public class TableButtonRendererHeader extends JButton implements TableCellRenderer 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -9091328110526347150L;
	
	protected TableButtonRendererHeader rendererComponent;
	protected int column;

	public TableButtonRendererHeader() 
	{
		rendererComponent = this;	
	}

	public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) 
	{
		if (table != null) 
		{
			JTableHeader header = table.getTableHeader();

			if (header != null) 
			{
				rendererComponent.setForeground( header.getForeground());
				rendererComponent.setBackground( header.getBackground());
				rendererComponent.setFont( header.getFont() );
			}
		}
		setColumn( column );

		setBorder( UIManager.getBorder( "TableHeader.cellBorder" ) );

		return rendererComponent;
	}

	protected void setColumn(int column) 
	{
		this.column = column;
	}

	public int getColumn() 
	{
		return column;
	}	
}