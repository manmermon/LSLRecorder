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
 * Based on StreamInfo class from Lab Streaming Layer project.
 *
 */

/**
 * The stream_info object stores the declaration of a data stream.
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
public interface IStreamSetting 
{
	enum Library { LSL };
	
	 /**
     * Constant to indicate that a stream has variable sampling rate.
     */
    public static final double IRREGULAR_RATE = 0.0;
	
	/**
	 * 
	 * @return additional information.
	 */
	public String getAdditionalInfo();
	
	/**
	 * 
	 * @return Tuple with root node and node name where adding information.
	 * 			rootNode = Tuple.t1;
	 * 			nodeName = Tuple.t2;
	 * 			result: <rootNode><nodeName>getAdditionalInfo()</nodeName></rootNode>
	 * @see this.description()
	 */
	public StringTuple getAdditionInfoLabel();
	
	/**
	 * 
	 * @return if is selected the LSL streaming.
	 */
	public boolean isSelected();	
	
    public abstract Library getLibraryID();
    
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
    public abstract String name();

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
    public abstract String content_type();

    /**
     * Number of channels of the stream. A stream has at least one channel;
     * the channel count stays constant for all samples.
     */
    public abstract int channel_count();

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
    public abstract double sampling_rate();

    /**
     * Channel format of the stream. All channels in a stream have the same
     * format. However, a device might offer multiple time-synched streams
     * each with its own format.
     */
    public abstract StreamDataType data_type();

    /**
     * Unique identifier of the stream's source, if available. The unique
     * source (or device) identifier is an optional piece of information
     * that, if available, allows that endpoints (such as the recording
     * program) can re-acquire a stream automatically once it is back
     * online.
     */
    public abstract String source_id();


    // ======================================
    // === Additional Hosting Information ===
    // ======================================
    // (these fields are implicitly assigned once bound to an outlet/inlet)

    /**
     * Protocol version used to deliver the stream.
     */
    public abstract int version();

    /**
     * Creation time stamp of the stream. This is the time stamp when the
     * stream was first created (as determined via local_clock() on the
     * providing machine).
     */
    public abstract double created_at();

    /**
     * Unique ID of the stream outlet instance (once assigned). This is a
     * unique identifier of the stream outlet, and is guaranteed to be
     * different across multiple instantiations of the same outlet (e.g.,
     * after a re-start).
     */
    public abstract String uid();

    /**
     * Session ID for the given stream. The session id is an optional
     * human-assigned identifier of the recording session. While it is
     * rarely used, it can be used to prevent concurrent recording
     * activitites on the same sub-network (e.g., in multiple experiment
     * areas) from seeing each other's streams (assigned via a configuration
     * file by the experimenter, see Network Connectivity in the LSL wiki).
     */
    public abstract String session_id();

    /**
     * Hostname of the providing machine.
     */
    public abstract String hostname();
   
    /**
     * Retrieve the entire stream_info in XML format.
     * This yields an XML document (in string form) whose top-level element is <info>. The info element contains
     * one element for each field of the stream_info class, including:
     *  a) the core elements <name>, <type>, <channel_count>, <nominal_srate>, <channel_format>, <source_id>
     *  b) the misc elements <version>, <created_at>, <uid>, <session_id>, <v4address>, <v4data_port>, <v4service_port>, <v6address>, <v6data_port>, <v6service_port>
     *  c) the extended description element <desc> with user-defined sub-elements.
     */
    public abstract String description();
    
    /**
	 * 
	 * @return chunck size.
	 */
	public abstract int getChunkSize();
	
	/**
	 * 
	 * @return If data of channels are interleaved.
	 */
	public abstract boolean isInterleavedData();
	
	/**
	 * @return Indicate if synchronization marks are caught from this stream.
	 */
	public abstract boolean isSynchronationStream();
	
	/**
	 * @return data type of timestamp
	 */
	public abstract StreamDataType getTimestampDataType();
	
	/**
	 * 
	 * @return Data type of string length 
	 */
	public abstract StreamDataType getStringLegthDataType();
    
    /**
     * Get access to the underlying native handle.
     */
    public abstract Pointer handle();
 
    public int getDataTypeBytes( StreamDataType type );
    
    public default int streamHashCode()
    {
    	String t = "" + this.name() + this.content_type() + this.source_id();  
    	
    	return t.hashCode();
    }
}