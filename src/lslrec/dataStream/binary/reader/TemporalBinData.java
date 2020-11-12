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

package lslrec.dataStream.binary.reader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lslrec.dataStream.binary.BinaryDataFormat;
import lslrec.dataStream.binary.setting.BinaryFileStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;
import lslrec.dataStream.outputDataFile.dataBlock.ByteBlock;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;

public class TemporalBinData
{	
	private ReaderBinaryFile reader = null;
	
	private File binFile = null;
	
	private boolean deleteBinaries = true;
	
	private int dataTypeBytes;
	private List< BinaryDataFormat > formats;
	
	private IStreamSetting streamSetting;
	private OutputFileFormatParameters outputFormat;
	
	public TemporalBinData( BinaryFileStreamSetting strSetting
							, OutputFileFormatParameters outFormat ) throws Exception
	{	
		if( strSetting == null || outFormat == null )
		{
			throw new IllegalArgumentException( "DataStreamSetting and/or OutputFileFormatParameters null");
		}
		
		this.streamSetting = strSetting.getStreamSetting();
		this.outputFormat = outFormat;
		
		this.deleteBinaries = (Boolean)this.outputFormat.getParameter( OutputFileFormatParameters.DELETE_BIN ).getValue();
		
		this.binFile = strSetting.getStreamBinFile();
		
		this.dataTypeBytes = this.streamSetting.getDataTypeBytes( this.streamSetting.data_type() );
		
		if( this.dataTypeBytes < 1 )
		{
			throw new Exception( "Data type unknown." );
		}
		
		this.formats = new ArrayList<BinaryDataFormat>();
		
		int len = this.streamSetting.channel_count() * this.streamSetting.getChunkSize();
		
		if( this.streamSetting.data_type() != StreamDataType.string )
		{
			this.formats.add( new BinaryDataFormat( this.streamSetting.data_type(), this.dataTypeBytes, len ) );
		}
		else
		{
			BinaryDataFormat strLenFormat = new BinaryDataFormat( this.streamSetting.getStringLegthDataType(), this.streamSetting.getDataTypeBytes( this.streamSetting.getStringLegthDataType() ), len );
			this.formats.add( new BinaryDataFormat( this.streamSetting.data_type(), this.dataTypeBytes, strLenFormat ) );
		}
		
		
		this.formats.add( new BinaryDataFormat( this.streamSetting.getTimestampDataType(), this.streamSetting.getDataTypeBytes( this.streamSetting.getTimestampDataType() ), this.streamSetting.getChunkSize() ) );
		
		this.reader = new ReaderBinaryFile( this.binFile, this.formats, '\n' );
	}
	
	public IStreamSetting getDataStreamSetting()
	{
		return this.streamSetting;
	}
	
	public OutputFileFormatParameters getOutputFileFormat()
	{
		return this.outputFormat;
	}
		
	public int getTypeDataBytes()
	{
		return this.dataTypeBytes;
	}
	
	public List< ByteBlock > getDataBlocks(  ) throws Exception
	{		
		List< ByteBlock > data = this.reader.readDataFromBinaryFile();		
		
		return data;
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