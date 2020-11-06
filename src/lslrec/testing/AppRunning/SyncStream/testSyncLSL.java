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
package testing.AppRunning.SyncStream;

import java.util.ArrayList;
import java.util.List;

import stoppableThread.AbstractStoppableThread;
import stoppableThread.IStoppableThread;
import timers.ITimerMonitor;
import timers.Timer;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLConfigParameters;

public class testSyncLSL extends AbstractStoppableThread implements ITimerMonitor
{
	private List< Integer > msgs = new ArrayList< Integer >();
	private int indMsg;

	private Timer timer = new Timer();

	private int counter = 0;
	
	private int _softClose = 1;
	
	private lslrec.dataStream.family.lsl.StreamOutlet out;
	private lslrec.dataStream.family.lsl.StreamOutlet out_time;
	
	public static String PREFIX =  "SyncLSL-"; 
	
	public testSyncLSL( int id, long time, int count, int softClose ) 
	{
		try 
		{			
			
			double f = Math.abs( time / 1000D );
			if( f != 0 )
			{
				f = 1 / f;
			}
			
			LSL.StreamInfo lslInfo = new LSL.StreamInfo( PREFIX + id
														, "value"
														, 1
														, f
														, LSL.ChannelFormat.int32
														, super.getClass().getSimpleName() + "-" + id ) ;

			lslInfo.desc().append_child_value( MutableDataStreamSetting.ID_GENERAL_DESCRIPTION_LABEL, "test" );
			out = new LSL.StreamOutlet( lslInfo );
			
			LSL.StreamInfo lslInfo_time = new LSL.StreamInfo( PREFIX + "Tiempos-" + id
															, "value"
															, 2
															, f
															, LSL.ChannelFormat.double64
															, super.getClass().getSimpleName() ) ;
			lslInfo_time.desc().append_child_value( "details", "ch1-time; ch2-mark");

			lslInfo_time.desc().append_child_value( MutableDataStreamSetting.ID_GENERAL_DESCRIPTION_LABEL, "tiempo de cada marca enviada" );
			out_time = new LSL.StreamOutlet( lslInfo_time );
			
			_softClose = softClose; 
			
			counter = count;
			
			timer.setTimerValue( time );
			timer.setTimerMonitor( this );
			
			/*
			msgs.add( -2 );
			msgs.add( -1 );
			// */
			
			msgs.add( 1 );
			
			int val = 2;
			
			int lim = (  (int)( 1 / f) ) % 29;
			
			if( lim < 1 )				
			{
				lim = 29;
			}
			
			int step = ( id + 1 );
			
			if( step < 1 )
			{
				step = 1;
			}
			
			for( int c = 0; c <= lim; c++ )
			{
				val = val << step;
				
				if( val == 1 || val == 2 || val == 0)
				{
					val = 3;
				}
				
				msgs.add( val );
			}
						
			msgs.add( 2 );
			
			timer.setName( "TIMER-SyncLSL");
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

		if( counter <= 1 && _softClose != 0 )
		{
			msg = msgs.get( msgs.size() - 1 );			
		}

		indMsg++;

		double[] t = new double[] { System.nanoTime() / 1e9D, msg };
		
		out.push_sample( new int[] { msg } );
		out_time.push_sample( t );

		counter--;
		if( counter <= 0 )
		{	
			try 
			{
				if( _softClose != 0 )
				{
					Thread.sleep( 5000L );
				}
				
				this.out.close();
				this.out_time.close();
				this.timer.stopThread( IStoppableThread.FORCE_STOP );
				//Thread.sleep( 30000L );				
				//System.exit( 0 );
				
			} 
			catch (Exception e) 
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

	@Override
	protected void startUp() throws Exception 
	{
		super.startUp();
		while( !out.have_consumers() )
		{
			try
			{
				Thread.sleep( 500L );
			}
			catch (Exception e) 
			{
			}
		}
		

		timer.startThread();
		
		super.stopThread = true;
	}
	
	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{
		// TODO Auto-generated method stub
		timer.stopThread( friendliness );
		this.out.close();
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void runInLoop() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
