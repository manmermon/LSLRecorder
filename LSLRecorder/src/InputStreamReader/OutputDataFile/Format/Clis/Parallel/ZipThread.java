package InputStreamReader.OutputDataFile.Format.Clis.Parallel;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import Auxiliar.Extra.ConvertTo;
import Auxiliar.Extra.Tuple;
import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskMonitor;
import Controls.Messages.EventInfo;
import Controls.Messages.eventType;
import InputStreamReader.OutputDataFile.Compress.IOutZip;
import InputStreamReader.OutputDataFile.DataBlock.CompressedByteBlock;
import StoppableThread.AbstractStoppableThread;
import edu.ucsd.sccn.LSL;

public class ZipThread extends AbstractStoppableThread implements INotificationTask  
{
	public final static int BYTE_TYPE = 0;
	public final static int SHORT_TYPE = 1;
	public final static int INT_TYPE = 2;
	public final static int LONG_TYPE = 3;
	public final static int FLOAT_TYPE = 4;
	public final static int DOUBLE_TYPE = 5;
	public final static int STRING_TYPE = 6;
	
	private IOutZip zip;
	private ICompressDataCollector collector;
	
	private Tuple< Integer, Object[] > DataBlock = null;
	
	private Object sync = new Object();
	
	private int DataType;
	
	private Charset charCode;
	
	private String varName;
	
	private int numChannels;
	
	private List< EventInfo > events = null;
	private ITaskMonitor monitor = null;
	
	private CompressedByteBlock compressedData = null;
	
	public ZipThread( String varName, int dataType, int nChannels, IOutZip zp, ICompressDataCollector col, Charset coding ) throws NullPointerException 
	{
		if( zp == null || col == null )
		{
			throw new NullPointerException( "Any input is null." );
		}
		
		super.setName( this.getClass().getName() );
		
		this.varName = varName;
		this.numChannels = nChannels;		
		
		this.zip = zp;
		this.collector = col;
		
		this.DataBlock = null;
		
		this.DataType = dataType;
		
		this.charCode = coding;
		if( this.charCode == null )
		{
			this.charCode = Charset.forName( "UTF-8" );
		}
		
		this.events = new ArrayList<EventInfo>();
	}
		
	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{	
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{	
	}

	@Override
	protected void runInLoop() throws Exception 
	{
		int n = -1;
		Object[] data = null;

		synchronized ( this.sync )
		{
			if( this.DataBlock != null )
			{				
				n = this.DataBlock.x;
				data = this.DataBlock.y;

				this.DataBlock = null;			
			}
		}

					
		if( data != null && data.length > 0 )
		{
			byte[] compressData = null;

			// Save binary data
			switch ( this.DataType )
			{
				case LSL.ChannelFormat.float32:
				{
					float[] aux = new float[data.length];
					int i = 0;
					for (Object value  : data)
					{
						aux[i] = ((Float)value).floatValue();
						i++;
					}
	
					compressData = this.zip.zipData( aux );
	
					break;
				}
				case LSL.ChannelFormat.double64:
				{
					double[] aux = new double[data.length];
					int i = 0;
					for (Object value : data)
					{
						aux[i] = ((Double)value).doubleValue();
						i++;
					}
	
					compressData = this.zip.zipData( aux );
	
					break;
				}
				case LSL.ChannelFormat.int64:
				{
					long[] aux = new long[data.length];
					int i = 0;
					for( Object value : data )
					{
						aux[ i ] = (long)value;
						i++;
					}
	
					compressData = this.zip.zipData( aux );
	
					break;
				}
				case  LSL.ChannelFormat.string:
				{
					String aux = new String();
					for( Object value : data )
					{
						aux += (Character)value;
	
					}
	
					compressData = this.zip.zipData( aux.toCharArray(), this.charCode );
	
					break;
				}
				case LSL.ChannelFormat.int8:
				{
					byte[] aux = new byte[data.length];
					int i = 0;
					for( Object value : data )
					{
						aux[ i ] = (byte)value;
						i++;
					}
	
					compressData = this.zip.zipData( aux );
	
					break;
				}
				case LSL.ChannelFormat.int16:
				{
					short[] aux = new short[data.length];
					int i = 0;
					for (Object value : data)
					{
						aux[i] = ((Short)value).shortValue();
						i++;
					}
	
					compressData = this.zip.zipData( aux );
	
					break;
				}
				default: // LSL.ChannelFormat.int32
				{
					int[] aux = new int[data.length];
					int i = 0;
					for (Object value : data)
					{
						aux[i] = ((Integer)value).intValue();
						i++;
					}
	
					compressData = this.zip.zipData( aux );
				}
			}

			if( compressData != null )
			{
				this.compressedData = new CompressedByteBlock( n, this.varName, this.DataType, this.numChannels, ConvertTo.byteArray2ByteArray( compressData ) );				
				this.collector.SaveCompressedData( this );
			}
		}
	}
	
	public CompressedByteBlock getCompressedData()
	{
		return this.compressedData;
	}
	
	@Override
	protected void targetDone() throws Exception 
	{
		super.stopThread = true;
	}
	
	public void CompressData( int ordered, Object[] data )
	{
		synchronized ( this.sync ) 
		{
			this.DataBlock = new Tuple<Integer, Object[] >( ordered, data );
		}
	}

	@Override
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
		
		if( this.monitor != null )
		{
			EventInfo e = new  EventInfo( eventType.THREAD_STOP, this );
			this.events.add( e );
			
			this.monitor.taskDone( this );
		}
	}
	
	@Override
	public void taskMonitor(ITaskMonitor m ) 
	{
		this.monitor = m;
	}

	@Override
	public List<EventInfo> getResult() 
	{
		return this.events;
	}

	@Override
	public void clearResult() 
	{
		this.events.clear();		
	}

	@Override
	public String getID() 
	{
		return super.getName();
	}
}
