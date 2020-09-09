/*
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
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
package testing.Auxiliar;

import java.util.ArrayList;
import java.util.List;

import Auxiliar.extra.NumberRange;

public class testRangeFilter 
{
	public static void main(String[] args) 
	{
		NumberRange r = new NumberRange( -10, -5 );
				
		List< NumberRange > rngs = new ArrayList<NumberRange>();
		rngs.add( new NumberRange( -15, -12 ) );
		rngs.add( new NumberRange( -15, -10 ) );
		rngs.add( new NumberRange( -15, -9 ) );
		rngs.add( new NumberRange( -10, -8 ) );	
		rngs.add( new NumberRange( -9, -8 ) );
		rngs.add( new NumberRange( -8, -5 ) );
		rngs.add( new NumberRange( -8, -3 ) );
		rngs.add( new NumberRange( -5, -3 ) );
		rngs.add( new NumberRange( -3, 0 ) );
		rngs.add( new NumberRange( -13, 0 ) );
		
		for( NumberRange rng : rngs )
		{
			System.out.println( "Is " + rng + " overlapped with " + r + " ? " + r.overlap( rng ) );
			System.out.println( "Is " + rng + " contained in " + r + " ? " + r.contain( rng ) );
			System.out.println();
		}
		System.out.println();
		
		r = new NumberRange( 0, 5 );
		rngs.clear();		
		rngs.add( new NumberRange( -2, -1 ) );
		rngs.add( new NumberRange( -2, 0 ) );	
		rngs.add( new NumberRange( -2, 1 ) );
		rngs.add( new NumberRange( 0, 1 ) );
		rngs.add( new NumberRange( 1, 3 ) );
		rngs.add( new NumberRange( 3, 5 ) );
		rngs.add( new NumberRange( 5, 7 ) );
		rngs.add( new NumberRange( 7, 10 ) );
		rngs.add( new NumberRange( -3, 10 ) );
		for( NumberRange rng : rngs )
		{
			System.out.println( "Is " + rng + " overlapped with " + r + " ? " + r.overlap( rng ) );
			System.out.println( "Is " + rng + " contained in " + r + " ? " + r.contain( rng ) );
			System.out.println();
		}
		System.out.println();
		
		r = new NumberRange( 10, 15 );
		rngs.clear();
		
		rngs.add( new NumberRange( 5, 7 ) );
		rngs.add( new NumberRange( 5, 10 ) );	
		rngs.add( new NumberRange( 5, 11 ) );
		rngs.add( new NumberRange( 11, 13 ) );
		rngs.add( new NumberRange( 13, 15 ) );
		rngs.add( new NumberRange( 13, 17 ) );
		rngs.add( new NumberRange( 15, 17 ) );
		rngs.add( new NumberRange( 17, 18 ) );
		rngs.add( new NumberRange( 3, 20 ) );
		for( NumberRange rng : rngs )
		{
			System.out.println( "Is " + rng + " overlapped with " + r + " ? " + r.overlap( rng ) );
			System.out.println( "Is " + rng + " contained in " + r + " ? " + r.contain( rng ) );
			System.out.println();
		}
		System.out.println();
	}	
	
}
