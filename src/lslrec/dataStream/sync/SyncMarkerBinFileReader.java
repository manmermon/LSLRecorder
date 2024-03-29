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
package lslrec.dataStream.sync;

import java.io.EOFException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.dataStream.binary.BinaryDataFormat;
import lslrec.dataStream.binary.reader.ReaderBinaryFile;
import lslrec.dataStream.binary.setting.BinaryFileStreamSetting;
import lslrec.dataStream.outputDataFile.dataBlock.ByteBlock;
import lslrec.dataStream.tools.StreamUtils.StreamDataType;

public class SyncMarkerBinFileReader
{
	private File file = null;
	
	private boolean deleteBinFile = false;
	
	private boolean CloseStream = false;
	
	private StreamDataType markDataType;
	private StreamDataType timeDataType;
	
	private ReaderBinaryFile reader = null;
	
	public SyncMarkerBinFileReader( BinaryFileStreamSetting streamSetting, char headerEnd, boolean delBinaries ) throws Exception 
	{	
		this.deleteBinFile = delBinaries;
		
		this.file = streamSetting.getStreamBinFile();
		
		this.markDataType = streamSetting.getStreamSetting().data_type();
		this.timeDataType = streamSetting.getStreamSetting().getTimestampDataType();
		
		List< BinaryDataFormat > formats = new ArrayList< BinaryDataFormat >();
		
		BinaryDataFormat markFormat = new BinaryDataFormat( this.markDataType, streamSetting.getStreamSetting().getDataTypeBytes( this.markDataType ), 1 );
		formats.add( markFormat );
		
		BinaryDataFormat timeFormat = new BinaryDataFormat( this.timeDataType, streamSetting.getStreamSetting().getDataTypeBytes( this.timeDataType ), 1 );
		formats.add( timeFormat );
				
		this.reader = new ReaderBinaryFile( this.file, formats, headerEnd );		
	}
	
	/*
	private void OpenInputStream() throws FileNotFoundException 
	{
		this.syncStream = new BufferedInputStream( new  FileInputStream( this.file ) );
	}
	*/
	
	public String getHeader()
	{
		String header = "";
		
		if( this.reader != null )
		{
			header = this.reader.getHeader();
		}
		
		return header;
	}
	
	public SyncMarker getSyncMarker() throws Exception
	{
		SyncMarker mark = null;
		
		if( !this.CloseStream )
		{
			try
			{
				List<ByteBlock> block = this.reader.readDataFromBinaryFile();
				
				Number markValue = null;
				Number timeValue = null;				
				
				for( int i = 0; i < block.size(); i++ )
				{
					ByteBlock val = block.get( i );
					
					if( i == 0 )
					{						
						markValue = ( ConvertTo.Transform.ByteArrayTo( val.getData(), this.markDataType )[0] );
					}
					else if( i == 1 )
					{
						timeValue = ( ConvertTo.Transform.ByteArrayTo( val.getData(), this.timeDataType )[0] );
					}						
				}
				
				if( markValue != null && timeValue != null )
				{
					mark = new SyncMarker( markValue.intValue(), timeValue.doubleValue() );
				}
			}
			catch ( EOFException e) 
			{
				this.CloseStream = true;
			}
		}
		
		return mark;
	}
	
	/*
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
	*/
		
	public long getFileSize()
	{
		long size = 0;
		
		if( this.file != null )
		{
			size = this.file.length();
		}
		return size;
	}
	
	public void resetStream() throws Exception
	{
		if( this.reader != null )
		{
			this.CloseStream = false;
			this.reader.reset();
		}		
	}
	
	public void closeStream() throws Exception
	{
		this.CloseStream = true;
		
		if( this.reader != null )
		{
			this.reader.close();
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
