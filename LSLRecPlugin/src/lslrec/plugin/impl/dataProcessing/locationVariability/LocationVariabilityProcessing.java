/* 
 * Copyright 2018-2026 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.plugin.impl.dataProcessing.locationVariability;

import java.util.Arrays;
import java.util.List;

import lslrec.config.Parameter;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

public class LocationVariabilityProcessing extends LSLRecPluginDataProcessing
{
	public static final String BUFFER_LEN = "Buffer length";
	public static final String SHIFT_LEN = "Shift length";
	public static final String FREQ = "Sampling rate";
	public static final String CUT_TAILS = "Cut tails";
	
	public enum CUT_TAILS_PERCENT { P0, P5, P10, P15, P20, P25, P50 } 
	
	private int bufLen = 2;
	private int shift = 1;
	private double frq = 1;
	private double cutTails = 0;
	
	public LocationVariabilityProcessing(IStreamSetting setting, LSLRecPluginDataProcessing prevProc) 
	{
		super(setting, prevProc);
	}

	@Override
	public String getID() 
	{
		return "LocationVariability";
	}

	@Override
	protected void finishProcess() 
	{	
	}

	@Override
	public int getBufferLength() 
	{
		return this.bufLen;
	}

	@Override
	public int getShiftOffset() 
	{
		return this.shift;
	}

	@Override
	public void loadProcessingSettings(List<Parameter<String>> pars ) 
	{
		for( Parameter< String > par : pars )
		{
			String id = par.getID();
			String val = par.getValue();
			
			try
			{
				switch( id ) 
				{					
					case BUFFER_LEN:
					{		
						int len = Integer.parseInt( val );
						this.bufLen = ( len > 0 ) ? len : 2;
						
						break;
					}
					case SHIFT_LEN:
					{	
						int len = Integer.parseInt( val );
						this.shift = ( len > 0 ) ? len : 1;
						
						break;
					}
					case FREQ:
					{	
						double sr= Double.parseDouble( val );
						this.frq = ( sr > 0 ) ? sr : 1;
						
						break;
					}
					case CUT_TAILS:
					{
						CUT_TAILS_PERCENT p = CUT_TAILS_PERCENT.valueOf( val );
						
						this.cutTails = 0;
						switch ( p ) 
						{
							case P5:
							{
								this.cutTails = 0.05;
								
								break;
							}
							case P10:
							{
								this.cutTails = 0.10;
								
								break;
							}
							case P15:
							{
								this.cutTails = 0.15;
								
								break;
							}
							case P20:
							{
								this.cutTails = 0.20;
								
								break;
							}
							case P25:
							{
								this.cutTails = 0.25;
								
								break;
							}
							case P50:
							{
								this.cutTails = 0.5;
								
								break;
							}
							default:
							{
								break;
							}
						}
						
						break;
					}
					default:
						break;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	protected Number[] processData( Number[] locs ) 
	{	
		Number[] res = new Number[0];
		
		if( locs != null && locs.length > 1  )
		{
			double[] loc2Time = new double[ locs.length - 1 ];
			
			int cut = (int)Math.round( this.cutTails * loc2Time.length ) / 2;
			
			for( int i = 1; i < locs.length; i++ )
			{
				loc2Time[ i-1 ]= (locs[ i ].doubleValue() - locs[ i - 1 ].doubleValue()) /this.frq;  
			}
			
			//System.out.println( Arrays.toString( locs ) );
			
			if( cut > 0 && 2*cut < loc2Time.length )
			{
				Arrays.sort( loc2Time );
				loc2Time = Arrays.copyOfRange( loc2Time, cut, loc2Time.length - cut );
			}
			
			double valSquare = 0;
			double mean = 0;
			for( double v : loc2Time )
			{
				valSquare += v * v;
				mean += v;
			}
			
			mean /= loc2Time.length;
			
			res = new Double[] { Math.sqrt( valSquare / loc2Time.length - mean * mean ) };
		}
		
		return res;
	}
}
