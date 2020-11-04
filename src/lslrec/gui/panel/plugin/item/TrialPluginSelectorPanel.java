package lslrec.gui.panel.plugin.item;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.Collection;
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

import lslrec.config.language.Language;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionDictionary;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.gui.GuiLanguageManager;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin.PluginType;
import lslrec.plugin.lslrecPlugin.trial.ILSLRecPluginTrial;
import lslrec.plugin.register.TrialPluginRegistrar;

import javax.swing.BoxLayout;
import javax.swing.JFrame;

public class TrialPluginSelectorPanel extends JPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4212736396179567663L;
			
	private JPanel contentPanel;
	private JPanel panelPluginSetting;
	
	private JTable tablePluginList;
	
	private PluginType plgType = PluginType.TRIAL;
	
	public TrialPluginSelectorPanel( Collection< ILSLRecPluginTrial > plugins )
	{
		super.setLayout( new BorderLayout() );
		
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
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.SETTING_PLUGIN, this.tablePluginList.getTableHeader() );
			
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
						}
						else
						{
							if( sel < table.getRowCount() )
							{
								ILSLRecPluginTrial trial = (ILSLRecPluginTrial)table.getValueAt( sel, 0 );

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
					t.setRowSelectionInterval( r, 0 );
					
					break;
				}
			}
		}
	}
}
