package DataStream.OutputDataFile.Format.HDF5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

import Auxiliar.Extra.ConvertTo;
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
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Writer;


public class OutputHDF5DataWriter extends OutputFileWriterTemplate 
{
	private ConcurrentLinkedDeque< DataBlock > dataBlockList = null;
	
	private AtomicBoolean dataBlockProcessed = new AtomicBoolean( true );
	
	private IHDF5Writer writer = null;
	private Map< String, HDF5Data> dataWriters = null;
		
	private List< String > header = null ;
	
	public OutputHDF5DataWriter( String file ) throws Exception 
	{
		super( file, false );
		
		super.setName( this.getClass().getSimpleName() + "-" + this.fileName );
		
		this.dataBlockList = new ConcurrentLinkedDeque< DataBlock >();
				
		this.dataWriters = new HashMap< String, HDF5Data >();		
		
		this.writer = HDF5Factory.open( file );
				
		this.header = new ArrayList<String>();
	}

	@Override
	public void addHeader( String id, String text ) throws Exception 
	{
		id = id.replace("\n", "").replace("\r", "");
		text = text.replace("\n", "").replace("\r", "");

		this.header.add( id + "=" + text );
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

	@Override
	protected boolean DataBlockManager( DataBlock dataBlock ) throws Exception 
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
	protected void ProcessDataBlock() throws Exception
	{	
		if( !this.dataBlockList.isEmpty() )
		{			
			/* KEEP */
			this.dataBlockProcessed.set( false );
			
			DataBlock dataBlock = this.dataBlockList.poll();
			
			/* */
			
			HDF5Data wr = this.dataWriters.get( dataBlock.getName() );
			
			if( wr == null )
			{
				wr = new HDF5Data( this.writer, dataBlock.getName(), dataBlock.getDataType(), dataBlock.getNumCols() );
				this.dataWriters.put( dataBlock.getName(), wr );
			}
			
			if( dataBlock instanceof ByteBlock)
			{
				ByteBlock d = (ByteBlock)dataBlock;
				Byte[] dat = d.getData();

				wr.addData( dat );
			}
			else if(dataBlock instanceof ShortBlock)
			{
				ShortBlock d = (ShortBlock)dataBlock;
				Short[] dat = d.getData();

				wr.addData( dat );
			}
			else if(dataBlock instanceof IntegerBlock )
			{
				IntegerBlock d = (IntegerBlock)dataBlock;
				Integer[] dat = d.getData();
				
				wr.addData( dat );
			}
			else if(dataBlock instanceof LongBlock)
			{
				LongBlock d = (LongBlock)dataBlock;
				Long[] dat = d.getData();

				wr.addData( dat );
			}
			else if(dataBlock instanceof FloatBlock)
			{
				FloatBlock d = (FloatBlock)dataBlock;
				Float[] dat = d.getData();

				wr.addData( dat );
			}
			else if(dataBlock instanceof DoubleBlock)
			{
				DoubleBlock d = (DoubleBlock)dataBlock;
				Double[] dat = d.getData();

				wr.addData( dat );
			}
			else if(dataBlock instanceof CharBlock)
			{
				CharBlock d = (CharBlock)dataBlock;
				Character[] dat = d.getData();

				wr.addData( new String( ConvertTo.CharacterArray2charArray( dat ) ) );
			}
			else if( dataBlock instanceof StringBlock )
			{
				StringBlock d = (StringBlock)dataBlock;
				Character[] dat = d.getData();

				wr.addData( new String( ConvertTo.CharacterArray2charArray( dat ) ) );
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
		return this.dataBlockProcessed.get();
	}

	@Override
	protected void CloseWriterActions() throws Exception 
	{
		this.writer.writeStringArray( "header", this.header.toArray( new String[0] ) );
		
		this.closeWriter();
	}
	
	@Override
	public void closeWriter() throws Exception 
	{
		super.closeWriter();
		
		for( HDF5Data wr : this.dataWriters.values() )
		{
			wr.close();
		}
		
		if( this.writer != null )
		{	
			this.writer.close();
		}
	}


	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{		
	}

}
