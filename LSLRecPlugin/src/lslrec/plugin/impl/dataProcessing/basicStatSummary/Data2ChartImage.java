package lslrec.plugin.impl.dataProcessing.basicStatSummary;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BoxAndWhiskerRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.statistics.DefaultBoxAndWhiskerCategoryDataset;
import org.jfree.data.xy.DefaultXYDataset;

import lslrec.config.language.Language;
import lslrec.gui.miscellany.BasicPainter2D;

public class Data2ChartImage 
{
	public static Image drawLineSerie( double[][] ydat
									, double xAxisMult, double xAxisOffset
									, Dimension imgSize, String title
									, String xlabel, String ylabel )
	{
		Image img = null;

		if( ydat != null && ydat.length > 0 )
		{
			List< Integer > col2Draw = new ArrayList<Integer>();
			
			for( int i = 0; i < ydat[0].length; i++ )
			{
				col2Draw.add( i );
			}
			
			img = drawLineSerie( ydat, col2Draw, xAxisMult, xAxisOffset, imgSize, title, xlabel, ylabel );
		}
		return img;
	}
	
	public static Image drawLineSerie( double[][] ydat, List< Integer > col2draw
										, double xAxisMult, double xAxisOffset
										, Dimension imgSize, String title
										, String xlabel, String ylabel )
	{
		Image img = null;

		if( ydat != null )
		{
			int rows = ydat.length;

			double[] xValues = new double[ rows ];

			for( int i = 0; i < rows; i++ )
			{
				xValues[ i ] = i * xAxisMult + xAxisOffset;
			}

			img = drawLineSerie( ydat, xValues, col2draw, imgSize, title, xlabel, ylabel );
		}

		return img;
	}
	
	public static Image drawLineSerie( double[][] ydat, double[] xValues
										, Dimension imgSize, String title
										, String xlabel, String ylabel )
	{
		Image img = null;

		if( ydat != null && ydat.length > 0 )
		{
			List< Integer > col2Draw = new ArrayList<Integer>();
			
			for( int i = 0; i < ydat[0].length; i++ )
			{
				col2Draw.add( i );
			}
			
			img = drawLineSerie( ydat, xValues, col2Draw, imgSize, title, xlabel, ylabel );
		}
		
		return img;
	}
	
	public static Image drawLineSerie( double[][] ydat, double[] xValues
										, List< Integer > col2Draw
										, Dimension imgSize, String title
										, String xlabel, String ylabel )
	{
		Image img = null;

		if( ydat != null && ydat.length > 0 && col2Draw != null && !col2Draw.isEmpty() )
		{
			int w = ( imgSize != null && imgSize.width > 0 ) ? imgSize.width : 500;
			int h = ( imgSize != null && imgSize.height > 0 ) ? imgSize.height : 500;

			img = BasicPainter2D.createEmptyImage( w, h, null );

			int rows = ydat.length;
			int cols = ydat[ 0 ].length;

			DefaultXYDataset xyValues = new DefaultXYDataset();
			double minVal = Double.POSITIVE_INFINITY;
			double maxVal = Double.NEGATIVE_INFINITY;
			
			if( xValues == null )
			{
				xValues = new double[ rows ];
				
				for( int i = 0; i < rows; i++ )
				{
					xValues[ i ] = i;
				}
			}
			else if( xValues.length < rows )
			{
				int xValueLen = xValues.length;
				double padValue = 0;
				if( xValueLen > 1 )
				{
					padValue = xValues[ xValueLen - 1 ] - xValues[ xValueLen - 2 ];
				}
				
				xValues = Arrays.copyOf( xValues, rows );
				
				for( int i = xValueLen; i < rows; i++ )
				{
					xValues[ i ] = xValues[ i - 1 ] + padValue;
				}
			}
			
			for( int ich = 0; ich < cols && ich < cols; ich++ )
			{					
				if( !col2Draw.contains( ich  ) )
				{
					continue;
				}
				
				double[][] interval = new double[2][ rows ];
				for( int i = 0; i < rows; i++ )
				{
					interval[0][ i ] = xValues[ i ]; //i * xAxisMult + xAxisOffset;

					double val = ydat[ i ][ ich ];
					interval[1][ i ] = val;

					if( val < minVal )
					{
						minVal = val;
					}

					if( val > maxVal )
					{
						maxVal = val;
					}
				}						

				String serieName = Language.getLocalCaption( Language.LSL_CHANNEL ) + "[" + (ich+1)+"]";
				xyValues.addSeries( serieName, interval );						
			}

			if( xyValues.getSeriesCount() > 0 )
			{
				final JFreeChart chart = ChartFactory.createXYLineChart( title, xlabel, ylabel, xyValues  );
				chart.setAntiAlias( true );				
				chart.setBackgroundPaint( Color.WHITE );
				chart.getXYPlot().setBackgroundPaint( Color.WHITE );
				chart.getXYPlot().setRangeGridlinePaint( Color.BLACK );
				chart.getXYPlot().setDomainGridlinePaint( Color.BLACK );
				XYLineAndShapeRenderer render = new XYLineAndShapeRenderer();
				render.setDefaultShapesVisible( false );
				for( int iserie = 0; iserie < xyValues.getSeriesCount(); iserie++ )
				{
					render.setSeriesStroke( iserie, new BasicStroke( 3F ) );
				}
				chart.getXYPlot().setRenderer( render );
				chart.getXYPlot().getDomainAxis().setTickLabelFont( new Font( Font.DIALOG, Font.BOLD, 15 ) );
				chart.getXYPlot().getRangeAxis().setTickLabelFont( new Font( Font.DIALOG, Font.BOLD, 15 ) );
				ValueAxis yaxis = chart.getXYPlot().getRangeAxis();

				if( minVal == maxVal )
				{
					minVal -= 0.5;
					maxVal += 0.5; 
				}					

				minVal = ( minVal == Double.POSITIVE_INFINITY ) ? Double.NEGATIVE_INFINITY : minVal;
				maxVal = ( maxVal == Double.NEGATIVE_INFINITY ) ? Double.POSITIVE_INFINITY : maxVal;

				yaxis.setRange( minVal, maxVal );

				chart.getXYPlot().setRangeAxis( yaxis );	

				chart.draw( (Graphics2D)img.getGraphics(), 
						new Rectangle2D.Double( 0, 0, img.getWidth( null ), img.getHeight( null ) ) );
			}
		}

		return img;
	}

	public static Image drawCategoryiesBoxplot( List< double[] > dat
												, Dimension imgSize, String title
												, String xlabel, String ylabel )
	{
		Image img = null;

		if( dat != null && !dat.isEmpty() )
		{
			List< String > categories = new ArrayList<String>();
			
			for( int i = 0; i < dat.size(); i++ )
			{
				categories.add( (i+1) + "" );
			}
			
			img = drawCategoryiesBoxplot( dat, categories, imgSize, title, xlabel, ylabel );
		}
		
		return img;
	}
	
	public static Image drawCategoryiesBoxplot( List< double[] > dat
									, List< String > categories
									, Dimension imgSize, String title
									, String xlabel, String ylabel )
	{
		Image img = null;

		if( dat != null && !dat.isEmpty() )
		{
			int w = ( imgSize != null && imgSize.width > 0 ) ? imgSize.width : 500;
			int h = ( imgSize != null && imgSize.height > 0 ) ? imgSize.height : 500;

			img = BasicPainter2D.createEmptyImage( w, h, null );
			
			DefaultBoxAndWhiskerCategoryDataset dataset = new DefaultBoxAndWhiskerCategoryDataset( );
		
			for( int iSerie = 0; iSerie < dat.size(); iSerie++ )
			{
				String serieName = (iSerie+1) + "";
				
				if( categories != null && iSerie < categories.size() )
				{
					serieName = categories.get( iSerie );
				}
				
				Double[] doubleArray = ArrayUtils.toObject( dat.get( iSerie ) );
				List<Double> dserie = Arrays.asList( doubleArray );
				
				dataset.add( dserie, iSerie, serieName);						
			}

			 CategoryAxis xAxis = new CategoryAxis( xlabel );
		     NumberAxis yAxis = new NumberAxis( ylabel );
		        
		     BoxAndWhiskerRenderer renderer = new BoxAndWhiskerRenderer();
		     CategoryPlot plot = new CategoryPlot(dataset, xAxis, yAxis, renderer);
		        
		     JFreeChart chart = new JFreeChart( title, plot);

		     chart.draw( (Graphics2D)img.getGraphics(), 
		    		 	new Rectangle2D.Double( 0, 0, img.getWidth( null ), img.getHeight( null ) ) );
		}

		return img;
	}
}
