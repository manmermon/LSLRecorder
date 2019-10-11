package InputStreamReader.OutputDataFile.DataBlock;

import edu.ucsd.sccn.LSL;

public class LongBlock extends DataBlock<Long> 
{
	public LongBlock(int seqNum,String name, int nCols, Long[] data) 
	{
		super(seqNum,name, LSL.ChannelFormat.int64, nCols, data);
	}

}
