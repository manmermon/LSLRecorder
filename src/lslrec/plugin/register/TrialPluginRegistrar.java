/**
 * 
 */
package lslrec.plugin.register;

import lslrec.plugin.lslrecPlugin.trial.ILSLRecPluginGUIExperiment;

/**
 * @author Manuel Merino Monge
 *
 */
public class TrialPluginRegistrar 
{
	private static ILSLRecPluginGUIExperiment trial = null;
	
	public static void setTrialPlugin( ILSLRecPluginGUIExperiment test )
	{
		trial = test;
	}
	
	public static void removeTrialPlugin()
	{
		trial = null;
	}
	
	public static ILSLRecPluginGUIExperiment getNewInstanceOfTrialPlugin()
	{
		ILSLRecPluginGUIExperiment test = null;
	
		try
		{
			test = trial.getClass().newInstance();
			
			test.loadSettings( trial.getSettings() );
		}
		catch( Exception e )
		{			
		}
		
		return test;
	}
}
