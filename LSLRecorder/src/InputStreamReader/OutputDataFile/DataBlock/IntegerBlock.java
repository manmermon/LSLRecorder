package InputStreamReader.OutputDataFile.DataBlock;

import edu.ucsd.sccn.LSL;

public class IntegerBlock extends DataBlock<Integer> 
{
	public IntegerBlock(int seqNum,String name, int nCols, Integer[] data) 
	{
		super(seqNum,name, LSL.ChannelFormat.int32, nCols, data);
	}

}
