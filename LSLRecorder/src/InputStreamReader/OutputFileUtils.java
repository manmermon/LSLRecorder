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
package InputStreamReader;

import java.io.File;

import javax.annotation.processing.FilerException;


public class OutputFileUtils 
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
}
