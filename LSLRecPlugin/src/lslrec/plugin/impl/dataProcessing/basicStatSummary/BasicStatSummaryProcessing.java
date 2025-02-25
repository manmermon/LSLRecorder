package lslrec.plugin.impl.dataProcessing.basicStatSummary;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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

public class BasicStatSummaryProcessing extends LSLRecPluginDataProcessing
{	
	private List< Number[] > data = null;
	private int dataSize = 0;
	
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
		// TODO Auto-generated method stub
		//String par = ConfigApp.getProperty( ConfigApp.OUTPUT_FILE_NAME ).toString();
		String par = FileUtils.getOutputCompletedFileNameFromConfig();
		
		String imgOutputFolder = "./";
		if( par != null )
		{
			File outFile = new File( par );
			try 
			{
				imgOutputFolder = outFile.getCanonicalPath();
			} 
			catch (IOException e)
			{
			}
		}
		
		String varId = super.streamSetting.name();		
		
		Number[] DATA = new Number[ dataSize ];
		int posData = 0;
		
		for( Number[] d : this.data )
		{	
			System.arraycopy( d, 0, DATA, posData, d.length );
			posData += d.length;
		}
		
		// free memory
		this.data = null;
			
		
		double[] xValues = new double[ dataSize ];
		String xlabel = "Samples";
		double frq = ( super.streamSetting.sampling_rate() == IStreamSetting.IRREGULAR_RATE ) ? 1D : super.streamSetting.sampling_rate(); 
		for( int i = 0; i < dataSize; i++ )
		{
			xValues[ i ] = i * frq;
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
				Tuple< String, Boolean> fileName = FileUtils.checkOutputFileName( imgOutputFolder + varId + "-completeChannels.png", "", "" );		
				
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
						Tuple< String, Boolean> fileName = FileUtils.checkOutputFileName( imgOutputFolder + varId + "-completeChannel_" + c +".png", "", "" );
						
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

							Tuple< String, Boolean> fileName = FileUtils.checkOutputFileName( imgOutputFolder + varId + "-markSegmentChannel_" + ch +".png", "", "" );

							 ImageIO.write( (BufferedImage)img, "png", new File( fileName.t1 )  );
						 }
					}
				}
			}	
			
			if( par == null )
			{
				String path = (new File( "./" ) ).getCanonicalPath();
				ExceptionDialog.showMessageDialog( new ExceptionMessage( new Throwable("Output folder: " + path ), "Default path", ExceptionMessage.WARNING_MESSAGE ), true, true );
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
	}

	@Override
	protected Number[] processData(Number[] dat) 
	{
		if( dat != null )
		{
			this.dataSize += dat.length;
			
			this.data.add( dat );
		}
		
		return dat;
	}
}
