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

package lslrec.dataStream.binary.input.writer;

import lslrec.dataStream.setting.DataStreamSetting;
import lslrec.edu.ucsd.sccn.LSL.StreamInfo;

public class StreamBinaryHeader
{	
	public static final String HEADER_BINARY_SEPARATOR = ";"; 
	public static final char HEADER_END = '\n';
	
	public static String getStreamBinHeader( DataStreamSetting streamSetting )
	{
		String binHeader = "";
		
		if( streamSetting != null )
		{
			StreamInfo strInfo = streamSetting.getStreamInfo();
			binHeader = streamSetting.getStreamName() + HEADER_BINARY_SEPARATOR
						+ strInfo.channel_format() + HEADER_BINARY_SEPARATOR
						+ strInfo.channel_count() + HEADER_BINARY_SEPARATOR
						+ streamSetting.getChunkSize()+ HEADER_BINARY_SEPARATOR
						+ streamSetting.getTimeDataType() + HEADER_BINARY_SEPARATOR
						+ streamSetting.getStringLegthType() + HEADER_BINARY_SEPARATOR
						+ streamSetting.isInterleavedData() + HEADER_BINARY_SEPARATOR
						+ strInfo.as_xml();

			binHeader = binHeader.trim().replace( "\r", "" ).replace( "\n", "" ) + HEADER_END;
		}
		
		return binHeader;
	}
}