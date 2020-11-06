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

package lslrec.controls;

import java.util.HashMap;
import java.util.Map;

import lslrec.config.ParameterList;

/**
 * 
 * @author Manuel Merino Monge
 *
 */
public class MinionParameters 
{	
	private Map< String, ParameterList > minionParameters = new HashMap< String, ParameterList >();
	
	/**
	 * Add or replace minion parameter's list.
	 * 
	 * @param minionID 	-> minion identifier.
	 * @param pars		-> parameter list.
	 */
	public void setMinionParameters( String minionID, ParameterList pars )
	{
		this.minionParameters.put( minionID, pars );
		
	}
	
	/**
	 * 
	 * @param minioID	-> minion identifier.
	 * @return	List of parameters of minion or null if the identifier is not found.
	 */
	public ParameterList getMinionParameters( String minioID )
	{
		return this.minionParameters.get( minioID ); 
	}
}
