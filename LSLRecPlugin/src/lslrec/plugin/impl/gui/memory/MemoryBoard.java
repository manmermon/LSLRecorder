/**
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

import lslrec.plugin.impl.gui.basicPainter2D;

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
				icon = new ImageIcon( basicPainter2D.circumference( 0, 0, L, 1F, Color.BLACK, 
										basicPainter2D.circle( 0, 0, L, colorFig, null) ) );
				break;
	
			}
			case MemoryMatrix.SHAPE_SQUARE:
			{
				icon = new ImageIcon( basicPainter2D.rectangle( L, L, 1.0F, Color.BLACK, colorFig ) );
				break;
	
			}
			case MemoryMatrix.SHAPE_DIAMOND:
			{
				icon = new ImageIcon( basicPainter2D.diamond(  L, 1.0F, Color.BLACK, colorFig ) );
				break;
			} 
			default: 
			{
				icon = new ImageIcon( basicPainter2D.triangle( L, 1.0F, Color.BLACK, colorFig, 0 ) );
			}
		}

		return icon;
	}
	
	
}
