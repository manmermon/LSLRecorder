/**
 * 
 */
package lslrec.dataStream.sync;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lslrec.plugin.lslrecPluginInterface.ILSLRecPluginSyncMethod;

/**
 * @author Manuel Merino Monge
 *
 */
public class SyncMethod 
{
	public static final String SYNC_NONE = "None";
	public static final String SYNC_SOCKET = "Socket";
	public static final String SYNC_LSL = "LabStreaming Layer";
	
	private static final Map< String, ILSLRecPluginSyncMethod > syncs = new HashMap< String, ILSLRecPluginSyncMethod >();
	
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
	public static void addSyncMethod( ILSLRecPluginSyncMethod plg )
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
	public static ILSLRecPluginSyncMethod getSyncPlugin( String id )
	{
		return syncs.get( id );
	}
}
