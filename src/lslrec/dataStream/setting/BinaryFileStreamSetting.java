package lslrec.dataStream.setting;

public class BinaryFileStreamSetting extends DataStreamSetting 
{
	private String streamBinFile = null;
	
	public BinaryFileStreamSetting( DataStreamSetting dataStream, String file ) 
	{
		super( dataStream );
		
		if( file == null || file.isEmpty() )
		{
			throw new IllegalArgumentException( "File null or empty." );
		}
		
		this.streamBinFile = file;
	}
	
	public void setStreamBinFile(String streamBinFile) 
	{
		this.streamBinFile = streamBinFile;
	}
	
	public String getStreamBinFile() 
	{
		return streamBinFile;
	}
}
