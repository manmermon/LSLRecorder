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

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Pointer;

import lslrec.dataStream.tools.StreamUtils.StreamDataType;

/**
 * @author Manuel Merino Monge
 *
 */
public class SimpleStreamSetting implements IStreamSetting 
{	
	private String name = "", content = "";
	private StreamDataType data_type = StreamDataType.float32;
	 
	private StreamDataType timeDataType = StreamDataType.double64;
	
	private StreamDataType stringLenDataType = StreamDataType.int64;
		
	private Map< String, String > extraInfo = new HashMap<String, String>();
	
	protected int chunkSize = 1;
	
	protected boolean interleaved = false;
	
	protected boolean syncStream = false;
	
	protected boolean selected = false;
	
	protected String desc = "";
	
	private StreamLibrary lib_type = StreamLibrary.LSL;
	
	private int numChannels = 1;
	
	private double samplint_rate = 0D;
	 
	private String sourceID = "", uid = "";
	
	private String hostname = "", sessionID = "";
	
	private int ver;
	
	private double createdAt;
	
	protected int recordingCheckerTimer = 3;
	protected boolean enableCheckerTimer = true;
		
	/**
	 * 
	 * @param libType
	 * @param name
	 * @param dataType
	 * @param timeDataType
	 * @param stringLenType
	 * @param numChs
	 * @param chunkSize
	 * @param samplingRate
	 * @param recordingCheckerTimer
	 * @param enableCheckerTimer
	 * @param sourceID
	 * @param uid
	 * @param extraInfo
	 */
	public SimpleStreamSetting( StreamLibrary libType
								, String name
								, StreamDataType dataType 
								, StreamDataType timeDataType
								, StreamDataType stringLenType
								, int numChs
								, int chunkSize
								, double samplingRate
								, int recordingCheckerTimer
								, boolean enableCheckerTimer
								, String sourceID
								, String uid
								, Map< String, String > extraInfo								  
								)
	{
		this.name = name;
		
		//this.content = content_type;
		
		this.data_type = dataType;
		
		this.timeDataType = timeDataType;
		
		this.stringLenDataType = stringLenType;
		
		this.extraInfo = extraInfo;
		if( this.extraInfo == null )
		{
			this.extraInfo = new HashMap<String, String>();
		}
		
		this.chunkSize = chunkSize;

		this.interleaved = false;
		
		this.selected = false;
		
		this.syncStream = false;
		
		this.lib_type = libType;
		
		this.numChannels = numChs;
		
		this.samplint_rate = samplingRate;

		this.recordingCheckerTimer = recordingCheckerTimer;
		this.enableCheckerTimer = enableCheckerTimer;
		
		this.sourceID = sourceID;
		this.uid = uid;
		
		this.hostname = "";
		this.ver = 1;
		this.createdAt = System.nanoTime();
		
		this.sessionID = createdAt + "";
		
		this.desc = "<stream>\n";
		
		this.desc += "<name>" + this.name + "</name>\n";
		this.desc += "<dataType>" + this.data_type + "</dataType>\n";
		this.desc += "<timeType>" + this.timeDataType + "</timeType>\n";
		this.desc += "<stringLengthType>" + this.stringLenDataType + "</stringLengthType>\n";
		this.desc += "<channels>" + this.numChannels+ "</channels>\n";
		this.desc += "<chunk>" + this.chunkSize + "</chunk>\n";
		this.desc += "<interleaved>" + this.interleaved + "</interleaved>\n";
		this.desc += "<samplingRate>" + samplingRate + "</samplingRate>\n";
		this.desc += "<create_at>" + this.createdAt + "</create_at>\n";
		this.desc += "<hostname>" + this.hostname + "</hostname>\n";
		this.desc += "<session_id>" + this.sessionID + "</session_id>\n";		
		this.desc += "<source_id>" + this.sourceID + "</source_id>\n";
		this.desc += "<uid>" + this.uid + "</uid>\n";
		this.desc += "<version>" + this.ver + "</version>\n";
		this.desc += "<" + this.getRootNode2ExtraInfoLabel() + "> </" + this.getRootNode2ExtraInfoLabel() + ">\n";
		
		this.desc += "</stream>";
		
	}		

	/**
	 * 
	 * @param libType
	 * @param name
	 * @param dataType
	 * @param numChs
	 * @param chunkSize
	 * @param samplingRate
	 * @param recordingCheckerTimer
	 * @param enableCheckerTimer
	 * @param sourceID
	 * @param uid
	 */
	public SimpleStreamSetting( StreamLibrary libType
								, String name
								, StreamDataType dataType
								, int numChs
								, int chunkSize
								, double samplingRate
								, int recordingCheckerTimer
								, boolean enableCheckerTimer
								, String sourceID
								, String uid								  
								)
	{
		
		this( libType, name, dataType, numChs, chunkSize, samplingRate, recordingCheckerTimer, enableCheckerTimer, sourceID, uid, null );
	}
	
	/**
	 * 
	 * @param libType
	 * @param name
	 * @param dataType
	 * @param numChs
	 * @param chunkSize
	 * @param samplingRate
	 * @param recordingCheckerTimer
	 * @param enableCheckerTimer
	 * @param sourceID
	 * @param uid
	 * @param extraInfo
	 */
	public SimpleStreamSetting( StreamLibrary libType
								, String name
								, StreamDataType dataType
								, int numChs
								, int chunkSize
								, double samplingRate
								, int recordingCheckerTimer
								, boolean enableCheckerTimer
								, String sourceID
								, String uid
								, Map< String, String > extraInfo								  
								)
	{
		
		this( libType, name, dataType
				, StreamDataType.double64, StreamDataType.int64
				, numChs, chunkSize, samplingRate, recordingCheckerTimer, enableCheckerTimer, sourceID, uid, extraInfo );
	}
	
	@Override
	public Map< String, String > getExtraInfo() 
	{
		return this.extraInfo;
	}

	@Override
	public boolean isSelected() 
	{
		return this.selected;
	}

	@Override
	public StreamLibrary getLibraryID() 
	{
		return this.lib_type;
	}

	@Override
	public String name() 
	{
		return this.name;
	}

	@Override
	public String content_type() 
	{
		return this.content;
	}

	@Override
	public int channel_count() 
	{
		return this.numChannels;
	}

	@Override
	public double sampling_rate() 
	{
		return this.samplint_rate;
	}

	@Override
	public StreamDataType data_type() 
	{
		return this.data_type;
	}

	@Override
	public String source_id() 
	{
		return this.sourceID;
	}

	@Override
	public int version() 
	{
		return this.ver;
	}

	@Override
	public double created_at() 
	{
		return this.createdAt;
	}

	@Override
	public String uid() 
	{
		return this.uid;
	}

	@Override
	public String session_id() 
	{
		return this.sessionID;
	}

	@Override
	public String hostname() 
	{
		return this.hostname;
	}

	@Override
	public String description() 
	{
		return this.desc;
	}

	@Override
	public int getChunkSize() 
	{
		return this.chunkSize;
	}

	@Override
	public boolean isInterleavedData() 
	{
		return this.interleaved;
	}

	@Override
	public boolean isSynchronationStream() 
	{
		return this.syncStream;
	}

	@Override
	public StreamDataType getTimestampDataType() 
	{
		return this.timeDataType;
	}

	@Override
	public StreamDataType getStringLengthDataType() 
	{
		return this.stringLenDataType;
	}

	@Override
	public Pointer handle() 
	{
		return null;
	}

	@Override
	public int getDataTypeBytes( StreamDataType type ) 
	{
		int size = 0;
		
		switch ( type ) 
		{		
			case double64:
			{
				size = Double.BYTES;
				
				break;
			}
			case float32:
			{
				size = Float.BYTES;
				
				break;
			}
			case int64:
			{
				size = Long.BYTES;
				
				break;
			}
			case int32:
			{
				size = Integer.BYTES;
				
				break;
			}
			case int16:
			{
				size = Short.BYTES;
				
				break;
			}
			case int8:
			{
				size = Byte.BYTES;
				
				break;
			}
			case string:
			{
				Charset c = Charset.forName( "UTF-8" );
				
				size = ( "A" ).getBytes( c ).length;
				break;
			}
			default:
			{
				break;
			}
		}
		
		return size;
	}

	@Override
	public String getRootNode2ExtraInfoLabel() 
	{
		return StreamExtraLabels.ID_GENERAL_DESCRIPTION_LABEL;
	}

	@Override
	public int getRecordingCheckerTimer() 
	{
		return this.recordingCheckerTimer;
	}

	@Override
	public boolean isEnableRecordingCheckerTimer() 
	{
		return this.enableCheckerTimer;
	}
	
	@Override
	public void destroy() 
	{	
	}

	/*
	@Override
	public void setAdditionalInfo(String id, String info) 
	{	
		this.extraInfo.put( id, info );
	}

	@Override
	public void removeAdditionalInfo(String id) 
	{
		this.extraInfo.remove( id );
	}

	@Override
	public void setSelected( boolean select ) 
	{
		this.selected = select;
	}

	@Override
	public void setSynchronizationStream(boolean sync) 
	{
		this.syncStream = sync;
	}

	@Override
	public void setChunckSize(int size) 
	{
		this.chunkSize = size;
	}

	@Override
	public void setInterleaveadData( boolean interleaved ) 
	{
		this.interleaved = interleaved;
	}

	@Override
	public void setDescription(String desc)
	{
		this.desc = desc;
	}
	 */
}
