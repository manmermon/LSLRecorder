/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.
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
 */
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

	public static int getTimeMarkBytes( )
	{
		return getDataTypeBytes( getTimeMarkType() );
	}
	
	public static int getTimeMarkType( )
	{
		return LSL.ChannelFormat.double64;
	}
}
