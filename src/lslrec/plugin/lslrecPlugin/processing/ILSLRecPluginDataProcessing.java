/**
 * 
 */
package lslrec.plugin.lslrecPlugin.processing;

import lslrec.dataStream.setting.DataStreamSetting;
import lslrec.plugin.lslrecPlugin.ILSLRecConfigurablePlugin;

/**
 * @author Manuel Merino Monge
 *
 */
public interface ILSLRecPluginDataProcessing extends ILSLRecConfigurablePlugin 
{
	public LSLRecPluginDataProcessing getProcessing( DataStreamSetting setting, LSLRecPluginDataProcessing prevDataProcessing );
}
