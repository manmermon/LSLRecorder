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
package lslrec.plugin.lslrecPlugin.processing;

import java.util.Set;

import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing.ProcessingLocation;

public class PluginDataProcessingSettings extends ParameterList 
{
	public static final String PAR_OUTPUT_FOLDER = "PAR_OUTPUT_FOLDER";
	public static final String PAR_PROCESSING_LOCATION = "PAR_PROCESSING_LOCATION";
	
	private IStreamSetting streamSettings;
	
	private ParameterList parlist;
	
	public PluginDataProcessingSettings( IStreamSetting streamSettings ) 
	{
		super();
		
		if( streamSettings == null )
		{
			throw new IllegalArgumentException( "IStreamSetting input null.");
		}
		
		this.streamSettings = streamSettings; 
		
		this.parlist = new ParameterList();
		
		this.parlist.addParameter( new Parameter< String >( PAR_OUTPUT_FOLDER, "./" ) );
		this.parlist.addParameter( new Parameter< ProcessingLocation >( PAR_PROCESSING_LOCATION, ProcessingLocation.DURING ) );
	}
	
	public IStreamSetting getStreamSettings() 
	{
		return streamSettings;
	}
	
	public Set< String > getParameterIDs()
	{
		return this.parlist.getParameterIDs();
	}
	
	public Parameter getParameter( String id )
	{
		return this.parlist.getParameter( id );
	}
	
	public void setParameter( String id, Object value )
	{
		this.parlist.addParameter( new Parameter( id, value ));
	}
}
