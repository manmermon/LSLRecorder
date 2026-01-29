/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.exceptions.handler;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import lslrec.gui.GuiManager;
import lslrec.gui.KeyActions;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.gui.miscellany.TextAreaPrintStream;

public class ExceptionDialog 
{
	private static JDialog dialog;
	
	private static Object sync = new Object();
	private static Object syncLogFile = new Object();
	
	private static ExceptionLogGUIThread logGUI = new ExceptionLogGUIThread( 1 );
	
	//private static File errorWarningLog =  null;
	
	//private static String recordSubjID = null;
	//private static String recordSessionID = null;
	
	private static ExceptionLogFileThread errorWarningLog;
	
	public static void createExceptionDialog( Window owner ) 
	{
		synchronized( sync )
		{
			if( dialog != null )
			{			
				dialog.setVisible( false );
				dialog.dispose();
				
				dialog = null;
			}
			
			JTextPane jta = new JTextPane();
			jta.setText( "" );
			jta.setAutoscrolls( true );
			jta.setEditable( false );
			//jta.setLineWrap( true );
			//jta.setTabSize( 0 );
	
			//log2 = new TextAreaPrintStream( jta, new ByteArrayOutputStream() );
			 logGUI.addLog( new TextAreaPrintStream( jta, new ByteArrayOutputStream() ) );
	
			dialog = new JDialog( owner );
	
			Icon icono = GeneralAppIcon.Warning( 16, Color.RED );
			
			int w = icono.getIconWidth();
			int h = icono.getIconHeight();			
			
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice gd = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gd.getDefaultConfiguration();
			
			Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
			if( dim == null )
			{
				dim = new Dimension( 200, 200 );
			}
			
			BufferedImage img = gc.createCompatibleImage( w, h, BufferedImage.TYPE_INT_ARGB ); 
			Graphics2D g = img.createGraphics();
			icono.paintIcon( null, g, 0, 0 );
			dialog.setIconImage( img );
			
			Dimension d = new Dimension( (int)( dim.width /3 ), dim.height / 2 );
			dialog.setSize( d );
	
			/*
			Point pos = ge.getCenterPoint();
			pos.x -= d.width / 2;
			pos.y -= d.height / 2;
			dialog.setLocation(pos);
			*/
			dialog.setLocationRelativeTo( owner );
	
			dialog.addWindowListener(new WindowAdapter()
			{
				public void windowClosing(WindowEvent e)
				{
					dialog.dispose();
				}
			});		
			
			JButton clearBt = new JButton( "Clear" );
			clearBt.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					synchronized( sync )
					{
						/*
						log2.flush();
						
						if( log1 != null )
						{
							log1.flush();
						}
						//*/
						
						logGUI.clearLog();
					}					
				}
			});
			
			dialog.add( new JScrollPane( jta ), BorderLayout.CENTER );
			dialog.add( clearBt, BorderLayout.SOUTH );
			//dialog.toFront();
			//dialog.setVisible( true );
			
			dialog.getRootPane().registerKeyboardAction( KeyActions.getEscapeCloseWindows( "EscapeCloseWindow" ), 
														KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0), 
														JComponent.WHEN_IN_FOCUSED_WINDOW );
		}
	}
	
	public static void setMainTextLog( TextAreaPrintStream mainLog )
	{
		synchronized ( sync )
		{
			logGUI.addLog( mainLog );
			//log1 = mainLog;
		}
	}
	
	public static boolean wasCreatedExceptionDialog()
	{
		synchronized ( sync )
		{
			return dialog != null;
		}		
	}
	
	public static void AppExitWhenWindowClosing()
	{
		closeLogFile();
		
		synchronized ( sync )
		{
			if( dialog != null )
			{
				dialog.addWindowListener( new WindowAdapter() 
				{
					@Override
					public void windowClosing(WindowEvent e) 
					{
						System.exit( 0 );
					} 
				});
			}
		}
	}

	public static void openLogFile( String subjID, String sessionID ) throws Exception
	{
		synchronized ( syncLogFile )
		{
			if( errorWarningLog != null )
			{
				throw new RuntimeException( "Log is opened." );
			}
			
			errorWarningLog = new ExceptionLogFileThread( subjID, sessionID );
		}
	}
	
	public static void closeLogFile()
	{
		logGUI.flush();
		
		synchronized ( syncLogFile )
		{
			if( errorWarningLog != null )
			{
				errorWarningLog.close();
				errorWarningLog = null;
			}
		}		
	}
	
 	public static void showMessageDialog( ExceptionMessage msg, boolean concatMsg, boolean printExceptionTrace ) 
	{
 		SwingUtilities.invokeLater(() -> 
 		{
	 		if( logGUI != null )
	 		{
	 			GuiManager.getInstance().showLogTab();
	 			logGUI.write( msg, concatMsg, printExceptionTrace );
	 		}
	 		
	 		if( dialog != null )
			{
				dialog.setTitle( msg.getTitleException() );
				
				if( msg.getMessageType() == ExceptionMessage.ERROR_MESSAGE )
				{
					if( !dialog.isVisible() )
					{							
						dialog.setLocationRelativeTo( dialog.getOwner() );
					}
					
					dialog.setVisible( true );
					dialog.toFront();				
				}
			}						
 		});
 		
 		Throwable ex = msg.getException();
 		StringWriter sw = new StringWriter();
 		PrintWriter pw = new PrintWriter(sw);
 		ex.printStackTrace(pw);

 		synchronized ( syncLogFile )
		{
 			if( errorWarningLog != null )
 			{
 				errorWarningLog.write( sw.toString(), msg.getMessageType() );
 			}
		}
	}	

	public static void showDialog()
	{
		synchronized ( sync )
		{
			if( dialog != null )
			{
				if( !dialog.isVisible() )
				{
					dialog.setLocationRelativeTo( dialog.getOwner() );
				}
				dialog.setVisible( true );
				dialog.toFront();
			}
		}
	}

	public static void clearMessages()
	{
		synchronized( sync )
		{
			logGUI.clearLog();
		}
	}
}
