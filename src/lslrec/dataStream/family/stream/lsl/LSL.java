/*
 * From https://github.com/labstreaminglayer/liblsl-Java/blob/39799dae02edf34e138d2a67ae768dc38a0248a9/src/edu/ucsd/sccn/LSL.java
 */
package lslrec.dataStream.family.stream.lsl;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import com.sun.jna.Pointer;

import lslrec.config.ConfigApp;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.stream.INumberStream;
import lslrec.dataStream.family.stream.IStringtream;
import lslrec.exceptions.LostException;
import lslrec.exceptions.TimeoutException;

import java.io.IOException;


/**
 * Java API for the lab streaming layer.
 *
 * The lab streaming layer provides a set of functions to make instrument data accessible
 * in real time within a lab network. From there, streams can be picked up by recording programs,
 * viewing programs or custom experiment applications that access data streams in real time.
 *
 * The API covers two areas:
 * - The "push API" allows to create stream outlets and to push data (regular or irregular measurement
 *   time series, event data, coded audio/video frames, etc.) into them.
 * - The "pull API" allows to create stream inlets and read time-synched experiment data from them
 *   (for recording, viewing or experiment control).
 */
public class LSL 
{
    /**
     * Constant to indicate that a sample has the next successive time stamp.
     * This is an optional optimization to transmit less data per sample.
     * The stamp is then deduced from the preceding one according to the stream's sampling rate
     * (in the case of an irregular rate, the same time stamp as before will is assumed).
     */
    public static final double DEDUCED_TIMESTAMP = -1.0;

    /**
     * Constant to indicate that there is no preference about how a data stream shall be chunked for transmission.
     * (can be used for the chunking paramters in the inlet or the outlet).
     */
    public static final int NO_PREFERENCE = 0;
  
    /**
     * Post-processing options for stream inlets.
     */
    public class ProcessingOptions 
    {
        public static final int proc_none = 0;			/* No automatic post-processing; return the ground-truth time stamps for manual post-processing */
        /* (this is the default behavior of the inlet). */
        public static final int proc_clocksync = 1;		/* Perform automatic clock synchronization; equivalent to manually adding the time_correction() value */
        /* to the received time stamps. */
        public static final int proc_dejitter = 2;		/* Remove jitter from time stamps. This will apply a smoothing algorithm to the received time stamps; */
        /* the smoothing needs to see a minimum number of samples (30-120 seconds worst-case) until the remaining */
								/* jitter is consistently below 1ms. */
        public static final int proc_monotonize = 4;	/* Force the time-stamps to be monotonically ascending (only makes sense if timestamps are dejittered). */
        public static final int proc_threadsafe = 8;    /* Post-processing is thread-safe (same inlet can be read from by multiple threads); uses somewhat more CPU. */
        public static final int proc_ALL = 1|2|4|8;		/* The combination of all possible post-processing options. */
    }

    /**
     * Possible error codes.
     */
    public class ErrorCode {
        public static final int no_error = 0;           /* No error occurred */
        public static final int timeout_error = -1;     /* The operation failed due to a timeout. */
        public static final int lost_error = -2;        /* The stream has been lost. */
        public static final int argument_error = -3;    /* An argument was incorrectly specified (e.g., wrong format or wrong length). */
        public static final int internal_error = -4;     /* Some other internal error has happened. */
    }


    /**
     * Protocol version.
     * The major version is protocol_version() / 100;
     * The minor version is protocol_version() % 100;
     * Clients with different minor versions are protocol-compatible with each other
     * while clients with different major versions will refuse to work together.
     */
    public static int protocol_version() { return inst.lsl_protocol_version(); }

    /**
     * Version of the liblsl library.
     * The major version is library_version() / 100;
     * The minor version is library_version() % 100;
     */
    public static int library_version() { return inst.lsl_library_version(); }
    public static String library_info() { return inst.lsl_library_info(); }

    /**
     * Obtain a local system time stamp in seconds. The resolution is better than a millisecond.
     * This reading can be used to assign time stamps to samples as they are being acquired.
     * If the "age" of a sample is known at a particular time (e.g., from USB transmission
     * delays), it can be used as an offset to local_clock() to obtain a better estimate of
     * when a sample was actually captured. See stream_outlet::push_sample() for a use case.
     */
    public static double local_clock() { return inst.lsl_local_clock(); }



    // ===========================
    // ==== Resolve Functions ====
    // ===========================

    /**
     * Resolve all streams on the network.
     * This function returns all currently available streams from any outlet on the network.
     * The network is usually the subnet specified at the local router, but may also include
     * a multicast group of machines (given that the network supports it), or list of hostnames.
     * These details may optionally be customized by the experimenter in a configuration file
     * (see Network Connectivity in the LSL wiki).
     * This is the default mechanism used by the browsing programs and the recording program.
     * @param wait_time The waiting time for the operation, in seconds, to search for streams.
     *                  Warning: If this is too short (less than 0.5s) only a subset (or none) of the
     *                  outlets that are present on the network may be returned.
     * @return An array of stream info objects (excluding their desc field), any of which can
     *         subsequently be used to open an inlet. The full info can be retrieve from the inlet.
     */
    public static IStreamSetting[] resolve_streams(double wait_time)
    {
        Pointer[] buf = new Pointer[1024]; int num = inst.lsl_resolve_all(buf, buf.length, wait_time);
        IStreamSetting[] res = new IStreamSetting[num];
        for (int k = 0; k < num; k++)
        {
        	res[k] = new LSLStreamInfo(buf[k]);
        }
        
        return res;
    }
    public static IStreamSetting[] resolve_streams() { return resolve_streams(1.0); }

    /**
     * Resolve all streams with a specific value for a given property.
     * If the goal is to resolve a specific stream, this method is preferred over resolving all streams and then selecting the desired one.
     * @param prop The stream_info property that should have a specific value (e.g., "name", "type", "source_id", or "desc/manufaturer").
     * @param value The String value that the property should have (e.g., "EEG" as the type property).
     * @param minimum Optionally return at least this number of streams.
     * @param timeout Optionally a timeout of the operation, in seconds (default: no timeout).
     *                If the timeout expires, less than the desired number of streams (possibly none) will be returned.
     * @return An array of matching stream info objects (excluding their meta-data), any of
     *         which can subsequently be used to open an inlet.
     */
    public static LSLStreamInfo[] resolve_stream(String prop, String value, int minimum, double timeout)
    {
        Pointer[] buf = new Pointer[1024]; int num = inst.lsl_resolve_byprop(buf, buf.length, prop, value, minimum, timeout);
        LSLStreamInfo[] res = new LSLStreamInfo[num];
        for (int k = 0; k < num; k++)
            res[k] = new LSLStreamInfo(buf[k]);
        return res;
    }
    public static LSLStreamInfo[] resolve_stream(String prop, String value, int minimum) { return resolve_stream(prop, value, minimum, INumberStream.TIME_FOREVER); }
    public static LSLStreamInfo[] resolve_stream(String prop, String value) { return resolve_stream(prop, value, 1, INumberStream.TIME_FOREVER); }

    /**
     * Resolve all streams that match a given predicate.
     * Advanced query that allows to impose more conditions on the retrieved streams; the given String is an XPath 1.0
     * predicate for the <info> node (omitting the surrounding []'s), see also http://en.wikipedia.org/w/index.php?title=XPath_1.0&oldid=474981951.
     * @param pred The predicate String, e.g. "name='BioSemi'" or "type='EEG' and starts-with(name,'BioSemi') and count(info/desc/channel)=32"
     * @param minimum Return at least this number of streams.
     * @param timeout Optionally a timeout of the operation, in seconds (default: no timeout).
     *                If the timeout expires, less than the desired number of streams (possibly none) will be returned.
     * @return An array of matching stream info objects (excluding their meta-data), any of
     *         which can subsequently be used to open an inlet.
     */
    public static LSLStreamInfo[] resolve_stream(String pred, int minimum, double timeout)
    {
        Pointer[] buf = new Pointer[1024]; int num = inst.lsl_resolve_bypred(buf, buf.length, pred, minimum, timeout);
        LSLStreamInfo[] res = new LSLStreamInfo[num];
        for (int k = 0; k < num; k++)
            res[k] = new LSLStreamInfo(buf[k]);
        return res;
    }
    public static LSLStreamInfo[] resolve_stream(String pred, int minimum) { return resolve_stream(pred, minimum, INumberStream.TIME_FOREVER); }
    public static LSLStreamInfo[] resolve_stream(String pred) { return resolve_stream(pred, 1, INumberStream.TIME_FOREVER); }


    // ======================
    // ==== Stream Inlet ====
    // ======================

    /**
     * A stream inlet.
     * Inlets are used to receive streaming data (and meta-data) from the lab network.
     */
    public static class StreamInlet implements INumberStream, IStringtream
    {
        /**
         * Construct a new stream inlet from a resolved stream info.
         * @param info A resolved stream info object (as coming from one of the resolver functions).
         *             Note: the stream_inlet may also be constructed with a fully-specified stream_info,
         *                   if the desired channel format and count is already known up-front, but this is
         *                   strongly discouraged and should only ever be done if there is no time to resolve the
         *                   stream up-front (e.g., due to limitations in the client program).
         * @param max_buflen Optionally the maximum amount of data to buffer (in seconds if there is a nominal
         *                   sampling rate, otherwise x100 in samples). Recording applications want to use a fairly
         *                   large buffer size here, while real-time applications would only buffer as much as
         *                   they need to perform their next calculation.
         * @param max_chunklen Optionally the maximum size, in samples, at which chunks are transmitted
         *                     (the default corresponds to the chunk sizes used by the sender).
         *                     Recording applications can use a generous size here (leaving it to the network how
         *                     to pack things), while real-time applications may want a finer (perhaps 1-sample) granularity.
         *                     If left unspecified (=0), the sender determines the chunk granularity.
         * @param recover Try to silently recover lost streams that are recoverable (=those that that have a source_id set).
         *                In all other cases (recover is false or the stream is not recoverable) functions may throw a
         *                LostException if the stream's source is lost (e.g., due to an app or computer crash).
         */
        public StreamInlet(LSLStreamInfo info, int max_buflen, int max_chunklen, boolean recover) throws IOException { obj = inst.lsl_create_inlet(info.handle(), max_buflen, max_chunklen, recover?1:0); if(obj == null) throw new IOException("Unable to open LSL inlet.");}
        public StreamInlet(LSLStreamInfo info, int max_buflen, int max_chunklen) throws IOException { obj = inst.lsl_create_inlet(info.handle(), max_buflen, max_chunklen, 1);if(obj == null) throw new IOException("Unable to open LSL inlet."); }
        public StreamInlet(LSLStreamInfo info, int max_buflen) throws IOException { obj = inst.lsl_create_inlet(info.handle(), max_buflen, 0, 1); if(obj == null) throw new IOException("Unable to open LSL inlet.");}
        public StreamInlet(LSLStreamInfo info) throws IOException { obj = inst.lsl_create_inlet(info.handle(), 360, 0, 1); if(obj == null) throw new IOException("Unable to open LSL inlet.");}

        /**
         * Disconnect and close the inlet.
         */
        public void close() { inst.lsl_destroy_inlet(obj); }

        /**
         * Retrieve the complete information of the given stream, including the extended description.
         * Can be invoked at any time of the stream's lifetime.
         * @param timeout Timeout of the operation (default: no timeout).
         * @throws TimeoutException (if the timeout expires), or LostException (if the stream source has been lost).
         */
        public IStreamSetting info( double timeout ) throws Exception 
        { 
        	int[] ec={0}; Pointer res = inst.lsl_get_fullinfo(obj, timeout, ec); check_error(ec); 
        
        	LSLStreamInfo info = new LSLStreamInfo( res );
        	        	
        	return  info;
        }
        
        public IStreamSetting info() throws Exception 
        { 
        	return info( INumberStream.TIME_FOREVER ); 
        }
        

        /**
         * Subscribe to the data stream.
         * All samples pushed in at the other end from this moment onwards will be queued and
         * eventually be delivered in response to pull_sample() or pull_chunk() calls.
         * Pulling a sample without some preceding open_stream is permitted (the stream will then be opened implicitly).
         * @param timeout Optional timeout of the operation (default: no timeout).
         * @throws TimeoutException (if the timeout expires), or LostException (if the stream source has been lost).
         */
        public void open_stream(double timeout) throws Exception { int[] ec = {0}; inst.lsl_open_stream(obj, timeout, ec); check_error(ec); }
        public void open_stream() throws Exception { open_stream( INumberStream.TIME_FOREVER ); }

        /**
         * Drop the current data stream.
         * All samples that are still buffered or in flight will be dropped and transmission
         * and buffering of data for this inlet will be stopped. If an application stops being
         * interested in data from a source (temporarily or not) but keeps the outlet alive,
         * it should call close_stream() to not waste unnecessary system and network
         * resources.
         */
        public void close_stream() { inst.lsl_close_stream(obj); }

        /**
         * Retrieve an estimated time correction offset for the given stream.
         * The first call to this function takes several milliseconds until a reliable first estimate is obtained.
         * Subsequent calls are instantaneous (and rely on periodic background updates).
         * The precision of these estimates should be below 1 ms (empirically within +/-0.2 ms).
         * @timeout Timeout to acquire the first time-correction estimate (default: no timeout).
         * @return The time correction estimate. This is the number that needs to be added to a time stamp
         *         that was remotely generated via lsl_local_clock() to map it into the local clock domain of this machine.
         * @throws TimeoutException (if the timeout expires), or LostException (if the stream source has been lost).
         */
        public double time_correction(double timeout) throws Exception { int[] ec = {0}; double res = inst.lsl_time_correction(obj, timeout, ec); check_error(ec); return res; }
        public double time_correction() throws Exception { return time_correction( INumberStream.TIME_FOREVER  ); }

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
         * @throws LostException (if the stream source has been lost).
         */
        public double pull_sample(float[] sample, double timeout) throws Exception { int[] ec = {0}; double res = inst.lsl_pull_sample_f(obj, sample, sample.length, timeout, ec); check_error(ec); return res; }
        public double pull_sample(float[] sample) throws Exception { return pull_sample(sample, INumberStream.TIME_FOREVER );  }
        public double pull_sample(double[] sample, double timeout) throws Exception { int[] ec = {0}; double res = inst.lsl_pull_sample_d(obj, sample, sample.length, timeout, ec); check_error(ec); return res; }
        public double pull_sample(double[] sample) throws Exception { return pull_sample(sample, INumberStream.TIME_FOREVER ); }
        public double pull_sample(int[] sample, double timeout) throws Exception { int[] ec = {0}; double res = inst.lsl_pull_sample_i(obj, sample, sample.length, timeout, ec); check_error(ec); return res; }
        public double pull_sample(int[] sample) throws Exception { return pull_sample(sample, INumberStream.TIME_FOREVER ); }
        public double pull_sample(short[] sample, double timeout) throws Exception { int[] ec = {0}; double res = inst.lsl_pull_sample_s(obj, sample, sample.length, timeout, ec); check_error(ec); return res; }
        public double pull_sample(short[] sample) throws Exception { return pull_sample(sample, INumberStream.TIME_FOREVER ); }
        public double pull_sample(byte[] sample, double timeout) throws Exception { int[] ec = {0}; double res = inst.lsl_pull_sample_c(obj, sample, sample.length, timeout, ec); check_error(ec); return res; }
        public double pull_sample(byte[] sample) throws Exception { return pull_sample(sample, INumberStream.TIME_FOREVER ); }
        public double pull_sample(String[] sample, double timeout) throws Exception { int[] ec = {0}; double res = inst.lsl_pull_sample_str(obj, sample, sample.length, timeout, ec); check_error(ec); return res; }
        public double pull_sample(String[] sample) throws Exception { return pull_sample(sample, INumberStream.TIME_FOREVER ); }



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
         * @throws LostException (if the stream source has been lost).
         */
        public int pull_chunk(float[] data_buffer, double[] timestamp_buffer, double timeout) throws Exception { int[] ec = {0}; long res = inst.lsl_pull_chunk_f(obj, data_buffer, timestamp_buffer, data_buffer.length, timestamp_buffer.length, timeout, ec); check_error(ec); return (int)res; }
        public int pull_chunk(float[] data_buffer, double[] timestamp_buffer) throws Exception { return pull_chunk(data_buffer, timestamp_buffer, 0.0); }
        public int pull_chunk(double[] data_buffer, double[] timestamp_buffer, double timeout) throws Exception { int[] ec = {0}; long res = inst.lsl_pull_chunk_d(obj, data_buffer, timestamp_buffer, data_buffer.length, timestamp_buffer.length, timeout, ec); check_error(ec); return (int)res; }
        public int pull_chunk(double[] data_buffer, double[] timestamp_buffer) throws Exception { return pull_chunk(data_buffer, timestamp_buffer, 0.0); }
        public int pull_chunk(short[] data_buffer, double[] timestamp_buffer, double timeout) throws Exception { int[] ec = {0}; long res = inst.lsl_pull_chunk_s(obj, data_buffer, timestamp_buffer, data_buffer.length, timestamp_buffer.length, timeout, ec); check_error(ec); return (int)res; }
        public int pull_chunk(short[] data_buffer, double[] timestamp_buffer) throws Exception { return pull_chunk(data_buffer, timestamp_buffer, 0.0); }
        public int pull_chunk(byte[] data_buffer, double[] timestamp_buffer, double timeout) throws Exception { int[] ec = {0}; long res = inst.lsl_pull_chunk_c(obj, data_buffer, timestamp_buffer, data_buffer.length, timestamp_buffer.length, timeout, ec); check_error(ec); return (int)res; }
        public int pull_chunk(byte[] data_buffer, double[] timestamp_buffer) throws Exception { return pull_chunk(data_buffer, timestamp_buffer, 0.0); }
        public int pull_chunk(int[] data_buffer, double[] timestamp_buffer, double timeout) throws Exception { int[] ec = {0}; long res = inst.lsl_pull_chunk_i(obj, data_buffer, timestamp_buffer, data_buffer.length, timestamp_buffer.length, timeout, ec); check_error(ec); return (int)res; }
        public int pull_chunk(int[] data_buffer, double[] timestamp_buffer) throws Exception { return pull_chunk(data_buffer, timestamp_buffer, 0.0); }
        public int pull_chunk(String[] data_buffer, double[] timestamp_buffer, double timeout) throws Exception { int[] ec = {0}; long res = inst.lsl_pull_chunk_str(obj, data_buffer, timestamp_buffer, data_buffer.length, timestamp_buffer.length, timeout, ec); check_error(ec); return (int)res; }
        public int pull_chunk(String[] data_buffer, double[] timestamp_buffer) throws Exception { return pull_chunk(data_buffer, timestamp_buffer, 0.0); }

        /**
         * Query whether samples are currently available for immediate pickup.
         * Note that it is not a good idea to use samples_available() to determine whether
         * a pull_*() call would block: to be sure, set the pull timeout to 0.0 or an acceptably
         * low value. If the underlying implementation supports it, the value will be the number of
         * samples available (otherwise it will be 1 or 0).
         */
        public int samples_available() { return (int)inst.lsl_samples_available(obj); }

        /**
         * Query whether the clock was potentially reset since the last call to was_clock_reset().
         * This is a rarely-used function that is only useful to applications that combine multiple time_correction
         * values to estimate precise clock drift; it allows to tolerate cases where the source machine was
         * hot-swapped or restarted in between two measurements.
         */
        public boolean was_clock_reset() { return (int)inst.lsl_was_clock_reset(obj)!=0; }

        private Pointer obj;
    }


    
    // ===========================
    // === Continuous Resolver ===
    // ===========================

    /**
     * A convenience class that resolves streams continuously in the background throughout
     * its lifetime and which can be queried at any time for the set of streams that are currently
     * visible on the network.
     */
    public static class ContinuousResolver {
        /**
         * Construct a new continuous_resolver that resolves all streams on the network.
         * This is analogous to the functionality offered by the free function resolve_streams().
         * @param forget_after When a stream is no longer visible on the network (e.g., because it was shut down),
         *                     this is the time in seconds after which it is no longer reported by the resolver.
         */
        public ContinuousResolver(double forget_after) { obj = inst.lsl_create_continuous_resolver(forget_after); }
        public ContinuousResolver() { obj = inst.lsl_create_continuous_resolver(5.0); }

        /**
         * Construct a new continuous_resolver that resolves all streams with a specific value for a given property.
         * This is analogous to the functionality provided by the free function resolve_stream(prop,value).
         * @param prop The stream_info property that should have a specific value (e.g., "name", "type", "source_id", or "desc/manufaturer").
         * @param value The String value that the property should have (e.g., "EEG" as the type property).
         * @param forget_after When a stream is no longer visible on the network (e.g., because it was shut down),
         *                     this is the time in seconds after which it is no longer reported by the resolver.
         */
        public ContinuousResolver(String prop, String value, double forget_after) { obj = inst.lsl_create_continuous_resolver_byprop(prop, value, forget_after); }
        public ContinuousResolver(String prop, String value) { obj = inst.lsl_create_continuous_resolver_byprop(prop, value, 5.0); }

        /**
         * Construct a new continuous_resolver that resolves all streams that match a given XPath 1.0 predicate.
         * This is analogous to the functionality provided by the free function resolve_stream(pred).
         * @param pred The predicate String, e.g. "name='BioSemi'" or "type='EEG' and starts-with(name,'BioSemi') and count(info/desc/channel)=32"
         * @param forget_after When a stream is no longer visible on the network (e.g., because it was shut down),
         *                     this is the time in seconds after which it is no longer reported by the resolver.
         */
        public ContinuousResolver(String pred, double forget_after) { obj = inst.lsl_create_continuous_resolver_bypred(pred, forget_after); }
        public ContinuousResolver(String pred) { obj = inst.lsl_create_continuous_resolver_bypred(pred, 5.0); }

        /**
         * Close the resolver and stop sending queries.
         * It is recommended to close a resolver once not needed any more to avoid spamming
         * the network with resolve queries.
         */
        void close() { inst.lsl_destroy_continuous_resolver(obj); }

        /**
         * Obtain the set of currently present streams on the network (i.e. resolve result).
         * @return An array of matching stream info objects (excluding their meta-data), any of
         *         which can subsequently be used to open an inlet.
         */
        public LSLStreamInfo[] results() {
            Pointer[] buf = new Pointer[1024];
            int num = inst.lsl_resolver_results(obj,buf,buf.length);
            LSLStreamInfo[] res = new LSLStreamInfo[num];
            for (int k = 0; k < num; k++)
                res[k] = new LSLStreamInfo(buf[k]);
            return res;
        }

        private Pointer obj; // the underlying native handle
    }


    // =======================
    // === Exception Types ===
    // =======================
    /**
     * Exception class that indicates that an invalid argument has been passed.
     */
    public static class ArgumentException extends Exception {
        public ArgumentException(String message) { super(message); }
    }

    /**
     * Exception class that indicates that an internal error has occurred inside liblsl.
     */
    public static class InternalException extends Exception {
        public InternalException(String message) { super(message); }
    }

    /**
     * Check an error condition and throw an exception if appropriate.
     */
    static void check_error(int[] ec) throws Exception {
        if (ec[0] < 0)
            switch (ec[0]) {
                case -1: throw new TimeoutException("The operation failed due to a timeout.");
                case -2: throw new LostException("The stream has been lost.");
                case -3: throw new ArgumentException("An argument was incorrectly specified (e.g., wrong format or wrong length).");
                case -4: throw new InternalException("An internal internal error has occurred.");
                default: throw new Exception("An unknown error has occurred.");
            }
    }


    public static LSLDll getDllInstance()
    {
    	return inst;
    }

    private static LSLDll inst;
    static {
        System.setProperty("jna.debug_load", "true");
        System.setProperty("jna.debug_load.jna", "true");
        
        String path = ConfigApp.SYSTEM_LIB_PATH;
        
        switch ( Platform.getOSType() ) 
        {
            case Platform.WINDOWS:
            {
            	path += ( Platform.is64Bit() ? "liblsl64.dll" : "liblsl32.dll" );
                break;
            }
            case Platform.MAC:
            {
                path += ( Platform.is64Bit() ? "liblsl64.dylib" : "liblsl32.dylib" );
                break;
            }
            case Platform.ANDROID:
            {
                // For JNA <= 5.1.0
                System.setProperty("jna.nosys", "false");
                path += "lsl";                
                break;
            }
            default:
            {
                path += ( Platform.is64Bit() ? "liblsl64.so" : "liblsl32.so" );                
                break;
            }
        }
        
        inst = (LSLDll)Native.loadLibrary( path, LSLDll.class);        
        if (inst == null)
        {
            inst = (LSLDll)Native.loadLibrary( ConfigApp.SYSTEM_LIB_PATH  + "liblsl.so", LSLDll.class );
        }
    }
}
