/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.firFilter;

import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.PI;
import static java.lang.Math.exp;
import static java.lang.Math.abs;


/**
 * @author Manuel Merino Monge
 *
 */
public class FilterWindow 
{
	enum WindowType { BARTLETT, BARTLETT_HANN, BLACKMAN, BLACKMAN_HARRIS, BOHMAN
						, FLATTOP, GAUSS, HAMMING, HANN, NUTTALL, PARZEN, RECT, TRIANGULAR
						//, CHEBYSHEV, KAISER, TUKEY
					}
	
	public static double[] getWindow( WindowType type, int L )
	{
		int len = L;
		if( L < 0 )
		{
			len = 0;
		}
		
		double[] w = new double[ len ];
		
		if( len > 0 )
		{
			for( int n = 0; n < len; n++ )
			{
				w[ n ] = 1;
			}
			
			double N_1 = ( len - 1D );
			
			switch ( type )
			{
				case BARTLETT:
				{
					for( int n = 0; n < len; n++ )
					{
						w[ n ] = 2D * n / N_1;
						
						if( n > (len+1) / 2 )
						{
							w[ n ] = 2D - w[ n ];  
						}
					}
					
					break;
				}
				case BARTLETT_HANN:
				{
					double a0 = 0.62, a1 = 0.48, a2 = 0.38;
					
					for( int n = 0; n < len; n++ )
					{
						double aux = ( n / N_1 ) - 0.5;
						w[ n ] = a0 - a1 * abs( aux ) + a2 * cos( 2 * PI * aux );
					}
					
					break;
				}
				case BLACKMAN:
				{	
					for( int n = 0; n < len; n++ )
					{
						double b = 2 * PI * n / N_1;
						
						w[ n ] = 0.42 - 0.5 * cos( b ) + 0.08 * cos( 2 * b ); 
					}
					
					break;
				}
				case BLACKMAN_HARRIS:
				{
					double a0 = 0.35875, a1 = 0.48829, a2 = 0.14128, a3 = 0.01168;
					
					for( int n = 0; n < len; n++ )
					{
						double b = 2 * PI * n / N_1;
						
						w[ n ] = a0 - a1 * cos( b ) + a2 * cos( 2 * b ) - a3 * cos( 3 * b ); 
					}
					
					break;
				}
				case BOHMAN:
				{
					for( int n = 0; n < len; n++ )
					{
						double a = abs( 2 * n / N_1 - 1 );
						
						w[ n ] = ( 1 - a ) * cos( PI * a ) + sin( PI * a ) / PI; 
					}
					
					break;
				}	
				case FLATTOP:
				{
					double a0 = 0.21557895, a1 = 0.41663158, a2 = 0.277263158, a3 = 0.083578947, a4 = 0.006947368;
					for( int n = 0; n < len; n++ )
					{
						double b = 2 * PI * n / N_1;
						
						w[ n ] = a0 - a1 * cos( b ) + a2 * cos( 2 * b ) - a3 * cos( 3 * b ) + a4 * cos( 4 * b ); 
					}
					
					break;
				}
				case GAUSS:
				{	
					double sigma = 0.5;
					
					for( int n = 0; n < len; n++ )
					{
						double ex = n - N_1 / 2;
						ex = ex / ( sigma * N_1 / 2);
						ex = -0.5 * ( ex * ex );
						
						w[ n ] = exp( ex ); 
					}
					
					break;
				}		
				case HAMMING:
				{	
					for( int n = 0; n < len; n++ )
					{
						w[ n ] = 0.53836 - 0.46164 * cos( 2 * PI * n / N_1 );
					}
					
					break;
				}
				case HANN:
				{	
					for( int n = 0; n < len; n++ )
					{
						w[ n ] = 0.5 - 0.5 * cos( 2 * PI * n / N_1 ); 
					}
					
					break;
				}
				case NUTTALL:
				{
					double a0 = 0.3635819, a1 = 0.4891775, a2 = 0.1365995, a3 = 0.0106411;
					
					for( int n = 0; n < len; n++ )
					{
						double b = 2 * PI * n / len;
						
						w[ n ] = a0 - a1 * cos( b ) + a2 * cos( 2 * b ) - a3 * cos( 3 * b ); 
					}
					
					break;
				}
				case PARZEN:
				{
					for( int i = 0; i < len; i++ )
					{
						double n = abs( i - N_1 / 2 );
						double a = n / ( len / 2 );
						
						if( n >= 0 && n <= N_1 / 4 )
						{
							w[ i ] = 1 - 6 * a * a *( 1 - a); 
						}
						else
						{						
							w[ i ] =  2 * Math.pow( 1 - a, 3 );
						}
					}
					
					break;
				}
				case TRIANGULAR:
				{	
					for( int n = 0; n < len; n++ )
					{
						w[ n ] = 1 - abs( n -  N_1 / 2 )  / ( 0.5 * N_1 ); 
					}
					
					break;
				}
				default:
				{
					break;
				}
			}
		}
		
		return w;
	}
	
}
