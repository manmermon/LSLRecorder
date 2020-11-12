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
package lslrec.dataStream.binary.setting;

import java.io.File;

import lslrec.dataStream.family.setting.IStreamSetting;

public class BinaryFileStreamSetting 
{
	private File streamBinFile = null;
	private IStreamSetting sstr = null;
			
	public BinaryFileStreamSetting( IStreamSetting dataStream, File file ) 
	{
		
		if( dataStream == null || file == null )
		{
			throw new IllegalArgumentException( "Input(s) null or empty." );
		}
		
		this.sstr = dataStream;
		this.streamBinFile = file;
	}
	
	public IStreamSetting getStreamSetting()
	{
		return this.sstr;
	}
	
	public void setStreamBinFile( File streamBinFile) 
	{
		this.streamBinFile = streamBinFile;
	}
	
	public File getStreamBinFile() 
	{
		return this.streamBinFile;
	}
}
