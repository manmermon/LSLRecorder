/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.zTransform;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.util.Arrays;

import javax.swing.JPanel;

import org.apache.commons.math3.complex.Complex;

import lslrec.auxiliar.extra.NumberRange;
import lslrec.plugin.impl.gui.BasicPainter2D;

/**
 * @author Manuel Merino Monge
 *
 */
public class FrequencyPanel extends JPanel 
{    
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8234697357387106377L;
		
	private Object lock = new Object();
	
	private Complex[] fftData = null;

	private int padding = 5;
	
	private float thinkness = 1.5F;
	private int markerSize = 5;
	private int axisThinckness = 30;			
	
	private Color color = Color.BLUE;
	
	@Override
	protected void paintComponent( Graphics graphics ) 
	{
		super.paintComponent( graphics );   
						
		int width = super.getWidth() - this.padding;
		int height = super.getHeight() / 2 - this.padding;
		
		width = ( width <= 0 ) ? 1 : width;
		height = ( height <= 0 ) ? 1 : height;
					
		Image imgMod = BasicPainter2D.createEmptyCanva( width, height, Color.WHITE );
		Image imgPha = BasicPainter2D.createEmptyCanva( width, height, Color.WHITE );
		
		Image imgModFFT = null;
		Image imgPhaseFFT = null;
		synchronized( lock )
		{
			Dimension d = new Dimension( width, height );
			imgModFFT = draw( this.modFFT( this.fftData ), d, new NumberRange( 0, Double.NaN ), false );
			imgPhaseFFT = draw( this.phaseFFT( this.fftData ), d, new NumberRange( -Math.PI*1.1, Math.PI*1.1), true );
		}
		
		BasicPainter2D.composeImage( imgMod, 0, 0, imgModFFT );				
		BasicPainter2D.composeImage( imgPha, 0, 0, imgPhaseFFT );
		
				
		graphics.setColor( Color.WHITE );
		graphics.drawImage( imgMod, padding / 2, padding / 2, null );
		graphics.drawImage( imgPha, padding / 2, imgMod.getHeight( null ) + padding , null );
		
	}

	private double[] modFFT( Complex[] fft )
	{
		double[] res = new double[ 0 ];
		
		if( fft != null )
		{
			res = new double[ fft.length ];
			
			for( int i = 0; i < fft.length; i++ )
			{
				res[ i ] = fft[ i ].abs();
			}
		}
		
		return res;
	}
	
	private double[] phaseFFT( Complex[] fft )
	{
		double[] res = new double[ 0 ];
		
		if( fft != null )
		{
			res = new double[ fft.length ];
			
			for( int i = 0; i < fft.length; i++ )
			{
				res[ i ] = fft[ i ].getArgument();
			}
		}
		
		return res;
	}
	
	private Image draw( double[] y, Dimension d, NumberRange limitYaxis, boolean showZero )
	{		
		Image plot = null;
		
		if( y != null && d != null )
		{
			plot = BasicPainter2D.createEmptyCanva( d.width, d.height, null );
			
			Image area = BasicPainter2D.createEmptyCanva( d.width - this.axisThinckness, d.height - this.axisThinckness, null );
			int width = area.getWidth( null );
			int height = area.getHeight( null );
			
			double[] k = new double[ y.length ];
			for( int i = 0; i < k.length; i++ )
			{
				k[ i ] = i;
			}
			
			int[] px = ConvertDataToPixel( k, width, 0, Double.NaN, Double.NaN );
			
			Double limMin = Double.NaN;
			Double limMax = Double.NaN;
			if( limitYaxis != null )
			{
				limMin = limitYaxis.getMin();
				limMax = limitYaxis.getMax();
			}
			
			int[] py = ConvertDataToPixel( y, -height, height, limMin, limMax );
			
			
			Image tx1 = BasicPainter2D.text( "1", super.getFontMetrics( super.getFont()), null, Color.BLACK, null ); 
			Image tx1_2 = BasicPainter2D.text( "0.5", super.getFontMetrics( super.getFont()), null, Color.BLACK, null );
			Image tx0 = BasicPainter2D.text( "0", super.getFontMetrics( super.getFont()), null, Color.BLACK, null );
			
			BasicPainter2D.composeImage( plot, d.width - tx1.getWidth( null ), d.height - this.axisThinckness + 2, tx1 );
			BasicPainter2D.composeImage( plot, this.axisThinckness + 2, d.height - this.axisThinckness + 2, tx0 );
			
			if( px.length > 0 )
			{
				BasicPainter2D.composeImage( plot, px[ px.length / 2 ] + this.axisThinckness - tx1_2.getWidth( null ) / 2, d.height - this.axisThinckness + 2, tx1_2 );
				BasicPainter2D.line( px[ px.length / 2 ] + this.axisThinckness, d.height - this.axisThinckness, px[ px.length / 2 ] + this.axisThinckness, 0, 1, Color.LIGHT_GRAY, plot );
				
				double[] in = y.clone();
				Arrays.sort( in );
				double maxY = in[ y.length -1 ];
				double minY = in[ 0 ];
				double[] posAux = new double[]{ minY, 0, 1, maxY };				
				
				if( !showZero )
				{
					posAux = new double[]{ minY, 1, maxY };
				}
				
				Arrays.sort( posAux );
				
				
				int[] yLocs = ConvertDataToPixel( posAux, -height, height, limMin, limMax );
				
				for( int _k = 0; _k < yLocs.length; _k++ )
				{
					int yloc = yLocs[ _k ];
					double v = posAux[ _k ];
					
					yloc = ( yloc < 0 ) ? 0 : yloc;
					
					Image txY = BasicPainter2D.text( String.format( "%.2f", v ), super.getFontMetrics( super.getFont()), null, Color.BLACK, null );
					
					if( yloc + txY.getHeight( null ) > d.height - this.axisThinckness )
					{
						continue;
					}
					
					int ylTx = yloc -txY.getHeight( null) / 2;
					ylTx = ( ylTx < 0 ) ? 0 : ylTx; 
					
					BasicPainter2D.composeImage( plot, this.axisThinckness - txY.getWidth( null ), ylTx, txY );
					BasicPainter2D.line( this.axisThinckness, yloc, d.width, yloc, 1, Color.LIGHT_GRAY, plot );					
				}
				
				/*
				if( showZero )
				{
					int[] y1Loc = ConvertDataToPixel( new double[] { minY, 0, maxY }, -height, height, limMin, limMax );
					
					int yl = y1Loc[ 1 ] - tx1.getHeight( null )/2;
					
					BasicPainter2D.composeImage( plot, this.axisThinckness - tx0.getWidth( null ), yl, tx0 );
					BasicPainter2D.line( this.axisThinckness, y1Loc[ 1 ], d.width, y1Loc[ 1 ], 1, Color.LIGHT_GRAY, plot );
				}
				*/
			}			
			
			
			
			
			if( px.length < 2 )
			{
				for( int i = 0; i < px.length; i++ )
				{
					BasicPainter2D.circle( px[ i ]-this.markerSize/2
										, height - py[ i ] - this.markerSize/2, this.markerSize, this.color, area );
				}
			}
			else
			{
				for( int i = 1; i < px.length; i++ )
				{					
					BasicPainter2D.line( px[ i - 1 ], py[ i - 1 ]
										, px[ i ], py[ i ], 1.5F, this.color, area );
				}
			}
			
			BasicPainter2D.composeImage( plot, axisThinckness, 0, area );
			
			BasicPainter2D.line( this.axisThinckness, 0, this.axisThinckness, d.height, this.thinkness, Color.BLACK, plot );
			BasicPainter2D.line( 0, d.height - this.axisThinckness, d.width, d.height - this.axisThinckness, this.thinkness, Color.BLACK, plot );
			
			
		}
		
		return plot;
	}
	
	private int[] ConvertDataToPixel( double[] input, int span, int shift, double minLimit, double maxLimit )
	{		
		int[] pos = new int[ 0 ];
		
		if( input != null && input.length > 0 )
		{
			pos = new int[ input.length ];

			double[] in = input.clone() ;
			Arrays.sort( in );
			
			double min = in[ 0 ];
			double max = in[ in.length - 1 ];
			
			if( !Double.isNaN( minLimit ) )
			{
				min = minLimit;
			}
			
			if( !Double.isNaN( maxLimit ) )
			{
				max = maxLimit;
			}
			
			double w = max - min;

			for( int i = 0; i < input.length; i++ )
			{
				double d = input[ i ];
				double aux = ( d - min ) / w;

				int p = (int)( aux * span + shift );
								
				pos[ i ] = p;
			}
		}

		return pos;
	}
	
	public void drawData( Complex[] fft_data )
	{   
		synchronized( this.lock )
		{
			this.fftData = fft_data;
		}
		
		repaint();
	}
}
