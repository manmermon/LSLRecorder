//
// From: https://lefunes.wordpress.com/2008/08/12/modificando-el-classpath-dinamicamente-en-tiempo-de-ejecucion/
// From: https://stackoverflow.com/questions/34661944/remove-folder-from-java-classpath-at-runtime
//

package lslrec.plugin.loader;

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
	
	//private final URLClassLoader loader;
	private final ClassLoader loader;
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
