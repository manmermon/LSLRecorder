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
package lslrec.dataStream.binary.reader;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.dataStream.binary.BinaryDataFormat;
import lslrec.dataStream.outputDataFile.dataBlock.ByteBlock;

public class ReaderBinaryFile 
{			
	private File binFile = null;
	protected List< BinaryDataFormat > dataFormats = null;
	protected BufferedInputStream binStream;
	
	private boolean isSkipDataBinHeader = false;
	
	private String EOH = "";
	
	private String header = "";
	
	public ReaderBinaryFile( File binaryFile, List< BinaryDataFormat > binaryFormats, Character endHeader ) throws Exception
	{		
		this.binFile = binaryFile;
		
		this.binStream = new BufferedInputStream( new FileInputStream( this.binFile ) );
		
		if( binaryFormats == null || binaryFormats.isEmpty() )
		{
			throw new IllegalArgumentException( "List of binary formats is null or empty." );
		}
		
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

		/*
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
		 */

		List< ByteBlock > out = this.readDataFromBinaryFile_1( this.binStream, this.dataFormats );

		return out;
	}

	private List< ByteBlock > readDataFromBinaryFile_1( BufferedInputStream binStream
														, List< BinaryDataFormat > dataFormats  ) throws Exception
	{
		List< ByteBlock > out = new ArrayList< ByteBlock >();

		if( binStream != null && dataFormats != null )
		{
			for( BinaryDataFormat dataFormat : dataFormats )
			{
				for( ByteBlock bb : this.readDataFromBinaryFile_2( binStream, dataFormat ) )
				{
					out.add( bb );
				}
			}
		}

		return out;
	}

	private List< ByteBlock > readDataFromBinaryFile_2( BufferedInputStream binStream
														, BinaryDataFormat dataFormat  ) throws Exception
	{
		List< ByteBlock > out = new ArrayList< ByteBlock >();

		if( dataFormat.getChunckSize() != BinaryDataFormat.UNKNOW_CHUCKSIZE )
		{
			ByteBlock bb = this.readDataFromBinaryFile_3( binStream, dataFormat );
			if( bb != null )
			{
				out.add( bb );
			}
		}
		else
		{
			ByteBlock bb = this.readDataFromBinaryFile_3( binStream, dataFormat.getLengthFormat() );

			if( bb != null )
			{
				Number[] lens = ConvertTo.ByteArrayTo( bb.getData(), dataFormat.getLengthFormat().getDataType() );

				long L = 0;
				for( Number l : lens )
				{
					L += l.longValue();
				}

				BinaryDataFormat bdf = new BinaryDataFormat( dataFormat.getDataType(), dataFormat.getDataByteSize(), L );
				ByteBlock dbb = this.readDataFromBinaryFile_3( binStream, bdf );

				if( dbb != null )
				{
					out.add( bb );
					out.add( dbb );
				}
			}
		}

		return out;
	}

	private ByteBlock readDataFromBinaryFile_3( BufferedInputStream binStream
												, BinaryDataFormat dataFormat  ) throws Exception
	{
		ByteBlock out = null;

		List< Byte[] > data = new ArrayList< Byte[] >();

		byte[] aux = new byte[ dataFormat.getDataByteSize() ];

		int count = 0;

		while( count < dataFormat.getChunckSize()
				&& binStream.read( aux ) > 0 )
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


			out = new ByteBlock( 0, "", dataFormat.getChunckSize(), d );
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
