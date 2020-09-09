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
package lslrec.dataStream.outputDataFile.dataBlock;

public class StringBlock extends CharBlock 
{

	public StringBlock(int seqNum,String name, int nCols, String data) 
	{	
		super( seqNum, name, nCols, toCharacterArray( data ) );
	}
	
	private static Character[] toCharacterArray( String data )
	{
		char[] arr = data.toCharArray();
		Character[] d = new Character[ arr.length ];
		
		for( int i = 0; i < arr.length; i++ )
		{
			d[ i ] = arr[ i ];
		}
		
		return d;
	}

}
