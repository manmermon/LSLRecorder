/**
 * 
 */
package lslrec.plugin.impl.gui;

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
import lslrec.plugin.impl.gui.arithmetic.PluginArithmeticTest;
import lslrec.plugin.impl.gui.memory.PluginMemoryTest;
import lslrec.plugin.lslrecPlugin.trial.LSLRecPluginGUIExperiment;

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
		
		
		//PluginMemoryTest pl = new PluginMemoryTest();
		PluginArithmeticTest pl = new PluginArithmeticTest();
		
		contentPane.add( pl.getSettingPanel(), BorderLayout.CENTER );
		
		JButton start = new JButton( "Start" );
		contentPane.add( start, BorderLayout.SOUTH );
		
		start.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{	
				LSLRecPluginGUIExperiment test = pl.getGUIExperiment();
				
				JFrame d = test.getWindonw();
				d.setVisible( false );
				d.setLocationRelativeTo( null );
				d.setSize( new Dimension( 400, 400 ) );
								
				test.taskMonitor( new ITaskMonitor() 
				{					
					@Override
					public void taskDone(INotificationTask arg0) throws Exception 
					{
						List< EventInfo > inf = arg0.getResult( true );
						
						for( EventInfo e : inf )
						{
							System.out.println("guiTest.guiTest() " + e.getEventInformation() );
						}
					}
				});
				
				try 
				{					
					d.setVisible( true );
					test.startThread();
				}
				catch (Exception e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
	}

}
