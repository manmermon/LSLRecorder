package auxiliar.tasks;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import controls.messages.EventInfo;
import stoppableThread.AbstractStoppableThread;

public class NotificationTask extends AbstractStoppableThread implements INotificationTask 
{
	private List< EventInfo > events = new ArrayList< EventInfo >();
	
	private String ID = this.getClass().getName();
	
	private ITaskMonitor monitor = null;

	private AtomicBoolean blocking = new AtomicBoolean( true );

	private Thread antideadlockThread = null;	
	private AtomicBoolean antideadLockIsWorking = new AtomicBoolean();
	
	/**
	 * 
	 * @param blockingNotification -> True if task notification must be blocked. 
	 */
	public NotificationTask( boolean blockingNotification )
	{
		this.blocking.set( blockingNotification );
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
	
	public boolean addEvent( EventInfo event, boolean addIfNotContainSameEventType )
	{
		boolean add = event != null;
		
		if( add )
		{
			synchronized ( this.events )
			{
				if( addIfNotContainSameEventType )
				{
					for( EventInfo ev : this.events )
					{
						add = add && !ev.getEventType().equals( event.getEventType() );
						
						if( !add )
						{
							break;
						}
					}
				}
				
				if( add )
				{
					this.events.add( event );
				}
			}
		}
		
		return add;
	}
	
	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{	
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{	
		synchronized ( this )
		{
			if( super.getState().equals( Thread.State.WAITING ) )
			{
				this.notify();
			}
		}
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
				
				if( wait && !super.stopWhenTaskDone.get() )
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
			if( this.blocking.get() )
			{
				this.monitor.taskDone( this );
			}
			else
			{
				final NotificationTask refNot = this;
				
				synchronized ( this.antideadLockIsWorking )
				{
					if( !this.antideadLockIsWorking.get() )
					{
						this.antideadLockIsWorking.set( true );
						
						this.antideadlockThread = new Thread()
						{
							@Override
							public void run() 
							{
								try 
								{
									monitor.taskDone( refNot );									
								}
								catch (Exception e) 
								{
									runExceptionManager( e );
								}
								finally 
								{
									synchronized ( antideadLockIsWorking )
									{
										antideadLockIsWorking.set( false );
									}
								}
							}
						};
						
						this.antideadlockThread.setName( this.getID() + "-AntiDeadlock" );
						this.antideadlockThread.start();
					}
				}				
			}
		}		
	}
}
