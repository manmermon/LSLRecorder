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

package ImportClisData.Java.Compress;

import ImportClisData.Java.Compress.Zip.UnzipBZip2Data;
import ImportClisData.Java.Compress.Zip.UnzipGZipData;

public class UnzipDataFactory 
{
	public static final int UNDEFINED = -1;
	public static final int GZIP = 0;
	public static final int BZIP2 = 1;
	
	public static final String UNDEFINED_ID = "Undefined";
	public static final String GZIP_ID = "GZIP";
	public static final String BZIP2_ID = "BZIP2";
	
	/**
	 * 
	 * @param type: Compress algorithm ID: 0 -> GZip, 1 -> BZIP2
	 * @return Temporal output to compress data
	 * @throws IllegalArgumentException: if type value is not supported.
	 */
	public static IUnzip createUnzipStream( int type ) throws IllegalArgumentException
	{
		IUnzip zip =  null;
		
		switch ( type ) 
		{
			case GZIP:
			{
				zip = new UnzipGZipData();
				break;
			}
			case BZIP2:
			{
				zip = new UnzipBZip2Data();
			}
			default:
			{
				//throw new IllegalArgumentException( "Unsupport type" );
			}
		}
		
		return zip;
	}
	
	/**
	 * 
	 * @param type: Compress algorithm ID
	 * @return Temporal output to compress data
	 * @throws IllegalArgumentException: if type value is not supported.
	 */
	public static IUnzip createUnzipStream( String type ) throws IllegalArgumentException
	{
		IUnzip zip =  null;
		
		switch ( type ) 
		{
			case GZIP_ID:
			{
				zip = new UnzipGZipData();
				break;
			}
			case BZIP2_ID:
			{
				zip = new UnzipBZip2Data();
			}
			default:
			{
				//throw new IllegalArgumentException( "Unsupport type" );
			}
		}
		
		return zip;
	}
}
