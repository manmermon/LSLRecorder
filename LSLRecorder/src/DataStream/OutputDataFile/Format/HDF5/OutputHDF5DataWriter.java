package DataStream.OutputDataFile.Format.HDF5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import Auxiliar.Extra.ConvertTo;
import Auxiliar.Tasks.ITaskMonitor;
import DataStream.OutputDataFile.IOutputDataFileWriter;
import DataStream.OutputDataFile.DataBlock.ByteBlock;
import DataStream.OutputDataFile.DataBlock.CharBlock;
import DataStream.OutputDataFile.DataBlock.DataBlock;
import DataStream.OutputDataFile.DataBlock.DoubleBlock;
import DataStream.OutputDataFile.DataBlock.FloatBlock;
import DataStream.OutputDataFile.DataBlock.IntegerBlock;
import DataStream.OutputDataFile.DataBlock.LongBlock;
import DataStream.OutputDataFile.DataBlock.ShortBlock;
import DataStream.OutputDataFile.DataBlock.StringBlock;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Writer;

public class OutputHDF5DataWriter implements IOutputDataFileWriter 
{
	private IHDF5Writer writer = null;
	private Map< String, HDF5Data> dataWriters = null;
		
	private List< String > header = null ;
	
	private String fileName = null;
	
	private AtomicBoolean savedDataBlock = new AtomicBoolean( true );
	
	public OutputHDF5DataWriter( String file, ITaskMonitor monitor ) throws Exception 
	{
		this.fileName = file;
				
		this.dataWriters = new HashMap< String, HDF5Data >();		
		
		this.taskMonitor( monitor );
		
		this.writer = HDF5Factory.open( file );
				
		this.header = new ArrayList<String>();
	}

	@Override
	public void addMetadata( String id, String text ) throws Exception 
	{
		id = id.replace("\n", "").replace("\r", "");
		text = text.replace("\n", "").replace("\r", "");

		this.header.add( id + "=" + text );
	}

	@Override
	public boolean finished() 
	{
		return this.savedDataBlock.get();
	}

	@Override
	public boolean saveData( DataBlock dataBlock ) throws Exception
	{	
		this.savedDataBlock.set( false );
		
		if( dataBlock != null )
		{			
					
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
			
		}
		
		this.savedDataBlock.set( true );
		
		return true;
	}


	@Override
	public void close() throws Exception 
	{
		this.writer.writeStringArray( "header", this.header.toArray( new String[0] ) );
		
		for( HDF5Data wr : this.dataWriters.values() )
		{
			wr.close();
		}
		
		this.writer.close();
	}

	@Override
	public void taskMonitor(ITaskMonitor monitor) 
	{
	}

	@Override
	public String getFileName() 
	{
		return this.fileName;
	}
}
