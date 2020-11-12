/**
 * 
 */
package lslrec.dataStream.family;

import java.io.IOException;

import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.MutableStreamSetting;
import lslrec.dataStream.family.stream.IDataStream;
import lslrec.dataStream.family.stream.lsl.LSL;
import lslrec.dataStream.family.stream.lsl.LSLStreamInfo;

/**
 * @author Manuel Merino Monge
 *
 */
public class DataStreamFactory 
{
	/**
     * A very large time duration (> 1 year) for timeout values.
     * Note that significantly larger numbers can cause the timeout to be invalid on some operating systems (e.g., 32-bit UNIX).
     */
    public static final double TIME_FOREVER = 32000000.0;
	   
	 /**
     * Resolve all streams on the network.
     *
     * @param wait_time The waiting time for the operation, in seconds, to search for streams.
     *                  Warning: If this is too short (less than 0.5s) only a subset (or none) of the
     *                  outlets that are present on the network may be returned.
     * @return An array of stream info objects (excluding their desc field), any of which can
     *         subsequently be used to open an inlet. The full info can be retrieve from the inlet.
     */
	public static IStreamSetting[] createStreamSettings( IStreamSetting.StreamLibrary lib, double timeout )
	{
		IStreamSetting[] sst = null;
		
		switch ( lib )
		{
			case LSL:
			{		
				sst = LSL.resolve_streams();
				
				break;
			}
			default:
			{
				break;
			}
		}
		
		return sst;
	}	
	 
	public static IStreamSetting[] getStreamSettings( IStreamSetting.StreamLibrary lib )
	{
		return createStreamSettings( lib, TIME_FOREVER );
	}
	
	public static IDataStream createDataStream( IStreamSetting streamSetting ) throws Exception
	{
		IDataStream bds = null;
		
		if( streamSetting != null )
		{
			IStreamSetting stream = streamSetting;
			
			if( streamSetting instanceof MutableStreamSetting )
			{
				stream = ((MutableStreamSetting) streamSetting).getStreamSetting();
			}
			
			switch ( streamSetting.getLibraryID() )
			{
				case LSL:
				{
					bds = new LSL.StreamInlet( (LSLStreamInfo)stream
												, streamSetting.getStreamBufferLength()
												, streamSetting.getChunkSize()
												, streamSetting.recoverLostStream() );
					break;
				}
				default:
				{
					break;
				}
			}
		}
		
		return bds;
	}
}
