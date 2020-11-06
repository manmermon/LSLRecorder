/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.
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