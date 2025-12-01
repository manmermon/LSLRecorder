package lslrec.auxiliar.thread.timer.event;

import java.util.EventObject;

public class ActionTimerEvent extends EventObject
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	public enum Type { DESTROY, START, RESTART, STOP, ACTION };
	
	private Type typeEvent;
	
	private long consumedTime;
	
	public ActionTimerEvent( Object source, Type type, long consumedTime ) 
	{
		super( source );
		
		this.typeEvent = type;
		
		this.consumedTime = consumedTime;
	}
	
	public Type getType()
	{
		return this.typeEvent;
	}
	
	public long getConsumedTime() 
	{
		return this.consumedTime;
	}
}
