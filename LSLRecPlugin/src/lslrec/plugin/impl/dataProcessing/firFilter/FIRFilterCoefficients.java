/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.firFilter;

import lslrec.plugin.impl.dataProcessing.firFilter.FilterWindow.WindowType;

/**
 * @author Manuel Merino Monge
 *
 */
public class FIRFilterCoefficients 
{
	public static double[] FIRLowpassFilterCoefficients( int L, double fc, WindowType wt )
	{
		double[] h = null;
		
		if( L < 0 )
		{
			h = new double[ 0 ];
		}
		else
		{
			h = new double[ L ];
				
			if( fc >= 0 )
			{				
				fc = fc - (int)( fc );
				
				double[] w = FilterWindow.getWindow( wt, L );

				for( int i = 0; i < h.length; i++ )
				{
					double n = i - ( L - 1 ) / 2;

					// Low filter
					double lf = 2 * fc * sinc( 2 * fc * Math.PI * n );

					h[ h.length - i - 1 ] = lf * w[ i ];
				}
			}
		}
		
		return h;
	}
	
	public static double[] FIRHighpassFilterCoefficients( int L, double fc, WindowType wt )
	{
		double fh = fc - (int)( fc );
		fh = 0.5 - fh;
		
		double[] h = FIRLowpassFilterCoefficients( L, fh, wt );
		for( int i = 0; i < h.length; i++ )
		{
			h[ i ] *= Math.pow( -1, i ); 
		}
				
		return h;
	}
	
	public static double[] FIRBandpassFilterCoefficients( int L, double fc1, double fc2, WindowType wt )
	{
		double[] h = new double[ 0 ];
				
		if( fc1 <= fc2 && fc1 >= 0)
		{	
			
			double fcl = 0.5 * ( fc2 - fc1 );
			double f0 = 0.5 * ( fc2 + fc1 );
			
			h = FIRLowpassFilterCoefficients( L, fcl, wt );
			
			double aux = 2 * Math.PI;
			for( int i = 0; i < h.length; i++ )
			{
				double n = ( L - 1D  ) / 2D - i; // h is inverted order
				h[ i ] = h[ i ] * 2 * Math.cos( aux * f0 * n );
			}
		}
				
		return h;
	}
	
	public static double[] FIRNotchFilterCoefficients( int L, double fc1, double fc2, WindowType wt )
	{
		double[] h = new double[ 0 ];
		
		if( fc1 <= fc2 && fc1 >= 0)
		{	
			h = FIRLowpassFilterCoefficients( L, fc1, wt );			
			double[] hh = FIRHighpassFilterCoefficients( L, fc2, wt );
			
			for( int i = 0; i < h.length; i++ )
			{
				h[ i ] += hh[ i ];
			}
		}
		
		return h;
	}
	
	private static double sinc( double x )
	{
		double r = 1;
		
		if( x != 0 )
		{
			r = Math.sin( x ) / x;
		}
		
		return r;
	}

}
