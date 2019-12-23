package Exceptions;

public class ExceptionManager 
{
	private static ExceptionManager exMng;
	
	private ExceptionManager()
	{
	}
	
	public static ExceptionManager getInstance()
	{
		if( exMng == null )
		{
			exMng = new ExceptionManager();
		}
		
		return exMng; 
	}
	
	public static void handleException( Exception ex )
	{
		
	}
	
	public static void handleException( String msg )
	{
		
	}
}
