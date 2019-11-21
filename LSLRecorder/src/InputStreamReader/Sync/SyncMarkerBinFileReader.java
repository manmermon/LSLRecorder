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
package InputStreamReader.Sync;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLUtils;

public class SyncMarkerBinFileReader
{
	private BufferedInputStream syncStream = null;
	
	private File file = null;
	
	private boolean deleteBinFile = false;
	
	private boolean CloseStream = false;
	
	private int markType;
	private int timeType;
	
	private byte[] markBuf = null;
	private byte[] timeBuf = null;
	
	public SyncMarkerBinFileReader( File syncFile, int markDataType, int timeDataType, boolean delBinaries ) throws Exception 
	{	
		this.deleteBinFile = delBinaries;
		
		this.file = syncFile;
		
		this.OpenInputStream();
		
		this.markType = markDataType;
		this.timeType = timeDataType;
		
		this.markBuf = new byte[ LSLUtils.getDataTypeBytes( markDataType ) ];
		this.timeBuf = new byte[ LSLUtils.getDataTypeBytes( timeDataType ) ];
	}
	
	private void OpenInputStream() throws FileNotFoundException 
	{
		this.syncStream = new BufferedInputStream( new  FileInputStream( this.file ) );
	}
		
	public SyncMarker getSyncMarker() throws Exception
	{
		SyncMarker mark = null;
		
		if( !this.CloseStream )
		{
			try
			{
				Number markValue = this.readDataFromBinaryFile( this.markType, this.markBuf );
				Number timeValue = this.readDataFromBinaryFile( this.timeType, this.timeBuf );
				
				if( markValue != null && timeValue != null )
				{
					mark = new SyncMarker( (Integer)markValue, (Double)timeValue );
				}
			}
			catch ( EOFException e) 
			{
				this.CloseStream = true;
			}
		}
		
		return mark;
	}
	
	private Number readDataFromBinaryFile( int dataType, byte[] buf ) throws Exception
	{				
		Number value = null;
		
		switch( dataType ) 
		{
			case( LSL.ChannelFormat.double64 ):
			{
				if( this.syncStream.read( buf ) > 0 )
				{
					value = ByteBuffer.wrap( buf ).order( ByteOrder.BIG_ENDIAN ).getDouble();					
				}
				
				break;
			}
			case( LSL.ChannelFormat.float32 ):
			{
				if( this.syncStream.read( buf ) > 0 )
				{
					value = ByteBuffer.wrap( buf ).order( ByteOrder.BIG_ENDIAN ).getFloat();					
				}
				break;
			}
			case( LSL.ChannelFormat.int8 ):
			{
				if( this.syncStream.read( buf ) > 0 )
				{
					value = (new Byte( buf[ 0 ] ) ).byteValue();					
				}
				
				break;
			}
			case( LSL.ChannelFormat.int16 ):
			{
				if( this.syncStream.read( buf ) > 0 )
				{
					value = ByteBuffer.wrap( buf ).order( ByteOrder.BIG_ENDIAN ).getShort();
				}
				
				break;
			}
			case( LSL.ChannelFormat.int32 ):
			{
				if( this.syncStream.read( buf ) > 0 )
				{
					value = ByteBuffer.wrap( buf ).order( ByteOrder.BIG_ENDIAN ).getInt() ;
					
				}
				
				break;
			}
			default: // Int 64
			{	
				if( this.syncStream.read( buf ) > 0 )
				{
					value = ByteBuffer.wrap( buf ).order( ByteOrder.BIG_ENDIAN ).getLong();
				}
				
				break;				
			}			
		}
				
		return value;
	}
		
	public long getFileSize()
	{
		long size = 0;
		
		if( this.file != null )
		{
			size = this.file.length();
		}
		return size;
	}
	
	public void resetStream() throws IOException
	{
		if( this.syncStream != null )
		{
			this.CloseStream = false;
			try
			{
				this.syncStream.reset();
			}
			catch( IOException ex )
			{
				this.syncStream.close();
				this.OpenInputStream();
			}
		}
	}
	
	public void closeStream() throws IOException
	{
		this.CloseStream = true;
		
		if( this.syncStream != null )
		{
			this.syncStream.close();
		}
	}
	
	public void closeAndRemoveTempBinaryFile()
	{
		try
		{	
			this.closeStream();
			
			if( this.deleteBinFile )
			{
				if( !this.file.delete() )
				{
					this.file.deleteOnExit();
				}
			}
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}		
	}
}
