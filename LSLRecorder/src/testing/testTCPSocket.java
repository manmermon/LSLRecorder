package testing;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import Prototype.Socket.TCPClientSocketThread;
import Sockets.TCPSeverSocketTheard;

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
			
			for( int i = 2; i < 100; i++ )
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
