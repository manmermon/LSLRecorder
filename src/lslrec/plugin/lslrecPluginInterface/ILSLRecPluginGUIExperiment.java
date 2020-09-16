package lslrec.plugin.lslrecPluginInterface;

import lslrec.auxiliar.tasks.IMonitoredTask;
import lslrec.auxiliar.tasks.INotificationTask;
import lslrec.stoppableThread.IStoppableThread;

public interface ILSLRecPluginGUIExperiment extends ILSLRecConfigurablePlugin
													, IMonitoredTask
													, INotificationTask
													, IStoppableThread
{	
	
}
