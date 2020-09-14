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
import lslrec.dataStream.binary.reader.TemporalBinData;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.setting.DataStreamSetting;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.annotation.processing.FilerException;

import lslrec.auxiliar.extra.FileUtils;

public class TemporalOutDataFileWriter extends LSLInStreamDataReceiverTemplate
{
	protected File file = null;

	private BufferedOutputStream out; 

	private String ext = ".temp";
	
	private OutputFileFormatParameters outputFormat;
		
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
	
	protected void preStart() throws Exception
	{
		super.preStart();

		if (!this.file.exists())
		{
			this.file.createNewFile();
		}

		if (( !this.file.isFile() ) || ( !this.file.canWrite() )  )
		{
			throw new FilerException(this.file.getAbsolutePath() + " is not a file or is only read mode");
		}

		this.out = new BufferedOutputStream( new FileOutputStream( this.file ) );
	}
	
	@Override
	protected void startUp() throws Exception 
	{
		String binHeader = StreamBinaryHeader.getStreamBinHeader( super.streamSetting );
		
		binHeader = binHeader.trim().replace( "\r", "" ).replace( "\n", "" ) + "\n";
		
		this.out.write( binHeader.getBytes() );
		
		super.startUp();
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
		if( this.notifTask != null )
		{
			EventInfo event = new EventInfo( this.getID(), GetFinalOutEvent(), this.getTemporalFileData() );

			super.notifTask.addEvent( event );
			
			super.closeNotifierThread();
		}
	}
	
	public static String GetFinalOutEvent()
	{
		return EventType.SAVED_OUTPUT_TEMPORAL_FILE;
	}
				
	private TemporalBinData getTemporalFileData() throws Exception
	{		
		TemporalBinData data = new TemporalBinData( this.file
													, this.streamSetting
													, this.outputFormat );
		
		return data;
	}

	@Override
	public String getID() 
	{
		return this.getName();
	}
}