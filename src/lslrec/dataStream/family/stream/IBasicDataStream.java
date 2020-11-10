/**
 * 
 */
package lslrec.dataStream.family.stream;

import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;

/**
 * @author Manuel Merino Monge
 * 
 * Based on StreamInlet class from Lab Streaming Layer project.
 *
 */
public interface IBasicDataStream 
{
    /**
     * A very large time duration (> 1 year) for timeout values.
     * Note that significantly larger numbers can cause the timeout to be invalid on some operating systems (e.g., 32-bit UNIX).
     */
    public static final double TIME_FOREVER = 32000000.0;
    
    /**
     * Disconnect and close the inlet.
     */
    public void close();

    /**
     * Retrieve the complete information of the given stream, including the extended description.
     * Can be invoked at any time of the stream's lifetime.
     * @param timeout Timeout of the operation (default: no timeout).
     * @throws Exception (if the timeout expires), or LostException (if the stream source has been lost).
     */
    public IStreamSetting info( double timeout ) throws Exception;
    public IStreamSetting info() throws Exception;

    /**
     * Subscribe to the data stream.
     * All samples pushed in at the other end from this moment onwards will be queued and
     * eventually be delivered in response to pull_sample() or pull_chunk() calls.
     * Pulling a sample without some preceding open_stream is permitted (the stream will then be opened implicitly).
     * @param timeout Optional timeout of the operation (default: no timeout).
     * @throws Exception (if the timeout expires), or LostException (if the stream source has been lost).
     */
    public void open_stream( double timeout ) throws Exception;
    public void open_stream() throws Exception;

    /**
     * Drop the current data stream.
     * All samples that are still buffered or in flight will be dropped and transmission
     * and buffering of data for this inlet will be stopped. If an application stops being
     * interested in data from a source (temporarily or not) but keeps the outlet alive,
     * it should call close_stream() to not waste unnecessary system and network
     * resources.
     */
    public void close_stream();

    /**
     * Retrieve an estimated time correction offset for the given stream.
     * The first call to this function takes several milliseconds until a reliable first estimate is obtained.
     * Subsequent calls are instantaneous (and rely on periodic background updates).
     * The precision of these estimates should be below 1 ms (empirically within +/-0.2 ms).
     * @timeout Timeout to acquire the first time-correction estimate (default: no timeout).
     * @return The time correction estimate. This is the number that needs to be added to a time stamp
     *         that was remotely generated via lsl_local_clock() to map it into the local clock domain of this machine.
     * @throws Exception (if the timeout expires), or LostException (if the stream source has been lost).
     */
    public double time_correction( double timeout ) throws Exception;
    public double time_correction() throws Exception;

    /**
     * Query whether samples are currently available for immediate pickup.
     * Note that it is not a good idea to use samples_available() to determine whether
     * a pull_*() call would block: to be sure, set the pull timeout to 0.0 or an acceptably
     * low value. If the underlying implementation supports it, the value will be the number of
     * samples available (otherwise it will be 1 or 0).
     */
    public int samples_available();

    /**
     * Query whether the clock was potentially reset since the last call to was_clock_reset().
     * This is a rarely-used function that is only useful to applications that combine multiple time_correction
     * values to estimate precise clock drift; it allows to tolerate cases where the source machine was
     * hot-swapped or restarted in between two measurements.
     */
    public boolean was_clock_reset();    
}
