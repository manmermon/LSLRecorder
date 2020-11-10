/**
 * 
 */
package lslrec.dataStream.family.stream;

/**
 * @author Manuel Merino Monge
 * 
 * Based on StreamInlet class from Lab Streaming Layer project.
 *
 */
public interface INumberStream extends IBasicDataStream 
{   
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
    public double pull_sample( float[] sample, double timeout ) throws Exception;
    public double pull_sample( float[] sample ) throws Exception;
    public double pull_sample( double[] sample, double timeout ) throws Exception;
    public double pull_sample( double[] sample ) throws Exception;
    public double pull_sample( int[] sample, double timeout ) throws Exception;
    public double pull_sample( int[] sample ) throws Exception;
    public double pull_sample( short[] sample, double timeout ) throws Exception;
    public double pull_sample( short[] sample ) throws Exception;
    public double pull_sample( byte[] sample, double timeout ) throws Exception;
    public double pull_sample( byte[] sample ) throws Exception;



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
    public int pull_chunk( float[] data_buffer, double[] timestamp_buffer, double timeout ) throws Exception;
    public int pull_chunk( float[] data_buffer, double[] timestamp_buffer ) throws Exception;
    public int pull_chunk( double[] data_buffer, double[] timestamp_buffer, double timeout ) throws Exception;
    public int pull_chunk( double[] data_buffer, double[] timestamp_buffer ) throws Exception;
    public int pull_chunk( short[] data_buffer, double[] timestamp_buffer, double timeout ) throws Exception;
    public int pull_chunk( short[] data_buffer, double[] timestamp_buffer ) throws Exception;
    public int pull_chunk( byte[] data_buffer, double[] timestamp_buffer, double timeout ) throws Exception;
    public int pull_chunk( byte[] data_buffer, double[] timestamp_buffer ) throws Exception;
    public int pull_chunk( int[] data_buffer, double[] timestamp_buffer, double timeout ) throws Exception;
    public int pull_chunk( int[] data_buffer, double[] timestamp_buffer ) throws Exception;
}
