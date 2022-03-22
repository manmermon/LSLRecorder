package lslrec.plugin.impl.test;

import lslrec.dataStream.family.setting.SimpleStreamSetting;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlock;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlockFactory;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.tools.StreamUtils.StreamDataType;
import lslrec.plugin.impl.encoder.matlab.MatlabEncoder;
import lslrec.plugin.impl.encoder.matlab.MatlabEncoderPlugin;
import lslrec.plugin.impl.encoder.matlab.OutputMatDataWriter;

public class testEncoder 
{
	public static void main(String[] args) 
	{
		try
		{
			OutputMatDataWriter wr = new OutputMatDataWriter( "prueba.mat", null );
			for( int i = 0; i < 10; i++ )
			{
				Double[] d = new Double[ 10 ];
				
				for( int j = 0; j < d.length; j++ )
				{
					d[ j ] = j + i * 10D;
				}
			
				DataBlock data = DataBlockFactory.getDataBlock( StreamDataType.double64, 0, "var", 2, d );
				wr.saveData( data );
			}
			
			while( !wr.isFinished() )
			{
				Thread.sleep( 100L );
			}
			
			wr.close();			
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
