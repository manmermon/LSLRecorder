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
package auxiliar.extra;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.annotation.processing.FilerException;


public class FileUtils 
{	
	public static File CreateTemporalBinFile ( String filePath ) throws Exception
	{
		File file = new File( filePath );

		int index = filePath.lastIndexOf("/");
		if (index < 0)
		{
			index = filePath.lastIndexOf("\\");
		}

		File dir = null;
		if (index >= 0)
		{
			String folder = filePath.substring(0, index + 1);
			dir = new File(folder);
		}

		boolean ok = true;
		String errorMsg = "Problem: file " + filePath;

		if ((dir != null) && ( file != null))
		{
			try
			{
				if (!dir.exists())
				{
					if (!dir.mkdir())
					{
						ok = false;
					}
				}

				if (!file.exists())
				{
					file.createNewFile();
				}

				if( !file.isFile() || !file.canWrite() )
				{   
					ok = false;
					errorMsg += " is not files or it is not possible to write";
				}

			}
			catch (Exception e)
			{
				ok = false;
				errorMsg = errorMsg + e.getMessage();
			}
		}
		else
		{
			ok = false;
			errorMsg = errorMsg + " not found";
		}

		if (!ok)
		{
			throw new FilerException(errorMsg);
		}
		
		return file;
	}
	
	public static void CreateTemporalBinFile( File file ) throws Exception
	{
		if( file != null )
		{
			if (!file.exists())
			{
				file.createNewFile();
			}
	
			if (( !file.isFile() ) || ( !file.canWrite() ) )
			{
				throw new FilerException( file.getAbsolutePath() + " is not a file or is only read mode");
			}
		}
	}
	
	/**
	 * Format output data file name.
	 * 
	 * @param FilePath -> absolute file path.
	 * @param sourceID -> LSL streaming name.
	 * 
	 * @return Join file name and LSL name. File extension is conserved. Example: 
	 * 		- FilePath "data.clis"
	 * 		- sourceID "SerialPort"
	 * 	output is "data_SerialPort.clis"
	 */
	public static Tuple< String, Boolean > checkOutputFileName( String FilePath, String sourceID )
	{		
		boolean ok = true;
		boolean cont = true;

		Calendar c = Calendar.getInstance();

		int index = FilePath.lastIndexOf(".");
		String name = FilePath;
		String ext = "";
		if (index > -1)
		{
			name = FilePath.substring(0, index);
			ext = FilePath.substring(index);
		}

		String aux2 = name + "_" + sourceID + ext;
		while ( cont )
		{
			File file = new File(aux2);

			if ( file.exists() )
			{
				ok = false;

				c.add( 13, 1 );
				String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format( c.getTime() );

				aux2 = name + "_" + sourceID + "_" + date + ext;
			}
			else
			{
				cont = false;
			}
		}
		
		Tuple< String, Boolean > res = new Tuple< String, Boolean>(aux2,  ok );

		return res;
	}
}
