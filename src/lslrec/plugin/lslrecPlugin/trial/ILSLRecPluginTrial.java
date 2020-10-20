/**
 * 
 */
package lslrec.plugin.lslrecPlugin.trial;

import lslrec.plugin.lslrecPlugin.ILSLRecConfigurablePlugin;

/**
 * @author Manuel Merino Monge
 *
 */
public interface ILSLRecPluginTrial extends ILSLRecConfigurablePlugin 
{
	/**
	 * 
	 * @return true if Trial has one or more streams.
	 */
	public boolean activeTrialStream();
	
	public LSLRecPluginTrial getGUIExperiment();
}
