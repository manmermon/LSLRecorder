/**
 * 
 */
package lslrec.plugin.register;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lslrec.dataStream.setting.DataStreamSetting;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;

/**
 * @author Manuel Merino Monge
 *
 */
public class DataProcessingPluginRegistrar 
{
	private static Set< ILSLRecPluginDataProcessing > processRegister = new HashSet< ILSLRecPluginDataProcessing >();
	private static Map< String, Set< DataStreamSetting > > streamPluginRegister = new HashMap<String, Set< DataStreamSetting > >(); 
	
	public static void addDataProcessing( ILSLRecPluginDataProcessing process )
	{
		processRegister.add( process );
	}
	
	private static String getDataProcessPluginID( ILSLRecPluginDataProcessing process )
	{
		return process.getID() + ((Object)process).toString();
	}
	
	public static int addDataStream( ILSLRecPluginDataProcessing process, DataStreamSetting stream )
	{
		int num = 0;
		
		if( process != null && stream != null )
		{	
			addDataProcessing( process );
			
			String id = getDataProcessPluginID( process );
			
			Set< DataStreamSetting > dss = streamPluginRegister.get( id );
			
			if( dss == null )
			{
				dss = new HashSet< DataStreamSetting >();
				streamPluginRegister.put( id, dss );
			}	
			
			dss.add( stream );
			
			num = dss.size();
		}
		
		return num;
	}
	
	public static void removeDataProcessing( ILSLRecPluginDataProcessing process )
    {
		if( process != null )
		{
			String id = getDataProcessPluginID( process );
			
			processRegister.remove( process );
			streamPluginRegister.remove( id );
		}
    }
	
	public static void removeDataStream( ILSLRecPluginDataProcessing process, DataStreamSetting stream )
	{
		if( process != null && stream != null )
		{
			String id = getDataProcessPluginID( process );
			
			Set< DataStreamSetting > list = streamPluginRegister.get( id );
			
			if( list != null )
			{
				list.remove( stream );
			}
		}
	}
	
	public static void removeAllDataStreams( DataStreamSetting stream )
	{
		for( Set< DataStreamSetting > strs : streamPluginRegister.values() )
		{
			strs.remove( stream );
		}
	}	

	public static void clear()
	{
		processRegister.clear();
		streamPluginRegister.clear();
	}	
	
	public static Set< DataStreamSetting > getDataStreams( ILSLRecPluginDataProcessing process )
	{
		Set< DataStreamSetting > streams = null;
		
		if( process != null )
		{
			String id = getDataProcessPluginID( process );
			streams = streamPluginRegister.get( id );
		}
		
		if( streams == null )
		{
			streams = new HashSet<DataStreamSetting>();
		}
		
		return streams;
	}
	
	public static Set< ILSLRecPluginDataProcessing > getDataProcessing( DataStreamSetting stream )
	{
		Set< ILSLRecPluginDataProcessing > processes = new HashSet< ILSLRecPluginDataProcessing >();
		
		for( ILSLRecPluginDataProcessing process : processRegister  )
		{
			String id = getDataProcessPluginID( process );
			Set< DataStreamSetting > dss = streamPluginRegister.get( id );
			if( dss != null )
			{
				if( dss.contains( stream ) )
				{
					processes.add( process );
				}
			}
		}
		
		return processes;
	}
}
