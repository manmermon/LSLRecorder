package lslrec.plugin.impl.dataProcessing.resender;

import java.util.ArrayList;
import java.util.List;

public class LSLStreamResenderTools 
{
	public static List< Integer > convertIntegerStringList2IntArray( String intList )
	{
		List< Integer > intValues = new ArrayList< Integer >();
		
		if( intList != null && !intList.isEmpty() )
		{
			String[] integersList = intList.replaceAll( "\\s+", "" ).split( "," );
			
			for( String intStr : integersList )
			{
				try
				{
					int v = Integer.parseInt( intStr );
					
					intValues.add( v );
				}
				catch (Exception e) 
				{
					intValues.clear();
					
					break;
				}
			}			
			
		}
		
		return intValues;
	}
}
