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
 *
 */
package lslrec.dataStream.binary.input.writer.plugin;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.auxiliar.extra.FileUtils;
import lslrec.auxiliar.tasks.ITaskIdentity;
import lslrec.dataStream.binary.input.writer.StreamBinaryHeader;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.stoppableThread.AbstractStoppableThread;

public class DataProcessingExecutor extends AbstractStoppableThread implements ITaskIdentity
{
	private ConcurrentLinkedQueue< Byte[] > inputs;
	private ConcurrentLinkedQueue< Byte[] > inputTimes;
	private List< Number > data;
	private LSLRecPluginDataProcessing process;
	
	private int dataBlockSize = -1;
	
	private File outfile = null;	
	private BufferedOutputStream out;
	
	/**
	 * 
	 */
	public DataProcessingExecutor( LSLRecPluginDataProcessing processes, String outputFileName )  throws Exception 
	{
		this.inputs = new ConcurrentLinkedQueue< Byte[] >();
		this.inputTimes = new ConcurrentLinkedQueue< Byte[] >();
		
		this.data = new ArrayList< Number >();
		this.process = processes;
		
		if( outputFileName != null )
		{
			this.outfile =  new File( outputFileName );
		}
		
		if( this.process != null )
		{
			this.dataBlockSize = this.process.getDataStreamSetting().getChunkSize() * this.process.getDataStreamSetting().channel_count();
		}
		
		super.setName( this.getID() );
	}
	
	public File getOutputBinaryFile()
	{
		return this.outfile;
	}
	
	@Override
	public String getID() 
	{
		return this.getClass().getName() + "-" + this.process.getDataStreamSetting().name();
	}
	
	@Override
	protected void preStart() throws Exception 
	{	
		if( this.outfile != null )
		{
			FileUtils.CreateTemporalBinFile( this.outfile );
			
			this.out = new BufferedOutputStream( new FileOutputStream( this.outfile ) );
		}
		
		super.preStart();
	}
	
	@Override
	protected void startUp() throws Exception 
	{
		super.stopThread = ( this.process == null );
		
		if( !super.stopThread && this.out != null )
		{
			String binHeader = StreamBinaryHeader.getStreamBinHeader( this.process.getDataStreamSetting() );
			
			binHeader = binHeader.trim().replace( "\r", "" ).replace( "\n", "" ) + "\n";
			
			this.out.write( binHeader.getBytes() );
		}
	}
	
	@Override
	protected void runInLoop() throws Exception 
	{
		synchronized ( this )
		{
			if( this.inputs.isEmpty() )
			{
				super.wait();
			}
		}
		
		if( this.process != null )
		{
			while( !this.inputs.isEmpty() )
			{
				Number[] vals = ConvertTo.Transform.ByteArrayTo( this.inputs.poll(), this.process.getDataStreamSetting().data_type() );
				Byte[] times = this.inputTimes.poll(); 
				
				synchronized ( this.data )
				{
					for( Number v : vals )
					{
						this.data.add( v );
					}
				}
				
				if( this.data.size() >= this.dataBlockSize )
				{
					Number[] d;
					synchronized ( this.data )
					{
						d = this.data.subList( 0, this.dataBlockSize ).toArray( new Number[0] );
						this.data.subList( 0, this.dataBlockSize ).clear();
					}
									
					Number[] processedData = this.process.processDataBlock( d );
					//
					// No interleaved data, that is:
					// [A0, B0, C0, .., A1, B1, C2, ..., An, Bn, Cn, ...]  
					//
										
					if( this.out != null && processedData != null )
					{
						if( this.process.getDataStreamSetting().isInterleavedData() )
						{
							processedData = ConvertTo.Transform.Interleaved( processedData
																	, this.process.getDataStreamSetting().channel_count()
																	, this.process.getDataStreamSetting().getChunkSize() );
						}
						
						byte[] DAT = ConvertTo.Transform.NumberArray2byteArra( processedData, this.process.getDataStreamSetting().data_type() );
						
						this.out.write( DAT );
						this.out.write( ConvertTo.Casting.ByterArray2byteArray( times ) );
					}
				}
			}	
		}
	}
	
	@Override
	protected void runExceptionManager(Throwable e) 
	{
		if( !( e instanceof InterruptedException ) )
		{	
			super.runExceptionManager(e);
		}
	} 

	public final void processData( byte[] inputs, byte[] times )
	{
		if( this.process != null && inputs != null )
		{
			this.inputs.add( ConvertTo.Casting.byteArray2ByteArray( inputs ) );
			this.inputTimes.add( ConvertTo.Casting.byteArray2ByteArray( times ) );
						
			synchronized ( this ) 
			{
				super.notify();
			}
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
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
		
		if( this.out != null )
		{
			this.out.close();
		}
	}
	
	public List< Integer > getTotalBufferLengths()
	{
		List< Integer > res = new ArrayList<Integer>();
		
		if( this.process != null )
		{
			res = this.process.getAllDataBufferLengths();
		}
		
		return res;
	}
}
