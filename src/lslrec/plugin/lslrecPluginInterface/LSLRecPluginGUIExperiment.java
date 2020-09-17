package lslrec.plugin.lslrecPluginInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import lslrec.auxiliar.tasks.IMonitoredTask;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.config.Parameter;
import lslrec.dataStream.sync.SyncMarker;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;

public abstract class LSLRecPluginGUIExperiment extends AbstractStoppableThread 
												implements ILSLRecConfigurablePlugin
														, IMonitoredTask
													
{
	private ITaskMonitor monitor;
	private LSLRecPluginSyncMethod syncMethod;
	
	protected Map< String, Parameter< String > > pars;
	
	protected int Stage = 4;
	
	/**
	 * 
	 */
	public LSLRecPluginGUIExperiment() 
	{
		this.syncMethod = this.getSyncMarkerMethod();
		
		this.pars = new HashMap< String, Parameter< String > >();
		
		super.setName( this.getID() );
	}

	@Override
	public void taskMonitor(ITaskMonitor monitor)
	{
		this.monitor = monitor;
		
		if( this.syncMethod != null )
		{
			this.syncMethod.taskMonitor( this.monitor );
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
			
			if( this.syncMethod != null )
			{
				this.syncMethod.loadSettings( pars );
			}
		}
	}
	
	@Override
	public List< Parameter< String > > getSettings()
	{
		return new ArrayList< Parameter< String > >( this.pars.values() );
	}
	
	@Override
	protected void preStart() throws Exception 
	{
		super.preStart();
		
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
		
	@Override
	protected abstract void preStopThread(int friendliness) throws Exception;

	@Override
	protected abstract void postStopThread(int friendliness) throws Exception;

	@Override
	protected abstract void runInLoop() throws Exception;
	
	@Override
	public abstract String getID();
	
	@Override
	public abstract JPanel getSettingPanel();
	
	public abstract JPanel getGUIExperiment();
	
	protected LSLRecPluginSyncMethod getSyncMarkerMethod()
	{
		return new LSLRecPluginSyncMethod()
		{
			@Override
			protected SyncMarker getSyncMarker() 
			{
				SyncMarker mark = new SyncMarker( Stage, System.nanoTime() / 1e9D );
				
				return mark;
			}

			@Override
			public String getID() 
			{
				return super.getClass().getName();
			}

			@Override
			public JPanel getSettingPanel() 
			{
				return null;
			}

			@Override
			protected void preStopThread(int friendliness) throws Exception 
			{	
			}

			@Override
			protected void postStopThread(int friendliness) throws Exception 
			{	
			}
		};
	}
	
}
