package Exceptions;

public class UnsupportedDataTypeException extends Exception 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 3222289336391196548L;

	public UnsupportedDataTypeException( )
	{
		super( "Unsuppoted data type." );
	}
	
	public UnsupportedDataTypeException( String msg )
	{
		super( msg );
	}
}
