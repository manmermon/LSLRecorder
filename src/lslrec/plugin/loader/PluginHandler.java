package lslrec.plugin.loader;

import java.util.List;

import lslrec.auxiliar.WarningMessage;
import lslrec.auxiliar.tasks.INotificationTask;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.controls.HandlerMinionTemplate;
import lslrec.controls.MinionParameters;
import lslrec.stoppableThread.IStoppableThread;

public class PluginHandler extends HandlerMinionTemplate implements ITaskMonitor 
{

	@Override
	public WarningMessage checkParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void taskDone(INotificationTask task) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void cleanUpSubordinates() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void startWork(Object info) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected List<IStoppableThread> createSubordinates(MinionParameters parameters) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected void preStopThread(int friendliness) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void runInLoop() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
