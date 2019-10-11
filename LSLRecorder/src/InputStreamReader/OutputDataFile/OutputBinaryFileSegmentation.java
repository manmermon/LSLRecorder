/* 
 * Copyright 2018-2019 by Manuel Merino Monge <manmermon@dte.us.es>
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

package InputStreamReader.OutputDataFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskMonitor;
import Controls.Messages.EventInfo;
import Controls.Messages.eventType;
import InputStreamReader.TemporalData;
import InputStreamReader.TemporalDataStream;
import InputStreamReader.OutputDataFile.DataBlock.ByteBlock;
import InputStreamReader.OutputDataFile.DataBlock.DataBlock;
import InputStreamReader.OutputDataFile.DataBlock.DoubleBlock;
import InputStreamReader.OutputDataFile.DataBlock.FloatBlock;
import InputStreamReader.OutputDataFile.DataBlock.IntegerBlock;
import InputStreamReader.OutputDataFile.DataBlock.LongBlock;
import InputStreamReader.OutputDataFile.DataBlock.ShortBlock;
import InputStreamReader.OutputDataFile.DataBlock.StringBlock;
import InputStreamReader.OutputDataFile.Format.OutputFileWriterTemplate;
import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;
import edu.ucsd.sccn.LSL;

/**
 * 
 * @author Manuel Merino Monge
 *
 */

public class OutputBinaryFileSegmentation extends AbstractStoppableThread implements INotificationTask, ITaskMonitor
{
	private TemporalData DATA;
	private OutputFileWriterTemplate writer;
	private ITaskMonitor monitor;
	
	private List< EventInfo > events;
		
	/**
	 *  Save output data file
	 *  
	 * @param DAT	-> temporal binary data.
	 * @param wr 	-> Output writer.
	 */
	public OutputBinaryFileSegmentation( TemporalData DAT, OutputFileWriterTemplate wr) throws Exception
	{
		if( DAT == null || wr == null )
		{
			throw new IllegalArgumentException( "Input null." );
		}
		
		super.setName( this.getClass().getSimpleName() + "-" + DAT.getStreamingName() );
				
		this.DATA = DAT;
		this.writer = wr;
						
		this.events = new ArrayList< EventInfo >();	
	}

	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#startUp()
	 */
	@Override
	protected void startUp() throws Exception
	{
		super.startUp();
		
		this.writer.taskMonitor( this );
		this.writer.startThread();
	}

	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#runInLoop()
	 */
	@Override
	protected void runInLoop() throws Exception
	{
		if ( this.DATA != null && this.writer != null )
		{
			//int dataType = this.DATA.getDataType(); // LSL type data
			int nChannel = this.DATA.getNumberOfChannels(); // number of channels
			String lslName = this.DATA.getStreamingName(); // LSL streaming name
			String lslXML = this.DATA.getLslXml(); // LSL description

			String variableName = "data"; // data variable name
			String timeVarName = "time"; // time variable name
			String info = "deviceInfo"; // LSL description variable name
			
			int counterDataBlock = -1;
			
			// Header info
			this.writer.addHeader( info + "_" + lslName, lslXML ); // output file header
						
			// Save data
			String varName = variableName + "_" + lslName;
			
			counterDataBlock = this.ProcessDataStream( this.DATA.getDataStream(), counterDataBlock, varName, nChannel );

			// Save time stamps			
			String timeName = timeVarName + "_" + lslName;
			
			counterDataBlock = this.ProcessDataStream( this.DATA.getTimeStream(), counterDataBlock - 1, timeName, 1 );			
		}
		else
		{
			if( this.monitor != null )
			{
				EventInfo event = new EventInfo( eventType.PROBLEM, new IOException( "Problem: it is not possible to write in the file " + this.writer.getFileName() + ", because Writer null."));

				this.events.add( event );
				this.monitor.taskDone( this );
			}
		}
	}

	private int ProcessDataStream( TemporalDataStream stream, int seqNum, String name, int nChannels ) throws Exception
	{
		int counterDataBlock = seqNum;
		
		if( stream != null )
		{
			List< Object > data = null;
			
			boolean getNextData = true;
			
			do
			{
				synchronized ( this )
				{
					while( !this.writer.isReady() )
					{
						try
						{
							super.wait();
						}
						catch ( InterruptedException e) 
						{
						}
					}					
				}
				
				if( getNextData )
				{
					data = stream.getData(); 
					counterDataBlock++;
				}
				
				if( data != null 
						&& data.size() > 0 )
				{
					DataBlock block = this.ListNumberToDataBlock( counterDataBlock, name, nChannels, stream.getDataType(), data );

					if( block != null )
					{
						getNextData = this.writer.saveData( block ); // save data
					}
				}
			}
			while( data.size() > 0 );
		}
		
		return counterDataBlock;
	}
	
	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#targetDone()
	 */
	@Override
	protected void targetDone() throws Exception
	{
		super.targetDone();

		this.stopThread = true;
		
		this.writer.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
		
		synchronized ( this )
		{
			while( !this.writer.finished() )
			{
				try
				{
					super.wait();
				}
				catch ( InterruptedException e) 
				{
				}
			}			
		}		
	}
	
	private DataBlock ListNumberToDataBlock( int secNum, String varName, int nChannel, int dataType, List data ) throws ClassCastException
	{	
		DataBlock b = null;
				
		if( data != null && !data.isEmpty() )
		{
			// Save binary data
			switch ( dataType )
			{
				case LSL.ChannelFormat.float32:
				{
					Float[] aux = new Float[data.size()];
					int i = 0;
					for (Object value  : data)
					{
						aux[i] = (Float)value;
						i++;
					}
	
					b = new FloatBlock( secNum, varName, nChannel, aux );
					
					break;
				}
				case LSL.ChannelFormat.double64:
				{
					Double[] aux = new Double[data.size()];
					int i = 0;
					for (Object value : data)
					{
						aux[i] = (Double)value;
						i++;
					}
	
					b = new DoubleBlock( secNum, varName, nChannel, aux );
	
					break;
				}
				case LSL.ChannelFormat.int64:
				{
					Long[] aux = new Long[data.size()];
					int i = 0;
					for( Object value : data )
					{
						aux[ i ] = (long)value;
						i++;
					}
	
					 b = new LongBlock( secNum, varName, nChannel, aux );
	
					break;
				}
				case  LSL.ChannelFormat.string:
				{
					String aux = new String();
					for( Object value : data )
					{
						aux += (Character)value;
					}
	
					b = new StringBlock( secNum,  varName, 1, aux );
	
					break;
				}
				case LSL.ChannelFormat.int8:
				{
					Byte[] aux = new Byte[data.size()];
					int i = 0;
					for( Object value : data )
					{
						aux[ i ] = (byte)value;
						i++;
					}
					
					b = new ByteBlock( secNum, varName, nChannel, aux );
					
					break;
				}
				case LSL.ChannelFormat.int16:
				{
					Short[] aux = new Short[data.size()];
					int i = 0;
					for (Object value : data)
					{
						aux[i] = (Short)value;
						i++;
					}
	
					b = new ShortBlock( secNum, varName, nChannel, aux );
					
					break;
				}
				default: // LSL.ChannelFormat.int32
				{
					Integer[] aux = new Integer[data.size()];
					int i = 0;
					for (Object value : data)
					{
						aux[i] = (Integer)value;
						i++;
					}
	
					b = new IntegerBlock( secNum, varName, nChannel, aux );
				}
			}
		}
		
		return b;
	}
	
	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#runExceptionManager(java.lang.Exception)
	 */
	@Override
	protected void runExceptionManager(Exception e)
	{
		if (!(e instanceof InterruptedException))
		{
			if( this.monitor != null )
			{
				EventInfo event = new EventInfo( eventType.PROBLEM, new IOException("Problem: it is not possible to write in the file " + this.writer.getFileName() + "\n" + e.getClass()));
				
				this.events.add( event );
				try 
				{
					this.monitor.taskDone( this );
				} 
				catch (Exception e1) 
				{	
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#cleanUp()
	 */
	@Override
	protected void cleanUp() throws Exception
	{
		super.cleanUp();

		//this.writer.closeWriter();
		this.writer.stopThread( IStoppableThread.FORCE_STOP );
		this.writer = null;
			
		this.DATA.closeTempBinaryFile();
		
		//this.WriterloopEndInteractionNotifier.stopThread( IStoppableThread.FORCE_STOP );
		//this.WriterloopEndInteractionNotifier = null;
		
		if( this.monitor != null )
		{		
			EventInfo event = new EventInfo( eventType.OUTPUT_DATA_FILE_SAVED, DATA.getStreamingName() );
			this.events.add( event );
		
			this.monitor.taskDone( this );
		}
	}

	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#preStopThread(int)
	 */
	@Override
	protected void preStopThread(int friendliness) throws Exception
	{}

	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#postStopThread(int)
	 */
	@Override
	protected void postStopThread(int friendliness) throws Exception
	{}

	/*
	 * (non-Javadoc)
	 * @see Auxiliar.Tasks.INotificationTask#taskMonitor(Auxiliar.Tasks.ITaskMonitor)
	 */
	@Override
	public void taskMonitor( ITaskMonitor m )
	{
		this.monitor = m;
	}

	/*
	 * (non-Javadoc)
	 * @see Auxiliar.Tasks.INotificationTask#getResult()
	 */
	@Override
	public synchronized List<EventInfo> getResult() 
	{			
		return this.events;
	}

	/*
	 * (non-Javadoc)
	 * @see Auxiliar.Tasks.INotificationTask#clearResult()
	 */
	@Override
	public synchronized void clearResult() 
	{
		this.events.clear();
	}
	
	@Override
	public String getID() 
	{
		return super.getName();
	}
	
	@Override
	public void taskDone(INotificationTask task) throws Exception 
	{
		List< EventInfo > EVENTS =  new ArrayList< EventInfo>( task.getResult() );
		task.clearResult();
		
		for( EventInfo e : EVENTS )
		{		 
			if( e.getEventType().equals( eventType.THREAD_STOP ) )
			{
				System.out.println("OutputBinaryFileSegmentation.taskDone() THREAD STOP");
				synchronized ( this )
				{
					super.notify();
				}
			}
			else if( e.getEventType().equals( eventType.OUTPUT_FILE_WRITER_READY ) )
			{
				synchronized ( this )
				{						
					super.notify();
				}		
			}
		}
	}
	
	//////////////////////////////////////////
	//
	//
	//
	
	/*
	private class LoopEndInteractionNotifier extends AbstractStoppableThread
	{
		private OutputBinaryFileHandler handler = null;
		
		public LoopEndInteractionNotifier( OutputBinaryFileHandler Handler ) 
		{
			this.handler = Handler;
					
			super.setName( super.getClass().getSimpleName() );
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
			synchronized ( this )
			{
				try
				{
					super.wait();
				}
				catch ( InterruptedException e) 
				{
				}
			}
			
			synchronized ( counterProcessingDataBlocks ) 
			{
				if( counterProcessingDataBlocks.incrementAndGet() > maxNumProcessors )
				{
					counterProcessingDataBlocks.set( maxNumProcessors );
				}
				
				//System.out.println("OutputBinaryFileHandler.LoopEndInteractionNotifier.runInLoop() " + counterProcessingDataBlocks.get() );
			}	

			if( this.handler != null )
			{
				synchronized ( this.handler )
				{
					this.handler.notify();
				}
			}
		}		
	}
	*/
}
