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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import lslrec.auxiliar.extra.FileUtils;
import lslrec.auxiliar.extra.StringTuple;
import lslrec.auxiliar.extra.Tuple;
import lslrec.auxiliar.tasks.NotificationTask;
import lslrec.config.ConfigApp;
import lslrec.config.language.Language;
import lslrec.controls.CoreControl;
import lslrec.controls.OutputDataFileHandler;
import lslrec.controls.messages.AppState;
import lslrec.controls.messages.EventInfo;
import lslrec.controls.messages.EventType;
import lslrec.dataStream.binary.input.writer.StreamBinaryHeader;
import lslrec.dataStream.binary.reader.TemporalBinData;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.setting.BinaryFileStreamSetting;
import lslrec.dataStream.setting.DataStreamSetting;
import lslrec.dataStream.setting.MutableDataStreamSetting;
import lslrec.dataStream.sync.SyncMarkerBinFileReader;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionDictionary;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.gui.dialog.Dialog_BinaryConverter;
import lslrec.gui.miscellany.BasicPainter2D;
import lslrec.gui.miscellany.LevelIndicator;
import lslrec.gui.miscellany.SelectedButtonGroup;
import lslrec.gui.panel.plugin.Panel_PluginSettings;
import lslrec.gui.panel.primary.Panel_StreamingSettings;
import lslrec.plugin.loader.PluginLoader;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.register.DataProcessingPluginRegistrar;
import lslrec.stoppableThread.IStoppableThread;

public class GuiManager
{
	public static final Icon START_ICO = new ImageIcon( BasicPainter2D.paintTriangle(10, 1.0F, Color.BLACK, Color.GREEN, BasicPainter2D.EAST ) );
	public static final Icon STOP_ICO = new ImageIcon( BasicPainter2D.paintRectangle(10, 10, 1.0F, Color.BLACK, Color.RED ) );

	private static GuiManager ctr = null;

	private Timer sessionTimer;
	private final int sessionTimeUpdateElapsed = 250; // 
	private Date initSessionTime = null;
	
	//private OpeningDialog preparingRunDiag;
	
	//Map
	private static Map< StringTuple, Component > guiParameters = new HashMap< StringTuple, Component>();
	
	private GuiManager()
	{
		this.sessionTimer = new Timer( this.sessionTimeUpdateElapsed, this.getSessionTimerAction() );
	}

	public static GuiManager getInstance() 
	{
		if (ctr == null)
		{
			ctr = new GuiManager();
		}

		return ctr;
	}
	
	public AppUI getAppUI()
	{
		return AppUI.getInstance();
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
			
			AppUI.getInstance().getSessionTimeTxt().setText( String.format( "%02d:%02d:%02d.%03d", HH, mm, ss, SS));
		}
	}
	
	protected void saveFileConfig()
	{
		File[] f = FileUtils.selectFile(ConfigApp.defaultNameFileConfig
										, Language.getLocalCaption( Language.DIALOG_SAVE )
										, JFileChooser.SAVE_DIALOG, false, JFileChooser.FILES_ONLY
										, "config (*." + ConfigApp.defaultNameFileConfigExtension + ")"
										, new String[] { ConfigApp.defaultNameFileConfigExtension }
										, System.getProperty("user.dir"));

		if ((f != null) && (f[0].exists()))
		{
			String[] opts = { UIManager.getString("OptionPane.yesButtonText"), 
					UIManager.getString("OptionPane.noButtonText") };

			int actionDialog = JOptionPane.showOptionDialog( AppUI.getInstance(), Language.getLocalCaption( Language.DIALOG_REPLACE_FILE_MESSAGE )
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
		File[] f = FileUtils.selectFile( ConfigApp.defaultNameFileConfig
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
		AppUI.getInstance().getJButtonRefreshDevice().doClick();
	}
	
	protected void convertBin2CLIS()
	{
		Dialog_BinaryConverter diag = new Dialog_BinaryConverter( this.getAppUI(), true );
		
		diag.setSize( 550, 450 );
		
		diag.setTitle( Language.getLocalCaption( Language.MENU_CONVERT ) );
		
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
				Tuple< BinaryFileStreamSetting, OutputFileFormatParameters> dat = files.t1;
			
				//
				// Data binary files
				//
				
				BinaryFileStreamSetting binSetting = dat.t1;
				OutputFileFormatParameters format = dat.t2;
				
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
				
				BinaryFileStreamSetting sync = files.t2;
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

	
	
	private void loadValueConfig(File f)
	{
		try
		{	
			ConfigApp.loadConfig( f );
			
			loadConfigValues2GuiComponents();
		}
		catch (Exception e)
		{
			//JOptionPane.showMessageDialog( appUI.getInstance(), e.getMessage(), Language.getLocalCaption( Language.MSG_WARNING ), JOptionPane.WARNING_MESSAGE );			
			ExceptionMessage msg = new ExceptionMessage( e, Language.getLocalCaption( Language.MSG_WARNING ), ExceptionDictionary.WARNING_MESSAGE );
			ExceptionDialog.showMessageDialog( msg,	true, false );
		}
	}
	
	public static void setGUIComponent( String guiID, String cfgPropertyID, Component c )
	{
		if( guiID != null && c != null && cfgPropertyID != null ) 
		{
			guiParameters.put( new StringTuple( guiID, cfgPropertyID ), c );
		}
	}
	
	public static void loadConfigValues2GuiComponents()
	{	
		Set< StringTuple > IDs = guiParameters.keySet();

		for( StringTuple id : IDs )
		{
			String guiID = id.t1;
			String propID = id.t2;
			
			Component c = guiParameters.get( id );
			c.setVisible( false );			

			if( c instanceof JComboBox )
			{
				//((JComboBox< String >)c).setSelectedItem( ConfigApp.getProperty( id ) );
				((JComboBox)c).setSelectedItem( ConfigApp.getProperty( propID ).toString() );
			}
			else if( c instanceof JToggleButton )
			{
				//((JToggleButton)c).setSelected( (Boolean)ConfigApp.getProperty( id ) );
				
				JToggleButton b = (JToggleButton)c;
				if( b.isEnabled() )
				{
					b.setSelected( (Boolean)ConfigApp.getProperty( propID ) );
				}
			}
			else if( c instanceof JTextComponent )
			{
				((JTextComponent)c).setText( ConfigApp.getProperty( propID ).toString() );
			}
			else if( c instanceof JTable )
			{
				Set< String > SOCKETS = (Set< String >)ConfigApp.getProperty( propID );
				
				JTable tableSocket = (JTable)c;
								
				DefaultTableModel socketModel = (DefaultTableModel)tableSocket.getModel();
				int nRowBefore = socketModel.getRowCount();
				
				for( int i = nRowBefore - 1 ; i >= 0; i-- )
				{
					socketModel.removeRow( i );
				}
				
				for( String socket : SOCKETS )
				{
					Object[] socketInfo = new Object[3];
					
					System.arraycopy( socket.split( ":" ), 0, socketInfo, 0, 3 );
					socketInfo[ 2 ] = new Integer( socketInfo[ 2 ].toString() );
										
					socketModel.addRow( socketInfo );					
				}
				
				if( tableSocket.getRowCount() > 0 )
				{
					tableSocket.setRowSelectionInterval( 0, 0 );
				}
				
			}		
			else if( c instanceof SelectedButtonGroup )
			{
				SelectedButtonGroup gr = (SelectedButtonGroup)c;

				if( guiID.equals( Panel_StreamingSettings.LSL_STREAM_NAME ) 
						|| guiID.equals( Panel_StreamingSettings.LSL_STREAM_SYNC ) )
				{
					HashSet< MutableDataStreamSetting > devs = (HashSet< MutableDataStreamSetting >) ConfigApp.getProperty( propID );
					if( devs != null )
					{
						Iterator< MutableDataStreamSetting > itDevs = devs.iterator();
		
						while( itDevs.hasNext() )
						{
							MutableDataStreamSetting dev = itDevs.next();
							boolean find = false;
		
							if( c.isEnabled() )
							{
								if( dev.isSelected() || dev.isSynchronationStream() )
								{
									String devID = dev.getSourceID();
			
									find = searchButton( gr, devID );
			
									Enumeration< AbstractButton > bts = gr.getElements();
			
									while( bts.hasMoreElements() && !find )
									{
										AbstractButton b = bts.nextElement();
										find = b.getName().equals( devID );
			
										if( b.isEnabled() )
										{									
											b.setSelected( find );
										}
										else
										{
											if( dev.isSelected() )
											{
												dev.setSelected( false );
											}
											else if( dev.isSynchronationStream() )
											{
												dev.setSynchronizationStream( false );
											}
										}
									}
								}
							}
						}
						
						ConfigApp.setProperty( propID, devs );
					}
				}
			}

			c.setVisible( true );
		}
	
		AppUI.getInstance().getSocketSetting().loadRegisteredSyncInputMessages();		
		
		try 
		{
			AppUI.getInstance().getStreamSetting().refreshLSLStreamings();
		}
		catch (Exception e) 
		{
		}
	}
	
	private static boolean searchButton( SelectedButtonGroup gr, String devID )
	{
		boolean find = false;
		Enumeration< AbstractButton > BTS = gr.getElements();

		while( BTS.hasMoreElements() && !find )
		{
			AbstractButton bt = BTS.nextElement();
			find = bt.getName().equals( devID );

			if( find )
			{
				bt.setSelected( true );
			}
		}
		
		return find;
	}

	private void enableGUI( boolean enable )
	{	
		//ui.getJMenuAcercaDe().setEnabled( enable );		
		//ui.getJMenuGNUGPL().setEnabled( enable );
		AppUI ui = AppUI.getInstance();
		
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
			ui.getStreamSetting().enableSettings( enable );
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		ui.getSocketSetting().enableSettings( enable );
	}
		
	public void restoreGUI()
	{
		JButton btnStart = AppUI.getInstance().getJButtonPlay();
		btnStart.setText( Language.getLocalCaption( Language.ACTION_PLAY ) );		
		btnStart.setIcon( START_ICO );
		
		this.StopSessionTimer();
		
		this.enableGUI( true );
		
		this.getAppUI().getFileMenu().requestFocusInWindow();
	}

	private void enablePlayButton( boolean enable )
	{
		AppUI.getInstance().getJButtonPlay().setEnabled( enable );
	}
	
	public void startTest( final boolean test )
	{	
		Thread t = new Thread()				
		{
			@Override
			public void run() 
			{
				AppUI.getInstance().requestFocusInWindow();
				
				AppUI.getInstance().getSessionTimeTxt().setText( "" );
				
				enablePlayButton( false );
				
				JButton playBtn = AppUI.getInstance().getJButtonPlay();
								
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
		JButton btnStart = AppUI.getInstance().getJButtonPlay();
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
		LevelIndicator statePanel = AppUI.getInstance().getExecutionTextState();
		
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
				
		JTextField timeState = AppUI.getInstance().getTimeState();
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

		AppUI.getInstance().appendTextLog( Color.BLACK,msg, attributeSet);
	}
	
	public void LoadPluginSetting( ) throws Exception 
	{
		List< ILSLRecPlugin > ps = PluginLoader.getInstance().getPlugins();
		
		if( ps != null && !ps.isEmpty() )
		{
			Panel_PluginSettings pps = new Panel_PluginSettings();
					
			String title = Language.getLocalCaption( Language.SETTING_PLUGIN );
			
			getAppUI().getStreamSetting().addSetting2TabbedPanel( title, pps );		
		}
	}
	
	public boolean refreshPlugins()
	{
		HashSet< DataStreamSetting > streams = (HashSet< DataStreamSetting >)ConfigApp.getProperty( ConfigApp.LSL_ID_DEVICES );
		Set< DataStreamSetting > plgStr = DataProcessingPluginRegistrar.getAllDataStreams();
		boolean del = false;
		for( DataStreamSetting dss : plgStr )
		{
			if( !streams.contains( dss ) )
			{
				DataProcessingPluginRegistrar.removeDataStreamInAllProcess( dss );
				
				del = true;
			}
		}
		
		if( del )
		{
			try 
			{
				AppUI.getInstance().getStreamSetting().refreshPlugins();
			}
			catch (Exception e) 
			{
			}
		}
		
		return del;
	}
}