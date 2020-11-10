/**
 * 
 */
package lslrec.dataStream.family.setting;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Pointer;

import lslrec.auxiliar.extra.StringTuple;
import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;

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
	private int chunkSize = 1;
	
	private boolean interleaved = false;
	
	private boolean syncStream = false;
	
	private boolean selected = false;
	
	private Library lib_type = Library.LSL;
	
	private int numChannels = 1;
	
	private double samplint_rate = 0D;
	 
	private String sourceID = "", uid = ""
					, hostname = "", desc = ""
					, sessionID = "";
	private int ver;
	
	private double createdAt;
	
	/**
	 * 
	 * @param name
	 * @param content_type
	 * @param dataType
	 * @param timeDataTyp
	 * @param extraInfo
	 * @param chunkSize
	 * @param interleaved
	 * @param selected
	 * @param syncStream
	 */
	public SimpleStreamSetting( Library libType
								, String name
								, String content_type
								, StreamDataType dataType 
								, StreamDataType timeDataType
								, StreamDataType stringLenType
								, int numChs
								, double samplingRate
								, String sourceID
								, String uid
								, String hostname
								, String sessionID
								, int ver
								, double createdAt
								, String desc
								, Map< String, String > extraInfo
								, int chunkSize
								, boolean interleaved
								, boolean selected
								, boolean syncStream  )
	{
		this.name = name;
		
		this.content = content_type;
		
		this.data_type = dataType;
		
		this.timeDataType = timeDataType;
		
		this.stringLenDataType = stringLenType;
		
		this.extraInfo = extraInfo;
		if( this.extraInfo == null )
		{
			this.extraInfo = new HashMap<String, String>();
		}
		
		this.chunkSize = chunkSize;

		this.interleaved = interleaved;
		
		this.selected = selected;
		
		this.syncStream = syncStream;
		
		this.lib_type = libType;
		
		this.numChannels = numChs;
		
		this.samplint_rate = samplingRate;

		this.sourceID = sourceID;
		this.uid = uid;
		this.hostname = hostname;
		this.ver = ver;
		this.createdAt = createdAt;
		this.desc = desc;
		this.sessionID = sessionID;
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
	public Library getLibraryID() 
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
	public StreamDataType getStringLegthDataType() 
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
		return StreamSettingExtraLabels.ID_GENERAL_DESCRIPTION_LABEL;
	}

}
