/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.painter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * @author Manuel Merino Monge
 *
 */
public class DataPanel extends JPanel 
{    
	/**
	 * 
	 */
	private BufferedImage bufferedImage;
	private Graphics2D graphic = null;
	
	private int width;
	private int height;

	private double xLen;
	private double ySpan;
	private double minY;
	
	private int currentIndex = -1;
	private List< Double[] > currentSample = new ArrayList< Double[] >();
		
	private Object lock = new Object();
	
	private Timer resizeImageTimer = null;
	private boolean first = true;
	/**
	 * 
	 */
	public DataPanel( int xLen, double minY, double maxY ) 
	{
		super();
		
		super.setBorder( BorderFactory.createEtchedBorder() );
		
		this.xLen = xLen - 1;
		
		this.ySpan = maxY - minY;
		this.minY = minY;
		
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
		
		this.resizeImageTimer = new Timer( 100, new ActionListener()
									{		
										@Override
										public void actionPerformed(ActionEvent e) 
										{
											createDataImage();
										}
									});
		
		this.resizeImageTimer.setRepeats( false );
		
		super.addComponentListener( new ComponentAdapter() 
		{
			@Override
			public void componentResized(ComponentEvent e) 
			{
				if( !first )
				{
					if( resizeImageTimer.isRunning() )
					{
						resizeImageTimer.restart();
					}
					else
					{
						resizeImageTimer.start();
					}
				}
				else
				{
					first = false;
				}
			}
		});
	}
	
	@Override
	protected void paintComponent( Graphics graphics ) 
	{
		super.paintComponent( graphics );   
		
		if ( bufferedImage == null) 
		{
			createDataImage();
		}
		
		synchronized( this.lock )
		{
			graphics.drawImage( this.bufferedImage, 0, 0, this );
		}
	}

	private void createDataImage()
	{
		synchronized ( this.lock )
		{
			this.width = getWidth();
			this.height = getHeight();
					
			this.bufferedImage = new BufferedImage( this.width, this.height, BufferedImage.TYPE_INT_ARGB );
			
			this.graphic = (Graphics2D)this.bufferedImage.getGraphics();
			this.graphic.setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
			
			this.graphic.setColor( Color.WHITE );
			this.graphic.fillRect( 0, 0, this.width, this.height );
		
			//BasicStroke b = new BasicStroke( 1F );
			//this.graphic.setStroke( b );
		}
	}

	public void drawData( double[][] data )
	{   
		if( this.graphic == null )
		{
			this.createDataImage();
		}
		
		if( data != null )
		{
			synchronized( this.lock )
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
					Color c = Color.getHSBColor( h, 1F, 1F );
					
					int from = 0;
					int to = values.length;
				
					if( r >= this.currentSample.size() )
					{
						this.currentSample.add( new Double[] { Double.NaN, Double.NaN } );
					}
					
					int refX =  this.currentIndex;
					Double[] refY = this.currentSample.get( r );
					
					if( refX + values.length > this.xLen )
					{	
						to = from + (int)( this.xLen - refX );
						
						this.drawAux( Arrays.copyOfRange( values, from, to ), c, refX, refY );
	
						refX = -1;
						refY[ 0 ] = Double.NaN;
						refY[ 1 ] = Double.NaN;
						
						from = to;
						to = values.length;					
					}
								
					this.drawAux( Arrays.copyOfRange( values, from, to ), c, refX, refY );
					
					if( update < values.length )
					{
						update = values.length;
					}
					
					refY[ 0 ] = refY[ 1 ];
					refY[ 1 ] = values[ to - 1 ];
					//refY[ 0 ] = Double.NaN;
					if( to -2 >= 0 )
					{
						refY[ 0 ] = values[ to - 2 ];
					}				
					
					this.currentSample.set( r, refY );
				}
					
				this.currentIndex += update;
				if( this.currentIndex >= this.xLen )
				{
					this.currentIndex = (int)( this.currentIndex - this.xLen - 1 );
				}
				
				this.drawVerticalBar( Color.BLACK, this.currentIndex );
							
				this.graphic.setBackground( Color.BLACK );
			}
			
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
		
		if( x + w > this.width )
		{
			w = (int)( this.width - x ); 
		}
		
		this.graphic.fill( new Rectangle2D.Double( x, 0, w, this.height ) );
	}
	
	private void drawAux( double[] values, Color lineColor, int currentIndex, Double[] currentSamples )
	{
		int[] xs = new int[ values.length + currentSamples.length ];
		int[] ys = new int[ xs.length ];
		
		for( int i = 0; i < currentSamples.length; i++ )
		{
			xs[ i ] = (int)(  this.width * ( currentIndex - ( currentSamples.length - 1 ) + i ) / this.xLen );
			ys[ i ] = (int)( this.height * ( 1 - ( currentSamples[ i ] - this.minY ) / this.ySpan ) );
		}

		//this.graphic.setColor( Color.WHITE );
		//this.graphic.drawLine( xs[ 0 ], 0, xs[ 0 ], this.height );
		
		for( int i = currentSamples.length; i < xs.length; i++ )
		{
			xs[ i ] = (int)( this.width * ( currentIndex + i - 1 ) / this.xLen );
			ys[ i ] = (int)( this.height *( 1 - (values[ i - currentSamples.length ] - this.minY ) / this.ySpan ) );
		}
		
		this.drawPolyLine( xs, ys, lineColor );				
	}
	
	private void drawPolyLine( int[] xs, int[] ys, Color lineColor )
	{						
		this.graphic.setColor( lineColor );
		this.graphic.drawPolyline( xs, ys, xs.length );	
	}
}
