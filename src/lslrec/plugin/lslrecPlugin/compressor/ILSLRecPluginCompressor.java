/**
 * 
 */
package lslrec.plugin.lslrecPlugin.compressor;

import lslrec.dataStream.outputDataFile.compress.IOutZip;
import lslrec.plugin.lslrecPlugin.ILSLRecConfigurablePlugin;

/**
 * @author Manuel Merino Monge
 *
 */
public interface ILSLRecPluginCompressor extends ILSLRecConfigurablePlugin 
{
	public IOutZip getCompressor();
}
