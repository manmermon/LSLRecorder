package lslrec;
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

import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionDictionary;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.gui.appUI;
import lslrec.gui.guiManager;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.gui.miscellany.OpeningDialog;
import lslrec.plugin.loader.PluginLoader;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.compressor.ILSLRecPluginCompressor;
import lslrec.plugin.lslrecPlugin.encoder.ILSLRecPluginEncoder;
import lslrec.config.ConfigApp;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.io.File;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import javax.swing.UIManager;

import lslrec.config.language.Language;
import lslrec.controls.CoreControl;
import lslrec.dataStream.outputDataFile.compress.CompressorDataFactory;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;

public class mainLSLRecorder
{
	/*
	 * @param args
	 */
	public static void main(String[] args)
	{
		String p = System.getProperty( "user.dir" ) + "/" + ConfigApp.SYSTEM_LIB_PATH;
		try 
		{
			addLibraryPath( p );
		} 
		catch (Exception e) 
		{
			showError( e, false );
		}
		
		try 
		{
			String OS = System.getProperty("os.name").toLowerCase();
			
			if( OS.indexOf("nix") < 0 
				&& OS.indexOf("nux") < 0 
				&& OS.indexOf("aix") < 0 )
			{
				UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
			}
			else
			{
				UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
			}
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
		
		try
		{
			Language.loadLanguages();
			Language.setDefaultLocalLanguage();
			

			ExceptionDialog.createExceptionDialog( null );
						
			createApplication();
			
			// load configuration
			try
			{
				if( args.length > 1 )
				{
					if( args[0].equals( "-c" ) )
					{
						guiManager.getInstance().getAppUI().getGlassPane().setVisible( true );
						
						ConfigApp.loadConfig( new File( args[ 1 ] ) );
						
						//appUI.getInstance().checkConfig();
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
				guiManager.getInstance().getAppUI().loadConfigValues();
				guiManager.getInstance().getAppUI().getGlassPane().setVisible( false );
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


	public static void addLibraryPath(String pathToAdd) throws Exception 
	{
		Field usrPathsField = ClassLoader.class.getDeclaredField("usr_paths");
		usrPathsField.setAccessible(true);
		String[] paths = (String[]) usrPathsField.get(null);
		for (String path : paths)
		{
			if (path.equals(pathToAdd))
			{
				return;
			}
		}

		String[] newPaths = Arrays.copyOf(paths, paths.length + 1);
		newPaths[newPaths.length - 1] = pathToAdd;
		usrPathsField.set(null, newPaths);
	}

	public static void createApplication() throws Throwable
	{	
		// Opening dialog
		OpeningDialog open = showOpeningDialog();
		
		// 
		// Load plugins
		//

		//if( false )		
		try
		{
			List< Exception > exs = PluginLoader.LoadPlugins();

			for( Exception e : exs )
			{
				//ExceptionMessage msg = new ExceptionMessage( e, "Load plugin errors", ExceptionDictionary.WARNING_MESSAGE );
				showError( e, false );
			}			
		}
		catch( Exception | Error e )
		{
			/*
			ExceptionMessage msg = new ExceptionMessage( e, "Load plugin errors", ExceptionDictionary.WARNING_MESSAGE );
			ExceptionDialog.showMessageDialog( msg, true, true );
			*/
			
			showError( e, false );
		}
		
		registerPlugins();
		
		// Load GUI
		ExceptionDialog.createExceptionDialog( createAppGUI() );
		
		guiManager.getInstance().LoadPluginSetting();
		
		open.dispose();
		
		// Load Controllers
		createAppCoreControl();
		
		ConfigApp.setTesting( true );
	}
	
	private static void registerPlugins()
	{
		List< ILSLRecPlugin > plugins = PluginLoader.getPlugins();
		
		for( ILSLRecPlugin plg : plugins )
		{
			if( plg instanceof ILSLRecPluginEncoder )
			{
				DataFileFormat.addEncoder( (ILSLRecPluginEncoder)plg );
			}
			else if( plg instanceof ILSLRecPluginCompressor )
			{
				CompressorDataFactory.addCompressor( (ILSLRecPluginCompressor)plg );
			}
		}		
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

	private static OpeningDialog showOpeningDialog()
	{
		Dimension openDim = new Dimension( 500, 200 );
		OpeningDialog openDialog = new OpeningDialog( openDim 
												,  GeneralAppIcon.getIconoAplicacion( 128, 128).getImage()
												, ConfigApp.shortNameApp
												, "<html><center><h1>Opening " + ConfigApp.fullNameApp + ".<br>Wait please...</h1></center></html>" 
												, Color.WHITE );
		openDialog.setVisible( true );
		openDialog.setDefaultCloseOperation( OpeningDialog.DISPOSE_ON_CLOSE );
		
		Toolkit t = Toolkit.getDefaultToolkit();
		Dimension dm = t.getScreenSize();
		Insets pad = t.getScreenInsets( openDialog.getGraphicsConfiguration() );

		
		openDialog.setLocation( dm.width / 2 - openDim.width / 2, dm.height / 2 - openDim.height / 2 );		
		
		return openDialog;
	}
	
	private static appUI createAppGUI() throws Exception
	{	
		Toolkit t = Toolkit.getDefaultToolkit();
		Dimension dm = t.getScreenSize();
	
		appUI ui = appUI.getInstance();
		
		Insets pad = t.getScreenInsets( ui.getGraphicsConfiguration() );
		
		ui.setIconImage(GeneralAppIcon.getIconoAplicacion(64, 64).getImage());
		ui.setTitle(  ConfigApp.fullNameApp );
		
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
