/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.
 *
 *   LSLRec is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   LSLRec is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with LSLRec.  If not, see <http://www.gnu.org/licenses/>.
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
	
	public static boolean isSelectedTrialPlugin()
	{
		return ( trial != null );
	}
	
	public static boolean isSelected( String trialID )	
	{
		boolean eq = false;
		
		if( trialID != null && isSelectedTrialPlugin() )
		{
			eq = trial.getID().equals( trialID );
		}
		
		return eq;
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
			if( trial != null )
			{
				test = trial.getClass().newInstance();
				
				List< Parameter< String > > pars = trial.getSettings();
				
				test.loadSettings( pars  );
			}
		}
		catch( Exception e )
		{			
			e.printStackTrace();
		}
		
		return test;
	}
}
