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
package lslrec.dataStream.setting;

public class BinaryFileStreamSetting extends DataStreamSetting 
{
	private String streamBinFile = null;
	
	public BinaryFileStreamSetting( DataStreamSetting dataStream, String file ) 
	{
		super( dataStream );
		
		if( file == null || file.isEmpty() )
		{
			throw new IllegalArgumentException( "File null or empty." );
		}
		
		this.streamBinFile = file;
	}
	
	public void setStreamBinFile(String streamBinFile) 
	{
		this.streamBinFile = streamBinFile;
	}
	
	public String getStreamBinFile() 
	{
		return streamBinFile;
	}
}
