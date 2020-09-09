/*
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
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

package lslrec.config;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 
 * @author Manuel Merino Monge
 *
 */
public class ParameterList 
{
	private Map< String, Parameter > lst = null;
	
	/**
	 * 
	 */
	public ParameterList() 
	{
		this.lst = new HashMap< String, Parameter >();
	}
	
	/**
	 * Add a parameter. If parameter exists, its value will be updated.  
	 * 
	 * @param par
	 */
	public void addParameter( Parameter par ) 
	{
		if( par != null )
		{
			Parameter p = this.lst.get( par.getID() );
			
			if( p == null )
			{
				this.lst.put( par.getID(), par );
			}
			else
			{
				p.setValue( par.getValue() );
			}
		}
	}
	
	/**
	 * 
	 * @param ID -> parameter identifier
	 * @return Parameter or null if ID is not registered.
	 */
	public Parameter getParameter( String ID )
	{
		return this.lst.get( ID );
	}
	
	/**
	 * 
	 * @return all parameter identifiers.
	 */
	public Set< String > getParameterIDs()
	{
		return this.lst.keySet();
	}
}
