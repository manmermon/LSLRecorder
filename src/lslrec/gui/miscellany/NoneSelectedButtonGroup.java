package lslrec.gui.miscellany;
/*
 * Copyright 2011-2013 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of CLIS.
 *
 *   CLIS is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CLIS is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CLIS.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */

import java.awt.Component;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

public class NoneSelectedButtonGroup extends Component
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ButtonGroup grp;

	public NoneSelectedButtonGroup() 
	{
		this.grp = new ButtonGroup()
				{
					@Override
					public void setSelected( ButtonModel model, boolean selected) 
					{
						if( selected ) 
						{
							super.setSelected( model, selected );
						}
						else
						{
							ButtonModel b = super.getSelection(); 
							if( b == null || b.equals( model ) )
							{
								clearSelection();
							}
						}
					}
				};
	}	
	
	public void add( AbstractButton bt )
	{
		this.grp.add( bt );		
	}
	
	public void clearSelection()
	{
		this.grp.clearSelection();		
	}
	
	public int getButtonCount()
	{		
		return this.grp.getButtonCount();		
	}
	
	public Enumeration< AbstractButton > getElements()
	{
		return this.grp.getElements();
	}
	
	public ButtonModel getSelection()
	{
		return this.grp.getSelection();	
	}
	
	public int hashCode()
	{
		return this.grp.hashCode();
	}
	
	public boolean isSelected( ButtonModel b )
	{		
		return this.grp.isSelected( b );
	}
	
	public void remove( AbstractButton b )
	{		
		this.grp.remove(b );
	}
	
	public void setSelected( ButtonModel m, boolean b )
	{
		this.grp.setSelected( m, b );
	}	
	
	public void removeAllButtons()
	{
		Enumeration< AbstractButton > bts = this.grp.getElements();
		
		while( bts.hasMoreElements() )
		{
			this.grp.remove( bts.nextElement() );
		}
	}
}

