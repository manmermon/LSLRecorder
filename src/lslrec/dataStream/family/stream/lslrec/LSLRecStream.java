/**
 * 
 */
package lslrec.dataStream.family.stream.lslrec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting.StreamLibrary;
import lslrec.dataStream.family.stream.lslrec.streamgiver.ByteStreamGiver;

/**
 * @author Manuel Merino Monge
 *
 */
public class LSLRecStream 
{
	private static Map< String, IStreamSetting > lslrecStreamsList = new HashMap< String, IStreamSetting >();
	private static Map< String, ByteStreamGiver > lslrecDataGiver = new HashMap< String, ByteStreamGiver >();
	
	public static void addDataStream( IStreamSetting datStream )
	{
		if( datStream != null && datStream.getLibraryID() == StreamLibrary.LSLREC )
		{
			lslrecStreamsList.put( datStream.uid(), datStream );
		}		
	}
	
	public static boolean setDataStreamGiver( String streamId, ByteStreamGiver g )
	{
		boolean add = false;
		
		if( g != null && lslrecStreamsList.containsKey( streamId ) )
		{
			lslrecDataGiver.put( streamId, g );
		}
		
		return add;
	}
	
	public static void removeDataStream( IStreamSetting datStream )
	{
		if( datStream != null )
		{
			lslrecStreamsList.remove( datStream.uid() );
			lslrecDataGiver.remove( datStream.uid() );
		}
	}
	
	public static void clearDataStream()
	{
		lslrecStreamsList.clear();
		lslrecDataGiver.clear();
	}
	
	public static LSLRecSimpleDataStream createDataStream( IStreamSetting sst )
	{
		LSLRecSimpleDataStream datStr = null;
		
		if( sst != null )
		{
			IStreamSetting st = lslrecStreamsList.get( sst.uid() );
			
			if( st != null )
			{
				datStr = new LSLRecSimpleDataStream( st );
				datStr.setDataStreamGiver( lslrecDataGiver.get( st.uid() ) );
			}
		}
		
		return datStr;
	}
	
	public static List< IStreamSetting > getRegisteredStreamSettings()
	{
		List< IStreamSetting > ss = new ArrayList<IStreamSetting>( lslrecStreamsList.values() );
		
		return ss;
	}
}
