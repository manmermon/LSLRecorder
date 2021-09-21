/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.zTransform;

import java.util.Arrays;

import org.apache.commons.math3.complex.Complex;

/**
 * @author Manuel Merino Monge
 *
 */
public class Convolution 
{
	/**
	 * 
	 */
	public static double[] conv1D( double[] x, double[] y ) 
	{
		double[] res = new double[ 0 ];
		
		if( x != null && y != null 
				&& x.length > 0 && y.length > 0 )
		{
			res = new double[ x.length + y.length - 1 ];
			
			double[] refShortest = x;
			double[] refLargest = y;
			
			if( x.length > y.length )
			{
				refShortest = y;
				refLargest = x;
			}
			
			int shortestLen = refShortest.length;			
			int endLargest = 0;
			int startLargest = endLargest - shortestLen;
			int indexShortest = 0;
			for( int iter = 0; iter < res.length; iter++ )
			{
				double acum = 0;
				
				int i = endLargest;
				for( ; i > startLargest && i >= 0 && i <= endLargest && indexShortest < shortestLen
					 ; i--, indexShortest++ )
				{					
					double vL = refLargest[ i ];
					double vS = refShortest[ indexShortest ];
					
					acum += vS * vL;
				}
				
				endLargest++;
				if( endLargest >= refLargest.length )
				{
					endLargest--;
				}
				startLargest++;
				
				indexShortest = 0;
				if( endLargest - startLargest < shortestLen )
				{
					indexShortest = shortestLen - endLargest + startLargest;
				}
				
				res[ iter ] = acum;
			}
		}
		
		//System.out.println("Convolution.conv1D() " + Arrays.toString( res ) );
		
		return res;
	}
	
	public static Complex[] conv1D( Complex[] x, Complex[] y )
	{
		Complex[] res = new Complex[ 0 ];
		
		if( x != null && y != null 
				&& x.length > 0 && y.length > 0 )
		{
			res = new Complex[ x.length + y.length - 1 ];
			
			Complex[] refShortest = x;
			Complex[] refLargest = y;
			
			if( x.length > y.length )
			{
				refShortest = y;
				refLargest = x;
			}
			
			int shortestLen = refShortest.length;			
			int endLargest = 0;
			int startLargest = endLargest - shortestLen;
			int indexShortest = 0;
			for( int iter = 0; iter < res.length; iter++ )
			{
				Complex acum = new Complex( 0, 0 );
				
				int i = endLargest;
				for( ; i > startLargest && i >= 0 && i <= endLargest && indexShortest < shortestLen
					 ; i--, indexShortest++ )
				{					
					Complex vL = refLargest[ i ];
					Complex vS = refShortest[ indexShortest ];
					
					Complex mult = vL.multiply( vS );
										
					acum = acum.add( mult );
				}
				
				endLargest++;
				if( endLargest >= refLargest.length )
				{
					endLargest--;
				}
				startLargest++;
				
				indexShortest = 0;
				if( endLargest - startLargest < shortestLen )
				{
					indexShortest = shortestLen - endLargest + startLargest;
				}
				
				res[ iter ] = acum;
			}
		}
		
		return res;
	}
}
