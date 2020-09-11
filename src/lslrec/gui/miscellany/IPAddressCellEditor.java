package lslrec.gui.miscellany;

import java.awt.Component;
import java.net.Inet4Address;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;
import javax.swing.JTextField;

public class IPAddressCellEditor extends DefaultCellEditor
{
	private static final long serialVersionUID = 1L;
	private JTextField ipAddress;
	
	private String oldText = "";

    public IPAddressCellEditor( JTextField ip )
    {
    	super( ip );
    	ipAddress = ip;    	
    }

    public Component getTableCellEditorComponent(
    	JTable table, Object value, boolean isSelected, int row, int column)
    {
    	if( value == null )
    	{
    		oldText = Inet4Address.getLoopbackAddress().getHostAddress();
    	}
    	else
    	{
    		oldText = value.toString();    	
    	}
    	
    	ipAddress.setText( oldText );
    	
    	return ipAddress;
    }

    public Object getCellEditorValue()
    {    	    	
    	String v = checkText( ipAddress.getText());
    	
    	if( v.isEmpty() )
    	{
    		ipAddress.setText( oldText );
    	}
    	else
    	{
    		ipAddress.setText( v );
    	}

    	return ipAddress.getText();
    }
    
    private String checkText( String text )
    {
    	String txt = "";    	
    	if( IPAddressValidator.validate( text ))
    	{  
    		txt = text.replace( " ", "" );
    	}
    	
    	return txt;
    }
}