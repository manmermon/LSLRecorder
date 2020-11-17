package lslrec.testing.AppRunning;
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


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.util.TreeSet;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import lslrec.config.ConfigApp;
import lslrec.config.language.Language;
import lslrec.controls.CoreControl;
import lslrec.dataStream.sync.SyncMethod;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionDictionary;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.gui.AppUI;
import lslrec.gui.GuiManager;
import lslrec.gui.dialog.Dialog_Opening;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.sockets.info.SocketSetting;

public class testLSLRecModeTest
{	
	
	/*
	 * @param args
	 */
	public static void main(String[] args)
	{
		/*
		try 
		{
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
			//UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
		} 
		catch (Exception e) 
		{
			try 
			{
				// Set cross-platform Java L&F (also called "Metal")
				UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
			}
			catch ( Exception e1) 
			{
			}
		}
		*/
		
		ConfigApp.setTesting( true );
		
		try
		{		
			ExceptionDialog.createExceptionDialog( null );
			
			Language.loadLanguages();
			Language.setDefaultLocalLanguage();
			
			TreeSet< String > map = new TreeSet< String >();
			map.add(  SocketSetting.getSocketString( SocketSetting.UDP_PROTOCOL,"127.0.0.1", 45678) );
			ConfigApp.setProperty( ConfigApp.SERVER_SOCKET, map );
			
			ConfigApp.setProperty( ConfigApp.SELECTED_SYNC_METHOD, SyncMethod.SYNC_SOCKET );
			//ConfigApp.setProperty( ConfigApp.SELECTED_SYNC_METHOD, ConfigApp.SYNC_LSL );
			//ConfigApp.setProperty( ConfigApp.IS_ACTIVE_SPECIAL_INPUTS, true );
			ConfigApp.setProperty( ConfigApp.OUTPUT_FILE_NAME, "G:/LSLRecorderTests/data.clis" );
			
			createApplication();
			
			/*
			if( ConfigApp.isTesting() )
			{
				//cpuLoadThread loadThread = new cpuLoadThread( 1000L );
				cpuLoadThread2 loadThread = new cpuLoadThread2( 1000L );
				loadThread.startThread();
			}
			*/
			
			boolean startRecord = false;
			
			// load configuration
			try
			{
				System.out.println("\n\ntestLSLRecModeTest.main() " + args.length + "\n");				
				
				if( args.length > 1 )
				{
					if( args[0].equals( "-c" ) )
					{
						GuiManager.getInstance().getAppUI().getGlassPane().setVisible( true );
						
						ConfigApp.loadConfig( new File( args[ 1 ] ) );
						
						//appUI.getInstance().checkConfig();
					}
					
					if( args.length > 2 ) 
					{						
						startRecord = args[ 2 ].toLowerCase().equals( "-start" );
					}

				}
			}
			catch( Throwable e)
			{
				new Thread() 
				{
					public void run() 
					{					
						showError( e, false );
					};
				}.start();
			}
			finally
			{
				GuiManager.getInstance().loadConfigValues2GuiComponents();;
				GuiManager.getInstance().getAppUI().getGlassPane().setVisible( false );				
				
				
				if( startRecord )
				{
					System.out.println("testLSLRecModeTest.main() START ");
					GuiManager.getInstance().startTest( );
				}
			}
		}
		catch (Throwable e2)
		{
			showError( e2, true );
		}
		finally
		{			
		}		
	}

	public static void createApplication() throws Throwable
	{	
		ExceptionDialog.createExceptionDialog( createAppGUI() );
		createAppCoreControl();
	}

	private static void createAppCoreControl()
	{
		try
		{
			CoreControl ctrl = CoreControl.getInstance();
			ctrl.start();
		}
		catch (Exception e)
		{
			showError( e, true );
		}
	}

	private static AppUI createAppGUI() throws Exception
	{	
		Dimension openDim = new Dimension( 500, 200 );
		Dialog_Opening open = new Dialog_Opening( openDim 
												,  GeneralAppIcon.getIconoAplicacion( 128, 128).getImage()
												, ConfigApp.shortNameApp
												, "<html><center><h1>Opening " + ConfigApp.fullNameApp + ".<br>Wait please...</h1></center></html>" 
												, Color.WHITE );
		open.setVisible( true );
		open.setDefaultCloseOperation( Dialog_Opening.DISPOSE_ON_CLOSE );
		
		Toolkit t = Toolkit.getDefaultToolkit();
		Dimension dm = t.getScreenSize();
		Insets pad = t.getScreenInsets( open.getGraphicsConfiguration() );

		
		open.setLocation( dm.width / 2 - openDim.width / 2, dm.height / 2 - openDim.height / 2 );		
		
	
		AppUI ui = AppUI.getInstance();
		
		ui.setIconImage(GeneralAppIcon.getIconoAplicacion(64, 64).getImage());
		
		String pid = "";
		if( ConfigApp.isTesting() )
		{
			pid += " + " + ManagementFactory.getRuntimeMXBean().getName();
		}
		
		ui.setTitle(  ConfigApp.fullNameApp  + pid );
		
		ui.setBackground(SystemColor.info);

		dm.width = (dm.width / 2 - (pad.left + pad.right));
		dm.height = (dm.height / 2 - (pad.top + pad.bottom));

		if( dm.width < 650 )
		{
			dm.width = 650;
		}
		
		if( dm.height < 650 )
		{
			dm.height = 650;
		}
		
		ui.setSize( dm );

		ui.toFront();
		Dimension d = new Dimension(dm);
		d.width /= 5;

		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());
		ui.setLocation( insets.left + 1, insets.top + 1 );

		ui.setVisible(true);
		
		open.dispose();

		return ui;
	}

	private static void showError( Throwable e, final boolean fatalError )
	{	
		if( fatalError )
		{
			ExceptionDialog.AppExitWhenWindowClosing();
		}
		
		ExceptionMessage msg = new ExceptionMessage( e, Language.getLocalCaption( Language.DIALOG_ERROR), ExceptionDictionary.ERROR_MESSAGE );
		
		ExceptionDialog.showMessageDialog( msg, true, true );		
	}

}
