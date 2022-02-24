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
package lslrec.dataStream.binary;

import lslrec.dataStream.tools.StreamUtils.StreamDataType;

public class BinaryDataFormat 
{
	public static int UNKNOW_CHUCKSIZE = -1;
	
	private StreamDataType dType = StreamDataType.float32;
	private int bytes = 1;
	private long samples = 1;	
	
	private BinaryDataFormat lenFormat = null;
	
	public BinaryDataFormat()
	{		
	}
	
	public BinaryDataFormat( StreamDataType dataType, int byteLength, long chunckSize )
	{
		this.dType = dataType;
		this.bytes = byteLength;
		this.samples = chunckSize;
	}
	
	public BinaryDataFormat( StreamDataType dataType, int byteLength, BinaryDataFormat len )
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
	
	public StreamDataType getDataType() 
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
