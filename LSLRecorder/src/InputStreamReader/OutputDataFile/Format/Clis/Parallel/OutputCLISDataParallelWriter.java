package InputStreamReader.OutputDataFile.Format.Clis.Parallel;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;

import Auxiliar.Extra.ConvertTo;
import InputStreamReader.OutputDataFile.Compress.IOutZip;
import InputStreamReader.OutputDataFile.Compress.OutputZipDataFactory;
import InputStreamReader.OutputDataFile.DataBlock.CompressedByteBlock;
import InputStreamReader.OutputDataFile.DataBlock.DataBlock;
import InputStreamReader.OutputDataFile.Format.Clis.OutputCLISDataWriter;
import StoppableThread.IStoppableThread;

public class OutputCLISDataParallelWriter extends OutputCLISDataWriter implements ICompressDataCollector
{
	private int zipType = OutputZipDataFactory.GZIP;
	
	private ConcurrentSkipListMap< Integer, CompressedByteBlock > compressDataList = null;
	
	private int nextNumSeqCompressedDataBlock = 0;
	
	private List< ZipThread > zpThreadList = null;
	
	public OutputCLISDataParallelWriter( String file, long headersize, int zip, Charset coding ) throws Exception 
	{
		super( file, headersize, zip, coding );
		
		this.zipType = zip;
		
		this.compressDataList = new ConcurrentSkipListMap< Integer, CompressedByteBlock >();
		this.zpThreadList = new ArrayList< ZipThread >();
	}
	
	@Override
	protected int getMaxNumThreads() 
	{
		return Runtime.getRuntime().availableProcessors();
	}
	
	@Override
	protected boolean DataBlockManager(DataBlock dataBlock) throws Exception 
	{
		boolean add = this.zpThreadList.size() < this.getMaxNumThreads() ;
		
		if( add )
		{
			this.Zip( dataBlock.getName(), dataBlock.getDataType(), dataBlock.getNumCols(), dataBlock.getSeqNum(), dataBlock.getData() );
		}
		
		return add; 
	}
	
	private void Zip( String varName, int dataType, int nChannels, int ordered, Object[] data ) throws Exception
	{
		IOutZip zp = OutputZipDataFactory.createOuputZipStream( this.zipType );
		
		ZipThread zipThr = new ZipThread( varName, dataType, nChannels, zp, this, this.charCode );
				
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

	@Override
	protected void ProcessDataBlock() throws Exception 
	{
		CompressedByteBlock block = null;
		
		block = this.compressDataList.get( this.nextNumSeqCompressedDataBlock );
		
		super.dataBlockProcessed.set( false );
		
		if( block != null )
		{		
			super.saveCompressedData( ConvertTo.ByterArray2byteArray( block.getData() ), block.getName(), block.getDataType(), block.getNumCols() );
			
			this.compressDataList.remove( this.nextNumSeqCompressedDataBlock );
			
			this.nextNumSeqCompressedDataBlock++;
			
			super.dataBlockProcessed.set( true );			
		}
	}	
	
	@Override
	public void SaveCompressedData( ZipThread zpThread ) throws Exception 
	{
		if( zpThread != null )
		{
			CompressedByteBlock block = zpThread.getCompressedData();
			
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
			
			System.out.println("OutputCLISDataParallelWriter.SaveCompressedData() REMOVE ZipThread " + orderedNumStream );
			
			this.zpThreadList.remove( zpThread );
		}
	}
	
	@Override
	public boolean finished() 
	{	
		return this.zpThreadList.isEmpty() 
				&& this.compressDataList.isEmpty();
	}
}