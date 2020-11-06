/**
 * 
 */
package lslrec.plugin.impl.test;

import java.io.File;

/**
 * @author Manuel Merino Monge
 *
 */
public class testPWD {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try
		{
		// TODO Auto-generated method stub
		File f = new File( "../binaries/" );
		System.out.println("testPWD.main() " + f.getCanonicalPath() );
		File[] fs = f.listFiles();
		
		for( File ff : fs )
			System.out.println("testPWD.main() " + ff.getCanonicalPath());
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	}

}
