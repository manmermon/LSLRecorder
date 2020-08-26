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

package prototype.discarded.malfunction.socket;

import java.util.ArrayList;
import java.util.List;

import sockets.Info.SocketSetting;

public class streamingOutputMessage 
{
	private SocketSetting source = null;
	private List< SocketSetting > destination = new ArrayList< SocketSetting >();
	private String message = "";
	
	public streamingOutputMessage( String msg, SocketSetting origin, SocketSetting destination ) 
	{
		this.setOrigin( origin );
		
		this.addDestination( destination );
		
		this.message = msg;
	}
	
	public streamingOutputMessage( String msg, SocketSetting origin, List< SocketSetting > destination ) 
	{
		this.source = origin;
		
		this.addDestination( destination );
		
		this.message = msg;
	}
	
	public void addDestination( SocketSetting d )
	{
		if( d == null )
		{
			throw new IllegalArgumentException( "Streaming paramters null." );
		}
		
		this.destination.add( d );
	}
	
	public void addDestination( List< SocketSetting > d )
	{
		if( d == null )
		{
			throw new IllegalArgumentException( "Streaming paramters null." );
		}
		
		this.destination.addAll( d );
	}
	
	public List< SocketSetting > getDestinations()
	{
		return this.destination;
	}
	
	public void setDestination( int pos, SocketSetting d )
	{
		this.destination.set( pos, d );
	}
	
	public SocketSetting getOrigin()
	{
		return this.source;
	}
	
	public void setOrigin( SocketSetting origin )
	{
		if( origin == null )
		{
			throw new IllegalArgumentException( "Origin streamSocketInfo null." );
		}
		
		this.source = origin;
	}
	
	public void clearDestination()
	{
		this.destination.clear();
	}

	public String getMessage( )
	{
		return this.message;
	}
	
	public void setMessage( String msg )
	{
		this.message = msg;
	}
	
	@Override
	public String toString() 
	{	
		return "<" + this.message + ", [" + this.source + ", "+ this.destination +"]>";
	}
}
