package lslrec.testing.Others;

import java.security.MessageDigest;

public class testCheckSum {

	public static void main(String[] args) throws Exception
	{		
		MessageDigest dg = MessageDigest.getInstance( "MD5" );
		
		byte[] t = ("Prue").getBytes();
		
		dg.update( t );
		
		
		t = ("ba").getBytes();
		dg.update( t );
		
		byte[] bytes = dg.digest();
		//5bc8c567a89112d5f408a8af4f17970d
		
		StringBuilder sb = new StringBuilder();
		for(int i=0; i< bytes.length ;i++)
	    {
	        sb.append( Integer.toString( (bytes[i] & 0xff ) + 0x100, 16 ).substring(1));
	    }
		
		System.out.println("testStringFormat.main() " + sb.toString() );

	}

}
