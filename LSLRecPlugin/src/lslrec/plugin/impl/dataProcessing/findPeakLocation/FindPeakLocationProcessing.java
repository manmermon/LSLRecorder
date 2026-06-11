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
package lslrec.plugin.impl.dataProcessing.findPeakLocation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lslrec.auxiliar.extra.ConvertTo.Casting;
import lslrec.config.Parameter;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

public class FindPeakLocationProcessing extends LSLRecPluginDataProcessing 
{
	public static final String MIN_DISTANCE = "Min. distance in samples";
	public static final String MIN_PROMINENCE = "Min. prominence";	
	public static final String BUFFER_LEN = "Buffer length in samples";
	public static final String SHIFT_LEN = "Shift length in samples";
	public static final String PEAK_TYPE = "Peak type";
	public static final String ADJUST_SHIFTING = "Adjust shifting";
	
	public enum Peak_type { MIN, MAX, MIN_MAX };
	
	private int minDist = 0;
	private double minProminence = 0;
	private int bufLen = 1;
	private int shift = 1;
	private Peak_type peakType = Peak_type.MIN_MAX;
	
	private boolean adjust = false;
	private int winShift = 0;
	
	public FindPeakLocationProcessing(IStreamSetting setting, LSLRecPluginDataProcessing prevProc) 
	{
		super(setting, prevProc);
	}

	@Override
	public String getID() 
	{
		return "PeakInterval";
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
					case MIN_DISTANCE:
					{	
						double dist = Double.parseDouble( val );
						this.minDist = ( dist >= 0 ) ? (int)dist : 0;
						
						break;
					}
					case MIN_PROMINENCE:
					{	
						double pr = Double.parseDouble( val );
						this.minProminence = ( pr >= 0 ) ? pr : 0;
						
						break;
					}
					case BUFFER_LEN:
					{		
						int len = (int)(Double.parseDouble( val ) );
						this.bufLen = ( len > 0 ) ? len : 1;
						
						break;
					}
					case SHIFT_LEN:
					{	
						int len = (int)(Double.parseDouble( val ) );
						this.shift = ( len > 0 ) ? len : 1;
						
						break;
					}
					case ADJUST_SHIFTING:
					{	
						this.adjust = Boolean.parseBoolean( val );
						
						break;
					}
					case PEAK_TYPE:
					{
						this.peakType= Peak_type.valueOf( val );
												
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
	protected Number[] processData(Number[] in ) 
	{
		Number[] res = new Number[0];
		
		if( in != null )
		{
			List< Integer > peakLocs = new ArrayList<Integer>();
			
			double[] signal = Casting.NumberArray2DoubleArray( in );
			
			if( this.peakType.equals( Peak_type.MAX ) || this.peakType.equals( Peak_type.MIN_MAX ) )
			{
				peakLocs.addAll( this.findMaxPeaks( signal ) );
			}
			
			if( this.peakType.equals( Peak_type.MIN ) || this.peakType.equals( Peak_type.MIN_MAX ) )
			{
				peakLocs.addAll( this.findMinPeaks( signal ) );
			}
			
			if( this.peakType.equals( Peak_type.MIN_MAX ) )
			{
				Collections.sort( peakLocs );
			}
			res = peakLocs.toArray( new Number[0] );
			
			this.winShift += ( this.getShiftOffset() * ( this.adjust ? 1 : 0 ) );
		}
		
		return res;
	}
	
	// =========================================================
    // FIND MAX PEAKS
    // =========================================================
	//
	// By Chatgpt
	//
    private List< Integer > findMaxPeaks( double[] signal )
    {
        List< Integer > peaks = new ArrayList< Integer >();

        int lastPeak = -this.minDist;

        for (int i = 1; i < signal.length - 1; i++) 
        {
            // m�ximo local
            boolean isPeak = signal[i] > signal[i - 1] 
            					&& signal[i] >= signal[i + 1];

            if (!isPeak)
            {
            	continue;
            }

            // -------------------------------------------------
            // PROMINENCE
            // -------------------------------------------------

            double leftMin = signal[i];
            double rightMin = signal[i];

            // buscar valle izquierda
            for (int l = i - 1; l >= 0; l--) 
            {
                if (signal[l] > signal[i])
                {
                	break;
                }

                leftMin = Math.min(leftMin, signal[l]);
            }

            // buscar valle derecha
            for (int r = i + 1; r < signal.length; r++) 
            {
                if (signal[r] > signal[i])
                {
                	break;
                }

                rightMin = Math.min(rightMin, signal[r]);
            }

            double prominence = signal[i] - Math.max(leftMin, rightMin);

            if (prominence < this.minProminence)
            {
            	continue;
            }

            // -------------------------------------------------
            // MIN DISTANCE
            // -------------------------------------------------

            if ( (i - lastPeak)  < this.minDist) 
            {

                // conservar el mayor
                int previous = peaks.get(peaks.size() - 1) - this.winShift;

                if (signal[i] > signal[ previous ] ) 
                {
                    peaks.remove( peaks.size() - 1 );

                } 
                else 
                {
                    continue;
                }
            }

            peaks.add( i + this.winShift );
            lastPeak = i;
        }

        return peaks;
    }
	
    // =========================================================
    // FIND MIN PEAKS
    // =========================================================
    //
	// By Chatgpt
	//
    private List< Integer > findMinPeaks( double[] signal )
    {
        double[] inverted = new double[signal.length];

        for (int i = 0; i < signal.length; i++) 
        {
            inverted[i] = -signal[i];
        }
        
        return this.findMaxPeaks( inverted );
    }

}
