package testing;

import java.util.ArrayList;
import java.util.List;

import Timers.ITimerMonitor;
import Timers.Timer;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLConfigParameters;

public class testSyncLSL implements ITimerMonitor
{
	static List< Integer > msgs = new ArrayList< Integer >();
	static int indMsg;

	static Timer timer = new Timer();

	static int cuenta = 0;
	
	static LSL.StreamOutlet out;

	static long timeValue = 500L;
	
	public static void main(String[] args) 
	{
		try 
		{			
			testSyncLSL main = new testSyncLSL();

			LSL.StreamInfo lslInfo = new LSL.StreamInfo( main.getClass().getSimpleName() + "SyncLSL-" + 2
														, "value"
														, 1
														, 0
														, LSL.ChannelFormat.int32
														, main.getClass().getSimpleName() ) ;

			lslInfo.desc().append_child_value( LSLConfigParameters.ID_GENERAL_DESCRIPTION_LABEL, "prueba" );
			out = new LSL.StreamOutlet( lslInfo );
			
			while( !out.have_consumers() )
			{
				Thread.sleep( 500L );
			}
						
			cuenta = (int)( ( timeValue / 1000D) * 60 ) * 1;
			
			timer.setTimerValue( timeValue );
			timer.setTimerMonitor( main );

			msgs.add( 1 );
			msgs.add( 3 );
			msgs.add( 4 );
			msgs.add( 5 );
			msgs.add( 6 );
			msgs.add( 2 );

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

		int msg = msgs.get( indMsg );

		if( cuenta <= 1 )
		{
			msg = msgs.get( msgs.size() - 1 );			
		}

		indMsg++;
				
		out.push_sample( new int[] { msg } );
		

		cuenta--;
		if( cuenta <= 0 )
		{	
			try 
			{
				Thread.sleep( 30000L );				
				System.exit( 0 );
			} 
			catch (InterruptedException e) 
			{
				
			}
			finally 
			{
				
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
