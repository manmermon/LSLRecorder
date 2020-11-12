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

package lslrec.dataStream;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;
import lslrec.dataStream.family.stream.lsl.LSLUtils;

public class TemporalDataStream
{
	private BufferedInputStream data;
	private boolean CloseStream = false;
	
	private boolean isSkipDataBinHeader = false;
	
	private StreamDataType dataType;
	
	private int maxNumElements;
	
	private byte[] buf = new byte[ Float.BYTES ];
		
	public TemporalDataStream( IStreamSetting stream, int streamDataLength, BufferedInputStream inStream ) throws Exception
	{
		if( stream == null )
		{
			throw new IllegalArgumentException( "Stream setting null.");
		}
		
		this.dataType = stream.data_type();
		this.maxNumElements = streamDataLength;
		
		this.data = inStream;
		
		this.buf = new byte[ stream.getDataTypeBytes( stream.data_type() ) ];
	}
	
	public StreamDataType getDataType()
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
			case double64:
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
			case float32:
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
			case int8:
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
			case int16:
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
			case int32:
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
			case int64:
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
