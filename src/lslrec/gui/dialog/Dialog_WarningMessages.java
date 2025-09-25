package lslrec.gui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Timer;

import lslrec.config.ConfigApp;
import lslrec.config.language.Language;

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
	
	private JButton btOk;
	private JButton btnCancel;
	
	private JLabel jlbCheckcount;
		
	private int selectedOption = OPTION_NO_SELECTED;

	private int checkCount = 0;
	
	private AtomicBoolean checkOn = new AtomicBoolean( false );
	private Timer timer = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) 
	{
		try {
			Dialog_WarningMessages dialog = new Dialog_WarningMessages( null, new ArrayList<String>() );
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
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
		}
		
		JPanel checklistpanel = this.getChecklistPanel();
		
		checklistpanel.setVisible( false );
		this.checkCount = messages.size();
		for( String msg : messages )
		{
			JCheckBox ch = new JCheckBox( msg );
			ch.addItemListener( new ItemListener() 
			{				
				@Override
				public void itemStateChanged(ItemEvent e) 
				{
					synchronized( checkOn )
					{
						if( checkOn.get() )
						{
							int updateValue = ( e.getStateChange() == ItemEvent.SELECTED  ) ? -1 : 1;
							
							checkCount += updateValue;
							getJLabelCheckCount().setText( "" + checkCount );
							
							getBtOk().setEnabled( checkCount < 1 );
							
							if( timer != null )
							{
								if( checkCount > 0 )
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
			
			checklistpanel.add( ch );
		}
		
		this.getJLabelCheckCount().setText( "" + this.checkCount );
		this.getBtOk().setEnabled( !( this.checkCount > 0 ) );		
		checklistpanel.setVisible( true );		
		
		if( this.timer != null )
		{
			this.timer.start();
		}
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
			
			JScrollPane sc = new JScrollPane( this.getChecklistPanel(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
			this.contentPanel.add( sc, BorderLayout.CENTER );
		}
		
		return this.contentPanel;
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
			this.jlbCheckcount.setText( this.checkCount + "" );
		}
		
		return this.jlbCheckcount;
	}
}
