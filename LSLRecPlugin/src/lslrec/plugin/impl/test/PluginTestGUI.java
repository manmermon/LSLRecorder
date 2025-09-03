package lslrec.plugin.impl.test;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.BorderLayout;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.border.TitledBorder;

import lslrec.auxiliar.extra.ArrayTreeMap;
import lslrec.plugin.impl.dataProcessing.basicStatSummary.BasicStatSummaryPlugin;
import lslrec.plugin.impl.dataProcessing.downSampling.DownSamplingPlugin;
import lslrec.plugin.impl.dataProcessing.fftPlot.FFTPlugin;
import lslrec.plugin.impl.dataProcessing.firFilter.FIRFilterPlugin;
import lslrec.plugin.impl.dataProcessing.openposePlotter.OpenposePlotterPlugin;
import lslrec.plugin.impl.dataProcessing.painter.DataDisplayPlugin;
import lslrec.plugin.impl.dataProcessing.zTransform.ZTransformPlugin;
import lslrec.plugin.impl.encoder.binary.BinaryEncoderPlugin;
import lslrec.plugin.impl.encoder.csv.CSVEncoderPlugin;
import lslrec.plugin.impl.encoder.hdf5.HDF5EncoderPlugin;
import lslrec.plugin.impl.encoder.matlab.MatlabEncoderPlugin;
import lslrec.plugin.impl.gui.alarm.PluginAlarmTest;
import lslrec.plugin.impl.gui.arithmetic.PluginArithmeticTest;
import lslrec.plugin.impl.gui.memory.PluginMemoryTest;
import lslrec.plugin.impl.gui.trialStagesMarker.TrialStageMarkerPlugin;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin.PluginType;
import lslrec.plugin.lslrecPlugin.test.LSLRecPluginTesting;

import javax.swing.border.EtchedBorder;
import java.awt.Color;
import javax.swing.JLabel;
import javax.swing.border.CompoundBorder;

public class PluginTestGUI extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5197038486886594674L;
	
	private JPanel contentPane;
	private final JPanel panel = new JPanel();
	private final JComboBox< PluginType > cbPluginType = new JComboBox< PluginType >();
	private final JButton btnStart = new JButton("Start");
	private final JPanel panel_1 = new JPanel();
	private final JLabel lblPluginType = new JLabel("Type: ");
	private final JPanel panel_2 = new JPanel();
	private final JComboBox< ILSLRecPlugin > cbPlugin = new JComboBox< ILSLRecPlugin >();
	private final JLabel lblIdType = new JLabel("Unknown");
	
	private ArrayTreeMap< PluginType, ILSLRecPlugin > plugins = new ArrayTreeMap<PluginType, ILSLRecPlugin>();
	private final JPanel panel_3 = new JPanel();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					PluginTestGUI frame = new PluginTestGUI();
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
	public PluginTestGUI() {
		initialize();
	}
	
	private void initialize() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 175);
		this.contentPane = new JPanel();
		this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		super.setResizable( false );
		
		setContentPane(this.contentPane);
		this.contentPane.setLayout(new BorderLayout(0, 0));
		this.panel.setBorder(new CompoundBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, new Color(255, 255, 255), new Color(160, 160, 160)), "Plugins", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)), new EmptyBorder(5, 5, 5, 5)));
		
		this.contentPane.add(this.panel, BorderLayout.NORTH);
		this.panel.setLayout(new BoxLayout(this.panel, BoxLayout.Y_AXIS));
		
		this.panel.add(this.panel_1);
		this.panel_1.setLayout(new BoxLayout(this.panel_1, BoxLayout.X_AXIS));
		
		this.panel_1.add(this.lblPluginType);
		this.panel_1.add(this.cbPluginType);
		
		this.panel.add(this.panel_3);
		
		this.panel.add(this.panel_2);
		this.panel_2.setLayout(new BoxLayout(this.panel_2, BoxLayout.X_AXIS));
		
		this.panel_2.add(this.lblIdType);
		
		this.panel_2.add(this.cbPlugin);
		this.contentPane.add(this.btnStart, BorderLayout.SOUTH);
		
		setPlugins();
		
		cbPluginType.addItemListener( new ItemListener() 
		{			
			@Override
			public void itemStateChanged(ItemEvent e) 
			{
				if( e.getStateChange() == e.SELECTED )
				{
					PluginType tp = (PluginType)e.getItem();
					
					cbPlugin.removeAllItems();
					
					for( ILSLRecPlugin plg : plugins.get( tp ) )
					{
						cbPlugin.addItem( plg );
					}
					
					cbPlugin.setVisible( false );
					cbPlugin.setSelectedIndex( 0 );
					cbPlugin.setVisible( true );
				}
			}
		});
		
		for( PluginType pt : plugins.keySet() )
		{
			cbPluginType.addItem( pt );			
		}
		
		cbPluginType.setSelectedIndex( 0 );
		
		btnStart.addActionListener( new ActionListener() 
		{			
			@Override
			public void actionPerformed(ActionEvent e) 
			{
				LSLRecPluginTesting testing = new LSLRecPluginTesting( (ILSLRecPlugin)cbPlugin.getSelectedItem() );
				
				testing.startTest();
				
				testing.PluginTestWindow().setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
			}
		});
	}
	
	private void setPlugins()
	{		
		plugins.putElement( PluginType.DATA_PROCESSING, new DownSamplingPlugin() );
		plugins.putElement( PluginType.DATA_PROCESSING, new FFTPlugin() );
		plugins.putElement( PluginType.DATA_PROCESSING, new FIRFilterPlugin() );
		plugins.putElement( PluginType.DATA_PROCESSING, new OpenposePlotterPlugin() );
		plugins.putElement( PluginType.DATA_PROCESSING, new DataDisplayPlugin() );
		plugins.putElement( PluginType.DATA_PROCESSING, new ZTransformPlugin() );
		plugins.putElement( PluginType.DATA_PROCESSING, new BasicStatSummaryPlugin() );
		
		plugins.putElement( PluginType.ENCODER , new CSVEncoderPlugin() );
		plugins.putElement( PluginType.ENCODER , new HDF5EncoderPlugin() );
		plugins.putElement( PluginType.ENCODER , new MatlabEncoderPlugin() );
		plugins.putElement( PluginType.ENCODER , new BinaryEncoderPlugin() );
		
		plugins.putElement( PluginType.TRIAL, new PluginArithmeticTest() );
		plugins.putElement( PluginType.TRIAL, new PluginAlarmTest() );
		plugins.putElement( PluginType.TRIAL, new PluginMemoryTest() );
		plugins.putElement( PluginType.TRIAL, new TrialStageMarkerPlugin() );
	}
}
