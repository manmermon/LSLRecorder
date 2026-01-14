package lslrec.control.inputDataChecker;

public class StreamCheckerSettings 
{
	private long reconnectionTime = 0;
	private long dataWaitingTime = 0;
	
	public long getDataWaitingTime() 
	{
		return this.dataWaitingTime;
	}
	
	public long getReconnectionTime() 
	{
		return this.reconnectionTime;
	}
	
}
