package lslrec.gui.panel.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import lslrec.config.language.Caption;
import lslrec.config.language.Language;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionDictionary;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.gui.GuiLanguageManager;
import lslrec.gui.GuiManager;
import lslrec.gui.dialog.Dialog_Info;
import lslrec.gui.miscellany.BasicPainter2D;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.plugin.loader.PluginLoader;
import lslrec.plugin.lslrecPlugin.ILSLRecConfigurablePlugin;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin.PluginType;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class PluginSelectorPanel extends JPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4212736396179567663L;
	
	public static final int SINGLE_SELECTION = 0;
	public static final int MULTIPLE_SELECTION = 1;
		
	private JPanel contentPanel;
	private JPanel panelSelectControl;
	private JPanel panelProcessList;
	private JPanel panelSelectedProcesses;
	private JPanel panelUpDownControl;
	private JPanel panelMoveCtr;
	private JPanel panelProecssSetting;
	
	private JTable tableProcessList;
	private JTable tableSelectedProcessList;

	private JButton buttonSelect;
	private JButton buttonRemove;
	private JButton btnClear;
	private JButton buttonUp;
	private JButton buttonDown;
	
	private int selectionMode = MULTIPLE_SELECTION;
	private PluginType plgType = PluginType.DATA_PROCESSING;
		
	public PluginSelectorPanel( PluginType type, Collection< String > idPlugins )
	{	
		this( type, idPlugins, MULTIPLE_SELECTION );
	}
		
	public PluginSelectorPanel( PluginType type, Collection< String > idPlugins, int selectMode )
	{
		this.setSelectionMode( selectMode );
		
		this.plgType = type;
		
		super.setLayout( new BorderLayout() );
		
		this.add( this.getProcessPanel(), BorderLayout.WEST );		
		this.add( this.getPluginSettingPanel(), BorderLayout.CENTER );
		
		JTable t = this.getProcessListTable();
		
		DefaultTableModel tm = (DefaultTableModel)t.getModel();
		
		for( String id : idPlugins )
		{
			tm.addRow( new String[] { id } );
		}
	}
	
	public void setSelectionMode( int mode )
	{
		this.selectionMode = mode;
	}
	
	public void setPluginIDs( List< String > ids )
	{
		if( ids != null )
		{		
			JTable processList = this.getProcessListTable();
			JTable selectedList = this.getSelectedProcessTable();
			
			processList.addRowSelectionInterval( 0, processList.getRowCount() );
			processList.clearSelection();
			
			selectedList.addRowSelectionInterval( 0, selectedList.getRowCount() );
			selectedList.clearSelection();
			
			DefaultTableModel tm = (DefaultTableModel)processList.getModel();
			for( String id : ids )
			{
				tm.addRow( new String[] { id } );
			}
		}
	}
	
	private JPanel getPluginSettingPanel()
	{
		if( this.panelProecssSetting == null )
		{
			this.panelProecssSetting = new JPanel( new BorderLayout());	
		}
		
		return this.panelProecssSetting;
	}
	
	private JPanel getProcessPanel()
	{
		if( this.contentPanel == null )
		{
			this.contentPanel = new JPanel();
			this.contentPanel.setBorder( BorderFactory.createEtchedBorder() );
			
			this.contentPanel.setLayout( new GridLayout( 0, 2 ) );
						
			this.contentPanel.add( this.getProcessListPanel() );
			this.contentPanel.add( this.getSelectControl() );
		}
		
		return this.contentPanel;
	}

	private JPanel getProcessListPanel()
	{
		if( this.panelProcessList == null )
		{
			this.panelProcessList = new JPanel( new BorderLayout() );
			
			JPanel aux = new JPanel( new BorderLayout() );			
			aux.setBorder( BorderFactory.createTitledBorder( Language.getLocalCaption( Language.PROCESS_TEXT ) ) );
			aux.setBackground( Color.WHITE );
			aux.add( this.getProcessListTable(), BorderLayout.CENTER );
			
			GuiLanguageManager.addComponent( GuiLanguageManager.BORDER, Language.PROCESS_TEXT, aux.getBorder() );
			
			this.panelProcessList.add( new JScrollPane( aux, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER ), BorderLayout.CENTER );
			this.panelProcessList.add( this.getPanelControl(), BorderLayout.EAST );
		}
		
		return this.panelProcessList;
	}

	private JPanel getSelectControl()
	{
		if( this.panelSelectControl == null )
		{
			this.panelSelectControl = new JPanel( new BorderLayout() );
			
			this.panelSelectControl.add( new JScrollPane( this.getSelectedProcessPanel() ), BorderLayout.CENTER );
			
			if( this.selectionMode != SINGLE_SELECTION )
			{
				this.panelSelectControl.add( this.getJPanelUpDownCtrl(), BorderLayout.EAST );
			}
		}
		
		return this.panelSelectControl;
	}
	
	private JPanel getJPanelUpDownCtrl()
	{
		if( this.panelUpDownControl == null )
		{
			this.panelUpDownControl = new JPanel();
			
			BoxLayout ly = new BoxLayout( panelUpDownControl, BoxLayout.Y_AXIS);
			panelUpDownControl.setLayout( ly );
			
			panelUpDownControl.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5));
			
			panelUpDownControl.add( this.getBtnUp() );
			panelUpDownControl.add( Box.createRigidArea( new Dimension( 5, 5 ) ));
			panelUpDownControl.add( this.getBtnDown());
		}
		
		return this.panelUpDownControl;
	}
	
	private JButton getBtnUp()
	{
		if( this.buttonUp == null )
		{
			this.buttonUp = new JButton( );
			
			try
			{
				ImageIcon icon = new ImageIcon( BasicPainter2D.paintTriangle(  16,  2, Color.BLACK
																				, Color.LIGHT_GRAY, BasicPainter2D.NORTH ) );
				
				Dimension d = new Dimension( icon.getIconWidth(), icon.getIconHeight() );
				d.width += 6;
				d.height += 6;
				this.buttonUp.setPreferredSize( d );
				this.buttonUp.setIcon( icon );
			}
			catch( Exception ex )
			{
				Caption cap = Language.getAllCaptions().get(  Language.UP_TEXT );
				this.buttonUp.setText( cap.getCaption( Language.getCurrentLanguage() ) );
				
				GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.UP_TEXT, this.buttonUp );
			}
			
			this.buttonUp.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			this.buttonUp.addActionListener( new ActionListener()
			{				
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					JTable t = getSelectedProcessTable();
					moveProcess( t, -1 );
				}
			});
		}
		
		return this.buttonUp;
	}
	
	private JButton getBtnDown()
	{
		if( this.buttonDown == null )
		{
			this.buttonDown = new JButton();
			
			try
			{
				ImageIcon icon = new ImageIcon( BasicPainter2D.paintTriangle( 16,  2, Color.BLACK
																			, Color.LIGHT_GRAY
																			, BasicPainter2D.SOUTH ) );
				
				Dimension d = new Dimension( icon.getIconWidth(), icon.getIconHeight() );
				d.width += 6;
				d.height += 6;
				this.buttonDown.setPreferredSize( d );
				this.buttonDown.setIcon( icon );
			}
			catch( Exception ex )
			{
				Caption cap = Language.getAllCaptions().get( Language.DOWN_TEXT );
				this.buttonDown.setText( cap.getCaption( Language.getCurrentLanguage() ) );
				
				GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.DOWN_TEXT, this.buttonDown );
			}
			
			this.buttonDown.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			this.buttonDown.addActionListener( new ActionListener()
			{				
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					JTable t = getSelectedProcessTable();
					moveProcess( t, 1 );
				}
			});
		}
		
		return this.buttonDown;
	}
	
	private JPanel getSelectedProcessPanel()
	{
		if( this.panelSelectedProcesses == null )
		{
			this.panelSelectedProcesses = new JPanel( new BorderLayout() );
			this.panelSelectedProcesses.setBorder(BorderFactory.createTitledBorder( Language.getLocalCaption( Language.SELECT_TEXT ) ) );
			this.panelSelectedProcesses.setBackground( Color.WHITE );
			
			GuiLanguageManager.addComponent( GuiLanguageManager.BORDER											
											, Language.SELECT_TEXT
											, this.panelSelectedProcesses.getBorder() );

			
			this.panelSelectedProcesses.add( this.getSelectedProcessTable(), BorderLayout.CENTER );			
		}
		
		return this.panelSelectedProcesses;
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
											        Component cellComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
											        	
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
	
	private TableModel createTablemodel( )
	{					
		TableModel tm =  new DefaultTableModel( null, new String[] { Language.getLocalCaption( Language.PROCESS_TEXT ) } )
							{
								private static final long serialVersionUID = 1L;
								
								Class[] columnTypes = new Class[]{ String.class };								
								boolean[] columnEditables = new boolean[] { false };
								
								public Class getColumnClass(int columnIndex) 
								{
									return columnTypes[columnIndex];
								}
																								
								public boolean isCellEditable(int row, int column) 
								{
									boolean editable = columnEditables[ column ];
									
									return editable;
								}
							};
		return tm;
	}

	private JTable getProcessListTable()
	{
		if( this.tableProcessList == null )
		{	
			this.tableProcessList = this.getCreateJTable();
			this.tableProcessList.setModel( this.createTablemodel() );
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.PROCESS_TEXT, this.tableProcessList.getTableHeader() );
			
			this.tableProcessList.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
			
			if( this.selectionMode == SINGLE_SELECTION )
			{
				this.tableProcessList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
			}
			
			this.tableProcessList.setPreferredScrollableViewportSize( this.tableProcessList.getPreferredSize() );
			this.tableProcessList.setFillsViewportHeight( true );
			
			
		}
		
		return this.tableProcessList;
	}	
	
	private JTable getSelectedProcessTable()
	{
		if( this.tableSelectedProcessList == null )
		{	
			this.tableSelectedProcessList = this.getCreateJTable();
			this.tableSelectedProcessList.setModel( this.createTablemodel() );
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT
											, Language.SELECT_TEXT
											,	this.tableSelectedProcessList.getTableHeader() );
			
			this.tableSelectedProcessList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
			
			this.tableSelectedProcessList.setPreferredScrollableViewportSize( this.tableSelectedProcessList.getPreferredSize() );
			this.tableSelectedProcessList.setFillsViewportHeight( true );
			
			this.tableSelectedProcessList.getSelectionModel().addListSelectionListener( new ListSelectionListener()
			{	
				@Override
				public void valueChanged(ListSelectionEvent arg0)
				{	
					int sel = tableSelectedProcessList.getSelectedRow();
					
					// TODO
					if( sel < 0 )
					{
						getPluginSettingPanel().removeAll();
					}
					else
					{
						if( sel < tableProcessList.getRowCount() )
						{
							String id = tableSelectedProcessList.getValueAt( sel, 0 ).toString();
							
							loadPluginSettingPanel( id, sel );						
						}
					}
				}
			});
			
			
			this.tableSelectedProcessList.getModel().addTableModelListener( new TableModelListener() 
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
			
		}
		
		return this.tableSelectedProcessList;
	}
	
	private int getSelectedProcessIndex( int selectIndex, String idProcess )
	{
		int index = -1;
		for( int r = 0; r <= selectIndex; r++ )
		{
			String id = tableSelectedProcessList.getValueAt( r, 0 ).toString();
			
			if( id.equals( idProcess ) )
			{
				index++;
			}
		}
		
		return index;
	}
			
	private JPanel getPanelControl() 
	{
		if (panelMoveCtr == null) 
		{
			panelMoveCtr = new JPanel();
			
			BoxLayout ly = new BoxLayout(panelMoveCtr, BoxLayout.Y_AXIS);
			panelMoveCtr.setLayout( ly );
			
			panelMoveCtr.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5));
			
			panelMoveCtr.add(getButtonSelect());
			panelMoveCtr.add( Box.createRigidArea( new Dimension( 5, 5 ) ));
			panelMoveCtr.add(getButtonRemove());
			panelMoveCtr.add( Box.createRigidArea( new Dimension( 5, 5 ) ));
			panelMoveCtr.add(getBtnClear());
		}
		return panelMoveCtr;
	}
	
	private JButton getButtonSelect() 
	{
		if (buttonSelect == null) 
		{
			buttonSelect = new JButton( );
			
			try
			{
				ImageIcon icon = new ImageIcon( BasicPainter2D.paintTriangle( 16,  2, Color.BLACK
																			, Color.LIGHT_GRAY
																			, BasicPainter2D.EAST ) );

				Dimension d = new Dimension( icon.getIconWidth(), icon.getIconHeight() );
				d.width += 6;
				d.height += 6;
				this.buttonSelect.setPreferredSize( d );
				
				this.buttonSelect.setIcon( icon );
			}
			catch( Exception ex )
			{
				this.buttonSelect.setText( ">>" );
			}
			
			buttonSelect.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			this.buttonSelect.addActionListener( new ActionListener()
			{				
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					JTable tSelectedProcess = getSelectedProcessTable();
					JTable tProcessList = getProcessListTable();
					
					//moveSong( tSongList, tSelectedSong, false );				
					shiftSelectedProcess( tProcessList, tSelectedProcess );
				}
			});
		}
		return buttonSelect;
	}
	
	private JButton getButtonRemove() 
	{
		if (buttonRemove == null) 
		{
			buttonRemove = new JButton(  );
			
			try
			{
				ImageIcon icon = new ImageIcon( BasicPainter2D.paintTriangle( 16,  2, Color.BLACK
																			, Color.LIGHT_GRAY
																			, BasicPainter2D.WEST ) );

				Dimension d = new Dimension( icon.getIconWidth(), icon.getIconHeight() );
				d.width += 6;
				d.height += 6;
				this.buttonRemove.setPreferredSize( d );
				
				this.buttonRemove.setIcon( icon );
			}
			catch( Exception ex )
			{
				this.buttonSelect.setText( "<<" );
			}
			
			buttonRemove.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			this.buttonRemove.addActionListener( new ActionListener()
			{				
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					JTable tSelectedProcess = getSelectedProcessTable();
					DefaultTableModel tm = (DefaultTableModel)tSelectedProcess.getModel();
										
					int[] index = tSelectedProcess.getSelectedRows();
					Arrays.sort( index );
					
					for( int i = index.length - 1; i >= 0; i-- )
					{			
						int r = index[ i ];
						
						String id = tm.getValueAt( r, 0 ).toString();
						
						int ind = getSelectedProcessIndex( r, id );
						
						tm.removeRow( r );
								
						try
						{
							PluginLoader loader = PluginLoader.getInstance();

							loader.removePluginInstance( plgType, id, ind );
						}
						catch( Exception ex )
						{
							ExceptionMessage msg = new ExceptionMessage( ex, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionDictionary.ERROR_MESSAGE );
							ExceptionDialog.showMessageDialog( msg, true, true );
						}					
					}
				}
			});
		}
		return buttonRemove;
	}
	
	private JButton getBtnClear() 
	{
		if (btnClear == null) 
		{
			btnClear = new JButton( );
			
			try
			{
				this.btnClear.setIcon( GeneralAppIcon.Clear( 16, Color.BLACK ) );
			}
			catch( Exception ex )
			{	
				Caption cap = Language.getAllCaptions().get( Language.CLEAR );
			
				this.btnClear.setText( cap.getCaption( Language.getCurrentLanguage() ) );
				
				GuiLanguageManager.addComponent( GuiLanguageManager.TEXT
												, Language.CLEAR
												, this.btnClear );
			}
			
			btnClear.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			this.btnClear.addActionListener( new ActionListener()
			{				
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					JTable tsel = getSelectedProcessTable();
					
					// TODO
					
					for( int i = tsel.getRowCount() - 1; i >= 0; i-- )
					{						
						// TODO						
						((DefaultTableModel)tsel.getModel()).removeRow( i );						
					}
				}
			});
		}
		return btnClear;
	}
		
	private void shiftSelectedProcess( JTable source, JTable dest )
	{
		DefaultTableModel tmDest = (DefaultTableModel)dest.getModel();
		
		int[] selIndex = source.getSelectedRows();
		Arrays.sort( selIndex );
		
		if( selIndex.length > 0 )
		{			
			for( int i = selIndex.length - 1; i >= 0; i-- )
			{
				int index = selIndex[ i ];
				
				String process = source.getValueAt( index, 0 ).toString();
			
				try 
				{	
					tmDest.addRow( new String[] { process } );
				} 
				catch (Exception e) 
				{
					Dialog_Info d = new Dialog_Info( GuiManager.getInstance().getAppUI(), e.getMessage() + "\n" + e.getCause(), true );
					Dimension size = Toolkit.getDefaultToolkit().getScreenSize();
					size.width /= 2;
					size.height /= 2;
					d.setSize( size );
					d.setVisible( true );
				}								
			}
		}
	}
		
	private void moveProcess( JTable source, int shift )
	{
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
					
					if( i == 0 )
					{
						source.setRowSelectionInterval( row, row );
					}
				}				
			}
		}
	}

	private void loadPluginSettingPanel( String idPlugin, int selectedTableIndex )
	{
		int plgIndex = this.getSelectedProcessIndex( selectedTableIndex, idPlugin );
		
		PluginLoader loader;
		try 
		{
			loader = PluginLoader.getInstance();
			
			List< ILSLRecPlugin > plgs = loader.getAllPlugins( plgType, idPlugin );
			
			ILSLRecConfigurablePlugin plg = (ILSLRecConfigurablePlugin)plgs.get( plgIndex );
			
			JPanel p = this.getPluginSettingPanel();
			p.setVisible( false );
			p.removeAll();
			
			JPanel p2 = plg.getSettingPanel();
			
			p.add( p2, BorderLayout.CENTER );
			
			p.setVisible( true );			
		}
		catch (Exception e) 
		{
			ExceptionMessage msg = new ExceptionMessage( e, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionDictionary.ERROR_MESSAGE );
			
			ExceptionDialog.showMessageDialog( msg, true, true );
		}	
	}
}
