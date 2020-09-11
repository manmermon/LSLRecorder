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

package lslrec.gui.miscellany;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;

import javax.swing.AbstractButton;
import javax.swing.ButtonModel;

public class SelectedButtonGroup extends Component
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private List< AbstractButton > grp;

	public SelectedButtonGroup() 
	{
		this.grp = new ArrayList< AbstractButton >();
	}	
	
	public void add( AbstractButton bt )
	{
		this.grp.add( bt );		
	}
	
	public void clearSelection()
	{
		this.grp.clear();		
	}
	
	public int getButtonCount()
	{		
		return this.grp.size();		
	}
	
	public Enumeration< AbstractButton > getElements()
	{
		return ( new Vector< AbstractButton >( this.grp ) ).elements();
	}
	
	public List< ButtonModel > getSelections()
	{
		List< ButtonModel > aux = new ArrayList< ButtonModel >();
		for( AbstractButton bt : this.grp )
		{
			if( bt.isSelected() )
			{
				aux.add( bt.getModel() );
			}
		}
		return aux;	
	}
	
	public int hashCode()
	{
		return this.grp.hashCode();
	}
	
	public boolean isSelected( ButtonModel b )
	{		
		boolean selected = false;
		
		for( AbstractButton bt : this.grp )
		{
			if( bt.getModel().equals( b ) )
			{
				selected = bt.isSelected();
			}
		}
		
		return selected;
	}
	
	public void remove( AbstractButton b )
	{		
		this.grp.remove( b );
	}
	
	public void setSelected( ButtonModel m, boolean b )
	{
		for( AbstractButton bt : this.grp )
		{
			if( bt.getModel().equals( m ) )
			{
				bt.setSelected( b );
			}
		}
	}	
	
	public void removeAllButtons()
	{
		this.grp.clear();
	}
}

