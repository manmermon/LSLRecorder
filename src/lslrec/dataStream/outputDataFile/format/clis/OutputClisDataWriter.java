/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
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

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.auxiliar.task.ITaskMonitor;
import lslrec.dataStream.outputDataFile.compress.IOutZip;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.outputDataFile.compress.CompressorDataFactory;
import lslrec.dataStream.outputDataFile.dataBlock.ByteBlock;
import lslrec.dataStream.outputDataFile.dataBlock.CharBlock;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlock;
import lslrec.dataStream.outputDataFile.dataBlock.DoubleBlock;
import lslrec.dataStream.outputDataFile.dataBlock.FloatBlock;
import lslrec.dataStream.outputDataFile.dataBlock.IntegerBlock;
import lslrec.dataStream.outputDataFile.dataBlock.LongBlock;
import lslrec.dataStream.outputDataFile.dataBlock.ShortBlock;
import lslrec.dataStream.outputDataFile.dataBlock.StringBlock;
import lslrec.dataStream.outputDataFile.format.IOutputDataFileWriter;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.tools.StreamUtils.StreamDataType;

public class OutputClisDataWriter implements IOutputDataFileWriter //extends OutputFileWriterTemplate
{	
	private IOutZip zipProcess;
		
	private ClisCompressorWriter clisWriter = null;
	
	public OutputClisDataWriter( OutputFileFormatParameters formatPars, IStreamSetting streamSettings, ITaskMonitor monitor ) throws Exception
	{		
		//this.dataBlockList = new ConcurrentLinkedDeque< DataBlock >();
		
		String zip = (String)formatPars.getParameter( OutputFileFormatParameters.ZIP_ID ).getValue();
		
		if( zip == null )
		{
			zip = CompressorDataFactory.UNDEFINED;
		}
		
		this.zipProcess = CompressorDataFactory.createOuputZipStream( zip );
		
		if( this.zipProcess == null )
		{
			throw new IllegalArgumentException( "Compress technique unknown." );
		}
		
		this.taskMonitor( monitor );
				
		ClisMetadata metadata = new ClisMetadata( formatPars, streamSettings );		
		this.clisWriter = new ClisCompressorWriter( (String)formatPars.getParameter( OutputFileFormatParameters.OUT_FILE_NAME ).getValue(), metadata );		
	}

	public void addMetadata( String id, String text ) throws Exception
	{	
		this.clisWriter.addMetadata( id, text );
	}
		
	private void storeData( String text, byte[] data, long nCols ) throws Exception
	{
		byte[] compressData = this.zipProcess.zipData( data );
				
		this.clisWriter.saveCompressedData(  compressData, text, StreamDataType.int8, nCols );
	}
	
	private void storeData( String text, short[] data, long nCols ) throws Exception
	{		
		byte[] compressData = this.zipProcess.zipData( data );
		
		this.clisWriter.saveCompressedData(  compressData, text, StreamDataType.int16, nCols );
	}
	
	private void storeData( String text, int[] data, long nCols ) throws Exception
	{		
		byte[] compressData = this.zipProcess.zipData( data );
		
		this.clisWriter.saveCompressedData( compressData, text, StreamDataType.int32, nCols );
	}

	private void storeData(String text, long[] data, long nCols) throws Exception
	{		
		byte[] compressData = this.zipProcess.zipData( data );
		
		this.clisWriter.saveCompressedData( compressData, text, StreamDataType.int64, nCols );
	}

	private void storeData(String text, double[] data, long nCols) throws Exception
	{		
		byte[] compressData = this.zipProcess.zipData( data );
		
		this.clisWriter.saveCompressedData( compressData, text, StreamDataType.double64, nCols );
	}

	private void storeData(String text, float[] data, long nCols) throws Exception
	{		
		byte[] compressData = this.zipProcess.zipData( data );
		
		this.clisWriter.saveCompressedData( compressData, text, StreamDataType.float32, nCols );
	}

	private void storeData(String text, char[] data) throws Exception
	{		
		byte[] compressData = this.zipProcess.zipData( data, this.clisWriter.getCharCode() );
		
		this.clisWriter.saveCompressedData( compressData, text, StreamDataType.string, 1 );
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

			byte[] copy = ConvertTo.Casting.ByterArray2byteArray( dat );

			this.storeData( dataBlock.getName(), copy, dataBlock.getNumCols() );
		}
		else if(dataBlock instanceof ShortBlock)
		{
			ShortBlock d = (ShortBlock)dataBlock;
			Short[] dat = d.getData();

			short[] copy = ConvertTo.Casting.ShortArray2shortArray( dat );

			this.storeData( dataBlock.getName(), copy, dataBlock.getNumCols() );
		}
		else if(dataBlock instanceof IntegerBlock )
		{
			IntegerBlock d = (IntegerBlock)dataBlock;
			Integer[] dat = d.getData();

			int[] copy = ConvertTo.Casting.IntegerArray2intArray( dat );

			this.storeData( dataBlock.getName(), copy, dataBlock.getNumCols() );
		}
		else if(dataBlock instanceof LongBlock)
		{
			LongBlock d = (LongBlock)dataBlock;
			Long[] dat = d.getData();

			long[] copy = ConvertTo.Casting.LongArray2longArray( dat );

			this.storeData( dataBlock.getName(), copy, dataBlock.getNumCols() );
		}
		else if(dataBlock instanceof FloatBlock)
		{
			FloatBlock d = (FloatBlock)dataBlock;
			Float[] dat = d.getData();

			float[] copy = ConvertTo.Casting.FloatArray2floatArray( dat );

			this.storeData( dataBlock.getName(), copy, dataBlock.getNumCols() );
		}
		else if(dataBlock instanceof DoubleBlock)
		{
			DoubleBlock d = (DoubleBlock)dataBlock;
			Double[] dat = d.getData();

			double[] copy = ConvertTo.Casting.DoubleArray2doubleArray( dat );

			this.storeData( dataBlock.getName(), copy, dataBlock.getNumCols() );
		}
		else if(dataBlock instanceof CharBlock)
		{
			CharBlock d = (CharBlock)dataBlock;
			Character[] dat = d.getData();

			char[] copy = ConvertTo.Casting.CharacterArray2charArray( dat );

			this.storeData( dataBlock.getName(), copy );
		}
		else if( dataBlock instanceof StringBlock )
		{
			StringBlock d = (StringBlock)dataBlock;
			Character[] dat = d.getData();

			char[] copy = ConvertTo.Casting.CharacterArray2charArray( dat );

			this.storeData( dataBlock.getName(), copy );
		}
		else
		{
			throw new Exception( "Data block type unknown." );
		}

		return true;
	}
		
	@Override
	public boolean isFinished() 
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
