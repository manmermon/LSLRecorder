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

package lslrec.plugin.loader.java8;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import lslrec.auxiliar.extra.ArrayTreeMap;
import lslrec.plugin.IPluginLoader;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin.PluginType;
import lslrec.plugin.lslrecPlugin.compressor.LSLRecPluginCompressor;
import lslrec.plugin.lslrecPlugin.encoder.LSLRecPluginEncoder;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.sync.ILSLRecPluginSyncMethod;
import lslrec.plugin.lslrecPlugin.trial.ILSLRecPluginTrial;

public class PluginLoader implements IPluginLoader
{
	private static PluginLoader loader = null;
	
	private final String DEFAULT_FOLDER = System.getProperty( "user.dir" ) + "/plugins/";
	
	private final String EXTENSION_JAR = ".jar";
	
	private final Map< PluginType, Class > PLUGIN_TYPES = new HashMap< PluginType, Class >();
		
	private ArrayTreeMap< PluginType, ILSLRecPlugin > _Plugins = new ArrayTreeMap< PluginType, ILSLRecPlugin >(); 
	
	private PluginLoader() throws Exception
	{
		PLUGIN_TYPES.put( PluginType.DATA_PROCESSING,  ILSLRecPluginDataProcessing.class );
		PLUGIN_TYPES.put( PluginType.ENCODER,  LSLRecPluginEncoder.class );
		PLUGIN_TYPES.put( PluginType.TRIAL,  ILSLRecPluginTrial.class );
		PLUGIN_TYPES.put( PluginType.SYNC,  ILSLRecPluginSyncMethod.class );
		PLUGIN_TYPES.put( PluginType.COMPRESSOR,  LSLRecPluginCompressor.class );
		
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
    				
    				/*
    				int c = pluginCount;
    				
    				for( PluginType plg : PLUGIN_TYPES.keySet() )
    				{
    					List< ILSLRecPlugin > ps = getPluginsByType( plg );
    					System.out.println( "PluginLoader.LoadPlugins " + ps );
    					if( ps != null )
    					{
    						c = ps.size();
	    				
		    				if( c != pluginCount )
		    				{		    					
		    					pluginCount = c;
		    					
		    					break;
		    				}
    					}
    				}
    				*/
    				
    				/*
    				if( c == pluginCount )
    				{
    					cp.removeFile( jar );
    				}
    				*/
    			}
    			catch (Exception e) 
    			{
    				exs.add( e );
				}
    		}
    		
    		for( PluginType plg : PLUGIN_TYPES.keySet() )
			{
				List< ILSLRecPlugin > ps = getPluginsByType( plg );				
			}
    	}
        
        return exs;
    }
    
    /**
     * Get plugins from classpath
     * @return plugin list
     */
    @Override
    public List< ILSLRecPlugin > getPluginsByType( PluginType plgType ) 
    {
    	List< ILSLRecPlugin > plgs = _Plugins.get( plgType );
    	    	
    	//System.out.println("PluginLoader.getPluginsByType() " + plgType);
    	
    	if( plgs == null || plgs.isEmpty() )
    	{	
    		Class plgClss = PLUGIN_TYPES.get( plgType );
    		
    		if( plgClss != null )
    		{
		        //Load ILslrecPlugins
		        ServiceLoader sl = ServiceLoader.load( plgClss );
		        sl.reload();
		        
		        for ( Iterator it = sl.iterator(); it.hasNext(); ) 
		        {   
		        	try
		        	{
			        	ILSLRecPlugin pl = (ILSLRecPlugin) it.next();
			        	_Plugins.putElement( plgType, pl );
		        	}
		        	catch( Exception | Error e)
		        	{}
		        }
    		}
    	}
        
        //loaded plugins
        return plgs;
    }
    
    @Override
    public List< ILSLRecPlugin > getAllPlugins( PluginType plgClss, String id )
    {
    	List< ILSLRecPlugin > plg = new ArrayList< ILSLRecPlugin >();
    	
    	List< ILSLRecPlugin > plgs = this.getPluginsByType( plgClss );
    	
    	if( plgs != null )
    	{
	    	for( ILSLRecPlugin p : plgs )
	    	{
	    		if( p.getID().equals( id ) )
	    		{
	    			plg.add( p );
	    		}
	    	}
    	}
    	
    	return plg;
    }
    
    @Override
    public ILSLRecPlugin createNewPluginInstance( PluginType plgType, String id, boolean registerInstance )
    {    		
    	ILSLRecPlugin pl = null;
    	
    	List< ILSLRecPlugin > plgs = this.getAllPlugins( plgType, id );
    	if( !plgs.isEmpty() )
    	{
    		ILSLRecPlugin plg = plgs.get( 0 );
    		
    		try 
    		{
    			String idPl = plg.getClass().getCanonicalName();
    			
				Class p = Class.forName( idPl );
				
				pl= (ILSLRecPlugin)p.newInstance();
				
				if( registerInstance )
				{
					this._Plugins.putElement( plgType, pl );
				}
			} 
    		catch ( Exception e) 
    		{
			}
    	}
    	
    	return pl;
    }
    
    @Override
    public ILSLRecPlugin removePluginInstance( PluginType plgCl, String id, int index )
    {
    	ILSLRecPlugin pl = null;
    	
    	List< ILSLRecPlugin > plgs = this._Plugins.get( plgCl );
    	
    	if( plgs != null )
    	{
	    	for( int i = 0; i < plgs.size(); i++ )
	    	{	    		
	    		ILSLRecPlugin p = plgs.get( i );
	    		if( p.getID().equals( id ) )
	    		{
	    			if( index == 0 )
	    			{
	    				pl = plgs.remove( i );
	    				
	    				break;
	    			}
	    			else
	    			{
	    				index--;
	    			}
	    		}
	    	}
    	}
    	
    	return pl;
    }
    
    /**
     * Get all plugins from classpath
     * @return plugin list
     */
    @Override
    public List< ILSLRecPlugin > getPlugins() 
    { 
        //Load ILslrecPlugins
    	List< ILSLRecPlugin > lplg = new ArrayList<ILSLRecPlugin>();
    	
    	for( PluginType pl : PLUGIN_TYPES.keySet() )
    	{
    		List< ILSLRecPlugin > ps = this.getPluginsByType( pl );
    		
    		if( ps != null )
    		{
    			lplg.addAll( ps );
    		}
    	}
    	         
        //loaded plugins
        return lplg;
    }
}
