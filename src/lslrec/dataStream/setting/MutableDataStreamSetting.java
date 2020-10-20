/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018-2019 by Manuel Merino Monge <manmermon@dte.us.es>
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

import lslrec.dataStream.family.lsl.LSL.StreamInfo;

/**
 * 
 * @author Manuel Merino Monge
 *
 */
public class MutableDataStreamSetting extends DataStreamSetting
{	
	public MutableDataStreamSetting( DataStreamSetting streamSetting )
	{
		this( streamSetting.getStreamInfo()
				, streamSetting.getTimeDataType()
				, streamSetting.getStringLegthType()
				, streamSetting.getAdditionalInfo()
				, streamSetting.getChunkSize()
				, streamSetting.isInterleavedData()
				, streamSetting.isSynchronationStream() );
	}
	
	/**
	 * 
	 * @param stream
	 * @param timeDataTyp
	 * @param extraInfo
	 * @param chunkSize
	 * @param interleaved
	 * @param syncStream
	 */
	public MutableDataStreamSetting( StreamInfo stream, int timeDataType, String extraInfo, int chunkSize, boolean interleaved, boolean syncStream  )
	{
		super(stream, timeDataType, extraInfo, chunkSize, interleaved, syncStream);
	}
	
	/**
	 * 
	 * @param stream
	 * @param timeDataType
	 * @param stringLenType
	 * @param extraInfo
	 * @param chunkSize
	 * @param interleaved
	 * @param syncStream
	 */
	public MutableDataStreamSetting( StreamInfo stream, int timeDataType, int stringLenType, String extraInfo, int chunkSize, boolean interleaved, boolean syncStream  )
	{
		super(stream, timeDataType, stringLenType, extraInfo, chunkSize, interleaved, syncStream);
	}
	
	/**
	 * 
	 * @param stream
	 * @param extraInfo
	 * @param chunkSize
	 * @param interleaved
	 * @param syncStream
	 * 
	 * super( stream, extraInfo, chunkSize, interleaved, syncStream );
	 */
	public MutableDataStreamSetting( StreamInfo stream, String extraInfo, int chunkSize, boolean interleaved, boolean syncStream  )
	{
		super( stream, extraInfo, chunkSize, interleaved, syncStream );
	}
	
	/**
	 * 
	 * @param stream
	 * @param extraInfo
	 * @param chunkSize
	 * @param interleaved
	 * 
	 * super( stream, extraInfo, chunkSize, interleaved, false );
	 */
	public MutableDataStreamSetting( StreamInfo stream, String extraInfo, int chunkSize, boolean interleaved )
	{
		super( stream, extraInfo, chunkSize, interleaved, false );
	}
	
	/**
	 * 
	 * @param stream
	 * @param extraInfo
	 * @param chunkSize
	 * 
	 * super( stream, extraInfo, chunkSize, false, false );
	 */
	public MutableDataStreamSetting( StreamInfo stream, String extraInfo, int chunkSize )
	{
		super( stream, extraInfo, chunkSize, false, false );
	}
		
	/**
	 * 
	 * @param stream
	 * @param extraInfo
	 * 
	 * super( stream, extraInfo, 1, false, false );
	 */
	public MutableDataStreamSetting( StreamInfo stream, String extraInfo )
	{
		super( stream, extraInfo, 1, false, false );
	}
	
	/**
	 * 
	 * @param stream
	 * 
	 * super( stream, "", 1, false, false );
	 */
	public MutableDataStreamSetting( StreamInfo stream  )
	{
		super( stream, "", 1, false, false );
	}
	
	public void setStreamInfo( StreamInfo stream )
	{
		if( stream == null )
		{
			throw new IllegalArgumentException( "StreamInfo null" );
		}
		
		super.streamInfo = stream;
	}
	
	/**
	 * Set addtional information.
	 * 
	 * @param info
	 */
	public void setAdditionalInfo( String info )
	{
		super.additionalInfo = info;
	}

	/**
	 * Set if the LSL streaming is selected.
	 * @param select
	 */
	public void setSelected( boolean select )
	{
		super.selectedDevice = select;
	}

	/**
	 * 
	 * @param sync
	 */
	public void setSynchronizationStream( boolean sync )
	{
		super.isSyncStream = sync;
	}
		
	/**
	 * Set the chunck size.
	 * 
	 * @param size
	 */
	public void setChunckSize( int size )
	{
		super.chuckSize = size;		
		
		if( super.chuckSize < 1 )
		{
			super.chuckSize = 1;
		}
	}
	
	/**
	 * Set LSL data of channels are interleaved.
	 * 
	 * @param interleaved
	 */
	public void setInterleaveadData( boolean interleaved )
	{
		super.interleavedData = interleaved;
	}
	
	/**
	 * Set time data type.
	 * @param type
	 */
	public void setTimeDataType( int type )
	{
		super.timeDataType = type;
	}
}
