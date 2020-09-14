/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
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

package lslrec.gui;

import lslrec.config.language.Language;
import lslrec.controls.messages.AppState;
import lslrec.controls.messages.EventInfo;
import lslrec.controls.messages.EventType;
import lslrec.dataStream.binary.input.writer.StreamBinaryHeader;
import lslrec.dataStream.binary.reader.TemporalBinData;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.setting.BinaryFileStreamSetting;
import lslrec.dataStream.sync.SyncMarkerBinFileReader;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionDictionary;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.gui.miscellany.LevelIndicator;
import lslrec.gui.miscellany.BasicPainter2D;
import lslrec.plugin.loader.PluginLoader;
import lslrec.plugin.lslrecPluginInterface.ILSLRecPlugin;
import lslrec.plugin.lslrecPluginInterface.ILSLRecPluginEncoder;
import lslrec.stoppableThread.IStoppableThread;
import lslrec.config.ConfigApp;
import lslrec.controls.CoreControl;
import lslrec.controls.OutputDataFileHandler;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import lslrec.auxiliar.extra.Tuple;
import lslrec.auxiliar.tasks.NotificationTask;

public class guiManager
{
	public static final Icon START_ICO = new ImageIcon( BasicPainter2D.paintTriangle(10, 1.0F, Color.BLACK, Color.GREEN, BasicPainter2D.EAST ) );
	public static final Icon STOP_ICO = new ImageIcon( BasicPainter2D.paintRectangle(10, 10, 1.0F, Color.BLACK, Color.RED ) );

	private static guiManager ctr = null;

	private Timer sessionTimer;
	private final int sessionTimeUpdateElapsed = 250; // 
	private Date initSessionTime = null;
	
	//private OpeningDialog preparingRunDiag;
	
	private guiManager()
	{
		this.sessionTimer = new Timer( this.sessionTimeUpdateElapsed, this.getSessionTimerAction() );
	}

	public static guiManager getInstance() 
	{
		if (ctr == null)
		{
			ctr = new guiManager();
		}

		return ctr;
	}
	
	public appUI getAppUI()
	{
		return appUI.getInstance();
	}

	private ActionListener getSessionTimerAction()
	{
		return new ActionListener() 
		{			
			@Override
			public void actionPerformed(ActionEvent e) 
			{	
				UpdateSessionTime();
			}
		};
	}
	
	private void UpdateSessionTime()
	{
		if( this.sessionTimer.isRunning() && this.initSessionTime != null )
		{
			Date current = new Date();
			long diff = current.getTime() - this.initSessionTime.getTime();
			
			long HH = ( diff / ( 3600 * 1000 ) ) % 24;
			long mm = ( diff / ( 60 * 1000 ) ) % 60;
			long ss = ( diff / 1000 ) % 60;			
			long SS = diff % 1000;
			
			appUI.getInstance().getSessionTimeTxt().setText( String.format( "%02d:%02d:%02d.%03d", HH, mm, ss, SS));
		}
	}
	
	protected void saveFileConfig()
	{
		File[] f = selectFile(ConfigApp.defaultNameFileConfig
				, Language.getLocalCaption( Language.DIALOG_SAVE )
				, JFileChooser.SAVE_DIALOG, false, JFileChooser.FILES_ONLY
				, "config (*." + ConfigApp.defaultNameFileConfigExtension + ")"
				, new String[] { ConfigApp.defaultNameFileConfigExtension }
				, System.getProperty("user.dir"));

		if ((f != null) && (f[0].exists()))
		{
			String[] opts = { UIManager.getString("OptionPane.yesButtonText"), 
					UIManager.getString("OptionPane.noButtonText") };

			int actionDialog = JOptionPane.showOptionDialog( appUI.getInstance(), Language.getLocalCaption( Language.DIALOG_REPLACE_FILE_MESSAGE )
					, Language.getLocalCaption( Language.DIALOG_SELECT_OPTS ),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, 
					null, opts, opts[ 1 ] );

			if (actionDialog == JOptionPane.NO_OPTION ||
					actionDialog == JOptionPane.CLOSED_OPTION )
			{
				f = null;
			}
		}

		if (f != null)
		{
			try
			{
				ConfigApp.saveConfig( f[0] );
			}
			catch (Exception e)
			{	
				/*
				JOptionPane.showMessageDialog( appUI.getInstance(), e.getMessage(), Language.getLocalCaption( Language.DIALOG_ERROR )
												, JOptionPane.ERROR_MESSAGE );
				*/
				ExceptionMessage msg = new ExceptionMessage( e, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionDictionary.ERROR_MESSAGE );
				ExceptionDialog.showMessageDialog( msg,	true, false );
			}
		}
	}
		
	protected void loadFileConfig()
	{
		File[] f = this.selectFile( ConfigApp.defaultNameFileConfig
								, Language.getLocalCaption( Language.DIALOG_LOAD )
								, JFileChooser.OPEN_DIALOG, false, JFileChooser.FILES_ONLY
								, "config (*." + ConfigApp.defaultNameFileConfigExtension + ")"
								, new String[] { ConfigApp.defaultNameFileConfigExtension }
								, System.getProperty("user.dir") );

		if ((f != null) && ( f[0].exists() ) )
		{
			loadValueConfig( f[0] );
		}
		else if ((f != null) && (!f[0].exists()))
		{
			//JOptionPane.showMessageDialog( appUI.getInstance(), Language.getLocalCaption( Language.FILE_NOT_FOUND), Language.getLocalCaption( Language.DIALOG_ERROR ), JOptionPane.WARNING_MESSAGE );
			Exception e1 = new Exception( Language.getLocalCaption( Language.FILE_NOT_FOUND ) );
			ExceptionMessage msg = new ExceptionMessage( e1, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionDictionary.WARNING_MESSAGE );
			ExceptionDialog.showMessageDialog( msg,	true, false );
		}
	}
	
	public void refreshLSLDevices()
	{
		appUI.getInstance().getJButtonRefreshDevice().doClick();
	}
	
	protected void convertBin2CLIS()
	{
		dialogConverBin2CLIS diag = new dialogConverBin2CLIS( this.getAppUI(), true );
		
		diag.setSize( 550, 450 );
		
		diag.setTitle( Language.getLocalCaption( Language.MENU_CONVERT2 ) );
		
		diag.setVisible( true );
				
		List< Tuple< Tuple< BinaryFileStreamSetting, OutputFileFormatParameters>, BinaryFileStreamSetting > > binFiles;
		
		try 
		{
			binFiles = diag.getBinaryFiles();
		}
		catch (Exception e1) 
		{
			//JOptionPane.showMessageDialog( this.getAppUI(),  e1.getMessage(), Language.getLocalCaption( Language.PROBLEM_TEXT ), JOptionPane.ERROR_MESSAGE );
			ExceptionMessage msg = new ExceptionMessage( e1, Language.getLocalCaption( Language.PROBLEM_TEXT ), ExceptionDictionary.ERROR_MESSAGE );
			ExceptionDialog.showMessageDialog( msg,	true, true );
			
			binFiles = new ArrayList< Tuple< Tuple< BinaryFileStreamSetting, OutputFileFormatParameters>, BinaryFileStreamSetting >  >();
		}
				
		OutputDataFileHandler outCtr = OutputDataFileHandler.getInstance();
		
		String idEvent = EventType.CONVERT_OUTPUT_TEMPORAL_FILE;
		
		if( binFiles.size() > 0 )
		{ 
			this.setAppState( AppState.SAVING, 0, true );
		}
						
		try 
		{
			List< Tuple< TemporalBinData, SyncMarkerBinFileReader > > STREAMS = new ArrayList<Tuple< TemporalBinData, SyncMarkerBinFileReader >>();
			for( Tuple< Tuple< BinaryFileStreamSetting, OutputFileFormatParameters>, BinaryFileStreamSetting > files : binFiles )
			{
				Tuple< BinaryFileStreamSetting, OutputFileFormatParameters> dat = files.x;
			
				//
				// Data binary files
				//
				
				BinaryFileStreamSetting binSetting = dat.x;
				OutputFileFormatParameters format = dat.y;
				
				String folder = (String)format.getParameter( OutputFileFormatParameters.OUT_FILE_NAME ).getValue();
				if( !folder.endsWith( File.separator ) )
				{
					folder += File.separator;
				}
					
				format.setParameter( OutputFileFormatParameters.OUT_FILE_NAME, folder + "data" + DataFileFormat.getSupportedFileExtension().get(  (String)format.getParameter( OutputFileFormatParameters.OUT_FILE_FORMAT ).getValue() ) );
				
				File dataFile = null;
				if( dat != null )
				{
					dataFile = new File( binSetting.getStreamBinFile() );
				}

				TemporalBinData binData = new TemporalBinData( dataFile, binSetting, format );

				
				// 
				// Sync markers
				//
				
				BinaryFileStreamSetting sync = files.y;
				File syncFile = null;
				if( sync != null )
				{
					syncFile = new File( sync.getStreamBinFile() );
				}
				
				SyncMarkerBinFileReader reader = null;

				if( syncFile != null )
				{
					reader = new SyncMarkerBinFileReader( syncFile
														, sync
														, StreamBinaryHeader.HEADER_END
														, (Boolean)format.getParameter( OutputFileFormatParameters.DELETE_BIN ).getValue() );
				}

				STREAMS.add( new Tuple< TemporalBinData, SyncMarkerBinFileReader >( binData, reader ) );
			}

			if( !STREAMS.isEmpty() )
			{	
				EventInfo event = new EventInfo( this.getClass().getName(), idEvent, STREAMS );

				/*
				INotificationTask notifTask = new INotificationTask() 
				{				
					@Override
					public void taskMonitor(ITaskMonitor monitor)
					{					
					}

					@Override
					public List<EventInfo> getResult() 
					{
						List< EventInfo > ev = new ArrayList< EventInfo >();
						ev.add( event );

						return ev;
					}

					@Override
					public String getID() 
					{
						return "NotifyConvertBin2OutputFile";
					}

					@Override
					public void clearResult() 
					{	
					}
				};
				

				new Thread()
				{
					public void run() 
					{
						try 
						{
							outCtr.taskDone( notifTask );
						}
						catch (Exception e) 
						{						
						}
					};
				}.start();
				*/
				
				NotificationTask notifTask = new NotificationTask( true );
				notifTask.setID( notifTask.getID() + "-NotifyConvertBin2OutputFile" );
				notifTask.setName( notifTask.getID() );
				notifTask.taskMonitor( outCtr );
				notifTask.addEvent( event );
				notifTask.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
				
				notifTask.startThread();
			}
		}
		catch (Exception e) 
		{
		}			
		finally 
		{
		}
	}

	protected String[] selectUserFile(String defaultName, boolean mustExist, boolean multiSelection, int selectionModel, String descrFilter, String[] filterExtensions, String defaultFolder )
	{
		File[] f = selectFile( defaultName, Language.getLocalCaption( Language.DIALOG_SELECT_UESR_FILE )
								, JFileChooser.OPEN_DIALOG
								, multiSelection, selectionModel, descrFilter, filterExtensions, defaultFolder );

		int N = 1;
		
		if( f != null && f.length > 0 )
		{
			N = f.length;
		}
		
		String[] path = null;
				
		if (f != null)
		{			
			boolean allFileExist = true;
			for( int iF = 0; iF < N && allFileExist; iF++ )
			{
				allFileExist = f[ iF ].exists();				
			}
						
			
			if ( mustExist && !allFileExist )
			{
				path = null;
				//JOptionPane.showMessageDialog( appUI.getInstance(), Language.getLocalCaption( Language.FILE_NOT_FOUND ), Language.getLocalCaption( Language.DIALOG_ERROR ),JOptionPane.WARNING_MESSAGE );
				Exception e = new Exception( Language.getLocalCaption( Language.FILE_NOT_FOUND ) );
				ExceptionMessage msg = new ExceptionMessage( e, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionDictionary.WARNING_MESSAGE );
				ExceptionDialog.showMessageDialog( msg,	true, false );
			}
			else
			{
				//path = f[0].getAbsolutePath();
				path = new String[ N ];
				
				for( int iF = 0; iF < N; iF++ )
				{
					path[ iF ] = f[ iF ].getAbsolutePath();					
				}
			}
		}		

		return path;
	}
	
	private File[] selectFile(String defaulName, String titleDialog, int typeDialog, boolean multiSelection, int selectionModel, String descrFilter, String[] filterExtensions, String defaultFolder )
	{		
		FileNameExtensionFilter filter = null;
				
		if( filterExtensions != null && filterExtensions.length > 0 )
		{
			filter = new FileNameExtensionFilter( descrFilter, filterExtensions );
		}
		
		
		File[] file = null;

		JFileChooser jfc = null;

		//jfc = new JFileChooser(System.getProperty("user.dir"));
		jfc = new JFileChooser( defaultFolder );

		jfc.setMultiSelectionEnabled(multiSelection);

		jfc.setDialogTitle(titleDialog);
		jfc.setDialogType(typeDialog);
		jfc.setFileSelectionMode(selectionModel);
		jfc.setSelectedFile(new File(defaulName));
		
		if( filter != null )
		{
			jfc.setFileFilter( filter );
		}

		int returnVal = jfc.showDialog( appUI.getInstance(), null);

		if (returnVal == JFileChooser.APPROVE_OPTION )
		{
			if (multiSelection)
			{
				file = jfc.getSelectedFiles();
			}
			else
			{
				file = new File[1];
				file[0] = jfc.getSelectedFile();
			}
		}

		return file;
	}
	
	private void loadValueConfig(File f)
	{
		try
		{	
			ConfigApp.loadConfig( f );
		}
		catch (Exception e)
		{
			//JOptionPane.showMessageDialog( appUI.getInstance(), e.getMessage(), Language.getLocalCaption( Language.MSG_WARNING ), JOptionPane.WARNING_MESSAGE );			
			ExceptionMessage msg = new ExceptionMessage( e, Language.getLocalCaption( Language.MSG_WARNING ), ExceptionDictionary.WARNING_MESSAGE );
			ExceptionDialog.showMessageDialog( msg,	true, false );
		}
	}

	private void enableGUI( boolean enable )
	{	
		//ui.getJMenuAcercaDe().setEnabled( enable );		
		//ui.getJMenuGNUGPL().setEnabled( enable );
		appUI ui = appUI.getInstance();
		
		ui.getMenuLoad().setEnabled( enable );
		ui.getMenuSave().setEnabled( enable );
		ui.getMenuBin2Clis().setEnabled( enable );
		ui.getMenuExit().setEnabled( enable );
		ui.getMenuWritingTest().setEnabled( enable );
		
		ui.getJButtonRefreshDevice().setEnabled( enable );
		ui.getJComboxSyncMethod().setEnabled( enable );
		ui.getJCheckActiveSpecialInputMsg().setEnabled( enable );
		
		//ui.getJButtonInfo().setEnabled( enable );
				
		try 
		{
			ui.getLSLSetting().enableSettings( enable );
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		ui.getSocketSetting().enableSettings( enable );
	}
		
	public void restoreGUI()
	{
		JButton btnStart = appUI.getInstance().getJButtonPlay();
		btnStart.setText( Language.getLocalCaption( Language.ACTION_PLAY ) );		
		btnStart.setIcon( START_ICO );
		
		this.StopSessionTimer();
		
		this.enableGUI( true );
		
		this.getAppUI().getFileMenu().requestFocusInWindow();
	}

	private void enablePlayButton( boolean enable )
	{
		appUI.getInstance().getJButtonPlay().setEnabled( enable );
	}
	
	public void startTest( final boolean test )
	{	
		Thread t = new Thread()				
		{
			@Override
			public void run() 
			{
				appUI.getInstance().requestFocusInWindow();
				
				appUI.getInstance().getSessionTimeTxt().setText( "" );
				
				enablePlayButton( false );
				
				JButton playBtn = appUI.getInstance().getJButtonPlay();
								
				try 
				{						
					playBtn.setText( Language.getLocalCaption( Language.ACTION_STOP ) );
					playBtn.setIcon( STOP_ICO );
					
					//playBtn.setEnabled( false );
					
					enableGUI( false );
															
					CoreControl.getInstance().startWorking( test );
					
				} 
				catch ( Exception e) 
				{					
					stopTest();
					
					/*
					String m = "";
					
					if( e != null )
					{
						if( e.getCause() != null )
						{						
							m = e.getCause().getMessage();
						}
						else
						{
							m = e.getLocalizedMessage();
						}
					}
						
					JOptionPane.showMessageDialog( appUI.getInstance(), Language.getLocalCaption( Language.PROBLEM_TEXT )+ ": " + m,
													Language.getLocalCaption( Language.DIALOG_ERROR ), JOptionPane.ERROR_MESSAGE);
					*/
					
					ExceptionMessage msg = new ExceptionMessage( e, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionDictionary.ERROR_MESSAGE );
					ExceptionDialog.showMessageDialog( msg,	true, true );
				}
				finally 
				{
					//playBtn.setEnabled( true );
					enablePlayButton( true );
				}				
			};
		};
		
		t.start();
	}
	
	public void StartSessionTimer()
	{
		sessionTimer.restart();
		initSessionTime = new Date();
	}
	
	private void StopSessionTimer()
	{		
		sessionTimer.stop();
		UpdateSessionTime();
		this.initSessionTime = null;
	}
	
	public void stopTest()
	{
		JButton btnStart = appUI.getInstance().getJButtonPlay();
		btnStart.setText( Language.getLocalCaption( Language.ACTION_PLAY ) );		
		btnStart.setIcon( START_ICO );
		
		//this.StopSessionTimer();
		
		try
		{
			CoreControl.getInstance().stopWorking( );
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			/*
			JOptionPane.showMessageDialog( appUI.getInstance(), Language.getLocalCaption( Language.PROBLEM_TEXT )+ ": " + e.getCause(),
					Language.getLocalCaption( Language.DIALOG_ERROR ), JOptionPane.ERROR_MESSAGE);
			*/
			
			ExceptionMessage msg = new ExceptionMessage( e, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionDictionary.ERROR_MESSAGE );
			ExceptionDialog.showMessageDialog( msg, true, true );
			
		}
		finally
		{	
		}
	}

	/*
	public synchronized void setAppState( String msg )
	{				
		JTextField statePanel = appUI.getInstance().getExecutionTextState();
			
		statePanel.setText( msg );
		statePanel.setCaretPosition( 0 );
		
		statePanel.setToolTipText( msg );		
		
		JTextField timeState = appUI.getInstance().getTimeState();
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();		
		timeState.setText( dateFormat.format( date ) );
	}
	*/
	
	public synchronized void setAppState( String msg, int perc, boolean showPerc )
	{				
		LevelIndicator statePanel = appUI.getInstance().getExecutionTextState();
		
		if( perc >= 0 )
		{
			statePanel.setValue( perc );
			statePanel.setLevels( new int[] { perc} );
		}
		
		if( showPerc )
		{
			msg += " (" + perc + "%)";
		}
		
		statePanel.setString( msg  );		
				
		JTextField timeState = appUI.getInstance().getTimeState();
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();		
		timeState.setText( dateFormat.format( date ) );
	}
	
	/*
	public void showWaitingStartCommand() throws Exception
	{
		//-->Start Command MESSAGE
		ui.getJPanelOper().setVisible( false );
		ui.getJPanelOper().removeAll();

		final JButton wait = new JButton();
		wait.setVisible(true);
		wait.setFocusable(false);
		wait.setFocusPainted(false);
		wait.setText( Language.getLocalCaption( Language.WAITING ) 
												+ " " + Language.getLocalCaption( Language.MSG_TEXT ).toLowerCase() 
												+ " " + RegisterSyncMessages.INPUT_START );
		wait.setBackground(Color.WHITE);
		wait.addComponentListener(new ComponentAdapter()
		{

			public void componentResized(ComponentEvent arg0)
			{
				FontMetrics fm = null;
				int div = 2;
				do
				{
					fm = wait.getFontMetrics(new Font( Font.DIALOG, Font.BOLD, wait.getHeight() / div));
					div += 2;
				}
				while ((fm.stringWidth(wait.getText()) > 0) && (fm.stringWidth(wait.getText()) >= wait.getSize().width - wait.getInsets().left - wait.getInsets().right));

				wait.setFont(fm.getFont());
				wait.setText(wait.getText());
			}

		});
		
		ui.getJPanelOper().add(wait, null);

		ui.getJPanelOper().setVisible(true);
		//<--Start Command MESSAGE
	}
	*/
	
	public synchronized void addInputMessageLog( String msg )
	{
		SimpleAttributeSet attributeSet = new SimpleAttributeSet();
		StyleConstants.setUnderline( attributeSet, false);
		StyleConstants.setBold( attributeSet, true );
		StyleConstants.setItalic( attributeSet, true );

		appUI.getInstance().appendTextLog( Color.BLACK,msg, attributeSet);
	}
	
	public void LoadPluginSetting( ) throws Exception 
	{
		List< ILSLRecPlugin > plugins = PluginLoader.getPlugins();
		
		for( ILSLRecPlugin plg : plugins )
		{
			String id = plg.getID();
			JPanel p = plg.getSettingPanel();
			
			if( plg instanceof ILSLRecPluginEncoder )
			{
				DataFileFormat.addEncoder( (ILSLRecPluginEncoder)plg );
			}
		}
	}
}