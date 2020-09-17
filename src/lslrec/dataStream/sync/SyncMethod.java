/**
 * 
 */
package lslrec.dataStream.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lslrec.plugin.lslrecPluginInterface.LSLRecPluginSyncMethod;

/**
 * @author Manuel Merino Monge
 *
 */
public class SyncMethod 
{
	public static final String SYNC_NONE = "None";
	public static final String SYNC_SOCKET = "Socket";
	public static final String SYNC_LSL = "LabStreaming Layer";
	
	private static final Map< String, LSLRecPluginSyncMethod > syncs = new HashMap< String, LSLRecPluginSyncMethod >();
	
	public static String[] getSyncMethodID()
	{
		List< String > met = new ArrayList< String >();
		
		met.add( SYNC_NONE );
		met.add( SYNC_SOCKET );
		met.add( SYNC_LSL );
		
		for( String id : syncs.keySet() )
		{
			met.add( id );
		}
		
		return met.toArray( new String[0] );
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
