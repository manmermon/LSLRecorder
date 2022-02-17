/**
 * 
 */
package lslrec.dataStream.family.stream.lslrec;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.Thread.State;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import javax.swing.Timer;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting.StreamLibrary;
import lslrec.dataStream.family.setting.MutableStreamSetting;
import lslrec.dataStream.family.setting.SimpleStreamSetting;
import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;
import lslrec.dataStream.family.stream.IDataStream;
import lslrec.dataStream.family.stream.lslrec.streamgiver.ByteStreamGiver;
import lslrec.stoppableThread.IStoppableThread;

/**
 * @author Manuel Merino Monge
 *
 */
public class LSLRecSimpleDataStream implements IDataStream
{	
	private MutableStreamSetting simpleStream = null;
	
	private Semaphore lockSem = new Semaphore( 1, true );
	
	private Timer timer = null;
	
	private ByteStreamGiver datGiver = null;
	
	//private Boolean open = false;
	
	/**
	 * 
	 */
	public LSLRecSimpleDataStream( String name, StreamDataType dataType, double samplingRate
									, int channels, int chunkSize
									//, ByteStreamGiver giver 
									) 
	{
		
		
		SimpleStreamSetting sss = new SimpleStreamSetting( StreamLibrary.LSLREC
															, name
															, dataType
															, StreamDataType.double64
															, StreamDataType.int64
															, channels
															, chunkSize
															, samplingRate
															, 3
															, name + "-" + System.nanoTime()
															, name + "-" + System.nanoTime()
															, new HashMap< String, String >()
															
															);
				
		//this.setSettings( sss, giver );
		this.setSettings( sss );
	}

	public LSLRecSimpleDataStream( IStreamSetting strSetting )//, ByteStreamGiver giver	) 
	{
		this.setSettings( strSetting );
	}
	
	public ByteStreamGiver getDataStreamGiver()
	{
		return this.datGiver;
	}
	
	private void setSettings( IStreamSetting strSetting ) //, ByteStreamGiver giver )
	{
		if( strSetting == null )//|| giver == null )
		{
			throw new NullPointerException( "StreamSetting null." );
		}
		
		//this.datGiver = giver;

		this.simpleStream = new MutableStreamSetting( strSetting );

		this.lockSem.drainPermits();
		
		//this.open = false;
	}
	
	public void setDataStreamGiver( ByteStreamGiver g )
	{
		if( this.datGiver == null 
				|| this.datGiver.getState().equals( State.NEW )
				|| this.datGiver.getState().equals( State.TERMINATED ) )
		{
			this.datGiver = g;
		}
	}
	
	@Override
	public void close() 
	{		
		if( this.datGiver != null && !this.datGiver.getState().equals( State.NEW ) )
		{
			this.datGiver.stopThread( IStoppableThread.STOP_WITH_TASKDONE  );
		}
		
		/*
		synchronized ( this.open )
		{
			if( this.open )
			{
				this.datGiver.release();
			}
			
			this.open = false;
		}
		*/		
	}

	@Override
	public IStreamSetting info( double timeout ) throws Exception 
	{
		return this.simpleStream.getStreamSetting();
	}
		
	@Override
	public IStreamSetting info() throws Exception 
	{
		return this.info( TIME_FOREVER );
	}

	@Override
	public void open_stream( double timeout ) throws Exception 
	{	
		this.startTimer( timeout, this.getTimeoutOpenAction() );
		
		try
		{
			/*
			synchronized ( this.open )
			{
				this.open = true;
			}
			*/
			
			if( this.datGiver != null && this.datGiver.getState().equals( State.NEW ) )
			{
				this.datGiver.startThread();
			}
		}
		catch (Exception e) 
		{
			throw e;
		}
		finally 
		{
			this.stopTimer();
		}
	}

	@Override
	public void open_stream() throws Exception 
	{
		this.open_stream( TIME_FOREVER );
	}
	
	@Override
	public void close_stream() 
	{
		//this.close();
		
		if( this.datGiver != null && !this.datGiver.getState().equals( State.NEW ) )
		{
			this.datGiver.stopThread( IStoppableThread.FORCE_STOP );
		}
	}

	@Override
	public double time_correction( double timeout ) throws Exception 
	{
		return 0;
	}

	@Override
	public double time_correction() throws Exception 
	{
		return 0;
	}

	@Override
	public boolean was_clock_reset() 
	{
		return false;
	}

	private void startTimer( double timeout, ActionListener action )
	{
		if( this.timer != null )
		{
			this.timer.stop();			
		}
		
		int delay = (int)Math.round( 1000 * timeout );
		
		this.timer = new Timer( delay, action );
		
		this.timer.start();
	}
	
	private void stopTimer()
	{
		if( this.timer != null )
		{
			this.timer.stop();			
		}
		
		this.timer = null;
	}
	
	private ActionListener getTimeoutOpenAction( )
	{
		return new ActionListener() 
				{			
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						if( datGiver != null )
						{
							datGiver.stopThread( IStoppableThread.FORCE_STOP );
						}
					}
				};
	}
	
	private ActionListener getTimeoutReadAction( )
	{
		return new ActionListener() 
				{			
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						if( datGiver != null )
						{
							datGiver.release();
						}
					}
				};
	}
	

	/**
	 * Do nothing
	 */
	@Override
	public int samples_available() 
	{
		return 0;
	}
	
	private byte[] createByteArray( int nBytes )
	{
		return new byte[ nBytes ];
	}
	
	@Override
	public double pull_sample( byte[] sample, double timeout) throws Exception 
	{
		double[] time = new double[] { 0D };
	
		this.pull_chunk( sample, time );
				
		return time[ 0 ];
	}

	@Override
	public double pull_sample( byte[] sample) throws Exception 
	{	
		return this.pull_sample( sample, TIME_FOREVER );
	}

	@Override
	public int pull_chunk( byte[] data_buffer, double[] timestamp_buffer, double timeout) throws Exception 
	{
		int nSamples = 0;
		
		if( this.datGiver != null && !this.datGiver.getState().equals( State.NEW ) && !this.datGiver.getState().equals( State.TERMINATED ) )
		{
			this.startTimer( timeout, this.getTimeoutReadAction() );
					
			nSamples = this.datGiver.getSamples( data_buffer, timestamp_buffer );
			
			this.stopTimer();
		}
		
		return nSamples;
	}

	@Override
	public int pull_chunk( byte[] data_buffer, double[] timestamp_buffer) throws Exception 
	{
		return this.pull_chunk( data_buffer, timestamp_buffer, TIME_FOREVER );
	}

	@Override
	public double pull_sample(float[] sample, double timeout) throws Exception 
	{	
		double time = 0;
		
		if( this.datGiver != null && !this.datGiver.getState().equals( State.NEW ) && !this.datGiver.getState().equals( State.TERMINATED ) )
		{
			StreamDataType type = StreamDataType.float32;		
			byte[] dat = this.createByteArray( simpleStream.getDataTypeBytes( type ) * sample.length );
			
			time = this.pull_sample( dat, timeout );
			
			Number[] ns = ConvertTo.Transform.ByteArray2ArrayOf( dat, type );
			for( int i = 0; i < ns.length && i < sample.length; i++ )
			{
				sample[ i ] = ns[ i ].floatValue();
			}
		}
		
		return time;
	}

	@Override
	public double pull_sample(float[] sample) throws Exception 
	{
		return this.pull_sample( sample, TIME_FOREVER );
	}
	
	@Override
	public int pull_chunk(float[] data_buffer, double[] timestamp_buffer, double timeout) throws Exception 
	{
		int nSamples = 0;
		
		if( this.datGiver != null && !this.datGiver.getState().equals( State.NEW ) && !this.datGiver.getState().equals( State.TERMINATED ) )
		{		
			StreamDataType type = StreamDataType.float32;		
			byte[] dat = this.createByteArray( simpleStream.getDataTypeBytes( type ) * data_buffer.length );
			
			nSamples = this.pull_chunk( dat, timestamp_buffer, timeout );		
			
			Number[] ns = ConvertTo.Transform.ByteArray2ArrayOf( dat, type );
			nSamples = ns.length;
			for( int i = 0; i < ns.length && i < data_buffer.length; i++ )
			{
				data_buffer[ i ] = ns[ i ].floatValue();
			}
		}
		
		return nSamples;		
	}

	@Override
	public int pull_chunk(float[] data_buffer, double[] timestamp_buffer) throws Exception 
	{
		return this.pull_chunk( data_buffer, timestamp_buffer, TIME_FOREVER );
	}

	@Override
	public double pull_sample(double[] sample, double timeout) throws Exception 
	{
		double time = 0D;
		
		if( this.datGiver != null && !this.datGiver.getState().equals( State.NEW ) && !this.datGiver.getState().equals( State.TERMINATED ) )
		{
			StreamDataType type = StreamDataType.double64;		
			byte[] dat = this.createByteArray( simpleStream.getDataTypeBytes( type ) * sample.length );
			
			time = this.pull_sample( dat, timeout );
			
			Number[] ns = ConvertTo.Transform.ByteArray2ArrayOf( dat, type );
			for( int i = 0; i < ns.length && i < sample.length; i++ )
			{
				sample[ i ] = ns[ i ].doubleValue();
			}
		}
		
		return time;
	}

	@Override
	public double pull_sample(double[] sample) throws Exception 
	{
		return this.pull_sample( sample, TIME_FOREVER );
	}
	
	@Override
	public int pull_chunk(double[] data_buffer, double[] timestamp_buffer, double timeout) throws Exception 
	{
		int nSamples = 0;
		
		if( this.datGiver != null && !this.datGiver.getState().equals( State.NEW ) && !this.datGiver.getState().equals( State.TERMINATED ) )
		{
			StreamDataType type = StreamDataType.double64;		
			byte[] dat = this.createByteArray( simpleStream.getDataTypeBytes( type ) * data_buffer.length );
			
			nSamples = this.pull_chunk( dat, timestamp_buffer, timeout );		
			
			Number[] ns = ConvertTo.Transform.ByteArray2ArrayOf( dat, type );
			nSamples = ns.length;
			for( int i = 0; i < ns.length && i < data_buffer.length; i++ )
			{
				data_buffer[ i ] = ns[ i ].doubleValue();
			}
		}
		
		return nSamples;
	}

	@Override
	public int pull_chunk(double[] data_buffer, double[] timestamp_buffer) throws Exception 
	{
		return this.pull_chunk( data_buffer, timestamp_buffer, TIME_FOREVER );
	}

	@Override
	public double pull_sample(long[] sample, double timeout) throws Exception 
	{
		double time = 0;
		
		if( this.datGiver != null && !this.datGiver.getState().equals( State.NEW ) && !this.datGiver.getState().equals( State.TERMINATED ) )
		{		
			StreamDataType type = StreamDataType.int64;		
			byte[] dat = this.createByteArray( simpleStream.getDataTypeBytes( type ) * sample.length );
			
			time = this.pull_sample( dat, timeout );
			
			Number[] ns = ConvertTo.Transform.ByteArray2ArrayOf( dat, type );
			for( int i = 0; i < ns.length && i < sample.length; i++ )
			{
				sample[ i ] = ns[ i ].longValue();
			}
		}
		
		return time;
	}

	@Override
	public double pull_sample(long[] sample) throws Exception 
	{
		return this.pull_sample( sample, TIME_FOREVER );
	}

	@Override
	public int pull_chunk(long[] data_buffer, double[] timestamp_buffer, double timeout) throws Exception 
	{
		int nSamples = 0;
		
		if( this.datGiver != null && !this.datGiver.getState().equals( State.NEW ) && !this.datGiver.getState().equals( State.TERMINATED ) )
		{
			StreamDataType type = StreamDataType.int64;		
			byte[] dat = this.createByteArray( simpleStream.getDataTypeBytes( type ) * data_buffer.length );
			
			nSamples = this.pull_chunk( dat, timestamp_buffer, timeout );		
			
			Number[] ns = ConvertTo.Transform.ByteArray2ArrayOf( dat, type );
			nSamples = ns.length;
			for( int i = 0; i < ns.length && i < data_buffer.length; i++ )
			{
				data_buffer[ i ] = ns[ i ].longValue();
			}
		}
		
		return nSamples;
	}

	@Override
	public int pull_chunk(long[] data_buffer, double[] timestamp_buffer) throws Exception 
	{
		return this.pull_chunk( data_buffer, timestamp_buffer, TIME_FOREVER );
	}
	
	@Override
	public double pull_sample(int[] sample, double timeout) throws Exception 
	{
		double time = 0D;
		
		if( this.datGiver != null && !this.datGiver.getState().equals( State.NEW ) && !this.datGiver.getState().equals( State.TERMINATED ) )
		{
			StreamDataType type = StreamDataType.int32;		
			byte[] dat = this.createByteArray( simpleStream.getDataTypeBytes( type ) * sample.length );
			
			time = this.pull_sample( dat, timeout );
			
			Number[] ns = ConvertTo.Transform.ByteArray2ArrayOf( dat, type );
			for( int i = 0; i < ns.length && i < sample.length; i++ )
			{
				sample[ i ] = ns[ i ].intValue();
			}
		}
		
		return time;
	}

	@Override
	public double pull_sample(int[] sample) throws Exception 
	{
		return this.pull_sample( sample, TIME_FOREVER );
	}

	@Override
	public int pull_chunk(int[] data_buffer, double[] timestamp_buffer, double timeout) throws Exception 
	{
		int nSamples = 0;
		
		if( this.datGiver != null && !this.datGiver.getState().equals( State.NEW ) && !this.datGiver.getState().equals( State.TERMINATED ) )
		{
			StreamDataType type = StreamDataType.int32;		
			byte[] dat = this.createByteArray( simpleStream.getDataTypeBytes( type ) * data_buffer.length );
			
			nSamples = this.pull_chunk( dat, timestamp_buffer, timeout );		
			
			Number[] ns = ConvertTo.Transform.ByteArray2ArrayOf( dat, type );
			nSamples = ns.length;
			for( int i = 0; i < ns.length && i < data_buffer.length; i++ )
			{
				data_buffer[ i ] = ns[ i ].intValue();
			}
		}
		
		return nSamples;
	}

	@Override
	public int pull_chunk(int[] data_buffer, double[] timestamp_buffer) throws Exception 
	{
		return this.pull_chunk( data_buffer, timestamp_buffer, TIME_FOREVER );
	}
	
	@Override
	public double pull_sample(short[] sample, double timeout) throws Exception 
	{
		double time = 0D;
		
		if( this.datGiver != null && !this.datGiver.getState().equals( State.NEW ) && !this.datGiver.getState().equals( State.TERMINATED ) )
		{
			StreamDataType type = StreamDataType.int16;		
			byte[] dat = this.createByteArray( simpleStream.getDataTypeBytes( type ) * sample.length );
			
			time = this.pull_sample( dat, timeout );
			
			Number[] ns = ConvertTo.Transform.ByteArray2ArrayOf( dat, type );
			for( int i = 0; i < ns.length && i < sample.length; i++ )
			{
				sample[ i ] = ns[ i ].shortValue();
			}
		}
		
		return time;
	}

	@Override
	public double pull_sample(short[] sample) throws Exception 
	{
		return this.pull_sample( sample, TIME_FOREVER );
	}
	
	@Override
	public int pull_chunk(short[] data_buffer, double[] timestamp_buffer, double timeout) throws Exception 
	{
		int nSamples = 0;
		
		if( this.datGiver != null && !this.datGiver.getState().equals( State.NEW ) && !this.datGiver.getState().equals( State.TERMINATED ) )
		{
			StreamDataType type = StreamDataType.int16;		
			byte[] dat = this.createByteArray( simpleStream.getDataTypeBytes( type ) * data_buffer.length );
			
			nSamples = this.pull_chunk( dat, timestamp_buffer, timeout );		
			
			Number[] ns = ConvertTo.Transform.ByteArray2ArrayOf( dat, type );
			nSamples = ns.length;
			for( int i = 0; i < ns.length && i < data_buffer.length; i++ )
			{
				data_buffer[ i ] = ns[ i ].shortValue();
			}
		}
		
		return nSamples;
	}

	@Override
	public int pull_chunk(short[] data_buffer, double[] timestamp_buffer) throws Exception 
	{
		return this.pull_chunk( data_buffer, timestamp_buffer, TIME_FOREVER );
	}

	@Override
	public double pull_sample(String[] sample, double timeout) throws Exception 
	{
		double[] time = new double[] { 0D };
		
		this.pull_chunk( sample, time );
				
		return time[ 0 ];
	}

	@Override
	public double pull_sample(String[] sample) throws Exception 
	{
		return this.pull_sample( sample, TIME_FOREVER );
	}

	@Override
	public int pull_chunk(String[] data_buffer, double[] timestamp_buffer, double timeout) throws Exception 
	{
		int nSamples = 0;
		
		if( this.datGiver != null && !this.datGiver.getState().equals( State.NEW ) && !this.datGiver.getState().equals( State.TERMINATED ) )
		{
			this.startTimer( timeout, this.getTimeoutReadAction() );
			
			nSamples = this.datGiver.getSamples( data_buffer, timestamp_buffer );
						
			this.stopTimer();
		}
		
		return nSamples;
	}

	@Override
	public int pull_chunk(String[] data_buffer, double[] timestamp_buffer) throws Exception 
	{
		return this.pull_chunk( data_buffer, timestamp_buffer, TIME_FOREVER );
	}	
}
