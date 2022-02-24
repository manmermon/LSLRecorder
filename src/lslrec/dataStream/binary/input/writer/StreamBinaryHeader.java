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

package lslrec.dataStream.binary.input.writer;

import java.util.Map;

import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.MutableStreamSetting;
import lslrec.dataStream.family.setting.SimpleStreamSetting;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.tools.StreamUtils;

public class StreamBinaryHeader
{	
	public static final String HEADER_BINARY_SEPARATOR = ";"; 
	public static final char HEADER_END = '\n';
	
	public static String getStreamBinHeader( IStreamSetting streamSetting )
	{
		String binHeader = "";
		
		if( streamSetting != null )
		{
			String desc = streamSetting.description();
			
			if( !(streamSetting instanceof SimpleStreamSetting ) )
			{
				if( streamSetting instanceof MutableStreamSetting )
				{
					desc = StreamUtils.getDeepXmlStreamDescription( ((MutableStreamSetting) streamSetting).getStreamSetting() );
				}
				else
				{
					desc = StreamUtils.getDeepXmlStreamDescription( streamSetting );
				}
			}
			
			String rootNode = streamSetting.getRootNode2ExtraInfoLabel();
			Map< String, String > addInfo = streamSetting.getExtraInfo();
			if( addInfo != null )
			{
				for( String id : addInfo.keySet() )
				{
					desc = StreamUtils.addElementToXmlStreamDescription( desc, rootNode, id, addInfo.get( id ) );
				}
			}
			
			binHeader = streamSetting.name()+ HEADER_BINARY_SEPARATOR
						+ streamSetting.data_type() + HEADER_BINARY_SEPARATOR
						+ streamSetting.channel_count() + HEADER_BINARY_SEPARATOR
						+ streamSetting.getChunkSize()+ HEADER_BINARY_SEPARATOR
						+ streamSetting.getTimestampDataType() + HEADER_BINARY_SEPARATOR
						+ streamSetting.getStringLengthDataType() + HEADER_BINARY_SEPARATOR
						+ streamSetting.isInterleavedData() + HEADER_BINARY_SEPARATOR
						+ desc;

			binHeader = binHeader.trim().replace( "\r", "" ).replace( "\n", "" ) + HEADER_END;
		}
		
		return binHeader;
	}
}