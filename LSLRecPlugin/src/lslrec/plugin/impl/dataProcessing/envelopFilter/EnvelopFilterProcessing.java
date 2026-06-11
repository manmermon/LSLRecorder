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
package lslrec.plugin.impl.dataProcessing.envelopFilter;

import java.util.ArrayList;
import java.util.List;

import lslrec.config.Parameter;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.plugin.impl.dataProcessing.findPeakLocation.FindPeakLocationProcessing;
import lslrec.plugin.impl.dataProcessing.findPeakLocation.FindPeakLocationProcessing.Peak_type;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

public class EnvelopFilterProcessing  extends LSLRecPluginDataProcessing
{
	public static final String ENVELOP_TYPE = "Envelop type";
	public static final String PEAK_DIST = "Min. peak distance in samples";
	public static final String BUFFER_LEN = "Buffer length in samples";
	public static final String SHIFT_LEN = "Shift length in samples";
		
	public enum Envelop_type { LOWER, UPPER }; 
	
	private FindPeakLocationProcessing findPeaks = null; 
	
	public EnvelopFilterProcessing(IStreamSetting setting, LSLRecPluginDataProcessing prevProc) 
	{
		super(setting, prevProc);
		
		this.findPeaks = new FindPeakLocationProcessing( setting, null );
	}

	@Override
	public String getID() 
	{
		return "EnvelopFilter";
	}

	@Override
	protected void finishProcess() 
	{		
	}

	@Override
	public int getBufferLength() 
	{
		return this.findPeaks.getBufferLength();
	}

	@Override
	public int getShiftOffset() 
	{
		return this.findPeaks.getShiftOffset();
	}

	@Override
	public void loadProcessingSettings(List<Parameter<String>> arg0) 
	{
		List< Parameter< String > > findPeaksPars = new ArrayList<Parameter<String>>();
		
		for( Parameter< String > par : arg0 )
		{
			String id = par.getID();
			String val = par.getValue();
			
			switch ( id )
			{
				case ENVELOP_TYPE:
				{
					Envelop_type type = Envelop_type.valueOf( val );
					
					FindPeakLocationProcessing.Peak_type peakType = Peak_type.MIN;
					if( type.equals( Envelop_type.UPPER ) )
					{
						peakType = Peak_type.MAX;
					}
					
					findPeaksPars.add( new Parameter<String>( FindPeakLocationProcessing.PEAK_TYPE, peakType.name() ) );
					
					break;
				}
				case PEAK_DIST:
				{
					findPeaksPars.add( new Parameter<String>( FindPeakLocationProcessing.MIN_DISTANCE, (int)( Double.parseDouble( val ) ) + "" ) );
					
					break;
				}
				case BUFFER_LEN:
				{		
					int len = (int)(Double.parseDouble( val ) );
					findPeaksPars.add( new Parameter<String>( FindPeakLocationProcessing.BUFFER_LEN, len + "" ) );
					
					break;
				}
				case SHIFT_LEN:
				{	
					int len = (int)(Double.parseDouble( val ) );
					findPeaksPars.add( new Parameter<String>( FindPeakLocationProcessing.SHIFT_LEN, len + "" ) );
					
					break;
				}
				default:
				{
					break;
				}
			}
		}
		
		this.findPeaks.loadProcessingSettings( findPeaksPars );
	}

	@Override
	protected Number[] processData(Number[] signal ) 
	{
			Number[] peakLocs = this.findPeaks.processDataBlock( signal );
			
			double[] px = new double[ peakLocs.length ];
			double[] py = new double[ px.length ];
			
			for (int i = 0; i < peakLocs.length; i++) 
			{
			    px[i] = peakLocs[i].doubleValue();
			    py[i] = signal[ peakLocs[i].intValue() ].doubleValue();
			}
			
			PchipInterpolator pchip = new PchipInterpolator( px, py );
			
			Double[] res = new Double[ signal.length ];
			
			for( int i = 0; i < signal.length; i++ )
			{
				res[ i ] = signal[ i ].doubleValue() - pchip.interpolate( i );
			}
			
			return res;
	}	
}
