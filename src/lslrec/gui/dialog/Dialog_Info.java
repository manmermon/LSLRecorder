/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2011-2018 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec and CLIS.
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
import java.awt.Dialog;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import lslrec.gui.KeyActions;

public class Dialog_Info extends JDialog 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3453716688958678189L;
	private JTextPane textAreaInfo = null;

	public Dialog_Info( String text ) 
	{
		this.init( text );
	}
	
	public Dialog_Info( Frame winOwner, String text ) 
	{
		super( winOwner );
		this.init( text );
	}
	
	public Dialog_Info( Window winOwner, String text ) 
	{
		super( winOwner );
		this.init( text );
	}
	
	public Dialog_Info( Dialog winOwner, String text ) 
	{
		super( winOwner );
		this.init( text );
	}
	
	public Dialog_Info( Frame winOwner, String text, boolean modal ) 
	{
		super( winOwner, modal );
		this.init( text );
	}
	
	public Dialog_Info( Dialog winOwner, String text, boolean modal ) 
	{
		super( winOwner, modal );
		this.init( text );
	}
	
	private void init( String text )
	{		
		super.setUndecorated( true );
		super.setLayout( new BorderLayout() );
		super.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
		
		JPanel areaTextPanel = new JPanel( new BorderLayout() );
		areaTextPanel.setBorder( BorderFactory.createLineBorder( Color.BLACK ));
		
		JScrollPane scr = new JScrollPane( this.getTextAreaInfo( text ) );
		areaTextPanel.add( scr, BorderLayout.CENTER );
		
		super.add( areaTextPanel );			
		
		super.addWindowFocusListener( new WindowFocusListener()
		{				
			@Override
			public void windowLostFocus(WindowEvent e) 
			{
				((JDialog)e.getSource()).dispose();
			}
			
			@Override
			public void windowGainedFocus(WindowEvent e) 
			{					
			}
		});
		

		super.getRootPane().registerKeyboardAction( KeyActions.getEscapeCloseWindows( "EscapeCloseWindow"), 
														KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0), 
														JComponent.WHEN_IN_FOCUSED_WINDOW );
	}
	
	private JTextPane getTextAreaInfo( String text )
	{
		if( this.textAreaInfo == null )
		{
			this.textAreaInfo = new JTextPane();
			this.textAreaInfo.setEnabled(true);
			this.textAreaInfo.setEditable(false);						
			this.textAreaInfo.setBorder( BorderFactory.createEmptyBorder( 3, 3, 3, 3 ) );
			
			FontMetrics fm = this.textAreaInfo.getFontMetrics( this.textAreaInfo.getFont() );
			
			MutableAttributeSet set = new SimpleAttributeSet();
			StyleConstants.setSpaceBelow( set, fm.getHeight() * 0.5F );
			
			this.textAreaInfo.setParagraphAttributes( set, false );
						
			this.textAreaInfo.setText( text );
		}
		
		return this.textAreaInfo;
	}
	
}
