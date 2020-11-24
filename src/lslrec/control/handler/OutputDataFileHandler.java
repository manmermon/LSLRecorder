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
import lslrec.auxiliar.task.INotificationTask;
import lslrec.auxiliar.task.ITaskMonitor;
import lslrec.auxiliar.task.NotificationTask;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;
import lslrec.auxiliar.WarningMessage;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.control.HandlerMinionTemplate;
import lslrec.control.IHandlerMinion;
import lslrec.control.MinionParameters;
import lslrec.control.message.EventInfo;
import lslrec.control.message.EventType;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.Timer;

public class OutputDataFileHandler extends HandlerMinionTemplate implements ITaskMonitor
{	
	public static final String ID = "OutputDataHandler";
	
	public static final String ACTION_START_RECORD = "start record";
	public static final String ACTION_START_SYNC = "Start sync";
	public static final String ACTION_SET_MARK = "mark";
	
	public static final String PARAMETER_OUTPUT_FORMAT = "PARAMETER_OUTPUT_FORMAT";	
	//public static final String PARAMETER_FILE_PATH = "filePath";
	public static final String PARAMETER_LSL_SETTING = "LSL settings";
	public static final String PARAMETER_WRITE_TEST = "Writing test";
	public static final String PARAMETER_DATA_PROCESSING = "data processing";
	public static final String PARAMETER_SAVE_DATA_PROCESSING = "save data processing";
	
	private static OutputDataFileHandler ctr = null;

	private List< TemporalOutDataFileWriter > temps;
	private List< InputSyncData > syncInputData;
	private SyncMarkerCollectorWriter syncCollector = null;
		
	private boolean inputSyncLauched = false;
	private boolean isRecorderThreadOn = false;
	private AtomicBoolean saveSyncMarker = new AtomicBoolean( false );
		
	//private List<LSL.StreamInfo> streamInfos = null;

	private Map<String, OutputBinaryFileSegmentation> outWriterHandlers = null;
	private AtomicInteger NumberOfSavingThreads = new AtomicInteger( 0 );
	//private AtomicInteger NumberOfTestThreads = new AtomicInteger( 0 );
	private AtomicBoolean isRunBinData = new AtomicBoolean( false ); 
	
	private Object sync = new Object();
	
	private ConcurrentLinkedQueue< EventInfo > _events = new ConcurrentLinkedQueue< EventInfo >();
	
	private Timer checkOutWriterTimer = null;
	
	private Map< String, Integer > savingPercentage = new  HashMap< String, Integer >();
	
	//private boolean isSyncThreadActive = false;
		
	/**
	 * Private constructor.
	 */
	private OutputDataFileHandler()
	{
		this.temps = new ArrayList< TemporalOutDataFileWriter >();
		this.syncInputData = new ArrayList< InputSyncData >();
		//this.streamInfos = new ArrayList< LSL.StreamInfo >();
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
			
			for ( TemporalOutDataFileWriter temp : this.temps )
			{					
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
							temp.taskMonitor( this );
	
							LaunchThread tLaunch = new LaunchThread( temp );
							tLaunch.taskMonitor( this );
														
							tLaunch.startThread();
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
	
	private void startSyncThread( ) throws Exception
	{
		if( this.syncCollector.getState().equals( Thread.State.NEW ) )
		{
			this.syncCollector.startThread();
		}
		
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
								EventInfo event = new EventInfo( sync.getID(), EventType.PROBLEM, e );
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
			
			this.syncCollector = new SyncMarkerCollectorWriter( fileFormat.getParameter( OutputFileFormatParameters.OUT_FILE_NAME ).getValue().toString() );				

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
				
				for ( int indexInlets = 0; indexInlets < streamSettings.size(); indexInlets++)
				{
					IStreamSetting t = streamSettings.get( indexInlets );
					
					if( t.isSynchronationStream() )
					{
						InputSyncData sync = new InputSyncData( t );
						this.syncInputData.add( sync );
						//syncs.add( t );
					}
					else
					{	
						TemporalOutDataFileWriter temp;
						
						if( !test )
						{
							temp = new TemporalOutDataFileWriter( t, fileFormat.clone(), indexInlets );
							
							if( processes != null )
							{
								LSLRecPluginDataProcessing p = processes.get( t );
								if( p != null )
								{
									temp.setDataProcessing( p, saveProcDat );
								}
							}
						}
						else
						{
							temp = new WritingTest( t, fileFormat.clone(),  indexInlets );
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
	public synchronized void taskDone( INotificationTask task ) throws Exception
	{
		if (task != null)
		{
			List<EventInfo> events = task.getResult( true );
			
			if( events != null ) 
			{
				this._events.addAll( events );				
				
				synchronized ( this )
				{
					super.notify();
				}
			}
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
		return this.NumberOfSavingThreads.get() > 0;
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
		
		boolean continued = true;
		
		do
		{
			final EventInfo event  = this._events.poll();
			
			continued = ( event != null );
			
			if( continued )
			{
				if ( event.getEventType().equals( EventType.PROBLEM ) )
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
						
						if( this.NumberOfSavingThreads.decrementAndGet() < 1 )
						{								
							if( this.checkOutWriterTimer != null )
							{
								this.checkOutWriterTimer.stop();
								this.checkOutWriterTimer = null;
							}

							this.savingPercentage.clear();
							
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
					final ITaskMonitor handMonitor = this;
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
																
								NotificationTask nt = new NotificationTask( true );
								nt.setID( nt.getID() +  "-" + event.getEventType() );
								nt.setName( nt.getID() );
								nt.taskMonitor( handMonitor );
								nt.addEvent( new EventInfo( event.getIdSource(), EventType.OUTPUT_DATA_FILE_SAVED, new Tuple< String, SyncMarkerBinFileReader >( event.getIdSource(), null ) ));
								nt.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
								
								try 
								{
									nt.startThread();
								}
								catch (Exception e) 
								{
									runExceptionManager( e );
								}
							}
						};
						
						t.setName( EventType.TEST_WRITE_TIME + "-AuxThread");
						
						t.start();
					}
				}
				else if ( event.getEventType().equals( EventType.CONVERT_OUTPUT_TEMPORAL_FILE ) )
				{			
					this.InitCheckOutOWriters();
					
					synchronized ( this.outWriterHandlers )
					{	
						OutputBinaryFileSegmentation saveOutFileThread = null;
						
						List< Tuple< TemporalBinData, SyncMarkerBinFileReader > > list = (List< Tuple< TemporalBinData, SyncMarkerBinFileReader > >)event.getEventInformation();

						if( !this.isRunBinData.get() )
						{
							this.NumberOfSavingThreads.addAndGet( list.size() );
						}

						for( Tuple< TemporalBinData, SyncMarkerBinFileReader > set : list )
						{	
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
									binName = dat.getDataStreamSetting().name();

									Tuple< String, Boolean > res;

									synchronized( this.sync ) 
									{
										res = FileUtils.checkOutputFileName( dat.getOutputFileFormat().getParameter( OutputFileFormatParameters.OUT_FILE_NAME ).getValue().toString(), binName );

										try
										{
											super.sleep( 1000L ); // To avoid problems if 2 or more input streaming are called equal.
											long tSleep = ThreadLocalRandom.current().nextLong( 1000L, 1100L );

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
									saveOutFileThread.taskMonitor( this );

									if( this.outWriterHandlers != null )
									{
										this.outWriterHandlers.put( binName, saveOutFileThread );

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
				}
				else if( event.getEventType().equals( EventType.SAVING_DATA_PROGRESS ) )
				{
					int perc = (Integer) event.getEventInformation();
					
					this.savingPercentage.put( event.getIdSource(), perc );
					
					int minPerc = Integer.MAX_VALUE;					
					for( Integer Perc : this.savingPercentage.values() )
					{
						if( Perc < minPerc)
						{
							minPerc = Perc;
						}
					}
					
					if( perc == minPerc )
					{					
						this.supervisor.eventNotification( this, event );
					}					
				}
				else if ( event.getEventType().equals( EventType.SAVED_OUTPUT_TEMPORAL_FILE ) )
				{			
					if( !this.isRunBinData.get() )
					{
						this.NumberOfSavingThreads.incrementAndGet();
					}
					
					this.InitCheckOutOWriters();
					

					LaunchOutBinFileSegmentation launch = null;
					
					try
					{
						TemporalBinData dat = (TemporalBinData)event.getEventInformation();
						
						Tuple< String, Boolean > res;
						
						synchronized( this.sync ) 
						{
							res = FileUtils.checkOutputFileName( dat.getOutputFileFormat().getParameter( OutputFileFormatParameters.OUT_FILE_NAME ).getValue().toString(), dat.getDataStreamSetting().name() );
							
							try
							{
								super.sleep( 1000L ); // To avoid problems if 2 or more input streaming are called equal.
								long tSleep = ThreadLocalRandom.current().nextLong( 1000L, 1100L );
								
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
						
						launch = new LaunchOutBinFileSegmentation( this.syncCollector, dat, this, this.outWriterHandlers );

						launch.startThread();

						if (!((Boolean)res.t2).booleanValue())
						{
							super.event = new EventInfo( event.getIdSource(), EventType.WARNING, "The output data file exist. It was renamed as " + (String)res.t1);
							super.supervisor.eventNotification( this, super.event );
						}
					}
					catch (Exception ex)
					{							
						if( launch != null )
						{
							launch.stopThread( IStoppableThread.FORCE_STOP );
							launch.StopOutBinFileSegmentation( IStoppableThread.FORCE_STOP );
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
		while( continued );
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
	}
	
	private void InitCheckOutOWriters()
	{
		if( this.checkOutWriterTimer == null )
		{
			this.checkOutWriterTimer = new Timer( 10_000 , new ActionListener() // 10 s 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					CheckOutWriters();
				}
			});
			
			this.checkOutWriterTimer.start();
		}
	}
	
	
	private void CheckOutWriters()
	{
		if( !this.outWriterHandlers.isEmpty() )
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
		
		if( this.outWriterHandlers.isEmpty() && this.NumberOfSavingThreads.get() < 1 )
		{
			super.supervisor.eventNotification( this, new EventInfo( super.getName(), EventType.ALL_OUTPUT_DATA_FILES_SAVED, "" )  );
			this.checkOutWriterTimer.stop();
			this.checkOutWriterTimer = null;
		}
		else
		{
			if( this.checkOutWriterTimer != null )
			{
				this.checkOutWriterTimer.restart();
			}
		}
		
	}
	
	//
	//
	//******************************************************************************
	//******************************************************************************
	//
	//
	
	private class LaunchOutBinFileSegmentation extends AbstractStoppableThread
	{
		private SyncMarkerCollectorWriter syncCollector = null;
		private TemporalBinData dat = null;
		private ITaskMonitor m = null;
		private  Map< String, OutputBinaryFileSegmentation > writeList = null;
		
		private OutputBinaryFileSegmentation saveOutFileThread = null;
		
		private boolean error = false;
		
		public LaunchOutBinFileSegmentation( SyncMarkerCollectorWriter syncCol, TemporalBinData data, ITaskMonitor monitor,  Map< String, OutputBinaryFileSegmentation > wrList ) throws Exception 
		{
			if( syncCol == null )
			{
				throw new IllegalArgumentException( "SyncMarkerCollectorWriter null" );
			}
			
			super.setName( this.getClass().getSimpleName() + "-" + data.getDataStreamSetting().name() );
			
			this.syncCollector = syncCol;
			this.dat = data;
			this.m = monitor;
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
				this.saveOutFileThread.taskMonitor( this.m );
				
				if( this.writeList != null )
				{
					this.writeList.put( this.dat.getDataStreamSetting().name(), this.saveOutFileThread );
		
					savingPercentage.put( this.saveOutFileThread.getID(), 0 );
					
					this.saveOutFileThread.startThread();		
				}
			}
		}
		
		/*
		@Override
		protected void targetDone() throws Exception 
		{
			super.targetDone();
			
			super.stopThread = true;
		}
		*/
		
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
				
				if( !this.saveOutFileThread.getState().equals( Thread.State.NEW ) )
				{
					this.StopOutBinFileSegmentation( IStoppableThread.FORCE_STOP );
				}
				else if( this.m != null )
				{
					NotificationTask notif = new NotificationTask( false );
					notif.taskMonitor( this.m );
					
					notif.addEvent( new EventInfo( this.getName(), EventType.PROBLEM, e ) );
					
					try 
					{
						notif.startThread();
					}
					catch (Exception e1) 
					{
						e1.printStackTrace();
					}
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
}
