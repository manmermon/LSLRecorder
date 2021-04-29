/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.openposePlotter;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * @author Manuel Merino Monge
 *
 */
public class OpenposePlotterPanel extends JPanel 
{    
	/**
	 * 
	 */
	private static final long serialVersionUID = 9101899790779650335L;
	private BufferedImage bufferedImage;
	private Graphics2D graphic = null;
	
	private int width;
	private int height;

	//private Dimension oppResolution;
	
	private float pointSize = 10;
	
	private List< Point2D.Double > currentSample = new ArrayList< Point2D.Double >();
		
	private Object lock = new Object();
	
	private Timer resizeImageTimer = null;
	private boolean first = true;
	/**
	 * 
	 */
	public OpenposePlotterPanel() //( Dimension oppImageResolution  ) 
	{
		super();
		
		super.setBorder( BorderFactory.createEtchedBorder() );
		
		super.setDoubleBuffered( true );
		
		/*
		this.oppResolution = new Dimension( oppImageResolution );
		
		if( this.oppResolution == null 
				|| this.oppResolution.width <= 0 
				|| this.oppResolution.height <= 0 )
		{
			throw new IllegalArgumentException( "Resolution must be a non-zero, positive value." );
		}
		*/
		
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
			super.setVisible( false );
			
			synchronized( this.lock )
			{	
				int chs = data.length;
								
 				this.clearDrawData( Color.WHITE, this.currentSample );
												
				this.currentSample.clear();
				
				this.graphic.setColor( Color.RED );
				
				for( int r = 0; r < chs; r++ )
				{
					double[] values = data[ r ];
					
					/*
					double x = this.width * ( values[ 1 ] / this.oppResolution.width ) - this.pointSize / 2;
					double y = this.height * ( values[ 2 ]  / this.oppResolution.height ) - this.pointSize / 2;
					*/
					
					double x = this.width * values[ 1 ]  - this.pointSize / 2;
					double y = this.height * values[ 2 ]  - this.pointSize / 2;
				
					this.currentSample.add( new Point2D.Double( x, y ) );															
				}
				
				this.paintPoints( Color.RED, this.currentSample, (int)this.pointSize );
			}
			
			super.setVisible( true );
			
			repaint();            
		}
	}   
		
	private void clearDrawData( Color c, List< Point2D.Double > prevPoints )
	{	
		if( prevPoints == null || prevPoints.isEmpty() )
		{		
			this.graphic.setColor( c );
			this.graphic.fill( new Rectangle2D.Double( 0, 0, this.width, this.height ) );
		}
		else
		{
			for( Point2D.Double p : prevPoints )
			{
				p.x -= 1;
				p.y -= 1;
			}
			
			this.paintPoints( c, prevPoints, (int)this.pointSize + 2 );
		}
	}
	
	private void paintPoints( Color c,  List< Point2D.Double > prevPoints, int pointSize )
	{
		this.graphic.setColor( c );
		
		for( Point2D.Double p : prevPoints )
		{
			this.graphic.fillOval( (int)p.x, (int)p.y, pointSize, pointSize );
		}
	}
}
