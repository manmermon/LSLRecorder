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

package lslrec.testing.Socket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import lslrec.sockets.TCPSeverSocketTheard;


public class testTCPSocket 
{
	public static void main(String[] args) throws Exception 
	{	
		TCPSeverSocketTheard server = new TCPSeverSocketTheard();
		
		InetSocketAddress remote = new InetSocketAddress( server.getIPAddress(), server.getServerPort() );
		TCPClientSocketThread client = new TCPClientSocketThread( remote  );
		TCPClientSocketThread client2 = new TCPClientSocketThread( remote );
				
		server.startThread();
		client.startThread();
		client2.startThread();
		
		client.sendMessage( "Prueba ");
		if( true )
		{			
			List< String > msgs = new ArrayList< String >();
			msgs.add( "__start" );
			
			String m = "msg";
			
			for( int i = 2; i < 6; i++ )
			{
				msgs.add( m + i  );
			}
			
			msgs.add( "__stop" );
			
			int c = 0;
			for( String msg : msgs )
			{
				client.sendMessage( msg );
				if( c < msgs.size() / 2 )
				{
					client2.sendMessage( msg );
				}
				else if( c == msgs.size() / 2 )
				{
					//System.out.println("testTCPSocket.main() CLOSE SERVER: " + server.getName() );
					//server.stopThread( server.FORCE_STOP );
					client2.stopThread( client2.FORCE_STOP );
				}
				
				c++;
				//Thread.sleep( 300L );
			}
		}
		
		System.out.println("testTCPSocket.main() CLOSES ");
		
		client.stopThread( client.FORCE_STOP );
		server.stopThread( server.FORCE_STOP );
		//client.stopThread( client.STOP_WITH_TASKDONE );
	}
}
