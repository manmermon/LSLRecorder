package lslrec.plugin.lslrecPluginInterface;

import lslrec.dataStream.outputDataFile.format.Encoder;

public interface ILSLRecPluginEncoder extends ILSLRecPlugin
{
	public Encoder getEncoder( );
	
}
