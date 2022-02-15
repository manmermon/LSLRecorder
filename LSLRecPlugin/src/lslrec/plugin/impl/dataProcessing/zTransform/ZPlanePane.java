/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.zTransform;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

import lslrec.auxiliar.extra.NumberRange;
import lslrec.auxiliar.extra.Tuple;
import lslrec.plugin.impl.gui.BasicPainter2D;

/**
 * @author Manuel Merino Monge
 *
 */
public class ZPlanePane extends JPanel
{	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4845495955082963578L;

	private int xc = 0, yc = 0;
	
	private int unitCircleRadius = 1;
	
	private float thr = 1.5F;
	
	private Point mouseLoc = null;
	private int markerSize = 13;
	
	private List< Marker > zeros_poles = new ArrayList< Marker >();
	
	private Marker.Type markerType = Marker.Type.POLE;
	
	private double samplingRate = 1;
	
	private EventListenerList listenerList;
	
	/**
	 * 
	 */
	public ZPlanePane() 
	{
		super();
		
		this.listenerList = new EventListenerList();
		
		super.setDoubleBuffered( true );
		
		super.setBackground( Color.WHITE );
				
		this.mouseListener();
	}
	
	public void setSamplingRate( double frq )
	{
		this.samplingRate = frq;
	}
	
	public void setMarkerType( Marker.Type t )
	{
		this.markerType = t;
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
			public void mouseExited(MouseEvent e) 
			{
				mouseLoc = null;
				
				repaint();
			}
		
			@Override
			public void mouseReleased( MouseEvent e ) 
			{
				Point mloc = e.getPoint();
				//markers.add( mloc );
				
				int[] locs = getMarkerLocation( mloc, unitCircleRadius, zpp.getSize() );
				
				Point m1 = new Point( locs[ 0 ], locs[ 1 ] );
				//Point m2 = new Point( locs[ 0 ] + markerSize / 2, locs[ 2 ] + markerSize / 2 );
				
				Tuple< Double, Double > msc1 = getScaleMarkerValue( m1, unitCircleRadius );
				Tuple< Double, Double > msc2 = new Tuple<Double, Double>( msc1.t1, -msc1.t2 );
				
				zeros_poles.add( new Marker( msc1.t1, msc1.t2, markerType ) );
				
				if( msc1.t2 != 0 )
				{
					zeros_poles.add( new Marker( msc2.t1, msc2.t2, markerType ) );
				}
				
				fireZeroPoleEvent( ZeroPoleEvent.NEW );
			}
		});
	}
	
	public synchronized void addZeroPoleEventListener( ZeroPoleEventListener listener ) 
	{
		this.listenerList.add( ZeroPoleEventListener.class, listener );
	}
	
	public synchronized void removeSceneEventListener( ZeroPoleEventListener listener ) 
	{
		this.listenerList.remove( ZeroPoleEventListener.class, listener );		
	}
	
	/**
	 * 
	 * @param typeEvent
	 */
	protected synchronized void fireZeroPoleEvent( int typeEvent )
	{
		ZeroPoleEvent event = new ZeroPoleEvent( this, typeEvent );

		ZeroPoleEventListener[] listeners = this.listenerList.getListeners( ZeroPoleEventListener.class );

		for (int i = 0; i < listeners.length; i++ ) 
		{
			listeners[ i ].ZeroPoleEvent( event );
		}
	}
	
	public void removeZeroPole( Marker m )
	{
		if( zeros_poles.remove( m ) )
		{
			super.repaint();
			
			this.fireZeroPoleEvent( ZeroPoleEvent.REMOVE );
		}
	}
	
	public void removeZeroPole( List< Marker > L )
	{
		if( L != null )
		{
			if( zeros_poles.removeAll( L ) )
			{
				super.repaint();
				
				this.fireZeroPoleEvent( ZeroPoleEvent.REMOVE );
			}
		}
	}
	
	public void clearZeroPole()
	{
		this.removeZeroPole( this.zeros_poles );
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
		
		int wSize = this.markerSize;
		int hSize = this.markerSize;
		
		if( this.mouseLoc != null )
		{
			int[] markerLoc = this.getMarkerLocation( mouseLoc, rCirc, dim );
			int mx = markerLoc[ 0 ];
			int my = markerLoc[ 1 ];
			int myc = markerLoc[ 2 ];
			
			if( this.markerType == Marker.Type.ZERO )
			{
				mx -= wSize / 2;
				my -= hSize / 2;
				myc -= hSize / 2;
				
				BasicPainter2D.circumference( mx, my, wSize, 1, Color.BLUE, zPlane );
				BasicPainter2D.circumference( mx, myc, wSize, 1, Color.BLUE, zPlane );
			}
			else
			{						
				
				Image imgAng = BasicPainter2D.text( "X", super.getFontMetrics( super.getFont() ), Color.BLUE, Color.BLUE, null );
				wSize = imgAng.getWidth( null );				
				hSize = imgAng.getHeight( null );
				
 				BasicPainter2D.composeImage( zPlane, mx - wSize / 2, my - hSize / 2, imgAng );
				BasicPainter2D.composeImage( zPlane, mx - wSize / 2, myc - hSize / 2, imgAng );
			}
			
			BasicPainter2D.line( xc, yc
								, mouseLoc.x, mouseLoc.y
								, this.thr, Color.BLACK, zPlane );
			BasicPainter2D.line( xc, yc
								, mouseLoc.x, dim.height - mouseLoc.y
								, this.thr, Color.BLACK, zPlane );
		
			double radian = Math.abs( Math.atan2( my - yc, mx - xc) );
			double ang = Math.toDegrees( radian );
			
			double m = 2 * Math.sqrt( ( mx - xc ) * ( mx - xc ) + ( my - yc )* ( my - yc ) ) / rCirc;
			
			int d = (int)( rCirc * m / 2 );
			if( d > rCirc / 2 )
			{
				d = rCirc / 2;
			}
			
			BasicPainter2D.arc( xc - d / 2, yc - d / 2
								, d, d
								, 0, (int)ang, 1F, Color.BLACK, null, zPlane );
		
			String tNorm = String.format( "%.3f", radian / ( 2 * Math.PI )  );
			String tRad = String.format( "%.3f", radian  );
			String tF = String.format( "%.3f",  this.samplingRate * radian / ( 2 * Math.PI )  );
			
			Font f = new Font( Font.DIALOG, Font.PLAIN, 18 );
			Image imgAngRad = BasicPainter2D.text( "R: " + tRad,  super.getFontMetrics( f )
											, Color.BLACK, Color.BLACK, null );
			Image imgAngNorm = BasicPainter2D.text( "N: " + tNorm,  super.getFontMetrics( f )
													, Color.BLACK, Color.BLACK, null );
			Image imgAngF = BasicPainter2D.text( "F: " + tF,  super.getFontMetrics( f )
												, Color.BLACK, Color.BLACK, null );
			
			int shift = Math.max( imgAngF.getWidth( null ), Math.max( imgAngRad.getWidth( null ), imgAngNorm.getWidth( null ) ) );
			
			int tx = xc - shift - 2;
			int ty = yc + 2;
			 
			if( mouseLoc.x < xc )
			{
				tx = xc + 2;
			}
							 
			
			BasicPainter2D.composeImage( zPlane, tx, ty, imgAngRad );
			BasicPainter2D.composeImage( zPlane, tx, ty + imgAngRad.getHeight( null ) + 2, imgAngNorm );
			BasicPainter2D.composeImage( zPlane, tx, ty + imgAngRad.getHeight( null ) + imgAngNorm.getHeight( null ) + 4, imgAngF );			
		}
		
		for( Marker mark : this.zeros_poles )
		{
			Tuple< Double, Double > val = mark.getValue();
			
			int mx = this.xc + (int)( val.t1 * rCirc / 2 ) - wSize / 2;
			int my = this.yc + (int)( val.t2 * rCirc / 2 ) - hSize / 2;
			
			if( mark.getType() == Marker.Type.ZERO )
			{
				BasicPainter2D.circumference( mx, my, this.markerSize, 1, Color.BLUE, zPlane );
			}
			else
			{				
				mx += wSize/2;
				my += hSize/2;
				
				Image imgAng = BasicPainter2D.text( "X", super.getFontMetrics( super.getFont() ), Color.BLUE, Color.BLUE, null );
				wSize = imgAng.getWidth( null );
				hSize = imgAng.getHeight( null );
				
				BasicPainter2D.composeImage( zPlane, mx - wSize / 2, my - hSize / 2, imgAng );
			}
		}
		
		this.unitCircleRadius = rCirc; 
		
		graphics.drawImage( zPlane, 0, 0, getFocusCycleRootAncestor( ) );
	}
	
	private int[] getMarkerLocation( Point markerLoc, int rCirc, Dimension dim )
	{
		int mx = markerLoc.x;
		int my = markerLoc.y;
		int myc = dim.height - markerLoc.y;
				
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
			
			mx = xc + (int)ix;
			my = yc - (int)iy;
			myc = yc + (int)iy;
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
