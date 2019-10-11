package InputStreamReader.OutputDataFile.DataBlock;

import edu.ucsd.sccn.LSL;

public class FloatBlock extends DataBlock<Float> 
{

	public FloatBlock(int seqNum,String name, int nCols, Float[] data) 
	{
		super( seqNum, name, LSL.ChannelFormat.float32, nCols, data);
	}

}
