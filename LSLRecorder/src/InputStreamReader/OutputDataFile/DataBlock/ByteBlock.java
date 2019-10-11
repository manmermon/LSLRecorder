package InputStreamReader.OutputDataFile.DataBlock;

import edu.ucsd.sccn.LSL;

public class ByteBlock extends DataBlock<Byte>
{
	public ByteBlock(int seqNum, String name, int nCols, Byte[] data) 
	{
		super( seqNum, name, LSL.ChannelFormat.int8, nCols, data);
	}

}
