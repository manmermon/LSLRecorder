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

package DataStream.Binary;

import Config.ConfigApp;
import Controls.Messages.EventInfo;
import Controls.Messages.EventType;
import DataStream.StreamHeader;
import DataStream.OutputDataFile.Format.DataFileFormat;
import StoppableThread.IStoppableThread;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLConfigParameters;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.annotation.processing.FilerException;

import Auxiliar.Extra.FileUtils;

public class TemporalOutDataFileWriter extends LSLInStreamDataReceiverTemplate
{
	protected File file = null;

	private BufferedOutputStream out; 

	private String ext = ".temp";
	
	private String outFileName = "";
	private String outFileFormat = DataFileFormat.CLIS;
	
	//private int dataTypeByteLength = 1;
	//private int timeTypeByteLength = 1;	
		
	public TemporalOutDataFileWriter( String filePath, LSL.StreamInfo info
										, LSLConfigParameters lslCfg, int Number ) throws Exception
	{
		super( info, lslCfg );
		
		String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

		this.outFileName = filePath;
		
		super.setName( info.name() + "(" + info.uid() + ")");

		this.file = FileUtils.CreateTemporalBinFile( filePath + "_" + date + "_" + info.name() +  this.ext + Number );
		
		//this.timeTypeByteLength = LSLUtils.getTimeMarkBytes();
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

		//this.dataTypeByteLength = LSLUtils.getDataTypeBytes( super.LSLFormatData );
		
		this.out = new BufferedOutputStream( new FileOutputStream( this.file ) );
	}
	
	@Override
	protected void startUp() throws Exception 
	{
		StreamHeader header = new StreamHeader( this.file.getAbsolutePath()
												, super.LSLName
												, super.LSLFormatData
												, super.timeType
												, super.lslChannelCounts
												, super.chunckLength
												, super.interleavedData
												, super.lslXML
												, this.outFileFormat
												, this.outFileName
												, !ConfigApp.isTesting() );
		
		String binHeader = header.getStreamBinHeader();
		
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
		EventInfo event = new EventInfo( this.getID(), GetFinalOutEvent(), this.getTemporalFileData() );

		/*		
		this.events.add(event);
		
		if (this.monitor != null)
		{
			this.monitor.taskDone(this);
		}
		*/
		
		super.notifTask.addEvent( event );
		super.notifTask.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
		synchronized ( super.notifTask )
		{
			super.notifTask.notify();
		}
	}
	
	public static String GetFinalOutEvent()
	{
		return EventType.SAVED_OUTPUT_TEMPORAL_FILE;
	}
	
	
	public void setOutputFileFormat( String fileFormat )
	{
		this.outFileFormat = fileFormat;
	}
		
	private TemporalBinData getTemporalFileData() throws Exception
	{		
				
		TemporalBinData data = new TemporalBinData( this.file
												//, this.timeStampFile
												, super.LSLFormatData
												, super.lslChannelCounts //+ 1
												, super.chunckLength
												, super.interleavedData
												, super.timeType
												, super.LSLName
												, super.lslXML
												, this.outFileName
												, this.outFileFormat
												, !ConfigApp.isTesting() );
		return data;
	}

	@Override
	public String getID() 
	{
		return this.getName();
	}
}