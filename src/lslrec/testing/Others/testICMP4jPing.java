package testing.Others;

import org.icmp4j.IcmpPingRequest;
import org.icmp4j.IcmpPingResponse;
import org.icmp4j.IcmpPingUtil;

public class testICMP4jPing 
{

	public static void main(String[] args) throws Exception
	{
		String[] ipAddress = new String[] { "127.0.0.1"
											, "150.214.141.201"
											, "150.214.141.1"
											, "150.214.141.198"
											, "8.8.8.8"
											, "110.174.25.190"
		};
		
		
		// repeat a few times

		for( String ip : ipAddress )
		{
			IcmpPingRequest request = IcmpPingUtil.createIcmpPingRequest();
			request.setHost( ip );
			
			for (int count = 1; count <= 4; count ++) {
	
					// delegate
			
					final IcmpPingResponse response = IcmpPingUtil.executePingRequest (request);
			
					// log
			
			
					System.out.println( "IP " + ip + ", Ping Duration " + response.getDuration() / 1e9D + " ns. " + response );
			
					// rest
			
					Thread.sleep (1000);
	
			}
		}
	}

}
