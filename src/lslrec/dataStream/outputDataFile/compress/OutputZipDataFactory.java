/*
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
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

package lslrec.dataStream.outputDataFile.compress;

import lslrec.dataStream.outputDataFile.compress.zip.OutputBZip2Data;
import lslrec.dataStream.outputDataFile.compress.zip.OutputGZipData;

public class OutputZipDataFactory 
{
	public static final String UNDEFINED = "UNDEFINED";
	public static final String GZIP = "GZIP";
	public static final String BZIP2 = "BZIP2";
	
	/**
	 * 
	 * @param type: Compress algorithm ID
	 * @return Temporal output to compress data
	 * @throws IllegalArgumentException: if type value is not supported.
	 */
	public static IOutZip createOuputZipStream( String type ) throws IllegalArgumentException
	{
		IOutZip zip =  null;
		
		switch ( type ) 
		{
			case GZIP:
			{
				zip = new OutputGZipData();
				break;
			}
			case BZIP2:
			{
				zip = new OutputBZip2Data();
			}
			default:
			{
				//throw new IllegalArgumentException( "Unsupport type" );
			}
		}
		
		return zip;
	}
}
