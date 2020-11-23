package lslrec.testing.AppRunning;


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

import javax.swing.UIManager;

import lslrec.config.ConfigApp;
import lslrec.config.language.Language;
import lslrec.controls.handler.CoreControl;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionDictionary;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.gui.AppUI;
import lslrec.gui.GuiManager;
import lslrec.gui.dialog.Dialog_Opening;
import lslrec.gui.miscellany.GeneralAppIcon;

public class testRunLSLRec
{
	/*
	 * @param args
	 */
	public static void main(String[] args)
	{
		String p = System.getProperty( "user.dir" ) + "/systemLib/";
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
						GuiManager.getInstance().getAppUI().getGlassPane().setVisible( true );
						
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
				GuiManager.getInstance().loadConfigValues2GuiComponents();
				GuiManager.getInstance().getAppUI().getGlassPane().setVisible( false );
			}
                        
                        ConfigApp.setTesting( true );
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
		
		open.dispose();
		
		return ui;
	}

	/*
	private static void showError( Throwable e, final boolean fatalError )
	{
		JTextArea jta = new JTextArea();
		jta.setAutoscrolls( true );
		jta.setEditable( false );
		jta.setLineWrap( true );
		jta.setTabSize( 0 );

		TextAreaPrintStream log = new TextAreaPrintStream( jta, new ByteArrayOutputStream() );

		e.printStackTrace( log );

		String[] lines = jta.getText().split( "\n" );
		int wd = Integer.MIN_VALUE;
		FontMetrics fm = jta.getFontMetrics( jta.getFont() );
		for (int i = 0; i < lines.length; i++)
		{
			if (wd < fm.stringWidth( lines[i] ) )
			{
				wd = fm.stringWidth( lines[i] );
			}
		}

		JDialog p = new JDialog();

		Icon icono = UIManager.getIcon( "OptionPane.warningIcon" );
		
		int w = icono.getIconWidth();
		int h = icono.getIconHeight();
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		
		BufferedImage img = gc.createCompatibleImage( w, h, BufferedImage.TYPE_INT_ARGB ); 
		Graphics2D g = img.createGraphics();
		icono.paintIcon( null, g, 0, 0 );
		p.setIconImage( img );

		p.setTitle( "Problem" );
		
		if( fatalError )
		{
			p.setTitle( "Fatal Error" );
		}
		
		Dimension d = new Dimension( (int)( wd * 1.25D ), fm.getHeight() * 10 );
		p.setSize( d );

		Point pos = ge.getCenterPoint();
		pos.x -= d.width / 2;
		pos.y -= d.height / 2;
		p.setLocation(pos);

		p.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				if( fatalError )
				{
					System.exit( 0 );
				}
			}

		});
		
		JButton close = new JButton("Cerrar");
		close.addActionListener(new ActionListener()
		{
			public void actionPerformed( ActionEvent e )
			{
				if( fatalError )
				{
					System.exit( 0 );
				}
				else
				{
					p.dispose();
				}
			}

		});
		
		p.add( new JScrollPane( jta ), BorderLayout.CENTER );
		p.add( close, BorderLayout.SOUTH );
		p.toFront();
		p.setVisible( true );		
	}
	*/
	
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
