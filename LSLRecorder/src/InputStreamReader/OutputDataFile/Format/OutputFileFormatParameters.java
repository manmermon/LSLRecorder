package InputStreamReader.OutputDataFile.Format;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import InputStreamReader.OutputDataFile.Compress.IOutZip;
import InputStreamReader.OutputDataFile.Compress.OutputZipDataFactory;

public class OutputFileFormatParameters 
{
	private final String HEARDER_SIZE = "HEADER_SIZE";
	private final String ZIP_ID = "ZIP_ID";
	private final String CHAR_CODING = "CHAR_CODING";
	
	private Map< String, Object > pars = new HashMap< String, Object >();
	
	public OutputFileFormatParameters() 
	{
		this.setHeaderSize( 0 );
		this.setCompressType( OutputZipDataFactory.UNDEFINED );
		this.setCharset( Charset.forName( "UTF-8" )  );
	}
	
	public void setHeaderSize( long size )
	{
		this.pars.put( this.HEARDER_SIZE, size );
	}
	
	public void setCompressType( int type )
	{
		this.pars.put( this.ZIP_ID, type );
	}
	
	public void setCharset( Charset coding )
	{
		if( coding != null )
		{
			this.pars.put( this.CHAR_CODING, coding );
		}
	}
	
	public long getHeaderSize( )
	{
		return (long)this.pars.get( this.HEARDER_SIZE );
	}
	
	public int getCompressType( )
	{
		return (int)this.pars.get( this.ZIP_ID );
	}
	
	public Charset getCharset( )
	{
		return (Charset)this.pars.get( this.CHAR_CODING );
	}
}
