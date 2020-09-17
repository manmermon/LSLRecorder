package lslrec.plugin.lslrecPluginInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import lslrec.auxiliar.tasks.IMonitoredTask;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.auxiliar.tasks.NotificationTask;
import lslrec.config.Parameter;
import lslrec.controls.messages.EventInfo;
import lslrec.controls.messages.EventType;
import lslrec.dataStream.sync.SyncMarker;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;

public abstract class LSLRecPluginSyncMethod extends AbstractStoppableThread 
												implements ILSLRecConfigurablePlugin
														, IMonitoredTask
{
	private ITaskMonitor monitor = null;
	private NotificationTask notifier = null;
	
	protected Map< String, Parameter< String > > pars;
		
	/**
	 * 
	 */
	public LSLRecPluginSyncMethod() 
	{
		this.pars = new HashMap< String, Parameter< String > >();
		
		super.setName( this.getID() );
	}
	
	
	@Override
	public void taskMonitor(ITaskMonitor monitor) 
	{
		this.monitor = monitor;
	}
	
	@Override
	protected void preStart() throws Exception 
	{		
		super.preStart();
		
		if( this.monitor != null )
		{
			this.notifier = new NotificationTask( false );
			this.notifier.taskMonitor( this.monitor );
		}
	}
	
	@Override
	public void loadSettings( List< Parameter< String > > pars)
	{
		if( pars != null )
		{
			for( Parameter< String > p : pars )
			{
				this.pars.put( p.getID(), p );
			}
		}
	}
	
	@Override
	public List< Parameter< String > > getSettings()
	{
		return new ArrayList< Parameter< String > >( this.pars.values() );
	}
	
	protected abstract SyncMarker getSyncMarker( );
	
	@Override
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
		
		if( this.notifier != null )
		{
			this.notifier.stopThread( IStoppableThread.STOP_WITH_TASKDONE );
			this.notifier = null;
		}
	}
	
	
	@Override
	public abstract String getID();

	@Override
	public abstract JPanel getSettingPanel();

	@Override
	protected abstract void preStopThread( int friendliness ) throws Exception;

	@Override
	protected abstract void postStopThread( int friendliness ) throws Exception;

	@Override
	protected void runInLoop() throws Exception
	{
		synchronized ( this )
		{
			super.wait();
		}
		
		SyncMarker marker = this.getSyncMarker();
		
		if( this.notifier != null && marker != null )
		{
			this.notifier.addEvent( new EventInfo( this.getID(), EventType.INPUT_MARK_READY, marker ) );
			this.notifier.notify();
		}
	}
}
