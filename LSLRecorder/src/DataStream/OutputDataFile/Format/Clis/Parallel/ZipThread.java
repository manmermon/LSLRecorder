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

import Auxiliar.Extra.ConvertTo;
import Auxiliar.Extra.Tuple;
import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskMonitor;
import Controls.Messages.EventInfo;
import Controls.Messages.EventType;
import DataStream.OutputDataFile.Compress.IOutZip;
import DataStream.OutputDataFile.DataBlock.DataInByteFormatBlock;
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
	
	private long numChannels;
	
	private int order = -1;
	
	private List< EventInfo > events = null;
	private ITaskMonitor monitor = null;
	
	private DataInByteFormatBlock compressedData = null;
	
	public ZipThread( String varName, int dataType, long nChannels, IOutZip zp, ICompressDataCollector col, Charset coding ) throws NullPointerException 
	{
		if( zp == null || col == null )
		{
			throw new NullPointerException( "Any input is null." );
		}
		
		super.setName( this.getClass().getName() + "-" + super.getId() );
		
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
				this.compressedData = new DataInByteFormatBlock( n, this.varName, this.DataType, this.numChannels, ConvertTo.byteArray2ByteArray( compressData ) );				
				this.collector.SaveZipData( this );
			}
		}
	}
	
	public DataInByteFormatBlock getCompressedData()
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
			this.order = ordered;
			
			this.DataBlock = new Tuple<Integer, Object[] >( ordered, data );
		}
	}
	
	public int getZipOrdered()
	{
		return this.order;
	}

	@Override
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
		
		if( this.monitor != null )
		{
			EventInfo e = new  EventInfo( this.getID(), EventType.THREAD_STOP, this );
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
	public List<EventInfo> getResult( boolean clear) 
	{
		List< EventInfo > evs = new ArrayList< EventInfo >();
		
		synchronized ( this.events )
		{
			evs.addAll( this.events );
		
			if( clear )
			{
				this.events.clear();
			}
			
		}
		
		return evs;
	}

	@Override
	public void clearResult() 
	{
		synchronized ( this.events )
		{
			this.events.clear();
		}				
	}

	@Override
	public String getID() 
	{
		return super.getName();
	}
}
