/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.zTransform;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

/**
 * @author Manuel Merino Monge
 *
 */
public class ZPlanePluginWindow extends JFrame {

	private JPanel contentPane;

	private ZPlanePane zpp = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ZPlanePluginWindow frame = new ZPlanePluginWindow();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public ZPlanePluginWindow() 
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		contentPane.add( this.getZPlanePane() );
	}

	private ZPlanePane getZPlanePane()
	{
		if( this.zpp == null )
		{
			this.zpp = new ZPlanePane();
		}
		
		return this.zpp;
	}
	
	public List< Marker > getZerosPoles()
	{
		return this.getZPlanePane().getZerosPoles();
	}
}
