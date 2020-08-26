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

package dataStream;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLUtils;

public class TemporalDataStream
{
	private BufferedInputStream data;
	private boolean CloseStream = false;
	
	private boolean isSkipDataBinHeader = false;
	
	private int dataType;
	
	private int maxNumElements;
	
	private byte[] buf = new byte[ Float.BYTES ];
		
	public TemporalDataStream( int datType, int streamDataLength, BufferedInputStream inStream ) throws Exception
	{
		this.dataType = datType;
		this.maxNumElements = streamDataLength;
		
		this.data = inStream;
		
		this.buf = new byte[ LSLUtils.getDataTypeBytes( this.dataType ) ];
	}
	
	public int getDataType()
	{
		return this.dataType;
	}
	
	public List< Object > getData() throws Exception
	{
		List< Object > data = new ArrayList< Object >();
		
		if( !this.CloseStream )
		{
			try
			{
				data = this.readDataFromBinaryFile();
			}
			catch ( EOFException e) 
			{
				this.closeStream();
			}
		}
		
		return data; 
	}
	
	public void closeStream() throws IOException
	{
		this.CloseStream = true;
		this.data.close();
	}
	
	private List< Object > readDataFromBinaryFile( ) throws Exception
	{
		List< Object > Data = new ArrayList< Object >();
		
		if( !this.isSkipDataBinHeader )
		{
			this.isSkipDataBinHeader = true;
			
			try
			{
				byte[] aux = new byte[ 1 ];
				while( this.data.read( aux ) > 0 )
				{
					if( (new String( aux ) ).equals( "\n" ) )
					{
						break;
					}
				}
			}
			catch (Exception e) 
			{
			}		
		}
		
		switch( this.dataType ) 
		{
			case( LSL.ChannelFormat.double64 ):
			{
				while( this.data.read( this.buf ) > 0 )
				{
					Data.add( ByteBuffer.wrap( this.buf ).order( ByteOrder.BIG_ENDIAN ).getDouble() );
					
					if( Data.size() >= this.maxNumElements )
					{
						break;
					}
				}
				break;
			}
			case( LSL.ChannelFormat.float32 ):
			{
				while( this.data.read( this.buf ) > 0 )
				{
					Data.add( ByteBuffer.wrap( this.buf ).order( ByteOrder.BIG_ENDIAN ).getFloat() );
					
					if( Data.size() >= this.maxNumElements )
					{
						break;
					}
				}
				break;
			}
			case( LSL.ChannelFormat.int8 ):
			{
				while( this.data.read( this.buf ) > 0 )
				{
					Data.add( (new Byte( this.buf[ 0 ] ) ).byteValue() );
					
					if( Data.size() >= this.maxNumElements )
					{
						break;
					}
				}
				
				break;
			}
			case( LSL.ChannelFormat.int16 ):
			{
				while( this.data.read( this.buf ) > 0 )
				{
					Data.add( ByteBuffer.wrap( this.buf ).order( ByteOrder.BIG_ENDIAN ).getShort() );
					
					if( Data.size() >= this.maxNumElements )
					{
						break;
					}
				}
				
				break;
			}
			case( LSL.ChannelFormat.int32 ):
			{
				while( this.data.read( this.buf ) > 0 )
				{
					Data.add( ByteBuffer.wrap( this.buf ).order( ByteOrder.BIG_ENDIAN ).getInt() );
					
					if( Data.size() >= this.maxNumElements )
					{
						break;
					}
				}
				break;
			}
			case( LSL.ChannelFormat.int64 ):
			{
				while( this.data.read( this.buf ) > 0 )
				{
					Data.add( ByteBuffer.wrap( this.buf ).order( ByteOrder.BIG_ENDIAN ).getLong() );
					
					if( Data.size() >= this.maxNumElements )
					{
						break;
					}
				}
				break;
			}
			default: // String
			{					
				while( this.data.read( this.buf ) > 0 )
				{
					if( this.buf.length > 1 )
					{
						Data.add( ByteBuffer.wrap( this.buf ).order( ByteOrder.BIG_ENDIAN ).getChar() );
					}
					else
					{
						Data.add( new Character( (char)this.buf[ 0 ] ) );
					}
					
					if( Data.size() >= this.maxNumElements )
					{
						break;
					}
				}
				break;
			}			
		}
				
		return Data;
	}
}
