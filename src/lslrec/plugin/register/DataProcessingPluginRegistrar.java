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
	public static final int PROCESSING = 0;
	public static final int POSTPROCESSING = 1;
	//private static List< ILSLRecPluginDataProcessing > processRegister = new ArrayList< ILSLRecPluginDataProcessing >();
	//private static Map< String, Set< IStreamSetting > > streamPluginRegister = new HashMap<String, Set< IStreamSetting > >(); 
	
	private static Map< IStreamSetting, List< ILSLRecPluginDataProcessing > > dataProcessingRegister= new HashMap< IStreamSetting, List< ILSLRecPluginDataProcessing> >();
	private static Map< IStreamSetting, List< ILSLRecPluginDataProcessing > > dataPostprocessingRegister = new HashMap< IStreamSetting, List< ILSLRecPluginDataProcessing> >();
	
	/*
	public static void addDataProcessing( ILSLRecPluginDataProcessing process )
	{
		if( !processRegister.contains( process ) )
		{
			processRegister.add( process );
		}
	}
	//*/
	
	/*
	public static void moveDataProcessing( int oldIndex, int newIndex )
	{
		ILSLRecPluginDataProcessing pr = processRegister.remove( oldIndex );
		processRegister.add( newIndex, pr );		
	}
	//*/
	
	public static void moveDataProcessing( IStreamSetting str, int processLoc, int oldIndex, int newIndex )
	{
		if( str != null )
		{
			List<ILSLRecPluginDataProcessing > processRegister;
			
			if( processLoc == POSTPROCESSING )
			{
				processRegister = dataPostprocessingRegister.get( str );
			}
			else
			{
				processRegister = dataProcessingRegister.get( str );				
			}
			
			ILSLRecPluginDataProcessing pr = processRegister.remove( oldIndex );
			processRegister.add( newIndex, pr );
		}
	}
	
	private static String getDataProcessPluginID( ILSLRecPluginDataProcessing process )
	{
		return process.getID() + ((Object)process).toString();
	}
	
	public static int addDataStreamProcessing( ILSLRecPluginDataProcessing process, IStreamSetting stream, int processLoc )
	{
		int num = 0;
		
		if( process != null && stream != null )
		{	
			Map< IStreamSetting, List< ILSLRecPluginDataProcessing > > reg = dataProcessingRegister;
			
			if( processLoc == POSTPROCESSING )
			{
				reg = dataPostprocessingRegister;
			}
			
			List< ILSLRecPluginDataProcessing > dss = reg.get( stream );
			
			if( dss == null )
			{
				dss = new ArrayList< ILSLRecPluginDataProcessing >();
				reg.put( stream, dss );
			}	
			
			dss.add( process );
			
			num = dss.size();
		}
		
		return num;
	}
	
	public static void removeDataProcessing( IStreamSetting stream, ILSLRecPluginDataProcessing process, int processLoc )
    {
		if( process != null && stream != null)
		{
			Map< IStreamSetting, List< ILSLRecPluginDataProcessing > > reg = dataProcessingRegister;
			
			if( processLoc == POSTPROCESSING )
			{
				reg = dataPostprocessingRegister;
			}
			
			List< ILSLRecPluginDataProcessing > processes = reg.get( stream );
			if( processes != null )
			{
				processes.remove( process );
				
				if( processes.isEmpty() )
				{
					reg.remove( stream );
				}
			}
		}
    }
	
	public static void removeDataStream( IStreamSetting stream )
	{
		if( stream != null )
		{
			dataProcessingRegister.remove( stream );
			dataPostprocessingRegister.remove( stream );
		}
	}

	public static void clear()
	{
		dataPostprocessingRegister.clear();
		dataProcessingRegister.clear();
	}	
	
	public static Set< IStreamSetting > getDataStreams( ILSLRecPluginDataProcessing process, int processLoc )
	{
		Set< IStreamSetting > streams = new HashSet<IStreamSetting>();
		
		if( process != null )
		{	
			Map< IStreamSetting, List< ILSLRecPluginDataProcessing > > reg = dataProcessingRegister;
			
			if( processLoc == POSTPROCESSING )
			{
				reg = dataPostprocessingRegister;
			}
			
			for( IStreamSetting str : reg.keySet() )
			{
				List< ILSLRecPluginDataProcessing > processes = reg.get( str );
				
				for( ILSLRecPluginDataProcessing pr : processes )
				{
					if( pr.equals( process ) )
					{
						streams.add( str );
						
						continue;
					}
				}
			}
		}
		
		return streams;
	}
	
	public static List< ILSLRecPluginDataProcessing > getDataProcessing( IStreamSetting stream, int processLoc )
	{
		List< ILSLRecPluginDataProcessing > processes = new ArrayList<ILSLRecPluginDataProcessing>();
		
		if( stream != null )
		{			
			Map< IStreamSetting, List< ILSLRecPluginDataProcessing > > reg = dataProcessingRegister;
			
			if( processLoc == POSTPROCESSING )
			{
				reg = dataPostprocessingRegister;
			}
		
			List< ILSLRecPluginDataProcessing > procs = reg.get( stream ); 
			
			if( procs != null )
			{
				processes = procs;
			}
		}
		
		return processes;
	}
	
	public static List< ILSLRecPluginDataProcessing > getNewInstanceOfDataProcessing( IStreamSetting stream, int processLoc )
	{
		List< ILSLRecPluginDataProcessing > processes = new ArrayList< ILSLRecPluginDataProcessing >();
		
		List< ILSLRecPluginDataProcessing > PROCESSES = getDataProcessing( stream, processLoc );
		
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
		Set< IStreamSetting > strs = new HashSet<IStreamSetting>( dataProcessingRegister.keySet() );
		strs.addAll( dataPostprocessingRegister.keySet() );
		
		return strs;
	}
}
