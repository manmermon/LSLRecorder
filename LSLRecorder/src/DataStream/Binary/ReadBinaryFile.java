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
package DataStream.Binary;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import Auxiliar.Extra.ConvertTo;
import DataStream.OutputDataFile.DataBlock.ByteBlock;

public class ReadBinaryFile 
{			
	private File binFile = null;
	private List< BinaryDataFormat > dataFormats = null;
	private BufferedInputStream binStream;
	
	private boolean isSkipDataBinHeader = false;
	
	private String EOH = "";
	
	private String header = "";
	
	public ReadBinaryFile( File binaryFile, List< BinaryDataFormat > binaryFormats, Character endHeader ) throws Exception
	{			
		this.binFile = binaryFile;
		
		this.binStream = new BufferedInputStream( new FileInputStream( this.binFile ) );
		
		this.dataFormats = binaryFormats;	
		
		this.EOH = endHeader + "";
	}
	
	public String getHeader()
	{
		if( !this.isSkipDataBinHeader )
		{
			this.isSkipDataBinHeader = true;
			
			if( this.EOH != null && !this.EOH.isEmpty() )
			{
				try
				{
					byte[] aux = new byte[ 1 ];
					while( this.binStream.read( aux ) > 0 )
					{
						String str = new String( aux );
						this.header += str;
						
						if( str.equals( this.EOH ) )
						{
							break;
						}
					}
				}
				catch (Exception e) 
				{
				}		
			}
		}
		
		return this.header;
	}
	
	public List< ByteBlock > readDataFromBinaryFile( ) throws Exception
	{
		this.getHeader();
		
		List< ByteBlock > out = new ArrayList< ByteBlock >();
		
		for( BinaryDataFormat dataFormat : this.dataFormats )
		{			
			List< Byte[] > data = new ArrayList< Byte[] >();
			
			byte[] aux = new byte[ dataFormat.getDataByteSize() ];
			
			int count = 0;
			
			while( count < dataFormat.getChunckSize()
					&& this.binStream.read( aux ) > 0 )
			{
				data.add( ConvertTo.byteArray2ByteArray( aux ) );				
				
				count++;
			}
			
			
			if( count > 0 )
			{
				Byte[] d = new Byte[ data.size() * dataFormat.getDataByteSize() ];
				
				int index = 0;
				
				for( int i = 0; i < count; i++ )
				{
					Byte[] aux2 = data.get( i );
					
					for( int j = 0; j < aux2.length && index < d.length; j++ )
					{
						d[ index ] = aux2[ j ];
						
						index++;
					}
				}
				
				out.add( new ByteBlock( 0, "", dataFormat.getChunckSize(), d ) );
			}
		}
				
		return out;
	}

	public void close() throws Exception
	{
		if( this.binStream != null )
		{
			this.binStream.close();
		}
	}
	
	public void reset() throws Exception
	{
		this.close();
		
		if( this.binStream != null )
		{
			this.binStream = new BufferedInputStream( new FileInputStream( this.binFile ) );
			
			this.isSkipDataBinHeader = false;
		}
	}
	
	public long getFileSize()
	{
		long size = 0;
		
		if( binFile != null )
		{
			size = this.binFile.length();
		}
		
		return size;
	}

}
