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

package Prototype.Discarded.Malfunction.Socket.Manager;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

import Controls.Messages.EventInfo;
import Controls.Messages.eventType;
import Sockets.Info.streamInputMessage;

public class readSocketThread extends TemplateReadWriteSocketThread
{	
	private boolean waitingData = true;		

	private CharsetDecoder decorder;
	
	public readSocketThread( AbstractSelectableChannel channel ) throws Exception 
	{
		super( channel );
		
		this.decorder = Charset.forName( "UTF-8" ).newDecoder();
	}

	@Override
	protected boolean checkIsWait() 
	{
		return this.waitingData;
	}

	@Override
	protected void runInLoop() throws Exception 
	{
		int bufferSize = 65507; 
		ByteBuffer buf = ByteBuffer.allocate( bufferSize );
				
		String msg = "";
		int nbytesRead = -1;
		
		SocketAddress client = null;
		
		this.waitingData = true;
		
		if( super.socket instanceof SocketChannel )
		{
			nbytesRead  = ( ( SocketChannel )super.socket).read( buf );
			client = ((SocketChannel) super.socket).getRemoteAddress();
		}
		else
		{
			client = ( ( DatagramChannel )super.socket).receive( buf );
		}		
				
		this.waitingData = false;
				
		buf.flip();
		if( client != null )
		{	
			nbytesRead = buf.limit();
		}
		
		System.out.println("readSocketThread.runInLoop() " + nbytesRead );
		
		if( nbytesRead > 0 )
		{	
			//msg += new String( buf.array(), 0, nbytesRead );//buf.limit() );
			msg += decorder.decode( buf ).toString();
			
			while( nbytesRead == bufferSize 
					&& !(super.socket instanceof DatagramChannel) )
			{				
				buf.clear();
				if( super.socket instanceof SocketChannel )
				{
					nbytesRead = ( ( SocketChannel )super.socket).read( buf );
				}
				else
				{
					nbytesRead = ( ( DatagramChannel )super.socket).read( buf );
				}
				
				if( nbytesRead > 0 )
				{
					buf.flip();
					//msg += new String( buf.array(), 0, nbytesRead );
					msg += decorder.decode( buf ).toString();
					//nbytesRead += nbytesRead;
				}
			}			
			
			buf.clear();
			
			System.out.println("readSocketThread.runInLoop() Read " + msg);
			
			streamInputMessage in  = new streamInputMessage( msg , (InetSocketAddress)client, this.localSocketInfo.getSocketAddress() );
			
			synchronized ( this.events )
			{
				this.events.add( new EventInfo( eventType.SOCKET_INPUT_MSG, in ) );
			}
		}
		
		synchronized ( super.monitorThread )
		{
			if( this.events.size() > 0 )
			{
				super.monitorThread.notify();
			}			
		}		
	}
	
	@Override
	public String getID() 
	{
		return this.getName();
	}
}
