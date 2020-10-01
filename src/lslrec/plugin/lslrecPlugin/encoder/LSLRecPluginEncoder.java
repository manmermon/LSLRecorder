package lslrec.plugin.lslrecPlugin.encoder;

import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.plugin.lslrecPlugin.LSLRecConfigurablePluginAbstract;

public abstract class LSLRecPluginEncoder extends LSLRecConfigurablePluginAbstract
{
	public abstract Encoder getEncoder( );	
}
