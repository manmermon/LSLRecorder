/**
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
