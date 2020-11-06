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
package testing.LSLSender;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;
import edu.ucsd.sccn.LSL;

public class LSLSimulationStreaming extends AbstractStoppableThread 
{
	private LSLSimulationParameters parameters;

	//private List< LSL.StreamInfo > infos;
	private List< LSL.StreamOutlet > outlets;

	private int numOfThreads;

	private byte[] buffer;
	private List sample;
	private List samplesInOneChannel;
	//private int stringBufferIndex = 0;
	private String stringBuffer;
	private int DataType;
	private double samplingRate = 1L;

	private double[] Sin;
	private int indexSin;
	
	private int NOutBlocks = 0;
	
	private long delay;
	
	private Random rand;
	private boolean isInteger = true;

	private boolean sleep = false;
	
	public LSLSimulationStreaming( LSLSimulationParameters pars ) throws Exception
	{
		outlets = new ArrayList< LSL.StreamOutlet>();
		parameters = pars;
		numOfThreads = pars.getNumOfThreads();
		DataType = pars.getOutDataType();

		NOutBlocks = pars.getNumberOutputBlocks(); 
		
		char Suf = 'A';
		for( int i = 0; i < numOfThreads; i++ )
		{
			LSL.StreamInfo info = new LSL.StreamInfo( pars.getStreamName() + (char)(Suf + i)
														, pars.getStreamType()
														, pars.getChannelNumber()
														, pars.getSamplingRate()
														, pars.getOutDataType()
														, pars.getStreamID() + (char)( Suf + i ) );

			LSL.StreamOutlet out = new LSL.StreamOutlet( info );

			outlets.add( out );
		}
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
	protected void runInLoop() throws Exception 
	{	
		double[] nextVal = this.getNextOutputValue( this.parameters.getBlockSize() );

		for( int i = 0; i < nextVal.length; i++ )
		{		
			double out = nextVal[ i ];
			
			switch ( DataType )
			{
			case LSL.ChannelFormat.float32:
			{
				//float value = (float)Math.random();				
				float value = (float)out;
				ByteBuffer.wrap( buffer ).putFloat( value );
	
				break;
			}
			case LSL.ChannelFormat.double64:
			{	
				//ByteBuffer.wrap( buffer ).putDouble( Math.random() );
				double value = out;
				ByteBuffer.wrap( buffer ).putDouble( value );
	
				break;
			}
			case LSL.ChannelFormat.int32:
			{
				//int value = (int)( Math.random() * 100 );
				int value = (int)( out );
				ByteBuffer.wrap( buffer ).putInt( value );
	
				break;
			}
			case LSL.ChannelFormat.int16:
			{
				//short value = (short)( Math.random() * 10 );
				short value = (short)( out );
				ByteBuffer.wrap( buffer ).putShort( value );
	
				break;
			}
			case LSL.ChannelFormat.int64:
			{
				//long value = (long)( Math.random() * 100 );
				long value = (long)( out );
				ByteBuffer.wrap( buffer ).putLong( value );
	
				break;
			}	
			case LSL.ChannelFormat.string:
			{
				String aux = "\n";
	
	
				buffer = ( out + aux ).getBytes();
				break;
			}
	
			default:
			{
				//byte value = (byte)( Math.random() * 10 );
				byte value = (byte)( out );
				ByteBuffer.wrap( buffer ).put( value );
	
				break;
			}
			}
		}

		if( DataType != LSL.ChannelFormat.string )
		{
			convertBufferToOutDataType( buffer, samplesInOneChannel );

			if( parameters.getOutputFunctionType() != parameters.RANDOM )
			{
				if( this.samplesInOneChannel.size() >= parameters.getBlockSize() )
				{	
					this.sample.clear();
					if( !parameters.isInterleavedData() )
					{
						for( int i = 0; i < parameters.getChannelNumber(); i++ )
						{
							this.sample.addAll( this.samplesInOneChannel );
						}
					}
					else
					{
						for( int i = 0; i < samplesInOneChannel.size(); i++ )
						{
							Object v = this.samplesInOneChannel.get( i );
							for( int c = 0; c < parameters.getChannelNumber(); c++ )
							{
								this.sample.add( v );
							}
						}
					}
									
					sendDataByLSL( );
					
					this.sample.clear();
					this.samplesInOneChannel.clear();
				}
			}
			else
			{
				this.sample.addAll( this.samplesInOneChannel );
				
				if( this.sample.size() >= parameters.getChannelNumber() * parameters.getBlockSize() )
				{							
					sendDataByLSL();
					
					this.sample.clear();
				}
				
				this.samplesInOneChannel.clear();
			}
		}
		else
		{		
			stringBuffer += new String( buffer );

			try
			{
				convertStringToNumber( stringBuffer, samplesInOneChannel );
			}
			catch( Exception e ){}

			if( this.samplesInOneChannel.size() >= parameters.getBlockSize() )
			{
				this.sample.clear();				
				if( !parameters.isInterleavedData() )
				{
					for( int i = 0; i < parameters.getChannelNumber(); i++ )
					{
						this.sample.addAll( this.samplesInOneChannel );
					}
				}
				else
				{
					for( int i = 0; i < this.samplesInOneChannel.size(); i++ )
					{
						Object v = this.samplesInOneChannel.get( i );
						for( int c = 0; c < parameters.getChannelNumber(); c++ )
						{
							this.sample.add( v );
						}
					}
				}
								
				sendDataByLSL( );
				
				this.sample.clear();
				this.samplesInOneChannel.clear();
			}

			stringBuffer = "";

			//} 
		}

	}

	@Override
	protected void targetDone() throws Exception 
	{
		if( this.sleep )
		{
			super.targetDone();
	
			//if( this.samplesInOneChannel.isEmpty() )
			{
				double sleepTime = this.samplingRate;
				if( this.delay > 0 )
				{
					sleepTime -= ( System.nanoTime() - this.delay ) / 1e9;
				}
							
				double sleepT2 = sleepTime * 1000;
				
				if( sleepT2 > 0 )
				{
					if( sleepT2  > 2 )
					{
						Thread.sleep( (long)sleepT2 );
					}
					else
					{
						sleepT2 = sleepTime * 1e6;
						//sleepTime -= 1e5;						
						busyWaitMicros( (long)sleepT2 );
					}
				}
			}
			
			
			this.sleep = false;
		}
	}
	
	 private void busyWaitMicros( long micros )
	 {
		 long waitUntil = System.nanoTime() + (micros * 1_000);

		 while(waitUntil > System.nanoTime()) 
		 {
			 
		 }
	 }
	
	@Override
	protected void finallyManager() 
	{	
		super.finallyManager();
		
		this.delay = System.nanoTime();
	}

	@Override
	protected void runExceptionManager(Exception e) 
	{	
		if( !( e instanceof InterruptedException ) )
		{
			e.printStackTrace();
		}
	}

	private void convertBufferToOutDataType( byte[] buf, List samples )
	{
		switch ( parameters.getOutDataType() )
		{
			case 1: 
			{	
				float v = ByteBuffer.wrap( buf ).order( parameters.getInDataFormat() ).getFloat();
				samples.add( v );
				
				break;
			}
			case 2:
			{
				double v = ByteBuffer.wrap( buf ).order( parameters.getInDataFormat()).getDouble();
				samples.add( v );
				
				break;
			}
			case 4:
			{
				int v = ByteBuffer.wrap( buf ).order( parameters.getInDataFormat() ).getInt();
				samples.add( v );
				
				break;
			}
			case 5:
			{
				short v = ByteBuffer.wrap( buf ).order( parameters.getInDataFormat() ).getShort();
				samples.add( v );
				
				break;
			}
			case 6:
			{				
				samples.add( buf[ 0 ] );
				
				break;
			}		
			case 7:
			{
				long v = ByteBuffer.wrap( buf ).order( parameters.getInDataFormat() ).getLong();
				samples.add( v );
				
				break;
			}			
			default:
			{	
				samples.add( new String( buf ) );
				
				break;
			}
		}
	}

	private void sendDataByLSL( )
	{
		if( !this.sample.isEmpty() )
		{
			this.sleep = true;
			
			Object[] values = this.sample.toArray();

			boolean sent = false;
			for( LSL.StreamOutlet outlet : outlets )
			{
				if( outlet.have_consumers() )
				{
					sent = true;
					
					switch ( parameters.getOutDataType() )
					{
						case LSL.ChannelFormat.float32:
						{
							float[] samples = new float[ values.length ];
							for( int i = 0; i < values.length; i++ )
							{
								samples[ i ] = ( float )values[ i ];
							}
							
							if( parameters.getBlockSize() < 2 )
							{
								outlet.push_sample( samples );
							}
							else
							{
								outlet.push_chunk( samples );
							}
							
							break;
						}
						case LSL.ChannelFormat.double64:
						{
							double[] samples = new double[ values.length ];
							for( int i = 0; i < values.length; i++ )
							{
								samples[ i ] = ( double )values[ i ];
							}
	
							if( parameters.getBlockSize() < 2 )
							{
								outlet.push_sample( samples );
							}
							else
							{
								outlet.push_chunk( samples );
							}
							
							break;				
						}
						case LSL.ChannelFormat.int32:
						{
							int[] samples = new int[ values.length ];
							for( int i = 0; i < values.length; i++ )
							{
								samples[ i ] = ( int )values[ i ];
							}
	
							if( parameters.getBlockSize() < 2 )
							{
								outlet.push_sample( samples );
							}
							else
							{
								outlet.push_chunk( samples );
							}
							
							break;
						}
						case LSL.ChannelFormat.int16:
						{
							short[] samples = new short[ values.length ];
							for( int i = 0; i < values.length; i++ )
							{
								samples[ i ] = ( short )values[ i ];
							}
	
							if( parameters.getBlockSize() < 2 )
							{
								outlet.push_sample( samples );
							}
							else
							{
								outlet.push_chunk( samples );
							}
							
							break;
						}
						case LSL.ChannelFormat.int8:
						{
							byte[] samples = new byte[ values.length ];
							for( int i = 0; i < values.length; i++ )
							{
								samples[ i ] = ( byte )values[ i ];
							}
	
							if( parameters.getBlockSize() < 2 )
							{
								outlet.push_sample( samples );
							}
							else
							{
								outlet.push_chunk( samples );
							}
							
							break;
						}		
						/*
						case 7:
						{
							long[] samples = new long[ values.length ];
							for( int i = 0; i < values.length; i++ )
							{
								samples[ i ] = ( long )values[ i ];
							}
	
							outlet.push_sample( samples );	
							break;
						}			
						 */
						default:
						{
							String[] samples = new String[ values.length ];
							for( int i = 0; i < values.length; i++ )
							{
								samples[ i ] = (String)values[ i ];
							}
	
							if( parameters.getBlockSize() < 2 )
							{
								outlet.push_sample( samples );
							}
							else
							{
								outlet.push_chunk( samples );
							}
							
							break;
						}
					}
				}
				else
				{
					this.resetIndexOuputValue();
				}
			}
			
			if( sent )
			{
				if( NOutBlocks < 1 )
				{
					super.stopThread( IStoppableThread.FORCE_STOP );
				}
				else
				{
					NOutBlocks--;
				}
			}
		}
	}

	protected void convertStringToNumber( String value, List samples ) throws Exception
	{
		switch ( parameters.getOutDataType() )
		{
		case 1:
		{
			samples.add( new Float( value ).floatValue() );
			break;
		}
		case 2:
		{
			samples.add( new Double( value ).doubleValue() );				
			break;				
		}
		case 4:
		{
			samples.add( new Integer( value ).intValue() );				
			break;
		}
		case 5:
		{
			samples.add( new Short( value ).shortValue() );	
			break;
		}
		case 6:
		{
			samples.add( new Byte( value ).byteValue() );
			break;
		}	
		case 7:
		{
			samples.add( new Long( value ).longValue() );	
			break;
		}			
		default:
		{
			samples.add( value );	
			break;
		}
		}
	}

	@Override
	protected void preStart() throws Exception 
	{
		RandomString gen = new RandomString( 8, ThreadLocalRandom.current() );
		this.buffer = ( gen.nextString() + "\n" ).getBytes();
		//this.LN = ThreadLocalRandom.current().nextInt( 1, 10 );

		int blockSize = parameters.getBlockSize();

		this.stringBuffer = "";
		this.sample = new ArrayList();
		this.samplesInOneChannel = new ArrayList();
		
		switch ( parameters.getOutDataType() )
		{
			case LSL.ChannelFormat.float32:
			{
				this.buffer = new byte[ Float.BYTES * blockSize ];
				this.isInteger = false;
				break;
			}
			case LSL.ChannelFormat.double64:
			{
				this.buffer = new byte[ Double.BYTES * blockSize  ]; 
				this.isInteger = false;
				break;
			}
			case LSL.ChannelFormat.int32:
			{
				this.buffer = new byte[ Integer.BYTES * blockSize  ];
				break;
			}
			case LSL.ChannelFormat.int16:
			{
				this.buffer = new byte[ Short.BYTES * blockSize  ];
				break;
			}
			case LSL.ChannelFormat.int64:
			{
				this.buffer = new byte[ Long.BYTES * blockSize ];
				break;
			}		
			case LSL.ChannelFormat.string:
			{
				this.isInteger = false;
				break;
			}
			default:
			{
				this.buffer = new byte[ 1 * blockSize ]; // byte & String
				break;
			}
		}

		double f = parameters.getSamplingRate();
		int N = (int)f;
		double t = f;
		if( N == 0 )
		{
			N = ThreadLocalRandom.current().nextInt( 1, 200 );			
		}
		
		t = 1D / f;

		if( t <= 0 )
		{
			t = 1;
		}

		this.Sin = new double[ N ];
		
		if( this.Sin.length < 10 )
		{
			this.Sin = new double[ 10 ];
		}
		
		switch( parameters.getOutputFunctionType() )
		{
			case LSLSimulationParameters.LINEAR:
			{
				for( int c = 0; c < this.Sin.length; c++ )
				{
					this.Sin[ c ] = c - this.Sin.length / 2;
				}
				
				if( parameters.getOutDataType() < 3 )
				{
					for( int c = 0; c < this.Sin.length; c++ )
					{
						this.Sin[ c ] = this.Sin[ c ] / this.Sin.length;
					}
				}
				break;
			}
			case LSLSimulationParameters.RANDOM:
			{
				/*
				for( int c = 0; c < this.Sin.length; c++ )
				{
					this.Sin[ c ] = Math.random();
				}
				*/
				
				this.rand = new Random();
						
				break;
			}
			default:
			{
				double step = 2 * Math.PI / this.Sin.length;
				for( int c = 0; c < this.Sin.length; c++ )
				{
					this.Sin[ c ] = Math.sin( c * step );
				}
			}
		}
		
		this.indexSin = -1;

		this.samplingRate = t;		
	}	
	
	@Override
	protected void startUp() throws Exception 
	{
		super.startUp();		
	}

	@Override
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();

		for( LSL.StreamOutlet outlet : outlets )
		{

			outlet.close();
		}

		outlets.clear();

		this.sample.clear();
		this.sample = null;		
	}
	
	private double[] getNextOutputValue( int len )
	{
		this.indexSin++;
		if( this.indexSin >= this.Sin.length )
		{
			this.resetIndexOuputValue();
		}
		
		double[] val = new double[ len ];
		
		for( int i = 0; i < val.length; i++ )
		{		
			val[ i ] = this.Sin[ this.indexSin ];
			
			if( this.parameters.getOutputFunctionType() == LSLSimulationParameters.RANDOM )
			{
				val[ i ] = this.rand.nextDouble();
				
				if( this.isInteger )
				{
					val[ i ] = this.rand.nextInt();
				}				
			}
		}	
		
		return val; 
	}
	
	private void resetIndexOuputValue()
	{
		this.indexSin = 0;
	}
}