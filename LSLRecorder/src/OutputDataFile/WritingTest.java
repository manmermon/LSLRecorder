package OutputDataFile;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Timer;

import Auxiliar.LSLConfigParameters;
import Auxiliar.Extra.Tuple;
import Config.ConfigApp;
import Controls.Messages.EventInfo;
import Controls.Messages.eventType;
import StoppableThread.IStoppableThread;
import edu.ucsd.sccn.LSL.StreamInfo;

public class WritingTest extends TemporalOutputDataFile 
{
	private List< Long > times;
	private long initTime;
		
	public WritingTest(String filePath, StreamInfo info, LSLConfigParameters lslCfg, int Number) throws Exception 
	{
		super(filePath, info, lslCfg, Number);
	}	
	
	@Override
	protected void preStart() throws Exception 
	{
		super.preStart();
		
		this.times = new ArrayList< Long >();
	}

	@Override
	protected void startUp() throws Exception 
	{
		super.startUp();
	}
	
	@Override
	protected void managerData(byte[] data) throws Exception 
	{
		this.initTime = System.nanoTime();
		
		super.managerData( data );
		
		this.times.add( (System.nanoTime() - this.initTime ) );	
	}
	
	@Override
	protected String GetFinalOutEvent() 
	{
		return eventType.TEST_OUTPUT_TEMPORAL_FILE;
	}	
	
	@Override
	protected void postCleanUp() throws Exception 
	{
		EventInfo event = new EventInfo( eventType.TEST_WRITE_TIME, new Tuple< String, List< Long >>( super.LSLName, this.times ) );

		this.events.add(event);
		
		if (this.monitor != null)
		{
			this.monitor.taskDone(this);
		}
		
		super.postCleanUp();		
	}
	
}
