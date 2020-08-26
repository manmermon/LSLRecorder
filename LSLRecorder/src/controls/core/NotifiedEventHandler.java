package controls.core;

import java.lang.Thread.State;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import auxiliar.tasks.INotificationTask;
import auxiliar.tasks.ITaskMonitor;
import controls.core.ControlNotifiedManager;
import controls.messages.EventInfo;
import controls.messages.EventType;
import exceptions.handler.ExceptionDialog;
import exceptions.handler.ExceptionDictionary;
import exceptions.handler.ExceptionMessage;
import stoppableThread.AbstractStoppableThread;
import stoppableThread.IStoppableThread;

public class NotifiedEventHandler  extends AbstractStoppableThread implements ITaskMonitor
{
	private LinkedHashMap<String, Object> eventRegister = new LinkedHashMap<String, Object>();
	private boolean treatEvent = true;

	private ControlNotifiedManager ctrlManager = null;

	private Semaphore controlNotifySemphore = null;
	private Semaphore eventRegisterSemaphore = null;

	public NotifiedEventHandler()
	{
		this.controlNotifySemphore = new Semaphore(1, true);
		this.eventRegisterSemaphore = new Semaphore(1, true);
	}


	protected void preStopThread(int friendliness) throws Exception
	{}

	protected void postStopThread(int friendliness) throws Exception
	{
		try
		{
			this.eventRegisterSemaphore.acquire();
		}
		catch (Exception localException) 
		{}

		this.ctrlManager.stopThread( IStoppableThread.FORCE_STOP );
		this.ctrlManager = null;
		this.eventRegister.clear();

		if (this.eventRegisterSemaphore.availablePermits() < 1)
		{
			this.eventRegisterSemaphore.release();
		}
	}

	protected void runInLoop() throws Exception
	{
		if (this.eventRegister.size() == 0)
		{
			synchronized ( this )
			{
				try
				{
					super.wait();
				}
				catch( InterruptedException e)
				{						
				}
			}				
		}

		try
		{
			this.eventRegisterSemaphore.acquire();
		}
		catch (Exception localException ) 
		{}

		synchronized ( this.eventRegister )
		{		
			if (this.ctrlManager == null || this.ctrlManager.getState().equals( State.TERMINATED ) )
			{
				this.ctrlManager = new controlNotifiedManager( this.eventRegister );
				this.ctrlManager.taskMonitor( this );

				this.eventRegister.clear();

				this.ctrlManager.startThread();
			}
		}

		if (this.eventRegisterSemaphore.availablePermits() < 1)
		{
			this.eventRegisterSemaphore.release();
		}
	}

	public void registreNotification( EventInfo event )
	{
		try
		{
			this.controlNotifySemphore.acquire();
		}
		catch ( InterruptedException localInterruptedException ) 
		{}      


		String event_type = event.getEventType();
		Object event_Info = event.getEventInformation();
		
		try
		{
			this.eventRegisterSemaphore.acquire();
		}
		catch ( Exception localException ) 
		{}
		
		synchronized ( this.eventRegister )
		{
			//if ( this.eventRegister.size() > 0 )
			//{
				if ( event_type.equals( EventType.SOCKET_EVENTS ) )
				{
					List< EventInfo > storedEvents = ( List< EventInfo > )this.eventRegister.get( event_type );
					List< EventInfo > newEvents = ( List< EventInfo > )event_Info;
					Set< String > setRegisteredEvents = new HashSet< String >(); 
					
					if ( storedEvents != null )
					{
						Iterator< EventInfo > itEvent = storedEvents.iterator();

						while ( itEvent.hasNext() )
						{
							EventInfo e = ( EventInfo )itEvent.next();

							if ( !e.getEventType().equals( EventType.SOCKET_MSG_DELAY )
									&& setRegisteredEvents.contains( e.getEventType() ) )
							{
								itEvent.remove();
							}
							else
							{
								setRegisteredEvents.add( e.getEventType() );
							}
						}
					}
					
					Iterator<EventInfo> itNewEvent = newEvents.iterator();
					while ( itNewEvent.hasNext() )
					{
						EventInfo ev = itNewEvent.next();
						
						if( !ev.getEventType().equals( EventType.SOCKET_MSG_DELAY ) )
						{
							if ( setRegisteredEvents.contains( ev.getEventType() ) )
							{
								itNewEvent.remove();
							}
						}		
					}
					
					if (storedEvents != null)
					{
						storedEvents.addAll( newEvents );
					}
					else
					{
						storedEvents = newEvents;
					}

					event_Info = storedEvents;
				}

				if( event_type.equals( EventType.TEST_WRITE_TIME ) )
				{
					List ob = (List)this.eventRegister.get( event_type );
					
					if( ob == null )
					{
						ob = new ArrayList();
					}
					
					ob.add( event_Info );
					
					this.eventRegister.put( event_type, ob );
				}
				else
				{
					this.eventRegister.put(event_type, event_Info);
				}
			}
		//}

		if (this.eventRegisterSemaphore.availablePermits() < 1)
		{
			this.eventRegisterSemaphore.release();
		}

		if (this.controlNotifySemphore.availablePermits() < 1)
		{
			this.controlNotifySemphore.release();
		}
	}

	public void treatEvent()
	{
		/*
		if ( super.getState().equals( Thread.State.WAITING ) )
		{
			this.treatEvent = true;
			super.notify();
		}
		*/
		
		synchronized ( this )
		{
			this.treatEvent = true;
			super.notify();	
		}
	}

	public void interruptProcess()
	{
		this.treatEvent = false;
	}

	public void clearEvent()
	{
		try
		{
			this.eventRegisterSemaphore.acquire();
		}
		catch (Exception localException) 
		{}

		synchronized (this.eventRegister)
		{
			if (this.eventRegister.size() > 0)
			{
				this.eventRegister.clear();

				super.interrupt();
			}
		}

		if (this.eventRegisterSemaphore.availablePermits() < 1)
		{
			this.eventRegisterSemaphore.release();
		}
	}

	protected void runExceptionManager(Exception e)
	{
		if (!(e instanceof InterruptedException))
		{
			e.printStackTrace();
			
			/*
			JOptionPane.showMessageDialog( coreControl.this.managerGUI.getAppUI(), e.getMessage(), 
										"Exception in " + getClass().getSimpleName(),
										JOptionPane.ERROR_MESSAGE);
			*/
			
			ExceptionMessage msg = new ExceptionMessage( e, "Exception in " + getClass().getSimpleName(), ExceptionDictionary.ERROR_MESSAGE );
			ExceptionDialog.showMessageDialog( msg, true, true );
		}
	}

	public void taskDone(INotificationTask task) throws Exception
	{
		try
		{
			this.eventRegisterSemaphore.acquire();
		}
		catch (Exception localException) 
		{}
		
		if (this.eventRegisterSemaphore.availablePermits() < 1)
		{
			this.eventRegisterSemaphore.release();
		}
	}

}
