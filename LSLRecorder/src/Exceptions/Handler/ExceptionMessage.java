package Exceptions.Handler;

public class ExceptionMessage 
{
	private Throwable exception = null;
	private String titleEx = "";
	private int msgType = 0;
		
	public ExceptionMessage( Throwable ex, String title, int type ) 
	{
		this.exception = ex;
		this.titleEx = title;
		this.msgType = type;
	}
	
	public String getTitleException() 
	{
		return this.titleEx;
	}
	
	public int getMessageType() 
	{
		return this.msgType;
	}
	
	public Throwable getException() 
	{
		return this.exception;
	}
}
