/**
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
