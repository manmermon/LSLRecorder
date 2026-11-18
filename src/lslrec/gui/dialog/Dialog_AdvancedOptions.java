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

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.event.AWTEventListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.border.TitledBorder;

import lslrec.config.GeneralSettings;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.config.language.Language;
import lslrec.gui.KeyActions;
import lslrec.gui.setting.CreatorDefaultSettingPanel;
import lslrec.gui.setting.SettingOptions;

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
	
	private JComponent focusedComponent = null;
    private JComponent hoveredComponent = null;
    
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
		JScrollPane scr = new JScrollPane( optPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		
		JTextArea infoTx = new JTextArea(3, 0);	
		this.enableInfoTx( infoTx );
	
		JScrollPane scr2 = new JScrollPane( infoTx, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		scr2.setBorder( BorderFactory.createTitledBorder( Language.getLocalCaption( Language.INFO_TEXT )  ) );		
		scr2.getVerticalScrollBar().setUnitIncrement( 5 );		
		
		main.add( scr2, BorderLayout.NORTH );
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
	
	public void enableInfoTx(JTextArea targetArea) 
	{
        targetArea.setLineWrap(true);
        targetArea.setWrapStyleWord(true);
        targetArea.setEditable( false );
        targetArea.setBackground( GeneralSettings.COLOR_INFO );
        ToolTipManager.sharedInstance().setEnabled(false);        

        // 1. Listen for focus changes (keyboard / click)
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addPropertyChangeListener("focusOwner", evt -> 
	        {
	                Object newOwner = evt.getNewValue();
	                this.focusedComponent = (newOwner instanceof JComponent) ? (JComponent) newOwner : null;
	                updateText(targetArea, null);
	            });

        // 2. Listen for mouse movement
        Toolkit.getDefaultToolkit().addAWTEventListener(event -> 
        {
            if (event instanceof MouseEvent)
            {
                MouseEvent me = (MouseEvent) event;
                if (me.getID() != MouseEvent.MOUSE_MOVED)
                {
                	return;
                }

                Component comp = SwingUtilities.getDeepestComponentAt( me.getComponent(), me.getX(), me.getY() );

                JComponent jcomp = (comp instanceof JComponent) ? (JComponent) comp : null;
                
                if (jcomp != hoveredComponent) 
                {
                    this.hoveredComponent = jcomp;
                    updateText(targetArea, me);
                }
            }
        }, AWTEvent.MOUSE_MOTION_EVENT_MASK);
    }

    private void updateText(JTextArea targetArea, MouseEvent me) 
    {
        // Prioritize the hovered component; if it has no tooltip, fall back to the focused one
        if ( this.focusedComponent != null && this.focusedComponent.getToolTipText() != null) {
            targetArea.setText( this.focusedComponent.getToolTipText());
            return;
        }

        if( this.hoveredComponent != null) 
        {
            String hoverTip = (me != null) ? this.hoveredComponent.getToolTipText(me) : this.hoveredComponent.getToolTipText();
            if (hoverTip != null) {
                targetArea.setText(hoverTip);
                return;
            }
        }
        
        targetArea.setText("");
    }
}
