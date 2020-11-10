/**
 * 
 */
package lslrec.plugin.impl.test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import lslrec.auxiliar.tasks.INotificationTask;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.config.ConfigApp;
import lslrec.controls.messages.EventInfo;
import lslrec.dataStream.family.stream.lsl.DataStreamSetting;
import lslrec.dataStream.family.stream.lsl.LSLUtils;
import lslrec.dataStream.family.stream.lsl.LSL.StreamInfo;
import lslrec.plugin.impl.dataProcessing.downSampling.DownSamplingPlugin;
import lslrec.plugin.impl.dataProcessing.firFilter.FIRFilterPlugin;
import lslrec.plugin.impl.gui.arithmetic.PluginArithmeticTest;
import lslrec.plugin.impl.gui.memory.PluginMemoryTest;
import lslrec.plugin.lslrecPlugin.ILSLRecConfigurablePlugin;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;

/**
 * @author Manuel Merino Monge
 *
 */
public class guiProcessingTest extends JFrame {

	private JPanel contentPane;

	private AbstractStoppableThread t = null;
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
			
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					guiProcessingTest frame = new guiProcessingTest();
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
	public guiProcessingTest() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);		
		
		//ILSLRecPluginDataProcessing pl = new FIRFilterPlugin();
		ILSLRecConfigurablePlugin pl = new DownSamplingPlugin();
		
		contentPane.add( pl.getSettingPanel(), BorderLayout.CENTER );
		
		JButton start = new JButton( "Start" );
		contentPane.add( start, BorderLayout.SOUTH );
				
		start.addActionListener( new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) 
			{	
				/*
				DataStreamSetting dset = new DataStreamSetting( new StreamInfo( "test", "value", 1,  256, LSLUtils.double64 ) );
				
				LSLRecPluginDataProcessing process = pl.getProcessing( dset, null );
		
				if( t != null )
				{
					t.stopThread( IStoppableThread.FORCE_STOP );
				}
				
				t = new AbstractStoppableThread() 
				{
					double[] v = new double[] {3.000000000000000e+00,1.904664812914517e+00,6.309863136978344e-01,1.214153044667635e+00,1.707106781186548e+00,1.728868006545129e-01,-1.324423348821458e+00,-7.287892104951589e-01,1.110223024625157e-16,-1.118969854527414e+00,-2.089790213551638e+00,-9.382536653846947e-01,2.928932188134523e-01,-4.487861799374563e-01,-1.216772751324739e+00,-5.690574789194661e-02,1.000000000000000e+00,-5.690574789194443e-02,-1.216772751324739e+00,-4.487861799374580e-01,2.928932188134526e-01,-9.382536653846925e-01,-2.089790213551637e+00,-1.118969854527418e+00,-2.220446049250313e-16,-7.287892104951587e-01,-1.324423348821457e+00,1.728868006545078e-01,1.707106781186547e+00,1.214153044667635e+00,6.309863136978331e-01,1.904664812914513e+00};
					
					double fm = 256D;
					
					long T = (long)( 1000 / fm );
					
					int index = 0;
					@Override
					protected void runInLoop() throws Exception 
					{							
						Number[] in = new Number[] {  v[ index ] };
						Number[] d = process.processDataBlock( in );
						
						System.out.println( Arrays.deepToString( d ) );
						
						index++;
						if( index >= v.length )
						{
							index  = 0;
						}
						
						wait( T );
					}
					
					@Override
					protected void runExceptionManager(Throwable e) 
					{
						if( !( e instanceof InterruptedException ) )
						{
							super.runExceptionManager(e);
						}
					}
					
					@Override
					protected void preStopThread(int arg0) throws Exception {
						// TODO Auto-generated method stub
						
					}
					
					@Override
					protected void postStopThread(int arg0) throws Exception {
						// TODO Auto-generated method stub
						
					}
				};
				*/
				
				try {
					t.startThread();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		
	}

}
