/**
 * 
 */
package lslrec.gui.panel.plugin;

import java.awt.BorderLayout;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import lslrec.config.language.Language;
import lslrec.plugin.loader.PluginLoader;
import lslrec.plugin.lslrecPlugin.ILSLRecConfigurablePlugin;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin.PluginType;

/**
 * @author Manuel Merino Monge
 *
 */
public class Panel_PluginSettings extends JPanel 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5517816000307765982L;
	
	private JTabbedPane tab = null;
	
	public Panel_PluginSettings() 
	{
		super.setLayout( new BorderLayout() );
		
		super.add( this.getTabbedPanel(), BorderLayout.CENTER );
		
		this.loadPluginSettingTab( );
	}

	private JTabbedPane getTabbedPanel()
	{
		if( this.tab == null )
		{
			this.tab = new JTabbedPane( JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT );
		}
		
		return this.tab;
	}
		
	private void loadPluginSettingTab( )
	{
		JTabbedPane plugingTabPanel = this.getTabbedPanel();
		
		PluginLoader loader;
		try 
		{
			loader = PluginLoader.getInstance();
		
			for( PluginType pluginType : PluginType.values() )
			{	
				String pluginCategory = Language.getLocalCaption( Language.OTHERS_TEXT );						
				switch( pluginType )
				{
					case SYNC:
					{
						pluginCategory = Language.getLocalCaption( Language.SETTING_LSL_SYNC );
						break;
					}
					case COMPRESSOR:
					{
						pluginCategory = Language.getLocalCaption( Language.SETTING_COMPRESSOR );
						break;
					}
					case TRIAL:
					{
						pluginCategory = Language.getLocalCaption( Language.TEST_TEXT );						
						break;
					}
					case ENCODER:
					{
						pluginCategory = Language.getLocalCaption( Language.ENCODER_TEXT );
						break;
					}
					case DATA_PROCESSING:
					{
						pluginCategory = Language.getLocalCaption( Language.PROCESS_TEXT );											
						break;
					}
				}
				
				try
				{
					int indTab = plugingTabPanel.indexOfTab( pluginCategory );
					
					List< ILSLRecPlugin > list = loader.getPluginsByType( pluginType );
					if( list != null )
					{
						ILSLRecConfigurablePlugin[] plgs = list.toArray( new ILSLRecConfigurablePlugin[ 0 ] );
					
						if( pluginType == PluginType.DATA_PROCESSING 
								|| pluginType == PluginType.TRIAL )
						{
							Set< String > idPlugins = new HashSet< String >();
							
							for( ILSLRecConfigurablePlugin pl : plgs )
							{
								idPlugins.add( pl.getID() );
							}
							
							int selMode = PluginSelectorPanel.MULTIPLE_SELECTION;
							if( pluginType == PluginType.TRIAL )
							{
								selMode = PluginSelectorPanel.SINGLE_SELECTION;
								
							}
							
							PluginSelectorPanel psp = new PluginSelectorPanel( pluginType, idPlugins, selMode );
														
							plugingTabPanel.addTab( pluginCategory, new JScrollPane( psp ) );
						} 
						else
						{
							for( ILSLRecConfigurablePlugin p : plgs )
							{	
								JPanel settingPanel = p.getSettingPanel();
								
								if( settingPanel != null )
								{
									JTabbedPane subTab = new JTabbedPane( JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT ); 
									if( indTab < 0 )
									{			
										plugingTabPanel.addTab( pluginCategory, subTab );
									}
									else
									{
										subTab = (JTabbedPane)plugingTabPanel.getComponentAt( indTab );
									}
									
									subTab.addTab( p.getID(), new JScrollPane( settingPanel ) );
								}
							}
						}
					}
				}			
				catch ( ClassCastException e)
				{
				}
			}
		}
		catch (Exception e1) 
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
	}
}
