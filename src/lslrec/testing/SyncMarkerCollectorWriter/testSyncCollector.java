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
package lslrec.testing.SyncMarkerCollectorWriter;

import java.util.ArrayList;
import java.util.List;

import lslrec.auxiliar.tasks.INotificationTask;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.controls.messages.EventInfo;
import lslrec.controls.messages.EventType;
import lslrec.dataStream.sync.SyncMarker;
import lslrec.dataStream.sync.SyncMarkerBinFileReader;
import lslrec.dataStream.sync.SyncMarkerCollectorWriter;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;


public class testSyncCollector 
{	
	static SyncMarkerCollectorWriter collector = null;
	
	public static void main(String[] args) 
	{
		try
		{
			for( int i = 0; i < 5; i++ )
			{
				System.out.println( "\nTest " + i + " sender");
				
				Thread.sleep( 500L );
				
				final int N = i;
				
				Thread t = new Thread()
				{
					public void run() 
					{ 
						try 
						{
							testNSender( N );
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
			
		}
		catch( Exception e )
		{
			e.printStackTrace();
		}
	}
		
	private static void testNSender( int N ) throws Exception
	{
		collector = new SyncMarkerCollectorWriter( "G:/test.sync" );
		
		try 
		{
			Reader r = new Reader();
			
			SyncReceiver receive = new SyncReceiver( collector );
			
			collector.taskMonitor( r );
			
			collector.startThread();			
			
			if( N < 1 )
			{
				collector.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
			}
			else
			{
				List< Sender > senders = new ArrayList< Sender >();
				
				for( int i = 0; i < N; i++)
				{			
					Sender send = new Sender( 1 );
					
					send.setName( "Sender " + i);
					send.taskMonitor( receive );
					
					senders.add( send );
					
					send.startThread();
				}
				
				Thread t = new Thread()
				{
					public void run() 
					{
						try 
						{
							for( Sender send : senders )
							{
								send.join();
							}
							
							collector.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
						}
						catch (InterruptedException e) 
						{
							e.printStackTrace();
						}
					};
				};
				
				t.start();
			}						
		}
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally 
		{
			try 
			{
				collector.join();
			}
			catch (InterruptedException e) 
			{
				e.printStackTrace();
			}
			
			System.out.println( "\nTest " + N + " sender END");
		}
	}
	
	public static class SyncReceiver implements ITaskMonitor
	{
		SyncMarkerCollectorWriter col = null; 
		List< SyncMarker > syncList = new ArrayList<SyncMarker>();
		
		public SyncReceiver( SyncMarkerCollectorWriter c ) 
		{
			col = c;
		}
		
		@Override
		public synchronized void taskDone(INotificationTask task) throws Exception 
		{
			List< EventInfo > EV = new ArrayList<EventInfo>( task.getResult( true ) );
			
			for( EventInfo ev : EV )
			{
				if( ev.getEventType().equals( EventType.INPUT_MARK_READY ) )
				{
					System.out.println("testSyncCollector.SyncReceiver.taskDone() " + ev.getEventInformation() );
					
					col.SaveSyncMarker( (SyncMarker)ev.getEventInformation() );
				}
				else if(ev.getEventType().equals( EventType.PROBLEM ) )
				{
					throw new Exception( ev.getEventInformation() + "" );
				}
			}			
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
						
			EventInfo ev = new EventInfo( this.getID(), EventType.INPUT_MARK_READY, sync );
			
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
							sync = reader.getSyncMarker();
							System.out.println("\t" + sync );
						}
						while( sync != null );
						
						reader.closeAndRemoveTempBinaryFile();
							
						
						break;
					}
					default:
						break;
				}
			}
		}		
	}
	
}
