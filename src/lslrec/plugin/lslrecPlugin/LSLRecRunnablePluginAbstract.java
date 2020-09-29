/**
 * 
 */
package lslrec.plugin.lslrecPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import lslrec.config.Parameter;

/**
 * @author Manuel Merino Monge
 *
 */
public abstract class LSLRecRunnablePluginAbstract implements ILSLRecConfigurablePlugin 
{	
	protected Map< String, Parameter< String > > pars;
	
	private JPanel SettingPanel = null;
	
	/**
	 * 
	 */
	public LSLRecRunnablePluginAbstract( )
	{		
		this.pars = new HashMap< String, Parameter< String > >();
				
		this.SettingPanel = new JPanel();
	}
	
	@Override
	public void loadSettings( List< Parameter< String > > pars)
	{
		if( pars != null )
		{
			for( Parameter< String > p : pars )
			{
				this.pars.put( p.getID(), p );
			}			
		}
	}
	
	@Override
	public List< Parameter< String > > getSettings()
	{
		return new ArrayList< Parameter< String > >( this.pars.values() );
	}
	
	@Override
	public JPanel getSettingPanel()
	{
		this.setSettingPanel( this.SettingPanel );
		
		return this.SettingPanel;
	}
	
	protected abstract void setSettingPanel( JPanel panel );
}
