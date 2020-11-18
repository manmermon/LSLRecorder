/**
 * 
 */
package lslrec.testing.DataStream.family.lslrec;

import java.util.concurrent.atomic.AtomicBoolean;

import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;
import lslrec.dataStream.family.stream.lslrec.DataStreamGiver;
import lslrec.dataStream.family.stream.lslrec.SimpleDataStream;
import lslrec.stoppableThread.IStoppableThread;

/**
 * @author Manuel Merino Monge
 *
 */
public class testSimpleDataStream {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		DataGiver dg = new DataGiver();
		try {
			dg.startThread();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		SimpleDataStream< Double > dataStream = new SimpleDataStream<Double>( "test"
																				, StreamDataType.double64
																				, 0
																				, 2
																				, 5
																				, dg
																			);	
		
		AtomicBoolean end = new AtomicBoolean( false );
		Thread t = new Thread()
		{
			public void run() 
			{
				while( !end.get() )
				{
					try 
					{
						synchronized ( this )
						{
							super.wait( 100L );
						}

						dg.setData();
						
					} 
					catch (InterruptedException e) 
					{
					}

				}
			}
		};
				
		t.start();
		
		Double[] dat = new Double[ 10 ];
		double[] time = new double[ 2 ];
		for( int i = 0 ; i < 100 ; i++ )
		{
			try 
			{
				int s = dataStream.pull_chunk( dat,time );
				
				if( s == 0 )
				{
					System.out.println("Empty data");
				}
				else
				{
					System.out.print( i +">> Inputs: " );
					
					for( int j = 0; j < s; j++ )
					{
						System.out.print( dat[ j ] + "," );
					}					
					System.out.println();
				}
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		
		end.set( false );
		
		dg.stopThread( IStoppableThread.FORCE_STOP );
	}
		
	
	static class DataGiver extends DataStreamGiver< Double >
	{
		@Override
		protected void setData() 
		{
			for( int i = 0; i < 10; i++ )
			{
				super.data.add( Double.valueOf( i ) );
			}
			
			for( int i = 0; i < 2; i++ )
			{
				super.timestamps.add( Double.valueOf( System.nanoTime() ) );
			}
			
			synchronized ( this )
			{
				super.notify();
			}
		}
		
	}
}
