/* 
 * 
 * Based on StreamInlet class from Lab Streaming Layer project.
 * From:
 *  https://github.com/labstreaminglayer/liblsl-Java/blob/39799dae02edf34e138d2a67ae768dc38a0248a9/src/edu/ucsd/sccn/LSL.java
 * 
 */
package lslrec.dataStream.family.stream;

import lslrec.dataStream.family.setting.IStreamSetting;

public interface IObjectStream < T >
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
     * Query whether the clock was potentially reset since the last call to was_clock_reset().
     * This is a rarely-used function that is only useful to applications that combine multiple time_correction
     * values to estimate precise clock drift; it allows to tolerate cases where the source machine was
     * hot-swapped or restarted in between two measurements.
     */
    public boolean was_clock_reset();    

 // =======================================
    // === Pulling a sample from the inlet ===
    // =======================================

    /**
     * Pull a sample from the inlet and read it into an array of values.
     * Handles type checking & conversion.
     * @param sample An array to hold the resulting values.
     * @param timeout The timeout for this operation, if any. Use 0.0 to make the function non-blocking.
     * @return The capture time of the sample on the remote machine, or 0.0 if no new sample was available.
     *         To remap this time stamp to the local clock, add the value returned by .time_correction() to it.
     * @throws Exception (if the stream source has been lost).
     */
    public double pull_sample( T[] sample, double timeout ) throws Exception;
    public double pull_sample( T[] sample ) throws Exception;


    // =============================================================
    // === Pulling a chunk of multiplexed samples from the inlet ===
    // =============================================================

    /**
     * Pull a chunk of data from the inlet.
     * @param data_buffer A pre-allocated buffer where the channel data shall be stored.
     * @param timestamp_buffer A pre-allocated buffer where time stamps shall be stored.
     * @param timeout Optionally the timeout for this operation, if any. When the timeout expires, the function
     *                may return before the entire buffer is filled. The default value of 0.0 will retrieve only
     *                data available for immediate pickup.
     * @return samples_written Number of samples written to the data and timestamp buffers.
     * @throws Exception (if the stream source has been lost).
     */
    public int pull_chunk( T[] data_buffer, double[] timestamp_buffer, double timeout ) throws Exception;
    public int pull_chunk( T[] data_buffer, double[] timestamp_buffer ) throws Exception;
}
