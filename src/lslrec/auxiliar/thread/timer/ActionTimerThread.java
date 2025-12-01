/**
 * 
 */
package lslrec.auxiliar.thread.timer;

import lslrec.stoppableThread.AbstractStoppableThread;

/**
 * @author Manuel Merino Monge
 *
 */
public class ActionTimerThread extends AbstractStoppableThread 
{
	private IAction act;
	
	public ActionTimerThread( IAction ac )
	{
		this.act = ac;
		
		super.setName( super.getClass().getName() + "-" + super.getId());
	}

	@Override
	protected void preStopThread(int friendliness) throws Exception
	{	
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{	
	}

	@Override
	protected void runInLoop() throws Exception 
	{
		synchronized( this )
		{
			super.wait();
		}
		
		if( this.act != null )
		{
			this.act.execute();
		}
	}
	
	public void execute() 
	{
		synchronized( this )
		{
			super.notify();
		}
	}
	
	@Override
	protected void runExceptionManager(Throwable e) 
	{
		if( !( e instanceof InterruptedException ) )
		{
			super.runExceptionManager( e );
		}
	}
	
}
