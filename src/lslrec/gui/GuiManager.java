/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
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

package lslrec.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
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
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import lslrec.auxiliar.WarningMessage;
import lslrec.auxiliar.extra.FileUtils;
import lslrec.auxiliar.extra.StringTuple;
import lslrec.auxiliar.extra.Tuple;
import lslrec.auxiliar.task.NotificationTask;
import lslrec.config.ConfigApp;
import lslrec.config.language.Language;
import lslrec.control.handler.CoreControl;
import lslrec.control.handler.OutputDataFileHandler;
import lslrec.control.message.AppState;
import lslrec.control.message.EventInfo;
import lslrec.control.message.EventType;
import lslrec.dataStream.binary.input.writer.StreamBinaryHeader;
import lslrec.dataStream.binary.reader.TemporalBinData;
import lslrec.dataStream.binary.setting.BinaryFileStreamSetting;
import lslrec.dataStream.family.setting.IMutableStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.sync.SyncMarkerBinFileReader;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.gui.dialog.Dialog_BinaryConverter;
import lslrec.gui.miscellany.BasicPainter2D;
import lslrec.gui.miscellany.LevelIndicator;
import lslrec.gui.miscellany.SelectedButtonGroup;
import lslrec.gui.panel.plugin.Panel_PluginSettings;
import lslrec.gui.panel.primary.RightPanelSettings;
import lslrec.plugin.loader.PluginLoader;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.register.DataProcessingPluginRegistrar;
import lslrec.plugin.register.TrialPluginRegistrar;
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
	
	private Boolean isWriteTest = false;
	
	private AppState.State appState = AppState.State.NONE;
	
	//Map
	private static Map< StringTuple, Component > guiParameters = new HashMap< StringTuple, Component>();
	
	private static Object sync = new Object();
	
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
				ExceptionMessage msg = new ExceptionMessage( e, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionMessage.ERROR_MESSAGE );
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
			ExceptionMessage msg = new ExceptionMessage( e1, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionMessage.WARNING_MESSAGE );
			ExceptionDialog.showMessageDialog( msg,	true, false );
		}
	}
	
	public void refreshDataStreams()
	{
		AppUI.getInstance().getJButtonRefreshDataStreams().doClick();		
	}
	
	protected void convertBin2CLIS()
	{
		Dialog_BinaryConverter diag = new Dialog_BinaryConverter( this.getAppUI(), true );
		
		diag.setSize( 550, 450 );
		
		diag.setTitle( Language.getLocalCaption( Language.MENU_CONVERT_BIN ) );
		
		diag.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowOpened(WindowEvent e) 
			{
				JDialog dial = (JDialog)e.getSource();
				GuiManager.getInstance().adjustDialog2Screen( dial );
			}
		});
		
		diag.setLocationRelativeTo( AppUI.getInstance() );
		
		diag.setVisible( true );
				
		List< Tuple< Tuple< BinaryFileStreamSetting, OutputFileFormatParameters>, BinaryFileStreamSetting > > binFiles;
		
		try 
		{
			binFiles = diag.getBinaryFiles();
		}
		catch (Exception e1) 
		{
			//JOptionPane.showMessageDialog( this.getAppUI(),  e1.getMessage(), Language.getLocalCaption( Language.PROBLEM_TEXT ), JOptionPane.ERROR_MESSAGE );
			ExceptionMessage msg = new ExceptionMessage( e1, Language.getLocalCaption( Language.PROBLEM_TEXT ), ExceptionMessage.ERROR_MESSAGE );
			ExceptionDialog.showMessageDialog( msg,	true, true );
			
			binFiles = new ArrayList< Tuple< Tuple< BinaryFileStreamSetting, OutputFileFormatParameters>, BinaryFileStreamSetting >  >();
		}
				
		OutputDataFileHandler outCtr = OutputDataFileHandler.getInstance();
		
		String idEvent = EventType.CONVERT_OUTPUT_TEMPORAL_FILE;
		
		if( binFiles.size() > 0 )
		{ 
			this.setAppState( AppState.State.SAVING, 0, true );
			
			enablePlayButton( false );
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
					
				String idEnc = (String)format.getParameter( OutputFileFormatParameters.OUT_FILE_FORMAT ).getValue();
				String ofn = DataFileFormat.getSupportedFileExtension().get( idEnc  );
				format.setParameter( OutputFileFormatParameters.OUT_FILE_NAME, folder + "data" +  ofn );
				
				File dataFile = null;
				if( dat != null )
				{
					dataFile = binSetting.getStreamBinFile();
				}

				TemporalBinData binData = new TemporalBinData( binSetting, format );

				
				// 
				// Sync markers
				//
				
				BinaryFileStreamSetting sync = files.t2;
				File syncFile = null;
				if( sync != null )
				{
					syncFile = sync.getStreamBinFile();
				}
				
				SyncMarkerBinFileReader reader = null;

				if( syncFile != null )
				{
					reader = new SyncMarkerBinFileReader( sync
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
			enablePlayButton( true );
		}			
		finally 
		{
		}
	}

	
	
	private void loadValueConfig(File f)
	{		
		try
		{	
			DataProcessingPluginRegistrar.clear();
			TrialPluginRegistrar.removeTrialPlugin();
			
			refreshPlugins();
			
			WarningMessage msg = ConfigApp.loadConfig( f );
			
			if( msg.getWarningType() != WarningMessage.ERROR_MESSAGE )
			{
				loadConfigValues2GuiComponents();
				
				refreshPlugins();
			}
			
			if( msg.getWarningType() != WarningMessage.OK_MESSAGE )
			{
				throw new Exception( msg.getMessage() );
			}
		}
		catch (Exception e)
		{
			//JOptionPane.showMessageDialog( appUI.getInstance(), e.getMessage(), Language.getLocalCaption( Language.MSG_WARNING ), JOptionPane.WARNING_MESSAGE );			
			ExceptionMessage msg = new ExceptionMessage( e, Language.getLocalCaption( Language.MSG_WARNING ), ExceptionMessage.WARNING_MESSAGE );
			ExceptionDialog.showMessageDialog( msg,	true, false );
		}
	}
	
	public static void setGUIComponent( String guiID, String cfgPropertyID, Component c )
	{
		synchronized( sync )
		{
			if( guiID != null && c != null && cfgPropertyID != null ) 
			{
				guiParameters.put( new StringTuple( guiID, cfgPropertyID ), c );
			}
		}
	}
	
	public static void loadConfigValues2GuiComponents()
	{	
		synchronized ( sync ) 
		{
			List< StringTuple > IDs = new ArrayList< StringTuple >( guiParameters.keySet() );
			Collections.sort( IDs );
			Collections.reverse( IDs );

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
				else if( c instanceof JButton )
				{
					if( propID.equals( ConfigApp.SELECTED_SYNC_METHOD ) )
					{
						Set< String > mets = (Set< String > )ConfigApp.getProperty( propID );

						String btText =  "";
						for( String m : mets )
						{
							if( btText.isEmpty() )
							{
								btText = m;
							}
							else
							{
								btText = mets.size() + " " + Language.getLocalCaption( Language.SETTING_SYNC_METHOD );
							}
						}

						((JButton)c).setText( btText);

					}
				}
				else if( c instanceof JTextComponent )
				{
					c.setVisible( true );
					((JTextComponent)c).setText( ConfigApp.getProperty( propID ).toString() );
				}
				else if( c instanceof JSpinner )
				{
					((JSpinner)c).setValue( ConfigApp.getProperty( propID ) );
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

					if( guiID.equals( RightPanelSettings.STREAM_NAME ) 
							|| guiID.equals( RightPanelSettings.STREAM_SYNC ) )
					{
						HashSet< IMutableStreamSetting > devs = (HashSet< IMutableStreamSetting >) ConfigApp.getProperty( propID );
						if( devs != null )
						{
							Iterator< IMutableStreamSetting > itDevs = devs.iterator();

							while( itDevs.hasNext() )
							{
								IMutableStreamSetting dev = itDevs.next();
								boolean find = false;

								if( c.isEnabled() )
								{
									if( dev.isSelected() || dev.isSynchronationStream() )
									{
										String devID = dev.source_id();

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

			AppUI.getInstance().getLeftPanelSetting().loadRegisteredSyncInputMessages();		

			try 
			{
				AppUI.getInstance().getRightPanelSetting().refreshDataStreams();
			}
			catch (Exception e) 
			{
			}
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
		AppUI ui = AppUI.getInstance();
		
		ui.getMenuLoad().setEnabled( enable );
		ui.getMenuSave().setEnabled( enable );
		ui.getMenuConvertBinary().setEnabled( enable );
		ui.getMenuExit().setEnabled( enable );
		ui.getMenuWritingTest().setEnabled( enable );
		
		ui.getJButtonRefreshDataStreams().setEnabled( enable );
		//ui.getJComboxSyncMethod().setEnabled( enable );
		ui.getBtnSyncMethod().setEnabled( enable );
		ui.getJCheckActiveSpecialInputMsg().setEnabled( enable );
		
		ui.getPreferenceMenu().setEnabled( enable );
		ui.getMenuClisTo().setEnabled( enable );
				
		try 
		{
			ui.getRightPanelSetting().enableSettings( enable );
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		ui.getLeftPanelSetting().enableSettings( enable );
	}
		
	public void restoreGUI()
	{
		JToggleButton btnStart = AppUI.getInstance().getJButtonPlay();
		
		btnStart.setSelected( false );
				
		this.StopSessionTimer();
		
		this.enableGUI( true );
		
		this.getAppUI().getFileMenu().requestFocusInWindow();
	}

	public void enablePlayButton( boolean enable )
	{
		//System.out.println("GuiManager.enablePlayButton() " + enable);
		AppUI.getInstance().getJButtonPlay().setEnabled( enable );
	}
	
	public void setWriteTest( boolean isTest )
	{
		synchronized ( this.isWriteTest )
		{
			this.isWriteTest = isTest;
		}
	}
	
	public void startTest( )
	{	
		Thread t = new Thread()				
		{
			@Override
			public void run() 
			{
				AppUI.getInstance().requestFocusInWindow();
				
				AppUI.getInstance().getSessionTimeTxt().setText( "" );
				
				enablePlayButton( false );
				
				JToggleButton playBtn = AppUI.getInstance().getJButtonPlay();
								
				try 
				{						
					playBtn.setText( Language.getLocalCaption( Language.ACTION_STOP ) );
					playBtn.setIcon( STOP_ICO );
					
					enableGUI( false );
					
					boolean test = false;
					synchronized ( isWriteTest )
					{
						test = isWriteTest;
						isWriteTest = false;
					}
					
					CoreControl.getInstance().startWorking( test );
					
				} 
				catch ( Exception e) 
				{					
					stopTest();
					
					ExceptionMessage msg = new ExceptionMessage( e, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionMessage.ERROR_MESSAGE );
					ExceptionDialog.showMessageDialog( msg,	true, true );
				}
				finally 
				{
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
		enablePlayButton( false );
		
		JToggleButton btnStart = AppUI.getInstance().getJButtonPlay();
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
			
			ExceptionMessage msg = new ExceptionMessage( e, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionMessage.ERROR_MESSAGE );
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
	
	public AppState.State getAppState()
	{
		return this.appState;
	}
	
	public synchronized void setAppState( AppState.State state, int perc, boolean showPerc )
	{				
		LevelIndicator statePanel = AppUI.getInstance().getExecutionTextState();
		
		if( perc >= 0 )
		{
			statePanel.setValue( perc );
			statePanel.setLevels( new int[] { perc} );
		}
		
		this.appState = state;
		String msg = state.name();
		
		if( showPerc )
		{
			msg += " " + perc + "%";
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
		//List< ILSLRecPlugin > ps = PluginLoader.getInstance().getPlugins();
		List< ILSLRecPlugin > ps = PluginLoader.getInstance().getPlugins();
		
		if( ps != null && !ps.isEmpty() )
		{
			Panel_PluginSettings pps = new Panel_PluginSettings();
					
			String title = Language.getLocalCaption( Language.SETTING_PLUGIN );
			
			getAppUI().getRightPanelSetting().addSetting2TabbedPanel( title, pps );
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.SETTING_PLUGIN, pps );
		}
	}
	
	public boolean refreshPlugins()
	{
		HashSet< IStreamSetting > streams = (HashSet< IStreamSetting >)ConfigApp.getProperty( ConfigApp.ID_STREAMS );
		Set< IStreamSetting > plgStr = DataProcessingPluginRegistrar.getAllDataStreams();
		
		boolean del = false;
		for( IStreamSetting dss : plgStr )
		{
			if( !streams.contains( dss ) )
			{
				//DataProcessingPluginRegistrar.removeDataStreamInAllProcess( dss );
				DataProcessingPluginRegistrar.removeDataStream( dss );
		
				del = true;
			}
		}
		
		try 
		{
			AppUI.getInstance().getRightPanelSetting().refreshPlugins();
		}
		catch (Exception e) 
		{
		}
		
		return del;
	}
	
	public void adjustDialog2Screen( JDialog dialog )
	{
		if( dialog != null )
		{
			Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
			Rectangle winSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
	
			int taskBarHeight = scrnSize.height - winSize.height;
			int taskBarWidth = scrnSize.width - winSize.width;
			
			Point loc = dialog.getLocationOnScreen();
			
			loc.y = ( loc.y < taskBarHeight ) ? taskBarHeight : loc.y;			
			loc.x = ( loc.x < taskBarWidth ) ? taskBarWidth : loc.x;
			
			dialog.setLocation( loc );
		}
	}
	
	public void showLogTab()
	{
		try 
		{
			AppUI.getInstance().getRightPanelSetting().showStreamTab( RightPanelSettings.TAB_LOG );
		}
		catch (Exception e) 
		{
		}
	}
	
}