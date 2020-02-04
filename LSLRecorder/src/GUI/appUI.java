/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018-2019 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.
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

package GUI;

import Config.ConfigApp;
import Config.Language.Language;
import Controls.coreControl;
import Controls.Messages.AppState;
import Controls.Messages.RegisterSyncMessages;
import Exceptions.Handler.ExceptionDialog;
import Exceptions.Handler.ExceptionDictionary;
import Exceptions.Handler.ExceptionMessage;
import GUI.Miscellany.DisabledGlassPane;
import GUI.Miscellany.GeneralAppIcon;
import GUI.Miscellany.InfoDialog;
import GUI.Miscellany.MenuScroller;
import GUI.Miscellany.LevelIndicator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Insets;
import java.awt.Point;
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
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

public class appUI extends JFrame
{
	private static appUI ui = null;

	private static final long serialVersionUID = 1L;

	// panel	
	private JPanel jContentPane = null;	
	private JPanel jPanelAppState = null;
	private JPanel jPanelInputMsgLog;
	private JPanel jPanelSelectSyncMethod;
	//private JPanel jPanelMenus;

	// Button
	private JButton jButtonClearLog;
	private JButton btnRefreshDevices;
	private JButton jBtnInfo;
	private JButton jButtomPlayStop = null;

	// ScrollPanel
	private JScrollPane scrollPane_2;
	private JScrollPane scrollPanelCtrl;

	// TextPanel
	private JTextPane logTextArea;

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

	// menuItem	
	private JMenuItem jMenuAbout = null;
	private JMenuItem jGNUGLP = null;
	private JMenu menuPreference = null;
	private JMenuItem menuLoad = null;
	private JMenuItem menuSave = null;
	private JMenuItem menuBin2Clis = null;
	private JMenuItem menuWritingTest = null;
	private JMenuItem menuExit = null;
	private JMenuItem menuShowLog = null;

	// Processbar
	private LevelIndicator appTextState = null;
	
	// textField
	//private JTextField appTextState = null;
	private JTextField timeState = null;
	private JTextField sessionTimeText = null;

	// Settings
	private settingMenu_SocketSetting SocketSetting;
	private settingMenu_labStreamingLayer lslSetting;

	// JCombox
	private JComboBox< String > jComboxSyncMethod;

	// CheckBox
	private JCheckBox checkActiveSpecialInputMsg;
	private JCheckBox checkAutoScroll;

	//Map
	private Map< String, Component > parameters;


	private appUI() 
	{
		this.parameters = new HashMap<String, Component>();

		initialize();
	}

	public static appUI getInstance()
	{
		if (ui == null)
		{
			ui = new appUI();
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
		
		GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.GENERAL_WAIT_MSG, glass );

		super.setJMenuBar( getJJMenuBar() );
		super.setContentPane( getJContentPane() );

		super.setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );

		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{				
				closingChecks();
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
		actionMap.put( idAct, keyActions.getButtonClickAction( idAct, this.getJButtonPlay() ) );

		idAct = "actionRefresh";
		inputMap.put( KeyStroke.getKeyStroke( KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK ), idAct );		 
		actionMap.put( idAct, keyActions.getButtonClickAction( idAct, this.getJButtonRefreshDevice() ) );
	}

	private void closingChecks()
	{
		try
		{					 
			if(  !getGlassPane().isVisible( ) )
			{
				if( coreControl.getInstance().isDoingSomething() )
				{						 
					String[] opts = { UIManager.getString( "OptionPane.yesButtonText" ), 
							UIManager.getString( "OptionPane.noButtonText" ) };

					int actionDialog = JOptionPane.showOptionDialog( ui, Language.getLocalCaption( Language.MSG_APP_STATE )
							//+ " " + getExecutionTextState().getText() + "."
							+ " " + getExecutionTextState().getString() + "."
							+ "\n" + Language.getLocalCaption( Language.MSG_INTERRUPT ) 
							+ "?", 
							Language.getLocalCaption( Language.MSG_WARNING )
							, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, 
							null, opts, opts[1]);

					if ( actionDialog == JOptionPane.YES_OPTION )
					{								 
						if( coreControl.getInstance().isRecording() )
						{
							coreControl.getInstance().stopWorking( );
						}

						getGlassPane().setVisible( true );

						coreControl.getInstance().closeWhenDoingNothing( );
					}
				}
				else
				{
					System.exit( 0 );
				}
			}
			else if( coreControl.getInstance().isClosing() )
			{
				String[] opts = { Language.getLocalCaption( Language.FORCE_QUIT ),
						Language.getLocalCaption( Language.WAIT ) };

				int actionDialog = JOptionPane.showOptionDialog( ui, Language.getLocalCaption( Language.TOO_MUCH_TIME ), 
						Language.getLocalCaption( Language.MSG_WARNING )
						, JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, 
						null, opts, opts[1]);

				if ( actionDialog == JOptionPane.YES_OPTION )
				{								 
					System.exit( 0 );
				}
			}
		}
		catch (Exception e1)
		{
			e1.printStackTrace();
			String msg = e1.getMessage();
			if ((msg == null) || (msg.isEmpty()))
			{
				msg = "" + e1.getCause();
			}

			JOptionPane.showMessageDialog( appUI.ui, msg, Language.getLocalCaption( Language.DIALOG_ERROR ), JOptionPane.ERROR_MESSAGE  );
		}				 
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

			this.jPanelSelectSyncMethod.add( this.getJButtonRefreshDevice() );

			JLabel lb = new JLabel( Language.getLocalCaption( Language.SETTING_SYNC_METHOD ) );
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.SETTING_SYNC_METHOD, lb );
			
			this.jPanelSelectSyncMethod.add( lb );
			this.jPanelSelectSyncMethod.add( this.getJComboxSyncMethod() );
			this.jPanelSelectSyncMethod.add( this.getJCheckActiveSpecialInputMsg() );
			this.jPanelSelectSyncMethod.add( this.getJButtonInfo() );			
		}

		return this.jPanelSelectSyncMethod;
	}

	protected JComboBox< String > getJComboxSyncMethod()	
	{
		if( this.jComboxSyncMethod == null )
		{
			final String ID = ConfigApp.SELECTED_SYNC_METHOD;

			this.jComboxSyncMethod = new JComboBox< String >( new String[] { ConfigApp.SYNC_NONE
					, ConfigApp.SYNC_SOCKET
					, ConfigApp.SYNC_LSL } );

			this.jComboxSyncMethod.setSelectedItem( ConfigApp.getProperty( ID ) );

			this.jComboxSyncMethod.addItemListener( new ItemListener() 
			{				
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					JComboBox< String > jc = ( JComboBox< String >)e.getSource();

					String sync = (String)jc.getSelectedItem();

					getJCheckActiveSpecialInputMsg().setEnabled( true );
					
					if( sync.equalsIgnoreCase( ConfigApp.SYNC_NONE ) )
					{
						getJCheckActiveSpecialInputMsg().setSelected( false );
						getJCheckActiveSpecialInputMsg().setEnabled( false );
					}
					
					if( !sync.equalsIgnoreCase( ConfigApp.SYNC_LSL ) )
					{
						try 
						{
							getLSLSetting().deselectSyncDevices();
						}
						catch (Exception e1) 
						{
						}
					}
					
					ConfigApp.setProperty( ID, sync );
				}
			});

			this.parameters.put( ID, this.jComboxSyncMethod );			
		}

		return this.jComboxSyncMethod;
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
					
					if( e.getStateChange() == ItemEvent.SELECTED )
					{
						if( !c.isEnabled() )
						{
							c.setSelected( false );
						}					
					}					
					
					ConfigApp.setProperty( ID, c.isSelected() );
				}
			});

			if( this.getJComboxSyncMethod().getSelectedItem().toString().equalsIgnoreCase( ConfigApp.SYNC_NONE ) )
			{
				this.checkActiveSpecialInputMsg.setSelected( false );;
				this.checkActiveSpecialInputMsg.setEnabled( false );
			}
			
			this.parameters.put( ID, this.checkActiveSpecialInputMsg );
						
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.SETTING_SPECIAL_IN_METHOD, this.checkActiveSpecialInputMsg );

		}

		return this.checkActiveSpecialInputMsg;
	}

	protected JButton getJButtonInfo()
	{
		if( this.jBtnInfo == null )
		{
			Dimension d = new Dimension( 16, 16 );

			this.jBtnInfo = new JButton( );			

			this.jBtnInfo.setText( "?" );			
			this.jBtnInfo.setBorder( BorderFactory.createLineBorder( Color.BLACK ) );
			this.jBtnInfo.setBackground( Color.YELLOW.darker() );
			this.jBtnInfo.setForeground( Color.BLACK );

			this.jBtnInfo.setPreferredSize( d );

			this.jBtnInfo.addActionListener( new ActionListener()
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					JButton b = (JButton)e.getSource();

					InfoDialog w = new InfoDialog( ui, getSpecialInputMessages() );

					w.setSize( 350, 110 );
					Dimension size = w.getSize();
					Point pos = b.getLocationOnScreen();

					Point loc = new Point( pos.x - size.width, pos.y ); 

					w.setLocation( loc );

					w.setVisible( true );
				}
			});
		}

		return this.jBtnInfo;
	}

	private String getSpecialInputMessages()
	{
		Map< String, String > legends = RegisterSyncMessages.getInputSpecialMessageLengeds( true );

		String text = Language.getLocalCaption( Language.SETTING_SPECIAL_IN_METHOD_LEGEND) + ":\n";

		for( String cm : legends.keySet() )
		{
			Integer mark = RegisterSyncMessages.getSyncMark( cm );

			String lg = legends.get( cm );				
			text += "    " + "(" + mark + ", " + cm.toLowerCase() + "): ";
			text += lg + "\n";
		}

		return text;
	}

	protected JButton getJButtonRefreshDevice()
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
					try
					{
						final JButton bt = (JButton)e.getSource();
						
						bt.setEnabled( false );
						
						appUI.this.getJButtonPlay().setEnabled( false );
						
						Thread t = new Thread()
						{
							public void run() 
							{
								try 
								{	
									getLSLSetting().refreshLSLStreamings();
									
									bt.setEnabled( true );
									
									appUI.this.getJButtonPlay().setEnabled( true );
								} 
								catch (Exception e) 
								{
								}
							} 
						};		
						
						t.start();
					}					
					catch( Exception ex )
					{						
					}			
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
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.SETTING_LSL_REFRESH, this.btnRefreshDevices );
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
				this.jPanelOper.setRightComponent( this.getLSLSetting() );
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
				
				//JOptionPane.showMessageDialog( this, e.getMessage() + "\n" + e.getCause(), "LSL Exception", JOptionPane.ERROR_MESSAGE );
				ExceptionMessage msg = new  ExceptionMessage( e
																, "LSL Exception" 
																, ExceptionDictionary.ERROR_MESSAGE );
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

			this.jPanelInputMsg.setLeftComponent( this.getSocketSetting() );
			this.jPanelInputMsg.setRightComponent( this.getInputLogPanel() ); 
		}
		
		return this.jPanelInputMsg;
	}

	protected settingMenu_SocketSetting getSocketSetting()
	{
		if( this.SocketSetting == null )
		{
			this.SocketSetting = new settingMenu_SocketSetting( this );
		}

		return this.SocketSetting;
	}

	protected settingMenu_labStreamingLayer getLSLSetting() throws Exception
	{
		if( this.lslSetting == null )
		{
			this.lslSetting = new settingMenu_labStreamingLayer( this );
		}

		return this.lslSetting;
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
			//this.jJMenuBar.add( this.getLangMenu() );
			this.jJMenuBar.add( this.getAppStatePanel( this.jJMenuBar.getPreferredSize().height ) );

			this.setBackgroundContainer( this.jJMenuBar,Color.LIGHT_GRAY.brighter() );
		}
		return this.jJMenuBar;
	}

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

			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.LANGUAGE_TEXT, this.jLangMenu );
			
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
							GuiLanguageManager.changeLanguage( m.getText() );
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

	protected JMenu getFileMenu()
	{
		if( this.jFileMenu == null )
		{
			this.jFileMenu = new JMenu( Language.getLocalCaption( Language.MENU_FILE ) );

			this.jFileMenu.setMnemonic( KeyEvent.VK_F );

			this.jFileMenu.add( this.getMenuLoad() );
			this.jFileMenu.add( this.getMenuSave() );
			this.jFileMenu.add( new JSeparator( JSeparator.HORIZONTAL ) );
			this.jFileMenu.add( this.getMenuBin2Clis() );
			this.jFileMenu.add( this.getMenuWritingTest() );
			this.jFileMenu.add( this.getShowLogMenu() );
			this.jFileMenu.add( new JSeparator( JSeparator.HORIZONTAL ) );
			this.jFileMenu.add( this.getPreferenceMenu() );
			this.jFileMenu.add( new JSeparator( JSeparator.HORIZONTAL ) );
			this.jFileMenu.add( this.getJMenuAbout() );
			this.jFileMenu.add( this.getJMenuGNUGPL() );
			this.jFileMenu.add( new JSeparator( JSeparator.HORIZONTAL ) );
			this.jFileMenu.add( this.getMenuExit() );
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.MENU_FILE, this.jFileMenu );
		}

		return this.jFileMenu;
	}

	private JMenu getPreferenceMenu()
	{
		if( this.menuPreference == null )
		{
			this.menuPreference = new JMenu( Language.getLocalCaption( Language.MENU_PREFERENCE ) );
			this.menuPreference.setIcon( new ImageIcon( GeneralAppIcon.Config2( Color.BLACK ).getScaledInstance( 16, 16, BufferedImage.SCALE_SMOOTH ) ) );
			
			MenuScroller menuScr = new MenuScroller( this.getLangMenu(), 5 );
			this.menuPreference.add( this.getLangMenu() );
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.MENU_PREFERENCE, this.menuPreference );	
		}
		
		return this.menuPreference;
	}
	
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
						
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.MENU_SHOW_LOG, this.menuShowLog );	
		}
		
		return this.menuShowLog;
	}
	
	private JPanel getAppStatePanel( int maxHeight )
	{
		if( this.jPanelAppState == null )
		{
			this.jPanelAppState = new JPanel( new FlowLayout( FlowLayout.RIGHT, 1, 0 ) );

			JLabel lbstate = new JLabel( Language.getLocalCaption( Language.INFO_STATE_LABEL ) );
			JLabel lbTime = new JLabel( Language.getLocalCaption( Language.INFO_SESSION_TIME_LABEL ) ); 
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.INFO_STATE_LABEL, lbstate );
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.INFO_SESSION_TIME_LABEL, lbTime );

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
			d.width = fm.stringWidth( String.format( "%02d:%02d:%02d.%03d", 23, 56, 56, 9999 ) + 2);
			
			this.sessionTimeText.setPreferredSize( d );
			this.sessionTimeText.setEditable( false );
			
		}
		
		return this.sessionTimeText;
	}
	
	/*
	protected JTextField getTextState()
	{
		if( this.appTextState == null ) 
		{
			this.appTextState = new JTextField( );

			//this.appTextState.setBorder( BorderFactory.createEmptyBorder() );	
			Font f = this.appTextState.getFont();
			this.appTextState.setFont( new Font( f.getName(), Font.BOLD, f.getSize() ) );
			
			FontMetrics fm = this.appTextState.getFontMetrics( this.appTextState.getFont() );
			
			Dimension d = this.appTextState.getPreferredSize();
			d.width = fm.stringWidth( AppState.PREPARING + 5 );
			this.appTextState.setPreferredSize( d );
			this.appTextState.setEditable( false );
			
		}

		return this.appTextState;
	}
	*/
	
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
			d.width = fm.stringWidth( AppState.SAVING + " (100%)" + 5 );						
			this.appTextState.setPreferredSize( d );			
		}

		return this.appTextState;
	}

	protected JTextField getTimeState()
	{
		if( this.timeState == null ) 
		{
			this.timeState = new JTextField( );

			//this.appTextState.setBorder( BorderFactory.createEmptyBorder() );
			Font f = this.timeState.getFont();
			this.timeState.setFont( new Font( f.getName(), Font.BOLD, f.getSize() ) );
			
			FontMetrics fm = this.timeState.getFontMetrics( this.timeState.getFont() );
			
			Dimension d = this.timeState.getPreferredSize();			
			d.width = fm.stringWidth( "23:59:59" + 5);
			
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
	protected JButton getJButtonPlay()
	{
		if (this.jButtomPlayStop == null)
		{					
			String txt = Language.getLocalCaption( Language.ACTION_PLAY ) ;

			this.jButtomPlayStop = new JButton( txt );
			
			this.jButtomPlayStop.setFocusable(false);
			this.jButtomPlayStop.setFocusPainted(false);
			this.jButtomPlayStop.setFocusCycleRoot(false);
			this.jButtomPlayStop.setForeground(null);

			this.jButtomPlayStop.setBackground(this.jJMenuBar.getBackground());

			this.jButtomPlayStop.addActionListener( new ActionListener() 
			{		
				@Override
				public void actionPerformed(ActionEvent arg0) 
				{
					JButton b = (JButton) arg0.getSource();

					String actText = Language.getLocalCaption( Language.ACTION_PLAY );

					if ( b.getText().equals( actText ) )
					{
						b.setText(Language.getCaption( Language.ACTION_STOP, ConfigApp.getProperty( ConfigApp.LANGUAGE ).toString()));
						Icon aux = b.getIcon();
						b.setIcon( jButtomPlayStop.getSelectedIcon());
						b.setSelectedIcon(aux);

						guiManager.getInstance().startTest( false );

					}
					else
					{
						b.setText( Language.getLocalCaption( Language.ACTION_PLAY ) );
						Icon aux = b.getSelectedIcon();
						b.setSelectedIcon(b.getIcon());
						b.setIcon(aux);

						guiManager.getInstance().stopTest();

						jButtomPlayStop.transferFocus();
					}

				}
			});

			/*
			this.jMenuPlayStop.addMouseListener(new MouseAdapter()
			{

				public void mousePressed(MouseEvent arg0)
				{
					jMenuPlayStop.setArmed(true);
				}

				public void mouseReleased(MouseEvent e)
				{
					jMenuPlayStop.setArmed(false);
				}
			});
			 */

			this.jButtomPlayStop.setIcon(guiManager.START_ICO);
			this.jButtomPlayStop.setSelectedIcon(guiManager.STOP_ICO);
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.ACTION_PLAY, this.jButtomPlayStop );
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
					getGlassPane().setVisible( true );					

					guiManager.getInstance().loadFileConfig();

					getGlassPane().setVisible( false );

					loadConfigValues();
				}
			});

			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.MENU_LOAD, this.menuLoad );
		}

		return this.menuLoad;
	}

	public void loadConfigValues()
	{	
		Set< String > IDs = this.parameters.keySet();

		for( String id : IDs )
		{
			Component c = this.parameters.get( id );
			c.setVisible( false );			

			if( c instanceof JComboBox )
			{
				((JComboBox< String >)c).setSelectedItem( ConfigApp.getProperty( id ) );
			}

			c.setVisible( true );
		}
		
		for( String id : IDs )
		{
			Component c = this.parameters.get( id );
			c.setVisible( false );			

			if( c instanceof JToggleButton )
			{
				((JToggleButton)c).setSelected( (Boolean)ConfigApp.getProperty( id ) );
			}

			c.setVisible( true );
		}

		this.getSocketSetting().loadConfigValues();

		try 
		{
			this.getLSLSetting().loadConfigValues();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
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
					guiManager.getInstance().saveFileConfig();
				}
			});
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.MENU_SAVE, this.menuSave );
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
					closingChecks();
				}
			});
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.MENU_EXIT, this.menuExit );
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
					DecimalFormat df = new DecimalFormat( "#.##" );
					
					//guiManager.getInstance().addInputMessageLog( "Writing test duration " + df.format( ConfigApp.WRITING_TEST_TIME / 1000.0D ) + " seconds.\n");
					Exception ex = new Exception( "Writing test duration " + df.format( ConfigApp.WRITING_TEST_TIME / 1000.0D ) + " seconds.\n" );
					ExceptionMessage msg = new ExceptionMessage( ex, Language.getLocalCaption( Language.MENU_WRITE_TEST ), ExceptionDictionary.INFO_MESSAGE );
					ExceptionDialog.showMessageDialog( msg, true, false );

					guiManager.getInstance().startTest( true );
				}
			});
			
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.MENU_WRITE_TEST, this.menuWritingTest );
		}
		
		return this.menuWritingTest;
	}
	
	protected JMenuItem getMenuBin2Clis()
	{
		if( this.menuBin2Clis == null )
		{
			this.menuBin2Clis = new JMenuItem( Language.getLocalCaption( Language.MENU_CONVERT2 ) );
			this.menuBin2Clis.setAccelerator( KeyStroke.getKeyStroke( KeyEvent.VK_C, KeyEvent.ALT_MASK ) );
			
			this.menuBin2Clis.setIcon( GeneralAppIcon.Convert( 16, Color.BLACK ) );

			this.menuBin2Clis.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					guiManager.getInstance().convertBin2CLIS();
				}
			});
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.MENU_CONVERT2, this.menuBin2Clis );
		}

		return this.menuBin2Clis;
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
						JDialog jDialogGPL;

						try 
						{
							jDialogGPL = new menuWindow_GNUGLPLicence( appUI.getInstance() );


							jDialogGPL.setVisible(false);
							jDialogGPL.pack();
							jDialogGPL.validate();

							Dimension dd = Toolkit.getDefaultToolkit().getScreenSize();
							dd.width /= 4;
							dd.height /= 4;
							jDialogGPL.setSize(dd);

							Point l = appUI.ui.getLocation();
							Dimension d = appUI.ui.getSize();
							Point loc = new Point(l.x + d.width / 2 - dd.width / 2, l.y + d.height / 2 - dd.height / 2);

							Insets ssooPAD = Toolkit.getDefaultToolkit().getScreenInsets(appUI.ui.getGraphicsConfiguration());
							if (loc.x < ssooPAD.left + 1)
							{
								loc.x = (ssooPAD.left + 1);
							}

							if (loc.y < ssooPAD.top + 1)
							{
								loc.y = (ssooPAD.top + 1);
							}

							jDialogGPL.setLocation(loc);

							jDialogGPL.setResizable(true);
							jDialogGPL.setVisible(true);
						} 
						catch (Exception e1) 
						{
							e1.printStackTrace();
						}
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
	
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.MENU_GNU_GPL, this.jGNUGLP );
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
						try
						{
							JDialog jDialogAcercaDe = new menuWindow_AboutApp(appUI.getInstance());
							jDialogAcercaDe.setVisible(false);
							jDialogAcercaDe.pack();
							jDialogAcercaDe.validate();

							Dimension dd = Toolkit.getDefaultToolkit().getScreenSize();
							dd.width /= 4;
							dd.height /= 2;
							jDialogAcercaDe.setSize(dd);

							Point l = appUI.ui.getLocation();
							Dimension d = appUI.ui.getSize();
							Point loc = new Point(l.x + d.width / 2 - dd.width / 2, l.y + d.height / 2 - dd.height / 2);

							Insets ssooPAD = Toolkit.getDefaultToolkit().getScreenInsets(appUI.ui.getGraphicsConfiguration());
							if (loc.x < ssooPAD.left + 1)
							{
								loc.x = (ssooPAD.left + 1);
							}

							if (loc.y < ssooPAD.top + 1)
							{
								loc.y = (ssooPAD.top + 1);
							}

							jDialogAcercaDe.setLocation(loc);

							jDialogAcercaDe.setResizable(true);
							jDialogAcercaDe.setVisible(true);
						}
						catch (Exception ex) 
						{
						}
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
		
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.MENU_ABOUT, this.jMenuAbout );
		}

		return this.jMenuAbout;
	}

	private JPanel getInputLogPanel()
	{
		if( this.jPanelInputMsgLog == null )
		{
			this.jPanelInputMsgLog = new JPanel( new BorderLayout() );
			this.jPanelInputMsgLog.setBorder( BorderFactory.createTitledBorder( Language.getLocalCaption( Language.INPUT_MSGS ) ) );
			
			this.jPanelInputMsgLog.add( this.getScrollPaneLog(), BorderLayout.CENTER );

			JPanel p = new JPanel( new BorderLayout() ); 

			p.add( this.getCheckAutoScroll(), BorderLayout.EAST );
			p.add( this.getJButtonClearLog(), BorderLayout.CENTER );

			this.jPanelInputMsgLog.add( p, BorderLayout.NORTH );
			
			GuiLanguageManager.addComponent( GuiLanguageManager.BORDER, Language.INPUT_MSGS, this.jPanelInputMsgLog.getBorder() );
		}

		return this.jPanelInputMsgLog;
	}

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
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.CLEAR, this.jButtonClearLog );
		}

		return jButtonClearLog;
	}

	private JScrollPane getScrollPaneLog() 
	{
		if ( this.scrollPane_2 == null) 
		{
			this.scrollPane_2 = new JScrollPane();
			this.scrollPane_2.setViewportView( this.getLogTextArea() );
		}
		return this.scrollPane_2;
	}

	private JTextPane getLogTextArea() 
	{
		if ( this.logTextArea == null)
		{
			this.logTextArea = new JTextPane();
			this.logTextArea.setEditable(false);
			this.logTextArea.setBorder( BorderFactory.createEtchedBorder() );
			this.addPopup( this.logTextArea, getPopupMenu_2() );
		}
		return this.logTextArea;
	}

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
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.COPY, this.mntmCopy );
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
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.COPY_ALL, this.mntmCopyall );
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
			
			GuiLanguageManager.addComponent( GuiLanguageManager.TEXT, Language.CLEAR, this.mntmClear );
		}

		return this.mntmClear;
	}

	protected void appendTextLog( Color c, String s, AttributeSet attr ) 
	{ 		
		JTextPane log = getLogTextArea();

		StyledDocument doc = log.getStyledDocument();

		Color color = c;

		if( c == null )
		{
			color = Color.BLACK;
		}

		StyleContext sc = StyleContext.getDefaultStyleContext(); 

		AttributeSet attrs = attr;
		if( attrs == null )
		{
			attrs = SimpleAttributeSet.EMPTY;
		}

		AttributeSet aset = sc.addAttribute( attrs , StyleConstants.Foreground, color);

		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

		try 
		{	
			int numLine = 0;

			String t = log.getText();

			if( !t.isEmpty() )
			{
				numLine = t.split("\n").length;
			}

			int nl = s.split( "\n" ).length;

			String numTxt = "";

			if( nl + numLine > numLine )
			{
				numTxt += ( numLine + nl );
			}						

			int len = log.getDocument().getLength();
			len = log.getDocument().getLength();
			doc.insertString( len, dateFormat.format( Calendar.getInstance().getTime() ) + " " + Language.getLocalCaption( Language.INPUT_TEXT ) + " " + numTxt + ": ", null );

			len = log.getDocument().getLength();
			doc.insertString( len , s, aset );

			if( this.getCheckAutoScroll().isSelected() )
			{
				log.setCaretPosition( len + s.length() );
			}
		} 
		catch (BadLocationException e) 
		{
			getLogTextArea().setText( getLogTextArea().getText() + s );
		}
	} 

	private JCheckBox getCheckAutoScroll()
	{
		if( this.checkAutoScroll == null )
		{
			this.checkAutoScroll = new JCheckBox(  Language.getLocalCaption( Language.AUTOSCROLL ) );
			this.checkAutoScroll.setSelected( true );				
		}

		return this.checkAutoScroll;
	}
}