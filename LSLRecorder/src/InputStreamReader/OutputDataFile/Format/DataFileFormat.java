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

package InputStreamReader.OutputDataFile.Format;

import InputStreamReader.OutputDataFile.Compress.OutputZipDataFactory;
import InputStreamReader.OutputDataFile.Format.Clis.OutputCLISDataWriter;
import InputStreamReader.OutputDataFile.Format.Clis.Parallel.OutputCLISDataParallelWriter;

public class DataFileFormat
{
	//public static String MATLAB = "Matlab";
	//public static String CSV = "CSV";
	public static String CLIS = "CLIS";
	public static String PCLIS = "PCLIS";


	public static String[] getSupportedFileFormat()
	{
		return new String[] { PCLIS, CLIS };//, MATLAB, CSV };
		//return new String[] { CLIS, PCLIS };//, MATLAB, CSV };
	}

	public static String[] getSupportedFileExtension()
	{
		return new String[] { ".clis", ".clis" };//, ".mat", ".csv" };
	}

	public static int getCompressTech( String fileFormat )
	{
		int zp = OutputZipDataFactory.UNDEFINED;
		
		if( fileFormat.toUpperCase().equals( CLIS ))
		{
			zp = OutputZipDataFactory.GZIP;
		}
		else if( fileFormat.toUpperCase().equals( PCLIS ))
		{
			zp = OutputZipDataFactory.GZIP;
		}
		
		return zp;
	}
	
	public static String getSupportedFileExtension(String fileFormat) throws IllegalArgumentException
	{
		if( !isSupportedFileFormat( fileFormat ) )
		{
			throw new IllegalArgumentException( "Unsupport file format." );
		}
		
		String[] exts = getSupportedFileExtension();

		String ext = exts[0];
		
		if (fileFormat.toUpperCase().equals( PCLIS ))
		{
			ext = exts[1];
		}
		
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

	public static OutputFileWriterTemplate getDataFileWriter(String format, String file, OutputFileFormatParameters p ) throws Exception
	{
		OutputFileWriterTemplate writer = null;
		if (isSupportedFileFormat(format))
		{
			/*
			if (format.equals(MATLAB))
			{
				writer = new MatlabFile(file);
			}
			else if (format.equals(CSV))
			{
				writer = new CSVFile(file);
			}
			*/
			if (format.toUpperCase().equals(CLIS))
			{				
				writer = new OutputCLISDataWriter( file, p.getHeaderSize(), p.getCompressType(), p.getCharset() );
			}
			else if ( format.toUpperCase().equals( PCLIS ) )
			{				
				//writer = new OutputCLISDataParallelWriter_old( file, p.getHeaderSize(), p.getCompressType(), p.getCharset() ); 
				writer = new OutputCLISDataParallelWriter( file, p.getHeaderSize(), p.getCompressType(), p.getCharset() );
			}
		}

		return writer;
	}
}