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

package lslrec.dataStream.outputDataFile.format;

import java.nio.charset.Charset;
import java.util.HashMap;

import lslrec.config.ConfigApp;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.dataStream.outputDataFile.compress.CompressorDataFactory;

public class OutputFileFormatParameters
{
	public static final String OUT_FILE_NAME = ConfigApp.OUTPUT_FILE_NAME;
	public static final String ZIP_ID = ConfigApp.OUTPUT_COMPRESSOR;
	public static final String CHAR_CODING = "CHAR_CODING";
	public static final String ENCRYPT_KEY = ConfigApp.OUTPUT_ENCRYPT_DATA;	
	public static final String PARALLELIZE = ConfigApp.OUTPUT_PARALLELIZE;
	public static final String OUT_FILE_FORMAT = ConfigApp.OUTPUT_FILE_FORMAT;
	public static final String NUM_BLOCKS= "NUM_BLOCKS";	
	public static final String BLOCK_DATA_SIZE = "BLOCK_DATA_SIZE";
	public static final String DATA_NAMES = "DATA_NAME";
	public static final String RECORDING_INFO = "RECORDING_INFO";
	public static final String DELETE_BIN = "DELETE_BIN";
	
	private ParameterList pars = new ParameterList();
	
	public OutputFileFormatParameters() 
	{
		this.setParameter( OUT_FILE_FORMAT, DataFileFormat.CLIS );
		
		Parameter< String > p = this.pars.getParameter( OUT_FILE_FORMAT );
		this.setParameter( OUT_FILE_NAME, "./data" + DataFileFormat.getSupportedFileExtension().get( p.getValue() ) );
		
		this.setParameter( ZIP_ID, CompressorDataFactory.GZIP );
		this.setParameter( CHAR_CODING,  Charset.forName( "UTF-8" )  );
		//this.setParameter( new Parameter< String >( ENCRYPT_KEY, null ) );
		this.setParameter( PARALLELIZE, true );
		
		
		this.setParameter(NUM_BLOCKS, 2L );
		this.setParameter( BLOCK_DATA_SIZE, ConfigApp.DEFAULT_SEGMENTATION_BLOCK_SIZE );
		
		this.setParameter( DATA_NAMES, "" );
		
		this.setParameter( RECORDING_INFO, new HashMap< String, String >() );				
		this.setParameter( DELETE_BIN, !ConfigApp.isTesting() );		
	}
	
	public void setParameter( String id, Object value )
	{
		Parameter p = this.pars.getParameter( id );
		if( p == null )
		{
			p = new Parameter( id, value );
			this.pars.addParameter( p );
		}
		
		p.setValue( value );
	}
	
	public Parameter getParameter( String id )
	{
		return this.pars.getParameter( id );
	}
	
	public ParameterList getAllParameters()
	{		
		return this.pars;
	}
	
	public OutputFileFormatParameters clone() 
	{
		OutputFileFormatParameters clon = new OutputFileFormatParameters();
		
		for( String id : this.pars .getParameterIDs())
		{
			Parameter par = this.pars.getParameter( id );
			clon.setParameter( par.getID(), par.getValue() );
		}
		
		return clon;
	}
	
}
