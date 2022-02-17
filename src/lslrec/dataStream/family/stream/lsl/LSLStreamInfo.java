/*
 * 
 * From:
 *  https://github.com/labstreaminglayer/liblsl-Java/blob/39799dae02edf34e138d2a67ae768dc38a0248a9/src/edu/ucsd/sccn/LSL.java
 * 
 */
package lslrec.dataStream.family.stream.lsl;

import java.util.HashMap;
import java.util.Map;

import com.sun.jna.Pointer;

import lslrec.dataStream.family.setting.IMutableStreamSetting;
import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;



// ==========================
// === Stream Declaration ===
// ==========================

/**
 * The stream_info this.object stores the declaration of a data stream.
 * Represents the following information:
 *  a) stream data format (#channels, channel format)
 *  b) core information (stream name, content type, sampling rate)
 *  c) optional meta-data about the stream content (channel labels, measurement units, etc.)
 *
 * Whenever a program wants to provide a new stream on the lab network it will typically first
 * create a stream_info to describe its properties and then construct a stream_outlet with it to create
 * the stream on the network. Recipients who discover the outlet can query the stream_info; it is also
 * written to disk when recording the stream (playing a similar role as a file header).
 */

public class LSLStreamInfo implements IMutableStreamSetting 
{
	private LSLDll inst = LSL.getDllInstance();
	
	private Pointer obj;
	
	private Map< String, String > additionalInfo = new HashMap<String, String>();
	
	private boolean selectedStream;
	
	private int chuckSize = 1;
	private boolean interleavedData = false;
	private boolean isSyncStream = false;
	
	private int recordingCheckerTimer;
		
    /**
     * Construct a new stream_info this.object.
     * Core stream information is specified here. Any remaining meta-data can be added later.
     * @param name Name of the stream. Describes the device (or product series) that this stream makes available
     *            (for use by programs, experimenters or data analysts). Cannot be empty.
     * @param type Content type of the stream. Please see https://github.com/sccn/xdf/wiki/Meta-Data (or web search for:
     *            XDF meta-data) for pre-defined content-type names, but you can also make up your own.
     *            The content type is the preferred way to find streams (as opposed to searching by name).
     * @param channel_count Number of channels per sample. This stays constant for the lifetime of the stream.
     * @param nominal_srate The sampling rate (in Hz) as advertised by the data source, if regular (otherwise set to IRREGULAR_RATE).
     * @param channel_format Format/type of each channel. If your channels have different formats, consider supplying
     *                       multiple streams or use the largest type that can hold them all (such as cf_double64).
     * @param source_id Unique identifier of the device or source of the data, if available (such as the serial number).
     *                  This is critical for system robustness since it allows recipients to recover from failure even after the
     *                 serving app, device or computer crashes (just by finding a stream with the same source id on the network again).
     *                 Therefore, it is highly recommended to always try to provide whatever information can uniquely identify the data source itself.
     */
    public LSLStreamInfo(String name, String type, int channel_count, double nominal_srate, int channel_format, String source_id) { this.obj = this.inst.lsl_create_streaminfo( name, type, channel_count, nominal_srate, channel_format, source_id ); }
    public LSLStreamInfo(String name, String type, int channel_count, double nominal_srate, int channel_format) { this.obj = this.inst.lsl_create_streaminfo( name, type, channel_count, nominal_srate, channel_format, ""); }
    public LSLStreamInfo(String name, String type, int channel_count, double nominal_srate) { this.obj = this.inst.lsl_create_streaminfo(name, type, channel_count, nominal_srate, StreamDataType.float32.ordinal(), ""); }
    public LSLStreamInfo(String name, String type, int channel_count) { this.obj = this.inst.lsl_create_streaminfo(name, type, channel_count, IRREGULAR_RATE, StreamDataType.float32.ordinal(), ""); }
    public LSLStreamInfo(String name, String type) { this.obj = this.inst.lsl_create_streaminfo(name, type, 1, IRREGULAR_RATE, StreamDataType.float32.ordinal(), ""); }
    public LSLStreamInfo(Pointer handle) { this.obj = handle; }

    /** Destroy a previously created LSLStreamInfo this.object. */
    public void destroy() { this.inst.lsl_destroy_streaminfo( this.obj ); }


    @Override
    public StreamLibrary getLibraryID() 
    {
    	return StreamLibrary.LSL;
    }
    
    // ========================
    // === Core Information ===
    // ========================
    // (these fields are assigned at construction)

    /**
     * Name of the stream. This is a human-readable name. For streams
     * offered by device modules, it refers to the type of device or product
     * series that is generating the data of the stream. If the source is an
     * application, the name may be a more generic or specific identifier.
     * Multiple streams with the same name can coexist, though potentially
     * at the cost of ambiguity (for the recording app or experimenter).
     */
    public String name() { return this.inst.lsl_get_name(this.obj); }

    /**
     * Content type of the stream. The content type is a short string such
     * as "EEG", "Gaze" which describes the content carried by the channel
     * (if known). If a stream contains mixed content this value need not be
     * assigned but may instead be stored in the description of channel
     * types. To be useful to applications and automated processing systems
     * using the recommended content types is preferred. See Table of
     * Content types usually follow those pre-defined in
     * https://github.com/sccn/xdf/wiki/Meta-Data (or web search for:
     * XDF meta-data).
     */
    public String content_type() { return this.inst.lsl_get_type(this.obj); }

    /**
     * Number of channels of the stream. A stream has at least one channel;
     * the channel count stays constant for all samples.
     */
    public int channel_count() { return this.inst.lsl_get_channel_count(this.obj); }

    /**
     * Sampling rate of the stream, according to the source (in Hz). If a
     * stream is irregularly sampled, this should be set to IRREGULAR_RATE.
     *
     * Note that no data will be lost even if this sampling rate is
     * incorrect or if a device has temporary hiccups, since all samples
     * will be recorded anyway (except for those dropped by the device
     * itself). However, when the recording is imported into an application,
     * a good importer may correct such errors more accurately if the
     * advertised sampling rate was close to the specs of the device.
     */
    public double sampling_rate() { return this.inst.lsl_get_nominal_srate(this.obj); }

    /**
     * Channel format of the stream. All channels in a stream have the same
     * format. However, a device might offer multiple time-synched streams
     * each with its own format.
     */
    public StreamDataType  data_type() 
    {
    	StreamDataType t = StreamDataType.undefined;
    	
    	StreamDataType[] types = StreamDataType.values();
    	    	
    	int val = this.inst.lsl_get_channel_format( this.obj );
    	
    	for( StreamDataType sdt : types )
    	{
    		if( sdt.ordinal() == val )
    		{
    			t = sdt;
    			
    			break;
    		}
    	}
    	
    	return t;
    }

    /**
     * Unique identifier of the stream's source, if available. The unique
     * source (or device) identifier is an optional piece of information
     * that, if available, allows that endpoints (such as the recording
     * program) can re-acquire a stream automatically once it is back
     * online.
     */
    public String source_id() { return this.inst.lsl_get_source_id(this.obj); }


    // ======================================
    // === Additional Hosting Information ===
    // ======================================
    // (these fields are implicitly assigned once bound to an outlet/inlet)

    /**
     * Protocol version used to deliver the stream.
     */
    public int version() { return this.inst.lsl_get_version(this.obj); }

    /**
     * Creation time stamp of the stream. This is the time stamp when the
     * stream was first created (as determined via local_clock() on the
     * providing machine).
     */
    public double created_at() { return this.inst.lsl_get_created_at(this.obj); }

    /**
     * Unique ID of the stream outlet instance (once assigned). This is a
     * unique identifier of the stream outlet, and is guaranteed to be
     * different across multiple instantiations of the same outlet (e.g.,
     * after a re-start).
     */
    public String uid() { return this.inst.lsl_get_uid(this.obj); }

    /**
     * Session ID for the given stream. The session id is an optional
     * human-assigned identifier of the recording session. While it is
     * rarely used, it can be used to prevent concurrent recording
     * activitites on the same sub-network (e.g., in multiple experiment
     * areas) from seeing each other's streams (assigned via a configuration
     * file by the experimenter, see Network Connectivity in the LSL wiki).
     */
    public String session_id() { return this.inst.lsl_get_session_id(this.obj); }

    /**
     * Hostname of the providing machine.
     */
    public String hostname() { return this.inst.lsl_get_hostname(this.obj); }

    // ========================
    // === Data Description ===
    // ========================

    /**
     * Extended description of the stream.
     * It is highly recommended that at least the channel labels are described here.
     * See code examples on the LSL wiki. Other information, such as amplifier settings,
     * measurement units if deviating from defaults, setup information, subject information, etc.,
     * can be specified here, as well. Meta-data recommendations follow the XDF file format project
     * (github.com/sccn/xdf/wiki/Meta-Data or web search for: XDF meta-data).
     *
     * Important: if you use a stream content type for which meta-data recommendations exist, please
     * try to lay out your meta-data in agreement with these recommendations for compatibility with other applications.
     */
    public XMLElement desc() { return new XMLElement(this.inst.lsl_get_desc(this.obj)); }

    /**
     * Retrieve the entire stream_info in XML format.
     * This yields an XML document (in string form) whose top-level element is <info>. The info element contains
     * one element for each field of the stream_info class, including:
     *  a) the core elements <name>, <type>, <channel_count>, <nominal_srate>, <channel_format>, <source_id>
     *  b) the misc elements <version>, <created_at>, <uid>, <session_id>, <v4address>, <v4data_port>, <v4service_port>, <v6address>, <v6data_port>, <v6service_port>
     *  c) the extended description element <desc> with user-defined sub-elements.
     */
    public String description() { return this.inst.lsl_get_xml(this.obj); }

    /**
     * Get access to the underlying native handle.
     */
    public Pointer handle() { return this.obj; }

	@Override
	public Map< String, String > getExtraInfo() 
	{
		return this.additionalInfo;
	}
	
	@Override
	public boolean isSelected() 
	{
		return this.selectedStream;
	}
	
	@Override
	public int getChunkSize() 
	{
		return this.chuckSize;
	}
	
	@Override
	public boolean isInterleavedData() 
	{
		return this.interleavedData;
	}
	
	@Override
	public boolean isSynchronationStream() 
	{
		return this.isSyncStream;
	}
	
	@Override
	public StreamDataType getTimestampDataType() 
	{
		return StreamDataType.double64;
	}
	
	@Override
	public StreamDataType getStringLegthDataType() 
	{
		return StreamDataType.int64;
	}
	
	@Override
	public void setAdditionalInfo( String id, String info) 
	{
		if( id != null && info != null )
		{
			this.additionalInfo.put( id, info );
		}
	}
	
	@Override
	public void removeAdditionalInfo(String id) 
	{
		if( id != null )
		{
			this.additionalInfo.remove( id );
		}
	}
	
	@Override
	public void setSelected(boolean select) 
	{
		this.selectedStream = select;
	}
	
	@Override
	public void setSynchronizationStream(boolean sync) 
	{
		this.isSyncStream = sync;
	}
	
	@Override
	public void setChunckSize( int size ) 
	{
		this.chuckSize = size;
	}
	
	@Override
	public void setInterleaveadData( boolean interleaved ) 
	{
		this.interleavedData = interleaved;
	}
		
	@Override
	public int getDataTypeBytes(StreamDataType type) 
	{	
		return LSLUtils.getDataTypeBytes( type );
	}
	
	@Override
	public String getRootNode2ExtraInfoLabel() 
	{
		String root = "desc";
		
		return root;
	}
	
	/**
	 * Without effect.
	 */
	@Override
	public void setDescription( String desc ) 
	{
		
	}
	@Override
	public int getRecordingCheckerTimer() 
	{
		return this.recordingCheckerTimer;
	}
	
	@Override
	public void setRecordingCheckerTimer( int t ) 
	{
		this.recordingCheckerTimer = t;
	}
}

