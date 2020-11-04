/**
 * 
 */
package lslrec.stoppableThread;

/**
 * @author Manuel Merino Monge
 *
 */
public class ThreadStopException extends Exception 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7542822226826638015L;

	public ThreadStopException( )
	{
		super( "Thread stop causes by an error." );
	}
	
	public ThreadStopException( String msg )
	{
		super( msg );
	}
}