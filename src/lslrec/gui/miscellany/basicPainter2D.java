package lslrec.gui.miscellany;
/*
 * Copyright 2011-2013 by Manuel Merino Monge <manmermon@dte.us.es>
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
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.font.GlyphVector;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DataBufferInt;
import java.awt.image.WritableRaster;

public class basicPainter2D
{	
	public static final int NORTH = 0;
	public static final int SOUTH = 1;
	public static final int EAST = 2;
	public static final int WEST = 3;
	
	public static Image copyImage( Image originalImage )
	{
		BufferedImage copyOfImage = null;
		if( originalImage != null )
		{
			//int widthOfImage = originalImage.getWidth( null );
			//int heightOfImage = originalImage.getHeight( null );
			
			BufferedImage aux = (BufferedImage)originalImage;
			ColorModel cm = aux.getColorModel();
			boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
			WritableRaster raster = aux.copyData( null );
									
			copyOfImage = new BufferedImage( cm , raster, isAlphaPremultiplied, null );
			//copyOfImage = new BufferedImage( widthOfImage , heightOfImage, BufferedImage.TYPE_INT_RGB);
			//Graphics g = copyOfImage.createGraphics();
			//g.drawImage( originalImage, 0, 0, null);
		}
		
		return copyOfImage;
	}
	
	public static Image changeColorPixels( Color oldColor, Color newColor, Image img )
	{
		if( oldColor != null && newColor != null && img != null )
		{ 
			BufferedImage aux = (BufferedImage)img;

			DataBufferInt rasterDB = (DataBufferInt)aux.getRaster().getDataBuffer();
			int[] imagePixelData = rasterDB.getData();
		
			int a = ( newColor.getAlpha() << 24 ) & 0xFF000000;
			int r = ( newColor.getRed() << 16 ) & 0x00FF0000;
			int g = ( newColor.getGreen() << 8 ) & 0x0000FF00;
			int b = ( newColor.getBlue() ) & 0x000000FF;
			
			int new_argb = a | r | g | b;
			
			a = ( oldColor.getAlpha() << 24 ) & 0xFF000000;
			r = ( oldColor.getRed() << 16 ) & 0x00FF0000;
			g = ( oldColor.getGreen() << 8 ) & 0x0000FF00;
			b = ( oldColor.getBlue() ) & 0x000000FF;
			
			int old_argb = a | r | g | b;
			
			for( int i = 0; i < imagePixelData.length; i++ )
			{	
				if( imagePixelData[ i ] == old_argb )
				{
					imagePixelData[ i ] = new_argb;
				}
			}
			/*
			for( int i = 0; i < w; i++ )
			{
				for( int j = 0; j < h; j++ )
				{
					if( aux.getRGB( i, j ) == old_argb )
					{						
						aux.setRGB( i, j, new_argb );
					}
				}
			}
			*/			
		}
		return img;
	}
	
	public static Image paintDot( int x, int y, int thickness, Color c, boolean fill, Image img )
	{	
		Image imagen = null;
		
		if( fill )
		{
			imagen = paintFillCircle( x, y, thickness, c, img );
		}
		else
		{
			imagen = paintOutlineCircle( x, y, thickness, 1, c, img );
		}
		
		return imagen;
	}
	
	public static Image paintLine( int x1, int y1, int x2, int y2, float thickness, Color c, Image img )
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
			int wd = Math.abs( x1 - x2 );
			int hg = Math.abs( y1 - y2 );
			
			imagen = new BufferedImage( wd + 1, hg + 1, BufferedImage.TYPE_INT_ARGB );			
			g = imagen.getGraphics();
		}
		
		((Graphics2D)g).setStroke( new BasicStroke( thickness ));	
		((Graphics2D)g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
		g.setColor( c );
		g.drawLine( x1, y1, x2, y2);
		
		return imagen;
	}
	
	public static Image paintPolygonLine( int[] xs, int[] ys, float thickness, Color c, Image img )
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
		
	public static Image paintOutlinePolygon( int[] xs, int[] ys, float thickness, Color c, Image img )
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
		g.drawPolygon( xs, ys, xs.length );
		
		return imagen;
	}
	
	public static Image paintFillPolygon( int[] xs, int[] ys,Color c, Image img )
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
		g.fillPolygon( xs, ys, xs.length );
		
		return imagen;
	}
	
	public static Image paintTriangle( int lado, float thicknessBorder, Color colorBorde, Color colorRelleno, int orientacion )
	{		
		int[] xs = { 0,    lado, lado / 2 };
		int[] ys = { lado, lado, 0 };
		
		switch ( orientacion ) 
		{
			case( SOUTH ):
			{
				ys[ 0 ] = 0; ys[ 1 ] = 0; ys[ 2 ] = lado;
				break;
			}	
			case( WEST ):
			{
				xs[ 0 ] = lado; xs[ 1 ] = lado; xs[ 2 ] = 0;
				ys[ 0 ] = 0;    ys[ 1 ] = lado; ys[ 2 ] = lado / 2;
				break;
			}
			case( EAST ):
			{
				xs[ 0 ] = 0; xs[ 1 ] = 0;    xs[ 2 ] = lado;
				ys[ 0 ] = 0; ys[ 1 ] = lado; ys[ 2 ] = lado / 2;
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
			img = paintFillPolygon( xs, ys, colorRelleno , img );
		}
		
		Color cBorde = Color.BLACK;
		
		if( colorBorde != null )
		{
			cBorde = colorBorde;
		}
		
		 return paintOutlinePolygon( xs, ys, thicknessBorder, cBorde, img );		
	}
	
	public static Image paintFillCircle( int x, int y, int radio, Color c, Image img )
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
	
	public static Image paintOutlineCircle( int x, int y, int radio, float thicknessBorder, Color c, Image img )
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
	
	public static Image paintEllipse( int x, int y, int width, int height, float thicknessBorder, Color colorBorder, Color colorRelleno, Image img )
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
		
			imagen = new BufferedImage( width + p, height + p, BufferedImage.TYPE_INT_ARGB );			
			g = imagen.getGraphics();
		}
		
		if( colorRelleno != null )
		{		
			g.setColor( colorRelleno );
			g.fillOval( x, y, width, height );
		}
		
		Color cBorder = Color.BLACK;
		if( colorBorder != null )
		{
			cBorder = colorBorder;
		}
		
		((Graphics2D)g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
		g.setColor( cBorder );
		((Graphics2D)g).setStroke( new BasicStroke( thicknessBorder ) );
		g.drawOval( x + p, y + p, width - p -1, height - p -1 );
				
		return imagen;
	}
	
	public static Image paintArc( int x, int y, int width, int height, int startAngle, int arcAngle, float thicknessBorder, Color colorBorde, Color ColorRelleno, Image img )
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
	
	public static Image paintRectangle( int ancho, int alto, float thicknessBorder, Color colorBorde, Color colorRelleno )
	{		
		int t = (int)thicknessBorder;
		int[] xs = { 0, ancho, ancho, 0 };
		int[] ys = { 0,  0, alto - t / 2,  alto - t / 2 };
		
		Image img = null;
		
		BasicStroke b = new BasicStroke( thicknessBorder );
		int p = (int)(b.getLineWidth() / 2);
		
		if( colorRelleno != null )
		{
			img = paintFillPolygon( xs, ys, colorRelleno , img );
		}
		
		Color cBorde = Color.BLACK;
		
		if( colorBorde != null )
		{
			cBorde = colorBorde;
		}
		
		 return paintOutlinePolygon( xs, ys, thicknessBorder, cBorde, img );		
	}

	public static Image paintDiamond( int lado, float thicknessBorder, Color colorBorde, Color colorRelleno )
	{
		int w = (int) Math.round( ( 1.0  * lado ) / ( Math.sqrt( 2.0 ) ) );
		
		int[] xs = { w,	0,	w,   w*2 };
		int[] ys = { 0, w,	w*2, w  };
		
		Image img = null;
		
		if( colorRelleno != null )
		{
			img = paintFillPolygon( xs, ys, colorRelleno , img );
		}
		
		Color cBorde = Color.BLACK;
		
		if( colorBorde != null )
		{
			cBorde = colorBorde;
		}
		
		return paintOutlinePolygon( xs, ys, thicknessBorder, cBorde, img );
	}

	public static Image paintText( int x, int y, String texto, FontMetrics fm, Color colorBorder, Color colorRelleno, Image img )
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
			int wd = fm.stringWidth( texto ), hg = fm.getAscent() - fm.getLeading() * 2;
			imagen = new BufferedImage( wd, hg, BufferedImage.TYPE_INT_ARGB );			
			g = (Graphics2D)imagen.getGraphics();
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
		
		return imagen;
	}	
	
	public static Image paintOutlineText( int x, int y, String texto, FontMetrics fm, Color color, Image img )
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
			int wd = fm.stringWidth( texto ), hg = fm.getHeight();
			imagen = new BufferedImage( wd + 1, hg + 1, BufferedImage.TYPE_INT_ARGB );			
			g = (Graphics2D)imagen.getGraphics();
		}
		
		((Graphics2D)g).setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON );
		g.setFont( fm.getFont() );
		g.setColor( color );
		g.draw( generateShapeFromText( x, y + fm.getAscent(), imagen.getWidth(), imagen.getHeight(), fm.getFont(), texto ) );		
		
		return imagen;
	}
	
	public static Image paintOutlineImage( int x, int y, Image img, Color colorFigure, Color colorPerfil, Color colorBackground, float thickness )
	{
		BufferedImage imagen = null;
		Graphics2D g = null;
		
		if( img != null )
		{
			CustomShape shape = new CustomShape( (BufferedImage)img );
			Area fig = shape.getArea_FastHack( colorFigure );
			
			imagen = ((BufferedImage)img).getSubimage( 0, 0, img.getWidth( null ), img.getHeight( null ) );
			g = (Graphics2D)imagen.getGraphics();						
			
			((Graphics2D)g).setRenderingHint( RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON );
			g.setClip( null );
			g.setStroke( new BasicStroke( thickness ) );
			g.setColor( colorPerfil );
			g.draw( fig );
		}
				
		return imagen;
	}
	
	public static Image paintOutlineShape( int x, int y, Shape fig,  Color colorPerfil, float thickness, Image img )
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
	
	public static Image paintOutlineShape( int x, int y, GeneralPath fig,  Color colorPerfil, float thickness, Image img )
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
	
	public static Image paintFillShape( int x, int y, Shape fig, Color color, Image img )
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
	
	public static Image paintFillShape( int x, int y, GeneralPath fig, Color color, Image img )
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
	
	public static Image createEmptyImage( int width, int height, Color c )
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

	public static Image compoundImages( Image baseImg, int x, int y, Image pasteImg )
	{		
		if( baseImg != null && pasteImg != null )
		{
			Graphics g = baseImg.getGraphics();
			g.drawImage( pasteImg, x, y, null );
		}
				
		return baseImg;
	}
	
	private static Shape generateShapeFromText( int x, int y, int width, int height, Font font, String string) 
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
}
