/**
 * 
 */
package lslrec.dataStream.family.stream.lslrec;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.stoppableThread.AbstractStoppableThread;

/**
 * @author Manuel Merino Monge
 *
 */
public abstract class DataStreamGiver< T > extends AbstractStoppableThread 
{
	protected List< T > data = null;
	protected List< Double > timestamps = null;
	
	private Semaphore lock = new Semaphore( 1, true ); // to lock data read when non data available.
	
	public DataStreamGiver()
	{
		this.data = new ArrayList< T >();
		this.timestamps = new ArrayList< Double >();
	}

	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{	
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{	
	}

	@Override
	protected void startUp() throws Exception 
	{
		this.lock.drainPermits();
	}
	
	@Override
	protected void runInLoop() throws Exception 
	{	
		synchronized ( this )
		{
			super.wait();
		}

		this.release();
	}
			
	public int getSamples( T[] samples, double[] timestamps )
	{
		int length = 0;
		
		if( !this.data.isEmpty() )
		{
			length = this.transferSamples( samples, timestamps ); 
		}
		else
		{
			try 
			{
				this.lock.acquire();
				
				length = this.transferSamples( samples, timestamps );
			}
			catch (InterruptedException e) 
			{
			}
		}		
		
		this.lock.drainPermits();
						
		return length;
	}
	
	public void release()
	{
		this.lock.release();
	}
	
	private int transferSamples( T[] samples, double[] timestamps )
	{
		int length = 0;
		
		synchronized ( this.data )
		{
			Object[] vals = this.data.toArray( );
			double[] times = ConvertTo.Casting.DoubleArray2doubleArray( this.timestamps.toArray( new Double[0] ) );
			
			length = vals.length;
			int timeLen = times.length;
			
			if( length > samples.length )
			{
				length = samples.length;
			}
			
			if( timeLen > timestamps.length )
			{
				timeLen = timestamps.length;
			}
			
			if( length > 0 )
			{
				System.arraycopy( vals, 0, samples, 0, length);
				System.arraycopy( times, 0, timestamps, 0, timeLen );
				
				this.data.subList( 0,length ).clear();
				this.timestamps.subList( 0, timeLen ).clear();
			}
		}
				
		return length;
	}
	
	/**
	 * Set data and their timestamps into buffers.
	 * Then, notify() function must be called to transfer samples
	 */
	protected abstract void setData();
}
