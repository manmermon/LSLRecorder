package DataStream.OutputDataFile.Format.Clis;

import java.nio.charset.Charset;
import java.util.List;


public class CLISMetadata 
{
	private String headerInfo = "";
	private String header = "";

	private final String endLine = "\n";

	private final String GrpSep = ";" ;
	private final String fieldSep = "," ;
 
	private String compressAlg = "GZIP";

	private boolean addHeader = false;
	
	private Charset charCode;
	
	private byte[] padding = null;
	
	private long headerSize = 0;
	
	public CLISMetadata( long headersize, String zipID, Charset coding  ) 
	{	
		this.compressAlg = zipID;
				
		this.charCode = coding;
		if( this.charCode == null )
		{
			this.charCode = Charset.forName( "UTF-8" );
		}
				
		this.headerInfo = "ver=2.0" + this.GrpSep + "compress=" + this.compressAlg + this.GrpSep + "headerByteSize=";
		
		headersize += this.headerInfo.length() * 2;
		
		this.headerSize = headersize;
		
		//byte[] padding = new byte[ Character.BYTES ];
		int charByteSize = this.headerInfo.getBytes( coding ).length / this.headerInfo.length(); 
		this.padding = new byte[ charByteSize ];
		
		for( int i = 0; i < this.padding.length; i++ )
		{
			this.padding[ i ] = '\r';
		}
		
		this.headerInfo += ( headersize * padding.length ) + this.GrpSep;
	}
	
	public long getHeaderSize() 
	{
		return headerSize;
	}
	
	public byte[] getPadding() 
	{
		return this.padding;
	}
	
	public void addMetadataProtocolInfo(String name, String type, int typeBytes, int col, List< Integer > blockSizes )
	{
		this.headerInfo = this.headerInfo + name + this.fieldSep + type + this.fieldSep + typeBytes 
							+ this.fieldSep + col;
		
		if( blockSizes.size() == 0 )
		{
			this.headerInfo += this.fieldSep + 0;
		}	
		else
		{
			for( Integer size : blockSizes )
			{
				this.headerInfo += this.fieldSep + size; 
			}
		}
							
		this.headerInfo += this.GrpSep;
	}
	
	public void addMetadataHeader( String id, String text )
	{
		this.addHeader = true;

		id = id.replace("\n", "").replace("\r", "");
		text = text.replace("\n", "").replace("\r", "");

		this.header = (id + "=" + text);
	}
	
	public Charset getCharCode() 
	{
		return this.charCode;
	}
	
	public boolean hasHeader()
	{
		return this.addHeader;
	}
	
	public String getHeader()
	{
		return this.header.trim() + this.endLine;
	}
	
	public String getMetaDataProtocol()
	{
		return this.headerInfo + "header," + this.addHeader + this.GrpSep + this.endLine;
	}
}
