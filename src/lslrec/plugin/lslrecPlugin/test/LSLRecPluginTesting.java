/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.  https://github.com/manmermon/LSLRecorder
 *
 *   LSLRec is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   LSLRec is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with LSLRec.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */
package lslrec.plugin.lslrecPlugin.test;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.border.EmptyBorder;

import lslrec.auxiliar.WarningMessage;
import lslrec.auxiliar.extra.ConvertTo;
import lslrec.auxiliar.extra.FileUtils;
import lslrec.auxiliar.extra.Tuple;
import lslrec.auxiliar.task.INotificationTask;
import lslrec.auxiliar.task.ITaskMonitor;
import lslrec.config.ConfigApp;
import lslrec.config.Parameter;
import lslrec.dataStream.family.setting.IStreamSetting.StreamLibrary;
import lslrec.dataStream.family.setting.SimpleStreamSetting;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlock;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlockFactory;
import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.dataStream.outputDataFile.format.IOutputDataFileWriter;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.tools.StreamUtils.StreamDataType;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.plugin.lslrecPlugin.ILSLRecConfigurablePlugin;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.LSLRecConfigurablePluginAbstract;
import lslrec.plugin.lslrecPlugin.compressor.LSLRecPluginCompressor;
import lslrec.plugin.lslrecPlugin.encoder.LSLRecPluginEncoder;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.sync.ILSLRecPluginSyncMethod;
import lslrec.plugin.lslrecPlugin.sync.LSLRecPluginSyncMethod;
import lslrec.plugin.lslrecPlugin.trial.ILSLRecPluginTrial;
import lslrec.plugin.lslrecPlugin.trial.LSLRecPluginTrial;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.JComboBox;
import javax.swing.JDialog;

import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;

public class LSLRecPluginTesting {
	private JPanel contentPane;

	private ILSLRecPlugin plugin = null;
	private JTextField textField;
	private JTextArea textArea;

	private JSpinner time;

	private AbstractStoppableThread thr = null;

	private JToggleButton btnStartTest = null;

	private JFrame window = null;

	// private JFrame plgWin = null;

	private EndedPluginInspector inspector = null;

	private LinkedList<Number> dataBuffer = null;
	private int overlapCounter = 0;

	private StreamLibrary lib = StreamLibrary.LSL;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LSLRecPluginTesting test = new LSLRecPluginTesting(new LSLRecConfigurablePluginAbstract() {

						@Override
						public int compareTo(ILSLRecPlugin o) {
							// TODO Auto-generated method stub
							return 0;
						}

						@Override
						public String getID() {
							// TODO Auto-generated method stub
							return "TEST";
						}

						@Override
						public PluginType getType() {
							// TODO Auto-generated method stub
							return PluginType.ENCODER;
						}

						@Override
						public WarningMessage checkSettings() {
							// TODO Auto-generated method stub
							return null;
						}

						@Override
						protected void setSettingPanel(JPanel panel) {
						}

						@Override
						public List<Parameter<String>> getSettings() {
							List<Parameter<String>> parlist = new ArrayList<Parameter<String>>();
							parlist.add(new Parameter<String>("prueba", "true"));

							super.loadSettings(parlist);

							return super.getSettings();
						}

						@Override
						protected void postLoadSettings() {

						}
					});

					test.startTest();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public LSLRecPluginTesting(ILSLRecPlugin plg) {
		this.plugin = plg;

		if (plugin == null) {
			throw new NullPointerException();
		}
	}

	/**
	 * Launch the application.
	 */
	public void startTest() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					JFrame frame = PluginTestWindow();
					frame.setVisible(true);

					ExceptionDialog.createExceptionDialog(frame);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public JFrame PluginTestWindow() {
		if (this.window == null) {
			this.window = new JFrame();

			this.window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			this.window.setLocation(100, 100);

			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			d.width /= 2;
			d.height /= 2;

			this.window.setSize(d);

			this.window.setTitle("Plugin testing: " + plugin.getID());

			this.contentPane = new JPanel();
			this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			this.contentPane.setLayout(new BorderLayout(0, 0));

			this.window.setContentPane(contentPane);

			this.btnStartTest = new JToggleButton("Start test");
			this.contentPane.add(btnStartTest, BorderLayout.SOUTH);

			JPanel panel_east = new JPanel(new BorderLayout());

			JPanel panel_east_nort = new JPanel();
			panel_east_nort.setLayout(new BoxLayout(panel_east_nort, BoxLayout.X_AXIS));
			panel_east_nort.add(new JLabel("Loop time (ms):"));

			this.time = new JSpinner(new SpinnerNumberModel(500, 20, 500 * 20, 10));
			panel_east_nort.add(this.time);
			JButton clearlog = new JButton("clear");
			clearlog.setBorder(BorderFactory.createRaisedSoftBevelBorder());
			clearlog.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					textArea.setText("");
				}
			});
			panel_east_nort.add(clearlog);

			panel_east.add(panel_east_nort, BorderLayout.NORTH);

			textArea = new JTextArea();
			textArea.setWrapStyleWord(true);
			textArea.setEditable(false);
			textArea.setColumns(15);
			textArea.setBorder(BorderFactory.createTitledBorder("log"));
			textArea.setLineWrap(false);
			panel_east.add(new JScrollPane(textArea), BorderLayout.CENTER);

			contentPane.add(panel_east, BorderLayout.EAST);

			JPanel panel = new JPanel();
			contentPane.add(panel, BorderLayout.NORTH);
			panel.setLayout(new BorderLayout(0, 0));

			JPanel panel_1 = new JPanel();
			panel.add(panel_1, BorderLayout.WEST);
			panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

			JComboBox<StreamDataType> comboBox = new JComboBox<StreamDataType>(StreamDataType.values());
			comboBox.removeItem(StreamDataType.undefined);

			JLabel lblNewLabel = new JLabel("Data type:");
			panel_1.add(lblNewLabel);
			comboBox.setSelectedItem(StreamDataType.float32);

			panel_1.add(comboBox);

			JButton pluginOptions = new JButton();
			pluginOptions.setIcon(
					new ImageIcon(GeneralAppIcon.Config2(Color.BLACK).getScaledInstance(16, 16, Image.SCALE_SMOOTH)));

			pluginOptions.setEnabled((plugin instanceof ILSLRecConfigurablePlugin)
					&& (((ILSLRecConfigurablePlugin) plugin).getSettings() != null)
					&& (!(((ILSLRecConfigurablePlugin) plugin).getSettings().isEmpty())));

			pluginOptions.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					List<Parameter<String>> pars = ((ILSLRecConfigurablePlugin) plugin).getSettings();

					JDialog dial = new JDialog();

					JPanel pmain = new JPanel();
					dial.setContentPane(pmain);

					pmain.setLayout(new BorderLayout());

					JPanel p = new JPanel();
					pmain.add(new JScrollPane(p), BorderLayout.CENTER);

					p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

					for (Parameter<String> par : pars) {
						JPanel p2 = new JPanel();
						p2.setLayout(new BoxLayout(p2, BoxLayout.X_AXIS));

						p2.add(new JLabel(par.getID() + " "));

						JTextField tx = new JTextField(par.getValue());
						tx.addFocusListener(new FocusListener() {
							@Override
							public void focusLost(FocusEvent e) {
								par.setValue(tx.getText());
							}

							@Override
							public void focusGained(FocusEvent e) {
							}
						});
						p2.add(tx);

						p.add(p2);
					}

					dial.setSize(200, 300);
					dial.setLocationRelativeTo(window);
					// dial.pack();
					dial.setResizable(true);
					dial.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
					dial.setVisible(true);
				}
			});

			panel_1.add(pluginOptions);

			panel_1.add(new JLabel("Channels:"));
			JSpinner sp = new JSpinner(new SpinnerNumberModel(1, 1, null, 1));

			sp.addMouseWheelListener(new MouseWheelListener() {
				@Override
				public void mouseWheelMoved(MouseWheelEvent e) {
					if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
						try {
							JSpinner sp = (JSpinner) e.getSource();

							int d = e.getWheelRotation();

							if (d > 0) {
								sp.setValue(sp.getModel().getPreviousValue());
							} else {
								sp.setValue(sp.getModel().getNextValue());
							}
						} catch (IllegalArgumentException ex) {
						}
					}
				}
			});

			panel_1.add(sp);

			JCheckBox chb = new JCheckBox("Offset");
			panel_1.add(chb);

			textField = new JTextField();
			textField.setBorder(BorderFactory.createTitledBorder("values (separator ';')"));
			panel.add(textField, BorderLayout.CENTER);
			textField.setColumns(10);

			if (this.plugin instanceof ILSLRecConfigurablePlugin) {
				contentPane.add(((ILSLRecConfigurablePlugin) this.plugin).getSettingPanel(), BorderLayout.CENTER);
			}

			btnStartTest.addItemListener(new ItemListener() {
				@Override
				public void itemStateChanged(ItemEvent arg0) {
					JToggleButton b = (JToggleButton) arg0.getSource();

					if (b.isSelected()) {
						try {
							Number[] values = null;
							String[] strValues = null;

							StreamDataType datType = (StreamDataType) comboBox.getSelectedItem();

							int ch = (Integer) sp.getValue();

							if (datType != StreamDataType.string) {
								values = createNumberArray(ch, chb.isSelected());
							} else {
								strValues = createStringArray(ch);
							}

							switch (plugin.getType()) {
								case COMPRESSOR: {
									startCompressorTest(values, strValues, datType);

									break;
								}
								case DATA_PROCESSING: {
									startProcessingTest(ch, values, strValues, datType);

									break;
								}
								case ENCODER: {
									startEncoderTest(ch, values, strValues, datType);

									break;
								}
								case SYNC: {
									startSyncTest();

									break;
								}
								case TRIAL: {
									startTrialTest();

									break;
								}
								default:
									break;
							}

							if (inspector != null) {
								inspector.stopThread(IStoppableThread.FORCE_STOP);
							}

							inspector = new EndedPluginInspector(thr);
							inspector.startThread();
						} catch (Exception e) {
							b.setSelected(false);

							ExceptionMessage msg = new ExceptionMessage(e, e.getMessage(),
																	ExceptionMessage.ERROR_MESSAGE);
							ExceptionDialog.showMessageDialog(msg, true, true);
						}
					} else {
						stopTestThread();
					}
				}
			});
		}

		return this.window;
	}

	private Number[] createNumberArray(int ch, boolean offset) {
		Number[] values = null;

		try {
			String[] strVal = createStringArray(ch);

			if (strVal != null) {
				List<Number> list = new ArrayList<Number>();

				int DC = offset ? 1 : 0;

				for (int i = 0; i < ch; i++) {
					for (String str : strVal) {
						list.add(Double.parseDouble(str) + DC * i);
					}
				}

				values = list.toArray(new Number[0]);

				values = ConvertTo.Transform.Interleaved(values, strVal.length, ch);
			}
		} catch (Exception e) {
		}

		return values;
	}

	private String[] createStringArray(int ch) {
		String t = textField.getText().replaceAll("\\s+", "");

		String[] strVal = t.split(";");

		List<String> values = new ArrayList<String>();

		if (strVal.length > 0) {
			for (int i = 0; i < ch; i++) {
				for (String v : strVal) {
					if (!v.isEmpty()) {
						values.add(v);
					}
				}
			}
		}

		return values.toArray(new String[0]);
	}

	private void stopTestThread() {
		if (thr != null) {
			thr.stopThread(IStoppableThread.FORCE_STOP);
			thr = null;
		}

		this.dataBuffer = null;
	}

	private SimpleStreamSetting getSimpleStreamSetting(String name, StreamDataType type, int chs) 
	{
		return new SimpleStreamSetting(this.lib, name, type, type, type, chs, 1, 0, 3, true, getClass().getCanonicalName(),
				getClass().getCanonicalName()
				// , "testing"
				, null);
	}

	private ITaskMonitor getDefaultMonitor() {
		return new ITaskMonitor() {
			@Override
			public void taskDone(INotificationTask task) throws Exception {
				String msg = task.getResult(true).toString();

				if (!msg.endsWith("\n")) {
					msg += "\n";
				}

				textArea.append(">> " + msg);
			}
		};
	}

	private void startCompressorTest(Number[] values, String[] strValues, StreamDataType type) throws Exception {
		stopTestThread();

		LSLRecPluginCompressor compressor = (LSLRecPluginCompressor) this.plugin;

		byte[] bytes = null;

		if (type == StreamDataType.string) {
			bytes = ConvertTo.Transform.StringArray2byteArray(strValues);
		} else {
			bytes = ConvertTo.Transform.NumberArray2byteArray(values, type);
		}

		final byte[] dataByte = bytes;
		thr = new AbstractStoppableThread() {
			@Override
			protected void runInLoop() throws Exception {
				byte[] zipdata = compressor.getCompressor().zipData(dataByte);

				String ziplen = zipdata + "";

				if (zipdata != null) {
					ziplen = "" + zipdata.length;
				}

				textArea.append(compressor.getID() + ">> input data bytes = " + dataByte.length
						+ " : compressed data bytes = " + ziplen + "\n");

				this.wait(500L);
			}

			@Override
			protected void preStopThread(int friendliness) throws Exception {
			}

			@Override
			protected void postStopThread(int friendliness) throws Exception {
			}

			@Override
			protected void runExceptionManager(Throwable e) {
				if (!(e instanceof InterruptedException)) {
					super.runExceptionManager(e);
				}
			}
		};

		thr.startThread();
	}

	private void startProcessingTest(int chs, Number[] values, String[] strs, StreamDataType type) throws Exception {
		stopTestThread();

		ILSLRecPluginDataProcessing plg = (ILSLRecPluginDataProcessing) this.plugin;
		LSLRecPluginDataProcessing proc = plg.getProcessing(getSimpleStreamSetting(plg.getID(), type, chs), null);
		proc.loadProcessingSettings(plg.getSettings());

		this.dataBuffer = new LinkedList<Number>();

		int len = proc.getBufferLength();

		if (len <= 0) {
			len = values.length;
		}

		List<Number> data = new ArrayList<Number>();

		for (Number d : values) {
			data.add(d);
		}

		long timeWait = ((Number) this.time.getValue()).longValue();
		if (timeWait < 20) {
			timeWait = 20;
		}

		final long waitT = timeWait;
		thr = new AbstractStoppableThread() {
			int from = 0;
			int to = 1;

			@Override
			protected void runInLoop() throws Exception {
				List<Number> d = new ArrayList<Number>();

				to = from + proc.getBufferLength();

				while (to > data.size()) {
					d.addAll(data.subList(from, data.size()));
					from = 0;
					to = to - data.size();
				}

				d.addAll(data.subList(from, to));
				from = to;
				if (from >= data.size()) {
					from = 0;
				}

				Number[] res = proc.processDataBlock(d.toArray(new Number[0]));

				String r = res + "";
				if (res != null) {
					r = Arrays.deepToString(res);
				}
				textArea.append(plg.getID() + ">> input data = " + d + " : compressed data bytes = " + r + "\n");

				synchronized (this) {
					super.wait(waitT);
				}
			}

			@Override
			protected void cleanUp() throws Exception {
				super.cleanUp();

				proc.finish();
			}

			@Override
			protected void preStopThread(int friendliness) throws Exception {
			}

			@Override
			protected void postStopThread(int friendliness) throws Exception {
			}

			@Override
			protected void runExceptionManager(Throwable e) {
				if (!(e instanceof InterruptedException)) {
					super.runExceptionManager(e);
				}
			}
		};

		thr.startThread();
	}

	private void startTrialTest() throws Exception {
		stopTestThread();

		ILSLRecPluginTrial trial = (ILSLRecPluginTrial) this.plugin;

		LSLRecPluginTrial test = trial.getGUIExperiment();

		test.loadSettings(trial.getSettings());

		test.taskMonitor(getDefaultMonitor());

		test.setTrialWindowSize(new Dimension(400, 400));

		/*
		 * JFrame window = test.getWindonw();
		 * 
		 * window.setVisible( false );
		 * 
		 * window.setSize( 400, 400 );
		 * window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		 * 
		 * if( window.getTitle() == null || window.getTitle().trim().isEmpty() )
		 * {
		 * window.setTitle( this.window.getTitle() );
		 * }
		 * 
		 * window.setVisible( true );
		 * window.toFront();
		 * 
		 * plgWin = window;
		 */

		test.showTrialWindow();

		thr = test;

		thr.startThread();
	}

	private void startEncoderTest(int chs, Number[] values, String[] strValues, StreamDataType type) throws Exception {
		stopTestThread();

		LSLRecPluginEncoder plgEncoder = (LSLRecPluginEncoder) this.plugin;
		Encoder encoder = plgEncoder.getEncoder();
		OutputFileFormatParameters outFormat = new OutputFileFormatParameters();

		Tuple<String, Boolean> nameFile = FileUtils.checkOutputFileName("./plugin.dat", "test", "");
		outFormat.setParameter(OutputFileFormatParameters.OUT_FILE_NAME, nameFile.t1);
		outFormat.setParameter(OutputFileFormatParameters.OUT_FILE_FORMAT, encoder.getID());
		outFormat.setParameter(OutputFileFormatParameters.ZIP_ID, "");
		outFormat.setParameter(OutputFileFormatParameters.CHAR_CODING, Charset.forName("UTF-8"));
		outFormat.setParameter(OutputFileFormatParameters.PARALLELIZE, true);
		outFormat.setParameter(OutputFileFormatParameters.NUM_BLOCKS, 2L);
		outFormat.setParameter(OutputFileFormatParameters.BLOCK_DATA_SIZE,
				(int) (Math.pow(2, 20) * (Integer) ConfigApp.getProperty(ConfigApp.SEGMENT_BLOCK_SIZE)));
		outFormat.setParameter(OutputFileFormatParameters.DATA_NAMES, "");
		outFormat.setParameter(OutputFileFormatParameters.RECORDING_INFO, new HashMap<String, String>());
		outFormat.setParameter(OutputFileFormatParameters.DELETE_BIN, true);

		String name = "test";
		final IOutputDataFileWriter writer = encoder.getWriter(outFormat, getSimpleStreamSetting(name, type, chs),
				getDefaultMonitor());

		Object[] data = values;

		if (type == StreamDataType.string) {
			data = strValues;
		}

		final Object[] ddb = data;
		thr = new AbstractStoppableThread() {
			int seq = 0;

			@Override
			protected void runInLoop() throws Exception {
				DataBlock db = DataBlockFactory.getDataBlock(type, seq, name, 1, ddb);

				writer.saveData(db);

				super.wait(500L);
			}

			@Override
			protected void preStopThread(int friendliness) throws Exception {
			}

			@Override
			protected void postStopThread(int friendliness) throws Exception {
			}

			@Override
			protected void cleanUp() throws Exception {
				super.cleanUp();

				writer.close();

				try {
					File f = new File(nameFile.t1);
					if (!f.delete()) {
						f.deleteOnExit();
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			protected void runExceptionManager(Throwable e) {
				if (!(e instanceof InterruptedException)) {
					super.runExceptionManager(e);
				}
			}
		};

		thr.startThread();
	}

	private void startSyncTest() throws Exception {
		stopTestThread();

		ILSLRecPluginSyncMethod syncplg = (ILSLRecPluginSyncMethod) this.plugin;

		LSLRecPluginSyncMethod met = syncplg.getSyncMethod();
		met.loadSyncSettings(syncplg.getSettings());

		met.taskMonitor(getDefaultMonitor());

		thr = met;

		thr.startThread();
	}

	/**
	 * 
	 * @author Manuel Merino Monge
	 *
	 */
	private class EndedPluginInspector extends AbstractStoppableThread {
		private Thread thread = null;

		public EndedPluginInspector(Thread thr) {
			this.thread = thr;
		}

		@Override
		protected void preStopThread(int friendliness) throws Exception {
		}

		@Override
		protected void postStopThread(int friendliness) throws Exception {
		}

		@Override
		protected void startUp() throws Exception {
			super.startUp();

			super.stopThread = (this.thread == null);
		}

		@Override
		protected void runInLoop() throws Exception {
			synchronized (this) {
				super.wait(300L);
			}

			if (thread.getState().equals(State.TERMINATED)) {
				super.stopThread(IStoppableThread.STOP_IN_NEXT_LOOP);
			}
		}

		@Override
		protected void cleanUp() throws Exception {
			super.cleanUp();

			if (thread instanceof LSLRecPluginTrial) {
				((LSLRecPluginTrial) thread).disposeTrialWindow();
			}

			btnStartTest.setSelected(false);
		}

		@Override
		protected void runExceptionManager(Throwable e) {
			if (!(e instanceof InterruptedException)) {
				super.runExceptionManager(e);
			}
		}
	}
}
