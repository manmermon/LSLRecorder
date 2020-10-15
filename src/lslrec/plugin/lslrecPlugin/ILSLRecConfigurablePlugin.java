/**
 * 
 */
package lslrec.plugin.lslrecPlugin;

import java.util.List;

import javax.swing.JPanel;

import lslrec.auxiliar.WarningMessage;
import lslrec.config.Parameter;

/**
 * @author Manuel Merino Monge
 *
 */
public interface ILSLRecConfigurablePlugin extends ILSLRecPlugin
{
	//
	// Plugin setting
	//
	
	public JPanel getSettingPanel();
	
	public void loadSettings( List< Parameter< String > > pars );
	
	public List< Parameter< String > > getSettings();
	
	public WarningMessage checkSettings();
}
