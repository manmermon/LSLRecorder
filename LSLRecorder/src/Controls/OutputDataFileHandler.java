/*
 * Work based on outputDataFileControl class of 
 * CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
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

package Controls;

import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskMonitor;
import Config.Parameter;
import Config.ParameterList;
import Controls.Messages.EventInfo;
import Controls.Messages.eventType;
import InputStreamReader.TemporalData;
import InputStreamReader.Binary.TemporalOutputDataFile;
import InputStreamReader.OutputDataFile.OutputBinaryFileSegmentation;
import InputStreamReader.OutputDataFile.Compress.OutputZipDataFactory;
import InputStreamReader.OutputDataFile.Format.Clis.OutputCLISDataWriter;
import InputStreamReader.OutputDataFile.Format.DataFileFormat;
import InputStreamReader.OutputDataFile.Format.OutputFileFormatParameters;
import InputStreamReader.OutputDataFile.Format.OutputFileWriterTemplate;
import InputStreamReader.Sync.InputSyncData;
import InputStreamReader.WritingSystemTester.WritingTest;
import Auxiliar.WarningMessage;
import Auxiliar.Extra.Tuple;
import StoppableThread.IStoppableThread;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLConfigParameters;

import java.io.File;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class OutputDataFileHandler extends HandlerMinionTemplate implements ITaskMonitor
{	
	public static final String ID = "OutputDataHandler";
	
	public static final String PARAMETER_FILE_FORMAT = "format";
	public static final String PARAMETER_SET_MARK = "mark";
	public static final String PARAMETER_FILE_PATH = "filePath";
	public static final String PARAMETER_LSL_SETTING = "LSL settings";
	public static final String PARAMETER_START_SYNC = "Start sync";
	public static final String PARAMETER_WRITE_TEST = "Writing test";
	
	private static OutputDataFileHandler ctr = null;

	private List< TemporalOutputDataFile > temps;
	private List< InputSyncData > syncInputData;
	
	private boolean inputSyncLauched = false;
	private boolean isRecorderThreadOn = false;
	
	private String fileFormat = DataFileFormat.CLIS;
	
	//private List<LSL.StreamInfo> streamInfos = null;

	private Map<String, OutputBinaryFileSegmentation> outWriterHandlers = null;
	private AtomicInteger NumberOfSavingThreads = new AtomicInteger( 0 );
	private AtomicInteger NumberOfTestThreads = new AtomicInteger( 0 );
	private AtomicBoolean isRunBinData = new AtomicBoolean( false ); 
	
	//private boolean isSyncThreadActive = false;
	
	/**
	 * Private constructor.
	 */
	private OutputDataFileHandler()
	{
		this.temps = new ArrayList< TemporalOutputDataFile >();
		this.syncInputData = new ArrayList< InputSyncData >();
		//this.streamInfos = new ArrayList< LSL.StreamInfo >();
		this.outWriterHandlers = new HashMap< String, OutputBinaryFileSegmentation >();
		
		super.setName( "OutputDataFileHandler" );
	}

	/**
	 * Singleton class.
	 * 
	 * @return Instance of SocketHandler class
	 */
	public static OutputDataFileHandler getInstance()
	{
		if (ctr == null)
		{
			ctr = new OutputDataFileHandler();
		}

		return ctr;
	}

	/*
	 * (non-Javadoc)
	 * @see Controls.HandlerMinionTemplate#cleanUpSubordinates()
	 */
	@Override
	protected void cleanUpSubordinates()
	{
		synchronized ( this.temps )
		{	
			//this.NumberOfSavingThreads += this.temps.size(); 
			this.NumberOfSavingThreads.addAndGet( this.temps.size() );
			this.isRunBinData.set( true );
			
			for ( TemporalOutputDataFile temp : this.temps )
			{
				temp.setOutputFileFormat( this.fileFormat );
				
				if( !temp.getState().equals( Thread.State.NEW ) )
				{					
					temp.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
				}
				else
				{
					//this.NumberOfSavingThreads--;
					this.NumberOfSavingThreads.decrementAndGet();
				}
			}
			
			if( this.NumberOfSavingThreads.get() < 1 )
			{
				this.isRunBinData.set( false );
			}
			
			this.temps.clear();
		}
		
		synchronized ( this.syncInputData )
		{
			for( InputSyncData sync : this.syncInputData )
			{
				sync.stopThread( IStoppableThread.FORCE_STOP );
			}
			
			this.syncInputData.clear();
		}
		
		this.inputSyncLauched = false;
		this.isRecorderThreadOn = false;
		//this.isSyncThreadActive = false;
	}

	/*
	 * (non-Javadoc)
	 * @see Controls.HandlerMinionTemplate#startWork(java.lang.Object)
	 */
	@Override
	protected void startWork( Object info ) throws Exception
	{
		final OutputDataFileHandler auxOutDataFileCtrl = this;
		
		if (this.temps != null)
		{
			synchronized ( this.temps )
			{
				Tuple t = (Tuple)info;
				
				if( t.x.equals( PARAMETER_START_SYNC ) )
				{
					this.startSyncThread( t.x.toString() );
				}
				else if ( t.x.toString().equals( PARAMETER_FILE_FORMAT ) )
				{
					String format = t.y.toString();
					
					if ( !DataFileFormat.isSupportedFileFormat( format ) )
					{
						throw new IllegalArgumentException( "Unsupport file format." );
					}

					this.fileFormat = format;
					
					this.startSyncThread( t.x.toString() );
					
					if( !this.isRecorderThreadOn )
					{
						this.isRecorderThreadOn = true;	
							
						for( final TemporalOutputDataFile temp : this.temps )
						{						
							temp.taskMonitor( this );
	
							Thread tLauch = new Thread()
							{	
								public void run()
								{
									try
									{
										temp.startThread();										
									}
									catch (Exception e)
									{
										EventInfo event = new EventInfo( eventType.PROBLEM, e);
										supervisor.eventNotification( auxOutDataFileCtrl, event );
									}
								}
							};
							
							tLauch.start();
						
						}
					}
				}
				else if ( t.x.toString().equals( PARAMETER_SET_MARK ) )
				{					
					for( final TemporalOutputDataFile temp : this.temps )
					{	
						temp.addMark( (Integer)t.y );
					}
				}
			}
		}
	}
	
	private void startSyncThread( String origin )
	{
		if( this.syncInputData != null  && !this.inputSyncLauched )
		{
			this.inputSyncLauched = true;
			
			final OutputDataFileHandler auxOutDataFileCtrl = this;
			
			synchronized ( this.syncInputData )
			{
				for( InputSyncData sync : this.syncInputData )
				{	
					sync.taskMonitor( this );
					
					Thread tLauch = new Thread()
					{
						public void run()
						{
							try
							{
								sync.startThread();
							}
							catch (Exception e)
							{
								EventInfo event = new EventInfo( eventType.PROBLEM, e );
								supervisor.eventNotification( auxOutDataFileCtrl, event );
							}
						}
					};
					
					tLauch.start();					
				}				
				
				//this.isSyncThreadActive = origin.equals( PARAMETER_START_SYNC ) && ( this.syncInputData.size() > 0 );
			}			
		}
	}

	/*
	 * (non-Javadoc)
	 * @see Controls.HandlerMinionTemplate#createSubordinates(Controls.MinionParameters)
	 */
	@Override
	protected List< IStoppableThread > createSubordinates( MinionParameters minionPars ) throws Exception 
	{
		List<IStoppableThread> list = new ArrayList< IStoppableThread >();
		if ( minionPars != null )
		{
			//
			//
			// Clear control list
			//
			
			//this.streamInfos.clear();
			this.temps.clear();
			this.syncInputData.clear();

			//
			//
			// Get settings
			//
			
			ParameterList parameters = minionPars.getMinionParameters( ID );
			
			Parameter filePar = parameters.getParameter( PARAMETER_FILE_PATH );			
			String filePath = (String)filePar.getValue();
			
			Parameter lslSetting = parameters.getParameter( PARAMETER_LSL_SETTING );
			HashSet< LSLConfigParameters > lslCFGs = ( HashSet< LSLConfigParameters > )lslSetting.getValue();
			
			Parameter writingTest = parameters.getParameter( PARAMETER_WRITE_TEST );
			boolean test = false;
			if( writingTest != null ) 
			{
				test = (boolean)writingTest.getValue();
			}					

			//
			//
			// Check LSL streamings
			//
			
			LSL.StreamInfo[] results = LSL.resolve_streams();

			// To check alive stream
			List< Tuple< LSL.StreamInfo, LSLConfigParameters > > inlets = new ArrayList< Tuple< LSL.StreamInfo, LSLConfigParameters > >();
			
			// chunck size of each stream
			List< Integer > inLetsChunckSizes = new ArrayList< Integer >();

			// check selected stream
			for( int i = 0; i < results.length; i++ )
			{
				LSL.StreamInfo info = results[ i ];
				boolean found = false;
				
				Iterator< LSLConfigParameters > itLSLcfg = lslCFGs.iterator();

				while ( ( itLSLcfg.hasNext() ) && ( !found ) )
				{
					LSLConfigParameters t = itLSLcfg.next();
					if( t.isSelected() || t.isSynchronationStream() )
					{
						found = info.uid().equals( t.getUID() );
	
						if ( found )
						{								
							// copy extra information
							info.desc().append_child_value( t.getExtraInfoLabel(), t.getAdditionalInfo() );
							
							// Save selected and alive stream
							Tuple< LSL.StreamInfo, LSLConfigParameters > tLSL = new Tuple<LSL.StreamInfo, LSLConfigParameters>( info , t );
							inlets.add( tLSL );
							
							// Save chunck size
							inLetsChunckSizes.add( t.getChunckSize() );
						}						
					}					
				}
			}

			// Save selected LSL stream
			if ( !inlets.isEmpty() )
			{
				for ( int indexInlets = 0; indexInlets < inlets.size(); indexInlets++)
				{
					Tuple< LSL.StreamInfo, LSLConfigParameters > t = inlets.get( indexInlets );
					
					LSL.StreamInfo inletInfo = t.x;
					//this.streamInfos.add( inletInfo );
					
					if( t.y.isSynchronationStream() )
					{
						InputSyncData sync = new InputSyncData( inletInfo, t.y );
						this.syncInputData.add( sync );
					}
					else
					{	
						TemporalOutputDataFile temp;
						
						if( !test )
						{
							temp = new TemporalOutputDataFile( filePath,  inletInfo, t.y, indexInlets );
						}
						else
						{
							temp = new WritingTest( filePath,  inletInfo, t.y, indexInlets );
						}

						this.temps.add( temp );
					}
				}
			}

			list.addAll( this.temps );
		}

		return list;
	}

	/*
	 * (non-Javadoc)
	 * @see Auxiliar.Tasks.ITaskMonitor#taskDone(Auxiliar.Tasks.INotificationTask)
	 */
	@Override
	public void taskDone( INotificationTask task ) throws Exception
	{
		if (task != null)
		{
			List<EventInfo> events = task.getResult();
			
			synchronized ( events )
			{
				for (EventInfo event : events)
				{
					if ( event.getEventType().equals( eventType.PROBLEM ) )
					{
						this.supervisor.eventNotification( this, event );
					}
					else if( event.getEventType().equals( eventType.OUTPUT_DATA_FILE_SAVED ) )
					{	
						synchronized ( this.outWriterHandlers ) 
						{
							//this.NumberOfSavingThreads--;
							
							this.outWriterHandlers.remove( event.getEventInformation() );

							//if( this.NumberOfSavingThreads < 1 )
							if( this.NumberOfSavingThreads.decrementAndGet() < 1 )
							{
								this.supervisor.eventNotification( this, new EventInfo( eventType.ALL_OUTPUT_DATA_FILES_SAVED, event.getEventInformation() )  );
							}
							
							if( this.NumberOfSavingThreads.intValue() < 0 )
							{
								this.NumberOfSavingThreads.set( 0 );
							}
						}										
					}
					else if ( event.getEventType().equals( eventType.INPUT_MARK_READY ) )
					{					
						this.supervisor.eventNotification( this, new EventInfo( event.getEventType(),  event.getEventInformation() ) );
						
						this.startWorking( new Tuple( PARAMETER_SET_MARK, event.getEventInformation() ) );
					}
					else if ( event.getEventType().equals( eventType.TEST_WRITE_TIME ) )
					{		
						final IHandlerMinion hand = this;
						final long time = this.NumberOfTestThreads.incrementAndGet() * 10;
						synchronized ( this.outWriterHandlers )
						{							
							Thread t = new Thread()
							{
								@Override
								public synchronized void run() 
								{
									System.out.println("OutputDataFileHandler.taskDone(...).new Thread() {...}.run() " + ((Tuple)event.getEventInformation()).x + " " + time );
									
									try 
									{
										Thread.sleep( time );
									} 
									catch (InterruptedException e) 
									{
									}
									
									supervisor.eventNotification( hand , new EventInfo( eventType.TEST_WRITE_TIME, event.getEventInformation() ) );
								}
							};
							
							t.start();
						}
					}
					else if ( event.getEventType().equals( eventType.TEST_OUTPUT_TEMPORAL_FILE ) )
					{			
						synchronized ( this.outWriterHandlers )
						{
							this.outWriterHandlers.remove( event.getEventInformation() );
														
							if( this.NumberOfSavingThreads.decrementAndGet() < 1 )
							{
								this.supervisor.eventNotification( this, new EventInfo( eventType.ALL_OUTPUT_DATA_FILES_SAVED, event.getEventInformation() )  );
							}
							
							if( this.NumberOfSavingThreads.intValue() < 0 )
							{
								this.NumberOfSavingThreads.set( 0 );
							}
							
							( (TemporalData)event.getEventInformation() ).closeTempBinaryFile();
						}
					}
					else if ( event.getEventType().equals( eventType.SAVED_OUTPUT_TEMPORAL_FILE ) )
					{			
						if( !this.isRunBinData.get() )
						{
							this.NumberOfSavingThreads.incrementAndGet();
						}
						
						//OutputCLISDataSequencialWriter saveOutFileThread = null;
						OutputBinaryFileSegmentation saveOutFileThread = null;

						try
						{
							TemporalData dat = (TemporalData)event.getEventInformation();

							Tuple< String, Boolean > res = checkOutputFileName( dat.getOutputFileName(), dat.getStreamingName() );

							//OutputDataFileWriter writer = DataFileFormat.getDataFileWriter( dat.getOutputFileFormat(), (String)res.x );
							long dataBlocks = dat.getDataBinaryFileSize();
							dataBlocks = (long) Math.ceil( 1.0D * dataBlocks / TemporalData.BLOCK_SIZE );

							long timeBlocks = dat.getTimeBinaryFileSize();
							timeBlocks = (long)Math.ceil(  1.0D * timeBlocks / TemporalData.BLOCK_SIZE );

							int blockSizeStrLen = Integer.toString( TemporalData.BLOCK_SIZE ).length() + 1;

							long headerSize = dataBlocks * blockSizeStrLen + timeBlocks * blockSizeStrLen;

							String xml = dat.getLslXml();						

							headerSize += xml.toCharArray().length;
							headerSize += ( dat.getStreamingName().length() + 10 ) * 4 ; // device info, binary and time data; 10 -> length of data type in string
							headerSize += Integer.toString( dat.getNumberOfChannels() ).length() * 2; // channel numbers 

							//headerSize *= Character.BYTES;
							//headerSize = headerSize * 2; 

							/*
							OutputCLISDataWriter writer = new OutputCLISDataWriter( (String)res.x
																					, headerSize 
																					, OutputZipDataFactory.createOuputZipStream( OutputZipDataFactory.GZIP )
																					, Charset.forName( "UTF-8" ) );
							*/
							
							String outFormat = dat.getOutputFileFormat();
							OutputFileFormatParameters pars = new OutputFileFormatParameters();
							pars.setCharset( Charset.forName( "UTF-8") );
							pars.setHeaderSize( headerSize );
							pars.setCompressType( DataFileFormat.getCompressTech( outFormat ) );
							
							OutputFileWriterTemplate writer = DataFileFormat.getDataFileWriter( outFormat, (String)res.x, pars );

							/*							
							saveOutFileThread = new OutputCLISDataSequencialWriter( dat, writer );
							saveOutFileThread.taskMonitor( this );
							 */
							
							saveOutFileThread = new OutputBinaryFileSegmentation( dat, writer );
							saveOutFileThread.taskMonitor( this );
							
							this.outWriterHandlers.put( dat.getStreamingName(), saveOutFileThread );

							saveOutFileThread.startThread();

							if (!((Boolean)res.y).booleanValue())
							{
								this.event = new EventInfo( eventType.WARNING, "The output data file exist. It was renamed as " + (String)res.x);
								this.supervisor.eventNotification( this, this.event );
							}
						}
						catch (Exception ex)
						{
							if( saveOutFileThread != null )
							{
								saveOutFileThread.stopThread( IStoppableThread.FORCE_STOP );
							}
							
							this.NumberOfSavingThreads.decrementAndGet();
							
							if( this.NumberOfSavingThreads.get() < 1 )
							{
								this.supervisor.eventNotification( this, new EventInfo( eventType.ALL_OUTPUT_DATA_FILES_SAVED, ((TemporalData)event.getEventInformation()).getStreamingName() )  );
							}

							this.event = new EventInfo( eventType.PROBLEM, "Save process error: " + ex.getMessage() );
							this.supervisor.eventNotification( this, event );
						}
					}
				}
			}
			
			task.clearResult();
		}
	}

	/*
	public void unparkOutputFile()
	{
		for( SaveOutputFileThread outFile : this.writers.values() )
		{
			System.out.println("OutputDataFileHandler.unparkOutputFile() " + outFile.getName() + " > " + outFile.getState() );
			if( outFile.getState().equals( Thread.State.WAITING ) )
			{
				LockSupport.unpark( outFile );
				synchronized (outFile)
				{
					outFile.notify();
				}
			}
		}
			
	}
	*/
	
	public List< State > getOutFileState()
	{
		List< State > states = new ArrayList< State >();
		
		for( OutputBinaryFileSegmentation outfile : this.outWriterHandlers.values() )
		{
			states.add( outfile.getState() );
		}
		
		return states;
	}
	
	/**
	 * Format output data file name.
	 * 
	 * @param FilePath -> absolute file path.
	 * @param sourceID -> LSL streaming name.
	 * 
	 * @return Join file name and LSL name. File extension is conserved. Example: 
	 * 		- file name "data.clis"
	 * 		- LSL name  "SerialPort"
	 * 	output is "data_SerialPort.clis"
	 */
	private synchronized Tuple< String, Boolean > checkOutputFileName( String FilePath, String sourceID )
	{		
		boolean ok = true;
		boolean cont = true;

		Calendar c = Calendar.getInstance();

		int index = FilePath.lastIndexOf(".");
		String name = FilePath;
		String ext = "";
		if (index > -1)
		{
			name = FilePath.substring(0, index);
			ext = FilePath.substring(index);
		}

		String aux2 = name + "_" + sourceID + ext;
		while ( cont )
		{
			File file = new File(aux2);

			if ( file.exists() )
			{
				ok = false;

				c.add( 13, 1 );
				String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format( c.getTime() );

				aux2 = name + "_" + sourceID + "_" + date + ext;
			}
			else
			{
				cont = false;
			}
		}
		
		Tuple< String, Boolean > res = new Tuple< String, Boolean>(aux2,  ok );
		
		try 
		{
			super.sleep( 1000L ); // To avoid problems if 2 or more input streaming are called equal.
		}
		catch (InterruptedException e) 
		{
		}

		return res;
	}

	/*
	 * (non-Javadoc)
	 * @see Controls.IHandlerMinion#checkParameters()
	 */
	public WarningMessage checkParameters()
	{
		return new WarningMessage();
	}

	//
	//
	//******************************************************************************
	//******************************************************************************
	//
	//
		
	/**
	 * 
	 * @return False if no SaveOutputFileThread instance is saving data, otherwise, True
	 */
	public boolean isSavingData()
	{			
		//return this.NumberOfSavingThreads > 0;
		return this.NumberOfSavingThreads.intValue() > 0;
	}

	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#preStopThread(int)
	 */
	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{	
	}

	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#postStopThread(int)
	 */
	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{	
	}

	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#runInLoop()
	 */
	@Override
	protected void runInLoop() throws Exception 
	{	
	}	
	
	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#preStart()
	 */
	@Override
	protected void preStart() throws Exception 
	{
		super.preStart();
		
		this.NumberOfTestThreads.set( 0 );
		
		super.stopThread = true;
	}
}
