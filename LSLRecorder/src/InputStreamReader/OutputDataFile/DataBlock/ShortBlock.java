package InputStreamReader.OutputDataFile.DataBlock;

import edu.ucsd.sccn.LSL;

public class ShortBlock extends DataBlock<Short> 
{
	public ShortBlock(int seqNum,String name, int nCols, Short[] data) 
	{
		super(seqNum, name, LSL.ChannelFormat.int16, nCols, data);
	}

}
