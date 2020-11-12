/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
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
 */

package lslrec.config;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.EventListenerList;

import lslrec.config.language.Language;

public class Parameter< T > implements IParameter< T >
{
	private String ID;
	private String txtID;
	private T value = null;
	
	private EventListenerList listenerList;

	/**
	 * Create a parameter
	 * 
	 * @param id: parameter identifier.
	 * @param defaultValue: parameter value.
	 * @throws IllegalArgumentException
	 */
	public Parameter(String id, T defaultValue) throws IllegalArgumentException
	{
		if ( id == null || defaultValue == null )
		{
			throw new IllegalArgumentException( "Parameter ID and/or value are null or empty" );
		}
		
		if( id.trim().isEmpty() )
		{
			throw new IllegalArgumentException( "Parameter ID and/or value are null or empty" );
		}

		this.ID = id.trim();
		this.value = defaultValue;
		
		this.listenerList = new EventListenerList();
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
		
		this.ID = id.trim();
	}
	
	/**
	 * 
	 * Set parameter value
	 * 
	 * @param newValue: new value
	 * @return previous value
	 * @throws ClassCastException: Class new value is different
	 * @throws NullPointerException: newValue null
	 */
	public T setValue( T newValue ) throws ClassCastException, NullPointerException
	{
		if( newValue == null )
		{
			throw new NullPointerException( "New value null." );
		}
		
		String parClass = this.value.getClass().getCanonicalName();
		if ( !parClass.equals( newValue.getClass().getCanonicalName() ) )
		{
			throw new ClassCastException("New value class is different to parameter class: " + parClass );
		}

		T prev = this.value;
		this.value = newValue;

		this.fireChangeEvent();
		
		return prev;
	}

	/**
	 * 
	 * @return parameter value
	 */
	public T getValue()
	{
		return this.value;
	}
	
	public void setLangID( String t )
	{
		this.txtID = t;
	}
	
	public String getLangID( )
	{
		return this.txtID;
	}
	
	public String getDescriptorText()
	{
		String t = Language.getLocalCaption( this.txtID );
		
		if( t == null || t.isEmpty() )
		{
			t = this.ID;
		}
			
		return t;
	}
		
	public void addValueChangeListener( ChangeListener listener )
	{
		this.listenerList.add( ChangeListener.class, listener );
	}
	
	/**
	 * 
	 */
	private synchronized void fireChangeEvent( )
	{
		ChangeEvent event = new ChangeEvent( this );

		ChangeListener[] listeners = this.listenerList.getListeners( ChangeListener.class );

		for (int i = 0; i < listeners.length; i++ ) 
		{
			listeners[ i ].stateChanged( event );
		}
	}
	
	@Override
	public String toString() 
	{
		
		return "<" + this.ID + ", " + this.value.toString() + ">";
	}
}