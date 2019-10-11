package testing;

import java.io.File;
import java.io.IOException;

public class testRegex {

	public static void main(String[] args) 
	{
		String[] strs = { 	"G:\\Sync_datos\\WorkSpace\\GitHub\\LSLRecorder\\data.clis"
							, "G:\\Sync_datos\\WorkSpace\\GitHub\\LSLRecorder\\data.clis."
							, "G:\\Sync_datos\\WorkSpace\\GitHub\\LSLRecorder.\\data.clis"
							, "G:\\Sync_datos\\WorkSpace\\GitHub\\LSLRecorder\\data.clis/"
							, "G:\\Sync_datos\\WorkSpace\\GitHub\\LSLRecorder\\data.clis\\"
							, "G:\\Sync_datos\\WorkSpace\\GitHub\\LSLRecorder\\data."
							, "G:\\Sync_datos\\WorkSpace\\GitHub\\LSLRecorder\\data./"
							, "G:\\Sync_datos\\WorkSpace\\GitHub\\LSLRecorder\\data.\\"
							, "G:\\Sync_datos\\WorkSpace\\GitHub\\LSLRecorder\\.data.clis"
							, ".\\data.clis"
							, "..\\data.clis"
							, "...\\data.clis"
							, "....\\data.clis"
							, "....\\data.clis"
							, ".data.clis"
							, "..data.clis."
							, "..data.clis./"
							, "..data.clis.\\"
						};
		
		
		for( String str : strs )
		{
			File f = new File( str );
		    try 
		    {
		    	f.getCanonicalPath();
		    	System.out.println( str+ " -> " + f.getAbsolutePath() + " OK, file name " + f.getName() );
		       
		    }
		    catch (IOException e) 
		    {
		    	System.out.println( str + " FAIL" );
		    }
		}

	}

}
