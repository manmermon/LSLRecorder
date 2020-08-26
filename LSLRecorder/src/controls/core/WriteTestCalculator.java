package controls.core;

import java.text.DecimalFormat;
import java.util.List;

import config.language.Language;
import exceptions.handler.ExceptionDialog;
import exceptions.handler.ExceptionDictionary;
import exceptions.handler.ExceptionMessage;

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
			
			String[] timeUnits = new String[] { "seconds", "milliseconds", "microseconds", "nanoseconds" };
			acumM /= 1e9D; // seconds
			acumSD /= 1e9D;
			
			double freq = 1 / acumM;
			
			int unitIndex = 0;
			while( acumM < 1 && unitIndex < timeUnits.length )
			{
				unitIndex++;
				acumM *= 1000;
				acumSD *= 1000;
			}
			
			DecimalFormat df = new DecimalFormat("#.00"); 
			//managerGUI.addInputMessageLog( this.ID + " -> average of writing time " + df.format( acumM ) + " \u00B1 " + df.format( acumSD ) + " " + timeUnits[ unitIndex ] + "" +" (Freq = " + df.format( freq )+ ")\n");
			Exception ex = new Exception( this.ID + " -> average of writing time " + df.format( acumM ) + " \u00B1 " + df.format( acumSD ) + " " + timeUnits[ unitIndex ] + "" +" (Freq = " + df.format( freq )+ ")" );
			ExceptionMessage msg = new ExceptionMessage( ex, Language.getLocalCaption( Language.MENU_WRITE_TEST ), ExceptionDictionary.INFO_MESSAGE );
			ExceptionDialog.showMessageDialog( msg, true, false );
		}
		else
		{
			//managerGUI.addInputMessageLog( this.ID + " -> non data available.\n" );
			Exception ex = new Exception( this.ID + " -> non data available." );
			ExceptionMessage msg = new ExceptionMessage( ex, Language.getLocalCaption( Language.MENU_WRITE_TEST ), ExceptionDictionary.INFO_MESSAGE );
			ExceptionDialog.showMessageDialog( msg, true, false );				
		}
		
		this.values.clear();
		this.values = null;
	}
}