package lslrec.plugin.lslrecPlugin.processing.setting;

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

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import lslrec.config.language.Caption;
import lslrec.config.language.Language;
import lslrec.gui.GuiLanguageManager;
import lslrec.gui.guiManager;
import lslrec.gui.miscellany.BasicPainter2D;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.gui.miscellany.InfoDialog;
import lslrec.plugin.loader.PluginLoader;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;

public class SelectDataProcessPanel extends JPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4212736396179567663L;
	
	private static SelectDataProcessPanel ssp = null;
	
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
		
	public static SelectDataProcessPanel getInstance()
	{
		if( ssp == null )
		{
			ssp = new SelectDataProcessPanel();
		}
		
		return ssp;
	}
	
	private SelectDataProcessPanel( )
	{		
		super.setLayout( new BorderLayout() );
		
		this.add( this.getContainerPanel(), BorderLayout.WEST );		
		this.add( this.getProcessSettingPanel(), BorderLayout.CENTER );
	}
	
	private JPanel getProcessSettingPanel()
	{
		if( this.panelProecssSetting == null )
		{
			this.panelProecssSetting = new JPanel();
			this.panelProecssSetting.setBackground( Color.red );
		}
		
		return this.panelProecssSetting;
	}
	
	private JPanel getContainerPanel()
	{
		if( this.contentPanel == null )
		{
			this.contentPanel = new JPanel();
			this.contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			
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
			
			JPanel panel = new JPanel( new BorderLayout() );
			panel.add( this.gettableProcessList() , BorderLayout.CENTER );
			
			JScrollPane scroll = new JScrollPane( panel );
			scroll.setBorder( BorderFactory.createTitledBorder( Language.getLocalCaption( Language.PROCESS_TEXT ) ) );
			scroll.setBackground( Color.WHITE );
			
			GuiLanguageManager.addComponent( GuiLanguageManager.BORDER, Language.PROCESS_TEXT, scroll.getBorder() );
			
			this.panelProcessList.add( scroll, BorderLayout.CENTER );
			this.panelProcessList.add( this.getPanelControl(), BorderLayout.EAST );
		}
		
		return this.panelProcessList;
	}

	private JPanel getSelectControl()
	{
		if( this.panelSelectControl == null )
		{
			this.panelSelectControl = new JPanel( new BorderLayout() );
			
			this.panelSelectControl.add( new JScrollPane( this.getSelectedSongs() ), BorderLayout.CENTER );
			this.panelSelectControl.add( this.getJPanelUpDownCtrl(), BorderLayout.EAST );
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
	
	private JPanel getSelectedSongs()
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

	private JTable gettableProcessList()
	{
		if( this.tableProcessList == null )
		{	
			this.tableProcessList = this.getCreateJTable();
			this.tableProcessList.setModel( this.createTablemodel() );
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.PROCESS_TEXT, this.tableProcessList.getTableHeader() 
					);						
			this.tableProcessList.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
			
			this.tableProcessList.setPreferredScrollableViewportSize( this.tableProcessList.getPreferredSize() );
			this.tableProcessList.setFillsViewportHeight( true );
			
			this.addSelectionListenerTable( this.tableProcessList );
			
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
						getProcessSettingPanel().removeAll();
					}
					else
					{
						String idProcess = tableSelectedProcessList.getValueAt( sel, 0 ).toString();
						
						ILSLRecPluginDataProcessing pl = (ILSLRecPluginDataProcessing)PluginLoader.getPlugin( ILSLRecPluginDataProcessing.class, idProcess );
						
						JPanel p = getProcessSettingPanel();
						p.removeAll();
						
						if( pl != null )
						{
							p.add( pl.getSettingPanel(), BorderLayout.CENTER );
						}
					}
				}
			});
			
			this.tableSelectedProcessList.getModel().addTableModelListener( new TableModelListener()
			{	
				@Override
				public void tableChanged(TableModelEvent arg0)
				{
					DefaultTableModel tm = (DefaultTableModel)arg0.getSource();
					
								
				}
			});
			
		}
		
		return this.tableSelectedProcessList;
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
					JTable tSelectedSong = getSelectedProcessTable();
					JTable tSongList = gettableProcessList();
					
					//moveSong( tSongList, tSelectedSong, false );				
					shiftSelectedProcess( tSongList, tSelectedSong );
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
					JTable tSelectedSong = getSelectedProcessTable();
					
					// TODO
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
				
				String song = source.getValueAt( index, 0 ).toString();
			
				try 
				{	
					tmDest.addRow( new String[] { song } );
				} 
				catch (Exception e) 
				{
					InfoDialog d = new InfoDialog( guiManager.getInstance().getAppUI(), e.getMessage() + "\n" + e.getCause(), true );
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
						
					String song = source.getValueAt( index, 0 ).toString();
					
					if( row >= 0 && row < source.getRowCount() )
					{				
						tmSource.removeRow( index );
						tmSource.insertRow( row, new String[] { song } );
					}
					
					if( i == 0 )
					{
						source.clearSelection();
						source.addRowSelectionInterval( row, row );
					}
				}
			}
		}
	}
}
