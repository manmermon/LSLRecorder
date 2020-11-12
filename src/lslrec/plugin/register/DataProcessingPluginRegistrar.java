/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.plugin.register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;

/**
 * @author Manuel Merino Monge
 *
 */
public class DataProcessingPluginRegistrar 
{
	private static List< ILSLRecPluginDataProcessing > processRegister = new ArrayList< ILSLRecPluginDataProcessing >();
	private static Map< String, Set< IStreamSetting > > streamPluginRegister = new HashMap<String, Set< IStreamSetting > >(); 
	
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
	
	public static int addDataStream( ILSLRecPluginDataProcessing process, IStreamSetting stream )
	{
		int num = 0;
		
		if( process != null && stream != null )
		{	
			addDataProcessing( process );
			
			String id = getDataProcessPluginID( process );
			
			Set< IStreamSetting > dss = streamPluginRegister.get( id );
			
			if( dss == null )
			{
				dss = new HashSet< IStreamSetting >();
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
	
	public static void removeDataStream( ILSLRecPluginDataProcessing process, IStreamSetting stream )
	{
		if( process != null && stream != null )
		{
			String id = getDataProcessPluginID( process );
			
			Set< IStreamSetting > list = streamPluginRegister.get( id );
			
			if( list != null )
			{
				list.remove( stream );
			}
		}
	}
	
	public static void removeDataStreamInAllProcess( IStreamSetting stream )
	{
		for( Set< IStreamSetting > strs : streamPluginRegister.values() )
		{
			strs.remove( stream );
		}
	}	

	public static void clear()
	{
		processRegister.clear();
		streamPluginRegister.clear();
	}	
	
	public static Set< IStreamSetting > getDataStreams( ILSLRecPluginDataProcessing process )
	{
		Set< IStreamSetting > streams = null;
		
		if( process != null )
		{
			String id = getDataProcessPluginID( process );
			streams = streamPluginRegister.get( id );
		}
		
		if( streams == null )
		{
			streams = new HashSet< IStreamSetting >();
		}
		
		return streams;
	}
	
	public static List< ILSLRecPluginDataProcessing > getDataProcessing( IStreamSetting stream )
	{
		List< ILSLRecPluginDataProcessing > processes = new ArrayList< ILSLRecPluginDataProcessing >();
		
		for( ILSLRecPluginDataProcessing process : processRegister  )
		{
			String id = getDataProcessPluginID( process );
			Set< IStreamSetting > dss = streamPluginRegister.get( id );
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
	
	public static List< ILSLRecPluginDataProcessing > getNewInstanceOfDataProcessing( IStreamSetting stream )
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

	public static Set< IStreamSetting > getAllDataStreams()
	{
		Set< IStreamSetting > strs = new HashSet< IStreamSetting >();
		
		for( Set< IStreamSetting > dss : streamPluginRegister.values() )
		{
			strs.addAll( dss );
		}
		
		return strs;
	}
	
	public static List< ILSLRecPluginDataProcessing > getDataProcesses()
	{
		return new ArrayList<ILSLRecPluginDataProcessing>( processRegister );
	}
}
