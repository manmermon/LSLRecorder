package lslrec.plugin.lslrecPluginInterface;

import javax.swing.JPanel;

import lslrec.auxiliar.tasks.ITaskIdentity;
import lslrec.config.ParameterList;

public interface ILSLRecPlugin extends ITaskIdentity
{	
	//
	//
	//
	
	public static final double VERSION = 1.0;
	
	//
	// Plugin setting
	//
	
	public JPanel getSettingPanel();
	
	public void loadSettings( ParameterList pars );
	
	public ParameterList getSettings();
}
