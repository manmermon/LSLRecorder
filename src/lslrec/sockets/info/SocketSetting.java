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

package lslrec.sockets.info;

import java.net.InetSocketAddress;

/**
 * 
 * @author Manuel Merino Monge
 *
 */
public class SocketSetting implements Comparable< SocketSetting >
{	
	public static final int TCP_PROTOCOL = 0;
	public static final int UDP_PROTOCOL = 1;
	
	private InetSocketAddress socketAddress;
	private int protocolType = TCP_PROTOCOL;
	
	/**
	 * Set socket information.
	 * 
	 * @param protocol_type	-> protocol type: 0 - TCP, 1 - UDP
	 * @param ipAddress		-> IP address.
	 * @param port			-> application port.
	 * 
	 * @exception IllegalArgumentException -> unsupported protocol, IP format incorrect, or port outbound range.  
	 */
	public SocketSetting( int protocol_type, String ipAddress, int port ) throws IllegalArgumentException
	{
		this.setProtocolType( protocol_type );
		this.setSocketAddress( ipAddress, port );
	}		
	
	/**
	 * 
	 * @return protocol type: 0 - TCP, 1 - UDP.
	 */
	public int getProtocolType()
	{
		return this.protocolType;
	}
	
	/**
	 * Set protocol.
	 * 
	 * @param protocol_Type	-> 0 - TCP, 1 - UDP. 
	 * @throws IllegalArgumentException -> unsupported protocol.
	 */
	public void setProtocolType( int protocol_Type ) throws IllegalArgumentException
	{
		if( protocol_Type != TCP_PROTOCOL 
				&& protocol_Type != UDP_PROTOCOL )
		{
			throw new IllegalArgumentException( "Unsupported protocol." );
		}
		
		this.protocolType = protocol_Type;
	}
	
	/**
	 * 
	 * @return Socket information (IP-Port)
	 */
	public InetSocketAddress getSocketAddress( )
	{
		return this.socketAddress;
	}
	
	/**
	 * Set socket IP-Port.
	 * 
	 * @param ipAddress -> IP address.
	 * @param port		-> Port.
	 * 
	 * @throws IllegalArgumentException -> if the port parameter is outside the range of valid port values, or if the hostname parameter is null.
	 * @throws SecurityException -> if a security manager is present and permission to resolve the host name is denied.
	 *  
	 */
	public void setSocketAddress( String ipAddress, int port ) throws IllegalArgumentException, SecurityException
	{
		this.socketAddress = new InetSocketAddress( ipAddress, port );		
	}	
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{			
		return getSocketString( this.protocolType, this.socketAddress.getAddress().getHostAddress(), this.socketAddress.getPort() );
	}
	
	/**
	 * Returns a string representation of socket from input parameters.
	 * 
	 * @param protocol_Type -> protocol type: 0 - TCP; otherwise - UDP.
	 * @param ip			-> IP address.
	 * @param port			-> port.
	 * 
	 * @return string representation of socket from input parameters.
	 */
	public static String getSocketString( int protocol_Type, String ip, int port )
	{
		String str = "TCP";
		if( protocol_Type == UDP_PROTOCOL )
		{
			str = "UDP";
		}
		
		return str + ":" + ip + ":" + port;
	}
	
	/**
	 * True if both objects have the same protocol, ip address, and port.
	 */
	@Override
	public boolean equals( Object obj ) 
	{
		boolean equal = obj instanceof SocketSetting;
		
		if( equal )
		{
			SocketSetting pars = ( SocketSetting )obj;
			
			equal = this.socketAddress.getPort() == pars.getSocketAddress().getPort()
					&& this.protocolType == pars.getProtocolType() 
					&& this.socketAddress.getAddress().getHostAddress().equals( pars.getSocketAddress().getAddress().getHostAddress() );
		}
		
		return equal;
	} 
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() 
	{
		int hash = this.socketAddress.getAddress().getHostAddress().hashCode() + 
					this.socketAddress.getPort() + this.protocolType;
        return hash;
	}

	/**
	 * 
	 * @return the value 0 if the input parameter is equal to the StreamSocketInfo; 
	 * 			a value less than 0 if input is UDP, inpyt IP address is lexicographically less than the string argument, 
	 * 			and input port is less than the StreamSocketInfo; 
	 * 			and a value greater than 0 if input protocol is TCP, input IP address is lexicographically greater than the string argument,
	 * 			and input port is greater than the StreamSocketInfo. 
	 */
	@Override
	public int compareTo( SocketSetting o ) 
	{
		int p = o.getProtocolType() - this.protocolType;
		
		if( p == 0 )
		{
			p = this.socketAddress.getAddress().getHostAddress().compareTo( o.getSocketAddress().getAddress().getHostAddress() );
			
			if( p == 0 )
			{
				p = o.getSocketAddress().getPort() - this.socketAddress.getPort();
			}
		}
		
		return p;	
	}
}
