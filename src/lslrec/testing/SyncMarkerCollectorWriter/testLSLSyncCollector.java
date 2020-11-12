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
package testing.SyncMarkerCollectorWriter;

import java.util.ArrayList;
import java.util.List;

import Auxiliar.extra.Tuple;
import Auxiliar.tasks.INotificationTask;
import Auxiliar.tasks.ITaskMonitor;
import Controls.Messages.EventInfo;
import Controls.Messages.EventType;
import DataStream.Sync.SyncMarker;
import DataStream.Sync.SyncMarkerBinFileReader;
import DataStream.Sync.SyncMarkerCollectorWriter;
import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSL.StreamInfo;
import edu.ucsd.sccn.LSLConfigParameters;
import testing.LSLSender.LSLSimulationParameters;
import testing.LSLSender.LSLSimulationStreaming;

public class testLSLSyncCollector 
{	
	public static void main(String[] args) 
	{
		try
		{
			System.out.println( "\nTest 1 LSL sender");
			
			Thread.sleep( 500L );
			
			Thread t = new Thread()
			{
				public void run() 
				{
					try 
					{
						testLSLSender( 5, 50, 8, 0 );
					} 
					catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				};
			};
			
			t.setName( "Launch" );
			t.start();
			
			t.join();		
			
			System.out.println( "\nTest 2 LSL sender");
			
			Thread.sleep( 500L );
			
			t = new Thread()
			{
				public void run() 
				{
					try 
					{
						testLSLSender( 5, 256 * 20, 256, 8000 );
					} 
					catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				};
			};
			
			t.setName( "Launch" );
			t.start();
						
			t.join();
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
	
	private static void testLSLSender( int N, int size, double f, long waitToStop ) throws Exception
	{
		SyncMarkerCollectorWriter collector = new SyncMarkerCollectorWriter( "G:/test.sync" );
		
		try 
		{	
			List< LSLSimulationStreaming > lslOutStream = new ArrayList<LSLSimulationStreaming>();
			for( int i = 0; i < N; i++ )
			{
				LSLSimulationParameters par = new LSLSimulationParameters( );
				par.setStreamName( "LSLSender-" + i);
				par.setNumberOutputBlocks( size );
				par.setOutDataType( LSL.ChannelFormat.int32 );
				par.setSamplingRate( f );
				par.setOutputFunctionType( LSLSimulationParameters.LINEAR );
												
				LSLSimulationStreaming stream = new LSLSimulationStreaming( par );
				stream.setName( "LSLSender-" + par.getStreamName() );
				
				lslOutStream.add( stream );
				
				stream.startThread();				
			}
			
			List< Tuple< IStreamSetting, IMutableStreamSetting > > LSLthreadList = new ArrayList< Tuple< IStreamSetting, IMutableStreamSetting > >();
			
			IStreamSetting.StreamInfo[] results = LSL.resolve_streams();
			
			if( results.length >= 0 )
			{
				for( IStreamSetting.StreamInfo info : results )
				{
					IMutableStreamSetting par = new IMutableStreamSetting( info.uid()
																		, info.name()
																		, info.type()
																		, info.source_id()
																		, info.as_xml()
																		, true
																		, 1
																		, false
																		, true
																		, info.nominal_srate() );	
					
					LSLthreadList.add( new Tuple<IStreamSetting.StreamInfo, IMutableStreamSetting>( info, par ) );
				}
			}
			
			Reader r = new Reader();
			
			collector.taskMonitor( r );
			
			collector.setName( "collector" );
									
			Thread t = new Thread()
			{
				public void run() 
				{
					try 
					{
						collector.startThread();
						collector.join();
					
						collector.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
					}
					catch (Exception e) 
					{
						e.printStackTrace();
					}
					finally 
					{
						System.out.println("TEST END");
					}
				};
			};
			
			t.start();
			
			if( waitToStop > 0 )
			{
				Thread.sleep( waitToStop );
				
				collector.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
				
				for( LSLSimulationStreaming str : lslOutStream )
				{
					str.stopThread( IStoppableThread.FORCE_STOP );
				}
			}
			else
			{
				t.join();
			}
			
		}
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static class Sender extends AbstractStoppableThread implements ITaskMonitor, INotificationTask 
	{	
		private int mark = 0;
		private ITaskMonitor monitor;
		private int inc = 1;
		
		private List< EventInfo > events = null;
		
		public Sender( int increment ) 
		{
			this.events = new ArrayList<EventInfo>();
			
			inc = increment;
		}
		
		@Override
		public void taskMonitor(ITaskMonitor m)
		{	
			this.monitor = m;
		}

		@Override
		public List<EventInfo> getResult( boolean clear ) 
		{
			List< EventInfo > evs = new ArrayList< EventInfo >();
			
			synchronized ( this.events )
			{
				evs.addAll( this.events );
				
				if( clear )
				{
					this.events.clear();
				}
			}
			
			return evs;
		}

		@Override
		public void clearResult() 
		{
			synchronized ( this.events )
			{
				this.events.clear();
			}
		}

		@Override
		public String getID() 
		{
			return super.getClass().getSimpleName();
		}

		@Override
		public void taskDone(INotificationTask task) throws Exception 
		{			
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
			double t = Math.random(); //System.nanoTime() / 1e9D;
			this.mark += this.inc;
			
			SyncMarker sync = new SyncMarker( mark, t );
						
			EventInfo ev = new EventInfo( this.getClass().getName(), EventType.INPUT_MARK_READY, sync );
			
			this.events.add( ev );
			
			if( this.monitor != null )
			{
				this.monitor.taskDone( this );
			}
			
			super.wait( 100L );
		}
		
		@Override
		protected void targetDone() throws Exception 
		{
			if( this.mark >= 101 )
			{
				super.stopThread = true;
			}
		}

	}

	public static class Reader implements ITaskMonitor
	{		
		public Reader( ) 
		{
			
		}

		@Override
		public void taskDone(INotificationTask task) throws Exception 
		{
			List< EventInfo > EVS = task.getResult( true );
			
			for( EventInfo ev : EVS )
			{
				switch ( ev.getEventType() )
				{
					case EventType.SAVED_SYNCMARKER_TEMPORAL_FILE:
					{
						SyncMarkerBinFileReader reader = ( SyncMarkerBinFileReader)ev.getEventInformation();
						
						System.out.println("\ntestSyncCollector.Reader.taskDone() File Size = " + reader.getFileSize());
						
						SyncMarker sync = null;
						
						do
						{	
							sync = reader.getSyncMarkerMethod();
							System.out.println("\t" + sync );
						}
						while( sync != null );
						
						reader.closeAndRemoveTempBinaryFile();
							
						
						break;
					}
					case EventType.INPUT_MARK_READY:
					{
						SyncMarker mark = (SyncMarker)ev.getEventInformation();
						
						System.out.println("testLSLSyncCollector.Reader.taskDone() " + mark);
						
						break;
					}
					case EventType.PROBLEM:
					{
						if( task instanceof SyncMarkerCollectorWriter )
						{
							((SyncMarkerCollectorWriter)task).stopThread( IStoppableThread.STOP_WITH_TASKDONE );
						}
						
						break;
					}
					default:
						break;
				}
			}
		}		
	}
	
}
