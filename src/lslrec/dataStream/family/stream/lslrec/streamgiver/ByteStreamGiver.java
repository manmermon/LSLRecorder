/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.  https://github.com/manmermon/LSLRecorder
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
package lslrec.dataStream.family.stream.lslrec.streamgiver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.stoppableThread.AbstractStoppableThread;

/**
 * @author Manuel Merino Monge
 *
 */
public abstract class ByteStreamGiver extends AbstractStoppableThread 
{
	protected List< Byte > data = null;
	protected List< String > strs = null;	
	protected List< Double > timestamps = null;
	
	private Semaphore lock = new Semaphore( 1, true ); // to lock data read when non data available.
	
	public ByteStreamGiver()
	{
		this.data = new ArrayList< Byte >();
		this.strs = new ArrayList< String >();
		this.timestamps = new ArrayList< Double >();
		
		super.setName( this.getClass().getCanonicalName() );
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
	
	@Override
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
		
		synchronized ( this.data )
		{
			this.data.clear();
		}
		
		synchronized ( this.timestamps )
		{
			this.timestamps.clear();
		}
		
		this.release();		
	}
	
	@Override
	protected void runExceptionManager(Throwable e) 
	{
		if( !( e instanceof InterruptedException ) )
		{
			super.runExceptionManager( e );
		}
	}
	
	public int availableData()
	{
		synchronized ( this.data )
		{
			return this.data.size();
		}		
	}
	
	public int getSamples( byte[] samples, double[] timestamps )
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
	
	public int getSamples( String[] samples, double[] timestamps )
	{
		int length = 0;
		
		if( !this.strs.isEmpty() )
		{
			length = this.transferStringSamples( samples, timestamps ); 
		}
		else
		{
			try 
			{
				this.lock.acquire();
				
				length = this.transferStringSamples( samples, timestamps );
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
	
	private int transferSamples( byte[] samples, double[] timestamps )
	{
		int length = 0;
		
		synchronized ( this.data )
		{
			byte[] vals = ConvertTo.Casting.ByterArray2byteArray( this.data.toArray( new Byte[ 0 ] ) );
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
	
	private int transferStringSamples( String[] samples, double[] timestamps )
	{
		int length = 0;
		
		synchronized ( this.strs )
		{
			String[] vals = this.strs.toArray( new String[ 0 ] );
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
				
				this.strs.subList( 0,length ).clear();
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
