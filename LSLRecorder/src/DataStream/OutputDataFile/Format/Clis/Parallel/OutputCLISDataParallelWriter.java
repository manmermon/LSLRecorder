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
package DataStream.OutputDataFile.Format.Clis.Parallel;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicBoolean;

import Auxiliar.Extra.ConvertTo;
import Auxiliar.Tasks.ITaskMonitor;
import DataStream.OutputDataFile.Compress.IOutZip;
import DataStream.OutputDataFile.Compress.OutputZipDataFactory;
import DataStream.OutputDataFile.DataBlock.DataBlock;
import DataStream.OutputDataFile.DataBlock.DataInByteFormatBlock;
import DataStream.OutputDataFile.Format.Clis.CLISCompressorWriter;
import DataStream.OutputDataFile.Format.Clis.CLISMetadata;
import DataStream.OutputDataFile.Format.Parallelize.OutputParallelizableFileWriterTemplate;
import StoppableThread.IStoppableThread;

public class OutputCLISDataParallelWriter extends OutputParallelizableFileWriterTemplate implements ICompressDataCollector, IStoppableThread
{
	private int zipType = OutputZipDataFactory.GZIP;
	
	private ConcurrentSkipListMap< Integer, DataInByteFormatBlock > compressDataList = null;
	
	private int nextNumSeqCompressedDataBlock = 0;
	
	private List< ZipThread > zpThreadList = null;
	
	private CLISMetadata metadata = null;
	
	private CLISCompressorWriter clisWriter = null;
	
	private AtomicBoolean dataBlockProcessed = new AtomicBoolean( false );
	
	public OutputCLISDataParallelWriter( String file, long headersize, int zip, Charset coding, ITaskMonitor monitor ) throws Exception 
	{
		super();
		
		IOutZip zp = OutputZipDataFactory.createOuputZipStream( zip );
		
		if( zp == null )
		{
			throw new IllegalArgumentException( "Compress technique unknown." );
		}
		
		super.taskMonitor( monitor );
		
		this.metadata = new CLISMetadata( headersize, zp.getZipID(), coding );
		this.clisWriter = new CLISCompressorWriter( file, this.metadata );
		
		this.zipType = zip;
		
		this.compressDataList = new ConcurrentSkipListMap< Integer, DataInByteFormatBlock >();
		this.zpThreadList = new ArrayList< ZipThread >();
		
		super.setName( this.getClass().getSimpleName() + "-" + this.clisWriter.getSimpleFileName() );
		
		super.startThread();
	}
	
	@Override
	public String getFileName() 
	{
		return this.clisWriter.getFileName();
	}
	
	@Override
	protected int getMaxNumThreads() 
	{
		return Runtime.getRuntime().availableProcessors();
	}
	
	@Override
	protected boolean DataBlockManager( DataBlock dataBlock ) throws Exception 
	{
		boolean add = this.zpThreadList.size() < this.getMaxNumThreads() ;
		
		if( add )
		{
			this.Zip( dataBlock.getName(), dataBlock.getDataType(), dataBlock.getNumCols(), dataBlock.getSeqNum(), dataBlock.getData() );
		}
		
		return add; 
	}
	
	/*
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
	*/
		
	private void Zip( String varName, int dataType, int nChannels, int ordered, Object[] data ) throws Exception
	{
		IOutZip zp = OutputZipDataFactory.createOuputZipStream( this.zipType );
		
		ZipThread zipThr = new ZipThread( varName, dataType, nChannels, zp, this, this.metadata.getCharCode() );
				
		zipThr.CompressData( ordered, data );
		
		this.zpThreadList.add( zipThr );
		
		zipThr.startThread();
	}
		
	@Override
	protected boolean DataBlockAvailable() 
	{		
		boolean available = false;
				
		if( !this.compressDataList.isEmpty() )
		{
			available = this.compressDataList.firstKey() == this.nextNumSeqCompressedDataBlock;
		}
				
		return available;  
	}
	
	/*
	@Override
	protected boolean DataBlockAvailable()
	{
		return !this.dataBlockList.isEmpty();
	}
	*/
	
	@Override
	protected void ProcessDataBlock() throws Exception 
	{
		DataInByteFormatBlock block = null;
		
		block = this.compressDataList.get( this.nextNumSeqCompressedDataBlock );
		
		this.dataBlockProcessed.set( false );
		
		if( block != null )
		{	
			this.clisWriter.saveCompressedData( ConvertTo.ByterArray2byteArray( block.getData() ), block.getName(), block.getDataType(), block.getNumCols() );
			
			this.compressDataList.remove( this.nextNumSeqCompressedDataBlock );
			
			this.nextNumSeqCompressedDataBlock++;
			
			this.dataBlockProcessed.set( true ); 
			// If block == null, wasDataBlockProcessed() return false; otherwise true
			// To avoid this thread can be awakened without data
		}
	}	
	
	
	@Override
	public void SaveZipData( ZipThread zpThread ) throws Exception 
	{
		if( zpThread != null )
		{
			DataInByteFormatBlock block = zpThread.getCompressedData();
			
			int orderedNumStream = -1;
			
			if( block != null )
			{	
				orderedNumStream = block.getSeqNum();
				
				if( orderedNumStream >= this.nextNumSeqCompressedDataBlock )
				{
					this.compressDataList.put( orderedNumStream, block );
					
					synchronized( this )
					{	
						super.notify();
					}
				}			
			}
			
			zpThread.stopThread( IStoppableThread.FORCE_STOP );
			
			this.zpThreadList.remove( zpThread );
		}
	}
	
	@Override
	public boolean finished() 
	{	
		return this.zpThreadList.isEmpty() 
				&& this.compressDataList.isEmpty();
	}

	@Override
	public void addMetadata(String id, String text) throws Exception 
	{
		this.clisWriter.addMetadata(  id, text );
	}

	@Override
	protected boolean wasDataBlockProcessed() 
	{
		return this.dataBlockProcessed.get() ;
	}
	
	@Override
	protected void CloseWriterActions() throws Exception 
	{	
		this.clisWriter.saveMetadata();
		this.clisWriter.close();		
	}

	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{	
	}	
}