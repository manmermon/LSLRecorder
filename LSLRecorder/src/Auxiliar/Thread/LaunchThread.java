package Auxiliar.Thread;

import java.util.ArrayList;
import java.util.List;

import Auxiliar.Tasks.INotificationTask;
import Auxiliar.Tasks.ITaskMonitor;
import Controls.Messages.EventInfo;
import Controls.Messages.EventType;
import StoppableThread.AbstractStoppableThread;

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
	public List<EventInfo> getResult() 
	{
		return this.events;
	}

	@Override
	public void clearResult() 
	{
		this.events.clear();
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
	protected void runExceptionManager( Exception e ) 
	{
		EventInfo ev = new EventInfo( EventType.PROBLEM, e );
		
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
