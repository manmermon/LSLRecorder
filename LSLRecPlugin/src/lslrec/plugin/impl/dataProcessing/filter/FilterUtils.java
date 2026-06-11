package lslrec.plugin.impl.dataProcessing.filter;

import java.util.ArrayList;
import java.util.List;

public class FilterUtils 
{
	public static List< Double > parseFilterCoef( String coef )
	{
		List< Double > c = new ArrayList<Double>();
				
		try
		{
			String[] str_cs = coef.split( "," );
			
			for( String ci : str_cs )
			{
				c.add( Double.parseDouble( ci ) );
			}
		}
		catch (Exception e) 
		{
			c.clear();
		}
		
		return c;
	}
}
