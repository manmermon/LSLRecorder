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

import java.io.IOException;
import java.net.DatagramPacket;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.util.LinkedList;
import java.util.List;

import Controls.Messages.EventInfo;
import Controls.Messages.eventType;
import Sockets.Info.SocketSetting;
import Prototype.Discarded.Malfunction.Socket.streamingOutputMessage;

public class writeSocketThread extends TemplateReadWriteSocketThread
{	
	private List< streamingOutputMessage > msgList = null;
	
	private boolean isSendingMsg = false;
	
	public writeSocketThread( AbstractSelectableChannel channel ) throws Exception  
	{
		super( channel );
		
		this.msgList = new LinkedList< streamingOutputMessage >();
	}
	
	@Override
	protected boolean checkIsWait() 
	{	
		return this.getState().equals( State.WAITING ) && !this.isSendingMsg;		
	}
	
	/**
	 * Send message to remote socket. 
	 * @param msg: String data to send.
	 * @param destinations: remote socket list (only for UDP).
	 */
	public void sendMessage( final streamingOutputMessage msg )
	{		
		if( msg != null )
		{
			this.isSendingMsg = true;
			
			final Thread notifiedThread = this;
			new Thread()
			{
				public void run() 
				{
					try
					{
						synchronized ( msgList ) 
						{
							msgList.add( msg );
						}
									
						synchronized ( notifiedThread )
						{
							notifiedThread.notify();
						}
					}
					catch( Exception e )
					{
						
					}
				};
				
			}.start();
		}
	}

	@Override
	protected void runInLoop() throws InterruptedException, IOException 
	{		
		super.wait();
		
		synchronized ( this.msgList )
		{
			for( streamingOutputMessage msg : this.msgList )
			{	
				String message = msg.getMessage();
				ByteBuffer buf = ByteBuffer.allocate( message.length() );					
				buf.clear();
				buf.put( message.getBytes() );
						
				buf.flip();
				
				if( this.socket instanceof SocketChannel )
				{						
					while( buf.hasRemaining() ) 
					{
					    ( ( SocketChannel )this.socket ).write( buf );
					}
				}
				else
				{
					List< SocketSetting > dests = msg.getDestinations();
					
					if( dests != null )
					{
						for( SocketSetting d : dests )
						{
							DatagramPacket data = new DatagramPacket( buf.array(), 
																		buf.array().length, 
																		d.getSocketAddress() );
							
							( ( DatagramChannel ) this.socket ).socket().send( data );
						}
					}	
				}
				
				buf.clear();
			}
			
			this.msgList.clear();
			
			synchronized ( this.events )
			{
				this.events.add( new EventInfo( eventType.SOCKET_OUTPUT_MSG_OK, true ) );
			}
		}	
		
		this.isSendingMsg = false;
		
		synchronized ( this.monitorThread )
		{
			if( this.events.size() > 0 )
			{
				this.monitorThread.notify();
			}
		}
	}

	@Override
	public void targetDone() throws Exception 
	{
		synchronized( this.msgList )
		{
			if( super.stopWhenTaskDone )
			{
				super.stopThread = this.msgList.size() == 0;				
			}
		}		
	}
	
	@Override
	public String getID() 
	{
		return this.getName();
	}
}
