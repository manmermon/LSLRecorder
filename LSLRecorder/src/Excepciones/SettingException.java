package Excepciones;

public class SettingException extends Exception 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5170467081718636098L;

	public SettingException( )
	{
		super( "Settings non correct." );
	}
	
	public SettingException( String msg )
	{
		super( msg );
	}
}