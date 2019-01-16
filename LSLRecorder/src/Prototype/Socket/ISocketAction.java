package Prototype.Socket;

public interface ISocketAction extends Cloneable
{
	/**
	 * Specific action with respect to received message.
	 * 
	 * @param inputMessage
	 * @return
	 */
	public String prepareResponse( String inputMessage );
	
	/**
	 * Specific output message
	 * 
	 * @param outputMessage
	 * @return
	 */
	public String prepareSending( String outputMessage );	
}
