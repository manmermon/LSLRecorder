/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec and CLIS.
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

package gui.miscellany;
import java.awt.Component;
import java.text.ParseException;

import javax.swing.DefaultCellEditor;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

public class SpinnerNumberCellEditor extends DefaultCellEditor
{
	private static final long serialVersionUID = 1L;
	private JSpinner spinner;

    public SpinnerNumberCellEditor( JSpinner sp )
    {
    	super( new JTextField() );    	    	
    	spinner = sp;
    }
    
    public Component getTableCellEditorComponent(
    	JTable table, Object value, boolean isSelected, int row, int column)
    {
    	try
    	{
    		spinner.setValue( value );
    	}
    	catch( Exception e)
    	{
    		Comparable v =((SpinnerNumberModel)spinner.getModel()).getMinimum();
    		spinner.setValue( v );
    	}
    	
    	return spinner;
    }
    
    public Object getCellEditorValue()
    {
    	try 
    	{
			spinner.commitEdit();
		}
    	catch (ParseException e) 
    	{}
    	
    	return spinner.getValue();
    }    
}