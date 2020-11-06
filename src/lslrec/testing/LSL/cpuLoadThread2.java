package testing.LSL;

import java.lang.management.ManagementFactory;
import java.util.Timer;
import java.util.TimerTask;

import org.hyperic.sigar.ProcCpu;
import org.hyperic.sigar.Sigar;
import org.hyperic.sigar.SigarException;

import StoppableThread.AbstractStoppableThread;
import config.ConfigApp;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSL.StreamInfo;
import edu.ucsd.sccn.LSL.StreamOutlet;

public class cpuLoadThread2 extends AbstractStoppableThread 
{
	private long time = 1000L;
	private StreamOutlet outLet = null;
	private StreamInfo info = null;
	
	private double[] loads = new double[ 1 ];
	private SigarLoadMonitor sg = null;
	
	private boolean sendData = false;
	
	public cpuLoadThread2() throws Exception
	{
		this.info = new LSL.StreamInfo( ConfigApp.shortNameApp + "-" + this.getClass().getSimpleName()
												, "value"
												, loads.length
												, LSL.IRREGULAR_RATE
												, LSL.ChannelFormat.float32
												, this.getId() + "" );
		this.addDesc();
	}
	
	public cpuLoadThread2( long timeCheck ) throws Exception
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
		
		super.setName( this.getClass().getSimpleName() );
	}
	
	private void addDesc() throws Exception
	{
		this.info.desc().append_child_value( "time", ""+time );
		this.info.desc().append_child_value( "idChannels", "Total CPU Load" );
		
	}

	@Override
	protected void preStart() throws Exception 
	{
		this.outLet = new StreamOutlet( this.info );
		
		this.sg = new SigarLoadMonitor( time );
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
			this.sendData = true;
			
			this.loads[ 0 ] = this.sg.getLoad();

			System.out.println("cpuLoadThread2.runInLoop() " + this.loads[ 0 ]);
			
			this.outLet.push_chunk( this.loads );
		}
	}
	
	@Override
	protected void targetDone() throws Exception 
	{
		if( !this.outLet.have_consumers() && this.sendData )
		{
			super.stopThread = true;
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

        private TimerTask updateLoadTask = new TimerTask() 
        {
            @Override public void run() 
            {            	
                try 
                {
                    ProcCpu curPc = sigar.getProcCpu(pid);
                    long totalDelta = curPc.getTotal() - prevPc.getTotal();
                    long timeDelta = curPc.getLastTime() - prevPc.getLastTime();
                    if (totalDelta == 0) 
                    {
                        if (timeDelta > TOTAL_TIME_UPDATE_LIMIT) 
                        {
                        	load = 0;
                        }
                        
                        if ( load == 0 ) 
                        {
                        	prevPc = curPc;
                        }
                    } 
                    else 
                    {
                        load = 100. * totalDelta / timeDelta / cpuCount;
                        prevPc = curPc;
                    }
                }
                catch (SigarException ex) 
                {
                    throw new RuntimeException(ex);
                }
            }
        };

        public SigarLoadMonitor( long time ) throws SigarException 
        {
            sigar = new Sigar();
            cpuCount = sigar.getCpuList().length;
            String PID = "";
            for( char c : ManagementFactory.getRuntimeMXBean().getName().toCharArray() )
            {
            	try
            	{            		
            		PID += (new Long( c+"" )).toString() ;
            	}
            	catch( Exception ex )
            	{
            		break;
            	}
            }
            
            System.out.println("cpuLoadThread2.SigarLoadMonitor.SigarLoadMonitor() " + PID);
            
            pid = new Long( PID );
            prevPc = sigar.getProcCpu( pid );
            load = 0;
            new Timer( "cpuLoadTimer", true).schedule(updateLoadTask, 0, time );
        }

        public double getLoad() 
        {
            return load;
        }
    }
}
