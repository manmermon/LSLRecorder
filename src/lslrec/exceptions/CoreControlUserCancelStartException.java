package lslrec.exceptions;

public class CoreControlUserCancelStartException extends Exception 
{
	private static final long serialVersionUID = 1L;

	public CoreControlUserCancelStartException()
	{
		super();
	}
	
	public CoreControlUserCancelStartException( String message ) 
    { 
    	super(message); 
    }
}