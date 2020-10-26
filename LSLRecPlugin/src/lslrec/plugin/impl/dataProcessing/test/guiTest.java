/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import lslrec.auxiliar.tasks.INotificationTask;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.controls.messages.EventInfo;
import lslrec.plugin.impl.dataProcessing.firFilter.FIRFilterPlugin;
import lslrec.plugin.impl.gui.arithmetic.PluginArithmeticTest;
import lslrec.plugin.impl.gui.memory.PluginMemoryTest;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;

/**
 * @author Manuel Merino Monge
 *
 */
public class guiTest extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					guiTest frame = new guiTest();
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
	public guiTest() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);		
		
		FIRFilterPlugin pl = new FIRFilterPlugin();
		
		contentPane.add( pl.getSettingPanel(), BorderLayout.CENTER );
		
		JButton start = new JButton( "Start" );
		contentPane.add( start, BorderLayout.SOUTH );
	}

}
