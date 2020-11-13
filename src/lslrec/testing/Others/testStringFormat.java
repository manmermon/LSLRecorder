package lslrec.testing.Others;

import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;

public class testStringFormat {
	
	public static void main(String[] args) throws Exception 
	{
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		dfs.setGroupingSeparator(','); 
		
		DecimalFormat df = new DecimalFormat("#.0#", dfs);
				
		System.out.println("testStringFormat.main() " + df.format( 2.0));
		System.out.println("testStringFormat.main() " + df.format( 2.1));
		System.out.println("testStringFormat.main() " + df.format( 2.11));
		
		String st = new String( new byte[] { (byte)-4, (byte)56 } );
		System.out.println("testStringFormat.main() " + st );
		System.out.println("testStringFormat.main() " + Arrays.toString( st.getBytes() ) );
	}

}
