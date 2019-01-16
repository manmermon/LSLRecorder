package testing;

import java.util.ArrayList;
import java.util.List;
import Prototype.Socket.TCPClientSocketThread;
import Prototype.Socket.UDPClientSocketThread;
import Sockets.Info.SocketSetting;
import StoppableThread.IStoppableThread;
import Timers.ITimerMonitor;
import Timers.Timer;
import edu.ucsd.sccn.LSL;

public class testSyncSocket implements ITimerMonitor
{
	static List< String > msgs = new ArrayList< String >();
	static int indMsg;

	static Timer timer = new Timer();

	static int cuenta = 0;

	static List< TCPClientSocketThread > clients;
	static List< UDPClientSocketThread > udpClients;
	
	static LSL.StreamOutlet out;

	public static void main(String[] args) 
	{
		try 
		{			
			testSyncSocket main = new testSyncSocket();

			LSL.StreamInfo lslInfo = new LSL.StreamInfo( main.getClass().getSimpleName() + "UDP-" + 6
														, "value"
														, 1
														, 1
														, LSL.ChannelFormat.double64
														, main.getClass().getSimpleName() ) ;

			out = new LSL.StreamOutlet( lslInfo );
			
			System.out.println("Pulsa tecla para continuar");
			System.in.read();
			System.out.println( "Continuando" );
			
			long time = 1000L;

			cuenta = (int)( (time / 1000D) * 3600 ) * 8;
			
			timer.setTimerValue( time );
			timer.setTimerMonitor( main );


			SocketSetting info = new SocketSetting( SocketSetting.UDP_PROTOCOL, "127.0.0.1", 5439 );
			//SocketSetting info = new SocketSetting( SocketSetting.TCP_PROTOCOL, "127.0.0.1", 12345 );

			clients = new ArrayList< TCPClientSocketThread >();
			udpClients = new ArrayList< UDPClientSocketThread >();

			for( int n = 0; n < 1; n++ )
			{
				if( info.getProtocolType() == SocketSetting.TCP_PROTOCOL )
				{
					TCPClientSocketThread c = new TCPClientSocketThread( info.getSocketAddress() );
					clients.add( c );

					c.startThread();					
				}
				else
				{
					UDPClientSocketThread updClient = new UDPClientSocketThread( info.getSocketAddress() );
					udpClients.add( updClient );

					updClient.startThread();					
				}
			}

			String end = "" + "\n";
			msgs.add( "__start" + end );
			msgs.add( "msg2" + end);
			msgs.add( "msg3" + end);
			msgs.add( "msg4" + end);
			msgs.add( "msg5" + end);
			msgs.add( "msg6" + end);
			msgs.add( "msg7"+ end );
			msgs.add( "msg8"+ end );
			msgs.add( "msg9" + end);
			msgs.add( "msg10" + end);
			msgs.add( "msg11" + end);
			msgs.add( "__stop" + end);

			timer.setName( "TIMER-testSendMsg2LSLRec");
			timer.startThread();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	@Override
	public void timeOver(String timerName) 
	{
		if( indMsg > 0 )
		{
			if( indMsg >= ( msgs.size() - 1 ))
			{
				indMsg = 1;
			}
		}		

		String msg = msgs.get( indMsg );

		if( cuenta <= 1 )
		{
			msg = msgs.get( msgs.size() - 1 );			
		}

		indMsg++;
		
		//double t = ( System.nanoTime() / 1e9D );
		//out.push_sample( new double[] { t } );
		
		//System.out.println("testSendSocketMsgToLslRec.timeOver() " + msg + " > time " + t );
		
		for( TCPClientSocketThread client : clients )
		{
			client.sendMessage( msg );
		}

		for( UDPClientSocketThread client : udpClients )
		{
			client.sendMessage( msg );
		}

		cuenta--;
		if( cuenta <= 0 )
		{		
			for( TCPClientSocketThread client : clients )
			{
				client.stopThread( IStoppableThread.FORCE_STOP );
			}

			for( UDPClientSocketThread client : udpClients )
			{
				client.stopThread( IStoppableThread.FORCE_STOP );
			}

			timer.stopThread( IStoppableThread.FORCE_STOP );
			
			try 
			{
				Thread.sleep( 10000L );
			} 
			catch (InterruptedException e) 
			{
				
			}
			finally 
			{
				System.out.println("FIN");
				System.exit( 0 );
			}
		}
		else
		{
			timer.restartTimer();
		}
	}

	@Override
	public void reportClockTime(long time) 
	{		
	}

}
