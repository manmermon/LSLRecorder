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

public class StreamInputMessage 
{
	private InetSocketAddress source = null;
	private InetSocketAddress dest = null;
	private String message = "";
	
	private double time = Double.NaN;
	
	public StreamInputMessage( String msg, InetSocketAddress origin, InetSocketAddress destination ) 
	{	
		if( origin == null && destination == null )
		{
			throw new IllegalArgumentException( "InetSocketAddress inputs null." );
		}
		
		this.time = System.nanoTime() / 1e9D;
		
		this.dest = destination;
		this.source = origin;
		this.message = msg;
	}
		
	public InetSocketAddress getOrigin()
	{
		return this.source;
	}
	
	public InetSocketAddress getDestination()
	{
		return this.dest;
	}
	
	public String getMessage( )
	{
		return this.message;
	}
	
	public double receivedTime()
	{
		return this.time;
	}
	
	@Override
	public String toString() 
	{	
		return "<" + this.message + ", [" + this.source +", " + this.dest + "]>";
	}

}
