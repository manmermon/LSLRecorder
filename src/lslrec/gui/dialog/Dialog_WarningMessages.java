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
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;

import lslrec.auxiliar.thread.timer.ActionTimerThread;
import lslrec.auxiliar.thread.timer.IAction;
import lslrec.auxiliar.thread.timer.Timer;
import lslrec.config.ConfigApp;
import lslrec.config.language.Language;
import lslrec.gui.KeyActions;
import lslrec.stoppableThread.IStoppableThread;

public class Dialog_WarningMessages extends JDialog 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int OPTION_NO_SELECTED = -2;
	public static final int OPTION_CANCEL = -1;
	public static final int OPTION_OK = 0;
	
	private JPanel contentPanel;
	private JPanel panelButtons;
	private JPanel panelChecklist;
	
	private JScrollPane scrListPanel;
	
	private JButton btOk;
	private JButton btnCancel;
	
	private JLabel jlbCheckcount;
		
	private int selectedOption = OPTION_NO_SELECTED;

	//private AtomicBoolean checkOn = new AtomicBoolean( false );
	private Timer timer = null;
	
	private List< JCheckBox > chbList = new ArrayList< JCheckBox >();
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) 
	{
		try 
		{
			Dialog_WarningMessages dialog = new Dialog_WarningMessages( null, new ArrayList<String>() );
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public Dialog_WarningMessages( Window owner, List< String > messages ) 
	{
		super( owner );
		
		this.setTitle( Language.getLocalCaption( Language.CHECKLIST_TEXT ) );
		
		super.getRootPane().registerKeyboardAction( KeyActions.getEscapeCloseWindows( "EscapeCloseWindow" ), 
												KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0), 
												JComponent.WHEN_IN_FOCUSED_WINDOW );
		
		//super.setSize( new Dimension( 250, 175 ) );
		//super.setResizable( false );
				
		super.setModalityType( JDialog.DEFAULT_MODALITY_TYPE );
		
		super.setBounds(100, 100, 450, 300);
		super.getContentPane().setLayout(new BorderLayout());
		
		super.getContentPane().add( this.getContentPanel(), BorderLayout.CENTER);
		
		super.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
		
		/*
		super.addWindowListener( new WindowAdapter() 
		{
			@Override
			public void windowOpened(WindowEvent e) 
			{
				selectedOption = OPTION_CANCEL;
			}
		});
		//*/
		
		int checklistTimer = (Integer)ConfigApp.getProperty( ConfigApp.CHECKLIST_TIMER );
		
		if( checklistTimer > 0 )
		{
			/*
			this.timer = new Timer( checklistTimer*1000, new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					synchronized( checkOn )
					{
						checkOn.set( true );
					}
				}
			});
			//*/
			
			this.timer = new Timer( checklistTimer * 1000, false, new ActionTimerThread( new IAction() 
			{				
				@Override
				public void execute() 
				{
					enableCheckboxList( true );
				}
			}));
		}
		
		JPanel checklistpanel = this.getChecklistPanel();
		
		checklistpanel.setVisible( false );
		
		for( String msg : messages )
		{
			JCheckBox ch = new JCheckBox( msg );
			
			chbList.add( ch );
			
			/*
			ch.addItemListener( new ItemListener() 
			{				
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					synchronized( checkOn )
					{
						JCheckBox ch = (JCheckBox)e.getSource();
						
						if( checkOn.get() )
						{	
							boolean wasSelected = ( ch.getName() != null );
							
							if( !wasSelected && e.getStateChange() == e.SELECTED )
							{
								ch.setName( "was selected" );
							}
							
							int totalchecks = numberOfSelectedChecks();
							
							if( timer != null && !wasSelected )
							{
								if( totalchecks > 0 )
								{
									checkOn.set( false );

									timer.restart();
								}
								else
								{
									timer.stop();
									timer = null;
								}
							}
						}
						else
						{
							JOptionPane.showMessageDialog( checklistpanel, Language.getLocalCaption( Language.MSG_CHECKLIST_WARNING ) );
						}
					}
				}
			});
			//*/

			ch.addItemListener( new ItemListener() 
			{				
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					enableCheckboxList( false );
					
					boolean wasSelected = ( ch.getName() != null );
					
					if( !wasSelected && e.getStateChange() == e.SELECTED )
					{
						ch.setName( "was selected" );
					}
					
					int totalchecks = numberOfSelectedChecks();
					
					if( wasSelected )
					{
						enableCheckboxList( true );
					}
					else
					{
						if( totalchecks > 0 )
						{
							if( timer != null )
							{
								timer.restartTimer();
							}
							else
							{
								enableCheckboxList( true );
							}
						}
						else
						{
							if( timer != null )
							{
								timer.stopThread( IStoppableThread.FORCE_STOP );
								timer = null;
							}
							
							enableCheckboxList( true );
						}
					}
				}
			});
			
			ch.addFocusListener( new FocusAdapter()
			{
				@Override
				public void focusGained(FocusEvent e) 
				{
					
				}
			});
			
			checklistpanel.add( ch );
		}
		
		this.getJLabelCheckCount().setText( "" + this.chbList.size() );
		this.getBtOk().setEnabled( !( this.chbList.size() > 0 ) );		
		checklistpanel.setVisible( true );		
		
		if( this.timer != null )
		{
			this.timer.restartTimer();
		}
	}
	
	private void enableCheckboxList( boolean ena )
	{
		if( this.chbList != null )
		{
			for( JCheckBox cb : this.chbList )
			{
				cb.setEnabled(ena);
			}
		}
	}
	
	private int numberOfSelectedChecks()
	{
		int totalchecks = this.chbList.size();
		
		boolean setScrollBarPosition = false;
		
		for( JCheckBox ch : this.chbList )
		{
			totalchecks += ( ch.isSelected() ) ? -1 : 0;
			
			if( !ch.isSelected() && !setScrollBarPosition )
			{				 
				Rectangle pos = ch.getBounds();
				
				getChecklistPanel().scrollRectToVisible( pos );
				
				setScrollBarPosition = true;
			}
		}
		
		getJLabelCheckCount().setText( "" + totalchecks );
		
		getBtOk().setEnabled( totalchecks < 1 );
		
		return totalchecks;
	}
	
	
	public int getSelectedOption()
	{
		return this.selectedOption;
	}
	
	private JPanel getContentPanel()
	{
		if( this.contentPanel == null )
		{
			this.contentPanel = new JPanel();
			
			this.contentPanel.setLayout( new BorderLayout() );
			
			this.contentPanel.add( this.getButtonsPanel(), BorderLayout.SOUTH );
			
			//JScrollPane sc = new JScrollPane( this.getChecklistPanel(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
			JScrollPane sc = this.getMessageListScrollPanel();
			sc.setViewportView( this.getChecklistPanel() );
			this.contentPanel.add( sc, BorderLayout.CENTER );
		}
		
		return this.contentPanel;
	}
	
	private JScrollPane getMessageListScrollPanel()
	{
		if( this.scrListPanel == null )
		{
			this.scrListPanel = new JScrollPane( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
		}
		
		return this.scrListPanel;
	}
	
	private JPanel getButtonsPanel()
	{
		if( this.panelButtons == null )
		{
			this.panelButtons = new JPanel( new FlowLayout( FlowLayout.RIGHT, 5, 5 ) );
			
			this.panelButtons.add( new JLabel( Language.getLocalCaption( Language.MSGS_TEXT ) + ":" ) );
			this.panelButtons.add( this.getJLabelCheckCount() );
			this.panelButtons.add( this.getBtOk() );
			this.panelButtons.add( this.getBtCancel() );
		}
		return this.panelButtons;
	}
	
	private JPanel getChecklistPanel()
	{
		if( this.panelChecklist == null )
		{
			this.panelChecklist = new JPanel(  );
			this.panelChecklist.setLayout( new BoxLayout( this.panelChecklist, BoxLayout.Y_AXIS ) );
		}
		return this.panelChecklist;
	}
	
	private JButton getBtOk()
	{
		if( this.btOk == null )
		{
			this.btOk = new JButton( Language.getLocalCaption( Language.OK_TEXT ) );
			
			this.btOk.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e)
				{
					selectedOption = OPTION_OK;
					dispose();
				}
			});
			
		}
		
		return this.btOk;
	}
	
	private JButton getBtCancel()
	{
		if( this.btnCancel == null )
		{
			this.btnCancel = new JButton( Language.getLocalCaption( Language.CANCEL_TEXT ) );
			
			this.btnCancel.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e)
				{
					selectedOption = OPTION_CANCEL;
					dispose();
				}
			});
		}
		
		return this.btnCancel;
	}

	private JLabel getJLabelCheckCount()
	{
		if( this.jlbCheckcount == null )
		{
			this.jlbCheckcount = new JLabel( );
			this.jlbCheckcount.setText( this.chbList.size() + "" );
		}
		
		return this.jlbCheckcount;
	}
}
