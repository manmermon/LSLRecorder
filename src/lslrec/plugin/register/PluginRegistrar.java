/**
 * 
 */
package lslrec.plugin.register;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lslrec.auxiliar.extra.ArrayTreeMap;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin.PluginType;

/**
 * @author Manuel Merino Monge
 *
 */
public class PluginRegistrar 
{
	private static Map< String, ArrayTreeMap< PluginType, String > > register = new HashMap<String, ArrayTreeMap< PluginType, String > >();
	
	public static int addPluginInstance( String key, PluginType type, String plgID )
	{
		int num = 0;
		
		ArrayTreeMap< PluginType, String > plgs = getPlugins( key );
		
		if( plgs == null )
		{
			plgs = new ArrayTreeMap< PluginType, String >();
			
			register.put( key, plgs );
		}
		
		List< String > lst = plgs.putElement( type, plgID );
			
		if( lst != null )
		{
			num = lst.size();
		}
		
		return num;
	}
	
	public static Set< String > getRegisterKey()
	{
		return register.keySet();
	}
	
	public static void removePlugin( String key, PluginType type, String pluginID, int numInstance )
    {
    	ArrayTreeMap< PluginType, String > plugins = getPlugins( key );
    	
    	if( plugins != null )
    	{
    		List< String > plgs = plugins.get( type );
    		
    		searchProcess:
	    	for( int i = 0; i < plgs.size(); i++ )
	    	{	    		
	    		String p = plgs.get( i );
	    		
	    		if( p.equals( pluginID ) )
	    		{
	    			if( numInstance == 0 )
	    			{
	    				plgs.remove( i );
	    				
	    				break searchProcess;
	    			}
	    			
	    			numInstance--;
	    		}
	    	}
    	}
    }
	
	public static ArrayTreeMap< PluginType, String > getPlugins( String key )
	{
		return register.get( key );
	}
	
	public static void removePlugins( String key )
	{
		register.remove( key );
	}
	
	public static void removePlugins( String key, PluginType type )
	{
		ArrayTreeMap< PluginType, String > plgs = getPlugins( key );
		
		if( plgs != null )
		{
			plgs.remove( type );
		}
	}
	
	public static void removeAll()
	{
		register.clear();
	}	
}
