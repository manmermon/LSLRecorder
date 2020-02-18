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

package DataStream.OutputDataFile;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import Auxiliar.Extra.ConvertTo;
import Auxiliar.Extra.Tuple;
import Auxiliar.Tasks.IMonitoredTask;
import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskIdentity;
import Auxiliar.Tasks.ITaskMonitor;
import Auxiliar.Tasks.NotificationTask;
import Config.ConfigApp;
import Controls.Messages.EventInfo;
import Controls.Messages.EventType;
import DataStream.Binary.Reader.TemporalBinData;
import DataStream.OutputDataFile.DataBlock.ByteBlock;
import DataStream.OutputDataFile.DataBlock.DataBlock;
import DataStream.OutputDataFile.DataBlock.DataBlockFactory;
import DataStream.OutputDataFile.Format.DataFileFormat;
import DataStream.OutputDataFile.Format.OutputFileFormatParameters;
import DataStream.Sync.SyncMarker;
import DataStream.Sync.SyncMarkerBinFileReader;
import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLConfigParameters;
import edu.ucsd.sccn.LSLUtils;

/**
 * 
 * @author Manuel Merino Monge
 *
 */

public class OutputBinaryFileSegmentation extends AbstractStoppableThread implements ITaskMonitor, ITaskIdentity, IMonitoredTask
{
	private int BLOCK_SIZE = ConfigApp.DEFAULT_SEGMENTATION_BLOCK_SIZE; 
	private int maxNumElements = BLOCK_SIZE / Float.BYTES; // 5 MB  
	
	private TemporalBinData DATA;
	private SyncMarkerBinFileReader syncReader;
	
	private IOutputDataFileWriter writer;
	private ITaskMonitor monitor;
	
	private NotificationTask notifTask = null;
	
	private AtomicInteger antideadlockCounter = new AtomicInteger( 0 );
	
	//private List< EventInfo > events;
	
	private long totalBlock = 0;
	
	private double totalSampleByChannels = 0;
	
	private String encrypt_key = null;
	
	/**
	 *  Save output data file
	 *  
	 * @param DAT	-> temporal binary data.
	 * @param SYN -> temporal binary sync markers. 
	 */
	public OutputBinaryFileSegmentation( TemporalBinData DAT, SyncMarkerBinFileReader syncReader ) throws Exception //SyncMarkerCollectorWriter markCollector ) throws Exception
	{
		//this( DAT, markCollector, (byte)10 );
		this( DAT, syncReader, (byte) 0 ); // Default buffer length
	}
	
	/**
	 *  Save output data file
	 *  
	 * @param DAT	-> temporal binary data.
	 * @param SYN -> temporal binary sync markers.
	 * @param bufLen -> buffer length in MiB. 
	 */
	public OutputBinaryFileSegmentation( TemporalBinData DAT, SyncMarkerBinFileReader syncReader, byte bufLen ) throws Exception //SyncMarkerCollectorWriter markCollector, byte bufLen ) throws Exception
	{
		if( DAT == null )
		{
			throw new IllegalArgumentException( "Data input null." );
		}
		
		super.setName( this.getClass().getSimpleName() + "-" + DAT.getOutputFileName() );
		
		this.encrypt_key = DAT.getEncryptKey();
		
		this.DATA = DAT;
				
		this.BLOCK_SIZE = ConfigApp.DEFAULT_SEGMENTATION_BLOCK_SIZE;
		
		if( bufLen <= 0 )
		{						
			bufLen = (byte)10;
		}		
		
		this.BLOCK_SIZE = (int)( bufLen * Math.pow( 2, 20 ) );
				
		this.setMaxNumElements( this.DATA.getTypeDataBytes(), this.DATA.getNumberOfChannels() + 1 ); 
		
		this.syncReader = syncReader;		

		this.totalBlock = 2 + ( DAT.getDataBinaryFileSize() / ( this.maxNumElements * this.DATA.getTypeDataBytes() ) );
			
	}
	
	private void setMaxNumElements( int dataTypeBytes, int channels )
	{
		this.maxNumElements = (int)( ( Math.floor( 1.0D * this.maxNumElements / channels ) ) * channels );
		
		if( this.maxNumElements < this.DATA.getNumberOfChannels() )
		{
			this.maxNumElements = this.DATA.getNumberOfChannels();
		}	
	}
	
	@Override
	protected void preStart() throws Exception 
	{
		// TODO Auto-generated method stub
		super.preStart();
		
		synchronized ( this )
		{
			if( this.monitor != null )
			{
				this.notifTask = new NotificationTask( false );
				this.notifTask.setName( this.notifTask.getID() + "-" + this.getID() );
				this.notifTask.taskMonitor( this.monitor );
				this.notifTask.startThread(); 
			}
			
			// Setting		
			String outFormat = this.DATA.getOutputFileFormat();
			OutputFileFormatParameters pars = new OutputFileFormatParameters();
			pars.setCharset( Charset.forName( "UTF-8") );			
			pars.setCompressType( DataFileFormat.getCompressTech( outFormat ) );
			pars.setEncryptKey( this.encrypt_key );
			pars.setBlockDataLength( this.BLOCK_SIZE );
			
			// Header size stimation
			long binFileSizeLen = this.DATA.getDataBinaryFileSize();
			binFileSizeLen = (long) Math.ceil( 1.0D * binFileSizeLen / this.BLOCK_SIZE );
			
			long syncBlockLen = 0;
			
			if( this.syncReader != null )
			{
				syncBlockLen = this.syncReader.getFileSize();
				syncBlockLen = (long)Math.ceil(  1.0D * syncBlockLen / this.BLOCK_SIZE );
			}
	
			pars.setNumerOfBlock( syncBlockLen + binFileSizeLen  );
			pars.setDataInfo( this.DATA.getLslXml() );
			pars.setDataNames( this.DATA.getStreamingName() );
			pars.setChannels( this.DATA.getNumberOfChannels() );
			
			IOutputDataFileWriter wr = DataFileFormat.getDataFileWriter( outFormat, this.DATA.getOutputFileName(), pars, this );
					
			this.writer = wr;
		}
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
			
			int counterDataBlock = 0;
									
			// Save data
			String varName = variableName + "_" + lslName;
			
			this.setMaxNumElements( this.DATA.getDataType(), nChannel + 1 );			
			counterDataBlock = this.ProcessDataAndSync( counterDataBlock, varName );
			
			// Save time stamps			
			String timeName = timeVarName + "_" + lslName;
			 
			
			this.DATA.reset();
			this.setMaxNumElements( this.DATA.getTimeDataType(), 1 );
			counterDataBlock = this.ProcessTimeStream(  this.DATA, this.DATA.getTimeDataType(), counterDataBlock, timeName );
			
			// Header info
			
			lslXML = this.addElementToXml( lslXML, LSLUtils.getAdditionalInformationLabelInXml()
													, LSLConfigParameters.ID_RECORDED_SAMPLES_BY_CHANNELS
													, (long)( this.totalSampleByChannels / ( nChannel + 2 ) )); // nChannel + 2: channels + marker column + time ;			
			
			this.writer.addMetadata( info + "_" + lslName, lslXML ); // output file header
		}
		else
		{
			/*
			if( this.monitor != null )
			{
				EventInfo event = new EventInfo( this.getID(), EventType.PROBLEM, new IOException( "Problem: it is not possible to write in the file " + this.writer.getFileName() + ", because Writer null."));

				this.events.add( event );
				this.monitor.taskDone( this );
			}
			*/
			
			if( this.notifTask != null )
			{
				EventInfo event = new EventInfo( this.getID(), EventType.PROBLEM, new IOException( "Problem: it is not possible to write in the file " + this.writer.getFileName() + ", because Writer null."));
				
				this.notifTask.addEvent( event );
				synchronized ( this.notifTask )
				{
					this.notifTask.notify();
				}				
			}
			
		}
	}
	
	private String addElementToXml( String xml, String nodeRoot, String name, long value )
	{			
		if( xml != null && nodeRoot != null && name != null )
		{
			Document doc = this.convertStringToXMLDocument( xml );

			if( doc != null )
			{
				NodeList nodes = doc.getElementsByTagName( nodeRoot );
				
				if( nodes.getLength() > 0 )
				{
					try 
				    {
						Element newElement = doc.createElement( name );
						newElement.appendChild( doc.createTextNode( value + "" ) );
						
						Node node = nodes.item( 0 );
						node.appendChild( newElement );
					
						TransformerFactory tf = TransformerFactory.newInstance();
					    Transformer transformer;
				    
				        transformer = tf.newTransformer();
				        transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes");
				        
				        StringWriter writer = new StringWriter();
				        
				        //transform document to string 
				        transformer.transform(new DOMSource( doc ), new StreamResult( writer ) );
				 
				        xml = writer.getBuffer().toString(); 
				    }
				    catch( Exception e )
				    {		    	
				    }
				}
			}
		}
		
		return xml;
	}	
	
	private Document convertStringToXMLDocument(String xmlString) 
    {
		Document doc = null;
		
		if( xmlString != null && xmlString.length() > 0 )
		{
	        //Parser that produces DOM object trees from XML content
	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	         
	        //API to obtain DOM Document instance
	        DocumentBuilder builder = null;
	        try
	        {
	            //Create DocumentBuilder with default configuration
	            builder = factory.newDocumentBuilder();
	             
	            //Parse the content to Document object
	           doc = builder.parse(new InputSource(new StringReader( xmlString ) ) );
	        } 
	        catch (Exception e) 
	        {
	            e.printStackTrace();
	        }
		}
		
		return doc;
    }
	
	private int ProcessDataAndSync( int seqNum,  String name ) throws Exception
	{
		try
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
									
			SyncMarker marker = null;
			
			if( this.syncReader != null )
			{
				marker = this.syncReader.getSyncMarker();
			}
			
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
	
						if( !this.DATA.isInterleave() )
						{
							dat = ConvertTo.Interleaved( dat, this.DATA.getNumberOfChannels(), this.DATA.getChunckSize() );
						}
	
						Number[] Data = new Number[ dat.length + this.DATA.getChunckSize() ];
	
						int index = 0;
						int cc = 0;
						for( int i = 0; i < dat.length; i++ )
						{
							Data[ index ] = dat[ i ];
	
							index++;
							cc++;
							if( cc >= this.DATA.getNumberOfChannels() )
							{
								Data[ index ] = (Number)NonSyncMarker;
	
								index++;
								cc = 0;
							}
						}
	
						index = 0;
						while( timeData != null && index < timeData.length && marker != null)
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
	
						while( dataBuffer.size() >= this.maxNumElements )
						{					
							seqNum = this.SaveDataBuffer( seqNum, dataBuffer, this.DATA.getDataType(), this.DATA.getNumberOfChannels() + 1, name );
						}
					}				
	
				}
	
				Loop = ( block != null );
			}

			while( dataBuffer.size() > 0 )
			{				
				seqNum = this.SaveDataBuffer( seqNum, dataBuffer, this.DATA.getDataType(), this.DATA.getNumberOfChannels() + 1, name );
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw e;
		}
		
		return seqNum;
	}
	
	private int SaveDataBuffer( int seqNum, List< Object > dataBuffer, int dataType, int Nchannels, String name ) throws Exception
	{
		int from = 0;
		int to = this.maxNumElements;
		
		if( to > dataBuffer.size() )
		{
			to = dataBuffer.size();
		}
			
		DataBlock dataBlock = DataBlockFactory.getDataBlock( dataType, seqNum, name, Nchannels, dataBuffer.subList( from, to ).toArray() );
				
		/*
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
		*/
		
		synchronized ( this )
		{
			this.totalSampleByChannels += ( to - from ) / Nchannels;
			
			while( !this.writer.saveData( dataBlock ) )
			{			
				try 
				{
					super.wait( 1000L );
				}
				catch (Exception e) 
				{
				}
			}
		}
		
		/*
		this.antideadlockCounter.incrementAndGet();
		
		Thread t = new Thread()
		{
			@Override
			public synchronized void run() 
			{
				try 
				{	
					writer.saveData( dataBlock );
				}
				catch (Exception e) 
				{
					runExceptionManager( e );
				}
				finally 
				{
					antideadlockCounter.decrementAndGet();
				}
			}
		};
		
		t.setName( "Antideadlock-writer" );
		t.start();
		*/
		
		 dataBuffer.subList( from, to ).clear();
		
		seqNum++;
		
		if( this.notifTask != null )
		{
			EventInfo ev = new EventInfo( this.getID(), EventType.SAVING_DATA_PROGRESS, (int)( ( 100.0D * seqNum ) / totalBlock ) );
			
			this.notifTask.addEvent( ev );
			synchronized ( this.notifTask )
			{
				this.notifTask.notify();
			}			
		}
				
		return seqNum;
	}
		
	private Tuple< Number[], Number[] > getNextNumberBlock( TemporalBinData temp ) throws Exception
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
	
	private int ProcessTimeStream( TemporalBinData stream, int dataType, int seqNum, String name ) throws Exception
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
						
					while( dataBuffer.size() >= this.maxNumElements )
					{
						
						seqNum = this.SaveDataBuffer( seqNum, dataBuffer, dataType, 1, name );
					}
				}
			}
			while( block != null );		
			
			while( dataBuffer.size() > 0 )
			{
				seqNum = this.SaveDataBuffer( seqNum, dataBuffer, dataType, 1, name);
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

		super.stopThread = true;
		
		//this.writer.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
		this.writer.close();
		
		synchronized ( this )
		{
			while( !this.writer.finished() )
			{
				if( this.writer instanceof Thread )
				{
					if( ((Thread)this.writer).getState().equals( Thread.State.TIMED_WAITING ) 
							|| ((Thread)this.writer).getState().equals( Thread.State.WAITING ) )
					{
						synchronized ( this.writer ) 
						{
							this.writer.notify();
						} 
					}
				}
				
				try
				{
					super.wait( 1000L );					
				}
				catch ( InterruptedException e) 
				{
				}
				finally 
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
	protected void runExceptionManager( Throwable e )
	{
		if (!(e instanceof InterruptedException))
		{
			/*
			if( this.monitor != null )
			{
				EventInfo event = new EventInfo( this.getID(), EventType.PROBLEM, new IOException("Problem: it is not possible to write in the file " + this.writer.getFileName() + "\n" + e.getClass()));
				
				this.events.add( event );
				try 
				{
					this.monitor.taskDone( this );
				} 
				catch (Exception e1) 
				{	
				}
			}
			*/
			
			super.stopThread = true;
						
			if( this.notifTask != null )
			{	
				String fileName = "";
				
				if( this.writer != null )
				{
					fileName = this.writer.getFileName();
				}
				
				String cl = "";
				if( e != null )
				{
					cl = e.getClass().getName();
					
					for( StackTraceElement track : e.getStackTrace() )
					{
						cl += "\n\t" + track.toString();
					}
				}
				
				EventInfo event = new EventInfo( this.getID(), EventType.PROBLEM, new Exception("Problem: it is not possible to write in the file " + fileName + "\n" + cl));				
				this.notifTask.addEvent( event );
				
				synchronized ( this.notifTask )
				{
					this.notifTask.notify();
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
		while( this.antideadlockCounter.get() > 0 )
		{
			try 
			{
				super.wait( 1000L );
			}
			catch (Exception e) 
			{
			}
		}
		
		if( !this.writer.finished() )
		{
			this.writer.close();
			
			if( this.writer instanceof IStoppableThread )
			{
				((IStoppableThread)this.writer).stopThread( IStoppableThread.FORCE_STOP );
			}
		}
				
		this.writer = null;
			
		this.DATA.closeTempBinaryFile();
		
		if( this.syncReader != null )
		{
			this.syncReader.closeStream();
		}
		
		//this.WriterloopEndInteractionNotifier.stopThread( IStoppableThread.FORCE_STOP );
		//this.WriterloopEndInteractionNotifier = null;
			
		/*
		if( this.monitor != null )
		{		
			Tuple< String, SyncMarkerBinFileReader > t = new Tuple<String, SyncMarkerBinFileReader>( DATA.getStreamingName(), this.syncReader );
			
			EventInfo event = new EventInfo( this.getID(), EventType.OUTPUT_DATA_FILE_SAVED, t);
			this.events.add( event );
		
			this.monitor.taskDone( this );
		}
		*/
		
		if( this.notifTask != null )
		{		
			Tuple< String, SyncMarkerBinFileReader > t = new Tuple<String, SyncMarkerBinFileReader>( DATA.getStreamingName(), this.syncReader );
			
			EventInfo event = new EventInfo( this.getID(), EventType.OUTPUT_DATA_FILE_SAVED, t);
			this.notifTask.addEvent( event );
			this.notifTask.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
			synchronized ( this.notifTask )
			{
				this.notifTask.notify();
			}
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
	 * @see Auxiliar.Tasks.IMonitoredTask#taskMonitor(Auxiliar.Tasks.ITaskMonitor)
	 */
	@Override
	public void taskMonitor( ITaskMonitor m )
	{
		if( super.getState().equals( Thread.State.NEW ) && this.notifTask == null )
		{
			synchronized ( this )
			{
				this.monitor = m;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see Auxiliar.Tasks.INotificationTask#getResult()
	 */
	/*
	@Override
	public synchronized List<EventInfo> getResult( boolean clear ) 
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
	*/

	/*
	 * (non-Javadoc)
	 * @see Auxiliar.Tasks.INotificationTask#clearResult()
	 */
	/*
	@Override
	public synchronized void clearResult() 
	{
		synchronized ( this.events )
		{
			this.events.clear();
		}		
	}
	*/
		
	@Override
	public String getID() 
	{
		return super.getName();
	}
	
	@Override
	public void taskDone(INotificationTask task) throws Exception 
	{
		List< EventInfo > EVENTS =  new ArrayList< EventInfo>( task.getResult( true ) );
		
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
