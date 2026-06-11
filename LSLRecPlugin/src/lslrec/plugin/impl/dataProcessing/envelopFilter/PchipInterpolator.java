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

package lslrec.plugin.impl.dataProcessing.envelopFilter;

//
// by Chatgpt
//
public class PchipInterpolator {

    private final double[] x;
    private final double[] y;
    private final double[] m;

    public PchipInterpolator(double[] x, double[] y) 
    {
        if (x.length != y.length)
        {
        	throw new IllegalArgumentException();
        }

        if (x.length < 2)
        {
        	throw new IllegalArgumentException();
        }

        this.x = x.clone();
        this.y = y.clone();

        this.m = computeSlopes(x, y);
    }

    private static double[] computeSlopes( double[] x, double[] y)
    {
        int n = x.length;

        if( n < 3 )
        {
        	return x;
        }        
        
        double[] h = new double[n - 1];
        double[] d = new double[n - 1];

        for (int i = 0; i < n - 1; i++) 
        {
            h[i] = x[i + 1] - x[i];

            d[i] = (y[i + 1] - y[i]) / h[i];
        }

        double[] m = new double[n];

        //----------------------------------------------------
        // extremos
        //----------------------------------------------------

        m[0] = endpointSlope( h[0], h[1]
        						,d[0], d[1]);

        m[n - 1] = endpointSlope(h[n - 2]
                					, h[n - 3]
                					, d[n - 2]
                					, d[n - 3]);

        //----------------------------------------------------
        // interiores
        //----------------------------------------------------

        for (int i = 1; i < n - 1; i++) 
        {
            if (d[i - 1] * d[i] <= 0) 
            {
                m[i] = 0;

            }
            else 
            {
                double w1 = 2 * h[i] + h[i - 1];

                double w2 = h[i] + 2 * h[i - 1];

                m[i] = (w1 + w2) / ( (w1 / d[i - 1]) + (w2 / d[i]) );
            }
        }

        return m;
    }

    private static double endpointSlope( double h0, double h1, double d0, double d1) 
    {
        double m = ((2 * h0 + h1) * d0 - h0 * d1) / (h0 + h1);

        if (Math.signum(m) != Math.signum(d0))
        {
        	return 0;
        }

        if ( Math.signum(d0) != Math.signum(d1)
                && Math.abs(m) > Math.abs(3 * d0))
        {
        	return 3 * d0;
        }

        return m;
    }

    //--------------------------------------------------------
    // Evaluación
    //--------------------------------------------------------

    public double interpolate( double xx )
    {
        int k = findInterval( xx );

        double h = x[k + 1] - x[k];

        double t = (xx - x[k]) / h;

        double t2 = t * t;
        double t3 = t2 * t;

        double h00 = 2 * t3 - 3 * t2 + 1;

        double h10 = t3 - 2 * t2 + t;

        double h01 = -2 * t3 + 3 * t2;

        double h11 = t3 - t2;

        return h00 * y[k]  
        		+ h10 * h * m[k]
                + h01 * y[k + 1]
                + h11 * h * m[k + 1];
    }

    private int findInterval(double xx) 
    {
        int n = x.length;

        if (xx <= x[0])
        {
            return 0;
        }

        if (xx >= x[n - 2])
        {
        	return n - 2;
        }

        int low = 0;
        int high = n - 1;

        while ( ( high - low ) > 1) 
        {
            int mid = (low + high) >>> 1;

            if (x[mid] <= xx)
            {
            	low = mid;
            }
            else
            {
            	high = mid;
            }
        }

        return low;
    }
}