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
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;

import org.apache.commons.lang3.StringUtils;

import lslrec.config.ConfigApp;
import lslrec.gui.GuiManager;
import lslrec.gui.KeyActions;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.gui.miscellany.TextAreaPrintStream;

public class ExceptionDialog 
{
	private static JDialog dialog;
	private static TextAreaPrintStream log2;
	
	private static TextAreaPrintStream log1;
	
	private static Object sync = new Object();
	
	private static File errorWarningLog =  null;
	
	private static String recordSubjID = null;
	private static String recordSessionID = null;
	
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
			jta.setAutoscrolls( true );
			jta.setEditable( false );
			//jta.setLineWrap( true );
			//jta.setTabSize( 0 );
	
			log2 = new TextAreaPrintStream( jta, new ByteArrayOutputStream() );
	
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
						log2.flush();
						
						if( log1 != null )
						{
							log1.flush();
						}
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
			log1 = mainLog;
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

	public static void setRecordSessionInfo( String subjID, String sessionID )
	{
		synchronized ( sync )
		{
			recordSubjID = subjID;
			recordSessionID = sessionID;
		}		
	}
	
 	public static void showMessageDialog( ExceptionMessage msg, boolean concatMsg, boolean printExceptionTrace ) 
	{
		(new Thread()
		{
			@Override
			public void run() 
			{
				super.setName( "ExceptionDialog-showMessage-" + super.getId() );
				
				synchronized ( sync )
				{
					GuiManager.getInstance().showLogTab();
					
					Throwable ex = msg.getException();
					
					Color msgColor = Color.ORANGE;
					boolean errorFocus = false;
					ImageIcon ic = GeneralAppIcon.Warning( 64, Color.ORANGE );

					if( msg.getMessageType() == ExceptionMessage.ERROR_MESSAGE )
					{
						ic = GeneralAppIcon.Error( 64, Color.RED );
						msgColor = Color.RED;
						errorFocus = true;
					}
					else if( msg.getMessageType() == ExceptionMessage.INFO_MESSAGE )
					{
						ic = GeneralAppIcon.Info( 64, Color.BLACK );
						msgColor = Color.BLACK;
					}
					
					if( log1 != null )
					{
						if( !concatMsg )
						{
							log1.flush();
						}
	
						log1.SetColorText( msgColor );
							
						if( ex != null )
						{
							if( printExceptionTrace )
							{
								ex.printStackTrace( log1 );
							}
							else
							{
								log1.println( ex.getMessage() );
							}
						}
					}
					
					if( dialog != null )
					{
						if( !concatMsg )
						{
							log2.flush();
						}
	
						dialog.setTitle( msg.getTitleException() );
	
						log2.SetColorText( msgColor );
	
						if( ic != null )
						{
							dialog.setIconImage( ic.getImage() );
						}
	
						
	
						if( ex != null )
						{
							if( printExceptionTrace )
							{
								ex.printStackTrace( log2 );
							}
							else
							{
								log2.println( ex.getMessage() );
							}
						}
						
						if( errorFocus )
						{
							if( !dialog.isVisible() )
							{							
								dialog.setLocationRelativeTo( dialog.getOwner() );
							}
								
							boolean show = false;
							
							while( !show )
							{
								try
								{
									dialog.setVisible( true );
									
									show = true;
								}
								catch (Exception e) 
								{								
								}
							}
							
							if( msg.getMessageType() == ExceptionMessage.ERROR_MESSAGE )
							{
								dialog.toFront();
							}
						}
					}
					
					String date = new SimpleDateFormat("yyyy-MM-dd").format( new Date());
										
					String subj = recordSubjID;
					String session = recordSessionID;
										
					String subjSession = "";
					if( subj != null && !subj.isEmpty() )
					{
						subjSession += subj;
					}
					
					if( session != null && !session.isEmpty() )
					{
						subjSession = ( subjSession.isEmpty() ) ? "-" + session : subjSession + "-" + session;
					}
					
					String fileName = ConfigApp.defaultLogPathFile;
					fileName += ConfigApp.defaulLogFileNamePrefix;
					fileName += "_" + date + "_" + subjSession + "." + ConfigApp.defaulLogFileExtension;
					
					boolean newLogFile = ( errorWarningLog == null ) || ( !errorWarningLog.getAbsoluteFile().toString().equals( fileName ) );
					
					if( newLogFile )
					{	
						errorWarningLog = new File( fileName );
						
						try 
						{
							errorWarningLog.getParentFile().mkdirs();
							errorWarningLog.createNewFile();
						} 
						catch (IOException e) 
						{
							e.printStackTrace();
						}
					}
					 
					if( errorWarningLog != null )
					{
						try
						{
							PrintWriter out = new PrintWriter( new BufferedWriter(
																new FileWriter( errorWarningLog, true ) )
																, false );
							
							String header = "\n"+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( new Date());
							header += " (" + subjSession + "):";
							
							String type = "WARNING";
							if( msg.getMessageType() == ExceptionMessage.ERROR_MESSAGE )
							{
								type = "ERROR";
							}
							else if( msg.getMessageType() == ExceptionMessage.INFO_MESSAGE )
							{
								type = "INFO";
							}
																					
							header += type + "\n";
							header += StringUtils.repeat( "-", header.length() );
							header += "\n";
							
							out.print( header );
														
							ex.printStackTrace( out );
							out.close();
						}
						catch (Exception e) 
						{
							e.printStackTrace();
						}
					}
						
				}
			}
		}).start();
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
			if( log1 != null )
			{
				log1.flush();
			}
			
			if( log2 != null )
			{
				log2.flush();
			}
		}
	}
}
