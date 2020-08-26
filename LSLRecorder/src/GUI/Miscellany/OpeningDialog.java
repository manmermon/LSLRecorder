/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
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

package gui.miscellany;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class OpeningDialog extends JFrame 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6463721786839022299L;

	public OpeningDialog( Dimension size, Image ico, String appName, String msg, Color backgroundcolor ) 
	{		
		super.setPreferredSize( size );
		super.setSize( size );
		super.setResizable( false );		
		super.setUndecorated( true );
		
		super.setTitle( appName );
		super.setIconImage( ico );
		
		JPanel content = new JPanel( new BorderLayout() );
		content.setBackground( backgroundcolor );		
		
		super.setContentPane( content );
		
		JLabel lb = new JLabel( new ImageIcon( ico ) );
		lb.setText( msg );
		
		lb.setBorder( BorderFactory.createEmptyBorder() );
		lb.setOpaque( false );
				
		content.add( lb, BorderLayout.CENTER );
		content.setBorder( BorderFactory.createEtchedBorder() );
		
		lb.setVisible( true );
		content.setVisible( true );
	}	
}
