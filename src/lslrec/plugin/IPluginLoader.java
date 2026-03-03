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
package lslrec.plugin;

import java.util.List;

import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin.PluginType;

/**
 * @author Manuel Merino Monge
 *
 */
public interface IPluginLoader 
{
	 public List< ILSLRecPlugin > getPluginsByType( PluginType plgType );

	 public List< ILSLRecPlugin > getAllPlugins( PluginType plgClss, String id );

	 public ILSLRecPlugin createNewPluginInstance( PluginType plgType, String id, boolean registerInstance );

	 public ILSLRecPlugin removePluginInstance( PluginType plgCl, String id, int index );

	 public List< ILSLRecPlugin > getPlugins(); 
}
