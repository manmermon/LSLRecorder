/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec and CLIS.
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

package sockets.info;

/**
 * 
 * @author Manuel Merino Monge
 *
 */
public class SocketParameters
{	
	public static final int SOCKET_CHANNEL_IN = 0;
	public static final int SOCKET_CHANNEL_OUT = 1;
	public static final int SOCKET_CHANNEL_INOUT = 2;
	
	private int In_Out = SOCKET_CHANNEL_INOUT;
	private SocketSetting socketInfo =  null;
	
	/**
	 * Set socket setting.
	 *  
	 * @param info 	-> Socket setting: protocol, IP, and port.
	 * @param INOUT -> stream direction: 
	 * 			0 - input streaming. 
	 * 			1 - output streaming.
	 * 			2 - bidirectional streaming.
	 *
	 * @exception IllegalArgumentException  -> SocketSetting parameter null.
	 */
	public SocketParameters( SocketSetting info, int INOUT ) throws IllegalArgumentException
	{
		if( info == null )
		{
			throw new IllegalArgumentException( "StreamSocketInfo null." );
		}
		this.socketInfo = info;
		this.setDirectionSocketConnetion( INOUT );
	}	
	
	/**
	 * 
	 * @return SocketSetting
	 */
	public SocketSetting getSocketInfo()
	{
		return this.socketInfo;
	}
	
	/**
	 * 
	 * @return Stream direction: 
	 * 			0 - input.
	 * 			1 - output.
	 * 			2 - bidirectional.
	 */
	public int getDirectionSocketConnection()
	{
		return this.In_Out;
	}
	
	/**
	 * Direction of socket connection.	 
	 * 
	 * @param in_out == 0 -> input socket: a read socket is created.
	 * 			in_out == 1 -> ouput socket: a write socket is create.
	 * 			in_out == otherwise -> bidirectional socket: a in-out socket (read and send messages) is created (default).
	 */
	public void setDirectionSocketConnetion( int in_out )
	{
		if( in_out != SOCKET_CHANNEL_IN
				&& in_out != SOCKET_CHANNEL_OUT )
		{
			this.In_Out = SOCKET_CHANNEL_INOUT;
		}		
		else
		{
			this.In_Out = in_out;
		}
	}
		
	/**
	 * True if both objects have the same protocol, ip address, port and strem direction.
	 */
	@Override
	public boolean equals( Object obj ) 
	{
		boolean equal = obj instanceof SocketParameters;
		
		if( equal )
		{
			SocketParameters pars = ( SocketParameters )obj;
			
			equal = this.In_Out == pars.getDirectionSocketConnection() && this.socketInfo.equals( pars.getSocketInfo() );
		}
		
		return equal;
	} 
}
