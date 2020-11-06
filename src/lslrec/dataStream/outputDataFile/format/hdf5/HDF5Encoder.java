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
package lslrec.dataStream.outputDataFile.format.hdf5;

import java.util.List;

import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.config.ParameterList;
import lslrec.config.SettingOptions;
import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.dataStream.outputDataFile.format.IOutputDataFileWriter;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.setting.DataStreamSetting;

public class HDF5Encoder implements Encoder 
{
	@Override
	public List<SettingOptions> getSettiongOptions() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IOutputDataFileWriter getWriter( OutputFileFormatParameters pars, DataStreamSetting streamSettings, ITaskMonitor monitor )
			throws Exception 
	{	
		return new OutputHDF5DataWriter( (String)pars.getParameter( OutputFileFormatParameters.OUT_FILE_NAME ).getValue(), monitor );
	}

	@Override
	public String getOutputFileExtension() 
	{
		return ".h5";
	}

	@Override
	public boolean isSupportedEncryption() 
	{
		return false;
	}

	@Override
	public String getID() 
	{
		return "HDF5";
	}

	@Override
	public ParameterList getParameters() 
	{
		return null;
	}

}
