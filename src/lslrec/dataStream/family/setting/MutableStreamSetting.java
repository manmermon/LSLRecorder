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

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Pointer;

import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;
import lslrec.exceptions.ReadInputDataException;

/**
 * @author Manuel Merino Monge
 *
 */
public class MutableStreamSetting implements IMutableStreamSetting 
{
	private IStreamSetting str = null;
	
	private Map< String, String > extraInfo = null;
	
	private int chunkSize = 1;
	
	private boolean selected = false;
	
	private boolean syncStream = false;
	
	private boolean interleaved = false;
	
	private String description = null;
	
	/**
	 * 
	 * @param streamSetting
	 * @throws IllegalArgumentException if streamsSetting is null
	 */
	public MutableStreamSetting( IStreamSetting streamSetting )
	{
		if( streamSetting == null )
		{
			throw new IllegalArgumentException( "Stream setting null." );
		}
		
		this.str = streamSetting;
		
		this.extraInfo = this.str.getExtraInfo();
		
		if( this.extraInfo == null )
		{
			this.extraInfo = new HashMap<String, String>();
		}
		
		this.chunkSize = this.str.getChunkSize();
		this.selected = this.str.isSelected();
		this.syncStream = this.str.isSynchronationStream();
		this.interleaved = this.str.isInterleavedData();
	}
	
	@Override
	public Map< String, String> getExtraInfo()
	{
		return this.extraInfo;
	}

	@Override
	public String getRootNode2ExtraInfoLabel() 
	{
		return this.str.getRootNode2ExtraInfoLabel();
	}
	
	@Override
	public boolean isSelected() 
	{
		return this.selected;
	}

	@Override
	public StreamLibrary getLibraryID() 
	{
		return this.str.getLibraryID();
	}

	@Override
	public String name() 
	{
		return this.str.name();
	}

	@Override
	public String content_type() 
	{
		return this.str.content_type();
	}

	@Override
	public int channel_count() 
	{
		return this.str.channel_count();
	}

	@Override
	public double sampling_rate() 
	{
		return this.str.sampling_rate();
	}

	@Override
	public StreamDataType data_type() 
	{
		return this.str.data_type();
	}

	@Override
	public String source_id() 
	{
		return this.str.source_id();
	}

	@Override
	public int version() 
	{
		return this.str.version();
	}

	@Override
	public double created_at() 
	{
		return this.str.created_at();
	}

	@Override
	public String uid() 
	{
		return this.str.uid();
	}

	@Override
	public String session_id() 
	{
		return this.str.session_id();
	}

	@Override
	public String hostname() 
	{
		return this.str.hostname();
	}

	@Override
	public String description() 
	{
		String desc = this.description;
		
		if( desc == null )
		{
			desc = this.str.description();
			String rootNode = this.getRootNode2ExtraInfoLabel();
			
			for( String nodeName : this.extraInfo.keySet() )
			{
				String info = this.extraInfo.get( nodeName );
				desc = StreamSettingUtils.addElementToXmlStreamDescription( desc, rootNode, nodeName, info );
			}
		}
		
		return desc;
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
		return this.str.getTimestampDataType();
	}

	@Override
	public StreamDataType getStringLegthDataType() 
	{
		return this.str.getStringLegthDataType();
	}

	@Override
	public Pointer handle() 
	{
		return this.str.handle();
	}

	@Override
	public void setAdditionalInfo( String id, String info ) 
	{
		this.extraInfo.put( id, info );
	}
	
	@Override
	public void removeAdditionalInfo( String id ) 
	{
		this.extraInfo.remove( id );
	}

	@Override
	public void setSelected(boolean select) 
	{
		this.selected = select;
	}

	@Override
	public void setSynchronizationStream(boolean sync) 
	{
		this.syncStream = sync;
	}

	@Override
	public void setChunckSize( int size ) 
	{
		this.chunkSize = size;
	}

	@Override
	public void setInterleaveadData( boolean interleaved )
	{
		this.interleaved = interleaved;
	}

    public int getDataTypeBytes( StreamDataType type )
    {
    	return this.str.getDataTypeBytes( type );
    }

	@Override
	public int getStreamBufferLength() throws ReadInputDataException 
	{
		return this.str.getStreamBufferLength();
	}
	
	public IStreamSetting getStreamSetting()
	{
		return this.str;
	}

	@Override
	public void setDescription( String desc ) 
	{
		this.description = desc;
	}
	
	@Override
	public boolean equals( Object obj ) 
	{
		boolean eq = super.equals( obj );
		
		if( !eq )
		{
			eq = this.str.equals( obj );
		}
		
		return eq;
	}
}
