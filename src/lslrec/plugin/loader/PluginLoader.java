package lslrec.plugin.loader;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

import lslrec.plugin.lslrecPluginInterface.ILslrecPlugin;

public class PluginLoader 
{
	public static final String DEFAULT_FOLDER = System.getProperty( "user.dir" ) + "/plugins/";
	
	private static final String EXTENSION_JAR = ".jar";
	 
    /**
     * Load plugins
     * @return exception list. It is empty if all plugins were loaded.
     * @throws NoSuchMethodException 
     */
    public static List< Exception > LoadPlugins() throws NoSuchMethodException
    {
    	//Jar file list
    	List< Exception > exs = new ArrayList<Exception>();
    	
        List< File > vUrls = new ArrayList< File >();        
 
        File pluginFolder = new File( DEFAULT_FOLDER );
        
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
    				
    				int c = getPlugins().size();
    				
    				if( c != pluginCount )
    				{
    					pluginCount = c;
    				}
    				else
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
     * Get all plugins from classpath
     * @return plugin list
     */
    public static List< ILslrecPlugin > getPlugins() 
    { 
        //Load ILslrecPlugins
        ServiceLoader< ILslrecPlugin> sl = ServiceLoader.load( ILslrecPlugin.class );        
        sl.reload();
 
        List< ILslrecPlugin > lplg = new ArrayList< ILslrecPlugin >();
           
        for ( Iterator< ILslrecPlugin > it = sl.iterator(); it.hasNext(); ) 
        {   
        	ILslrecPlugin pl = it.next();
        	lplg.add( pl );
        }
         
        //loaded plugins
        return lplg;
    }
}
