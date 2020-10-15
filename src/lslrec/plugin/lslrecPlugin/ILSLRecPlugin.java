package lslrec.plugin.lslrecPlugin;

import lslrec.auxiliar.tasks.ITaskIdentity;

public interface ILSLRecPlugin extends ITaskIdentity, Comparable< ILSLRecPlugin >
{	
	//
	//
	//
	
	public static final double VERSION = 1.0;
	
	public enum PluginType { COMPRESSOR, ENCODER, DATA_PROCESSING, TRIAL, SYNC };
	
	public PluginType getType();
}
