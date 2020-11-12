/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec. https://github.com/manmermon/LSLRecorder
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
