/* 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
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

package DataStream.Sync.LSL;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import Auxiliar.Extra.ConvertTo;
import Auxiliar.Tasks.ITaskMonitor;
import Auxiliar.Tasks.BridgeNotifierThread;
import Controls.Messages.EventInfo;
import Controls.Messages.EventType;
import DataStream.Binary.LSLInStreamDataReceiverTemplate;
import DataStream.Sync.SyncMarker;
import StoppableThread.IStoppableThread;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLConfigParameters;
import edu.ucsd.sccn.LSLUtils;
import edu.ucsd.sccn.LSL.StreamInfo;

public class InputSyncData extends LSLInStreamDataReceiverTemplate
{	
	private BridgeNotifierThread notifierThread; // Notification thread. Avoid blocks.
	
	/*
	 * 
	 */
	public InputSyncData( StreamInfo info, LSLConfigParameters lslCfg ) throws Exception 
	{
		super( info, lslCfg );		
						
		super.setName( info.name() + "(" + info.uid() + ")");
		
		if( info.channel_count() > 1 || info.channel_format() != LSL.ChannelFormat.int32 )
		{
			throw new IllegalArgumentException( this.getClass().getName() + 
													" - Incorrect LSL setting: number of channels = 1"
													+ " and format data must be integer (32 bits)." );
		}		
	}
	
	/*
	 * (non-Javadoc)
	 * @see OutputDataFile.readInputData#taskMonitor(Auxiliar.Tasks.ITaskMonitor)
	 */
	@Override
	public void taskMonitor(ITaskMonitor m) 
	{
		super.taskMonitor( m );
		
		this.notifierThread = new BridgeNotifierThread( m,  this );
		this.notifierThread.setName( this.notifierThread.getClass().getName() + "-" + this.getClass().getName() );
	}

	/*
	 * (non-Javadoc)
	 * @see OutputDataFile.readInputData#preStart()
	 */
	@Override
	protected void preStart() throws Exception 
	{
		super.preStart();
		
		if( this.notifierThread == null )
		{
			throw new IllegalStateException( "Monitor non defined. Use taskMonitor( ... ) function to set it." );
		}
		else
		{
			this.notifierThread.startThread();
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see OutputDataFile.readInputData#postCleanUp()
	 */
	@Override
	protected void postCleanUp() throws Exception 
	{
		this.notifierThread.stopThread( IStoppableThread.FORCE_STOP );
		
		//this.notifierThread = null;
	}

	/*
	 * (non-Javadoc)
	 * @see OutputDataFile.readInputData#managerData(byte[])
	 */
	@Override
	protected void managerData( byte[] dataArrayOfBytes, byte[] timeArrayOfBytes ) throws Exception 
	{
		int nBytes = Integer.BYTES;
		
		byte[] markBuffer = new byte[ nBytes ];
		for( int i = 0; i < nBytes; i++ )
		{
			markBuffer[ i ] = dataArrayOfBytes[ i ];
		}
		
		int mark = ByteBuffer.wrap( markBuffer ).order( ByteOrder.BIG_ENDIAN ).getInt();
		double time = ConvertTo.ByteArray2Double( Arrays.copyOfRange( timeArrayOfBytes, 0, LSLUtils.getTimeMarkBytes() ) );		
		
		//EventInfo event = new EventInfo( eventType.INPUT_MARK_READY, new Tuple< Integer, Double >( mark, super.timeMark[ 0 ] ) );	
		EventInfo event = new EventInfo( this.getID(), EventType.INPUT_MARK_READY, new SyncMarker( mark, time ) );
		
		synchronized ( super.events )
		{
			super.events.add(event);
		}		
		
		Thread antiDeadlock = new Thread()
				{
					@Override
					public synchronized void run() 
					{
						synchronized ( notifierThread )
						{
							notifierThread.notify();
						}
					}					
				};
			
		antiDeadlock.start();
		
	}
	
	@Override
	public String getID() 
	{
		return this.getName();
	}

}
