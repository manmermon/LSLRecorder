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

package InputStreamReader.Binary;

import Config.ConfigApp;
import Controls.Messages.EventInfo;
import Controls.Messages.EventType;
import InputStreamReader.OutputFileUtils;
import InputStreamReader.TemporalData;
import InputStreamReader.OutputDataFile.Format.DataFileFormat;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLConfigParameters;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.annotation.processing.FilerException;

public class TemporalOutDataFileWriter extends LSLInStreamDataReceiverTemplate
{
	protected File file = null;

	private BufferedOutputStream out; 

	private String ext = ".temp";
	
	private String outFileName = "";
	private String outFileFormat = DataFileFormat.CLIS;
	
	//private int dataTypeByteLength = 1;
	//private int timeTypeByteLength = 1;	
	
	private final String headerBinSeparator = ConfigApp.HEADER_SEPARATOR ;
	
	public TemporalOutDataFileWriter( String filePath, LSL.StreamInfo info
										, LSLConfigParameters lslCfg, int Number ) throws Exception
	{
		super( info, lslCfg );
		
		String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());

		this.outFileName = filePath;
		
		super.setName( info.name() + "(" + info.uid() + ")");

		this.file = OutputFileUtils.CreateTemporalBinFile( filePath + "_" + date + "_" + info.name() +  this.ext + Number );
		
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
		
		super.startUp();
	}
	
	protected void managerData( byte[] data, byte[] time ) throws Exception
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

	protected void postCleanUp() throws Exception
	{	
		this.out.close();		
		
		EventInfo event = new EventInfo( GetFinalOutEvent(), this.getTemporalFileData() );

		this.events.add(event);
		
		if (this.monitor != null)
		{
			this.monitor.taskDone(this);
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
		
	private TemporalData getTemporalFileData() throws Exception
	{		
				
		TemporalData data = new TemporalData( this.file
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