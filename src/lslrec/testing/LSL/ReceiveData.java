package lslrec.testing.LSL;

import lslrec.dataStream.family.DataStreamFactory;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting.StreamLibrary;
import lslrec.dataStream.family.stream.lsl.LSLStreamInfo;
import lslrec.dataStream.family.stream.lsl.LSL.StreamInlet;

public class ReceiveData {
    public static void main(String[] args) throws Exception  {
        System.out.println("Resolving an EEG stream...");
       IStreamSetting[] results = DataStreamFactory.getStreamSettings();

       StreamInlet inlet = null; 
       for( IStreamSetting st : results )
       {
    	   if( st.getLibraryID() == StreamLibrary.LSL && st.name().equalsIgnoreCase( "Simulation" ) )
    	   {
    		   inlet = new StreamInlet( (LSLStreamInfo)st );
    		   break;
    	   }
       }
    		   
        // open an inlet
       if( inlet != null )
       {
	        // receive data
	        float[] sample = new float[inlet.info().channel_count()];
	        while (true) {
	            inlet.pull_sample(sample);
	            for (int k=0;k<sample.length;k++)
	                System.out.print("\t" + Double.toString(sample[k]));
	            System.out.println();
	        }
       }
    }
}
