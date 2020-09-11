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
package testing.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import Config.language.Caption;
import Config.language.Language;
import Controls.Messages.RegisterSyncMessages;
import edu.ucsd.sccn.LSLConfigParameters;

public class testLanguage {

	public static void main(String[] args) 
	{
		Map< String, Caption > lng = Language.getAllCaptions( );
		
		int save = 1;
		
		if( save == 1 )
		{
			Properties prop = new Properties();
			
			Iterator< String > it = lng.keySet().iterator();
	
			while ( it.hasNext() )
			{
				String key = (String)it.next();
				
				prop.setProperty(key, lng.get( key ).getCaption( Language.defaultLanguage ) );			
			}
			
			FileOutputStream fOut;
			try 
			{
				fOut = new FileOutputStream( new File( Language.DefaultFolder + "default.lang" ), false);
				prop.store(fOut, "");
				fOut.close();
			} 
			catch (Exception e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if( save == 0)
		{
			Properties prop = new Properties();
			FileInputStream propFileIn = null;

			try
			{
				propFileIn = new FileInputStream( new File( Language.DefaultFolder + "es-es.lang" ) );

				prop.load( propFileIn );
				
				Object idLang = prop.get( Language.LANGUAGE );
				
				if( idLang != null )
				{
					prop.remove( Language.LANGUAGE );
					
					Caption cap = lng.get( Language.LANGUAGE );
					
					cap.setCaption( idLang.toString(), idLang.toString() );
					
					for( Object key : prop.keySet() )
					{
						Object val = prop.get( key );
						lng.get( key ).setCaption( idLang.toString(), val.toString() );
					}
				}
				
				System.out.println( lng );
				
			}
			catch( Exception ex )
			{
				ex.printStackTrace();
			}

		}
		else
		{
			Language.loadLanguages();
			System.out.println("testLanguage.main() " + Language.getLocalCaption( Language.ABOUT_SOURCE_CODE_LABEL ));
		}

		

	}

}
