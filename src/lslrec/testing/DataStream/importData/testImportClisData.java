/**
 * 
 */
package lslrec.testing.DataStream.importData;

import java.io.IOException;
import java.util.Map;

import lslrec.dataStream.convertData.clis.ClisData;
import lslrec.dataStream.convertData.clis.ClisMetadataException;

/**
 * @author Manuel Merino Monge
 *
 */
public class testImportClisData {
	
	public static void main(String[] args) 
	{
		
		try 
		{
			ClisData clis = new ClisData( "F:\\NextCloud\\WorkSpace\\GitHub\\LSLRecorder\\data_SimulationA.clis" );
			
			Map< String, Number[][] > data = clis.importAllData();
			
			System.out.println("testImportClisData.main() " + data.keySet() );
		} catch ( Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
