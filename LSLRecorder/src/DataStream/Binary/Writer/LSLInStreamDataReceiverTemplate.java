/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec and CLIS.
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

package DataStream.Binary.Writer;

import Auxiliar.Extra.ConvertTo;
import Auxiliar.Tasks.IMonitoredTask;
import Auxiliar.Tasks.ITaskIdentity;
import Auxiliar.Tasks.ITaskMonitor;
import Auxiliar.Tasks.NotificationTask;
import Config.ConfigApp;
import Controls.Messages.EventInfo;
import Controls.Messages.RegisterSyncMessages;
import DataStream.StreamHeader;
import Exceptions.ReadInputDataException;
import Exceptions.SettingException;
import Controls.Messages.EventType;
import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLConfigParameters;
import edu.ucsd.sccn.LSL.StreamInfo;
import edu.ucsd.sccn.LSL.StreamInlet;
import edu.ucsd.sccn.LSLUtils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.management.ManagementFactory;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.activation.UnsupportedDataTypeException;
import javax.security.auth.DestroyFailedException;
import javax.swing.Timer;

import com.sun.management.OperatingSystemMXBean;

public abstract class LSLInStreamDataReceiverTemplate extends AbstractStoppableThread implements IMonitoredTask, ITaskIdentity //, ITimerMonitor, INotificationTask
{
	protected ITaskMonitor monitor = null;

	private LSL.StreamInlet inLet = null;

	private Timer timer = null;

	private double blockTimer = 0.0D;
	
	private byte[] byteData;

	private short[] shortData;

	private int[] intData;

	private float[] floatData;
	private double[] doubleData;
	//private long[] longData;
	private String[] stringData;

	private List tempSampleBytes;
	private List< Double > tempTimeMark;
	
	private double timeCorrection;
	private double[] timeMark;	
	protected int LSLFormatData = LSL.ChannelFormat.float32;

	//protected Semaphore syncMarkSem = null;

	protected String LSLName = "";

	protected String lslXML = "";

	protected int lslChannelCounts = 0;

	//protected List< EventInfo > events;
	protected NotificationTask notifTask = null;
	
	//protected int LSLFormatData = LSL.ChannelFormat.float32;
	
	protected int chunckLength = 1;
	
	protected boolean interleavedData = false;
	
	protected int timeType = LSL.ChannelFormat.double64;
	
	protected double samplingRate = LSL.IRREGULAR_RATE;
	
	private AtomicBoolean isStreamClosed = new AtomicBoolean( false );
	private AtomicBoolean isRecording = new AtomicBoolean( false );
	
	private AtomicBoolean postCleanDone = new AtomicBoolean( false );
		
	public LSLInStreamDataReceiverTemplate( LSL.StreamInfo info, LSLConfigParameters lslCfg ) throws Exception
	{
		if ( info == null )
		{
			throw new IllegalArgumentException("LSL.StreamInlet is null");
		}
		
		if( lslCfg == null )
		{
			throw new IllegalArgumentException( "LSL parameters is null" );
		}
			
		this.chunckLength = lslCfg.getChunckSize();
		this.interleavedData = lslCfg.isInterleavedData();
		
		this.lslChannelCounts = info.channel_count();
				
		this.LSLFormatData = info.channel_format();

		this.LSLName = info.name();
		
		this.samplingRate = info.nominal_srate();
		
		int nBytes = LSLUtils.getDataTypeBytes( this.LSLFormatData );
		
		long maxMem = Runtime.getRuntime().maxMemory() / 2;
		maxMem /= nBytes;
		
		int bufSize = 1000_000;
		
		if( this.samplingRate != LSL.IRREGULAR_RATE )
		{
			bufSize = 360; // 360 s
			
			if( bufSize * this.samplingRate * this.lslChannelCounts * this.chunckLength > maxMem )
			{
				bufSize = (int)( maxMem / ( this.samplingRate * this.lslChannelCounts * this.chunckLength ) ) ;
				
				if( bufSize < 1 )
				{
					throw new ReadInputDataException( "The maximum amount of data to LSL buffer is greater than available memory." );
				}
			}
				
			/*
			long freePhysicalMemorySize = ((OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean()).getFreePhysicalMemorySize();
			
			if( bufSize > freePhysicalMemorySize / nBytes )
			{
				throw new ReadInputDataException( "The maximum amount of data to LSL buffer is greater than available free physical memory." );
			}
			*/
		}
		
		//bufSize *= this.lslChannelCounts * this.chunckLength;
		
		
		int chunk = 0;
		
		if( this.chunckLength > 1 )
		{
			chunk = this.chunckLength;
		}
		
		this.inLet = new StreamInlet( info, bufSize, chunk, false );
		
		
		//info = str.info();
		
		//this.LSLFormatData = info.channel_format();
		

		Map< String, Integer > MARKS = RegisterSyncMessages.getSyncMessagesAndMarks();
		String markInfoText = "";
		for( String idMark : MARKS.keySet() )
		{
			Integer v = MARKS.get( idMark );
			markInfoText += idMark + "=" + v + StreamHeader.HEADER_BINARY_SEPARATOR;
		}		
					
		StreamInfo infoAux = this.inLet.info();
				
		String idLab = LSLConfigParameters.ID_SOCKET_MARK_INFO_LABEL;
		int countEq = LSLConfigParameters.existNodoName( this.inLet.info().desc().first_child(), idLab );		
		
		if( countEq > 0 )
		{
			idLab += "_" + countEq;
		}		
		infoAux.desc().remove_child( idLab );
		infoAux.desc().append_child_value( idLab, markInfoText );
		
		idLab = LSLConfigParameters.ID_GENERAL_DESCRIPTION_LABEL;
		countEq = LSLConfigParameters.existNodoName( this.inLet.info().desc().first_child(), idLab );
		
		if( countEq > 0 )
		{
			idLab += "_" + countEq;
		}			
		infoAux.desc().remove_child( idLab );
		infoAux.desc().append_child_value( idLab, ConfigApp.getProperty( ConfigApp.LSL_OUTPUT_FILE_DESCR ).toString() );
		
		idLab = LSLConfigParameters.ID_LSLREC_SETTING_LABEL;
		countEq = LSLConfigParameters.existNodoName( this.inLet.info().desc().first_child(), idLab );
		
		if( countEq > 0 )
		{
			idLab += "_" + countEq;
		}			
		infoAux.desc().remove_child( idLab );
		
		String setting = LSLConfigParameters.ID_CHUNKSIZE_LABEL + "=" + this.chunckLength + StreamHeader.HEADER_BINARY_SEPARATOR;
		setting += LSLConfigParameters.ID_INTERLEAVED_LABEL + "=" + this.interleavedData;
		infoAux.desc().append_child_value( idLab, setting );		
		
		infoAux.desc().remove_child( lslCfg.getExtraInfoLabel() );
		infoAux.desc().append_child_value( lslCfg.getExtraInfoLabel(), lslCfg.getAdditionalInfo() );
				
		this.lslXML = infoAux.as_xml();
				
		//this.events = new ArrayList< EventInfo >();
		
		try
		{	
			/*
			double waitTime = 3 / info.nominal_srate();
			
			if( waitTime < 3 )
			{
				waitTime = 3;
			}			
			
			this.timeCorrection = this.inLet.time_correction( waitTime );
			*/
			
			this.timeCorrection = this.inLet.time_correction( );
		}
		catch( Exception | Error e )
		{
			this.timeCorrection =  0D;
		}
		
		this.createArrays();
		
		// Avoid unnecessary buffering data, waste unnecessary system, and network resources.
		this.inLet.close_stream();
	}

	protected int createArrayData() throws Exception
	{
		int nBytes = 1;
		switch (this.LSLFormatData)
		{
			case( LSL.ChannelFormat.int8 ):
			{
				this.byteData = new byte[ this.inLet.info().channel_count() * this.chunckLength ];			
				break;
	
			}
			case( LSL.ChannelFormat.int16 ):
			{
				nBytes = Short.BYTES;
	
				this.shortData = new short[this.inLet.info().channel_count() * this.chunckLength ];
				break;
			}
			case( LSL.ChannelFormat.int32 ):
			{
				nBytes = Integer.BYTES;
	
				this.intData = new int[this.inLet.info().channel_count() * this.chunckLength ];
				break;
			}
			/*
			case( LSL.ChannelFormat.int64 ):
			{
				nBytes = Long.BYTES;
	
				this.longData = new long[ inLet.info().channel_count() ];
				break;
			}
			 */	
			case( LSL.ChannelFormat.float32 ):
			{
				nBytes = Float.BYTES;
	
				this.floatData = new float[ this.inLet.info().channel_count() * this.chunckLength ];
				break;
			}
			case( LSL.ChannelFormat.double64 ):
			{
				nBytes = Double.BYTES;
	
				this.doubleData = new double[ this.inLet.info().channel_count() * this.chunckLength ];
				break;
			}
			case( LSL.ChannelFormat.string ):
			{
				nBytes = Character.BYTES;
	
				this.stringData = new String[ this.inLet.info().channel_count() * this.chunckLength ];
				break;
			}
			default:
			{
				String msg = "Data type (" + this.LSLFormatData + ") of stream input " + this.LSLName + " unsupported.";
				throw new UnsupportedDataTypeException( msg );
			}
		}

		this.timeMark = new double[ this.chunckLength ];

		return nBytes;
	}
	
	@Override
	protected void preStart() throws Exception
	{
		super.preStart(); 
		
		synchronized ( this.isRecording )
		{
			this.isRecording.set( true );
		}
		
		synchronized ( this )
		{
			startMonitor();
		}
		
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

		double samplingRate = this.inLet.info().nominal_srate();
		
		this.blockTimer = 0.5D; // 0.5 s
		
		if ( samplingRate != LSL.IRREGULAR_RATE )
		{
			this.blockTimer = 1.5 / samplingRate; // 1.5 times the period time  
			
			//this.timer = new Timer();
			
			int time = (int)(3*1000.0D / samplingRate);
			if (time < 3000)
			{
				time = 3000; // 3 seconds
			}
						
			this.timer = new Timer( time, new ActionListener() 
				{				
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						timeOver();
					}
			});
			
			//this.timer.setTimerValue( time );
			//this.timer.setName( this.getClass() + "-Timer");
			//this.timer.setTimerMonitor( this );
			
			//this.timer.startThread();					
		}		
		
		this.timeCorrection = this.inLet.time_correction();
	}

	protected void startUp() throws Exception
	{
		super.startUp();

		if( this.timer != null )
		{
			this.timer.start();
		}
		
		synchronized ( this.isStreamClosed )
		{
			this.isStreamClosed.set( false );
			this.inLet.open_stream();
		}
	}

	protected void preStopThread(int friendliness) throws Exception
	{		
		if( friendliness == IStoppableThread.FORCE_STOP 
				|| ( this.samplingRate == LSL.IRREGULAR_RATE ))
		{
			if( this.timer != null )
			{
				this.timer.stop();
			}
			
			synchronized ( this.isStreamClosed ) 
			{
				this.isStreamClosed.set( true );
				this.inLet.close_stream();				
			}
						
			//this.inLet.close();
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
			if (this.timer != null)
			{
				this.timer.interruptTimer();
			}
			*/
			
			if (this.timer != null)
			{
				this.timer.stop();
			}
			
			this.managerData( data, ConvertTo.doubleArray2byteArray( this.timeMark ) );
			
			/*
			if (this.timer != null)
			{
				this.timer.restartTimer();
			}
			*/
			
			if (this.timer != null)
			{
				this.timer.restart();
			}
		}
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
		int chunckSize = this.chunckLength * this.lslChannelCounts;
		
		switch (this.LSLFormatData)
		{
			case( LSL.ChannelFormat.int8 ):
			{				
				nReadData = this.inLet.pull_chunk( this.byteData, timestamp_buffer, this.blockTimer );
				
				if( nReadData > 0 )
				{
					int i = 0;					
					while( i < nReadData  
							&& this.tempSampleBytes.size() < chunckSize )
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
					
					if( this.tempSampleBytes.size() >= chunckSize )
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
							&& this.tempSampleBytes.size() < chunckSize )
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
			case( LSL.ChannelFormat.int16 ):
			{
				nReadData = this.inLet.pull_chunk( this.shortData, timestamp_buffer, this.blockTimer );
				
				if( nReadData > 0 )
				{
					int i = 0;					
					while( i < nReadData  
							&& this.tempSampleBytes.size() < chunckSize )
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
					
					if( this.tempSampleBytes.size() >= chunckSize )
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
							&& this.tempSampleBytes.size() < chunckSize )
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
			case( LSL.ChannelFormat.int32 ):
			{					
				nReadData = this.inLet.pull_chunk( this.intData, timestamp_buffer, this.blockTimer );
				
				if( nReadData > 0 )
				{					
					int i = 0;					
					while( i < nReadData 
							&& this.tempSampleBytes.size() < chunckSize )
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
					
					if( this.tempSampleBytes.size() >= chunckSize )
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
							&& this.tempSampleBytes.size() < chunckSize )
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
			case( LSL.ChannelFormat.float32 ):
			{
				nReadData = this.inLet.pull_chunk( this.floatData, timestamp_buffer, this.blockTimer );
				
				if( nReadData > 0 )
				{	
					int i = 0;					
					while( i < nReadData 
							&& this.tempSampleBytes.size() < chunckSize )
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
					
					if( this.tempSampleBytes.size() >= chunckSize )
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
							&& this.tempSampleBytes.size() < chunckSize )
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
			case( LSL.ChannelFormat.double64 ):
			{
				nReadData = this.inLet.pull_chunk( doubleData, timestamp_buffer, this.blockTimer  );
				
				if( nReadData > 0 )
				{	
					int i = 0;					
					while( i < nReadData 
							&& this.tempSampleBytes.size() < chunckSize )
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
					
					if( this.tempSampleBytes.size() >= chunckSize )
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
							&& this.tempSampleBytes.size() < chunckSize )
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
			case( LSL.ChannelFormat.string ):
			{
				nReadData = this.inLet.pull_chunk( this.stringData, timestamp_buffer, this.blockTimer  );
				
				if( nReadData > 0 )
				{	
					int i = 0;					
					while( i < nReadData 
							&& this.tempSampleBytes.size() < chunckSize )
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
					
					if( this.tempSampleBytes.size() >= chunckSize )
					{			
						String txt = "";
						for( int iS = 0; iS < this.tempSampleBytes.size(); iS++ )
						{
							if( iS > 0 )
							{
								txt += " ";
							}
							
							txt += this.tempSampleBytes.get( iS );														
						}	
						txt += "\n";
												
						out = txt.getBytes();												
						
						for( int iS = 0; iS < this.tempTimeMark.size(); iS++ )
						{							
							this.timeMark[ iS ] = (Double)this.tempTimeMark.get( iS ) + this.timeCorrection;
						}	
						
						this.tempSampleBytes.clear();
						this.tempTimeMark.clear();
					}
					
					while( i < nReadData 
							&& this.tempSampleBytes.size() < chunckSize )
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
				throw new UnsupportedDataTypeException();
			}
		}
		
		return out;
	}
				
	protected void runExceptionManager( Throwable e )
	{
		if ( !(e instanceof InterruptedException) 
				|| ( e instanceof Error ) )
		{
			if( this.timer != null )
			{
				this.timer.stop(); 
			}
			
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

		
		if (this.timer != null)
		{
			this.timer.stop();
		}		
		this.timer = null;
		
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
								
				int delay = 3000; // 3 seconds								
				this.timer = new Timer( delay, new ActionListener() 
				{					
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						timeOver2();
					}
				});
				this.timer.start();
								
				this.inLet.close();
			}
		}
		
		synchronized ( this.postCleanDone ) 
		{
			if( this.timer != null )
			{
				this.timer.stop();
			}
			
			if( !this.postCleanDone.get() )
			{
				this.postCleanDone.set( true );
				
				postCleanUp();
			}
		}		
	}
	
	private void readRemainingData() throws Exception
	{						
		double timestamp_buffer[] = new double[ this.chunckLength ];
		int nReadData = 0;
		int chunckSize = this.chunckLength * this.lslChannelCounts;		
		
		double timeout = 0.0D;
				
		boolean rep = true;
		int rerun = (int)this.inLet.info().nominal_srate() / 2;
		if( rerun < 10 )
		{
			rerun = 10;
		}		
		
		do
		{
			rerun--;
			
			ByteBuffer data = null;
			byte[] out = null;			
			
			switch ( this.LSLFormatData )
			{
				case( LSL.ChannelFormat.int8 ):
				{
					nReadData = this.inLet.pull_chunk( this.byteData, timestamp_buffer, timeout );
					
					if( nReadData > 0 )
					{
						int i = 0;					
						while( i < nReadData  
								&& this.tempSampleBytes.size() < chunckSize )
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
						
						if( this.tempSampleBytes.size() >= chunckSize )
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
								&& this.tempSampleBytes.size() < chunckSize )
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
				case( LSL.ChannelFormat.int16 ):
				{
					nReadData = this.inLet.pull_chunk( this.shortData, timestamp_buffer, timeout );
					
					if( nReadData > 0 )
					{
						int i = 0;					
						while( i < nReadData  
								&& this.tempSampleBytes.size() < chunckSize )
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
						
						if( this.tempSampleBytes.size() >= chunckSize )
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
								&& this.tempSampleBytes.size() < chunckSize )
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
				case( LSL.ChannelFormat.int32 ):
				{				
					nReadData = this.inLet.pull_chunk( this.intData, timestamp_buffer, timeout );
					
					if( nReadData > 0 )
					{
						int i = 0;					
						while( i < nReadData 
								&& this.tempSampleBytes.size() < chunckSize )
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
						
						if( this.tempSampleBytes.size() >= chunckSize )
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
								&& this.tempSampleBytes.size() < chunckSize )
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
				case( LSL.ChannelFormat.float32 ):
				{
					nReadData = this.inLet.pull_chunk( this.floatData, timestamp_buffer, timeout );
					
					if( nReadData > 0 )
					{						
						int i = 0;					
						while( i < nReadData 
								&& this.tempSampleBytes.size() < chunckSize )
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
						
						if( this.tempSampleBytes.size() >= chunckSize )
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
								&& this.tempSampleBytes.size() < chunckSize )
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
				case( LSL.ChannelFormat.double64 ):
				{
					nReadData = this.inLet.pull_chunk( doubleData, timestamp_buffer, timeout );
					
					if( nReadData > 0 )
					{																			
						int i = 0;					
						while( i < nReadData 
								&& this.tempSampleBytes.size() < chunckSize )
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
						
						if( this.tempSampleBytes.size() >= chunckSize )
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
								&& this.tempSampleBytes.size() < chunckSize )
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
				case( LSL.ChannelFormat.string ):
				{
					nReadData = this.inLet.pull_chunk( this.stringData, timestamp_buffer, timeout );
					//nChunck = nBytes / this.lslChannelCounts;
					
					if( nReadData > 0 )
					{
						//this.timeMark = timestamp_buffer[ 0 ];
						
						int i = 0;					
						while( i < nReadData 
								&& this.tempSampleBytes.size() < chunckSize )
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
						
						if( this.tempSampleBytes.size() >= chunckSize )
						{			
							String txt = "";
							for( int iS = 0; iS < this.tempSampleBytes.size(); iS++ )
							{
								if( iS > 0 )
								{
									txt += " ";
								}
								
								txt += this.tempSampleBytes.get( iS );														
							}	
							txt += "\n";
													
							out = txt.getBytes();
													
							
							for( int iS = 0; iS < this.tempTimeMark.size(); iS++ )
							{							
								this.timeMark[ iS ] = (Double)this.tempTimeMark.get( iS ) + this.timeCorrection;
							}	
							
							this.tempSampleBytes.clear();
							this.tempTimeMark.clear();
						}
						
						while( i < nReadData 
								&& this.tempSampleBytes.size() < chunckSize )
						{
							this.tempSampleBytes.add( this.stringData[ i ] );
							i++;
						}		
						
						while( j < timestamp_buffer.length && timestamp_buffer[ j ] > 0.0D )
						{
							this.tempTimeMark.add( timestamp_buffer[ j ] );
						}
					}
		
					break;
				}
				default:
				{
					throw new UnsupportedDataTypeException();
				}
			}
			
			if( out != null )
			{
				this.managerData( out, ConvertTo.doubleArray2byteArray( this.timeMark ) );
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
			
			this.notifTask.addEvent( ev );
			synchronized ( this.notifTask )
			{
				this.notifTask.notify();
			}
		}
	}

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


	/*
	@Override
	public List<EventInfo> getResult( boolean clear)
	{
		List< EventInfo > lst = new ArrayList< EventInfo >();
		
		synchronized( this.events )
		{			
			lst.addAll( this.events );			
			
			if( clear )
			{
				this.events.clear();
			}
		}
		
		return lst;
	}
	 */

	/*
	@Override
	public void clearResult()
	{
		synchronized ( this.events)
		{
			this.events.clear();
		}		
	}
	*/
		
	private void timeOver( )
	{	
		this.stopThread( IStoppableThread.FORCE_STOP );
		//this.inLet.close_stream();
		synchronized ( this.isStreamClosed )
		{
			if( !this.isStreamClosed.get() )
			{
				this.isStreamClosed.set( true );
				
				this.timer = new Timer( this.timer.getDelay(), new ActionListener()
				{					
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						timeOver2();
					}
				});
				this.timer.start();
				
				this.inLet.close();
				
				this.timer.stop();
			}
		}		
		
		this.notifyProblem( new TimeoutException( "Waiting time for input data from device <" + this.LSLName + "> was exceeded." ) );		
	}
	
	private void timeOver2( )
	{	
		super.interrupt(); // a new try to stop the thread
				
		this.timer = new Timer( this.timer.getDelay(), new ActionListener() 
		{			
			@Override
			public void actionPerformed(ActionEvent e) 
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
					notifyProblem( new DestroyFailedException( "The input stream " + LSLName 
																+ " is blocked. It is not possible released/closed. "
																+ "Quit " + ConfigApp.shortNameApp + " is recommended.") );
				}
			}
		});
		
		this.timer.start();
	}

	protected void startMonitor() throws Exception 
	{
		if( this.monitor != null )
		{
			this.notifTask = new NotificationTask( false );
			this.notifTask.taskMonitor( this.monitor );
			this.notifTask.setName( this.notifTask.getID() + "-" + this.getClass().getName() );
			this.notifTask.startThread();
		}
		else
		{
			throw new SettingException( "Monitor non defined. Use taskMonitor( ... ) function to set it." );
		}
	}
	
	//public void reportClockTime(long time) {}

	protected abstract void postCleanUp() throws Exception;

	protected abstract void managerData(byte[] dataArrayOfBytes, byte[] timeArrayOfBytes ) throws Exception;
	
}