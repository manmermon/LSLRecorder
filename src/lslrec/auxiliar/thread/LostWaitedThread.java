package lslrec.auxiliar.thread;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import lslrec.auxiliar.task.NotificationTask;
import lslrec.dataStream.outputDataFile.format.IOutputDataFileWriter;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;

public class LostWaitedThread extends AbstractStoppableThread 
{
	private static LostWaitedThread lwt = null;
	
	private ConcurrentLinkedQueue< Long > idThreads = null;
	
	private LostWaitedThread()
	{
		this.idThreads = new ConcurrentLinkedQueue< Long >();
		
		super.setName( super.getClass().getName() );
	}
	
	public static LostWaitedThread getInstance()
	{
		if( lwt == null )
		{
			lwt = new LostWaitedThread();
		}
		
		return lwt;
	}
	
	private boolean checkLostWaitOutputFile( Long id )
	{
		boolean find = false;
		
		ThreadMXBean mbean = ManagementFactory.getThreadMXBean();
		
		long[] waitedThreadIds =  mbean.getAllThreadIds();
		if( id != null )
		{
			waitedThreadIds = new long[] { id };
		}
		
		if (waitedThreadIds != null) 
		{
			ThreadInfo[] threadInfos = mbean.getThreadInfo( waitedThreadIds );
			
			Map< Thread, StackTraceElement[] > thrs = new HashMap<Thread, StackTraceElement[]>();
			
			for ( ThreadInfo threadInfo : threadInfos ) 
			{
				if (threadInfo != null) 
				{
					Map< Thread, StackTraceElement[] > stackTraceMap = Thread.getAllStackTraces();
					for (Thread thread : stackTraceMap .keySet() ) 
					{
						if ( thread.getId() == threadInfo.getThreadId() ) 
						{	
							thrs.put( thread, stackTraceMap.get( thread ) );
						}
					}
				}
			}
			
			for( Thread th : thrs.keySet() )
			{
				if( th instanceof IOutputDataFileWriter )
				{
					synchronized ( th )
					{
						try 
						{
							((IOutputDataFileWriter)th).close();
						}
						catch (Exception e) 
						{
						}
						
						th.notify();
					}
					
					find = true;
				}
				else if( th instanceof NotificationTask )
				{
					if( th.getState().equals( Thread.State.WAITING ) && ((NotificationTask)th).isDeletable() )
					{
						synchronized ( th )
						{
							try 
							{
								((NotificationTask)th).stopThread( IStoppableThread.FORCE_STOP );
							}
							catch (Exception e) 
							{
							}
							
							th.notify();
						}
						
						find = true;
					}
				}
			}
		}
		
		return find;
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
	protected void runInLoop() throws Exception 
	{
		synchronized( this )
		{
			if( this.idThreads.isEmpty() )
			{
				super.wait();
			}
		}
		
		if( !this.idThreads.isEmpty() )
		{
			while( !this.idThreads.isEmpty() )
			{
				while( this.checkLostWaitOutputFile( this.idThreads.poll() ) )
				{
					super.wait( 100L );
				}
			}
		}
		else
		{
			while( this.checkLostWaitOutputFile( null ) )
			{
				super.wait( 100L );
			}
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
	
	public void wakeup()
	{
		synchronized( this )
		{
			super.notify();
		}
	}
	
	public synchronized void wakeup( long idThread )
	{
		synchronized( this )
		{
			this.idThreads.add( idThread );
			
			super.notify();
		}
	}
	
}
