package lslrec.gui.panel.plugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

import org.jfree.ui.tabbedui.VerticalLayout;

import lslrec.config.ConfigApp;
import lslrec.config.language.Caption;
import lslrec.config.language.Language;
import lslrec.dataStream.setting.DataStreamSetting;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionDictionary;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.gui.GuiLanguageManager;
import lslrec.gui.GuiManager;
import lslrec.gui.dialog.Dialog_Info;
import lslrec.gui.dialog.Dialog_OptionList;
import lslrec.gui.miscellany.BasicPainter2D;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.plugin.loader.PluginLoader;
import lslrec.plugin.lslrecPlugin.ILSLRecConfigurablePlugin;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin.PluginType;
import lslrec.plugin.register.PluginRegistrar;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class DataProcessingPluginSelectorPanel extends JPanel
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4212736396179567663L;
			
	private JPanel contentPanel;
	private JPanel panelUpDownControl;
	private JPanel panelMoveCtr;
	private JPanel panelPluginSetting;
	
	private JTable tablePluginList;
	private JTable tableSelectedPluginList;

	private JButton buttonSelect;
	private JButton buttonRemove;
	private JButton btnClear;
	private JButton buttonUp;
	private JButton buttonDown;
	
	private final PluginType plgType = PluginType.DATA_PROCESSING;
	
	public DataProcessingPluginSelectorPanel( Collection< String > idPlugins )
	{		
		super.setLayout( new BorderLayout() );
		
		this.add( this.getPluginPanel(), BorderLayout.WEST );		
		this.add( this.getPluginSettingPanel(), BorderLayout.CENTER );
		
		JTable t = this.getProcessListTable();
		
		DefaultTableModel tm = (DefaultTableModel)t.getModel();
		
		for( String id : idPlugins )
		{
			tm.addRow( new String[] { id } );
		}
		
		JFrame w = (JFrame) SwingUtilities.windowForComponent( this );
		ExceptionDialog.createExceptionDialog( w );
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
		if( this.panelPluginSetting == null )
		{
			this.panelPluginSetting = new JPanel( new BorderLayout() );	
		}
		
		return this.panelPluginSetting;
	}
	
	private JPanel getStreamPanel( String pluginID, int numInstance )
	{
		JPanel panel = new JPanel( new BorderLayout( 0, 0 ) );
		
		JPanel p = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
				
		JButton addbt = new JButton( );
		Icon ic = GeneralAppIcon.Add( 32, Color.BLACK );
				
		//
		// Load selected streams
		//
		
		List< DataStreamSetting > streams = new ArrayList< DataStreamSetting >( ( HashSet< DataStreamSetting > )ConfigApp.getProperty( ConfigApp.LSL_ID_DEVICES ) );
		
		List< DataStreamSetting > selectedStreams = new ArrayList< DataStreamSetting >();
		List< String > removeKeys = new ArrayList<String>();
		for( String key : PluginRegistrar.getRegisterKey() )
		{			
			boolean del = true;
			
			searchStream:
			for( DataStreamSetting str : streams )
			{
				if( ( str.getStreamName() + str.getUID() ).equals( key ) )
				{
					del = false;
					
					List< String > idPlugins = PluginRegistrar.getPlugins( key ).get( PluginType.DATA_PROCESSING );
					
					if( idPlugins != null )
					{
						searchPlugin:
						for( String id : idPlugins )
						{
							if( id.equals( pluginID ) )
							{
								if( numInstance == 0 )
								{
									selectedStreams.add( str );
									
									break searchPlugin;
								}
								
								numInstance--;
							}
						}
					}
					
					break searchStream;
				}
			}			
			
			if( del )
			{
				removeKeys.add( key );
			}
		}
		
		for( String key : removeKeys )
		{
			PluginRegistrar.removePlugins( key );
		}
		
		this.addStream( p, selectedStreams, pluginID );
		
		
		//
		// Button Add
		//
		
		addbt.setIcon( ic );
		if( ic == null )
		{
			addbt.setText( "+" );
		}
		
		addbt.addActionListener( new ActionListener() 
		{	
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				List< String > opts = new ArrayList< String >();
				for( DataStreamSetting str : streams )
				{
					opts.add( str.getStreamName() + " (" + str.getUID() + ")" );
				}

				Dialog_OptionList dial = new Dialog_OptionList();

				dial.setOptions( opts );

				dial.setLocationRelativeTo( (Component)e.getSource() );

				Dimension d = Toolkit.getDefaultToolkit().getScreenSize();

				dial.setSize( d.width / 3, d.height / 3 );

				dial.setModal( true );

				dial.setVisible( true );

				int[] selIndexes = dial.getSelectedIndex();

				List< DataStreamSetting > selStreams = new ArrayList< DataStreamSetting >();
				for( int index : selIndexes )
				{
					selStreams.add( streams.get( index ) );
				}

				addStream( p, selStreams, pluginID );
			}
		});
				
		
		panel.add( addbt, BorderLayout.WEST );
		JScrollPane sp = new JScrollPane( p, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		sp.setPreferredSize( panel.getPreferredSize() );
		sp.getHorizontalScrollBar().setPreferredSize( new Dimension( 0, 5 ) );
		panel.add( sp, BorderLayout.CENTER );
		
		JPanel strPanel = new JPanel( new BorderLayout( 0, 0 ) );
		strPanel.add( panel, BorderLayout.NORTH );
		
		return strPanel;
	}
	
	private void addStream( JPanel panel, List< DataStreamSetting > streams, String plgId )
	{
		if( panel != null && streams != null && plgId != null )
		{
			panel.setVisible( false );
			
			for( DataStreamSetting str : streams )
			{
				final String id = str.getStreamName() + str.getUID();
				
				
				
								
				JLabel lb = new JLabel( str.getStreamName() );
				lb.setToolTipText( str.getStreamName() + " (" + str.getUID() + ")" );
				JButton b = new JButton( );
				b.setBorder( BorderFactory.createEmptyBorder() );
				b.setIcon( GeneralAppIcon.Close( 10, Color.RED  ) );
				
				Font f = b.getFont();
				b.setFont( new Font( f.getName(), Font.BOLD, f.getSize() ) );
				
				Dimension d = b.getPreferredSize();
				d.height = lb.getPreferredSize().height;
				b.setPreferredSize( d );
				
				JPanel p = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
				p.setBorder( BorderFactory.createEtchedBorder() );
				p.getInsets( new Insets( 0, 0, 0, 0 ) );
				
				/*
				FontMetrics fm = lb.getFontMetrics( lb.getFont() ); 
				d = new Dimension( );
				d.height = lb.getPreferredSize().height;
				d.width = fm.stringWidth( lb.getText() ) * 2;
				p.setPreferredSize( d );
				*/
				
				final int numInstance = PluginRegistrar.addPluginInstance( id , PluginType.DATA_PROCESSING, plgId );
				
				b.addActionListener( new ActionListener() 
				{	
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						PluginRegistrar.removePlugin( id, PluginType.DATA_PROCESSING, plgId, numInstance);
						
						panel.setVisible( false );
						
						panel.remove( p );
						
						panel.setVisible( true );
					}
				});
				

				p.add( lb );
				p.add( b );
				
				panel.add( p );
			}
			
			panel.setVisible( true );
		}
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
			JTable tab = this.getProcessListTable();
			aux.add( new JScrollPane( tab, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER ) );
				
			tab = this.getSelectedProcessTable();

			aux.add( this.getSelectionControlPanel( this.getProcessListTable(), tab ) );

			aux.add( new JScrollPane( tab, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER ) );

			aux.add( this.getJPanelUpDownCtrl( tab ), BorderLayout.EAST );

			this.setPluginTableSelectionEvents( this.getSelectedProcessTable() );
			
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
	
	private TableModel createTablemodel( String tableHeaderID )
	{					
		TableModel tm =  new DefaultTableModel( null, new String[] { Language.getLocalCaption( tableHeaderID ) } )
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
		if( this.tablePluginList == null )
		{	
			this.tablePluginList = this.getCreateJTable();
			this.tablePluginList.setModel( this.createTablemodel( Language.SETTING_PLUGIN ) );
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.SETTING_PLUGIN, this.tablePluginList.getTableHeader() );
			
			this.tablePluginList.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );
			
			this.tablePluginList.setPreferredScrollableViewportSize( this.tablePluginList.getPreferredSize() );
			this.tablePluginList.setFillsViewportHeight( true );
		}
		
		return this.tablePluginList;
	}	
	
	private JTable getSelectedProcessTable()
	{
		if( this.tableSelectedPluginList == null )
		{	
			this.tableSelectedPluginList = this.getCreateJTable();
			this.tableSelectedPluginList.setModel( this.createTablemodel( Language.SELECTED_TEXT ) );
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT
											, Language.SELECTED_TEXT
											,	this.tableSelectedPluginList.getTableHeader() );
			
			this.tableSelectedPluginList.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
			
			this.tableSelectedPluginList.setPreferredScrollableViewportSize( this.tableSelectedPluginList.getPreferredSize() );
			this.tableSelectedPluginList.setFillsViewportHeight( true );			
		}
		
		return this.tableSelectedPluginList;
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
							String idPlugin = table.getValueAt( sel, 0 ).toString();
							
							loadPluginSettingPanel( idPlugin, sel, table );						
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
			
	private JPanel getSelectionControlPanel( JTable sourceTable, JTable destTable ) 
	{
		if ( this.panelMoveCtr == null && sourceTable != null && destTable != null ) 
		{
			this.panelMoveCtr = new JPanel();
			
			VerticalLayout ly = new VerticalLayout();
			this.panelMoveCtr.setLayout( ly );
			
			this.panelMoveCtr.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5));
			
			this.panelMoveCtr.add( this.getButtonSelect( sourceTable, destTable ) );
			this.panelMoveCtr.add( Box.createRigidArea( new Dimension( 5, 5 ) ));
			this.panelMoveCtr.add( this.getButtonRemove( destTable )) ;
			this.panelMoveCtr.add( Box.createRigidArea( new Dimension( 5, 5 ) ));
			this.panelMoveCtr.add( this.getBtnClear( destTable ) );
		}
		
		return this.panelMoveCtr;
	}
	
	private JButton getButtonSelect( JTable sourceTable, JTable destTable ) 
	{
		if (buttonSelect == null && sourceTable != null && destTable != null  ) 
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
					copySelectedPlugin( sourceTable, destTable );
				}
			});
		}
		return buttonSelect;
	}
	
	private JButton getButtonRemove( JTable refTable ) 
	{
		if ( this.buttonRemove == null && refTable != null ) 
		{
			this.buttonRemove = new JButton(  );
			
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
			
			this.buttonRemove.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			this.buttonRemove.addActionListener( new ActionListener()
			{				
				@Override
				public void actionPerformed(ActionEvent arg0)
				{
					removePlugins( refTable );					
				}
			});
		}
		
		return this.buttonRemove;
	}
	
	private void removePlugins( JTable refTable )
	{
		DefaultTableModel tm = (DefaultTableModel)refTable.getModel();
		
		int[] index = refTable.getSelectedRows();
		
		Arrays.sort( index );
		
		for( int i = index.length - 1; i >= 0; i-- )
		{			
			int r = index[ i ];
			
			String id = tm.getValueAt( r, 0 ).toString();
			
			int ind = getNumPluginInstances( refTable, r, id );
			
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
	
	private JButton getBtnClear( JTable refTable ) 
	{
		if ( this.btnClear == null && refTable != null ) 
		{
			this.btnClear = new JButton( );
			
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
			
			this.btnClear.setAlignmentX(Component.CENTER_ALIGNMENT);
			
			this.btnClear.addActionListener( new ActionListener()
			{				
				@Override
				public void actionPerformed(ActionEvent arg0)
				{				
					for( int i = refTable.getRowCount() - 1; i >= 0; i-- )
					{
						refTable.setRowSelectionInterval( i, i );
						removePlugins( refTable );
					}
				}
			});
		}
		return btnClear;
	}
	
	private JPanel getJPanelUpDownCtrl( JTable refTable )
	{
		if( this.panelUpDownControl == null && refTable != null )
		{
			this.panelUpDownControl = new JPanel();
			
			VerticalLayout ly = new VerticalLayout();
			panelUpDownControl.setLayout( ly );
			
			panelUpDownControl.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5));
			
			panelUpDownControl.add( this.getBtnUp( refTable ) );
			panelUpDownControl.add( Box.createRigidArea( new Dimension( 5, 5 ) ));
			panelUpDownControl.add( this.getBtnDown( refTable ) );
		}
		
		return this.panelUpDownControl;
	}
	
	private JButton getBtnUp( JTable refTable )
	{
		if( this.buttonUp == null && refTable != null )
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
					reorderPlugin( refTable, -1 );
				}
			});
		}
		
		return this.buttonUp;
	}
	
	private JButton getBtnDown( JTable refJTable )
	{
		if( this.buttonDown == null && refJTable != null  )
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
					reorderPlugin( refJTable, 1 );
				}
			});
		}
		
		return this.buttonDown;
	}
	
	private void copySelectedPlugin( JTable source, JTable dest )
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
		
	private void reorderPlugin( JTable source, int shift )
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

	private void loadPluginSettingPanel( String idPlugin, int selectedTableIndex, JTable refTable )
	{
		int plgIndex = this.getNumPluginInstances( refTable, selectedTableIndex, idPlugin );
		
		PluginLoader loader;
		try 
		{
			loader = PluginLoader.getInstance();
			
			List< ILSLRecPlugin > plgs = loader.getAllPlugins( this.plgType, idPlugin );
			
			ILSLRecConfigurablePlugin plg;
			if( plgIndex >= plgs.size() )
			{
				plg = (ILSLRecConfigurablePlugin)loader.addNewPluginInstance( this.plgType, idPlugin );
			}
			else
			{
				plg = (ILSLRecConfigurablePlugin)plgs.get( plgIndex );
			}
			
			JPanel p = this.getPluginSettingPanel();
			p.setVisible( false );
			p.removeAll();
			
			if( plg != null )
			{
				JPanel streamPanel = getStreamPanel( idPlugin, plgIndex );
				streamPanel.setBorder( BorderFactory.createTitledBorder( Language.getLocalCaption( Language.SETTING_LSL_DEVICES ) ) );
				
				//GuiLanguageManager.addComponent( GuiLanguageManager.BORDER, Language.SETTING_LSL_DEVICES, streamPanel.getBorder() );
				
				p.add( streamPanel, BorderLayout.NORTH );
								
				JPanel p2 = plg.getSettingPanel();				
				p.add( p2, BorderLayout.CENTER );
			}
			
			p.setVisible( true );			
		}
		catch (Exception e) 
		{
			Exception e1 = new Exception( "Setting panel for plugin " + idPlugin + " is not available.", e );
			ExceptionMessage msg = new ExceptionMessage(  e1
														, Language.getLocalCaption( Language.DIALOG_ERROR )
														, ExceptionDictionary.ERROR_MESSAGE );
		
			ExceptionDialog.showMessageDialog( msg, true, true );
		}	
	}
	
	private int getNumPluginInstances( JTable refTable, int selectIndex, String idProcess )
	{
		int index = -1;
		
		if( refTable != null )
		{
			for( int r = 0; r <= selectIndex; r++ )
			{
				String id = refTable.getValueAt( r, 0 ).toString();
				
				if( id.equals( idProcess ) )
				{
					index++;
				}
			}
		}
		
		return index;
	}
}
