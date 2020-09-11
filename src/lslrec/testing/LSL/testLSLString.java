package testing.LSL;

import edu.ucsd.sccn.LSL;
import testing.LSLSender.RandomString;

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
			        LSL.StreamInfo[] results = LSL.resolve_stream("name","stringTets");
	
			        // open an inlet
			        LSL.StreamInlet inlet = new LSL.StreamInlet(results[0]);
			        
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
					LSL.StreamInfo info = new LSL.StreamInfo( "stringTets", "value", 5, 0, LSL.ChannelFormat.string
																, "TEST-LSLRec" );

					LSL.StreamOutlet out = new LSL.StreamOutlet( info );
	
					
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
