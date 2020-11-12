/**
 * 
 */
package lslrec.dataStream.family.stream.lsl;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

/**
 * @author Manuel Merino Monge
 *
 * From:
 *  https://github.com/labstreaminglayer/liblsl-Java/blob/39799dae02edf34e138d2a67ae768dc38a0248a9/src/edu/ucsd/sccn/LSL.java
 * 
 */

/**
 * Internal: C library interface.
 */
public interface LSLDll extends Library 
{
    int lsl_protocol_version();
    int lsl_library_version();
    String lsl_library_info();
    double lsl_local_clock();
    int lsl_resolve_all(Pointer[] buffer, int buffer_elements, double wait_time);
    int lsl_resolve_byprop(Pointer[] buffer, int buffer_elements, String prop, String value, int minimum, double wait_time);
    int lsl_resolve_bypred(Pointer[] buffer, int buffer_elements, String pred, int minimum, double wait_time);

    Pointer lsl_create_streaminfo(String name, String type, int channel_count, double nominal_srate, int channel_format, String source_id);
    void lsl_destroy_streaminfo(Pointer info);
    Pointer lsl_copy_streaminfo(Pointer info);

    String lsl_get_name(Pointer info);
    String lsl_get_type(Pointer info);
    int lsl_get_channel_count(Pointer info);
    double lsl_get_nominal_srate(Pointer info);
    int lsl_get_channel_format(Pointer info);
    String lsl_get_source_id(Pointer info);
    int lsl_get_version(Pointer info);
    double lsl_get_created_at(Pointer info);
    String lsl_get_uid(Pointer info);
    String lsl_get_session_id(Pointer info);
    String lsl_get_hostname(Pointer info);
    Pointer lsl_get_desc(Pointer info);
    String lsl_get_xml(Pointer info);
    int lsl_get_channel_bytes(Pointer info);
    int lsl_get_sample_bytes(Pointer info);
    Pointer lsl_streaminfo_from_xml(String xml);

    Pointer lsl_create_outlet(Pointer info, int chunk_size, int max_buffered);
    void lsl_destroy_outlet(Pointer obj);

    int lsl_push_sample_f(Pointer obj, float[] data);
    int lsl_push_sample_ft(Pointer out, float[] data, double timestamp);
    int lsl_push_sample_ftp(Pointer obj, float[] data, double timestamp, int pushthrough);

    int lsl_push_sample_d(Pointer obj, double[] data);
    int lsl_push_sample_dt(Pointer obj, double[] data, double timestamp);
    int lsl_push_sample_dtp(Pointer obj, double[] data, double timestamp, int pushthrough);

    int lsl_push_sample_i(Pointer obj, int[] data);
    int lsl_push_sample_it(Pointer obj, int[] data, double timestamp);
    int lsl_push_sample_itp(Pointer obj, int[] data, double timestamp, int pushthrough);

    int lsl_push_sample_s(Pointer obj, short[] data);
    int lsl_push_sample_st(Pointer obj, short[] data, double timestamp);
    int lsl_push_sample_stp(Pointer obj, short[] data, double timestamp, int pushthrough);

    int lsl_push_sample_c(Pointer obj, byte[] data);
    int lsl_push_sample_ct(Pointer obj, byte[] data, double timestamp);
    int lsl_push_sample_ctp(Pointer obj, byte[] data, double timestamp, int pushthrough);

    int lsl_push_sample_str(Pointer obj, String[] data);
    int lsl_push_sample_strt(Pointer obj, String[] data, double timestamp);
    int lsl_push_sample_strtp(Pointer obj, String[] data, double timestamp, int pushthrough);

    int lsl_push_sample_buf(Pointer obj, byte[][] data, int[] lengths);
    int lsl_push_sample_buft(Pointer obj, byte[][] data, int[] lengths, double timestamp);
    int lsl_push_sample_buftp(Pointer obj, byte[][] data, int[] lengths, double timestamp, int pushthrough);

    int lsl_push_sample_v(Pointer obj, Pointer data);
    int lsl_push_sample_vt(Pointer obj, Pointer data, double timestamp);
    int lsl_push_sample_vtp(Pointer obj, Pointer data, double timestamp, int pushthrough);

    int lsl_push_chunk_f(Pointer obj, float[] data, int data_elements);
    int lsl_push_chunk_ft(Pointer obj, float[] data, int data_elements, double timestamp);
    int lsl_push_chunk_ftp(Pointer obj, float[] data, int data_elements, double timestamp, int pushthrough);
    int lsl_push_chunk_ftn(Pointer obj, float[] data, int data_elements, double[] timestamps);
    int lsl_push_chunk_ftnp(Pointer obj, float[] data, int data_elements, double[] timestamps, int pushthrough);

    int lsl_push_chunk_d(Pointer obj, double[] data, int data_elements);
    int lsl_push_chunk_dt(Pointer obj, double[] data, int data_elements, double timestamp);
    int lsl_push_chunk_dtp(Pointer obj, double[] data, int data_elements, double timestamp, int pushthrough);
    int lsl_push_chunk_dtn(Pointer obj, double[] data, int data_elements, double[] timestamps);
    int lsl_push_chunk_dtnp(Pointer obj, double[] data, int data_elements, double[] timestamps, int pushthrough);

    int lsl_push_chunk_l(Pointer obj, long[] data, int data_elements);
    int lsl_push_chunk_lt(Pointer obj, long[] data, int data_elements, double timestamp);
    int lsl_push_chunk_ltp(Pointer obj, long[] data, int data_elements, double timestamp, int pushthrough);
    int lsl_push_chunk_ltn(Pointer obj, long[] data, int data_elements, double[] timestamps);
    int lsl_push_chunk_ltnp(Pointer obj, long[] data, int data_elements, double[] timestamps, int pushthrough);


    int lsl_push_chunk_i(Pointer obj, int[] data, int data_elements);
    int lsl_push_chunk_it(Pointer obj, int[] data, int data_elements, double timestamp);
    int lsl_push_chunk_itp(Pointer obj, int[] data, int data_elements, double timestamp, int pushthrough);
    int lsl_push_chunk_itn(Pointer obj, int[] data, int data_elements, double[] timestamps);
    int lsl_push_chunk_itnp(Pointer obj, int[] data, int data_elements, double[] timestamps, int pushthrough);

    int lsl_push_chunk_s(Pointer obj, short[] data, int data_elements);
    int lsl_push_chunk_st(Pointer obj, short[] data, int data_elements, double timestamp);
    int lsl_push_chunk_stp(Pointer obj, short[] data, int data_elements, double timestamp, int pushthrough);
    int lsl_push_chunk_stn(Pointer obj, short[] data, int data_elements, double[] timestamps);
    int lsl_push_chunk_stnp(Pointer obj, short[] data, int data_elements, double[] timestamps, int pushthrough);

    int lsl_push_chunk_c(Pointer obj, byte[] data, int data_elements);
    int lsl_push_chunk_ct(Pointer obj, byte[] data, int data_elements, double timestamp);
    int lsl_push_chunk_ctp(Pointer obj, byte[] data, int data_elements, double timestamp, int pushthrough);
    int lsl_push_chunk_ctn(Pointer obj, byte[] data, int data_elements, double[] timestamps);
    int lsl_push_chunk_ctnp(Pointer obj, byte[] data, int data_elements, double[] timestamps, int pushthrough);

    int lsl_push_chunk_str(Pointer obj, String[] data, int data_elements);
    int lsl_push_chunk_strt(Pointer obj, String[] data, int data_elements, double timestamp);
    int lsl_push_chunk_strtp(Pointer obj, String[] data, int data_elements, double timestamp, int pushthrough);
    int lsl_push_chunk_strtn(Pointer obj, String[] data, int data_elements, double[] timestamps);
    int lsl_push_chunk_strtnp(Pointer obj, String[] data, int data_elements, double[] timestamps, int pushthrough);

    int lsl_push_chunk_buf(Pointer obj, byte[][] data, int[] lengths, int data_elements);
    int lsl_push_chunk_buft(Pointer obj, byte[][] data, int[] lengths, int data_elements, double timestamp);
    int lsl_push_chunk_buftp(Pointer obj, byte[][] data, int[] lengths, int data_elements, double timestamp, int pushthrough);
    int lsl_push_chunk_buftn(Pointer obj, byte[][] data, int[] lengths, int data_elements, double[] timestamps);
    int lsl_push_chunk_buftnp(Pointer obj, byte[][] data, int[] lengths, int data_elements, double[] timestamps, int pushthrough);

    int lsl_have_consumers(Pointer obj);
    int lsl_wait_for_consumers(Pointer obj, double timeout);
    Pointer lsl_get_info(Pointer obj);

    Pointer lsl_create_inlet(Pointer info, int max_buflen, int max_chunklen, int recover);
    void lsl_destroy_inlet(Pointer obj);

    Pointer lsl_get_fullinfo(Pointer obj, double timeout, int[] ec);
    void lsl_open_stream(Pointer obj, double timeout, int[] ec);
    void lsl_close_stream(Pointer obj);
    double lsl_time_correction(Pointer obj, double timeout, int[] ec);
    double lsl_time_correction_ex(Pointer obj, double[] remote_time, double[] uncertainty, double timeout, int[] ec);
    int lsl_set_postprocessing(Pointer obj, int flags);

    double lsl_pull_sample_f(Pointer obj, float[] buffer, int buffer_elements, double timeout, int[] ec);
    double lsl_pull_sample_d(Pointer obj, double[] buffer, int buffer_elements, double timeout, int[] ec);
    double lsl_pull_sample_i(Pointer obj, int[] buffer, int buffer_elements, double timeout, int[] ec);
    double lsl_pull_sample_s(Pointer obj, short[] buffer, int buffer_elements, double timeout, int[] ec);
    double lsl_pull_sample_c(Pointer obj, byte[] buffer, int buffer_elements, double timeout, int[] ec);
    double lsl_pull_sample_str(Pointer obj, String[] buffer, int buffer_elements, double timeout, int[] ec);
    double lsl_pull_sample_buf(Pointer obj, byte[][] buffer, int[] buffer_lengths, int buffer_elements, double timeout, int[] ec);
    double lsl_pull_sample_v(Pointer obj, Pointer buffer, int buffer_bytes, double timeout, int[] ec);

    long lsl_pull_chunk_f(Pointer obj, float[] data_buffer, double[] timestamp_buffer, int data_buffer_elements, int timestamp_buffer_elements, double timeout, int[] ec);
    long lsl_pull_chunk_d(Pointer obj, double[] data_buffer, double[] timestamp_buffer, int data_buffer_elements, int timestamp_buffer_elements, double timeout, int[] ec);
    long lsl_pull_chunk_i(Pointer obj, int[] data_buffer, double[] timestamp_buffer, int data_buffer_elements, int timestamp_buffer_elements, double timeout, int[] ec);
    long lsl_pull_chunk_s(Pointer obj, short[] data_buffer, double[] timestamp_buffer, int data_buffer_elements, int timestamp_buffer_elements, double timeout, int[] ec);
    long lsl_pull_chunk_c(Pointer obj, byte[] data_buffer, double[] timestamp_buffer, int data_buffer_elements, int timestamp_buffer_elements, double timeout, int[] ec);
    long lsl_pull_chunk_str(Pointer obj, String[] data_buffer, double[] timestamp_buffer, int data_buffer_elements, int timestamp_buffer_elements, double timeout, int[] ec);
    long lsl_pull_chunk_buf(Pointer obj, byte[][] data_buffer, long[] lengths_buffer, double[] timestamp_buffer, int data_buffer_elements, int timestamp_buffer_elements, double timeout, int[] ec);

    int lsl_samples_available(Pointer obj);
    int lsl_was_clock_reset(Pointer obj);
    int lsl_smoothing_halftime(Pointer obj, float value);

    Pointer lsl_first_child(Pointer e);
    Pointer lsl_last_child(Pointer e);
    Pointer lsl_next_sibling(Pointer e);
    Pointer lsl_previous_sibling(Pointer e);
    Pointer lsl_parent(Pointer e);
    Pointer lsl_child(Pointer e, String name);
    Pointer lsl_next_sibling_n(Pointer e, String name);
    Pointer lsl_previous_sibling_n(Pointer e, String name);
    int lsl_empty(Pointer e);
    int lsl_is_text(Pointer e);
    String lsl_name(Pointer e);
    String lsl_value(Pointer e);
    String lsl_child_value(Pointer e);
    String lsl_child_value_n(Pointer e, String name);

    Pointer lsl_append_child_value(Pointer e, String name, String value);
    Pointer lsl_prepend_child_value(Pointer e, String name, String value);
    int lsl_set_child_value(Pointer e, String name, String value);
    int lsl_set_name(Pointer e, String rhs);
    int lsl_set_value(Pointer e, String rhs);
    Pointer lsl_append_child(Pointer e, String name);
    Pointer lsl_prepend_child(Pointer e, String name);
    Pointer lsl_append_copy(Pointer e, Pointer e2);
    Pointer lsl_prepend_copy(Pointer e, Pointer e2);
    void lsl_remove_child_n(Pointer e, String name);
    void lsl_remove_child(Pointer e, Pointer e2);

    Pointer lsl_create_continuous_resolver(double forget_after);
    Pointer lsl_create_continuous_resolver_byprop(String prop, String value, double forget_after);
    Pointer lsl_create_continuous_resolver_bypred(String pred, double forget_after);
    int lsl_resolver_results(Pointer obj, Pointer[] buffer, int buffer_elements);
    void lsl_destroy_continuous_resolver(Pointer obj);

}
