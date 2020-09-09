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
package testing.DataStream;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.ucsd.sccn.LSL;

public class testBufferedInputStreams 
{

	public static void main(String[] args) 
	{
		String path = "G:\\Sync_datos\\WorkSpace\\GitHub\\LSLRecorder\\";
		String file = "testOpenFiles.temp0";
		
		File f = new File( path + file );
		
		System.out.println("testBufferedInputStreams.main() OPEN ");
		
		try 
		{
			BufferedInputStream stream1 = new BufferedInputStream( new  FileInputStream( file ) );
			BufferedInputStream stream2 = new BufferedInputStream( new  FileInputStream( file ) );
			
			byte[] buf = new byte[ Double.BYTES ]; 
			
			for( int i = 0; i < 10; i++ )
			{
				System.out.println("testBufferedInputStreams.readDataFromBinaryFile VALUE Stream 1 " + readDataFromBinaryFile( stream1, LSL.ChannelFormat.double64, buf ) );
			}
			
			for( int i = 0; i < 10; i++ )
			{
				System.out.println("testBufferedInputStreams.readDataFromBinaryFile VALUE Stream 2 " + readDataFromBinaryFile( stream2, LSL.ChannelFormat.double64, buf ) );
			}
			
		} 
		catch ( Exception e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			System.out.println("testBufferedInputStreams.main() CLOSE ");
		}		

	}
	
	private static Number readDataFromBinaryFile( BufferedInputStream syncStream, int dataType, byte[] buf ) throws Exception
	{				
		Number value = null;
		
		switch( dataType ) 
		{
			case( LSL.ChannelFormat.double64 ):
			{
				if( syncStream.read( buf ) > 0 )
				{
					value = ByteBuffer.wrap( buf ).order( ByteOrder.BIG_ENDIAN ).getDouble();					
				}
				
				break;
			}
			case( LSL.ChannelFormat.float32 ):
			{
				if( syncStream.read( buf ) > 0 )
				{
					value = ByteBuffer.wrap( buf ).order( ByteOrder.BIG_ENDIAN ).getFloat();					
				}
				break;
			}
			case( LSL.ChannelFormat.int8 ):
			{
				if( syncStream.read( buf ) > 0 )
				{
					value = (new Byte( buf[ 0 ] ) ).byteValue();					
				}
				
				break;
			}
			case( LSL.ChannelFormat.int16 ):
			{
				if( syncStream.read( buf ) > 0 )
				{
					value = ByteBuffer.wrap( buf ).order( ByteOrder.BIG_ENDIAN ).getShort();
				}
				
				break;
			}
			case( LSL.ChannelFormat.int32 ):
			{
				if( syncStream.read( buf ) > 0 )
				{
					value = ByteBuffer.wrap( buf ).order( ByteOrder.BIG_ENDIAN ).getInt() ;
					
				}
				
				break;
			}
			default: // Int 64
			{	
				if( syncStream.read( buf ) > 0 )
				{
					value = ByteBuffer.wrap( buf ).order( ByteOrder.BIG_ENDIAN ).getLong();
				}
				
				break;				
			}			
		}
				
		return value;
	}

}
