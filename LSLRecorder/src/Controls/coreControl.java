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

package Controls;

import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskMonitor;
import Auxiliar.WarningMessage;
import Auxiliar.Extra.Tuple;
import Config.ConfigApp;
import Config.Parameter;
import Config.ParameterList;
import Config.Language.Language;
import Controls.Messages.AppState;
import Controls.Messages.EventInfo;
import Controls.Messages.RegisterSyncMessages;
import Controls.Messages.SocketInformations;
import DataStream.Binary.Plotter.outputDataPlot;
import DataStream.Sync.SyncMarker;
import Controls.Messages.EventType;
import Excepciones.SettingException;
import GUI.appUI;
import GUI.CanvasLSLDataPlot;
import GUI.guiManager;
import Sockets.Info.StreamInputMessage;
import Sockets.Info.SocketSetting;
import Sockets.Info.StreamSocketProblem;
import Sockets.Info.SocketParameters;
import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLConfigParameters;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.nio.file.FileSystemException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Semaphore;
import javax.swing.JOptionPane;
import javax.swing.Timer;
import javax.swing.UIManager;

public class coreControl extends Thread implements IHandlerSupervisor
{
	private SocketInformations streamPars = null;

	//private SocketHandler ctrSocket = null;
	private SocketHandler ctrSocket = null;

	//private OutputDataFileHandler ctrlOutputFile = null;
	private OutputDataFileHandler ctrlOutputFile = null;
	private outputDataPlot ctrLSLDataPlot = null;

	private NotifiedEventHandler notifiedEventHandler = null;

	private boolean isWaitingForStartCommand = false;
	private boolean isRecording = false;

	private static coreControl core = null;

	private guiManager managerGUI;

	private WarningMessage warnMsg = null;

	private boolean showWarningEvent = true;
	
	private boolean closeWhenDoingNothing = false;
	
	private boolean isActiveSpecialInputMsg = false;
	
	private Timer writingTestTimer;
	
	/**
	 * Create main control unit.
	 * 
	 * @throws Exception - Error in subordinates. 
	 */
	private coreControl() throws Exception
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
	public static coreControl getInstance() throws Exception
	{
		if (core == null)
		{
			core = new coreControl();
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
		this.managerGUI = guiManager.getInstance(); // GUI manager

		// socket handler
		//this.ctrSocket = SocketHandler.getInstance();
		this.ctrSocket = SocketHandler.getInstance();
		this.ctrSocket.setControlSupervisor( this );
		this.ctrSocket.startThread();

		// Output file control
		try
		{
			//this.ctrlOutputFile = OutputDataFileHandler.getInstance();
			this.ctrlOutputFile = OutputDataFileHandler.getInstance();
			this.ctrlOutputFile.setControlSupervisor( this );
			this.ctrlOutputFile.startThread();
		}
		catch (Error localError) 
		{
		}    

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
	 * Create a plot.
	 *  
	 * @param LSLDataPlot 	-> plot.
	 * @param lslSetting	-> LSL setting to plot data.
	 */
	public void createLSLDataPlot( CanvasLSLDataPlot LSLDataPlot, LSLConfigParameters lslSetting )
	{
		try
		{
			if (this.ctrLSLDataPlot != null)
			{
				this.disposeLSLDataPlot(); // Delete previous plot
			}
			
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
				int queueLength = ((int)(5.0D * frq)) * lslSetting.getChunckSize();
				if (queueLength < 10)
				{
					queueLength = 100;
				}

				LSLDataPlot.setDataLength( queueLength );
				LSLDataPlot.clearData();
				LSLDataPlot.clearFilters();

				// Plot data
				this.ctrLSLDataPlot = new outputDataPlot( LSLDataPlot, inletInfo, lslSetting );
				this.ctrLSLDataPlot.startThread();
			}
			else
			{
				JOptionPane.showMessageDialog( appUI.getInstance(), Language.getLocalCaption( Language.MSG_LSL_PLOT_ERROR ), Language.getLocalCaption( Language.DIALOG_ERROR ), JOptionPane.ERROR_MESSAGE);
			}
		}
		catch (Exception localException) 
		{
			localException.printStackTrace();
		}
		catch (Error localError) 
		{
			localError.printStackTrace();
		}
	}

	/**
	 * Start to record data
	 */
	public synchronized void startWorking( boolean test )
	{
		try
		{	
			System.gc(); // Clean memory

			this.warnMsg = new WarningMessage(); // To check setting
			
			this.disposeLSLDataPlot(); // Delete plots.
			
			guiManager.getInstance().setAppState( AppState.PREPARING );
			
			// Check settings
			this.checkSettings();
			if (this.warnMsg.getWarningType() == WarningMessage.ERROR_MESSAGE )
			{	
				throw new SettingException( this.warnMsg.getMessage() );
			}

			if (this.warnMsg.getWarningType() == WarningMessage.WARNING_MESSAGE 
					&& !test )
			{
				String[] opts = { UIManager.getString( "OptionPane.yesButtonText" ), 
						UIManager.getString( "OptionPane.noButtonText" ) };

				int actionDialog = JOptionPane.showOptionDialog( this.managerGUI.getAppUI(), this.warnMsg.getMessage() + "\n"
						+Language.getLocalCaption( Language.CONTINUE_TEXT) + "?", 
						Language.getLocalCaption( Language.MSG_WARNING ), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, 
						null, opts, opts[1]);

				if ( actionDialog == JOptionPane.CANCEL_OPTION 
						|| actionDialog == JOptionPane.NO_OPTION 
						|| actionDialog == JOptionPane.CLOSED_OPTION )
				{
					this.isRecording = false;
					this.managerGUI.setAppState( AppState.STOP );
					this.managerGUI.restoreGUI();
					this.managerGUI.refreshLSLDevices();

					return;
				}
			}			
			

			//Create In-Out sockets
			this.streamPars = this.getStreamingInformations();
			ParameterList socketParameters = new ParameterList();
			
			/*
			if ( !this.streamPars.getInputSocketInformation().isEmpty() )
			{
				Parameter par = new Parameter( SocketHandler.CLIENT_SOCKET_STREAMING, this.streamPars.getInputSocketInformation() );
				socketParameters.addParameter( par );
			}

			if ( this.streamPars.getOuputSocketInformation() != null)
			{
				List< SocketParameters > server = new ArrayList< SocketParameters >();
				server.add( this.streamPars.getOuputSocketInformation() );

				Parameter par = new Parameter( SocketHandler.SERVER_SOCKET_STREAMING, server );
				socketParameters.addParameter( par );
			}
			*/

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

			////Output data file
			
			boolean isSyncLSL = false;
			
			if (this.ctrlOutputFile != null )
			{						
				String file = ConfigApp.getProperty( ConfigApp.LSL_OUTPUT_FILE_NAME ).toString();

				HashSet< LSLConfigParameters > deviceIDs = (HashSet< LSLConfigParameters >)ConfigApp.getProperty( ConfigApp.LSL_ID_DEVICES );

				HashSet< LSLConfigParameters > DEV_ID = new HashSet< LSLConfigParameters >();
				for( LSLConfigParameters dev : deviceIDs )
				{
					if( dev.isSelected() )
					{
						DEV_ID.add( dev );
					}
					else
					{
						if( ConfigApp.getProperty( ConfigApp.SELECTED_SYNC_METHOD ).toString().equals( ConfigApp.SYNC_LSL ) 
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
						if( !fpath.exists() &&!fpath.mkdirs() )
						{
							throw new FileSystemException( "Folder " + folder + " not created." );
						}
					}

					ParameterList LSLPars = new ParameterList();

					Parameter filePar = new Parameter( this.ctrlOutputFile.PARAMETER_FILE_PATH, file );
					LSLPars.addParameter( filePar );

					Parameter lslSetting = new Parameter( this.ctrlOutputFile.PARAMETER_LSL_SETTING, DEV_ID );
					LSLPars.addParameter( lslSetting );
					
					Parameter writingTest = new Parameter( this.ctrlOutputFile.PARAMETER_WRITE_TEST, test );
					LSLPars.addParameter( writingTest );					

					minionPars.setMinionParameters( this.ctrlOutputFile.ID, LSLPars );

					this.ctrlOutputFile.addSubordinates( minionPars );						
				}
			}

			this.isActiveSpecialInputMsg = (Boolean)ConfigApp.getProperty( ConfigApp.IS_ACTIVE_SPECIAL_INPUTS );
			
			if( !test )
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
						guiManager.getInstance().stopTest();						
					}
				} );
				
				this.writingTestTimer.start();
			}
			
			waitStartCommand();
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
			this.managerGUI.setAppState( AppState.STOP );
			this.managerGUI.restoreGUI();
			this.managerGUI.refreshLSLDevices();
			this.isWaitingForStartCommand = false;
			this.showWarningEvent = true;
						
			try 
			{
				String msg = e.getMessage() + "\n";
				
				if( !( e instanceof SettingException ) )
				{
					for( StackTraceElement t : e.getStackTrace() )
					{
						msg += t.toString() + "\n";
					}
				}
				
				JOptionPane.showMessageDialog( appUI.getInstance(), msg, Language.getLocalCaption( Language.DIALOG_ERROR ), JOptionPane.ERROR_MESSAGE);
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
		WarningMessage outFileMsg = this.ctrlOutputFile.checkParameters();
		WarningMessage socketMsg = this.ctrSocket.checkParameters();
		
		this.warnMsg.setMessage( "", WarningMessage.OK_MESSAGE );

		this.warnMsg.addMessage( outFileMsg.getMessage(), outFileMsg.getWarningType() );
		this.warnMsg.addMessage( socketMsg.getMessage(), socketMsg.getWarningType() );

		boolean specialInMsg = (Boolean)ConfigApp.getProperty( ConfigApp.IS_ACTIVE_SPECIAL_INPUTS );
		if( !specialInMsg )
		{
			this.warnMsg.addMessage( Language.getLocalCaption( Language.CHECK_SPECIAL_IN_WARNING_MSG ), WarningMessage.WARNING_MESSAGE );
		}
		
		if( ConfigApp.getProperty( ConfigApp.SELECTED_SYNC_METHOD ).equals( ConfigApp.SYNC_NONE ) )
		{
			this.warnMsg.addMessage( Language.getLocalCaption( Language.CHECK_SYNC_METHOD_WARNING_MSG ), WarningMessage.WARNING_MESSAGE );
		}
		
		HashSet< LSLConfigParameters > lslPars = (HashSet< LSLConfigParameters >)ConfigApp.getProperty( ConfigApp.LSL_ID_DEVICES );
		
		LSL.StreamInfo[] results = LSL.resolve_streams();
				
		boolean existSelectedSyncLSL = false;
		
		if( results.length >= 0 )
		{
			boolean selected = false;
			for( LSLConfigParameters lslcfg : lslPars )
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
									
			for( LSLConfigParameters lslcfg : lslPars )
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
			
			if( ConfigApp.getProperty( ConfigApp.SELECTED_SYNC_METHOD ).equals( ConfigApp.SYNC_LSL ) && specialInMsg && !existSelectedSyncLSL )
			{
				this.warnMsg.addMessage( Language.getLocalCaption( Language.CHECK_SYNC_LSL_UNSELECTABLE_ERROR_MSG ), WarningMessage.ERROR_MESSAGE );
			}
			else if( existSelectedSyncLSL && !ConfigApp.getProperty( ConfigApp.SELECTED_SYNC_METHOD ).equals( ConfigApp.SYNC_LSL ) )
			{
				this.warnMsg.addMessage( Language.getLocalCaption( Language.CHECK_LSL_SYNC_STREAM_WARNING_MSG ), WarningMessage.WARNING_MESSAGE );
			}
			
			if( results.length != lslPars.size() )
			{
				boolean change = false;
				
				for( int i = 0; i < results.length && !change; i++ )
				{
					LSL.StreamInfo stream = results[ i ];
					
					for( LSLConfigParameters lslcfg : lslPars )
					{
						change = !stream.uid().equals( lslcfg.getUID() );
						
						if( change )
						{
							break;
						}
					}
				}
				
				if( change )
				{
					this.warnMsg.addMessage( Language.getLocalCaption( Language.CHECK_LSL_DEVICES_CHANGE_WARNING_MSG ), WarningMessage.WARNING_MESSAGE );					
				}
			}
		}
	}
	
	/**
	 * Wait to start message
	 * 
	 * @throws Exception
	 */
	private synchronized void waitStartCommand() throws Exception
	{
		if (this.isWaitingForStartCommand )
		{
			guiManager.getInstance().setAppState( AppState.WAIT );
			
			this.ctrlOutputFile.toWorkSubordinates( new Tuple<String, String>( OutputDataFileHandler.PARAMETER_START_SYNC, "" ) );			
		}
		else
		{			
			this.startRecord();
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
		guiManager.getInstance().StartSessionTimer();
		
		this.managerGUI.setAppState( AppState.RUN );
		
		this.isRecording = true;
		
		this.ctrlOutputFile.toWorkSubordinates( new Tuple< String, String >( OutputDataFileHandler.PARAMETER_FILE_FORMAT 
												, ConfigApp.getProperty( ConfigApp.LSL_OUTPUT_FILE_FORMAT ).toString() ) );
		
		this.ctrlOutputFile.toWorkSubordinates( new Tuple< String, SyncMarker >( OutputDataFileHandler.PARAMETER_SET_MARK 
												, new SyncMarker( RegisterSyncMessages.getSyncMark( RegisterSyncMessages.INPUT_START )
																	, System.nanoTime() / 1e9D ) ) );
	}
					
	/**
	 * Stop recording.
	 * 
	 * @throws Exception
	 */
	public synchronized void stopWorking( ) throws Exception
	{
		if ( this.isRecording 
				|| this.isWaitingForStartCommand )
		{						
			guiManager.getInstance().setAppState( AppState.STOP );
			
			if( this.writingTestTimer != null )
			{
				this.writingTestTimer.stop();
				this.writingTestTimer = null;
			}
			
			if (this.ctrlOutputFile != null)
			{
				try
				{	
					String key = RegisterSyncMessages.INPUT_STOP;
					
					this.ctrlOutputFile.setBlockingStartWorking( true );
										
					// TODO
					this.ctrlOutputFile.toWorkSubordinates( new Tuple< String, SyncMarker>( ctrlOutputFile.PARAMETER_SET_MARK,
																						 	new SyncMarker( RegisterSyncMessages.getSyncMark( key )
																						 					, System.nanoTime() / 1e9D ) ) );
										
					this.ctrlOutputFile.setBlockingStartWorking( false );
										
					this.ctrlOutputFile.deleteSubordinates( IStoppableThread.STOP_IN_NEXT_LOOP );
					
					if( this.ctrlOutputFile.isSavingData() )
					{
						guiManager.getInstance().setAppState( AppState.SAVING );
					}
				}
				catch (Exception localException) 
				{
					localException.printStackTrace();
					guiManager.getInstance().setAppState( AppState.NONE );
				}
				catch (Error localError) 
				{}
			}
					
			/*
			if ( !this.isWaitingForStartCommand )
			{
				try
				{
					this.ctrSocket.setBlockingStartWorking( true );
					this.ctrSocket.toWorkSubordinates( this.getOutputMessage( stopTriggeredEvent ) );
					this.ctrSocket.setBlockingStartWorking(false);
				}
				catch (Exception localException1)
				{}
			}
			*/
			
			this.isRecording = false;
			this.isWaitingForStartCommand = false;
			this.isActiveSpecialInputMsg = false;

			this.notifiedEventHandler.interruptProcess();
			this.notifiedEventHandler.clearEvent();
			
			this.ctrSocket.deleteSubordinates( IStoppableThread.FORCE_STOP );
			
			this.managerGUI.restoreGUI();
			
			System.gc();
						
			if( this.closeWhenDoingNothing && !this.isDoingSomething() )
			{
				System.exit( 0 );
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
	public void eventNotification( IHandlerMinion subordinate, EventInfo event)
	{
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
		if ((EVENTS != null) && (!EVENTS.isEmpty()))
		{
			for (EventInfo event : EVENTS)
			{
				if (event.getEventType().equals( EventType.SOCKET_INPUT_MSG ))
				{
					StreamInputMessage msg = (StreamInputMessage)event.getEventInformation();
					
					Integer msgMark = this.streamPars.getInputCommands().get( msg.getMessage() );

					if ( msgMark != null )
					{
						this.managerGUI.addInputMessageLog( msg.getMessage() + "\n");
						
						if ( msg.getMessage().equals( RegisterSyncMessages.INPUT_STOP ) )
						{ 
							if( this.isActiveSpecialInputMsg )
							{
								stopWorking( );
							}
						}
						else if ( msg.getMessage().equals(RegisterSyncMessages.INPUT_START ) )
						{
							if( this.isActiveSpecialInputMsg 
									&& !this.isRecording 
									&&  this.isWaitingForStartCommand )
								{
									this.isWaitingForStartCommand = false;

									try
									{
										this.startRecord();
									}
									catch (Exception e)
									{
										e.printStackTrace();
									}
								}
						}
						else 
						{
							this.ctrlOutputFile.toWorkSubordinates( new Tuple< String, Integer>( this.ctrlOutputFile.PARAMETER_SET_MARK,
																								 msgMark ) );

						}
					}
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
					
					JOptionPane.showMessageDialog( managerGUI.getAppUI(), msg, 
													EventType.SOCKET_CONNECTION_PROBLEM, 
													JOptionPane.WARNING_MESSAGE );
				}
				else if (event.getEventType().equals(  EventType.SOCKET_CHANNEL_CLOSE ))
				{
					StreamSocketProblem problem = (StreamSocketProblem)event.getEventInformation();

					String msg = problem.getProblemCause().getMessage();
					if (msg.isEmpty())
					{
						msg = problem.getProblemCause().getCause().toString();
					}

					JOptionPane.showMessageDialog( managerGUI.getAppUI(), msg, 
													EventType.SOCKET_CHANNEL_CLOSE, 
													JOptionPane.WARNING_MESSAGE );
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
	private SocketInformations getStreamingInformations()
	{
		SocketInformations infos = new SocketInformations();

		if ( ( (String)ConfigApp.getProperty( ConfigApp.SELECTED_SYNC_METHOD ) ).equals( ConfigApp.SYNC_SOCKET ) )
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

		private coreControl.controlNotifiedManager ctrlManager = null;

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
				super.wait();
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

		public void registreNotification(EventInfo event)
		{
			try
			{
				this.controlNotifySemphore.acquire();
			}
			catch (InterruptedException localInterruptedException) 
			{}      


			String event_type = event.getEventType();
			Object event_Info = event.getEventInformation();
			
			try
			{
				this.eventRegisterSemaphore.acquire();
			}
			catch (Exception localException) 
			{}
			
			synchronized (this.eventRegister)
			{
				//if ( this.eventRegister.size() > 0 )
				{
					if (event_type.equals( EventType.SOCKET_EVENTS ) )
					{
						List<EventInfo> storedEvents = (List<EventInfo>)this.eventRegister.get(event_type);
						List<EventInfo> newEvents = (List<EventInfo>)event_Info;
						Set<String> setEvents = new HashSet<String>();

						if (storedEvents != null)
						{
							Iterator<EventInfo> itEvent = storedEvents.iterator();

							while (itEvent.hasNext())
							{
								EventInfo e = (EventInfo)itEvent.next();

								if (setEvents.contains(e.getEventType()))
								{
									itEvent.remove();
								}
								else
								{
									setEvents.add(e.getEventType());
								}
							}
						}

						Iterator<EventInfo> itEvent = newEvents.iterator();
						while (itEvent.hasNext())
						{
							EventInfo e = itEvent.next();

							if (setEvents.contains(e.getEventType()))
							{
								itEvent.remove();
							}
							else
							{
								setEvents.add(e.getEventType());
							}
						}

						if (storedEvents != null)
						{
							newEvents.addAll(storedEvents);
						}

						event_Info = newEvents;
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
			}

			if (this.eventRegisterSemaphore.availablePermits() < 1)
			{
				this.eventRegisterSemaphore.release();
			}

			if (this.controlNotifySemphore.availablePermits() < 1)
			{
				this.controlNotifySemphore.release();
			}
		}

		public synchronized void treatEvent()
		{
			if (super.getState().equals( Thread.State.WAITING ) )
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
				JOptionPane.showMessageDialog( coreControl.this.managerGUI.getAppUI(), e.getMessage(), 
												"Exception in " + getClass().getSimpleName(), 
												JOptionPane.ERROR_MESSAGE);
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
		}

		protected void preStart() throws Exception
		{
			super.preStart();
		}


		protected void preStopThread(int friendliness) throws Exception
		{}

		protected void postStopThread(int friendliness)  throws Exception
		{
			this.eventRegister.clear();
			this.eventRegister = null;
		}

		protected void runInLoop() throws Exception
		{
			if (this.eventRegister.size() > 0)
			{
				String event_type = (String)this.eventRegister.keySet().iterator().next();
				final Object eventObject = this.eventRegister.get(event_type);

				this.eventRegister.remove(event_type);

				if( event_type.equals( EventType.ALL_OUTPUT_DATA_FILES_SAVED ) )
				{
					guiManager.getInstance().setAppState( AppState.SAVED );
					
					if( closeWhenDoingNothing && !isDoingSomething() )
					{
						System.exit( 0 );
					}
				}
				else if (event_type.equals( EventType.SOCKET_EVENTS ))
				{
					eventSocketMessagesManager( (List< EventInfo> )eventObject );
				}
				else if( event_type.equals( EventType.TEST_WRITE_TIME ) )
				{
					List< Tuple< String, List< Long > > > testValues = (List)eventObject;
					for( Tuple< String, List< Long > > times : testValues )
					{				
						WriteTestCalculator cal = new WriteTestCalculator(  times.x, times.y );
						cal.start();
					}
				}
				else if( event_type.equals( EventType.INPUT_MARK_READY ) )
				{
					SyncMarker mark = (SyncMarker) eventObject;
					
					if( isRecording )
					{
						managerGUI.addInputMessageLog( mark.getMarkValue() + "\n");
					}
										
					if ( mark.getMarkValue() == RegisterSyncMessages.getSyncMark( RegisterSyncMessages.INPUT_STOP ) )
					{ 
						if( isActiveSpecialInputMsg )
						{
							stopWorking( );
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
									startRecord();
								}
								catch (Exception e)
								{
									e.printStackTrace();
								}
							}
					}
				}				
				else if (event_type.equals( EventType.PROBLEM ) )
				{
					stopWorking( );

					JOptionPane.showMessageDialog(  coreControl.this.managerGUI.getAppUI(), 
													eventObject.toString(), 
													event_type, 
													JOptionPane.ERROR_MESSAGE);
				}
				else if (event_type.equals( EventType.WARNING ) )
				{
					if( showWarningEvent )
					{
						new Thread()
						{
							public void run()
							{
								JOptionPane.showMessageDialog(   managerGUI.getAppUI(), 
										eventObject.toString(), 
										Language.getLocalCaption( Language.MSG_WARNING ), 
										JOptionPane.WARNING_MESSAGE);
							}
						}.start();
					}
				}
			}
		}

		protected void targetDone() throws Exception
		{
			super.targetDone();

			this.stopThread = this.eventRegister.isEmpty();
		}

		protected void runExceptionManager(Exception e)
		{
			if (!(e instanceof InterruptedException))
			{
				e.printStackTrace();
				JOptionPane.showMessageDialog(   managerGUI.getAppUI(), e.getMessage(), 
						"Exception in " + getClass().getSimpleName(), 
						JOptionPane.ERROR_MESSAGE);
			}
		}

		protected void cleanUp() throws Exception
		{
			super.cleanUp();

			if (this.monitor != null)
			{
				this.monitor.taskDone(this);
			}
		}


		public void taskMonitor(ITaskMonitor monitor)
		{
			this.monitor = monitor;
		}


		public List<EventInfo> getResult()
		{
			return null;
		}

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
				
				String[] timeUnits = new String[] { "seconds", "milliseconds", "microseconds", "nanoseconds" };
				acumM /= 1e9D; // seconds
				acumSD /= 1e9D;
				
				double freq = 1 / acumM;
				
				int unitIndex = 0;
				while( acumM < 1 && unitIndex < timeUnits.length )
				{
					unitIndex++;
					acumM *= 1000;
					acumSD *= 1000;
				}
				
				DecimalFormat df = new DecimalFormat("#.00"); 
				managerGUI.addInputMessageLog( this.ID + " -> average of writing time " + df.format( acumM ) + " \u00B1 " + df.format( acumSD ) + " " + timeUnits[ unitIndex ] + "" +" (Freq = " + df.format( freq )+ ")\n");
			}
			else
			{
				managerGUI.addInputMessageLog( this.ID + " -> non data available.\n" );
			}
			
			this.values.clear();
			this.values = null;
		}
	}
}