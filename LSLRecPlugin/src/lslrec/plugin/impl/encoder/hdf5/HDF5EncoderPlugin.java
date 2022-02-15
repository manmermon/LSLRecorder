/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec. https://github.com/manmermon/LSLRecorder
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
package lslrec.plugin.impl.encoder.hdf5;

import javax.swing.JPanel;

import lslrec.auxiliar.WarningMessage;
import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.encoder.LSLRecPluginEncoder;

public class HDF5EncoderPlugin extends LSLRecPluginEncoder
{
	private Encoder enc = new HDF5Encoder(); 

	@Override
	public String getID() 
	{
		return "HDF5 test";
	}

	@Override
	public Encoder getEncoder() 
	{
		return enc;
	}

	@Override
	public WarningMessage checkSettings() 
	{
		return new WarningMessage();
	}

	@Override
	public PluginType getType() 
	{
		return PluginType.ENCODER;
	}

	@Override
	public int compareTo(ILSLRecPlugin arg0) 
	{
		int eq = 1;
		
		if( arg0 != null )
		{
			eq = arg0.getID().compareTo( this.getID() );
		}
			
		
		return eq;
	}

	@Override
	protected void setSettingPanel(JPanel arg0) 
	{		
	}

}
