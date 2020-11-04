/**
 * 
 */
package lslrec.auxiliar.thread;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JOptionPane;

import lslrec.auxiliar.tasks.INotificationTask;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.config.language.Language;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionDictionary;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.gui.GuiManager;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;
import lslrec.stoppableThread.ThreadStopException;

/**
 * @author Manuel Merino Monge
 *
 * From: https://dzone.com/articles/how-detect-java-deadlocks
 */
public class DeadlockDetector extends AbstractStoppableThread
{	
	private long period = 1000L; // a second
	private int numIter = 10;
	
	private Map< Thread, Integer > counter = new HashMap<Thread, Integer>();
	
	private ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
		
	public DeadlockDetector( final long period, final int iter ) 
	{
		this.period = period;
		
		if( this.period < 1000L )
		{
			this.period = 1000L;
		}
		
		this.numIter = iter;
		
		if( this.numIter < 1 )
		{
			this.numIter = 10;
		}
		
		super.setName( this.getClass().getCanonicalName() );
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
		
		this.mbean = null;
		this.counter.clear();
	}
	
	@Override
	protected void runInLoop() throws Exception 
	{
		long[] deadlockedThreadIds =  this.mbean.findDeadlockedThreads();
		
		if (deadlockedThreadIds != null) 
		{
			ThreadInfo[] threadInfos = this.mbean.getThreadInfo( deadlockedThreadIds );

			Map< Thread, StackTraceElement[] > thrs = this.handleDeadlock( threadInfos );
			
			for( Thread th : thrs.keySet() )
			{
				Integer value = this.counter.get( th );
				
				if( value == null )
				{
					value = numIter;
				}
				
				value--;
				
				if( value <= 0 )
				{						
					if( value == 0 )
					{	
						if( th instanceof IStoppableThread )
						{
							((IStoppableThread)th).stopThread( IStoppableThread.ERROR_STOP );
						}
						
						ThreadStopException ex = new ThreadStopException( th.getName() + ": deadlock detected." );
						ex.setStackTrace( thrs.get( th ) );
						
						ExceptionMessage msg = new ExceptionMessage( ex, "Deadlock", ExceptionDictionary.ERROR_MESSAGE );
						
						ExceptionDialog.showMessageDialog( msg, true, true );
					}
					else if( value < -this.numIter )
					{
						th.stop();
					}
				}
				
				this.counter.put( th, value );
			}
			
			Iterator< Thread > itTh = this.counter.keySet().iterator();
			
			while( itTh.hasNext() )
			{
				Thread th = itTh.next();
				
				if( !thrs.containsKey( th ) )
				{
					itTh.remove();
				}
			}			
		}
		
		try
		{
			super.wait( this.period );
		}
		catch (Exception e) 
		{
		}
	}
	
	@Override
	protected void runExceptionManager(Throwable e) 
	{
		if( !( e instanceof InterruptedException ) )
		{
			super.runExceptionManager(e);
		}
	}
	
	public Map< Thread, StackTraceElement[] > handleDeadlock( final ThreadInfo[] deadlockedThreads )
	{
		Map< Thread, StackTraceElement[] > deadlocks = new HashMap<Thread, StackTraceElement[]>();
		
		if (deadlockedThreads != null) 
		{
			for ( ThreadInfo threadInfo : deadlockedThreads ) 
			{
				if (threadInfo != null) 
				{
					Map< Thread, StackTraceElement[] > stackTraceMap = Thread.getAllStackTraces();
					for (Thread thread : stackTraceMap .keySet() ) 
					{
						if ( thread.getId() == threadInfo.getThreadId() ) 
						{	
							deadlocks.put( thread, stackTraceMap.get( thread ) );
						}
					}
				}
			}
		}
		
		return deadlocks;
	}


	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{ 
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{	
	}

	

}
