/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
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

package lslrec.controls;

import lslrec.auxiliar.tasks.INotificationTask;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.config.ConfigApp;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.config.language.Language;
import lslrec.controls.messages.AppState;
import lslrec.controls.messages.EventInfo;
import lslrec.controls.messages.RegisterSyncMessages;
import lslrec.controls.messages.SocketInformations;
import lslrec.dataStream.binary.input.plotter.DataPlotter;
import lslrec.dataStream.binary.input.plotter.StringPlotter;
import lslrec.dataStream.binary.input.writer.StreamBinaryHeader;
import lslrec.dataStream.family.lsl.LSL;
import lslrec.dataStream.family.lsl.LSLUtils;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.setting.DataStreamSetting;
import lslrec.dataStream.setting.MutableDataStreamSetting;
import lslrec.dataStream.sync.SyncMarker;
import lslrec.dataStream.sync.SyncMethod;
import lslrec.exceptions.SettingException;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionDictionary;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.controls.messages.EventType;
import lslrec.gui.GuiManager;
import lslrec.gui.dataPlot.CanvasStreamDataPlot;
import lslrec.gui.dialog.Dialog_Password;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.sync.LSLRecPluginSyncMethod;
import lslrec.plugin.lslrecPlugin.trial.ILSLRecPluginTrial;
import lslrec.plugin.lslrecPlugin.trial.LSLRecPluginTrial;
import lslrec.plugin.register.DataProcessingPluginRegistrar;
import lslrec.plugin.register.TrialPluginRegistrar;
import lslrec.sockets.info.StreamInputMessage;
import lslrec.sockets.info.SocketSetting;
import lslrec.sockets.info.StreamSocketProblem;
import lslrec.sockets.SocketMessageDelayCalculator;
import lslrec.sockets.info.SocketParameters;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;
import lslrec.auxiliar.WarningMessage;
import lslrec.auxiliar.extra.Tuple;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.FileSystemException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.UIManager;

public class CoreControl extends Thread implements IHandlerSupervisor
{
	private SocketInformations streamPars = null;

	private SocketHandler ctrSocket = null;

	private OutputDataFileHandler ctrlOutputFile = null;
	private DataPlotter ctrLSLDataPlot = null;
	private StringPlotter ctrLSLDataStringPlot = null;

	private NotifiedEventHandler notifiedEventHandler = null;

	private boolean isWaitingForStartCommand = false;
	private boolean isRecording = false;

	private static CoreControl core = null;

	private GuiManager managerGUI;

	private WarningMessage warnMsg = null;

	private boolean showWarningEvent = true;
	
	private boolean closeWhenDoingNothing = false;
	
	private boolean isActiveSpecialInputMsg = false;
	
	private Timer writingTestTimer;
	
	private SyncMarker SpecialMarker = null;

	private SocketMessageDelayCalculator socketMsgDelayCal = null;
	
	private StopWorkingThread stopThread = null;
	
	private int savingDataProgress = 0;
	
	private volatile String encryptKey = "";
	
	private LSLRecPluginTrial trial = null;
	private JFrame trialWindows = null;
		
	/**
	 * Create main control unit.
	 * 
	 * @throws Exception - Error in subordinates. 
	 */
	private CoreControl() throws Exception
	{	
		this.setName( this.getClass().getSimpleName() );
		
		this.createControlUnits();
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

	/**
	 * Create the subordinate controls.
	 * 
	 * @throws Exception
	 */
	private void createControlUnits() throws Exception
	{
		this.managerGUI = GuiManager.getInstance(); // GUI manager

		// socket handler
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
		this.socketMsgDelayCal = new SocketMessageDelayCalculator( ConfigApp.DEFAULT_NUM_SOCKET_PING );
		this.socketMsgDelayCal.taskMonitor( this.ctrlOutputFile );
		this.socketMsgDelayCal.startThread();

		// Notification control thread
		this.notifiedEventHandler = new NotifiedEventHandler();
		this.notifiedEventHandler.setName( this.notifiedEventHandler.getClass().getName() );
		this.notifiedEventHandler.startThread();
	}

	/**
	 * Delete plot thread.
	 */
	public void disposeLSLDataPlot()
	{
		if (this.ctrLSLDataPlot != null)
		{
			try
			{
				this.ctrLSLDataPlot.stopThread( IStoppableThread.FORCE_STOP );
			}
			catch (Exception e) 
			{
				if( !this.ctrLSLDataPlot.getState().equals( Thread.State.TERMINATED ) )
				{
					this.ctrLSLDataPlot.stop();
				}
			}
			catch ( Error e)
			{
				if( !this.ctrLSLDataPlot.getState().equals( Thread.State.TERMINATED ) )
				{
					this.ctrLSLDataPlot.stop();
				}
			}
			
			this.ctrLSLDataPlot = null;
		}
	}
	
	/**
	 * Delete string plot thread.
	 */
	public void disposeLSLDataStringPlot()
	{
		if (this.ctrLSLDataStringPlot != null)
		{
			try
			{
				this.ctrLSLDataStringPlot.stopThread( IStoppableThread.FORCE_STOP );
			}
			catch (Exception e) 
			{
				if( !this.ctrLSLDataStringPlot.getState().equals( Thread.State.TERMINATED ) )
				{
					this.ctrLSLDataStringPlot.stop();
				}
			}
			catch ( Error e)
			{
				if( !this.ctrLSLDataStringPlot.getState().equals( Thread.State.TERMINATED ) )
				{
					this.ctrLSLDataStringPlot.stop();
				}
			}
			
			this.ctrLSLDataStringPlot = null;
		}
	}

	/**
	 * Create a plot.
	 *  
	 * @param PlotPanel 	-> plot panel.
	 * @param lslSetting	-> LSL setting to plot data.
	 */
	public void createLSLDataPlot( JPanel PlotPanel, DataStreamSetting lslSetting )
	{
		try
		{
			this.disposeLSLDataStringPlot();
			this.disposeLSLDataPlot(); // Delete previous plot
			
			PlotPanel.setVisible( false );
			PlotPanel.removeAll();
						
			// Get alive LSL streaming
			LSL.StreamInfo[] results = LSL.resolve_streams();

			LSL.StreamInfo inletInfo = null;

			// Look for the LSL streaming 
			for (int i = 0; i < results.length && inletInfo == null; i++)
			{
				LSL.StreamInfo info = results[i];
				if ( info.uid().equals( lslSetting.getUID() ) )
				{
					inletInfo = results[i];
				}
			}

			if ( inletInfo != null)
			{
				// Set sampling rate
				double frq = inletInfo.nominal_srate();

				// Data plot queue length								
				int queueLength = ((int)(5.0D * frq)) * lslSetting.getChunkSize();
				if (queueLength < 10)
				{
					queueLength = 100;
				}

				if( inletInfo.channel_format() != LSLUtils.string )
				{	
					CanvasStreamDataPlot LSLCanvaPlot = new CanvasStreamDataPlot( queueLength ); 
					
					LSLCanvaPlot.clearData();
					LSLCanvaPlot.clearFilters();
	
					PlotPanel.add( LSLCanvaPlot, BorderLayout.CENTER );
					
					// Plot data
					this.ctrLSLDataPlot = new DataPlotter( LSLCanvaPlot, lslSetting );
					this.ctrLSLDataPlot.startThread();
				}
				else
				{
					JTextPane log = new JTextPane();
					
					PlotPanel.add( new JScrollPane( log ), BorderLayout.CENTER );
					
					// String data plot
					this.ctrLSLDataStringPlot = new StringPlotter( log, lslSetting );
					this.ctrLSLDataStringPlot.startThread();
				}
				
				PlotPanel.setVisible( true );
			}
			else
			{
				//JOptionPane.showMessageDialog( appUI.getInstance(), Language.getLocalCaption( Language.MSG_LSL_PLOT_ERROR ), Language.getLocalCaption( Language.DIALOG_ERROR ), JOptionPane.ERROR_MESSAGE);
				
				Exception ex = new Exception( Language.getLocalCaption( Language.MSG_LSL_PLOT_ERROR ) );
												
				ExceptionMessage msg = new ExceptionMessage( ex, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionDictionary.ERROR_MESSAGE ); 
				ExceptionDialog.showMessageDialog( msg, true, false );
			}
		}
		catch (Exception localException) 
		{
			localException.printStackTrace();
			
			ExceptionMessage msg = new ExceptionMessage( localException, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionDictionary.ERROR_MESSAGE ); 
			ExceptionDialog.showMessageDialog( msg, true, true );
		}
		catch (Error localError) 
		{
			localError.printStackTrace();
			
			ExceptionMessage msg = new ExceptionMessage( localError, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionDictionary.ERROR_MESSAGE ); 
			ExceptionDialog.showMessageDialog( msg, true, true );
		}
	}

	/**
	 * Start to record data
	 */
	public synchronized void startWorking( boolean testWriting )
	{
		try
		{	
			System.gc(); // Clean memory

			this.warnMsg = new WarningMessage(); // To check setting
			
			this.disposeLSLDataPlot(); // Delete plots.
            this.disposeLSLDataStringPlot();
			
			this.managerGUI.setAppState( AppState.PREPARING, 0, false );
			
			this.ctrlOutputFile.setEnableSaveSyncMark( false );
			
			// Check settings
			this.checkSettings();
			if (this.warnMsg.getWarningType() == WarningMessage.ERROR_MESSAGE )
			{	
				throw new SettingException( this.warnMsg.getMessage() );
			}

			if (this.warnMsg.getWarningType() == WarningMessage.WARNING_MESSAGE 
					&& !testWriting 
					&& !ConfigApp.isTesting() )
			{
				String[] opts = { UIManager.getString( "OptionPane.yesButtonText" ), 
						UIManager.getString( "OptionPane.noButtonText" ) };

				int actionDialog = JOptionPane.showOptionDialog( this.managerGUI.getAppUI(), this.warnMsg.getMessage() + "\n"
										+ Language.getLocalCaption( Language.CONTINUE_TEXT) + "?" 
										, Language.getLocalCaption( Language.MSG_WARNING ), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE 
										, null, opts, opts[1]);

				if ( actionDialog == JOptionPane.CANCEL_OPTION 
						|| actionDialog == JOptionPane.NO_OPTION 
						|| actionDialog == JOptionPane.CLOSED_OPTION )
				{
					this.isRecording = false;
					this.managerGUI.setAppState( AppState.STOP, 0, false );
					this.managerGUI.restoreGUI();
					this.managerGUI.refreshLSLDevices();

					return;
				}
			}			
			
			//Create In-Out sockets
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
			
			////Output data file
			
			boolean isSyncLSL = false;
			
			if (this.ctrlOutputFile != null )
			{						
				String file = ConfigApp.getProperty( ConfigApp.OUTPUT_FILE_NAME ).toString();

				HashSet< MutableDataStreamSetting > deviceIDs = (HashSet< MutableDataStreamSetting >)ConfigApp.getProperty( ConfigApp.LSL_ID_DEVICES );

				HashSet< MutableDataStreamSetting > DEV_ID = new HashSet< MutableDataStreamSetting >();
				Map< MutableDataStreamSetting, LSLRecPluginDataProcessing > DataProcesses = new HashMap<MutableDataStreamSetting, LSLRecPluginDataProcessing >();
				for( MutableDataStreamSetting dev : deviceIDs )
				{
					if( dev.isSelected() )
					{
						DEV_ID.add( dev );
						
						LSLRecPluginDataProcessing process = null;
						for( ILSLRecPluginDataProcessing pr : DataProcessingPluginRegistrar.getDataProcessing( dev ) )
						{
							process = pr.getProcessing( dev, process );
							process.loadProcessingSettings( pr.getSettings() );
						}
						
						DataProcesses.put( dev, process );
					}
					else
					{
						if( ConfigApp.getProperty( ConfigApp.SELECTED_SYNC_METHOD ).toString().equals( SyncMethod.SYNC_LSL ) 
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
						if( fpath == null || ( !fpath.exists() &&!fpath.mkdirs() ) )
						{
							throw new FileSystemException( "Folder " + folder + " not created." );
						}
					}

					ParameterList StreamPars = new ParameterList();
					
					Parameter lslSetting = new Parameter( this.ctrlOutputFile.PARAMETER_LSL_SETTING, DEV_ID );
					StreamPars.addParameter( lslSetting );
					
					Parameter datProcesses = new Parameter( this.ctrlOutputFile.PARAMETER_DATA_PROCESSING, DataProcesses );
					StreamPars.addParameter( datProcesses );
					
					Parameter savePprocessedDat = new Parameter( this.ctrlOutputFile.PARAMETER_SAVE_DATA_PROCESSING, (Boolean)ConfigApp.getProperty( ConfigApp.OUTPUT_SAVE_DATA_PROCESSING ) );
					StreamPars.addParameter( savePprocessedDat );
					
					Parameter writingTest = new Parameter( this.ctrlOutputFile.PARAMETER_WRITE_TEST, testWriting );
					StreamPars.addParameter( writingTest );
					
					OutputFileFormatParameters outFormat = new OutputFileFormatParameters();
					outFormat.setParameter( OutputFileFormatParameters.OUT_FILE_NAME, file );
					outFormat.setParameter( OutputFileFormatParameters.ZIP_ID, ConfigApp.getProperty( ConfigApp.OUTPUT_COMPRESSOR ).toString() );
					outFormat.setParameter( OutputFileFormatParameters.OUT_FILE_FORMAT, (String)ConfigApp.getProperty( ConfigApp.OUTPUT_FILE_FORMAT ) );
					outFormat.setParameter( OutputFileFormatParameters.PARALLELIZE, (Boolean)ConfigApp.getProperty( ConfigApp.OUTPUT_PARALLELIZE ) );
					outFormat.setParameter( OutputFileFormatParameters.ENCRYPT_KEY, this.encryptKey );
					this.encryptKey = "";
					
					
					String nodeId = DataStreamSetting.ID_SOCKET_MARK_INFO_LABEL;
					String nodeText = "";
					
					Map< String, Integer > MARKS = RegisterSyncMessages.getSyncMessagesAndMarks();
					
					for( String idMark : MARKS.keySet() )
					{
						Integer v = MARKS.get( idMark );
						nodeText += idMark + "=" + v + StreamBinaryHeader.HEADER_BINARY_SEPARATOR;
					}		
					
					((Map< String, String >)( outFormat.getParameter( OutputFileFormatParameters.RECORDING_INFO ).getValue()) ).put( nodeId, nodeText );
										
					nodeId = DataStreamSetting.ID_RECORD_GENERAL_DESCRIPTION;
					nodeText = ConfigApp.getProperty( ConfigApp.OUTPUT_FILE_DESCR ).toString();
					((Map< String, String >)( outFormat.getParameter( OutputFileFormatParameters.RECORDING_INFO ).getValue() ) ).put( nodeId, nodeText );
					
					Parameter outFileFormat = new Parameter( this.ctrlOutputFile.PARAMETER_OUTPUT_FORMAT, outFormat );
					StreamPars.addParameter( outFileFormat );
					
					minionPars.setMinionParameters( this.ctrlOutputFile.ID, StreamPars );

					this.ctrlOutputFile.addSubordinates( minionPars );						
				}
			}
			
			LSLRecPluginSyncMethod syncMet = SyncMethod.getSyncPlugin( ConfigApp.getProperty( ConfigApp.SELECTED_SYNC_METHOD ).toString() );

			if( syncMet != null )
			{
				//TODO
				
			}
			
			this.isActiveSpecialInputMsg = (Boolean)ConfigApp.getProperty( ConfigApp.IS_ACTIVE_SPECIAL_INPUTS );
			
			if( !testWriting )
			{
				this.isWaitingForStartCommand = this.isActiveSpecialInputMsg
												&& 
												( this.streamPars.getInputCommands().containsKey( RegisterSyncMessages.INPUT_START )
														|| isSyncLSL );
			}
			else
			{
				this.isWaitingForStartCommand = false;
				
				this.writingTestTimer = new Timer( ConfigApp.WRITING_TEST_TIME, new ActionListener() 
				{			
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						managerGUI.stopTest();						
					}
				} );
				
				this.writingTestTimer.start();
			}
			
			ILSLRecPluginTrial trialPl = TrialPluginRegistrar.getNewInstanceOfTrialPlugin();
			
			if( trialPl != null )
			{
				this.trial = trialPl.getGUIExperiment();
				
				if( this.trial != null )
				{
					if( this.trialWindows != null )
					{
						this.trialWindows.dispose();
					}
					
					this.trialWindows = this.trial.getWindonw();
					this.trialWindows.setVisible( false );
					this.trialWindows.setExtendedState( this.trialWindows.getExtendedState() | JFrame.MAXIMIZED_BOTH );
					
					this.trial.loadSettings( trialPl.getSettings() );
					
					this.trial.taskMonitor( this.ctrlOutputFile );
				}
			}
			
			this.waitStartCommand();
		}
		catch (Exception | Error e )
		{			
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
				this.managerGUI.setAppState( AppState.STOP, 0, false );
			}
			else
			{
				this.managerGUI.setAppState( AppState.SAVING, 0, true );
			}
			
			this.managerGUI.restoreGUI();
			this.managerGUI.refreshLSLDevices();
			this.isWaitingForStartCommand = false;
			this.showWarningEvent = true;
						
			try 
			{
				ExceptionMessage ex = new ExceptionMessage( e,  Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionDictionary.ERROR_MESSAGE );
				ExceptionDialog.showMessageDialog( ex, true, true );
				
				e.printStackTrace();
			}
			catch (Exception e1) 
			{
				e1.printStackTrace();
			}			
		}
	}
	
	/**
	 * 
	 * Check Settings.
	 *   
	 */
	private void checkSettings() 
	{
		this.warnMsg.setMessage( "", WarningMessage.OK_MESSAGE );
		
		WarningMessage outFileMsg = this.ctrlOutputFile.checkParameters();
		WarningMessage socketMsg = this.ctrSocket.checkParameters();
		
		ILSLRecPluginTrial trial = TrialPluginRegistrar.getNewInstanceOfTrialPlugin();
		
		if( trial != null )
		{
			WarningMessage trW = trial.checkSettings();
			
			this.warnMsg.addMessage( trW.getMessage(), trW.getWarningType() );
		}
		
		this.warnMsg.addMessage( outFileMsg.getMessage(), outFileMsg.getWarningType() );
		this.warnMsg.addMessage( socketMsg.getMessage(), socketMsg.getWarningType() );

		boolean specialInMsg = (Boolean)ConfigApp.getProperty( ConfigApp.IS_ACTIVE_SPECIAL_INPUTS );
		if( !specialInMsg )
		{
			this.warnMsg.addMessage( Language.getLocalCaption( Language.CHECK_SPECIAL_IN_WARNING_MSG ), WarningMessage.WARNING_MESSAGE );
		}
		
		if( ConfigApp.getProperty( ConfigApp.SELECTED_SYNC_METHOD ).equals( SyncMethod.SYNC_NONE ) )
		{
			this.warnMsg.addMessage( Language.getLocalCaption( Language.CHECK_SYNC_METHOD_WARNING_MSG ), WarningMessage.WARNING_MESSAGE );
		}
		
		if( (Boolean)ConfigApp.getProperty( ConfigApp.OUTPUT_ENCRYPT_DATA ) )
		{
			Dialog_Password pass = new Dialog_Password( this.managerGUI.getAppUI(), Language.getLocalCaption( Language.ENCRYPT_KEY_TEXT ) );			
			
			pass.setLocationRelativeTo( this.managerGUI.getAppUI() );
			pass.setVisible( true );
			
			while( pass.getState() == Dialog_Password.PASSWORD_INCORRECT )
			{
				pass.setMessage( pass.getPasswordError()  + " " + Language.getLocalCaption( Language.REPEAT_TEXT ) + ".");
				pass.setVisible( true );
			}
			
			if( pass.getState() != Dialog_Password.PASSWORD_OK )
			{
				this.warnMsg.addMessage( Language.getLocalCaption( Language.PROCESS_TEXT ) + " " + Language.getLocalCaption( Language.CANCEL_TEXT )
										, WarningMessage.ERROR_MESSAGE );
			}
			
			this.encryptKey = pass.getPassword();
			
			if( this.encryptKey == null )
			{
				this.encryptKey = "";
			}
		}
		
		HashSet< DataStreamSetting > lslPars = (HashSet< DataStreamSetting >)ConfigApp.getProperty( ConfigApp.LSL_ID_DEVICES );
				
		LSL.StreamInfo[] results = LSL.resolve_streams();
				
		boolean existSelectedSyncLSL = false;
		
		if( results.length >= 0 )
		{
			boolean selected = false;
			
			// Check if one stream is selected.
			for( DataStreamSetting lslcfg : lslPars )
			{
				selected = lslcfg.isSelected();

				if( selected )
				{	
					selected = false;
					for( int i = 0; i < results.length && !selected; i++ )
					{
						selected = results[ i ].uid().equals( lslcfg.getUID() );
					}
					
					if( selected )
					{
						break;
					}
				}
			}
					
			// Check if sync stream is selected.
			for( DataStreamSetting lslcfg : lslPars )
			{
				existSelectedSyncLSL = lslcfg.isSynchronationStream();

				if( existSelectedSyncLSL )
				{
					break;
				}
			}

			if( !selected )
			{
				this.warnMsg.addMessage( Language.getLocalCaption( Language.CHECK_NON_SELECT_LSL_ERROR_MSG ), WarningMessage.ERROR_MESSAGE );
			}
			
			this.warnMsg.addMessage( Language.getLocalCaption( Language.CHECK_LSL_CHUNCKSIZE_WARNING_MSG ), WarningMessage.WARNING_MESSAGE );
			
			if( ConfigApp.getProperty( ConfigApp.SELECTED_SYNC_METHOD ).equals( SyncMethod.SYNC_LSL ) && specialInMsg && !existSelectedSyncLSL )
			{
				this.warnMsg.addMessage( Language.getLocalCaption( Language.CHECK_SYNC_LSL_UNSELECTABLE_ERROR_MSG ), WarningMessage.ERROR_MESSAGE );
			}
			else if( existSelectedSyncLSL && !ConfigApp.getProperty( ConfigApp.SELECTED_SYNC_METHOD ).equals( SyncMethod.SYNC_LSL ) )
			{
				this.warnMsg.addMessage( Language.getLocalCaption( Language.CHECK_LSL_SYNC_STREAM_WARNING_MSG ), WarningMessage.WARNING_MESSAGE );
			}
			
			boolean change = false;
				
			for( int i = 0; i < results.length && !change; i++ )
			{
				LSL.StreamInfo stream = results[ i ];

				for( DataStreamSetting lslcfg : lslPars )
				{
					if( ( lslcfg.isSelected() || lslcfg.isSynchronationStream() )
							&& lslcfg.getStreamName().equals( stream.name() ) 
							&& lslcfg.getSourceID().equals( stream.source_id() ) )
					{
						change = !stream.uid().equals( lslcfg.getUID() ) ;
	
						if( change )
						{
							break;
						}
					}
				}
			}

			if( change )
			{
				this.warnMsg.addMessage( Language.getLocalCaption( Language.CHECK_LSL_DEVICES_CHANGE_WARNING_MSG ), WarningMessage.ERROR_MESSAGE );
			}
		}
		
		// Checking plugin setting
		for( ILSLRecPluginDataProcessing process : DataProcessingPluginRegistrar.getDataProcesses() )
		{
			if( !DataProcessingPluginRegistrar.getDataStreams( process ).isEmpty() )
			{
				WarningMessage w = process.checkSettings();
				this.warnMsg.addMessage( w.getMessage(), w.getWarningType() );
			}
		}
		
		if( this.ctrlOutputFile.isSavingData() )
		{
			this.warnMsg.addMessage( "Saving data. Wait for the process to finish.", WarningMessage.ERROR_MESSAGE );
		}
	}
	
	/**
	 * Wait to start message
	 * 
	 * @throws Exception
	 */
	private synchronized void waitStartCommand() throws Exception
	{
		if ( this.isWaitingForStartCommand )
		{			
			this.managerGUI.setAppState( AppState.WAIT, 0, false );
			
			this.ctrlOutputFile.toWorkSubordinates( new Tuple<String, String>( OutputDataFileHandler.ACTION_START_SYNC, "" ) );
			this.ctrSocket.toWorkSubordinates( null );
		}
		else
		{			
			this.ctrSocket.toWorkSubordinates( null );
			this.startRecord();
		}
		
		if( this.trial != null )
		{	
			this.trialWindows.setVisible( true );
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
		this.managerGUI.StartSessionTimer();
		
		this.managerGUI.setAppState( AppState.RUN, 0, false );
		
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
	public void stopWorking( ) throws Exception
	{	
		synchronized ( this )
		{
			if( this.stopThread == null )
			{
				this.stopThread = new StopWorkingThread();
				this.stopThread.setName( this.stopThread.getClass().getCanonicalName() );
				this.stopThread.startThread();
			}
		}		
	}
	
	private void stopWorkingThreadEnd()
	{
		synchronized ( this )
		{
			if( this.stopThread != null )
			{
				if( !this.stopThread.getState().equals( Thread.State.TERMINATED ) )
				{
					this.stopThread.stopThread( IStoppableThread.FORCE_STOP );
				}
				
				this.stopThread = null;
			}
		}
	}
	
	/*
	private void unparkOutputFileThread()
	{
		if( this.getOutFileStates() != 0 )
		{
			this.ctrlOutputFile.unparkOutputFile();
		}
	}
	*/

	/**
	 * Register and processing the notification.
	 */
	public synchronized void eventNotification( IHandlerMinion subordinate, final EventInfo event)
	{
		/*
		Thread t = new Thread()
		{
			@Override
			public void run() 
			{
				super.setName( "coreControl-eventNotification" );
				
				notifiedEventHandler.registreNotification( event );
				
				notifiedEventHandler.treatEvent();
			}
		};
		
		t.start();
		*/
		
		this.notifiedEventHandler.registreNotification( event );
		
		this.notifiedEventHandler.treatEvent();
	}

	/**
	 * Socket message manager.
	 * 
	 * @param EVENTS
	 * 
	 * @throws Exception
	 */
	private synchronized void eventSocketMessagesManager( List< EventInfo > EVENTS ) throws Exception
	{
		if ( ( EVENTS != null ) && ( !EVENTS.isEmpty() ) )
		{
			for (EventInfo event : EVENTS)
			{
				if (event.getEventType().equals( EventType.SOCKET_INPUT_MSG ) )
				{
					this.socketMsgDelayCal.CalculateMsgDelay( ( StreamInputMessage )event.getEventInformation() );
				}
				else if (event.getEventType().equals( EventType.SOCKET_CONNECTION_PROBLEM ))
				{
					StreamSocketProblem problem = (StreamSocketProblem)event.getEventInformation();

					String msg = problem.getProblemCause().getMessage();
										
					if ( msg == null || msg.isEmpty() )
					{
						msg = "Streaming connection problems";
					}

					//this.ctrSocket.removeClientStreamSocket( problem.getSocketAddress() );
					
					Exception ex = new Exception( msg );
					ExceptionMessage exmsg = new ExceptionMessage( ex, EventType.SOCKET_CONNECTION_PROBLEM, ExceptionDictionary.WARNING_MESSAGE );
					ExceptionDialog.showMessageDialog( exmsg, true, false );
					
					/*
					if( !ConfigApp.isTesting() )
					{
						final String copyMsg = msg;
						Thread t = new Thread()
						{
							@Override
							public void run() 
							{
								JOptionPane.showMessageDialog( guiManager.getInstance().getAppUI(), copyMsg, 
																EventType.SOCKET_CONNECTION_PROBLEM, 
																JOptionPane.WARNING_MESSAGE );
							}
						};
						
						t.start();						
					}
					else
					{
						this.managerGUI.addInputMessageLog( EventType.SOCKET_CONNECTION_PROBLEM + ": " + msg );
					}
					*/
				}
				else if (event.getEventType().equals(  EventType.SOCKET_CHANNEL_CLOSE ))
				{
					StreamSocketProblem problem = (StreamSocketProblem)event.getEventInformation();

					String msg = problem.getProblemCause().getMessage();
					if (msg.isEmpty())
					{
						msg = problem.getProblemCause().getCause().toString();
					}

					Exception ex = new Exception( msg );
					ExceptionMessage exmsg = new ExceptionMessage( ex, EventType.SOCKET_CHANNEL_CLOSE, ExceptionDictionary.WARNING_MESSAGE );
					ExceptionDialog.showMessageDialog( exmsg, true, false );
									
					/*
					if( !ConfigApp.isTesting() )
					{						
						final String copyMsg = msg;
						Thread t = new Thread()
						{
							@Override
							public void run() 
							{
								JOptionPane.showMessageDialog( guiManager.getInstance().getAppUI(), copyMsg, 
																EventType.SOCKET_CHANNEL_CLOSE, 
																JOptionPane.WARNING_MESSAGE );
							}
						};
						
						t.start();
					}
					else
					{
						this.managerGUI.addInputMessageLog( EventType.SOCKET_CHANNEL_CLOSE + ": " + msg );
					}
					*/
				}
			}
		}
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
		
		if( this.closeWhenDoingNothing && !this.isDoingSomething() )
		{
			System.exit( 0 );
		}
		else
		{
			if( this.getOutFileStates() != 0 )
			{
				//this.unparkOutputFileThread();
				this.startCloseTimer();
			}
		}
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
			String[] opts = { UIManager.getString( "OptionPane.yesButtonText" ), 
					UIManager.getString( "OptionPane.noButtonText" ) };
	
			int actionDialog = JOptionPane.showOptionDialog( managerGUI.getAppUI(), Language.getLocalCaption( Language.TOO_MUCH_TIME )				
					+ "\n" + Language.getLocalCaption( Language.FORCE_QUIT ) 
					+ "?", 
					Language.getLocalCaption( Language.MSG_WARNING )
					, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, 
					null, opts, opts[1]);
			
			if ( actionDialog == JOptionPane.YES_OPTION )
			{
				System.exit( 0 );
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

		if ( ( (String)ConfigApp.getProperty( ConfigApp.SELECTED_SYNC_METHOD ) ).equals( SyncMethod.SYNC_SOCKET ) )
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

	private String createMessage(String msg)
	{
		if (msg != null)
		{
			String[] special = { "\\t", "\\r", "\\n" };
			String[] speChars = { "\t", "\r", "\n" };
			for (int i = 0; i < special.length; i++)
			{
				String msgAux = "";
				String str = special[i];

				int index = 0;
				int init = 0;
				while (index >= 0)
				{
					index = msg.substring(init).indexOf( "\\" + str);
					if (index < 0)
					{
						msgAux = msgAux + msg.substring(init).replaceAll(new StringBuilder("\\").append(str).toString(), speChars[i]);
					}
					else
					{
						msgAux = msgAux + msg.substring(init, index);
						msgAux = msgAux.replaceAll( new StringBuilder( "\\" ).append(str).toString(), speChars[i] ) + str;
						init = index + str.length() + 1;

						if (init >= msg.length())
						{
							index = -1;
						}
					}
				}

				msg = msgAux;
			}
		}

		return msg;
	}

	public boolean controlNotifiedEventSemBlock()
	{
		return this.notifiedEventHandler.controlNotifySemphore.availablePermits() == 0;
	}


	///////////////////////////////////////
	//
	//

	private class NotifiedEventHandler extends AbstractStoppableThread implements ITaskMonitor
	{
		private LinkedHashMap<String, Object> eventRegister = new LinkedHashMap<String, Object>();
		private boolean treatEvent = true;

		private CoreControl.controlNotifiedManager ctrlManager = null;

		private Semaphore controlNotifySemphore = null;
		private Semaphore eventRegisterSemaphore = null;

		public NotifiedEventHandler()
		{
			this.controlNotifySemphore = new Semaphore(1, true);
			this.eventRegisterSemaphore = new Semaphore(1, true);
		}


		protected void preStopThread(int friendliness) throws Exception
		{}

		protected void postStopThread(int friendliness) throws Exception
		{
			try
			{
				this.eventRegisterSemaphore.acquire();
			}
			catch (Exception localException) 
			{}

			this.ctrlManager.stopThread( IStoppableThread.FORCE_STOP );
			this.ctrlManager = null;
			this.eventRegister.clear();

			if (this.eventRegisterSemaphore.availablePermits() < 1)
			{
				this.eventRegisterSemaphore.release();
			}
		}

		protected void runInLoop() throws Exception
		{
			if (this.eventRegister.size() == 0)
			{
				synchronized ( this )
				{
					try
					{
						super.wait();
					}
					catch( InterruptedException e)
					{						
					}
				}				
			}

			try
			{
				this.eventRegisterSemaphore.acquire();
			}
			catch (Exception localException ) 
			{}

			synchronized ( this.eventRegister )
			{		
				if (this.ctrlManager == null || this.ctrlManager.getState().equals( State.TERMINATED ) )
				{
					this.ctrlManager = new controlNotifiedManager( this.eventRegister );
					this.ctrlManager.taskMonitor( this );

					this.eventRegister.clear();

					this.ctrlManager.startThread();
				}
			}

			if (this.eventRegisterSemaphore.availablePermits() < 1)
			{
				this.eventRegisterSemaphore.release();
			}
		}

		public void registreNotification( EventInfo event )
		{
			try
			{
				this.controlNotifySemphore.acquire();
			}
			catch ( InterruptedException localInterruptedException ) 
			{}      


			String event_type = event.getEventType();
			Object event_Info = event.getEventInformation();
			
			try
			{
				this.eventRegisterSemaphore.acquire();
			}
			catch ( Exception localException ) 
			{}
			
			synchronized ( this.eventRegister )
			{
				//if ( this.eventRegister.size() > 0 )
				//{
					if ( event_type.equals( EventType.SOCKET_EVENTS ) )
					{
						List< EventInfo > storedEvents = ( List< EventInfo > )this.eventRegister.get( event_type );
						List< EventInfo > newEvents = ( List< EventInfo > )event_Info;
						Set< String > setRegisteredEvents = new HashSet< String >(); 
						
						if ( storedEvents != null )
						{
							Iterator< EventInfo > itEvent = storedEvents.iterator();

							while ( itEvent.hasNext() )
							{
								EventInfo e = ( EventInfo )itEvent.next();

								if ( !e.getEventType().equals( EventType.SOCKET_MSG_DELAY )
										&& setRegisteredEvents.contains( e.getEventType() ) )
								{
									itEvent.remove();
								}
								else
								{
									setRegisteredEvents.add( e.getEventType() );
								}
							}
						}
						
						Iterator<EventInfo> itNewEvent = newEvents.iterator();
						while ( itNewEvent.hasNext() )
						{
							EventInfo ev = itNewEvent.next();
							
							if( !ev.getEventType().equals( EventType.SOCKET_MSG_DELAY ) )
							{
								if ( setRegisteredEvents.contains( ev.getEventType() ) )
								{
									itNewEvent.remove();
								}
							}		
						}
						
						if (storedEvents != null)
						{
							storedEvents.addAll( newEvents );
						}
						else
						{
							storedEvents = newEvents;
						}

						event_Info = storedEvents;
					}

					if( event_type.equals( EventType.TEST_WRITE_TIME ) )
					{
						List ob = (List)this.eventRegister.get( event_type );
						
						if( ob == null )
						{
							ob = new ArrayList();
						}
						
						ob.add( event_Info );
						
						this.eventRegister.put( event_type, ob );
					}
					else
					{
						this.eventRegister.put(event_type, event_Info);
					}
				}
			//}

			if (this.eventRegisterSemaphore.availablePermits() < 1)
			{
				this.eventRegisterSemaphore.release();
			}

			if (this.controlNotifySemphore.availablePermits() < 1)
			{
				this.controlNotifySemphore.release();
			}
		}

		public void treatEvent()
		{
			/*
			if ( super.getState().equals( Thread.State.WAITING ) )
			{
				this.treatEvent = true;
				super.notify();
			}
			*/
			
			synchronized ( this )
			{
				this.treatEvent = true;
				super.notify();	
			}
		}

		public void interruptProcess()
		{
			this.treatEvent = false;
		}

		public void clearEvent()
		{
			try
			{
				this.eventRegisterSemaphore.acquire();
			}
			catch (Exception localException) 
			{}

			synchronized (this.eventRegister)
			{
				if (this.eventRegister.size() > 0)
				{
					this.eventRegister.clear();

					super.interrupt();
				}
			}

			if (this.eventRegisterSemaphore.availablePermits() < 1)
			{
				this.eventRegisterSemaphore.release();
			}
		}

		protected void runExceptionManager(Exception e)
		{
			if (!(e instanceof InterruptedException))
			{
				e.printStackTrace();
				
				/*
				JOptionPane.showMessageDialog( coreControl.this.managerGUI.getAppUI(), e.getMessage(), 
											"Exception in " + getClass().getSimpleName(),
											JOptionPane.ERROR_MESSAGE);
				*/
				
				ExceptionMessage msg = new ExceptionMessage( e, "Exception in " + getClass().getSimpleName(), ExceptionDictionary.ERROR_MESSAGE );
				ExceptionDialog.showMessageDialog( msg, true, true );
			}
		}

		public void taskDone(INotificationTask task) throws Exception
		{
			try
			{
				this.eventRegisterSemaphore.acquire();
			}
			catch (Exception localException) 
			{}
			
			if (this.eventRegisterSemaphore.availablePermits() < 1)
			{
				this.eventRegisterSemaphore.release();
			}
		}
	}


	////////////////////////////////
	//
	//
	private class controlNotifiedManager extends AbstractStoppableThread implements INotificationTask
	{
		private LinkedHashMap<String, Object> eventRegister = new LinkedHashMap<String, Object>();

		private ITaskMonitor monitor;

		public controlNotifiedManager( Map< String, Object > events )
		{
			if (events != null)
			{
				synchronized (events)
				{
					for (String event : events.keySet())
					{
						this.eventRegister.put( event, events.get( event ) );
					}
				}				
			}
			
			super.setName( this.getClass().getName() );
		}

		@Override
		protected void preStart() throws Exception
		{
			super.preStart();
		}


		@Override
		protected void preStopThread(int friendliness) throws Exception
		{}

		@Override
		protected void postStopThread(int friendliness)  throws Exception
		{
			this.eventRegister.clear();
			this.eventRegister = null;
		}

		@Override
		protected void runInLoop() throws Exception
		{
			if ( this.eventRegister.size() > 0 )
			{
				String event_type = (String)this.eventRegister.keySet().iterator().next();
				final Object eventObject = this.eventRegister.get( event_type );

				this.eventRegister.remove( event_type );

				if( event_type.equals( EventType.ALL_OUTPUT_DATA_FILES_SAVED ) )
				{
					managerGUI.setAppState( AppState.SAVED, 100, false );
					
					savingDataProgress = 0;
					//managerGUI.enablePlayButton( true );
					
					if( closeWhenDoingNothing && !isDoingSomething() )
					{
						System.exit( 0 );
					}
				}
				else if( event_type.equals( EventType.SAVING_OUTPUT_TEMPORAL_FILE ) )
				{	
					managerGUI.setAppState( AppState.SAVING, 0, true );
				}		
				else if( event_type.equals( EventType.SAVING_DATA_PROGRESS ) )
				{
					int val = -1;
					
					try
					{
						val = (Integer)eventObject;
					}
					catch (Exception e) 
					{
						val = -1;
					}
					
					if( val > savingDataProgress )
					{
						managerGUI.setAppState( AppState.SAVING, val, true );
					}
				}
				else if (event_type.equals( EventType.SOCKET_EVENTS ))
				{
					eventSocketMessagesManager( (List< EventInfo> )eventObject );
				}
				/*
				else if( event_type.equals( EventType.SOCKET_MSG_DELAY ) )
				{					
					NotificationTask not = new NotificationTask();
					not.setID( not.getID() + "-" + EventType.SOCKET_MSG_DELAY );
					not.addEvent( new EventInfo( not.getID(), EventType.INPUT_MARK_READY, eventObject ));
					not.taskMonitor( ctrlOutputFile );					
					not.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
					
					not.startThread();					
				}
				*/
				else if( event_type.equals( EventType.TEST_WRITE_TIME ) )
				{
					List< Tuple< String, List< Long > > > testValues = (List)eventObject;
					for( Tuple< String, List< Long > > times : testValues )
					{				
						WriteTestCalculator cal = new WriteTestCalculator(  times.x, times.y );
						cal.start();
					}
				}
				/*
				else if( event_type.equals( EventType.SOCKET_PING_END ) )
				{	
					sockMsgDelayCalculator = null;
				}
				*/
				else if( event_type.equals( EventType.INPUT_MARK_READY ) )
				{
					this.InputMarker( (SyncMarker) eventObject );
				}				
				else if (event_type.equals( EventType.PROBLEM ) )
				{
					/*
					Thread t = new Thread()
					{
						@Override
						public synchronized void run() 
						{
							try 
							{
								stopWorking( );
							}
							catch (Exception e) 
							{
								
							}
						}
					};
					
					t.setName( this.getClass().getSimpleName() + "-stopWorking" );
					
					t.start();
					*/
					
					try 
					{
						stopWorking( );
					}
					catch (Exception e) 
					{
						ExceptionMessage msg = new ExceptionMessage( e, "Stop Exception", ExceptionDictionary.ERROR_MESSAGE );
						ExceptionDialog.showMessageDialog( msg , true, true );
					}

					/*
					if( !ConfigApp.isTesting() )
					{
						JOptionPane.showMessageDialog(  coreControl.this.managerGUI.getAppUI(), 
														eventObject.toString(), 
														event_type, 
														JOptionPane.ERROR_MESSAGE);
					}
					else
					{
						managerGUI.addInputMessageLog( event_type + ": " + eventObject.toString());
					}
					*/
					
					Exception ex = new Exception( eventObject.toString() );
										
					if( eventObject instanceof Exception )
					{
						ex = (Exception)eventObject;
					}
					
					ExceptionMessage msg = new ExceptionMessage( ex, event_type, ExceptionDictionary.ERROR_MESSAGE );
					ExceptionDialog.showMessageDialog( msg, true, true );
					
				}
				else if (event_type.equals( EventType.WARNING ) )
				{
					if( showWarningEvent )
					{
						new Thread()
						{
							public void run()
							{
								Exception ex = new Exception( eventObject.toString() );
								
								if( eventObject instanceof Exception )
								{
									ex = (Exception)eventObject ;
								}
								
								ExceptionMessage msg = new ExceptionMessage( ex
																			, Language.getLocalCaption( Language.MSG_WARNING )
																			, ExceptionDictionary.WARNING_MESSAGE );
								
								ExceptionDialog.showMessageDialog( msg, true, false );
								
								/*
								if( !ConfigApp.isTesting() )
								{
									JOptionPane.showMessageDialog(   managerGUI.getAppUI(), 
											eventObject.toString(), 
											Language.getLocalCaption( Language.MSG_WARNING ), 
											JOptionPane.WARNING_MESSAGE);
								}
								else
								{
									managerGUI.addInputMessageLog( Language.getLocalCaption( Language.MSG_WARNING ) + ": " + eventObject.toString());
								}
								*/
							}
						}.start();
					}
				}
			}
		}
		
		private void InputMarker( SyncMarker mark )
		{			
			if( isRecording )
			{
				managerGUI.addInputMessageLog( mark.getMarkValue() + "\n");
			}
								
			if ( mark.getMarkValue() == RegisterSyncMessages.getSyncMark( RegisterSyncMessages.INPUT_STOP ) )
			{ 
				if( isActiveSpecialInputMsg )
				{
					SpecialMarker = mark;
					/*
					Thread t = new Thread()
					{
						@Override
						public synchronized void run() 
						{
							try 
							{
								stopWorking( );
							}
							catch (Exception e) 
							{
								
							}
						}
					};
					
					t.setName( this.getClass().getSimpleName() + "-stopWorking" );
					
					t.start();
					*/					
				
					try 
					{
						stopWorking( );
					}
					catch (Exception e) 
					{
						e.printStackTrace();
					}
					
				}
			}
			else if ( mark.getMarkValue() == RegisterSyncMessages.getSyncMark( RegisterSyncMessages.INPUT_START ) )
			{
				if( isActiveSpecialInputMsg 
						&& !isRecording 
						&&  isWaitingForStartCommand )
					{
						isWaitingForStartCommand = false;

						managerGUI.addInputMessageLog( mark.getMarkValue() + "\n");
						
						try
						{
							SpecialMarker = mark;
							startRecord();
						}
						catch (Exception e)
						{
							e.printStackTrace();
						}
					}
			}
		}

		@Override
		protected void targetDone() throws Exception
		{
			super.targetDone();

			this.stopThread = this.eventRegister.isEmpty();
		}

		@Override
		protected void runExceptionManager( Throwable e)
		{
			if (!(e instanceof InterruptedException))
			{
				e.printStackTrace();
								
				ExceptionMessage msg = new ExceptionMessage( e
															, "Exception in " + getClass().getSimpleName()
															, ExceptionDictionary.ERROR_MESSAGE );
				ExceptionDialog.showMessageDialog( msg, true, true );
				
				/*
				JOptionPane.showMessageDialog(   managerGUI.getAppUI(), e.getMessage(), 
						"Exception in " + getClass().getSimpleName(), 
						JOptionPane.ERROR_MESSAGE);
				*/
			}
		}

		@Override
		protected void cleanUp() throws Exception
		{
			super.cleanUp();

			if (this.monitor != null)
			{
				this.monitor.taskDone(this);
			}
		}

		@Override
		public void taskMonitor(ITaskMonitor monitor)
		{
			this.monitor = monitor;
		}


		@Override
		public List<EventInfo> getResult( boolean clear )
		{
			return null;
		}

		@Override
		public void clearResult() 
		{

		}
		
		@Override
		public String getID() 
		{
			return super.getName();
		}
	}
	
	///////////////////////////////////////
	//
	//
	//
	
	private class WriteTestCalculator extends Thread
	{
		private List< Long > values;
		private String ID;
		public WriteTestCalculator( String streamId, List< Long > val )
		{
			this.ID = streamId;
			this.values = val;
		}
		
		@Override
		public void run() 
		{
			if( this.values != null && !this.values.isEmpty() )
			{
				double acumM= 0.0;
				double acumSD = 0.0;
				for( Long v : this.values )
				{
					acumM += v;
					acumSD += (v * v );
				}
				
				acumM /= this.values.size();				
				acumSD -= ( acumM * acumM * this.values.size() ) ;
				
				if( this.values.size() > 1 )
				{
					acumSD /= ( this.values.size() - 1 );
				}
				
				acumSD = Math.sqrt( acumSD );
				
				String[] timeUnits = new String[] { "seconds"	, "milliseconds", "microseconds", "nanoseconds" };
				String[] freqUnits = new String[] { "Hz"		, "kHz"			, "MHz"			, "GHz" };
				acumM /= 1e9D; // seconds
				acumSD /= 1e9D;
				
				double freq = 1 / acumM;
				
				int timeUnitIndex = 0;
				while( acumM < 1 && timeUnitIndex < timeUnits.length )
				{
					timeUnitIndex++;
					acumM *= 1_000;
					acumSD *= 1_000;
					
					freq /= 1_000;
				}
				

				int freqUnitIndex = timeUnitIndex;
				if( freqUnitIndex > 0 )
				{
					if( freq < 1 )
					{
						freqUnitIndex--;
						freq *= 1_000;
					}
				}
				
				DecimalFormat df = new DecimalFormat("#.00"); 
				
				Exception ex = new Exception( this.ID + " -> average of writing time " + df.format( acumM ) + " \u00B1 " + df.format( acumSD ) 
															+ " " + timeUnits[ timeUnitIndex ] + "" +" (Freq = " + df.format( freq )+ " " + freqUnits[ freqUnitIndex ] + ")" );
				
				ExceptionMessage msg = new ExceptionMessage( ex, Language.getLocalCaption( Language.MENU_WRITE_TEST ), ExceptionDictionary.INFO_MESSAGE );
				ExceptionDialog.showMessageDialog( msg, true, false );
			}
			else
			{
				Exception ex = new Exception( this.ID + " -> non data available." );
				ExceptionMessage msg = new ExceptionMessage( ex, Language.getLocalCaption( Language.MENU_WRITE_TEST ), ExceptionDictionary.INFO_MESSAGE );
				ExceptionDialog.showMessageDialog( msg, true, false );				
			}
			
			this.values.clear();
			this.values = null;
		}
	}
	

	///////////////////////////////////////
	//
	//
	//
	
	private class StopWorkingThread extends AbstractStoppableThread 
	{

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
				if( trial != null )
				{
					trial.stopThread( IStoppableThread.FORCE_STOP );
				}
				
				isRecording = false;
				isWaitingForStartCommand = false;
				isActiveSpecialInputMsg = false;

				managerGUI.setAppState( AppState.STOPPING, 0, false );
				//managerGUI.enablePlayButton( false );

				notifiedEventHandler.interruptProcess();
				notifiedEventHandler.clearEvent();

				ctrSocket.deleteSubordinates( IStoppableThread.FORCE_STOP );

				managerGUI.restoreGUI();

				if( writingTestTimer != null )
				{
					writingTestTimer.stop();
					writingTestTimer = null;
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

						// TODO
						ctrlOutputFile.toWorkSubordinates( new Tuple< String, SyncMarker>( ctrlOutputFile.ACTION_SET_MARK, SpecialMarker ) );

						Thread.sleep( 10L );

						ctrlOutputFile.setBlockingStartWorking( false );

						ctrlOutputFile.setEnableSaveSyncMark( false );

						ctrlOutputFile.deleteSubordinates( IStoppableThread.STOP_IN_NEXT_LOOP );

						if( ctrlOutputFile.isSavingData() )
						{
							managerGUI.setAppState( AppState.SAVING, 0, true );
						}
						else
						{
							managerGUI.setAppState( AppState.STOP, 0, true );
						}
					}
					catch (Exception localException) 
					{
						localException.printStackTrace();
						managerGUI.setAppState( AppState.NONE, 0, false );
					}
					catch (Error localError) 
					{}
				}

				SpecialMarker = null;

				System.gc();

				if( closeWhenDoingNothing 
						&& !isDoingSomething() )
				{
					System.exit( 0 );
				}
			}			
			
			trialWindows = null;
		}
		
		@Override
		protected void runExceptionManager( Throwable e) 
		{
			if( !( e instanceof InterruptedException ) )
			{
				e.printStackTrace();
				
				ExceptionMessage msg = new ExceptionMessage( e, "Exception in " + getClass().getSimpleName(), ExceptionDictionary.ERROR_MESSAGE );
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
			
			stopWorkingThreadEnd();
		}
		
	}

}