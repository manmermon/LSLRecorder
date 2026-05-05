/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
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

import lslrec.auxiliar.thread.BeepSound;
import lslrec.auxiliar.thread.DeadlockDetector;
import lslrec.auxiliar.thread.LostWaitedThread;
import lslrec.auxiliar.thread.timer.ActionTimerThread;
import lslrec.auxiliar.thread.timer.IAction;
import lslrec.auxiliar.thread.timer.Timer;
import lslrec.config.ConfigApp;
import lslrec.config.GeneralSettings;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.config.checklistMessage.CheckMessage;
import lslrec.config.language.Language;
import lslrec.control.IHandlerMinion;
import lslrec.control.IHandlerSupervisor;
import lslrec.control.MinionParameters;
import lslrec.control.handler.minion.OutputDataFileHandler;
import lslrec.control.handler.minion.SocketHandler;
import lslrec.control.message.AppState;
import lslrec.control.message.EventInfo;
import lslrec.control.message.RegisterSyncMessages;
import lslrec.control.message.SocketInformations;
import lslrec.control.notification.manager.ControlNotificationCoordinator;
import lslrec.dataStream.binary.input.writer.StreamBinaryHeader;
import lslrec.dataStream.family.DataStreamFactory;
import lslrec.dataStream.family.setting.IMutableStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.SimpleMutableStreamSetting;
import lslrec.dataStream.family.setting.StreamExtraLabels;
import lslrec.dataStream.family.stream.lslrec.LSLRecStream;
import lslrec.dataStream.family.stream.lslrec.streamgiver.StringLogStream;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.outputDataFile.format.clis.ClisEncoder;
import lslrec.dataStream.sync.SyncMarker;
import lslrec.dataStream.sync.SyncMethod;
import lslrec.exceptions.CoreControlUserCancelStartException;
import lslrec.exceptions.SettingException;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.gui.GuiManager;
import lslrec.gui.dataPlot.DataStreamPlotter;
import lslrec.gui.dialog.Dialog_Password;
import lslrec.gui.dialog.Dialog_WarningMessages;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.PluginDataProcessingSettings;
import lslrec.plugin.lslrecPlugin.sync.LSLRecPluginSyncMethod;
import lslrec.plugin.lslrecPlugin.trial.ILSLRecPluginTrial;
import lslrec.plugin.lslrecPlugin.trial.LSLRecPluginTrial;
import lslrec.plugin.register.DataProcessingPluginRegistrar;
import lslrec.plugin.register.TrialPluginRegistrar;
import lslrec.sockets.info.StreamInputMessage;
import lslrec.sockets.info.SocketSetting;
import lslrec.sockets.SocketMessageDelayCalculator;
import lslrec.sockets.info.SocketParameters;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;
import lslrec.auxiliar.WarningMessage;
import lslrec.auxiliar.extra.FileUtils;
import lslrec.auxiliar.extra.Tuple;

import java.awt.Dimension;
import java.io.File;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class CoreControl extends AbstractStoppableThread implements IHandlerSupervisor
{
	private static CoreControl core = null;
	
	private SocketHandler ctrSocket = null;
	private OutputDataFileHandler ctrlOutputFile = null;
	private ControlNotificationCoordinator notifiedEventHandler = null;
	
	private StopWorkingThread stopThread = null; // To avoid deadlock
	private Object lock = new Object();
				
	private SocketInformations streamPars = null;
	
	private boolean isWaitingForStartCommand = false;
	private boolean isRecording = false;
	
	private boolean closeWhenDoingNothing = false;
	
	private Timer writingTestTimer;
	
	private SyncMarker SpecialMarker = null;

	private SocketMessageDelayCalculator socketMsgDelayCal = null;
		
	private volatile String encryptKey = "";
	
	private LSLRecPluginTrial trial = null;
	
	private List< LSLRecPluginSyncMethod > syncPluginMet = new ArrayList< LSLRecPluginSyncMethod >();
	
	private DeadlockDetector deadlockDetector = null;
	
	private BeepSound beep = null;
	
	private String trialPlgExtraStreamInfo = "";
	
	/**
	 * Create main control unit.
	 * 
	 * @throws Exception - Error in subordinates. 
	 */
	private CoreControl() throws Exception
	{	
		this.setName( this.getClass().getSimpleName() );
		
		this.createControlUnits();
		
		try
		{
			this.beep = new BeepSound();
			
			this.beep.startThread();
		}
		catch (Exception | Error e) 
		{	
		}		
	}

	/**
	 * Singleton class
	 * 
	 * @return coreControl instance
	 * 
	 * @throws Exception - Constructor error
	 */
	public static CoreControl getInstance() throws Exception
	{
		if (core == null)
		{
			core = new CoreControl();
		}

		return core;
	}

	public void setSpecialMarker( SyncMarker mark )
	{	
		if ( mark.getMarkValue() == RegisterSyncMessages.getSyncMark( RegisterSyncMessages.INPUT_STOP ) )
		{ 			
			if( (Boolean)ConfigApp.getProperty( ConfigApp.IS_ACTIVE_SPECIAL_INPUTS ) )
			{				
				this.SpecialMarker = mark;
				
				try 
				{
					CoreControl.getInstance().stopWorking();
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				
			}
		}
		else if ( mark.getMarkValue() == RegisterSyncMessages.getSyncMark( RegisterSyncMessages.INPUT_START ) )
		{			
			if( (Boolean)ConfigApp.getProperty( ConfigApp.IS_ACTIVE_SPECIAL_INPUTS ) 
					&& !isRecording() 
					&&  isWaitingForStartCommand )
				{
					isWaitingForStartCommand = false;

					GuiManager.getInstance().addInputMessageLog( mark.getMarkValue() + "\n");
					
					try
					{
						this.SpecialMarker = mark;
						startRecord();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
		}
	}
	
	public void calculateSocketDelay( StreamInputMessage msg )
	{
		this.socketMsgDelayCal.CalculateMsgDelay( msg );
	}
	
	/**
	 * Create the subordinate controls.
	 * 
	 * @throws Exception
	 */
	private void createControlUnits() throws Exception
	{
		// socket control
		this.ctrSocket = SocketHandler.getInstance();
		this.ctrSocket.setControlSupervisor( this );
		this.ctrSocket.startThread();
		
		// Output file control
		try
		{
			this.ctrlOutputFile = OutputDataFileHandler.getInstance();
			this.ctrlOutputFile.setControlSupervisor( this );
			this.ctrlOutputFile.startThread();
		}
		catch (Error localError) 
		{
		}
		
		// Socket delay calculator
		this.socketMsgDelayCal = new SocketMessageDelayCalculator( GeneralSettings.DEFAULT_NUM_SOCKET_PING );
		this.socketMsgDelayCal.taskMonitor( this.ctrlOutputFile );
		this.socketMsgDelayCal.startThread();

		// Notification control thread
		this.notifiedEventHandler = new ControlNotificationCoordinator();
		this.notifiedEventHandler.setName( this.notifiedEventHandler.getClass().getName() );
		this.notifiedEventHandler.startThread();
		
		//		
		LostWaitedThread.getInstance().startThread();
	}
	
	/**
	 * Start to record data
	 */
	public synchronized void startWorking( boolean testWriting )
	{		
		try
		{	
			if( this.writingTestTimer != null )
			{
				this.writingTestTimer.stopThread( IStoppableThread.FORCE_STOP );
				this.writingTestTimer = null;
			}
					
			System.gc(); // Clean memory

			// Delete plots.
			DataStreamPlotter.getInstance().disposeDataPlots();
			
			GuiManager.getInstance().setAppState( AppState.State.PREPARING, 0, false );
			
			this.ctrlOutputFile.setEnableSaveSyncMark( false );
			
			// Check settings
			List< WarningMessage > warnMsg = this.checkSettings();
					
			List< String > errorMsgs = new ArrayList<String>();
			List< String > warningMsgs = new ArrayList<String>();
			for( WarningMessage wmsg : warnMsg )
			{
				if( wmsg.getWarningType() == WarningMessage.ERROR_MESSAGE )
				{
					errorMsgs.add( wmsg.getMessage() );
				}
				else if( wmsg.getWarningType() == WarningMessage.WARNING_MESSAGE )
				{
					warningMsgs.add( wmsg.getMessage() );
				}
			}

			if( !errorMsgs.isEmpty() )
			{
				String error = "";
				for( String err : errorMsgs )
				{
					error = ( err.endsWith( "\n" ) ) ?  error + err : error + err + "\n";
				}
				
				if( !error.isEmpty() )
				{
					error = error.substring( 0, error.length()-1 );
				}
				
				throw new SettingException( error );
			}
			
			if( !warningMsgs.isEmpty() 
					&& !testWriting 
					//&& !ConfigApp.isTesting() 
					)
			{
				Dialog_WarningMessages dialog = new Dialog_WarningMessages( GuiManager.getInstance().getAppUI(), warningMsgs );

				dialog.setLocationRelativeTo( GuiManager.getInstance().getAppUI() );
				dialog.setVisible( true );
				
				int actionDialog = dialog.getSelectedOption();
				if ( actionDialog == Dialog_WarningMessages.OPTION_CANCEL 
						|| actionDialog == Dialog_WarningMessages.OPTION_NO_SELECTED )
				{					
					GuiManager.getInstance().setAppState( AppState.State.NONE, 0, false );
										
					throw new CoreControlUserCancelStartException();
				}
			}
		
			//
			// Create In-Out sockets
			//
			this.setSocketHandlerSetting();
			
			//
			// Trial Plugin
			//
			// Before than setOutputHandlerSetting(.)
			//
			this.trialPlgExtraStreamInfo = this.setTrialPlugin();
			
			//
			//Output data file
			//			
			boolean isSyncLSL = this.setOutputHandlerSetting( testWriting );
			
			boolean isActiveSpecialInputMsg = (Boolean)ConfigApp.getProperty( ConfigApp.IS_ACTIVE_SPECIAL_INPUTS );
			
			if( !testWriting )
			{
				this.isWaitingForStartCommand = isActiveSpecialInputMsg												
												&& 
												( this.streamPars.getInputCommands().containsKey( RegisterSyncMessages.INPUT_START )
														|| isSyncLSL
														|| ( this.trial != null && this.trial.hasSyncMethod() ) );
			}
			else
			{
				this.isWaitingForStartCommand = false;
												
				this.writingTestTimer = new Timer( GeneralSettings.WRITING_TEST_TIME, false, new ActionTimerThread( new IAction() 
				{					
					@Override
					public void execute() 
					{
						GuiManager.getInstance().stopRecording();						
					}
				} ));
				
				this.writingTestTimer.startThread();
			}			
			
			//
			// Sync method plugin
			//
			this.setSyncPlugin();
			
			//
			// deadlock detector
			//
			this.setDeadlockDetector();
			
			this.waitStartCommand();
		}
		catch ( Exception | Error e )
		{	
			/*
			ExceptionDialog.showMessageDialog( new ExceptionMessage(  e //new Throwable( "Exception recording" )
																	, "Exception"
																	, ExceptionMessage.ERROR_MESSAGE)
																, true, false);
			//*/
			
			if( this.ctrSocket != null )
			{
				this.ctrSocket.deleteSubordinates( IStoppableThread.FORCE_STOP );
			}
			
			if( this.ctrlOutputFile != null )
			{
				this.ctrlOutputFile.deleteSubordinates( IStoppableThread.FORCE_STOP );
			}
			
			this.isRecording = false;
			
			if( !this.ctrlOutputFile.isSavingData() )
			{
				GuiManager.getInstance().setAppState( AppState.State.STOP, 0, false );
			}
			else
			{
				GuiManager.getInstance().setAppState( AppState.State.SAVING, 0, false);
			}
			
			GuiManager.getInstance().restoreGUI();
			GuiManager.getInstance().refreshDataStreams();
			
			this.isWaitingForStartCommand = false;
			
			if( !(e instanceof CoreControlUserCancelStartException ) )
			{
				try 
				{
					ExceptionMessage ex = new ExceptionMessage( e,  Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionMessage.ERROR_MESSAGE );
					ExceptionDialog.showMessageDialog( ex, true, !(e instanceof SettingException ) );
					
					e.printStackTrace();
				}
				catch (Exception e1) 
				{
					e1.printStackTrace();
				}		
			}
		}
	}
	
	private void setSocketHandlerSetting() throws Exception
	{
		this.streamPars = this.getSocketStreamingInformations();
		ParameterList socketParameters = new ParameterList();
		
		if ( this.streamPars.getOuputSocketInformation() != null)
		{
			List< SocketParameters > server = new ArrayList< SocketParameters >();
			server.add( this.streamPars.getOuputSocketInformation() );

			Parameter par = new Parameter( SocketHandler.SERVER_SOCKET_STREAMING, server );
			socketParameters.addParameter( par );
		}
		
		MinionParameters minionPars = new MinionParameters();
		minionPars.setMinionParameters( this.ctrSocket.ID, socketParameters );
								
		this.ctrSocket.addSubordinates( minionPars );			

		this.socketMsgDelayCal.clearInputMessages();
		if( !this.streamPars.getInputCommands().isEmpty() )
		{				
			this.socketMsgDelayCal.AddInputMessages( this.streamPars.getInputCommands() );
		}
	}
	
	private boolean setOutputHandlerSetting( boolean testWriting ) throws Exception
	{
		boolean isSyncLSL = false;
		
		if ( this.ctrlOutputFile != null )
		{	
			//String file = FileUtils.getOutputCompletedFileNameFromConfig();
			List< String > fileParts = FileUtils.getOutputCompletedFileNameFromConfig();
			String file = FileUtils.getOutputCompletedFileName( fileParts );
			if( file == null)
			{
				throw new IllegalArgumentException( "Output file path error" );
			}
			
			HashSet< IMutableStreamSetting > deviceIDs = (HashSet< IMutableStreamSetting >)ConfigApp.getProperty( ConfigApp.ID_STREAMS );

			HashSet< IStreamSetting > DEV_ID = new HashSet< IStreamSetting >();
			Map< IMutableStreamSetting, LSLRecPluginDataProcessing > DataProcesses = new HashMap<IMutableStreamSetting, LSLRecPluginDataProcessing >();
			Map< IMutableStreamSetting, LSLRecPluginDataProcessing > DataPostProcesses = new HashMap<IMutableStreamSetting, LSLRecPluginDataProcessing >();

			
			int recordingCheckerTimer = (Integer)ConfigApp.getProperty( ConfigApp.RECORDING_CHECKER_TIMER );
			double waitingTime2reconnect = (Double)ConfigApp.getProperty( ConfigApp.WAITING_TIME_TO_RECONNECT_LOST_STREAM );			
			Set< String > syncMet = (Set< String >)ConfigApp.getProperty( ConfigApp.SELECTED_SYNC_METHOD );
						
			for( IMutableStreamSetting dev : deviceIDs )
			{
				if( dev.isSelected() )
				{						
					dev.setRecordingCheckerTimer( recordingCheckerTimer );
					dev.setReconnectionWaitingTime( waitingTime2reconnect );
					
					DEV_ID.add( dev );
					
					LSLRecPluginDataProcessing process = null;
					
					PluginDataProcessingSettings plgDatProcessingSettings = new PluginDataProcessingSettings( dev );
					File filePath = new File( file );
					plgDatProcessingSettings.setParameter( PluginDataProcessingSettings.PAR_OUTPUT_FOLDER, filePath.getParentFile().getCanonicalPath());
					for( ILSLRecPluginDataProcessing pr : DataProcessingPluginRegistrar.getNewInstanceOfDataProcessing( dev, DataProcessingPluginRegistrar.PROCESSING ) )
					{
						process = pr.getProcessing( plgDatProcessingSettings, process );
						process.loadProcessingSettings( pr.getSettings() );
					}
					
					DataProcesses.put( dev, process );
					
					IMutableStreamSetting posDev = new SimpleMutableStreamSetting( dev.getLibraryID()
																				, dev.name()
																				, dev.data_type()
																				, dev.getTimestampDataType()
																				, dev.getStringLengthDataType()
																				, dev.channel_count() + 1
																				, dev.sampling_rate()
																				, dev.getRecordingCheckerTimer()
																				, dev.isEnableRecordingCheckerTimer()
																				, dev.source_id()
																				, dev.uid()
																				, dev.reconnectionWaitingTime()
																				, dev.getExtraInfo()
																				, dev.getChunkSize() );
					PluginDataProcessingSettings plgDatPostProcessingSettings = new PluginDataProcessingSettings( posDev );
					plgDatPostProcessingSettings.setParameter( PluginDataProcessingSettings.PAR_OUTPUT_FOLDER, filePath.getParentFile().getCanonicalPath());
					process = null;
					
					for( ILSLRecPluginDataProcessing pr : DataProcessingPluginRegistrar.getNewInstanceOfDataProcessing( dev, DataProcessingPluginRegistrar.POSTPROCESSING ) )
					{						
						process = pr.getProcessing( plgDatPostProcessingSettings, process );
						process.loadProcessingSettings( pr.getSettings() );
					}
					
					DataPostProcesses.put( dev, process );
				}
				else
				{
					if( syncMet.contains( SyncMethod.SYNC_STREAM )
							&& dev.isSynchronationStream() )
					{
						DEV_ID.add( dev );
						
						isSyncLSL = isSyncLSL | dev.isSynchronationStream();
					}
				}
			}

			if ( !DEV_ID.isEmpty() )
			{
				File folder = new File( file );
				if( !folder.exists() )
				{
					File fpath = folder.getParentFile();
					if( fpath == null || ( !fpath.exists() && !fpath.mkdirs() ) )
					{
						throw new FileSystemException( "Folder " + fpath.getCanonicalPath() + " not created." );
					}
				}

				ParameterList StreamPars = new ParameterList();
				
				Parameter lslSetting = new Parameter( this.ctrlOutputFile.PARAMETER_LSL_SETTING, DEV_ID );
				StreamPars.addParameter( lslSetting );
				
				Parameter datProcesses = new Parameter( this.ctrlOutputFile.PARAMETER_DATA_PROCESSING, DataProcesses );
				StreamPars.addParameter( datProcesses );
				Parameter datPostProcesses = new Parameter( this.ctrlOutputFile.PARAMETER_DATA_POSTPROCESSING, DataPostProcesses );
				StreamPars.addParameter( datPostProcesses );
				
				Parameter savePprocessedDat = new Parameter( this.ctrlOutputFile.PARAMETER_SAVE_DATA_PROCESSING, (Boolean)ConfigApp.getProperty( ConfigApp.OUTPUT_SAVE_DATA_PROCESSING ) );
				StreamPars.addParameter( savePprocessedDat );
				
				Parameter writingTest = new Parameter( this.ctrlOutputFile.PARAMETER_WRITE_TEST, testWriting );
				StreamPars.addParameter( writingTest );
				
				OutputFileFormatParameters outFormat = DataFileFormat.getDefaultOutputFileFormatParameters();
				outFormat.setParameter( OutputFileFormatParameters.OUT_FILE_NAME, file );
				outFormat.setParameter( OutputFileFormatParameters.ZIP_ID, ConfigApp.getProperty( ConfigApp.OUTPUT_COMPRESSOR ).toString() );
				outFormat.setParameter( OutputFileFormatParameters.OUT_FILE_FORMAT, (String)ConfigApp.getProperty( ConfigApp.OUTPUT_FILE_FORMAT ) );
				outFormat.setParameter( OutputFileFormatParameters.PARALLELIZE, (Boolean)ConfigApp.getProperty( ConfigApp.OUTPUT_PARALLELIZE ) );				
				outFormat.setParameter( OutputFileFormatParameters.ENCRYPT_KEY, this.encryptKey );
				this.encryptKey = "";
				
				String nodeId = StreamExtraLabels.ID_SOCKET_MARK_INFO_LABEL;
				String nodeText = "";
				
				Map< String, Integer > MARKS = RegisterSyncMessages.getSyncMessagesAndMarks();
				
				for( String idMark : MARKS.keySet() )
				{
					Integer v = MARKS.get( idMark );
					nodeText += idMark + "=" + v + StreamBinaryHeader.HEADER_BINARY_SEPARATOR;
				}		
				
				((Map< String, String >)( outFormat.getParameter( OutputFileFormatParameters.RECORDING_INFO ).getValue()) ).put( nodeId, nodeText );
							
				if( this.trial != null && this.trialPlgExtraStreamInfo != null && !this.trialPlgExtraStreamInfo.isEmpty() )
				{
						nodeId = StreamExtraLabels.ID_TRIAL_INFO_LABEL + "_" + this.trial.getID().replaceAll("\\s+", "" );
						
						((Map< String, String >)( outFormat.getParameter( OutputFileFormatParameters.RECORDING_INFO ).getValue()) ).put( nodeId, this.trialPlgExtraStreamInfo );
				}	
				
				nodeId = StreamExtraLabels.ID_RECORD_GENERAL_DESCRIPTION;
				nodeText = ConfigApp.getProperty( ConfigApp.OUTPUT_FILE_DESCR ).toString();
				((Map< String, String >)( outFormat.getParameter( OutputFileFormatParameters.RECORDING_INFO ).getValue() ) ).put( nodeId, nodeText );
				
				nodeId = StreamExtraLabels.ID_SUBJ;
				nodeText = ConfigApp.getProperty( ConfigApp.OUTPUT_SUBJ_ID ).toString();
				((Map< String, String >)( outFormat.getParameter( OutputFileFormatParameters.RECORDING_INFO ).getValue() ) ).put( nodeId, nodeText );
				
				nodeId = StreamExtraLabels.ID_SESSION;
				nodeText = fileParts.get( 2 );
				((Map< String, String >)( outFormat.getParameter( OutputFileFormatParameters.RECORDING_INFO ).getValue() ) ).put( nodeId, nodeText );
				
				Parameter outFileFormat = new Parameter( this.ctrlOutputFile.PARAMETER_OUTPUT_FORMAT, outFormat );
				StreamPars.addParameter( outFileFormat );
				
				MinionParameters minionPars = new MinionParameters();
				minionPars.setMinionParameters( this.ctrlOutputFile.ID, StreamPars );

				this.ctrlOutputFile.addSubordinates( minionPars );
				
				//
				//
				//				
				Object testID = ConfigApp.getProperty( ConfigApp.OUTPUT_TEST_ID );
				Object subjID = ConfigApp.getProperty( ConfigApp.OUTPUT_SUBJ_ID );
				
				File filePath = new File( file );
				folder = null;
				
				String sessionID = "";
				String sjID = "";
				
				if( testID != null && !testID.toString().isEmpty() )
				{
					folder = filePath.getParentFile();
					
					sessionID = ( folder != null ) ? folder.getName() : "";
				}
				
				if( subjID != null && !subjID.toString().isEmpty() )
				{
					if( folder != null )
					{
						folder = folder.getParentFile();
					}
					else
					{
						folder = filePath.getParentFile();
					}
					
					sjID = ( folder != null ) ? folder.getName() : "";
				}
				
				ExceptionDialog.openLogFile( sjID, sessionID );
			}
		}
		
		return isSyncLSL;
	}
	
	private void setDeadlockDetector() throws Exception
	{
		if( this.deadlockDetector != null )
		{
			this.deadlockDetector.stopThread( IStoppableThread.FORCE_STOP );				
		}
		
		this.deadlockDetector = new DeadlockDetector( 10_000L, 10 ); // 10 s, 10 iteractions
		this.deadlockDetector.startThread();
	}
	
	public void stopRunningBackgroundThreads() throws Exception
	{
		if( this.deadlockDetector != null )
		{ 
			this.deadlockDetector.stopThread( IStoppableThread.FORCE_STOP );
			this.deadlockDetector = null;
		}		
	}
	
	private void setSyncPlugin()
	{
		if( !this.syncPluginMet.isEmpty() )
		{
			for( LSLRecPluginSyncMethod plgSyncMet : this.syncPluginMet )
			{
				plgSyncMet.stopThread( IStoppableThread.FORCE_STOP );
			}
			
			this.syncPluginMet.clear();
		}
		
		//String syncMet = ConfigApp.getProperty( ConfigApp.SELECTED_SYNC_METHOD ).toString();
		Set< String > syncMet = (Set< String >)ConfigApp.getProperty( ConfigApp.SELECTED_SYNC_METHOD );
		
		for( String met : SyncMethod.getSyncMethodID() )
		{
			if( syncMet.contains( met ) )
			{
				LSLRecPluginSyncMethod plgSyncMet = SyncMethod.getSyncPlugin( met );
				
				if( plgSyncMet != null )
				{
					plgSyncMet.taskMonitor( this.ctrlOutputFile );
					
					this.syncPluginMet.add( plgSyncMet );
				}
			}
		}		
	}
		
	private String setTrialPlugin()
	{
		ILSLRecPluginTrial trialPl = TrialPluginRegistrar.getNewInstanceOfTrialPlugin();
		
		String plExtraInfo = "";
		
		if( trialPl != null )
		{
			this.trial = trialPl.getGUIExperiment();
			
			if( this.trial != null )
			{
				plExtraInfo = trialPl.getExtraInfo2Stream();
				
				if( trialPl.hasTrialLog() )
				{
					StringLogStream log = new StringLogStream();
					
					LSLRecStream.setDataStreamGiver( trialPl.getID(), log );
					this.trial.setTrialLogStream( log );
				}
				
				if( (Boolean)ConfigApp.getProperty( ConfigApp.TRIAL_FULLSCREEN ) )
				{
					this.trial.setTrialWindowState( JFrame.MAXIMIZED_BOTH );
				}
				else
				{
					int w = (Integer)ConfigApp.getProperty( ConfigApp.TRIAL_WINDOW_WIDTH );
					int h = (Integer)ConfigApp.getProperty( ConfigApp.TRIAL_WINDOW_HEIGHT );
					
					this.trial.setTrialWindowSize( new Dimension( w, h ) );
				}
				
				this.trial.loadSettings( trialPl.getSettings() );
				
				this.trial.taskMonitor( this.ctrlOutputFile );
			}
		}
		
		return plExtraInfo;
	}
	
	/**
	 * 
	 * Check Settings.
	 *   
	 */
	private List< WarningMessage > checkSettings() 
	{
		List< WarningMessage > warnMsgsList = new ArrayList< WarningMessage >();
				
		WarningMessage outFileMsg = this.ctrlOutputFile.checkParameters();
		WarningMessage socketMsg = this.ctrSocket.checkParameters();
		
		warnMsgsList.add( outFileMsg );
		warnMsgsList.add( socketMsg );
		
		ILSLRecPluginTrial trial = TrialPluginRegistrar.getNewInstanceOfTrialPlugin();
		
		if( trial != null )
		{
			WarningMessage trW = trial.checkSettings();
			
			warnMsgsList.add( new WarningMessage( trW.getMessage(), trW.getWarningType() ) );
		}
		
		String idFormat = ConfigApp.getProperty( ConfigApp.OUTPUT_FILE_FORMAT ).toString();
		Tuple< Encoder, WarningMessage > enc = DataFileFormat.getDataFileEncoder( idFormat );
		if( enc == null || enc.t1 == null)
		{
			warnMsgsList.add( new WarningMessage( "Encoder null", WarningMessage.ERROR_MESSAGE ) );
		}
		else if( !( enc.t1 instanceof ClisEncoder ) )
		{
			WarningMessage wm = enc.t2;
			if( wm != null )
			{
				warnMsgsList.add( new WarningMessage( wm.getMessage(), wm.getWarningType() ) );
			}
		}
		
		if( (Boolean)ConfigApp.getProperty( ConfigApp.OUTPUT_ENCRYPT_DATA ) )
		{
			Dialog_Password pass = new Dialog_Password( GuiManager.getInstance().getAppUI(), Language.getLocalCaption( Language.ENCRYPT_KEY_TEXT ) );			
			
			pass.setLocationRelativeTo( GuiManager.getInstance().getAppUI() );
			pass.setVisible( true );
			
			while( pass.getState() == Dialog_Password.PASSWORD_INCORRECT )
			{
				pass.setMessage( pass.getPasswordError()  + " " + Language.getLocalCaption( Language.REPEAT_TEXT ) + ".");
				pass.setVisible( true );
			}
			
			if( pass.getState() != Dialog_Password.PASSWORD_OK )
			{
				warnMsgsList.add( new WarningMessage( Language.getLocalCaption( Language.PROCESS_TEXT ) + " " + Language.getLocalCaption( Language.CANCEL_TEXT )
													, WarningMessage.ERROR_MESSAGE ) );
			}
			
			this.encryptKey = pass.getPassword();
			
			if( this.encryptKey == null )
			{
				this.encryptKey = "";
			}
		}
		
		HashSet< IStreamSetting > lslPars = (HashSet< IStreamSetting >)ConfigApp.getProperty( ConfigApp.ID_STREAMS );
		IStreamSetting[] results = DataStreamFactory.getStreamSettings( );
				
		boolean existSelectedSyncLSL = false;
		
		if( results.length >= 0 )
		{
			boolean selectedStreamsOK = true;
			boolean selectOneOrMoreStream = false;

			for( IStreamSetting lslcfg : lslPars )
			{
				selectOneOrMoreStream = lslcfg.isSelected();
				
				if( selectOneOrMoreStream )
				{
					break;
				}
			}
			
			if( selectOneOrMoreStream )
			{
				// Check selected streams.
				for( IStreamSetting lslcfg : lslPars )
				{
					if( lslcfg.isSelected() )
					{	
						boolean findStream = false;
						for( int i = 0; i < results.length && !findStream; i++ )
						{
							findStream = results[ i ].uid().equals( lslcfg.uid() );
						}
						
						if( !findStream )
						{
							selectedStreamsOK = false;
							break;
						}
					}
				}
			}
			
			// Check if sync stream is selected.
			for( IStreamSetting lslcfg : lslPars )
			{
				existSelectedSyncLSL = lslcfg.isSynchronationStream();

				if( existSelectedSyncLSL )
				{
					break;
				}
			}

			if( !selectOneOrMoreStream )
			{
				warnMsgsList.add( new WarningMessage( Language.getLocalCaption( Language.CHECK_NON_SELECTED_STREAMS_ERROR_MSG ), WarningMessage.ERROR_MESSAGE ) );	
			}
			
			if( !selectedStreamsOK )
			{
				warnMsgsList.add( new WarningMessage( Language.getLocalCaption( Language.CHECK_DEVICES_CHANGE_WARNING_MSG ), WarningMessage.ERROR_MESSAGE ) );
			}
			
			Set< String > syncMeths = (Set< String >)ConfigApp.getProperty( ConfigApp.SELECTED_SYNC_METHOD );
			boolean specialInMsg = (Boolean)ConfigApp.getProperty( ConfigApp.IS_ACTIVE_SPECIAL_INPUTS );
			if( syncMeths.contains( SyncMethod.SYNC_STREAM ) && !existSelectedSyncLSL )
			{
					String msg = Language.getLocalCaption( Language.CHECK_SYNC_NO_SELECT_STREAM_WARNING_MSG );
					int warmType = WarningMessage.WARNING_MESSAGE;
					
					if( specialInMsg )
					{
						msg = Language.getLocalCaption( Language.CHECK_SYNC_UNSELECTABLE_ERROR_MSG );
						warmType = WarningMessage.ERROR_MESSAGE;
					}
					
					warnMsgsList.add( new WarningMessage( msg, warmType ) );
			}			
			else if( existSelectedSyncLSL && !syncMeths.contains( SyncMethod.SYNC_STREAM ) )
			{
				warnMsgsList.add( new WarningMessage( Language.getLocalCaption( Language.CHECK_SYNC_STREAM_WARNING_MSG ), WarningMessage.WARNING_MESSAGE ) );
			}
			
			boolean change = false;
				
			for( int i = 0; i < results.length && !change; i++ )
			{
				IStreamSetting stream = results[ i ];

				for( IStreamSetting lslcfg : lslPars )
				{
					if( ( lslcfg.isSelected() || lslcfg.isSynchronationStream() )
							&& lslcfg.name().equals( stream.name() ) 
							&& lslcfg.uid().equals( stream.source_id() ) )
					{
						change = !stream.uid().equals( lslcfg.uid() ) ;
	
						if( change )
						{
							break;
						}
					}
				}
			}

			if( change )
			{
				warnMsgsList.add( new WarningMessage( Language.getLocalCaption( Language.CHECK_DEVICES_CHANGE_WARNING_MSG ), WarningMessage.ERROR_MESSAGE ) );
			}
		}
		else
		{
			warnMsgsList.add( new WarningMessage( Language.getLocalCaption( Language.CHECK_NON_SELECTED_STREAMS_ERROR_MSG ), WarningMessage.ERROR_MESSAGE ) );
		}
		
		// Checking plugin setting
		for( IStreamSetting str : DataProcessingPluginRegistrar.getAllDataStreams() )
		{
			int[] processLocs = new int[] { DataProcessingPluginRegistrar.PROCESSING, DataProcessingPluginRegistrar.POSTPROCESSING };
			
			for( int processLoc : processLocs )
			{
				for( ILSLRecPluginDataProcessing process : DataProcessingPluginRegistrar.getDataProcessing( str, processLoc ) ) 
				{
					WarningMessage w = process.checkSettings();			
					String msg = w.getMessage();

					warnMsgsList.add( new WarningMessage( msg, w.getWarningType() ) );
				}
			}
		}
		
		if( this.ctrlOutputFile.isSavingData() )
		{
			LostWaitedThread.getInstance().wakeup();
			warnMsgsList.add( new WarningMessage( "Saving data. Wait for the process to finish.", WarningMessage.ERROR_MESSAGE ) );
		}
		
		List< CheckMessage > checklist = (List< CheckMessage >)ConfigApp.getProperty( ConfigApp.CHECKLIST_MSGS );
		for( CheckMessage msg : checklist )
		{
			if( msg.isEnable() )
			{
				WarningMessage w = msg.evaluateMessage();
				warnMsgsList.add( w );
			}
		}
		
		return warnMsgsList;
	}
	
	/**
	 * Wait to start message
	 * 
	 * @throws Exception
	 */
	private synchronized void waitStartCommand() throws Exception
	{
		while( !this.ctrlOutputFile.isReadyInputStreams() )
		{
			synchronized( this )
			{
				super.wait( 100L );
			}
		}
		
		if ( this.isWaitingForStartCommand )
		{			
			GuiManager.getInstance().setAppState( AppState.State.WAIT, 0, false );
			
			this.ctrlOutputFile.toWorkSubordinates( new Tuple<String, String>( OutputDataFileHandler.ACTION_START_SYNC, "" ) );
			this.ctrSocket.toWorkSubordinates( null );
		}
		else
		{			
			this.ctrSocket.toWorkSubordinates( null );
			this.startRecord();
		}
		
		/*
		if( this.isWaitingForStartCommand )
		{
			while( !this.ctrlOutputFile.areReadySyncMarkThreads() )
			{
				System.out.println("CoreControl.waitStartCommand() NO areReadySyncMarkThreads");
				synchronized ( this )
				{
					super.wait( 100L );
				}
			}
		}
		//*/
		
		if( !this.syncPluginMet.isEmpty() )
		{
			for( LSLRecPluginSyncMethod plgSyncMet : this.syncPluginMet )
			{
				plgSyncMet.startThread();
			}
		}
		
		if( this.trial != null )
		{	
			this.trial.showTrialWindow();
			this.trial.startThread();
		}
	}

	/**
	 * Start recording data.
	 * 
	 * 
	 * @throws Exception
	 */
	private synchronized void startRecord() throws Exception
	{
		ExceptionDialog.showMessageDialog( new ExceptionMessage(  new Throwable( "Start recording" )
																	, "Start"
																	, ExceptionMessage.INFO_MESSAGE)
																, true, false);
		
		GuiManager.getInstance().StartSessionTimer();
		
		GuiManager.getInstance().setAppState( AppState.State.RUN, 0, false );
		
		if( this.beep == null )
		{
			try
			{
				this.beep = new BeepSound();
				this.beep.startThread();
			}
			catch (Exception | Error e)
			{
			}
		}
		
		if( this.beep != null )
		{
			this.beep.play();
		}
		
		this.isRecording = true;
		
		this.ctrlOutputFile.toWorkSubordinates( new Tuple< String, OutputFileFormatParameters >( OutputDataFileHandler.ACTION_START_RECORD 
																								,  null) );
		
		this.ctrlOutputFile.setEnableSaveSyncMark( true );
		
		if( this.SpecialMarker == null )
		{
			this.SpecialMarker =  new SyncMarker( RegisterSyncMessages.getSyncMark( RegisterSyncMessages.INPUT_START )
												, System.nanoTime() / 1e9D );
		}

		this.ctrlOutputFile.toWorkSubordinates( new Tuple< String, SyncMarker >( OutputDataFileHandler.ACTION_SET_MARK 
												, this.SpecialMarker ) );
		
		this.SpecialMarker = null;
	}
					
	/**
	 * Stop recording.
	 * 
	 * @throws Exception
	 */
	/*
	public void stopWorking( ) throws Exception
	{	
		synchronized ( this )
		{
			GuiManager.getInstance().getAppUI().getGlassPane().setVisible( true );
			
			ExceptionDialog.showMessageDialog( new ExceptionMessage(  new Throwable( "Stop recording" )
																					, "Stop"
																					, ExceptionMessage.INFO_MESSAGE)
																				, true, false);
			super.notify();
		}		
	}
	//*/	
	public void stopWorking( ) throws Exception
	{	
		synchronized ( this.lock )
		{
			if( this.stopThread == null )
			{
				GuiManager.getInstance().getAppUI().getGlassPane().setVisible( true );
				
				ExceptionDialog.showMessageDialog( new ExceptionMessage(  new Throwable( "Stop recording" )
																						, "Stop"
																						, ExceptionMessage.INFO_MESSAGE)
																					, true, false);
				
				// To avoid deadlock
				this.stopThread = new StopWorkingThread();
				this.stopThread.setName( this.stopThread.getClass().getCanonicalName() );
				this.stopThread.startThread();
			}
		}		
	}

	/**
	 * Register and processing the notification.
	 */
	public synchronized void eventNotification( IHandlerMinion subordinate, final EventInfo event)
	{
		this.notifiedEventHandler.registreNotification( event );
		
		//this.notifiedEventHandler.treatEvent();
		
		// To avoid a block due to notifiedEventHandler is processing.
		Thread t = new Thread()
		{
			@Override
			public void run() 
			{
				super.setName( "coreControl-eventNotification.treatEvent" );				
				notifiedEventHandler.treatEvent();
			}
		};
		
		t.start();
	}

	/**
	 * 
	 * @return True if OutputDataFileHandler is saving data; otherwise false.
	 */
	public boolean isDoingSomething()
	{
		boolean doing = this.isRecording;
		
		if( !doing && this.ctrlOutputFile != null )
		{
			doing = this.ctrlOutputFile.isSavingData();
		}
		
		return doing;
	}
	
	public boolean isRecording()
	{
		return this.isRecording;
	}
		
	/**
	 * 	
	 * @param close
	 */
	public void closeWhenDoingNothing( )
	{
		this.closeWhenDoingNothing = true;
		
		if( !this.isDoingSomething() )
		{
			this.exitLSLRecorder();
		}
		else
		{
			if( this.getOutFileStates() != 0 )
			{
				this.startCloseTimer();
			}
		}
	}
	
	private void exitLSLRecorder()
	{
		super.stopThread( IStoppableThread.FORCE_STOP );
		
		System.exit( 0 );
	}
	
	public boolean isClosing()
	{
		return this.closeWhenDoingNothing;
	}
	
	private void startCloseTimer()
	{
		Thread t = new Thread()
		{
			public void run() 
			{
				try 
				{
					sleep(  5 * 60 * 1000L );
				}
				catch (InterruptedException e) 
				{
				}
				finally 
				{
					closeTimerOver();					
				}
			}
		};
		
		t.setName( this.getClass().getSimpleName() + "-startCloseTimer");
		t.start();
	}
	
	private int getOutFileStates()
	{
		int ok = 0;
		
		List< State > STATES = this.ctrlOutputFile.getOutFileState();
		Iterator< State > itStates = STATES.iterator();
		
		while( ok == 0 && itStates.hasNext() )
		{
			State state = itStates.next();
			
			if( state.equals( State.BLOCKED ) )
			{
				ok = -1;
			}
			else if( state.equals( State.TIMED_WAITING ) || state.equals( State.WAITING ) )
			{
				ok = 1;
			}
		}
		
		return ok;
	}
	
	private void closeTimerOver()
	{
		if( this.getOutFileStates() != 0 )
		{		
			int actionDialog = GuiManager.getInstance().forceQuitDialog();
			
			if ( actionDialog == JOptionPane.YES_OPTION )
			{
				//System.exit( 0 );
				this.exitLSLRecorder();
			}
			else
			{
				this.startCloseTimer();
			}
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private SocketInformations getSocketStreamingInformations()
	{
		SocketInformations infos = new SocketInformations();

		Set< String > syncMet = ( Set< String > )ConfigApp.getProperty( ConfigApp.SELECTED_SYNC_METHOD );
		
		if( syncMet.contains( SyncMethod.SYNC_SOCKET ) )
		{
			Set< String > SOCKETS = (Set< String > )ConfigApp.getProperty( ConfigApp.SERVER_SOCKET );

			for( String socket : SOCKETS )
			{
				//Socket information
				String[] socketInfo = socket.split( ":" );

				String protocol = socketInfo[ 0 ];
				String ip = socketInfo[ 1 ];
				int port = new Integer( socketInfo[ 2 ] );

				int protocol_type = SocketSetting.TCP_PROTOCOL;					
				if( protocol.equals( "UDP" ) )
				{
					protocol_type = SocketSetting.UDP_PROTOCOL;
				}

				SocketSetting info = new SocketSetting( protocol_type, ip, port);

				infos.setServerInformation( new SocketParameters( info, SocketParameters.SOCKET_CHANNEL_IN ) );

				//In-out Messages
				Map< String, Integer > cmdTable = RegisterSyncMessages.getSyncMessagesAndMarks();
				
				for( String command : cmdTable.keySet() )
				{
					Integer commandType = cmdTable.get( command );

					infos.addInputCommands( command, commandType );
				}
			} 
		} 

		return infos;
	}
	
	//*
	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{}

	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{}

	@Override
	protected void runInLoop() throws Exception 
	{
		synchronized( this )
		{
			super.wait();
		}
	}
	
	@Override
	protected void runExceptionManager( Throwable e) 
	{
		if( !( e instanceof InterruptedException ) )
		{
			e.printStackTrace();
			
			ExceptionMessage msg = new ExceptionMessage( e, "Exception in " + getClass().getSimpleName(), ExceptionMessage.ERROR_MESSAGE );
			ExceptionDialog.showMessageDialog( msg, true, true );
		}
	}
	//*/
	
	private void stopWorkingThreadEnd()
	{
		synchronized ( this.lock )
		{
			if( this.stopThread != null )
			{
				if( !this.stopThread.getState().equals( Thread.State.TERMINATED ) )
				{
					this.stopThread.stopThread( IStoppableThread.FORCE_STOP );
				}
				
				this.stopThread = null;
				
				//ExceptionDialog.closeLogFile();
			}
		}
	}

	///////////////////////////////////////
	//
	// To avoid deadlock
	//
	
	private class StopWorkingThread extends AbstractStoppableThread 
	{
		//
		// To avoid deadlock
		//

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
			if ( isRecording 
					|| isWaitingForStartCommand )
			{	
				if( !syncPluginMet.isEmpty() )
				{
					for( LSLRecPluginSyncMethod plgSyncMet : syncPluginMet )
					{
						plgSyncMet.stopThread( IStoppableThread.FORCE_STOP );
					}
					
					syncPluginMet.clear();
				}
				
				if( trial != null )
				{					
					trial.stopThread( IStoppableThread.FORCE_STOP );
					trial.disposeTrialWindow();
				}
				
				isRecording = false;
				isWaitingForStartCommand = false;
				
				GuiManager.getInstance().setAppState( AppState.State.STOPPING, 0, false );
				
				if( beep == null )
				{
					try
					{
						beep = new BeepSound();
						beep.startThread();
					}
					catch (Exception | Error e)
					{
					}
				}
				
				if( beep != null )
				{
					beep.play();
				}
				
				//notifiedEventHandler.interruptProcess();
				notifiedEventHandler.clearEvent();

				ctrSocket.deleteSubordinates( IStoppableThread.FORCE_STOP );

				GuiManager.getInstance().restoreGUI();
				
				if( writingTestTimer != null )
				{
					//writingTestTimer.stop();
					writingTestTimer.stopThread(  IStoppableThread.FORCE_STOP );
					//writingTestTimer = null;
				}

				if ( ctrlOutputFile != null)
				{
					while( socketMsgDelayCal.isCalculating() )
					{		
						try 
						{
							super.wait( 100L );
						} 
						catch (Exception e) 
						{
						}
					}

					try
					{	
						String key = RegisterSyncMessages.INPUT_STOP;

						ctrlOutputFile.setBlockingStartWorking( true );

						if( SpecialMarker == null )
						{
							SpecialMarker = new SyncMarker( RegisterSyncMessages.getSyncMark( key )
									, System.nanoTime() / 1e9D );
						}

						ctrlOutputFile.toWorkSubordinates( new Tuple< String, SyncMarker>( ctrlOutputFile.ACTION_SET_MARK, SpecialMarker ) );

						Thread.sleep( 10L );

						ctrlOutputFile.setBlockingStartWorking( false );

						ctrlOutputFile.setEnableSaveSyncMark( false );

						ctrlOutputFile.deleteSubordinates( IStoppableThread.STOP_IN_NEXT_LOOP );

						if( ctrlOutputFile.isSavingData() )
						{
							GuiManager.getInstance().setAppState( AppState.State.SAVING, 0, false );
						}
						else
						{
							GuiManager.getInstance().setAppState( AppState.State.STOP, 0, true );
						}
					}
					catch (Exception localException) 
					{
						localException.printStackTrace();
						GuiManager.getInstance().setAppState( AppState.State.NONE, 0, false );
					}
					catch (Error localError) 
					{
						localError.printStackTrace();
					}
				}

				SpecialMarker = null;
				
				GuiManager.getInstance().refreshDataStreams();
			}
		}
		
		@Override
		protected void runExceptionManager( Throwable e) 
		{
			if( !( e instanceof InterruptedException ) )
			{
				e.printStackTrace();
				
				ExceptionMessage msg = new ExceptionMessage( e, "Exception in " + getClass().getSimpleName(), ExceptionMessage.ERROR_MESSAGE );
				ExceptionDialog.showMessageDialog( msg, true, true );
				
			}
		}
		
		@Override
		protected void targetDone() throws Exception 
		{	
			super.targetDone();
			
			super.stopThread = true;
		}
		
		@Override
		protected void cleanUp() throws Exception 
		{
			super.cleanUp();

			System.gc();
			
			if( closeWhenDoingNothing 
					&& !isDoingSomething() )
			{
				System.exit( 0 );
			}
			
			stopWorkingThreadEnd();
		}
		
	}
}