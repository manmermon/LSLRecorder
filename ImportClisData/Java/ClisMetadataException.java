package ImportClisData.Java;

public class ClisMetadataException extends Exception 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5170467081718636099L;

	public ClisMetadataException( )
	{
		super( "Clis Metadata non correct." );
	}
	
	public ClisMetadataException( String msg )
	{
		super( msg );
	}
}
