package lslrec.exceptions;

public class UnsupportedTypeException extends Exception 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3222289336391196548L;

	public UnsupportedTypeException( )
	{
		super( "Unsuppoted data type." );
	}
	
	public UnsupportedTypeException( String msg )
	{
		super( msg );
	}
}
