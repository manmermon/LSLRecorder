/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018-2019 by Manuel Merino Monge <manmermon@dte.us.es>
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

package InputStreamReader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;

import InputStreamReader.OutputDataFile.Format.DataFileFormat;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLUtils;

public class TemporalData
{
	public static final int BLOCK_SIZE = (int)( 5 * ( Math.pow( 2, 20 ) ) ); 
	private int maxNumElements = BLOCK_SIZE / Float.BYTES; // 5 MB  
	
	//private byte[] buf = new byte[ Float.BYTES ];
	
	//private BufferedInputStream data;
	//private DataInputStream timeStampData;
	//private BufferedInputStream timeStampData;
	
	private int dataType;
	
	private String streamName;
	private String lslXML;
	private int CountChannels;
	
	private String outFileName = "./data" + DataFileFormat.getSupportedFileExtension( DataFileFormat.CLIS );
	private String outFileFormat = DataFileFormat.CLIS;
	
	private File binFile = null;
	private File timeFile = null;
	
	//private boolean dataStreamClose = false;
	//private boolean timeStreamClose = false;
	
	//private boolean isSkipDataBinHeader = false;
	//private boolean isSkipTimeBinHeader = false;
	
	private boolean deleteBinaries = true;
	
	private TemporalDataStream dataStream = null;
	private TemporalDataStream timeStream = null;
	
	private int dataTypeBytes;
	
	public TemporalData( File dataBin, File timeBin, int type, int nChannels, String name
						, String xml, String outName, String outputFormat
						, boolean delBinaries ) throws Exception
	{		
		//this.dataStreamClose = true;
		//this.timeStreamClose = true;
		
		this.deleteBinaries = delBinaries;
		
		this.outFileFormat = outputFormat;
		
		this.binFile = dataBin;
		this.timeFile = timeBin;
		
		this.dataType = type;
		this.CountChannels = nChannels;
		this.streamName = name;
		this.lslXML = xml;
		this.outFileName = outName;
		
		this.dataTypeBytes = LSLUtils.getDataTypeBytes( this.dataType );
		
		if( dataTypeBytes < 1 )
		{
			throw new Exception( "Data type unknown." );
		}
		
		this.maxNumElements = ( BLOCK_SIZE / this.dataTypeBytes ); 
				
		this.maxNumElements = (int)( ( Math.floor( 1.0D * this.maxNumElements / this.CountChannels ) ) * this.CountChannels );
		
		if( this.maxNumElements < this.CountChannels )
		{
			this.maxNumElements = this.CountChannels;
		}
		
		if( dataBin != null )
		{
			BufferedInputStream data = new BufferedInputStream( new FileInputStream( dataBin ) );
			this.dataStream = new TemporalDataStream( type, this.maxNumElements, data );
			//this.dataStreamClose = false;
		}
		
		if( timeBin != null )
		{
			//this.timeStampData = new DataInputStream( new FileInputStream( timeBin ) );
			BufferedInputStream timeStampData = new BufferedInputStream( new FileInputStream( timeBin ) );
			this.timeStream = new TemporalDataStream( LSL.ChannelFormat.double64, this.maxNumElements,  timeStampData );
			//this.timeStreamClose = false;
		}
	}
	
	public String getOutputFileFormat()
	{
		return this.outFileFormat;
	}

	public String getOutputFileName()
	{
		return this.outFileName;
	}
	
	/*
	public List<Object> getData() throws Exception
	{	
		List< Object > d = new ArrayList< Object >();
	
		if( !this.dataStreamClose )
		{
			try
			{
				d = this.readDataFromBinaryFile();
			}
			catch ( EOFException e) 
			{
				this.data.close();
				this.dataStreamClose = true;
			}
		}
		
		return d; 
	}
	*/
	
	public int getTypeDataBytes()
	{
		return this.dataTypeBytes;
	}

	/*
	public List< Double > getTimeStamp() throws Exception
	{
		List< Double > t = new ArrayList< Double >();
		
		if( !this.timeStreamClose )
		{
			try
			{
				t = this.readTimeBinaryFile();
			}
			catch ( EOFException e) 
			{
				this.timeStampData.close();
				this.timeStreamClose = true;
			}
		}
		
		return t; 
	}
	*/

	public int getDataType()
	{
		return this.dataType;
	}

	public int getNumberOfChannels()
	{
		return this.CountChannels;
	}

	public String getStreamingName()
	{
		return this.streamName;
	}

	public String getLslXml()
	{
		return this.lslXML;
	}
	
	public TemporalDataStream getDataStream()
	{
		return this.dataStream;
	}
	
	public TemporalDataStream getTimeStream()
	{
		return this.timeStream;
	}
	
	/*
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
	*/
		
	/*
	private List< Double > readTimeBinaryFile() throws Exception
	{
		List< Double > Time = new ArrayList< Double >();
		
		if( !this.isSkipTimeBinHeader )
		{
			this.isSkipTimeBinHeader = true;
			
			try
			{
				byte[] aux = new byte[ 1 ];
				while( this.timeStampData.read( aux ) > 0 )
				{					
					if( new String( aux ).equals( "\n" ) )
					{
						break;
					}
				}
			}
			catch (Exception e) 
			{
			}		
		}
		
		while( this.timeStampData.available() > 0 )
		{
			Time.add( this.timeStampData.readDouble() );
			
			if(  Time.size() >= this.maxNumElements )
			{
				break;
			}
		}

		return Time;
	}
	*/
	
	public long getDataBinaryFileSize()
	{
		long size = 0;
		
		if( binFile != null )
		{
			size = this.binFile.length();
		}
		
		return size;
	}
	
	public long getTimeBinaryFileSize()
	{
		long size = 0;
		
		if( this.timeFile != null )
		{
			size = this.timeFile.length();
		}
		return size;
	}
	
	public void closeTempBinaryFile()
	{
		try
		{
			/*
			if( this.data != null )
			{
				this.data.close();
			}
			
			if( this.timeStampData != null )
			{
				this.timeStampData.close();
			}
			*/
			
			if( this.dataStream != null )
			{
				this.dataStream.closeStream();
			}
			
			if( this.timeStream != null )
			{
				this.timeStream.closeStream();
			}
			
			if( this.binFile != null  && this.deleteBinaries )
			{
				if( !this.binFile.delete() )
				{
					this.binFile.deleteOnExit();
				}
			}

			if( this.timeFile != null && this.deleteBinaries )
			{
				if( !this.timeFile.delete() )
				{
					this.timeFile.deleteOnExit();
				}
			}
		}
		catch( Exception e )
		{}		
	}	
}