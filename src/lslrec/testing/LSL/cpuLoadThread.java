package testing.LSL;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import Auxiliar.tasks.NotificationTask;
import Controls.core.CoreControl;
import DataStream.Binary.Input.writer.TemporalOutDataFileWriter;
import DataStream.OutputDataFile.IOutputDataFileWriter;
import DataStream.OutputDataFile.OutputBinaryFileSegmentation;
import DataStream.OutputDataFile.Format.Clis.parallel.ZipThread;
import DataStream.Sync.SyncMarkerCollectorWriter;
import DataStream.Sync.LSL.InputSyncData;
import Sockets.SocketMessageDelayCalculator;
import Sockets.SocketReaderThread;
import Sockets.TCP_UDPServer;
import StoppableThread.AbstractStoppableThread;
import config.ConfigApp;
import controls.OutputDataFileHandler;
import controls.SocketHandler;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSL.StreamInfo;
import edu.ucsd.sccn.LSL.StreamOutlet;

public class cpuLoadThread extends AbstractStoppableThread 
{
	private long time = 1000L;
	private StreamOutlet outLet = null;
	private StreamInfo info = null;
	
	private double[] loadPrev = new double[ 8 ];
	private double[] loads = new double[ 8 ];
	
	private List[] threadClass = new ArrayList[ loads.length ];
	
	private int cpus = Runtime.getRuntime().availableProcessors();
	
	private Map< Long, Long > prevCpuTime = new HashMap< Long, Long>();
	private double adj = 0;
	
	public cpuLoadThread() throws Exception
	{
		this.info = new LSL.StreamInfo( ConfigApp.shortNameApp + "-" + this.getClass().getSimpleName()
												, "value"
												, loads.length
												, 1000D / time
												, LSL.ChannelFormat.float32
												, this.getId() + "" );
		this.addDesc();
	}
	
	public cpuLoadThread( long timeCheck ) throws Exception
	{
		this();
		
		this.time  = timeCheck;
		this.info = new StreamInfo( info.name()
									, info.type()
									, info.channel_count()
									, LSL.IRREGULAR_RATE
									, info.channel_format()
									, info.source_id() );
		
		this.addDesc();
	}
	
	private void addDesc() throws Exception
	{
		this.info.desc().append_child_value( "time", ""+time );
		this.info.desc().append_child_value( "idChannels", "{coreControl, OutDataFileHandler, TemporalOutDataFileWriter, Sync-Socket, LSL-Sync, Converters, NotificationTask, Total}" );
		
		List l0 = new ArrayList();
		l0.add( CoreControl.class.getCanonicalName() );
		l0.add( CoreControl.controlNotifiedManager.class.getCanonicalName() );
		l0.add( CoreControl.NotifiedEventHandler.class.getCanonicalName() );
		l0.add( CoreControl.StopWorkingThread.class.getCanonicalName() );
		l0.add( CoreControl.WriteTestCalculator.class.getCanonicalName() );
		this.threadClass[ 0 ] = l0;
		
		List l1 = new ArrayList();
		l1.add( OutputDataFileHandler.class.getCanonicalName() );
		this.threadClass[ 1 ] = l1;
		
		List l2 = new ArrayList();
		l2.add( TemporalOutDataFileWriter.class.getCanonicalName() );
		this.threadClass[ 2 ] = l2;
		
		List l3 = new ArrayList();
		l3.add( SocketHandler.class.getCanonicalName() );		
		l3.add( SocketMessageDelayCalculator.class.getCanonicalName() );
		l3.add( TCP_UDPServer.class.getCanonicalName() );
		l3.add( SocketReaderThread.class.getCanonicalName() );
		this.threadClass[ 3 ] = l3;
				
		List l4 = new ArrayList();
		l4.add( SyncMarkerCollectorWriter.class.getCanonicalName() );		
		l4.add( InputSyncData.class.getCanonicalName() );
		this.threadClass[ 4 ] = l4;
		
		List l5 = new ArrayList();
		l5.add( OutputBinaryFileSegmentation.class.getCanonicalName() );
		l5.add( IOutputDataFileWriter.class.getCanonicalName() );
		l5.add( ZipThread.class );
		this.threadClass[ 5 ] = l5;
				
		List l6 = new ArrayList();
		l6.add( NotificationTask.class.getCanonicalName() );
		this.threadClass[ 6 ] = l6;
	}

	@Override
	protected void preStart() throws Exception 
	{
		this.outLet = new StreamOutlet( this.info );		
		
		this.adj = ( 100D / ( time / 1e3D ) );
		System.out.println("cpuLoadThread.preStart() " + adj);
	}
	
	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{	
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void startUp() throws Exception 
	{
		super.startUp();
	}
	
	@Override
	protected void runInLoop() throws Exception 
	{
		try
		{
			synchronized ( this )
			{
				super.wait( this.time );
			}
		}
		catch (Exception e) 
		{
		}
		
		if( this.outLet.have_consumers() )
		{
			ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
			Set< Thread > threadSet = Thread.getAllStackTraces().keySet();
			
			if( this.prevCpuTime.isEmpty() )
			{
				for(Long threadID : threadMXBean.getAllThreadIds()) 
				{
					this.prevCpuTime.put( threadID, threadMXBean.getThreadCpuTime( threadID ) );
				}
			}
						
			this.loads[ this.loads.length - 1 ] = 0;
			for(Long threadID : threadMXBean.getAllThreadIds()) 
			{
				long cputime = threadMXBean.getThreadCpuTime( threadID );
				
				this.loads[ this.loads.length - 1 ] += ( ( cputime - this.prevCpuTime.get( threadID ) ) / 1e9D );
				
				this.prevCpuTime.put( threadID, cputime );
			}
			
			this.loads[ this.loads.length - 1 ] = 100 * this.loads[ this.loads.length - 1 ] / ( time / 1e3D ); 
			
			
			int c = 0;
			for( List list : this.threadClass )
			{							
				if( list != null )
				{
					this.loads[ c ] = 0;
					
					for( Long thID : threadMXBean.getAllThreadIds() )
					{
						for( Thread thread : threadSet ) 
						{
							if( thread.getId() == thID )
							{							
								for( Object cl : list )
								{				
									if( thread.getClass().getCanonicalName().equals( cl.toString() ) )
									{
										long cputime = threadMXBean.getThreadCpuTime( thID );
										
										this.loads[ c ] += ( ( cputime - this.prevCpuTime.get( thID ) ) / 1e9D );
										
										this.prevCpuTime.put( thID, cputime );
									}
								}
							}						
						}
					}
				}
				
				this.loads[ c ] *= this.adj; 
				
				c++;
			}
			
			System.out.println("cpuLoadThread.runInLoop() " + Arrays.toString( this.loads ));

			this.outLet.push_chunk( this.loads );
		}
		else
		{
			this.prevCpuTime.clear();
		}
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{
		this.outLet.close();
	}

	public static class SigarLoadMonitor {

        private static final int TOTAL_TIME_UPDATE_LIMIT = 2000;

        private final Sigar sigar;
        private final int cpuCount;
        private final long pid;
        private ProcCpu prevPc;
        private double load;

        private TimerTask updateLoadTask = new TimerTask() {
            @Override public void run() {
                try {
                    ProcCpu curPc = sigar.getProcCpu(pid);
                    long totalDelta = curPc.getTotal() - prevPc.getTotal();
                    long timeDelta = curPc.getLastTime() - prevPc.getLastTime();
                    if (totalDelta == 0) {
                        if (timeDelta > TOTAL_TIME_UPDATE_LIMIT) load = 0;
                        if (load == 0) prevPc = curPc;
                    } else {
                        load = 100. * totalDelta / timeDelta / cpuCount;
                        prevPc = curPc;
                    }
                } catch (SigarException ex) {
                    throw new RuntimeException(ex);
                }
            }
        };

        public SigarLoadMonitor() throws SigarException {
            sigar = new Sigar();
            cpuCount = sigar.getCpuList().length;
            pid = 11876;
            prevPc = sigar.getProcCpu( pid );
            load = 0;
            new Timer(true).schedule(updateLoadTask, 0, 1000);
        }

        public double getLoad() {
            return load;
        }
    }
}
