package testing.AppRunning.LSLStream;

import java.util.ArrayList;
import java.util.List;

import StoppableThread.IStoppableThread;
import testing.LSLSender.LSLSimulationParameters;
import testing.LSLSender.LSLSimulationStreaming;

public class LSLStream 
{
static List< LSLSimulationParameters > cfgs;
	
	public static void CreateDataLSLStreams( int time, int dataType, int nChannels, int frq, int nStreams, int chunkSize, int interleaved ) 
	{
		try
		{			
			Thread t = new Thread()
			{
				public void run() 
				{
					try 
					{
						cfgs = new ArrayList< LSLSimulationParameters >();
						
						for( int i = 0; i < nStreams; i++ )
						{						
							LSLSimulationParameters cfg = new LSLSimulationParameters( );
							cfg.setSamplingRate( frq );
							cfg.setOutDataType( dataType );
							cfg.setBlockSize( chunkSize );
							cfg.setInterleavedData( interleaved != 0);
							cfg.setNumberOutputBlocks( (int)(cfg.getSamplingRate() * time ));
							cfg.setChannelNumber( nChannels );
							cfg.setStreamName( "LSLSender-" + i);
							cfg.setOutputFunctionType( LSLSimulationParameters.SIN );
							
							cfgs.add( cfg );
						}
						
						testLSLSender( cfgs );
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
	
	static List< LSLSimulationStreaming > lslOutStream = new ArrayList<LSLSimulationStreaming>();
	
	private static void testLSLSender( List< LSLSimulationParameters > cfgs ) throws Exception
	{	
		try 
		{							
			for( LSLSimulationParameters par : cfgs )
			{
				LSLSimulationStreaming stream = new LSLSimulationStreaming( par );
				stream.setName( par.getStreamName() );
				
				lslOutStream.add( stream );
				
				stream.startThread();
			}
		}
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void stopStreams()
	{
		for( LSLSimulationStreaming str : lslOutStream )
		{
			str.stopThread( IStoppableThread.FORCE_STOP );
		}
	}
	
}
