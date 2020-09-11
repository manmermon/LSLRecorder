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

package lslrec.config;

public class Parameter implements IParameter
{
	private String ID;
	private Object value = null;

	/**
	 * Create a parameter
	 * 
	 * @param id: parameter identifier.
	 * @param defaultValue: parameter value.
	 * @throws IllegalArgumentException: 
	 * 			if id == null || id.trim().isempty() || defaultValue == null  
	 */
	public Parameter(String id, Object defaultValue) throws IllegalArgumentException
	{

		if ( id == null || id.trim().isEmpty() || defaultValue == null )
		{
			throw new IllegalArgumentException( "Parameter ID and/or value are null or empty" );
		}

		this.ID = id;
		this.value = defaultValue;
	}

	/**
	 * 
	 * @return parameter's identifier
	 */
	public String getID()
	{
		return this.ID;
	}
	
	/**
	 * Set parameter identifier.
	 * 
	 * @param ID -> parameter's identifier
	 * 
	 * @exception IllegalArgumentException if ID is null, empty, or whitespace.
	 */
	public void setID( String id ) throws IllegalArgumentException
	{
		if( id == null || id.trim().isEmpty() || id.isEmpty() )
		{
			throw new IllegalArgumentException( "ID is null, whitespace, or empty.");
		}
		this.ID = id;
	}
	
	/**
	 * 
	 * Set parameter value
	 * 
	 * @param newValue: new value
	 * @return previous value
	 * @throws ClassCastException: Class new value is different
	 */
	public Object setValue( Object newValue ) throws ClassCastException
	{
		String parClass = this.value.getClass().getCanonicalName();
		if ( !parClass.equals( newValue.getClass().getCanonicalName() ) )
		{
			throw new ClassCastException("New value class is different to parameter class: " + parClass );
		}

		Object prev = this.value;
		this.value = newValue;

		return prev;
	}

	/**
	 * 
	 * @return parameter value
	 */
	public Object getValue()
	{
		return this.value;
	}
}