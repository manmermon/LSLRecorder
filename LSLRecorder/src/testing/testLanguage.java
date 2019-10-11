package testing;

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

import Config.Language.Caption;
import Config.Language.Language;
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
