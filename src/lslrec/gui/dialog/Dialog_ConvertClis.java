/**
 * 
 */
package lslrec.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import lslrec.auxiliar.WarningMessage;
import lslrec.auxiliar.extra.ConvertTo;
import lslrec.auxiliar.extra.FileUtils;
import lslrec.auxiliar.extra.Tuple;
import lslrec.auxiliar.task.INotificationTask;
import lslrec.auxiliar.task.ITaskMonitor;
import lslrec.auxiliar.thread.LostWaitedThread;
import lslrec.config.ConfigApp;
import lslrec.config.ParameterList;
import lslrec.config.SettingOptions;
import lslrec.config.language.Language;
import lslrec.control.message.AppState;
import lslrec.dataStream.convertData.clis.ClisData;
import lslrec.dataStream.convertData.clis.MetadataVariableBlock;
import lslrec.dataStream.family.setting.IStreamSetting.StreamLibrary;
import lslrec.dataStream.family.setting.MutableStreamSetting;
import lslrec.dataStream.family.setting.SimpleStreamSetting;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlock;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlockFactory;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.dataStream.outputDataFile.format.IOutputDataFileWriter;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.tools.StreamUtils;
import lslrec.dataStream.tools.StreamUtils.StreamDataType;
import lslrec.exceptions.SettingException;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.gui.miscellany.BasicPainter2D;
import lslrec.gui.miscellany.GeneralAppIcon;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import java.awt.Container;
import javax.swing.JSplitPane;

/**
 * @author Manuel Merino Monge
 *
 */
public class Dialog_ConvertClis extends JDialog 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2257212620236071644L;
	
	private JPanel centerPanel = null;
	private JPanel southPanel;	
	private JPanel outputFormatPanel;
	private JPanel westOutputFormatPanel;
	private JPanel centerOutputFormatPanel;
	private JPanel eastOutputFormatPanel;	
	private JPanel loadFilePanel;
	private JPanel inputFilePanel;
	private JPanel infoFilePanel;
	private JPanel northInputFilePanel;
	private JPanel loadFileBtnPanel;
	
	private JButton btnLoadFile;
	private JButton btnLoadRecursiveFilesFromFolder;
	private JButton btnOutFormatOptions;
	private JButton btnOk;
	private JButton btnCancel;
	//private JButton btnOutFolder;
	private JButton btnShowFileInfo;
	
	private JCheckBox chbxEncrypt;
	
	//private JTextField txtOutFileFolder;
	
	private JLabel lblLoadFile;	
	
	private JTable tableFileData;
	
	private JComboBox< String > fileFormat;
	
	private JSplitPane splitCenterPane;
	
	private Dialog_ProgressTask progressTaskBar;
	
	private OutputFileFormatParameters outFormat;
	private String currentFolderPath;
	private static ClisData currentClisFile = null;
	
	private AbstractStoppableThread convertThread = null;
	private Object sync = new Object();
	
	/**
	 * Launch the application.
	 */
	private static Dialog_ConvertClis dgclis = null;
	
	public static void main(String[] args) {
		try {
					
			JFrame jf = new JFrame();
			
			JButton jb = new JButton( "show" );
			
			jb.addActionListener( new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					showJdialog(); 
				}
			});
			
			jf.getContentPane().add( jb );
			jf.setBounds( 100, 100 , 300, 100 );
			
			jf.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
			
			jf.addWindowListener( new WindowAdapter() {
				@Override
				public void windowClosing(WindowEvent e) 
				{
					if( dgclis != null )
					{
						dgclis.dispose();
					}
				}
			});
			
			jf.setVisible( true );
						
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void showJdialog()
	{
		if( dgclis != null )
		{
			dgclis.dispose();
		}
		
		dgclis = new Dialog_ConvertClis();
		dgclis.setBounds( 200, 100, 400, 400 );
		
		dgclis.addWindowListener( new WindowAdapter() 
		{
			@Override
			public void windowClosing(WindowEvent e) 
			{
				if( currentClisFile != null )
				{
					try 
					{
						currentClisFile.close();
					} 
					catch (IOException ex)
					{
					}
				}
			}
		});
		
		dgclis.setVisible( true );
	}
	
 	private void checkEncryptKey()
	{
		if( getChckboxEncrypt().isSelected() )
		{	
			Dialog_Password dg = new Dialog_Password( this, Language.getLocalCaption( Language.ENCRYPT_KEY_TEXT ) );
			
			dg.setLocationRelativeTo( this );
			
			dg.setVisible( true );
			
			while( dg.getState() == Dialog_Password.PASSWORD_INCORRECT )
			{
				dg.setMessage( dg.getPasswordError() + Language.getLocalCaption( Language.REPEAT_TEXT ) + "." );
				dg.setVisible( true );
			}
			
			if( dg.getState() != Dialog_Password.PASSWORD_OK )
			{	
				JOptionPane.showMessageDialog( super.getOwner(), Language.getLocalCaption( Language.PROCESS_TEXT ) 
													+ " " + Language.getLocalCaption( Language.CANCEL_TEXT ) );
			}
			else
			{
				String key = dg.getPassword();
				
				this.outFormat.setParameter( OutputFileFormatParameters.ENCRYPT_KEY, key );
			}
		}
	}
	
	/**
	 * Create the dialog.
	 */
	public Dialog_ConvertClis() 
	{	
		this.currentFolderPath = System.getProperty( "user.dir" );
		this.outFormat = DataFileFormat.getDefaultOutputFileFormatParameters();
		
		super.setBounds( 100, 100, 450, 300 );
		
		this.progressTaskBar = new Dialog_ProgressTask( this );
		this.progressTaskBar.setTitle( this.getTitle() );
		this.progressTaskBar.setVisible( false );
		this.progressTaskBar.setResizable( false );
		this.progressTaskBar.pack();
		Dimension sptb = this.progressTaskBar.getSize();
		sptb.width = ( super.getSize().width * 3 ) / 4; 
		this.progressTaskBar.setSize( sptb );
		
		this.progressTaskBar.setDefaultCloseOperation( JDialog.DO_NOTHING_ON_CLOSE );
		
		this.progressTaskBar.addWindowListener( new WindowAdapter()
		{			
			@Override
			public void windowClosing(WindowEvent e) 
			{
				synchronized ( sync ) 
				{
					if( convertThread != null )
					{
						int sel = JOptionPane.showConfirmDialog( getContentPane(), Language.getLocalCaption( Language.MSG_CANCEL_PROCESS ) );
						
						if( sel == JOptionPane.YES_OPTION )
						{
							convertThread.stopThread( IStoppableThread.FORCE_STOP );
							convertThread = null;
							
							dispose();
						}
					}
				}
			}
		} );
		
		
		super.getContentPane().setLayout( new BorderLayout() );
		
		Container container = super.getContentPane();
		container.add( this.getCenterPanel(), BorderLayout.CENTER);
		super.getContentPane().add( this.getSouthPanel(), BorderLayout.SOUTH);
		super.getContentPane().add( this.getLoadFilePanel(), BorderLayout.NORTH);
	}
	
	private JPanel getCenterPanel()
	{
		if( this.centerPanel == null )
		{
			this.centerPanel = new JPanel();
			
			this.centerPanel.setLayout( new BorderLayout( 0, 5 ) );
			this.centerPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
			
			this.centerPanel.add( this.getOutputFormatPanel(), BorderLayout.SOUTH );			
			this.centerPanel.add( this.getSplitCenterPane(), BorderLayout.CENTER);			
		}
		
		return this.centerPanel;
	}
	
	private JPanel getNorthInputFilePanel()
	{
		if( this.northInputFilePanel == null )
		{
			this.northInputFilePanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) );
			
			this.northInputFilePanel.add( this.getBtnShowFileInfo() );
		}
		
		return this.northInputFilePanel;
	}

	private JPanel getSouthPanel() 
	{
		if (southPanel == null) 
		{
			southPanel = new JPanel();
			southPanel.setLayout( new FlowLayout( FlowLayout.RIGHT ));
			
			southPanel.add( this.getBtnOk() );
			southPanel.add( this.getBtnCancel());
		}
		return southPanel;
	}
	
	private JButton getBtnOk() 
	{
		if (btnOk == null) 
		{
			btnOk = new JButton( Language.getLocalCaption( Language.OK_TEXT ) );
			
			final Dialog_ConvertClis dcc = this;
			this.btnOk.addActionListener( new ActionListener() 
			{				
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					synchronized( sync )
					{
						convertThread = new AbstractStoppableThread()
						{
							ClisData clis;
							IOutputDataFileWriter wr;
							File ff;
							
							public void runInLoop() 
							{
								checkEncryptKey();
								
								JTable t = getTableFileData();
									
								Timer actTimer = null;
								
								Thread thrExc = this;
								
								dcc.setEnabled( false );
								
								String idEncoder = outFormat.getParameter( OutputFileFormatParameters.OUT_FILE_FORMAT ).getValue().toString();
								
								try
								{	
									Tuple< Encoder, WarningMessage > tEnc = DataFileFormat.getDataFileEncoder( idEncoder );
									
									Encoder enc = tEnc.t1;
									WarningMessage wm = tEnc.t2;
									
									if( enc != null )
									{
										if( wm.getWarningType() != WarningMessage.ERROR_MESSAGE )
										{
											int nFiles = t.getRowCount();
																											
											progressTaskBar.setMaximum( nFiles );
											progressTaskBar.setTitle( dcc.getTitle() + " " +idEncoder );
											progressTaskBar.setVisible( true );
																					
											if( wm.getWarningType() == WarningMessage.WARNING_MESSAGE )
											{
												ExceptionMessage msg = new ExceptionMessage( new SettingException( wm.getMessage() ) , Language.getLocalCaption( Language.MSG_WARNING ), ExceptionMessage.WARNING_MESSAGE );
												ExceptionDialog.showMessageDialog( msg, true, true );
											}
	
											String outExtFile = DataFileFormat.getSupportedFileExtension().get( idEncoder );
											if( outExtFile == null )
											{
												outExtFile = "." + idEncoder;
											}								
																					
											for( int i = 0; i < nFiles; i++ )
											{
												String file = t.getValueAt( i, 0).toString();
												
												ff = new File( file );
												
												if( actTimer != null )
												{
													actTimer.stop();
												}
												
												final int ii = i;
												actTimer =  new Timer( 1100, new ActionListener() 
												{									
													int c = 0;
													
													@Override
													public void actionPerformed(ActionEvent e) 
													{
														if( thrExc.getState().equals( Thread.State.RUNNABLE ) )
														{
															c++;														
															
															switch( c )
															{
																case 1:
																{
																	updateProgress( "/ " + ff.getName(), ii );
																	
																	break;
																}
																case 2:
																{
																	updateProgress( "- " + ff.getName(), ii );
																	
																	break;
																}
																case 3:
																{
																	updateProgress( "\\ " + ff.getName(), ii );
																	
																	break;
																}
																default:
																{
																	updateProgress( "| " + ff.getName(), ii );
																	
																	break;
																}
															}
														}
														
														c = c % 4;
													}
												});
												
												actTimer.setRepeats( true );
												actTimer.start();
												
												updateProgress(  "| " + ff.getName(), i );
																							
												String outFile = file + outExtFile;
												if( file.endsWith( ".clis" ) )
												{
													int lastDot = file.lastIndexOf( "." );
	
													outFile = file.substring( 0, lastDot ) + outExtFile;
												}
	
												Tuple< String, Boolean > check = FileUtils.checkOutputFileName( outFile, "", "");
	
												if( !check.t2 )
												{
													outFile = check.t1;
												}
												
												outFormat.setParameter( OutputFileFormatParameters.OUT_FILE_NAME, outFile);
												
												ITaskMonitor itm = new ITaskMonitor() 
												{											
													@Override
													public void taskDone(INotificationTask task) throws Exception 
													{	
														
													}
												};
												
	
												clis = new ClisData( file );
												
												List< MetadataVariableBlock > lmvb = clis.getVarInfo();
												
												List< String > varNames = new ArrayList< String >();
												String v = "";
												StreamDataType varType = StreamDataType.double64;
												int nChs = 0;
												for( MetadataVariableBlock vb : lmvb )
												{
													varNames.add( vb.getName() );
													nChs += vb.getCols();
													v += vb.getName();												
												}
	
												String header = clis.getHeader();
												
												SimpleStreamSetting sst = new SimpleStreamSetting( StreamLibrary.LSL
																									, v
																									, varType
																									, nChs
																									, 1
																									, 0
																									, 3
																									, true
																									, ""
																									, ""
																									, null );
	
												MutableStreamSetting msst = new MutableStreamSetting( sst );
												msst.setDescription( header );
												outFormat.setParameter( OutputFileFormatParameters.DATA_NAMES, varNames.toString() );
												
												wr = enc.getWriter( outFormat, msst, itm );
																							
												int iSeq = 0;
												for( int iv = 0; iv < lmvb.size(); iv++ )
												{												
													MetadataVariableBlock vb = lmvb.get( iv );
													
													double memorySize = (Integer)ConfigApp.getProperty( ConfigApp.SEGMENT_BLOCK_SIZE ) * Math.pow( 2, 20 );
													int dataByteSize = StreamUtils.getDataTypeBytes( vb.getDataType() );
													
													int chunkSize = (int)( memorySize / dataByteSize );
													if( chunkSize < 1 )
													{
														chunkSize = 1;
													}
													
													nChs = vb.getCols();
													String var = vb.getName();
													varType = vb.getDataType();
														
													boolean cont = true;
													while( cont )
													{
														Number[][] vData = clis.importNextDataBlock( iv, chunkSize );
														
														cont = ( vData != null );
														
														if( cont )
														{
															DataBlock db = DataBlockFactory.getDataBlock( varType, iSeq, var, nChs, ConvertTo.Transform.Matrix2Array( vData ) );
		
															wr.saveData( db );
		
															iSeq++;
														}
													}
												}
												
												wr.addMetadata( "", header );
												while( !wr.isFinished() )
												{
													synchronized ( this )
													{
														super.wait( 100L );
													}
												}
												
												updateProgress( ff.getName(), i + 1 );
												
												wr.close();
											}
										}
										else
										{											
											throw new SettingException( wm.getMessage() );
										}
									}
								}
								catch (Exception ex) 
								{
									LostWaitedThread.getInstance().wakeup();
									
									ExceptionMessage msg = new ExceptionMessage( ex, Language.getLocalCaption( Language.DIALOG_ERROR ), ExceptionMessage.ERROR_MESSAGE );
									ExceptionDialog.showMessageDialog( msg, true, true );
	
									ex.printStackTrace();
								}
								finally 
								{
									if( actTimer != null )
									{
										actTimer.stop();
									}
									
									dcc.setEnabled( false );
									
									super.stopThread = true;
								}
								//JOptionPane.showMessageDialog( dcc, AppState.State.SAVED );
								
								synchronized( sync )
								{
									convertThread = null;
								}
								
								progressTaskBar.dispose();
								
								JOptionPane.showConfirmDialog( dcc
																, AppState.State.SAVED
																, dcc.getTitle() + " " + idEncoder
																, JOptionPane.DEFAULT_OPTION
																, JOptionPane.INFORMATION_MESSAGE, null );
								
								dcc.dispose();
							}

							@Override
							protected void preStopThread(int friendliness) throws Exception 
							{}

							@Override
							protected void postStopThread(int friendliness) throws Exception 
							{}
							
							protected void cleanUp() throws Exception 
							{
								wr.close();
								clis.close();
								
								synchronized ( this )
								{
									super.wait( 1000L );
								}
							};
						};
						
						convertThread.setName( "exc-convertClis");
						
						try 
						{
							convertThread.startThread();
						} 
						catch (Exception e1) 
						{
							e1.printStackTrace();
						}
					}
					
				}
			});
		}
		
		return btnOk;
	}
	
	private void updateProgress( String note, int v )
	{
		//progressTaskBar.setVisible( false );
		progressTaskBar.setLocationRelativeTo( this );
		progressTaskBar.setNote( note );
		progressTaskBar.setProgress( v );
		//progressTaskBar.setVisible( true );
	}
	
	private JButton getBtnCancel() 
	{
		if (btnCancel == null) 
		{
			btnCancel = new JButton( Language.getLocalCaption( Language.CANCEL_TEXT ) );
			
			this.btnCancel.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{
					dispose();
				}
			});
		}
		return btnCancel;
	}
	
	private JPanel getLoadFilePanel() 
	{
		if ( this.loadFilePanel == null ) 
		{
			this.loadFilePanel = new JPanel( );
			loadFilePanel.setLayout( new BorderLayout(0, 0) );
			
			this.loadFilePanel.add( this.getNorthInputFilePanel(), BorderLayout.EAST );
			this.loadFilePanel.add( this.getLoadFileBtnPanel(), BorderLayout.WEST);
		}
		return this.loadFilePanel;
	}
	
	private JButton getBtnLoadFile() 
	{
		if (btnLoadFile == null) {
			
			btnLoadFile = new JButton(  );
			
			this.btnLoadFile.setBorder( BorderFactory.createRaisedSoftBevelBorder( ) );
			
			ImageIcon ic = GeneralAppIcon.Folder( 24, 20, Color.BLACK, Color.ORANGE );
			
			if( ic != null )
			{
				this.btnLoadFile.setIcon( ic );
			}
			else
			{
				this.btnLoadFile.setText( Language.getLocalCaption( Language.LOAD_TEXT ) );
			}
			
			this.btnLoadFile.addActionListener( new ActionListener( ) 
			{
				public void actionPerformed( ActionEvent e ) 
				{											
					String[] FILES = getClisFiles( false );
					if( FILES != null )
					{						
						loadFile2Table( FILES );
						
						currentFolderPath = (new File( FILES[ 0 ] ) ).getAbsolutePath();
					}
				}
			} );
			
		}
		return btnLoadFile;
	}
	
	private String[] getClisFiles( boolean recursive )
	{
		String idEncoder = DataFileFormat.CLIS;
		String ext = DataFileFormat.getSupportedFileExtension().get( idEncoder );
						
		boolean multiSel = !recursive;
		int selFilesOrDir = ( recursive ) ? JFileChooser.DIRECTORIES_ONLY : JFileChooser.FILES_ONLY;
		
		String[] selExt = null;
		
		if( ext != null && !recursive )
		{						
			if( ext.charAt( 0 ) == '.' )
			{
				ext = ext.substring( 1 );
			}
			selExt = new String[] { ext };
		}
				
		String[] FILES = FileUtils.selectUserFile( "", true, multiSel, selFilesOrDir, idEncoder, selExt, this.currentFolderPath );
		
		if( recursive )
		{
			List< String > files = new ArrayList<String>();
			
			List<Path> allFiles = new ArrayList< Path >();
			for( String dir : FILES )
			{											 
				try 
				{
					listAllFiles( Paths.get( dir ), ext, allFiles );
				}
				catch (IOException e1) 
				{
				}
			}
			
			for( Path file : allFiles )
			{	
				String fileName = file.toFile().getAbsolutePath().toString();
				files.add( fileName );
			}
			
			FILES = files.toArray( new String[0] );
		}
		
		return FILES;
	}
	
	private void loadFile2Table( String[] Files )
	{
		if( Files != null )
		{
			JTable t = getTableFileData();
			
			int rc = t.getRowCount();
			if( rc > 0)
			{
				for( int i = rc-1; i >= 0; i-- )
				{
					((DefaultTableModel)t.getModel()).removeRow( i );
				}
			}
			
			for( String file : Files )
			{	
				insertBinaryFilesInTable( t, file );
			}
		}
	}
	
	private JButton getBtnLoadRecursiveFiles() 
	{
		if (btnLoadRecursiveFilesFromFolder == null) {
			
			btnLoadRecursiveFilesFromFolder = new JButton(  );
			
			this.btnLoadRecursiveFilesFromFolder.setBorder( BorderFactory.createRaisedSoftBevelBorder( ) );
			this.btnLoadRecursiveFilesFromFolder.setForeground( Color.RED );
			
			ImageIcon ic = GeneralAppIcon.Folder( 24, 20, Color.BLACK, Color.RED );
						
			if( ic != null )
			{
				Image imgR = BasicPainter2D.paintText( "R", this.btnLoadRecursiveFilesFromFolder.getFontMetrics( new Font( Font.DIALOG, Font.PLAIN, 10 ) )
														, null, Color.BLACK, null );
				
				Image folder = ic.getImage();
				BasicPainter2D.compoundImages( folder, folder.getWidth( null ) - imgR.getWidth( null) - 1
													, folder.getHeight( null ) - imgR.getHeight( null )+1
													, imgR );
				
				this.btnLoadRecursiveFilesFromFolder.setIcon( new ImageIcon( folder ) );
			}
			else
			{
				this.btnLoadRecursiveFilesFromFolder.setText( Language.getLocalCaption( Language.LOAD_TEXT ) );
			}
			
			this.btnLoadRecursiveFilesFromFolder.addActionListener( new ActionListener( ) 
			{
				public void actionPerformed( ActionEvent e ) 
				{	
					String[] FILES = getClisFiles( true );
					
					if( FILES != null && FILES.length > 0 )
					{						
						loadFile2Table( FILES );
						
						currentFolderPath = (new File( FILES[ 0 ] ) ).getAbsolutePath();
					}
				}
			} );
			
		}
		return btnLoadRecursiveFilesFromFolder;
	}
	
	private void listAllFiles( Path currentPath, String fileExtension,  List<Path> allFiles) throws IOException  
	{ 
		try ( DirectoryStream<Path> stream = Files.newDirectoryStream( currentPath ) )  
		{ 
			for (Path entry : stream) 
			{ 
				if ( Files.isDirectory( entry ) ) 
				{ 
					listAllFiles(entry, fileExtension, allFiles); 
				}
				else 
				{ 
					if( fileExtension != null && !fileExtension.trim().isEmpty() )
					{
						PathMatcher matcher = FileSystems.getDefault().getPathMatcher( "glob:*" + fileExtension );
						if( matcher.matches( entry.getFileName() ) )
						{
							allFiles.add( entry );
						}
					}
					else
					{
						allFiles.add( entry ); 
					}
				} 
			} 
		} 
	} 
	
	private JLabel getLblLoadFile() 
	{
		if (lblLoadFile == null) 
		{
			lblLoadFile = new JLabel( Language.getLocalCaption( Language.LOAD_TEXT ) );
		}
		
		return lblLoadFile;
	}
	
	private JPanel getOutputFormatPanel() 
	{
		if (outputFormatPanel == null) 
		{
			outputFormatPanel = new JPanel( new BorderLayout( 0, 0 ) );
									
			this.outputFormatPanel.add( this.getWestOutputFormatPanel(), BorderLayout.WEST );
			this.outputFormatPanel.add( this.getCenterOutputFormatPanel(), BorderLayout.CENTER );
			this.outputFormatPanel.add( this.getEastOutputFormatPanel(), BorderLayout.EAST );
		}
		
		return outputFormatPanel;
	}
	
	private JPanel getWestOutputFormatPanel()
	{
		if( this.westOutputFormatPanel == null )
		{
			this.westOutputFormatPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 5, 0 ) );
			
			this.westOutputFormatPanel.add( this.getComboBoxOutputFormat() );
			this.westOutputFormatPanel.add( this.getOutputFormatOptsButton() );
			this.westOutputFormatPanel.add( this.getChckboxEncrypt() );
		}
		
		return this.westOutputFormatPanel;
	}
	
	private JPanel getCenterOutputFormatPanel()
	{
		if( this.centerOutputFormatPanel == null )
		{
			this.centerOutputFormatPanel = new JPanel( );
			BoxLayout bl = new BoxLayout( this.centerOutputFormatPanel, BoxLayout.X_AXIS );
			this.centerOutputFormatPanel.setLayout( bl );
			
			//this.centerOutputFormatPanel.add( this.getTxtOutFileFolder() );
		}
		
		return this.centerOutputFormatPanel;
	}
	
	private JPanel getEastOutputFormatPanel()
	{
		if( this.eastOutputFormatPanel == null )
		{
			this.eastOutputFormatPanel = new JPanel( new FlowLayout( FlowLayout.LEFT, 0, 0 ) );
			
			//this.eastOutputFormatPanel.add( this.getBtnOutFolder() );
		}
		
		return this.eastOutputFormatPanel;
	}
	
	private JComboBox< String > getComboBoxOutputFormat( ) 
	{
		if ( fileFormat == null )
		{
			fileFormat = new JComboBox< String >( );
			
			String[] formats = DataFileFormat.getSupportedFileFormat( );
			for( int i = 0; i < formats.length; i++ )
			{
				this.fileFormat.addItem( formats[ i ] );
			}
			
			//this.fileFormat.removeItem( DataFileFormat.CLIS );
			
			fileFormat.addItemListener( new ItemListener( ) 
			{				
				@Override
				public void itemStateChanged( ItemEvent e ) 
				{
					if( e.getStateChange( ) == ItemEvent.SELECTED )
					{
						JComboBox< String > cb = ( JComboBox<String> )e.getSource( );
						String f = cb.getSelectedItem( ).toString( );
						outFormat.setParameter( OutputFileFormatParameters.OUT_FILE_FORMAT, f );			
						
						Tuple< Encoder, WarningMessage > tenc = DataFileFormat.getDataFileEncoder( f );
						Encoder enc = tenc.t1;
						
						List< SettingOptions > opts = enc.getSettiongOptions();
						
						getOutputFormatOptsButton().setEnabled( opts != null && !opts.isEmpty() );
					}
				}
			} );
			
			fileFormat.setSelectedIndex( -1 );
			fileFormat.setSelectedIndex( 0 );
		}

		return fileFormat;
	}
	
	private JButton getOutputFormatOptsButton()
	{
		if( this.btnOutFormatOptions == null )
		{
			this.btnOutFormatOptions = new JButton();
			this.btnOutFormatOptions.setEnabled( false );
			
			int s = this.getComboBoxOutputFormat().getPreferredSize().height;
			
			this.btnOutFormatOptions.setPreferredSize( new Dimension( s, s ) );
			
			s = (int)( s * 0.75D);
			
			this.btnOutFormatOptions.setIcon( new ImageIcon( GeneralAppIcon.Config2( Color.BLACK )
																		.getScaledInstance( s, s, Image.SCALE_SMOOTH ) ) ); // GeneralAppIcon.Pencil( s, Color.BLACK ) );
			this.btnOutFormatOptions.setBorder( BorderFactory.createEtchedBorder() );
			
			final JDialog ref = this;
			this.btnOutFormatOptions.addActionListener( new ActionListener()
			{
				@Override
				public void actionPerformed(ActionEvent arg0) 
				{
					Object format = getComboBoxOutputFormat().getSelectedItem();
					if( format != null )
					{
						/*
						JDialog dial = new JDialog( ref );
						
						dial.setModal( true );
						dial.getContentPane().setLayout( new BorderLayout() );
						dial.setDefaultCloseOperation( JDialog.DISPOSE_ON_CLOSE );
						
						JPanel main = new JPanel( new BorderLayout() );
						main.setBackground( Color.green );
						
						
						dial.setTitle( format.toString() + " - " + Language.getLocalCaption( Language.SETTING_LSL_OUTPUT_FORMAT ) );

						
						Tuple< Encoder, WarningMessage > tenc = DataFileFormat.getDataFileEncoder( format.toString() );
						Encoder enc = tenc.t1;
						List< SettingOptions > opts = enc.getSettiongOptions();
						
						ParameterList encPars = enc.getParameters();
						
						for( SettingOptions opt : opts )
						{	
							Parameter p = outFormat.getParameter( opt.getIDReferenceParameter() );
							Parameter p2  = encPars.getParameter( opt.getIDReferenceParameter() );
							
							String id = opt.getIDReferenceParameter();
							Object val = null;
							String langID = null;
									
							if( p != null )
							{
								val = p.getValue();
								langID = p.getLangID();
							}
							
							if( val == null )
							{
								val = p2.getValue();
								langID = p2.getLangID();
							}
							
							if( langID == null || langID.isEmpty() )
							{
								langID = p2.getLangID();
							}
							
							outFormat.setParameter( id, val );							
							outFormat.getParameter( id ).setLangID( langID );
						}
						
						
						JScrollPane scr = new JScrollPane( CreatorDefaultSettingPanel.getSettingPanel( opts, outFormat.getAllParameters() ) );

						main.add( scr, BorderLayout.CENTER );
						
						dial.getContentPane().add( main );
						
						dial.setLocationRelativeTo( ref );					
						dial.pack();
						
						Dimension s = dial.getSize();
						FontMetrics fm = dial.getFontMetrics( dial.getFont() );
						
						int t = fm.stringWidth( dial.getTitle() ) * 2;
						if( t > s.width )
						{
							s.width = t;
						}
						s.height += 10;
						
						dial.setSize( s );
						
						dial.setVisible( true );
						//*/
						
						Tuple< Encoder, WarningMessage > tenc = DataFileFormat.getDataFileEncoder( format.toString() );
						Encoder enc = tenc.t1;
						List< SettingOptions > opts = enc.getSettiongOptions();
						
						ParameterList pars = enc.getParameters();
						
						Dialog_AdvancedOptions dial = new Dialog_AdvancedOptions( opts, pars );
						dial.setTitle( format.toString() + " - " + Language.getLocalCaption( Language.SETTING_LSL_OUTPUT_FORMAT ) );
						
						dial.setLocationRelativeTo( ref );
						dial.setResizable( false );
						dial.setVisible( true );											
						dial.pack();
					}
				}
			});
		}
		
		return this.btnOutFormatOptions;
	}
	
	private JCheckBox getChckboxEncrypt() 
	{
		if ( this.chbxEncrypt == null ) 
		{
			this.chbxEncrypt = new JCheckBox( Language.getLocalCaption( Language.ENCRYPT_KEY_TEXT ) );			
		}
		
		return chbxEncrypt;
	}
	
	/*
	private JTextField getTxtOutFileFolder( ) 
	{
		if ( txtOutFileFolder == null )
		{
			txtOutFileFolder = new JTextField( );			
			txtOutFileFolder.setColumns( 10 );
			
			Dimension s = new Dimension();
			
			FontMetrics fm = txtOutFileFolder.getFontMetrics( txtOutFileFolder.getFont() );
			s.width = fm.stringWidth( "W" ) * 30;
			
			txtOutFileFolder.setPreferredSize( s );
						
			txtOutFileFolder.getDocument( ).addDocumentListener( new DocumentListener( ) 
			{				
				@Override
				public void removeUpdate( DocumentEvent e ) 
				{
					update( e );					
				}
				
				@Override
				public void insertUpdate( DocumentEvent e ) 
				{
					update( e );
				}
				
				@Override
				public void changedUpdate( DocumentEvent e ) 
				{
					update( e );
				}
				
				private void update( DocumentEvent e )
				{
					try 
					{
						String folder = e.getDocument( ).getText( 0, e.getDocument( ).getLength( ) );
					
						outFormat.setParameter( OutputFileFormatParameters.OUT_FILE_NAME, folder );						
					}
					catch ( BadLocationException e1 ) 
					{
					}
				}
			} );
			
			txtOutFileFolder.setText( this.currentFolderPath );
		}
		
		return txtOutFileFolder;
	}
	*/
	
	/*
	private JButton getBtnOutFolder( ) 
	{
		if ( btnOutFolder == null )
		{
			btnOutFolder = new JButton( );
			btnOutFolder.setBorder( BorderFactory.createRaisedSoftBevelBorder( ) );
			
			try 
			{
				btnOutFolder.setIcon( GeneralAppIcon.Folder( 20, 20, Color.BLACK, Color.CYAN.darker( ) ) );
			}
			catch ( Exception e ) 
			{
				btnOutFolder.setText( "..." );
			}
			
			btnOutFolder.addActionListener( new ActionListener( ) 
			{				
				@Override
				public void actionPerformed( ActionEvent e ) 
				{
					String[] folder = FileUtils.selectUserFile( "", true, false, JFileChooser.DIRECTORIES_ONLY, null, null, currentFolderPath );
					
					if( folder != null && folder.length > 0 )
					{
						getTxtOutFileFolder( ).setText( folder[ 0 ] );
						
						currentFolderPath = folder[ 0 ];
					}
				}
			} );
		}

		return btnOutFolder;
	}
	*/
	
	private JPanel getInputFilePanel() 
	{
		if (inputFilePanel == null) 
		{
			inputFilePanel = new JPanel( new BorderLayout( 0, 0 ) );
			
			this.inputFilePanel.setBorder( BorderFactory.createEtchedBorder() );
			
			this.inputFilePanel.add( new JScrollPane( this.getTableFileData() ), BorderLayout.CENTER );
			this.inputFilePanel.add( this.getTableFileData().getTableHeader(), BorderLayout.NORTH );
			
		}
		return inputFilePanel;
	}
	
	private JTable getTableFileData( )
	{
		if( this.tableFileData == null )
		{	
			this.tableFileData = this.getCreateJTable( );
			this.tableFileData.setModel( this.createBinFileTable( ) );
			/*
			this.tableFileData.getModel().addTableModelListener( new TableModelListener() 
			{				
				@Override
				public void tableChanged(TableModelEvent e) 
				{
					if( e.getType() == TableModelEvent.UPDATE )
					{
						int row = e.getFirstRow();
						int col = e.getColumn();
						
						if( row >= 0 && col >= 0 )
						{
							TableModel tm = (TableModel)e.getSource();							
							Object d = tm.getValueAt( row, col );
							
							String file = tm.getValueAt( row, 0 ).toString();
						}
					}
				}
			});
			//*/
			
			this.tableFileData.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
			
			this.tableFileData.setPreferredScrollableViewportSize( this.tableFileData.getPreferredSize( ) );
			this.tableFileData.setFillsViewportHeight( true );
			
			this.tableFileData.getColumnModel().getColumn( 0  ).setResizable( false );
						
			this.tableFileData.getSelectionModel( ).addListSelectionListener( new ListSelectionListener( ) 
			{				
				@Override
				public void valueChanged( ListSelectionEvent e ) 
				{
					ListSelectionModel md = ( ListSelectionModel )e.getSource( );
					if( !md.isSelectionEmpty( ) && !e.getValueIsAdjusting( ) )
					{
						int r = tableFileData.getSelectedRow( );
						
						if( r >= 0 )
						{
							String file = tableFileData.getValueAt( r, 0 ).toString();
							try 
							{
								currentClisFile = new ClisData( file );
							} 
							catch ( Exception e1) 
							{
								currentClisFile = null;
							}
							
							showBinaryFileInfo( currentClisFile );
						}
					}
				}
			} );		
		}
		
		return this.tableFileData;
	}	

	private void showBinaryFileInfo( ClisData clis )	
	{
		JPanel infoPanel = getInfoFilePanel();
		
		boolean showPanel = infoPanel.isVisible();
		
		if( showPanel )
		{
			infoPanel.setVisible( false );
		}
		
		infoPanel.removeAll();


		if( clis != null )
		{
			JTextArea jta = new JTextArea();

			List< MetadataVariableBlock > VARS = clis.getVarInfo();

			String tx = "";

			for( MetadataVariableBlock var : VARS )
			{
				tx += var.getName() + "," + var.getDataType().name() + "," + var.getCols() + "\n";
			}

			tx += clis.getHeader();

			jta.setText( tx );

			infoPanel.add( new JScrollPane( jta ), BorderLayout.CENTER );
		}
		
		infoPanel.setVisible( infoPanel.isVisible() );
	}
	
	private JTable getCreateJTable( )
	{
		JTable t = new JTable( )
				{
					private static final long serialVersionUID = 1L;
			
					//Implement table cell tool tips. 
					public String getToolTipText( MouseEvent e ) 					 
					{
						String tip = null;
						Point p = e.getPoint( );
						int rowIndex = rowAtPoint( p );
						int colIndex = columnAtPoint( p );
						
						try 
						{
							tip = getValueAt( rowIndex, colIndex ).toString( );
						}
						catch ( RuntimeException e1 )
						{
							//catch null pointer exception if mouse is over an empty line
						}
				
						return tip;
					}				 
				 };
				 
		t.getTableHeader( ).setReorderingAllowed( false );
				
		return t;
	}
	
	private TableModel createBinFileTable( )
	{					
		TableModel tm = new DefaultTableModel( null, new String[] { Language.getLocalCaption( Language.MENU_FILE ) } )
						{
							private static final long serialVersionUID = 1L;
								
							Class[] columnTypes = getColumnTableTypes();
							boolean[] columnEditables = new boolean[] { false };
								
							public Class getColumnClass( int columnIndex ) 
							{
								return columnTypes[columnIndex];
							}
																								
							public boolean isCellEditable( int row, int column ) 
							{
								boolean editable = columnEditables[column];
									
								return editable;
							}
						};		
						
		return tm;
	}
	
	private Class[] getColumnTableTypes()
	{
		return new Class[]{ String.class };
	}
		
	private void insertBinaryFilesInTable( JTable t, String file )
	{	
		Object[] vals = new Object[ t.getColumnCount( ) ];
		Class[] colTableTypes = this.getColumnTableTypes();
		
		for( int i = 0; i < vals.length; i++ )
		{			
			if( i < colTableTypes.length )
			{
				if( colTableTypes[ i ].equals( String.class ) )
				{
					vals[ i ] = file;
				}
			}			
		}
		
		DefaultTableModel m = ( DefaultTableModel )t.getModel( );
		m.addRow( vals );
	}
	private JSplitPane getSplitCenterPane() 
	{
		if (splitCenterPane == null) 
		{
			this.splitCenterPane = new JSplitPane( JSplitPane.HORIZONTAL_SPLIT, this.getInputFilePanel(), this.getInfoFilePanel() );			
			this.splitCenterPane.setDividerSize( 0 );
			
			this.getInfoFilePanel().setVisible( false );
		}
		return splitCenterPane;
	}
	
	private JPanel getInfoFilePanel()
	{
		if( this.infoFilePanel == null )
		{
			this.infoFilePanel = new JPanel( new BorderLayout( 0, 0 ) );
		}
		
		return this.infoFilePanel;
	}
	private JButton getBtnShowFileInfo() 
	{
		if (this.btnShowFileInfo == null) 
		{
			this.btnShowFileInfo = new JButton();
			
			this.btnShowFileInfo.setBorder( BorderFactory.createRaisedSoftBevelBorder() );
			
			ImageIcon ic = null;
			ic = new ImageIcon( GeneralAppIcon.Info( 256, Color.BLACK ).getImage().getScaledInstance( 20, 20, Image.SCALE_SMOOTH ) );			
			
			if( ic != null )
			{
				this.btnShowFileInfo.setIcon( ic );
			}
			else
			{
				this.btnShowFileInfo.setText( "  i  " );
			}
			
			this.btnShowFileInfo.addActionListener( new ActionListener() 
			{	
				@Override
				public void actionPerformed(ActionEvent e) 
				{					
					boolean vis = getInfoFilePanel().isVisible();
					
					getSplitCenterPane().setVisible( false );
										
					getInfoFilePanel().setVisible( !vis );
					
					double prop = ( !vis ) ? 0.5 : 0; 
					getSplitCenterPane().setDividerLocation( prop );
					
					getSplitCenterPane().setVisible( true );
				}
			});
		}
		return btnShowFileInfo;
	}
	private JPanel getLoadFileBtnPanel()
	{
		if ( this.loadFileBtnPanel == null) 
		{
			this.loadFileBtnPanel = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
			
			this.loadFileBtnPanel.add( this.getLblLoadFile() );
			this.loadFileBtnPanel.add( this.getBtnLoadFile() );
			this.loadFileBtnPanel.add( this.getBtnLoadRecursiveFiles() ) ;
		}
		return this.loadFileBtnPanel;
	}
}
