package InputStreamReader.OutputDataFile.DataBlock;

public class CompressedByteBlock extends DataBlock< Byte >
{
	public CompressedByteBlock(int seqNum, String name, int dataType, int nCols, Byte[] data) 
	{
		super(seqNum, name, dataType, nCols, data);
	}
}
