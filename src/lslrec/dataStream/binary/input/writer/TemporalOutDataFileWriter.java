/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
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

package lslrec.dataStream.binary.input.writer;

import lslrec.controls.messages.EventInfo;
import lslrec.controls.messages.EventType;
import lslrec.dataStream.binary.input.LSLInStreamDataReceiverTemplate;
import lslrec.dataStream.binary.input.writer.plugin.DataProcessingExecutor;
import lslrec.dataStream.binary.reader.TemporalBinData;
import lslrec.dataStream.family.lsl.LSLUtils;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.setting.DataStreamSetting;
import lslrec.dataStream.setting.MutableDataStreamSetting;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.stoppableThread.IStoppableThread;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import lslrec.auxiliar.extra.FileUtils;
import lslrec.auxiliar.extra.StringTuple;

public class TemporalOutDataFileWriter extends LSLInStreamDataReceiverTemplate
{
	protected File file = null;

	private BufferedOutputStream out; 

	private final String ext = ".temp";
	
	private OutputFileFormatParameters outputFormat;
	
	private DataProcessingExecutor datProcessingExec = null;
	private final String outDatProcessInfix = "_processedData";
	
	public TemporalOutDataFileWriter( DataStreamSetting lslCfg, OutputFileFormatParameters outFormat,  int Number ) throws Exception
	{
		super( lslCfg );
		
		String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

		if( outFormat == null )
		{
			throw new IllegalArgumentException( "Output file format null" );
		}
		
		this.outputFormat = outFormat;
		
		super.setName( super.streamSetting.getStreamName() + "(" + super.streamSetting.getStreamInfo().uid() + ")");
		
		this.file = FileUtils.CreateTemporalBinFile( this.outputFormat.getParameter( OutputFileFormatParameters.OUT_FILE_NAME ).getValue() + "_" + date + "_" + super.streamSetting.getStreamName() +  this.ext + Number );
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
	
	protected void preStart() throws Exception
	{
		super.preStart();
		
		FileUtils.CreateTemporalBinFile( this.file );

		this.out = new BufferedOutputStream( new FileOutputStream( this.file ) );
	}
	
	@Override
	protected void startUp() throws Exception 
	{
		String binHeader = StreamBinaryHeader.getStreamBinHeader( super.streamSetting );
		
		binHeader = binHeader.trim().replace( "\r", "" ).replace( "\n", "" ) + "\n";
		
		this.out.write( binHeader.getBytes() );
		
		super.startUp();
		
		if( this.datProcessingExec != null )
		{
			this.datProcessingExec.startThread();
		}
	}
	
	protected void managerData( byte[] data, byte[] time ) throws Exception
	{	
		int len = 0;
		
		len = ( data != null ? len + data.length : len );
		len = ( time != null ? len + time.length : len );
		
		if( len > 0 )
		{
			byte[] DAT = new byte[ len ];
			
			int init = 0;
			
			if( data != null )
			{
				System.arraycopy( data, 0, DAT, init, data.length );
				
				init = data.length;
			}
			
			if( time != null )
			{
				System.arraycopy( time, 0, DAT, init, time.length );
			}
						
			this.out.write( DAT );			
			
			if( this.datProcessingExec != null )
			{
				this.datProcessingExec.processData( data, time );
			}
		}
	}		

	@Override
	protected void cleanUp() throws Exception 
	{
		this.out.close();
		
		super.cleanUp();	
	}
	
	protected void postCleanUp() throws Exception
	{	
		EventInfo processingEvent = null;		
		
		if( this.datProcessingExec != null )
		{
			this.datProcessingExec.stopThread( IStoppableThread.FORCE_STOP );
			
			File processfile = this.datProcessingExec.getOutputBinaryFile();			
			
			if( processfile != null )
			{
				OutputFileFormatParameters procFormat = new OutputFileFormatParameters();
				
				for( String idPar : this.outputFormat.getAllParameters().getParameterIDs() )
				{	
					procFormat.setParameter( idPar, this.outputFormat.getParameter( idPar ).getValue() );
				}
				
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
				
				MutableDataStreamSetting dss = new MutableDataStreamSetting( super.streamSetting );
				
				String info = dss.getAdditionalInfo();
				if( !info.isEmpty() )
				{
					info += ";" ;
				}
				info += "ProcessingBufferLengths=" + this.datProcessingExec.getTotalBufferLengths();
				
				dss.setAdditionalInfo( info );
				LSLUtils.addNode( dss.getStreamInfo(), new StringTuple( "DataProcessingInfo", info ) );
				
				processingEvent = new EventInfo( this.datProcessingExec.getID()
													, GetFinalOutEvent()
													, this.getTemporalFileData( processfile, dss, procFormat ) );
			}
			
			this.datProcessingExec = null;
		}
		
		if( this.notifTask != null )
		{
			super.notifTask.addEvent( processingEvent );
			
			EventInfo event = new EventInfo( this.getID(), GetFinalOutEvent(), this.getTemporalFileData( this.file, super.streamSetting, this.outputFormat ) );

			super.notifTask.addEvent( event );
					
			
			super.closeNotifierThread();
		}
	}
	
	public static String GetFinalOutEvent()
	{
		return EventType.SAVED_OUTPUT_TEMPORAL_FILE;
	}
				
	private TemporalBinData getTemporalFileData( File file, DataStreamSetting setting, OutputFileFormatParameters formatPars ) throws Exception
	{		
		TemporalBinData data = new TemporalBinData( file, setting, formatPars );
		
		return data;
	}

	@Override
	public String getID() 
	{
		return this.getName();
	}
}