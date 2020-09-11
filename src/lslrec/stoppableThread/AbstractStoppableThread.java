/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec and CLIS.
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

package lslrec.stoppableThread;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.event.EventListenerList;

public abstract class AbstractStoppableThread extends Thread implements IStoppableThread//, IStoppableThreadEventControl
{
    protected volatile boolean stopThread = false;
    protected volatile AtomicBoolean stopWhenTaskDone = new AtomicBoolean( false );
    protected boolean d ;
    
    private EventListenerList listenerList = new EventListenerList();
   
    /*
     * (non-Javadoc)
     * @see StoppableThread.IStoppableThread#startThread()
     */
    public synchronized void startThread() throws Exception
    {
    	this.preStart();
    	
    	super.start();
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Thread#start()
     */
    @Deprecated
    public void start()
    {
    	try 
    	{
			this.startThread();
		}
    	catch (Exception e) 
    	{
			e.printStackTrace();
		}
    }
    
    /**
     * Action before to start thread execution.  
     */
    protected void preStart() throws Exception
    {
    	
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public synchronized void run() 
    {
        try 
        {
			startUp();
		}
        catch (Exception e1) 
        {
			e1.printStackTrace();
		}
        
        //this.fireStartedEvent();
        
        while ( !this.stopThread ) 
        {
        	//this.fireStartLoopInteractionEvent();
        	
        	 try
             { 
	            this.runInLoop();
	            this.targetDone();
             }
        	 catch( Exception | Error e )
             {
        		 this.runExceptionManager( e );             	
             }
             finally
             {
            	 this.finallyManager();
             }
        	 
        	 //this.fireEndLoopInteractionEvent();
        }
        
        //this.fireFinishedLoopEvent();
        
    	try 
    	{
			cleanUp();
		}
    	catch (Exception e) 
    	{
			e.printStackTrace();
		}
    	
    	//this.fireTerminedEvent();
    }
    
    /**
     * Manager of exception in the function run()
     * 
     * @param e	-> Exception
     */
    protected void runExceptionManager( Throwable e )
    {
    	e.printStackTrace();
    }
    
    /**
     * In the run() function, instructions of the finally block (try-catch-finally).
     */
    protected void finallyManager( )
    {	
    }

    /*
     * (non-Javadoc)
     * @see StoppableThread.IStoppableThread#stopThread(int)
     */
    public void stopThread( int friendliness ) 
    {
    	try 
    	{
			this.preStopThread( friendliness );
		}
    	catch (Exception e) 
    	{
			e.printStackTrace();
		}
    	
    	if( friendliness < 0 )
    	{
    		synchronized ( this.stopWhenTaskDone )
    		{
    			this.stopWhenTaskDone.set( true );
			}    		
    	}
    	else
    	{
    		this.stopThread = true;
    	}
    	
    	if( friendliness > 0 )
    	{
    		super.interrupt();
    	}
        
    	try 
    	{
			this.postStopThread( friendliness );
		}
    	catch (Exception e) 
    	{
			e.printStackTrace();
		}
    }
     
    /**
     * Activity after that the main performance is done.
     *  
     * @throws Exception 
     */
    protected void targetDone() throws Exception
    {
    	synchronized ( this.stopWhenTaskDone ) 
    	{
    		if( this.stopWhenTaskDone.get() )
        	{
        		this.stopThread = true;
        		
        		super.interrupt();
        	}
		}    	
    }
    
    /**
     * Action before start of loop execution.  
     */
    protected void startUp() throws Exception
    {        
    }

    /**
     * Action before to stop execution.  
     * 
     * @param friendliness: 
     * - if friendliness < 0: stop execution when task is done.
     * - if friendliness = 0: stop execution before the next loop interaction.
     * - if friendliness > 0: interrupt immediately task and then execution is stopped.
     */
    protected abstract void preStopThread( int friendliness ) throws Exception;

    /**
     * Action after to stop execution.  
     * @param friendliness:
     * - if friendliness < 0: stop execution when task is done.
     * - if friendliness = 0: stop execution before the next loop interaction.
     * - if friendliness > 0: interrupt immediately task and then execution is stopped.
     */
    protected abstract void postStopThread( int friendliness ) throws Exception;
    
    
    /**
     * Specify task of the loop.
     */
    protected abstract void runInLoop() throws Exception ;

    
    /**
     * Action after of stopping loop.  
     */
    protected void cleanUp() throws Exception
    {    	
    }
    
    /*
    @Override
    public void addStoppableThreadEventListener( IStoppableThreadEventListener listener )
    {
    	this.listenerList.add( IStoppableThreadEventListener.class, listener );
    }
	
    @Override
	public void removeStoppableThreadEventControl( IStoppableThreadEventListener listener )
	{
		this.listenerList.remove( IStoppableThreadEventListener.class, listener );
	}
	*/
	
    /*
	private void fireStartedEvent()
	{
		StoppableThreadEvent event = new StoppableThreadEvent( this, super.getId(), Thread.State.RUNNABLE );

		IStoppableThreadEventListener[] listeners = this.listenerList.getListeners( IStoppableThreadEventListener.class );

		for (int i = 0; i < listeners.length; i++ ) 
		{
			listeners[ i ].Started( event );
		}
	}
	
	private void fireStartLoopInteractionEvent()
	{
		StoppableThreadEvent event = new StoppableThreadEvent( this, super.getId(), Thread.State.RUNNABLE );

		IStoppableThreadEventListener[] listeners = this.listenerList.getListeners( IStoppableThreadEventListener.class );

		for (int i = 0; i < listeners.length; i++ ) 
		{
			listeners[ i ].LoopStartInteraction( event );
		}
	}
	
	private void fireEndLoopInteractionEvent()
	{
		StoppableThreadEvent event = new StoppableThreadEvent( this, super.getId(), Thread.State.RUNNABLE );

		IStoppableThreadEventListener[] listeners = this.listenerList.getListeners( IStoppableThreadEventListener.class );

		for (int i = 0; i < listeners.length; i++ ) 
		{
			listeners[ i ].LoopEndInteraction(event );
		}
	}
	
	private void fireFinishedLoopEvent()
	{
		StoppableThreadEvent event = new StoppableThreadEvent( this, super.getId(), Thread.State.RUNNABLE );

		IStoppableThreadEventListener[] listeners = this.listenerList.getListeners( IStoppableThreadEventListener.class );

		for (int i = 0; i < listeners.length; i++ ) 
		{
			listeners[ i ].Termined( event );
		}
	}
	
	private void fireTerminedEvent()
	{
		StoppableThreadEvent event = new StoppableThreadEvent( this, super.getId(), Thread.State.TERMINATED );

		IStoppableThreadEventListener[] listeners = this.listenerList.getListeners( IStoppableThreadEventListener.class );

		for (int i = 0; i < listeners.length; i++ ) 
		{
			listeners[ i ].Termined( event );
		}
	}	
	*/
}