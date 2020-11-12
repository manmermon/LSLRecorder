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

import java.util.List;

import lslrec.auxiliar.tasks.ITaskIdentity;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.config.ParameterList;
import lslrec.config.SettingOptions;
import lslrec.dataStream.family.setting.IStreamSetting;

public interface Encoder extends ITaskIdentity 
{	
	/**
	 * 
	 * @return output file extension
	 */
	public String getOutputFileExtension();
	
	/**
	 * 	
	 * @return true if data encryptation is enabled. Otherwise, false
	 */
	public boolean isSupportedEncryption();	
	
	public IOutputDataFileWriter getWriter( OutputFileFormatParameters pars, IStreamSetting streamSettings, ITaskMonitor monitor ) throws Exception;	
	
	public List< SettingOptions > getSettiongOptions();
	
	public ParameterList getParameters();
}
