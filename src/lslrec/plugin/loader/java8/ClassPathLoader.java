//
// From: https://lefunes.wordpress.com/2008/08/12/modificando-el-classpath-dinamicamente-en-tiempo-de-ejecucion/
// From: https://stackoverflow.com/questions/34661944/remove-folder-from-java-classpath-at-runtime
//

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
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Stack;

import sun.misc.URLClassPath;

public class ClassPathLoader 
{
	private static final String ADD_URL = "addURL";
	private static final Class[] METHODS = new Class[]{ URL.class };
	
	private final URLClassLoader loader;
	
	private final Method method;

	public ClassPathLoader() throws NoSuchMethodException 
	{
		this.loader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		
		this.method = URLClassLoader.class.getDeclaredMethod( ADD_URL, METHODS );
		
		this.method.setAccessible( true );
	}

	/*
	public URL[] getURLs() 
	{
		return this.loader.getURLs();
	}
	*/

	public void addURL( URL url ) throws Exception 
	{
		if (url != null) 
		{
			this.method.invoke( this.loader, new Object[]{ url } );
		}
	}	

	public void addURLs( URL[] urls ) throws Exception 
	{
		if (urls != null) 
		{
			for (URL url : urls) 
			{
				this.addURL( url );
			}
		}
	}

	public void addFile( File file ) throws Exception
	{
		if ( file != null) 
		{
			this.addURL( file.toURI().toURL() );
		}
	}

	public void addFile( String filename ) throws Exception 
	{
		this.addFile( new File( filename ) );
	}

	public void removeFile( File path ) throws Exception
	{
		URL url = path.toURI().toURL();
        URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        
        Class< ? > urlClass = URLClassLoader.class;
        
        Field ucpField = urlClass.getDeclaredField( "ucp" );
        ucpField.setAccessible( true );
        
        URLClassPath ucp = (URLClassPath) ucpField.get(urlClassLoader);
        Class<?> ucpClass = URLClassPath.class;
        
        Field urlsField = ucpClass.getDeclaredField( "urls" );
        
        urlsField.setAccessible(true);
        Stack urls = (Stack) urlsField.get(ucp);
        
        urls.remove( url );
	}
	
	public void removeFile( String path ) throws Exception
	{
		this.removeFile( new File( path ) );
	}	
}
