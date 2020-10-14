/**
 * 
 */
package lslrec.gui.panel.plugin;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import lslrec.plugin.lslrecPlugin.ILSLRecPlugin.PluginType;

/**
 * @author Manuel Merino Monge
 *
 */
public class SelectProcessTest extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					SelectProcessTest frame = new SelectProcessTest();
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
	public SelectProcessTest() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new GridLayout( 2, 1));
		setContentPane(contentPane);
		
		List< String > ids = new ArrayList< String >();
		for( int i =0; i < 50; i++)
			ids.add( "test " + i);
		
		contentPane.add( new DataProcessingPluginSelectorPanel( PluginType.DATA_PROCESSING, new ArrayList<String>( ids ), DataProcessingPluginSelectorPanel.SINGLE_SELECTION ), BorderLayout.NORTH );
		contentPane.add( new DataProcessingPluginSelectorPanel( PluginType.DATA_PROCESSING, ids, DataProcessingPluginSelectorPanel.MULTIPLE_SELECTION ), BorderLayout.SOUTH );
	}

}
