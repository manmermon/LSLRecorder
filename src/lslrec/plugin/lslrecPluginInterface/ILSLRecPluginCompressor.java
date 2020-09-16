/**
 * 
 */
package lslrec.plugin.lslrecPluginInterface;

import lslrec.dataStream.outputDataFile.compress.IOutZip;

/**
 * @author Manuel Merino Monge
 *
 */
public interface ILSLRecPluginCompressor extends ILSLRecConfigurablePlugin 
{
	public IOutZip getCompressor();
}
