/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.zTransform;

import java.util.EventObject;

/**
 * @author Manuel Merino Monge
 *
 */
public class ZeroPoleEvent extends EventObject
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * @author Manuel Merino Monge
	 *
	 */

	public static final int REMOVE = -1;
	public static final int NEW = 0;;
	
	
	private int typeEvent;
	
	public ZeroPoleEvent( Object source ) 
	{
		super( source );
	}
	
	public ZeroPoleEvent( Object source, int type ) 
	{
		super( source );
		
		this.typeEvent = type;
	}
	
	public int getType()
	{
		return this.typeEvent;
	}

}
