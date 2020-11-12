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
package lslrec.dataStream.outputDataFile.format.clis;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Cipher;

import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;
import lslrec.dataStream.family.stream.lsl.LSLUtils;

public class CLISCompressorWriter 
{
	private String fileName;
	
	private RandomAccessFile fStream = null;
	
	private String currentVarName = "";
	
	private StreamDataType currentType = StreamDataType.float32;
	
	private long currentNumCols = 0;

	private List< Integer > blockSizes = null;
	
	private CLISMetadata metadata;
	
	public CLISCompressorWriter( String file, CLISMetadata meta ) throws Exception 
	{
		this.metadata = meta;
		
		this.fileName = file;
		
		this.fStream = new RandomAccessFile( new File( file ), "rw" );
		
		long headersize = this.metadata.getHeaderSize();
		for( ; headersize > 0; headersize-- )
		{
			this.fStream.write( this.metadata.getPadding() );
		}
		
		this.blockSizes = new ArrayList< Integer >();
	}
	
	public void addMetadata( String id, String text )
	{
		this.metadata.addMetadataHeader( id, text );
	}
	
	public Charset getCharCode()
	{
		return this.metadata.getCharCode();
	}
	
	public String getFileName() 
	{
		return this.fileName;		
	}
	
	public String getSimpleFileName()
	{
		String simpleFileName = this.fileName;
		
		String sep = FileSystems.getDefault().getSeparator();
				
		int index = this.fileName.lastIndexOf( sep );
		
		if( index < 0 )
		{
			String[] opts = new String[] { "/", "\\" };
 			
			for( String sp : opts )
			{
				index = this.fileName.lastIndexOf( sp );
				
				if( index >= 0 )
				{
					break;
				}	
			}
		}
		
		if( index > 0 )
		{
			simpleFileName.substring( index );
		}
		
		return simpleFileName;
	}
	
	public void saveCompressedData( byte[] compressData, String varName, StreamDataType dataType, long nCols ) throws Exception
	{
		if( !this.currentVarName.equals( varName ) )
		{
			if( !this.currentVarName.isEmpty() )
			{
				this.metadata.addMetadataProtocolInfo( this.currentVarName, this.getDataTypeIdentifier( this.currentType )
													, this.metadata.getDataTypeBytes( this.currentType ) //this.getBytesCurrentDataType( this.currentType )
													, this.currentNumCols
													, this.blockSizes );
			}
			
			this.currentVarName = varName;
			this.currentType = dataType;
			this.currentNumCols = nCols;			
			
			this.blockSizes.clear();
		}
		else if( this.currentType != dataType )
		{			
				throw new IllegalStateException( "More than one type data for the same variable." );
		}
		else if( this.currentNumCols != nCols )
		{
			throw new IllegalStateException( "Number of columns changed for the same variable." );
		}
		
		compressData = this.getEncryptData( compressData );		
		this.metadata.updateCheckSum( compressData );
		this.blockSizes.add( compressData.length );
		
		if( this.fStream != null )
		{
			this.fStream.write( compressData );		
		}
	}
	
	private byte[] getEncryptData( byte[] decryptData ) throws Exception
	{
		byte[] encryptData = decryptData;
		
		if( decryptData != null && decryptData.length > 0 )
		{
			Cipher cipher = this.metadata.getEncryptor();
			if( cipher != null )
			{    
		        encryptData = cipher.doFinal( decryptData );		        
			}
		}
		
		return encryptData;
	}
	
	public void saveMetadata() throws Exception
	{
		if( this.fStream != null )
		{
			this.metadata.addMetadataProtocolInfo(  this.currentVarName
												, this.getDataTypeIdentifier( this.currentType )
												, this.metadata.getDataTypeBytes( this.currentType ) // this.getBytesCurrentDataType( this.currentType )
												, this.currentNumCols, this.blockSizes );
			
			/*
			String head = this.metadata.getMetaDataProtocol();
	
			CharBuffer charBuffer = CharBuffer.wrap( head.toCharArray() );
			ByteBuffer byteBuffer = this.metadata.getCharCode().encode( charBuffer );
			byte[] bytes = Arrays.copyOfRange( byteBuffer.array(), byteBuffer.position(), byteBuffer.limit() );
			*/
			byte[] bytes = this.metadata.getMetaDataProtocol();
	
			this.fStream.seek( 0 );
			this.fStream.write( bytes );
	
			/*
			if ( this.metadata.getDataStreamInfoLength() > 0 )
			{
				charBuffer = CharBuffer.wrap( this.metadata.getDataStreamInfo() );
				byteBuffer = this.metadata.getCharCode().encode(charBuffer);
				bytes = Arrays.copyOfRange( byteBuffer.array(), byteBuffer.position(), byteBuffer.limit() );
				this.fStream.write( bytes );
			}
			*/
		}
	}
	
	public void close() throws Exception
	{
		if( this.fStream != null )
		{
			this.fStream.close();
		}
	}
	
	private String getDataTypeIdentifier( StreamDataType type )
	{
		String id = "float";
		
		switch ( type )
		{
			case int8:
			{			
				id = "int";
				
				break;
			}
			case int16:
			{
				id = "int";
				
				break;
			}
			case int32:
			{
				id = "int";
				
				break;
			}
			case int64:
			{
				id = "int";
				
				break;
			}
			case string:
			{
				id = "char";
				break;
			}
			default:
			{
				break;
			}
		}
		
		return id;
	}

}
