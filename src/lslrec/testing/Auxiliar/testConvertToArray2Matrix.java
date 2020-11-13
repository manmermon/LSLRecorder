package lslrec.testing.Auxiliar;

import java.util.Arrays;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.auxiliar.extra.Tuple;


public class testConvertToArray2Matrix {

	public static void main(String[] args) 
	{
		Integer[] ar = new Integer[ 13 ];
		for( int i =0; i < ar.length; i++ )
		{
			ar[ i ] = i;
		}
		
		Integer[] ar2 = new Integer[ 13 ];
		for( int i =0; i < ar.length; i++ )
		{
			ar2[ i ] = -i;
		}
		
		for( int i = 0; i < 15; i++ )
		{
			System.out.println( "Cols = " + i );
			Tuple< Number[][], Number[] > r = ConvertTo.Transform.Array2Matrix( ar, i );
			System.out.println(Arrays.deepToString( 
					ConvertTo.Casting.NumberMatrix2ByteMatrix( r.t1 ) ) +
								"\n " + Arrays.toString( r .t2 ));
		}
				
	}

}
