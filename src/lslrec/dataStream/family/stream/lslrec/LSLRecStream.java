/**
 * 
 */
package lslrec.dataStream.family.stream.lslrec;

import java.util.ArrayList;
import java.util.List;

import lslrec.dataStream.family.setting.IStreamSetting;

/**
 * @author Manuel Merino Monge
 *
 */
public class LSLRecStream 
{
	private static List< LSLRecSimpleDataStream > lslrecStreamsList = new ArrayList<LSLRecSimpleDataStream>();
	
	public static void addDataStream( LSLRecSimpleDataStream datStream )
	{
		lslrecStreamsList.add( datStream );
	}
	
	public static void removeDataStream( LSLRecSimpleDataStream datStream )
	{
		lslrecStreamsList.remove( datStream );
	}
	
	public static void clearDataStream()
	{
		lslrecStreamsList.clear();
	}
	
	public static LSLRecSimpleDataStream  getDataStream( IStreamSetting sst )
	{
		LSLRecSimpleDataStream datStr = null;
		
		for( LSLRecSimpleDataStream dst : lslrecStreamsList )
		{
			try
			{
				if( sst.equals( dst.info() ) )
				{
					datStr = dst;
					
					break;
				}
			}
			catch (Exception e) 
			{
			}
		}
		
		return datStr;
	}
	
	public static List< IStreamSetting > getRegisteredStreamSettings()
	{
		List< IStreamSetting > ss = new ArrayList< IStreamSetting >();
		
		for( LSLRecSimpleDataStream st : lslrecStreamsList )
		{
			try 
			{
				ss.add( st.info() );
			}
			catch (Exception e) 
			{
			}
		}
		
		return ss;
	}
}
