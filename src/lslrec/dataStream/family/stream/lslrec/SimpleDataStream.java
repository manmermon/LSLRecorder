/**
 * 
 */
package lslrec.dataStream.family.stream.lslrec;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.concurrent.Semaphore;

import javax.swing.Timer;

import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting.StreamLibrary;
import lslrec.dataStream.family.setting.MutableStreamSetting;
import lslrec.dataStream.family.setting.SimpleStreamSetting;
import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;
import lslrec.dataStream.family.stream.IObjectStream;

/**
 * @author Manuel Merino Monge
 *
 */
public class SimpleDataStream< T > implements IObjectStream< T >
{	
	private MutableStreamSetting simpleStream = null;
	
	private Semaphore lockSem = new Semaphore( 1, true );
	
	private Timer timer = null;
	
	private DataStreamGiver< T > datGiver = null;
	
	/**
	 * 
	 */
	public SimpleDataStream( String name, StreamDataType dataType, double samplingRate
							, int channels, int chunkSize, DataStreamGiver< T > giver ) 
	{
		SimpleStreamSetting sss = new SimpleStreamSetting( StreamLibrary.LSL
															, name
															, dataType
															, StreamDataType.double64
															, StreamDataType.int64
															, channels
															, samplingRate
															, ""
															, ""
															, ""
															, new HashMap< String, String >()
															, chunkSize
															);
		
		if( giver == null )
		{
			throw new NullPointerException( "DataStreamGiver null." );
		}
		
		this.datGiver = giver;
		
		this.simpleStream = new MutableStreamSetting( sss );
		
		this.lockSem.drainPermits();
	}

	@Override
	public void close() 
	{		
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
	}

	@Override
	public void open_stream() throws Exception 
	{
	}
	
	@Override
	public void close_stream() 
	{
	}

	@Override
	public double time_correction(double timeout) throws Exception 
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

	private void startTimer( double timeout )
	{
		if( this.timer != null )
		{
			this.timer.stop();			
		}
		
		int delay = (int)Math.round( 1000 * timeout );
		
		this.timer = new Timer( delay, this.getTimeoutReadAction() );
		
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
	
	private ActionListener getTimeoutReadAction( )
	{
		return new ActionListener() 
				{			
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						datGiver.release();
					}
				};
	}
	
	@Override
	public double pull_sample( T[] sample, double timeout) throws Exception 
	{
		double[] time = new double[] { 0D };
	
		this.pull_chunk( sample, time );
				
		return time[ 0 ];
	}

	@Override
	public double pull_sample(T[] sample) throws Exception 
	{	
		return this.pull_sample( sample, TIME_FOREVER );
	}

	@Override
	public int pull_chunk( T[] data_buffer, double[] timestamp_buffer, double timeout) throws Exception 
	{
		this.startTimer( timeout );
		
		int nSamples = 0;
		
		nSamples = this.datGiver.getSamples( data_buffer, timestamp_buffer );
		
		this.stopTimer();
		
		return nSamples;
	}

	@Override
	public int pull_chunk( T[] data_buffer, double[] timestamp_buffer) throws Exception 
	{
		return this.pull_chunk( data_buffer, timestamp_buffer, TIME_FOREVER );
	}
}
