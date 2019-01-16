package testing;

import java.util.ArrayList;
import java.util.List;
import Prototype.Socket.TCPClientSocketThread;
import Prototype.Socket.UDPClientSocketThread;
import Sockets.Info.SocketSetting;
import StoppableThread.IStoppableThread;
import Timers.ITimerMonitor;
import Timers.Timer;

public class testSendSocketMsgToLslRec implements ITimerMonitor
{
	static List< String > msgs = new ArrayList< String >();
	static int indMsg;

	static Timer timer = new Timer();

	static int cuenta = 0;

	static List< TCPClientSocketThread > clients;
	static List< UDPClientSocketThread > udpClients;

	public static void main(String[] args) 
	{
		try 
		{			
			testSendSocketMsgToLslRec main = new testSendSocketMsgToLslRec();
						
			long time = 500L;

			cuenta = (int)( (time / 1000D) * 60 ) * 60;
			cuenta = 12;
			
			timer.setTimerValue( time );
			timer.setTimerMonitor( main );


			SocketSetting info = new SocketSetting( SocketSetting.TCP_PROTOCOL, "127.0.0.1", 34839 );

			clients = new ArrayList< TCPClientSocketThread >();
			udpClients = new ArrayList< UDPClientSocketThread >();

			for( int n = 0; n < 1; n++ )
			{
				if( info.getProtocolType() == SocketSetting.TCP_PROTOCOL )
				{
					boolean ok = false;
					while( !ok )
					{
						try
						{
							TCPClientSocketThread c = new TCPClientSocketThread( info.getSocketAddress() );
							ok = true;
							clients.add( c );

							c.startThread();
						}
						catch( Exception ex)
						{	
						}
					}					
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
			msgs.add( "mark_1" + end);
			msgs.add( "mark_2" + end);
			msgs.add( "mark_3" + end);
			msgs.add( "mark_4" + end);
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

		System.out.println("testSendSocketMsgToLslRec.timeOver() " + msg);
		indMsg++;
		
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
