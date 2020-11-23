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
package lslrec.dataStream.family.setting;

import java.util.Map;


import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;

/**
 * @author Manuel Merino Monge
 *
 */
public class SimpleMutableStreamSetting extends SimpleStreamSetting implements IMutableStreamSetting 
{	
	/**
	 * @param libType
	 * @param name
	 * @param dataType
	 * @param timeDataType
	 * @param stringLenType
	 * @param numChs
	 * @param samplingRate
	 * @param sourceID
	 * @param uid
	 * @param extraInfo
	 * @param chunkSize
	 */
	public SimpleMutableStreamSetting(StreamLibrary libType, String name, StreamDataType dataType
										, StreamDataType timeDataType, StreamDataType stringLenType
										, int numChs, double samplingRate, String sourceID
										, String uid, Map<String, String> extraInfo, int chunkSize) 
	{
		super(libType, name, dataType, timeDataType, stringLenType, numChs
				, samplingRate, sourceID, uid, extraInfo,
				chunkSize);
	}
	
	@Override
	public void setAdditionalInfo(String id, String info) 
	{	
		super.getExtraInfo().put( id, info );
	}

	@Override
	public void removeAdditionalInfo(String id) 
	{
		super.getExtraInfo().remove( id );
	}

	@Override
	public void setSelected( boolean select ) 
	{
		super.selected = select;
	}

	@Override
	public void setSynchronizationStream(boolean sync) 
	{
		super.syncStream = sync;
	}

	@Override
	public void setChunckSize(int size) 
	{
		super.chunkSize = size;
	}

	@Override
	public void setInterleaveadData( boolean interleaved ) 
	{
		super.interleaved = interleaved;
	}

	@Override
	public void setDescription(String desc)
	{
		super.desc = desc;
	}
}
