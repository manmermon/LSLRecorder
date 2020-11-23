/*
 * From https://github.com/labstreaminglayer/liblsl-Java/blob/39799dae02edf34e138d2a67ae768dc38a0248a9/src/edu/ucsd/sccn/LSL.java
 */

package lslrec.testing;

import java.io.IOException;

import com.sun.jna.Pointer;

import lslrec.dataStream.family.stream.lsl.LSL;
import lslrec.dataStream.family.stream.lsl.LSLStreamInfo;
import lslrec.dataStream.family.stream.lsl.LSLDll;

/**
 * @author Manuel Merino Monge
 *
 */
// =======================
// ==== Stream Outlet ====
// =======================

/**
 * A stream outlet.
 * Outlets are used to make streaming data (and the meta-data) available on the lab network.
 */
public class StreamOutlet 
{
	private LSLDll inst = LSL.getDllInstance();
	
    /**
     * Establish a new stream outlet. This makes the stream discoverable.
     * @param info The stream information to use for creating this stream. Stays constant over the lifetime of the outlet.
     * @param chunk_size Optionally the desired chunk granularity (in samples) for transmission. If unspecified,
     *                  each push operation yields one chunk. Inlets can override this setting.
     * @param max_buffered Optionally the maximum amount of data to buffer (in seconds if there is a nominal
     *                     sampling rate, otherwise x100 in samples). The default is 6 minutes of data.
     */
    public StreamOutlet(LSLStreamInfo info, int chunk_size, int max_buffered) throws IOException { obj = inst.lsl_create_outlet(info.handle(), chunk_size, max_buffered); throw new IOException("Unable to open LSL outlet.");}
    public StreamOutlet(LSLStreamInfo info, int chunk_size) throws IOException { obj = inst.lsl_create_outlet(info.handle(), chunk_size, 360); throw new IOException("Unable to open LSL outlet.");}
    public StreamOutlet(LSLStreamInfo info) throws IOException { obj = inst.lsl_create_outlet(info.handle(), 0, 360); if(obj == null) throw new IOException("Unable to open LSL outlet."); }

    /**
     * Close the outlet.
     * The stream will no longer be discoverable after closure and all paired inlets will stop delivering data.
     */
    public void close() { inst.lsl_destroy_outlet(obj); }


    // ========================================
    // === Pushing a sample into the outlet ===
    // ========================================

    /**
     * Push an array of values as a sample into the outlet.
     * Each entry in the vector corresponds to one channel.
     * @param data An array of values to push (one for each channel).
     * @param timestamp Optionally the capture time of the sample, in agreement with local_clock(); if omitted, the current time is used.
     * @param pushthrough Optionally whether to push the sample through to the receivers instead of buffering it with subsequent samples.
     *                   Note that the chunk_size, if specified at outlet construction, takes precedence over the pushthrough flag.
     */
    public void push_sample(float[] data, double timestamp, boolean pushthrough) { inst.lsl_push_sample_ftp(obj, data, timestamp, pushthrough ? 1 : 0); }
    public void push_sample(float[] data, double timestamp) { inst.lsl_push_sample_ft(obj, data, timestamp); }
    public void push_sample(float[] data) { inst.lsl_push_sample_f(obj, data); }
    public void push_sample(double[] data, double timestamp, boolean pushthrough) { inst.lsl_push_sample_dtp(obj, data, timestamp, pushthrough ? 1 : 0); }
    public void push_sample(double[] data, double timestamp) { inst.lsl_push_sample_dt(obj, data, timestamp); }
    public void push_sample(double[] data) { inst.lsl_push_sample_d(obj, data); }
    public void push_sample(int[] data, double timestamp, boolean pushthrough) { inst.lsl_push_sample_itp(obj, data, timestamp, pushthrough ? 1 : 0); }
    public void push_sample(int[] data, double timestamp) { inst.lsl_push_sample_it(obj, data, timestamp); }
    public void push_sample(int[] data) { inst.lsl_push_sample_i(obj, data); }
    public void push_sample(short[] data, double timestamp, boolean pushthrough) { inst.lsl_push_sample_stp(obj, data, timestamp, pushthrough ? 1 : 0); }
    public void push_sample(short[] data, double timestamp) { inst.lsl_push_sample_st(obj, data, timestamp); }
    public void push_sample(short[] data) { inst.lsl_push_sample_s(obj, data); }
    public void push_sample(byte[] data, double timestamp, boolean pushthrough) { inst.lsl_push_sample_ctp(obj, data, timestamp, pushthrough ? 1 : 0); }
    public void push_sample(byte[] data, double timestamp) { inst.lsl_push_sample_ct(obj, data, timestamp); }
    public void push_sample(byte[] data) { inst.lsl_push_sample_c(obj, data); }
    public void push_sample(String[] data, double timestamp, boolean pushthrough) { inst.lsl_push_sample_strtp(obj, data, timestamp, pushthrough ? 1 : 0); }
    public void push_sample(String[] data, double timestamp) { inst.lsl_push_sample_strt(obj, data, timestamp); }
    public void push_sample(String[] data) { inst.lsl_push_sample_str(obj, data); }

    public void push_sample(long[] data, double timestamp, boolean pushthrough) { inst.lsl_push_sample_ltp(obj, data, timestamp, pushthrough ? 1 : 0); }
    public void push_sample(long[] data, double timestamp) { inst.lsl_push_sample_lt(obj, data, timestamp); }
    public void push_sample(long[] data) { inst.lsl_push_sample_l(obj, data); }

    // ===============================================================
    // === Pushing an chunk of multiplexed samples into the outlet ===
    // ===============================================================

    /**
     * Push a chunk of multiplexed samples into the outlet. Single timestamp provided.
     * @param data A rectangular array of values for multiple samples.
     * @param timestamp Optionally the capture time of the most recent sample, in agreement with local_clock(); if omitted, the current time is used.
     *                  The time stamps of other samples are automatically derived based on the sampling rate of the stream.
     * @param pushthrough Optionally whether to push the chunk through to the receivers instead of buffering it with subsequent samples.
     *                    Note that the chunk_size, if specified at outlet construction, takes precedence over the pushthrough flag.
     */
    public void push_chunk(float[] data, double timestamp, boolean pushthrough) { inst.lsl_push_chunk_ftp(obj, data, data.length, timestamp, pushthrough ? 1 : 0); }
    public void push_chunk(float[] data, double timestamp) { inst.lsl_push_chunk_ft(obj, data, data.length, timestamp); }
    public void push_chunk(float[] data) { inst.lsl_push_chunk_f(obj, data, data.length); }
    public void push_chunk(double[] data, double timestamp, boolean pushthrough) { inst.lsl_push_chunk_dtp(obj, data, data.length, timestamp, pushthrough ? 1 : 0); }
    public void push_chunk(double[] data, double timestamp) { inst.lsl_push_chunk_dt(obj, data, data.length, timestamp); }
    public void push_chunk(double[] data) { inst.lsl_push_chunk_d(obj, data, data.length); }
    public void push_chunk(int[] data, double timestamp, boolean pushthrough) { inst.lsl_push_chunk_itp(obj, data, data.length, timestamp, pushthrough ? 1 : 0); }
    public void push_chunk(int[] data, double timestamp) { inst.lsl_push_chunk_it(obj, data, data.length, timestamp); }
    public void push_chunk(int[] data) { inst.lsl_push_chunk_i(obj, data, data.length); }
    public void push_chunk(short[] data, double timestamp, boolean pushthrough) { inst.lsl_push_chunk_stp(obj, data, data.length, timestamp, pushthrough ? 1 : 0); }
    public void push_chunk(short[] data, double timestamp) { inst.lsl_push_chunk_st(obj, data, data.length, timestamp); }
    public void push_chunk(short[] data) { inst.lsl_push_chunk_s(obj, data, data.length); }
    public void push_chunk(byte[] data, double timestamp, boolean pushthrough) { inst.lsl_push_chunk_ctp(obj, data, data.length, timestamp, pushthrough ? 1 : 0); }
    public void push_chunk(byte[] data, double timestamp) { inst.lsl_push_chunk_ct(obj, data, data.length, timestamp); }
    public void push_chunk(byte[] data) { inst.lsl_push_chunk_c(obj, data, data.length); }
    public void push_chunk(String[] data, double timestamp, boolean pushthrough) { inst.lsl_push_chunk_strtp(obj, data, data.length, timestamp, pushthrough ? 1 : 0); }
    public void push_chunk(String[] data, double timestamp) { inst.lsl_push_chunk_strt(obj, data, data.length, timestamp); }
    public void push_chunk(String[] data) { inst.lsl_push_chunk_str(obj, data, data.length); }

    public void push_chunk(long[] data, double timestamp, boolean pushthrough) { inst.lsl_push_chunk_ltp(obj, data, data.length, timestamp, pushthrough ? 1 : 0); }
    public void push_chunk(long[] data, double timestamp) { inst.lsl_push_chunk_lt(obj, data, data.length, timestamp); }
    public void push_chunk(long[] data) { inst.lsl_push_chunk_l(obj, data, data.length); }
    
    /**
     * Push a chunk of multiplexed samples into the outlet. One timestamp per sample is provided.
     * @param data A rectangular array of values for multiple samples.
     * @param timestamps An array of timestamp values holding time stamps for each sample in the data buffer.
     * @param pushthrough Optionally whether to push the chunk through to the receivers instead of buffering it with subsequent samples.
     *                    Note that the chunk_size, if specified at outlet construction, takes precedence over the pushthrough flag.
     */
    public void push_chunk(float[] data, double[] timestamps, boolean pushthrough) { inst.lsl_push_chunk_ftnp(obj, data, data.length, timestamps, pushthrough ? 1 : 0); }
    public void push_chunk(float[] data, double[] timestamps) { inst.lsl_push_chunk_ftn(obj, data, data.length, timestamps); }
    public void push_chunk(double[] data, double[] timestamps, boolean pushthrough) { inst.lsl_push_chunk_dtnp(obj, data, data.length, timestamps, pushthrough ? 1 : 0); }
    public void push_chunk(double[] data, double[] timestamps) { inst.lsl_push_chunk_dtn(obj, data, data.length, timestamps); }
    public void push_chunk(int[] data, double[] timestamps, boolean pushthrough) { inst.lsl_push_chunk_itnp(obj, data, data.length, timestamps, pushthrough ? 1 : 0); }
    public void push_chunk(int[] data, double[] timestamps) { inst.lsl_push_chunk_itn(obj, data, data.length, timestamps); }
    public void push_chunk(short[] data, double[] timestamps, boolean pushthrough) { inst.lsl_push_chunk_stnp(obj, data, data.length, timestamps, pushthrough ? 1 : 0); }
    public void push_chunk(short[] data, double[] timestamps) { inst.lsl_push_chunk_stn(obj, data, data.length, timestamps); }
    public void push_chunk(byte[] data, double[] timestamps, boolean pushthrough) { inst.lsl_push_chunk_ctnp(obj, data, data.length, timestamps, pushthrough ? 1 : 0); }
    public void push_chunk(byte[] data, double[] timestamps) { inst.lsl_push_chunk_ctn(obj, data, data.length, timestamps); }
    public void push_chunk(String[] data, double[] timestamps, boolean pushthrough) { inst.lsl_push_chunk_strtnp(obj, data, data.length, timestamps, pushthrough ? 1 : 0); }
    public void push_chunk(String[] data, double[] timestamps) { inst.lsl_push_chunk_strtn(obj, data, data.length, timestamps); }


    // ===============================
    // === Miscellaneous Functions ===
    // ===============================

    /**
     * Check whether consumers are currently registered.
     * While it does not hurt, there is technically no reason to push samples if there is no consumer.
     */
    public boolean have_consumers() { return inst.lsl_have_consumers(obj)>0; }

    /**
     * Wait until some consumer shows up (without wasting resources).
     * @return True if the wait was successful, false if the timeout expired.
     */
    public boolean wait_for_consumers(double timeout) { return inst.lsl_wait_for_consumers(obj, timeout)>0; }

    /**
     * Retrieve the stream info provided by this outlet.
     * This is what was used to create the stream (and also has the Additional Network Information fields assigned).
     */
    public LSLStreamInfo info() { return new LSLStreamInfo(inst.lsl_get_info(obj)); }

    private Pointer obj;    
}

