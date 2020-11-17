/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2011-2018 by Manuel Merino Monge <manmermon@dte.us.es>
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

package lslrec.gui.dialog;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Image;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import lslrec.config.language.Language;
import lslrec.dataStream.family.stream.lsl.LSL;
import lslrec.config.ConfigApp;
import lslrec.gui.AppUI;
import lslrec.gui.KeyActions;

public class Dialog_AboutApp extends JDialog
{
	private static final long serialVersionUID = 2004993043954574898L;

	//WEB
	private final String url = "http://matrix.dte.us.es/grupotais/";
	private final String authorEmail = "manmermon@dte.us.es";
	private final String sourceURL = "https://github.com/manmermon/LSLRecorder";

	//PANELS
	private JPanel jContentPaneAcercaDe = null;

	//ScrollPaneS
	private JScrollPane jScrollGPL;

	//LABELS
	private JLabel jLabelWeb = null;
	private JLabel jLabelEmail = null;
	private JLabel jLabelOrganizacion = null;
	private JLabel jLabelUniversidad = null;
	private JLabel jLabelAutor = null;
	private JLabel jLabelVersion = null;
	private JLabel jLabelSourceURL = null;	

	//TEXTAREAS
	private JTextArea jTextAreaGPL;

	public Dialog_AboutApp(Window owner)
	{
		super(owner);

		init();
	}

	private void init()
	{
		super.getRootPane().registerKeyboardAction( KeyActions.getEscapeCloseWindows( "EscapeCloseWindow" ), 
				KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0), 
				JComponent.WHEN_IN_FOCUSED_WINDOW );

		super.setTitle( Language.getLocalCaption( Language.MENU_ABOUT ) );// + " " + ConfigApp.shortNameApp );

		super.setContentPane( this.getJContentPaneAcercaDe() );

		super.addWindowListener(new WindowAdapter()
		{
			public void windowDeactivated( WindowEvent e)
			{
				((JDialog)e.getSource()).dispose();
			}
		});
	}

	private JPanel getJContentPaneAcercaDe()
	{
		if (this.jContentPaneAcercaDe == null)
		{
			this.jLabelWeb = new JLabel();
			this.jLabelWeb.setText( Language.getLocalCaption( Language.ABOUT_WEB_LABEL ) 
									+": " + this.url );
			this.jLabelWeb.setAlignmentX( Component.CENTER_ALIGNMENT );
			this.jLabelWeb.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
			this.jLabelWeb.addMouseListener(new MouseAdapter()
			{
				public void mouseExited(MouseEvent e)
				{
					JLabel lb = (JLabel)e.getSource();
					Font f = lb.getFont();
					lb.setFont( new Font( f.getFamily(), Font.BOLD, f.getSize()));
					lb.setForeground(new Color(51, 51, 51));
				}

				public void mouseEntered(MouseEvent e)
				{
					JLabel lb = (JLabel)e.getSource();
					Font f = lb.getFont();
					lb.setFont( new Font( f.getFamily(), Font.ITALIC | Font.BOLD, f.getSize()));
					lb.setForeground(Color.blue);
				}

				public void mouseClicked(MouseEvent e)
				{
					try 
					{
						Desktop.getDesktop().browse(new URI( url ));
					}
					catch (IOException localIOException)
					{}
					catch (URISyntaxException localURISyntaxException) 
					{}
				}

			});

			this.jLabelWeb.addMouseMotionListener(new MouseMotionAdapter()
			{
				public void mouseMoved(MouseEvent e)
				{
					((JLabel)e.getSource()).setCursor(new Cursor( Cursor.HAND_CURSOR ));
				}

			});
			
			
			this.jLabelUniversidad = new JLabel();
			this.jLabelUniversidad.setAlignmentX( Component.CENTER_ALIGNMENT );
			this.jLabelUniversidad.setText("Universidad de Sevilla");

			this.jLabelOrganizacion = new JLabel();
			this.jLabelOrganizacion.setText("Departamento de Tecnolog\u00EDa Electr\u00F3nica,");
			this.jLabelOrganizacion.setAlignmentX( Component.CENTER_ALIGNMENT );
			this.jLabelOrganizacion.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));

			this.jLabelEmail = new JLabel();
			this.jLabelEmail.setText(Language.getLocalCaption( Language.ABOUT_EMAIL_LABEL )
										+": " + this.authorEmail );
			this.jLabelEmail.setAlignmentX( Component.CENTER_ALIGNMENT );
			this.jLabelEmail.addMouseListener(new MouseAdapter()
			{
				public void mouseExited(MouseEvent e)
				{
					JLabel lb = (JLabel)e.getSource();
					Font f = lb.getFont();
					lb.setFont( new Font( f.getFamily(), Font.BOLD, f.getSize()));
					lb.setForeground(new Color(51, 51, 51));
				}

				public void mouseEntered(MouseEvent e)
				{
					JLabel lb = (JLabel)e.getSource();
					Font f = lb.getFont();
					lb.setFont( new Font( f.getFamily(), Font.ITALIC | Font.BOLD, f.getSize()));
					lb.setForeground(Color.blue);
				}

				public void mouseClicked(MouseEvent e)
				{
					try 
					{
						Desktop.getDesktop().mail();

					}
					catch (IOException localIOException) {}
				}

			});

			this.jLabelEmail.addMouseMotionListener(new MouseMotionAdapter()
			{
				public void mouseMoved(MouseEvent e)
				{
					((JLabel)e.getSource()).setCursor(new Cursor( Cursor.HAND_CURSOR ));
				}

			});
			
			this.jLabelAutor = new JLabel();
			this.jLabelAutor.setAlignmentX( Component.CENTER_ALIGNMENT );
			this.jLabelAutor.setText( Language.getLocalCaption( Language.ABOUT_AUTHOR_LABEL ) 
										+": Manuel Merino Monge");
			
			this.jLabelSourceURL = new JLabel();
			this.jLabelSourceURL.setAlignmentX( Component.CENTER_ALIGNMENT );
			this.jLabelSourceURL.setText( Language.getLocalCaption( Language.ABOUT_SOURCE_CODE_LABEL )
											+ " " + this.sourceURL );
			this.jLabelSourceURL.addMouseMotionListener(new MouseMotionAdapter()
			{
				public void mouseMoved(MouseEvent e)
				{
					((JLabel)e.getSource()).setCursor(new Cursor( Cursor.HAND_CURSOR ));
				}

			});
			this.jLabelSourceURL.addMouseListener( new MouseAdapter()
			{	
				@Override
				public void mouseExited(MouseEvent e)
				{
					JLabel lb = (JLabel)e.getSource();
					Font f = lb.getFont();
					lb.setFont( new Font( f.getFamily(), Font.BOLD, f.getSize()));
					lb.setForeground(new Color(51, 51, 51));
				}

				@Override
				public void mouseEntered(MouseEvent e)
				{
					JLabel lb = (JLabel)e.getSource();
					Font f = lb.getFont();
					lb.setFont( new Font( f.getFamily(), Font.ITALIC | Font.BOLD, f.getSize()));
					lb.setForeground(Color.blue);
				}
				
				@Override
				public void mouseClicked(MouseEvent e) 
				{
					try
					{
						if( Desktop.isDesktopSupported() ) 
						{
							Desktop.getDesktop().browse( new URI( sourceURL ) );
						}
					}
					catch( Exception ex )
					{}
				}
			});
			
			this.jContentPaneAcercaDe = new JPanel();
			this.jContentPaneAcercaDe.setLayout(new BoxLayout(this.jContentPaneAcercaDe, BoxLayout.Y_AXIS ));

			this.jLabelVersion = new JLabel();
			this.jLabelVersion.setAlignmentX( Component.CENTER_ALIGNMENT );			
			DateFormat df = DateFormat.getDateInstance( DateFormat.LONG, Locale.getDefault() );			
			this.jLabelVersion.setText( "<html><p><font size='5' face='serif'><b>" + ConfigApp.fullNameApp + " " + ConfigApp.version 
										+ "</b><br/></font><font size='2' face='serif'>" + df.format( ConfigApp.buildDate.getTime() ) 
										+"</font><font size='2' face='serif'> LSL Library version: "  + LSL.library_version() 
										+"</font></p></html>" );
			
			this.jLabelVersion.setIcon(new ImageIcon(AppUI.getInstance().getIconImage().getScaledInstance(32, 32, Image.SCALE_FAST )));
			
			this.jLabelVersion.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
			this.jLabelVersion.setHorizontalAlignment( SwingConstants.CENTER );
			//this.jLabelVersion.setFont(new Font( Font.SERIF, Font.BOLD, 18));
		
			
			this.jContentPaneAcercaDe.add( this.jLabelVersion );
			this.jContentPaneAcercaDe.add( this.jLabelAutor );
			this.jContentPaneAcercaDe.add( this.jLabelEmail );
			this.jContentPaneAcercaDe.add( this.jLabelOrganizacion );
			this.jContentPaneAcercaDe.add( this.jLabelUniversidad );
			this.jContentPaneAcercaDe.add( this.jLabelWeb );
			this.jContentPaneAcercaDe.add( this.jLabelSourceURL );
			this.jContentPaneAcercaDe.add( this.getJScrolGPL() );
		}
		return this.jContentPaneAcercaDe;
	}

	private JScrollPane getJScrolGPL()
	{
		if (this.jScrollGPL == null)
		{
			this.jScrollGPL = new JScrollPane();

			this.jScrollGPL.setViewportView( this.getChkbxGPL());
		}

		return this.jScrollGPL;
	}

	private JTextArea getChkbxGPL()
	{
		if (this.jTextAreaGPL == null)
		{
			String txt = "Copyright " + ConfigApp.appDateRange + " by Manuel Merino Monge "; 
			txt += "<" + this.authorEmail + ">\n\n";
			txt = txt + ConfigApp.shortNameApp + " is free software: you can redistribute it and/or modify ";
			txt = txt + "it under the terms of the GNU General Public License as published by ";
			txt = txt + "the Free Software Foundation, either version 3 of the License, or ";
			txt = txt + "(at your option) any later version.\n\n";
			txt = txt + ConfigApp.shortNameApp + " is distributed in the hope that it will be useful, ";
			txt = txt + "but WITHOUT ANY WARRANTY; without even the implied warranty of ";
			txt = txt + "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the ";
			txt = txt + "GNU General Public License for more details.\n\n";
			txt = txt + "You should have received a copy of the GNU General Public License ";
			txt = txt + "along with " + ConfigApp.shortNameApp + ".  If not, see <http://www.gnu.org/licenses/>.";

			this.jTextAreaGPL = new JTextArea();
			this.jTextAreaGPL.setLineWrap(true);
			this.jTextAreaGPL.setWrapStyleWord(true);
			this.jTextAreaGPL.setText(txt);
			this.jTextAreaGPL.setCaretPosition(this.jTextAreaGPL.getDocument().getDefaultRootElement().getElement(3).getStartOffset());

			this.jTextAreaGPL.setEditable(false);
		}

		return this.jTextAreaGPL;
	}
}