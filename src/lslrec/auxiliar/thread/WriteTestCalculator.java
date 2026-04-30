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