package lslrec.plugin.loader;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import lslrec.auxiliar.extra.ArrayTreeMap;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.compressor.LSLRecPluginCompressor;
import lslrec.plugin.lslrecPlugin.encoder.LSLRecPluginEncoder;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.sync.LSLRecPluginSyncMethod;
import lslrec.plugin.lslrecPlugin.trial.LSLRecPluginGUIExperiment;

public class PluginLoader 
{
	private static PluginLoader loader = null;
	
	private final String DEFAULT_FOLDER = System.getProperty( "user.dir" ) + "/plugins/";
	
	private final String EXTENSION_JAR = ".jar";
	
	private final Class< ILSLRecPlugin >[] PLUGIN_TYPES = new Class[] { LSLRecPluginDataProcessing.class
																			, LSLRecPluginEncoder.class
																			, LSLRecPluginGUIExperiment.class
																			, LSLRecPluginSyncMethod.class
																			, LSLRecPluginCompressor.class
																		};
	
	private ArrayTreeMap< Class, ILSLRecPlugin > _Plugins = new ArrayTreeMap<Class, ILSLRecPlugin>(); 
	
	private PluginLoader() throws Exception
	{
		List< Exception > exs = this.LoadPlugins();
		if( !exs.isEmpty() )
		{
			Exception e = new Exception( );
			for( Exception ex : exs )
			{
				StackTraceElement[] tr = ex.getStackTrace();
				
				e.setStackTrace( tr );				
			}
			
			throw e;
		}
	}
	
	public static PluginLoader getInstance() throws Exception
	{
		if( loader == null )
		{
			loader = new PluginLoader();
		}
		
		return loader;
	}
	
    /**
     * Load plugins
     * @return exception list. It is empty if all plugins were loaded.
     * @throws NoSuchMethodException 
     */
    private List< Exception > LoadPlugins() throws NoSuchMethodException
    {
    	//Jar file list
    	List< Exception > exs = new ArrayList<Exception>();
    	
        List< File > vUrls = new ArrayList< File >();        
 
        File pluginFolder = new File( this.DEFAULT_FOLDER );
        
        //
        //Search jar files
        //
        
        if ( pluginFolder.exists() && pluginFolder.isDirectory() )
        {                    	
            File[] jars = pluginFolder.listFiles( new FilenameFilter() 
            { 
                @Override
                public boolean accept( File dir, String name) 
                {
                    return name.endsWith( EXTENSION_JAR );
                }
            });
 
            //
            for (File jar : jars) 
            {
                vUrls.add( jar );
            }
        }
        
        //
        // Check and load plugins
        //
        
        if ( vUrls != null && !vUrls.isEmpty() )
    	{
        	int pluginCount = getPlugins().size();
        	
    		ClassPathLoader cp = new ClassPathLoader();

    		Iterator< File > jarIt = vUrls.iterator();
    		
    		while( jarIt.hasNext() ) 
    		{    			
    			File jar = jarIt.next();
    			
    			try
    			{
    				cp.addFile( jar );
    				
    				int c = pluginCount;
    				
    				for( Class< ILSLRecPlugin > plg : PLUGIN_TYPES )
    				{
	    				c = getPluginsByType( plg ).size();
	    				
	    				if( c != pluginCount )
	    				{
	    					pluginCount = c;
	    					
	    					break;
	    				}
    				}
    				
    				if( c == pluginCount )
    				{
    					cp.removeFile( jar );
    				}
    			}
    			catch (Exception e) 
    			{
    				exs.add( e );
				}
    		}
    	}
        
        return exs;
    }
    
    /**
     * Get plugins from classpath
     * @return plugin list
     */
    public List< ILSLRecPlugin > getPluginsByType( Class plgClss ) 
    {
    	List< ILSLRecPlugin > plgs = _Plugins.get( plgClss );
    	
    	if( plgs.isEmpty() )
    	{	
	        //Load ILslrecPlugins
	        ServiceLoader sl = ServiceLoader.load( plgClss );        
	        sl.reload();
	        
	        for ( Iterator it = sl.iterator(); it.hasNext(); ) 
	        {   
	        	ILSLRecPlugin pl = (ILSLRecPlugin) it.next();
	        	plgs.add( pl );
	        }
    	}
        
        //loaded plugins
        return plgs;
    }
    
    public List< ILSLRecPlugin > getPlugin( Class plgClss, String id )
7    {
    	List< ILSLRecPlugin > plg = new ArrayList<ILSLRecPlugin>();
    	
    	List< ILSLRecPlugin > plgs = this.getPluginsByType( plgClss );
    	
    	for( ILSLRecPlugin p : plgs )
    	{
    		if( p.getID().equals( id ) )
    		{
    			plg.add( p );
    		}
    	}
    	
    	return plg;
    }
    
    public int addNewPluginInstance( Class plgClass, String id )
    {
    	int index = -1;
    	List< ILSLRecPlugin > plgs = this.getPlugin( plgClass, id );
    	if( !plgs.isEmpty() )
    	{
    		ILSLRecPlugin plg = plgs.get( 0 );
    		
    		try 
    		{
				Class p = Class.forName( plg.getClass().getCanonicalName() );
				
				ILSLRecPlugin pl = (ILSLRecPlugin)p.cast( p );
				this._Plugins.putElement( plgClass, pl );
				index = this._Plugins.size() - 1;
			} 
    		catch (ClassNotFoundException e) 
    		{
    			e.printStackTrace();
			}
    	}
    	
    	return index;
    }
    
    public ILSLRecPlugin removePluginInstance( Class plgCl, String id, int index )
    {
    	ILSLRecPlugin pl = null;
    	
    	List< ILSLRecPlugin > plgs = this.getPlugin( plgCl, id );
    	if( index >= 0 && index < plgs.size() )
    	{
    		pl = plgs.remove( index );
    	}
    	
    	return pl;
    }
    
    /**
     * Get all plugins from classpath
     * @return plugin list
     */
    public List< ILSLRecPlugin > getPlugins() 
    { 
        //Load ILslrecPlugins
    	List< ILSLRecPlugin > lplg = new ArrayList<ILSLRecPlugin>();
    	for( Class< ILSLRecPlugin > pl : PLUGIN_TYPES )
    	{
    		lplg.addAll( this.getPluginsByType( pl ) );
    	}
    	         
        //loaded plugins
        return lplg;
    }
}
