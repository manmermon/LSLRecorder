package lslrec.plugin.lslrecPlugin.test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.border.EmptyBorder;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.auxiliar.extra.FileUtils;
import lslrec.auxiliar.extra.Tuple;
import lslrec.auxiliar.tasks.INotificationTask;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.config.ConfigApp;
import lslrec.dataStream.family.setting.IStreamSetting.StreamLibrary;
import lslrec.dataStream.family.setting.SimpleStreamSetting;
import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlock;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlockFactory;
import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.dataStream.outputDataFile.format.IOutputDataFileWriter;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.plugin.lslrecPlugin.ILSLRecConfigurablePlugin;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
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
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JComboBox;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;

public class LSLRecPluginTesting
{
	private JPanel contentPane;

	private ILSLRecPlugin plugin = null;
	private JTextField textField;
	private JTextArea textArea; 
	
	private JSpinner time;
	
	private AbstractStoppableThread thr = null;
	
	private JToggleButton btnStartTest = null;
	
	private JFrame window = null;
	
	private JFrame plgWin = null;
	
	private EndedPluginInspector inspector = null;
	
	public LSLRecPluginTesting( ILSLRecPlugin plg )
	{
		this.plugin = plg;
		
		if( plugin == null )
		{
			throw new NullPointerException();
		}
	}
	
	
	/**
	 * Launch the application.
	 */
	public void startTest( )
	{
		EventQueue.invokeLater(new Runnable() 
		{
			public void run() 
			{
				try 
				{
					JFrame frame = PluginTestWindow();
					frame.setVisible( true );
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public JFrame PluginTestWindow() 
	{
		if( this.window == null )
		{
			this.window = new JFrame();
			
			this.window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
			this.window.setLocation( 100, 100 );
			
			Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
			d.width /= 2;
			d.height /= 2;
			
			this.window.setSize( d );
			
			this.window.setTitle( "Plugin testing: " + plugin.getID() );
			
			this.contentPane = new JPanel();
			this.contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			this.contentPane.setLayout(new BorderLayout(0, 0));
			
			this.window.setContentPane(contentPane);
						
			this.btnStartTest = new JToggleButton("Start test");
			this.contentPane.add(btnStartTest, BorderLayout.SOUTH);
			
			JPanel panel_east = new JPanel( new BorderLayout() );
			
			JPanel panel_east_nort = new JPanel( );
			panel_east_nort.setLayout( new BoxLayout( panel_east_nort, BoxLayout.X_AXIS ) );
			panel_east_nort.add( new JLabel( "Loop time (ms):") );
						
			this.time = new JSpinner( new SpinnerNumberModel( 500, 20, 500 * 20, 10 ) );
			panel_east_nort.add( this.time );
			JButton clearlog = new JButton( "clear" );
			clearlog.setBorder( BorderFactory.createRaisedSoftBevelBorder() );
			clearlog.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					textArea.setText( "" );
				}
			});
			panel_east_nort.add( clearlog );
			
			
			panel_east.add( panel_east_nort, BorderLayout.NORTH );
			
			textArea = new JTextArea();
			textArea.setWrapStyleWord(true);
			textArea.setEditable(false);
			textArea.setColumns(15);
			textArea.setBorder( BorderFactory.createTitledBorder( "log" ) );
			textArea.setLineWrap( false );			
			panel_east.add( new JScrollPane( textArea ), BorderLayout.CENTER );
			
			contentPane.add( panel_east, BorderLayout.EAST);
			
			JPanel panel = new JPanel();
			contentPane.add(panel, BorderLayout.NORTH);
			panel.setLayout(new BorderLayout(0, 0));
			
			JPanel panel_1 = new JPanel();
			panel.add(panel_1, BorderLayout.WEST);
			panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
			
			JComboBox< StreamDataType > comboBox = new JComboBox< StreamDataType >( StreamDataType.values() );
			comboBox.removeItem( StreamDataType.undefined );
			
			JLabel lblNewLabel = new JLabel("Data type:");
			panel_1.add(lblNewLabel);
			comboBox.setSelectedItem( StreamDataType.float32 );
			
			panel_1.add(comboBox);
			
			
			textField = new JTextField();
			textField.setBorder( BorderFactory.createTitledBorder( "values (separator ';')"));
			panel.add(textField, BorderLayout.CENTER);
			textField.setColumns(10);
			
			if( this.plugin instanceof ILSLRecConfigurablePlugin )
			{
				contentPane.add( ((ILSLRecConfigurablePlugin)this.plugin).getSettingPanel(), BorderLayout.CENTER );
			}
			
			btnStartTest.addItemListener( new ItemListener() 
			{	
				@Override
				public void itemStateChanged(ItemEvent arg0) 
				{
					JToggleButton b = (JToggleButton)arg0.getSource();
									
					if( b.isSelected() )
					{
						try
						{
							Number[] values = null;
							String[] strValues = null;
							
							StreamDataType datType = (StreamDataType)comboBox.getSelectedItem();
							if( datType != StreamDataType.string )
							{
								values = createNumberArray();
							}
							else
							{
								strValues = createStringArray();
							}
							
							switch ( plugin.getType() )
							{
								case COMPRESSOR:
								{
									startCompressorTest( values, strValues, datType );
		
									break;
								}
								case DATA_PROCESSING:
								{
									startProcessingTest( values, strValues, datType );
		
									break;
								}
								case ENCODER:
								{
									startEncoderTest( values, strValues, datType);
		
									break;
								}
								case SYNC:
								{
									startSyncTest();
		
									break;
								}
								case TRIAL:
								{
									startTrialTest();
		
									break;
								}
								default:
									break;
							}

							if( inspector != null )
							{
								inspector.stopThread( IStoppableThread.FORCE_STOP );
							}
							
							inspector = new EndedPluginInspector( thr );
							inspector.startThread();
						}
						catch (Exception e) 
						{
							b.setSelected( false );
						}
					}
					else
					{
						stopTestThread();
					}
				}
			});
		}
		
		return this.window;
	}
	
	private Number[] createNumberArray()
	{
		Number[] values = null;
		
		try
		{
			String[] strVal = createStringArray();
			
			if( strVal != null )
			{
				List< Number > list = new ArrayList<Number>();
				
				for( String str : strVal )
				{
					list.add( Double.parseDouble( str ) );
				}			
			}
		}
		catch( Exception e)
		{
		}
		
		return values;
	}
	
	private String[] createStringArray()
	{
		String[] values = null;
		
		String t = textField.getText().replaceAll("\\s+", "" );
		
		String[] strVal = t.split( ";" );
		
		if( strVal.length > 0 )
		{
			if(strVal.length == 1  )
			{
				if(!strVal[ 0 ].isEmpty())
				{
					values = strVal;
				}
			}
			else if( strVal[ strVal.length - 1 ] .isEmpty() )
			{
				values = Arrays.copyOf( strVal, strVal.length - 1 );
			}
			else
			{
				values = strVal;
			}
		}
		
		return values;
	}
	
	private void stopTestThread()
	{
		if( thr != null )
		{
			thr.stopThread( IStoppableThread.FORCE_STOP );
			thr = null;
		}
	}
	
	private SimpleStreamSetting getSimpleStreamSetting( String name, StreamDataType type )
	{
		return new SimpleStreamSetting( StreamLibrary.LSL
					, name
					, type
					, type
					, type
					, 1
					, 0
					, getClass().getCanonicalName()
					, getClass().getCanonicalName()
					, "testing"
					, null
					, 1 );
	}
	
	private ITaskMonitor getDefaultMonitor()
	{
		return new ITaskMonitor() 
				{	
					@Override
					public void taskDone( INotificationTask task ) throws Exception 
					{
						String msg = task.getResult( true ).toString();
						
						if( !msg.endsWith( "\n" ) )
						{
							msg += "\n";
						}
						
						textArea.append( ">> " + msg);
					}
				};
	}
	
	private void startCompressorTest( Number[] values, String[] strValues, StreamDataType type ) throws Exception
	{ 
		stopTestThread();
		
		LSLRecPluginCompressor compressor = (LSLRecPluginCompressor)this.plugin;
		
		byte[] bytes = null;
		
		if( type == StreamDataType.string )
		{
			bytes = ConvertTo.Transform.StringArray2byteArray( strValues );
		}
		else
		{
			bytes = ConvertTo.Transform.NumberArray2byteArra( values, type );
		}
		
		final byte[] dataByte = bytes;
		thr = new AbstractStoppableThread()
		{
			@Override
			protected void runInLoop() throws Exception 
			{	
				byte[] zipdata = compressor.getCompressor().zipData( dataByte );
				
				String ziplen = zipdata + "";
				
				if( zipdata != null )
				{
					ziplen = "" + zipdata.length;
				}
				
				textArea.append( compressor.getID() + ">> input data bytes = " + dataByte.length + " : compressed data bytes = " + ziplen + "\n" );
				
				this.wait( 500L );
			}
			
			@Override
			protected void preStopThread(int friendliness) throws Exception 
			{	
			}
			
			@Override
			protected void postStopThread(int friendliness) throws Exception 
			{	
			}
			
			@Override
			protected void runExceptionManager(Throwable e) 
			{
				if( !( e instanceof InterruptedException  ) )
				{
					super.runExceptionManager(e);
				}
			}
		};
		
		thr.startThread();
	}
	
	private void startProcessingTest( Number[] values, String[] strs, StreamDataType type ) throws Exception
	{
		stopTestThread();
		
		ILSLRecPluginDataProcessing plg = (ILSLRecPluginDataProcessing)this.plugin;
		LSLRecPluginDataProcessing proc = plg.getProcessing( getSimpleStreamSetting( plg.getID(), type  ), null );
		proc.loadProcessingSettings( plg.getSettings() );
		
		int len = proc.getBufferLength();
		
		if( len <= 0 )
		{
			len = values.length;
		}
		
		Number[] data = Arrays.copyOf( values, len );
		
		thr = new AbstractStoppableThread() {
			
			@Override
			protected void runInLoop() throws Exception 
			{
				Number[] res = proc.processDataBlock( data );
				
				String r = res + "";
				if( res != null )
				{
					r = Arrays.deepToString( res );
				}
				textArea.append( plg.getID() + ">> input data = " + Arrays.deepToString( data )+ " : compressed data bytes = " + r + "\n" );
			}
			
			@Override
			protected void preStopThread(int friendliness) throws Exception 
			{				
			}
			
			@Override
			protected void postStopThread(int friendliness) throws Exception 
			{	
			}
			
			@Override
			protected void runExceptionManager(Throwable e) 
			{
				if( !( e instanceof InterruptedException  ) )
				{
					super.runExceptionManager(e);
				}
			}
		};
	}
	
	private void startTrialTest( ) throws Exception
	{
		stopTestThread();
		
		ILSLRecPluginTrial trial = (ILSLRecPluginTrial)this.plugin;
		
		LSLRecPluginTrial test = trial.getGUIExperiment();
		
		test.loadSettings( trial.getSettings() );
		
		test.taskMonitor( getDefaultMonitor() );
		
		JFrame window = test.getWindonw();
		
		window.setVisible( false );
		
		window.setSize( 400, 400 );
		window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
		
		if( window.getTitle() == null || window.getTitle().trim().isEmpty() )
		{
			window.setTitle( this.window.getTitle() );
		}
		
		window.setVisible( true );
		window.toFront();		
		
		plgWin = window;
		
		thr = test;
		
		thr.startThread();		
	}
	
	private void startEncoderTest( Number[] values, String[] strValues, StreamDataType type ) throws Exception
	{
		stopTestThread();
		
		LSLRecPluginEncoder plgEncoder = (LSLRecPluginEncoder)this.plugin;
		Encoder encoder = plgEncoder.getEncoder();
		OutputFileFormatParameters outFormat = new OutputFileFormatParameters();
		
		Tuple< String, Boolean > nameFile = FileUtils.checkOutputFileName( "./plugin.dat", "test" );
		outFormat.setParameter( OutputFileFormatParameters.OUT_FILE_NAME, nameFile.t1 );		
		outFormat.setParameter( OutputFileFormatParameters.OUT_FILE_FORMAT, encoder.getID() );		
		outFormat.setParameter( OutputFileFormatParameters.ZIP_ID, "" );
		outFormat.setParameter( OutputFileFormatParameters.CHAR_CODING,  Charset.forName( "UTF-8" )  );		
		outFormat.setParameter( OutputFileFormatParameters.PARALLELIZE, true );
		outFormat.setParameter( OutputFileFormatParameters.NUM_BLOCKS, 2L );
		outFormat.setParameter( OutputFileFormatParameters.BLOCK_DATA_SIZE, ConfigApp.DEFAULT_SEGMENTATION_BLOCK_SIZE );		
		outFormat.setParameter( OutputFileFormatParameters.DATA_NAMES, "" );		
		outFormat.setParameter( OutputFileFormatParameters.RECORDING_INFO, new HashMap< String, String >() );				
		outFormat.setParameter( OutputFileFormatParameters.DELETE_BIN, true );
		
		String name = "test";
		final IOutputDataFileWriter writer = encoder.getWriter( outFormat, getSimpleStreamSetting( name, type ), getDefaultMonitor() );
		
		Object[] data = values;
		
		if( type == StreamDataType.string )
		{
			data = strValues;
		}
				
		final Object[] ddb = data;
		thr = new AbstractStoppableThread() 
		{	
			int seq = 0;
			@Override
			protected void runInLoop() throws Exception 
			{
				DataBlock db = DataBlockFactory.getDataBlock( type, seq, name, 1, ddb );
						
				writer.saveData( db );
				
				super.wait( 500L );
			}
			
			@Override
			protected void preStopThread(int friendliness) throws Exception 
			{	
			}
			
			@Override
			protected void postStopThread(int friendliness) throws Exception 
			{	
			}
			
			@Override
			protected void cleanUp() throws Exception 
			{
				super.cleanUp();
				
				writer.close();
				
				try
				{
					File f = new File( nameFile.t1 );
					if( !f.delete() )
					{
						f.deleteOnExit();
					}					
				}
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
			
			@Override
			protected void runExceptionManager(Throwable e) 
			{
				if( !( e instanceof InterruptedException  ) )
				{
					super.runExceptionManager(e);
				}
			}
		};
		
		thr.startThread();
	}
	
	private void startSyncTest( ) throws Exception
	{
		stopTestThread();
		
		ILSLRecPluginSyncMethod syncplg = (ILSLRecPluginSyncMethod)this.plugin;
		
		LSLRecPluginSyncMethod met = syncplg.getSyncMethod();
		met.loadSyncSettings( syncplg.getSettings() );
		
		met.taskMonitor( getDefaultMonitor() );
		
		thr = met;
		
		thr.startThread();
	}
	
	private class EndedPluginInspector extends AbstractStoppableThread
	{
		private Thread thread = null;
		
		public EndedPluginInspector( Thread thr ) 
		{
			this.thread = thr;
		}

		@Override
		protected void preStopThread(int friendliness) throws Exception 
		{	
		}

		@Override
		protected void postStopThread(int friendliness) throws Exception 
		{	
		}

		@Override
		protected void startUp() throws Exception 
		{
			super.startUp();
			
			super.stopThread = ( this.thread == null );
		}
		
		@Override
		protected void runInLoop() throws Exception 
		{	
			synchronized ( this )
			{
				super.wait( 300L );
			}
						
			if( thread.getState().equals( State.TERMINATED ) )
			{
				super.stopThread( IStoppableThread.STOP_IN_NEXT_LOOP );
			}
		}
		
		@Override
		protected void cleanUp() throws Exception 
		{
			super.cleanUp();
			
			if( plgWin != null )
			{
				plgWin.dispose();
				
				plgWin = null;
			}			
			
			btnStartTest.setSelected( false );
		}
		
		@Override
		protected void runExceptionManager(Throwable e) 
		{
			if( !( e instanceof InterruptedException ) )
			{
				super.runExceptionManager(e);
			}
		}
	}
}