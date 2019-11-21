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

package Sockets.Info;

import java.net.InetSocketAddress;

public class StreamSocketProblem 
{
	private InetSocketAddress socketAddress = null;
	private Exception exc = null;
	
	public StreamSocketProblem( InetSocketAddress address, Exception ex ) throws IllegalArgumentException 
	{
		this.socketAddress = address;
		this.exc = ex; 
	}
	
	public InetSocketAddress getSocketAddress()
	{
		return this.socketAddress;
	}
	
	public Exception getProblemCause()
	{
		return this.exc;
	}
	
	@Override
	public String toString() 
	{
		return "<"+socketAddress + "-Problem=" + this.exc + ">";
	}
}
