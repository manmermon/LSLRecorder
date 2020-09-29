package lslrec.plugin.lslrecPlugin.trial;

import java.util.List;

import javax.swing.JPanel;

import lslrec.auxiliar.tasks.IMonitoredTask;
import lslrec.auxiliar.tasks.ITaskIdentity;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.config.Parameter;
import lslrec.dataStream.sync.SyncMarker;
import lslrec.plugin.lslrecPlugin.LSLRecRunnablePluginAbstract;
import lslrec.plugin.lslrecPlugin.sync.LSLRecPluginSyncMethod;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;

public abstract class LSLRecPluginGUIExperiment extends AbstractStoppableThread
													implements IMonitoredTask, ITaskIdentity
{
	private LSLRecPluginSyncMethod syncMethod;

	private JPanel GUIPanel = null;
	
	private ITaskMonitor monitor = null;
	
	/**
	 * 
	 */
	public LSLRecPluginGUIExperiment( )
	{
		super( );
								
		this.GUIPanel = new JPanel();
		
		super.setName( this.getID() );
	}
	
	@Override
	public void taskMonitor(ITaskMonitor monitor) 
	{
		this.monitor = monitor;
	}	
		
	@Override
	protected final void preStart() throws Exception 
	{
		super.preStart();
		
		this.syncMethod = this.getSyncMarkerMethod( this.monitor );
		
		if( this.syncMethod != null )
		{
			this.syncMethod.startThread();
		}
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
		
		if( this.syncMethod != null )
		{
			this.syncMethod.stopThread( IStoppableThread.FORCE_STOP );
		}
	}

	protected LSLRecPluginSyncMethod getSyncMarkerMethod( ITaskMonitor monitor )
	{
		LSLRecPluginSyncMethod sync = new LSLRecPluginSyncMethod( )
										{
											@Override
											protected SyncMarker getSyncMarker() 
											{
												SyncMarker mark = new SyncMarker( getStageMark(), System.nanoTime() / 1e9D );
												
												return mark;
											}
								
											@Override
											public String getID() 
											{
												return super.getClass().getName();
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
											public void setSyncParameters(List<Parameter<String>> pars) 
											{	
											}
										};
										
		sync.taskMonitor( monitor );
		
		return sync;
	}
	
	public final JPanel getGUIExperiment()
	{
		return this.GUIPanel;
	}
	
	@Override
	protected final void runInLoop() throws Exception
	{
		this.setStage( this.GUIPanel );
		
		if( this.syncMethod != null )
		{
			synchronized ( this.syncMethod )
			{
				this.syncMethod.notify();
			}			
		}
		
		synchronized ( this )
		{
			super.wait();
		}
	}
	
	public abstract void loadSettings( List< Parameter< String > > pars);
	
	public abstract void setStage( JPanel GUIPanel );
	
	public abstract int getStageMark();	
}
