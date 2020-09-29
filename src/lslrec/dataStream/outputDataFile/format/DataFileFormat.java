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

package lslrec.dataStream.outputDataFile.format;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lslrec.config.ParameterList;
import lslrec.config.SettingOptions;
import lslrec.dataStream.outputDataFile.format.clis.ClisEncoder;
import lslrec.dataStream.outputDataFile.format.hdf5.HDF5Encoder;
import lslrec.dataStream.outputDataFile.format.matlab.MatlabEncoder;
import lslrec.plugin.lslrecPlugin.encoder.ILSLRecPluginEncoder;

public class DataFileFormat
{
	public static String MATLAB = "MATLAB";
	//public static String CSV = "CSV";
	/*
	public static final String CLIS_GZIP = "CLIS-GZIP";
	public static final String PCLIS_GZIP = "PCLIS-GZIP";
	public static final String CLIS_BZIP2 = "CLIS-BZIP2";
	public static final String PCLIS_BZIP2 = "PCLIS-BZIP2";
	*/
	public static final String CLIS = "CLIS";
	public static final String HDF5 = "HDF5";

	private static final Map< String, ILSLRecPluginEncoder > pluginEncoders = new HashMap< String, ILSLRecPluginEncoder>();
	
	public static String[] getSupportedFileFormat()
	{
		List< String > formats = new ArrayList< String >();
		formats.add( CLIS );
		formats.add( HDF5 );
		formats.add( MATLAB );
		
		for( String id : pluginEncoders.keySet() )
		{
			formats.add( id );
		}
		
		return formats.toArray( new String[0] );
	}

	public static Map<String, String > getSupportedFileExtension()
	{
		Map< String, String > exts = new LinkedHashMap< String, String >();
		
		Encoder enc = getDataFileEncoder( CLIS );		
		exts.put( CLIS, enc.getOutputFileExtension() );
		
		enc = getDataFileEncoder( HDF5 );
		exts.put( HDF5, enc.getOutputFileExtension() );
		
		enc = getDataFileEncoder( MATLAB );
		exts.put( MATLAB, enc.getOutputFileExtension() );
		
		for( ILSLRecPluginEncoder pl : pluginEncoders.values() )
		{
			String ex = pl.getEncoder().getOutputFileExtension();
			if( ex == null || ex.isEmpty() )
			{
				ex = "." + pl.getID();
			}
			
			if( ex.charAt( 0 ) != '.' )
			{
				ex = "." + ex;
			}
			
			exts.put( pl.getID(), ex );
		}
		
		//exts.put( CSV, ".csv" );		
		
		return exts;
	}
	
	/*
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
	*/

	/*
	public static String getSupportedFileExtension( String fileFormat ) throws IllegalArgumentException
	{
		if( !isSupportedFileFormat( fileFormat ) )
		{
			throw new IllegalArgumentException( "Unsupport file format." );
		}
		
		Map<String, String > exts = getSupportedFileExtension();

		String ext = exts.get( fileFormat );
		
		return ext;
	}
	*/

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

	public static Encoder getDataFileEncoder( String format)
	{
		Encoder enc = null;
		
		if ( isSupportedFileFormat( format ) )
		{	
			format = format.toUpperCase();
						
			if ( format.equals( CLIS ) )
			{				
				enc = new ClisEncoder();
			}
			else if( format.equals( HDF5 ) )
			{
				enc = new HDF5Encoder();
			}
			else if (format.equals(MATLAB))
			{
				enc = new MatlabEncoder();
			}
			else
			{
				ILSLRecPluginEncoder pl = pluginEncoders.get( format );
				
				if( pl != null )
				{
					enc = pl.getEncoder( );					
				}
			}
		}

		return enc;
	}
	
	/*
	public static boolean isSupportedEncryption( String format ) 
	{
		boolean supported = false;
		
		if ( isSupportedFileFormat( format ) )
		{	
			format = format.toUpperCase();
			
			Encoder enc = getDataFileEncoder( format );
			if( enc != null )
			{
				supported = enc.isSupportedEncryption();
			}
		}
		
		return supported;
	}
	*/

	public static void addEncoder( ILSLRecPluginEncoder encoder )
	{
		if( encoder != null )
		{
			pluginEncoders.put( encoder.getID().toUpperCase(), encoder );
		}
	}

	public static List< SettingOptions > getOutputFileFormat( String format )
	{
		List< SettingOptions > opts = new ArrayList< SettingOptions >();
		
		Encoder enc = getDataFileEncoder( format );
		if( enc != null )
		{
			opts.addAll( enc.getSettiongOptions() );
		}
		
		return opts;
	}
	
	public static ParameterList getParameters( String format )
	{
		ParameterList pars = null;
		
		Encoder enc = getDataFileEncoder( format );
				
		if( enc != null )
		{
			pars = enc.getParameters();
		}
		
		return pars;
	}
}