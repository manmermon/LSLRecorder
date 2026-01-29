/*
 * Work based on outputDataFileControl class of 
 * CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018-2019 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.  https://github.com/manmermon/LSLRecorder
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

package lslrec.control.handler;

import lslrec.auxiliar.thread.LaunchThread;
import lslrec.auxiliar.thread.timer.ActionTimerThread;
import lslrec.auxiliar.thread.timer.IAction;
import lslrec.auxiliar.thread.timer.Timer;
import lslrec.dataStream.binary.input.writer.TemporalOutDataFileWriter;
import lslrec.dataStream.binary.reader.TemporalBinData;
import lslrec.dataStream.family.DataStreamFactory;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.outputDataFile.OutputBinaryFileSegmentation;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.sync.SyncMarker;
import lslrec.dataStream.sync.SyncMarkerBinFileReader;
import lslrec.dataStream.sync.SyncMarkerCollectorWriter;
import lslrec.dataStream.sync.dataStream.InputSyncData;
import lslrec.dataStream.writingSystemTester.WritingTest;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.auxiliar.extra.FileUtils;
import lslrec.auxiliar.extra.Tuple;
import lslrec.auxiliar.task.ITaskMonitor;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;
import lslrec.auxiliar.WarningMessage;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.control.HandlerMinionTemplate;
import lslrec.control.IHandlerMinion;
import lslrec.control.MinionParameters;
import lslrec.control.inputDataChecker.StreamChecker;
import lslrec.control.message.EventInfo;
import lslrec.control.message.EventType;
import lslrec.control.notification.INotificationTask;
import lslrec.control.notification.NotificationTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class OutputDataFileHandler extends HandlerMinionTemplate implements ITaskMonitor
{	
	public static final String ID = "OutputDataHandler";
	
	public static final String ACTION_START_RECORD = "start record";
	public static final String ACTION_START_SYNC = "Start sync";
	public static final String ACTION_SET_MARK = "mark";
	
	public static final String PARAMETER_OUTPUT_FORMAT = "PARAMETER_OUTPUT_FORMAT";	
	public static final String PARAMETER_LSL_SETTING = "LSL settings";
	public static final String PARAMETER_WRITE_TEST = "Writing test";
	public static final String PARAMETER_DATA_PROCESSING = "data processing";
	public static final String PARAMETER_DATA_POSTPROCESSING = "data post-processing";
	public static final String PARAMETER_SAVE_DATA_PROCESSING = "save data processing";
	
	private static OutputDataFileHandler ctr = null;

	private List< TemporalOutDataFileWriter > temps;
	private List< InputSyncData > syncInputData;
	private SyncMarkerCollectorWriter syncCollector = null;
		
	private boolean inputSyncLauched = false;
	private boolean isRecorderThreadOn = false;
	private AtomicBoolean saveSyncMarker = new AtomicBoolean( false );
	
	private AtomicBoolean syncMarkerThreadsReady = new AtomicBoolean( false );

	private Map<String, OutputBinaryFileSegmentation> outWriterHandlers = null;
	private AtomicInteger NumberOfSavingThreads = new AtomicInteger( 0 );
	private AtomicBoolean isRunBinData = new AtomicBoolean( false ); 
	
	private Object sync = new Object();
	
	private ConcurrentLinkedQueue< EventInfo > _events = new ConcurrentLinkedQueue< EventInfo >();
	
	private Timer checkOutWriterTimer = null;
	
	private Map< String, Integer > savingPercentage = new  HashMap< String, Integer >();
	
	private Semaphore taskDoneSem = new Semaphore( 1, true );
	
	private NotificationTask inputDataNotificationTask = null;
	
	private StreamChecker streamChecker = null;
		
	private LaunchOutBinFileSegmentation lauchConvertThread = null;
	
	private List< String > outputDataFileNames = new ArrayList<String>();
	
	/**
	 * Private constructor.
	 */
	private OutputDataFileHandler()
	{
		this.temps = new ArrayList< TemporalOutDataFileWriter >();
		this.syncInputData = new ArrayList< InputSyncData >();
		this.outWriterHandlers = new HashMap< String, OutputBinaryFileSegmentation >();
		
		super.setName( this.getClass().getSimpleName() );
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
			this.isRunBinData.set( true );
			
			this.NumberOfSavingThreads.set( this.temps.size() );
			
			this.savingPercentage.clear();
			
			if( this.streamChecker != null )
			{
				this.streamChecker.stopThread( IStoppableThread.FORCE_STOP );
				this.streamChecker = null;
			}
			
			for ( TemporalOutDataFileWriter temp : this.temps )
			{					
				if( !temp.getState().equals( Thread.State.NEW ) )
				{					
					temp.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
					
					this.savingPercentage.put( temp.getID(), 0 );
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
				
				super.supervisor.eventNotification( this, new EventInfo( this.getName(), EventType.ALL_OUTPUT_DATA_FILES_SAVED, null ) );
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
		
		if( this.syncCollector != null )
		{
			this.syncCollector.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
		}
		
		this.syncMarkerThreadsReady.set( false );
		this.inputSyncLauched = false;
		this.isRecorderThreadOn = false;
		//this.isSyncThreadActive = false;
	}

	public boolean isReadyInputStreams()
	{
		boolean set = ( this.temps == null );
		
		if (this.temps != null)
		{
			synchronized ( this.temps )
			{
				int counterTimeCorrectionReady = 0;

				for( TemporalOutDataFileWriter tw : this.temps )
				{
					counterTimeCorrectionReady =  tw.isReadyToStart() ? counterTimeCorrectionReady + 1 : counterTimeCorrectionReady;
				}

				set = ( counterTimeCorrectionReady >= this.temps.size() );					
			}
		}
		
		return set;
	}
		
	/*
	 * (non-Javadoc)
	 * @see Controls.HandlerMinionTemplate#startWork(java.lang.Object)
	 */
	@Override
	protected void startWork( Object info ) throws Exception
	{		
		if (this.temps != null)
		{
			synchronized ( this.temps )
			{
				Tuple t = (Tuple)info;
				
				String act = (String)t.t1;
								
				if( act.equals( ACTION_START_SYNC ) )
				{					
					this.startSyncThread( );
				}
				else if ( act.toString().equals( ACTION_START_RECORD ) )
				{	
					this.startSyncThread( );
					
					if( !this.isRecorderThreadOn )
					{
						this.isRecorderThreadOn = true;	
						
						for( final TemporalOutDataFileWriter temp : this.temps )
						{						
							temp.setNotificationTask( this.inputDataNotificationTask );
	
							LaunchThread tLaunch = new LaunchThread( temp );
							tLaunch.taskMonitor( this );
														
							tLaunch.startThread();
						}
						
						if( this.streamChecker != null )
						{
							this.streamChecker.startThread();
						}
					}					
				}
				else if ( act.toString().equals( ACTION_SET_MARK ) )
				{						
					if( this.saveSyncMarker.get() )
					{						
						this.syncCollector.SaveSyncMarker( (SyncMarker)t.t2 );
					}
				}
			}
		}
	}
		
	public boolean areReadySyncMarkThreads()
	{
		return this.saveSyncMarker.get() && this.syncMarkerThreadsReady.get();
	}
	
	private void startSyncThread( ) throws Exception
	{
		if( this.syncCollector.getState().equals( Thread.State.NEW ) )
		{
			this.syncCollector.startThread();
			
			synchronized( this )
			{
				super.wait( 100L );
			}
		}
		
		if( this.syncInputData != null  && !this.inputSyncLauched )
		{			
			this.inputSyncLauched = true;
			
			final OutputDataFileHandler auxOutDataFileCtrl = this;
			
			synchronized ( this.syncInputData )
			{
				final AtomicInteger syncThreadCounter = new AtomicInteger( this.syncInputData.size() );
				
				for( InputSyncData sync : this.syncInputData )
				{	
					//sync.taskMonitor( this );
					sync.setNotificationTask( this.inputDataNotificationTask );
					
					Thread tLauch = new Thread()
					{
						public void run()
						{
							try
							{
								sync.startThread();
							
								syncMarkerThreadsReady.set(syncThreadCounter.decrementAndGet() < 1 );
							}
							catch (Exception e)
							{
								EventInfo event = new EventInfo( sync.getID(), EventType.PROBLEM, e );
								supervisor.eventNotification( auxOutDataFileCtrl, event );
							}
						}
					};
					
					tLauch.start();					
				}
			}			
		}
	}
	
	public void setEnableSaveSyncMark( boolean saveSyncMark )
	{
		this.saveSyncMarker.set( saveSyncMark );
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
			// Get settings
			//
			
			ParameterList parameters = minionPars.getMinionParameters( ID );
			
			Parameter parFormat = parameters.getParameter( PARAMETER_OUTPUT_FORMAT );
			OutputFileFormatParameters fileFormat = (OutputFileFormatParameters)parFormat.getValue();
			
			if( fileFormat == null )
			{
				throw new IllegalArgumentException( "OutputFileFormatParameters null" );
			}
			
			if( !DataFileFormat.isSupportedFileFormat( fileFormat.getParameter( OutputFileFormatParameters.OUT_FILE_FORMAT  ).getValue().toString() ) )
			{
				throw new  IllegalArgumentException( "Unsupport file format." );
			}
						
			Parameter lslSetting = parameters.getParameter( PARAMETER_LSL_SETTING );
			HashSet< IStreamSetting > lslCFGs = ( HashSet< IStreamSetting > )lslSetting.getValue();
			
			Parameter writingTest = parameters.getParameter( PARAMETER_WRITE_TEST );
			boolean test = false;
			if( writingTest != null ) 
			{
				test = (boolean)writingTest.getValue();
			}				
			
			Parameter parProcesses = parameters.getParameter( PARAMETER_DATA_PROCESSING );
			Map< IStreamSetting, LSLRecPluginDataProcessing > processes = null;
			if( parProcesses != null )
			{
				processes = (Map< IStreamSetting, LSLRecPluginDataProcessing >) parProcesses.getValue();
			}
			
			Parameter saveProcessedDat = parameters.getParameter( PARAMETER_SAVE_DATA_PROCESSING );
			boolean saveProcDat = false;
			if( saveProcessedDat != null )
			{
				saveProcDat = (Boolean)saveProcessedDat.getValue();
			}
			
			Parameter parPostProcesses = parameters.getParameter( PARAMETER_DATA_POSTPROCESSING );
			Map< IStreamSetting, LSLRecPluginDataProcessing > postprocesses = null;
			if( parPostProcesses != null )
			{
				postprocesses = (Map< IStreamSetting, LSLRecPluginDataProcessing >) parPostProcesses.getValue();
			}
			
			//
			//
			// Clear control list
			//
						
			this.temps.clear();
			this.syncInputData.clear();
			
			if( this.syncCollector != null )
			{
				this.syncCollector.stopThread( IStoppableThread.FORCE_STOP );
			}
			
			if( this.streamChecker == null )
			{
				this.streamChecker = new StreamChecker();
				this.streamChecker.setNotificationTask( this.inputDataNotificationTask );
			}
			
			this.syncCollector = new SyncMarkerCollectorWriter( fileFormat.getParameter( OutputFileFormatParameters.OUT_FILE_NAME ).getValue().toString() );				

			/*
			if( this.inputDataNotificationTask != null )
			{
				this.inputDataNotificationTask.stopThread( IStoppableThread.FORCE_STOP );
			}

			this.inputDataNotificationTask = new NotificationTask( false, false );
			this.inputDataNotificationTask.taskMonitor( this );
			this.inputDataNotificationTask.setName( this.inputDataNotificationTask.getID() + "-" + this.getClass().getName() + "-" + super.getId() );		
			this.inputDataNotificationTask.startThread();
			//*/
			
			//
			//
			// Check LSL streamings
			//
			
			//IStreamSetting[] results = LSL.resolve_streams();
			
			//IStreamSetting[] results = DataStreamFactory.getStreamSettings( (StreamLibrary)ConfigApp.getProperty( ConfigApp.STREAM_LIBRARY ) );
			IStreamSetting[] results = DataStreamFactory.getStreamSettings( );

			// To check alive stream
			List< IStreamSetting > streamSettings = new ArrayList< IStreamSetting >();
			
			// chunck size of each stream
			List< Integer > inLetsChunkSizes = new ArrayList< Integer >();

			// check selected stream
			for( int i = 0; i < results.length; i++ )
			{
				IStreamSetting info = results[ i ];
				boolean found = false;
				
				Iterator< IStreamSetting > itLSLcfg = lslCFGs.iterator();

				while ( ( itLSLcfg.hasNext() ) && ( !found ) )
				{
					IStreamSetting stt = itLSLcfg.next();
					if( stt.isSelected() || stt.isSynchronationStream() )
					{
						found = info.uid().equals( stt.uid() );
	
						if ( found )
						{								
							// copy extra information
							//stt.getStreamInfo().desc().append_child_value( stt.getExtraInfoLabel(), stt.getExtraInfo() );
							
							// Save selected and alive stream
							streamSettings.add( stt );
							
							// Save chunck size
							inLetsChunkSizes.add( stt.getChunkSize() );
						}				
					}					
				}
			}

			// Save selected LSL stream
			if ( !streamSettings.isEmpty() )
			{
				//List< Tuple< LSL.StreamInfo, LSLConfigParameters > > syncs = new ArrayList< Tuple< LSL.StreamInfo, LSLConfigParameters > >();
				
				Set< String > nameStreams = new HashSet< String >();
							
				for ( int indexInlets = 0; indexInlets < streamSettings.size(); indexInlets++)
				{
					IStreamSetting stream2rec = streamSettings.get( indexInlets );
									
					if( stream2rec.isSynchronationStream() )
					{
						InputSyncData sync = new InputSyncData( stream2rec );
						this.syncInputData.add( sync );
						//syncs.add( t );
					}
					else
					{	
						TemporalOutDataFileWriter temp;

						if( !test )
						{
							OutputFileFormatParameters fformat = fileFormat.clone();
							if( !nameStreams.contains( stream2rec.name() ) )
							{
								nameStreams.add( stream2rec.name() );
							}
							else
							{
								Parameter< String > outFile = fformat.getParameter( OutputFileFormatParameters.OUT_FILE_NAME  );

								String out = outFile.getValue();

								int lastDot = out.lastIndexOf( "." );

								String suffix = "_1";
								if( lastDot < 0 )
								{
									out += suffix;
								}
								else
								{
									out = out.substring( 0, lastDot ) + suffix + out.substring( lastDot );
								}										

								outFile.setValue( out );
							}

							temp = new TemporalOutDataFileWriter( stream2rec, fformat, indexInlets );

							if( stream2rec.isEnableRecordingCheckerTimer() )
							{
								double samplingRate = stream2rec.sampling_rate();
								if( samplingRate == IStreamSetting.IRREGULAR_RATE )
								{
									samplingRate = 1;
								}
								
								int time = (int)( stream2rec.getRecordingCheckerTimer() * 1000.0D / samplingRate  ) ;
								if ( time < 3000 && stream2rec.sampling_rate() != IStreamSetting.IRREGULAR_RATE )
								{
									time = 3000; // 3000 milliseconds
								}
								
								this.streamChecker.setInputDataTime( temp, time );	
							}
							
							if( processes != null )
							{
								LSLRecPluginDataProcessing p = processes.get( stream2rec );
								if( p != null )
								{
									temp.setDataProcessing( p, saveProcDat );
								}
							}
							
							if( postprocesses != null )
							{
								LSLRecPluginDataProcessing postp = postprocesses.get( stream2rec );
								if( postp != null )
								{
									temp.setDataPostProcessing( postp );
								}
							}
						}
						else
						{
							temp = new WritingTest( stream2rec, fileFormat.clone(),  indexInlets );
						}

						this.temps.add( temp );
					}
				}				
				
				//this.syncCollector.SetLSLInStream( syncs );
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
		//System.out.println("OutputDataFileHandler.taskDone() A " + task.getResult( false ) );
		this.taskDoneSem.acquire();
		
		if (task != null)
		{
			List<EventInfo> events = task.getResult( true );
			
			if( events != null ) 
			{
				this._events.addAll( events );
				
				//System.out.println("OutputDataFileHandler.taskDone() B  " + this._events );
				
				synchronized ( this )
				{
					super.notify();
				}
			}
		}
		
		if( this.taskDoneSem.availablePermits() < 1 )
		{
			this.taskDoneSem.release();
		}
	}
	
	public List< State > getOutFileState()
	{
		List< State > states = new ArrayList< State >();
		
		for( OutputBinaryFileSegmentation outfile : this.outWriterHandlers.values() )
		{
			states.add( outfile.getState() );
		}
		
		return states;
	}
	
	/*
	 * (non-Javadoc)
	 * @see Controls.IHandlerMinion#checkParameters()
	 */
	public WarningMessage checkParameters()
	{
		return new WarningMessage();
	}
			
	/**
	 * 
	 * @return False if no SaveOutputFileThread instance is saving data, otherwise, True
	 */
	public boolean isSavingData()
	{			
		//return this.NumberOfSavingThreads > 0;
		//return this.NumberOfSavingThreads.get() > 0;
		
		//*
		boolean saving = false;
		
		synchronized ( this.outWriterHandlers ) 
		{
			//String[] iwh = this.outWriterHandlers.keySet().toArray( new String[0]);
			//int nWH = iwh.length;
			int nWH = this.outWriterHandlers.size();
			
			saving = ( nWH > 0 );
			// Problem with this.outWriterHandlers.isEmpty() and  this.outWriterHandlers.size()  
			
			if( saving )
			{
				String[] iwh = this.outWriterHandlers.keySet().toArray( new String[0]);
				List< String > terminatedwriter = new ArrayList<String>();
							
				for( String id : this.outWriterHandlers.keySet() )
				{
					OutputBinaryFileSegmentation obs = this.outWriterHandlers.get( id );
					
					if( obs.getState().equals( State.TERMINATED ) )
					{
						terminatedwriter.add( id );
					}				
				}
				
				for( String id : terminatedwriter )
				{
					this.outWriterHandlers.remove( id );
				}
				
				iwh = this.outWriterHandlers.keySet().toArray( new String[0]);		
				nWH = iwh.length;
				saving = ( nWH > 0 );
			}
		}
		//*/
		
		
		return saving;
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
		synchronized ( this )
		{
			if( this._events.isEmpty() )
			{
				try
				{
					this.wait();
				}
				catch (InterruptedException e)
				{
				}
			}
		}
		
		//boolean continued = true;
		
		do
		{
			final EventInfo event  = this._events.poll();
			
			//continued = ( event != null );
			
			//if( continued )
			if( event != null )
			{
				if ( event.getEventType().equals( EventType.PROBLEM ) )
				{
					super.supervisor.eventNotification( this, event );
				}
				else if ( event.getEventType().equals( EventType.WARNING ) )
				{
					super.supervisor.eventNotification( this, event );
				}
				else if( event.getEventType().equals( EventType.OUTPUT_DATA_FILE_SAVED ) )
				{	
					synchronized ( this.outWriterHandlers ) 
					{	
						this.outWriterHandlers.remove( event.getIdSource() );

						Tuple< String, SyncMarkerBinFileReader > t = (Tuple< String, SyncMarkerBinFileReader >)event.getEventInformation();
						
						if( t.t2 != null )
						{
							t.t2.closeStream();
						}
						
						String outFile = t.t1;
						super.supervisor.eventNotification( this, new EventInfo( super.getName(), EventType.OUTPUT_DATA_FILE_SAVED, new File( outFile ) ) );
						
						
						if( this.NumberOfSavingThreads.decrementAndGet() < 1 )
						{								
							if( this.checkOutWriterTimer != null )
							{
								//this.checkOutWriterTimer.stop();
								this.checkOutWriterTimer.stopThread( IStoppableThread.FORCE_STOP );
								this.checkOutWriterTimer = null;
							}
							
							if( this.lauchConvertThread != null )
							{
								this.lauchConvertThread.stopThread( IStoppableThread.FORCE_STOP );
								this.lauchConvertThread = null;
							}

							this.savingPercentage.clear();
							
							this.outputDataFileNames.clear();
							
							super.supervisor.eventNotification( this, new EventInfo( super.getName(), EventType.ALL_OUTPUT_DATA_FILES_SAVED, t.t1 )  );
														
							if( t.t2 != null )
							{
								t.t2.closeAndRemoveTempBinaryFile();
							}
							
							if( this.syncCollector != null )
							{				
								int c = 0;
								SyncMarkerBinFileReader reader = null;
								while( reader == null && c < 10)
								{
									try
									{			
										reader = this.syncCollector.getSyncMarkerBinFileReader();
										
										if( reader == null )
										{
											Thread.sleep( 100L );
										}
									}
									catch (FileNotFoundException e) 
									{
										c = 10;
									}
									catch (Exception e) 
									{
									}
									finally
									{
										c++;
									}
								}
								
								if( reader != null )
								{
									reader.closeAndRemoveTempBinaryFile();
								}
							}
						}
						else
						{
							Thread wakeupLauch = new Thread()
							{
								@Override
								public synchronized void run() 
								{
									synchronized( lauchConvertThread )
									{
										lauchConvertThread.notify();
									}
								}
							};
							
							wakeupLauch.setName( "Wake up launch convert thread" );
							wakeupLauch.start();
						}
						
						if( this.NumberOfSavingThreads.get() < 0 )
						{
							this.NumberOfSavingThreads.set( 0 );
						}
					}										
				}
				else if ( event.getEventType().equals( EventType.INPUT_MARK_READY ) )
				{	
					super.supervisor.eventNotification( this, new EventInfo( event.getIdSource(), event.getEventType(),  event.getEventInformation() ) );
					
					this.startWorking( new Tuple( ACTION_SET_MARK, event.getEventInformation() ) );
				}
				else if ( event.getEventType().equals( EventType.TEST_WRITE_TIME ) )
				{		
					final IHandlerMinion hand = this;
					//final ITaskMonitor handMonitor = this;
					final long time = ThreadLocalRandom.current().nextLong( 20L, 500L );
					
					this.InitCheckOutOWriters();
					
					synchronized ( this.outWriterHandlers )
					{							
						Thread t = new Thread()
						{
							@Override
							public synchronized void run() 
							{	
								try 
								{
									Thread.sleep( time );
								} 
								catch (InterruptedException e) 
								{
								}
								
								supervisor.eventNotification( hand , new EventInfo( event.getIdSource(), EventType.TEST_WRITE_TIME, event.getEventInformation() ) );
																		
								inputDataNotificationTask.queueAndSendEvent( new EventInfo( event.getIdSource(), EventType.OUTPUT_DATA_FILE_SAVED, new Tuple< String, SyncMarkerBinFileReader >( event.getIdSource(), null ) ) );
							}
						};
						
						t.setName( EventType.TEST_WRITE_TIME + "-AuxThread");
						
						t.start();
					}
				}
				else if ( event.getEventType().equals( EventType.CONVERT_OUTPUT_TEMPORAL_FILE ) )
				{	
					/*
					this.InitCheckOutOWriters();
					
					synchronized ( this.outWriterHandlers )
					{	
						OutputBinaryFileSegmentation saveOutFileThread = null;
						
						List< Tuple< TemporalBinData, SyncMarkerBinFileReader > > list = (List< Tuple< TemporalBinData, SyncMarkerBinFileReader > >)event.getEventInformation();

						if( !this.isRunBinData.get() )
						{
							this.NumberOfSavingThreads.addAndGet( list.size() );
						}
						
						int counterFile = -1;
						for( Tuple< TemporalBinData, SyncMarkerBinFileReader > set : list )
						{	
							counterFile++;
							String binName = "";
							
							try
							{										
								TemporalBinData dat = set.t1;
								SyncMarkerBinFileReader reader = set.t2;

								if( dat == null )
								{		
									this.NumberOfSavingThreads.decrementAndGet();
								}
								else
								{
									IStreamSetting stream = dat.getDataStreamSetting();
									binName = stream.name();

									Tuple< String, Boolean > res;

									synchronized( this.sync ) 
									{
										res = FileUtils.checkOutputFileName( dat.getOutputFileFormat().getParameter( OutputFileFormatParameters.OUT_FILE_NAME ).getValue().toString(), binName, "" + counterFile );

										try
										{
											// To avoid problems if 2 or more input streaming are called equal.
											long tSleep = ThreadLocalRandom.current().nextLong( 20L, 30L );

											super.sleep( tSleep );
										}
										catch (Exception e) 
										{
										}
									}

									if (!((Boolean)res.t2).booleanValue())
									{
										dat.getOutputFileFormat().setParameter( OutputFileFormatParameters.OUT_FILE_NAME, res.t1 );
									}

									dat.getOutputFileFormat().setParameter( OutputFileFormatParameters.OUT_FILE_NAME, res.t1 );

									saveOutFileThread = new OutputBinaryFileSegmentation( dat, reader );
									//saveOutFileThread.taskMonitor( this );
									saveOutFileThread.setNotificationTask( this.inputDataNotificationTask );
									
									if( this.outWriterHandlers != null )
									{
										//this.outWriterHandlers.put( binName, saveOutFileThread );
										this.outWriterHandlers.put( saveOutFileThread.getID(), saveOutFileThread );

										saveOutFileThread.startThread();		
									}

									if (!((Boolean)res.t2).booleanValue())
									{
										super.event = new EventInfo( event.getIdSource(), EventType.WARNING, "The output data file exist. It was renamed as " + (String)res.t1);
										super.supervisor.eventNotification( this, super.event );
									}
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
									super.supervisor.eventNotification( this, new EventInfo( event.getIdSource(), EventType.ALL_OUTPUT_DATA_FILES_SAVED, binName ) );
								}

								super.event = new EventInfo( event.getIdSource(), EventType.PROBLEM, "Save process error: " + ex.getMessage() );
								super.supervisor.eventNotification( this, super.event );
							}
						}
					}
					//*/
					
					Thread launch = new Thread()
					{
						@Override
						public synchronized void run() 
						{
							List< Tuple< TemporalBinData, SyncMarkerBinFileReader > > list = (List< Tuple< TemporalBinData, SyncMarkerBinFileReader > >)event.getEventInformation();
							for( Tuple< TemporalBinData, SyncMarkerBinFileReader > set : list )
							{
								EventInfo ev = new EventInfo( event.getIdSource(), EventType.SAVED_OUTPUT_TEMPORAL_FILE, set );
								inputDataNotificationTask.queueEvent( ev );								
							}
							
							synchronized( inputDataNotificationTask )
							{
								inputDataNotificationTask.notify();
							}
						}
					};
					launch.setName( "Launcher converter" );
					launch.start();
				}
				else if( event.getEventType().equals( EventType.SAVING_DATA_PROGRESS ) )
				{
					
					Tuple< String, Integer > value = (Tuple< String, Integer > )event.getEventInformation();
										
					int perc = value.t2;
					this.savingPercentage.put( event.getIdSource(), perc );
					
					this.supervisor.eventNotification( this, event );					
				}
				else if ( event.getEventType().equals( EventType.SAVED_OUTPUT_TEMPORAL_FILE ) )
				{			
					if( !this.isRunBinData.get() )
					{
						this.NumberOfSavingThreads.incrementAndGet();						
					}
					
					this.InitCheckOutOWriters();
										
					try
					{
						if( this.syncCollector != null )
						{
							this.syncCollector.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
						}
						
						Object evObj = event.getEventInformation();
						
						TemporalBinData dat = null;
						SyncMarkerBinFileReader syncMarkReader = null;
						
						if( evObj instanceof TemporalBinData )
						{
							dat = (TemporalBinData)event.getEventInformation();
							if( this.syncCollector != null )
							{
								syncMarkReader = this.syncCollector.getSyncMarkerBinFileReader();
								
								while( syncMarkReader == null )
								{
									super.wait( 500L );
									
									syncMarkReader = this.syncCollector.getSyncMarkerBinFileReader();
								}
							}
						}
						else if( evObj instanceof Tuple )
						{
							Tuple tupleObj = (Tuple)evObj;
							
							Object t1 = tupleObj.t1;
							Object t2 = tupleObj.t2;
							
							dat = (TemporalBinData)t1;
							
							if( t2 != null )
							{
								syncMarkReader = (SyncMarkerBinFileReader)t2;
							}
						}
						else
						{
							throw new IllegalArgumentException( "Input not is a TemporalBinData or Tuple< TemporalBinData, SyncMarkerBinFileReader >. " );
						}
						
						Tuple< String, Boolean > res = null;
						
						synchronized( this.sync ) 
						{
							boolean rep = false;
							
							String filePath = dat.getOutputFileFormat().getParameter( OutputFileFormatParameters.OUT_FILE_NAME ).getValue().toString();
							String streamName = dat.getDataStreamSetting().name();
							String suffix = "";
							int counter = 0;
							do
							{
								String name = streamName + suffix; 
								res = FileUtils.checkOutputFileName( filePath, name, "" );
								
								rep = this.outputDataFileNames.contains( res.t1 );
								
								if( rep )
								{
									counter++;
									Calendar c = Calendar.getInstance();
									c.add( 13, 1 );
									String date = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format( c.getTime() );
									suffix = "_" + date;									 
								}
								
								try
								{						
									// To avoid problems if 2 or more input streaming are called equal.
									long tSleep = ThreadLocalRandom.current().nextLong( 20L, 30L );
									
									super.sleep( tSleep );
								}
								catch (Exception e) 
								{
									e.printStackTrace();
								}
							}
							while( rep );
							
							this.outputDataFileNames.add( res.t1 );				
							
							res = new Tuple<String, Boolean>( res.t1, res.t2 && (counter == 0) );
						}
												
						if (!( (Boolean)res.t2 ).booleanValue())
						{
							dat.getOutputFileFormat().setParameter( OutputFileFormatParameters.OUT_FILE_NAME, res.t1 );
						}
						
						dat.getOutputFileFormat().setParameter( OutputFileFormatParameters.OUT_FILE_NAME, res.t1 );
						
						final String fileName = res.t1;
						Thread thrSetPerc = new Thread()
						{
							@Override
							public void run() 
							{
								EventInfo evPerc = new EventInfo( this.getName(), EventType.SAVING_DATA_PROGRESS, new Tuple< File, Integer>( new File( fileName ), 0 ) );
								
								supervisor.eventNotification( OutputDataFileHandler.getInstance(), evPerc );
							}
						};
						
						thrSetPerc.setName( "Thread2SetSavingFileProgress");
						thrSetPerc.start();
						
						//launch = new LaunchOutBinFileSegmentation( this.syncCollector, dat, this, this.outWriterHandlers );
						if( this.lauchConvertThread == null )
						{
							this.lauchConvertThread = new LaunchOutBinFileSegmentation( this.inputDataNotificationTask, this.outWriterHandlers, 4 );
							
							this.lauchConvertThread.startThread();
						}
						
						this.lauchConvertThread.addTemporalBinData( dat, syncMarkReader );
						synchronized( this.lauchConvertThread )
						{
							this.lauchConvertThread.notify();
						}

						if (!((Boolean)res.t2).booleanValue())
						{
							super.event = new EventInfo( event.getIdSource(), EventType.WARNING, "The output data file exist. It was renamed as " + (String)res.t1);
							super.supervisor.eventNotification( this, super.event );
						}						
					}
					catch (Exception ex)
					{		
						/*
						if( launch != null )
						{
							launch.stopThread( IStoppableThread.FORCE_STOP );
							launch.StopOutBinFileSegmentation( IStoppableThread.FORCE_STOP );
						}
						//*/
						
						if( this.lauchConvertThread != null )
						{
							this.lauchConvertThread.stopThread( IStoppableThread.FORCE_STOP );
							this.lauchConvertThread.StopOutBinFileSegmentation( IStoppableThread.FORCE_STOP );
						}
						
						this.NumberOfSavingThreads.decrementAndGet();
						
						if( this.NumberOfSavingThreads.get() < 1 )
						{
							super.supervisor.eventNotification( this, new EventInfo( super.getName(), EventType.ALL_OUTPUT_DATA_FILES_SAVED, ((TemporalBinData)event.getEventInformation()).getDataStreamSetting().name() ) );
						}

						super.event = new EventInfo( event.getIdSource(), EventType.PROBLEM, "Save process error: " + ex.getMessage() );
						super.supervisor.eventNotification( this, event );
					}
					finally
					{
						if( this.NumberOfSavingThreads.get() > 1 )
						{
							super.event = new EventInfo( event.getIdSource(), EventType.SAVING_OUTPUT_TEMPORAL_FILE, null );
							super.supervisor.eventNotification( this, event );
						}
					}
				}
			}
		}
		//while( continued );
		while( !this._events.isEmpty() );
	}	
	
	/*
	 * (non-Javadoc)
	 * @see StoppableThread.AbstractStoppableThread#preStart()
	 */
	@Override
	protected void preStart() throws Exception 
	{
		super.preStart();
		
		//this.NumberOfTestThreads.set( 0 );
		
		//super.stopThread = true;
		
		this.inputDataNotificationTask = new NotificationTask( false, false );
		this.inputDataNotificationTask.taskMonitor( this );
		this.inputDataNotificationTask.setName( this.inputDataNotificationTask.getID() + "-" + this.getClass().getSimpleName() + "-" + super.getId() );		
		this.inputDataNotificationTask.startThread();
	}
	
	private void InitCheckOutOWriters()
	{
		if( this.checkOutWriterTimer == null )
		{
			/*
			this.checkOutWriterTimer = new Timer( 5_000 , new ActionListener() // 5 s 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					CheckOutWriters();
				}
			});
			
			this.checkOutWriterTimer.start();
			//*/
			
			this.checkOutWriterTimer = new Timer( 5_000, false, new ActionTimerThread( new IAction() // 5s
			{				
				@Override
				public void execute() 
				{
					CheckOutWriters();
				}
			}));
			
			this.checkOutWriterTimer.restartTimer();
		}
	}
	
	/*
	private void checkWriterWatingLock()
	{
		if( this.checkWaitingLock != null )
		{
			this.checkWaitingLock.stop();
		}
		
		this.checkWaitingLock = new Timer( 30_000 , new ActionListener() // 30 s 
		{	
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				LostWaitedThread.getInstance().wakeup();
			}
		});
			
		this.checkWaitingLock.start();
	}
	//*/
	
	private void CheckOutWriters()
	{
		if( this.outWriterHandlers.size() > 0 )
		{
			synchronized ( this.outWriterHandlers )
			{
				for( OutputBinaryFileSegmentation wr : this.outWriterHandlers.values() )
				{
					if( wr.getState().equals( Thread.State.TERMINATED ) ) 
					{
						this.NumberOfSavingThreads.decrementAndGet();
					}
				}
			}
		}
		
		if( this.outWriterHandlers.size() < 1 && this.NumberOfSavingThreads.get() < 1 )
		{
			super.supervisor.eventNotification( this, new EventInfo( super.getName(), EventType.ALL_OUTPUT_DATA_FILES_SAVED, "" )  );
			//this.checkOutWriterTimer.stop();
			this.checkOutWriterTimer.stopThread( IStoppableThread.FORCE_STOP );
			this.checkOutWriterTimer = null;
		}
		else
		{
			if( this.checkOutWriterTimer != null )
			{
				//this.checkOutWriterTimer.restart();
				this.checkOutWriterTimer.restartTimer();
			}
		}
		
	}
	
	//
	//
	//******************************************************************************
	//******************************************************************************
	//
	//
	
	/*
	private class LaunchOutBinFileSegmentation extends AbstractStoppableThread
	{
		private SyncMarkerCollectorWriter syncCollector = null;
		private TemporalBinData dat = null;
		//private ITaskMonitor m = null;
		private NotificationTask notificationTask = null;
		private  Map< String, OutputBinaryFileSegmentation > writeList = null;
		
		private OutputBinaryFileSegmentation saveOutFileThread = null;
		
		private boolean error = false;
		
		public LaunchOutBinFileSegmentation( SyncMarkerCollectorWriter syncCol, TemporalBinData data
											//, ITaskMonitor monitor
											, NotificationTask notifTask
											,  Map< String, OutputBinaryFileSegmentation > wrList ) throws Exception 
		{
			if( syncCol == null )
			{
				throw new IllegalArgumentException( "SyncMarkerCollectorWriter null" );
			}
			
			super.setName( this.getClass().getSimpleName() + "-" + data.getDataStreamSetting().name() );
			
			this.syncCollector = syncCol;
			this.dat = data;
			//this.m = monitor;
			this.notificationTask = notifTask;
			this.writeList = wrList;
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
			if( this.dat != null )
			{
				this.syncCollector.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
				
				SyncMarkerBinFileReader reader = this.syncCollector.getSyncMarkerBinFileReader();
				
				while( reader == null )
				{
					super.wait( 500L );
					
					reader = this.syncCollector.getSyncMarkerBinFileReader();
				}
				
				this.saveOutFileThread = new OutputBinaryFileSegmentation( this.dat, reader );
				//this.saveOutFileThread.taskMonitor( this.m );
				this.saveOutFileThread.setNotificationTask( this.notificationTask );
				
				if( this.writeList != null )
				{
					//this.writeList.put( this.dat.getDataStreamSetting().name(), this.saveOutFileThread );
					this.writeList.put( this.saveOutFileThread.getID(), this.saveOutFileThread );
					
					this.saveOutFileThread.startThread();		
				}
			}
		}
				
		@Override
		protected void finallyManager() 
		{
			super.finallyManager();
			
			super.stopThread = true;
		}
		
		@Override
		protected void runExceptionManager(Throwable e) 
		{	
			if( !( e instanceof InterruptedException ) )
			{				
				this.error = true;
				
				if( this.saveOutFileThread != null && !this.saveOutFileThread.getState().equals( Thread.State.NEW ) )
				{
					this.StopOutBinFileSegmentation( IStoppableThread.FORCE_STOP );
				}
				
				else if( this.notificationTask != null )
				{
					
					this.notificationTask.queueAndSendEvent( new EventInfo( this.getName(), EventType.PROBLEM, e )  );
				}
			}
		}
		
		@Override
		protected void cleanUp() throws Exception 
		{
			super.cleanUp();
			
			if( this.error )
			{
				this.StopOutBinFileSegmentation( IStoppableThread.FORCE_STOP );
			}
		}
		
		public void StopOutBinFileSegmentation( int  friendliness )
		{
			if( this.saveOutFileThread != null )
			{
				this.saveOutFileThread.stopThread( friendliness );
			}
		}
	}
	//*/
	
	private class LaunchOutBinFileSegmentation extends AbstractStoppableThread
	{
		private LinkedList< Tuple< TemporalBinData, SyncMarkerBinFileReader > > data = null;
		//private ITaskMonitor m = null;
		private NotificationTask notificationTask = null;
		private  Map< String, OutputBinaryFileSegmentation > writeList = null;
				
		private boolean error = false;
		
		private int maxNumOfConvertThread = 4;
		
		
		public LaunchOutBinFileSegmentation( NotificationTask notifTask
											,  Map< String, OutputBinaryFileSegmentation > wrList
											, int maxNumOfThreads ) throws Exception 
		{
			super.setName( this.getClass().getSimpleName() );
			
			this.data = new LinkedList< Tuple< TemporalBinData, SyncMarkerBinFileReader > >();
			//this.m = monitor;
			this.notificationTask = notifTask;
			this.writeList = wrList;
			
			this.maxNumOfConvertThread = (maxNumOfThreads > 0 ) ? maxNumOfThreads : this.maxNumOfConvertThread;
		}
		
		public void addTemporalBinData( TemporalBinData dat, SyncMarkerBinFileReader sync )
		{
			synchronized( this.data ) 
			{
				if( dat != null )
				{
					this.data.add( new Tuple<TemporalBinData, SyncMarkerBinFileReader>( dat, sync ) );
				}
			}
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
			synchronized( this )
			{
				if( this.data.isEmpty() || this.writeList.size() >= this.maxNumOfConvertThread )
				{
					super.wait();
				}
			}
			
			synchronized( this.writeList )
			{		
				boolean launch = false;
				Tuple< TemporalBinData, SyncMarkerBinFileReader > datSyncMarks = null;
				synchronized( this.data )
				{
					launch = !this.data.isEmpty() && ( this.writeList.size() < this.maxNumOfConvertThread );
					
					datSyncMarks = ( launch ) ? this.data.poll() : null;
				}
				
				if( datSyncMarks != null )
				{
					TemporalBinData dat = datSyncMarks.t1;
					SyncMarkerBinFileReader reader = datSyncMarks.t2;
										
					OutputBinaryFileSegmentation saveOutFileThread = new OutputBinaryFileSegmentation( dat, reader );
					saveOutFileThread.setNotificationTask( this.notificationTask );
					
					if( this.writeList != null )
					{
						//this.writeList.put( this.dat.getDataStreamSetting().name(), this.saveOutFileThread );
						this.writeList.put( saveOutFileThread.getID(), saveOutFileThread );
						
						saveOutFileThread.startThread();		
					}
				}
			}
		}
				
		/*
		@Override
		protected void finallyManager() 
		{
			super.finallyManager();
			
			super.stopThread = true;
		}
		//*/
		
		@Override
		protected void runExceptionManager(Throwable e) 
		{	
			if( !( e instanceof InterruptedException ) )
			{				
				this.error = true;
				
				this.StopOutBinFileSegmentation( IStoppableThread.FORCE_STOP );
				
				if( this.notificationTask != null )
				{
					this.notificationTask.queueAndSendEvent( new EventInfo( this.getName(), EventType.PROBLEM, e )  );
				}
			}
		}
		
		@Override
		protected void cleanUp() throws Exception 
		{
			super.cleanUp();
			
			if( this.error )
			{
				this.StopOutBinFileSegmentation( IStoppableThread.FORCE_STOP );
			}
		}
		
		public void StopOutBinFileSegmentation( int  friendliness )
		{
			synchronized( this.writeList )
			{
				for( String idThread : this.writeList.keySet() )
				{
					OutputBinaryFileSegmentation saveOutFileThread = this.writeList.get( idThread );
					
					if( saveOutFileThread != null && !saveOutFileThread.getState().equals( Thread.State.NEW ) )
					{
						saveOutFileThread.stopThread( friendliness );
					}
				}
			}
		}
	}
}
