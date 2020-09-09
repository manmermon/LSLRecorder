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
package testing.DataStream.Binary;

import java.util.ArrayList;
import java.util.List;

import Auxiliar.extra.Tuple;
import DataStream.Binary.Input.writer.TemporalOutDataFileWriter;
import StoppableThread.IStoppableThread;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLConfigParameters;
import edu.ucsd.sccn.LSL.StreamInfo;
import testing.LSLSender.LSLSimulationParameters;
import testing.LSLSender.LSLSimulationStreaming;

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
						cfg.setOutDataType( LSL.ChannelFormat.float32 );
						cfg.setNumberOutputBlocks( 32 * 20 );
						cfg.setChannelNumber( 1 );
						cfg.setStreamName( "LSLSender-1");
						cfg.setOutputFunctionType( LSLSimulationParameters.SIN );
						
						cfgs.add( cfg );
						
						cfg = new LSLSimulationParameters( );
						cfg.setSamplingRate( 128 );
						cfg.setOutDataType( LSL.ChannelFormat.int32 );
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
			
			List< Tuple< StreamInfo, LSLConfigParameters > > LSLthreadList = new ArrayList< Tuple< StreamInfo, LSLConfigParameters > >();
			
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
					
					LSLConfigParameters par = new LSLConfigParameters( info.uid()
																		, info.name()
																		, info.type()
																		, info.source_id()
																		, info.as_xml()
																		, true
																		, chuckSize
																		, false
																		, false
																		, info.nominal_srate() );	
					
					LSLthreadList.add( new Tuple<LSL.StreamInfo, LSLConfigParameters>( info, par ) );
				}
			}
			
			List< TemporalOutDataFileWriter > writers = new ArrayList< TemporalOutDataFileWriter>();
			for( int i = 0; i < LSLthreadList.size(); i++ )
			{
				Tuple< LSL.StreamInfo, LSLConfigParameters > cfg = LSLthreadList.get( i );
				
				TemporalOutDataFileWriter wr = new TemporalOutDataFileWriter( "G:/test" + i + ".bin" , cfg.x, cfg.y, i );
				
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
