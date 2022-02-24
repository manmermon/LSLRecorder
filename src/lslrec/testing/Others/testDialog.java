/**
 * 
 */
package lslrec.testing.Others;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

/**
 * @author Manuel Merino Monge
 *
 */
public class testDialog {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		JDialog pdial = new JDialog(  );
		pdial.setVisible( false );								
		
		JPanel main = new JPanel( new BorderLayout() );
		main.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) );
										
		pdial.getContentPane().setLayout(new BorderLayout());																
		pdial.getContentPane().add( main, BorderLayout.CENTER );								
		
		JProgressBar jpb = new JProgressBar( 0, 5 );
		jpb.setValue( 0 );
		main.add( jpb, BorderLayout.SOUTH );
		
		JTextField note = new JTextField();
		note.setEditable( false );
		main.add( note, BorderLayout.NORTH );								
		
		JDialog _dial = pdial;
//		Thread tlaunch = new Thread() 
//		{
//			public void run() 
//			{					
				_dial.setTitle( "TEST");								
				_dial.setResizable( false );								
				_dial.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
				_dial.setSize( new Dimension( 300, 100 ) );
				_dial.setAlwaysOnTop( true );
				_dial.setLocationRelativeTo( null );
				//_dial.pack();
				_dial.setVisible(true);
	}

}
