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

public class Tuple<X, Y> 
{
	public final X x;
	public final Y y;
	
	public Tuple( X x, Y y)
	{
		this.x = x;
		this.y = y;
	}
	
	public static boolean isCorrectTypes( Object obj, Class xClass, Class yClass )
	{
		boolean correct = false;
		
		if( obj != null && xClass != null && yClass != null )
		{
			if( obj instanceof Tuple )
			{
				Tuple t = (Tuple)obj;
				correct = ( xClass.isInstance( t.x ) ) && ( yClass.isInstance( t.y ) );
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
			
			eq = tin.x.equals( this.x ) && tin.y.equals( this.y );
		}
				
		return eq;		
	}
	
	@Override
	public String toString() 
	{
		String out = "<";
		if( x != null )
		{
			out += x;
		}
		
		out += ",";
		
		if( y != null )
		{
			out += " " + y; 
		}
		
		out += ">";
		
		return out;
	}  
}
