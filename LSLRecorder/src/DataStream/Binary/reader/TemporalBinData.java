/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
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

package dataStream.binary.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import dataStream.binary.BinaryDataFormat;
import dataStream.outputDataFile.dataBlock.ByteBlock;
import dataStream.outputDataFile.format.DataFileFormat;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLUtils;

public class TemporalBinData
{	
	private int dataType;
	private int timeType;
	
	private String streamName;
	private String lslXML;
	private int CountChannels;
	private int ChunckSize;
	
	private String outFileName = "./data" + DataFileFormat.getSupportedFileExtension( DataFileFormat.CLIS_GZIP );
	private String outFileFormat = DataFileFormat.CLIS_GZIP;
	
	private ReaderBinaryFile reader = null;
	
	private File binFile = null;
	
	private boolean deleteBinaries = true;
	
	private boolean dataInterleave = false; 
	
	private int dataTypeBytes;
	private List< BinaryDataFormat > formats;
	
	private String encryptKey = null;
	
	private int strLenType;
	
	public TemporalBinData( File dataBin
						, int typeData
						, int nChannels
						, int chunckSize
						, boolean interleave
						, int typeTime
						, int typeOfStrLen
						, String name
						, String xml
						, String outName
						, String outputFormat
						, String encrypy_Key
						, boolean delBinaries ) throws Exception
	{	
		this.deleteBinaries = delBinaries;
		
		this.outFileFormat = outputFormat;

		this.binFile = dataBin;
		
		this.dataType = typeData;
		this.timeType = typeTime;
				
		this.CountChannels = nChannels;
		this.ChunckSize = chunckSize;
		this.streamName = name;
		this.lslXML = xml;
		this.outFileName = outName;
		
		this.encryptKey = encrypy_Key;
		
		this.strLenType = typeOfStrLen;
		
		this.dataTypeBytes = LSLUtils.getDataTypeBytes( this.dataType );
		
		if( this.dataTypeBytes < 1 )
		{
			throw new Exception( "Data type unknown." );
		}
		
		this.formats = new ArrayList<BinaryDataFormat>();
		
		if( this.dataType != LSL.ChannelFormat.string )
		{
			this.formats.add( new BinaryDataFormat( typeData, this.dataTypeBytes, this.CountChannels * this.ChunckSize ) );
		}
		else
		{
			BinaryDataFormat strLenFormat = new BinaryDataFormat( typeOfStrLen, LSLUtils.getDataTypeBytes( typeOfStrLen ), this.CountChannels * this.ChunckSize );
			this.formats.add( new BinaryDataFormat( typeData, this.dataTypeBytes, strLenFormat ) );
		}
		
		
		this.formats.add( new BinaryDataFormat( typeTime, LSLUtils.getDataTypeBytes( typeTime ), this.ChunckSize ) );
		
		this.reader = new ReaderBinaryFile( dataBin, this.formats, '\n' );
		
		this.dataInterleave = interleave;
	}
	
	public int getTimeDataType()
	{
		return this.timeType;
	}
	
	public boolean isInterleave()
	{
		return this.dataInterleave;
	}
	
	public String getOutputFileFormat()
	{
		return this.outFileFormat;
	}

	public String getOutputFileName()
	{
		return this.outFileName;
	}
	
	public void setOutputFileName( String file )
	{
		this.outFileName = file;
	}
	
	public int getTypeDataBytes()
	{
		return this.dataTypeBytes;
	}

	public int getDataType()
	{
		return this.dataType;
	}

	public int getNumberOfChannels()
	{
		return this.CountChannels;
	}

	public int getChunckSize() 
	{
		return this.ChunckSize;
	}
	
	public String getStreamingName()
	{
		return this.streamName;
	}

	public String getLslXml()
	{
		return this.lslXML;
	}
	
	public int getStringLengthDataType() 
	{
		return this.strLenType;
	}
	
	public List< ByteBlock > getDataBlocks(  ) throws Exception
	{		
		List< ByteBlock > data = this.reader.readDataFromBinaryFile();		
		
		return data;
	}
	
	public String getEncryptKey()
	{
		return this.encryptKey;
	}
		
	public void reset() throws Exception
	{
		this.reader.reset();
	}
	
	public long getDataBinaryFileSize()
	{
		return this.reader.getFileSize();
	}
	
	public void closeTempBinaryFile()
	{
		try
		{			
			this.reader.close();
			
			if( this.binFile != null  && this.deleteBinaries )
			{
				if( !this.binFile.delete() )
				{
					this.binFile.deleteOnExit();
				}
			}
		}
		catch( Exception e )
		{}		
	}	
}