package edu.ucsd.sccn;

import java.nio.charset.Charset;

public class LSLUtils 
{
	public static int getDataTypeBytes( int type )
	{
		int len = -1;
		
		switch ( type ) 
		{
			case LSL.ChannelFormat.double64:
			{
				len = Double.BYTES;
				break;
			}
			case LSL.ChannelFormat.float32:
			{
				len = Float.BYTES;
				break;
			}
			case LSL.ChannelFormat.string:
			{
				//len = Character.BYTES;
				
				Charset c = Charset.forName( "UTF-8" );
				
				len = ( "A" ).getBytes( c ).length;
				break;
			}
			case LSL.ChannelFormat.int8:
			{
				len = Byte.BYTES;
				break;
			}
			case LSL.ChannelFormat.int16:
			{
				len = Short.BYTES;
				break;
			}
			case LSL.ChannelFormat.int32:
			{
				len = Integer.BYTES;
				break;
			}			
			case LSL.ChannelFormat.int64:
			{
				len = Long.BYTES;
				break;
			}
			default: //undefined
			{
				len = -1;
				
				break;
			}
		}
		
		return len;
	}	
}
