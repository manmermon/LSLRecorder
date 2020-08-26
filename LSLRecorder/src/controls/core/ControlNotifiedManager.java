package controls.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import auxiliar.extra.Tuple;
import auxiliar.tasks.INotificationTask;
import auxiliar.tasks.ITaskMonitor;
import config.language.Language;
import controls.core.WriteTestCalculator;
import controls.messages.AppState;
import controls.messages.EventInfo;
import controls.messages.EventType;
import controls.messages.RegisterSyncMessages;
import dataStream.sync.SyncMarker;
import exceptions.handler.ExceptionDialog;
import exceptions.handler.ExceptionDictionary;
import exceptions.handler.ExceptionMessage;
import stoppableThread.AbstractStoppableThread;

public class ControlNotifiedManager  extends AbstractStoppableThread implements INotificationTask
{
	private LinkedHashMap<String, Object> eventRegister = new LinkedHashMap<String, Object>();

	private ITaskMonitor monitor;

	public ControlNotifiedManager( Map< String, Object > events )
	{
		if (events != null)
		{
			synchronized (events)
			{
				for (String event : events.keySet())
				{
					this.eventRegister.put( event, events.get( event ) );
				}
			}				
		}
		
		super.setName( this.getClass().getName() );
	}

	@Override
	protected void preStart() throws Exception
	{
		super.preStart();
	}


	@Override
	protected void preStopThread(int friendliness) throws Exception
	{}

	@Override
	protected void postStopThread(int friendliness)  throws Exception
	{
		this.eventRegister.clear();
		this.eventRegister = null;
	}

	@Override
	protected void runInLoop() throws Exception
	{
		if ( this.eventRegister.size() > 0 )
		{
			String event_type = (String)this.eventRegister.keySet().iterator().next();
			final Object eventObject = this.eventRegister.get( event_type );

			this.eventRegister.remove( event_type );

			if( event_type.equals( EventType.ALL_OUTPUT_DATA_FILES_SAVED ) )
			{
				 .gemanagerGUI.setAppState( AppState.SAVED, 100, false );
				
				savingDataProgress = 0;
				//managerGUI.enablePlayButton( true );
				
				if( closeWhenDoingNothing && !isDoingSomething() )
				{
					System.exit( 0 );
				}
			}
			else if( event_type.equals( EventType.SAVING_OUTPUT_TEMPORAL_FILE ) )
			{	
				managerGUI.setAppState( AppState.SAVING, 0, true );
			}		
			else if( event_type.equals( EventType.SAVING_DATA_PROGRESS ) )
			{
				int val = -1;
				
				try
				{
					val = (Integer)eventObject;
				}
				catch (Exception e) 
				{
					val = -1;
				}
				
				if( val > savingDataProgress )
				{
					managerGUI.setAppState( AppState.SAVING, val, true );
				}
			}
			else if (event_type.equals( EventType.SOCKET_EVENTS ))
			{
				eventSocketMessagesManager( (List< EventInfo> )eventObject );
			}
			/*
			else if( event_type.equals( EventType.SOCKET_MSG_DELAY ) )
			{					
				NotificationTask not = new NotificationTask();
				not.setID( not.getID() + "-" + EventType.SOCKET_MSG_DELAY );
				not.addEvent( new EventInfo( not.getID(), EventType.INPUT_MARK_READY, eventObject ));
				not.taskMonitor( ctrlOutputFile );					
				not.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
				
				not.startThread();					
			}
			*/
			else if( event_type.equals( EventType.TEST_WRITE_TIME ) )
			{
				List< Tuple< String, List< Long > > > testValues = (List)eventObject;
				for( Tuple< String, List< Long > > times : testValues )
				{				
					WriteTestCalculator cal = new WriteTestCalculator(  times.x, times.y );
					cal.start();
				}
			}
			/*
			else if( event_type.equals( EventType.SOCKET_PING_END ) )
			{	
				sockMsgDelayCalculator = null;
			}
			*/
			else if( event_type.equals( EventType.INPUT_MARK_READY ) )
			{
				this.InputMarker( (SyncMarker) eventObject );
			}				
			else if (event_type.equals( EventType.PROBLEM ) )
			{
				/*
				Thread t = new Thread()
				{
					@Override
					public synchronized void run() 
					{
						try 
						{
							stopWorking( );
						}
						catch (Exception e) 
						{
							
						}
					}
				};
				
				t.setName( this.getClass().getSimpleName() + "-stopWorking" );
				
				t.start();
				*/
				
				try 
				{
					stopWorking( );
				}
				catch (Exception e) 
				{
					ExceptionMessage msg = new ExceptionMessage( e, "Stop Exception", ExceptionDictionary.ERROR_MESSAGE );
					ExceptionDialog.showMessageDialog( msg , true, true );
				}

				/*
				if( !ConfigApp.isTesting() )
				{
					JOptionPane.showMessageDialog(  coreControl.this.managerGUI.getAppUI(), 
													eventObject.toString(), 
													event_type, 
													JOptionPane.ERROR_MESSAGE);
				}
				else
				{
					managerGUI.addInputMessageLog( event_type + ": " + eventObject.toString());
				}
				*/
				
				Exception ex = new Exception( eventObject.toString() );
									
				if( eventObject instanceof Exception )
				{
					ex = (Exception)eventObject;
				}
				
				ExceptionMessage msg = new ExceptionMessage( ex, event_type, ExceptionDictionary.ERROR_MESSAGE );
				ExceptionDialog.showMessageDialog( msg, true, true );
				
			}
			else if (event_type.equals( EventType.WARNING ) )
			{
				if( showWarningEvent )
				{
					new Thread()
					{
						public void run()
						{
							Exception ex = new Exception( eventObject.toString() );
							
							if( eventObject instanceof Exception )
							{
								ex = (Exception)eventObject ;
							}
							
							ExceptionMessage msg = new ExceptionMessage( ex
																		, Language.getLocalCaption( Language.MSG_WARNING )
																		, ExceptionDictionary.WARNING_MESSAGE );
							
							ExceptionDialog.showMessageDialog( msg, true, false );
							
							/*
							if( !ConfigApp.isTesting() )
							{
								JOptionPane.showMessageDialog(   managerGUI.getAppUI(), 
										eventObject.toString(), 
										Language.getLocalCaption( Language.MSG_WARNING ), 
										JOptionPane.WARNING_MESSAGE);
							}
							else
							{
								managerGUI.addInputMessageLog( Language.getLocalCaption( Language.MSG_WARNING ) + ": " + eventObject.toString());
							}
							*/
						}
					}.start();
				}
			}
		}
	}
	
	private void InputMarker( SyncMarker mark )
	{			
		if( isRecording )
		{
			managerGUI.addInputMessageLog( mark.getMarkValue() + "\n");
		}
							
		if ( mark.getMarkValue() == RegisterSyncMessages.getSyncMark( RegisterSyncMessages.INPUT_STOP ) )
		{ 
			if( isActiveSpecialInputMsg )
			{
				SpecialMarker = mark;
				/*
				Thread t = new Thread()
				{
					@Override
					public synchronized void run() 
					{
						try 
						{
							stopWorking( );
						}
						catch (Exception e) 
						{
							
						}
					}
				};
				
				t.setName( this.getClass().getSimpleName() + "-stopWorking" );
				
				t.start();
				*/					
			
				try 
				{
					stopWorking( );
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
				
			}
		}
		else if ( mark.getMarkValue() == RegisterSyncMessages.getSyncMark( RegisterSyncMessages.INPUT_START ) )
		{
			if( isActiveSpecialInputMsg 
					&& !isRecording 
					&&  isWaitingForStartCommand )
				{
					isWaitingForStartCommand = false;

					managerGUI.addInputMessageLog( mark.getMarkValue() + "\n");
					
					try
					{
						SpecialMarker = mark;
						startRecord();
					}
					catch (Exception e)
					{
						e.printStackTrace();
					}
				}
		}
	}

	@Override
	protected void targetDone() throws Exception
	{
		super.targetDone();

		this.stopThread = this.eventRegister.isEmpty();
	}

	@Override
	protected void runExceptionManager( Throwable e)
	{
		if (!(e instanceof InterruptedException))
		{
			e.printStackTrace();
							
			ExceptionMessage msg = new ExceptionMessage( e
														, "Exception in " + getClass().getSimpleName()
														, ExceptionDictionary.ERROR_MESSAGE );
			ExceptionDialog.showMessageDialog( msg, true, true );
			
			/*
			JOptionPane.showMessageDialog(   managerGUI.getAppUI(), e.getMessage(), 
					"Exception in " + getClass().getSimpleName(), 
					JOptionPane.ERROR_MESSAGE);
			*/
		}
	}

	@Override
	protected void cleanUp() throws Exception
	{
		super.cleanUp();

		if (this.monitor != null)
		{
			this.monitor.taskDone(this);
		}
	}

	@Override
	public void taskMonitor(ITaskMonitor monitor)
	{
		this.monitor = monitor;
	}


	@Override
	public List<EventInfo> getResult( boolean clear )
	{
		return null;
	}

	@Override
	public void clearResult() 
	{

	}
	
	@Override
	public String getID() 
	{
		return super.getName();
	}
}