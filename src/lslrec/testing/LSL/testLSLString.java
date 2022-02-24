package lslrec.testing.LSL;

import lslrec.dataStream.family.DataStreamFactory;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting.StreamLibrary;
import lslrec.dataStream.family.stream.lsl.LSLStreamInfo;
import lslrec.dataStream.family.stream.lsl.LSL.StreamInlet;
import lslrec.dataStream.tools.StreamUtils.StreamDataType;
import lslrec.testing.StreamOutlet;
import lslrec.testing.LSLSender.RandomString;

public class testLSLString 
{

	public static void main(String[] args) throws InterruptedException 
	{
		Thread tReceived = new Thread()
		{
			public void run() 
			{
				try
				{
			        
			        IStreamSetting[] results = DataStreamFactory.getStreamSettings( );

			        StreamInlet inlet = null; 
			        for( IStreamSetting st : results )
			        {
			     	   if( st.getLibraryID() == StreamLibrary.LSL && st.name().equalsIgnoreCase( "stringTets" ) )
			     	   {
			     		   inlet = new StreamInlet( (LSLStreamInfo)st );
			     		   break;
			     	   }
			        }
			     		   
			        
			        // receive data
			        String[] sample = new String[ inlet.info().channel_count() ];
			        while (true) 
			        {
			            inlet.pull_sample( sample );
			            System.out.println("Inputs:");
			            for (int k=0;k<sample.length;k++)
			            {	
			                System.out.println("\t" + sample[k] );
			            }
			            
			        }
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				
			}
		};

		Thread tSender = new Thread()
		{
			public void run() 
			{
				try
				{
					LSLStreamInfo info = new LSLStreamInfo( "stringTets", "value", 5, 0, StreamDataType.string.ordinal()
																, "TEST-LSLRec" );

					StreamOutlet out = new StreamOutlet( info );
	
					
			        String[] samples = new String[ info.channel_count() ];
			        RandomString[] rands = new RandomString[ info.channel_count() ];
			        
			        for( int i = 0; i < samples.length; i++ )
			        	rands[ i ] = new RandomString( 10 * ( i + 1 ) );
			        
			        int ini = -1;
		        	int upd = 1;
			        while ( true ) 
			        {
			        	if( ini == 0 )
			            {
			            	ini = samples.length - 1;
			            	upd = -1;
			            }
			            else
			            {
			            	ini = 0;
				        	upd = 1;	
			            }
			        	
			        	for (int k=0; k < samples.length; k++ )
			            {
			            	samples[ k ] = rands[ ini + upd * k ].nextString();
			            }
			            
			            out.push_sample( samples );
			            
			            Thread.sleep( 500L );
			        }			        
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}				
			}
		};
		
		tSender.start();
		
		Thread.sleep( 1000L );
		
		tReceived.start();
		
	}

}
