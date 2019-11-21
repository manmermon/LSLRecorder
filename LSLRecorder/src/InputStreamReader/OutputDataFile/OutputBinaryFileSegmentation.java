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

package InputStreamReader.OutputDataFile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import Auxiliar.Extra.ConvertTo;
import Auxiliar.Extra.Tuple;
import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskMonitor;
import Controls.Messages.EventInfo;
import Controls.Messages.EventType;
import InputStreamReader.TemporalData;
import InputStreamReader.OutputDataFile.DataBlock.ByteBlock;
import InputStreamReader.OutputDataFile.DataBlock.DataBlock;
import InputStreamReader.OutputDataFile.DataBlock.DataBlockFactory;
import InputStreamReader.OutputDataFile.Format.DataFileFormat;
import InputStreamReader.OutputDataFile.Format.OutputFileFormatParameters;
import InputStreamReader.OutputDataFile.Format.OutputFileWriterTemplate;
import InputStreamReader.Sync.SyncMarker;
import InputStreamReader.Sync.SyncMarkerBinFileReader;
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
	private int BLOCK_SIZE = (int)( 5 * ( Math.pow( 2, 20 ) ) ); 
	private int maxNumElements = BLOCK_SIZE / Float.BYTES; // 5 MB  
	
	private TemporalData DATA;
	private SyncMarkerBinFileReader syncReader;
	
	private OutputFileWriterTemplate writer;
	private ITaskMonitor monitor;
		
	private List< EventInfo > events;
	
	/**
	 *  Save output data file
	 *  
	 * @param DAT	-> temporal binary data.
	 * @param SYN -> temporal binary sync markers. 
	 */
	public OutputBinaryFileSegmentation( TemporalData DAT, SyncMarkerBinFileReader syncReader ) throws Exception //SyncMarkerCollectorWriter markCollector ) throws Exception
	{
		//this( DAT, markCollector, (byte)5 );
		this( DAT, syncReader, (byte)5 );
	}
	
	/**
	 *  Save output data file
	 *  
	 * @param DAT	-> temporal binary data.
	 * @param SYN -> temporal binary sync markers.
	 * @param bufLen -> buffer length in MiB. 
	 */
	public OutputBinaryFileSegmentation( TemporalData DAT, SyncMarkerBinFileReader syncReader, byte bufLen ) throws Exception //SyncMarkerCollectorWriter markCollector, byte bufLen ) throws Exception
	{
		if( DAT == null || syncReader == null )
		{
			throw new IllegalArgumentException( "Input null." );
		}
		
		super.setName( this.getClass().getSimpleName() + "-" + DAT.getStreamingName() );
		
		this.DATA = DAT;
				
		this.BLOCK_SIZE = (int)( 5 * Math.pow( 2, 20 ) );
		
		if( bufLen > 0)
		{						
			this.BLOCK_SIZE = (int)( bufLen * Math.pow( 2, 20 ) );

			this.maxNumElements = ( this.BLOCK_SIZE / this.DATA.getTypeDataBytes() ); 
					
			this.maxNumElements = (int)( ( Math.floor( 1.0D * this.maxNumElements / ( this.DATA.getNumberOfChannels() + 1 ) ) ) * ( this.DATA.getNumberOfChannels() + 1 ) );
			
			if( this.maxNumElements < this.DATA.getNumberOfChannels() )
			{
				this.maxNumElements = this.DATA.getNumberOfChannels();
			}			
		}		
	
		
		this.syncReader = syncReader;
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
		
		// Header size stimation
		long binFileSize = this.DATA.getDataBinaryFileSize();
		binFileSize = (long) Math.ceil( 1.0D * binFileSize / this.BLOCK_SIZE );
		
		long timeBlocks = this.syncReader.getFileSize();
		timeBlocks = (long)Math.ceil(  1.0D * timeBlocks / this.BLOCK_SIZE );

		int blockSizeStrLen = Integer.toString( this.BLOCK_SIZE ).length() + 1;

		long headerSize = binFileSize * blockSizeStrLen + timeBlocks * blockSizeStrLen;

		String xml = this.DATA.getLslXml();						

		headerSize += xml.toCharArray().length;
		headerSize += ( this.DATA.getStreamingName().length() + 10 ) * 4 ; // device info, binary and time data; 10 -> length of data type in string
		headerSize += Integer.toString( this.DATA.getNumberOfChannels() + 1 ).length() * 2; // channel numbers 

		// Setting		
		String outFormat = this.DATA.getOutputFileFormat();
		OutputFileFormatParameters pars = new OutputFileFormatParameters();
		pars.setCharset( Charset.forName( "UTF-8") );
		pars.setHeaderSize( headerSize );
		pars.setCompressType( DataFileFormat.getCompressTech( outFormat ) );
		
		OutputFileWriterTemplate wr = DataFileFormat.getDataFileWriter( outFormat, this.DATA.getOutputFileName(), pars );
			
		this.writer = wr;
		
				
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
			//int nChannel = this.DATA.getNumberOfChannels(); // number of channels
			String lslName = this.DATA.getStreamingName(); // LSL streaming name
			String lslXML = this.DATA.getLslXml(); // LSL description

			String variableName = "data"; // data variable name
			String timeVarName = "time"; // time variable name
			String info = "deviceInfo"; // LSL description variable name
			
			int counterDataBlock = 0;
			
			// Header info
			this.writer.addHeader( info + "_" + lslName, lslXML ); // output file header
						
			// Save data
			String varName = variableName + "_" + lslName;
			
			counterDataBlock = this.ProcessDataAndSync( counterDataBlock, varName );
			
			// Save time stamps			
			String timeName = timeVarName + "_" + lslName;
			
			this.DATA.reset();
			counterDataBlock = this.ProcessTimeStream(  this.DATA, this.DATA.getTimeDataType(), counterDataBlock, timeName );			
		}
		else
		{
			if( this.monitor != null )
			{
				EventInfo event = new EventInfo( EventType.PROBLEM, new IOException( "Problem: it is not possible to write in the file " + this.writer.getFileName() + ", because Writer null."));

				this.events.add( event );
				this.monitor.taskDone( this );
			}
		}
	}

	private int ProcessDataAndSync( int seqNum,  String name ) throws Exception
	{
		List< Object > dataBuffer = new ArrayList< Object >();
		
		Object NonSyncMarker = SyncMarker.NON_MARK;
		
		if( this.DATA.getDataType() == LSL.ChannelFormat.string )
		{
			NonSyncMarker = "" + SyncMarker.NON_MARK;
		}
		else
		{
			 NonSyncMarker = ConvertTo.NumberTo( SyncMarker.NON_MARK, this.DATA.getDataType() );
		}
								
		SyncMarker marker = this.syncReader.getSyncMarker();	
		
		boolean Loop = true;
		
		while( Loop )
		{			
			Tuple< Number[], Number[] > block = null;
			
			if( this.DATA.getDataType() != LSL.ChannelFormat.string 
					&& this.DATA.getDataType() != LSL.ChannelFormat.undefined 
				)
			{
				block = this.getNextNumberBlock( this.DATA );
				
				if( block != null )
				{
					Number[] dat = block.x;
					Number[] timeData = block.y;
										
					if( this.DATA.getChunckSize() > 1 && !this.DATA.isInterleave() )
					{
						Number[] copy = new Number[ dat.length ];
						
						int indexCopy = 0;
						
						for( int i = 0; i < this.DATA.getNumberOfChannels(); i++ )
						{
							for( int j = i; j < dat.length; j += this.DATA.getChunckSize() )
							{
								copy[ indexCopy ] = dat[ j ];
								indexCopy++;
							}
						}
						
						dat = copy;
					}
					
					Number[] Data = new Number[ dat.length + this.DATA.getChunckSize() ];
					
					int index = 0;
					for( int i = 0; i < dat.length; i++ )
					{
						Data[ index ] = dat[ i ];
						
						index++;
						
						if( index % this.DATA.getNumberOfChannels() == 0)
						{
							Data[ index ] = (Number)NonSyncMarker;
							
							index++;
						}
					}
					
					if( marker != null )
					{
						index = 0;
						
						while( index < timeData.length )
						{
							Number time = timeData[ index ];
							
							SyncMarker aux = new SyncMarker( marker.getMarkValue(), marker.getTimeMarkValue() );
														
							while( aux != null 
									&& time.doubleValue() > aux.getTimeMarkValue() )
							{
								marker.addMarkValue( aux.getMarkValue() );
								
								aux = this.syncReader.getSyncMarker();																
							}
							
							if( time.doubleValue() > marker.getTimeMarkValue() )
							{
								Data[ ( index + 1 ) * ( this.DATA.getNumberOfChannels() + 1 ) - 1] = ConvertTo.NumberTo( marker.getMarkValue(), this.DATA.getDataType() );
								
								marker = null;
								if( aux != null  )
								{
									marker = aux;
								}								
							}
							
							index++;
						}
						
						for( Number datVal : Data )
						{
							dataBuffer.add( datVal );
						}
					}
									
					if( dataBuffer.size() >= this.maxNumElements )
					{					
						DataBlock dataBlock = DataBlockFactory.getDataBlock( this.DATA.getDataType(), seqNum, name, this.DATA.getNumberOfChannels() + 1, dataBuffer.toArray() );
						
						dataBuffer.clear();
						
						synchronized ( this )
						{
							while( !this.writer.isReady() )
							{
								try
								{
									super.wait( 1000L );
								}
								catch ( InterruptedException e) 
								{
								}
							}					
						}
						
						this.writer.saveData( dataBlock );
						
						seqNum++;
					}
				}				
				
			}
			
			Loop = ( block != null );
		}
		
		if( !dataBuffer.isEmpty() )
		{
			DataBlock dataBlock = DataBlockFactory.getDataBlock( this.DATA.getDataType(), seqNum, name, this.DATA.getNumberOfChannels() + 1, dataBuffer.toArray() );

			dataBuffer.clear();

			synchronized ( this )
			{
				while( !this.writer.isReady() )
				{
					try
					{
						super.wait( 1000L );
					}
					catch ( InterruptedException e) 
					{
					}
				}					
			}

			this.writer.saveData( dataBlock );

			seqNum++;
		}
		
		return seqNum;
	}
		
	private Tuple< Number[], Number[] > getNextNumberBlock( TemporalData temp ) throws Exception
	{
		List< Object > dataBuffer = new ArrayList< Object >();
		
		Tuple< Number[], Number[] > out = null;
		
		if( temp != null )
		{
			List< ByteBlock > block = temp.getDataBlocks();
			
			if( block != null )
			{
				Number[] data = null; 
				Number[] time = null; 
				
				for( int index = 0; index < block.size(); index++ )
				{
					ByteBlock bytes = block.get( index );
					
					if( index == 0 )
					{
						data = ConvertTo.ByteArrayTo( bytes.getData(), temp.getDataType() );
					}
					else if( index == 1 )
					{
						time = ConvertTo.ByteArrayTo( bytes.getData(), temp.getTimeDataType() );
					}
				}
				
				if( data != null || time != null )
				{
					out = new Tuple<Number[], Number[]>( data, time );
				}
			}
		}
		
		return out;
	}
	
	private int ProcessTimeStream( TemporalData stream, int dataType, int seqNum, String name ) throws Exception
	{	
		if( stream != null )
		{
			List< Object > dataBuffer = new ArrayList< Object >();
			Tuple< Number[], Number[] > block = null;
						
			do
			{
				if( stream.getDataType() != LSL.ChannelFormat.string 
						&& stream.getDataType() != LSL.ChannelFormat.undefined 
					)
				{
					block = this.getNextNumberBlock( stream );
					
					if( block != null )
					{
						Number[] times = block.y;
						
						if( times != null )
						{
							for( Number t : times )
							{
								dataBuffer.add( t );
							}
						}
					}
						
					if( dataBuffer.size() >= this.maxNumElements )
					{
						DataBlock dataBlock = DataBlockFactory.getDataBlock( dataType, seqNum, name, 1, dataBuffer.toArray() );
						
						dataBuffer.clear();
						
						synchronized ( this )
						{
							while( !this.writer.isReady() )
							{
								try
								{
									super.wait( 1000L );
								}
								catch ( InterruptedException e) 
								{
								}
							}					
						}
						
						this.writer.saveData( dataBlock );
						
						seqNum++;
					}
				}
			}
			while( block != null );		
			
			if( !dataBuffer.isEmpty() )
			{
				DataBlock dataBlock = DataBlockFactory.getDataBlock( dataType, seqNum, name, 1, dataBuffer.toArray() );
				
				dataBuffer.clear();
				
				synchronized ( this )
				{
					while( !this.writer.isReady() )
					{
						try
						{
							super.wait( 1000L );
						}
						catch ( InterruptedException e) 
						{
						}
					}					
				}
				
				this.writer.saveData( dataBlock );
				
				seqNum++;
			}
		}
		
		return seqNum;
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
					super.wait( 1000L );
				}
				catch ( InterruptedException e) 
				{
				}
			}			
		}		
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
				EventInfo event = new EventInfo( EventType.PROBLEM, new IOException("Problem: it is not possible to write in the file " + this.writer.getFileName() + "\n" + e.getClass()));
				
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
		this.syncReader.closeStream();
		
		//this.WriterloopEndInteractionNotifier.stopThread( IStoppableThread.FORCE_STOP );
		//this.WriterloopEndInteractionNotifier = null;
		
		if( this.monitor != null )
		{		
			EventInfo event = new EventInfo( EventType.OUTPUT_DATA_FILE_SAVED, DATA.getStreamingName() );
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
			if( e.getEventType().equals( EventType.THREAD_STOP ) )
			{
				synchronized ( this )
				{
					super.notify();
				}
			}
			else if( e.getEventType().equals( EventType.OUTPUT_FILE_WRITER_READY ) )
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
