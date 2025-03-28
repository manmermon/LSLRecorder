package lslrec.plugin.impl.dataProcessing.basicStatSummary;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.auxiliar.extra.FileUtils;
import lslrec.auxiliar.extra.Tuple;
import lslrec.config.Parameter;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.sync.SyncMarker;
import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionMessage;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.PluginDataProcessingSettings;

public class BasicStatSummaryProcessing extends LSLRecPluginDataProcessing
{	
	public static final String MARKER_WIN_LEN_SAMPLES = "window length in samples for irregular sampling rate";
	public static final String MARKER_WIN_LEN_SECS = "window length in seconds for regular sampling rate";
	public static final String MARKER_ID_SEGMENTS = "marker id for segmentation";
	
	private static final String outSubfolder = "images/";
	
	private List< Number[] > data = null;
	private int dataSize = 0;
	
	private String imgOutputFolder = "./";
	
	private int marker_win_segm_len_samples = 0;
	private int marker_win_segm_len_secs = 0;
	private List< Integer > marker_id_list_2_segment = new ArrayList< Integer >(); 
	
	private Object sync = new Object();
	
	public BasicStatSummaryProcessing( IStreamSetting setting, LSLRecPluginDataProcessing prevProc ) 
	{
		super( setting, prevProc );
		
		this.data = new LinkedList< Number[] >();
	}

	@Override
	public String getID() 
	{
		return super.getClass().getName();
	}

	@Override
	protected void finishProcess() 
	{				
		String varId = super.streamSetting.name();		
		
		Number[] DATA = new Number[ dataSize ];
		int posData = 0;
		
		synchronized ( this.sync )
		{
			if( this.data != null )
			{
				for( Number[] d : this.data )
				{	
					System.arraycopy( d, 0, DATA, posData, d.length );
					posData += d.length;
				}				
			}
		}	
		
		double[] xValues = new double[ dataSize ];
		String xlabel = "Samples";
		double frq = ( super.streamSetting.sampling_rate() == IStreamSetting.IRREGULAR_RATE ) ? 1D : super.streamSetting.sampling_rate(); 
		for( int i = 0; i < dataSize; i++ )
		{
			xValues[ i ] = i / frq;
		}
		
		if( super.streamSetting.sampling_rate() != IStreamSetting.IRREGULAR_RATE )
		{
			xlabel = "Time (s)";
		}
		
		
		Tuple<Number[][], Number[]> matrixRest = ConvertTo.Transform.Array2Matrix( DATA, super.streamSetting.channel_count() );
		
		Number[][] matrix = matrixRest.t1;
		Number[] rest = matrixRest.t2;
			
		double[][] yData = ConvertTo.Casting.NumberMatrix2doubleMatrix( matrix );
	
		Image img = Data2ChartImage.drawLineSerie( yData, xValues, new Dimension( 1280, 720 ), varId + " - All channels", xlabel, "Amplitude" );
		
		try
		{
			if( img != null )
			{
				Tuple< String, Boolean> fileName = FileUtils.checkOutputFileName( this.imgOutputFolder + varId + "-completeChannels.png", "", "" );		
				
				ImageIO.write( (BufferedImage)img, "png", new File( fileName.t1 ) );
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
						Tuple< String, Boolean> fileName = FileUtils.checkOutputFileName( this.imgOutputFolder + varId + "-completeChannel_" + c +".png", "", "" );
						
						ImageIO.write( (BufferedImage)img, "png", new File( fileName.t1 ) );
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
							markLocs.remove( markLocs.size() - 1 );
							
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
								
								for( int r = init; r <= end; r++ )
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
						 
						 if( channelSegments != null && !channelSegments.isEmpty() )
						 {
							 img = Data2ChartImage.drawCategoryiesBoxplot( channelSegments, segmentMarkCategories, new Dimension( 1280, 720 ), varId + " - mark segments of channel " + ch, "Marks" , "Amplitude");

							Tuple< String, Boolean> fileName = FileUtils.checkOutputFileName( this.imgOutputFolder + varId + "-markSegmentChannel_" + ch +".png", "", "" );

							 ImageIO.write( (BufferedImage)img, "png", new File( fileName.t1 )  );
						 }
					}
					
					Map< Integer, Integer > markCount = new HashMap<Integer, Integer>();
					
					int winLen = (int)Math.ceil( super.streamSetting.sampling_rate() * this.marker_win_segm_len_secs );
					
					if( super.streamSetting.sampling_rate() == IStreamSetting.IRREGULAR_RATE )
					{
						winLen = this.marker_win_segm_len_samples;
					}
										
					if( winLen > 0 )
					{					
						for( int mloc : markLocs )
						{
							int markValue = (int)yData[ mloc ][ markCol ];
							
							Integer mCount = markCount.get( markValue );
							mCount = ( mCount == null ) ? 1 : mCount + 1;
							
							markCount.put( markValue, mCount );
							
							if( this.marker_id_list_2_segment.contains( markValue ) )
							{				
								init = mloc - winLen / 2;
								end = mloc + winLen / 2;
								
								end = ( end > ( yData.length -1 ) ) ? yData.length -1 : end;
								init = ( init < 0 ) ? 0 : init;
								
								if( end > init  )
								{
									double[][] datSegm1Channel = new double[ end - init + 1 ][1];								
									double[][] datSegmAllChannels = new double[ datSegm1Channel.length ][markCol];
	
									xValues = new double[ datSegm1Channel.length ];
									
									for( int r = init; r <= end; r++ )
									{
										xValues[ r - init ] = r / frq;
									}
									
									for( int c = 0; c < markCol; c++ )
									{	
										for( int r = init; r <= end; r++ )
										{
											datSegm1Channel[ r - init ][ 0 ] = yData[ r ][ c ];
											datSegmAllChannels[ r - init ][ c ] = yData[ r ][ c ];
										}
										
										img = Data2ChartImage.drawLineSerie( datSegm1Channel, xValues, new Dimension( 1280, 720), varId + "(channel " + c + ")-marker " + markValue, xlabel, "Amplitude" );
										
										if( img != null )
										{
											Tuple< String, Boolean> fileName = FileUtils.checkOutputFileName( this.imgOutputFolder + varId + "-channel" + c + "-mark" + markValue +"-segment"+ mCount+".png", "", "" );
											
											ImageIO.write( (BufferedImage)img, "png", new File( fileName.t1 ) );
										}
									}								
									
									img = Data2ChartImage.drawLineSerie( datSegmAllChannels, xValues, new Dimension( 1280, 720 ), varId + "(all channels)-marker " + markValue, xlabel, "Amplitude" );
																	
									if( img != null )
									{
										Tuple< String, Boolean> fileName = FileUtils.checkOutputFileName( this.imgOutputFolder + varId + "-allChannels-mark-" + markValue + "-segment"+mCount+".png", "", "" );
										
										ImageIO.write( (BufferedImage)img, "png", new File( fileName.t1 ) );
									}
								}
							}
						}
					}
				}
			}	
			
			// free memory
			this.data = null;
			this.dataSize = 0;
			
			if( this.imgOutputFolder.equals( "./" ) )
			{
				ExceptionDialog.showMessageDialog( new ExceptionMessage( new Throwable("Output folder: " + this.imgOutputFolder ), "Default path", ExceptionMessage.WARNING_MESSAGE ), true, true );
			}
		}
		catch (Exception e) 
		{
			ExceptionDialog.showMessageDialog( new ExceptionMessage( e, "Image error", ExceptionMessage.ERROR_MESSAGE ), true, true );
		}
	}

	@Override
	public int getBufferLength() 
	{
		return 1;
	}

	@Override
	public int getOverlapOffset() 
	{
		return 0;
	}

	@Override
	public void loadProcessingSettings(List< Parameter< String > > arg0) 
	{
		for( Parameter< String > par : arg0 )
		{
			switch ( par.getID())
			{
				case PluginDataProcessingSettings.PAR_OUTPUT_FOLDER:
				{	
					this.imgOutputFolder = par.getValue();
					
					if( this.imgOutputFolder != null )
					{	
						try 
						{
							File outFile = new File( this.imgOutputFolder );
							this.imgOutputFolder  = outFile.getCanonicalPath();
							
							if( !this.imgOutputFolder.endsWith( File.separator ) )
							{
								this.imgOutputFolder += File.separator;
							}
						} 
						catch (IOException e)
						{
						}
					}
					else
					{
						this.imgOutputFolder = "./";
					}
					
					File folder = new File( this.imgOutputFolder + this.outSubfolder );
					try
					{
						if( !folder.exists() )
						{
							if( folder.mkdir() )
							{
								this.imgOutputFolder += this.outSubfolder;							
							}
						}
						else
						{
							this.imgOutputFolder += this.outSubfolder;
						}					
					}
					catch (Exception e) 
					{
					}
					
					break;
				}
				case MARKER_WIN_LEN_SAMPLES:
				{
					try
					{
						this.marker_win_segm_len_samples = Integer.parseInt( par.getValue() );
						
						if( this.marker_win_segm_len_samples < 0 )
						{
							this.marker_win_segm_len_samples = 0;
						}
					}
					catch ( Exception e ) 
					{
					}
					
					break;
				}
				case MARKER_WIN_LEN_SECS:
				{
					try
					{
						this.marker_win_segm_len_secs = Integer.parseInt( par.getValue() );
						
						if( this.marker_win_segm_len_secs < 0 )
						{
							this.marker_win_segm_len_secs = 0;
						}
					}
					catch ( Exception e ) 
					{
					}
					
					break;
				}
				case MARKER_ID_SEGMENTS:
				{
					try
					{
						String vals = par.getValue();
						
						String[] ids = vals.replaceAll( "\\s+", "").split( ",|;" );
						
						for( String id : ids )
						{
							int idVal = Integer.parseInt( id );
							
							if( !this.marker_id_list_2_segment.contains( idVal ) )
							{
								this.marker_id_list_2_segment.add( idVal );
							}
						}
					}
					catch ( Exception e ) 
					{
						this.marker_id_list_2_segment.clear();
					}
					
					break;
				}
				default:
				{
					break;
				}
			}
		}
	}

	@Override
	protected Number[] processData(Number[] dat) 
	{
		synchronized ( this.sync )
		{
			if( dat != null && this.data != null)
			{
				this.dataSize += dat.length;
				
				this.data.add( dat );
			}
			
			return dat;
		}		
	}
}
