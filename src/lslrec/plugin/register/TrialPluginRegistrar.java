/**
 * 
 */
package lslrec.plugin.register;

import java.util.List;

import lslrec.config.Parameter;
import lslrec.plugin.lslrecPlugin.trial.ILSLRecPluginTrial;

/**
 * @author Manuel Merino Monge
 *
 */
public class TrialPluginRegistrar 
{
	private static ILSLRecPluginTrial trial = null;
	
	public static void setTrialPlugin( ILSLRecPluginTrial test )
	{
		trial = test;
	}
	
	public static void removeTrialPlugin()
	{
		trial = null;
	}
	
	public static ILSLRecPluginTrial getNewInstanceOfTrialPlugin()
	{
		ILSLRecPluginTrial test = null;
	
		try
		{
			test = trial.getClass().newInstance();
			
			List< Parameter< String > > pars = trial.getSettings();
			
			test.loadSettings( pars  );
		}
		catch( Exception e )
		{			
			e.printStackTrace();
		}
		
		return test;
	}
}
