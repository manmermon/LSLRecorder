/* 
 * Copyright 2018-2022 by Manuel Merino Monge <manmermon@dte.us.es>
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
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import lslrec.config.ConfigApp;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.config.SettingOptions;
import lslrec.config.language.Language;
import lslrec.gui.KeyActions;
import lslrec.gui.panel.plugin.item.CreatorDefaultSettingPanel;

/**
 * @author Manuel Merino Monge
 *
 */
public class Dialog_AdvancedOptions extends JDialog 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4829840520782310628L;
	
	private JPanel main;
	
	private JScrollPane scr;
	
	/**
	 * 
	 */
	public Dialog_AdvancedOptions( List< SettingOptions > opts, ParameterList pars ) 
	{
		super.setModal( true );
		super.setLayout( new BorderLayout() );
		super.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
		
		JPanel optPanel = CreatorDefaultSettingPanel.getSettingPanel( opts, pars );
		
		JPanel main = new JPanel( new BorderLayout() );		
		JScrollPane scr = new JScrollPane( optPanel );

		main.add( scr, BorderLayout.CENTER );


		super.add( main );
									
		super.pack();

		Dimension s = super.getSize();
		FontMetrics fm = super.getFontMetrics( optPanel.getFont() );

		int t = fm.stringWidth( super.getTitle() ) * 2;
		
		for( String id : pars.getParameterIDs() )
		{
			Parameter p = pars.getParameter( id );
			int wp = (int)(fm.stringWidth( Language.getLocalCaption( p.getLangID() ) ) * 2.5);
			if( t < wp )
			{
				t = wp;
			}
		}
		
		if( t > s.width )
		{
			s.width = t;
		}
		s.height += 15;

		if( s.height > 300 )
		{
			s.height = 300;
		}
		
		if( s.width > 480 )
		{
			s.width = 480;
		}
		
		super.setSize( s );

		super.getRootPane().registerKeyboardAction( KeyActions.getEscapeCloseWindows( "EscapeCloseWindow" ), 
													KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0), 
													JComponent.WHEN_IN_FOCUSED_WINDOW );
	}	
	
}
