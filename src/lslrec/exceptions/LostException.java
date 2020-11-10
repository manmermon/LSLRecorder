/**
 * 
 */
package lslrec.exceptions;

/**
 * From Lab Streaming Layer project.
 */
public class LostException extends Exception 
{
	/**
     * Exception class that indicates that a stream inlet's source has been irrecoverably lost.
     */
	private static final long serialVersionUID = 1L;

	public LostException()
	{
		super();
	}
	
	public LostException( String message ) 
    { 
    	super(message); 
    }
}
