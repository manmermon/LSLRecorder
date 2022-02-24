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
 */
package lslrec.testing.DataStream.Binary;

import java.util.ArrayList;
import java.util.List;

import com.sun.jna.platform.unix.X11.XClientMessageEvent.Data;

import lslrec.auxiliar.extra.Tuple;
import lslrec.dataStream.binary.input.writer.TemporalOutDataFileWriter;
import lslrec.dataStream.family.DataStreamFactory;
import lslrec.dataStream.family.setting.IMutableStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting.StreamLibrary;
import lslrec.dataStream.family.setting.MutableStreamSetting;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.dataStream.tools.StreamUtils.StreamDataType;
import lslrec.stoppableThread.IStoppableThread;
import lslrec.testing.LSLSender.LSLSimulationParameters;
import lslrec.testing.LSLSender.LSLSimulationStreaming;


public class testingTemporalOutDataFileWriter 
{
	public static void main(String[] args) 
	{
		try
		{			
			Thread t = new Thread()
			{
				public void run() 
				{
					try 
					{
						List< LSLSimulationParameters > cfgs = new ArrayList< LSLSimulationParameters >();
						
						LSLSimulationParameters cfg = new LSLSimulationParameters( );
						cfg.setSamplingRate( 32 );
						cfg.setOutDataType( StreamDataType.float32 );
						cfg.setNumberOutputBlocks( 32 * 20 );
						cfg.setChannelNumber( 1 );
						cfg.setStreamName( "LSLSender-1");
						cfg.setOutputFunctionType( LSLSimulationParameters.SIN );
						
						cfgs.add( cfg );
						
						cfg = new LSLSimulationParameters( );
						cfg.setSamplingRate( 128 );
						cfg.setOutDataType( StreamDataType.int32 );
						cfg.setNumberOutputBlocks( 128 * 30 );
						cfg.setChannelNumber( 1 );
						cfg.setStreamName( "LSLSyncSender");
						cfg.setOutputFunctionType( LSLSimulationParameters.LINEAR );
						
						cfgs.add( cfg );
						
						testLSLSender( cfgs, 8000 );
					} 
					catch (Exception e) 
					{
						e.printStackTrace();
					}
				};
			};
			
			t.setName( "Launch" );
			t.start();
						
			t.join();
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private static void testLSLSender( List< LSLSimulationParameters > cfgs, long waitToStop ) throws Exception
	{	
		try 
		{	
			System.out.println( "Test de " + cfgs.size() + " streams" );
			
			List< LSLSimulationStreaming > lslOutStream = new ArrayList<LSLSimulationStreaming>();
			for( LSLSimulationParameters par : cfgs )
			{
				LSLSimulationStreaming stream = new LSLSimulationStreaming( par );
				stream.setName( par.getStreamName() );
				
				lslOutStream.add( stream );
				
				stream.startThread();				
			}
			
			List< IStreamSetting > LSLthreadList = new ArrayList< IStreamSetting >();
			
			IStreamSetting[] results = DataStreamFactory.createStreamSettings( StreamLibrary.LSL, DataStreamFactory.TIME_FOREVER );
			
			if( results.length >= 0 )
			{
				for( IStreamSetting info : results )
				{
					int chuckSize = 1;
					
					int i = 0;
					for( LSLSimulationStreaming st : lslOutStream )
					{
						if( st.getName().equals( info.name() ))
						{
							if( i < cfgs.size() )
							{
								chuckSize = cfgs.get( i ).getBlockSize();
							}
							
							break;
						}
						
						i++;
					}
					
					MutableStreamSetting par = new MutableStreamSetting( info );
					par.setChunckSize( chuckSize );
					par.setSelected( true );	
					
					LSLthreadList.add( par );
				}
			}
			
			List< TemporalOutDataFileWriter > writers = new ArrayList< TemporalOutDataFileWriter>();
			for( int i = 0; i < LSLthreadList.size(); i++ )
			{
				IStreamSetting cfg = LSLthreadList.get( i );
				
				TemporalOutDataFileWriter wr = new TemporalOutDataFileWriter( cfg, DataFileFormat.getDefaultOutputFileFormatParameters(), i );
				
				writers.add( wr );
				
				Thread t = new Thread()
				{
					public void run() 
					{
						try 
						{
							wr.startThread();
						}
						catch (Exception e) 
						{
							e.printStackTrace();
						}
					}; 
				};
				
				t.start();
			}
			
			
			if( waitToStop <= 0 )
			{
				waitToStop = 1000L;
			}
			
			Thread.sleep( waitToStop );
			
			for( TemporalOutDataFileWriter wr : writers )
			{
				wr.stopThread( IStoppableThread.FORCE_STOP );
			}
			
			for( LSLSimulationStreaming str : lslOutStream )
			{
				str.stopThread( IStoppableThread.FORCE_STOP );
			}
			
			System.out.println( "Test END" );
			
		}
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
