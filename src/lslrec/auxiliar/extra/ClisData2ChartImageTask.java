package lslrec.auxiliar.extra;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.text.Segment;

import lslrec.config.ConfigApp;
import lslrec.dataStream.convertData.clis.ClisData;
import lslrec.dataStream.family.setting.IMutableStreamSetting;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.dataStream.sync.SyncMarker;
import lslrec.exceptions.ReadInputDataException;

public class ClisData2ChartImageTask 
{
	private String folder = "./";
	
	private ClisData currentClisFile = null;
	private Map< String, Number[][] > clisData = null;
	
	public ClisData2ChartImageTask( String pathFolder )
	{
		File folder =  Paths.get( pathFolder ).toFile();
		
		if( !folder.isDirectory() )
		{
			throw new IllegalArgumentException( "Input " + pathFolder +" is not a directory." );
		}
		
		this.folder = folder.getAbsolutePath();
	}
	
	public void createChartImageFromClisFiles() throws ReadInputDataException
	{
		String idEncoder = DataFileFormat.CLIS;
		String ext = DataFileFormat.getSupportedFileExtension().get( idEncoder );
		
		if( ext != null )
		{						
			if( ext.charAt( 0 ) == '.' )
			{
				ext = ext.substring( 1 );
			}
		}
							
		File dir = new File( this.folder );
		
		final String clisExt = ext;
		File[] files = dir.listFiles(new FilenameFilter()
		{
		    public boolean accept(File dir, String name) 
		    {
		    	boolean pass = name.toLowerCase().endsWith( clisExt );
		    	
		    	return pass;
		    }
		});
		
		if( files != null && files.length > 0 )
		{
			int numRecordedStreams = 0;
			
			HashSet< IMutableStreamSetting > deviceIDs = (HashSet< IMutableStreamSetting >)ConfigApp.getProperty( ConfigApp.ID_STREAMS );
			for( IMutableStreamSetting dev : deviceIDs )
			{
				numRecordedStreams = ( dev.isSelected() ) ? numRecordedStreams + 1 : numRecordedStreams;
			}
					
			ArrayTreeMap< Long, File > sortedFiles = new ArrayTreeMap<Long, File>();
			for( File file : files )
			{
				try 
				{
					FileTime fileTime = (FileTime)Files.getAttribute( Paths.get( file.getAbsolutePath() ), "creationTime" );
					
					sortedFiles.putElement( fileTime.toMillis(), file );
				}
				catch (IOException e) 
				{
				}
			}
			
			List< Long > fileTimes = new ArrayList<Long>( sortedFiles.keySet() );
			fileTimes.sort( Comparator.reverseOrder() );			
			
			List< File > fileList = new ArrayList<File>();
			for( Long fileTime : fileTimes )
			{
				fileList.addAll( sortedFiles.get( fileTime ) );
				
				if( fileList.size() >= numRecordedStreams )
				{
					break;
				}
			}
			
			String errors = "";
			String imgOutputFolder = ( new File( ConfigApp.getProperty( ConfigApp.OUTPUT_FILE_NAME ).toString() ) ).getParentFile().toString();
			if( !imgOutputFolder.endsWith( File.separator ) )
			{
				imgOutputFolder += File.separator;
			}
			
			for( File file : fileList )
			{
				try 
				{
					this.currentClisFile = new ClisData( file.getAbsolutePath() );
					
					this.clisData = this.currentClisFile.importAllData();
					
					String timeVar = "";
					for( String varId : this.clisData.keySet() )
					{
						if( varId.indexOf( "time_" ) >= 0 )
						{
							timeVar = varId;
							
							break;
						}
					}
					
					double[] xValues = null;
					String xlabel = "Samples";
					if( !timeVar.isEmpty() )
					{
						 xValues = ConvertTo.Casting.NumberArray2DoubleArray( ConvertTo.Transform.Matrix2Array( this.clisData.get( timeVar ) ) );
						 
						 double initVal = xValues[ 0 ];
						 for( int i = 0; i < xValues.length; i++ )
						 {
							 xValues[ i ] -= initVal; 
						 }
						 
						 xlabel = "Time (s)";
					}
					
					for( String varId : this.clisData.keySet() )
					{
						if( !varId.equals( timeVar ) )
						{
							Number[][] dataVar = this.clisData.get( varId );
							double[][] yData = ConvertTo.Casting.NumberMatrix2doubleMatrix( dataVar );
							Image img = Data2ChartImage.drawLineSerie( yData, xValues, new Dimension( 1280, 720 ), varId + " - All channels", xlabel, "Amplitude" );
							
							if( img != null )
							{
								ImageIO.write( (BufferedImage)img, "png", new File( imgOutputFolder + varId + "-completeChannels.png") );
							}
							
							if( yData.length > 0 && yData[0].length > 1 )
							{
								List< Integer > channel2Draw = new ArrayList<Integer>();
								for( int c = 0; c < yData[0].length; c++ )
								{
									channel2Draw.add( c );
									
									img = Data2ChartImage.drawLineSerie( yData, xValues, channel2Draw, new Dimension( 1280, 720 ), varId + " - Channel " + c, xlabel, "Amplitude" );
									
									if( img != null )
									{
										ImageIO.write( (BufferedImage)img, "png", new File( imgOutputFolder + varId + "-completeChannel_" + c +".png") );
									}
									
									channel2Draw.clear();
								}
								
								List< Integer > markLocs = new ArrayList< Integer >();
								
								int markCol = yData[ 0 ].length - 1;
								for( int r = 0; r < yData.length; r++  )
								{
									double m = yData[ r ][ markCol ];
									
									if( m != SyncMarker.NON_MARK )
									{
										markLocs.add( r );
																				
										if( (m - ((int)m) ) != 0 )
										{
											markLocs.clear();
											
											break;
										}
									}
								}
								
								if( !markLocs.isEmpty() )
								{
									if( markLocs.get( markLocs.size() - 1 ) < yData.length -1 )
									{
										markLocs.add( yData.length -1);
									}
									
									List< List< double[] > > markSegments = new ArrayList< List< double[] > >();
									
									for( int c = 0; c < markCol; c++ )
									{
										markSegments.add( new ArrayList<double[]>( ) );
									}
									
									int init = 0;
									int end = -1;
									for( int mloc : markLocs )
									{
										end = mloc -1 ;
										
										if( end > init  )
										{
											for( int c = 0; c < markCol; c++ )
											{
												double[] datSegm = new double[ end - init + 1 ];
												
												for( int r = init; r < end; r++ )
												{
													datSegm[ r - init ] = yData[ r ][ c ];
												}
												
												markSegments.get( c ).add( datSegm );
											}
										}
										
										init = mloc;
									}
									
									List< String > segmentMarkCategories = new ArrayList<String>();
									
									for( int nMark = 0; nMark < markLocs.size(); nMark++ )
									{
										int mLoc = markLocs.get( nMark );
										
										if( nMark == 0 && mLoc > 0 )
										{
											segmentMarkCategories.add( "prev-mark " +  (int)yData[ mLoc ][ markCol ] );
										}
										
										if( mLoc < yData.length - 1 )
										{
											segmentMarkCategories.add( "mark " +  (int)yData[ mLoc ][ markCol ] );
										}
									}
									
									for( int ch = 0; ch < markSegments.size(); ch++ )
									{
										 List< double[] > channelSegments = markSegments.get( ch );
										 
										 img = Data2ChartImage.drawCategoryiesBoxplot( channelSegments, segmentMarkCategories, new Dimension( 1280, 720 ), varId + " - mark segments of channel " + ch, "Marks" , "Amplitude");
										 
										 ImageIO.write( (BufferedImage)img, "png", new File( imgOutputFolder + varId + "-markSegmentChannel_" + ch +".png")  );
									}
								}
							}							
						}
					}
				}
				catch (Exception e) 
				{
					errors += "Error in " + file.getAbsolutePath() + ": " + e.getMessage() + "\n";
					e.printStackTrace();
				}
				finally
				{
					if( this.currentClisFile != null )
					{
						try 
						{
							this.currentClisFile.close();
						}
						catch (IOException e) 
						{
							e.printStackTrace();
						}
					}
					if( this.clisData != null )
					{
						this.clisData.clear();
					}
				}
			}
			
			if( !errors.isEmpty() )
			{
				throw new ReadInputDataException( errors ); 
			}
		}
		
	}
}
