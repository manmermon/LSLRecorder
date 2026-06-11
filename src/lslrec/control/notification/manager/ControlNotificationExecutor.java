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

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import lslrec.auxiliar.extra.ArrayTreeMap;
import lslrec.auxiliar.extra.Tuple;
import lslrec.auxiliar.task.ITaskMonitor;
import lslrec.auxiliar.thread.LostWaitedThread;
import lslrec.auxiliar.thread.WriteTestCalculator;
import lslrec.config.language.Language;
import lslrec.control.handler.CoreControl;
import lslrec.control.message.AppState;
import lslrec.control.message.EventInfo;
import lslrec.control.message.EventType;
import lslrec.control.notification.transfer.INotificationTask;
import lslrec.dataStream.sync.SyncMarker;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.gui.GuiManager;
import lslrec.sockets.info.StreamInputMessage;
import lslrec.sockets.info.StreamSocketProblem;
import lslrec.stoppableThread.AbstractStoppableThread;

public class ControlNotificationExecutor extends AbstractStoppableThread implements INotificationTask
{
	private ArrayTreeMap< String, Object > eventRegister = new ArrayTreeMap<String, Object>();

	private ITaskMonitor monitor;
	
	private int savingDataProgress = 0;
	
	private boolean showWarningEvent = true;

	public ControlNotificationExecutor( ArrayTreeMap< String, Object > events )
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
			List< Object > evObjList = this.eventRegister.get( event_type );

			this.eventRegister.remove( event_type );
						
			for( Object eventObject : evObjList )
			{
				if( event_type.equals( EventType.ALL_OUTPUT_DATA_FILES_SAVED ) )
				{
					this.setAllFilesSaved();
				}
				else if( event_type.equals( EventType.SAVING_OUTPUT_TEMPORAL_FILE ) )
				{	
					GuiManager.getInstance().setAppState( AppState.State.SAVING, 0, false );
					
					GuiManager.getInstance().enablePlayButton( false );
				}		
				else if( event_type.equals( EventType.SAVING_DATA_PROGRESS ) )
				{
					if( !GuiManager.getInstance().getAppState().equals( AppState.State.SAVED ) ) // all data saved
					{
						int val = -1;
						File file = null;
						
						try
						{	
							Tuple< File, Integer > progress = (Tuple< File, Integer >)eventObject;
							
							file = progress.t1;
							val = progress.t2;							
							
							GuiManager.getInstance().setSavingState( file, val );
						}
						catch (Exception e) 
						{
							val = -1;
						}
						
						if( val > this.savingDataProgress )
						{					
							GuiManager.getInstance().setAppState( AppState.State.SAVING, 0, false);
							this.savingDataProgress = val;
						}
					}
				}
				else if( event_type.equals( EventType.OUTPUT_DATA_FILE_SAVED ) )
				{
					File file = (File)eventObject;
					
					GuiManager.getInstance().setSavingStateEnd( file );
				}
				else if (event_type.equals( EventType.SOCKET_EVENTS ))
				{
					if( eventObject instanceof List )
					{
						this.eventSocketMessagesManager( (List< EventInfo> )eventObject );
					}
					else
					{
						ArrayList< EventInfo > lst = new ArrayList<EventInfo>();
						lst.add( (EventInfo) eventObject );
						this.eventSocketMessagesManager( lst );
					}
				}
				else if( event_type.equals( EventType.TEST_WRITE_TIME ) )
				{
					List< Tuple< String, List< Long > > > testValues = (List)eventObject;
					for( Tuple< String, List< Long > > times : testValues )
					{				
						WriteTestCalculator cal = new WriteTestCalculator(  times.t1, times.t2 );
						cal.start();
					}
				}
				else if( event_type.equals( EventType.INPUT_MARK_READY ) )
				{
					this.showInputMarker( (SyncMarker) eventObject );
				}				
				else if (event_type.equals( EventType.PROBLEM ) )
				{
					try 
					{
						CoreControl.getInstance().stopWorking();						
					}
					catch (Exception e) 
					{
						ExceptionMessage msg = new ExceptionMessage( e, "Stop Exception", ExceptionMessage.ERROR_MESSAGE );
						ExceptionDialog.showMessageDialog( msg , true, true );						
					}

					Exception ex = new Exception( eventObject.toString() );
										
					if( eventObject instanceof Exception )
					{
						ex = (Exception)eventObject;
					}
					
					ExceptionMessage msg = new ExceptionMessage( ex, event_type, ExceptionMessage.ERROR_MESSAGE );
					ExceptionDialog.showMessageDialog( msg, true, true );				
				}
				else if (event_type.equals( EventType.WARNING ) )
				{
					if( this.showWarningEvent )
					{
						new Thread()
						{
							public void run()
							{
								super.setName( "Thread show warning");
								
								Exception ex = new Exception( eventObject.toString() );
								
								if( eventObject instanceof Exception )
								{
									ex = (Exception)eventObject ;
								}
								
								ExceptionMessage msg = new ExceptionMessage( ex
																			, Language.getLocalCaption( Language.MSG_WARNING )
																			, ExceptionMessage.WARNING_MESSAGE );
								
								ExceptionDialog.showMessageDialog( msg, true, false );								
							}
						}.start();
					}
				}
			}
		}
	}
	
	private void setAllFilesSaved() throws Exception
	{	
		this.savingDataProgress = 0;
		
		CoreControl.getInstance().stopRunningBackgroundThreads();
		
		LostWaitedThread.getInstance().wakeup();
					
		GuiManager.getInstance().restoreGUI();
		GuiManager.getInstance().enablePlayButton( true );
		
		GuiManager.getInstance().getAppUI().getGlassPane().setVisible( false );
		
		GuiManager.getInstance().setAppState( AppState.State.SAVED, 100, false );
		GuiManager.getInstance().closeSavingFileProgressDialog();
		
		ExceptionDialog.showMessageDialog( new ExceptionMessage(  new Throwable( "All data files saved." )
																, AppState.State.SAVED.name()
																, ExceptionMessage.INFO_MESSAGE)
															, true, false);
								
		synchronized( this )
		{
			try 
			{
				super.wait( 100L );
			}
			catch (InterruptedException e) 
			{
			}
		}
		
		ExceptionDialog.closeLogFile();

		try 
		{
			if( CoreControl.getInstance().isClosing()
					&& !CoreControl.getInstance().isDoingSomething() 
				)
			{
				System.exit( 0 );
			}
		}
		catch (Exception e) 
		{
		}
	}
	
	private void showInputMarker( SyncMarker mark )
	{	 		
		try
		{
			if( CoreControl.getInstance().isRecording() )
			{
				GuiManager.getInstance().addInputMessageLog( mark.getMarkValue() + "\n");
			}
						
			CoreControl.getInstance().setSpecialMarker( mark );		
		}
		catch (Exception e) 
		{
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
														, ExceptionMessage.ERROR_MESSAGE );
			ExceptionDialog.showMessageDialog( msg, true, true );
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
	
	/**
	 * Socket message manager.
	 * 
	 * @param EVENTS
	 * 
	 * @throws Exception
	 */
	private synchronized void eventSocketMessagesManager( List< EventInfo > EVENTS ) throws Exception
	{
		if ( ( EVENTS != null ) && ( !EVENTS.isEmpty() ) )
		{
			for( Object evObj : EVENTS)
			{	
				EventInfo event = null;
				
				if( evObj instanceof List )
				{
					this.eventSocketMessagesManager( (List< EventInfo >)evObj );
				}
				else
				{
					event = (EventInfo)evObj;
				}
				
				if( event != null )
				{
					if (event.getEventType().equals( EventType.SOCKET_INPUT_MSG ) )
					{
						CoreControl.getInstance().calculateSocketDelay( ( StreamInputMessage )event.getEventInformation() );
					}
					else if (event.getEventType().equals( EventType.SOCKET_CONNECTION_PROBLEM ))
					{
						StreamSocketProblem problem = (StreamSocketProblem)event.getEventInformation();

						String msg = problem.getProblemCause().getMessage();

						if ( msg == null || msg.isEmpty() )
						{
							msg = "Streaming connection problems";
						}

						Exception ex = new Exception( msg );
						ExceptionMessage exmsg = new ExceptionMessage( ex, EventType.SOCKET_CONNECTION_PROBLEM, ExceptionMessage.WARNING_MESSAGE );
						ExceptionDialog.showMessageDialog( exmsg, true, false );					
					}
					else if (event.getEventType().equals(  EventType.SOCKET_CHANNEL_CLOSE ))
					{
						StreamSocketProblem problem = (StreamSocketProblem)event.getEventInformation();

						String msg = problem.getProblemCause().getMessage();
						if (msg.isEmpty())
						{
							msg = problem.getProblemCause().getCause().toString();
						}

						Exception ex = new Exception( msg );
						ExceptionMessage exmsg = new ExceptionMessage( ex, EventType.SOCKET_CHANNEL_CLOSE, ExceptionMessage.WARNING_MESSAGE );
						ExceptionDialog.showMessageDialog( exmsg, true, false );							
					}
				}
			}
		}
	}
}
