package lslrec.plugin.lslrecPlugin.encoder;

import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;

public interface ILSLRecPluginEncoder extends ILSLRecPlugin
{
	public Encoder getEncoder( );	
}
