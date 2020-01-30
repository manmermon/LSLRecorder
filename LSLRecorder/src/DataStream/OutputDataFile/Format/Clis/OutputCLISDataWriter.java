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

package DataStream.OutputDataFile.Format.Clis;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import Auxiliar.Extra.ConvertTo;
import DataStream.OutputDataFile.Compress.IOutZip;
import DataStream.OutputDataFile.Compress.OutputZipDataFactory;
import DataStream.OutputDataFile.DataBlock.ByteBlock;
import DataStream.OutputDataFile.DataBlock.CharBlock;
import DataStream.OutputDataFile.DataBlock.DataBlock;
import DataStream.OutputDataFile.DataBlock.DoubleBlock;
import DataStream.OutputDataFile.DataBlock.FloatBlock;
import DataStream.OutputDataFile.DataBlock.IntegerBlock;
import DataStream.OutputDataFile.DataBlock.LongBlock;
import DataStream.OutputDataFile.DataBlock.ShortBlock;
import DataStream.OutputDataFile.DataBlock.StringBlock;
import DataStream.OutputDataFile.Format.OutputFileWriterTemplate;
import edu.ucsd.sccn.LSLUtils;

public class OutputCLISDataWriter extends OutputFileWriterTemplate
{	
	private String headerInfo = "";
	private String header = "";

	private String endLine = "\n";

	private String GrpSep = ";" ;
	private String fieldSep = "," ;
 
	private String compressAlg = "GZIP";

	private boolean addHeader = false;
	
	private String currentVarName = "";
	private List< Integer > blockSizes = null;
	private int currentType = FLOAT_TYPE;	
	private int currentNumCols = 0;
	
	protected Charset charCode;
	
	private IOutZip zipProcess;
	
	private ConcurrentLinkedDeque< DataBlock > dataBlockList = null;
	
	protected AtomicBoolean dataBlockProcessed = new AtomicBoolean( true );
	
	public OutputCLISDataWriter( String file, long headersize, int zip, Charset coding ) throws Exception
	{	
		super( file, true );
		
		super.setName( this.getClass().getSimpleName() + "-" + this.fileName );
		
		this.dataBlockList = new ConcurrentLinkedDeque< DataBlock >();
		
		this.zipProcess = OutputZipDataFactory.createOuputZipStream( zip );
		
		if( this.zipProcess == null )
		{
			throw new IllegalArgumentException( "Compress technique unknown." );
		}
		
		this.compressAlg = this.zipProcess.getZipID();
				
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
		
		this.headerInfo += ( headersize * padding.length ) + this.GrpSep;
		
		for( ; headersize > 0; headersize-- )
		{
			super.fStream.write( padding );
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
		
	private void storeData( String text, byte[] data, int nCols ) throws Exception
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
	
	private void storeData( String text, short[] data, int nCols ) throws Exception
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
	
	private void storeData( String text, int[] data, int nCols ) throws Exception
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

	private void storeData(String text, long[] data, int nCols) throws Exception
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

	private void storeData(String text, double[] data, int nCols) throws Exception
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

	private void storeData(String text, float[] data, int nCols) throws Exception
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

	private void storeData(String text, char[] data) throws Exception
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

	private void storeData(String text, String data) throws Exception
	{
		storeData( text, data.toCharArray() );
	}

	protected void saveCompressedData( byte[] compressData, String varName, int dataType, int nCols ) throws Exception
	{
		if( !this.currentVarName.equals( varName ) )
		{
			if( !this.currentVarName.isEmpty() )
			{
				this.addHeaderInfo( this.currentVarName, this.getDataTypeIdentifier( this.currentType )
									, LSLUtils.getDataTypeBytes( this.currentType ) //this.getBytesCurrentDataType( this.currentType )
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
	
	/*
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
	*/

	@Override
	protected boolean DataBlockManager(DataBlock dataBlock) throws Exception 
	{
		boolean added = false;
			
		if( dataBlock != null )
		{
			added = this.dataBlockList.isEmpty();
		
			if( added )
			{
				this.dataBlockList.add( dataBlock );
			}
		}
		
		return added;
	}
	
	@Override
	protected boolean DataBlockAvailable()
	{
		return !this.dataBlockList.isEmpty();
	}	

	@Override
	protected void CloseWriterActions() throws Exception 
	{
		this.addHeaderInfo( this.currentVarName
				, this.getDataTypeIdentifier( this.currentType )
				, LSLUtils.getDataTypeBytes( this.currentType ) // this.getBytesCurrentDataType( this.currentType )
				, this.currentNumCols, this.blockSizes );

		String head = this.headerInfo + "header," + this.addHeader + this.GrpSep + this.endLine;

		CharBuffer charBuffer = CharBuffer.wrap( head.toCharArray() );
		ByteBuffer byteBuffer = this.charCode.encode( charBuffer );
		byte[] bytes = Arrays.copyOfRange( byteBuffer.array(), byteBuffer.position(), byteBuffer.limit() );

		super.fStream.seek( 0 );
		super.fStream.write( bytes );

		if ( this.addHeader )
		{
			charBuffer = CharBuffer.wrap( this.header.trim() + this.endLine);
			byteBuffer = this.charCode.encode(charBuffer);
			bytes = Arrays.copyOfRange( byteBuffer.array(), byteBuffer.position(), byteBuffer.limit() );
			super.fStream.write( bytes );
		}
	}

	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{	
	}

	/*
	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{		
	}
	*/
	
	@Override
	protected void ProcessDataBlock() throws Exception 
	{
		if( !this.dataBlockList.isEmpty() )
		{			
			/* KEEP */
			this.dataBlockProcessed.set( false );
			
			DataBlock dataBlock = this.dataBlockList.poll();
			
			/* */
			
			if( dataBlock instanceof ByteBlock)
			{
				ByteBlock d = (ByteBlock)dataBlock;
				Byte[] dat = d.getData();

				byte[] copy = ConvertTo.ByterArray2byteArray( dat );

				this.storeData( dataBlock.getName(), copy, dataBlock.getNumCols() );
			}
			else if(dataBlock instanceof ShortBlock)
			{
				ShortBlock d = (ShortBlock)dataBlock;
				Short[] dat = d.getData();

				short[] copy = ConvertTo.ShortArray2shortArray( dat );

				this.storeData( dataBlock.getName(), copy, dataBlock.getNumCols() );
			}
			else if(dataBlock instanceof IntegerBlock )
			{
				IntegerBlock d = (IntegerBlock)dataBlock;
				Integer[] dat = d.getData();

				int[] copy = ConvertTo.IntegerArray2intArray( dat );

				this.storeData( dataBlock.getName(), copy, dataBlock.getNumCols() );
			}
			else if(dataBlock instanceof LongBlock)
			{
				LongBlock d = (LongBlock)dataBlock;
				Long[] dat = d.getData();

				long[] copy = ConvertTo.LongArray2longArray( dat );

				this.storeData( dataBlock.getName(), copy, dataBlock.getNumCols() );
			}
			else if(dataBlock instanceof FloatBlock)
			{
				FloatBlock d = (FloatBlock)dataBlock;
				Float[] dat = d.getData();

				float[] copy = ConvertTo.FloatArray2floatArray( dat );

				this.storeData( dataBlock.getName(), copy, dataBlock.getNumCols() );
			}
			else if(dataBlock instanceof DoubleBlock)
			{
				DoubleBlock d = (DoubleBlock)dataBlock;
				Double[] dat = d.getData();

				double[] copy = ConvertTo.DoubleArray2doubleArray( dat );

				this.storeData( dataBlock.getName(), copy, dataBlock.getNumCols() );
			}
			else if(dataBlock instanceof CharBlock)
			{
				CharBlock d = (CharBlock)dataBlock;
				Character[] dat = d.getData();

				char[] copy = ConvertTo.CharacterArray2charArray( dat );

				this.storeData( dataBlock.getName(), copy );
			}
			else if( dataBlock instanceof StringBlock )
			{
				StringBlock d = (StringBlock)dataBlock;
				Character[] dat = d.getData();

				char[] copy = ConvertTo.CharacterArray2charArray( dat );

				this.storeData( dataBlock.getName(), copy );
			}
			else
			{
				throw new Exception( "Data block type unknown." );
			}
			
			this.dataBlockProcessed.set( true );
		}
	}
	
	@Override
	protected boolean wasDataBlockProcessed() 
	{
		return this.dataBlockProcessed.get() ;
	}
	
	@Override
	public boolean finished() 
	{
		return this.dataBlockList.isEmpty() && this.dataBlockProcessed.get();
	}
	
	@Override
	protected int getMaxNumThreads() 
	{
		return 1;
	}

	/*
	@Override
	public void taskDone(INotificationTask task) throws Exception 
	{	
	}
	*/
}
