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

package DataStream.OutputDataFile.Compress;

import DataStream.OutputDataFile.Compress.Zip.OutputBZip2Data;
import DataStream.OutputDataFile.Compress.Zip.OutputGZipData;

public class OutputZipDataFactory 
{
	public static final int UNDEFINED = -1;
	public static final int GZIP = 0;
	public static final int BZIP2 = 1;
	
	/**
	 * 
	 * @param type: Compress algorithm ID: 0 -> GZip, 1 -> BZIP2
	 * @return Temporal output to compress data
	 * @throws IllegalArgumentException: if type value is not supported.
	 */
	public static IOutZip createOuputZipStream( int type ) throws IllegalArgumentException
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
