/* 
 * Copyright 2018-2026 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.  https://github.com/manmermon/LSLRecorder
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
package lslrec.auxiliar.thread;

import java.io.File;

import com.sun.jna.Platform;

import lslrec.config.GeneralSettings;
import lslrec.config.language.Language;
import lslrec.dataStream.family.DataStreamFactory;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.stream.IDataStream;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionMessage;

public class StreamResponseTester 
{
    public static void main( String[] args ) 
    {
        try 
        {
    		String p = System.getProperty("user.dir") + "/" + GeneralSettings.SYSTEM_LIB_WIN_PATH;

    		if (Platform.getOSType() == Platform.LINUX) 
    		{
    			p = System.getProperty("user.dir") + "/" + GeneralSettings.SYSTEM_LIB_LINUX_PATH;
    		}
    		else if (Platform.getOSType() == Platform.MAC) 
    		{
    			p = System.getProperty("user.dir") + "/" + GeneralSettings.SYSTEM_LIB_MACOS_PATH;
    		}

    		try 
    		{
    			addLibraryPath(p);			
    		}
    		catch (Exception | Error e) 
    		{
    			showError(e, false);
    		}
    		
            String resultado = check();

            System.out.println( resultado );

        } 
        catch (Exception e) 
        {
            System.exit(1);
        }
    }
    
    private static void addLibraryPath(String pathToAdd) throws Exception 
	{		
		if( pathToAdd != null )
		{
			File folder = new File( pathToAdd );
			
			if( folder.exists() && folder.isDirectory() )
			{
				for( File f : folder.listFiles() )
				{
					String pathFile = f.getAbsolutePath();
					try
					{
						System.load( pathFile );
					}
					catch (Exception | Error e) 
					{
					}
				}
			}
		}
	}

    private static String check( ) 
    {
    	String state = "OK";
    	
    	IStreamSetting[] streams = DataStreamFactory.getStreamSettings();
    	
    	for( IStreamSetting sst : streams )
    	{
    		try 
    		{
				IDataStream stream = DataStreamFactory.createDataStream( sst );
				
				stream.info().description();
			}
    		catch (Exception e) 
    		{
				e.printStackTrace();
				state = "ERROR - " + e.getMessage();
			}
    	}
    	    	
        return state;
    }
    
    private static void showError(Throwable e, final boolean fatalError) 
	{
		if (fatalError) 
		{
			ExceptionDialog.AppExitWhenWindowClosing();
		}

		ExceptionMessage msg = new ExceptionMessage(e, Language.getLocalCaption(Language.DIALOG_ERROR),
														ExceptionMessage.ERROR_MESSAGE);

		ExceptionDialog.showMessageDialog(msg, true, true);
	}
}