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
package testing.DataStream.OutputDataFile;

import java.util.ArrayList;
import java.util.List;

import Auxiliar.extra.Tuple;
import Auxiliar.tasks.INotificationTask;
import Auxiliar.tasks.ITaskMonitor;
import Controls.Messages.EventInfo;
import Controls.Messages.EventType;
import DataStream.Binary.Input.writer.TemporalOutDataFileWriter;
import DataStream.Binary.reader.TemporalBinData;
import DataStream.OutputDataFile.OutputBinaryFileSegmentation;
import DataStream.Sync.SyncMarker;
import DataStream.Sync.SyncMarkerBinFileReader;
import DataStream.Sync.SyncMarkerCollectorWriter;
import DataStream.Sync.LSL.InputSyncData;
import StoppableThread.IStoppableThread;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLConfigParameters;
import edu.ucsd.sccn.LSL.StreamInfo;
import testing.LSLSender.LSLSimulationParameters;
import testing.LSLSender.LSLSimulationStreaming;

public class testingOutBinFileSegmentation implements ITaskMonitor
{
	public static testingOutBinFileSegmentation main;

	public static String syncName = "LSLSyncSender"; 
	
	static SyncMarkerCollectorWriter syncCollector = null;
	
	static List< LSLSimulationParameters > cfgs;
	
	public static void main(String[] args) 
	{
		try
		{	
			syncCollector = new SyncMarkerCollectorWriter( "G:/testSync.bin" );
			syncCollector.startThread();
			
			main = new testingOutBinFileSegmentation();
			
			Thread t = new Thread()
			{
				public void run() 
				{
					try 
					{
						cfgs = new ArrayList< LSLSimulationParameters >();
						
						LSLSimulationParameters cfg = new LSLSimulationParameters( );
						cfg.setSamplingRate( 32 );
						cfg.setOutDataType( LSL.ChannelFormat.float32 );
						cfg.setNumberOutputBlocks( (int)(cfg.getSamplingRate() * 20 ));
						cfg.setChannelNumber( 1 );
						cfg.setStreamName( "LSLSender-1");
						cfg.setOutputFunctionType( LSLSimulationParameters.SIN );
						
						cfgs.add( cfg );
						
						cfg = new LSLSimulationParameters( );
						cfg.setSamplingRate( 32 );
						cfg.setOutDataType( LSL.ChannelFormat.float32 );
						cfg.setNumberOutputBlocks( ((int)cfg.getSamplingRate()) * 20 );
						cfg.setChannelNumber( 2 );
						cfg.setStreamName( "LSLSender-2");
						cfg.setOutputFunctionType( LSLSimulationParameters.SIN );
						
						cfgs.add( cfg );
						
						cfg = new LSLSimulationParameters( );
						cfg.setSamplingRate( 32 );
						cfg.setOutDataType( LSL.ChannelFormat.float32 );
						cfg.setNumberOutputBlocks( ((int)cfg.getSamplingRate())  * 20 );
						cfg.setChannelNumber( 3 );
						cfg.setStreamName( "LSLSender-3");
						cfg.setOutputFunctionType( LSLSimulationParameters.SIN );
						
						cfgs.add( cfg );
						
						cfg = new LSLSimulationParameters( );
						cfg.setSamplingRate( 32 );
						cfg.setOutDataType( LSL.ChannelFormat.float32 );
						cfg.setNumberOutputBlocks( ((int)cfg.getSamplingRate())  * 20 );
						cfg.setChannelNumber( 4 );
						cfg.setStreamName( "LSLSender-4");
						cfg.setOutputFunctionType( LSLSimulationParameters.SIN );
						
						cfgs.add( cfg );
						
						cfg = new LSLSimulationParameters( );
						cfg.setSamplingRate( 128 );
						cfg.setOutDataType( LSL.ChannelFormat.int32 );
						cfg.setNumberOutputBlocks( ((int)cfg.getSamplingRate())  * 30 );
						cfg.setChannelNumber( 1 );
						cfg.setStreamName( "LSLSyncSender");
						cfg.setOutputFunctionType( LSLSimulationParameters.LINEAR );
						
						cfgs.add( cfg );
						
						testLSLSender( cfgs, 16000 );
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
			
			List< Tuple< StreamInfo, MutableDataStreamSetting > > LSLthreadList = new ArrayList< Tuple< StreamInfo, MutableDataStreamSetting > >();
			
			LSL.StreamInfo[] results = LSL.resolve_streams();
			
			if( results.length >= 0 )
			{
				for( LSL.StreamInfo info : results )
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
					
					MutableDataStreamSetting par = new MutableDataStreamSetting( info.uid()
																		, info.name()
																		, info.type()
																		, info.source_id()
																		, info.as_xml()
																		, true
																		, chuckSize
																		, false
																		, false
																		, info.nominal_srate() );	
					
					LSLthreadList.add( new Tuple<LSL.StreamInfo, MutableDataStreamSetting>( info, par ) );					
				}
			}
			
			List< TemporalOutDataFileWriter > writers = new ArrayList< TemporalOutDataFileWriter>();
			List< InputSyncData > syncs = new ArrayList< InputSyncData >();
			
			for( int i = 0; i < LSLthreadList.size(); i++ )
			{
				Tuple< LSL.StreamInfo, MutableDataStreamSetting > cfg = LSLthreadList.get( i );
				
				if( cfg.x.channel_count() == 1 && cfg.x.channel_format() ==LSL.ChannelFormat.int32 )
				{				
					InputSyncData syncData = new InputSyncData( cfg.x, cfg.y );
					syncData.taskMonitor( main );
										
					syncs.add( syncData );
					
					Thread t = new Thread()
					{
						public void run() 
						{
							try 
							{
								syncData.startThread();
							}
							catch (Exception e) 
							{
								e.printStackTrace();
							}
						}; 
					};
					
					t.start();
				}
				else
				{
					TemporalOutDataFileWriter wr = new TemporalOutDataFileWriter( "G:/test" + i + ".clis" , cfg.x, cfg.y, i );
					wr.taskMonitor( main );				
					
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
			}
			
			
			if( waitToStop <= 0 )
			{
				waitToStop = 1000L;
			}
			
			Thread.sleep( waitToStop );
			
			for( InputSyncData s : syncs )
			{
				s.stopThread( IStoppableThread.FORCE_STOP );
			}
			
			for( TemporalOutDataFileWriter wr : writers )
			{
				wr.stopThread( IStoppableThread.FORCE_STOP );
			}
			
			for( LSLSimulationStreaming str : lslOutStream )
			{
				str.stopThread( IStoppableThread.FORCE_STOP );
			}
			
			System.out.println( "Test " + cfgs.size() + " END" );
			
		}
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	static List< TemporalBinData > dat = new ArrayList< TemporalBinData >();
	
	int NSaving = 0;
	int count = 0;
	@Override
	public synchronized void taskDone(INotificationTask task) throws Exception 
	{
		List< EventInfo > ev = task.getResult( true );
			
		for( EventInfo e : ev )
		{
			if( e.getEventType().equals( TemporalOutDataFileWriter.GetFinalOutEvent() ) )
			{
				syncCollector.stopThread( IStoppableThread.FORCE_STOP );
				
				NSaving++;
				
				SyncMarkerBinFileReader reader = null;
				
				while( reader == null )
				{
					try
					{
						super.wait( 100L );
					}
					catch (Exception ex) 
					{
					}
					
					reader = syncCollector.getSyncMarkerBinFileReader();
				}
				
				TemporalBinData tempData = (TemporalBinData)e.getEventInformation();
								
				OutputBinaryFileSegmentation seg = new OutputBinaryFileSegmentation( tempData, reader );
				seg.taskMonitor( main );

				seg.startThread();
			}			
			else if( e.getEventType().equals( EventType.OUTPUT_DATA_FILE_SAVED )) 
			{
				System.out.println("testingOutBinFileSegmentation.taskDone() OUTPUT_DATA_FILE_SAVED " + e.getEventInformation() );
				NSaving--;
				
				if( NSaving < 1 )
				{
					SyncMarkerBinFileReader r = syncCollector.getSyncMarkerBinFileReader();
					if( r != null )
					{
						r.closeAndRemoveTempBinaryFile();
					}
					
					System.out.println("testingOutBinFileSegmentation.taskDone() Aprox. N. saved sync = " + count );
				}
			}
			else if( e.getEventType().equals( EventType.INPUT_MARK_READY ) )
			{
				System.out.println("testingOutBinFileSegmentation.taskDone() " + e.getEventInformation() );
				count++;
				syncCollector.SaveSyncMarker( (SyncMarker)e.getEventInformation() );
			}
		}
		
	}
}
