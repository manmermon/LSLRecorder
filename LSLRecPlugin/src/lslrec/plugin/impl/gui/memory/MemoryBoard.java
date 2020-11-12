/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.plugin.impl.gui.memory;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import lslrec.plugin.impl.gui.BasicPainter2D;

/**
 * @author Manuel Merino Monge
 *
 */
public class MemoryBoard 
{
	public static void setMemoryPanel( int[][] memoryMatrix, JPanel container )
	{
		if( memoryMatrix != null && container != null )
		{
			container.removeAll();
			container.setLayout( new GridLayout( memoryMatrix.length, memoryMatrix[ 0 ].length ) );
			
			for (int i = 0; i < memoryMatrix.length; i++)
			{
				for( int j = 0; j < memoryMatrix[0].length; j++ )
				{
					final MemoryButton b = new MemoryButton( i, j, memoryMatrix[ i ][ j ] );
		
					b.setBackground( Color.WHITE );
		
					b.setBorder( BorderFactory.createEtchedBorder() );
		
					b.setFocusable( false );
					b.setFocusPainted( false );
				
					final int indI = i, indJ = j;
					b.addComponentListener(new ComponentAdapter()
					{
						public void componentResized( ComponentEvent e)
						{
							b.setIcon( getFigure( b.getSize(), memoryMatrix[ indI ][ indJ ] ) );
						}
		
					});
					
					container.add( b );
				}
			}
		}
	}
	
	public static Icon getFigure( Dimension d, int fig )
	{
		ImageIcon icon = null;

		Color colorFig = Color.WHITE;
		if (fig % 2 == MemoryMatrix.COLOR_BLACK )
		{
			fig--;
			colorFig = Color.BLACK;
		}

		int w = d.width;int h = d.height;
		int L = w;
		if (w > h)
		{
			L = h;
		}

		L /= 2;

		switch (fig)
		{
			case MemoryMatrix.SHAPE_CIRCLE:
			{
				icon = new ImageIcon( BasicPainter2D.circumference( 0, 0, L, 1F, Color.BLACK, 
										BasicPainter2D.circle( 0, 0, L, colorFig, null) ) );
				break;
	
			}
			case MemoryMatrix.SHAPE_SQUARE:
			{
				icon = new ImageIcon( BasicPainter2D.rectangle( L, L, 1.0F, Color.BLACK, colorFig ) );
				break;
	
			}
			case MemoryMatrix.SHAPE_DIAMOND:
			{
				icon = new ImageIcon( BasicPainter2D.diamond(  L, 1.0F, Color.BLACK, colorFig ) );
				break;
			} 
			default: 
			{
				icon = new ImageIcon( BasicPainter2D.triangle( L, 1.0F, Color.BLACK, colorFig, 0 ) );
			}
		}

		return icon;
	}
	
	
}
