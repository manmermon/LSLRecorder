/**
 * 
 */
package lslrec.plugin.register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
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
	private static List< ILSLRecPluginDataProcessing > processRegister = new ArrayList< ILSLRecPluginDataProcessing >();
	private static Map< String, Set< DataStreamSetting > > streamPluginRegister = new HashMap<String, Set< DataStreamSetting > >(); 
	
	public static void addDataProcessing( ILSLRecPluginDataProcessing process )
	{
		if( !processRegister.contains( process ) )
		{
			processRegister.add( process );
		}
	}
	
	public static void moveDataProcessing( int oldIndex, int newIndex )
	{
		ILSLRecPluginDataProcessing pr = processRegister.remove( oldIndex );
		processRegister.add( newIndex, pr );		
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
	
	public static List< ILSLRecPluginDataProcessing > getDataProcessing( DataStreamSetting stream )
	{
		List< ILSLRecPluginDataProcessing > processes = new ArrayList< ILSLRecPluginDataProcessing >();
		
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
	
	public static List< ILSLRecPluginDataProcessing > getNewInstanceOfDataProcessing( DataStreamSetting stream )
	{
		List< ILSLRecPluginDataProcessing > processes = new ArrayList< ILSLRecPluginDataProcessing >();
		
		List< ILSLRecPluginDataProcessing > PROCESSES = getDataProcessing( stream );
		
		for( ILSLRecPluginDataProcessing process : PROCESSES  )
		{
			try 
			{
				ILSLRecPluginDataProcessing pr = process.getClass().newInstance();
				pr.loadSettings( process.getSettings() );

				processes.add( pr );
			} 
			catch (InstantiationException | IllegalAccessException e) 
			{
				e.printStackTrace();
			}
		}
		
		return processes;
	}
}
