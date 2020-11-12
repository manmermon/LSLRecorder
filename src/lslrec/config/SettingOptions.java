/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.  https://github.com/manmermon/LSLRecorder
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
 *
 */
package lslrec.config;

import java.util.ArrayList;
import java.util.List;


public class SettingOptions
{	
	public enum Type 
	{
		STRING, NUMBER, BOOLEAN 
	}
	
	private String ID;
	private List< String > values = null;
	private Type type;
	
	private boolean listType = false;
	private String refPar = null;
	
	private int selectedOpt =  -1;
	
	/**
	 * 
	 * @param id
	 * @param t
	 * @param isList
	 * @param idRefParameter
	 */
	public SettingOptions( String id, Type dataType, boolean isList, String idRefParameter  ) 
	{
		if( id == null || id.isEmpty() )
		{
			throw new IllegalArgumentException( "Input null or empty.");
		}
		
		if( idRefParameter == null || idRefParameter.isEmpty() )
		{
			throw new IllegalArgumentException( "ID of referenced parameter null or empty.");
		}
		
		this.ID = id;
		
		this.values = new ArrayList< String >( );
		this.type = dataType;
		
		this.listType = isList;
				
		this.refPar = idRefParameter;
	}
		
	/**
	 * 
	 * @param value
	 * @throws IllegalArgumentException
	 */
	public void addValue( String value ) 
	{
		if( !this.checkType( value ) )
		{
			throw new IllegalArgumentException( "String argument is null and is not equal"
													+ ", ignoring case, to the string of " 
													+ this.type.name() );
		}
		
		if( this.listType )
		{
			this.values.add( value );
		}
		else
		{
			this.values.clear();
			this.values.add( value );
		}
	}
	
	private boolean checkType( String value )
	{
		boolean ok = true;
		
		switch ( this.type )
		{
			case STRING:
			{
				break;
			}
			case NUMBER:
			{
				try
				{
					Double.parseDouble( value );
				}
				catch (Exception e) 
				{
					ok = false;
				}
				
				break;
			}
			case BOOLEAN:
			{
				try
				{
					Boolean.parseBoolean( value );
				}
				catch (Exception e) 
				{
					ok = false;
				}
				
				break;
			}
			default:
			{
				break;
			}
		}
		
		return ok;		
	}
	
	public String getIDReferenceParameter()
	{
		return this.refPar;
	}
	
	public void removeValue( Object value )
	{
		this.values.remove( value );
	}
		
	public String getID() 
	{
		return ID;
	}
		
	public boolean isList()
	{
		return this.listType;
	}	
	
	/**
	 * 
	 * @return A copy of values
	 */
	public String[] getOptions() 
	{
		return this.values.toArray( new String[0] );
	}
	
	public void setSelectedValue( int index )
	{
		this.selectedOpt = index;
	}
	
	public int getSelectedValue()
	{
		return this.selectedOpt;
	}
	
	public Type getDataType()
	{
		return this.type;
	}
}
