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

package lslrec.dataStream.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lslrec.plugin.lslrecPlugin.sync.LSLRecPluginSyncMethod;

/**
 * @author Manuel Merino Monge
 *
 */
public class SyncMethod 
{
	public static final String SYNC_NONE = "None";
	public static final String SYNC_SOCKET = "Socket";
	public static final String SYNC_STREAM = "Data stream";	
	public static final String SYNC_ALL = "All";
	
	
	private static final Map< String, LSLRecPluginSyncMethod > syncs = new HashMap< String, LSLRecPluginSyncMethod >();
	
	public static String[] getSyncMethodID()
	{
		List< String > met = new ArrayList< String >();
		
		met.add( SYNC_NONE );
		met.add( SYNC_ALL );
		met.add( SYNC_SOCKET );
		met.add( SYNC_STREAM );		
		
		for( String id : syncs.keySet() )
		{
			met.add( id );
		}
		
		return met.toArray( new String[0] );
	}
	
	public static boolean isSyncMethod( String m )
	{
		boolean check = false;
		
		if( m != null )
		{
			for( String met : getSyncMethodID() )
			{
				check = m.equalsIgnoreCase( met );
				
				if( check )
				{
					break;
				}
			}
		}
		
		return check;
	}
	
	public static boolean isNoneSyncMethod( String met )
	{
		boolean check = ( met != null && met.toLowerCase().equals( SyncMethod.SYNC_NONE.toLowerCase() ) );		
		
		return check;
	}
	
	public static boolean isAllSyncMethod( String met )
	{
		boolean check = ( met != null && met.toLowerCase().equals( SyncMethod.SYNC_ALL.toLowerCase() ) );		
		
		return check;
	}
	
	/**
	 * 
	 * @param plg 
	 */
	public static void addSyncMethod( LSLRecPluginSyncMethod plg )
	{
		if( plg != null )
		{
			syncs.put( plg.getID(), plg );
		}
	}
	
	/**
	 * 
	 * @param ID
	 * @return ILSLRecPluginSyncMethod object or null if ID is unknown.  
	 */
	public static LSLRecPluginSyncMethod getSyncPlugin( String id )
	{
		return syncs.get( id );
	}
}
