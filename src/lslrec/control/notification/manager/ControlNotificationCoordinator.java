/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2011-2026 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.  https://github.com/manmermon/LSLRecorder
 *
 *   LSLRec is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   LSLRec is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with LSLRec.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */

package lslrec.control.notification.manager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Semaphore;

import lslrec.auxiliar.extra.ArrayTreeMap;
import lslrec.auxiliar.task.ITaskMonitor;
import lslrec.control.message.EventInfo;
import lslrec.control.message.EventType;
import lslrec.control.notification.transfer.INotificationTask;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;

public class ControlNotificationCoordinator extends AbstractStoppableThread implements ITaskMonitor
{
	private ArrayTreeMap< String, Object > eventRegister = new ArrayTreeMap<String, Object >();

	private ControlNotificationExecutor ctrlManager = null;

	private Semaphore controlNotifySemphore = null;
	private Semaphore eventRegisterSemaphore = null;

	public ControlNotificationCoordinator()
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
		if( this.eventRegister.isEmpty() )
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
				this.ctrlManager = new ControlNotificationExecutor( this.eventRegister );
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
			if ( event_type.equals( EventType.SOCKET_EVENTS ) )
			{
				//List< EventInfo > storedEvents = ( List< EventInfo > )this.eventRegister.get( event_type );
				List< Object > storedEvents = this.eventRegister.get( event_type );
				List< EventInfo > newEvents = ( List< EventInfo > )event_Info;
				Set< String > setRegisteredEvents = new HashSet< String >(); 

				if ( storedEvents != null )
				{
					//Iterator< EventInfo > itEvent = storedEvents.iterator();
					Iterator< Object > itEvent = storedEvents.iterator();

					while ( itEvent.hasNext() )
					{
						Object evOb = itEvent.next();

						ArrayList< EventInfo > ev;
						if( !(evOb instanceof ArrayList ) )
						{
							ev = new ArrayList<EventInfo>();
							ev.add( (EventInfo)evOb);
						}
						else
						{
							ev = (ArrayList<EventInfo>)evOb;
						}

						for( EventInfo e : ev ) 
						{
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
					storedEvents = new ArrayList< Object >( newEvents );
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

				this.eventRegister.putElement( event_type, ob );
			}
			else
			{
				if( this.eventRegister.get( event_type ) == null )
				{
					this.eventRegister.putElement(event_type, event_Info);
				}
			}
		}
		
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
		synchronized ( this )
		{
			super.notify();	
		}
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
			
			ExceptionMessage msg = new ExceptionMessage( e, "Exception in " + getClass().getSimpleName(), ExceptionMessage.ERROR_MESSAGE );
			ExceptionDialog.showMessageDialog( msg, true, true );
		}
	}

	public void taskDone( INotificationTask task) throws Exception
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