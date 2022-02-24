/**
 * 
 */
package lslrec.testing.DataStream.family.lslrec;

import java.util.concurrent.atomic.AtomicBoolean;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.dataStream.family.stream.lslrec.LSLRecSimpleDataStream;
import lslrec.dataStream.family.stream.lslrec.streamgiver.ByteStreamGiver;
import lslrec.dataStream.tools.StreamUtils.StreamDataType;
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
		
		LSLRecSimpleDataStream dataStream = new LSLRecSimpleDataStream( "test"
																		, StreamDataType.double64
																		, 0
																		, 2
																		, 5
																		);
		dataStream.setDataStreamGiver( dg );
		
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
		
		double[] dat = new double[ 10 ];
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
					
					System.out.print( "\n\tTimestamps: " );
					
					for( int j = 0; j < s / 5; j++ )
					{
						System.out.print( time[ j ] + "," );
					}
					System.out.println();
				}
			}
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		
		end.set( true );
		
		dg.stopThread( IStoppableThread.FORCE_STOP );
	}
			
	static class DataGiver extends ByteStreamGiver
	{
		@Override
		protected void setData() 
		{
			for( int i = 0; i < 10; i++ )
			{				
				for( byte b :  ConvertTo.Transform.doubleArray2byteArray( new double[]{ Double.valueOf( i ) } ) )
				{				
					super.data.add( b );
				}
			}
			
			for( int i = 0; i < 2; i++ )
			{
				super.timestamps.add( Double.valueOf( System.nanoTime() / 1e9D) );
			}
			
			synchronized ( this )
			{
				super.notify();
			}
		}		
	}
}
