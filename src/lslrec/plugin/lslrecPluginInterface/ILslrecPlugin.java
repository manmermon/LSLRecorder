package lslrec.plugin.lslrecPluginInterface;

import javax.swing.JPanel;

import lslrec.config.Parameter;
import lslrec.config.ParameterList;

public interface ILslrecPlugin 
{	
	public enum Type
	{
		ENCODER ,SYNC_METHOD, TRIAL, ONLINE_DATA_PROCESSING, OFFLINE_DATA_PROCESSING
	};
	
	
	//
	//
	//
	
	public String getID();
	
	public Type getPluginType();
	
	//
	// Plugin setting
	//
	
	public JPanel getSettingPanel();
	
	public ParameterList getSettings();
	
	public Parameter getSettingParameter( String ID );
}
