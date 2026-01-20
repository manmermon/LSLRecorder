/* 
 * Copyright 2018-2026 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

import lslrec.config.language.Language;

public class Dialog_SelectChannels extends JDialog 
{
	private static final long serialVersionUID = -8251555211695444472L;
	
	private JPanel panelChannelsChb;
	private JPanel allNonePanel;
	
	private JComboBox< String > variable;
	
	private Object sync = new Object();
	
	private Map< String, boolean[] > selectedChannels;
	
	private String currentVar = null;
	
	public Dialog_SelectChannels( Map< String, boolean[] > varChannels ) 
	{			
		super.setBounds( 100, 100, 300, 100 );
		
		super.getContentPane().setLayout( new BorderLayout() );
		
		super.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
		
		Container container = super.getContentPane();
		
		this.selectedChannels = varChannels;
		
		JPanel varPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
		
		varPanel.add( this.getCbVariable() );
		varPanel.add( new JLabel( Language.getLocalCaption( Language.LSL_CHANNELS ) ) );
		varPanel.add( this.getAllNoneChannelPanel() );
		container.add( varPanel, BorderLayout.NORTH );
		
		
		JScrollPane sp = new JScrollPane( this.getChannelsPanel()
											, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
											, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		
				
		container.add( sp, BorderLayout.CENTER);
		
		this.variable.setSelectedIndex( -1 );
		if( varChannels.size() > 0 )
		{
			this.variable.setSelectedIndex( 0 );
		}
		
		this.setChannelCheckboxes();
	}
	
	private JComboBox< String > getCbVariable()
	{
		if( this.variable == null )
		{
			this.variable = new JComboBox<String>();
			
			for( String v : this.selectedChannels.keySet() )
			{
				this.variable.addItem( v );
			}
			
			this.variable.addItemListener( new ItemListener() 
			{				
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					JComboBox< String > cb = (JComboBox<String>)e.getSource();
					
					int selIndex = cb.getSelectedIndex();
					currentVar = null;
					if( selIndex >= 0 )
					{
						currentVar = cb.getItemAt( selIndex );						
					}
					
					setChannelCheckboxes();
				}
			});
		}
		
		return this.variable;
	}
	
	private JPanel getAllNoneChannelPanel()
	{
		if( this.allNonePanel == null )
		{
			this.allNonePanel = new JPanel( new FlowLayout() );
		
			JRadioButton allBt = new JRadioButton( Language.getLocalCaption( Language.ALL_TEXT ) );
			allBt.setSelected( true );
			allBt.addActionListener( new ActionListener() 
			{			
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JRadioButton b = (JRadioButton)e.getSource();				
					b.setSelected( true );

					synchronized ( sync )
					{
						if( currentVar != null )
						{					
							boolean[] chs = selectedChannels.get( currentVar );

							for( int ic = 0; ic < chs.length; ic++ )
							{
								chs[ ic ] = true;
							}
						}
					}

					setChannelCheckboxes();
				}
			});

			JRadioButton noneBt = new JRadioButton( Language.getLocalCaption( Language.NONE_TEXT) );
			noneBt.setSelected( true );
			noneBt.addActionListener( new ActionListener() 
			{			
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JRadioButton b = (JRadioButton)e.getSource();				
					b.setSelected( true );

					synchronized ( sync )
					{
						if( currentVar != null )
						{					
							boolean[] chs = selectedChannels.get( currentVar );

							for( int ic = 0; ic < chs.length; ic++ )
							{
								chs[ ic ] = false;
							}
						}
					}

					setChannelCheckboxes();
				}
			});

			this.allNonePanel .add( allBt );
			this.allNonePanel .add( noneBt );
		}
		
		return this.allNonePanel;
	}
	
	private JPanel getChannelsPanel()
	{
		if( this.panelChannelsChb == null )
		{
			this.panelChannelsChb = new JPanel( new GridLayout( 2, 0 ) );	
			
			this.panelChannelsChb.setBorder( null );	
		}
		
		return this.panelChannelsChb;
	}
	
	private void setChannelCheckboxes(  )
	{
		JPanel channelPanel = this.getChannelsPanel();
		channelPanel.setVisible( false );
		channelPanel.removeAll();		

		synchronized( this.sync )
		{
			if( this.currentVar != null )
			{				
				boolean[] channels = this.selectedChannels.get( this.currentVar );
				
				int len = channels.length;
				for( int i = 0; i < len; i++ )
				{
					JCheckBox ch = new JCheckBox( "" + (i+1) );
					ch.setSelected( channels[ i ] );
					
					ch.addActionListener( new ActionListener() 
					{	
						@Override
						public void actionPerformed(ActionEvent e) 
						{
							JCheckBox ch = (JCheckBox)e.getSource();
							int ich = Integer.parseInt( ch.getText() ) - 1;
							
							if( ich < channels.length )
							{
								channels[ ich ] = ch.isSelected();
							}
						}
					});
					
					channelPanel.add( ch );					
				}
			}
		}
		
		channelPanel.setVisible( true );
	}
}
