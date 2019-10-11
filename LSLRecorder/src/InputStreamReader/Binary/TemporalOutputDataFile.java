/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018-2019 by Manuel Merino Monge <manmermon@dte.us.es>
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

package InputStreamReader.Binary;

import Config.ConfigApp;
import Controls.Messages.EventInfo;
import Controls.Messages.eventType;
import InputStreamReader.TemporalData;
import InputStreamReader.OutputDataFile.Format.DataFileFormat;
import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLConfigParameters;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.annotation.processing.FilerException;

public class TemporalOutputDataFile extends readInputData
{
	private byte[] NO_MARK;
	private byte[] mark;

	protected File file = null;
	protected File timeStampFile = null;

	private BufferedOutputStream out;  
	private DataOutputStream outTimeStampFile;

	private String ext = ".temp";
	
	private String outFileName = "";
	private String outFileFormat = DataFileFormat.CLIS;
	
	//private Semaphore syncMarkSem = null;
	
	private MarkerRegister markReg;
	
	private final String headerBinSeparator = ";" ;
	
	public TemporalOutputDataFile( String filePath, LSL.StreamInfo info
										, LSLConfigParameters lslCfg, int Number ) throws Exception
	{
		super( info, lslCfg );
	
		
		this.markReg = new MarkerRegister();
		this.markReg.setName( this.markReg.getName() + "-" + super.LSLName );		
		
		String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

		this.outFileName = filePath;
		
		super.setName( info.name() + "(" + info.uid() + ")");

		this.file = new File( filePath + "_" + date + "_" + info.name() +  this.ext + Number);
		this.timeStampFile = new File(filePath + "_timeStamp_" + date + "_" + info.name() + this.ext + Number);

		int index = filePath.lastIndexOf("/");
		if (index < 0)
		{
			index = filePath.lastIndexOf("\\");
		}

		File dir = null;
		if (index >= 0)
		{
			String folder = filePath.substring(0, index + 1);
			dir = new File(folder);
		}

		boolean ok = true;
		String errorMsg = "Problem: file " + filePath;

		if ((dir != null) && (this.file != null))
		{
			try
			{
				if (!dir.exists())
				{
					if (!dir.mkdir())
					{
						ok = false;
					}
				}

				if (!this.file.exists())
				{
					this.file.createNewFile();
				}

				if (!this.timeStampFile.exists())
				{
					this.timeStampFile.createNewFile();
				}

				if( !this.file.isFile() || !this.file.canWrite() 
						|| !this.timeStampFile.isFile() || !this.timeStampFile.canWrite() )
				{   
					ok = false;
					errorMsg += " is not files or it is not possible to write";
				}

			}
			catch (Exception e)
			{
				ok = false;
				errorMsg = errorMsg + e.getMessage();
			}
		}
		else
		{
			ok = false;
			errorMsg = errorMsg + " not found";
		}

		if (!ok)
		{
			throw new FilerException(errorMsg);
		}
		
		//this.syncMarkSem = new Semaphore(1, true);
		
	}
	
	public void addMark( Integer value )
	{		
		if( this.markReg != null )
		{
			this.markReg.register( value );
		}
	}

	protected int createArrayData() throws Exception
	{
		int nBytes = super.createArrayData();

		if (this.LSLFormatData == LSL.ChannelFormat.string )
		{
			this.mark = new String("0").getBytes();
			this.NO_MARK = this.mark;
		}
		else
		{
			this.NO_MARK = new byte[nBytes];
			this.mark = this.NO_MARK;
		}

		return nBytes;
	}
	
	protected void preStart() throws Exception
	{
		super.preStart();
		
		this.markReg.startThread();

		if (!this.file.exists())
		{
			this.file.createNewFile();
		}

		if (!this.timeStampFile.exists())
		{
			this.timeStampFile.createNewFile();
		}

		if (( !this.file.isFile() ) || ( !this.file.canWrite() ) || 
				( !this.timeStampFile.isFile() ) || ( !this.timeStampFile.canWrite() ) )
		{
			throw new FilerException(this.file.getAbsolutePath() + " is not a file or is only read mode");
		}

		this.out = new BufferedOutputStream( new FileOutputStream( this.file ) );
		this.outTimeStampFile = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( this.timeStampFile ) ) );
	}
	
	@Override
	protected void startUp() throws Exception 
	{
		String binHeader = super.LSLName + this.headerBinSeparator 
							+ super.LSLFormatData + this.headerBinSeparator
							+ ( super.lslChannelCounts + 1 ) + this.headerBinSeparator
							+ super.lslXML;
		binHeader = binHeader.trim().replace( "\r", "" ).replace( "\n", "" ) + "\n";
		
		this.out.write( binHeader.getBytes() );

		binHeader = super.LSLName + this.headerBinSeparator 
					+ LSL.ChannelFormat.double64 + this.headerBinSeparator
					+ 1;
		binHeader = binHeader.trim().replace( "\r", "" ).replace( "\n", "" ) + "\n";
		this.outTimeStampFile.write( binHeader.getBytes() );
		
		super.startUp();
	}
	
	protected void managerData( byte[] data ) throws Exception
	{	
		/*
		try
		{
			this.syncMarkSem.acquire();
		}
		catch (InterruptedException localInterruptedException) 
		{
		}
		*/
		
		synchronized( this.mark )
		{		
			int numReadChunk = super.timeMark.length;
			int dataTypeByteLength = this.mark.length;
			
			byte[] aux = new byte[ data.length + dataTypeByteLength * numReadChunk ];
					
			if( numReadChunk == 1 )
			{
				int i = 0;
				while ( i < data.length )
				{
					aux[ i ] = data[ i ];
					
					i++;
				}
		
				for (int j = 0; j < this.mark.length; j++)
				{
					aux[ i ] = this.mark[ j ];
					i++;
				}
			}
			else
			{	
				int indexAux = 0;
				int count = super.lslChannelCounts;
				int N = super.lslChannelCounts * dataTypeByteLength;
				
				if( super.interleavedData )
				{				
					for( int j = 0; j < data.length; j += dataTypeByteLength )
					{	
						for( int k = 0; k < dataTypeByteLength; k++ )
						{
							aux[ indexAux ] = data[ j + k ];
							indexAux++;
						}
						
						count--;
						if( count == 0 )
						{
							indexAux += dataTypeByteLength;
							count = super.lslChannelCounts;
						}					
					}	
				}
				else // Sequential
				{				
					int dLen = numReadChunk * dataTypeByteLength;				
					
					for( int c = 0; c < this.lslChannelCounts; c++ )
					{					
						int indexA = c * dLen;
						int indexB = ( c + 1 ) * dLen;
						indexAux = c * dataTypeByteLength;
						
						for( int j = indexA; j < indexB; j += dataTypeByteLength )
						{
							for( int k = 0; k < dataTypeByteLength; k++ )
							{
								aux[ indexAux ] = data[ j + k ];
								indexAux++;							
							}
							
							indexAux += N;
						}
					}
				}
				
				for( int j =  N
						; j < aux.length
						; j += ( N + dataTypeByteLength ) )
				{
					for( int k = 0; k < this.NO_MARK.length; k++ )
					{
						aux[ j + k ] = this.NO_MARK[ k ];
					}
				}
				
				for(   int j = aux.length - dataTypeByteLength, k = 0
						 ; j < aux.length && k < this.mark.length
						 ; j++, k++ )
				{
					aux[ j ] = this.mark[ k ];
				}		
				
			}
			
			this.out.write( aux );
			
			this.mark = this.NO_MARK;
	
			for( int iT = 0; iT < super.timeMark.length; iT++ )
			{
				this.outTimeStampFile.writeDouble( super.timeMark[ iT ] );
			}
						
			/*
			if( this.syncMarkSem.availablePermits() < 1 )
			{
				this.syncMarkSem.release();
			}
			*/
		}
	}
		
	protected void cleanUp() throws Exception
	{
		/*
		try
		{
			this.syncMarkSem.acquire();
		}
		catch (InterruptedException localInterruptedException) 
		{

		}
		*/
		
		this.markReg.stopThread( IStoppableThread.FORCE_STOP );
		this.markReg = null;
				
		super.cleanUp();
			
		/*
		if (this.syncMarkSem.availablePermits() < 1)
		{
			this.syncMarkSem.release();
		}
		*/
	}

	protected void postCleanUp() throws Exception
	{	
		this.out.close();		
		this.outTimeStampFile.close();
		
		EventInfo event = new EventInfo( this.GetFinalOutEvent(), this.getTemporalFileData() );

		this.events.add(event);
		
		if (this.monitor != null)
		{
			this.monitor.taskDone(this);
		}
	}
	
	protected String GetFinalOutEvent()
	{
		return eventType.SAVED_OUTPUT_TEMPORAL_FILE;
	}
	
	//	
	/*
	protected void notifyProblem(Exception e)
	{
		super.notifyProblem( e );
		
		if (this.syncMarkSem.availablePermits() < 1)
		{
			this.syncMarkSem.release();
		}
	}
	*/

	public void setOutputFileFormat( String fileFormat )
	{
		this.outFileFormat = fileFormat;
	}
		
	private TemporalData getTemporalFileData() throws Exception
	{		
		/*
		List< Object > Data = new ArrayList< Object >();
		List< Double > Time = new ArrayList< Double >();

		BufferedInputStream din = null;

		//byte[] buf = new byte[ Float.BYTES ];
		byte[] buf = new byte[ this.NO_MARK.length ];

		try
		{
			din = new BufferedInputStream( new FileInputStream( this.file ) );
			switch( super.LSLFormatData ) 
			{
				case( LSL.ChannelFormat.double64 ):
				{
					while( din.read( buf ) > 0 )
					{
						Data.add( ByteBuffer.wrap( buf ).order( ByteOrder.BIG_ENDIAN ).getDouble() );
					}
					break;
				}
				case( LSL.ChannelFormat.float32 ):
				{
					while( din.read( buf ) > 0 )
					{
						Data.add( ByteBuffer.wrap( buf ).order( ByteOrder.BIG_ENDIAN ).getFloat() );
					}
					break;
				}
				case( LSL.ChannelFormat.int8 ):
				{
					while( din.read( buf ) > 0 )
					{
						Data.add( buf );
					}
					break;
				}
				case( LSL.ChannelFormat.int16 ):
				{
					while( din.read( buf ) > 0 )
					{
						Data.add( ByteBuffer.wrap( buf ).order( ByteOrder.BIG_ENDIAN ).getShort() );
					}
					break;
				}
				case( LSL.ChannelFormat.int32 ):
				{
					while( din.read( buf ) > 0 )
					{
						Data.add( ByteBuffer.wrap( buf ).order( ByteOrder.BIG_ENDIAN ).getInt() );
					}
					break;
				}
				case( LSL.ChannelFormat.int64 ):
				{
					while( din.read( buf ) > 0 )
					{
						Data.add( ByteBuffer.wrap( buf ).order( ByteOrder.BIG_ENDIAN ).getLong() );
					}
					break;
				}
				default: // String
				{					
					while( din.read( buf ) > 0 )
					{
						Data.add( ByteBuffer.wrap( buf ).order( ByteOrder.BIG_ENDIAN ).getChar() );
					}
					break;
				}
			}

			din.close();

			//buf = new byte[ Double.SIZE ];
			DataInputStream dinTime = new DataInputStream( new FileInputStream( this.timeStampFile ) );
			
			while( dinTime.available() > 0 )
			{
				//Time.add( (double)ByteBuffer.wrap( buf ).order( ByteOrder.BIG_ENDIAN ).getDouble() );
				Time.add( dinTime.readDouble() );
			}

			dinTime.close();

		}
		catch( EOFException ignore)
		{        	
		}
		catch (Exception ioe)
		{
		}
		finally
		{
			if( din != null )
			{
				try 
				{
					din.close();
				} 
				catch (IOException e) 
				{
				}
			}			
		}
		

		TemporalData data = new TemporalData( Data, Time, super.LSLFormatData, super.lslChannelCounts
												, super.LSLName, super.lslXML, this.outFileName, this.outFileFormat );
		 */
		
		TemporalData data = new TemporalData( this.file, this.timeStampFile, super.LSLFormatData, super.lslChannelCounts + 1
												, super.LSLName, super.lslXML, this.outFileName, this.outFileFormat
												, !ConfigApp.isTesting() );
		return data;
	}

	@Override
	public String getID() 
	{
		return this.getName();
	}

	
	////////////////////////////////////
	//
	//
	
	private class MarkerRegister extends AbstractStoppableThread
	{
		private Integer value;
		
		public MarkerRegister() 
		{	
			this.value = new Integer( 0 );
			super.setName( this.getClass().getSimpleName() );
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
			if( this.value == 0)
			{
				wait();
			}
			//syncMarkSem.acquire();
	
			synchronized( mark )
			{
				switch( LSLFormatData ) 
				{				
					case LSL.ChannelFormat.double64:
					{				
						double auxMark = 0.0D;
						if( mark != null )
						{
							auxMark = ByteBuffer.wrap( mark ).getDouble();
						}
		
						double v = ( value.intValue() | ( int )auxMark);
		
						mark = ByteBuffer.allocate( Double.BYTES).putDouble( v ).array();
						break;
					}
					case LSL.ChannelFormat.float32:
					{
						float auxMark = 0.0F;
						if( mark != null )
						{
							auxMark = ByteBuffer.wrap( mark ).getFloat();
						}
						float v = ( value.intValue() | (int)auxMark );
		
						mark = ByteBuffer.allocate( Float.BYTES).putFloat( v ).array();
						break;
					}
					case LSL.ChannelFormat.int64:
					{
						long auxMark = 0L;
						if( mark != null )
						{
							auxMark = ByteBuffer.wrap( mark ).getLong();
						}
						long v = value.longValue() | auxMark; 
		
						mark = ByteBuffer.allocate( Long.BYTES).putLong( v ).array();
						break;
					}
					case LSL.ChannelFormat.int32:
					{
						int auxMark = 0;
						if( mark != null )
						{
							auxMark = ByteBuffer.wrap( mark ).getInt();
						}
						int v = value.intValue() | auxMark;
		
						mark = ByteBuffer.allocate( Integer.BYTES).putInt( v ).array();
						break;
					}
					case LSL.ChannelFormat.int16:
					{
						short auxMark = 0;
						if( mark != null )
						{
							auxMark = ByteBuffer.wrap( mark ).getShort();
						}
						short v = (short)( auxMark | value.shortValue() );
		
						mark = ByteBuffer.allocate( Short.BYTES).putShort( v ).array();
						break;
					}
					case LSL.ChannelFormat.int8:
					{
						byte auxMark = 0;
						if( mark != null )
						{
							auxMark = mark[ 0 ];
						}
		
						byte v = (byte)( value.byteValue() | auxMark );
		
						mark = ByteBuffer.allocate( 1 ).put( v ).array();
						break;
					}
					default:
					{
						int auxMark = 0;
						if( mark != null )
						{
							auxMark = new Integer( new String( mark ) );
						}
						int v = value.intValue() | auxMark;
		
						mark = ("" + v ).getBytes();
						break;
					}
				}
			}
		}
		
		@Override
		protected void runExceptionManager(Exception e) 
		{
			if( !( e instanceof InterruptedException ) )
			{
				super.runExceptionManager(e);
			}
		}
		
		@Override
		protected void finallyManager() 
		{
			super.finallyManager();
			
			this.value = 0;
			
			/*
			if ( syncMarkSem.availablePermits() < 1)
			{
				syncMarkSem.release();
			}
			*/
		}
				
		public synchronized void register( int mark )
		{
			synchronized( value )
			{
				value = mark;
				
				this.notify();
			}
		}
		
		/*
		@Override
		protected void cleanUp() throws Exception 
		{
			super.cleanUp();
			
			if ( syncMarkSem.availablePermits() < 1)
			{
				syncMarkSem.release();
			}
		}
		*/
	}
	
}