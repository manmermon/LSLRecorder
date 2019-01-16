/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
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

package OutputDataFile;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OuputCLISDataWriter 
{
	private final static int BYTE_TYPE = 0;
	private final static int SHORT_TYPE = 1;
	private final static int INT_TYPE = 2;
	private final static int LONG_TYPE = 3;
	private final static int FLOAT_TYPE = 4;
	private final static int DOUBLE_TYPE = 5;
	private final static int STRING_TYPE = 6;
		
	private String fileName;
	private String headerInfo = "";
	private String header = "";

	private String endLine = "\n";

	private String GrpSep = ";";
	private String fieldSep = ",";

	//private FileOutputStream fStream;
	private RandomAccessFile fStream; 
	private String compressAlg = "GZIP";

	private boolean addHeader = false;
	//private List< byte[] > compressDataList;
	
	private String currentVarName = "";
	private List< Integer > blockSizes = null;
	private int currentType = FLOAT_TYPE;	
	private int currentNumCols = 0;
	
	private Charset charCode;
	
	private IOutZip zipProcess;
	
	public OuputCLISDataWriter( String file, long headersize, IOutZip zip, Charset coding ) throws Exception
	{	
		if( zip == null )
		{
			throw new IllegalArgumentException( "IOutZip null" );
		}
		
		this.zipProcess = zip;
		
		this.compressAlg = this.zipProcess.getZipID();
		
		this.fStream = new RandomAccessFile( new File( file ), "rw" );
				
		this.charCode = coding;
		if( this.charCode == null )
		{
			this.charCode = Charset.forName( "UTF-8" );
		}
		
		this.blockSizes = new ArrayList< Integer >();
		
		this.headerInfo = "ver=2.0" + this.GrpSep + "compress=" + this.compressAlg + this.GrpSep + "headerByteSize=";
		
		headersize += this.headerInfo.length() * 2;
				
		//byte[] padding = new byte[ Character.BYTES ];
		int charByteSize = this.headerInfo.getBytes( coding ).length / this.headerInfo.length(); 
		byte[] padding = new byte[ charByteSize ];
		
		for( int i = 0; i < padding.length; i++ )
		{
			padding[ i ] = '\r';
		}
		
		this.fileName = file;
		this.headerInfo += ( headersize * padding.length ) + this.GrpSep;
		
		for( ; headersize > 0; headersize-- )
		{
			this.fStream.write( padding );
		}
	}

	private void addHeaderInfo(String name, String type, int typeBytes, int col, List< Integer > blockSizes )
	{
		this.headerInfo = this.headerInfo + name + this.fieldSep + type + this.fieldSep + typeBytes 
							+ this.fieldSep + col;
		
		if( blockSizes.size() == 0 )
		{
			this.headerInfo += this.fieldSep + 0;
		}	
		else
		{
			for( Integer size : blockSizes )
			{
				this.headerInfo += this.fieldSep + size; 
			}
		}
							
		this.headerInfo += this.GrpSep;
	}

	public void addHeader( String id, String text ) throws Exception
	{
		this.addHeader = true;

		id = id.replace("\n", "").replace("\r", "");
		text = text.replace("\n", "").replace("\r", "");

		this.header = (id + "=" + text);
	}

	public void saveData( String text, byte[] data, int nCols ) throws Exception
	{
		/*
		int nBytes = data.length;

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream( nBytes );
		GZIPOutputStream zipStream = new GZIPOutputStream( byteStream );

		zipStream.write( data );
		zipStream.close();
		byteStream.close();

		byte[] compressData = byteStream.toByteArray();
		*/
		
		byte[] compressData = this.zipProcess.zipData( data );
		
		saveCompressedData( compressData, text, BYTE_TYPE, nCols );
	}
	
	public void saveData( String text, short[] data, int nCols ) throws Exception
	{	
		/*
		int typeBytes = Short.BYTES;
		int nBytes = data.length * typeBytes;
		byte[] dataToSave = new byte[ nBytes ];

		ByteBuffer bb = ByteBuffer.wrap( dataToSave );
		ShortBuffer buf = bb.asShortBuffer();
		buf.put( data );

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream( nBytes );
		GZIPOutputStream zipStream = new GZIPOutputStream( byteStream );

		zipStream.write( dataToSave );
		zipStream.close();
		byteStream.close();

		byte[] compressData = byteStream.toByteArray();
		*/
		
		byte[] compressData = this.zipProcess.zipData( data );
		
		saveCompressedData( compressData, text, SHORT_TYPE, nCols );
	}
	
	public void saveData( String text, int[] data, int nCols ) throws Exception
	{	
		/*
		int typeBytes = Integer.BYTES;
		int nBytes = data.length * typeBytes;
		byte[] dataToSave = new byte[nBytes];

		ByteBuffer bb = ByteBuffer.wrap(dataToSave);
		IntBuffer buf = bb.asIntBuffer();
		buf.put(data);

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(nBytes);
		GZIPOutputStream zipStream = new GZIPOutputStream(byteStream);

		zipStream.write(dataToSave);
		zipStream.close();

		byteStream.close();

		byte[] compressData = byteStream.toByteArray();
		*/
		
		byte[] compressData = this.zipProcess.zipData( data );
		
		saveCompressedData(compressData, text, INT_TYPE, nCols );
	}

	public void saveData(String text, long[] data, int nCols) throws Exception
	{
		/*
		int typeBytes = Long.BYTES;
		int nBytes = data.length * typeBytes;
		byte[] dataToSave = new byte[nBytes];

		ByteBuffer bb = ByteBuffer.wrap(dataToSave);
		LongBuffer buf = bb.asLongBuffer();
		buf.put(data);

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(nBytes);
		GZIPOutputStream zipStream = new GZIPOutputStream(byteStream);

		zipStream.write(dataToSave);
		zipStream.close();

		byteStream.close();

		byte[] compressData = byteStream.toByteArray();
		*/
		
		byte[] compressData = this.zipProcess.zipData( data );
		
		saveCompressedData(compressData, text, LONG_TYPE, nCols );
	}

	public void saveData(String text, double[] data, int nCols) throws Exception
	{
		/*
		int typeBytes = Double.BYTES;
		int nBytes = data.length * typeBytes;
		byte[] dataToSave = new byte[nBytes];

		ByteBuffer bb = ByteBuffer.wrap(dataToSave);
		DoubleBuffer buf = bb.asDoubleBuffer();
		buf.put(data);

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(nBytes);
		GZIPOutputStream zipStream = new GZIPOutputStream(byteStream);

		zipStream.write( dataToSave );
		zipStream.close();

		byteStream.close();

		byte[] compressData = byteStream.toByteArray();
		*/
		
		byte[] compressData = this.zipProcess.zipData( data );
		
		saveCompressedData(compressData, text, DOUBLE_TYPE, nCols );
	}

	public void saveData(String text, float[] data, int nCols) throws Exception
	{
		/*
		int typeBytes = Float.BYTES;
		int nBytes = data.length * typeBytes;
		byte[] dataToSave = new byte[nBytes];

		ByteBuffer bb = ByteBuffer.wrap(dataToSave);
		FloatBuffer buf = bb.asFloatBuffer();
		buf.put(data);

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(nBytes);
		GZIPOutputStream zipStream = new GZIPOutputStream(byteStream);

		zipStream.write(dataToSave);
		zipStream.close();

		byteStream.close();

		byte[] compressData = byteStream.toByteArray();
		*/
		
		byte[] compressData = this.zipProcess.zipData( data );
		
		saveCompressedData(compressData, text, FLOAT_TYPE, nCols );
	}

	public void saveData(String text, char[] data) throws Exception
	{
		/*
		int typeBytes = Character.BYTES;
		int nBytes = data.length * typeBytes;
		byte[] dataToSave = new byte[nBytes];

		ByteBuffer bb = ByteBuffer.wrap(dataToSave);
		CharBuffer buf = bb.asCharBuffer();
		buf.put(data);

		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(nBytes);
		GZIPOutputStream zipStream = new GZIPOutputStream(byteStream);

		zipStream.write(dataToSave);
		zipStream.close();

		byteStream.close();

		byte[] compressData = byteStream.toByteArray();
		*/
		
		byte[] compressData = this.zipProcess.zipData( data, this.charCode );
		
		saveCompressedData(compressData, text, STRING_TYPE, 1 );
	}

	public void saveData(String text, String data) throws Exception
	{
		saveData( text, data.toCharArray() );
	}

	public String getFileName()
	{
		return this.fileName;
	}

	public void closeFile() throws Exception
	{
		try
		{
			this.addHeaderInfo( this.currentVarName
								, this.getDataTypeIdentifier( this.currentType )
								, this.getBytesCurrentDataType( this.currentType )
								, this.currentNumCols, this.blockSizes );
			
			String head = this.headerInfo + "header," + this.addHeader + this.GrpSep + this.endLine;
	
			CharBuffer charBuffer = CharBuffer.wrap( head.toCharArray() );
			ByteBuffer byteBuffer = this.charCode.encode( charBuffer );
			byte[] bytes = Arrays.copyOfRange( byteBuffer.array(), byteBuffer.position(), byteBuffer.limit() );
	
			this.fStream.seek( 0 );
			this.fStream.write( bytes );
	
			if ( this.addHeader )
			{
				charBuffer = CharBuffer.wrap( this.header.trim() + this.endLine);
				byteBuffer = this.charCode.encode(charBuffer);
				bytes = Arrays.copyOfRange( byteBuffer.array(), byteBuffer.position(), byteBuffer.limit() );
				this.fStream.write( bytes );
			}
		}
		catch( Exception ex )
		{
			ex.printStackTrace();
		}
		finally 
		{
			this.fStream.close();
			this.fStream = null;
		}		
	}

	private void saveCompressedData( byte[] compressData, String varName, int dataType, int nCols ) throws Exception
	{
		if( !this.currentVarName.equals( varName ) )
		{
			if( !this.currentVarName.isEmpty() )
			{
				this.addHeaderInfo( this.currentVarName, this.getDataTypeIdentifier( this.currentType )
									, this.getBytesCurrentDataType( this.currentType ), this.currentNumCols
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
		
		this.blockSizes.add( compressData.length );
		
		this.fStream.write( compressData );		
	}
	
	private String getDataTypeIdentifier( int type )
	{
		String id = "float";
		
		switch ( type )
		{
			case( BYTE_TYPE ):
			{			
				id = "int";
				
				break;
			}
			case( SHORT_TYPE ):
			{
				id = "int";
				
				break;
			}
			case( INT_TYPE ):
			{
				id = "int";
				
				break;
			}
			case( LONG_TYPE ):
			{
				id = "int";
				
				break;
			}
			case( STRING_TYPE ):
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
	
	private int getBytesCurrentDataType( int type )
	{
		int b = Float.BYTES;
		
		switch ( type )
		{
			case( BYTE_TYPE ):
			{			
				b = Byte.BYTES;
				
				break;
			}
			case( SHORT_TYPE ):
			{			
				b = Short.BYTES;
				
				break;
			}
			case( INT_TYPE ):
			{			
				b = Integer.BYTES;
				
				break;
			}
			case( LONG_TYPE ):
			{			
				b = Long.BYTES;
				
				break;
			}
			case( DOUBLE_TYPE ):
			{			
				b = Double.BYTES;
				
				break;
			}
			case( STRING_TYPE ):
			{
				b = Character.BYTES;
				
				break;
			}
			default:
			{
				break;
			}
		}
		
		return b;
	}
}
