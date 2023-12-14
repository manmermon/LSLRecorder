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

// Working progress

package lslrec.plugin.loader.java9;

import java.lang.module.Configuration;
import java.lang.module.ModuleDescriptor;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.ServiceLoader.Provider;
import java.util.stream.Collectors;

import lslrec.auxiliar.extra.ArrayTreeMap;
import lslrec.plugin.IPluginLoader;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin.PluginType;
import lslrec.plugin.lslrecPlugin.compressor.LSLRecPluginCompressor;
import lslrec.plugin.lslrecPlugin.encoder.LSLRecPluginEncoder;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.sync.ILSLRecPluginSyncMethod;
import lslrec.plugin.lslrecPlugin.trial.ILSLRecPluginTrial;

public class PluginLoaderJava9 implements IPluginLoader
{
	private static PluginLoaderJava9 loader = null;
	
	private final String DEFAULT_FOLDER = System.getProperty( "user.dir" ) + "/plugins/";
		
	private final Map< PluginType, Class > PLUGIN_TYPES = new HashMap< PluginType, Class >();
		
	private ArrayTreeMap< PluginType, ILSLRecPlugin > _Plugins = new ArrayTreeMap< PluginType, ILSLRecPlugin >();
		
	private PluginLoaderJava9() throws Exception
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
	
	public static PluginLoaderJava9 getInstance() throws Exception
	{
		if( loader == null )
		{
			loader = new PluginLoaderJava9();
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
    	List< Exception > exs = new ArrayList<Exception>();
    	
    	try
    	{
	    	Path plgDir = Paths.get( this.DEFAULT_FOLDER );
	    	
	    	ModuleFinder plgFinder = ModuleFinder.of( plgDir );
	    	
	    	// Find all names of all found plugin modules
	    	Set< ModuleReference > refs = plgFinder.findAll();
	    	
	    
	    	List<String> plugins = refs.stream()
	    	        				.map( ModuleReference::descriptor)
	    	        				.map( ModuleDescriptor::name )
	    	        				.collect( Collectors.toList());
	
	    	// Create configuration that will resolve plugin modules
	    	// (verify that the graph of modules is correct)
	    	Configuration pluginsConfiguration = ModuleLayer.boot().configuration()
								    	        	.resolve( plgFinder, ModuleFinder.of(), plugins );
	
	    	// Create a module layer for plugins
	    	ModuleLayer layer = ModuleLayer.boot()
	    							.defineModulesWithOneLoader( pluginsConfiguration, ClassLoader.getSystemClassLoader() );
	    							//.defineModulesWithOneLoader( pluginsConfiguration, null );
	
	    	// Now you can use the new module layer to find service implementations in it
	    	List< ILSLRecPlugin > services = ServiceLoader.load( layer, ILSLRecPlugin.class )
	    	        							.stream().map( Provider::get )
	    	        							.collect( Collectors.toList() ); 
	    	
	    	if( services != null && !services.isEmpty() )
	    	{
	    		for( ILSLRecPlugin serv : services )
	    		{
	    			this._Plugins.putElement( serv.getType(),  serv );
	    		}
	    	}
    	}
    	catch (Exception e) 
    	{
    		e.printStackTrace();
    		exs.add( e );
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
				
				pl= (ILSLRecPlugin)p.getDeclaredConstructor().newInstance();
				
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
