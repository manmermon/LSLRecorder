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
package lslrec.testing.DataStream.OutputDataFile;

import java.util.ArrayList;
import java.util.List;

import lslrec.auxiliar.tasks.INotificationTask;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.controls.messages.EventInfo;
import lslrec.controls.messages.EventType;
import lslrec.dataStream.binary.input.writer.TemporalOutDataFileWriter;
import lslrec.dataStream.binary.reader.TemporalBinData;
import lslrec.dataStream.family.DataStreamFactory;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting.StreamLibrary;
import lslrec.dataStream.family.setting.MutableStreamSetting;
import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;
import lslrec.dataStream.outputDataFile.OutputBinaryFileSegmentation;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.dataStream.sync.SyncMarker;
import lslrec.dataStream.sync.SyncMarkerBinFileReader;
import lslrec.dataStream.sync.SyncMarkerCollectorWriter;
import lslrec.dataStream.sync.dataStream.InputSyncData;
import lslrec.stoppableThread.IStoppableThread;
import lslrec.testing.LSLSender.LSLSimulationParameters;
import lslrec.testing.LSLSender.LSLSimulationStreaming;


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
						cfg.setOutDataType( StreamDataType.float32 );
						cfg.setNumberOutputBlocks( (int)(cfg.getSamplingRate() * 20 ));
						cfg.setChannelNumber( 1 );
						cfg.setStreamName( "LSLSender-1");
						cfg.setOutputFunctionType( LSLSimulationParameters.SIN );
						
						cfgs.add( cfg );
						
						cfg = new LSLSimulationParameters( );
						cfg.setSamplingRate( 32 );
						cfg.setOutDataType( StreamDataType.float32 );
						cfg.setNumberOutputBlocks( ((int)cfg.getSamplingRate()) * 20 );
						cfg.setChannelNumber( 2 );
						cfg.setStreamName( "LSLSender-2");
						cfg.setOutputFunctionType( LSLSimulationParameters.SIN );
						
						cfgs.add( cfg );
						
						cfg = new LSLSimulationParameters( );
						cfg.setSamplingRate( 32 );
						cfg.setOutDataType( StreamDataType.float32 );
						cfg.setNumberOutputBlocks( ((int)cfg.getSamplingRate())  * 20 );
						cfg.setChannelNumber( 3 );
						cfg.setStreamName( "LSLSender-3");
						cfg.setOutputFunctionType( LSLSimulationParameters.SIN );
						
						cfgs.add( cfg );
						
						cfg = new LSLSimulationParameters( );
						cfg.setSamplingRate( 32 );
						cfg.setOutDataType( StreamDataType.float32 );
						cfg.setNumberOutputBlocks( ((int)cfg.getSamplingRate())  * 20 );
						cfg.setChannelNumber( 4 );
						cfg.setStreamName( "LSLSender-4");
						cfg.setOutputFunctionType( LSLSimulationParameters.SIN );
						
						cfgs.add( cfg );
						
						cfg = new LSLSimulationParameters( );
						cfg.setSamplingRate( 128 );
						cfg.setOutDataType( StreamDataType.int32 );
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
			
			List< MutableStreamSetting > LSLthreadList = new ArrayList< MutableStreamSetting >();
			
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
					par.setSelected( true );
					
					LSLthreadList.add( par );					
				}
			}
			
			List< TemporalOutDataFileWriter > writers = new ArrayList< TemporalOutDataFileWriter>();
			List< InputSyncData > syncs = new ArrayList< InputSyncData >();
			
			for( int i = 0; i < LSLthreadList.size(); i++ )
			{
				MutableStreamSetting cfg = LSLthreadList.get( i );
				
				if( cfg.channel_count() == 1 && cfg.data_type() ==StreamDataType.int32 )
				{				
					InputSyncData syncData = new InputSyncData( cfg );
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
					TemporalOutDataFileWriter wr = new TemporalOutDataFileWriter( cfg, DataFileFormat.getDefaultOutputFileFormatParameters(), i );
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
