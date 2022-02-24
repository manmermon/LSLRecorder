/*
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
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

import lslrec.config.Parameter;
import lslrec.config.ParameterList;

public class OutputFileFormatParameters
{
	public static final String OUT_FILE_NAME = "OUT_FILE_NAME";
	public static final String ZIP_ID = "ZIP_ID";
	public static final String CHAR_CODING = "CHAR_CODING";
	public static final String ENCRYPT_KEY = "ENCRYPT_KEY";	
	public static final String PARALLELIZE = "PARALLELIZE";
	public static final String OUT_FILE_FORMAT = "OUT_FILE_FORMAT";
	public static final String NUM_BLOCKS= "NUM_BLOCKS";	
	public static final String BLOCK_DATA_SIZE = "BLOCK_DATA_SIZE";
	public static final String DATA_NAMES = "DATA_NAME";
	public static final String RECORDING_INFO = "RECORDING_INFO";
	public static final String DELETE_BIN = "DELETE_BIN";
	//public static final String RECORDING_CHECKER_TIMER = "RECORDING_CHECKER_TIMER";
	public static final String ID_RECORDED_SAMPLES_BY_CHANNELS = "ID_RECORDED_SAMPLES_BY_CHANNELS";
	
	private ParameterList pars = new ParameterList();
	
	public OutputFileFormatParameters( ) 
	{
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
