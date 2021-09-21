/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.zTransform;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.complex.Complex;

import lslrec.auxiliar.extra.Tuple;

/**
 * @author Manuel Merino Monge
 *
 */
public class Filter 
{
	private List< Double > outputs = new ArrayList< Double >();
	private List< Double > inputs = new ArrayList< Double >();

	private double[] b = new double[] { 1 };
	private double[] a = new double[] { 1 };
		
	public void setZeroPoles( List< Marker > ZsPs )
	{
		if( ZsPs != null && !ZsPs.isEmpty() )
		{
			List< Marker > zeros = new ArrayList< Marker >();
			List< Marker > poles = new ArrayList< Marker >();
			for( Marker m : ZsPs )
			{
				if( m.getType() == Marker.Type.ZERO )
				{
					zeros.add( m );
				}
				else
				{
					poles.add( m );
				}
			}
						
			Complex unit = new Complex( 1, 0 );
			
			Complex[] _b = new Complex[] { unit };
			Complex[] _a = new Complex[] { unit };
			for( Marker z : zeros )
			{
				Tuple< Double, Double > v = z.getValue();			
				
				Complex[] zero = new Complex[] { unit, new Complex( -v.t1, -v.t2 ) };
				
				_b = Convolution.conv1D( _b, zero );
			}
			
			for( Marker p : poles )
			{
				Tuple< Double, Double > v = p.getValue();			
				
				Complex[] pole = new Complex[] { unit, new Complex( -v.t1, -v.t2 ) };
				
				_a = Convolution.conv1D( _a, pole );
			}
			
			this.b = new double[ _b.length ];		
			for( int i = 0; i < b.length; i++ )
			{
				Complex c = _b[ i ]; 
				this.b[ i ] = c.getReal();
			}
			
			this.a = new double[ _a.length ];
			for( int i = 0; i < a.length; i++ )
			{
				Complex c = _a[ i ]; 
				this.a[ i ] = c.getReal();
			}
			
			if( this.a.length == 0 )
			{
				this.a = new double[] { 1D };
			}
		}
		else
		{
			b = new double[] { 1 };
			a = new double[] { 1 };
		}
	}
	
	public Number[] process( Number[] x )
	{
		List< Number > ys = new ArrayList<Number>(); 
				
		for( int i = 0; i < x.length; i++ )
		{
			double in = x[ i ].doubleValue();
			this.inputs.add( 0, in );
			
			while( this.inputs.size() > b.length )
			{
				this.inputs.remove( this.inputs.size() - 1 );
			}
			
			double acumX = 0;
			for( int j = 0; j < b.length && j < this.inputs.size(); j++ )
			{
				double bj = this.b[ j ];
				double xj = this.inputs.get( j );
				
				acumX += bj * xj; 
			}
			
			while( this.outputs.size() > a.length )
			{
				this.outputs.remove( this.outputs.size() - 1 );
			}
			
			double out = acumX;				
			if( this.a.length > 1 )
			{					
				double acumY = 0;
				for( int j = 1; j < this.a.length && (j-1) < this.outputs.size(); j++ )
				{
					double aj = this.a[ j ];
					double yj = this.outputs.get( j - 1 );
					
					acumY += aj * yj; 
				}
				
				out -= acumY; 
			}
			
			this.outputs.add( 0, out );
			ys.add( out );				
		}
		
		return ys.toArray( new Number[0] );
	}
}
