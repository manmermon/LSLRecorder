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

package lslrec.dataStream.binary.input.writer;

import lslrec.dataStream.binary.input.InputDataStreamReceiverTemplate;
import lslrec.dataStream.binary.input.writer.plugin.DataProcessingExecutor;
import lslrec.dataStream.binary.reader.TemporalBinData;
import lslrec.dataStream.binary.setting.BinaryFileStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.MutableStreamSetting;
import lslrec.dataStream.family.setting.StreamExtraLabels;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.stoppableThread.IStoppableThread;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import lslrec.auxiliar.extra.FileUtils;
import lslrec.control.message.EventInfo;
import lslrec.control.message.EventType;

public class TemporalOutDataFileWriter extends InputDataStreamReceiverTemplate
{
	protected File file = null;

	private BufferedOutputStream out; 
	private FileOutputStream fout;

	private final String ext = ".temp";
	
	private OutputFileFormatParameters outputFormat;
	
	private DataProcessingExecutor datProcessingExec = null;
	private final String outDatProcessInfix = "_processedData";
	
	private LSLRecPluginDataProcessing postProcessing = null;
	
	private ScheduledExecutorService scheduler2SyncInData = null;
	private final Object fileLock = new Object();
	private long secSyncTosaveData = 0;	
	private ByteArrayOutputStream temporalDataBuffer = null;
	private ByteArrayOutputStream bufferToWrite = null;
	private volatile Exception storageError = null;
	
	public TemporalOutDataFileWriter( IStreamSetting lslCfg, OutputFileFormatParameters outFormat,  int Number ) throws Exception
	{
		super( lslCfg );
		
		String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		
		if( outFormat == null )
		{
			throw new IllegalArgumentException( "Output file format null" );
		}
		
		this.outputFormat = outFormat;
		
		this.secSyncTosaveData = (Integer)this.outputFormat.getParameter( OutputFileFormatParameters.SYNC_IN_DATA_EACH_X_SEC ).getValue();
				
		super.setName( super.streamSetting.name() + "(" + super.streamSetting.uid() + ")");
				
		this.file = FileUtils.CreateTemporalBinFile( this.outputFormat.getParameter( OutputFileFormatParameters.OUT_FILE_NAME ).getValue() + "_" + date + "_" + super.streamSetting.name() +  this.ext + Number );
		
	}

	public void setDataProcessing( LSLRecPluginDataProcessing process, boolean save ) throws Exception
	{
		if( super.getState().equals( State.NEW ) )
		{
			String outFile = null;
			
			if( save )
			{
				outFile = this.file.getAbsolutePath();
				
				int index = outFile.lastIndexOf( this.ext );
				
				outFile = outFile.substring( 0, index ) + this.outDatProcessInfix + outFile.substring( index );				
			}
					
			this.datProcessingExec = new DataProcessingExecutor( process, outFile );
		}
	}
	
	public void setDataPostProcessing( LSLRecPluginDataProcessing postprocess ) throws Exception
	{
		if( super.getState().equals( State.NEW ) )
		{
			this.postProcessing = postprocess;
		}
	}
	
	public LSLRecPluginDataProcessing getPostProcessing()
	{
		return this.postProcessing;
	}
	
	public void addExtraInfo2Stream( String label, String text )
	{
		if( label != null && !label.trim().isEmpty() && text != null )
		{
			super.streamSetting.getExtraInfo().put( label, text );
		}
	}
	
	protected void preStart() throws Exception
	{
		super.preStart();
		
		FileUtils.CreateTemporalBinFile( this.file );

		this.fout =  new FileOutputStream( this.file );
		this.out = new BufferedOutputStream( this.fout );
		
		this.temporalDataBuffer = new ByteArrayOutputStream( );
	}
	
	@Override
	protected void startUp() throws Exception 
	{	
		super.startUp();
		
		Calendar c = Calendar.getInstance();
		c.add( 13, 1 );
		String date = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format( c.getTime() );
		
		String idDate = StreamExtraLabels.ID_START_SESSION_DATE;
		Map< String, String > addInfo = (Map< String, String >)this.outputFormat.getParameter( OutputFileFormatParameters.RECORDING_INFO ).getValue();
		addInfo.put( idDate, date );
		
		if( this.secSyncTosaveData > 0 )
		{
			this.bufferToWrite = new ByteArrayOutputStream();
			
			this.scheduler2SyncInData = Executors.newSingleThreadScheduledExecutor();
		}
		
		this.saveFileHeader();
		
		if (this.scheduler2SyncInData != null)
	    {
	        this.scheduler2SyncInData.scheduleWithFixedDelay( () -> 
	        {
	        	ByteArrayOutputStream batch;

	    	    synchronized (this.fileLock)
	    	    {
	    	        if (this.storageError != null)
	    	        {
	    	            return;
	    	        }

	    	        batch = this.temporalDataBuffer;

	    	        this.temporalDataBuffer = this.bufferToWrite;
	    	        this.bufferToWrite = batch;

	    	        this.temporalDataBuffer.reset();
	    	    }

	    	    try
	    	    {
	    	        if (batch.size() > 0)
	    	        {
	    	            batch.writeTo(this.out);
	    	            this.out.flush();
	    	            this.fout.getFD().sync();
	    	        }
	    	    }
	    	    catch (IOException e)
	    	    {
	    	        synchronized (this.fileLock)
	    	        {
	    	            this.storageError = e;
	    	        }

	    	        throw new RuntimeException(
	    	            "Error writing or synchronizing the file", e
	    	        );
	    	    }
			},
	            this.secSyncTosaveData,
	            this.secSyncTosaveData,
	            TimeUnit.SECONDS
	        );
	    }
		
		if( this.datProcessingExec != null )
		{
			this.datProcessingExec.startThread();
		}
	}

	private void saveFileHeader() throws Exception 
	{
		String binHeader = StreamBinaryHeader.getStreamBinHeader( super.streamSetting );
		
		binHeader = binHeader.trim().replace( "\r", "" ).replace( "\n", "" ) + "\n";
		byte[] header = binHeader.getBytes( StandardCharsets.UTF_8 );
		
		synchronized( this.fileLock )
		{
			this.temporalDataBuffer.write( header );
		}
		
		if( this.scheduler2SyncInData == null )
		{
			try
			{
				this.temporalDataBuffer.writeTo( this.out );
				this.out.flush();
				this.fout.getFD().sync();
			}
			catch (Exception e) 
			{
				this.storageError = e;
				throw e;
			}
			finally 
			{
				this.temporalDataBuffer.reset();
			}
		}
	}
	
	protected void managerData( byte[] data, byte[] time ) throws Exception
	{			
		int len = 0;
		
		len = ( data != null ? len + data.length : len );
		len = ( time != null ? len + time.length : len );
		
		if( len > 0 )
		{						
			synchronized( this.fileLock )
			{
				if (this.storageError != null)
				{
				    throw new IOException("Storage has failed", this.storageError );
				}
				
				if ( this.scheduler2SyncInData != null )
				{					
					if( data != null )
					{
						this.temporalDataBuffer.write( data, 0, data.length );
					}

					if( time != null )
					{
						this.temporalDataBuffer.write( time, 0, time.length );
					}
				}
				else
				{					
					try
					{
				        if( data != null )
						{
							this.out.write( data );
						}

						if( time != null )
						{
							this.out.write( time );
						}
					}
					catch (Exception e) 
					{
						this.storageError = e;
						throw e;
					}
				}			
			}
			
			if( this.datProcessingExec != null )
			{
				this.datProcessingExec.processData( data, time );
			}
		}
	}		
	
	@Override
	protected void cleanUp() throws Exception 
	{	
		boolean interruptedStop = this.stopStorageScheduler();
		
		Calendar c = Calendar.getInstance();
		c.add( 13, 1 );
		String date = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format( c.getTime() );
		
		String idDate = StreamExtraLabels.ID_END_SESSION_DATE;
		Map< String, String > addInfo = (Map< String, String >)this.outputFormat.getParameter( OutputFileFormatParameters.RECORDING_INFO ).getValue();
		addInfo.put( idDate, date );
		
		synchronized( this.fileLock )
		{			
			try
			{
				if( this.storageError == null ) 
				{
					if (this.temporalDataBuffer != null && this.temporalDataBuffer.size() > 0 )
					{
						this.temporalDataBuffer.writeTo(this.out);
					}

					this.out.flush();
					this.fout.getFD().sync();
				}
			}
			catch (Exception e)
			{
				this.storageError = e;
			}
			finally
			{	
				super.cleanUp();
				
				try
				{
					this.temporalDataBuffer.close();
					this.bufferToWrite.close();
				}
				catch (Exception e) 
				{}
				
				try
				{
					if (this.storageError == null)
					{
					    this.out.close();
					}
					else
					{
					    this.fout.close();
					}
				}
				catch (IOException e)
				{
					if( this.storageError == null )
					{
						this.storageError = e;
					}
					else
					{
						this.storageError.addSuppressed( e );
					}
					
					 try
					 {
						 this.fout.close();
					 }
					 catch (IOException closeError)
					 {
						 this.storageError.addSuppressed(closeError);
					 }
				}
			}
		}
		
		if (interruptedStop)
		{
		    Thread.currentThread().interrupt();
		}
		
		if (this.storageError != null)
		{
			if( interruptedStop )
			{
				this.storageError.addSuppressed( 
						new InterruptedException(
								"Interrupted while waiting for storage scheduler"
								)
						);
			}

			throw this.storageError;
		}

		if( interruptedStop )
		{
			throw new InterruptedException(
					"Interrupted while waiting for storage scheduler"
					);
		}
	}
	
	private boolean stopStorageScheduler()
	{
	    if (this.scheduler2SyncInData == null)
	    {
	        return false;
	    }

	    this.scheduler2SyncInData.shutdown();

	    boolean interrupted = false;

	    while ( !this.scheduler2SyncInData.isTerminated() )
	    {
	        try
	        {
	            this.scheduler2SyncInData.awaitTermination( 10, TimeUnit.SECONDS );
	        }
	        catch (InterruptedException e)
	        {
	            interrupted = true;
	            this.scheduler2SyncInData.shutdownNow();
	        }
	    }

	    return interrupted;
	}
	
	@Override
	protected void postCleanUp() throws Exception
	{	
		EventInfo processingEvent = null;		
		
		if( this.datProcessingExec != null )
		{
			this.datProcessingExec.stopThread( IStoppableThread.FORCE_STOP );
			
			File processfile = this.datProcessingExec.getOutputBinaryFile();			
			
			if( processfile != null )
			{				
				OutputFileFormatParameters procFormat = this.outputFormat.clone();
				
				String filename = procFormat.getParameter( OutputFileFormatParameters.OUT_FILE_NAME ).getValue().toString();
				
				int index = filename.lastIndexOf( File.separator );
				String prefix = "";
				String sufix = filename;
				
				if( index >= 0 )
				{
					prefix = filename.substring( 0, index );
					sufix = filename.substring( index );
				}
				
				index = sufix.lastIndexOf( "." );
				
				if( index >= 0 )
				{
					sufix = sufix.substring( 0, index ) + this.outDatProcessInfix + sufix.substring( index );
				}
				else
				{
					sufix += this.outDatProcessInfix; 
				}
				
				filename = prefix + sufix;
				
				procFormat.setParameter( OutputFileFormatParameters.OUT_FILE_NAME, filename );
				
				MutableStreamSetting dss = new MutableStreamSetting( super.streamSetting );
				
				dss.setAdditionalInfo( "ProcessingBufferLengths", this.datProcessingExec.getProcessingIDSequence() +"=" + this.datProcessingExec.getTotalBufferLengths().toString() );
				//LSLUtils.addNode( dss.getStreamInfo(), new StringTuple( "DataProcessingInfo", info ) );
				
				processingEvent = new EventInfo( this.datProcessingExec.getID()
													, GetFinalOutEvent()
													, this.getTemporalFileData( processfile, dss, procFormat, false ) );
			}
			
			this.datProcessingExec = null;
		}
		
		if( this.notifTask != null )
		{
			super.notifTask.queueEvent( processingEvent );
			
			EventInfo event = new EventInfo( this.getID(), GetFinalOutEvent(), this.getTemporalFileData( this.file, super.streamSetting, this.outputFormat, true ) );
							
			super.notifTask.queueAndSendEvent( event );
			
			//super.closeNotifierThread();
		}
	}
	
	public static String GetFinalOutEvent()
	{
		return EventType.SAVED_OUTPUT_TEMPORAL_FILE;
	}
				
	private TemporalBinData getTemporalFileData( File file, IStreamSetting setting, OutputFileFormatParameters formatPars, boolean addPostProcessing ) throws Exception
	{		
		BinaryFileStreamSetting bin = new BinaryFileStreamSetting( setting, file );
		
		TemporalBinData data = new TemporalBinData( bin, formatPars );
		
		if( addPostProcessing )
		{
			data.setPostprocessing( this.postProcessing );
		}
		
		return data;
	}

	@Override
	public String getID() 
	{
		return super.streamSetting.uid();
	}
}