package lslrec.plugin.impl.gui;
/*
 * Copyright 2011-2018 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of CLIS.
 *
 *   CLIS is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CLIS is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CLIS.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;
import java.util.Arrays;

public class basicPainter2D
{	
	public static final int NORTH = 0;
	public static final int SOUTH = 1;
	public static final int EAST = 2;
	public static final int WEST = 3;
	
	public static BufferedImage copyImage( Image originalImage )
	{
		BufferedImage copyOfImage = null;
		if( originalImage != null )
		{
			if( !( originalImage instanceof BufferedImage ) )
			{
				copyOfImage = (BufferedImage)createEmptyCanva( originalImage.getWidth( null )
																, originalImage.getHeight( null )
																, null );
				
				Graphics2D g = copyOfImage.createGraphics();
				g.drawImage( originalImage, 0, 0, null);
				g.dispose();
			}
			else
			{
				BufferedImage aux = (BufferedImage)originalImage;
				ColorModel cm = aux.getColorModel();
				boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
				WritableRaster raster = aux.copyData( null );
										
				copyOfImage = new BufferedImage( cm , raster, isAlphaPremultiplied, null );
			}
		}
		
		return copyOfImage;
	}

	/*
	public static BufferedImage convertToBufferedImage( Image image )
	{
	    BufferedImage newImage = new BufferedImage(
	        image.getWidth(null), image.getHeight(null),
	        BufferedImage.TYPE_INT_ARGB);
	    
	    Graphics2D g = newImage.createGraphics();
	    g.drawImage(image, 0, 0, null);
	    g.dispose();
	    
	    return newImage;
	}
	//*/
	
	public static Image changeColorPixels( Color oldColor, Color newColor, Image img )
	{	
		if( oldColor != null && img != null )
		{ 
			BufferedImage aux = (BufferedImage)img;

			DataBufferInt rasterDB = (DataBufferInt)aux.getRaster().getDataBuffer();
			int[] imagePixelData = rasterDB.getData();
		
			int a = 0;
			int r = 0;
			int g = 0;
			int b = 0;
			
			int new_argb = 0;
			
			if( newColor != null )
			{
				a = ( newColor.getAlpha() << 24 ) & 0xFF000000;
				r = ( newColor.getRed() << 16 ) & 0x00FF0000;
				g = ( newColor.getGreen() << 8 ) & 0x0000FF00;
				b = ( newColor.getBlue() ) & 0x000000FF;
				
				new_argb = a | r | g | b;
			}
			
			a = ( oldColor.getAlpha() << 24 ) & 0xFF000000;
			r = ( oldColor.getRed() << 16 ) & 0x00FF0000;
			g = ( oldColor.getGreen() << 8 ) & 0x0000FF00;
			b = ( oldColor.getBlue() ) & 0x000000FF;
			
			int old_argb = a | r | g | b;
			
			for( int i = 0; i < imagePixelData.length; i++ )
			{	
				int px = imagePixelData[ i ];
				if( px == old_argb )
				{
					imagePixelData[ i ] = new_argb;
				}
			}			
		}
		return img;
	}
	
	public static Image changeColorPixels( Color oldColor, Color newColor, float tolerance, Image img )
	{	
		if( oldColor != null && img != null )
		{ 
			if( tolerance < 0 ) 
			{
				tolerance = 0;
			}
			else if( tolerance > 1 )
			{
				tolerance = 1;
			}
			
			BufferedImage aux = (BufferedImage)img;

			float[] hsb = Color.RGBtoHSB( oldColor.getRed(), oldColor.getGreen(), oldColor.getBlue(), null );
			
			float[] hsb1 = Arrays.copyOf( hsb, hsb.length );
			float[] hsb2 = Arrays.copyOf( hsb, hsb.length );
			for( int i = 0; i < hsb.length; i++ )
			{
				hsb1[ i ] -= tolerance;
				hsb2[ i ] += tolerance;
			}
			
			
			DataBufferInt rasterDB = (DataBufferInt)aux.getRaster().getDataBuffer();
			int[] imagePixelData = rasterDB.getData();
		
			int a = newColor.getAlpha();
			int r = newColor.getRed();
			int g = newColor.getGreen();
			int b = newColor.getBlue();
			
			for( int i = 0; i < imagePixelData.length; i++ )
			{	
				int px = imagePixelData[ i ];
				
				Color pxColor = new Color( px );
				float[] pxHSB = Color.RGBtoHSB( pxColor.getRed(), pxColor.getGreen(), pxColor.getBlue(), null );
								
				Color c = null;
				if( hsb[ 1 ] != 0 )
				{
					if( pxHSB[ 0 ] >= hsb1[ 0 ]
							&& pxHSB[ 0 ] <= hsb2[ 0 ] 
							&& pxHSB[ 1 ] >= hsb1[ 1 ] 
							&& pxHSB[ 1 ] <= hsb2[ 1 ] 
							&& pxHSB[ 2 ] >= hsb1[ 2 ] 
							&& pxHSB[ 2 ] <= hsb2[ 2 ]  
							)
						
					{
						c = new Color( r, g, b, pxColor.getAlpha() );
						imagePixelData[ i ] = c.getRGB();
					}
				}
				else if( pxHSB[ 2 ] >= hsb1[ 2 ] 
						&& pxHSB[ 2 ] <= hsb2[ 2 ] )
				{
					c = new Color( r, g, b, pxColor.getAlpha() );
				}
				
				if( c != null )
				{				
					imagePixelData[ i ] = c.getRGB();
				}
			}			
		}
		return img;
	}
	
	public static Image createImageFromArray( Integer[][] datImg )
	{
		BufferedImage out = null;
		
		if( datImg != null && datImg.length > 0 && datImg[ 0 ].length > 0 )
		{
			int h = datImg.length ;
			int w = datImg[ 0 ].length;
						
			out = (BufferedImage)createEmptyCanva(  w, h, Color.red );
			
			for( int r = 0; r < datImg.length; r++ )
			{
				for( int c = 0 ; c < datImg[ 0].length; c++ )
				{
					int argb = datImg[ r ][ c ];
					int b = ( argb & 0x00FF0000 ) >> 16;
					int g = ( argb & 0x0000FF00 ) >> 8;
					int red = ( argb & 0x000000FF );
							
					Color color = new Color( red, g, b );
					//basicPainter2D.paintLine( c, r, c, r, 1, color, out );
					out.setRGB( c, r, color.getRGB() );
				}
			}
		}
		
		return out;
	}
	
	public static Image concatImages( Image img1, Image img2, boolean horizontal )
	{
		Image img = null;
		
		if( img1 == null )
		{
			img = copyImage( img2 );
		}
		else if( img2 == null )
		{
			img = copyImage( img1 );
		}
		else
		{
			int w = img1.getWidth( null );
			int h = img1.getHeight( null );
			Point copyLoc = new  Point( w, 0 );
			
			if( horizontal )
			{
				w += img2.getWidth( null );
			}
			else
			{
				copyLoc.x = 0;
				copyLoc.y = h;
						
				h += img2.getHeight( null );				
			}
			
			img = createEmptyCanva( w, h, null );
			
			composeImage( img, 0, 0, img1 );
			composeImage( img, copyLoc.x, copyLoc.y, img2 );
					
		}
		
		return img;
	}
	
	public static Image keepColor( Color preserverColor, Image img )
	{	
		if( preserverColor != null )
		{ 
			BufferedImage aux = (BufferedImage)img;

			DataBufferInt rasterDB = (DataBufferInt)aux.getRaster().getDataBuffer();
			int[] imagePixelData = rasterDB.getData();
		
			int new_argb = 0;
			
			int old_argb = preserverColor.getRGB();
			
			for( int i = 0; i < imagePixelData.length; i++ )
			{	
				int px = imagePixelData[ i ];
				if( px != old_argb )
				{
					imagePixelData[ i ] = new_argb;
				}
			}			
		}
		return img;
	}
	
	public static Image dot( int x, int y, int thickness, Color c, boolean fill, Image img )
	{	
		Image imagen = null;
		
		if( fill )
		{
			imagen = circle( x, y, thickness, c, img );
		}
		else
		{
			imagen = circumference( x, y, thickness, 1, c, img );
		}
		
		return imagen;
	}
	
	public static Image line( int x1, int y1, int x2, int y2, float thickness, Color c, Image img )
	{	
		BufferedImage imagen = null;
		Graphics g = null;
		
		BasicStroke b = new BasicStroke( thickness );
		
		if( img != null )
		{
			imagen = ((BufferedImage)img).getSubimage( 0, 0, img.getWidth( null ), img.getHeight( null ) );
			g = imagen.getGraphics();						
		}
		else
		{			
			int wd = Math.abs( x1 - x2 );
			int hg = Math.abs( y1 - y2 );
			
			wd += (int)b.getLineWidth()/2;
			hg += (int)b.getLineWidth()/2;
			
			imagen = new BufferedImage( wd, hg, BufferedImage.TYPE_INT_ARGB );			
			g = imagen.getGraphics();
		}
		
		((Graphics2D)g).setStroke( b);	
		((Graphics2D)g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
		g.setColor( c );
		g.drawLine( x1, y1, x2, y2);
		
		return imagen;
	}
	
	public static Image polygonLine( int[] xs, int[] ys, float thickness, Color c, Image img )
	{
		if( xs == null || ys == null || xs.length != ys.length )
		{
			throw new IllegalArgumentException( "El vector de coordenadas del poligono son nulo o de diferentes longitudes.");
		}
		
		BufferedImage imagen = null;
		Graphics g = null;
		
		if( img != null )
		{
			imagen = ((BufferedImage)img).getSubimage( 0, 0, img.getWidth( null ), img.getHeight( null ) );
			g = imagen.getGraphics();						
		}
		else
		{
			int wd = Integer.MIN_VALUE, hg = Integer.MIN_VALUE;
			for( int i = 0; i < xs.length; i++ )
			{
				if( xs[ i ] > wd )
				{
					wd = xs[ i ];
				}
				
				if( ys[ i ] > hg )
				{
					hg = ys[ i ];
				}
			}
			
			imagen = new BufferedImage( wd + 1, hg + 1, BufferedImage.TYPE_INT_ARGB );			
			g = imagen.getGraphics();
		}
		
		((Graphics2D)g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
		g.setColor( c );
		((Graphics2D)g).setStroke( new BasicStroke( thickness ) );
		g.drawPolyline( xs, ys, xs.length );
		
		return imagen;	
	}
	
	
	public static Image outlinePolygon( int[] xs, int[] ys, float thickness, Color c, Image img )
	{
		if( xs == null || ys == null || xs.length != ys.length )
		{
			throw new IllegalArgumentException( "El vector de coordenadas del poligono son nulo o de diferentes longitudes.");
		}
		
		BufferedImage imagen = null;
		Graphics g = null;
		
		if( img != null )
		{
			imagen = ((BufferedImage)img).getSubimage( 0, 0, img.getWidth( null ), img.getHeight( null ) );
			g = imagen.getGraphics();						
		}
		else
		{
			int wd = Integer.MIN_VALUE, hg = Integer.MIN_VALUE;
			for( int i = 0; i < xs.length; i++ )
			{
				if( xs[ i ] > wd )
				{
					wd = xs[ i ];
				}
				
				if( ys[ i ] > hg )
				{
					hg = ys[ i ];
				}
			}
			
			wd += (int)( thickness /2 );
			hg += (int)( thickness /2 );
			
			imagen = new BufferedImage( wd, hg, BufferedImage.TYPE_INT_ARGB );			
			g = imagen.getGraphics();
		}
		
		((Graphics2D)g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
		g.setColor( c );
		((Graphics2D)g).setStroke( new BasicStroke( thickness ) );
		g.drawPolygon( xs, ys, xs.length );
		
		return imagen;
	}
	
	public static Image fillPolygon( int[] xs, int[] ys,Color c, Image img )
	{
		if( xs == null || ys == null || xs.length != ys.length )
		{
			throw new IllegalArgumentException( "El vector de coordenadas del poligono son nulo o de diferentes longitudes.");
		}
		
		BufferedImage imagen = null;
		Graphics g = null;
		
		if( img != null )
		{
			imagen = ((BufferedImage)img).getSubimage( 0, 0, img.getWidth( null ), img.getHeight( null ) );
			g = imagen.getGraphics();						
		}
		else
		{
			int wd = Integer.MIN_VALUE, hg = Integer.MIN_VALUE;
			for( int i = 0; i < xs.length; i++ )
			{
				if( xs[ i ] > wd )
				{
					wd = xs[ i ];
				}
				
				if( ys[ i ] > hg )
				{
					hg = ys[ i ];
				}
			}
			
			
			imagen = new BufferedImage( wd, hg, BufferedImage.TYPE_INT_ARGB );			
			g = imagen.getGraphics();
		}
		
		((Graphics2D)g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
		g.setColor( c );
		g.fillPolygon( xs, ys, xs.length );
		
		return imagen;
	}
	
	public static Image triangle( int lado, float thicknessBorder, Color colorBorde, Color colorRelleno, int orientacion )
	{		
		int[] xs = { 0,    lado, lado / 2 };
		int[] ys = { lado, lado, 0 };
		
		float lt = lado - thicknessBorder;
		int[] xs2 = { (int)thicknessBorder/2, (int)lt,(int)( (thicknessBorder + lt) / 2 )};
		int[] ys2 = { (int)lt, (int)lt, (int)thicknessBorder/2 };
		
		switch ( orientacion ) 
		{
			case( SOUTH ):
			{
				ys[ 0 ] = 0; ys[ 1 ] = 0; ys[ 2 ] = lado;
				ys2[ 0 ] = (int)thicknessBorder/2; ys2[ 1 ] = (int)thicknessBorder/2; ys2[ 2 ] = (int)lt;
				break;
			}	
			case( WEST ):
			{
				xs[ 0 ] = lado; xs[ 1 ] = lado; xs[ 2 ] = 0;
				ys[ 0 ] = 0;    ys[ 1 ] = lado; ys[ 2 ] = lado / 2;
				
				xs2[ 0 ] = (int)lt; xs2[ 1 ] = (int)lt; xs2[ 2 ] = (int)thicknessBorder/2;
				ys2[ 0 ] = (int)thicknessBorder/2; ys2[ 1 ] = (int)lt; ys2[ 2 ] = (int)( ( thicknessBorder + lt ) / 2 );
				break;
			}
			case( EAST ):
			{
				xs[ 0 ] = 0; xs[ 1 ] = 0;    xs[ 2 ] = lado;
				ys[ 0 ] = 0; ys[ 1 ] = lado; ys[ 2 ] = lado / 2;
				
				xs2[ 0 ] = (int)thicknessBorder/2; xs2[ 1 ] = (int)thicknessBorder/2; xs2[ 2 ] = (int)lt;
				ys2[ 0 ] = (int)thicknessBorder/2;    ys2[ 1 ] = (int)lt; ys2[ 2 ] = (int)( ( thicknessBorder + lt ) / 2 );
				break;
			}
			default:
			{
				break;
			}			
		}
		
		Image img = null;
		
		if( colorRelleno != null )
		{
			img = fillPolygon( xs, ys, colorRelleno , img );
		}
		
		if( colorBorde != null )
		{	
			img = outlinePolygon( xs2, ys2, thicknessBorder, colorBorde, img );
		}
		
		 return img;		
	}
	
	public static Image circle( int x, int y, int radio, Color c, Image img )
	{				
		BufferedImage imagen = null;
		
		Graphics g = null;
		
		if( img != null )
		{
			imagen = ((BufferedImage)img).getSubimage( 0, 0, img.getWidth( null ), img.getHeight( null ) );
			g = imagen.getGraphics();						
		}
		else
		{
		
			imagen = new BufferedImage( radio, radio, BufferedImage.TYPE_INT_ARGB );			
			g = imagen.getGraphics();
		}
		
		((Graphics2D)g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
		g.setColor( c );
		g.fillOval( x, y, radio, radio );
		
		return imagen;		
	}
	
	public static Image circumference( int x, int y, int radio, float thicknessBorder, Color c, Image img )
	{
		BufferedImage imagen = null;
		Graphics g = null;
		
		BasicStroke b = new BasicStroke( thicknessBorder );
		float lineWidth = b.getLineWidth(); 
		int p = Math.round( lineWidth / 2 );
		if( p < 1 )
		{
			p = 1;
		}
		
		int r = (int)( radio - lineWidth - 1);
				
		if( img != null )
		{
			imagen = ((BufferedImage)img).getSubimage( 0, 0, img.getWidth( null ), img.getHeight( null ) );
			g = imagen.getGraphics();						
		}
		else
		{		
			imagen = new BufferedImage( radio, radio, BufferedImage.TYPE_INT_ARGB );			
			g = imagen.getGraphics();
		}
		
		((Graphics2D)g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
		g.setColor( c );		
		((Graphics2D)g).setStroke( b );
		g.drawOval( x + p, y + p, r, r ); //radio - p - 1, radio - p -1 );
				
		return imagen;
	}
	
	public static Image oval( int x, int y, int width, int height, float thicknessBorder, Color colorBorder, Color colorRelleno, Image img )
	{
		BufferedImage imagen = null;
		Graphics g = null;
		
		BasicStroke b = new BasicStroke( thicknessBorder );
		int p = (int)(b.getLineWidth() / 2);
		
		if( img != null )
		{
			imagen = ((BufferedImage)img).getSubimage( 0, 0, img.getWidth( null ), img.getHeight( null ) );
			g = imagen.getGraphics();						
		}
		else
		{
		
			imagen = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );			
			g = imagen.getGraphics();
		}
		
		if( colorRelleno != null )
		{
			((Graphics2D)g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_OFF );
			g.setColor( colorRelleno );
			g.fillOval( x + p, y + p, width - p * 2, height - p * 2);
		}
		
		Color cBorder = Color.BLACK;
		if( colorBorder != null )
		{
			cBorder = colorBorder;
		}
		
		((Graphics2D)g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
		g.setColor( cBorder );
		((Graphics2D)g).setStroke( new BasicStroke( thicknessBorder ) );
		g.drawOval( x + p, y + p, width - p * 2, height - p * 2 );
				
		
		
		return imagen;
	}
	
	public static Image arc( int x, int y, int width, int height, int startAngle, int arcAngle, float thicknessBorder, Color colorBorde, Color ColorRelleno, Image img )
	{
		BufferedImage imagen = null;
		Graphics g = null;
				
		BasicStroke b = new BasicStroke( thicknessBorder );
		int p = (int)(b.getLineWidth() / 2);
		
		if( img != null )
		{
			imagen = ((BufferedImage)img).getSubimage( 0, 0, img.getWidth( null ), img.getHeight( null ) );
			g = imagen.getGraphics();						
		}
		else
		{	
			int w = width + p;
			int h = height + p;
						
			imagen = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB );			
			g = imagen.getGraphics();
		}
		
		if( ColorRelleno != null )
		{	
			g.setColor( ColorRelleno );
			g.fillArc( x + p, y + p, width - p -1, height - p - 1, startAngle, arcAngle );
		}
		
		Color cborde = Color.BLACK;
		
		if( colorBorde != null )
		{
			cborde = colorBorde;
		}
		
		((Graphics2D)g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
		g.setColor( cborde );
		((Graphics2D)g).setStroke( new BasicStroke( thicknessBorder ) );
		g.drawArc( x + p, y + p, width - p - 1, height - p - 1, startAngle, arcAngle );
				
		return imagen;
	}
	
	public static Image rectangle( int ancho, int alto, float thicknessBorder, Color colorBorde, Color colorRelleno )
	{		
		int t = (int)thicknessBorder;
		
		if( t < 2 )
		{
			t = 2;
		}
		
		int[] xs = { t/2, ancho - t /2, ancho - t/2, t/2 };
		int[] ys = { t/2,  t/2, alto - t/2,  alto - t/2 };
		
		Image img = null;
		
		//BasicStroke b = new BasicStroke( thicknessBorder );
		//int p = (int)(b.getLineWidth() / 2);
		
		if( colorRelleno != null )
		{
			int[] xs2 = { 0, ancho, ancho, 0 };
			int[] ys2 = { 0,  0, alto,  alto };
			
			img = fillPolygon( xs2, ys2, colorRelleno , img );
		}
		
		Color cBorde = Color.BLACK;
		
		if( colorBorde != null )
		{
			cBorde = colorBorde;
		}
		
		return outlinePolygon( xs, ys, thicknessBorder, cBorde, img );		
	}

	public static Image roundRectangle( int x, int y, int width,
													int height, int arcWidth, int arcHeight, float thickness, Color
													borderColor, Color fillColor, Image img )
	{
		BufferedImage imagen = null;
		Graphics g = null;

		BasicStroke b = new BasicStroke( thickness );

		if( img != null )
		{
			imagen = ((BufferedImage)img).getSubimage( 0, 0, img.getWidth( null ),
					img.getHeight( null ) );
			g = imagen.getGraphics();
		}
		else
		{
			imagen = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
			g = imagen.getGraphics();
		}

		((Graphics2D)g).setStroke( b);
		((Graphics2D)g).setRenderingHint(
				RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );

		if( fillColor != null )
		{
			g.setColor( fillColor );
			g.fillRoundRect( x, y, width, height, arcWidth, arcHeight );
		}

		g.setColor( borderColor );
		g.drawRoundRect(x, y, width, height, arcWidth, arcHeight);

		return imagen;
	}

	public static Image diamond( int lado, float thicknessBorder, Color colorBorde, Color colorRelleno )
	{
		int w = (int) Math.round( ( 1.0  * lado ) / ( Math.sqrt( 2.0 ) ) );
		
		int[] xs = { w,	0,	w,   w*2 };
		int[] ys = { 0, w,	w*2, w  };
		
		Image img = null;
		
		if( colorRelleno != null )
		{
			img = fillPolygon( xs, ys, colorRelleno , img );
		}
		
		Color cBorde = Color.BLACK;
		
		if( colorBorde != null )
		{
			cBorde = colorBorde;
		}
		
		return outlinePolygon( xs, ys, thicknessBorder, cBorde, img );
	}

	public static Image text( int x, int y, String texto, FontMetrics fm, Color colorBorder, Color colorRelleno, Image img )
	{
		BufferedImage imagen = null;
		Graphics2D g = null;
		
		if( img != null )
		{
			imagen = ((BufferedImage)img).getSubimage( 0, 0, img.getWidth( null ), img.getHeight( null ) );
			g = (Graphics2D)imagen.getGraphics();						
		}
		else if( fm != null )
		{ 		
			int wd = fm.stringWidth( texto ), hg = fm.getAscent() - fm.getLeading() * 2;
			imagen = new BufferedImage( wd, hg, BufferedImage.TYPE_INT_ARGB );			
			g = (Graphics2D)imagen.getGraphics();
		}
		
		if( imagen != null )
		{
			if( fm == null )
			{
				fm = resizeFontMetrics( g, new Dimension( imagen.getWidth(),  imagen.getHeight() ), texto );
			}
		
			((Graphics2D)g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
			g.setFont( fm.getFont() );
			
			int pos = fm.getAscent() - fm.getDescent() - fm.getLeading() * 2;
			
			if( colorBorder != null )
			{
				g.setColor( colorBorder );
				
				g.drawString( texto, x-1, y + pos);
				g.drawString( texto, x+1, y + pos );
				g.drawString( texto, x, y + pos - 1);
				g.drawString( texto, x, y + pos + 1);
			}
			
			g.setColor( colorRelleno );
			g.drawString( texto, x, y + pos );
		}
		
		return imagen;
	}	
	
	public static Image text( String texto, FontMetrics fm, Color colorBorder, Color colorRelleno, Image img )
	{
		BufferedImage imagen = null;
		Graphics2D g = null;
		
		if( img != null )
		{
			imagen = ((BufferedImage)img).getSubimage( 0, 0, img.getWidth( null ), img.getHeight( null ) );
			g = (Graphics2D)imagen.getGraphics();						
		}
		else if( fm != null )
		{ 		
			int wd = fm.stringWidth( texto ), hg = fm.getAscent() - fm.getLeading() * 2;
			imagen = new BufferedImage( wd, hg, BufferedImage.TYPE_INT_ARGB );			
			g = (Graphics2D)imagen.getGraphics();
		}
		
		if( imagen != null )
		{
			if( fm == null )
			{
				fm = resizeFontMetrics( g, new Dimension( imagen.getWidth(),  imagen.getHeight() ), texto );
			}
			
			int w = imagen.getWidth();
			int h = imagen.getHeight();
			int strW = fm.stringWidth( texto );
			int strH = fm.getAscent() - fm.getDescent() - fm.getLeading();			
			int x = ( w - strW ) / 2;
			int y = ( h - strH ) / 2;
			
			text( x, y, texto, fm, colorBorder, colorRelleno, imagen );
		}
		
		return imagen;
	}	
	
	private static FontMetrics resizeFontMetrics( Graphics g, Dimension size, String text )
	{
		FontMetrics fm = null;
		
		if( g != null )
		{
			fm = resizeHeightFontMetric( g, size.height );
			
			if( fm.stringWidth( text ) > size.width )
			{
				fm = resizeWidthFontMetric( g, size.width, text );
			}
		}
		
		return fm;
	}
	
	private static FontMetrics resizeHeightFontMetric( Graphics g, int size )
	{
		FontMetrics fm = null;
		
		if( g != null )
		{	
			Font f = new Font( Font.DIALOG, Font.BOLD, 12 );

			fm = g.getFontMetrics();
			while( fm.getHeight() < size )
			{
				f = new Font( f.getName(), Font.BOLD, f.getSize() + 1 );
				fm = g.getFontMetrics( f );
			}

			while( fm.getHeight() > size )
			{
				f = new Font( f.getName(), Font.BOLD, f.getSize() - 1 );
				fm = g.getFontMetrics( f );
			}
		}
		
		return fm;
	}
	
	private static FontMetrics resizeWidthFontMetric( Graphics g, int size, String txt )
	{
		FontMetrics fm = null;
		
		if( g != null )
		{	
			Font f = new Font( Font.DIALOG, Font.BOLD, 12 );

			fm = g.getFontMetrics();
			while( fm.stringWidth( txt ) < size )
			{
				f = new Font( f.getName(), Font.BOLD, f.getSize() + 1 );
				fm = g.getFontMetrics( f );
			}

			while( fm.stringWidth( txt ) > size )
			{
				f = new Font( f.getName(), Font.BOLD, f.getSize() - 1 );
				fm = g.getFontMetrics( f );
			}
		}
		
		return fm;
	}
	
	public static Image outlineText( int x, int y, String texto, FontMetrics fm, Color color, Image img )
	{
		BufferedImage imagen = null;
		Graphics2D g = null;
		
		if( img != null )
		{
			imagen = ((BufferedImage)img).getSubimage( 0, 0, img.getWidth( null ), img.getHeight( null ) );
			g = (Graphics2D)imagen.getGraphics();						
		}
		else if( fm != null )
		{ 			
			int wd = fm.stringWidth( texto ), hg = fm.getHeight();
			imagen = new BufferedImage( wd + 1, hg + 1, BufferedImage.TYPE_INT_ARGB );			
			g = (Graphics2D)imagen.getGraphics();
		}
		
		if( imagen != null )
		{
			if( fm == null )
			{
				fm = resizeFontMetrics( g, new Dimension( imagen.getWidth(),  imagen.getHeight() ), texto );
			}
			
			((Graphics2D)g).setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
			g.setFont( fm.getFont() );
			g.setColor( color );
			g.draw( generateShapeFromText( x, y + fm.getAscent() - fm.getDescent() - 3 * fm.getLeading()
											, imagen.getWidth(), imagen.getHeight(), fm.getFont(), texto ) );
		}
		
		return imagen;
	}
	
	public static Image outlineText( String texto, FontMetrics fm, Color color, Image img )
	{
		BufferedImage imagen = null;
		Graphics2D g = null;
		
		if( img != null )
		{
			imagen = ((BufferedImage)img).getSubimage( 0, 0, img.getWidth( null ), img.getHeight( null ) );
			g = (Graphics2D)imagen.getGraphics();						
		}
		else if( fm != null )
		{ 		
			int wd = fm.stringWidth( texto ), hg = fm.getAscent() - fm.getLeading() * 2;
			imagen = new BufferedImage( wd, hg, BufferedImage.TYPE_INT_ARGB );			
			g = (Graphics2D)imagen.getGraphics();
		}
		
		if( imagen != null )
		{
			if( fm == null )
			{
				fm = resizeFontMetrics( g, new Dimension( imagen.getWidth(),  imagen.getHeight() ), texto );
			}
			
			int w = imagen.getWidth();
			int h = imagen.getHeight();
			int strW = fm.stringWidth( texto );
			int strH = fm.getAscent() - fm.getDescent() - fm.getLeading();			
			int x = ( w - strW ) / 2;
			int y = ( h - strH ) / 2;
			
			outlineText( x, y, texto, fm, color, imagen );
		}
		
		return imagen;
	}	
	
	public static Image outlineImage( int x, int y, Image img, Color colorFigure, Color colorPerfil, Color fillColor, float thickness )
	{
		BufferedImage imagen = null;
		Graphics2D g = null;
		
		if( img != null )
		{
			
			BasicStroke b = new BasicStroke( thickness );
			float lineWidth = b.getLineWidth(); 
			double p = Math.round( lineWidth / 2.0 );
			
			CustomShape shape = new CustomShape( (BufferedImage)img );
			Area fig = shape.getArea_FastOutline( colorFigure );
			
			double scaleX = ( img.getWidth( null ) - 2*p ) / ( (double)img.getWidth( null ) );
			double scaleY = ( img.getHeight( null ) - 2*p ) / ( (double)img.getHeight( null ) );
			
			AffineTransform transf = new AffineTransform();
			transf.setToTranslation( p/2, p/2 );
			transf.scale( scaleX, scaleY );
			
			fig.transform( transf );
						
			imagen = (BufferedImage)createEmptyCanva( img.getWidth( null ), img.getHeight( null ), null );
			g = (Graphics2D)imagen.getGraphics();						
			
			((Graphics2D)g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );						
			
			g.setClip( null );
			g.setStroke( b );
			
			if( fillColor != null )
			{	
				g.setColor( fillColor );
				g.fill( fig );
			}
			
			g.setColor( colorPerfil );
			g.draw( fig );
		}
				
		return imagen;
	}
	
	public static Image paintOutlineFigure( int x, int y, Shape fig,  Color colorPerfil, float thickness, Image img )
	{
		BufferedImage imagen = null;
		Graphics2D g = null;
		
		if( img != null )
		{
			imagen = ((BufferedImage)img).getSubimage( 0, 0, img.getWidth( null ), img.getHeight( null ) );
			g = (Graphics2D)imagen.getGraphics();						
		}
		else
		{ 			
			Rectangle rect = fig.getBounds();
			int wd = rect.width;
			int hg = rect.height;
			imagen = new BufferedImage( wd + 1, hg + 1, BufferedImage.TYPE_INT_ARGB );			
			g = (Graphics2D)imagen.getGraphics();
		}
		
		((Graphics2D)g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
		g.setClip( null );

		//Stroke stroke = g.getStroke();
		//Shape strokedOutline = stroke.createStrokedShape( fig );
		g.setStroke( new BasicStroke( thickness ) );
		g.setColor( colorPerfil );
		//g.draw( strokedOutline );
		g.draw( fig );
				
		return imagen;
	}
	
	public static Image paintOutlineFigure( int x, int y, GeneralPath fig,  Color colorPerfil, float thickness, Image img )
	{
		BufferedImage imagen = null;
		Graphics2D g = null;
		
		if( img != null )
		{
			imagen = ((BufferedImage)img).getSubimage( 0, 0, img.getWidth( null ), img.getHeight( null ) );
			g = (Graphics2D)imagen.getGraphics();						
		}
		else
		{ 			
			Rectangle rect = fig.getBounds();
			int wd = rect.width;
			int hg = rect.height;
			imagen = new BufferedImage( wd + 1, hg + 1, BufferedImage.TYPE_INT_ARGB );			
			g = (Graphics2D)imagen.getGraphics();
		}
		
		((Graphics2D)g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
		g.setClip( null );
		Stroke stroke = g.getStroke();
		Shape strokedOutline = stroke.createStrokedShape( fig );
		g.setStroke( new BasicStroke( thickness ) );
		g.setColor( colorPerfil );
		g.draw( strokedOutline );
				
		return imagen;
	}
	
	public static Image paintFigure( int x, int y, Shape fig, Color color, Image img )
	{
		BufferedImage imagen = null;
		Graphics2D g = null;
		
		if( img != null )
		{
			imagen = ((BufferedImage)img).getSubimage( 0, 0, img.getWidth( null ), img.getHeight( null ) );
			g = (Graphics2D)imagen.getGraphics();						
		}
		else
		{ 			
			Rectangle rect = fig.getBounds();
			int wd = rect.width;
			int hg = rect.height;
			imagen = new BufferedImage( wd + 1, hg + 1, BufferedImage.TYPE_INT_ARGB );			
			g = (Graphics2D)imagen.getGraphics();
		}
		
		((Graphics2D)g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
		g.setColor( color );
		g.fill( fig );
				
		return imagen;
	}
	
	public static Image paintFigure( int x, int y, GeneralPath fig, Color color, Image img )
	{
		BufferedImage imagen = null;
		Graphics2D g = null;
		
		if( img != null )
		{
			imagen = ((BufferedImage)img).getSubimage( 0, 0, img.getWidth( null ), img.getHeight( null ) );
			g = (Graphics2D)imagen.getGraphics();						
		}
		else
		{ 			
			Rectangle rect = fig.getBounds();
			int wd = rect.width;
			int hg = rect.height;
			imagen = new BufferedImage( wd + 1, hg + 1, BufferedImage.TYPE_INT_ARGB );			
			g = (Graphics2D)imagen.getGraphics();
		}
		
		((Graphics2D)g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
		g.setColor( color );
		g.fill( fig );
				
		return imagen;
	}
	
	public static Image createEmptyCanva( int width, int height, Color c )
	{
		BufferedImage imagen = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		//imagen.getRaster().getDataBuffer().s
		Graphics2D g = imagen.createGraphics();//getGraphics();
		
		if( c != null )
		{			
			g.setPaint( c );
			g.fillRect( 0, 0, width, height);
		}
		
		return imagen;
	}	

	public static Image composeImage( Image baseImg, int x, int y, Image pasteImg )
	{		
		if( baseImg != null && pasteImg != null )
		{
			Graphics g = baseImg.getGraphics();
			g.drawImage( pasteImg, x, y, null );
			g.dispose();
		}
				
		return baseImg;
	}
	
	public static void invertColor( Image img )
	{
		if( img != null )
		{			
			BufferedImage image = (BufferedImage)img;
			final int[] pixels = ( ( DataBufferInt ) image.getRaster().getDataBuffer() ).getData();

			int width = img.getWidth( null );
			int height = img.getHeight( null );
					    
		    for (int pixel = 0; pixel < pixels.length; pixel++ ) 
		    {
		    	int px = pixels[ pixel ];

		    	pixels[ pixel ] = ~px;
		    } 
			
		    image.setRGB( 0, 0, width, height, pixels, 0, width );
		}
	}
	
	public static Shape generateShapeFromText( int x, int y, int width, int height, Font font, String string) 
	{		
	    BufferedImage img = new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2 = img.createGraphics();

	    try 
	    {
	      GlyphVector vect = font.createGlyphVector( g2.getFontRenderContext(), string );
	      //Shape shape = vect.getOutline( 0f, (float) -vect.getVisualBounds().getY() );
	      Shape shape = vect.getOutline( (float)x, (float)( y ));//-vect.getVisualBounds().getY() ));

	      return shape;
	    }
	    finally 
	    {
	      g2.dispose();
	    }
	}
	
	public static BufferedImage rotate(BufferedImage img, double degree) 
	{
		BufferedImage rotated = null;
		
		if( img != null )
		{
	        double rads = Math.toRadians( degree );
	        double sin = Math.abs(Math.sin( rads ) );
	        double cos = Math.abs(Math.cos( rads ) );
	        
	        int w = img.getWidth();
	        int h = img.getHeight();
	        
	        int newWidth = (int) Math.floor(w * cos + h * sin);
	        int newHeight = (int) Math.floor(h * cos + w * sin);
	
	        rotated = (BufferedImage)createEmptyCanva( newWidth, newHeight , null );
	        
	        Graphics2D g2d = rotated.createGraphics();
	        AffineTransform at = new AffineTransform();
	        at.translate((newWidth - w) / 2, (newHeight - h) / 2);
	
	        int x = w / 2;
	        int y = h / 2;
	
	        at.rotate(rads, x, y);
	        g2d.setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
	        g2d.setTransform(at);
	        g2d.drawImage( img, 0, 0, null );
	        g2d.dispose();
		}

        return rotated;
    }
}
