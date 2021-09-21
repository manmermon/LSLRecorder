/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.zTransform;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import lslrec.auxiliar.extra.NumberRange;
import lslrec.auxiliar.extra.Tuple;
import lslrec.plugin.impl.gui.BasicPainter2D;

/**
 * @author Manuel Merino Monge
 *
 */
public class ZPlanePane extends JPanel
{	
	private int xc = 0, yc = 0;
	
	private int unitCircleRadius = 1;
	
	private float thr = 1.5F;
	
	private Point mouseLoc = null;
	private int markerSize = 13;
	
	private List< Point > markers = new ArrayList< Point >(); 
	private List< Marker > zeros_poles = new ArrayList< Marker >();
	
	private Marker.Type markerType = Marker.Type.ZERO;
	
	/**
	 * 
	 */
	public ZPlanePane() 
	{
		super();
		
		super.setDoubleBuffered( true );
		
		super.setBackground( Color.WHITE );
				
		this.mouseListener();
	}
	
	public List< Marker > getZerosPoles()
	{
		return this.zeros_poles;
	}
	
	private void mouseListener()
	{
		final ZPlanePane zpp = this;
		
		super.addMouseMotionListener( new MouseMotionAdapter() 
		{
			@Override
			public void mouseMoved(MouseEvent e) 
			{
				mouseLoc = e.getPoint();			
				
				repaint();
			}
		});
		
		super.addMouseListener( new MouseAdapter() 
		{
			@Override
			public void mouseReleased( MouseEvent e ) 
			{
				Point mloc = e.getPoint();
				markers.add( mloc );
				
				int[] locs = getMarkerLocation( mloc, unitCircleRadius, zpp.getSize() );
				
				Point m1 = new Point( locs[ 0 ] + markerSize / 2, locs[ 1 ] + markerSize / 2 );
				//Point m2 = new Point( locs[ 0 ] + markerSize / 2, locs[ 2 ] + markerSize / 2 );
				
				Tuple< Double, Double > msc1 = getScaleMarkerValue( m1, unitCircleRadius );
				Tuple< Double, Double > msc2 = new Tuple<Double, Double>( msc1.t1, -msc1.t2 );
				
				zeros_poles.add( new Marker( msc1.t1, msc1.t2, markerType ) );
				
				if( msc1.t2 != 0 )
				{
					zeros_poles.add( new Marker( msc2.t1, msc2.t2, markerType ) );
				}
				
				System.out.println("zPlanePane.mouseListener().new MouseAdapter() {...}.mouseReleased() " + zeros_poles );
			}
		});
	}
	
	@Override
	protected void paintComponent( Graphics graphics ) 
	{
		super.paintComponent( graphics );   
		
		Dimension dim = super.getSize();
		
		int squareAxis = Math.min( dim.width, dim.height );
		
		double maxValue = 1.5;
		
		int rCirc = (int)( squareAxis  - squareAxis * ( 1 / maxValue  - 0.5 ) );		
		rCirc = ( rCirc < 1 ) ? 1 : rCirc;
		
		Image zPlane = BasicPainter2D.createEmptyCanva( dim.width, dim.height, null );
		
		int x = ( dim.width - rCirc) / 2;
		int y = ( dim.height - rCirc) / 2;
		
		x = ( x < 0 ) ? 0 : x;
		y = ( y < 0 ) ? 0 : y;
		
		this.xc =  x + rCirc / 2;
		this.yc = y + rCirc / 2;
		
		BasicPainter2D.line( this.xc, 0, this.xc, dim.height, 1, Color.BLACK, zPlane );
		BasicPainter2D.line( 0, yc, dim.width, yc, 1, Color.BLACK, zPlane );
		BasicPainter2D.circumference( x, y, rCirc, this.thr, Color.RED, zPlane );
		
		if( this.mouseLoc != null )
		{
			int[] markerLoc = this.getMarkerLocation( mouseLoc, rCirc, dim );
			int mx = markerLoc[ 0 ];
			int my = markerLoc[ 1 ];
			int myc = markerLoc[ 2 ];
			
			
			BasicPainter2D.circumference( mx, my, this.markerSize, 1, Color.BLUE, zPlane );
			BasicPainter2D.circumference( mx, myc, this.markerSize, 1, Color.BLUE, zPlane );
			
			
			/*
			mouseLoc.x -= xc;
			mouseLoc.y = yc - mouseLoc.y;
							
			double x = ( 2D * mouseLoc.x ) / unitCircleRadius;
			double y = ( 2D * mouseLoc.y ) / unitCircleRadius;
			*/
			
		}
		
		for( Point mark : this.markers )
		{
			int[] markerLoc = this.getMarkerLocation( mark, rCirc, dim );
			int mx = markerLoc[ 0 ];
			int my = markerLoc[ 1 ];
			int myc = markerLoc[ 2 ];
			
			
			BasicPainter2D.circumference( mx, my, markerSize, 1, Color.BLUE, zPlane );
			BasicPainter2D.circumference( mx, myc, markerSize, 1, Color.BLUE, zPlane );
		}
		
		this.unitCircleRadius = rCirc; 
		
		graphics.drawImage( zPlane, 0, 0, getFocusCycleRootAncestor( ) );
	}
	
	private int[] getMarkerLocation( Point markerLoc, int rCirc, Dimension dim )
	{
		int mx = markerLoc.x - markerSize / 2;
		int my = markerLoc.y - markerSize / 2;
		int myc = dim.height - markerLoc.y - markerSize / 2;
				
		NumberRange r = new NumberRange( ( rCirc - this.thr*10 ) / rCirc, ( rCirc + this.thr*10 ) / rCirc );
						
		Tuple< Double, Double > mscale = getScaleMarkerValue( markerLoc, rCirc );
		
		double dx = mscale.t1;
		double dy = mscale.t2;
		
		double m = Math.sqrt( dx * dx + dy * dy );
		
		if( r.within( Math.abs( m ) ) )
		{			
			double ang = Math.atan2( dx, dy);
			
			double iy = rCirc * Math.cos( ang ) /2;
			double ix = rCirc * Math.sin( ang ) /2;
			
			mx = xc + (int)ix - markerSize / 2;
			my = yc - (int)iy - markerSize / 2;
			myc = yc + (int)iy - markerSize / 2;
		}
		
		return new int[] { mx, my, myc };
	}
	
	private Tuple< Double, Double > getScaleMarkerValue( Point markerLoc, double rCirc )
	{
		double dx = (markerLoc.x - xc);
		double dy = yc - markerLoc.y;
		
		dx = ( 2D * dx ) / rCirc;
		dy = ( 2D * dy ) / rCirc;
		
		NumberRange r = new NumberRange( ( rCirc - this.thr*10 ) / rCirc, ( rCirc + this.thr*10 ) / rCirc );
		double m = Math.sqrt( dx * dx + dy * dy );
		
		double ix = dx;
		double iy = dy;
		
		if( r.within( Math.abs( m ) ) )
		{
			double ang = Math.atan2( dx, dy);
			
			iy = Math.cos( ang );
			ix = Math.sin( ang );
			
			ix = ( Math.abs( ix ) < 1e-5D ) ? 0 : ix;
			iy = ( Math.abs( iy ) < 1e-5D ) ? 0 : iy;					
		}
		
		return new Tuple< Double, Double >( ix, iy );
	}
	
}
