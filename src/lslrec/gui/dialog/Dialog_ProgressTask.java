/**
 * 
 */
package lslrec.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * @author Manuel Merino Monge
 *
 */
public class Dialog_ProgressTask extends JDialog {

	private final JPanel contentPanel = new JPanel();
	
	private JProgressBar jpb;
	private JTextField note;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			Dialog_ProgressTask dialog = new Dialog_ProgressTask( null );

			dialog.setTitle( "TEST");								
			dialog.setResizable( false );								
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setSize( new Dimension( 300, 90 ) );
			dialog.setAlwaysOnTop( true );
			dialog.setLocationRelativeTo( null );
			//pdial.pack();
			dialog.setVisible(true);

			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public Dialog_ProgressTask( Window w ) 
	{
		super( w );
		
		JPanel main = new JPanel( new BorderLayout() );
		main.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
										
		getContentPane().setLayout(new BorderLayout());
		getContentPane().setBackground( Color.BLUE );
		getContentPane().add( main, BorderLayout.CENTER );								
				
		main.add( this.getProgressBar( ), BorderLayout.SOUTH );
		this.getProgressBar().setValue( 0 );
		
		main.add( this.getNoteText(), BorderLayout.NORTH );				
	}
		
	private JProgressBar getProgressBar( )
	{
		if( this.jpb == null )
		{
			jpb = new JProgressBar( );
			jpb.setMinimum( 0 );
			jpb.setMaximum( 1 );
		}
		
		return this.jpb;
	}
	
	private JTextField getNoteText()
	{
		if( this.note == null )
		{
			this.note = new JTextField();
			this.note.setEditable( false );
			
			this.note.getDocument().addDocumentListener( new DocumentListener() 
			{
				
				@Override
				public void removeUpdate(DocumentEvent e) 
				{
					update( e );
				}
				
				@Override
				public void insertUpdate(DocumentEvent e) 
				{
					update( e );
				}
				
				@Override
				public void changedUpdate(DocumentEvent e) 
				{
					update( e );
				}
				
				private void update( DocumentEvent e )
				{
					note.setCaretPosition( note.getText().length() );
				}
			});
		}
		return this.note;
	}
	
	public void setMaximum( int max )
	{
		getProgressBar().setMaximum( max );
	}
	
	public void setMinimum( int min )
	{
		getProgressBar().setMinimum( min );
	}
	
	public void setProgress( int v )
	{
		this.getProgressBar().setValue( v );
	}
	
	public void setNote( String tx )
	{
		this.getNoteText().setText( tx );
	}
}
