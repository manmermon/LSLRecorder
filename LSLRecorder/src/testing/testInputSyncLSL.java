package testing;

import edu.ucsd.sccn.LSL;

public class testInputSyncLSL {

	public static void main(String[] args) 
	{
		try 
		{	
			LSL.StreamInfo l = new LSL.StreamInfo( "PRUEBA_INSYNC", "value", 1, 0, LSL.ChannelFormat.int32, "TAIS_TEST" );
			
			LSL.StreamOutlet out = new LSL.StreamOutlet( l );
			
			out.wait_for_consumers( LSL.FOREVER );
			
			System.out.println("testInputSyncLSL.main()");
			
			for( int i = 0; i < 1000; i++ )
			{
				out.push_chunk( new int[] { i } );
				Thread.sleep( 1000L );
			}
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	}
}
