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
package lslrec.gui;

import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;

import lslrec.auxiliar.extra.ArrayTreeMap;
import lslrec.config.language.Language;

public class GuiLanguageManager 
{
	public static final String BORDER = "border";
	public static final String TOOLTIP = "tooltip";
	public static final String TEXT = "text";
	
	private static Map< String, ArrayTreeMap< String, Object > > components = new HashMap<String, ArrayTreeMap< String, Object> >();
	
	public static void addComponent( String cateogry, String idTranslateToken, Object c )
	{
		ArrayTreeMap< String, Object > cs = components.get( cateogry );
		
		if( cs == null )
		{
			cs = new ArrayTreeMap< String, Object >();
			components.put( cateogry, cs );
		}
		
		cs.putElement(idTranslateToken, c );		
	}
	
	public static void clear()
	{
		components.clear();
	}
	
	public static synchronized String getTranslateToken( Object c )
	{
		String token = null;
		
		synchronized ( components)
		{
			outerloop:
			{
				for( ArrayTreeMap< String, Object > Comps : components.values() )
				{
					for( String tk : Comps.keySet() )
					{
						if( Comps.get( tk ).contains( c ) )
						{
							token = tk;
							break outerloop;
						}
					}
				}
			}
		}
		
		return token;
	}
	
	public static void removeComponent( Object c )
	{
		for( ArrayTreeMap< String , Object > Comps : components.values() )
		{
			for( List< Object > cs : Comps.values() )
			{
				cs.remove( c );
			}
		}
	}
	
	public static void removeComponent( String idTranslateToken, Object c )
	{
		for( ArrayTreeMap< String , Object > Comps : components.values() )
		{			
			List< Object > cs = Comps.get( idTranslateToken);
			if( cs != null )
			{
				cs.remove( c );
			}
		}
	}
	
	public static void removeComponent( String category, String idTranslateToken, Object c )
	{
		ArrayTreeMap< String , Object > Comps = components.get( category );
		if( Comps != null )
		{			
			List< Object > cs = Comps.get( idTranslateToken);
			if( cs != null )
			{
				cs.remove( c );
			}
		}
	}
	
	public static void clearCategory( String category )
	{
		components.remove( category );
	}
	
	public static void removeTranslateToken( String idTransToken ) 
	{
		for( ArrayTreeMap< String , Object > Comps : components.values() )
		{
			Comps.remove( idTransToken );
		}
	}
	
	public static void removeTranslateToken( String category, String idTransToken ) 
	{
		ArrayTreeMap< String, Object > cs = components.get( category );
		
		if( cs != null )
		{
			if( idTransToken != null )
			{
				cs.remove( idTransToken );
			}
		}
	}
	
	public static void changeLanguage( String lang )
	{
		Language.changeLanguage( lang );
		
		for( String category : components.keySet() )
		{
			ArrayTreeMap< String , Object > Comps = components.get( category );
						
			for( String langToken : Comps.keySet() )
			{
				String txt = Language.getLocalCaption( langToken );

				for( Object c : Comps.get( langToken ) )
				{
					if( category.equalsIgnoreCase( TEXT ) )
					{
						changeText( c, txt );
					}
					else if( category.equalsIgnoreCase( TOOLTIP ) )
					{
						changeToolTip( c, txt );
					}
					else if( category.equalsIgnoreCase( BORDER ) )
					{
						changeBorder( c, txt );
					}
				}
			}
		}		
	}
	
	private static void changeText( Object cs, String txt )
	{
		if( cs instanceof AbstractButton )
		{
			((AbstractButton)cs).setText( txt );
		}
		else if( cs instanceof JTextComponent )
		{
			((JTextComponent)cs).setText( txt );
		}
		else if( cs instanceof JLabel )
		{
			((JLabel)cs).setText( txt );
		}
		else if( cs instanceof TableColumn )
		{
			((TableColumn)cs).setHeaderValue( txt );
		}
		else if( cs instanceof DefaultMutableTreeNode )
		{
			((DefaultMutableTreeNode)cs).setUserObject( txt );
		}
		else
		{
			try
			{
				JTabbedPane tabbedPane = (JTabbedPane)SwingUtilities.getAncestorOfClass( JTabbedPane.class, (Component)cs);
				if( tabbedPane != null )
				{
					for( int i = 0; i < tabbedPane.getTabCount(); i++ ) 
					{
						if( SwingUtilities.isDescendingFrom( (Component)cs , tabbedPane.getComponentAt( i ) ) ) 
						{
					        tabbedPane.setTitleAt( i, txt);
					        break;
						}
					}
				}
			}
			catch( Exception e )
			{}
		}
	}
	
	private static void changeBorder( Object cs, String txt )
	{
		if( cs instanceof TitledBorder )
		{
			((TitledBorder)cs).setTitle( txt );
		}
	}
	
	private static void changeToolTip( Object cs, String txt )
	{
		if( cs instanceof JComponent )
		{
			((JComponent)cs).setToolTipText( txt );
		}
	}
}
