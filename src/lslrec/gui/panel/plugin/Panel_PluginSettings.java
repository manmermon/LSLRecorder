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
package lslrec.gui.panel.plugin;

import java.awt.BorderLayout;
import java.awt.Component;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import lslrec.config.language.Language;
import lslrec.gui.miscellany.DisabledPanel;
import lslrec.gui.panel.plugin.item.DataProcessingPluginSelectorPanel;
import lslrec.gui.panel.plugin.item.TrialPluginSelectorPanel;
import lslrec.plugin.loader.PluginLoader;
import lslrec.plugin.lslrecPlugin.ILSLRecConfigurablePlugin;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin.PluginType;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.trial.ILSLRecPluginTrial;

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
	
	private JPanel container = null;
	
	private JTabbedPane tab = null;
	
	// DisabledPanel
	private DisabledPanel disPanel;
	
	public Panel_PluginSettings() 
	{
		super.setLayout( new BorderLayout() );
		
		super.add( this.getDisabledPanel(), BorderLayout.CENTER );
		
		this.loadPluginSettingTab( );
	}
	
	private DisabledPanel getDisabledPanel( )
	{
		if( this.disPanel == null )
		{
			this.disPanel = new DisabledPanel( this.getContainerPanel() ); 
		}
		
		return this.disPanel;
	}
	
	public void enableSettings( boolean enable )
	{
		this.getDisabledPanel( ).setEnabled( enable );
	}
	
	private JPanel getContainerPanel()
	{
		if( this.container == null )
		{
			this.container = new JPanel( new BorderLayout() );
			
			this.container.add( this.getTabbedPanel(), BorderLayout.CENTER );
		}
		
		return this.container;
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
						pluginCategory = Language.getLocalCaption( Language.TRIALS_TEXT );						
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
					
						if( pluginType == PluginType.DATA_PROCESSING)
						{
							Set< ILSLRecPluginDataProcessing > idPlugins = new HashSet< ILSLRecPluginDataProcessing >();
							
							for( ILSLRecPlugin pl : plgs )
							{
								idPlugins.add( (ILSLRecPluginDataProcessing) pl );
							}
							
							DataProcessingPluginSelectorPanel psp = new DataProcessingPluginSelectorPanel( idPlugins );
														
							plugingTabPanel.addTab( pluginCategory, new JScrollPane( psp ) );
						} 
						else if( pluginType == PluginType.TRIAL) 
						{
							Set< ILSLRecPluginTrial > idPlugins = new HashSet< ILSLRecPluginTrial >();
							
							for( ILSLRecConfigurablePlugin pl : plgs )
							{
								idPlugins.add( (ILSLRecPluginTrial)pl );
							}
							
							TrialPluginSelectorPanel tsp = new TrialPluginSelectorPanel( idPlugins );
							
							plugingTabPanel.addTab( pluginCategory, new JScrollPane( tsp ) );
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
										indTab = 0;
									}
									else
									{
										subTab = (JTabbedPane)plugingTabPanel.getComponentAt( indTab );
									}
									
									String t = p.getID();
									subTab.addTab( t, new JScrollPane( settingPanel ) );
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
		
	public void refreshSelectedPlugins()
	{
		JTabbedPane plugingTabPanel = this.getTabbedPanel();
		
		for( Component c : plugingTabPanel.getComponents() )
		{
			if( c instanceof JScrollPane )
			{
				Component c2 = ( (JScrollPane) c ).getViewport().getComponent( 0 );
				if( c2 instanceof DataProcessingPluginSelectorPanel )
				{
					( (DataProcessingPluginSelectorPanel) c2 ).refreshSelectedProcessTable();
				}
				else if( c2 instanceof TrialPluginSelectorPanel )
				{
					( (TrialPluginSelectorPanel) c2 ).refreshSelectedTrial();
				}					
			}
		}
		
		try
		{
			PluginLoader loader = PluginLoader.getInstance();
		
			for( PluginType t : PluginType.values() )
			{
				if( t != PluginType.DATA_PROCESSING && t != PluginType.TRIAL )
				{
					String pluginCategory = Language.getLocalCaption( Language.OTHERS_TEXT );						
					switch( t )
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
						case ENCODER:
						{
							pluginCategory = Language.getLocalCaption( Language.ENCODER_TEXT );
							break;
						}
					}
					
					try
					{
						int indTab = plugingTabPanel.indexOfTab( pluginCategory );
						//plugingTabPanel.removeTabAt( indTab );
						
						List< ILSLRecPlugin > list = loader.getPluginsByType( t );
						if( list != null )
						{	
							JTabbedPane categoryTab = (JTabbedPane)plugingTabPanel.getComponentAt( indTab );
														
							for( ILSLRecPlugin val : list )
							{
								if( !( val instanceof ILSLRecConfigurablePlugin ) )
								{
									break;
								}
								
								ILSLRecConfigurablePlugin p = (ILSLRecConfigurablePlugin) val;
								
								int indSubTab = categoryTab.indexOfTab( p.getID() );
								
								if( indSubTab >= 0 )
								{
									categoryTab.removeTabAt( indSubTab );
								}
								else
								{
									indSubTab = 0;
								}
								
								JPanel settingPanel = p.getSettingPanel();
								
								if( settingPanel != null )
								{
									//JTabbedPane subTab = new JTabbedPane( JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT );
									categoryTab.insertTab( p.getID(), null,  new JScrollPane( settingPanel ), null, indSubTab );
									
									//subTab.addTab( p.getID(), new JScrollPane( settingPanel ) );
								}
							}
						}
					}			
					catch ( ClassCastException e)
					{
						e.printStackTrace();
					}
				}
			}
		}
		catch (Exception e) 
		{
		}
	}
}
