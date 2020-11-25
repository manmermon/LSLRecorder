/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.painter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * @author Manuel Merino Monge
 *
 */
public class DataPanel extends JPanel 
{    
	/**
	 * 
	 */
	private static final long serialVersionUID = 9101899790779650335L;
	private BufferedImage bufferedImage;
	private Graphics2D graphic = null;
	
	private int width;
	private int height;

	private double xLen;
	private double ySpan;
	
	private int currentIndex = -1;
	private List< Double > currentSample = new ArrayList<Double>();
		
	
	/**
	 * 
	 */
	public DataPanel( int xLen, double minY, double maxY ) 
	{
		super();
		
		super.setBorder( BorderFactory.createEtchedBorder() );
		
		this.xLen = xLen;
		
		this.ySpan = maxY - minY;
		
		if( xLen < 10 )
		{
			throw new IllegalArgumentException( "X-axis length is lower than 10." );
		}
		
		if( this.ySpan == Double.NaN 
				|| this.ySpan == Double.NEGATIVE_INFINITY 
				|| this.ySpan == Double.POSITIVE_INFINITY 
				|| this.ySpan <= 0)
		{
			throw new IllegalArgumentException( "y-axis span must be a greater-than-10, finite number." );
		}
	}

	@Override
	protected void paintComponent( Graphics graphics ) 
	{
		super.paintComponent( graphics );   

		if ( this.bufferedImage == null) 
		{
			this.createDataImage();
		}

		graphics.drawImage( bufferedImage, 0, 0, this );
	}

	private void createDataImage()
	{
		this.width = getWidth();
		this.height = getHeight();
				
		this.bufferedImage = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		
		this.graphic = (Graphics2D)this.bufferedImage.getGraphics();
		this.graphic.setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
		
		this.graphic.setColor( Color.WHITE );
		this.graphic.fillRect( 0, 0, this.width, this.height);
		
		//BasicStroke b = new BasicStroke( 1 );
		//this.graphic.setStroke( b );
	}

	public void drawData( double[][] data )
	{   
		if( this.graphic == null )
		{
			this.createDataImage();
		}
		
		if( data != null )
		{
			int chs = data.length;
			int len = data[0].length;
			
			this.drawVerticalBar( Color.WHITE, this.currentIndex );
			
			this.clearDrawData( Color.WHITE
								, (int)(  this.width * currentIndex / this.xLen )
								, (int)(  this.width * ( len + 1 ) / this.xLen )
								);
			if( currentIndex + len > this.xLen )
			{
				this.clearDrawData( Color.WHITE
									, 0
									, (int)(  this.width * ( currentIndex + len - ( this.xLen + 1 ) ) / this.xLen )
									);
			}
			
			float hueStep = 1F / chs;
			
			int update = 0;
			for( int r = 0; r < chs; r++ )
			{
				double[] values = data[ r ];
			
				float h = hueStep * r;
				Color c = new Color( Color.HSBtoRGB( h, 1F, 1F ) );
				
				int from = 0;
				int to = values.length;
			
				if( r >= this.currentSample.size() )
				{
					this.currentSample.add( Double.NaN );
				}
				
				int refX =  this.currentIndex;
				double refY = this.currentSample.get( r );
				
				if( refX + values.length > this.xLen )
				{	
					to = from + (int)( this.xLen - refX );
					
					this.drawAux( Arrays.copyOfRange( values, from, to ), c, refX, refY );

					refX = -1;
					refY = values[ to - 1 ];
					
					from = to;
					to = values.length;					
				}
							
				this.drawAux( Arrays.copyOfRange( values, from, to ), c, refX, refY );
				
				if( update < values.length )
				{
					update = values.length;
				}
				refY = values[ to - 1 ];
								
				
				this.currentSample.set( r, refY );
			}
				
			this.currentIndex += update;
			if( this.currentIndex >= this.xLen )
			{
				this.currentIndex = (int)( this.currentIndex - this.xLen - 1 );
			}
			
			this.drawVerticalBar( Color.BLACK, this.currentIndex );
						
			this.graphic.setBackground( Color.BLACK );
			
			repaint();            
		}
	}   
	
	private void drawVerticalBar( Color c, int index )
	{
		this.graphic.setColor( c );
		this.graphic.drawLine( (int)(  this.width * index / this.xLen ), 0
								,(int)(  this.width * index/ this.xLen ), this.height );
	}
	
	private void clearDrawData( Color c, double x, int w )
	{
		this.graphic.setColor( c );
		
		this.graphic.fill( new Rectangle2D.Double( x, 0, w, this.height ) );
	}
	
	private void drawAux( double[] values, Color lineColor, int currentIndex, double currentSample )
	{
		int[] xs = new int[ values.length + 1 ];
		int[] ys = new int[ values.length + 1 ];
		
		xs[ 0 ] = (int)(  this.width * currentIndex / this.xLen );
		ys[ 0 ] = (int)( this.height * ( 0.5 - currentSample / this.ySpan ) );

		this.graphic.setColor( Color.WHITE );
		this.graphic.drawLine( xs[ 0 ], 0, xs[ 0 ], this.height );
		
		for( int i = 1; i < xs.length; i++ )
		{
			xs[ i ] = (int)( this.width * ( currentIndex + i ) / this.xLen );
			ys[ i ] = (int)( this.height *( 0.5 - values[ i - 1 ] / this.ySpan ) );
		}
		
		this.drawPolyLine( xs, ys, lineColor );				
	}
	
	private void drawPolyLine( int[] xs, int[] ys, Color lineColor )
	{						
		this.graphic.setColor( lineColor );
		this.graphic.drawPolyline( xs, ys, xs.length );	
	}
}
