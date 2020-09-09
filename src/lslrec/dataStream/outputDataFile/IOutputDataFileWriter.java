/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec and CLIS.
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

package lslrec.dataStream.outputDataFile;

import lslrec.auxiliar.tasks.IMonitoredTask;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlock;
import lslrec.edu.ucsd.sccn.LSLUtils;

public abstract interface IOutputDataFileWriter extends IMonitoredTask //extends IStoppableThread //, IStoppableThreadEventControl
{
	public final static int BYTE_TYPE = LSLUtils.int8;
	public final static int SHORT_TYPE = LSLUtils.int16;
	public final static int INT_TYPE = LSLUtils.int32;
	public final static int LONG_TYPE = LSLUtils.int64;
	public final static int FLOAT_TYPE = LSLUtils.float32;
	public final static int DOUBLE_TYPE = LSLUtils.double64;
	public final static int STRING_TYPE = LSLUtils.string;
			
	/**
	 * 
	 * @param id
	 * @param text
	 * @throws Exception
	 */
	public void addMetadata(String id, String text) throws Exception;
	
	/**
	 * Save data block
	 * 
	 * @param data
	 * @return True if data was saved. Otherwise, false
	 * @throws Exception
	 */
	public boolean saveData( DataBlock data )  throws Exception;
	
	/**
	 * 
	 * @return Filen path
	 */
	public String getFileName();
	
	/**
	 * 
	 * @return True if all data are saved. Otherwise, false
	 */
	public boolean finished();
		
	/**
	 * Close output file and finish the writer
	 * @throws Exception
	 */
	public void close() throws Exception;
}