/**
 * 
 */
package lslrec.dataStream.family.setting;

import com.sun.jna.Pointer;

import lslrec.auxiliar.extra.StringTuple;
import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;

/**
 * @author Manuel Merino Monge
 *
 */
public class MutableStreamSetting implements IMutableStreamSetting 
{
	private IStreamSetting str = null;
	
	private String addInfo = null;
	
	private int chunkSize = 1;
	
	private boolean selected = false;
	
	private boolean syncStream = false;
	
	private boolean interleaved = false;
	
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
		
		this.addInfo = this.str.getAdditionalInfo();
		this.chunkSize = this.str.getChunkSize();
		this.selected = this.str.isSelected();
		this.syncStream = this.str.isSynchronationStream();
		this.interleaved = this.str.isInterleavedData();
	}
	
	@Override
	public String getAdditionalInfo()
	{
		return this.addInfo;
	}

	@Override
	public StringTuple getAdditionInfoLabel() 
	{
		return this.str.getAdditionInfoLabel();
	}
	
	@Override
	public boolean isSelected() 
	{
		return this.selected;
	}

	@Override
	public Library getLibraryID() 
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
		String desc = this.str.description();
		
		StringTuple nodeIds = this.getAdditionInfoLabel();
		
		desc = StreamSettingUtils.addElementToXml( desc, nodeIds.t1, nodeIds.t2, this.addInfo );
		
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
	public void setAdditionalInfo(String info) 
	{
		this.addInfo = info;
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
}
