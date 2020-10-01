/**
 * 
 */
package lslrec.plugin.lslrecPlugin.compressor;

import lslrec.dataStream.outputDataFile.compress.IOutZip;
import lslrec.plugin.lslrecPlugin.LSLRecConfigurablePluginAbstract;

/**
 * @author Manuel Merino Monge
 *
 */
public abstract class LSLRecPluginCompressor extends LSLRecConfigurablePluginAbstract 
{
	public abstract IOutZip getCompressor();
}
