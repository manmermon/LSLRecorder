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
package lslrec.plugin.impl.encoder.binary;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.auxiliar.extra.FileUtils;
import lslrec.auxiliar.task.ITaskMonitor;
import lslrec.dataStream.binary.input.writer.StreamBinaryHeader;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlock;
import lslrec.dataStream.outputDataFile.format.IOutputDataFileWriter;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.tools.StreamUtils;
import lslrec.dataStream.tools.StreamUtils.StreamDataType;

/**
 * @author Manuel Merino Monge
 *
 */
public class OutputBinaryDataWriter implements IOutputDataFileWriter 
{		
	private String fileName;
	private File file;
	private File copyFile =  null;
	private List< String > binHeader;

	private Charset charCode = Charset.forName( "UTF-8" );
	
	private RandomAccessFile FWriterMain;
	private RandomAccessFile FWriterToCopy;
	
	private StreamDataType currentDataType = null;
	private String currentVar = null;
	private long currentNumChannels = 1;
	
	private StreamDataType prevDataType = null;
	private String prevVar = null;
	private long prevNumChannels = 1;
	
	private int byteSizeBlock = 0;
		
	private AtomicBoolean savedDataBlock = new AtomicBoolean( true );
	
	private IStreamSetting strSetting;
	private Map< String, String > extraMetadata;
	
	
	public OutputBinaryDataWriter( String file, ITaskMonitor monitor, OutputFileFormatParameters pars, IStreamSetting settings ) throws Exception 
	{
		this.fileName = FileUtils.checkOutputFileName( file, settings.name(), "" ).t1;
		
		this.strSetting = settings;
				
		this.file = new File( this.fileName );
				
		FileUtils.CreateTemporalBinFile( this.file );

		this.FWriterMain = new RandomAccessFile( this.fileName, "rw" );
		 				
		this.taskMonitor( monitor );
		
		this.binHeader = new ArrayList<String>();
		
		this.extraMetadata = new HashMap<String, String>();
	}
	
	@Override
	public void taskMonitor(ITaskMonitor arg0) 
	{	
	}

	@Override
	public void close() throws Exception 
	{		
		if( this.FWriterMain != null )
		{
			savedDataBlock.set( false );
			
			this.createWriters();
			
			String header = "";
			
			for( String h : this.binHeader )
			{
				header += h;
			}
			
			for( String id : this.extraMetadata.keySet() )
			{
				header += id + this.extraMetadata.get( id );
			}
			
			if( !header.endsWith( StreamBinaryHeader.HEADER_END + "" ) )
			{
				header += StreamBinaryHeader.HEADER_END;
			}
			
			this.FWriterMain.write( header.getBytes( this.charCode ) );
			
			byte[] dat = new byte[ this.byteSizeBlock ];
			boolean copying = true;
			while( copying )
			{
				 int nbytes = this.FWriterToCopy.read( dat );
				 copying = ( nbytes >  0);
				 if( copying )
				 {
					 this.FWriterMain.write( dat, 0, nbytes );
				 }
			}
			
			if( this.FWriterMain != null )
			{
				this.FWriterMain.close();
			}
			
			if( this.FWriterToCopy != null )
			{
				this.FWriterToCopy.close();
				this.copyFile.delete();
			}
			
			if( this.file.getName().endsWith( "~" ) )
			{
				this.file.renameTo( this.copyFile );
			}

			this.FWriterMain = null;
			this.FWriterToCopy = null;
			
			savedDataBlock.set( true );
		}
		
	}

	@Override
	public boolean isFinished() 
	{
		return this.savedDataBlock.get();
	}

	@Override
	public String getFileName() 
	{
		return this.fileName;
	}

	@Override
	public boolean saveData( DataBlock dataBlock ) throws Exception 
	{
		this.savedDataBlock.set( false );
		
		if( dataBlock != null )
		{
			if( this.currentVar == null )
			{
				this.currentVar = dataBlock.getName();
				this.currentDataType = dataBlock.getDataType();
				this.currentNumChannels = dataBlock.getNumCols();
				
				this.binHeader.add( this.currentVar + StreamBinaryHeader.HEADER_BINARY_SEPARATOR
									+ this.currentDataType + StreamBinaryHeader.HEADER_BINARY_SEPARATOR
									+ this.currentNumChannels + StreamBinaryHeader.HEADER_BINARY_SEPARATOR
									+ 1  + StreamBinaryHeader.HEADER_BINARY_SEPARATOR // chunk size
									//+ this.strSetting.getTimestampDataType()  + StreamBinaryHeader.HEADER_BINARY_SEPARATOR
									//+ this.strSetting.getStringLengthDataType()  + StreamBinaryHeader.HEADER_BINARY_SEPARATOR
									+ true + StreamBinaryHeader.HEADER_BINARY_SEPARATOR //interleaved data
									);
				
				if( !this.currentDataType.equals( StreamDataType.undefined ) )
				{
					this.byteSizeBlock += (int)this.currentNumChannels *  StreamUtils.getDataTypeBytes( this.currentDataType );
				}
			}
			
			if( !dataBlock.getName().equals( this.currentVar ) )
			{
				this.prevVar = this.currentVar;
				this.prevDataType = this.currentDataType;
				this.prevNumChannels = this.currentNumChannels;
				
				this.currentVar = dataBlock.getName();
				this.currentNumChannels = dataBlock.getNumCols();
				this.currentDataType = dataBlock.getDataType();
				
				this.binHeader.add( this.currentVar + StreamBinaryHeader.HEADER_BINARY_SEPARATOR
									+ this.currentDataType + StreamBinaryHeader.HEADER_BINARY_SEPARATOR
									+ this.currentNumChannels + StreamBinaryHeader.HEADER_BINARY_SEPARATOR
									+ 1  + StreamBinaryHeader.HEADER_BINARY_SEPARATOR // chunk size
									//+ this.strSetting.getTimestampDataType()  + StreamBinaryHeader.HEADER_BINARY_SEPARATOR
									//+ this.strSetting.getStringLengthDataType()  + StreamBinaryHeader.HEADER_BINARY_SEPARATOR
									+ true + StreamBinaryHeader.HEADER_BINARY_SEPARATOR //interleaved data
									);
							
				if( !this.currentDataType.equals( StreamDataType.undefined ) )
				{
					this.byteSizeBlock += (int)this.currentNumChannels *  StreamUtils.getDataTypeBytes( this.currentDataType );
				}
				
				this.createWriters();
			}

			StreamDataType datType = dataBlock.getDataType();
			
			for( Object val : dataBlock.getData() )
			{
				byte[] prevData = null;
						
				if( this.FWriterToCopy != null )
				{
					prevData = new byte[ (int)this.prevNumChannels * StreamUtils.getDataTypeBytes( this.prevDataType ) ];
					if( this.FWriterToCopy.read( prevData ) < 1 )
					{
						prevData = null;
					}
				}
				
				/*
				if( datType.equals( StreamDataType.undefined ) )
				{
					try (ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
							ObjectOutputStream oos = new ObjectOutputStream(bos))
					{
						oos.writeObject( val );
						this.out.write( bos.toByteArray() );
					}
				}
				//*/
				if( datType.equals( StreamDataType.string ) )
				{
					if( prevData != null )
					{
						this.FWriterMain.write( prevData );
					}
					this.FWriterMain.write( (val.toString().getBytes( this.charCode ) ) );
				}
				else if( !datType.equals( StreamDataType.undefined ) )
				{
					if( prevData != null )
					{
						this.FWriterMain.write( prevData );
					}
					
					byte[] v = ConvertTo.Transform.NumberArray2byteArray( new Number[] { (Number)val } , datType );
					this.FWriterMain.write( v );
				}
			}
		}
		
		this.savedDataBlock.set( true );
		
		return true;
	}

	private void createWriters() throws Exception
	{
		if( this.copyFile != null )
		{
			this.FWriterToCopy.close();
			this.copyFile.delete();
		}
		
		this.copyFile = this.file;
		this.FWriterToCopy = this.FWriterMain;
		this.FWriterToCopy.seek( 0 );
		
		this.file = FileUtils.CreateTemporalBinFile( ( this.file.getName().endsWith("~" ) ? this.fileName : this.fileName + "~" ) );
		this.FWriterMain = new RandomAccessFile( this.file, "rw" );
	}
	
	@Override
	public void addMetadata(String id, String text) throws Exception 
	{
		this.extraMetadata.put( id, text );
	}
}
