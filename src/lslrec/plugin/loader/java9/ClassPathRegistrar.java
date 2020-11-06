/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.
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

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ClassPathRegistrar 
{
	private Set< String > classFiles = new HashSet< String >();
	
	public void addFile( File file ) throws Exception
	{
		if ( file != null) 
		{
			this.classFiles.add( file.getAbsolutePath() );
		}
	}

	public void addFile( String filename ) throws Exception 
	{
		this.addFile( new File( filename ) );
	}

	public void removeFile( File path ) throws Exception
	{
		if( path != null )
		{
			this.classFiles.remove( path.getAbsolutePath() );
		}
	}
		
	public void removeFile( String path ) throws Exception
	{
		this.removeFile( new File( path ) );
	}	
}
