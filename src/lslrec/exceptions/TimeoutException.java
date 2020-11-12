/**
 * 
 */
package lslrec.exceptions;

/**
 * From Lab Streaming Layer project.
 */
public class TimeoutException extends Exception 
{
	private static final long serialVersionUID = -7107612761710187211L;

	public TimeoutException() 
	{
		super();
	}
	
    public TimeoutException( String message ) 
    { 
    	super(message); 
    }
}