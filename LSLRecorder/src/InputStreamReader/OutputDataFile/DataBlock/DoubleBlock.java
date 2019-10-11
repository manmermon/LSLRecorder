package InputStreamReader.OutputDataFile.DataBlock;

import edu.ucsd.sccn.LSL;

public class DoubleBlock extends DataBlock<Double> 
{

	public DoubleBlock(int seqNum,String name, int nCols, Double[] data) 
	{
		super( seqNum, name, LSL.ChannelFormat.double64, nCols, data);
	}

}
