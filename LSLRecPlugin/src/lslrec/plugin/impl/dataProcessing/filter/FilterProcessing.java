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
package lslrec.plugin.impl.dataProcessing.filter;

import java.util.ArrayList;
import java.util.List;

import lslrec.config.Parameter;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

/**
 * @author Manuel Merino Monge
 *
 */
public class FilterProcessing extends LSLRecPluginDataProcessing
{
	public static final String COEF_A = "coef. A";	
	public static final String COEF_B = "coef. B";
	
	private List< Double > outputs = new ArrayList< Double >();
	private List< Double > inputs = new ArrayList< Double >();

	private Double[] b = new Double[] { 1D };
	private Double[] a = new Double[] { 1D };

	private int BufferLen = 1;
	
	private Object lock  = new Object();
		
	public FilterProcessing(IStreamSetting setting, LSLRecPluginDataProcessing prevProc) 
	{
		super( setting, prevProc );
	}
		
	@Override
	public String getID() 
	{
		return this.getClass().getSimpleName();
	}
	
	@Override
	protected void finishProcess() 
	{			
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
	public int getShiftOffset() 
	{
		return 1;
	}

	@Override
	public void loadProcessingSettings(List<Parameter<String>> arg0) 
	{	
		if( arg0 != null )
		{
			for( Parameter< String > p : arg0 )
			{
				String id = p.getID();
				String val = p.getValue();

				switch( id ) 
				{
					case COEF_A:
					{						
						List< Double > coef_a = FilterUtils.parseFilterCoef( val );
						
						if( coef_a.size() > 0 )
						{
							synchronized( this.lock )
							{
								this.a = coef_a.toArray( new Double[0] );
							}
						}
						
						break;
					}
					case COEF_B:
					{
						List< Double > coef_b = FilterUtils.parseFilterCoef( val );
						
						if( coef_b.size() > 0 )
						{
							synchronized( this.lock )
							{
								this.b = coef_b.toArray( new Double[0] );
							}
						}
						
						break;
					}
					default:
					{
						break;
					}
				}
			}
		}
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
}
