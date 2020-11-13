package lslrec.testing.Socket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lslrec.auxiliar.tasks.INotificationTask;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.controls.messages.EventInfo;
import lslrec.controls.messages.EventType;
import lslrec.sockets.SocketMessageDelayCalculator;
import lslrec.sockets.info.StreamInputMessage;
import lslrec.stoppableThread.IStoppableThread;


public class testSockectMessageDelayCalculator implements ITaskMonitor
{
	static testSockectMessageDelayCalculator test;
	
	
	public static void main(String[] args) throws Exception 
	{		
		test = new testSockectMessageDelayCalculator();
				
		List< StreamInputMessage > msgList = new ArrayList< StreamInputMessage >();
		int lim = 3;
		for( int i = 0; i < lim; i++ )
		{		
			StreamInputMessage msg = new StreamInputMessage( "test" + i, new InetSocketAddress( "127.0.0.1", 45678 ), new InetSocketAddress( "127.0.0.1", 45679 ) );
			msgList.add( msg );
		}
		
		Map< String, Integer > regMarks = new HashMap< String, Integer >();
		
		for( int i = 0; i < msgList.size(); i++ )
		{
			regMarks.put( msgList.get( i ).getMessage(), i );
		}
				
		SocketMessageDelayCalculator cal = new SocketMessageDelayCalculator( SocketMessageDelayCalculator.DEFAULT_NUM_PINGS );
		cal.taskMonitor( test );
		cal.AddInputMessages( regMarks );
		
		cal.startThread();
		
		for( StreamInputMessage msg : msgList )
		{
			cal.CalculateMsgDelay( msg );
			System.out.println();
		}
				
		System.out.println( "\ntestSockectMessageDelayCalculator.main() END" );
		
		cal.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
	}

	@Override
	public void taskDone( INotificationTask task )  
	{
		List< EventInfo > evs = task.getResult( true );
		
		for( EventInfo event : evs )
		{
			if( event.getEventType().equals( EventType.SOCKET_PING_END ) )
			{
				System.out.println( "testSockectMessageDelayCalculator.eventNotification() " + event );
			}
			else if( event.getEventType().equals( EventType.INPUT_MARK_READY ) )
			{
				System.out.println( "testSockectMessageDelayCalculator.eventNotification() " + event );
			}
		}
	}
}
