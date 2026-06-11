/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2011-2026 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.  https://github.com/manmermon/LSLRecorder
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
package lslrec.auxiliar.thread;

import java.text.DecimalFormat;
import java.util.List;

import lslrec.config.language.Language;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionMessage;

public class WriteTestCalculator extends Thread
{
	private List< Long > values;
	private String ID;
	public WriteTestCalculator( String streamId, List< Long > val )
	{
		this.ID = streamId;
		this.values = val;
	}
	
	@Override
	public void run() 
	{
		if( this.values != null && !this.values.isEmpty() )
		{
			double acumM= 0.0;
			double acumSD = 0.0;
			for( Long v : this.values )
			{
				acumM += v;
				acumSD += (v * v );
			}
			
			acumM /= this.values.size();				
			acumSD -= ( acumM * acumM * this.values.size() ) ;
			
			if( this.values.size() > 1 )
			{
				acumSD /= ( this.values.size() - 1 );
			}
			
			acumSD = Math.sqrt( acumSD );
			
			String[] timeUnits = new String[] { "seconds"	, "milliseconds", "microseconds", "nanoseconds" };
			String[] freqUnits = new String[] { "Hz"		, "kHz"			, "MHz"			, "GHz" };
			acumM /= 1e9D; // seconds
			acumSD /= 1e9D;
			
			double freq = 1 / acumM;
			
			int timeUnitIndex = 0;
			while( acumM < 1 && timeUnitIndex < timeUnits.length )
			{
				timeUnitIndex++;
				acumM *= 1_000;
				acumSD *= 1_000;
				
				freq /= 1_000;
			}
			

			int freqUnitIndex = timeUnitIndex;
			if( freqUnitIndex > 0 )
			{
				if( freq < 1 )
				{
					freqUnitIndex--;
					freq *= 1_000;
				}
			}
			
			DecimalFormat df = new DecimalFormat("#.00"); 
			
			Exception ex = new Exception( this.ID + " -> average of writing time " + df.format( acumM ) + " \u00B1 " + df.format( acumSD ) 
														+ " " + timeUnits[ timeUnitIndex ] + "" +" (Freq = " + df.format( freq )+ " " + freqUnits[ freqUnitIndex ] + ")" );
			
			ExceptionMessage msg = new ExceptionMessage( ex, Language.getLocalCaption( Language.MENU_WRITE_TEST ), ExceptionMessage.INFO_MESSAGE );
			ExceptionDialog.showMessageDialog( msg, true, false );
		}
		else
		{
			Exception ex = new Exception( this.ID + " -> non data available." );
			ExceptionMessage msg = new ExceptionMessage( ex, Language.getLocalCaption( Language.MENU_WRITE_TEST ), ExceptionMessage.INFO_MESSAGE );
			ExceptionDialog.showMessageDialog( msg, true, false );				
		}
		
		this.values.clear();
		this.values = null;
	}
}