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

package lslrec.dataStream.outputDataFile;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


import lslrec.auxiliar.extra.ConvertTo;
import lslrec.auxiliar.extra.Tuple;
import lslrec.auxiliar.tasks.IMonitoredTask;
import lslrec.auxiliar.tasks.INotificationTask;
import lslrec.auxiliar.tasks.ITaskIdentity;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.auxiliar.tasks.NotificationTask;
import lslrec.controls.messages.EventInfo;
import lslrec.controls.messages.EventType;
import lslrec.dataStream.binary.reader.TemporalBinData;
import lslrec.dataStream.outputDataFile.dataBlock.ByteBlock;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlock;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlockFactory;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.dataStream.outputDataFile.format.IOutputDataFileWriter;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.setting.DataStreamSetting;
import lslrec.dataStream.setting.MutableDataStreamSetting;
import lslrec.dataStream.sync.SyncMarker;
import lslrec.dataStream.sync.SyncMarkerBinFileReader;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;
import lslrec.config.ConfigApp;
import lslrec.edu.ucsd.sccn.LSLUtils;

/**
 * 
 * @author Manuel Merino Monge
 *
 */

public class OutputBinaryFileSegmentation extends AbstractStoppableThread implements ITaskMonitor, ITaskIdentity, IMonitoredTask
{
	private int BLOCK_SIZE = ConfigApp.DEFAULT_SEGMENTATION_BLOCK_SIZE; 
	private int maxNumElements = BLOCK_SIZE / Float.BYTES; // 10 MB
	
	private final String prefixData = "data_";
	private final String prefixStringLen = "length_";
	private final String prefixTime = "time_";
	private final String prefixDeviceInfo = "deviceInfo_";
	
	private TemporalBinData DATA;
	private SyncMarkerBinFileReader syncReader;
	private OutputFileFormatParameters outputFormat;
	
	private IOutputDataFileWriter writer;
	private ITaskMonitor monitor;
	
	private NotificationTask notifTask = null;
	
	private AtomicInteger antideadlockCounter = new AtomicInteger( 0 );
	
	//private List< EventInfo > events;
	
	private long totalReadedBlock = 0;
	
	private double totalSampleByChannels = 0;
		
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
			throw new IllegalArgumentException( "Data input and/or OutputFileFormatParameters are null." );
		}
		
		this.outputFormat = DAT.getOutputFileFormat();
		
		super.setName( this.getClass().getSimpleName() + "-" + this.outputFormat.getParameter( OutputFileFormatParameters.OUT_FILE_NAME ).getValue() );
		
		this.syncReader = syncReader;
		
		this.DATA = DAT;
		
				
		this.BLOCK_SIZE = ConfigApp.DEFAULT_SEGMENTATION_BLOCK_SIZE;
		
		if( bufLen <= 0 )
		{						
			bufLen = (byte)10;
		}		
		
		this.BLOCK_SIZE = (int)( bufLen * Math.pow( 2, 20 ) );		
		
		this.setMaxNumElements( this.DATA.getTypeDataBytes(), this.DATA.getDataStreamSetting().getStreamInfo().channel_count() + 1 );		
	}
	
	private void setMaxNumElements( int dataTypeBytes, int channels )
	{			
		this.maxNumElements = this.BLOCK_SIZE / dataTypeBytes;
		
		this.maxNumElements = (int)( ( Math.floor( 1.0D * this.maxNumElements / channels ) ) * channels );
		
		if( this.maxNumElements < this.DATA.getDataStreamSetting().getStreamInfo().channel_count() )
		{
			this.maxNumElements = this.DATA.getDataStreamSetting().getStreamInfo().channel_count();
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
			this.outputFormat.setParameter( OutputFileFormatParameters.BLOCK_DATA_SIZE, this.BLOCK_SIZE );
			String outFormat = (String)this.outputFormat.getParameter( OutputFileFormatParameters.OUT_FILE_FORMAT ).getValue();			
			
			// Header size stimation
			long binFileSizeLen = this.DATA.getDataBinaryFileSize();
			binFileSizeLen = (long) Math.ceil( 1.0D * binFileSizeLen / this.BLOCK_SIZE );
			
			long syncBlockLen = 0;
			
			if( this.syncReader != null )
			{
				syncBlockLen = this.syncReader.getFileSize();
				syncBlockLen = (long)Math.ceil(  1.0D * syncBlockLen / this.BLOCK_SIZE );
			}
	
			this.outputFormat.setParameter( OutputFileFormatParameters.NUM_BLOCKS, syncBlockLen + binFileSizeLen  );
						
			String varNames = "";
			String streamName = this.DATA.getDataStreamSetting().getStreamName();
			
			varNames += this.prefixData + streamName + ";";
			varNames += this.prefixDeviceInfo + streamName + ";";
			varNames += this.prefixStringLen + streamName + ";";
			varNames += this.prefixTime + streamName + ";";			
			
			this.outputFormat.setParameter( OutputFileFormatParameters.DATA_NAMES, varNames ); // CLIS: To calculate padding header 
						
			IOutputDataFileWriter wr = DataFileFormat.getDataFileEncoder( outFormat ).getWriter( this.outputFormat, this.DATA.getDataStreamSetting(), this );
					
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
			//int dataType = this.DATA.getDataStreamSetting().getDataType(); // LSL type data
			DataStreamSetting streamSettings = this.DATA.getDataStreamSetting();
			int nChannel = streamSettings.getStreamInfo().channel_count(); // number of channels
			String lslName = streamSettings.getStreamName(); // LSL streaming name
			String lslXML = streamSettings.getStreamInfo().as_xml(); // LSL description

			Map< String, String > addInfo = (Map< String, String >)this.outputFormat.getParameter( OutputFileFormatParameters.RECORDING_INFO ).getValue();
			if( addInfo != null )
			{
				for( String id : addInfo.keySet() )
				{
					lslXML = this.addElementToXml( lslXML, LSLUtils.getAdditionalInformationLabelInXml(), id, addInfo.get( id ) );
				}
			}
			
			String variableName = this.prefixData; // data variable name
			String timeVarName = this.prefixTime; // time variable name
			String info = this.prefixDeviceInfo; // LSL description variable name
			
			int counterDataBlock = 0;
									
			// Save data
			String varName = variableName + lslName;
			
			this.setMaxNumElements( LSLUtils.getDataTypeBytes( streamSettings.getDataType() ), nChannel + 1 );
			
			if( streamSettings.getDataType() != LSLUtils.string )
			{			
				counterDataBlock = this.ProcessDataAndSync( counterDataBlock, varName );			
			}
			else
			{
				Number NonSyncMarker = ConvertTo.NumberTo( SyncMarker.NON_MARK, streamSettings.getStringLegthType() );
				
				this.setMaxNumElements( LSLUtils.getDataTypeBytes( streamSettings.getDataType() ), 1 );
				counterDataBlock = this.ProcessStringDataAndSync( counterDataBlock, varName, NonSyncMarker, true );
				
				this.DATA.reset();
				
				this.setMaxNumElements( LSLUtils.getDataTypeBytes( streamSettings.getStringLegthType() ), nChannel + 1 );
				varName = this.prefixStringLen + lslName;
				counterDataBlock = this.ProcessStringDataAndSync( counterDataBlock, varName, NonSyncMarker, false );				
			}
			
			// Save time stamps			
			String timeName = timeVarName + lslName;
			 
			
			this.DATA.reset();
			this.setMaxNumElements( LSLUtils.getDataTypeBytes( streamSettings.getTimeDataType() ), 1 );
			counterDataBlock = this.ProcessTimeStream(  this.DATA, streamSettings.getTimeDataType(), counterDataBlock, timeName );
			
			// Header info
			
			
			
			lslXML = this.addElementToXml( lslXML, LSLUtils.getAdditionalInformationLabelInXml()
													, MutableDataStreamSetting.ID_RECORDED_SAMPLES_BY_CHANNELS
													, "" + ( this.totalSampleByChannels / ( nChannel + 2 ) ) ); // nChannel + 2: channels + marker column + time ;			
			
			this.writer.addMetadata( info + lslName, lslXML ); // output file header
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
	
	private String addElementToXml( String xml, String nodeRoot, String name, String value )
	{			
		if( xml != null && nodeRoot != null && name != null )
		{
			Document doc = ConvertTo.xmlStringToXMLDocument( xml );

			if( doc != null )
			{
				NodeList nodes = doc.getElementsByTagName( nodeRoot );
				
				if( nodes.getLength() > 0 )
				{
					try 
				    {
						Element newElement = doc.createElement( name );
						newElement.appendChild( doc.createTextNode( value ) );
						
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
	
	private int ProcessDataAndSync( int seqNum,  String name ) throws Exception
	{
		try
		{
			List< Object > dataBuffer = new ArrayList< Object >();
			
			/*
			Object NonSyncMarker = SyncMarker.NON_MARK;
			
			if( this.DATA.getDataStreamSetting().getDataType() == LSLUtils.string )
			{
				NonSyncMarker = "" + SyncMarker.NON_MARK;
			}
			else
			{
				 NonSyncMarker = ConvertTo.NumberTo( SyncMarker.NON_MARK, this.DATA.getDataStreamSetting().getDataType() );
			}
			*/
			
			Number NonSyncMarker = ConvertTo.NumberTo( SyncMarker.NON_MARK, this.DATA.getDataStreamSetting().getDataType() );
									
			SyncMarker marker = null;
			
			if( this.syncReader != null )
			{
				marker = this.syncReader.getSyncMarker();
			}
			
			boolean Loop = true;
			
			while( Loop )
			{			
				Tuple< Number[], Number[] > block = null;
	
				if( this.DATA.getDataStreamSetting().getDataType() != LSLUtils.string 
						&& this.DATA.getDataStreamSetting().getDataType() != LSLUtils.undefined 
						)
				{
					block = this.getNextNumberBlock( this.DATA );
	
					if( block != null )
					{
						Number[] dat = block.x;
						Number[] timeData = block.y;
	
						if( !this.DATA.getDataStreamSetting().isInterleavedData() )
						{
							dat = ConvertTo.Interleaved( dat, this.DATA.getDataStreamSetting().getStreamInfo().channel_count(), this.DATA.getDataStreamSetting().getChunkSize() );
						}
	
						Number[] Data = new Number[ dat.length + this.DATA.getDataStreamSetting().getChunkSize() ];
	
						int index = 0;
						int cc = 0;
						for( int i = 0; i < dat.length; i++ )
						{
							Data[ index ] = dat[ i ];
	
							index++;
							cc++;
							if( cc >= this.DATA.getDataStreamSetting().getStreamInfo().channel_count() )
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
								Data[ ( index + 1 ) * ( this.DATA.getDataStreamSetting().getStreamInfo().channel_count() + 1 ) - 1] = ConvertTo.NumberTo( marker.getMarkValue(), this.DATA.getDataStreamSetting().getDataType() );
	
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
							seqNum = this.SaveDataBuffer( seqNum, dataBuffer, this.DATA.getDataStreamSetting().getDataType(), this.DATA.getDataStreamSetting().getStreamInfo().channel_count() + 1, name );
						}
					}				
	
				}
	
				Loop = ( block != null );
			}

			while( dataBuffer.size() > 0 )
			{				
				seqNum = this.SaveDataBuffer( seqNum, dataBuffer, this.DATA.getDataStreamSetting().getDataType(), this.DATA.getDataStreamSetting().getStreamInfo().channel_count() + 1, name );
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			throw e;
		}
		
		return seqNum;
	}
	
	private int ProcessStringDataAndSync( int seqNum,  String name, Number NonSyncMarker, boolean saveString ) throws Exception
	{
		try
		{
			List< Object > dataBuffer = new ArrayList< Object >();
						
			//Number NonSyncMarker = ConvertTo.NumberTo( SyncMarker.NON_MARK, this.DATA.getDataStreamSetting().getDataType() );
									
			SyncMarker marker = null;
			
			if( this.syncReader != null )
			{
				marker = this.syncReader.getSyncMarker();
			}
			
			boolean Loop = true;
			
			while( Loop )
			{	
				Tuple< String[], Tuple< Number[], Number[] > > block = null;
						
				if( this.DATA.getDataStreamSetting().getDataType() == LSLUtils.string )
				{
					block = this.getNextStringBlock( this.DATA );				
					
					if( block != null )
					{
						String[] str = block.x;						
						
						if( !saveString )
						{
							Tuple< Number[], Number[] > lenTime = block.y;

							Number[] dat = lenTime.x;
							Number[] timeData = lenTime.y;

							if( !this.DATA.getDataStreamSetting().isInterleavedData() )
							{
								dat = ConvertTo.Interleaved( dat, this.DATA.getDataStreamSetting().getStreamInfo().channel_count(), this.DATA.getDataStreamSetting().getDataType() );
							}

							Number[] Data = new Number[ dat.length + this.DATA.getDataStreamSetting().getDataType() ];

							int index = 0;
							int cc = 0;
							for( int i = 0; i < dat.length; i++ )
							{
								Data[ index ] = dat[ i ];

								index++;
								cc++;
								if( cc >= this.DATA.getDataStreamSetting().getStreamInfo().channel_count() )
								{
									Data[ index ] = NonSyncMarker;

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
									Number m = ConvertTo.NumberTo( marker.getMarkValue(), this.DATA.getDataStreamSetting().getStringLegthType() );
									Data[ ( index + 1 ) * ( this.DATA.getDataStreamSetting().getStreamInfo().channel_count() + 1 ) - 1] = m;

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
								if( datVal == null )
								{
									System.out.println("OutputBinaryFileSegmentation.ProcessStringDataAndSync()");
								}
								
								dataBuffer.add( datVal );
							}
	
							while( dataBuffer.size() >= this.maxNumElements )
							{					
								seqNum = this.SaveDataBuffer( seqNum, dataBuffer, this.DATA.getDataStreamSetting().getStringLegthType(), this.DATA.getDataStreamSetting().getStreamInfo().channel_count() + 1, name );
							}
						}
						else if( str != null )
						{
							Object[] dat = str;
							
							if( !this.DATA.getDataStreamSetting().isInterleavedData() )
							{
								dat = ConvertTo.Interleaved( dat, this.DATA.getDataStreamSetting().getStreamInfo().channel_count(), this.DATA.getDataStreamSetting().getDataType() );
							}
							
							for( Object d : dat )
							{
								for( Byte b : d.toString().getBytes() )
								{
									dataBuffer.add( b );
								}
							}
							
							while( dataBuffer.size() >= this.maxNumElements )
							{					
								seqNum = this.SaveDataBuffer( seqNum, dataBuffer, LSLUtils.int8, 1, name );
							}
						}						
					}					
				}
				
				Loop = ( block != null );
			}

			while( dataBuffer.size() > 0 )
			{	
				if( !saveString )
				{
					seqNum = this.SaveDataBuffer( seqNum, dataBuffer, this.DATA.getDataStreamSetting().getStringLegthType(), this.DATA.getDataStreamSetting().getStreamInfo().channel_count() + 1, name );
				}
				else
				{
					seqNum = this.SaveDataBuffer( seqNum, dataBuffer, LSLUtils.int8, 1, name );
				}
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
		
		this.totalReadedBlock += ( to - from ) * LSLUtils.getDataTypeBytes( dataType );
		
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
			//EventInfo ev = new EventInfo( this.getID(), EventType.SAVING_DATA_PROGRESS, (int)( ( 100.0D * seqNum ) / totalBlock ) );
			
			EventInfo ev = new EventInfo( this.getID(), EventType.SAVING_DATA_PROGRESS, (int)( ( 100.0D * this.totalReadedBlock) / this.DATA.getDataBinaryFileSize() ) );
			
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
						data = ConvertTo.ByteArrayTo( bytes.getData(), temp.getDataStreamSetting().getDataType() );
					}
					else if( index == 1 )
					{
						time = ConvertTo.ByteArrayTo( bytes.getData(), temp.getDataStreamSetting().getTimeDataType() );
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
	
	private Tuple< String[], Tuple< Number[], Number[] > > getNextStringBlock( TemporalBinData temp ) throws Exception
	{
		Tuple< String[], Tuple< Number[], Number[] > > out = null;
		
		if( temp != null )
		{
			List< ByteBlock > block = temp.getDataBlocks();
			
			if( block != null )
			{				
				Number[] lens = null;
				String strs = null;
				String[] strData = null;
				Number[] time = null; 
				
				for( int index = 0; index < block.size(); index++ )
				{
					ByteBlock bytes = block.get( index );
					
					if( index == 0 )
					{
						lens = ConvertTo.ByteArrayTo( bytes.getData(), temp.getDataStreamSetting().getStringLegthType() );
					}
					else if( index == 1 )
					{
						strs = new String( ConvertTo.ByterArray2byteArray( bytes.getData() ) );
					}						
					else if( index == 2 )
					{
						time = ConvertTo.ByteArrayTo( bytes.getData(), temp.getDataStreamSetting().getTimeDataType() );
					}
				}
				
				if( lens != null && strs != null )
				{
					strData = new String[ lens.length ];
					int init = 0;
					
					for( int iL = 0; iL < lens.length && init < strs.length(); iL++ )
					{
						Number l = lens[ iL ];
						
						int end = init + l.intValue();
						strData[ iL ] = strs.substring( init, end );
						
						init = end;
					}
				}
				
				if( lens != null || time != null || strData != null )
				{
					out = new Tuple< String[], Tuple<Number[], Number[] > >( strData, new Tuple< Number[], Number[] >( lens, time ) );
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
			
			Number[] times = null; 
			
			do
			{
				times = null;
						
				if( stream.getDataStreamSetting().getDataType()!= LSLUtils.string 
						&& stream.getDataStreamSetting().getDataType() != LSLUtils.undefined 
					)
				{
					Tuple< Number[], Number[] > block = this.getNextNumberBlock( stream );
					
					if( block != null )
					{
						times = block.y;
						
						if( times != null )
						{
							for( Number t : times )
							{
								dataBuffer.add( t );
							}
						}
					}
				}
				else if( stream.getDataStreamSetting().getDataType() == LSLUtils.string )
				{
					Tuple< String[], Tuple< Number[], Number[] > > block = this.getNextStringBlock( stream );
					
					if( block != null )
					{
						times = block.y.y;
						
						if( times != null )
						{
							for( Number t : times )
							{
								dataBuffer.add( t );
							}
						}
					}
				}
				
				if( times != null )
				{
					while( dataBuffer.size() >= this.maxNumElements )
					{					
						seqNum = this.SaveDataBuffer( seqNum, dataBuffer, dataType, 1, name );
					}
				}
			}
			while( times != null );		
			
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
			Tuple< String, SyncMarkerBinFileReader > t = new Tuple<String, SyncMarkerBinFileReader>( DATA.getDataStreamSetting().getStreamName(), this.syncReader );
			
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
