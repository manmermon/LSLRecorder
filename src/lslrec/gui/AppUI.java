/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018-2019 by Manuel Merino Monge <manmermon@dte.us.es>
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

package lslrec.gui;

import lslrec.config.language.Language;
import lslrec.control.message.AppState;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.dataStream.sync.SyncMethod;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.gui.miscellany.BasicPainter2D;
import lslrec.gui.miscellany.DisabledGlassPane;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.gui.miscellany.MenuScroller;
import lslrec.config.ConfigApp;
import lslrec.gui.miscellany.LevelIndicator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;

import com.formdev.flatlaf.FlatDarculaLaf;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;

public class AppUI extends JFrame
{
	private static AppUI ui = null;

	private static final long serialVersionUID = 1L;

	// panel	
	private JPanel jContentPane = null;	
	private JPanel jPanelAppState = null;
	private JPanel jPanelInputMsgLog;
	private JPanel jPanelSelectSyncMethod;
	//private JPanel jPanelMenus;		
	//private JTabbedPane jTabPanelInMsg;
	//private JPanel jPanelAppStateLog;
	
	// Button
	private JButton jButtonClearLog;
	private JButton btnRefreshDevices;
	private JButton jBtnInfo;
	private JButton jBtnSyncMet;
	private JButton jBtnChecklist;
	//private JButton jButtonClearAppStateLog;
		
	private JToggleButton jButtomPlayStop = null;

	// ScrollPanel
	private JScrollPane scrollPaneInputMessage;
	private JScrollPane scrollPanelCtrl;
	//private JScrollPane scrollPaneAppStateLog;

	// TextPanel
	private JTextPane logTextArea;
	//private JTextPane appStateLogTextArea;

	// JPopMenu
	private JPopupMenu popupMenu_2;	
	private JMenuItem mntmCopy;
	private JMenuItem mntmCopyall;
	private JMenuItem mntmClear;

	// JSplitPane
	private JSplitPane jPanelOper = null;
	private JSplitPane jPanelInputMsg;

	// menuBar
	private JMenuBar jJMenuBar = null;

	// menu
	private JMenu jFileMenu = null;
	private JMenu jLangMenu = null;
	private JMenu menuPreference = null;
	private JMenu menuClis = null;
	private JMenu jThemeMenu = null;
	//private JMenu menuLibrary = null;
	
	// menuItem	
	private JMenuItem jMenuAbout = null;
	private JMenuItem jGNUGLP = null;	
	private JMenuItem menuLoad = null;
	private JMenuItem menuSave = null;
	private JMenuItem menuConvertBinary = null;
	private JMenuItem menuWritingTest = null;
	private JMenuItem menuExit = null;	
	//private JMenuItem menuShowLog = null;
	private JMenuItem menuAdvanceOpt = null;
	private JMenuItem menuConvertClisTo = null;
	private JMenuItem menuClisDataPlot = null;

	// Processbar
	private LevelIndicator appTextState = null;
	
	// textField
	//private JTextField appTextState = null;
	private JTextField timeState = null;
	private JTextField sessionTimeText = null;

	// Settings
	private SyncSocketSettingPanel leftSettingPanel;
	private RightSettingsPanel rightSettingPanel;

	// JCombox
	//private JComboBox< String > jComboxSyncMethod;	

	// CheckBox
	private JCheckBox checkActiveSpecialInputMsg;
	private JCheckBox checkAutoScroll;
	//private JCheckBox checkAutoScrollAppStateLog;
	
	private AppUI() 
	{		
		initialize();
	}

	public static AppUI getInstance()
	{
		if (ui == null)
		{
			ui = new AppUI();
		}

		return ui;
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize()
	{		 
		DisabledGlassPane glass = new DisabledGlassPane();
		glass.activate( Language.getLocalCaption( Language.GENERAL_WAIT_MSG ) );
		super.setGlassPane( glass );
		super.getGlassPane().setVisible( false );
		
		GuiTextManager.addComponent( GuiTextManager.TEXT, Language.GENERAL_WAIT_MSG, glass );
		
		super.setJMenuBar( this.getJJMenuBar() );		
		super.setContentPane( this.getJContentPane() );

		super.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );

		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{				
				GuiManager.getInstance().closingChecks();
			}
		});

		this.setHotKeys();		
	}

	private void setHotKeys()
	{
		InputMap inputMap = getRootPane().getInputMap( JComponent.WHEN_IN_FOCUSED_WINDOW );

		ActionMap actionMap = getRootPane().getActionMap(); 

		String idAct = "actionPlayStop";
		inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_P, KeyEvent.CTRL_DOWN_MASK ), idAct );		 
		actionMap.put( idAct, KeyActions.getButtonClickAction( idAct, this.getJButtonPlay() ) );

		idAct = "actionRefresh";
		inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK ), idAct );		 
		actionMap.put( idAct, KeyActions.getButtonClickAction( idAct, this.getJButtonRefreshDataStreams() ) );
	}

	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	protected JPanel getJContentPane() 
	{
		if (this.jContentPane == null)
		{
			this.jContentPane = new JPanel();
			this.jContentPane.setLayout(new BorderLayout());

			this.jContentPane.add( this.getJScrollPanelActCtr(), BorderLayout.NORTH );
			this.jContentPane.add( this.getJPanelOper(),  BorderLayout.CENTER );
			this.jContentPane.add(this.getAppStatePanel( this.getJJMenuBar().getPreferredSize().height ), BorderLayout.SOUTH );
		}

		return this.jContentPane;
	}

	public JScrollPane getJScrollPanelActCtr()
	{
		if( this.scrollPanelCtrl == null )
		{
			this.scrollPanelCtrl = new JScrollPane( this.getJPanelSelectSyncMethod() );

			this.scrollPanelCtrl.setBorder( BorderFactory.createEmptyBorder() );

			this.scrollPanelCtrl.getVerticalScrollBar().setPreferredSize( new Dimension( 10, 0 ) );
			this.scrollPanelCtrl.getHorizontalScrollBar().setPreferredSize( new Dimension( 0, 10 ) );
		}

		return this.scrollPanelCtrl;
	}

	public JPanel getJPanelSelectSyncMethod()
	{
		if( this.jPanelSelectSyncMethod == null )
		{
			this.jPanelSelectSyncMethod = new JPanel( new FlowLayout( FlowLayout.LEFT ) );

			this.jPanelSelectSyncMethod.add( this.getJButtonPlay() );

			this.jPanelSelectSyncMethod.add( this.getJButtonRefreshDataStreams() );

			JLabel lb = new JLabel( Language.getLocalCaption( Language.SETTING_SYNC_METHOD ) );			
			
			this.jPanelSelectSyncMethod.add( lb );
			//this.jPanelSelectSyncMethod.add( this.getJComboxSyncMethod() );
			this.jPanelSelectSyncMethod.add( this.getBtnSyncMethod() );
			this.jPanelSelectSyncMethod.add( this.getJCheckActiveSpecialInputMsg() );
			this.jPanelSelectSyncMethod.add( this.getJButtonInfo() );		
			this.jPanelSelectSyncMethod.add( this.getJBtChecklist() );
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.SETTING_SYNC_METHOD, lb );
		}

		return this.jPanelSelectSyncMethod;
	}
	
	protected JButton getBtnSyncMethod()	
	{
		if( this.jBtnSyncMet == null )
		{
			final String ID = ConfigApp.SELECTED_SYNC_METHOD;

			this.jBtnSyncMet = new JButton( SyncMethod.SYNC_NONE );
						
			String met = "";
			
			for( String m : SyncMethod.getSyncMethodID() )
			{
				if( m.length() > met.length() )
				{
					met = m; 
				}
			}
			
			ImageIcon ic = new ImageIcon( BasicPainter2D.paintPolygonLine( new int[] { 0, 5, 10 }
																			, new int[] {0, 5, 0 } 
																			, 1.5F, Color.DARK_GRAY, null )	 
										);
			if( ic != null )
			{
				this.jBtnSyncMet.setIcon( ic );
			}
			
			FontMetrics fm = this.jBtnSyncMet.getFontMetrics( this.jBtnSyncMet.getFont() );
			Dimension d = this.jBtnSyncMet.getPreferredSize();
			Insets inset = this.jBtnSyncMet.getInsets();
			d.width = fm.stringWidth( met ) + 5 + ic.getIconWidth() + inset.right + inset.left;
			if( d.width > 130 )
			{
				d.width = 130;
			}
			this.jBtnSyncMet.setPreferredSize( d );
			this.jBtnSyncMet.setSize( d );
			
			this.jBtnSyncMet.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					final JButton syncBtn = (JButton)e.getSource();

					GuiManager.getInstance().showSelectionSyncMethod( syncBtn );
				}
			});
			
			GuiManager.registerGUIComponent( ID, ID, this.jBtnSyncMet );
		}

		return this.jBtnSyncMet;
	}

	protected JCheckBox getJCheckActiveSpecialInputMsg()
	{
		if( this.checkActiveSpecialInputMsg == null )
		{
			final String ID = ConfigApp.IS_ACTIVE_SPECIAL_INPUTS;

			this.checkActiveSpecialInputMsg = new JCheckBox( Language.getLocalCaption( Language.SETTING_SPECIAL_IN_METHOD ) );
						
			this.checkActiveSpecialInputMsg.setSelected( (Boolean)ConfigApp.getProperty( ID ) );

			this.checkActiveSpecialInputMsg.addItemListener( new ItemListener()
			{	
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					JCheckBox c = (JCheckBox)e.getSource();
					
					GuiManager.getInstance().setConfigValueCheckbox( ID, c );
				}
			});
			
			GuiManager.registerGUIComponent( ID, ID, this.checkActiveSpecialInputMsg );
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.SETTING_SPECIAL_IN_METHOD, this.checkActiveSpecialInputMsg );

		}

		return this.checkActiveSpecialInputMsg;
	}

	protected JButton getJButtonInfo()
	{
		if( this.jBtnInfo == null )
		{
			Dimension d = new Dimension( 20, 16 );

			this.jBtnInfo = new JButton( );			
			
			FontMetrics fm = this.jBtnInfo.getFontMetrics( this.jBtnInfo.getFont() );
			this.jBtnInfo.setIcon( new ImageIcon( BasicPainter2D.paintText( 0, 0, "?", fm, null, Color.BLACK, null ) ) );
			
			this.jBtnInfo.setBorder( BorderFactory.createLineBorder( Color.BLACK ) );
			//this.jBtnInfo.setBackground( Color.YELLOW.darker() );
			this.jBtnInfo.setBackground( new Color( 255, 255, 204 ) );
			this.jBtnInfo.setForeground( Color.BLACK );

			this.jBtnInfo.setPreferredSize( d );

			this.jBtnInfo.addActionListener( new ActionListener()
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JButton b = (JButton)e.getSource();

					GuiManager.getInstance().showInfoPanel( b );
				}
			});
		}

		return this.jBtnInfo;
	}

	protected JButton getJButtonRefreshDataStreams()
	{
		if( this.btnRefreshDevices == null )
		{
			final String txt = Language.getLocalCaption( Language.SETTING_LSL_REFRESH );

			this.btnRefreshDevices = new JButton( txt );

			this.btnRefreshDevices.addActionListener( new ActionListener()  
			{		
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					GuiManager.getInstance().execRefreshStreams();
				}
			});

			ImageIcon icon = null;

			try
			{
				//icon = new ImageIcon( settingMenu_labStreamingLayer.class.getResource( "/com/sun/javafx/scene/web/skin/Redo_16x16_JFX.png" ) );
				icon = GeneralAppIcon.Refresh( 16, 16, Color.BLACK, null );
			}
			catch( Exception e )
			{				
			}
			catch( Error e )
			{				
			}

			this.btnRefreshDevices.setIcon( icon );
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.SETTING_LSL_REFRESH, this.btnRefreshDevices );
		}

		return this.btnRefreshDevices;
	}

	/**
	 * This method initializes jPanelOper	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	protected JSplitPane getJPanelOper() 
	{
		if (this.jPanelOper == null)
		{
			this.jPanelOper = new JSplitPane();
			//this.jPanelOper.setResizeWeight( 0.05 );
			//this.jPanelOper.setDividerLocation( 0.2 );
			this.jPanelOper.setOrientation( JSplitPane.HORIZONTAL_SPLIT );
			this.jPanelOper.setBackground(Color.white);
			this.jPanelOper.setFocusable(false);
			this.jPanelOper.setFocusCycleRoot(false);

			try 
			{
				this.jPanelOper.setLeftComponent( this.getJPanelInputMsg() );
				this.jPanelOper.setRightComponent( this.getRightPanelSetting() );
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				
				//JOptionPane.showMessageDialog( this, e.getMessage() + "\n" + e.getCause(), "LSL Exception", JOptionPane.ERROR_MESSAGE );
				ExceptionMessage msg = new  ExceptionMessage( e
															, "Stream Exception" 
															, ExceptionMessage.ERROR_MESSAGE );
				
				ExceptionDialog.showMessageDialog(msg, true, true );
			}
		}

		return this.jPanelOper;
	}

	private JSplitPane getJPanelInputMsg()
	{
		if( this.jPanelInputMsg == null )
		{
			this.jPanelInputMsg = new JSplitPane();
			this.jPanelInputMsg.setResizeWeight( 0.5 );
			this.jPanelInputMsg.setDividerLocation( 0.5 );
			this.jPanelInputMsg.setOrientation( JSplitPane.VERTICAL_SPLIT );

			this.jPanelInputMsg.setBackground(Color.WHITE );
			this.jPanelInputMsg.setFocusable(false);
			this.jPanelInputMsg.setFocusCycleRoot(false);

			this.jPanelInputMsg.setLeftComponent( this.getLeftPanelSetting() );
			this.jPanelInputMsg.setRightComponent( this.getInputLogPanel() ); 
			//this.jPanelInputMsg.setRightComponent( this.getLogTabPane() );
		}
		
		return this.jPanelInputMsg;
	}

	protected SyncSocketSettingPanel getLeftPanelSetting()
	{
		if( this.leftSettingPanel == null )
		{
			this.leftSettingPanel = new SyncSocketSettingPanel( ); //this );
		}

		return this.leftSettingPanel;
	}

	protected RightSettingsPanel getRightPanelSetting() throws Exception
	{
		if( this.rightSettingPanel == null )
		{
			this.rightSettingPanel = new RightSettingsPanel( this );
		}

		return this.rightSettingPanel;
	}

	/**
	 * This method initializes jJMenuBar	
	 * 	
	 * @return javax.swing.JMenuBar	
	 */
	private JMenuBar getJJMenuBar()
	{
		if (this.jJMenuBar == null)
		{
			this.jJMenuBar = new JMenuBar( );

			//this.jJMenuBar.setLayout( new BorderLayout() );

			this.jJMenuBar.add( this.getFileMenu() );
			//this.jJMenuBar.add( this.getStreamLibraryMenu() );
			//this.jJMenuBar.add( this.getLangMenu() );
			//this.jJMenuBar.add( this.getAppStatePanel( this.jJMenuBar.getPreferredSize().height ) );
		
			this.setBackgroundContainer( this.jJMenuBar, Color.LIGHT_GRAY.brighter() );
		}
		return this.jJMenuBar;
	}
	
	/*
	private JMenu getStreamLibraryMenu()
	{
		if( this.menuLibrary == null )
		{
			this.menuLibrary = new JMenu( Language.getLocalCaption( Language.MENU_LIBRARY ) );
			
			Border defaultBorder = this.menuLibrary.getBorder();
			this.menuLibrary.setBorder( BorderFactory.createCompoundBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5 ), defaultBorder ) );
			
			ButtonGroup menuGr = new ButtonGroup();
			
			for( StreamLibrary lib : StreamLibrary.values() )
			{
				JRadioButtonMenuItem libMenu = new JRadioButtonMenuItem( lib + "" );

				libMenu.addActionListener( new ActionListener() 
				{	
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						JMenuItem m = (JMenuItem)e.getSource();

						try
						{
							StreamLibrary lib = StreamLibrary.valueOf( m.getText() );
									
							ConfigApp.setProperty( ConfigApp.STREAM_LIBRARY, lib );
						}
						catch( Exception ex )
						{
							JOptionPane.showMessageDialog( GuiManager.getInstance().getAppUI()
															, ex.getMessage()
															, Language.getLocalCaption( Language.DIALOG_ERROR )
															, JOptionPane.ERROR_MESSAGE );
						}
					}
				});

				if( lib == StreamLibrary.LSL )
				{
					libMenu.setSelected( true );
				}
				
				menuGr.add( libMenu );

				this.menuLibrary.add( libMenu );
			
			}
			
			MenuScroller menuScr = new MenuScroller( this.menuLibrary, 5 );
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.MENU_LIBRARY, this.menuLibrary );
		}
		return this.menuLibrary;
	}
	*/
	
	private void setBackgroundContainer( Container cont, Color bg )
	{
		if( cont != null )
		{
			for( Component c : cont.getComponents() )
			{
				c.setBackground( bg );

				if( c instanceof Container )
				{
					this.setBackgroundContainer( (Container)c, bg );
				}
			}
		}
	}

	private JMenu getLangMenu()
	{
		if( this.jLangMenu == null )
		{
			this.jLangMenu = new JMenu( Language.getLocalCaption( Language.LANGUAGE_TEXT ) );

			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.LANGUAGE_TEXT, this.jLangMenu );
			
			ButtonGroup menuGr = new ButtonGroup();
			
			for( String lang : Language.getAvaibleLanguages() )
			{
				JRadioButtonMenuItem langMenu = new JRadioButtonMenuItem( lang );

				langMenu.addActionListener( new ActionListener() 
				{	
					@Override
					public void actionPerformed(ActionEvent e) 
					{
						JMenuItem m = (JMenuItem)e.getSource();

						if( !m.getText().toLowerCase().equals( Language.getCurrentLanguage().toLowerCase() ) )
						{
							GuiTextManager.changeLanguage( m.getText() );
						}
					}
				});

				if( langMenu.getText().toLowerCase().equals( Language.getCurrentLanguage().toLowerCase() ) )
				{
					langMenu.setSelected( true );
				}
				
				menuGr.add( langMenu );

				this.jLangMenu.add( langMenu );
			}
		}

		return this.jLangMenu;
	}
	
	private JMenu getThemeMenu()
	{
		if( this.jThemeMenu == null )
		{
			this.jThemeMenu = new JMenu( Language.getLocalCaption( Language.MENU_THEME ) );

			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.MENU_THEME, this.jThemeMenu );
			
			ButtonGroup menuGr = new ButtonGroup();
			
			List< LookAndFeelInfo > lfs = new ArrayList<UIManager.LookAndFeelInfo>();
			
			try
			{
				lfs.add( new LookAndFeelInfo( "FlatLaf Light", FlatLightLaf.class.getName() ) );
				lfs.add( new LookAndFeelInfo( "FlatLaf Dark", FlatDarkLaf.class.getName() ) );
				lfs.add( new LookAndFeelInfo( "FlatLaf Darcula", FlatDarculaLaf.class.getName() ) );
				lfs.add( new LookAndFeelInfo( "FlatLaf IntelliJ", FlatIntelliJLaf.class.getName() ) );				
				lfs.add( new LookAndFeelInfo( "FlatLaf macOS Light", FlatMacLightLaf.class.getName() ) );
				lfs.add( new LookAndFeelInfo( "FlatLaf macOS Dark", FlatMacDarkLaf.class.getName() ) );
			}
			catch (Exception e) 
			{
			}
			
			LookAndFeelInfo[] installedLFs = UIManager.getInstalledLookAndFeels();
			if( installedLFs != null )
			{
				for( LookAndFeelInfo lfi : installedLFs )
				{
					lfs.add( lfi );
				}
			}
			
			LookAndFeel currentLF = UIManager.getLookAndFeel();

			for( LookAndFeelInfo lf : lfs )
			{
				String LFinfo = lf.getName();
				JRadioButtonMenuItem lfMenu = new JRadioButtonMenuItem( LFinfo );

				lfMenu.addActionListener( new ActionListener() 
				{	
					@Override
					public void actionPerformed(ActionEvent e) 
					{	
						try
						{
							String idLF = lf.getClassName();

							GuiManager.getInstance().changeTheme( idLF );
							
						}
						catch (Exception ex) 
						{
							ex.printStackTrace();
						}
					}
				});

				if( currentLF != null && lfMenu.getText().toLowerCase().equals( currentLF.getName().toLowerCase() ) )
				{
					lfMenu.setSelected( true );
				}

				menuGr.add( lfMenu );

				this.jThemeMenu.add( lfMenu );
			}
		}

		return this.jThemeMenu;
	}
	
	private JMenuItem getAdvanceOptionMenu()
	{
		if( this.menuAdvanceOpt == null )
		{
			this.menuAdvanceOpt = new JMenuItem( Language.getLocalCaption( Language.MENU_ADVANCED ) );
			
			this.menuAdvanceOpt.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent arg0) 
				{	
					GuiManager.getInstance().getAdvanceMenu();
				}
			});

			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.MENU_ADVANCED, this.menuAdvanceOpt );
			
		}

		return this.menuAdvanceOpt;
	}
	
	protected JMenu getFileMenu()
	{
		if( this.jFileMenu == null )
		{
			this.jFileMenu = new JMenu( Language.getLocalCaption( Language.MENU_FILE ) );

			this.jFileMenu.setMnemonic( KeyEvent.VK_F );

			this.jFileMenu.add( this.getMenuLoad() );
			this.jFileMenu.add( this.getMenuSave() );
			this.jFileMenu.add( new JSeparator( JSeparator.HORIZONTAL ) );
			this.jFileMenu.add( this.getMenuConvertBinary() );
			this.jFileMenu.add( this.getClisMenu() );
			this.jFileMenu.add( this.getMenuWritingTest() );
			//this.jFileMenu.add( this.getShowLogMenu() );
			this.jFileMenu.add( new JSeparator( JSeparator.HORIZONTAL ) );
			this.jFileMenu.add( this.getPreferenceMenu() );
			this.jFileMenu.add( new JSeparator( JSeparator.HORIZONTAL ) );
			this.jFileMenu.add( this.getJMenuAbout() );
			this.jFileMenu.add( this.getJMenuGNUGPL() );
			this.jFileMenu.add( new JSeparator( JSeparator.HORIZONTAL ) );
			this.jFileMenu.add( this.getMenuExit() );
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.MENU_FILE, this.jFileMenu );
		}

		return this.jFileMenu;
	}

	protected JMenu getPreferenceMenu()
	{
		if( this.menuPreference == null )
		{
			this.menuPreference = new JMenu( Language.getLocalCaption( Language.MENU_PREFERENCE ) );
			this.menuPreference.setIcon( new ImageIcon( GeneralAppIcon.Config2( Color.BLACK ).getScaledInstance( 16, 16, BufferedImage.SCALE_SMOOTH ) ) );
			
			MenuScroller menuScr = new MenuScroller( this.getLangMenu(), 5 );
			this.menuPreference.add( this.getLangMenu() );
			this.menuPreference.add( this.getThemeMenu() );
			this.menuPreference.add( this.getAdvanceOptionMenu() );
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.MENU_PREFERENCE, this.menuPreference );	
		}
		
		return this.menuPreference;
	}
	
	protected JMenu getClisMenu()
	{
		if( this.menuClis == null )
		{
			this.menuClis = new JMenu( DataFileFormat.CLIS );
			this.menuClis.setIcon( new ImageIcon( GeneralAppIcon.Config2( Color.BLACK ).getScaledInstance( 16, 16, BufferedImage.SCALE_SMOOTH ) ) );
						
			this.menuClis.add( this.getMenuClisTo() );
			this.menuClis.add( this.getMenuClisDataPlot() );				
		}
		
		return this.menuClis;
	}
	
	private JMenuItem getMenuClisDataPlot()
	{
		if( this.menuClisDataPlot == null )
		{
			this.menuClisDataPlot = new JMenuItem( Language.getLocalCaption( Language.SETTING_LSL_PLOT ) );
			this.menuClisDataPlot.setIcon( GeneralAppIcon.ImageIcon( 16, Color.BLACK ) );
			
			this.menuClisDataPlot.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JMenuItem m = (JMenuItem)e.getSource();
					
					GuiManager.getInstance().showClisDataPlotDialog( m.getText() );
				}
			});
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.SETTING_LSL_PLOT, this.menuClisDataPlot );	
		}
		
		return this.menuClisDataPlot;
	}
	
	/*
	private JMenuItem getShowLogMenu()
	{
		if( this.menuShowLog == null )
		{
			this.menuShowLog = new JMenuItem( Language.getLocalCaption( Language.MENU_SHOW_LOG ) );
			this.menuShowLog.setIcon( GeneralAppIcon.NewFile( 16, Color.BLACK ) );
			
			this.menuShowLog.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					ExceptionDialog.showDialog();
				}
			});
						
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.MENU_SHOW_LOG, this.menuShowLog );	
		}
		
		return this.menuShowLog;
	}
	//*/
	
	private JPanel getAppStatePanel( int maxHeight )
	{
		if( this.jPanelAppState == null )
		{
			this.jPanelAppState = new JPanel( new FlowLayout( FlowLayout.RIGHT, 2, 2 ) );

			JLabel lbstate = new JLabel( Language.getLocalCaption( Language.INFO_STATE_LABEL ) );
			JLabel lbTime = new JLabel( Language.getLocalCaption( Language.INFO_SESSION_TIME_LABEL ) ); 
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.INFO_STATE_LABEL, lbstate );
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.INFO_SESSION_TIME_LABEL, lbTime );

			lbstate.setFont( new Font( Font.DIALOG, Font.PLAIN, 12 ) );
			lbTime.setFont( new Font( Font.DIALOG, Font.PLAIN, 12 ) );
			
			FontMetrics fm = lbstate.getFontMetrics( lbstate.getFont() );

			this.jPanelAppState.setPreferredSize( new Dimension( 60 * 2 + fm.stringWidth( lbstate.getText() ), 0 ) );

			this.jPanelAppState.add( lbstate );
			this.jPanelAppState.add( Box.createRigidArea( new Dimension(2, 0) ) );
			this.jPanelAppState.add( this.getExecutionTextState() );
			this.jPanelAppState.add( this.getTimeState() );
			
			this.jPanelAppState.add( Box.createRigidArea( new Dimension(2, 0) ) );
			this.jPanelAppState.add( lbTime );
			this.jPanelAppState.add( Box.createRigidArea( new Dimension(2, 0) ) );
			this.jPanelAppState.add( this.getSessionTimeTxt() );

			for( Component c : this.jPanelAppState.getComponents() )
			{
				Dimension d = c.getPreferredSize();
				d.height = maxHeight - 3;
				c.setPreferredSize( d );
				c.setSize( d );
			}
			
			Dimension d = this.jPanelAppState.getPreferredSize();
			d.height = maxHeight;
			this.jPanelAppState.setPreferredSize( d );
		}

		return this.jPanelAppState;
	}

	protected JTextField getSessionTimeTxt()
	{
		if( this.sessionTimeText == null )
		{
			this.sessionTimeText = new JTextField();
			
			Font f = this.sessionTimeText.getFont();
			this.sessionTimeText.setFont( new Font( f.getName(), Font.BOLD, f.getSize() ) );
			
			FontMetrics fm = this.sessionTimeText.getFontMetrics( this.sessionTimeText.getFont() );
			
			Dimension d = this.sessionTimeText.getPreferredSize();
			d.width = fm.stringWidth( String.format( "00%02d:%02d:%02d.%03d", 23, 56, 56, 9999 ) + 2);
			
			this.sessionTimeText.setPreferredSize( d );
			this.sessionTimeText.setEditable( false );
			
		}
		
		return this.sessionTimeText;
	}
	
	protected LevelIndicator getExecutionTextState()
	{
		if( this.appTextState == null ) 
		{    
			this.appTextState = new LevelIndicator( );
			this.appTextState.setMinimum( 0 );
			this.appTextState.setMaximum( 100 );
			
			Font f = this.getSessionTimeTxt().getFont();
			this.appTextState.setFont( new Font( f.getName(), Font.BOLD, f.getSize() ) );
			
			this.appTextState.setEditable( false );
			this.appTextState.setPaintedString( true );
			
			this.appTextState.setLevelIndicatorWidth( 0 );
			this.appTextState.setString( "" );
			
			this.appTextState.setOpaque( false );
			
			this.appTextState.setColorLevels( new Color[] { new Color(170, 242, 175), new Color( 255, 255, 255 ) } );
			this.appTextState.setLevels( new int[] { 0 } );
			
			FontMetrics fm = this.appTextState.getFontMetrics( this.appTextState.getFont() );
			
			Dimension d = this.appTextState.getPreferredSize();
			d.width = fm.stringWidth( AppState.State.PREPARING.name() + " (100%)" + 5 );						
			this.appTextState.setPreferredSize( d );			
		}

		return this.appTextState;
	}

	protected JTextField getTimeState()
	{
		if( this.timeState == null ) 
		{
			this.timeState = new JTextField( );

			Font f = this.timeState.getFont();
			this.timeState.setFont( new Font( f.getName(), Font.BOLD, f.getSize() ) );
			
			FontMetrics fm = this.timeState.getFontMetrics( this.timeState.getFont() );
			
			Dimension d = this.timeState.getPreferredSize();			
			d.width = fm.stringWidth( "0023:59:59" + 5);
			
			this.timeState.setPreferredSize( d );
			this.timeState.setEditable( false );
			
		}

		return this.timeState;
	}

	/**
	 * This method initializes jMenu	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	protected JToggleButton getJButtonPlay()
	{
		if (this.jButtomPlayStop == null)
		{					
			String txt = Language.getLocalCaption( Language.ACTION_PLAY ) ;

			this.jButtomPlayStop = new JToggleButton( txt );
			
			this.jButtomPlayStop.setFocusable(false);
			this.jButtomPlayStop.setFocusPainted(false);
			this.jButtomPlayStop.setFocusCycleRoot(false);
			this.jButtomPlayStop.setForeground( null );

			this.jButtomPlayStop.setBackground(this.jJMenuBar.getBackground());

			this.jButtomPlayStop.addItemListener( new ItemListener( )
			{	
				@Override
				public void itemStateChanged(ItemEvent e)  
				{	
					JToggleButton b = (JToggleButton) e.getSource();

					if ( b.isSelected() )
					{						
						GuiManager.getInstance().startRecording( );
					}
					else
					{						
						GuiManager.getInstance().stopRecording();

						jButtomPlayStop.transferFocus();
					}

				}
			});

			this.jButtomPlayStop.setIcon(GuiManager.START_ICO);
			this.jButtomPlayStop.setSelectedIcon(GuiManager.STOP_ICO);
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.ACTION_PLAY, this.jButtomPlayStop );
		}


		return this.jButtomPlayStop;
	}

	protected JMenuItem getMenuLoad()
	{
		if( this.menuLoad == null )
		{
			this.menuLoad = new JMenuItem( Language.getLocalCaption( Language.MENU_LOAD ) );
			this.menuLoad.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_L, KeyEvent.ALT_MASK ));

			try
			{
				this.menuLoad.setIcon( GeneralAppIcon.LoadFile( 16, Color.BLACK ) );
			}
			catch( Exception e )
			{				
			}
			catch( Error e )
			{}

			this.menuLoad.addActionListener(new java.awt.event.ActionListener() 
			{
				public void actionPerformed(java.awt.event.ActionEvent e) 
				{
					//getGlassPane().setVisible( true );					

					GuiManager.getInstance().loadFileConfig();

					//getGlassPane().setVisible( false );

					//loadConfigValues();
				}
			});

			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.MENU_LOAD, this.menuLoad );
		}

		return this.menuLoad;
	}	

	protected JMenuItem getMenuSave()
	{
		if( this.menuSave == null )
		{	
			this.menuSave = new JMenuItem( Language.getLocalCaption( Language.MENU_SAVE ) );
			this.menuSave.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_S, KeyEvent.ALT_MASK ) );

			try
			{
				this.menuSave.setIcon( GeneralAppIcon.SaveFile( 16, Color.BLACK ) );
			}
			catch( Exception e)
			{}
			catch( Error e )
			{}

			this.menuSave.addActionListener(new java.awt.event.ActionListener() 
			{
				public void actionPerformed(java.awt.event.ActionEvent e) 
				{
					GuiManager.getInstance().saveFileConfig();
				}
			});
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.MENU_SAVE, this.menuSave );
		}

		return this.menuSave;
	}

	protected JMenuItem getMenuExit()
	{
		if( this.menuExit == null )
		{
			this.menuExit = new JMenuItem( Language.getLocalCaption( Language.MENU_EXIT ) );
			this.menuExit.setIcon( GeneralAppIcon.Exit( 16, Color.BLACK ) );

			this.menuExit.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					GuiManager.getInstance().closingChecks();
				}
			});
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.MENU_EXIT, this.menuExit );
		}

		return this.menuExit;
	}

	protected JMenuItem getMenuWritingTest()
	{
		if( this.menuWritingTest == null )
		{
			this.menuWritingTest = new JMenuItem( Language.getLocalCaption( Language.MENU_WRITE_TEST ) );
			this.menuWritingTest.setIcon( GeneralAppIcon.Pencil( 16, Color.BLACK ) );			
			
			this.menuWritingTest.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{	
					/*
					DecimalFormat df = new DecimalFormat( "#.##" );
					
					Exception ex = new Exception( "Writing test duration " + df.format( GeneralSettings.WRITING_TEST_TIME / 1000.0D ) + " seconds.\n" );
					ExceptionMessage msg = new ExceptionMessage( ex, Language.getLocalCaption( Language.MENU_WRITE_TEST ), ExceptionMessage.INFO_MESSAGE );
					ExceptionDialog.showMessageDialog( msg, true, false );
					
					//GuiManager.getInstance().startTest( true );
					GuiManager.getInstance().setWriteTest( true );
					getJButtonPlay().setSelected( true );
					//*/
					
					GuiManager.getInstance().setWriteTest( true );
				}
			});
			
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.MENU_WRITE_TEST, this.menuWritingTest );
		}
		
		return this.menuWritingTest;
	}
	
	protected JMenuItem getMenuConvertBinary()
	{
		if( this.menuConvertBinary == null )
		{
			this.menuConvertBinary = new JMenuItem( Language.getLocalCaption( Language.MENU_CONVERT_BIN ) );
			this.menuConvertBinary.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_C, KeyEvent.ALT_MASK ) );
			
			this.menuConvertBinary.setIcon( GeneralAppIcon.Convert( 16, Color.BLACK ) );

			this.menuConvertBinary.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					GuiManager.getInstance().convertBin2CLIS();
				}
			});
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.MENU_CONVERT_BIN, this.menuConvertBinary );
		}

		return this.menuConvertBinary;
	}
	
	protected JMenuItem getMenuClisTo()
	{
		if( this.menuConvertClisTo == null )
		{
			this.menuConvertClisTo = new JMenuItem( Language.getLocalCaption( Language.MENU_CONVERT_CLIS ) );
			this.menuConvertClisTo.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_Z, KeyEvent.ALT_MASK ) );
			
			this.menuConvertClisTo.setIcon( GeneralAppIcon.Convert( 16, Color.BLACK ) );

			this.menuConvertClisTo.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JMenuItem m = (JMenuItem)e.getSource();
					
					GuiManager.getInstance().showConvertClisDialog( m.getText() );
				}
			});
			
			if( DataFileFormat.getSupportedFileFormat().length < 2 )
			{
				this.menuConvertClisTo.setEnabled( false );
				this.menuConvertClisTo.setToolTipText( Language.getLocalCaption( Language.MSG_ENCODER_PLUGIN_NO_FOUND ) );
			}
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.MENU_CONVERT_CLIS, this.menuConvertClisTo );
			GuiTextManager.addComponent( GuiTextManager.TOOLTIP, Language.MSG_ENCODER_PLUGIN_NO_FOUND, this.menuConvertClisTo );
		}

		return this.menuConvertClisTo;
	}
	

	/**
	 * This method initializes jMenuAcercaDe	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	protected JMenuItem getJMenuGNUGPL()
	{
		if (this.jGNUGLP == null)
		{
			this.jGNUGLP = new JMenuItem();
			this.jGNUGLP.setHorizontalTextPosition(2);
			this.jGNUGLP.setText( Language.getLocalCaption( Language.MENU_GNU_GPL ));

			KeyStroke k = KeyStroke.getKeyStroke( KeyEvent.VK_W, InputEvent.ALT_MASK );
			this.jGNUGLP.setAccelerator(k);

			this.jGNUGLP.setFocusable(false);
			this.jGNUGLP.setFocusPainted(false);
			this.jGNUGLP.setFocusCycleRoot(false);
			this.jGNUGLP.setEnabled(true);
			this.jGNUGLP.setForeground(Color.black);
			this.jGNUGLP.setBackground(this.jJMenuBar.getBackground());

			this.jGNUGLP.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (jGNUGLP.isEnabled())
					{
						GuiManager.getInstance().showGNULicenceDialog();
					}
				}

			});
			
			this.jGNUGLP.addMouseListener(new MouseAdapter()
			{

				public void mousePressed(MouseEvent arg0)
				{
					jGNUGLP.setArmed(true);
				}

				public void mouseReleased(MouseEvent e)
				{
					jGNUGLP.setArmed(false);
				}
			});
	
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.MENU_GNU_GPL, this.jGNUGLP );
		}

		return this.jGNUGLP;
	}

	/**
	 * This method initializes jMenuAcercaDe	
	 * 	
	 * @return javax.swing.JMenu	
	 */
	protected JMenuItem getJMenuAbout()
	{
		if (this.jMenuAbout == null)
		{
			this.jMenuAbout = new JMenuItem();
			this.jMenuAbout.setHorizontalTextPosition( SwingConstants.LEFT );
			this.jMenuAbout.setText( Language.getLocalCaption( Language.MENU_ABOUT ) );//+ " " + ConfigApp.shortNameApp);

			KeyStroke k = KeyStroke.getKeyStroke( KeyEvent.VK_H, InputEvent.ALT_MASK );
			this.jMenuAbout.setAccelerator(k);

			this.jMenuAbout.setFocusable(false);
			this.jMenuAbout.setFocusPainted(false);
			this.jMenuAbout.setFocusCycleRoot(false);
			this.jMenuAbout.setEnabled(true);
			this.jMenuAbout.setForeground(Color.black);
			this.jMenuAbout.setBackground(this.jJMenuBar.getBackground());

			this.jMenuAbout.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (jMenuAbout.isEnabled())
					{
						GuiManager.getInstance().showAboutDialog();
					}

				}
			});
			this.jMenuAbout.addMouseListener(new MouseAdapter()
			{

				public void mousePressed(MouseEvent arg0)
				{
					jMenuAbout.setArmed(true);
				}

				public void mouseReleased(MouseEvent e)
				{
					jMenuAbout.setArmed(false);
				}
			});
		
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.MENU_ABOUT, this.jMenuAbout );
		}

		return this.jMenuAbout;
	}

	/*
	private JTabbedPane getLogTabPane()
	{
		if( this.jTabPanelInMsg == null )
		{
			this.jTabPanelInMsg = new JTabbedPane();
			
			this.jTabPanelInMsg.addTab( Language.getLocalCaption( Language.INPUT_MSGS ), this.getInputLogPanel() );
			Component c = this.jTabPanelInMsg.getComponentAt( 0 );
			
			
			this.jTabPanelInMsg.setTabLayoutPolicy( JTabbedPane.SCROLL_TAB_LAYOUT );
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.INPUT_MSGS, c );
		}
		
		return this.jTabPanelInMsg;
	}
	//*/
	
	private JPanel getInputLogPanel()
	{
		if( this.jPanelInputMsgLog == null )
		{
			this.jPanelInputMsgLog = new JPanel( new BorderLayout() );
			this.jPanelInputMsgLog.setBorder( BorderFactory.createTitledBorder( Language.getLocalCaption( Language.INPUT_MSGS ) ) );
			//this.jPanelInputMsgLog.setBorder( BorderFactory.createEmptyBorder( 0, 5, 0, 5) );
			
			this.jPanelInputMsgLog.add( this.getScrollPaneLog(), BorderLayout.CENTER );

			JPanel p = new JPanel( new BorderLayout() ); 
			p.setBorder( BorderFactory.createEmptyBorder( 2, 5, 2, 2));
			p.add( this.getCheckAutoScroll(), BorderLayout.EAST );
			p.add( this.getJButtonClearLog(), BorderLayout.CENTER );

			this.jPanelInputMsgLog.add( p, BorderLayout.NORTH );
			
			GuiTextManager.addComponent( GuiTextManager.BORDER, Language.INPUT_MSGS, this.jPanelInputMsgLog.getBorder() );
		}

		return this.jPanelInputMsgLog;
	}
	
	/*
	private JPanel getAppStateLogPanel()
	{
		if( this.jPanelAppStateLog == null )
		{
			this.jPanelAppStateLog = new JPanel( new BorderLayout() );
			//this.jPanelInputMsgLog.setBorder( BorderFactory.createTitledBorder( Language.getLocalCaption( Language.INPUT_MSGS ) ) );
			
			this.jPanelAppStateLog.add( this.getScrollPaneAppStateLog(), BorderLayout.CENTER );

			JPanel p = new JPanel( new BorderLayout() ); 

			p.add( this.getCheckAutoScrollAppStateLog(), BorderLayout.EAST );
			p.add( this.getJButtonClearAppStateLog(), BorderLayout.CENTER );

			this.jPanelAppStateLog.add( p, BorderLayout.NORTH );
			
			//GuiTextManager.addComponent( GuiTextManager.BORDER, Language.INPUT_MSGS, this.jPanelInputMsgLog.getBorder() );
		}

		return this.jPanelAppStateLog;
	}
	//*/

	private JButton getJButtonClearLog()
	{
		if( jButtonClearLog == null )
		{
			jButtonClearLog = new JButton( Language.getLocalCaption( Language.CLEAR ) );			
			jButtonClearLog.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent arg0) 
				{
					getLogTextArea().setText( "" );
				}
			});
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.CLEAR, this.jButtonClearLog );
		}

		return jButtonClearLog;
	}
	
	/*
	private JButton getJButtonClearAppStateLog()
	{
		if( jButtonClearAppStateLog == null )
		{
			jButtonClearAppStateLog = new JButton( Language.getLocalCaption( Language.CLEAR ) );			
			jButtonClearAppStateLog.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent arg0) 
				{
					getAppStateLogTextArea().setText( "" );
				}
			});
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.CLEAR, this.jButtonClearAppStateLog );
		}

		return jButtonClearAppStateLog;
	}
	//*/
	/*
	private JScrollPane getScrollPaneAppStateLog() 
	{
		if ( this.scrollPaneAppStateLog == null) 
		{
			this.scrollPaneAppStateLog = new JScrollPane();
			this.scrollPaneAppStateLog.setViewportView( this.getAppStateLogTextArea() );
		}
		return this.scrollPaneAppStateLog;
	}
	//*/

	private JScrollPane getScrollPaneLog() 
	{
		if ( this.scrollPaneInputMessage == null) 
		{
			this.scrollPaneInputMessage = new JScrollPane();
			this.scrollPaneInputMessage.setViewportView( this.getLogTextArea() );
		}
		return this.scrollPaneInputMessage;
	}
	
	protected JTextPane getLogTextArea() 
	{
		if ( this.logTextArea == null)
		{
			this.logTextArea = new JTextPane();
			this.logTextArea.setBackground( Color.WHITE );
			this.logTextArea.setForeground( Color.BLACK );
			this.logTextArea.setEditable(false);
			this.logTextArea.setBorder( BorderFactory.createEtchedBorder() );
			this.addPopup( this.logTextArea, getPopupMenu_2() );
		}
		return this.logTextArea;
	}

	/*
	private JTextPane getAppStateLogTextArea() 
	{
		if ( this.appStateLogTextArea == null)
		{
			this.appStateLogTextArea = new JTextPane();
			this.appStateLogTextArea.setEditable(false);
			this.appStateLogTextArea.setBorder( BorderFactory.createEtchedBorder() );
			this.addPopup( this.appStateLogTextArea, getPopupMenu_2() );
		}
		return this.appStateLogTextArea;
	}
	//*/
	
	private void addPopup( Component component, final JPopupMenu popup ) 
	{
		component.addMouseListener(new MouseAdapter() 
		{
			public void mousePressed( MouseEvent e ) 
			{
				if ( e.isPopupTrigger() ) 
				{
					showMenu( e );
				}
			}

			public void mouseReleased( MouseEvent e ) 
			{

				if ( e.isPopupTrigger() ) 
				{
					showMenu( e );
				}
			}

			private void showMenu( MouseEvent e ) 
			{
				e.getComponent().requestFocus();
				popup.show( e.getComponent(), e.getX(), e.getY() );
			}
		});
	}

	private JPopupMenu getPopupMenu_2() 
	{
		if ( this.popupMenu_2 == null) 
		{
			this.popupMenu_2 = new JPopupMenu();
			this.popupMenu_2.add( this.getMntmCopy_2() );
			this.popupMenu_2.add( this.getMntmCopyall() );
			this.popupMenu_2.add( this.getMntmClear() );
		}

		return this.popupMenu_2;
	}

	private JMenuItem getMntmCopy_2() 
	{
		if ( this.mntmCopy == null) 
		{
			this.mntmCopy = new JMenuItem( new DefaultEditorKit.CopyAction() );
			this.mntmCopy.setText( Language.getLocalCaption( Language.COPY ) );
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.COPY, this.mntmCopy );
			//mntmCopy_2.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
		}

		return this.mntmCopy;
	}

	private JMenuItem getMntmCopyall() 
	{
		if ( this.mntmCopyall == null ) 
		{	
			this.mntmCopyall = new JMenuItem( Language.getLocalCaption( Language.COPY_ALL ) );
			this.mntmCopyall.addActionListener(new ActionListener()
			{
				public void actionPerformed( ActionEvent arg0 ) 
				{
					JMenuItem jm = (JMenuItem)arg0.getSource();
					JTextComponent c = (JTextComponent)( (JPopupMenu)jm.getParent() ).getInvoker();

					Toolkit.getDefaultToolkit().getSystemClipboard().setContents( new StringSelection( c.getText() ), null );
				}
			});			
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.COPY_ALL, this.mntmCopyall );
		}

		return this.mntmCopyall;
	}

	private JMenuItem getMntmClear() 
	{
		if ( this.mntmClear == null) 
		{
			this.mntmClear = new JMenuItem( Language.getLocalCaption( Language.CLEAR ) );
			//mntmClear.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, InputEvent.CTRL_MASK));

			this.mntmClear.addActionListener(new ActionListener() 
			{
				public void actionPerformed( ActionEvent arg0 ) 
				{
					JMenuItem jm = (JMenuItem)arg0.getSource();
					JTextComponent c = (JTextComponent)( (JPopupMenu)jm.getParent() ).getInvoker();

					c.setText( "" );
				}
			});
			
			GuiTextManager.addComponent( GuiTextManager.TEXT, Language.CLEAR, this.mntmClear );
		}

		return this.mntmClear;
	}

	protected JCheckBox getCheckAutoScroll()
	{
		if( this.checkAutoScroll == null )
		{
			this.checkAutoScroll = new JCheckBox(  Language.getLocalCaption( Language.AUTOSCROLL ) );
			this.checkAutoScroll.setSelected( true );				
		}

		return this.checkAutoScroll;
	}
	
	private JButton getJBtChecklist()
	{
		if( this.jBtnChecklist == null )
		{
			this.jBtnChecklist = new JButton( Language.getLocalCaption( Language.CHECKLIST_TEXT ) );
			
			this.jBtnChecklist.addActionListener( new ActionListener()
			{	
				@Override
				public void actionPerformed(ActionEvent e)
				{
					GuiManager.getInstance().showChecklistDialog();
				}
			});
		}
		
		return this.jBtnChecklist;
	}
	
	/*
	private JCheckBox getCheckAutoScrollAppStateLog()
	{
		if( this.checkAutoScrollAppStateLog == null )
		{
			this.checkAutoScrollAppStateLog = new JCheckBox(  Language.getLocalCaption( Language.AUTOSCROLL ) );
			this.checkAutoScrollAppStateLog.setSelected( true );				
		}

		return this.checkAutoScrollAppStateLog;
	}
	//*/
}