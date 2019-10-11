package InputStreamReader.OutputDataFile.DataBlock;

public abstract class DataBlock<T> 
{
	private int seqNum;
	
	private int type;
	
	private String name;
	
	private int numCols;
	
	private T[] Data;
	
	
	public DataBlock( int seqNumber, String name, int dataType, int nCols , T[] data) 
	{
		this.seqNum = seqNumber;
		this.name = name;
		this.type = dataType;
		this.numCols = nCols;		
		this.Data = data;
	}
	
	public String getName() 
	{
		return name;
	}
	
	public int getDataType() 
	{
		return type;
	}
	
	public int getNumCols() 
	{
		return numCols;
	}
	
	public T[] getData()
	{
		return this.Data;
	}
	
	public int getSeqNum() 
	{
		return seqNum;
	}
}
