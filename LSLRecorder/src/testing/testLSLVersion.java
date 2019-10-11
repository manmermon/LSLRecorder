package testing;

import edu.ucsd.sccn.LSL;

public class testLSLVersion 
{
	public static void main(String[] args) 
	{
		try 
		{				
			System.out.println("testLSLVersion.main() LSL.library_version = " + LSL.library_version() );
			
			System.out.println("testLSLVersion.main() LSL.protocol_version = " + LSL.protocol_version() );
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	}
}
