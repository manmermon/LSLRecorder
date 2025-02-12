/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.plugin.lslrecPlugin.trial;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import lslrec.auxiliar.task.IMonitoredTask;
import lslrec.auxiliar.task.ITaskIdentity;
import lslrec.auxiliar.task.ITaskLog;
import lslrec.auxiliar.task.ITaskMonitor;
import lslrec.config.Parameter;
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

	private Boolean isDisposed = false;
	
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
		
		this.testWindow.setTitle( this.getID() );
		
		this.testWindow.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );
		
		/*
		this.testWindow.addWindowListener( new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e) 
			{
				synchronized ( isDisposed )
				{
					isDisposed = true;
				}	
				
				super.windowClosing( e );				
			}
		});
		*/
		
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
			
			synchronized( this )
			{
				while( !this.syncMethod.getState().equals( Thread.State.WAITING ) )
				{
					super.wait( 30L );
				}
			}
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
		
		this.disposeTrialWindow();
		
		if( this.syncMethod != null )
		{
			this.syncMethod.stopThread( IStoppableThread.FORCE_STOP );
		}
	}

	public final void showTrialWindow()
	{
		if( this.testWindow != null )
		{
			this.testWindow.setVisible( true );
			this.testWindow.toFront();
		}
	}
	
	/**
	 * @see JFrame.getExtendedState()	
	 */
	public final void setTrialWindowState( int state )
	{
		if( this.testWindow != null )
		{
			this.testWindow.setExtendedState( state );
		}
	}
	
	public void setTrialWindowSize ( Dimension size )
	{
		if( this.testWindow != null && size != null )
		{
			this.testWindow.setSize( size );
		}
	}
	
	public final void disposeTrialWindow()
	{
		synchronized ( this.isDisposed )
		{
			if( this.testWindow != null && !this.isDisposed )
			{
				this.isDisposed = true;
				
				//this.testWindow.setVisible( false );
				try
				{
					this.testWindow.dispose();								
				}
				catch ( Exception e) 
				{
				}
				
				this.testWindow = null;
				
				this.GUIPanel.setVisible( false );		
				this.GUIPanel = null;
			}
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
											public void loadSyncSettings(List<Parameter<String>> pars) 
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
	
	public boolean hasSyncMethod()
	{
		return this.loadSyncMarkerMethod( this.monitor ) != null;
	}
	
	public final void setGUIPanel( JPanel tesPanel )
	{
		this.GUIPanel.setVisible( false );
		
		this.GUIPanel.removeAll();
		
		this.GUIPanel.setVisible( true );
	}
	
	/*
	public final JFrame getWindonw()
	{
		return this.testWindow;
	}
	*/
	
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
	
	public abstract void setTrialLogStream( ITaskLog log );	
	
	public abstract void loadSettings( List< Parameter< String > > pars);
	
	public abstract void setStage( JPanel GUIPanel );
	
	public abstract int getStageMark();	
}
