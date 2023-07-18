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
public abstract class LSLRecConfigurablePluginAbstract implements ILSLRecConfigurablePlugin 
{	
	protected Map< String, Parameter< String > > pars;
	
	private JPanel SettingPanel = null;
	
	/**
	 * 
	 */
	public LSLRecConfigurablePluginAbstract( )
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
			
			this.postLoadSettings( );
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
		this.SettingPanel.setVisible( false );
		
		this.setSettingPanel( this.SettingPanel );
		
		this.SettingPanel.setVisible( true );
		
		return this.SettingPanel;
	}
	
	protected abstract void setSettingPanel( JPanel panel );
	
	protected abstract void postLoadSettings( );
}
