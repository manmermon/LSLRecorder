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
package lslrec.gui.panel.plugin.item;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import lslrec.config.ConfigApp;
import lslrec.config.language.Caption;
import lslrec.config.language.Language;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionDictionary;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.gui.GuiManager;
import lslrec.gui.GuiTextManager;
import lslrec.gui.miscellany.BasicPainter2D;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.gui.miscellany.VerticalFlowLayout;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.register.DataProcessingPluginRegistrar;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class DataProcessingPluginSelectorPanel extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4212736396179567663L;
			
	private JPanel contentPanel;
	private JPanel panelPluginSetting;
	private JPanel panelSaveDataProcessing;
	private JPanel panelWarningMsg;
	
	private JTable tableStreamList;
	private JTable tableProcessingPluginList;
	private JTable tableSelectedProcessingPluginList;

	private JTable tablePostProcessingPluginList;
	private JTable tableSelectedPostProcessingPluginList;
		
	private JCheckBox saveOutpuDataProcessing;
	
	private JLabel lbWarningMsg;
	private JLabel lbDelayMessage;
	
	public DataProcessingPluginSelectorPanel( Collection< ILSLRecPluginDataProcessing > plugins )
	{		
		super.setLayout( new BorderLayout() );
		
		this.add( this.getSaveOutputDataProcessingPanel(), BorderLayout.NORTH );
		this.add( this.getPluginPanel(), BorderLayout.WEST );		
		this.add( this.getPluginSettingPanel(), BorderLayout.CENTER );
		this.add( this.getWarningMessagePanel(), BorderLayout.SOUTH );
		
		this.loadPlugins( plugins );
		this.updateStream();
				
		if( !ExceptionDialog.wasCreatedExceptionDialog() )
		{
			JFrame w = (JFrame) SwingUtilities.windowForComponent( this );
			ExceptionDialog.createExceptionDialog( w );
		}
	}
		
	private void loadPlugins( Collection< ILSLRecPluginDataProcessing > plugins )
	{
		JTable t = this.getProcessingPluginListTable();		
		DefaultTableModel tmProcessing = (DefaultTableModel)t.getModel();
		
		t = this.getPostProcessingPluginListTable();
		DefaultTableModel tmPostProcessing = (DefaultTableModel)t.getModel();
						
		List< ILSLRecPluginDataProcessing > pluginList = new ArrayList<ILSLRecPluginDataProcessing>( plugins );
		Collections.sort( pluginList, new Comparator<ILSLRecPluginDataProcessing>() 
		{
			@Override
			public int compare(ILSLRecPluginDataProcessing o1, ILSLRecPluginDataProcessing o2) 
			{
				return o1.getID().compareTo( o2.getID() );
			}
		} );
		
		for( ILSLRecPluginDataProcessing pl : pluginList )
		{
			switch ( pl.getProcessingLocation() ) 
			{
				case DURING:
				{
					tmProcessing.addRow( new ILSLRecPluginDataProcessing[] { pl } );

					break;
				}
				case POST:
				{
					tmPostProcessing.addRow( new ILSLRecPluginDataProcessing[] { pl } );
					break;
				}
				case BOTH:
				{
					tmProcessing.addRow( new ILSLRecPluginDataProcessing[] { pl } );
					tmPostProcessing.addRow( new ILSLRecPluginDataProcessing[] { pl } );

					break;
				}
				default:
				{
					break;
				}
			}
		}
	}
	
	public void updateStream()
	{
		//
		// Load streams
		//
		
		List< IStreamSetting > streams = new ArrayList< IStreamSetting >( ( HashSet< IStreamSetting > )ConfigApp.getProperty( ConfigApp.ID_STREAMS ) );

		Collections.sort( streams, new Comparator<IStreamSetting>() 
									{	
										@Override
										public int compare(IStreamSetting o1, IStreamSetting o2) 
										{
											int eq = (o1.name()+o1.uid()).compareTo( o2.name()+o2.uid() );
											
											return eq;
										}
									}
						);
		
		JTable t = this.getStreamListTable();		
		DefaultTableModel tm = (DefaultTableModel)t.getModel();
		t.clearSelection();
		
		while( tm.getRowCount() > 0 )
		{
			tm.removeRow( 0 );
		}
		
		for( IStreamSetting str : streams)
		{
			tm.addRow( new IStreamSetting[] { str } );
		}
	}
	
	private JPanel getWarningMessagePanel()
	{
		if( this.panelWarningMsg == null )
		{
			this.panelWarningMsg  = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			this.panelWarningMsg.setBackground( Color.WHITE );
			this.panelWarningMsg.setBorder( BorderFactory.createEtchedBorder() );
			
			this.panelWarningMsg.add( this.getJlabelWarningMsg() );
		}
		
		return this.panelWarningMsg;
	}
	
	private JLabel getJlabelWarningMsg()
	{
		if( this.lbWarningMsg == null )
		{
			this.lbWarningMsg = new JLabel();
			
			this.lbWarningMsg.setText( Language.getLocalCaption( Language.MSG_WARNING_DATA_PROCESSING  ) );
			
			Font f = this.lbWarningMsg.getFont();
			this.lbWarningMsg.setFont( new Font( f.getName(), Font.BOLD, f.getSize() ) );
			
			this.lbWarningMsg.setForeground( Color.ORANGE.darker() );
			
			GuiTextManager.addComponent( GuiTextManager.TEXT
												, Language.MSG_WARNING_DATA_PROCESSING
												, this.lbWarningMsg );
		}
		
		return this.lbWarningMsg;
	}
	
	
	private JPanel getSaveOutputDataProcessingPanel()
	{
		if( this.panelSaveDataProcessing == null )
		{
			this.panelSaveDataProcessing = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			
			this.panelSaveDataProcessing .add( this.getJChkSaveOutputDataProcessing() );
			
			this.panelSaveDataProcessing.add( this.getJLabelProcessingDelay() );
		}
		
		return this.panelSaveDataProcessing;
	}
	
	private JCheckBox getJChkSaveOutputDataProcessing()
	{
		if( this.saveOutpuDataProcessing == null )
		{
			this.saveOutpuDataProcessing = new JCheckBox( Language.getLocalCaption( Language.SAVE_DATA_PROCESSING_TEXT ) );
			
			String ID = ConfigApp.OUTPUT_SAVE_DATA_PROCESSING;
			
			this.saveOutpuDataProcessing.addItemListener( new ItemListener()
			{	
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					JCheckBox c = (JCheckBox)e.getSource();
					
					ConfigApp.setProperty( ID, c.isSelected() );
				}
			});
			
			GuiManager.setGUIComponent( ID, ID, this.saveOutpuDataProcessing );
			
			GuiTextManager.addComponent( GuiTextManager.TEXT
											, Language.SAVE_DATA_PROCESSING_TEXT
											, this.saveOutpuDataProcessing );
		}
		
		return this.saveOutpuDataProcessing;
	}
	
	private JLabel getJLabelProcessingDelay()
	{
		if( this.lbDelayMessage == null )
		{
			this.lbDelayMessage = new JLabel();
			this.lbDelayMessage.setForeground( Color.ORANGE.darker() );
		}
		
		return this.lbDelayMessage;
	}
	
	private void setProcessingDelayMessage( boolean show )
	{
		JLabel lb = this.getJLabelProcessingDelay();
		
		String msg = "";
		
		if( show )
		{
			msg = "<html><b>Processed data may be delayed with respect to stream timestamps.</b></html>";
		}
		
		lb.setText( msg );
	}
	
	
	private JPanel getPluginSettingPanel()
	{
		if( this.panelPluginSetting == null )
		{
			this.panelPluginSetting = new JPanel( new BorderLayout() );
			this.panelPluginSetting.setBorder( BorderFactory.createEtchedBorder() );
		}
		
		return this.panelPluginSetting;
	}
	
	private JPanel getPluginPanel()
	{
		if( this.contentPanel == null )
		{
			this.contentPanel = new JPanel();
			
			this.contentPanel.setLayout( new BorderLayout() );
			
			JPanel aux = new JPanel(  new BorderLayout() );
			this.contentPanel.add( aux, BorderLayout.WEST );
			
			JPanel aux1 = new JPanel();
			aux1.setLayout( new GridLayout( 0, 1, 5, 5 ) );
						
			aux.add( aux1, BorderLayout.WEST );
			
			JPanel aux2 = new JPanel( new GridLayout( 0, 1, 5, 5 ) );
			aux2.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
						
			// Adding			
			JTable tab = this.getStreamListTable();
			
			aux1.add( new JScrollPane( tab, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ) );
			
			// Processing
			tab = this.getSelectedProcessingPluginTable();
			
			aux1.add( this.getSelectionControlPanel( this.getProcessingPluginListTable(), tab, DataProcessingPluginRegistrar.PROCESSING ) );
			
			
			//aux2.add( new JScrollPane( tab, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ), BorderLayout.CENTER );
			aux2.add( this.getSelectedPlugingCtrl( tab, DataProcessingPluginRegistrar.PROCESSING ) );
			
			aux.add( aux2, BorderLayout.EAST );

			this.setPluginTableSelectionEvents( tab );
			
			this.setPluginTableSelectionEvents( tab );
			
			this.getProcessingPluginListTable().setEnabled( false );
			
			// Post processing
			tab = this.getSelectedPostProcessingPluginTable();
			aux1.add( this.getSelectionControlPanel( this.getPostProcessingPluginListTable(), tab, DataProcessingPluginRegistrar.POSTPROCESSING ) );
			
			aux2.add( this.getSelectedPlugingCtrl( tab, DataProcessingPluginRegistrar.POSTPROCESSING ) );

			this.setPluginTableSelectionEvents( tab );
			
			this.setPluginTableSelectionEvents( tab );
			
			this.getPostProcessingPluginListTable().setEnabled( false );
			
		}
		
		return this.contentPanel;
	}
	
	private JTable getCreateJTable()
	{
		JTable table =  new JTable()
						{
							private static final long serialVersionUID = 1L;
			
							//Implement table cell tool tips.           
				            public String getToolTipText( MouseEvent e) 
				            {
				                String tip = null;
				                Point p = e.getPoint();
				                int rowIndex = rowAtPoint(p);
				                int colIndex = columnAtPoint(p);
				
				                try 
				                {
				                    tip = getValueAt(rowIndex, colIndex).toString();
				                }
				                catch ( RuntimeException e1 )
				                {
				                    //catch null pointer exception if mouse is over an empty line
				                }
				
				                return tip;
				            }				            
				        };
				        
		table.setDefaultRenderer( Object.class, new DefaultTableCellRenderer()
											{	
												/**
												 * 
												 */
												private static final long serialVersionUID = 4144425414849985291L;

												public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
											    {
													String v = "Plugion Processing";
													boolean boldOn = false;
													
													if( value != null )
													{
														if( value instanceof ILSLRecPluginDataProcessing )
														{
															v = ( (ILSLRecPluginDataProcessing) value ).getID();
														}
														else if( value instanceof IStreamSetting )
														{
															boldOn = !DataProcessingPluginRegistrar.getDataProcessing( (IStreamSetting)value , DataProcessingPluginRegistrar.PROCESSING ).isEmpty()
																	|| !DataProcessingPluginRegistrar.getDataProcessing( (IStreamSetting)value , DataProcessingPluginRegistrar.POSTPROCESSING ).isEmpty();
															
															v = ((IStreamSetting) value).name() + " (" + ((IStreamSetting) value).uid() + ")";
														}
													}
													
											        Component cellComponent = super.getTableCellRendererComponent( table, v, isSelected, hasFocus, row, column);
											        
											        if( !table.isCellEditable( row, column ) )
											        {	
											        	cellComponent.setBackground( new Color( 255, 255, 224 ) );
											        	
											        	if( isSelected )
											        	{
											        		cellComponent.setBackground( new Color( 0, 120, 215 ) );
											        	}
											        	else
											        	{
											        		cellComponent.setBackground( Color.WHITE );
											        	}
											        }
											        
											        cellComponent.setFont( this.getFont().deriveFont(Font.PLAIN ) );
											        if( boldOn )
											        {
											        	cellComponent.setFont( this.getFont().deriveFont(Font.BOLD) );
											        }
											        
											        cellComponent.setForeground( Color.BLACK );
											        
											        return cellComponent;
											    }
											});
		
		table.getTableHeader().setReorderingAllowed( false );
		
		return table;
	}
	
	private TableModel createTablemodel( String tableHeaderID )
	{	
		TableModel tm =  new DefaultTableModel( null, new String[] { Language.getLocalCaption( tableHeaderID ) } )
							{
								private static final long serialVersionUID = 1L;
								
								Class[] columnTypes = new Class[]{ ILSLRecPluginDataProcessing.class };								
								boolean[] columnEditables = new boolean[] { false };
																
								public Class getColumnClass(int columnIndex) 
								{
									return columnTypes[ columnIndex ];
								}
																								
								public boolean isCellEditable(int row, int column) 
								{
									boolean editable = columnEditables[ column ];
									
									return editable;
								}
							};
		return tm;
	}
	
	private JTable getStreamListTable()
	{
		if( this.tableStreamList == null )
		{	
			this.tableStreamList = this.getCreateJTable();
			this.tableStreamList.setModel( this.createTablemodel( Language.SETTING_LSL_DEVICES ) );
			
			GuiTextManager.addComponent( GuiTextManager.TEXT
											, Language.SETTING_PLUGIN
											, this.tableStreamList.getColumnModel().getColumn( 0 ) );
			
			this.tableStreamList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
			
			this.tableStreamList.setPreferredScrollableViewportSize( this.tableStreamList.getPreferredSize() );
			this.tableStreamList.setFillsViewportHeight( true );
			
			this.tableStreamList.getSelectionModel().addListSelectionListener( new ListSelectionListener()
			{	
				@Override
				public void valueChanged(ListSelectionEvent arg0)
				{	
					int sel = tableStreamList.getSelectedRow();
					
					getProcessingPluginListTable().setEnabled( sel >= 0 && sel < tableStreamList.getRowCount() );
					getPostProcessingPluginListTable().setEnabled( getProcessingPluginListTable().isEnabled() );
					
					getProcessingPluginListTable().clearSelection();
					getPostProcessingPluginListTable().clearSelection();
					
					if( sel >= 0 && sel < tableStreamList.getRowCount() )
					{
						IStreamSetting strinfo = (IStreamSetting)tableStreamList.getValueAt(sel,0 );
						showSelectedPlugin( strinfo );
					}
					else
					{
						clearShowSelectedPlugins();
					}
				}
			});
		}
		
		return this.tableStreamList;
	}	
	
	private void clearShowSelectedPlugins()
	{
		JTable selectedPlugins = this.getSelectedProcessingPluginTable();
		DefaultTableModel tm = (DefaultTableModel)selectedPlugins.getModel();

		selectedPlugins.setVisible( false );
		while( selectedPlugins.getRowCount() > 0 )
		{
			tm.removeRow( 0 );
		}
		selectedPlugins.setVisible( true );	
		
		selectedPlugins = this.getSelectedPostProcessingPluginTable();
		tm = (DefaultTableModel)selectedPlugins.getModel();

		selectedPlugins.setVisible( false );
		while( selectedPlugins.getRowCount() > 0 )
		{
			tm.removeRow( 0 );
		}
		selectedPlugins.setVisible( true );	
	}
	
	private void showSelectedPlugin( IStreamSetting strinfo )
	{
		if( strinfo != null )
		{
			this.clearShowSelectedPlugins();
			
			JTable selectedPlugins = this.getSelectedProcessingPluginTable();
			DefaultTableModel tm = (DefaultTableModel)selectedPlugins.getModel();
			
			List< ILSLRecPluginDataProcessing> processes = DataProcessingPluginRegistrar.getDataProcessing( strinfo, DataProcessingPluginRegistrar.PROCESSING );
		
			if( processes != null && !processes.isEmpty() )
			{
				for( ILSLRecPluginDataProcessing process : processes  )
				{
					this.addProcess2Table( tm, process );
				}
			}
			
			
			selectedPlugins = this.getSelectedPostProcessingPluginTable();
			tm = (DefaultTableModel)selectedPlugins.getModel();
			processes = DataProcessingPluginRegistrar.getDataProcessing( strinfo, DataProcessingPluginRegistrar.POSTPROCESSING );
			
			if( processes != null && !processes.isEmpty() )
			{
				for( ILSLRecPluginDataProcessing process : processes  )
				{
					this.addProcess2Table( tm, process );
				}
			}
		}
	}
	
	private JTable getProcessingPluginListTable()
	{
		if( this.tableProcessingPluginList == null )
		{	
			this.tableProcessingPluginList = this.getCreateJTable();
			this.tableProcessingPluginList.setModel( this.createTablemodel( Language.PROCESS_TEXT ) );
			
			GuiTextManager.addComponent( GuiTextManager.TEXT
											, Language.PROCESS_TEXT
											, this.tableProcessingPluginList.getColumnModel().getColumn( 0 ) );
			
			this.tableProcessingPluginList.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
			
			this.tableProcessingPluginList.setPreferredScrollableViewportSize( this.tableProcessingPluginList.getPreferredSize() );
			this.tableProcessingPluginList.setFillsViewportHeight( true );
		}
		
		return this.tableProcessingPluginList;
	}	
	
	private JTable getPostProcessingPluginListTable()
	{
		if( this.tablePostProcessingPluginList == null )
		{	
			this.tablePostProcessingPluginList = this.getCreateJTable();
			this.tablePostProcessingPluginList.setModel( this.createTablemodel( Language.POST_PROCESS_TEXT ) );
			
			GuiTextManager.addComponent( GuiTextManager.TEXT
											, Language.POST_PROCESS_TEXT
											, this.tablePostProcessingPluginList.getColumnModel().getColumn( 0 ) );
			
			this.tablePostProcessingPluginList.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
			
			this.tablePostProcessingPluginList.setPreferredScrollableViewportSize( this.tablePostProcessingPluginList.getPreferredSize() );
			this.tablePostProcessingPluginList.setFillsViewportHeight( true );
		}
		
		return this.tablePostProcessingPluginList;
	}	
	
	private JTable getSelectedProcessingPluginTable()
	{
		if( this.tableSelectedProcessingPluginList == null )
		{	
			this.tableSelectedProcessingPluginList = this.getCreateJTable();
			this.tableSelectedProcessingPluginList.setModel( this.createTablemodel( Language.SELECTED_PROCESSING_TEXT ) );
			
			GuiTextManager.addComponent( GuiTextManager.TEXT
											, Language.SELECTED_PROCESSING_TEXT
											,	this.tableSelectedProcessingPluginList.getColumnModel().getColumn( 0 ) );
			
			this.tableSelectedProcessingPluginList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
			
			this.tableSelectedProcessingPluginList.setPreferredScrollableViewportSize( this.tableSelectedProcessingPluginList.getPreferredSize() );
			this.tableSelectedProcessingPluginList.setFillsViewportHeight( true );			
		}
		
		return this.tableSelectedProcessingPluginList;
	}
	
	private JTable getSelectedPostProcessingPluginTable()
	{
		if( this.tableSelectedPostProcessingPluginList == null )
		{	
			this.tableSelectedPostProcessingPluginList = this.getCreateJTable();
			this.tableSelectedPostProcessingPluginList.setModel( this.createTablemodel( Language.SELECTED_POST_PROCESSING_TEXT ) );
			
			GuiTextManager.addComponent( GuiTextManager.TEXT
											, Language.SELECTED_POST_PROCESSING_TEXT
											,	this.tableSelectedPostProcessingPluginList.getColumnModel().getColumn( 0 ) );
			
			this.tableSelectedPostProcessingPluginList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
			
			this.tableSelectedPostProcessingPluginList.setPreferredScrollableViewportSize( this.tableSelectedPostProcessingPluginList.getPreferredSize() );
			this.tableSelectedPostProcessingPluginList.setFillsViewportHeight( true );			
		}
		
		return this.tableSelectedPostProcessingPluginList;
	}
	
	private void setPluginTableSelectionEvents( final JTable table )
	{
		if( table != null )
		{
			table.getSelectionModel().addListSelectionListener( new ListSelectionListener()
			{	
				@Override
				public void valueChanged(ListSelectionEvent arg0)
				{	
					if( !arg0.getValueIsAdjusting() )
					{
						int sel = table.getSelectedRow();
						
						// TODO
						if( sel < 0 )
						{
							getPluginSettingPanel().setVisible( false );
							
							getPluginSettingPanel().removeAll();
							
							getPluginSettingPanel().setVisible( true );
						}
						else
						{
							if( sel < table.getRowCount() )
							{
								ILSLRecPluginDataProcessing plugin = (ILSLRecPluginDataProcessing)table.getValueAt( sel, 0 );
								
								loadPluginSettingPanel( plugin );						
							}
						}
					}
				}
			});
		}
	}
			
	private JPanel getSelectionControlPanel( JTable sourceTable, JTable destTable, final int processLoc) 
	{
		JPanel selPanelCtr = new JPanel();
		if ( sourceTable != null && destTable != null ) 
		{
			selPanelCtr = new JPanel( new BorderLayout() );
			
			selPanelCtr.add( new JScrollPane( sourceTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ), BorderLayout.CENTER ); 	
			
			JPanel ctrPanel = new JPanel();
			selPanelCtr.add( ctrPanel, BorderLayout.EAST );
			
			//VerticalLayout ly = new VerticalLayout();
			FlowLayout ly = new FlowLayout( FlowLayout.RIGHT );
			ctrPanel.setLayout( ly );
			
			ctrPanel.setBorder( BorderFactory.createEmptyBorder( 0, 5, 5, 5));
			
			//ctrPanel.add( Box.createRigidArea( new Dimension( 0, 5 ) ));
			ctrPanel.add( this.getButtonSelect( sourceTable, destTable, processLoc ) );
			//this.panelMoveCtr.add( this.getButtonSelect( ) );
			//this.panelMoveCtr.add( Box.createRigidArea( new Dimension( 5, 5 ) ));
			
			//this.panelMoveCtr.add( Box.createRigidArea( new Dimension( 5, 5 ) ));
			
		}
		
		return selPanelCtr;
	}
	
	private JButton getButtonSelect( final JTable sourceTable, final JTable destTable, final int processLoc ) 
	{
		JButton buttonSelect = null;
		
		if ( sourceTable != null && destTable != null  ) 
		{
			buttonSelect = new JButton( );
			
			try
			{
				ImageIcon icon = new ImageIcon( BasicPainter2D.paintTriangle( 14,  2, Color.BLACK
																			, Color.LIGHT_GRAY
																			, BasicPainter2D.EAST ) );

				Dimension d = new Dimension( icon.getIconWidth(), icon.getIconHeight() );
				d.width += 6;
				d.height += 6;
				buttonSelect.setPreferredSize( d );
				
				buttonSelect.setIcon( icon );
			}
			catch( Exception ex )
			{
				buttonSelect.setText( ">>" );
			}
			
			buttonSelect.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			buttonSelect.addActionListener( new ActionListener()
			{				
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					copySelectedPlugin( sourceTable, destTable, processLoc );
				}
			});
		}
		
		return buttonSelect;
	}
	
	private JButton getButtonRemove( final JTable refTable, final int processLoc ) 
	{
		JButton buttonRemove = null;
		
		if ( refTable != null ) 
		{
			buttonRemove = new JButton(  );
			
			try
			{
				ImageIcon icon = new ImageIcon( BasicPainter2D.paintTriangle( 14,  2, Color.BLACK
																			, Color.LIGHT_GRAY
																			, BasicPainter2D.WEST ) );

				Dimension d = new Dimension( icon.getIconWidth(), icon.getIconHeight() );
				d.width += 6;
				d.height += 6;
				
				buttonRemove.setPreferredSize( d );
				
				buttonRemove.setIcon( icon );
			}
			catch( Exception ex )
			{
				buttonRemove.setText( "<<" );
			}
			
			buttonRemove.setToolTipText( Language.getLocalCaption( Language.DELETE_TEXT ) );
			
			buttonRemove.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			buttonRemove.addActionListener( new ActionListener()
			{				
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					removePlugins( refTable, processLoc );					
				}
			});
		}
		
		return buttonRemove;
	}
	
	private JButton getBtnClear( final JTable refTable, final int processLoc ) 
	{
		JButton btnClear = null;
		if ( refTable != null ) 
		{
			btnClear = new JButton( );
			
			try
			{	
				ImageIcon icon = GeneralAppIcon.Clear( 14, Color.BLACK );

				Dimension d = new Dimension( icon.getIconWidth(), icon.getIconHeight() );
				d.width += 6;
				d.height += 6;
				
				btnClear.setPreferredSize( d );
				
				btnClear.setIcon( icon );
			}
			catch( Exception ex )
			{	
				Caption cap = Language.getAllCaptions().get( Language.CLEAR );
			
				btnClear.setText( cap.getCaption( Language.getCurrentLanguage() ) );
			
				GuiTextManager.addComponent( GuiTextManager.TEXT
												, Language.CLEAR
												, btnClear );
			}
			
			btnClear.setToolTipText( Language.getLocalCaption( Language.CLEAR ) );
			
			btnClear.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			btnClear.addActionListener( new ActionListener()
			{				
				@Override
				public void actionPerformed(ActionEvent arg0)
				{	
					for( int i = refTable.getRowCount() - 1; i >= 0; i-- )
					{
						refTable.setRowSelectionInterval( i, i );
						removePlugins( refTable, processLoc );
					}					
				}
			});
		}
		return btnClear;
	}
	
	private JPanel getSelectedPlugingCtrl( final JTable refTable, final int processLoc )
	{
		JPanel selPanelCtr = new JPanel();
		
		if( refTable != null )
		{
			selPanelCtr = new JPanel( new BorderLayout() );
			
			selPanelCtr.add( new JScrollPane( refTable, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED ), BorderLayout.CENTER ); 	
			
			
			JPanel selectedPluginPanelControl = new JPanel();
			selPanelCtr.add( selectedPluginPanelControl, BorderLayout.EAST );
			
			//FlowLayout ly = new FlowLayout( FlowLayout.LEFT );
			VerticalFlowLayout ly = new VerticalFlowLayout( VerticalFlowLayout.TOP );
			selectedPluginPanelControl.setLayout( ly );
			
			//selectedPluginPanelControl.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5));
			
		
			selectedPluginPanelControl.add( this.getBtnUp( refTable, processLoc ) );
			//panelUpDownControl.add( Box.createRigidArea( new Dimension( 5, 5 ) ));
			selectedPluginPanelControl.add( this.getBtnDown( refTable, processLoc ) );
			selectedPluginPanelControl.add( Box.createRigidArea( new Dimension( 5, 5 ) ));
			selectedPluginPanelControl.add( this.getButtonRemove( refTable, processLoc )) ;
			selectedPluginPanelControl.add( Box.createRigidArea( new Dimension( 5, 5 ) ));
			selectedPluginPanelControl.add( this.getBtnClear( refTable, processLoc ) );
			
		}
		
		return selPanelCtr;
	}
	
	private JButton getBtnUp( final JTable refTable, final int processLoc )
	{
		JButton buttonUp = null;
		
		if( refTable != null )
		{
			buttonUp = new JButton();
			try
			{
				ImageIcon icon = new ImageIcon( BasicPainter2D.paintTriangle(  14,  2, Color.BLACK
																				, Color.LIGHT_GRAY, BasicPainter2D.NORTH ) );
				
				Dimension d = new Dimension( icon.getIconWidth(), icon.getIconHeight() );
				d.width += 6;
				d.height += 6;
				buttonUp.setPreferredSize( d );
				buttonUp.setIcon( icon );
			}
			catch( Exception ex )
			{
				Caption cap = Language.getAllCaptions().get(  Language.UP_TEXT );
				buttonUp.setText( cap.getCaption( Language.getCurrentLanguage() ) );
				
				GuiTextManager.addComponent( GuiTextManager.TEXT, Language.UP_TEXT, buttonUp );
			}
			
			buttonUp.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			buttonUp.addActionListener( new ActionListener()
			{				
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					reorderPlugin( refTable, processLoc, -1 );
				}
			});
		}
		
		return buttonUp;
	}
	
	private JButton getBtnDown( final JTable refTable, final int processLoc )
	{
		JButton buttonDown = null;
		
		if( refTable != null  )
		{
			buttonDown = new JButton();
			
			try
			{
				ImageIcon icon = new ImageIcon( BasicPainter2D.paintTriangle( 14,  2, Color.BLACK
																			, Color.LIGHT_GRAY
																			, BasicPainter2D.SOUTH ) );
				
				Dimension d = new Dimension( icon.getIconWidth(), icon.getIconHeight() );
				d.width += 6;
				d.height += 6;
				buttonDown.setPreferredSize( d );
				buttonDown.setIcon( icon );
			}
			catch( Exception ex )
			{
				Caption cap = Language.getAllCaptions().get( Language.DOWN_TEXT );
				buttonDown.setText( cap.getCaption( Language.getCurrentLanguage() ) );
				
				GuiTextManager.addComponent( GuiTextManager.TEXT, Language.DOWN_TEXT, buttonDown );
			}
			
			buttonDown.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			buttonDown.addActionListener( new ActionListener()
			{				
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					reorderPlugin( refTable, processLoc, 1 );
				}
			});
		}
		
		return buttonDown;
	}
	
	private IStreamSetting getSelectedStream()
	{
		JTable strTable = this.getStreamListTable();
		
		int selStr = strTable.getSelectedRow();
		
		IStreamSetting str = null;
		if( selStr >= 0 )
		{
			str = (IStreamSetting) strTable.getValueAt( selStr, 0 );
		}
		
		return str;
	}	
		
	private void copySelectedPlugin( JTable source, JTable dest, int processLoc )
	{
		IStreamSetting str = this.getSelectedStream();
		
		getStreamListTable().setVisible( false );
		DefaultTableModel tmDest = (DefaultTableModel)dest.getModel();
		
		int[] selIndex = source.getSelectedRows();
		Arrays.sort( selIndex );
		
		if( str != null && selIndex.length > 0 )
		{			
			for( int i = selIndex.length - 1; i >= 0; i-- )
			{
				int index = selIndex[ i ];
				
				ILSLRecPluginDataProcessing process = (ILSLRecPluginDataProcessing)source.getValueAt( index, 0 );
											
				try 
				{
					ILSLRecPluginDataProcessing pr = process.getClass().newInstance();
					
					//DataProcessingPluginRegistrar.addDataProcessing( pr );
					DataProcessingPluginRegistrar.addDataStreamProcessing( pr, str, processLoc );
					
					this.addProcess2Table( tmDest, pr );
					
					setProcessingDelayMessage( true );
				} 
				catch (Exception e) 
				{
					ExceptionMessage msg = new ExceptionMessage( e, Language.getLocalCaption( Language.PROBLEM_TEXT ), ExceptionDictionary.ERROR_MESSAGE );
					
					ExceptionDialog.createExceptionDialog( SwingUtilities.getWindowAncestor( source ) );
					ExceptionDialog.showMessageDialog( msg, true, true );
				}								
			}
		}
		
		getStreamListTable().setVisible( true );
	}
	
	private void addProcess2Table( DefaultTableModel tmDest, ILSLRecPluginDataProcessing pr )
	{
		if( tmDest != null && pr != null )
		{
			tmDest.addRow( new ILSLRecPluginDataProcessing[] { pr } );
		}
	}
	
	private void removePlugins( JTable refTable, int processLoc )
	{
		IStreamSetting str = this.getSelectedStream();
		
		getStreamListTable().setVisible( false );
		DefaultTableModel tm = (DefaultTableModel)refTable.getModel();
		
		int[] index = refTable.getSelectedRows();
		
		Arrays.sort( index );
		
		for( int i = index.length - 1; i >= 0; i-- )
		{			
			int r = index[ i ];
					
			try
			{
				ILSLRecPluginDataProcessing pl = (ILSLRecPluginDataProcessing)tm.getValueAt( r, 0 );
				
				tm.removeRow( r );
				
				DataProcessingPluginRegistrar.removeDataProcessing( str, pl, processLoc );
			}
			catch( Exception ex )
			{
				ExceptionMessage msg = new ExceptionMessage( ex, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionDictionary.ERROR_MESSAGE );
				ExceptionDialog.showMessageDialog( msg, true, true );
			}					
		}
		
		getStreamListTable().setVisible( true );
		setProcessingDelayMessage( !DataProcessingPluginRegistrar.getAllDataStreams().isEmpty() );
	}
		
	private void reorderPlugin( JTable source, int processLoc, int shift )
	{
		IStreamSetting str = this.getSelectedStream();
		
		DefaultTableModel tmSource = (DefaultTableModel)source.getModel();
				
		int dir = 1;
		if( shift < 0 )
		{
			dir = -1;
		}
		
		int[] selIndex = source.getSelectedRows();
		Arrays.sort( selIndex );
		
		int ref = 0;
		int from = 0; 
		int to = selIndex.length;
		
		if( dir < 0 )
		{
			for( int i = 0; i < selIndex.length; i++ )
			{
				if( ref == selIndex[ i ] )
				{
					from++;
					ref++;
				}
				else
				{
					break;
				}
			}
		}
		else
		{
			ref = source.getRowCount() -1;
			for( int i = selIndex.length - 1; i >= 0; i-- )
			{
				if( ref == selIndex[ i ] )
				{
					to--;
					ref--;
				}
				else
				{
					break;
				}
			}
		}		
		
		if( from < to )
		{		
			selIndex = Arrays.copyOfRange( selIndex, from, to );
			
			if( selIndex.length > 0 )
			{
				for( int i = selIndex.length - 1; i >= 0; i-- )
				{
					int index = selIndex[ i ];
					int row = index + dir;
					
					tmSource.moveRow( index, index, row );
					
					DataProcessingPluginRegistrar.moveDataProcessing( str, processLoc,  index, row );
					
					
					if( i == 0 )
					{
						source.setRowSelectionInterval( row, row );
					}
				}				
			}
		}
	}

	private void loadPluginSettingPanel( ILSLRecPluginDataProcessing process )
	{
		try 
		{			
			JPanel p = this.getPluginSettingPanel();
			p.setVisible( false );
			p.removeAll();
			
			if( process != null )
			{
				JPanel p2 = process.getSettingPanel();
				p.setPreferredSize( p.getPreferredSize() );
				p.add( p2, BorderLayout.CENTER );
			}
			
			p.setVisible( true );			
		}
		catch (Exception | Error e) 
		{
			e.printStackTrace();
			Exception e1 = new Exception( "Setting panel for plugin is not available.", e );
			ExceptionMessage msg = new ExceptionMessage(  e1
														, Language.getLocalCaption( Language.DIALOG_ERROR )
														, ExceptionDictionary.ERROR_MESSAGE );
		
			ExceptionDialog.showMessageDialog( msg, true, true );
		}	
	}
}
