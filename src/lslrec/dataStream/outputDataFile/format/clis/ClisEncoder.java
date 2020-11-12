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
package lslrec.dataStream.outputDataFile.format.clis;

import java.util.ArrayList;
import java.util.List;

import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.config.ConfigApp;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.config.SettingOptions;
import lslrec.config.SettingOptions.Type;
import lslrec.config.language.Language;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.outputDataFile.compress.CompressorDataFactory;
import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.dataStream.outputDataFile.format.IOutputDataFileWriter;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.outputDataFile.format.clis.parallel.OutputCLISDataParallelWriter;

public class ClisEncoder implements Encoder 
{
	@Override
	public List<SettingOptions> getSettiongOptions() 
	{
		List< SettingOptions > opts = new ArrayList< SettingOptions >();
		
		SettingOptions zips = new SettingOptions( OutputFileFormatParameters.ZIP_ID, Type.STRING, true, ConfigApp.OUTPUT_COMPRESSOR );
		
		String selZip = ConfigApp.getProperty( ConfigApp.OUTPUT_COMPRESSOR ).toString().toLowerCase();
		
		String[] comps = CompressorDataFactory.getCompressorIDs();
		
		for( int i = 0; i < comps.length; i++ )
		{								
			String zip = comps[ i ];
			
			zips.addValue( zip );
			
			if( zip.toLowerCase().equals( selZip) )
			{
				zips.setSelectedValue( i );
			}
		}
		
		SettingOptions parall = new SettingOptions( OutputFileFormatParameters.PARALLELIZE
													, Type.BOOLEAN, false, ConfigApp.OUTPUT_PARALLELIZE );
		
		parall.addValue( ConfigApp.getProperty( ConfigApp.OUTPUT_PARALLELIZE ).toString() );
		
		opts.add( zips );
		opts.add( parall );
				
		return opts;
	}
	
	@Override
	public ParameterList getParameters() 
	{
		ParameterList pars = new ParameterList();
		
		Parameter< Boolean > par1 = new Parameter<Boolean>( ConfigApp.OUTPUT_PARALLELIZE, true );
		par1.setLangID( Language.PARALLELIZE_TEXT );
		pars.addParameter( par1 );
		
		Parameter< String > par2 = new Parameter<String>( ConfigApp.OUTPUT_COMPRESSOR, CompressorDataFactory.GZIP );
		par2.setLangID( Language.SETTING_COMPRESSOR );
		pars.addParameter( par2 );
		
		return pars;
	}

	@Override
	public IOutputDataFileWriter getWriter( OutputFileFormatParameters pars, IStreamSetting streamSettings, ITaskMonitor monitor )
			throws Exception 
	{
		IOutputDataFileWriter wr = null;
		
		Parameter< Boolean> p = pars.getParameter( OutputFileFormatParameters.PARALLELIZE );
		if( p != null && p.getValue() != null && !p.getValue() )
		{
			wr = new OutputCLISDataWriter( pars, streamSettings, monitor);
		}
		else
		{
			wr = new OutputCLISDataParallelWriter( pars, streamSettings, monitor );
		}
		
		return wr;
	}

	@Override
	public String getOutputFileExtension() 
	{
		return ".clis";
	}

	@Override
	public boolean isSupportedEncryption() 
	{
		return true;
	}

	@Override
	public String getID() 
	{
		return "CLIS";
	}

}
