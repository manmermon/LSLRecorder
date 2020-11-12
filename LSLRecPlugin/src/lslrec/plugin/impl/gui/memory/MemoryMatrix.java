/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.plugin.impl.gui.memory;

import java.awt.Point;

/**
 * @author Manuel Merino Monge
 *
 */
public class MemoryMatrix 
{
	public static final int SHAPE_CIRCLE = 0;
	public static final int SHAPE_SQUARE = 2;
	public static final int SHAPE_DIAMOND = 4;
	public static final int SHAPE_TRIANGLE = 6;
	
	public static final int COLOR_WHITE = 0;
	public static final int COLOR_BLACK = 1;

	private int[][] matrix = null;
	private Point matrixSize;
	
	public MemoryMatrix(int f, int c )
	{
		this.matrix = new int[ f ][ c ];
		
		this.matrixSize = new Point( f, c );
	}

	public Point getMatrixSize()
	{
		return this.matrixSize;
	}
	
	public int[][] getTask()
	{
		for (int i = 0; i < this.matrix.length; i++)
		{
			for( int j = 0; j < this.matrix[ 0 ].length; j++ )
			{
				int color = (int)Math.round(Math.random()); //0 -> WHITE; 1 -> BLACK
				int figure = (int)Math.floor(Math.random() * 4.0D);
			
				if (figure == 4)
				{
					figure = 3;
				}
	
				this.matrix[ i ][ j ] = (2 * figure + color);
				//0->whilte circle; 1-> black circle; 2 ->white square; 3 -> black square, etc.
			}
		}
		
		return this.matrix;
	}

	public static int[] getMemorySet()
	{
		int[] memory = new int[ 8 ];

		for (int i = 0; i < memory.length; i++)
		{
			memory[i] = i;
		}

		return memory;
	}
}
