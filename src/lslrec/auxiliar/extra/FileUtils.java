/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.auxiliar.extra;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.annotation.processing.FilerException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import lslrec.auxiliar.WarningMessage;
import lslrec.config.ConfigApp;
import lslrec.config.language.Language;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.gui.AppUI;

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
	 * @param suffix -> suffix if file exist
	 * 
	 * @return Join file name and LSL name. File extension is conserved. Example: 
	 * 		- FilePath "data.clis"
	 * 		- sourceID "SerialPort"
	 * 	output is "data_SerialPort.clis"
	 */
	public static Tuple< String, Boolean > checkOutputFileName( String FilePath, String sourceID, String suffix )
	{		
		boolean ok = true;
		boolean cont = true;

		Calendar c = Calendar.getInstance();

		suffix = ( suffix == null ) ? "" : suffix;
		
		int index = FilePath.lastIndexOf(".");
		String name = FilePath;
		String ext = "";
		if (index > -1)
		{
			name = FilePath.substring(0, index);
			ext = FilePath.substring(index);
		}

		String aux2 = name;
		
		if( sourceID != null && !sourceID.isEmpty() )
		{
			aux2 += "_" + sourceID;
		}
		aux2 += ext;
		
		while ( cont )
		{
			File file = new File(aux2);

			if ( file.exists() )
			{
				ok = false;

				c.add( 13, 1 );
				String date = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format( c.getTime() );

				aux2 = name;

				if( sourceID != null && !sourceID.isEmpty() )
				{
					aux2 += "_" + sourceID;
				}
				aux2 += "_" + date + suffix + ext;
			}
			else
			{
				cont = false;
			}
		}
		
		Tuple< String, Boolean > res = new Tuple< String, Boolean>(aux2,  ok );

		return res;
	}

	public static File[] selectFile(String defaulName, String titleDialog
							, int typeDialog, boolean multiSelection
							, int selectionModel, String descrFilter
							, String[] filterExtensions, String defaultFolder )
	{		
		FileNameExtensionFilter filter = null;
				
		if( filterExtensions != null && filterExtensions.length > 0 )
		{
			filter = new FileNameExtensionFilter( descrFilter, filterExtensions );
		}
		
		
		File[] file = null;

		JFileChooser jfc = null;

		jfc = new JFileChooser( defaultFolder );

		jfc.setMultiSelectionEnabled(multiSelection);

		jfc.setDialogTitle(titleDialog);
		jfc.setDialogType(typeDialog);
		jfc.setFileSelectionMode(selectionModel);
		jfc.setSelectedFile(new File(defaulName));
		
		if( filter != null )
		{
			jfc.setFileFilter( filter );
		}

		int returnVal = jfc.showDialog( AppUI.getInstance(), null);

		if (returnVal == JFileChooser.APPROVE_OPTION )
		{
			if (multiSelection)
			{
				file = jfc.getSelectedFiles();
			}
			else
			{
				file = new File[1];
				file[0] = jfc.getSelectedFile();
			}
		}

		return file;
	}
	
	public static String[] selectUserFile(String defaultName, boolean mustExist
									, boolean multiSelection, int selectionModel
									, String descrFilter, String[] filterExtensions
									, String defaultFolder )
	{
		File[] f = selectFile( defaultName, Language.getLocalCaption( Language.DIALOG_SELECT_UESR_FILE )
								, JFileChooser.OPEN_DIALOG
								, multiSelection, selectionModel, descrFilter, filterExtensions, defaultFolder );

		int N = 1;
		
		if( f != null && f.length > 0 )
		{
			N = f.length;
		}
		
		String[] path = null;
				
		if (f != null)
		{			
			boolean allFileExist = true;
			for( int iF = 0; iF < N && allFileExist; iF++ )
			{
				allFileExist = f[ iF ].exists();				
			}
						
			
			if ( mustExist && !allFileExist )
			{
				path = null;
				
				Exception e = new Exception( Language.getLocalCaption( Language.FILE_NOT_FOUND ) );
				ExceptionMessage msg = new ExceptionMessage( e, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionMessage.WARNING_MESSAGE );
				ExceptionDialog.showMessageDialog( msg,	true, false );
			}
			else
			{
				path = new String[ N ];
				
				for( int iF = 0; iF < N; iF++ )
				{
					path[ iF ] = f[ iF ].getAbsolutePath();					
				}
			}
		}		

		return path;
	}

	public static String getOutputCompletedFileNameFromConfig()
	{
		String outFile = ConfigApp.getProperty( ConfigApp.OUTPUT_FILE_FOLDER ).toString();
		String subj = ConfigApp.getProperty( ConfigApp.OUTPUT_SUBJ_ID ).toString();
		String test = ConfigApp.getProperty( ConfigApp.OUTPUT_TEST_ID ).toString();
		String filename = ConfigApp.getProperty( ConfigApp.OUTPUT_FILE_NAME ).toString();
		
		outFile = outFile.isEmpty() ? "." : outFile;
		
		if( !outFile.endsWith( File.separator ) )
		{
			outFile += File.separator;
		}
		
		if( subj != null && !subj.isEmpty() )
		{
			outFile += subj + File.separator;
		}
		
		if( test != null && !test.isEmpty() )
		{
			outFile += test + File.separator;
		}
		
		if( filename == null || filename.isEmpty() )
		{
			outFile += "data";
		}
		else
		{
			outFile += filename;
		}
		
		String format = ConfigApp.getProperty( ConfigApp.OUTPUT_FILE_FORMAT ).toString();
		
		Tuple< Encoder, WarningMessage > tencoder = DataFileFormat.getDataFileEncoder( format );
		Encoder encorder = tencoder.t1;
				
		String ext = encorder.getOutputFileExtension();
		
		int pos = outFile.lastIndexOf( "." );
		if( pos > 0 )
		{
			outFile = outFile.substring( 0 , pos );
		}
		
		outFile += ext;
		
		return outFile;
	}

	public static boolean checkOutputOutputFilePath(  )
	{
		String folder = ConfigApp.getProperty( ConfigApp.OUTPUT_FILE_FOLDER ).toString();
		String name = ConfigApp.getProperty( ConfigApp.OUTPUT_FILE_NAME ).toString();
		String subj = ConfigApp.getProperty( ConfigApp.OUTPUT_SUBJ_ID ).toString();
		String test = ConfigApp.getProperty( ConfigApp.OUTPUT_TEST_ID ).toString();
		
		//File f = new File( folder );	
		//String folderTxt = f.getPath();
		
		boolean ok = true;
		
		try 
		{
			Paths.get( folder );
		} 
		catch (InvalidPathException | NullPointerException ex) 
		{
		  ok = false;
		}
		
		ok = ok && !folder.isEmpty();// && !folderTxt.matches( "[^\\.]+(\\.[\\\\/]).*" );
		
		ok = ok && name.matches("^[a-zA-Z0-9-_]+$")
						&& ( subj.isEmpty() || subj.matches("^[a-zA-Z0-9-_]+$" ) )
						&& ( test.isEmpty() || test.matches("^[a-zA-Z0-9-_]+$" ) );
		
		return ok;
	}
	
}
