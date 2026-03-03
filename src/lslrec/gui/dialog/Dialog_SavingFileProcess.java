/* 
 * Copyright 2018-2026 by Manuel Merino Monge <manmermon@dte.us.es>
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import javax.swing.JProgressBar;
import javax.swing.JScrollPane;

import lslrec.config.language.Language;

public class Dialog_SavingFileProcess extends JDialog 
{
	private static final long serialVersionUID = 1786988360986430884L;
	
	private JPanel contentPanel;
	private JPanel panelProgress;
	private JPanel panelProgressContent;
	
	private JScrollPane scrollPanelProgress;
	
	private Map< String, JProgressBar > fileList = new HashMap<String, JProgressBar>();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			
			Dialog_SavingFileProcess dialog = new Dialog_SavingFileProcess( );

			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setBounds(100, 100, 450, 300);
			dialog.setVisible(true);
			
			List< File > list = new ArrayList< File >();
			for( int i = 0; i < 50; i++ )
			{
				File f = new File( "./" + "filesssssssssssssssss" + i );
				list.add( f );
				
				dialog.addFileProgressBar( f );
				
				Thread.sleep( 100L );
			}
			
			for( File f : list )
			{
				dialog.setProgressValue( f, 50 );
			}
			
			dialog.setProgressEnd( list.get( 0 ) );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public Dialog_SavingFileProcess( ) 
	{	
		super.getContentPane().setLayout( new BorderLayout() );
		
		this.getContentPane().add( this.getContentPanel(), BorderLayout.CENTER);
	}
	
	private JPanel getContentPanel()
	{
		if( this.contentPanel == null )
		{
			this.contentPanel = new JPanel( new BorderLayout() );
			
			this.contentPanel.add( new JLabel( Language.getLocalCaption( Language.MENU_CONVERT_BIN ) ), BorderLayout.NORTH );
			this.contentPanel.add( this.getScrollPanelProgress(), BorderLayout.CENTER );
			this.contentPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
		}
		return this.contentPanel;
	}
	
	private JScrollPane getScrollPanelProgress()
	{
		if( this.scrollPanelProgress == null )
		{
			this.scrollPanelProgress = new JScrollPane( this.getPanelProgressContent(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
			this.scrollPanelProgress.getVerticalScrollBar().setUnitIncrement( 8 );
		}
		
		return this.scrollPanelProgress;
	}
	
	private JPanel getPanelProgressContent()
	{
		if( this.panelProgressContent == null )
		{
			this.panelProgressContent = new JPanel( new BorderLayout() );
			
			this.panelProgressContent.add( this.getPanelProgress(), BorderLayout.NORTH );
		}
		return this.panelProgressContent;
	}
	
	private JPanel getPanelProgress()
	{
		if( this.panelProgress == null )
		{
			this.panelProgress = new JPanel( );
			
			BoxLayout ly = new BoxLayout( this.panelProgress, BoxLayout.Y_AXIS );
			this.panelProgress.setLayout( ly );
		}
		
		return this.panelProgress;
	}

	public void addFileProgressBar( File file )
	{
		JProgressBar progressBar = new JProgressBar( JProgressBar.HORIZONTAL, 0, 100 );
		progressBar.setStringPainted( true );
		
		progressBar.setBorder( BorderFactory.createEmptyBorder( 2, 2, 2, 2 ) );
		
		this.getPanelProgress().add( progressBar );
		this.getPanelProgress().revalidate();
		this.getPanelProgress().repaint();
		
		try 
		{
			this.fileList.put( file.getCanonicalPath(), progressBar );
		}
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		this.setProgressValue( file, 0 );
	}
	
	public void addFileProgressBar( List< File > fileList )
	{
		for( File file : fileList )
		{
			this.addFileProgressBar( file );
		}
	}
	
	public boolean contains( String file )
	{
		return this.fileList.keySet().contains( file );
	}
	
	public void setProgressValue( File file, int value )
	{
		if( file != null )
		{
			try
			{
				String path = file.getCanonicalPath();
				
				JProgressBar bar = this.fileList.get( path );
				
				if( bar != null )
				{
					bar.setValue( value );
					bar.setString( file.getName() + " - " + value + "%" );
				}
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}			
		}
	}
	
	public void setProgressEnd( File file )
	{
		if( file != null )
		{
			try 
			{
				String path = file.getCanonicalPath();
				
				JProgressBar bar = this.fileList.get( path );
				
				if( bar != null )
				{
					bar.setValue( bar.getMaximum() );
					bar.setString( file.getName() );
					bar.setForeground( Color.GREEN.darker() );
				}
			}
			catch (IOException e) 
			{
				e.printStackTrace();
			}
		}
	}
}
