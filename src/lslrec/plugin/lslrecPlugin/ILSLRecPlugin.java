package lslrec.plugin.lslrecPlugin;

import lslrec.auxiliar.tasks.ITaskIdentity;

public interface ILSLRecPlugin extends ITaskIdentity
{	
	//
	//
	//
	
	public static final double VERSION = 1.0;
	
	public enum PluginType { COMPRESSOR, ENCODER, DATA_PROCESSING, TRIAL, SYNC };
	
	public PluginType getType();
}
