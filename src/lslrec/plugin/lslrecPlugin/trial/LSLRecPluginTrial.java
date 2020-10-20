package lslrec.plugin.lslrecPlugin.trial;

import java.awt.BorderLayout;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import lslrec.auxiliar.tasks.IMonitoredTask;
import lslrec.auxiliar.tasks.ITaskIdentity;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.config.Parameter;
import lslrec.dataStream.family.lsl.LSL.StreamOutlet;
import lslrec.dataStream.sync.SyncMarker;
import lslrec.plugin.lslrecPlugin.sync.LSLRecPluginSyncMethod;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;

public abstract class LSLRecPluginTrial extends AbstractStoppableThread
													implements IMonitoredTask, ITaskIdentity
{	
	private LSLRecPluginSyncMethod syncMethod;

	private JFrame testWindow = null;
	private JPanel GUIPanel = null;
	
	private ITaskMonitor monitor = null;

	/**
	 * 
	 */
	public LSLRecPluginTrial( )
	{
		super( );
								
		this.testWindow = new JFrame();
		this.testWindow.setVisible( false );
		
		this.GUIPanel = new JPanel( new BorderLayout() );
		
		this.testWindow.setContentPane( this.GUIPanel );
		
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
		
		this.syncMethod = this.loadSyncMarkerMethod( this.monitor );
		
		if( this.syncMethod != null )
		{
			this.syncMethod.startThread();
		}
	}
	
	@Override
	protected void runExceptionManager(Throwable e) 
	{
		if( !( e instanceof InterruptedException ) )
		{
			super.runExceptionManager( e );
		}
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
	
		this.testWindow.setVisible( false );
		this.testWindow.dispose();
		this.testWindow = null;
		
		this.GUIPanel.setVisible( false );		
		this.GUIPanel = null;
		
		if( this.syncMethod != null )
		{
			this.syncMethod.stopThread( IStoppableThread.FORCE_STOP );
		}
	}

	protected LSLRecPluginSyncMethod loadSyncMarkerMethod( ITaskMonitor monitor )
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
	
	protected LSLRecPluginSyncMethod getSyncMethod()
	{
		return this.syncMethod;
	}
	
	public final void setGUIPanel( JPanel tesPanel )
	{
		this.GUIPanel.setVisible( false );
		
		this.GUIPanel.removeAll();
		
		this.GUIPanel.setVisible( true );
	}
	
	public final JFrame getWindonw()
	{
		return this.testWindow;
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
			if( !this.stopThread )
			{
				super.wait();
			}
		}
	}
		
	public abstract void loadSettings( List< Parameter< String > > pars);
	
	public abstract void setStage( JPanel GUIPanel );
	
	public abstract int getStageMark();	
}
