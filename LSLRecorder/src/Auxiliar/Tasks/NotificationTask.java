package Auxiliar.Tasks;

import java.util.ArrayList;
import java.util.List;

import Controls.Messages.EventInfo;
import StoppableThread.AbstractStoppableThread;

public class NotificationTask extends AbstractStoppableThread implements INotificationTask 
{
	private List< EventInfo > events = new ArrayList< EventInfo >();
	
	private String ID = this.getClass().getName();
	
	private ITaskMonitor monitor = null;

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
		synchronized ( this.events )
		{
			this.events.clear();
		}		
	}

	@Override
	public String getID() 
	{
		return this.ID;
	}
	
	public void setID( String id )
	{
		this.ID = id;
	}
	
	public void addEvent( EventInfo event )
	{
		if( event != null )
		{
			synchronized ( this.events )
			{
				this.events.add( event );
			}
		}
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
		synchronized ( this )
		{
			try 
			{
				boolean wait = true;
				
				synchronized ( this.events )
				{
					wait = this.events.isEmpty();
				}
				
				if( wait )
				{
					super.wait();
				}
			}
			catch (Exception e) 
			{
			}
		}
		
		if( this.monitor != null )
		{
			this.monitor.taskDone( this );
		}		
	}
}
