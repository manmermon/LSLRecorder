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

package lslrec.auxiliar.extra;

public class Tuple<X, Y> implements Comparable< Tuple<X, Y> > 
{
	public final X t1;
	public final Y t2;
	
	public Tuple( X t1, Y t2)
	{
		this.t1 = t1;
		this.t2 = t2;
	}
	
	public static boolean isCorrectTypes( Object obj, Class xClass, Class yClass )
	{
		boolean correct = false;
		
		if( obj != null && xClass != null && yClass != null )
		{
			if( obj instanceof Tuple )
			{
				Tuple t = (Tuple)obj;
				correct = ( xClass.isInstance( t.t1 ) ) && ( yClass.isInstance( t.t2 ) );
			}
		}
		
		return correct;
	}
	
	public static boolean isCorrectTypes( Object obj, Class cl )
	{	
		return isCorrectTypes( obj, cl, cl);
	}
	
	@Override
	public boolean equals( Object obj ) 
	{
		boolean eq = ( obj instanceof Tuple );
		
		if( eq )
		{
			Tuple tin = (Tuple)obj;
			
			eq = tin.t1.equals( this.t1 ) && tin.t2.equals( this.t2 );
		}
				
		return eq;		
	}
	
	@Override
	public String toString() 
	{
		String out = "<";
		if( t1 != null )
		{
			out += t1;
		}
		
		out += ",";
		
		if( t2 != null )
		{
			out += " " + t2; 
		}
		
		out += ">";
		
		return out;
	}

	@Override
	public int compareTo( Tuple<X, Y> o) 
	{
		int comp = 0;
		
		if( o != null )
		{
			X t1 = o.t1;
			Y t2 = o.t2;
			 
			String st1 = "" + this.t1 + this.t2;
			
			String st2 = "" + t1 + t2;
			
			comp = st1.compareTo( st2 );
		}
		
		return comp;
	}  
}
