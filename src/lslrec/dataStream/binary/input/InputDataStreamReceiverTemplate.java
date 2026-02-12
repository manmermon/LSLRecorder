/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
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

package lslrec.dataStream.binary.input;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.auxiliar.task.ITaskIdentity;
import lslrec.exceptions.LostException;
import lslrec.exceptions.ReadInputDataException;
import lslrec.exceptions.SettingException;
import lslrec.exceptions.UnsupportedTypeException;
import lslrec.dataStream.family.DataStreamFactory;
import lslrec.dataStream.family.setting.IMutableStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.MutableStreamSetting;
import lslrec.dataStream.family.setting.StreamExtraLabels;
import lslrec.dataStream.family.stream.IDataStream;
import lslrec.dataStream.tools.StreamUtils;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;
import lslrec.control.message.EventInfo;
import lslrec.control.message.EventType;
import lslrec.control.notification.NotificationTask;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.ArrayUtils;

public abstract class InputDataStreamReceiverTemplate extends AbstractStoppableThread implements ITaskIdentity //, IMonitoredTask //, ITimerMonitor, INotificationTask
{
	private IDataStream inLet = null;

	//private Timer noDataCheckerTimer = null;
	//private Timer reconnectionTimer = null;

	private double blockTimer = 0.0D;
	
	private byte[] byteData;

	private short[] shortData;

	private int[] intData;

	private float[] floatData;
	private double[] doubleData;
	private long[] longData;
	private String[] stringData;

	private List tempSampleBytes;
	private List< Double > tempTimeMark;
	
	private Double timeCorrection = null;
	private double[] timeMark;	
	
	protected NotificationTask notifTask = null;
	
	protected int chunckLength = 1;
	protected int arrayLen = 1; 
	
	private AtomicBoolean isStreamClosed = new AtomicBoolean( false );
	private AtomicBoolean isRecording = new AtomicBoolean( false );
	
	private AtomicBoolean postCleanDone = new AtomicBoolean( false );

	protected IStreamSetting streamSetting = null;
	
	private Object syncTimeCorrection = new Object();
	
	private Long numberReceivedDataBlock = 0L;
			
	public InputDataStreamReceiverTemplate( IStreamSetting lslCfg ) throws Exception
	{		
		if( lslCfg == null )
		{
			throw new IllegalArgumentException( "Stream setting null" );
		}
		
		this.streamSetting = lslCfg;
				
		this.chunckLength = this.streamSetting.getChunkSize();
		if( this.chunckLength < 1 )
		{
			this.chunckLength = 1;
		}
		
		if( this.streamSetting instanceof MutableStreamSetting )
		{
			((IMutableStreamSetting)this.streamSetting).setDescription( StreamUtils.getDeepXmlStreamDescription( ((MutableStreamSetting)this.streamSetting ).getStreamSetting() ) );
		}
		else if( this.streamSetting instanceof IMutableStreamSetting )
		{
			((IMutableStreamSetting)this.streamSetting).setDescription( StreamUtils.getDeepXmlStreamDescription( this.streamSetting ) );
		}
		
		this.createArrays();
		
		//this.createTimers();
		double samplingRate = this.streamSetting.sampling_rate();
		
		this.blockTimer = 0.5D; // 0.5 s
		
		if ( samplingRate != IStreamSetting.IRREGULAR_RATE )
		{
			this.blockTimer = 2 / samplingRate; // 2 times the period time  
		}
		
		this.inLet = DataStreamFactory.createDataStream( this.streamSetting );
		
		if( this.inLet == null)
		{
			throw new SettingException( "Unsupported Library.");
		}
						
		//*
		Thread th = new Thread()
		{
			@Override
			public void run() 
			{
				synchronized ( syncTimeCorrection )
				{
					try
					{	
						timeCorrection = inLet.time_correction( );
					}
					catch( Exception | Error e )
					{
						timeCorrection =  0D;
					}
				}
			}
		};
		
		th.setName( super.getName() + "-timeCorrection" );
		th.start();
		
		synchronized ( this )
		{
			super.wait( 50L );
		}
				
		//*/
				
		// Avoid unnecessary buffering data, waste unnecessary system, and network resources.
		inLet.close_stream();
	}

	protected int createArrayData( ) throws Exception
	{
		int nBytes = 1;
		
		this.arrayLen = this.streamSetting.channel_count() *  this.chunckLength;
		
		switch ( this.streamSetting.data_type() )
		{
			case int8:
			{
				this.byteData = new byte[ this.arrayLen ];			
				break;
	
			}
			case int16:
			{
				nBytes = Short.BYTES;
	
				this.shortData = new short[ this.arrayLen ];
				break;
			}
			case int32:
			{
				nBytes = Integer.BYTES;
	
				this.intData = new int[ this.arrayLen ];
				break;
			}
			case int64:
			{
				nBytes = Long.BYTES;
	
				this.longData = new long[ this.arrayLen ];
				break;
			}	
			case float32:
			{
				nBytes = Float.BYTES;
	
				this.floatData = new float[ this.arrayLen ];
				break;
			}
			case double64:
			{
				nBytes = Double.BYTES;
	
				this.doubleData = new double[ this.arrayLen ];
				break;
			}
			case string:
			{
				nBytes = Character.BYTES;
	
				this.stringData = new String[ this.arrayLen ];
				break;
			}
			default:
			{
				String msg = "Data type (" + this.streamSetting.data_type()  + ") of stream input " + this.streamSetting.name() + " unsupported.";
				throw new UnsupportedTypeException( msg );
			}
		}

		this.timeMark = new double[ this.chunckLength ];

		return nBytes;
	}
	
	public boolean isReadyToStart()
	{
		synchronized ( this.syncTimeCorrection )
		{
			return this.timeCorrection != null;
		}
	}
		
	@Override
	protected void preStart() throws Exception
	{
		synchronized ( this.syncTimeCorrection )
		{
			super.preStart(); 
		}
		
		synchronized ( this.isRecording )
		{
			this.isRecording.set( true );
		}
		
		/*
		synchronized ( this )
		{
			startMonitor();
		}
		//*/
		
		/*
		if( this.timer != null )
		{			
			this.timer.startThread();
		}
		*/
	}
		
	private void createArrays() throws Exception 
	{
		this.createArrayData();
		
		this.tempSampleBytes = new ArrayList();
		this.tempTimeMark = new ArrayList< Double >();
	}
	
	/*
	private void createTimers()
	{
		double samplingRate = this.streamSetting.sampling_rate();
		
		this.blockTimer = 0.5D; // 0.5 s
		
		if ( samplingRate != IStreamSetting.IRREGULAR_RATE )
		{
			this.blockTimer = 1.5 / samplingRate; // 1.5 times the period time  
		}
		
		int recordingCheckerTimer = this.streamSetting.getRecordingCheckerTimer();
		
		if(  recordingCheckerTimer > 0 &&  this.streamSetting.isEnableRecordingCheckerTimer() )
		{
			if ( samplingRate != IStreamSetting.IRREGULAR_RATE )
			{
				this.blockTimer = 1.5 / samplingRate; // 1.5 times the period time  
				
				int time = (int)( recordingCheckerTimer * 1000.0D / samplingRate) ;
				if ( time < 3000 )
				{
					time = 3000; // 3000 milliseconds
				}
							
				if( time > 0 )
				{								
					this.noDataCheckerTimer = new Timer( time, false, new ActionTimerThread( new IAction() 
					{						
						@Override
						public void execute() 
						{					
							IStreamSetting[] iss = DataStreamFactory.getStreamSetting( streamSetting.getLibraryID()
									, streamSetting.name()
									, streamSetting.content_type()
									, 1.0D );

							boolean isConnectedStream = ( iss != null ) && ( iss.length > 0 );

							if( isConnectedStream || !streamSetting.reconnectLostStream() )
							{
								timeOver();
							}
							else if( reconnectionTimer != null )
							{
								if( notifTask != null )
								{	
									EventInfo event = new EventInfo( getID(), EventType.WARNING, "Reconnecting the lost stream: " + streamSetting.name() );
									
									notifTask.queueAndSendEvent( event );
								}

								try 
								{
									reconnectionTimer.restartTimer();
								}
								catch (Exception e) 
								{
									EventInfo event = new EventInfo( getID(), EventType.WARNING, "Reconnection Timer exection: " + e.getMessage() );
									
									notifTask.queueAndSendEvent( event );
								}
							}
							else
							{
								if( notifTask != null )
								{	
									EventInfo event = new EventInfo( getID(), EventType.WARNING, "Stream " + streamSetting.name() + " is lost. Waiting to reconnect..." );

									
									notifTask.queueAndSendEvent( event );
								}
							}
						}
					}));
				}		
			}
			else if( !this.streamSetting.isSynchronationStream() )
			{
				int time = recordingCheckerTimer * 1000; // seconds
				String msg = "No data received " + String.format(Locale.getDefault(), "%d", time/1000 ) + "s from " + streamSetting.name() + " (irregular sampling rate).";
				
				this.noDataCheckerTimer = new Timer( time, false, new ActionTimerThread( new IAction() 
				{					
					@Override
					public void execute() 
					{
						if( notifTask != null )
						{	
							EventInfo event = new EventInfo( getID(), EventType.WARNING, msg );
														
							notifTask.queueAndSendEvent( event );
						}
						
						IStreamSetting[] iss = DataStreamFactory.getStreamSetting( streamSetting.getLibraryID()
								, streamSetting.name()
								, streamSetting.content_type()
								, 1.0D );

						boolean isConnectedStream = ( iss != null ) && ( iss.length > 0 );
						
						if( !isConnectedStream && streamSetting.reconnectLostStream() )
						{
							String msg = "Stream " + streamSetting.name() + " is lost. Waiting to reconnect...";
							if( reconnectionTimer != null )
							{
								msg = "Reconnect the lost stream: " + streamSetting.name();
							}
							
							if( notifTask != null )
							{	
								EventInfo event = new EventInfo( getID(), EventType.WARNING, msg );
																
								notifTask.queueAndSendEvent( event );
							}
							
							if( reconnectionTimer != null )
							{
								try 
								{
									reconnectionTimer.restartTimer();
								}
								catch (Exception e) 
								{
									EventInfo event = new EventInfo( getID(), EventType.WARNING, "Reconnection Timer exection: " + e.getMessage() );
									
									notifTask.queueAndSendEvent( event );
								}
							}
						}
					}
				}) );
			}
		}
		
		double reconnectTimer = this.streamSetting.reconnectionWaitingTime();
		
		if( reconnectTimer > 0 )
		{
			int reconnectTime = (int)Math.round(reconnectTimer * 1000 );
			reconnectTime = ( reconnectTime < 100 ) ? 100 : reconnectTime;
			
			this.reconnectionTimer = new Timer( reconnectTime, false, new ActionTimerThread( new IAction() 
			{				
				@Override
				public void execute() 
				{
					IStreamSetting[] iss = DataStreamFactory.getStreamSetting( streamSetting.getLibraryID()
							, streamSetting.name()
							, streamSetting.content_type()
							, 1.0D );

					boolean isConnectedStream = ( iss != null ) && ( iss.length > 0 );
					
					if( !isConnectedStream )
					{
						timeOver();
					}
				}
			}));
			
			this.reconnectionTimer.setName( "reconnectionTimer-" + this.getId() );
		}
	}
	//*/
	
	protected void startUp() throws Exception
	{
		super.startUp();

		/*
		if( this.noDataCheckerTimer != null )
		{
			this.noDataCheckerTimer.setName( "noDataCheckerTimer-" + this.getId() );
			this.noDataCheckerTimer.startThread();
		}
		//*/
		
		synchronized ( this.isStreamClosed )
		{
			this.isStreamClosed.set( false );
			this.inLet.open_stream();
		}
	}

	protected void preStopThread(int friendliness) throws Exception
	{		
		if( friendliness == IStoppableThread.FORCE_STOP 
				|| ( this.streamSetting.sampling_rate() == IStreamSetting.IRREGULAR_RATE ))
		{
			/*
			if( this.noDataCheckerTimer != null )
			{
				this.noDataCheckerTimer.stopThread( IStoppableThread.FORCE_STOP );
			}
			
			if( this.reconnectionTimer != null )
			{
				this.reconnectionTimer.stopThread( IStoppableThread.FORCE_STOP );
			}
			//*/
			
			synchronized ( this.isStreamClosed ) 
			{
				this.isStreamClosed.set( true );
				try
				{
					this.inLet.close_stream(); 				
				}
				catch ( Error e)
				{
				}
			}
		}
	}
	
	protected void postStopThread(int friendliness) throws Exception
	{		
	}

	protected void runInLoop() throws Exception
	{	
		byte[] data = this.readData();
		
		if( data != null )
		{				
			/*
			if (this.noDataCheckerTimer != null)
			{
				//this.noDataCheckerTimer.stop();
				this.noDataCheckerTimer.stopTimer();
			}
			
			if( this.reconnectionTimer != null )
			{
				//this.reconnectionTimer.stop();
				this.reconnectionTimer.stopTimer();
			}
			//*/
						
			this.managerData( data, ConvertTo.Transform.doubleArray2byteArray( this.timeMark ) );
			
			/*
			if (this.noDataCheckerTimer != null)
			{
				this.noDataCheckerTimer.restartTimer();
			}
			//*/
			
			synchronized( this.numberReceivedDataBlock )
			{
				this.numberReceivedDataBlock++;
			}
		}
		/*
		else if( this.reconnectionTimer != null && !this.reconnectionTimer.isRunning() )
		{
			this.reconnectionTimer.restartTimer();
		}
		//*/
	}
	
	public long getNumberReceivedDataBlocks()
	{
		synchronized( this.numberReceivedDataBlock )
		{
			return this.numberReceivedDataBlock;
		}
	}
	
	public IStreamSetting getStreamSetting()
	{
		return this.streamSetting;
	}
	
	@Override
	protected void finallyManager() 
	{	
		super.finallyManager();
		
		synchronized ( this.isStreamClosed )
		{
			if( this.isStreamClosed.get() && this.isRecording.get() )
			{
				super.stopThread = true;
			}
		}
	}
	
	private byte[] readData() throws Exception
	{
		byte[] out = null;
		ByteBuffer data = null;
				
		double timestamp_buffer[] = new double[ this.chunckLength ];
		int nReadData = 0;
		
		switch (this.streamSetting.data_type() )
		{
			case int8:
			{				
				nReadData = this.inLet.pull_chunk( this.byteData, timestamp_buffer, this.blockTimer );
				
				if( nReadData > 0 )
				{
					int i = 0;					
					while( i < nReadData  
							&& this.tempSampleBytes.size() < this.arrayLen )
					{
						this.tempSampleBytes.add( this.byteData[ i ] );						
						i++;
					}
					
					int j = 0;
					while( j < timestamp_buffer.length 
							&& timestamp_buffer[ j ] > 0.0D 
							&& this.tempTimeMark.size() < this.chunckLength )
					{
						this.tempTimeMark.add( timestamp_buffer[ j ] );
						j++;
					}
					
					if( this.tempSampleBytes.size() >= this.arrayLen )
					{
						out = new byte[ this.tempSampleBytes.size() ];						
						for( int iS = 0; iS < this.tempSampleBytes.size(); iS++ )
						{
							out[ iS ] = (Byte)this.tempSampleBytes.get( iS );							
						}
						
						for( int iS = 0; iS < this.tempTimeMark.size() && iS < this.timeMark.length; iS++ )
						{
							this.timeMark[ iS ] = (Double)this.tempTimeMark.get( iS ) + this.timeCorrection;
						}
						
						this.tempSampleBytes.clear();
						this.tempTimeMark.clear();
					}
					
					while( i < nReadData 
							&& this.tempSampleBytes.size() < this.arrayLen )
					{
						this.tempSampleBytes.add( this.byteData[ i ] );
						i++;
					}
					
					while( j < timestamp_buffer.length 
							&& timestamp_buffer[ j ] > 0.0D )
					{
						this.tempTimeMark.add( timestamp_buffer[ j ] );
						j++;
					}
				}
	
				break;
			}
			case int16:
			{
				nReadData = this.inLet.pull_chunk( this.shortData, timestamp_buffer, this.blockTimer );
				
				if( nReadData > 0 )
				{
					int i = 0;					
					while( i < nReadData  
							&& this.tempSampleBytes.size() < this.arrayLen )
					{
						this.tempSampleBytes.add( this.shortData[ i ] );
						i++;
					}
					
					int j = 0;
					while( j < timestamp_buffer.length 
							&& timestamp_buffer[ j ] > 0.0D 
							&& this.tempTimeMark.size() < this.chunckLength )
					{
						this.tempTimeMark.add( timestamp_buffer[ j ] );
						j++;
					}
					
					if( this.tempSampleBytes.size() >= this.arrayLen )
					{			
						short[] aux = new short[ this.tempSampleBytes.size() ];
						for( int iS = 0; iS < this.tempSampleBytes.size(); iS++ )
						{
							aux[ iS ] = (Short)this.tempSampleBytes.get( iS );
						}						
						
						int nBytes = this.tempSampleBytes.size() * Short.BYTES;						
						out = new byte[ nBytes ];
						
						data = ByteBuffer.wrap( out );
						ShortBuffer fBuf = data.asShortBuffer();
						fBuf.put( aux );
						
						for( int iS = 0; iS < this.tempTimeMark.size() && iS < this.timeMark.length; iS++ )
						{
							this.timeMark[ iS ] = (Double)this.tempTimeMark.get( iS ) + this.timeCorrection;
						}
						
						this.tempSampleBytes.clear();
						this.tempTimeMark.clear();
					}
					
					while( i < nReadData 
							&& this.tempSampleBytes.size() < this.arrayLen )
					{
						this.tempSampleBytes.add( this.shortData[ i ] );
						i++;
					}					
					
					while( j < timestamp_buffer.length 
							&& timestamp_buffer[ j ] > 0.0D  )
					{
						this.tempTimeMark.add( timestamp_buffer[ j ] );
						j++;
					}
				}
	
				break;
			}
			case int32:
			{					
				nReadData = this.inLet.pull_chunk( this.intData, timestamp_buffer, this.blockTimer );
				
				if( nReadData > 0 )
				{					
					int i = 0;					
					while( i < nReadData 
							&& this.tempSampleBytes.size() < this.arrayLen )
					{
						this.tempSampleBytes.add( this.intData[ i ] );
						i++;
					}
					
					int j = 0;
					while( j < timestamp_buffer.length 
							&& timestamp_buffer[ j ] > 0.0D 
							&& this.tempTimeMark.size() < this.chunckLength )
					{
						this.tempTimeMark.add( timestamp_buffer[ j ] );
						j++;
					}
					
					if( this.tempSampleBytes.size() >= this.arrayLen )
					{			
						int[] aux = new int[ this.tempSampleBytes.size() ];
						for( int iS = 0; iS < this.tempSampleBytes.size(); iS++ )
						{
							aux[ iS ] = (Integer)this.tempSampleBytes.get( iS );
						}						
						
						for( int iS = 0; iS < this.tempTimeMark.size() && iS < this.timeMark.length; iS++ )
						{
							this.timeMark[ iS ] = (Double)this.tempTimeMark.get( iS ) + this.timeCorrection;
						}	
						
						int nBytes = this.tempSampleBytes.size() * Integer.BYTES;						
						out = new byte[ nBytes ];
						
						data = ByteBuffer.wrap(out);
						IntBuffer fBuf = data.asIntBuffer();
						fBuf.put( aux );
						
						this.tempSampleBytes.clear();
						this.tempTimeMark.clear();
					}
					
					while( i < nReadData 
							&& this.tempSampleBytes.size() < this.arrayLen )
					{
						this.tempSampleBytes.add( this.intData[ i ] );
						i++;
					}					
					
					while( j < timestamp_buffer.length 
							&& timestamp_buffer[ j ] > 0.0D )
					{
						this.tempTimeMark.add( timestamp_buffer[ j ] );
						j++;
					}
				}
	
				break;
			}
			case int64:
			{					
				nReadData = this.inLet.pull_chunk( this.longData, timestamp_buffer, this.blockTimer );
				
				if( nReadData > 0 )
				{					
					int i = 0;					
					while( i < nReadData 
							&& this.tempSampleBytes.size() < this.arrayLen )
					{
						this.tempSampleBytes.add( this.longData[ i ] );
						i++;
					}
					
					int j = 0;
					while( j < timestamp_buffer.length 
							&& timestamp_buffer[ j ] > 0.0D 
							&& this.tempTimeMark.size() < this.chunckLength )
					{
						this.tempTimeMark.add( timestamp_buffer[ j ] );
						j++;
					}
					
					if( this.tempSampleBytes.size() >= this.arrayLen )
					{			
						long[] aux = new long[ this.tempSampleBytes.size() ];
						for( int iS = 0; iS < this.tempSampleBytes.size(); iS++ )
						{
							aux[ iS ] = (Long)this.tempSampleBytes.get( iS );
						}						
						
						for( int iS = 0; iS < this.tempTimeMark.size() && iS < this.timeMark.length; iS++ )
						{
							this.timeMark[ iS ] = (Double)this.tempTimeMark.get( iS ) + this.timeCorrection;
						}	
						
						int nBytes = this.tempSampleBytes.size() * Long.BYTES;						
						out = new byte[ nBytes ];
						
						data = ByteBuffer.wrap(out);
						LongBuffer fBuf = data.asLongBuffer();
						fBuf.put( aux );
						
						this.tempSampleBytes.clear();
						this.tempTimeMark.clear();
					}
					
					while( i < nReadData 
							&& this.tempSampleBytes.size() < this.arrayLen )
					{
						this.tempSampleBytes.add( this.longData[ i ] );
						i++;
					}					
					
					while( j < timestamp_buffer.length 
							&& timestamp_buffer[ j ] > 0.0D )
					{
						this.tempTimeMark.add( timestamp_buffer[ j ] );
						j++;
					}
				}
	
				break;
			}
			case float32:
			{
				nReadData = this.inLet.pull_chunk( this.floatData, timestamp_buffer, this.blockTimer );
				
				if( nReadData > 0 )
				{	
					int i = 0;					
					while( i < nReadData 
							&& this.tempSampleBytes.size() < this.arrayLen )
					{
						this.tempSampleBytes.add( this.floatData[ i ] );
						i++;
					}
					
					int j = 0;
					while( j < timestamp_buffer.length 
							&& timestamp_buffer[ j ] > 0.0D 
							&& this.tempTimeMark.size() < this.chunckLength )
					{
						this.tempTimeMark.add( timestamp_buffer[ j ] );
						j++;
					}
					
					if( this.tempSampleBytes.size() >= this.arrayLen )
					{			
						float[] aux = new float[ this.tempSampleBytes.size() ];
						for( int iS = 0; iS < this.tempSampleBytes.size(); iS++ )
						{
							aux[ iS ] = (Float)this.tempSampleBytes.get( iS );
						}
						
						for( int iS = 0; iS < this.tempTimeMark.size() && iS < this.timeMark.length; iS++ )
						{
							this.timeMark[ iS ] = (Double)this.tempTimeMark.get( iS ) + this.timeCorrection;
						}
						
						int nBytes = this.tempSampleBytes.size() * Float.BYTES;						
						out = new byte[ nBytes ];
						
						data = ByteBuffer.wrap( out );
						FloatBuffer fBuf = data.asFloatBuffer();
						fBuf.put( aux );
						
						this.tempSampleBytes.clear();
						this.tempTimeMark.clear();
					}
					
					while( i < nReadData 
							&& this.tempSampleBytes.size() < this.arrayLen )
					{
						this.tempSampleBytes.add( this.floatData[ i ] );
						i++;
					}				
					
					while( j < timestamp_buffer.length && timestamp_buffer[ j ] > 0.0D )
					{
						this.tempTimeMark.add( timestamp_buffer[ j ] );
						j++;
					}
				}
	
				break;
			}
			case double64:
			{
				nReadData = this.inLet.pull_chunk( doubleData, timestamp_buffer, this.blockTimer  );
				
				if( nReadData > 0 )
				{	
					int i = 0;					
					while( i < nReadData 
							&& this.tempSampleBytes.size() < this.arrayLen )
					{
						this.tempSampleBytes.add( this.doubleData[ i ] );
						i++;
					}
					
					int j = 0;
					while( j < timestamp_buffer.length 
							&& timestamp_buffer[ j ] > 0.0D 
							&& this.tempTimeMark.size() < this.chunckLength )
					{
						this.tempTimeMark.add( timestamp_buffer[ j ] );
						j++;
					}
					
					if( this.tempSampleBytes.size() >= this.arrayLen )
					{			
						double[] aux = new double[ this.tempSampleBytes.size() ];
						for( int iS = 0; iS < this.tempSampleBytes.size(); iS++ )
						{
							aux[ iS ] = (Double)this.tempSampleBytes.get( iS );
						}	
						
						for( int iS = 0; iS < this.tempTimeMark.size() && iS < this.timeMark.length; iS++ )
						{
							this.timeMark[ iS ] = (Double)this.tempTimeMark.get( iS ) + this.timeCorrection;
						}	
						
						int nBytes = this.tempSampleBytes.size() * Double.BYTES;						
						out = new byte[ nBytes ];
						
						data = ByteBuffer.wrap( out );
						DoubleBuffer fBuf = data.asDoubleBuffer();
						fBuf.put( aux );
						
						this.tempSampleBytes.clear();
						this.tempTimeMark.clear();
					}
					
					while( i < nReadData 
							&& this.tempSampleBytes.size() < this.arrayLen )
					{
						this.tempSampleBytes.add( this.doubleData[ i ] );
						i++;
					}
					
					while( j < timestamp_buffer.length && timestamp_buffer[ j ] > 0.0D )
					{
						this.tempTimeMark.add( timestamp_buffer[ j ] );
						j++;
					}
				}
	
				break;
			}
			case string:
			{
				nReadData = this.inLet.pull_chunk( this.stringData, timestamp_buffer, this.blockTimer  );
				
				if( nReadData > 0 )
				{	
					int i = 0;					
					while( i < nReadData 
							&& this.tempSampleBytes.size() < this.arrayLen )
					{
						this.tempSampleBytes.add( this.stringData[ i ] );
						i++;
					}
					
					int j = 0;
					while( j < timestamp_buffer.length 
							&& timestamp_buffer[ j ] > 0.0D 
							&& this.tempTimeMark.size() < this.chunckLength )
					{
						this.tempTimeMark.add( timestamp_buffer[ j ] );
					}
					
					if( this.tempSampleBytes.size() >= this.arrayLen )
					{			
						StringBuilder txt = new StringBuilder();
						long[] strLeng = new long[ this.tempSampleBytes.size() ];
						for( int iS = 0; iS < this.tempSampleBytes.size(); iS++ )
						{	
							String str = this.tempSampleBytes.get( iS ).toString();
							
							txt.append( str );							
							strLeng[ iS ] = str.length();
						}	
						
						int nBytes = this.tempSampleBytes.size() * Long.BYTES;						
						out = new byte[ nBytes ];
						
						data = ByteBuffer.wrap( out );
						LongBuffer fBuf = data.asLongBuffer();
						fBuf.put( strLeng );
						
						out = ArrayUtils.addAll( out, txt.toString().getBytes() );												
						
						for( int iS = 0; iS < this.tempTimeMark.size() && iS < this.timeMark.length; iS++ )
						{							
							this.timeMark[ iS ] = (Double)this.tempTimeMark.get( iS ) + this.timeCorrection;
						}	
						
						this.tempSampleBytes.clear();
						this.tempTimeMark.clear();
					}
					
					while( i < nReadData 
							&& this.tempSampleBytes.size() < this.arrayLen )
					{
						this.tempSampleBytes.add( this.stringData[ i ] );
						i++;
					}		
					
					while( j < timestamp_buffer.length && timestamp_buffer[ j ] > 0.0D )
					{
						this.tempTimeMark.add( timestamp_buffer[ j ] );
						j++;
					}
				}
	
				break;
			}
			default:
			{
				throw new UnsupportedTypeException();
			}
		}
		
		return out;
	}
				
	protected void runExceptionManager( Throwable e )
	{
		boolean spread = true;
		
		synchronized ( this.isStreamClosed ) 
		{
			spread = (e instanceof LostException) && !this.isStreamClosed.get() ;		
		}
		
		if( !spread )
		{
			spread = ( !(e instanceof InterruptedException) && !(e instanceof LostException) )
						|| ( e instanceof Error );
		}
		
		if ( spread )
		{
			/*
			if( this.noDataCheckerTimer != null )
			{
				//this.noDataCheckerTimer.stop(); 
				this.noDataCheckerTimer.stopThread( IStoppableThread.FORCE_STOP );
			}
			//*/
			
			this.stopThread = true;
			
			String msg = e.getMessage();
			
			if( msg != null )
			{
				msg += " <" + super.getName() + ">";
			}
			
			Exception ex = new Exception( msg, e );
			
			String errMsg = "Timer is over. ";
			
			if( e instanceof Error ) 
			{
				errMsg = "Fatal Error. "; 
			}
			
			errMsg += "The stream " + super.getName() + " does not respond." ;
			
			ex.addSuppressed( new ReadInputDataException( errMsg ) );
			
			this.notifyProblem( ex );
		}
	}

	protected void cleanUp() throws Exception
	{
		super.cleanUp();
		
		/*
		if (this.noDataCheckerTimer != null)
		{
			this.noDataCheckerTimer.stopThread( IStoppableThread.FORCE_STOP );
		}		
		this.noDataCheckerTimer = null;
		//*/
		
		try
		{
			this.readRemainingData();
		}
		catch (Exception localException) {}
			
		synchronized ( this.isStreamClosed )
		{	
			if( !this.isStreamClosed.get() )
			{
				this.isStreamClosed.set( true );
				
				/*
				int delay = 3000; // 3 seconds
				
				this.noDataCheckerTimer = new Timer( delay, false, new ActionTimerThread( new IAction() 
				{					
					@Override
					public void execute() 
					{
						timeOver2();
					}
				}));
				
				this.noDataCheckerTimer.setName( "noDataCheckerTimer-" + this.getId() );
				this.noDataCheckerTimer.startThread();
				//*/
				
				this.inLet.close();
			}
		}
		
		synchronized ( this.postCleanDone ) 
		{
			/*
			if( this.noDataCheckerTimer != null )
			{
				//this.noDataCheckerTimer.stop();
				this.noDataCheckerTimer.stopThread( IStoppableThread.FORCE_STOP );
			}
			//*/
			
			if( !this.postCleanDone.get() )
			{
				this.postCleanDone.set( true );
				
				this.postCleanUp();
				
				//this.closeNotifierThread();
				
				if( this.notifTask != null )
				{
					synchronized ( this.notifTask )
					{
						this.notifTask.notify();
					}
				}
			}
		}		
	}
		
	private void readRemainingData() throws Exception
	{						
		double timestamp_buffer[] = new double[ this.chunckLength ];
		int nReadData = 0;		
		
		double timeout = 0.0D;
				
		boolean rep = true;
		int rerun = (int)this.streamSetting.sampling_rate() / 2;
		if( rerun < 10 )
		{
			rerun = 10;
		}		
		
		do
		{
			rerun--;
			
			ByteBuffer data = null;
			byte[] out = null;			
			
			switch ( this.streamSetting.data_type() )
			{
				case int8:
				{
					nReadData = this.inLet.pull_chunk( this.byteData, timestamp_buffer, timeout );
					
					if( nReadData > 0 )
					{
						int i = 0;					
						while( i < nReadData  
								&& this.tempSampleBytes.size() < this.arrayLen )
						{
							this.tempSampleBytes.add( this.byteData[ i ] );						
							i++;
						}
						
						int j = 0;
						while( j < timestamp_buffer.length 
								&& timestamp_buffer[ j ] > 0.0D 
								&& this.tempTimeMark.size() < this.chunckLength )
						{
							this.tempTimeMark.add( timestamp_buffer[ j ] );
							j++;
						}
						
						if( this.tempSampleBytes.size() >= this.arrayLen )
						{
							out = new byte[ this.tempSampleBytes.size() ];						
							for( int iS = 0; iS < this.tempSampleBytes.size(); iS++ )
							{
								out[ iS ] = (Byte)this.tempSampleBytes.get( iS );							
							}
							
							for( int iS = 0; iS < this.tempTimeMark.size(); iS++ )
							{
								this.timeMark[ iS ] = (Double)this.tempTimeMark.get( iS ) + this.timeCorrection;
							}
							
							this.tempSampleBytes.clear();
							this.tempTimeMark.clear();
						}
						
						while( i < nReadData 
								&& this.tempSampleBytes.size() < this.arrayLen )
						{
							this.tempSampleBytes.add( this.byteData[ i ] );
							i++;
						}
						
						while( j < timestamp_buffer.length 
								&& timestamp_buffer[ j ] > 0.0D )
						{
							this.tempTimeMark.add( timestamp_buffer[ j ] );
							j++;
						}
					}
		
					break;
				}
				case int16:
				{
					nReadData = this.inLet.pull_chunk( this.shortData, timestamp_buffer, timeout );
					
					if( nReadData > 0 )
					{
						int i = 0;					
						while( i < nReadData  
								&& this.tempSampleBytes.size() < this.arrayLen )
						{
							this.tempSampleBytes.add( this.shortData[ i ] );
							i++;
						}
						
						int j = 0;
						while( j < timestamp_buffer.length 
								&& timestamp_buffer[ j ] > 0.0D 
								&& this.tempTimeMark.size() < this.chunckLength )
						{
							this.tempTimeMark.add( timestamp_buffer[ j ] );
							j++;
						}
						
						if( this.tempSampleBytes.size() >= this.arrayLen )
						{			
							short[] aux = new short[ this.tempSampleBytes.size() ];
							for( int iS = 0; iS < this.tempSampleBytes.size(); iS++ )
							{
								aux[ iS ] = (Short)this.tempSampleBytes.get( iS );
							}						
							
							int nBytes = this.tempSampleBytes.size() * Short.BYTES;						
							out = new byte[ nBytes ];
							
							data = ByteBuffer.wrap( out );
							ShortBuffer fBuf = data.asShortBuffer();
							fBuf.put( aux );
							
							for( int iS = 0; iS < this.tempTimeMark.size(); iS++ )
							{
								this.timeMark[ iS ] = (Double)this.tempTimeMark.get( iS ) + this.timeCorrection;
							}
							
							this.tempSampleBytes.clear();
							this.tempTimeMark.clear();
						}
						
						while( i < nReadData 
								&& this.tempSampleBytes.size() < this.arrayLen )
						{
							this.tempSampleBytes.add( this.byteData[ i ] );
							i++;
						}					
						
						while( j < timestamp_buffer.length 
								&& timestamp_buffer[ j ] > 0.0D  )
						{
							this.tempTimeMark.add( timestamp_buffer[ j ] );
							j++;
						}
					}
		
					break;
				}
				case int32:
				{				
					nReadData = this.inLet.pull_chunk( this.intData, timestamp_buffer, timeout );
					
					if( nReadData > 0 )
					{
						int i = 0;					
						while( i < nReadData 
								&& this.tempSampleBytes.size() < this.arrayLen )
						{
							this.tempSampleBytes.add( this.intData[ i ] );
							i++;
						}
						
						int j = 0;
						while( j < timestamp_buffer.length 
								&& timestamp_buffer[ j ] > 0.0D 
								&& this.tempTimeMark.size() < this.chunckLength )
						{
							this.tempTimeMark.add( timestamp_buffer[ j ] );
							j++;
						}
						
						if( this.tempSampleBytes.size() >= this.arrayLen )
						{			
							int[] aux = new int[ this.tempSampleBytes.size() ];
							for( int iS = 0; iS < this.tempSampleBytes.size(); iS++ )
							{
								aux[ iS ] = (Integer)this.tempSampleBytes.get( iS );
							}						
							
							for( int iS = 0; iS < this.tempTimeMark.size(); iS++ )
							{
								this.timeMark[ iS ] = (Double)this.tempTimeMark.get( iS ) + this.timeCorrection;
							}	
							
							int nBytes = this.tempSampleBytes.size() * Integer.BYTES;						
							out = new byte[ nBytes ];
							
							data = ByteBuffer.wrap(out);
							IntBuffer fBuf = data.asIntBuffer();
							fBuf.put( aux );
							
							this.tempSampleBytes.clear();
							this.tempTimeMark.clear();
						}
						
						while( i < nReadData 
								&& this.tempSampleBytes.size() < this.arrayLen )
						{
							this.tempSampleBytes.add( this.byteData[ i ] );
							i++;
						}					
						
						while( j < timestamp_buffer.length 
								&& timestamp_buffer[ j ] > 0.0D )
						{
							this.tempTimeMark.add( timestamp_buffer[ j ] );
							j++;
						}
					}
		
					break;
				}
				case int64:
				{					
					nReadData = this.inLet.pull_chunk( this.longData, timestamp_buffer, this.blockTimer );
					
					if( nReadData > 0 )
					{					
						int i = 0;					
						while( i < nReadData 
								&& this.tempSampleBytes.size() < this.arrayLen )
						{
							this.tempSampleBytes.add( this.longData[ i ] );
							i++;
						}
						
						int j = 0;
						while( j < timestamp_buffer.length 
								&& timestamp_buffer[ j ] > 0.0D 
								&& this.tempTimeMark.size() < this.chunckLength )
						{
							this.tempTimeMark.add( timestamp_buffer[ j ] );
							j++;
						}
						
						if( this.tempSampleBytes.size() >= this.arrayLen )
						{			
							long[] aux = new long[ this.tempSampleBytes.size() ];
							for( int iS = 0; iS < this.tempSampleBytes.size(); iS++ )
							{
								aux[ iS ] = (Long)this.tempSampleBytes.get( iS );
							}						
							
							for( int iS = 0; iS < this.tempTimeMark.size() && iS < this.timeMark.length; iS++ )
							{
								this.timeMark[ iS ] = (Double)this.tempTimeMark.get( iS ) + this.timeCorrection;
							}	
							
							int nBytes = this.tempSampleBytes.size() * Long.BYTES;						
							out = new byte[ nBytes ];
							
							data = ByteBuffer.wrap(out);
							LongBuffer fBuf = data.asLongBuffer();
							fBuf.put( aux );
							
							this.tempSampleBytes.clear();
							this.tempTimeMark.clear();
						}
						
						while( i < nReadData 
								&& this.tempSampleBytes.size() < this.arrayLen )
						{
							this.tempSampleBytes.add( this.longData[ i ] );
							i++;
						}					
						
						while( j < timestamp_buffer.length 
								&& timestamp_buffer[ j ] > 0.0D )
						{
							this.tempTimeMark.add( timestamp_buffer[ j ] );
							j++;
						}
					}
		
					break;
				}
				case float32:
				{
					nReadData = this.inLet.pull_chunk( this.floatData, timestamp_buffer, timeout );
					
					if( nReadData > 0 )
					{						
						int i = 0;					
						while( i < nReadData 
								&& this.tempSampleBytes.size() < this.arrayLen )
						{
							this.tempSampleBytes.add( this.floatData[ i ] );
							i++;
						}
						
						int j = 0;
						while( j < timestamp_buffer.length 
								&& timestamp_buffer[ j ] > 0.0D 
								&& this.tempTimeMark.size() < this.chunckLength )
						{
							this.tempTimeMark.add( timestamp_buffer[ j ] );
							j++;
						}
						
						if( this.tempSampleBytes.size() >= this.arrayLen )
						{			
							float[] aux = new float[ this.tempSampleBytes.size() ];
							for( int iS = 0; iS < this.tempSampleBytes.size(); iS++ )
							{
								aux[ iS ] = (Float)this.tempSampleBytes.get( iS );
							}
							
							for( int iS = 0; iS < this.tempTimeMark.size(); iS++ )
							{
								this.timeMark[ iS ] = (Double)this.tempTimeMark.get( iS ) + this.timeCorrection;
							}
							
							int nBytes = this.tempSampleBytes.size() * Float.BYTES;						
							out = new byte[ nBytes ];
							
							data = ByteBuffer.wrap( out );
							FloatBuffer fBuf = data.asFloatBuffer();
							fBuf.put( aux );
							
							this.tempSampleBytes.clear();
							this.tempTimeMark.clear();
						}
						
						while( i < nReadData 
								&& this.tempSampleBytes.size() < this.arrayLen )
						{
							this.tempSampleBytes.add( this.floatData[ i ] );
							i++;
						}				
						
						while( j < timestamp_buffer.length && timestamp_buffer[ j ] > 0.0D )
						{
							this.tempTimeMark.add( timestamp_buffer[ j ] );
							j++;
						}
					}
		
					break;
				}
				case double64:
				{
					nReadData = this.inLet.pull_chunk( doubleData, timestamp_buffer, timeout );
					
					if( nReadData > 0 )
					{																			
						int i = 0;					
						while( i < nReadData 
								&& this.tempSampleBytes.size() < this.arrayLen )
						{
							this.tempSampleBytes.add( this.doubleData[ i ] );
							i++;
						}
						
						int j = 0;
						while( j < timestamp_buffer.length 
								&& timestamp_buffer[ j ] > 0.0D 
								&& this.tempTimeMark.size() < this.chunckLength )
						{
							this.tempTimeMark.add( timestamp_buffer[ j ] );
							j++;
						}
						
						if( this.tempSampleBytes.size() >= this.arrayLen )
						{			
							double[] aux = new double[ this.tempSampleBytes.size() ];
							for( int iS = 0; iS < this.tempSampleBytes.size(); iS++ )
							{
								aux[ iS ] = (Double)this.tempSampleBytes.get( iS );
							}	
							
							for( int iS = 0; iS < this.tempTimeMark.size(); iS++ )
							{
								this.timeMark[ iS ] = (Double)this.tempTimeMark.get( iS ) + this.timeCorrection;
							}	
							
							int nBytes = this.tempSampleBytes.size() * Double.BYTES;						
							out = new byte[ nBytes ];
							
							data = ByteBuffer.wrap( out );
							DoubleBuffer fBuf = data.asDoubleBuffer();
							fBuf.put( aux );
							
							this.tempSampleBytes.clear();
							this.tempTimeMark.clear();
						}
						
						while( i < nReadData 
								&& this.tempSampleBytes.size() < this.arrayLen )
						{
							this.tempSampleBytes.add( this.doubleData[ i ] );
							i++;
						}
						
						while( j < timestamp_buffer.length && timestamp_buffer[ j ] > 0.0D )
						{
							this.tempTimeMark.add( timestamp_buffer[ j ] );
							j++;
						}
					}
		
					break;
				}
				case string:
				{
					nReadData = this.inLet.pull_chunk( this.stringData, timestamp_buffer, timeout );
					//nChunck = nBytes / this.lslChannelCounts;
					
					if( nReadData > 0 )
					{
						//this.timeMark = timestamp_buffer[ 0 ];
						
						int i = 0;					
						while( i < nReadData 
								&& this.tempSampleBytes.size() < this.arrayLen )
						{
							this.tempSampleBytes.add( this.stringData[ i ] );
							i++;
						}
						
						int j = 0;
						while( j < timestamp_buffer.length 
								&& timestamp_buffer[ j ] > 0.0D 
								&& this.tempTimeMark.size() < this.chunckLength )
						{
							this.tempTimeMark.add( timestamp_buffer[ j ] );
						}
						
						if( this.tempSampleBytes.size() >= this.arrayLen )
						{			
							StringBuilder txt = new StringBuilder();
							long[] strLeng = new long[ this.tempSampleBytes.size() ];
							for( int iS = 0; iS < this.tempSampleBytes.size(); iS++ )
							{	
								String str = this.tempSampleBytes.get( iS ).toString();
								
								txt.append( str );							
								strLeng[ iS ] = str.length();
							}	
							
							int nBytes = this.tempSampleBytes.size() * Long.BYTES;						
							out = new byte[ nBytes ];
							
							data = ByteBuffer.wrap( out );
							LongBuffer fBuf = data.asLongBuffer();
							fBuf.put( strLeng );
							
							out = ArrayUtils.addAll( out, txt.toString().getBytes() );	;
													
							
							for( int iS = 0; iS < this.tempTimeMark.size(); iS++ )
							{							
								this.timeMark[ iS ] = (Double)this.tempTimeMark.get( iS ) + this.timeCorrection;
							}	
							
							this.tempSampleBytes.clear();
							this.tempTimeMark.clear();
						}
						
						while( i < nReadData 
								&& this.tempSampleBytes.size() < this.arrayLen )
						{
							this.tempSampleBytes.add( this.stringData[ i ] );
							i++;
						}		
						
						while( j < timestamp_buffer.length && timestamp_buffer[ j ] > 0.0D )
						{
							this.tempTimeMark.add( timestamp_buffer[ j ] );
							j++;
						}
					}
		
					break;
				}
				default:
				{
					throw new UnsupportedTypeException();
				}
			}
			
			if( out != null )
			{
				this.managerData( out, ConvertTo.Transform.doubleArray2byteArray( this.timeMark ) );
			}
			
			rep = !this.tempSampleBytes.isEmpty();
		}
		while( rep && rerun > 0 );
	}
		
	protected void notifyProblem(Exception e)
	{		
		String errorMsg = e.getMessage();
		if( errorMsg == null )
		{
			errorMsg = "";
		}
		
		if ( errorMsg.isEmpty() )
		{
			Throwable t = e.getCause();
			if (t != null)
			{
				errorMsg = errorMsg + t.toString();
			}

			if (errorMsg.isEmpty())
			{
				errorMsg = errorMsg + e.getLocalizedMessage();
			}
		}

		e.printStackTrace();
		
		/*
		if (this.monitor != null)
		{
			this.events.add( new EventInfo( this.getID(), EventType.PROBLEM, errorMsg ) );
			try
			{
				this.monitor.taskDone(this);
			}
			catch (Exception localException) 
			{
				
			}
			finally
			{
				this.stopThread = true;
			}
		}
		*/
		
		if( this.notifTask != null )
		{
			EventInfo ev = new EventInfo( this.getID(), EventType.PROBLEM, errorMsg ) ;
			
			/*
			this.notifTask.addEvent( ev );
			synchronized ( this.notifTask )
			{
				this.notifTask.notify();
			}
			//*/
			
			notifTask.queueAndSendEvent( ev );
		}
	}

	/*
	public void taskMonitor(ITaskMonitor m)
	{
		if( super.getState().equals( Thread.State.NEW ) && this.notifTask == null )
		{
			synchronized ( this )
			{
				this.monitor = m;
			}			
		}
	}
	//*/
	
	public void setNotificationTask( NotificationTask notif )
	{

		if( super.getState().equals( Thread.State.NEW ) )
		{
			synchronized ( this )
			{
				this.notifTask = notif;
			}			
		}
	}

	/*
	private void timeOver( )
	{	
		this.stopThread( IStoppableThread.FORCE_STOP );
		
		synchronized ( this.isStreamClosed )
		{
			if( !this.isStreamClosed.get() )
			{
				this.isStreamClosed.set( true );

				this.noDataCheckerTimer = new Timer( 3000, false, new ActionTimerThread( new IAction() 
				{					
					@Override
					public void execute() 
					{
						timeOver2();
					}
				}));
				
				try
				{
					this.noDataCheckerTimer.setName( "noDataCheckerTimer-" + this.getId() );
					this.noDataCheckerTimer.startThread();
				}
				catch (Exception e1) 
				{
					runExceptionManager( e1 );
				}
				finally 
				{
					notifyProblem( new DestroyFailedException( "The input stream " + streamSetting.name() 
																+ " is blocked. It is not possible released/closed. "
																+ "Quit " + ConfigApp.shortNameApp + " is recommended.") );
				}	
				
				this.inLet.close();
				
				this.noDataCheckerTimer.stopThread( IStoppableThread.FORCE_STOP );
			}
		}		
		
		this.notifyProblem( new TimeoutException( "Waiting time for input data from device <" + this.streamSetting.name() + "> was exceeded." ) );		
	}
	
	
	private void timeOver2( )
	{	
		super.interrupt(); // a new try to stop the thread
		
		this.noDataCheckerTimer = new Timer( 5000, false, new ActionTimerThread( new IAction() 
		{			
			@Override
			public void execute() 
			{	
				try 
				{
					synchronized ( postCleanDone ) 
					{
						if( !postCleanDone.get() )
						{
							postCleanDone.set( true );
							
							postCleanUp();
						}
					}					
				} 
				catch (Exception e1) 
				{
					runExceptionManager( e1 );
				}
				finally 
				{
					notifyProblem( new DestroyFailedException( "The input stream " + streamSetting.name() 
																+ " is blocked. It is not possible released/closed. "
																+ "Quit " + ConfigApp.shortNameApp + " is recommended.") );
				}
			}
		}));		
		
		try 
		{
			this.noDataCheckerTimer.setName( "noDataCheckerTimer-" + this.getId() );
			this.noDataCheckerTimer.startThread();
		}
		catch (Exception e1) 
		{
			runExceptionManager( e1 );
		}
		finally 
		{
			notifyProblem( new DestroyFailedException( "The input stream " + streamSetting.name() 
														+ " is blocked. It is not possible released/closed. "
														+ "Quit " + ConfigApp.shortNameApp + " is recommended.") );
		}	
	}
	//*/
	

	protected abstract void postCleanUp() throws Exception;

	protected abstract void managerData(byte[] dataArrayOfBytes, byte[] timeArrayOfBytes ) throws Exception;
	
}