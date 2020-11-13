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
package lslrec.testing.Others;

import java.io.File;
import java.io.IOException;

public class testRegex {

	public static void main(String[] args) 
	{
		String[] strs = { 	"G:\\Sync_datos\\WorkSpace\\GitHub\\LSLRecorder\\data.clis"
							, "G:\\Sync_datos\\WorkSpace\\GitHub\\LSLRecorder\\data.clis."
							, "G:\\Sync_datos\\WorkSpace\\GitHub\\LSLRecorder.\\data.clis"
							, "G:\\Sync_datos\\WorkSpace\\GitHub\\LSLRecorder\\data.clis/"
							, "G:\\Sync_datos\\WorkSpace\\GitHub\\LSLRecorder\\data.clis\\"
							, "G:\\Sync_datos\\WorkSpace\\GitHub\\LSLRecorder\\data."
							, "G:\\Sync_datos\\WorkSpace\\GitHub\\LSLRecorder\\data./"
							, "G:\\Sync_datos\\WorkSpace\\GitHub\\LSLRecorder\\data.\\"
							, "G:\\Sync_datos\\WorkSpace\\GitHub\\LSLRecorder\\.data.clis"
							, ".\\data.clis"
							, "..\\data.clis"
							, "...\\data.clis"
							, "....\\data.clis"
							, "....\\data.clis"
							, ".data.clis"
							, "..data.clis."
							, "..data.clis./"
							, "..data.clis.\\"
						};
		
		
		for( String str : strs )
		{
			File f = new File( str );
		    try 
		    {
		    	f.getCanonicalPath();
		    	System.out.println( str+ " -> " + f.getAbsolutePath() + " OK, file name " + f.getName() );
		       
		    }
		    catch (IOException e) 
		    {
		    	System.out.println( str + " FAIL" );
		    }
		}

	}

}
