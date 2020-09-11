package lslrec.dataStream.setting;

import lslrec.edu.ucsd.sccn.LSL.StreamInfo;
import lslrec.edu.ucsd.sccn.LSLUtils;

public class DataStreamSetting 
{
	public static final String ID_GENERAL_DESCRIPTION_LABEL = "general";
	
	public static final String ID_RECORD_GENERAL_DESCRIPTION = "recordGeneralDescriptop";
	public static final String ID_SOCKET_MARK_INFO_LABEL = "socketMarkInfo";

	public static final String ID_STREAM_SETTING_LABEL = "streamSetting";
	public static final String ID_CHUNKSIZE_LABEL = "chunkSize";
	public static final String ID_INTERLEAVED_LABEL = "interleaved";
	
	public static final String ID_RECORDED_SAMPLES_BY_CHANNELS = "recordedSamplesByChannels";
	
	protected final String ID_EXTRA_INFO_LABEL = "extra";	
	
	protected int extraCount = 1;
	
	protected String additionalInfo;
	
	protected boolean selectedDevice;
	
	protected int chuckSize = 1;
	protected boolean interleavedData = false;
	protected boolean isSyncStream = false;
	
	protected int timeDataType = LSLUtils.getTimeMarkType();
	protected int strinLengthType = LSLUtils.int64;
		
	protected StreamInfo streamInfo;

	protected DataStreamSetting( DataStreamSetting sst )
	{
		this( sst.getStreamInfo(), sst.getTimeDataType()
				, sst.getAdditionalInfo()
				, sst.getChunkSize()
				, sst.isInterleavedData()
				, sst.isSynchronationStream() );
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
	public DataStreamSetting( StreamInfo stream, int timeDataType, String extraInfo, int chunkSize, boolean interleaved, boolean syncStream  )
	{
		this(stream, extraInfo, chunkSize, interleaved, syncStream);
		
		this.timeDataType = timeDataType;
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
	public DataStreamSetting( StreamInfo stream, int timeDataType, int stringLenType, String extraInfo, int chunkSize, boolean interleaved, boolean syncStream  )
	{
		this(stream, timeDataType, extraInfo, chunkSize, interleaved, syncStream);
		
		this.strinLengthType = stringLenType;
	}
	
	/**
	 * 
	 * @param stream
	 * @param extraInfo
	 * @param chunkSize
	 * @param interleaved
	 * @param syncStream
	 */
	public DataStreamSetting( StreamInfo stream, String extraInfo, int chunkSize, boolean interleaved, boolean syncStream  )
	{
		if( stream == null )
		{
			throw new IllegalArgumentException( "StreamInfo null" );
		}
		
		this.streamInfo = stream;
		this.additionalInfo = extraInfo;
		this.chuckSize = chunkSize;
		this.interleavedData = interleaved;
		this.isSyncStream = syncStream;
	}
	
	/**
	 * 
	 * @param stream
	 * @param extraInfo
	 * @param chunkSize
	 * @param interleaved
	 * 
	 * this( stream, extraInfo, chunkSize, interleaved, false );
	 */
	public DataStreamSetting( StreamInfo stream, String extraInfo, int chunkSize, boolean interleaved )
	{
		this( stream, extraInfo, chunkSize, interleaved, false );
	}
	
	/**
	 * 
	 * @param stream
	 * @param extraInfo
	 * @param chunkSize
	 * 
	 * this( stream, extraInfo, chunkSize, false, false );
	 */
	public DataStreamSetting( StreamInfo stream, String extraInfo, int chunkSize )
	{
		this( stream, extraInfo, chunkSize, false, false );
	}
		
	/**
	 * 
	 * @param stream
	 * @param extraInfo
	 * 
	 * this( stream, extraInfo, 1, false, false );
	 */
	public DataStreamSetting( StreamInfo stream, String extraInfo )
	{
		this( stream, extraInfo, 1, false, false );
	}
	
	/**
	 * 
	 * @param stream
	 * 
	 * this( stream, "", 1, false, false );
	 */
	public DataStreamSetting( StreamInfo stream  )
	{
		this( stream, "", 1, false, false );
	}
		

	/**
	 * 
	 * @return LSL name.
	 */
	public String getStreamName()
	{
		return this.streamInfo.name();
	}

	/**
	 * 
	 * @return LSL type value.
	 */
	public String getDeviceType()
	{
		return this.streamInfo.type();
	}

	/**
	 * 
	 * @return additional information.
	 */
	public String getAdditionalInfo()
	{
		return this.additionalInfo;
	}
	
	/**
	 * 
	 * @return if is selected the LSL streaming.
	 */
	public boolean isSelected()
	{
		return this.selectedDevice;
	}
	
	/**
	 * 
	 * @return LSL UID.
	 */
	public String getUID()
	{
		return this.streamInfo.uid();
	}	
	
	/**
	 * 
	 * @return source ID.
	 */
	public String getSourceID()
	{
		return this.streamInfo.source_id();
	}
	
	/**
	 * 
	 * @return chunck size.
	 */
	public int getChunkSize()
	{
		return this.chuckSize;
	}
		
	/**
	 * 
	 * @return If LSL data of channels are interleaved.
	 */
	public boolean isInterleavedData()
	{
		return this.interleavedData;
	}
	
	/**
	 * @return Indicate if synchronization marks are caught from this stream.
	 */
	public boolean isSynchronationStream()
	{
		return this.isSyncStream;
	}
	
	/**
	 * 
	 * @return Sampling rate
	 */
	public double getSamplingRate()
	{
		return this.streamInfo.nominal_srate();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return 		"<" 	+ this.streamInfo.source_id() 		// 1
				+ 	", " 	+ this.streamInfo.name() 			// 2
				+ 	", " 	+ this.streamInfo.type() 			// 3
				+ 	", " 	+ this.additionalInfo 				// 4
				+ 	", " 	+ this.selectedDevice 				// 5
				+ 	", " 	+ this.chuckSize 					// 6
				+ 	", " 	+ this.interleavedData				// 7
				+ 	", " 	+ this.isSyncStream					// 8
 				+ 	">";
	}	
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() 
	{		
		return ( this.streamInfo.name() + this.streamInfo.type() ).hashCode();
	}
	
	public void increaseExtraCountLabel()
	{
		this.extraCount++;
	}
	
	public String getExtraInfoLabel()
	{
		String lab = this.ID_EXTRA_INFO_LABEL;
		
		if( this.extraCount > 1 )
		{
			lab += this.extraCount;
		}
		
		return lab;
	}
	
	public int getDataType() 
	{
		return this.streamInfo.channel_format();
	}
	
	public StreamInfo getStreamInfo()
	{
		return this.streamInfo;
	}
	
	/**
	 * @return Time data type
	 */
	public int getTimeDataType() 
	{
		return timeDataType;
	}
	
	/**
	 * 
	 * @return Data type of string length 
	 */
	public int getStringLegthType()
	{
		return this.strinLengthType;
	}
}
