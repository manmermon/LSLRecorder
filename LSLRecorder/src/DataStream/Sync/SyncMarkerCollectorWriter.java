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
package DataStream.Sync;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

import Auxiliar.Extra.FileUtils;
import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskMonitor;
import Config.ConfigApp;
import Controls.Messages.EventInfo;
import Controls.Messages.EventType;
import DataStream.StreamHeader;
import GUI.Miscellany.ArrayTreeMap;
import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;
import edu.ucsd.sccn.LSL;

public class SyncMarkerCollectorWriter extends AbstractStoppableThread implements INotificationTask
{	
	private File syncFileDisordered = null;
	
	private ConcurrentLinkedDeque< SyncMarker > markerList = null;	

	private DataOutputStream outDisorderedStream = null;	
	
	private List< EventInfo > events;
	
	private ITaskMonitor monitor = null;
	
	private String outFileName = null;
	
	//private List< Tuple< LSL.StreamInfo, LSLConfigParameters > > inStreams = null;
	
	//private List< InputSyncData > syncInputData = null;
	
	private String ext = ".sync";
	
	private StreamHeader header = null;
	
	public SyncMarkerCollectorWriter( String file ) throws Exception 
	{
		String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
		
		super.setName( this.getClass().getSimpleName() );
		
		this.outFileName = file + "_" + date + this.ext;
		
		this.syncFileDisordered = FileUtils.CreateTemporalBinFile( file + "_" + date + "_disordered" + this.ext);
		
		this.outDisorderedStream = new DataOutputStream( new FileOutputStream( this.syncFileDisordered ) );
		
		this.markerList = new ConcurrentLinkedDeque< SyncMarker >();
		
		this.events = new ArrayList< EventInfo >();
		
		this.header = new StreamHeader( file
										, super.getClass().getSimpleName()
										, SyncMarker.MARK_DATA_TYPE
										, SyncMarker.MARK_TIME_TYPE
										, -1
										, 1
										, 1
										, false
										, ""
										, ""
										, ""
										, !ConfigApp.isTesting() );	
				
		//this.syncInputData = new ArrayList< InputSyncData >();
	}
	
	/*
	public void SetLSLInStream( List< Tuple< LSL.StreamInfo, LSLConfigParameters > > inLets )
	{
		if( super.getState().equals(Thread.State.NEW ) )
		{
			this.inStreams = inLets;
		}
	}
	*/

	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{	
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{	
		if( friendliness == IStoppableThread.STOP_WITH_TASKDONE )
		{
			synchronized ( this )
			{
				if( super.getState().equals( Thread.State.WAITING ) )
				{
					super.notify();
				}
			}
		}
	}
	
	/*
	@Override
	protected void preStart() throws Exception 
	{
		if( this.inStreams != null && !this.inStreams.isEmpty() )
		{	
			for ( int indexInlets = 0; indexInlets < this.inStreams.size(); indexInlets++)
			{
				Tuple< LSL.StreamInfo, LSLConfigParameters > t = this.inStreams.get( indexInlets );
				
				LSL.StreamInfo inletInfo = t.x;
				
				if( t.y.isSynchronationStream() )
				{
					InputSyncData sync = new InputSyncData( inletInfo, t.y );
										
					this.syncInputData.add( sync );
				}
			}
		}
		
		this.outDisorderedStream = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( this.syncFileDisordered ) ) );
	}
	*/
	
	/*
	@Override
	protected void startUp() throws Exception 
	{
		super.startUp();
		
		if( this.syncInputData != null  )
		{			
			synchronized ( this.syncInputData )
			{			
				for( InputSyncData sync : this.syncInputData )
				{	
					sync.taskMonitor( this );
					
					LaunchThread tLaunch = new LaunchThread( sync );
					tLaunch.taskMonitor( this );
					
					tLaunch.startThread();	
				}
			}	
		}
	}
	*/
	
	@Override
	protected void startUp() throws Exception 
	{	
		this.outDisorderedStream.write( this.header.getStreamBinHeader().getBytes() );
		
		super.startUp();
	}
	
	@Override
	protected void runInLoop() throws Exception 
	{
		try
		{
			synchronized ( this )
			{
				if( this.markerList.isEmpty() )
				{
					super.wait( 1000L );
				}
			}
		}
		catch ( InterruptedException e) 
		{
		}
		
		while( !this.markerList.isEmpty() )
		{
			SyncMarker mark = this.markerList.pollFirst();
						
			if( this.outDisorderedStream != null )
			{				
				this.outDisorderedStream.writeInt( mark.getMarkValue() );
				this.outDisorderedStream.writeDouble( mark.getTimeMarkValue() );
				
				
				if( this.monitor != null )
				{
					synchronized ( this.events )
					{
						this.events.add( new EventInfo( this.getID(), EventType.INPUT_MARK_READY, mark ) );
					}
				
					this.monitor.taskDone( this );					
				}
			}
		}
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{
		/*
		if( !this.syncInputData.isEmpty() )
		{
			for( InputSyncData sync : this.syncInputData )
			{
				sync.stopThread( IStoppableThread.FORCE_STOP );
			}
		}
		*/
		
		while( !this.markerList.isEmpty() )
		{
			SyncMarker mark = this.markerList.pollFirst();

			if( this.outDisorderedStream != null )
			{
				this.outDisorderedStream.writeInt( mark.getMarkValue() );
				this.outDisorderedStream.writeDouble( mark.getTimeMarkValue() );
			}
		}
				
		if( this.outDisorderedStream != null )
		{
			this.outDisorderedStream.close();			
		}
		
		sortMarkers( this.syncFileDisordered.getAbsolutePath(), this.outFileName, this.header.getStreamBinHeader(), !ConfigApp.isTesting() );
		
		/*
		EventInfo event = new EventInfo( GetFinalOutEventID(), syncReader );

		this.events.add(event);
		
		if (this.monitor != null)
		{
			this.monitor.taskDone( this ); 
		}
		*/
	}
	
	public SyncMarkerBinFileReader getSyncMarkerBinFileReader() throws Exception
	{
		SyncMarkerBinFileReader reader =  null;
		
		if( super.getState().equals( Thread.State.TERMINATED ) )
		{		
			reader = new SyncMarkerBinFileReader( new File( this.outFileName )
													, LSL.ChannelFormat.int32
													, LSL.ChannelFormat.double64
													, StreamHeader.HEADER_END
													, !ConfigApp.isTesting() );			
		}
		
		return reader;
	}
	
	public static void sortMarkers( String inSyncFileName, String outSynFileName, String newHeader, boolean delInSyncFile ) throws Exception
	{	
		boolean loop = true;
				
		DataOutputStream outStream = new DataOutputStream( new BufferedOutputStream( new FileOutputStream( FileUtils.CreateTemporalBinFile( outSynFileName ) ) ) );
		
		SyncMarkerBinFileReader syncReader = new SyncMarkerBinFileReader( new File( inSyncFileName )
																			, LSL.ChannelFormat.int32
																			, LSL.ChannelFormat.double64	
																			, StreamHeader.HEADER_END
																			, delInSyncFile );
		try 
		{
			String header = syncReader.getHeader();			
			if( newHeader != null )
			{
				header = newHeader;
			}
			
			outStream.write( header.getBytes() );
			
			long freeMemory = Runtime.getRuntime().freeMemory();
			
			if( syncReader.getFileSize() < freeMemory )
			{
				List< SyncMarker > markers = getSyncMarker( syncReader,  -1 ); // All sync markers
				
				Collections.sort( markers );
				
				toSave( outStream, markers);
			}
			else
			{
				int length = (int)( freeMemory / 2 );
				int lim = (int)( 5 * Math.pow( 2, 20 ) );
				
				if( length < lim )
				{
					length = lim;
				}
				
				ArrayTreeMap< Integer, SyncMarker > toShift = new ArrayTreeMap< Integer, SyncMarker >();
				ArrayTreeMap< Integer, SyncMarker > nonSave = new ArrayTreeMap< Integer, SyncMarker >();
				List< Double > refTimeList = new ArrayList< Double >();
				
				int blockCounter = -1;
				while( loop )
				{
					List< SyncMarker > markers = getSyncMarker( syncReader, length  ); // All sync markers					
					
					loop = markers.size() > 0 ;
					
					if( loop )
					{
						blockCounter++;
						
						Collections.sort( markers );
												
						boolean end = false;
						
						for( int j = 0; j < markers.size() && !end; j++ )
						{
							SyncMarker marker = markers.get( j );
							
							boolean inserted = false;
							for( int i = 0; i < refTimeList.size() && !inserted; i++ )
							{
								Double refTime = refTimeList.get( i );

								inserted = ( refTime > marker.getTimeMarkValue() );

								if( inserted )
								{
									nonSave.put( blockCounter, marker );
									toShift.put( i, marker );
								}
							}
							
							end = !inserted;
						}
						
						refTimeList.add( markers.get( markers.size() - 1  ).getTimeMarkValue() );							
					}
				}
				
				if( !toShift.isEmpty() )
				{
					syncReader.resetStream();
					
					blockCounter = 0;
					
					loop = true;
					
					while( loop )
					{
						List< SyncMarker > markers = getSyncMarker( syncReader, length  ); // All sync markers
						blockCounter++;
						
						loop = markers.size() > 0 ;
						
						if( loop )
						{
							List< SyncMarker > toAdd = toShift.get( blockCounter );
							List< SyncMarker > toRemove = nonSave.get( blockCounter );
							
							if( toRemove != null )
							{
								for( SyncMarker deleteMark : toRemove )
								{
									boolean del = false;
									
									Iterator< SyncMarker > itMarkers = markers.iterator();
									
									while( itMarkers.hasNext() && !del )
									{										
										del = itMarkers.next().equals( deleteMark );
										
										if( del )
										{
											itMarkers.remove();
										}
									}
								}
							}
							
							if( toAdd != null )
							{
								markers.addAll( toAdd );
								
								Collections.sort( markers );
							}
							
							toSave( outStream, markers );
						}
					}						
				}
			}
			
			/*
			boolean order = !isOrdered( syncReader );
			syncReader.resetStream();
			
			if( order )
			{	
				SyncMarker mark = null;
				
				do
				{			
					loop = true;
					minTimeValue = Double.POSITIVE_INFINITY;
					
					mark = null;
					
					while( loop )
					{	
						try
						{
							SyncMarker marker = syncReader.getSyncMarker();
							
							if( marker != null )
							{
								Integer markValue = marker.getMarkValue();
								Double timeValue = marker.getTimeMarkValue();
								
								if( timeValue > refTimeValue )
								{
									if( timeValue < minTimeValue )
									{				
										minTimeValue = timeValue;
										mark = new SyncMarker( markValue, timeValue );
									}
									else if( timeValue == minTimeValue )
									{
										if( mark != null )
										{
											markValue = markValue | mark.getMarkValue();
										}
										
										mark = new SyncMarker( markValue, timeValue );
									}
								}
							}
							else
							{
								loop = false;
							}
						}
						catch ( EOFException e) 
						{
							loop = false;
						}
					}
					
					if( mark != null )
					{
						outStream.writeInt( mark.getMarkValue() );
						outStream.writeDouble( mark.getTimeMarkValue() );
										
						refTimeValue = mark.getTimeMarkValue();
						
						syncReader.resetStream();
					}
				}
				while( mark != null );
			}
			else
			{								
				while( loop )
				{
					try
					{
						SyncMarker marker = syncReader.getSyncMarker();
						
						if( marker != null )
						{					
							outStream.writeInt( marker.getMarkValue() );
							outStream.writeDouble( marker.getTimeMarkValue() );
						}
						else
						{
							loop = false;
						}
					}
					catch (Exception e) 
					{
						loop = false;
					}
				}
			}
			*/
			
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			outStream.close();
			syncReader.closeAndRemoveTempBinaryFile();
		}
						
	}
	
	private static void toSave( DataOutputStream outStream, List< SyncMarker > markers ) throws IOException
	{
		if( outStream != null && markers != null )
		{
			for( SyncMarker mark : markers )
			{
				outStream.writeInt( mark.getMarkValue() );
				outStream.writeDouble( mark.getTimeMarkValue() );
			}
		}
	}
	
	private static List< SyncMarker > getSyncMarker( SyncMarkerBinFileReader syncReader, int length )
	{
		List< SyncMarker > markers = new ArrayList< SyncMarker >();
		
		SyncMarker mark = null;
		boolean loop = true;
		int count = 0;
		
		if( length == 0 ) // length <= 0 to get all markers
		{
			length = -1;
		}
		
		while( loop && ( count - length ) != 0 )
		{		
			try
			{
				mark = syncReader.getSyncMarker();

				loop = ( mark != null );
				
				if( loop )
				{
					count++;
					markers.add( mark );
				}
			}
			catch ( Exception e) 
			{
				loop = false;
			}			
		}
		
				
		return markers;
	}
	
	/*
	private static boolean isOrdered( SyncMarkerBinFileReader syncReader )
	{
		boolean ordered = true;
		boolean loop = true;
		
		double refTimeValue = Double.NEGATIVE_INFINITY;
		
		while( loop && ordered )
		{	
			try
			{
				SyncMarker marker = syncReader.getSyncMarker();

				if( marker != null )
				{
					Double timeValue = marker.getTimeMarkValue();


					ordered = timeValue > refTimeValue;
					
					if( ordered )
					{
						refTimeValue = timeValue;
					}
				}
				else
				{
					loop = false;
				}
			}
			catch ( Exception e) 
			{
				loop = false;
			}
		}
		
		return ordered;
	}
	*/
	
	public static String GetFinalOutEventID()
	{
		return EventType.SAVED_SYNCMARKER_TEMPORAL_FILE;
	}
	
	public synchronized void SaveSyncMarker( SyncMarker mark ) throws Exception 
	{
		if( mark != null )
		{
			this.markerList.add( mark );
			
			synchronized ( this ) 
			{
				super.notify();
			}
		}
	}

	@Override
	public void taskMonitor(ITaskMonitor m ) 
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
		return super.getName();
	}
	
	
}
