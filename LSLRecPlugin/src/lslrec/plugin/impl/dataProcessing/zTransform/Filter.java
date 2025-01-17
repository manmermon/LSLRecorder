/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.zTransform;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.math3.complex.Complex;

import lslrec.auxiliar.extra.Tuple;
import lslrec.config.Parameter;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

/**
 * @author Manuel Merino Monge
 *
 */
public class Filter extends LSLRecPluginDataProcessing
{
	private List< Double > outputs = new ArrayList< Double >();
	private List< Double > inputs = new ArrayList< Double >();

	private double[] b = new double[] { 1 };
	private double[] a = new double[] { 1 };

	private ZPlanePluginWindow window = null;
	
	private int BufferLen = 1;
	
	private Object lock  = new Object();
	
	private double Gain = 1;
	private double fgain = 0;
	
	public Filter(IStreamSetting setting, LSLRecPluginDataProcessing prevProc) 
	{
		super( setting, prevProc );
		
		this.window = new ZPlanePluginWindow( this );
		this.window.setVisible( true );
	}
		
	@Override
	public String getID() 
	{
		return this.getClass().getSimpleName();
	}
	
	@Override
	protected void finishProcess() 
	{	
		JFrame w = null;
		
		synchronized( this.lock )
		{
			w = this.window;
		}
		
		if( w != null )
		{
			w.dispose();
		}
	}

	@Override
	public int getBufferLength() 
	{
		synchronized ( this.lock )
		{
			return this.BufferLen;
		}		
	}

	@Override
	public int getOverlapOffset() 
	{
		return 1;
	}

	@Override
	public void loadProcessingSettings(List<Parameter<String>> arg0) 
	{		
	}

	@Override
	protected Number[] processData( Number[] x ) 
	{
		Number[] res = x;
		
		if( x != null && x.length > 0 )
		{
			res = this.process( x );
		}
		
		return res;
	}
	
	public synchronized void setZeroPoles( List< Marker > ZsPs )
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
			
			Tuple< Complex[], Complex[] > ab = this.getCoefs( zeros, poles );
			Complex[] _a = ab.t1;
			Complex[] _b = ab.t2;
			
			synchronized( this.lock )
			{
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
				
				this.BufferLen = this.b.length;
				
				this.setGain( this.Gain, this.fgain );
			}
		}
		else
		{
			synchronized( this.lock )
			{
				this.BufferLen = 1;
				
				this.b = new double[] { 1 };
				this.a = new double[] { 1 };
			}
		}
	}
	
	private Tuple< Complex[] , Complex[] > getCoefs( List< Marker > zeros, List< Marker > poles )
	{
		Complex unit = new Complex( 1, 0 );
		
		Complex[] _b = new Complex[] { unit };
		Complex[] _a = new Complex[] { unit };		
		for( Marker z : zeros )
		{
			Tuple< Double, Double > v = z.getValue();			
			
			Complex[] zero = new Complex[] { unit, new Complex( -v.t1, -v.t2 ) };
			
			_b = Utils.conv1D( _b, zero );
		}
		
		for( Marker p : poles )
		{
			Tuple< Double, Double > v = p.getValue();			
			
			Complex[] pole = new Complex[] { unit, new Complex( -v.t1, -v.t2 ) };
			
			_a = Utils.conv1D( _a, pole );
		}
		
		return new Tuple<Complex[], Complex[]>( _a, _b );
	}
	
	private Number[] process( Number[] x )
	{
		List< Number > ys = new ArrayList<Number>(); 
				
		synchronized( this.lock )
		{
			for( int i = 0; i < x.length; i++ )
			{
				double in = x[ i ].doubleValue();
				this.inputs.add( 0, in );
				
				while( this.inputs.size() > b.length )
				{
					this.inputs.remove( this.inputs.size() - 1 );
				}
										
				while( this.outputs.size() > a.length )
				{
					this.outputs.remove( this.outputs.size() - 1 );
				}
				
				double acumX = 0;
				for( int j = 0; j < b.length && j < this.inputs.size(); j++ )
				{
					double bj = this.b[ j ];
					double xj = this.inputs.get( j );
					
					acumX += bj * xj; 
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
		}
		
		return ys.toArray( new Number[0] );
	}

	public Tuple< double[], double[] > getFilterCoef()
	{
		return new Tuple<double[], double[]>( a, b );
	}
	
	public void setGain( double A, double fn )
	{
		this.fgain = fn;
		double w = 2 * Math.PI * fn;
		
		Complex z = new Complex( Math.cos( -w ), Math.sin( -w ) );
		
		int lenA = this.a.length;
		if( lenA < 2 )
		{
			lenA++;
		}
		
		int lenB = this.b.length;
		if( lenB < 2 )
		{
			lenB++;
		}
		
		double[] _a =  Arrays.copyOf( this.a, lenA );
		ArrayUtils.reverse( _a );
		
		double[] _b = Arrays.copyOf( this.b, lenB );				
		ArrayUtils.reverse( _b );
		
		synchronized ( this.lock )
		{
			Complex N = Utils.polyval( _b, z );
			Complex D = Utils.polyval( _a, z );
			
			double preGain = this.Gain;
			this.Gain = A * D.divide( N ).abs();
			
			if( Double.isNaN( this.Gain ) )
			{
				this.Gain = 1;
			}
			
			if( this.Gain == 0D )
			{
				this.Gain = preGain;
			}
			
			for( int i = 0; i < this.b.length; i++ )
			{
				this.b[ i ] *= this.Gain;
			}
		}				
	}
}
