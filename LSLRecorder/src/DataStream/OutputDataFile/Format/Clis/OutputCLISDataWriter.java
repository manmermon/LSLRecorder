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

import Auxiliar.Extra.ConvertTo;
import Auxiliar.Tasks.ITaskMonitor;
import DataStream.OutputDataFile.IOutputDataFileWriter;
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
import DataStream.OutputDataFile.Format.OutputFileFormatParameters;

public class OutputCLISDataWriter implements IOutputDataFileWriter //extends OutputFileWriterTemplate
{	
	private IOutZip zipProcess;
		
	private CLISCompressorWriter clisWriter = null;
	
	public OutputCLISDataWriter( String file, OutputFileFormatParameters pars, ITaskMonitor monitor ) throws Exception
	{		
		//this.dataBlockList = new ConcurrentLinkedDeque< DataBlock >();
		
		Integer zip = pars.getCompressType();
		
		if( zip == null )
		{
			zip = OutputZipDataFactory.UNDEFINED;
		}
		
		this.zipProcess = OutputZipDataFactory.createOuputZipStream( zip );
		
		if( this.zipProcess == null )
		{
			throw new IllegalArgumentException( "Compress technique unknown." );
		}
		
		this.taskMonitor( monitor );
				
		CLISMetadata metadata = new CLISMetadata( pars );		
		this.clisWriter = new CLISCompressorWriter( file, metadata );		
	}

	public void addMetadata( String id, String text ) throws Exception
	{	
		this.clisWriter.addMetadata( id, text );
	}
		
	private void storeData( String text, byte[] data, int nCols ) throws Exception
	{
		byte[] compressData = this.zipProcess.zipData( data );
				
		this.clisWriter.saveCompressedData(  compressData, text, BYTE_TYPE, nCols );
	}
	
	private void storeData( String text, short[] data, int nCols ) throws Exception
	{		
		byte[] compressData = this.zipProcess.zipData( data );
		
		this.clisWriter.saveCompressedData(  compressData, text, SHORT_TYPE, nCols );
	}
	
	private void storeData( String text, int[] data, int nCols ) throws Exception
	{		
		byte[] compressData = this.zipProcess.zipData( data );
		
		this.clisWriter.saveCompressedData( compressData, text, INT_TYPE, nCols );
	}

	private void storeData(String text, long[] data, int nCols) throws Exception
	{		
		byte[] compressData = this.zipProcess.zipData( data );
		
		this.clisWriter.saveCompressedData( compressData, text, LONG_TYPE, nCols );
	}

	private void storeData(String text, double[] data, int nCols) throws Exception
	{		
		byte[] compressData = this.zipProcess.zipData( data );
		
		this.clisWriter.saveCompressedData( compressData, text, DOUBLE_TYPE, nCols );
	}

	private void storeData(String text, float[] data, int nCols) throws Exception
	{		
		byte[] compressData = this.zipProcess.zipData( data );
		
		this.clisWriter.saveCompressedData( compressData, text, FLOAT_TYPE, nCols );
	}

	private void storeData(String text, char[] data) throws Exception
	{		
		byte[] compressData = this.zipProcess.zipData( data, this.clisWriter.getCharCode() );
		
		this.clisWriter.saveCompressedData( compressData, text, STRING_TYPE, 1 );
	}

	private void storeData(String text, String data) throws Exception
	{
		storeData( text, data.toCharArray() );
	}
	
	@Override
	public void close() throws Exception 
	{
		this.clisWriter.saveMetadata();
		this.clisWriter.close();
	}
	
	@Override
	public boolean saveData( DataBlock dataBlock ) throws Exception 
	{	

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

		return true;
	}
		
	@Override
	public boolean finished() 
	{
		return true;
	}


	@Override
	public String getFileName() 
	{
		return this.clisWriter.getFileName();
	}

	@Override
	public void taskMonitor(ITaskMonitor monitor) 
	{	
	}
}
