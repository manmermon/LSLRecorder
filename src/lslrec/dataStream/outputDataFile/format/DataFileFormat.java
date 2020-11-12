/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
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

package lslrec.dataStream.outputDataFile.format;


import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lslrec.config.ConfigApp;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.config.SettingOptions;
import lslrec.dataStream.outputDataFile.compress.CompressorDataFactory;
import lslrec.dataStream.outputDataFile.format.clis.ClisEncoder;
import lslrec.dataStream.outputDataFile.format.hdf5.HDF5Encoder;
import lslrec.dataStream.outputDataFile.format.matlab.MatlabEncoder;
import lslrec.plugin.lslrecPlugin.encoder.LSLRecPluginEncoder;

public class DataFileFormat
{
	public static String MATLAB = "MATLAB";
	//public static String CSV = "CSV";

	public static final String CLIS = "CLIS";
	public static final String HDF5 = "HDF5";

	private static final Map< String, LSLRecPluginEncoder > pluginEncoders = new HashMap< String, LSLRecPluginEncoder>();
	
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
		
		for( LSLRecPluginEncoder pl : pluginEncoders.values() )
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
				LSLRecPluginEncoder pl = pluginEncoders.get( format );
				
				if( pl != null )
				{
					enc = pl.getEncoder( );					
				}
			}
		}

		return enc;
	}
	
	public static void addEncoder( LSLRecPluginEncoder encoder )
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

	public static OutputFileFormatParameters getDefaultOutputFileFormatParameters()
	{
		OutputFileFormatParameters outPars = new OutputFileFormatParameters( );
				
		outPars.setParameter( OutputFileFormatParameters.OUT_FILE_FORMAT, DataFileFormat.CLIS );
		
		Parameter< String > p = outPars.getParameter( OutputFileFormatParameters.OUT_FILE_FORMAT );
		outPars.setParameter( OutputFileFormatParameters.OUT_FILE_NAME, "./data" + DataFileFormat.getSupportedFileExtension().get( p.getValue() ) );
		
		outPars.setParameter( OutputFileFormatParameters.ZIP_ID, CompressorDataFactory.GZIP );
		outPars.setParameter( OutputFileFormatParameters.CHAR_CODING,  Charset.forName( "UTF-8" )  );
		
		outPars.setParameter( OutputFileFormatParameters.PARALLELIZE, true );
		
		
		outPars.setParameter( OutputFileFormatParameters.NUM_BLOCKS, 2L );
		outPars.setParameter( OutputFileFormatParameters.BLOCK_DATA_SIZE, ConfigApp.DEFAULT_SEGMENTATION_BLOCK_SIZE );
		
		outPars.setParameter( OutputFileFormatParameters.DATA_NAMES, "" );
		
		outPars.setParameter( OutputFileFormatParameters.RECORDING_INFO, new HashMap< String, String >() );				
		outPars.setParameter( OutputFileFormatParameters.DELETE_BIN, !ConfigApp.isTesting() );
		
		return outPars;
	}
}