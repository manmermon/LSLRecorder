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
 */
package lslrec.auxiliar.extra;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

public class ArrayTreeMap< X, Y > extends AbstractMap< X, List< Y > > implements Cloneable 
{
	private final TreeMap< X, List< Y > > tree;
	
	/**
	 * Constructs a new, empty tree map, using the natural ordering of its keys. 
	 * All keys inserted into the map must implement the Comparable interface. 
	 * Furthermore, all such keys must be mutually comparable: k1.compareTo(k2) 
	 * must not throw a ClassCastException for any keys k1 and k2 in the map. If 
	 * the user attempts to put a key into the map that violates this constraint 
	 * (for example, the user attempts to put a string key into a map whose keys 
	 * are integers), the put(Object key, Object value) call will throw a 
	 * ClassCastException.
	 */
	public ArrayTreeMap() 
	{
		this.tree = new TreeMap< X, List< Y > >();
	}
	
	/**
	 * Constructs a new, empty tree map, ordered according to the given comparator. 
	 * All keys inserted into the map must be mutually comparable by the given comparator: 
	 * comparator.compare(k1, k2) must not throw a ClassCastException for any keys k1 and k2 
	 * in the map. If the user attempts to put a key into the map that violates this constraint, 
	 * the put(Object key, Object value) call will throw a ClassCastException. 
	 * @param com - the comparator that will be used to order this map. If null, the natural ordering of the keys will be used.
	 */
	public ArrayTreeMap( Comparator< ? super X > com )
	{
		this.tree = new TreeMap< X, List< Y > >( com );		
	}
	
	/**
	 * Copies all of the mappings from the specified ArrayTreeMap to this ArrayTreeMap.
	 * @param arrayTree - ArrayTreeMap to be stored in this map
	 * @throws ClassCastException - if the specified key cannot be compared with the keys currently in the map
	 * @throws NullPointerException - if the specified key is null and this map uses natural ordering, 
	 * 								or its comparator does not permit null keys
	 */
	public void putAll( ArrayTreeMap< X, Y > arrayTree )
	{
		for( X key : arrayTree.keySet() )
		{
			List< Y > vals = arrayTree.get( key );
			if( vals != null )
			{
				for( Y val : vals )
				{
					this.putElement(key, val );
				}
			}
		}
	}
	
	/**
	 * 
	 * @return Get the comparator uses to order the keys.
	 */
	public Comparator< ? super X > comparator()
	{
		return this.tree.comparator();
	}
	
	/**
	 * Removes the first instance of the value for this key from this TreeMap if present.
	 * @param key - key for which mapping should be removed	  			
	 * @param val - value for which mapping should be removed 
	 * @return Returns true if this map contains more instance of the value for for the specified key.
	 * @throws ClassCastException - if the specified key cannot be compared with the keys currently in the map
	 * @throws NullPointerException - if the specified key is null and this map uses natural ordering, or its comparator does not permit null keys
	 */
	public boolean removeValue( X key, Y val )
	{
		List< Y > VALS = this.tree.get( key );
		
		if( VALS != null )
		{
			VALS.remove( val );
		}
		
		return VALS.contains( val );
	}
		
	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractMap#clone()
	 */

    /**
     *
     * @return
     * @throws CloneNotSupportedException
     */

	@Override
	protected Object clone() throws CloneNotSupportedException 
	{
		TreeMap< X, List< Y > > clon = new TreeMap< X, List< Y > >( this.tree.comparator() );
		
		for( X key : this.tree.keySet() )
		{
			List< Y > copy = new ArrayList< Y >();
			List< Y > org = this.tree.get( key );
			if( org != null )
			{
				Collections.copy( copy, org );
				clon.put( key, copy );
			}			
		}
		
		return clon;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractMap#put(java.lang.Object, java.lang.Object)
	 */

    /**
     *
     * @param key
     * @param values
     * @return
     */

	@Override
	public List< Y > put( X key, List< Y > values ) 
	{
		List< Y > VALS = this.createValuesList( key );
		
		VALS.addAll( values );
		
		return VALS;
	}

        
	/**
	 * Associates the specified value with the specified key in this map. 
	 * If the map previously contained a mapping for the key, 
	 * the old value is replaced.
	 * @param key - key with which the specified value is to be associated
	 * @param value - value to be associated with the specified key
	 * @return  the previous value associated with key, or null if there 
	 * 			was no mapping for key. (A null return can also indicate 
	 * 			that the map previously associated null with key).
	 * @throws ClassCastException - if the specified key cannot be compared 
	 * 									with the keys currently in the map
     * @throws NullPointerException - if the specified key is null and this 
     *									map uses natural ordering, or its 
     *									comparator does not permit null keys
	 */
	public List< Y > putElement( X key, Y value )
	{
		List< Y > VALS = this.createValuesList( key );
		
		VALS.add( value );		
		
		return VALS;
	}
	
	/**
	 *  Create a value list for the key.
	 * @param key  - key with which the specified value is to be associated
	 * @return List of values for the key.
	 */
	private List< Y > createValuesList( X key )
	{
		List< Y > VALS = this.tree.get( key );
		
		if( VALS == null )
		{
			VALS = new ArrayList< Y >();
			this.tree.put( key, VALS );
		}
		
		return VALS;
	}
	
	/*
	 *  (non-Javadoc)
	 * @see java.util.AbstractMap#remove(java.lang.Object)
	 */
	@Override
	public List< Y > remove( Object key ) 
	{
		return this.tree.remove( key );
	}

	/**
	 * Clear the list of specified values associate with the specified key in this map.
	 * 
	 * @param key - key with which the specified value is to be associated
	 * @throws ClassCastException - if the specified key cannot be compared 
	 * 									with the keys currently in the map
     * @throws NullPointerException - if the specified key is null and this 
     *									map uses natural ordering, or its 
     *									comparator does not permit null keys
	 */
	public void emptyArray( X key )
	{
		List< Y > values = this.tree.get( key );
		if( values != null )
		{
			values.clear();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractMap#size()
	 */
	@Override
	public int size() 
	{
		return this.tree.size();
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractMap#containsKey(java.lang.Object)
	 */
	@Override
	public boolean containsKey( Object key ) 
	{
		return this.tree.containsKey( key );
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractMap#containsValue(java.lang.Object)
	 */
	@Override
	public boolean containsValue( Object val ) 
	{
		boolean cont = false;	
		
		for( List< Y > values : this.tree.values() )
		{
			cont = values.contains( val );
			
			if( cont )
			{
				break;
			}
		}
		
		return cont;
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractMap#get(java.lang.Object)
	 */
	@Override
	public List<Y> get( Object key ) 
	{
		return this.tree.get( key );
	}

	/*
	 * (non-Javadoc)
	 * @see java.util.AbstractMap#entrySet()
	 */
	@Override
	public Set< Entry< X, List< Y > > > entrySet() 
	{
		return this.tree.entrySet();
	}
}
