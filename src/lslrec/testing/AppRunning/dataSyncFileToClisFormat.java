package testing.AppRunning;

import java.io.File;

import DataStream.Binary.reader.TemporalBinData;
import DataStream.OutputDataFile.OutputBinaryFileSegmentation;
import DataStream.OutputDataFile.Format.DataFileFormat;
import DataStream.Sync.SyncMarkerBinFileReader;
import dataStream.StreamHeader;
import edu.ucsd.sccn.LSL;

public class dataSyncFileToClisFormat {

	public static void main(String[] args) 
	{
		String filePath = "G:/data.clis.sync";
		
		try
		{
			TemporalBinData dat = new TemporalBinData( new File( filePath )
					, LSL.ChannelFormat.int32
					, 1
					, 1
					, false
					, LSL.ChannelFormat.double64
					, "LSLSync"
					, ""
					, "G:/data_sync.clis"
					, DataFileFormat.CLIS_GZIP
					, false );

			SyncMarkerBinFileReader reader = new SyncMarkerBinFileReader( new File( filePath )
					, LSL.ChannelFormat.int32
					, LSL.ChannelFormat.double64
					, StreamBinaryHeader.HEADER_END
					, false );

			OutputBinaryFileSegmentation saveOutFileThread = new OutputBinaryFileSegmentation( dat, reader );
			saveOutFileThread.startThread();
			
			saveOutFileThread.join();
			
			System.out.println("dataSyncFileToClisFormat.main() END");
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

}
