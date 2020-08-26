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

package gui;

import java.awt.Window;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;

public class keyActions 
{
	public static Action getButtonClickAction( final String ID, final AbstractButton b ) 
	{		
		return  new AbstractAction()
				{
					{ super.putValue( ID, ID);}	
					private static final long serialVersionUID = 1L;
	
					@Override
					public void actionPerformed(ActionEvent arg0) 
					{
						b.doClick();
					}
				};
	}
	
	public static Action getEscapeCloseWindows( final String ID )  
    {  
		return new AbstractAction()
				{
					private static final long serialVersionUID = 1L;
			
					{ super.putValue( ID, ID); }  
			   
			        public void actionPerformed(ActionEvent e)  
			        {  
			            JComponent source = (JComponent)e.getSource();  
			            Window window = SwingUtilities.getWindowAncestor( source ); 
			            window.dispose();  
			        }  
			    }; 
    }
}
