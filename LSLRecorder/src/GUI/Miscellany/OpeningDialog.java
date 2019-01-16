package GUI.Miscellany;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class OpeningDialog extends JFrame 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6463721786839022299L;

	public OpeningDialog( Dimension size, Image ico, String appName, String msg, Color backgroundcolor ) 
	{		
		super.setPreferredSize( size );
		super.setSize( size );
		super.setResizable( false );		
		super.setUndecorated( true );
		
		super.setTitle( appName );
		super.setIconImage( ico );
		
		JPanel content = new JPanel( new BorderLayout() );
		content.setBackground( backgroundcolor );		
		
		super.setContentPane( content );
		
		JLabel lb = new JLabel( new ImageIcon( ico ) );
		lb.setText( msg );
		
		lb.setBorder( BorderFactory.createEmptyBorder() );
		lb.setOpaque( false );
				
		content.add( lb, BorderLayout.CENTER );
		content.setBorder( BorderFactory.createEtchedBorder() );
		
		lb.setVisible( true );
		content.setVisible( true );
	}	
}
