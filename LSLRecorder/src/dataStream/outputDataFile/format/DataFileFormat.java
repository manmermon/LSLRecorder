/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
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

package dataStream.outputDataFile.format;

import java.util.HashMap;
import java.util.Map;

import auxiliar.tasks.ITaskMonitor;
import dataStream.outputDataFile.IOutputDataFileWriter;
import dataStream.outputDataFile.compress.OutputZipDataFactory;
import dataStream.outputDataFile.format.clis.parallel.OutputCLISDataParallelWriter;
import dataStream.outputDataFile.format.clis.OutputCLISDataWriter;
import dataStream.outputDataFile.format.hdf5.OutputHDF5DataWriter;
import dataStream.outputDataFile.format.matlab.OutputMatDataWriter;

public class DataFileFormat
{
	public static String MATLAB = "MATLAB";
	//public static String CSV = "CSV";
	public static final String CLIS_GZIP = "CLIS-GZIP";
	public static final String PCLIS_GZIP = "PCLIS-GZIP";
	public static final String CLIS_BZIP2 = "CLIS-BZIP2";
	public static final String PCLIS_BZIP2 = "PCLIS-BZIP2";
	public static final String HDF5 = "HDF5";

	public static String[] getSupportedFileFormat()
	{
		return new String[] { PCLIS_GZIP, CLIS_GZIP, PCLIS_BZIP2, CLIS_BZIP2, HDF5, MATLAB };//, CSV };
		//return new String[] { CLIS, PCLIS };//, MATLAB, CSV };
	}

	public static Map<String, String > getSupportedFileExtension()
	{
		Map< String, String > exts = new HashMap< String, String >();
		
		exts.put( PCLIS_GZIP, ".clis" );
		exts.put( CLIS_GZIP, ".clis" );
		exts.put( PCLIS_BZIP2, ".clis" );
		exts.put( CLIS_BZIP2, ".clis" );
		exts.put( HDF5, ".h5" );		
		exts.put( MATLAB, ".mat" );
		
		//exts.put( CSV, ".csv" );		
		
		return exts;
	}
	
	public static int getCompressTech( String fileFormat )
	{
		int zp = OutputZipDataFactory.UNDEFINED;
		
		fileFormat = fileFormat.toUpperCase();
		
		if( fileFormat.equals( CLIS_GZIP ) 
				||fileFormat.equals( PCLIS_GZIP ))
		{
			zp = OutputZipDataFactory.GZIP;
		}
		else if( fileFormat.equals( CLIS_BZIP2 ) 
				|| fileFormat.equals( PCLIS_BZIP2 ) )
		{
			zp = OutputZipDataFactory.BZIP2;
		}
		
		return zp;
	}
	
	public static String getSupportedFileExtension( String fileFormat ) throws IllegalArgumentException
	{
		if( !isSupportedFileFormat( fileFormat ) )
		{
			throw new IllegalArgumentException( "Unsupport file format." );
		}
		
		Map<String, String > exts = getSupportedFileExtension();

		String ext = exts.get( fileFormat );
		
		/*
		if (fileFormat.toUpperCase().equals( PCLIS_GZIP ))
		{
			ext = exts[1];
		}
		*/
		
		/*
		if (fileFormat.equals(MATLAB))
		{
			ext = exts[1];
		}
		else if (fileFormat.equals(CSV))
		{
			ext = exts[2];
		}
		*/

		return ext;
	}

	public static boolean isSupportedFileFormat(String format)
	{
		boolean ok = false;

		String[] ff = getSupportedFileFormat();
		for (int i = 0; (i < ff.length) && (!ok); i++)
		{
			ok = ff[i].equals(format);
		}

		return ok;
	}

	public static IOutputDataFileWriter getDataFileWriter(String format, String file, OutputFileFormatParameters p, ITaskMonitor monitor ) throws Exception
	{
		IOutputDataFileWriter writer = null;
		
		if ( isSupportedFileFormat( format ) )
		{	
			format = format.toUpperCase();
			
			if ( format.equals(CLIS_GZIP) || format.equals( CLIS_BZIP2 ) )
			{				
				writer = new OutputCLISDataWriter( file, p, monitor );
			}
			else if( format.equals( PCLIS_GZIP ) || format.equals( PCLIS_BZIP2 ) ) 
			{
				writer = new OutputCLISDataParallelWriter( file, p, monitor );
			}
			else if( format.equals( HDF5 ) )
			{
				writer = new OutputHDF5DataWriter( file, monitor );
			}
			else if (format.equals(MATLAB))
			{
				writer = new OutputMatDataWriter( file, monitor );
			}
			/*
			else if (format.equals(CSV))
			{
				writer = new CSVFile(file);
			}
			*/
		}

		return writer;
	}
	
	public static boolean isSupportedEncryption( String format ) 
	{
		boolean supported = false;
		
		if ( isSupportedFileFormat( format ) )
		{	
			format = format.toUpperCase();
			
			if ( format.equals(CLIS_GZIP) || format.equals( CLIS_BZIP2 ) 
				|| format.equals( PCLIS_GZIP ) || format.equals( PCLIS_BZIP2 ) ) 
			{
				supported = true;
			}
		}
		
		return supported;
	}
}