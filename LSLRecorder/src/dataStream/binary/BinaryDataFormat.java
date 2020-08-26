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
package dataStream.binary;

public class BinaryDataFormat 
{
	public static int UNKNOW_CHUCKSIZE = -1;
	
	private int dType = 1;
	private int bytes = 1;
	private long samples = 1;	
	
	private BinaryDataFormat lenFormat = null;
	
	public BinaryDataFormat()
	{		
	}
	
	public BinaryDataFormat( int dataType, int byteLength, long chunckSize )
	{
		this.dType = dataType;
		this.bytes = byteLength;
		this.samples = chunckSize;
	}
	
	public BinaryDataFormat( int dataType, int byteLength, BinaryDataFormat len )
	{
		this( dataType, byteLength, UNKNOW_CHUCKSIZE );
		
		if( len == null )
		{
			throw new  IllegalArgumentException( "Binary format of length of data is null" ); 
		}
		
		if( len.getChunckSize() == UNKNOW_CHUCKSIZE )
		{
			throw new  IllegalArgumentException( "Chunk size of Length format cannot be unknown." );
		}
		
		this.lenFormat = len;
	}
	
	public int getDataType() 
	{
		return this.dType;
	}
	
	public int getDataByteSize() 
	{
		return this.bytes;
	}
	
	public long getChunckSize() 
	{
		return this.samples;
	}
	
	public BinaryDataFormat getLengthFormat() 
	{
		return this.lenFormat;
	}
}
