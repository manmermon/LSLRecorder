package Exceptions.Handler;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import GUI.Miscellany.GeneralAppIcon;
import GUI.Miscellany.TextAreaPrintStream;

public class ExceptionDialog 
{
	private static JDialog dialog;
	private static TextAreaPrintStream log;
		
	public static void createExceptionDialog( Window owner ) 
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

		log = new TextAreaPrintStream( jta, new ByteArrayOutputStream() );

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

		
		Point pos = ge.getCenterPoint();
		pos.x -= d.width / 2;
		pos.y -= d.height / 2;
		dialog.setLocation(pos);

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
				log.flush();
			}
		});
		
		dialog.add( new JScrollPane( jta ), BorderLayout.CENTER );
		dialog.add( clearBt, BorderLayout.SOUTH );
		//dialog.toFront();
		//dialog.setVisible( true );		
	}
	
	public static void AppExitWhenWindowClosing()
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

	public static void showMessageDialog( ExceptionMessage msg, boolean concatMsg, boolean printExceptionTrace ) 
	{
		(new Thread()
		{
			@Override
			public void run() 
			{
				if( dialog != null )
				{
					if( !concatMsg )
					{
						log.flush();
					}

					dialog.setTitle( msg.getTitleException() );

					ImageIcon ic = GeneralAppIcon.Warning( 64, Color.ORANGE );
					Color msgColor = Color.ORANGE;

					if( msg.getMessageType() == ExceptionDictionary.ERROR_MESSAGE )
					{
						ic = GeneralAppIcon.Error( 64, Color.RED );
						msgColor = Color.RED;
					}
					else if( msg.getMessageType() == ExceptionDictionary.INFO_MESSAGE )
					{
						ic = GeneralAppIcon.Info( 64, Color.BLACK );
						msgColor = Color.BLACK;
					}

					log.SetColorText( msgColor );

					if( ic != null )
					{
						dialog.setIconImage( ic.getImage() );
					}

					Throwable ex = msg.getException();

					if( ex != null )
					{
						if( printExceptionTrace )
						{
							ex.printStackTrace( log );
						}
						else
						{
							log.println( ex.getMessage() );
						}
					}

					dialog.setVisible( true );
					dialog.toFront();
				}
			}
		}).start();
	}	
}
