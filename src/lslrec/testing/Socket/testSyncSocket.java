package lslrec.testing.Socket;

import lslrec.sockets.info.SocketSetting;
import lslrec.testing.AppRunning.SyncStream.testSendSocketMsgToLslRec;

public class testSyncSocket 
{
	public static void main(String[] args) throws Exception 
	{
		long t = 100L;
		
		double f = 1000D / t;
		
		String ip = "127.0.0.1";
		int port = 45678;
		int protocol = SocketSetting.UDP_PROTOCOL;
		double time = 60;
		
		if( args != null && args.length > 0 )
		{
			for( int i = 1; i < args.length; i = i + 2 )
			{
				String id = args[ i-1 ];
				String val = args[ i ].toLowerCase();
				
				if( id.equals( "-fr" ) )
				{
					f = new Double( val );
					t = (long)( 1000D / f );
					if( t < 0 )
					{
						t = 1;
					}
							
				}
				else if( id.equals( "-ip" ) )
				{
					ip = val;
				}
				else if( id.equals( "-port" ) )
				{
					port = new Integer( val );
				}
				else if( id.equals( "-protocol") )
				{
					if( val.equals( "tcp" ) )
					{
						protocol = SocketSetting.TCP_PROTOCOL;
					}
				}
				else if( id.equals( "-time" ) )					
				{
					time = new Double( val );
				}
			}			
		}
		
		testSendSocketMsgToLslRec test = new testSendSocketMsgToLslRec( 1
																		, t
																		, (int)( time * 60 * f )
																		, protocol
																		, ip
																		, port );
		
		try
		{
			Thread.sleep( 15 * 1000L );
		}
		catch (Exception e) 
		{
		}
		
		System.out.println("testSyncSocket.main() START");
		
		test.startThread();
		test.join();
		
	}
}
