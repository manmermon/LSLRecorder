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
import java.awt.Point;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import lslrec.config.ConfigApp;
import lslrec.config.language.Language;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting.StreamLibrary;
import lslrec.dataStream.family.setting.SimpleStreamSetting;
import lslrec.dataStream.family.setting.StreamSettingExtraLabels;
import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;
import lslrec.dataStream.family.stream.lslrec.LSLRecStream;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionDictionary;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.gui.GuiLanguageManager;
import lslrec.gui.GuiManager;
import lslrec.plugin.lslrecPlugin.trial.ILSLRecPluginTrial;
import lslrec.plugin.register.TrialPluginRegistrar;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import java.awt.FlowLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JSpinner;

public class TrialPluginSelectorPanel extends JPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4212736396179567663L;
			
	private JPanel contentPanel;
	private JPanel panelPluginSetting;
	private JPanel panelWinSizeSetting;
		
	private JTable tablePluginList;
	private JCheckBox chckbxFullScreen;
	private JLabel lblWinWidth;
	private JSpinner spinnerWidth;
	private JSpinner spinnerHeight;
	private JLabel lblWinHeight;
	
	
	//private PluginType plgType = PluginType.TRIAL;
	
	public TrialPluginSelectorPanel( Collection< ILSLRecPluginTrial > plugins )
	{
		setLayout( new BorderLayout() );
		
		this.add( this.getWinSizeSettingPanel(), BorderLayout.NORTH );
		this.add( this.getPluginPanel(), BorderLayout.WEST );		
		this.add( this.getPluginSettingPanel(), BorderLayout.CENTER );
		
		JTable t = this.getTrialListTable();
		
		DefaultTableModel tm = (DefaultTableModel)t.getModel();
		
		for( ILSLRecPluginTrial pl : plugins )
		{
			tm.addRow( new ILSLRecPluginTrial[] { pl } );
		}
		
		JFrame w = (JFrame) SwingUtilities.windowForComponent( this );
		ExceptionDialog.createExceptionDialog( w );
	}
		
	public void setPluginIDs( List< String > ids )
	{
		if( ids != null )
		{		
			JTable processList = this.getTrialListTable();
			
			processList.addRowSelectionInterval( 0, processList.getRowCount() );
			processList.clearSelection();
			
			DefaultTableModel tm = (DefaultTableModel)processList.getModel();
			for( String id : ids )
			{
				tm.addRow( new String[] { id } );
			}
		}
	}
	
	private JPanel getWinSizeSettingPanel()
	{
		if( this.panelWinSizeSetting == null )
		{
			this.panelWinSizeSetting = new JPanel( );
			this.panelWinSizeSetting.setBorder( BorderFactory.createTitledBorder( Language.getLocalCaption( Language.WINDOW_TEXT ) ) );
			
			this.panelWinSizeSetting.setLayout(new FlowLayout( FlowLayout.LEFT, 5, 0 ) );
			this.panelWinSizeSetting.add( this.getChckbxFullScreen());
			this.panelWinSizeSetting.add( this.getLblWinSize());
			this.panelWinSizeSetting.add( this.getSpinnerWidth());
			panelWinSizeSetting.add(getLblWinHeight());
			this.panelWinSizeSetting.add( this.getSpinnerHeight());
			
			GuiLanguageManager.addComponent( GuiLanguageManager.BORDER
											, Language.WINDOW_TEXT
											, this.panelWinSizeSetting.getBorder() );
		}
		
		return this.panelWinSizeSetting;
	}
	
	private JPanel getPluginSettingPanel()
	{
		if( this.panelPluginSetting == null )
		{
			this.panelPluginSetting = new JPanel( new BorderLayout());	
		}
		
		return this.panelPluginSetting;
	}
	
	private JPanel getPluginPanel()
	{
		if( this.contentPanel == null )
		{
			this.contentPanel = new JPanel();
			
			this.contentPanel.setLayout( new BorderLayout() );
			
			JPanel aux = new JPanel();
			aux.setLayout( new BoxLayout( aux, BoxLayout.X_AXIS ) );
			
			this.contentPanel.add( aux, BorderLayout.WEST );
			
			// Adding			
			JTable tab = this.getTrialListTable();
			aux.add( new JScrollPane( tab, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER ) );
			
			this.setPluginTableSelectionEvents( tab );
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
				            
				            public void changeSelection(int rowIndex, int columnIndex, boolean toggle, boolean extend)
				            {
				                //Always toggle on single selection
				                super.changeSelection( rowIndex, columnIndex, !extend, extend );
				            }
				        };
				        
		table.setDefaultRenderer( Object.class, new DefaultTableCellRenderer()
											{	
												public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
											    {
													String v = ( (ILSLRecPluginTrial) value ).getID();
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
								
								Class[] columnTypes = new Class[]{ ILSLRecPluginTrial.class };								
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

	private JTable getTrialListTable()
	{
		if( this.tablePluginList == null )
		{	
			this.tablePluginList = this.getCreateJTable();
			this.tablePluginList.setModel( this.createTablemodel( Language.SETTING_PLUGIN ) );
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.SETTING_PLUGIN, this.tablePluginList.getColumnModel().getColumn( 0 ) );
			
			this.tablePluginList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
						
			this.tablePluginList.setPreferredScrollableViewportSize( this.tablePluginList.getPreferredSize() );
			this.tablePluginList.setFillsViewportHeight( true );
		}
		
		return this.tablePluginList;
	}	
	
	private void setPluginTableSelectionEvents( final JTable table )
	{
		if( table != null )
		{
			table.getSelectionModel().addListSelectionListener( new ListSelectionListener()
			{	
				private IStreamSetting lsrecStream = null;
				
				@Override
				public void valueChanged(ListSelectionEvent arg0)
				{	
					if( !arg0.getValueIsAdjusting() )
					{
						int sel = table.getSelectedRow();
						
						if( sel < 0 )
						{
							getPluginSettingPanel().setVisible( false );
							
							getPluginSettingPanel().removeAll();
							
							getPluginSettingPanel().setVisible( true );
							
							TrialPluginRegistrar.removeTrialPlugin();
							
							LSLRecStream.removeDataStream( lsrecStream );
							lsrecStream = null;
						}
						else
						{
							if( sel < table.getRowCount() )
							{
								ILSLRecPluginTrial trial = (ILSLRecPluginTrial)table.getValueAt( sel, 0 );

								if( trial.hasTrialLog() )
								{
									if( lsrecStream != null )
									{
										LSLRecStream.removeDataStream( lsrecStream );
										lsrecStream = null;
									}
									
									lsrecStream = new SimpleStreamSetting( StreamLibrary.LSLREC
																			, trial.getID()
																			, StreamDataType.string
																			, 1, 1, 0D
																			, trial.getID() 
																			, trial.getID() );
									
									
									lsrecStream.getExtraInfo().put( StreamSettingExtraLabels.ID_TRIAL_INFO_LABEL, trial.getLogDescription() );
									
									LSLRecStream.addDataStream( lsrecStream );
								}
								
								TrialPluginRegistrar.setTrialPlugin( trial );
								
								loadPluginSettingPanel( trial );
							}
						}
					}
				}
			});
						
			/*
			table.getModel().addTableModelListener( new TableModelListener() 
			{	
				@Override
				public void tableChanged( TableModelEvent arg0 )
				{
					if( selectionMode == SINGLE_SELECTION && arg0.getType() == TableModelEvent.INSERT )
					{
						DefaultTableModel tm = (DefaultTableModel)arg0.getSource();
						
						if( tm.getRowCount() > 2 )
						{
							for( int i = tm.getRowCount() - 2; i >= 0; i-- )
							{
								tm.removeRow( i );
							}
						}
						
						tableSelectedProcessList.setRowSelectionInterval( 0, 0 );
					}
				}
			});
			*/
		}
	}
		
	private void loadPluginSettingPanel( ILSLRecPluginTrial plg )
	{		
		try 
		{
			JPanel p = this.getPluginSettingPanel();
			p.setVisible( false );
			p.removeAll();
			
			if( plg != null )
			{
				JPanel p2 = plg.getSettingPanel();
				
				p.add( p2, BorderLayout.CENTER );
			}
			
			p.setVisible( true );			
		}
		catch (Exception e) 
		{
			Exception e1 = new Exception( "Setting panel for plugin is not available.", e );
			ExceptionMessage msg = new ExceptionMessage(  e1
														, Language.getLocalCaption( Language.DIALOG_ERROR )
														, ExceptionDictionary.ERROR_MESSAGE );
		
			ExceptionDialog.showMessageDialog( msg, true, true );
		}	
	}
	
	public void refreshSelectedTrial()
	{
		if( TrialPluginRegistrar.isSelectedTrialPlugin() )
		{
			JTable t = this.getTrialListTable();
			
			for( int r = 0; r < t.getRowCount(); r++ )
			{
				ILSLRecPluginTrial trial = (ILSLRecPluginTrial)t.getValueAt( r, 0 );
		
				if( TrialPluginRegistrar.isSelected( trial.getID() ) )
				{
					t.setRowSelectionInterval( r, r );
					
					break;
				}
			}
		}
	}
	
	private JCheckBox getChckbxFullScreen() 
	{
		if ( this.chckbxFullScreen == null) 
		{
			String ID = ConfigApp.TRIAL_FULLSCREEN;
			
			this.chckbxFullScreen = new JCheckBox( Language.getLocalCaption( Language.FULLSCREEN ) );
			this.chckbxFullScreen.setHorizontalTextPosition( JCheckBox.LEFT );
			
			this.chckbxFullScreen.addItemListener( new ItemListener()
			{	
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					JCheckBox c = (JCheckBox)e.getSource();
					
					ConfigApp.setProperty( ID, c.isSelected() );
				}
			});
			
			GuiManager.setGUIComponent( ID, ID, this.chckbxFullScreen );
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT
											, Language.FULLSCREEN
											, this.chckbxFullScreen );
		}
		
		return this.chckbxFullScreen;
	}
	
	private JLabel getLblWinSize() 
	{
		if ( this.lblWinWidth == null) 
		{
			this.lblWinWidth = new JLabel( Language.getLocalCaption( Language.WIDTH_TEXT ) );
		
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT
											, Language.WIDTH_TEXT
											, this.lblWinWidth );
		}
		
		return this.lblWinWidth;
	}
	
	private JSpinner getSpinnerWidth() 
	{
		if ( this.spinnerWidth == null) 
		{
			String ID = ConfigApp.TRIAL_WINDOW_WIDTH;
			
			this.spinnerWidth = new JSpinner( new SpinnerNumberModel( 500, 100, 8000, 10 ) );			
			
			this.spinnerWidth.addChangeListener( new ChangeListener()
			{								
				@Override
				public void stateChanged(ChangeEvent e)
				{
					JSpinner sp = (JSpinner)e.getSource();

					try
					{
						Integer val = (Integer)sp.getValue();
												
						ConfigApp.setProperty( ID, val );
						
					} 
					catch ( Exception e1)
					{
						ExceptionMessage m = new ExceptionMessage( e1, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionDictionary.ERROR_MESSAGE );
						
						ExceptionDialog.showMessageDialog( m, true, true );
					}
				}
			});
			
			this.spinnerWidth.addMouseWheelListener( new MouseWheelListener() 
			{				
				@Override
				public void mouseWheelMoved(MouseWheelEvent e) 
				{
					if( e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL )
					{
						try
						{	
							JSpinner sp = (JSpinner)e.getSource();
							
							int d = e.getWheelRotation();
							
							if( d > 0 )
							{
								sp.setValue( sp.getModel().getPreviousValue() );
							}
							else
							{
								sp.setValue( sp.getModel().getNextValue() );
							}	
						}
						catch( IllegalArgumentException ex )
						{												
						}
					}
				}
			});
			
			GuiManager.setGUIComponent( ID, ID, this.spinnerWidth );
		}
		
		
		return this.spinnerWidth;
	}
	
	private JSpinner getSpinnerHeight() 
	{
		if ( this.spinnerHeight == null) 
		{
			String ID = ConfigApp.TRIAL_WINDOW_HEIGHT;
			
			this.spinnerHeight = new JSpinner( new SpinnerNumberModel( 500, 100, 8000, 10 )  );
			
			this.spinnerHeight.addChangeListener( new ChangeListener()
			{								
				@Override
				public void stateChanged(ChangeEvent e)
				{
					JSpinner sp = (JSpinner)e.getSource();

					try
					{
						Integer val = (Integer)sp.getValue();
												
						ConfigApp.setProperty( ID, val );
						
					} 
					catch ( Exception e1)
					{
						ExceptionMessage m = new ExceptionMessage( e1, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionDictionary.ERROR_MESSAGE );
						
						ExceptionDialog.showMessageDialog( m, true, true );
					}
				}
			});
			
			this.spinnerHeight.addMouseWheelListener( new MouseWheelListener() 
			{				
				@Override
				public void mouseWheelMoved(MouseWheelEvent e) 
				{
					if( e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL )
					{
						try
						{	
							JSpinner sp = (JSpinner)e.getSource();
							
							int d = e.getWheelRotation();
							
							if( d > 0 )
							{
								sp.setValue( sp.getModel().getPreviousValue() );
							}
							else
							{
								sp.setValue( sp.getModel().getNextValue() );
							}	
						}
						catch( IllegalArgumentException ex )
						{												
						}
					}
				}
			});
			
			GuiManager.setGUIComponent( ID, ID, this.spinnerHeight );
		}
		
		return this.spinnerHeight;
	}
	
	private JLabel getLblWinHeight() 
	{
		if ( this.lblWinHeight == null) 
		{
			this.lblWinHeight = new JLabel( Language.getLocalCaption( Language.HEIGHT_TEXT ) );
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT
											, Language.HEIGHT_TEXT
											, this.lblWinHeight );
		}
		
		return lblWinHeight;
	}
}
