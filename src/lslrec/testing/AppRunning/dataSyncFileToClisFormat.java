package lslrec.testing.AppRunning;

import java.io.File;

import lslrec.dataStream.binary.input.writer.StreamBinaryHeader;
import lslrec.dataStream.binary.reader.TemporalBinData;
import lslrec.dataStream.binary.setting.BinaryFileStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting.StreamLibrary;
import lslrec.dataStream.family.setting.SimpleStreamSetting;
import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;
import lslrec.dataStream.outputDataFile.OutputBinaryFileSegmentation;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.sync.SyncMarkerBinFileReader;

public class dataSyncFileToClisFormat {

	public static void main(String[] args) 
	{
		String filePath = "G:/data.clis.sync";
		
		try
		{
			OutputFileFormatParameters par = DataFileFormat.getDefaultOutputFileFormatParameters();
			par.setParameter( OutputFileFormatParameters.OUT_FILE_NAME, filePath );
			
			
			SimpleStreamSetting sss = new SimpleStreamSetting( StreamLibrary.LSL
																, "LSLSync"
																, StreamDataType.int32
																, StreamDataType.double64
																, StreamDataType.int64
																, 1
																, 0
																, ""
																, ""
																, ""
																, null
																, 1
																);
			
			BinaryFileStreamSetting binSet = new BinaryFileStreamSetting( sss, new File( filePath ) );
			TemporalBinData dat = new TemporalBinData( binSet, par );
					

			BinaryFileStreamSetting redSet = new BinaryFileStreamSetting( sss, new File( filePath ) );
			SyncMarkerBinFileReader reader = new SyncMarkerBinFileReader( redSet, StreamBinaryHeader.HEADER_END, false );

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
