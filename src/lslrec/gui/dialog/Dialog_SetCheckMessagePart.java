package lslrec.gui.dialog;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import lslrec.config.language.Language;
import lslrec.control.message.checklist.CheckMessage;
import lslrec.control.message.checklist.CheckMessagePartFromText;
import lslrec.control.message.checklist.ICheckMessagePart;

public class Dialog_SetCheckMessagePart extends JDialog 
{
	private static final long serialVersionUID = 316113139809546547L;
	
	private JPanel contentPanel = null;
	private JPanel buttonPanel = null;
	
	private JButton okButton = null;
	//private JButton cancelButton = null;
	
	private JLabel lbEditPart = null;
	private JLabel lbAlternativeMsg = null;
	
	private JEditorPane msgEditor;
	private JTextField partTx;
	private JTextField alternativeTx;
	
	private List< ICheckMessagePart > msgParts;
	private ICheckMessagePart currentPart = null;	
	
	private CheckMessage chMsg = null;

	final String _prefixPartID = "part";
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			CheckMessage msg = new CheckMessage( "numSelectedSyncStreamChecker", CheckMessage.ERROR );
			ICheckMessagePart part = new CheckMessagePartFromText( Language.getLocalCaption( Language.MSG_ERROR_NUMBER_SELECTED_SYNC_STREAMS )
										, "", false );
			part.setIDLangCaption( Language.MSG_ERROR_NUMBER_SELECTED_SYNC_STREAMS );
			msg.addMessagePart( part );
			
			part = new CheckMessagePartFromText( "1", "^\\d+$", true );									
			msg.addMessagePart( part );

			msg.setAlternativeMessage( "alternative" );
			
			Dialog_SetCheckMessagePart dialog = new Dialog_SetCheckMessagePart( msg );
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		} 
	}

	/**
	 * Create the dialog.
	 */
	//public Dialog_SetCheckMessagePart( List< ICheckMessagePart > parts )
	public Dialog_SetCheckMessagePart( CheckMessage msg )
	{
		this.chMsg = msg;
		this.msgParts = msg.getMessagePart();
				
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());		
		
		super.getContentPane().add( this.getContentPanel(), BorderLayout.CENTER);
		super.getContentPane().add( this.getButtomPanel(), BorderLayout.SOUTH);
		
		super.addWindowListener( new WindowAdapter() 
		{
			@Override
			public void windowOpened(WindowEvent e) 
			{
				showMessage();
			}
		});
	}
	
	private void showMessage()
	{
		String tx = "";
		int numParts = -1;
		
		for( ICheckMessagePart part : this.msgParts )
		{
			String p = part.getMessagePart();
			
			if( p.isEmpty() )
			{
 				p = "&nbsp;&nbsp;";
			}
			
			numParts++;			
			if( part.isPartEditable() )
			{				
				p = "<a href='" + this._prefixPartID + numParts +  "'>" + p + "</a>";
			}
			
			tx += p + " ";
		}
		
		tx = tx.trim();
		
		this.getMessageEditorPane().setText( tx );
		
		this.getMessageEditorPane().addHyperlinkListener(e -> 
		{
			if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) 
			{
				 String part = e.getDescription();
				 
				 activeMessagePartEditor( part );
			}
		});
		
		if( this.msgParts.size() == 1 )
		{
			this.activeMessagePartEditor( this._prefixPartID + "0" );
		}
	}
	
	private void activeMessagePartEditor( String part )
	{
		for( int i = 0; i < this.msgParts.size(); i++ )
		 {
			 if( part.equals( this._prefixPartID + i ) )
			 {
				 currentPart = msgParts.get( i );
				 getJTxPart().setText( currentPart.getMessagePart() );
				 getJTxPart().setEditable( true );
				 getJTxPart().requestFocusInWindow();
			 }
		 }
	}
	
	private JPanel getContentPanel()
	{
		if( this.contentPanel == null )
		{	
			this.contentPanel = new JPanel();
			this.contentPanel.setLayout(new BorderLayout( 5, 5 ));
			this.contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			
			JScrollPane scr = new JScrollPane( this.getMessageEditorPane(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
			this.contentPanel.add( scr, BorderLayout.CENTER );
			
			JPanel aux = new JPanel( new BorderLayout( 5, 5 ) );
			aux.add( this.getJTxPart(), BorderLayout.CENTER );
			aux.add( this.getLbEditPart(), BorderLayout.WEST );
			
			JPanel aux2 = new JPanel( new BorderLayout( 5, 5 ) );
			aux2.add( this.getJTxAlternative(), BorderLayout.CENTER );
			aux2.add( this.getLbAlternativeMsg(), BorderLayout.WEST );
			
			JPanel mainAux = new JPanel();
			mainAux.setLayout( new BoxLayout( mainAux, BoxLayout.Y_AXIS ) );
			mainAux.add( aux );
			mainAux.add( aux2 );
			
			this.contentPanel.add( mainAux, BorderLayout.SOUTH );
		}
		
		return this.contentPanel;
	}
	
	private JPanel getButtomPanel()
	{
		if( this.buttonPanel == null )
		{	
			this.buttonPanel = new JPanel();
			this.buttonPanel.setLayout(new FlowLayout( FlowLayout.RIGHT ));
			this.buttonPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			
			//this.buttonPanel.add( this.getCancelButton() );
			this.buttonPanel.add( this.getOkButton() );
			getRootPane().setDefaultButton( this.getOkButton() );
		}
		
		return this.buttonPanel;
	}
	
	/*
	private JButton getCancelButton()
	{
		if( this.cancelButton == null )
		{
			this.cancelButton = new JButton( Language.getLocalCaption( Language.CANCEL_TEXT ) );
			this.cancelButton.setActionCommand("Cancel");		
			
			this.cancelButton.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					dispose();
				}
			});
		}
		
		return this.cancelButton;
	}
	//*/

	private JButton getOkButton()
	{
		if( this.okButton == null )
		{
			this.okButton = new JButton(Language.getLocalCaption(Language.OK_TEXT));
			this.okButton.setActionCommand("OK");		
			
			this.okButton.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					dispose();
				}
			});
		}
		
		return this.okButton;
	}
	
	private JEditorPane getMessageEditorPane()
	{
		if( this.msgEditor == null )
		{
			this.msgEditor = new JEditorPane();
			this.msgEditor.setContentType("text/html");
			this.msgEditor.setEditable(false);	
			
			HTMLEditorKit kit = new HTMLEditorKit();
			this.msgEditor.setEditorKit(kit);

			StyleSheet styleSheet = kit.getStyleSheet();
			styleSheet.addRule("body { font-size: 14px; }");
			styleSheet.addRule("a { font-size: 14px; }");
		}
		
		return this.msgEditor;
	}
	
	private JTextField getJTxPart()
	{
		if( this.partTx == null )
		{
			this.partTx = new JTextField();
			this.partTx.setFont( new Font( Font.DIALOG, Font.PLAIN, 14 ) );
			this.partTx.setEditable( false );
			
			this.partTx.addFocusListener( new FocusAdapter()
			{
				@Override
				public void focusLost(FocusEvent e) 
				{
					JTextField jtf = (JTextField)e.getSource();
					
					currentPart.setMessagePart( jtf.getText() );
					currentPart = null;
					
					jtf.setEditable( false );
					jtf.setText( "" );
					
					showMessage();
				}
			});
		}
		
		return this.partTx;
	}
	
	private JTextField getJTxAlternative()
	{
		if( this.alternativeTx == null )
		{
			this.alternativeTx = new JTextField();
			this.alternativeTx.setFont( new Font( Font.DIALOG, Font.PLAIN, 14 ) );
			this.alternativeTx.setText( this.chMsg.getMessage2Show() );
			
			this.alternativeTx.addFocusListener( new FocusAdapter()
			{
				@Override
				public void focusLost(FocusEvent e) 
				{
					JTextField jtf = (JTextField)e.getSource();
					
					chMsg.setAlternativeMessage( jtf.getText() );
				}
			});
		}
		
		return this.alternativeTx;
	}
	
	private JLabel getLbEditPart()
	{
		if( this.lbEditPart == null )
		{
			this.lbEditPart = new JLabel( Language.getLocalCaption( Language.INPUT_TEXT ) );
		}
		
		return this.lbEditPart;
	}
	
	private JLabel getLbAlternativeMsg()
	{
		if( this.lbAlternativeMsg == null )
		{
			this.lbAlternativeMsg = new JLabel( Language.getLocalCaption( Language.ALTERNATIVE_TEXT ) );
		}
		
		return this.lbAlternativeMsg;
	}
}
