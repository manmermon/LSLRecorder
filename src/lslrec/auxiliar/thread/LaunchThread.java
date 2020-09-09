package lslrec.auxiliar.thread;

import java.util.ArrayList;
import java.util.List;

import lslrec.auxiliar.tasks.INotificationTask;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.controls.messages.EventInfo;
import lslrec.controls.messages.EventType;
import lslrec.stoppableThread.AbstractStoppableThread;

public class LaunchThread extends AbstractStoppableThread implements INotificationTask 
{
	private ITaskMonitor monitor;
	private List< EventInfo > events = null;
	private AbstractStoppableThread thr;	
	
	public LaunchThread( AbstractStoppableThread t ) 
	{
		this.events = new ArrayList<EventInfo>();
		this.thr = t;
	}
	
	@Override
	public void taskMonitor( ITaskMonitor m ) 
	{
		this.monitor = m;
	}

	@Override
	public List<EventInfo> getResult( boolean clear ) 
	{
		List< EventInfo > evs = new ArrayList< EventInfo >();
		
		synchronized ( this.events )
		{
			evs.addAll( this.events );
			
			if( clear )
			{
				this.events.clear();
			}
		}
		
		return evs;
	}

	@Override
	public void clearResult() 
	{
		synchronized ( this.events)
		{
			this.events.clear();
		}		
	}

	@Override
	public String getID() 
	{
		return super.getClass().getSimpleName();
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
		if( this.thr != null && this.thr.getState().equals( Thread.State.NEW ) )
		{
			this.thr.startThread();		
		}
	}
	
	@Override
	protected void targetDone() throws Exception 
	{
		super.stopThread = true;
	}

	@Override
	protected void runExceptionManager( Throwable e ) 
	{
		EventInfo ev = new EventInfo( this.getID(), EventType.PROBLEM, e );
		
		this.events.add( ev );
		
		if( this.monitor != null )
		{
			try 
			{
				this.monitor.taskDone( this );
			}
			catch (Exception e1) 
			{
				e1.printStackTrace();
			}
		}
	}
}
