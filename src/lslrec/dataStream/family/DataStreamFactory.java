/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.  https://github.com/manmermon/LSLRecorder
 *
 *   LSLRec is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   LSLRec is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with LSLRec.  If not, see <http://www.gnu.org/licenses/>.
 *   
 * Based on StreamInlet class from Lab Streaming Layer project.
 * From:
 *  https://github.com/labstreaminglayer/liblsl-Java/blob/39799dae02edf34e138d2a67ae768dc38a0248a9/src/edu/ucsd/sccn/LSL.java
 */
package lslrec.dataStream.family;

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
