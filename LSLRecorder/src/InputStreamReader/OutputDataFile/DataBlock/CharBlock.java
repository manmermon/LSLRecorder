package InputStreamReader.OutputDataFile.DataBlock;

import edu.ucsd.sccn.LSL;

public class CharBlock extends DataBlock<Character> 
{

	public CharBlock(int seqNum, String name, int nCols, Character[] data) 
	{
		super( seqNum, name, LSL.ChannelFormat.string, nCols, data);
	}

}
