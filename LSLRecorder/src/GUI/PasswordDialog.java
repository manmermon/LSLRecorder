package GUI;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import org.apache.commons.lang3.CharUtils;

import Config.Language.Language;

/**
 * A Simple Login Dialog
 * 
 * @author Manuel Oliver Watkins (c)
 */
public class PasswordDialog extends JDialog 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 8647345891755673568L;
	
	public static final int PASSWORD_CANCEL = -1;
	public static final int UNDIFINED = 0;
	public static final int PASSWORD_OK = 1;
	public static final int PASSWORD_INCORRECT = 2;
	
	public static final int MIN_LENGTH = 5;
	
	private JPasswordField confirmPassworField = null;
	private JPasswordField passwordField = null;
	
	private JButton okButton = null;
	private JButton cancelButton = null;
	
	private JLabel msglab = null;
	
	private String password = null;
	
	private int state = UNDIFINED;

	private String passError = "";
	
	public PasswordDialog( JFrame owner, String message ) 
	{	
		super( owner );
		
		if( message == null )
		{
			message = Language.ENCRYPT_KEY_TEXT;
		}
		
		this.setTitle( Language.getLocalCaption( Language.ENCRYPT_KEY_TEXT ) );
		
		super.setSize( new Dimension( 250, 150 ) );
		super.setResizable( false );
		
		super.setModalityType( JDialog.DEFAULT_MODALITY_TYPE );

		JPanel passwordPanel = new JPanel( new GridBagLayout() );
		JPanel buttonPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );

		buttonPanel.add( this.getOkButton() );
		buttonPanel.add( this.getCancelButton() );

		GridBagConstraints gbc = new GridBagConstraints();

		gbc.insets = new Insets(4, 4, 4, 4);
		
		
		JLabel PasswordLabel1 = new JLabel( Language.getLocalCaption( Language.PASSWORD_TEXT ) );
		
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 0;
		passwordPanel.add( PasswordLabel1, gbc );

		JLabel passwordLabel2 = new JLabel( Language.getLocalCaption( Language.REPEAT_TEXT ) );
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1;
		passwordPanel.add( this.getPasswordField(), gbc );

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		passwordPanel.add( passwordLabel2, gbc );

		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.weightx = 1;
		passwordPanel.add( this.getConfirmPassworField(), gbc);

		this.setMessage( message );

		this.add( this.getMessageLabel(), BorderLayout.NORTH );
		this.add( passwordPanel, BorderLayout.CENTER );
		this.add( buttonPanel, BorderLayout.SOUTH );
		
		super.addWindowListener( new WindowAdapter()
		{	
			@Override
			public void windowClosing(WindowEvent e) 
			{	
				cancelPassword();
			}			
		});
	}

	private JPasswordField getConfirmPassworField()
	{
		if( this.confirmPassworField == null )
		{
			this.confirmPassworField = new JPasswordField( 16 );
		}
		
		return this.confirmPassworField;
	}
	
	private JPasswordField getPasswordField()
	{
		if( this.passwordField == null )
		{
			this.passwordField = new JPasswordField( 16 );			
		}
		
		return this.passwordField;
	}

	private JButton getOkButton()
	{
		if( this.okButton == null )
		{
			this.okButton = new JButton( Language.getLocalCaption( Language.OK_TEXT ) );
			
			this.okButton.addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					password = new String( getPasswordField().getPassword() );
					state = PASSWORD_OK;
					
					if( !passwordOK( password ) )
					{
						password = null;
						state = PASSWORD_INCORRECT;
					}

					PasswordDialog.this.setVisible( false );
					PasswordDialog.this.dispose();
				}
			});
		}
		
		return this.okButton;
	}
	
	private boolean passwordOK( String password )
	{
		boolean ok = ( password.length() >= MIN_LENGTH );
		
		if( !ok )
		{
			this.passError = "Password length > " + ( MIN_LENGTH - 1 ) + ".";
		}
		else
		{
			ok = isPrintablePassword( password ) ;
			
			if( !ok )
			{
				this.passError = "Non printable characters." ;
			}
			else
			{
				StringBuilder confirm = new StringBuilder();
				confirm.append(  getConfirmPassworField().getPassword() ); 
				
				StringBuilder pass = new StringBuilder();
				pass.append( password.getBytes() );
				
				ok = ( !confirm.equals( pass ) );
				
				if( !ok )
				{
					this.passError = "Password confirmation no match.";
				}
				else
				{
					ok = !containWhitespace( password );
					
					if( !ok )
					{
						this.passError = "Password containg whitspace(s).";
					}					
				}
			}			
		}
		
		return ok;
	}
	
	private boolean isPrintablePassword( String pass )
	{
		boolean ok = ( pass != null );
		
		if( ok )
		{
			for( char c : pass.toCharArray() )
			{
				Character.UnicodeBlock block = Character.UnicodeBlock.of( c );

				ok = ( !Character.isISOControl( c ) ) 
						&& ( c != KeyEvent.CHAR_UNDEFINED ) 
						&& ( block != null )
						&& ( block != Character.UnicodeBlock.SPECIALS )						
						&& ( !(c + "").equals( "" + CharUtils.LF ) )
						&& ( !(c + "").equals( "" + CharUtils.CR ) );
				
				if( !ok )
				{
					break;
				}
			}
		}
		
		return ok;
	}
	
	private boolean containWhitespace( String text )
	{
		boolean contain = false;
		
		if( text != null )
		{
			String txtNoWh = text.replaceAll( "\\s+", "" );
			contain = ( text.length() != txtNoWh.length() );
		}
		
		return contain;
	}
	
	private JButton getCancelButton()
	{
		if( this.cancelButton == null )
		{
			this.cancelButton = new JButton( Language.getLocalCaption( Language.CANCEL_TEXT ) );
			
			this.cancelButton.addActionListener(new ActionListener() 
			{
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					cancelPassword();
					
					PasswordDialog.this.setVisible( false );
					PasswordDialog.this.dispose();
				}
			});
		}
		
		return this.cancelButton;
	}
	
	public String getPasswordError()
	{
		return this.passError;
	}
	
	private void cancelPassword()
	{
		this.password = null;				
		this.state = PASSWORD_CANCEL;
		
		this.passError = "user cancel.";
	}
	
	private JLabel getMessageLabel()
	{
		if( this.msglab == null )
		{
			this.msglab = new JLabel( );
		}
		
		return this.msglab;
	}
	
	public void setMessage( String msg )
	{
		this.getMessageLabel().setText( msg );
	}
	
	public int getState()
	{
		return this.state;
	}
	
	public String getPassword()
	{
		return this.password;
	}
}