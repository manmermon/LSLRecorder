/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2011-2018 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec and CLIS.
 *
 *   LSLRec is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   LSLRec is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with LSLRec.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */

package GUI.Miscellany;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Image;

import javax.swing.ImageIcon;

public class GeneralAppIcon
{
	public static final int SMALL_SIZE_ICON = 16;
	public static final int REGULAR_SIZE_ICON = 32;
	public static final int BIG_SIZE_ICON = 48;
	public static final int BIG_PLUS_SIZE_ICON = 64;
	public static final int HUGE_SIZE_ICON = 128;

	public static ImageIcon WindowMax( int size, Color color )
	{
		ImageIcon ico = null;

		if( size > 0 )
		{
			int thick = size / 8;
			if( thick < 1 )
			{
				thick = 1;
			}

			Image img = basicPainter2D.createEmptyImage( size, size, null );

			basicPainter2D.paintPolygonLine( new int[ ]{ size / 2 - thick, thick, thick, size - thick/2, size - thick/2 }, 
					new int[ ]{ thick/2, thick/2, size - thick/2, size - thick/2, size / 2 + thick}, 
					thick, color, 
					img);

			basicPainter2D.paintFillPolygon( new int[]{ size / 2, size, size }, 
					new int[]{ 0, 0, size / 2 }, 
					color, img);

			ico = new ImageIcon( img );
		}

		return ico;
	}
	
	public static ImageIcon WindowMinimize( int size, Color color )
	{
		ImageIcon ico = null;

		if( size > 0 )
		{
			int thick = size / 8;
			if( thick < 1 )
			{
				thick = 1;
			}

			Image img = basicPainter2D.createEmptyImage( size, size, null );

			basicPainter2D.paintLine( 0, size - thick, size, size - thick, thick, color, img);

			Image imgTr = basicPainter2D.paintTriangle( size / 3 + thick, 1, color, color, basicPainter2D.SOUTH );
			basicPainter2D.compoundImages( img, ( size - imgTr.getWidth( null ) ) / 2 , size - imgTr.getHeight( null ) - thick, imgTr );

			basicPainter2D.paintLine( size / 2 - thick / 2 , 0, size / 2 - thick / 2, size / 2, thick, color, img );
			
			ico = new ImageIcon( img );
		}

		return ico;
	}
	
	public static ImageIcon WindowUndoMaximize( int size, Color color )
	{
		ImageIcon ico = null;

		if( size > 0 )
		{
			int thick = size / 8;
			if( thick < 1 )
			{
				thick = 1;
			}

			Image img = basicPainter2D.createEmptyImage( size, size, null );

			Image r = basicPainter2D.paintRectangle( ( 3 * size ) / 4, ( 3 * size ) / 4, thick, color, null );

			basicPainter2D.compoundImages( img, 0, size - r.getHeight( null), r );
			basicPainter2D.paintLine( size / 2 - thick / 2, size - r.getHeight( null), size / 2 - thick / 2, 0, thick, color, img );
			basicPainter2D.paintLine( size / 2 - thick / 2, thick / 2, size, thick / 2, thick, color, img );
			basicPainter2D.paintLine( size - thick / 2, 0, size - thick / 2, size / 2, thick, color, img );
			basicPainter2D.paintLine( r.getWidth( null ), size / 2, size, size / 2, thick, color, img );
			
			ico = new ImageIcon( img );
		}

		return ico;
	}

	public static ImageIcon NewFile( int size, Color color )
	{
		ImageIcon ico = null;

		if( size > 0 )
		{
			int thick = size / 16;
			if( thick < 1 )
			{
				thick = 1;
			}

			Image img = basicPainter2D.paintRectangle( size, size, thick, color, null );

			basicPainter2D.paintLine( size / 4, size / 4, size - size / 4, size / 4, thick, color, img );
			basicPainter2D.paintLine( size / 4, size / 2, size - size / 4, size / 2, thick, color, img );
			basicPainter2D.paintLine( size / 4, ( 3 * size ) / 4, size - size / 4, ( 3 * size ) / 4, thick, color, img );

			ico = new ImageIcon( img );
		}

		return ico;
	}
	
	public static ImageIcon Convert( int size, Color color )
	{
		ImageIcon ico = null;

		if( size > 0 )
		{			
			int thick = size / 16;
			if( thick < 1 )
			{
				thick = 1;
			}

			int radio = size / 2 - thick;
			
			Image img = basicPainter2D.createEmptyImage( size, size, null );
			
			Image circ = basicPainter2D.paintFillCircle( 0, 0, radio, color, null );
			Image circf = basicPainter2D.paintOutlineCircle( 0, 0, radio, thick, color, null );
			
			basicPainter2D.compoundImages( img, 0, 0, circ );
			basicPainter2D.compoundImages( img, img.getWidth( null ) - circf.getWidth( null ), img.getHeight( null ) - circf.getHeight( null ), circf );
			
			/*
			imagenPoligono2D.crearImagenArco( circ.getWidth( null ) / 2 + thick, circ.getHeight( null ) / 2 + thick
											, img.getWidth( null ) - circ.getWidth( null ) / 2 - circf.getWidth( null ) / 2
											, img.getHeight( null ) - circ.getHeight( null ) / 2 - circf.getHeight( null ) / 2
											, 0, 90, thick, color, null, img );
			
			imagenPoligono2D.crearImagenArco( circ.getWidth( null ) / 2 + thick, circ.getHeight( null ) / 2 + thick
												, img.getWidth( null ) - circ.getWidth( null ) / 2 - circf.getWidth( null ) / 2
												, img.getHeight( null ) - circ.getHeight( null ) / 2 - circf.getHeight( null ) / 2
												, 180, 90, thick, color, null, img );
			
			
			imagenPoligono2D.crearImagenLinea( img.getWidth( null ) - 2 * thick, img.getHeight( null ) / 2
												, img.getWidth( null ), img.getHeight( null ) / 2, thick, color, img );
			
			imagenPoligono2D.crearImagenLinea( img.getWidth( null ) - 2 * thick, img.getHeight( null ) / 2
												, img.getWidth( null ), img.getHeight( null ) / 2, thick, color, img );
												*/
			
			basicPainter2D.paintLine( circ.getWidth( null ) - thick, circ.getHeight( null ) - thick
												, img.getWidth( null ) - circf.getWidth( null ) + thick, img.getHeight( null ) - circf.getHeight( null ) + thick
												, thick, color, img );
			
			basicPainter2D.paintLine( img.getWidth( null ) - circf.getWidth( null ) + thick, img.getHeight( null ) - circf.getHeight( null ) + thick
												, img.getWidth( null ) - circf.getWidth( null ) + thick, img.getHeight( null ) - circf.getHeight( null ) - 2 * thick
												, thick, color, img );
			
			basicPainter2D.paintLine( img.getWidth( null ) - circf.getWidth( null ) + thick, img.getHeight( null ) - circf.getHeight( null ) + thick
												, img.getWidth( null ) - circf.getWidth( null ) - 2 * thick, img.getHeight( null ) - circf.getHeight( null ) + thick
												, thick, color, img );
			
			
			ico = new ImageIcon( img );
		}

		return ico;
	}

	public static ImageIcon LoadFile( int size, Color color )
	{
		ImageIcon ico = null;

		if( size > 0 )
		{
			int thick = size / 16;
			if( thick < 1 )
			{
				thick = 1;
			}

			int w = size / 2;

			Image img = basicPainter2D.compoundImages( 
					basicPainter2D.createEmptyImage( size, size, null ), 
					0, 0,  
					basicPainter2D.paintRectangle( w - thick, size - thick, thick, color, null ) );

			basicPainter2D.paintLine( w / 4, size / 4, w - w / 4 - thick, size / 4, thick, color, img );
			basicPainter2D.paintLine( w / 4, size / 2, w - w / 4 - thick, size / 2, thick, color, img );
			basicPainter2D.paintLine( w / 4, ( 3 * size ) / 4, w - w / 4 - thick, ( 3 * size ) / 4, thick, color, img );

			basicPainter2D.paintFillPolygon( new int[]{ w + thick, w + thick, size }, 
					new int[]{ size / 4, size - size / 4, size / 2 }, 
					color, img);

			ico = new ImageIcon( img );

		}

		return ico;
	}

	public static ImageIcon LoadFolder( int width, int height, Color borderColor, Color fillColor )
	{
		int w = width - 2;
		int h = height - 2;

		if( borderColor == null && fillColor == null )
		{
			borderColor = Color.BLACK;
		}

		Image img = basicPainter2D.createEmptyImage( w, h, null );

		Image imgAux = Folder( 2 * w / 3, h, borderColor, fillColor ).getImage();
		Image imgAux2 = basicPainter2D.paintTriangle( w / 3, 1, borderColor, borderColor, basicPainter2D.EAST );

		basicPainter2D.compoundImages( img, 0, 0, imgAux );
		return new ImageIcon( basicPainter2D.compoundImages( img, 2 * w / 3, h / 2 - w / 6, imgAux2 ) );
	}

	public static ImageIcon SaveFile( int size, Color color )
	{
		ImageIcon ico = null;

		if( size > 0 )
		{
			int thick = size / 16;
			if( thick < 1 )
			{
				thick = 1;
			}

			int w = size / 2;

			Image img = basicPainter2D.compoundImages( 
					basicPainter2D.createEmptyImage( size, size, null ), 
					0, 0,  
					basicPainter2D.paintRectangle( w - thick, size - thick, thick, color, null ) );

			basicPainter2D.paintLine( w / 4, size / 4, w - w / 4 - thick, size / 4, thick, color, img );
			basicPainter2D.paintLine( w / 4, size / 2, w - w / 4 - thick, size / 2, thick, color, img );
			basicPainter2D.paintLine( w / 4, ( 3 * size ) / 4, w - w / 4 - thick, ( 3 * size ) / 4, thick, color, img );

			basicPainter2D.paintFillPolygon( new int[]{ size, size, w + thick }, 
					new int[]{ size / 4, size - size / 4, size / 2 }, 
					color, img);

			ico = new ImageIcon( img );

		}

		return ico;
	}

	public static ImageIcon Error( int size, Color color )
	{
		ImageIcon ico = null;

		if( size > 0 )
		{
			int thick = size / 16;
			if( thick < 1 )
			{
				thick = 1;
			}

			Image img = basicPainter2D.createEmptyImage( size, size, null );

			basicPainter2D.paintLine( 0, 0, size, size, thick, color, img );
			basicPainter2D.paintLine( 0, size, size, 0, thick, color, img );

			basicPainter2D.compoundImages( img, size / 4, size / 4, 
					basicPainter2D.paintRectangle( size / 2, size / 2, thick, color, null ) );

			ico = new ImageIcon( img );
		}

		return ico;
	}

	public static ImageIcon Question( int size, Color color )
	{
		ImageIcon ico = null;

		if( size > 0 )
		{
			int thick = size / 16;
			if( thick < 1 )
			{
				thick = 1;
			}

			//Image img = imagenPoligono2D.crearLienzoVacio( size, size, null );

			Image img = basicPainter2D.paintRectangle( size-thick/2, size-thick/2, thick, color, null );

			Font f = new Font( Font.DIALOG, Font.BOLD, 12 );
			FontMetrics fm = img.getGraphics().getFontMetrics( f );			
			while( fm.getHeight() < size )
			{
				f = new Font( f.getName(), Font.BOLD, f.getSize() + 1 );
				fm = img.getGraphics().getFontMetrics( f );
			}

			Image imgTxt = basicPainter2D.paintText( 0, 0, "?", fm, color, color, null );
			int x = (int)Math.round( ( size - imgTxt.getWidth( null ) ) / 2.0 );
			int y = 0;
			img = basicPainter2D.compoundImages( img, x, y, imgTxt );

			ico = new ImageIcon( img );
		}

		return ico;
	}

	public static ImageIcon Warning( int size, Color color )
	{
		ImageIcon ico = null;

		if( size > 0 )
		{
			int thick = size / 8;
			if( thick < 1 )
			{
				thick = 1;
			}

			Image img = basicPainter2D.createEmptyImage( size, size, null );

			basicPainter2D.paintOutlinePolygon( new int[]{ thick - 1, size / 2, size - thick + 1},
					new int[]{ size - thick/2, thick, size -thick/2},
					thick, color, img );

			Font f = new Font( Font.DIALOG, Font.PLAIN, 12 );
			FontMetrics fm = img.getGraphics().getFontMetrics( f );			
			while( fm.getHeight() < ( size * 3 ) / 4 )
			{
				f = new Font( f.getName(), Font.PLAIN, f.getSize() + 1 );
				fm = img.getGraphics().getFontMetrics( f );
			}

			while( fm.getHeight() > ( size * 3 ) / 4 )
			{
				f = new Font( f.getName(), Font.PLAIN, f.getSize() - 1 );
				fm = img.getGraphics().getFontMetrics( f );
			}

			Image imgTxt = basicPainter2D.paintText( 0, 0, "!", fm, color, color, null );
			int x = (int)Math.round( ( size - imgTxt.getWidth( null ) ) / 2.0 );
			int y = size - imgTxt.getHeight( null ) + 1;
			img = basicPainter2D.compoundImages( img, x, y, imgTxt );

			ico = new ImageIcon( img );
		}

		return ico;
	}
	
	public static ImageIcon Info( int size, Color color )
	{
		ImageIcon ico = null;

		if( size > 0 )
		{
			int thick = size / 8;
			if( thick < 1 )
			{
				thick = 1;
			}

			Image img = basicPainter2D.createEmptyImage( size, size, null );
			
			Image c = basicPainter2D.paintOutlineCircle( 0, 0, size, thick, color, null );
						
			basicPainter2D.compoundImages( img, (img.getWidth( null ) - c.getWidth( null ) ) / 2
												, (img.getHeight( null ) - c.getHeight( null ) ) / 2
												, c );
			
			int fontStyle = Font.BOLD;
			
			Font f = new Font( Font.DIALOG, fontStyle, 12 );
			FontMetrics fm = img.getGraphics().getFontMetrics( f );
			
			int lim = c.getHeight( null ) - (thick * 3 ) /  4 ;
			
			while( fm.getHeight() < lim )
			{
				f = new Font( f.getName(), fontStyle, f.getSize() + 1 );				
				fm = img.getGraphics().getFontMetrics( f );
			}

			while( fm.getHeight() > lim  )
			{				
				f = new Font( f.getName(), fontStyle, f.getSize() - 1 );
				fm = img.getGraphics().getFontMetrics( f );
			}
			
			/*
			if( f.getSize() < 35 )
			{						
				fm = img.getGraphics().getFontMetrics( new Font( f.getName(), Font.PLAIN, f.getSize() ) );
			}
			*/
						
			Image imgI = basicPainter2D.paintText( 0, 0, "i", fm, null, color, null );
			basicPainter2D.compoundImages( img
												, ( img.getWidth( null ) - imgI.getWidth( null ) )/2
												, ( img.getHeight( null ) - imgI.getHeight( null ) )/2 + thick / 2
												, imgI );

			ico = new ImageIcon( img );
		}

		return ico;
	}

	public static ImageIcon ImageIcon( int size, Color color )
	{
		ImageIcon ico = null;

		if( size > 0 )
		{	
			int thick = size / 16;
			if( thick < 1 )
			{
				thick = 1;
			}

			Image img = basicPainter2D.paintRectangle( size, size, 1, color, null);

			int[] x = new int[ size ];
			int[] y = new int[ size ];
			int h = (int)( size * 0.75 );
			for( int i = 0; i < size; i++ )
			{
				x[ i ] = i;
				y[ i ] = (int)(( ( Math.sin( ( i * 4 * Math.PI ) / size ) + 1 ) / 2 ) * h ) + ( size - h ) / 2;
			}

			basicPainter2D.paintPolygonLine( x, y, thick, color, img );

			ico = new ImageIcon( img );
		}

		return ico;
	}

	public static ImageIcon Exit( int size, Color color )
	{
		ImageIcon ico = null;

		if( size > 0 )
		{	
			int w = size - 2;
			int l =  w / 3 ;

			int thickness = l / 3;

			thickness /= 2;
			
			if( thickness < 1 )
			{
				thickness = 1;
			}			
			
			Image img = basicPainter2D.createEmptyImage( w, w, null );
			
			int pI = (2 * w) / 3;
			
			basicPainter2D.paintFillPolygon( new int[] { pI, w, pI }
														, new int[] { 0, w / 2, w}
														, color, img);
			
			Image r = basicPainter2D.paintRectangle( pI - 2 * thickness 
															, ( 2 * w ) / 5
															, thickness
															, color
															, color );
			
			basicPainter2D.compoundImages( img, pI - r.getWidth( null ) + thickness * 2, w /2 - r.getHeight( null ) / 2, r );
			
			int thickness2 = thickness / 2;
			
			if( thickness2 < 1 )
			{
				thickness2 = 1;
			}
			
			basicPainter2D.paintPolygonLine( new int[] { pI - 2 * thickness, thickness2, thickness2, pI - 2 * thickness}
														, new int[] { thickness2, thickness2, w - thickness2, w - thickness2 }
														, thickness
														, color, img );
			
			
			ico = new ImageIcon( img );
		}

		return ico;
	}
	
	public static ImageIcon Close( int size, Color color )
	{
		ImageIcon ico = null;

		if( size > 0 )
		{	
			int thick = size / 3;
			if( thick < 1 )
			{
				thick = 1;
			}

			Image img = basicPainter2D.createEmptyImage( size, size, null  );

			basicPainter2D.paintLine( 0, 0, size, size, thick, color, img );
			basicPainter2D.paintLine( 0, size, size, 0, thick, color, img );

			ico = new ImageIcon( img );
		}

		return ico;
	}

	public static ImageIcon Pencil( int size, Color color )
	{
		ImageIcon ico = null;

		if( size > 0 )
		{	
			int thick = size / 16;

			if( thick < 1 )
			{
				thick = 1;
			}

			int shiftThick = thick / 2;
			if( shiftThick == 0 )
			{
				shiftThick = 1;
			}

			Image img = basicPainter2D.createEmptyImage( size, size, null  );

			double div = size / 10.0;
			int[] xs = new int[]{ size / 2 , size - shiftThick, size / 2, shiftThick  };			
			int[] ys = new int[]{ shiftThick, size / 2, size - shiftThick, size / 2  };

			basicPainter2D.paintOutlinePolygon(xs, ys, thick, color, img );

			basicPainter2D.paintLine( (int)( div * 1.5 ) + shiftThick, (int)( div * 6.5 ) - shiftThick, (int)( div * 6.5 ) - shiftThick, (int)( div * 1.5 ) + shiftThick, thick, color, img);
			basicPainter2D.paintLine( (int)( div * 3.5 ) + shiftThick, (int)( div * 8.5 ) - shiftThick, (int)( div * 8.5) - shiftThick, (int)( div * 3.5 ) + shiftThick, thick, color, img);

			basicPainter2D.paintLine( shiftThick, size / 2 + shiftThick, shiftThick, size, thick, color, img);
			basicPainter2D.paintLine( 0, size - shiftThick, size / 2 - shiftThick, size - shiftThick, thick, color, img);

			xs = new int[]{ shiftThick, shiftThick, (int)( 2.5 * div ) };
			ys = new int[]{ (int)( 7.5 * div ), size - shiftThick, size - shiftThick };

			basicPainter2D.paintFillPolygon(xs, ys, color, img );

			ico = new ImageIcon( img );			
		}

		return ico;
	}

	public static ImageIcon Config(  )
	{
		Image icono = null, aux = null;		
		Color colorRelleno = Color.LIGHT_GRAY;
		Color colorBorde = Color.BLACK;
		int width = 100,  height = 100;
		icono = basicPainter2D.createEmptyImage( width, height, null );

		int xs[] = { 0, 0, width / 6, width / 6, 2 * width / 6 , 2 * width / 6, width / 2, width / 2 };
		int ys[] = { height / 4, 0, 0, height / 4, height / 4, 0, 0, height / 4 };

		basicPainter2D.paintFillPolygon( xs, ys, colorRelleno, icono );
		basicPainter2D.paintOutlinePolygon( xs, ys, 1.0F, colorBorde, icono );		

		aux = basicPainter2D.paintRectangle( width / 6 , height / 2 -1, 1.0F, colorBorde, colorRelleno );		
		basicPainter2D.compoundImages( icono, width / 6 , height / 2 , aux );

		basicPainter2D.paintArc( 0, 0, width / 2, height / 2, 
				180, 180, 1.0F, colorBorde, colorRelleno, icono );
		basicPainter2D.paintLine( width / 6, height / 4, 2 * width / 6, height / 4 , 1.0F, colorBorde, icono );
		xs = new int[ 8 ];
		ys = new int[ 8 ];		
		xs[ 0 ] = width / 2 + width / 6; xs[ 1 ] = xs[ 0 ]; xs[ 2 ] = xs[ 1 ] - width / 12; xs[ 3 ] = xs[ 0 ]; xs[ 4 ] = xs[ 3 ] + width / 6 ; xs[ 5 ] = xs[ 4 ] + width / 12; xs[ 6 ] = xs[ 4 ]; xs[ 7 ] = xs[ 6 ];
		ys[ 0 ] = height / 2; ys[ 1 ] = ys[ 0 ] - height / 6; ys[ 2 ] = ys[ 1 ] - height / 4; ys[ 3 ] = 0; ys[ 4 ] = ys[ 3 ]; ys[ 5 ] = ys[ 2 ]; ys[ 6 ] = ys[ 1 ]; ys[ 7 ] = ys[ 0 ];

		basicPainter2D.paintFillPolygon( xs, ys, colorRelleno, icono );
		basicPainter2D.paintOutlinePolygon( xs, ys, 1.0F, colorBorde, icono );


		aux = basicPainter2D.paintRectangle( width / 6 + 4, height / 2, 1.0F, colorBorde, Color.RED);
		basicPainter2D.compoundImages( icono, width / 2 + width / 6 - 2, height / 2, aux );

		return new ImageIcon( icono );
	}
	
	public static Image Config2( Color color )
	{	
		if( color == null )
		{
			color = Color.BLACK;
		}
		
		int size = 512;
				
		Image base = basicPainter2D.createEmptyImage( size, size, null );
		
		int r = size / 3;
		
		int thick = r / 8;
		
		if( thick < 1 )
		{
			thick = 1;
		}
		
		int numLines = 2;		
		
		//Image circ = imagenPoligono2D.crearImagenCirculo( 0, 0, r, color, null );
		Image circ = basicPainter2D.paintDot( 0, 0, r, color, true, null );
		
		Color fill = new Color( ~color.getRGB() );
		//Image circ2 = imagenPoligono2D.crearImagenCirculo( 0, 0, r / 2, fill, null );
		Image circ2 = basicPainter2D.paintDot( 0, 0, ( 2 * r ) / 3, fill, true, null );
		basicPainter2D.compoundImages( circ, (int)Math.round( ( circ.getWidth( null ) - circ2.getWidth( null ) ) / 2.0 )
												, (int)Math.round( ( circ.getHeight( null ) - circ2.getHeight( null ) ) / 2.0 )												
												, circ2 );
		
		double h = size / ( 2.0 * numLines );
		int x = r / 2;
		int stepX = ( size - x ) / ( numLines + 1);
		double cc = circ.getHeight( null ) / 2.0;
		
		for( int i = 1; i <= numLines; i++ )
		{
			basicPainter2D.paintLine( 0, (int)( h ), size, (int)( h ) , thick, color, base );
			basicPainter2D.compoundImages( base, x, (int)Math.round( h - cc ), circ );
			
			h += size / numLines;
			x += stepX;
		}
		
		return base;
	}

	public static ImageIcon getIconoAplicacion( int width, int height  )
	{
		/*
		Image icono = null;
		Color colorBorde = Color.BLACK;
		//int width = 100, height = 100;
		float thicknessBorder = 1.5F;

		icono = imagenPoligono2D.crearLienzoVacio( width, height, null );

		int radio = Math.max( width, height ) / 2;
		double p = 1 - Math.cos( Math.PI / 4 );

		int pW = (int)( ( width - radio + radio * p ) / 2 + 2 );
		int pH = (int)( ( height - radio + radio * p ) / 2 + 2 );
				
		imagenPoligono2D.crearImagenLinea( radio / 4, radio / 4, pW - width / 8, pH - width / 8, radio / 5, Color.BLUE, icono );
		
		imagenPoligono2D.crearImagenPoligonoRelleno( new int[] { pW, pW, pW - width / 4 }
													, new int[] { pH, pH - height / 4, pH }
													, Color.BLUE, icono );
		
		pW -= 1;
		pH -= 1;
		imagenPoligono2D.crearImagenLinea( width - radio / 4, radio / 4
											, width - ( pW - width / 8)
											, pH - width / 8 , radio / 5, Color.GREEN.darker(), icono );
		
		imagenPoligono2D.crearImagenPoligonoRelleno( new int[] { width - pW, width - pW, width - ( pW - width / 4 ) }
													, new int[] { pH, pH - height / 4, pH }
													, Color.GREEN.darker(), icono );
		
		imagenPoligono2D.crearImagenLinea( width /2, height / 2, width / 2, height - ( height - radio ) / 4, radio / 5, Color.BLACK, icono );

		imagenPoligono2D.crearImagenPoligonoRelleno( new int[] { ( 3 * width ) / 8, ( 5 * width ) / 8, width / 2 }
													, new int[] { height - ( height - radio ) / 4, height - ( height - radio ) / 4, height }
													, Color.BLACK, icono );

		
		
		
		imagenPoligono2D.crearImagenCirculo( ( width - radio ) / 2, ( height - radio ) / 2, radio, Color.RED, icono );
		imagenPoligono2D.crearImagenCircunferencia( ( width - radio ) / 2, ( height - radio ) / 2, radio, thicknessBorder, Color.BLACK, icono );
		

		*/
		
		int iconSize = Math.min( width, height );
		
		int thick = 1; 
		int thick2 = iconSize / 64;
		if( thick2 < 1 )
		{
			thick2 = 1;
		}
		
		int arrowWidth = (int)( (iconSize / 8) * 1.25); 
	
		Image icon = basicPainter2D.createEmptyImage( iconSize, iconSize, Color.WHITE);
		
		Image icon2 = basicPainter2D.createEmptyImage( iconSize, iconSize, null );
		icon2 = basicPainter2D.paintLine( iconSize / 2 - thick2,  arrowWidth / 2, iconSize / 2 - thick2, iconSize - arrowWidth / 2, 2 * thick2 , Color.BLACK, icon2 );
		icon2 = basicPainter2D.paintLine( arrowWidth / 2, iconSize / 2 - thick2, iconSize - arrowWidth / 2, iconSize / 2 - thick2, 2 * thick2, Color.BLACK, icon2 );
				
		icon = basicPainter2D.paintArc( thick / 2, thick / 2, 
												iconSize - thick,
												iconSize - thick, 
												180, -90, 
												thick, Color.BLACK, Color.BLACK, icon );
		
		icon = basicPainter2D.paintArc( thick / 2, thick / 2, 
												iconSize - thick,
												iconSize - thick, 
												90, -90, 
												thick, Color.GREEN, Color.GREEN, icon );
		
		icon = basicPainter2D.paintArc( thick / 2, thick / 2, 
												iconSize - thick,
												iconSize - thick, 
												0, -90, 
												thick, Color.CYAN.darker(), Color.CYAN.darker(), icon );
		
		icon = basicPainter2D.paintArc( thick / 2, thick / 2, 
												iconSize - thick,
												iconSize - thick, 
												-90, -90, 
												thick, Color.YELLOW, Color.YELLOW, icon );
		
		Image aux = basicPainter2D.paintFillPolygon( new int[]{ 0, 0, arrowWidth / 2 },
																 new int[]{ 0, arrowWidth, arrowWidth / 2 }, 
																 Color.BLACK, 
																 null);
		
		icon = basicPainter2D.compoundImages( icon,  iconSize / 2 - 1, 0, aux);
		
		aux = basicPainter2D.paintFillPolygon( new int[]{ 0, arrowWidth, arrowWidth / 2 },
															 new int[]{  0, 0, arrowWidth / 2 }, 
															 Color.GREEN, 
															 null);

		icon = basicPainter2D.compoundImages( icon,  iconSize - arrowWidth, iconSize / 2 - 1, aux);		

		aux = basicPainter2D.paintFillPolygon( new int[]{ arrowWidth/2, arrowWidth/2, 0 },
															 new int[]{  0, arrowWidth, arrowWidth / 2 }, 
															 Color.CYAN.darker(), 
															 null);

		icon = basicPainter2D.compoundImages( icon,  iconSize / 2 - arrowWidth / 2, iconSize - arrowWidth, aux);		

		aux = basicPainter2D.paintFillPolygon( new int[]{ 0, arrowWidth, arrowWidth / 2 },
															 new int[]{  arrowWidth / 2, arrowWidth / 2, 0 }, 
															 Color.YELLOW, 
															 null);

		
		icon = basicPainter2D.compoundImages( icon,  0, iconSize / 2 - arrowWidth / 2, aux);		
		
		//icon = imagenPoligono2D.crearImagenCircunferencia(0, 0, iconSize-1, thick2, Color.BLACK, icon );
		
		icon = basicPainter2D.paintFillCircle( arrowWidth, arrowWidth, iconSize - 2 * arrowWidth, Color.WHITE, icon );
		//icon = imagenPoligono2D.crearImagenCircunferencia( arrowWidth, arrowWidth, iconSize - 2 * arrowWidth, thick2, Color.BLACK, icon );
		
		icon = basicPainter2D.changeColorPixels( Color.WHITE, new Color( 255, 255, 255, 0), icon );
		
		icon = basicPainter2D.compoundImages( icon2, 0, 0, icon );
		
		icon = basicPainter2D.paintFillCircle( iconSize / 2 - iconSize / 8 - thick2, 
													iconSize / 2 - iconSize / 8, 
													iconSize / 4, Color.RED, icon );
		
		icon = basicPainter2D.paintOutlineCircle( iconSize / 2 - iconSize / 8 - thick2 * 2 + 1,  
															iconSize / 2 - iconSize / 8 - thick2 / 2, 
															iconSize / 4 + 2*thick2, 
															2*thick2, Color.BLACK, icon );

		return new ImageIcon( icon );
	}

	public static ImageIcon Incorrect(  )
	{
		Image icono = null;

		Color colorBorder = Color.BLACK;
		Color colorRelleno = Color.RED;
		int width = 100, height = 100;
		float thickness = width / 4.0F;

		icono = basicPainter2D.createEmptyImage( width, height, null );

		basicPainter2D.paintLine( 0+(int)thickness, 0+(int)thickness, width-(int)thickness, height-(int)thickness, thickness+2.0F, colorBorder, icono );
		basicPainter2D.paintLine( width-(int)thickness, 0+(int)thickness, 0+(int)thickness, height-(int)thickness, thickness+2.0F, colorBorder, icono );
		basicPainter2D.paintLine( 0+(int)thickness, 0+(int)thickness, width-(int)thickness, height-(int)thickness, thickness, colorRelleno, icono );
		basicPainter2D.paintLine( width-(int)thickness, 0+(int)thickness, 0+(int)thickness, height-(int)thickness, thickness, colorRelleno, icono );

		return new ImageIcon( icono );
	}

	public static ImageIcon Correct( Color borderColor, Color fillColor )
	{
		Image icono = null;		
		int width = 100, height = 100;
		Color colorBorder = borderColor;
		Color colorRelleno = fillColor;
		float thickness = 1.0F;

		icono = basicPainter2D.createEmptyImage( width, height, null );

		int x = (int)thickness, y = height / 2 + (int)thickness;

		int xs[ ] = { x, x + width / 4, width - (int)thickness, x + width / 4 };
		int ys[ ] = { y, y + height / 4 - (int)thickness, 0 + (int)thickness,  height - (int)thickness };  

		basicPainter2D.paintFillPolygon( xs, ys, colorRelleno, icono );
		basicPainter2D.paintOutlinePolygon( xs, ys, thickness, colorBorder, icono );

		return new ImageIcon( icono );
	}

	public static ImageIcon Clock(  )
	{
		Image icono = null;		
		Image aux = null;
		int width = 100;
		int height = 100;

		Color colorBorder = Color.RED;
		float thickness = 2.5F;

		int x = (int)thickness, y = Math.abs( width - height ) / 2;
		int diametro = width - (int)Math.round( thickness );
		if( width > height )
		{
			diametro = height- (int)Math.round( thickness ); 
			x = y;
			y = (int)thickness;
		}

		icono = basicPainter2D.createEmptyImage( width, height, null );

		aux = basicPainter2D.paintRectangle(  5 * width / 8, height - (int)(thickness * 2 ), thickness, colorBorder, colorBorder );
		basicPainter2D.compoundImages( icono, ( width - aux.getWidth( null ) ) / 2 , ( height - aux.getHeight( null ) ) / 2 , aux); 

		basicPainter2D.paintFillCircle( x, y, diametro, Color.WHITE, icono );
		basicPainter2D.paintOutlineCircle( x, y, diametro, thickness, colorBorder, icono );

		aux = basicPainter2D.paintRectangle( diametro / 12, diametro / 12, thickness, Color.BLACK, Color.BLACK );		
		basicPainter2D.compoundImages( icono, width / 2 - aux.getWidth( null ) / 2, y + (int)(thickness * 2 ), aux );
		basicPainter2D.compoundImages( icono, width / 2 - aux.getWidth( null ) / 2, y + diametro - (int)(thickness * 2 ) - aux.getHeight( null ), aux );
		basicPainter2D.compoundImages( icono, 0 + (int)(thickness * 2 ), height / 2 - aux.getHeight( null ) / 2, aux );
		basicPainter2D.compoundImages( icono, width - (int)(thickness * 2 ) - aux.getWidth( null ), height / 2 - aux.getHeight( null ) / 2, aux );

		basicPainter2D.paintLine( width / 2, height / 2, width / 2 , height / 2 -  3 * ( diametro / 2 ) / 4, thickness*1.5F, colorBorder, icono );
		basicPainter2D.paintLine( width / 2, height / 2, width / 2 + diametro / 4, height / 2, thickness*1.5F, colorBorder, icono );		

		return new ImageIcon( icono );
	}

	public static ImageIcon Socket( Color colorIn, Color colorOut )
	{
		Image imgIn = basicPainter2D.paintTriangle( 14,  1, 
				Color.BLACK,
				colorIn,
				basicPainter2D.SOUTH );

		Image imgOut = basicPainter2D.paintTriangle( 14,  1, 
				Color.BLACK,
				colorOut,
				basicPainter2D.NORTH );

		Image img = basicPainter2D.createEmptyImage( 30,15, null );
		img = basicPainter2D.compoundImages( img, 0, 0, imgIn );

		return new ImageIcon( basicPainter2D.compoundImages( img, 12, 0, imgOut ) );
	}

	public static ImageIcon Folder( int width, int height, Color borderColor, Color fillColor )
	{
		int w = width - 2;
		int h = height - 2;
		int thickness = 1;

		if( borderColor == null && fillColor == null )
		{
			borderColor = Color.BLACK;
		}

		Image img = basicPainter2D.createEmptyImage( w, h, null );

		//Image imgAux = imagenPoligono2D.crearImagenRectangulo( w / 2, h / 5, thickness, borderColor, fillColor );
		int x[] = { thickness, 3 * w / 7, 3 * w / 7, w - thickness, w - thickness, thickness };
		int y[] = { thickness, thickness, h / 5, h / 5, 2 * h / 5, 2 * h / 5 };

		if( fillColor != null )
		{
			basicPainter2D.paintFillPolygon( x, y, fillColor, img );
		}

		if( borderColor != null )
		{
			basicPainter2D.paintOutlinePolygon( x, y, thickness, borderColor, img );
		}
		//imagenPoligono2D.componerImagen( img, 0, 0, imgAux );

		int xs[] = { thickness, w - thickness, w -thickness, thickness };
		int ys[] = { 2 * h / 5, 2 * h / 5, h - thickness, h - thickness };

		if( fillColor != null )
		{
			basicPainter2D.paintFillPolygon( xs, ys, fillColor, img );
		}

		if( borderColor != null )
		{
			basicPainter2D.paintOutlinePolygon( xs, ys, thickness, borderColor, img );
		}

		//Image imgAux = imagenPoligono2D.crearImagenRectangulo( w - thickness, 3 * h / 5, thickness, borderColor, fillColor );
		//imagenPoligono2D.componerImagen( img, 0, 2 * h / 5, imgAux );

		return new ImageIcon( img );
	}

	public static ImageIcon Refresh( int width, int height, Color borderColor, Color fillColor )
	{
		int w = width - 2;
		int h = height - 2;
		int l = (int)( Math.min( w, h ) / 3 );

		int thickness = l / 3;

		if( thickness < 2 )
		{
			thickness = 2;
		}


		Image img = basicPainter2D.createEmptyImage( w, h, null );

		if( borderColor == null )
		{
			borderColor = Color.BLACK;
		}

		Image imgAux = basicPainter2D.paintTriangle( l, 1, borderColor, borderColor, basicPainter2D.EAST );

		basicPainter2D.compoundImages( img, w / 2, 0, imgAux );

		basicPainter2D.paintArc( thickness / 2, thickness / 2, w - (thickness * 3 ) / 2 , h - 2 * l / 3 - thickness, 0, -270, thickness, borderColor, fillColor, img );

		return new ImageIcon( img );		
	}

	public static ImageIcon Plot( int size, Color color )
	{
		ImageIcon ico = null;

		if( size > 0 )
		{	
			int thick = size / 16;
			if( thick < 1 )
			{
				thick = 1;
			}

			Image img = basicPainter2D.paintRectangle( size, size, 1, color, null);

			int[] x = new int[ size ];
			int[] y = new int[ size ];
			int h = (int)( size * 0.75 );
			for( int i = 0; i < size; i++ )
			{
				x[ i ] = i;
				y[ i ] = (int)(( ( Math.sin( ( i * 4 * Math.PI ) / size ) + 1 ) / 2 ) * h ) + ( size - h ) / 2;
			}

			basicPainter2D.paintPolygonLine( x, y, thick, color, img );

			ico = new ImageIcon( img );
		}

		return ico;		
	}
	
	/*
	public static ImageIcon getSAMValence( int level, int side, Color borderColor, Color fillColor )
	{
		int thicknessBorder = 2;

		Image img = imagenPoligono2D.crearImagenRectangulo( side, side, thicknessBorder, borderColor, fillColor );

		Image eye = imagenPoligono2D.crearImagenCirculo( 0, 0, side/6, borderColor, null );

		Image mouth = getSmile( level, side, thicknessBorder, borderColor, fillColor );

		int w = mouth.getWidth( null );
		int hg = mouth.getHeight( null );

		img = imagenPoligono2D.componerImagen( img, side/6, side/6 , eye );
		img = imagenPoligono2D.componerImagen( img, side - side/3, side/6 , eye );
		//img = imagenPoligono2D.componerImagen( img, ( 5 * side ) / 12, ( 5 * side) / 12, nose );

		img = imagenPoligono2D.componerImagen( img, ( side - w ) / 2 , side - hg*2, mouth );

		return new ImageIcon( img );
		//return new ImageIcon( mouth );
	}
	*/
	/*
	private static Image getSmile( int level, int imgWidth, int thickness, Color borderColor, Color fillColor )
	{
		double x1 = 1;
		double x2 = ( 8 * imgWidth ) / 24.0 + x1, x3 = ( 8 * imgWidth ) / 12.0 + x1;
		double y1 = imgWidth / 6.0;
		double y2 = y1, y3 = y1;

		double h = ( imgWidth / 3.0 ) / 8.0; 

		int val = level - 5;

		y1 += -val * h;
		y3 = y1;
		y2 += val * h;

		if( y1 < 0 )
		{
			y1 = 0;
			y3 = y1;
			y2 = h * 8;
		}
		else if( y1 > h * 8 )
		{
			y2 = 0;
			y1 = h * 8;
			y3 = y1;
		}

		Bspline bspline = new Bspline();

		bspline.addPoint( -1, y1 );
		bspline.addPoint( 0, y1 );
		bspline.addPoint( x1, y1 );
		bspline.addPoint( x2, y2 );
		bspline.addPoint( x3, y3 );
		bspline.addPoint( x3, y3 );
		bspline.addPoint( x2, y2 );
		bspline.addPoint( x1, y1 );
		bspline.addPoint( 0, y1 );
		bspline.addPoint( -1, y1 );

		List< Coordinate > coords = bspline.getInterpolated();

		int n = coords.size();
		int[] xs = new int[ n ];
		int[] ys = new int[ xs.length ];

		int i = 0;
		int w = 0;
		int hg = 0;
		for( Coordinate c : coords )
		{
			xs[ i ] = (int)c.x;
			ys[ i ] = (int)c.y;

			if( w < xs[ i ] )
			{
				w = xs[ i ];
			}

			if( hg < ys[ i ] )
			{
				hg = ys[ i ];
			}

			i += 1;			
		}

		Image mouth = imagenPoligono2D.crearLienzoVacio( w + 1, hg + 1, fillColor );

		for( i = 1; i < xs.length; i++ )
		{
			imagenPoligono2D.crearImagenLinea( xs[i-1], ys[i-1]
					, xs[i], ys[i]
							, thickness
							, borderColor, mouth );
		}

		return mouth;
	}
	*/
	/*
	public static ImageIcon getSAMValence( float level, int side, Color borderColor, Color fillColor )
	{
		int thicknessBorder = 2;

		Image img = imagenPoligono2D.crearImagenRectangulo( side, side, thicknessBorder, borderColor, fillColor );

		Image eye = imagenPoligono2D.crearImagenCirculo( 0, 0, side/6, borderColor, null );

		Image mouth = getSmile( level, side, thicknessBorder, borderColor, fillColor );

		int w = mouth.getWidth( null );
		int hg = mouth.getHeight( null );

		img = imagenPoligono2D.componerImagen( img, side/6, side/6 , eye );
		img = imagenPoligono2D.componerImagen( img, side - side/3, side/6 , eye );
		//img = imagenPoligono2D.componerImagen( img, ( 5 * side ) / 12, ( 5 * side) / 12, nose );

		img = imagenPoligono2D.componerImagen( img, ( side - w ) / 2 , side - hg*2, mouth );

		return new ImageIcon( img );
		//return new ImageIcon( mouth );
	}
	*/
	/*
	private static Image getSmile( float level, int imgWidth, int thickness, Color borderColor, Color fillColor )
	{
		double x1 = 1;
		double x2 = ( 8 * imgWidth ) / 24.0 + x1, x3 = ( 8 * imgWidth ) / 12.0 + x1;
		double y1 = imgWidth / 6.0;
		double y2 = y1, y3 = y1;

		double h = ( imgWidth / 3.0 ) / 8.0; 

		if( level < 0 || level > 1 )
		{
			level = 1F;
		}
		
		int val = Math.round( level * 9 ) - 5;

		y1 += -val * h;
		y3 = y1;
		y2 += val * h;

		if( y1 < 0 )
		{
			y1 = 0;
			y3 = y1;
			y2 = h * 8;
		}
		else if( y1 > h * 8 )
		{
			y2 = 0;
			y1 = h * 8;
			y3 = y1;
		}

		Bspline bspline = new Bspline();

		bspline.addPoint( -1, y1 );
		bspline.addPoint( 0, y1 );
		bspline.addPoint( x1, y1 );
		bspline.addPoint( x2, y2 );
		bspline.addPoint( x3, y3 );
		bspline.addPoint( x3, y3 );
		bspline.addPoint( x2, y2 );
		bspline.addPoint( x1, y1 );
		bspline.addPoint( 0, y1 );
		bspline.addPoint( -1, y1 );

		List< Coordinate > coords = bspline.getInterpolated();

		int n = coords.size();
		int[] xs = new int[ n ];
		int[] ys = new int[ xs.length ];

		int i = 0;
		int w = 0;
		int hg = 0;
		for( Coordinate c : coords )
		{
			xs[ i ] = (int)c.x;
			ys[ i ] = (int)c.y;

			if( w < xs[ i ] )
			{
				w = xs[ i ];
			}

			if( hg < ys[ i ] )
			{
				hg = ys[ i ];
			}

			i += 1;			
		}

		Image mouth = imagenPoligono2D.crearLienzoVacio( w + 1, hg + 1, fillColor );

		for( i = 1; i < xs.length; i++ )
		{
			imagenPoligono2D.crearImagenLinea( xs[i-1], ys[i-1]
					, xs[i], ys[i]
							, thickness
							, borderColor, mouth );
		}

		return mouth;
	}
	*/
	/*
	public static ImageIcon getSAMArousal( int level, int side, Color borderColor, Color fillColor )
	{
		int l = level;
		if( l < 0 )
		{
			l = 1;
		}
		else if( l > 9 )
		{
			l = 9;
		}


		int[] xs = { 0, 3, 4, 5, 8, 5, 8, 5, 7, 4, 3, 3, 1, 3, 0 };
		int[] ys = { 0, 2, 0, 2, 0, 3, 5, 5, 8, 6, 8, 5, 7, 4, 0 };

		for( int i = 0; i < xs.length; i++ )
		{
			xs[ i ] = (int)( ( l / 9.0 ) * (2.0 / 3 ) * side * xs[ i ] / 8.0 );
			ys[ i ] = (int)( ( l / 9.0 ) * (2.0 / 3 ) *  side * ys[ i ] / 8.0 );
		}

		Image doll = getDoll( side, side, borderColor, fillColor, fillColor ).getImage();
		Image cloud = imagenPoligono2D.crearImagenPoligonoRelleno( xs, ys, fillColor, null ); 
		imagenPoligono2D.crearImagenPoligonoPerfil( xs, ys, 2, borderColor, cloud );
		Image mov = imagenPoligono2D.crearImagenArco( 0, 0, (int)( side * .25 / 2)
				, (int)( side * .25 / 2), 180, -90
				, 2, borderColor, null, null );

		Image mov2 = imagenPoligono2D.crearImagenArco( 0, 0, (int)( side * .25 / 2)
				, (int)( side * .25 / 2), 90, -90
				, 2, borderColor, null, null );

		Image mov3 = imagenPoligono2D.crearImagenArco( 0, 0, (int)( side * .25 / 2)
				, (int)( side * .25 / 2), 0, -90
				, 2, borderColor, null, null );

		Image mov4 = imagenPoligono2D.crearImagenArco( 0, 0, (int)( side * .25 / 2)
				, (int)( side * .25 / 2), 270, -90
				, 2, borderColor, null, null );												

		int w = cloud.getWidth( null ) ;
		int h = cloud.getHeight( null );

		imagenPoligono2D.componerImagen( doll, (side - w) / 2, (side - h ) / 2, cloud );

		if( l > 6 )
		{
			int gap = 5;
			w = mov.getWidth( null );
			h = mov.getHeight( null )/2;

			imagenPoligono2D.componerImagen( doll, w / 2 + gap, h + gap, mov );
			if( l > 7 )
			{

				imagenPoligono2D.componerImagen( doll, gap, gap, mov );
			}

			w = mov2.getWidth( null );
			h = mov2.getHeight( null )/2;

			imagenPoligono2D.componerImagen( doll, side - w - w / 2 - gap, h + gap, mov2 );
			if( l > 7 )
			{
				imagenPoligono2D.componerImagen( doll, side - w - gap, gap , mov2 );
			}

			if( l > 8 )
			{
				w = mov4.getWidth( null );
				h = mov4.getHeight( null )/2;

				imagenPoligono2D.componerImagen( doll, gap, side - 2 * h - gap, mov4 );
				imagenPoligono2D.componerImagen( doll, w  / 2 + gap, side - 2 * h - h/2 - gap, mov4 );

				w = mov3.getWidth( null );
				h = mov3.getHeight( null )/2;

				imagenPoligono2D.componerImagen( doll, side - w - gap, side - 2 * h - gap, mov3 );
				imagenPoligono2D.componerImagen( doll, side - w - w / 2 - gap, side - 2 * h - h/2 - gap, mov3 );
			}
		}	

		return new ImageIcon( doll );
	}
	*/
	/*
	public static ImageIcon getSAMArousal( double level, int side, Color borderColor, Color fillColor )
	{
		double l = level;
		if( l < 0 )
		{
			l = 1;
		}
		else if( l > 1 )
		{
			l = 1;
		}


		int[] xs = { 0, 3, 4, 5, 8, 5, 8, 5, 7, 4, 3, 3, 1, 3, 0 };
		int[] ys = { 0, 2, 0, 2, 0, 3, 5, 5, 8, 6, 8, 5, 7, 4, 0 };

		for( int i = 0; i < xs.length; i++ )
		{
			xs[ i ] = (int)( l * (2.0 / 3 ) * side * xs[ i ] / 8.0 );
			ys[ i ] = (int)( l * (2.0 / 3 ) *  side * ys[ i ] / 8.0 );
		}

		Image doll = getDoll( side, side, borderColor, fillColor, fillColor ).getImage();
		Image cloud = imagenPoligono2D.crearImagenPoligonoRelleno( xs, ys, fillColor, null ); 
		imagenPoligono2D.crearImagenPoligonoPerfil( xs, ys, 2, borderColor, cloud );
		Image mov = imagenPoligono2D.crearImagenArco( 0, 0, (int)( side * .25 / 2)
				, (int)( side * .25 / 2), 180, -90
				, 2, borderColor, null, null );

		Image mov2 = imagenPoligono2D.crearImagenArco( 0, 0, (int)( side * .25 / 2)
				, (int)( side * .25 / 2), 90, -90
				, 2, borderColor, null, null );

		Image mov3 = imagenPoligono2D.crearImagenArco( 0, 0, (int)( side * .25 / 2)
				, (int)( side * .25 / 2), 0, -90
				, 2, borderColor, null, null );

		Image mov4 = imagenPoligono2D.crearImagenArco( 0, 0, (int)( side * .25 / 2)
				, (int)( side * .25 / 2), 270, -90
				, 2, borderColor, null, null );												

		int w = cloud.getWidth( null ) ;
		int h = cloud.getHeight( null );

		imagenPoligono2D.componerImagen( doll, (side - w) / 2, (side - h ) / 2, cloud );

		if( l > 6 / 9.0D )
		{
			int gap = 5;
			w = mov.getWidth( null );
			h = mov.getHeight( null )/2;

			imagenPoligono2D.componerImagen( doll, w / 2 + gap, h + gap, mov );
			if( l > 7 )
			{

				imagenPoligono2D.componerImagen( doll, gap, gap, mov );
			}

			w = mov2.getWidth( null );
			h = mov2.getHeight( null )/2;

			imagenPoligono2D.componerImagen( doll, side - w - w / 2 - gap, h + gap, mov2 );
			if( l > 7 / 9.0D )
			{
				imagenPoligono2D.componerImagen( doll, side - w - gap, gap , mov2 );
			}

			if( l > 8 / 9.0D )
			{
				w = mov4.getWidth( null );
				h = mov4.getHeight( null )/2;

				imagenPoligono2D.componerImagen( doll, gap, side - 2 * h - gap, mov4 );
				imagenPoligono2D.componerImagen( doll, w  / 2 + gap, side - 2 * h - h/2 - gap, mov4 );

				w = mov3.getWidth( null );
				h = mov3.getHeight( null )/2;

				imagenPoligono2D.componerImagen( doll, side - w - gap, side - 2 * h - gap, mov3 );
				imagenPoligono2D.componerImagen( doll, side - w - w / 2 - gap, side - 2 * h - h/2 - gap, mov3 );
			}
		}	

		return new ImageIcon( doll );
	}
	*/
	/*
	public static ImageIcon getSAMDominance( double level, int side, Color borderColor, Color fillColor )
	{
		double l = level;
		if( l < 0 )
		{
			l = 1;
		}
		else if( l > 1 )
		{
			l = 1;
		}


		double w = 0.2 + l * 0.8 ; 
		w *= side;

		return getDoll((int)w , (int)w, borderColor, fillColor, fillColor ); //getSAMValence( 5, (int)w, borderColor, fillColor );
	}
	*/
	/*
	public static ImageIcon getSAMDominance( int level, int side, Color borderColor, Color fillColor )
	{
		int l = level;
		if( l < 0 )
		{
			l = 1;
		}
		else if( l > 9 )
		{
			l = 9;
		}


		double w = 0.2 + l * 0.8 / 9 ; 
		w *= side;

		return getDoll((int)w , (int)w, borderColor, fillColor, fillColor ); //getSAMValence( 5, (int)w, borderColor, fillColor );
	}
	*/
	/*
	public static ImageIcon getBasicEmotion(int level, int side, Color borderColor, Color fillColor, String text, FontMetrics fm)
	{
		int l = level;
		if (l < 1)
		{
			l = 1;
		}
		else if (l > 7)
		{
			l = 7;
		}

		int thicknessBorder = 3;

		int h = side;
		int hText = 0;
		Image imgText = null;

		if ((text != null) && (!text.isEmpty()))
		{
			imgText = imagenPoligono2D.crearImagenTexto(0, 0, text, fm, fillColor, borderColor, null);

			hText = imgText.getHeight(null);
			h -= hText;
		}

		if (h > 0)
		{
			side = h;
		}

		Image img = imagenPoligono2D.crearImagenRectangulo(side, h, thicknessBorder, borderColor, fillColor);
		Image eye = imagenPoligono2D.crearImagenCirculo(0, 0, side / 6, borderColor, null);

		if (l == 1)
		{
			// Sadness

			img = getSAMValence( 1, side, borderColor, fillColor).getImage();	
			if( h > 0)
			{
				img = img.getScaledInstance( img.getWidth( null ), h,  BufferedImage.SCALE_SMOOTH );
			}
		}
		else if (l == 2)
		{
			// Surprise	

			Image eyebrow = imagenPoligono2D.crearLienzoVacio( eye.getWidth( null ), thicknessBorder, borderColor );
			Image mouth = imagenPoligono2D.crearImagenCirculo( 0, 0, side/3, borderColor, null );

			img = imagenPoligono2D.componerImagen( img, side/6, side/6 , eye );
			img = imagenPoligono2D.componerImagen( img, side/6, side/6 - eye.getHeight( null )/4, eyebrow );

			img = imagenPoligono2D.componerImagen( img, side - side/3, side/6 , eye );
			img = imagenPoligono2D.componerImagen( img, side - side/3, side/6 - eye.getHeight( null )/4, eyebrow );

			img = imagenPoligono2D.componerImagen( img, img.getWidth( null ) / 2 - mouth.getWidth( null ) /2
					, img.getHeight( null ) - (int)( 1.25 * mouth.getHeight( null ) ), mouth );
		}
		else if (l == 3)
		{
			// Anger

			img = getSAMValence( 4, side, borderColor, fillColor).getImage();

			//Image mouth = getSmile( 5, side, thicknessBorder, borderColor, fillColor );					
			Image rightEyebrow = imagenPoligono2D.crearImagenLinea( 0, 0
					, eye.getWidth(null ), side/10
					, thicknessBorder, borderColor, null );

			Image leftEyebrow = imagenPoligono2D.crearImagenLinea( 0, side/10
					, eye.getWidth(null ), 0 
					, thicknessBorder, borderColor, null );

			img = imagenPoligono2D.componerImagen( img, side/6, side/5 - (int)( 1.05 * rightEyebrow.getHeight( null )), rightEyebrow );			
			img = imagenPoligono2D.componerImagen( img, side - leftEyebrow.getWidth( null ) - side/6, side/5 - (int)(1.05 * leftEyebrow.getHeight( null )), leftEyebrow );

			if( h > 0)
			{
				img = img.getScaledInstance( side, h,  BufferedImage.SCALE_SMOOTH );
			}
		}
		else if (l == 4)
		{
			// Disgust

			Image rigthEye = imagenPoligono2D.crearLienzoVacio(eye.getWidth(null), eye.getHeight(null), fillColor);
			imagenPoligono2D.crearImagenLinea(0, 0, eye.getWidth(null), eye.getHeight(null) / 2, thicknessBorder, borderColor, rigthEye);
			imagenPoligono2D.crearImagenLinea(eye.getWidth(null), eye.getHeight(null) / 2, 0, eye.getHeight(null), thicknessBorder, borderColor, rigthEye);

			Image leftEye = imagenPoligono2D.crearLienzoVacio(eye.getWidth(null), eye.getHeight(null), fillColor);
			imagenPoligono2D.crearImagenLinea(eye.getWidth(null), 0, 0, eye.getHeight(null) / 2, thicknessBorder, borderColor, leftEye);
			imagenPoligono2D.crearImagenLinea(eye.getWidth(null), eye.getHeight(null), 0, eye.getHeight(null) / 2, thicknessBorder, borderColor, leftEye);

			Image mouth = imagenPoligono2D.crearImagenArco(0, 0, (int)( side / 2.5 ), (int)( side / 2.5 ), 0, 180, thicknessBorder, borderColor, borderColor, null);

			int tongueW = mouth.getWidth(null) / 2;

			int x = thicknessBorder / 2;
			int y = thicknessBorder / 2;

			int w = tongueW - 2 * thicknessBorder;
			if (w < 1)
			{
				w = tongueW;
			}

			Image tongueTip = imagenPoligono2D.crearImagenArco(0, 0, tongueW, tongueW - thicknessBorder, 0, -180, thicknessBorder, borderColor, fillColor, null);

			Image tongueBody = imagenPoligono2D.crearLienzoVacio(tongueTip.getWidth(null), tongueTip.getHeight(null), fillColor);

			imagenPoligono2D.crearImagenLinea(x, y, tongueBody.getWidth(null), y, thicknessBorder, borderColor, tongueBody);
			imagenPoligono2D.crearImagenLinea(x, y, x, tongueBody.getHeight(null), thicknessBorder, borderColor, tongueBody);
			imagenPoligono2D.crearImagenLinea(tongueBody.getWidth(null) / 2 - x, y, tongueBody.getWidth(null) / 2 - x, tongueBody.getHeight(null), thicknessBorder, borderColor, tongueBody);
			imagenPoligono2D.crearImagenLinea(tongueBody.getWidth(null) - x - 1, y, tongueBody.getWidth(null) - x - 1, tongueBody.getHeight(null), thicknessBorder, borderColor, tongueBody);

			Image tongue = imagenPoligono2D.crearLienzoVacio(tongueTip.getWidth(null), tongueTip.getHeight(null) / 2 + tongueBody.getHeight(null), fillColor);

			tongue = imagenPoligono2D.componerImagen(tongue, 0, 0, tongueBody);
			tongue = imagenPoligono2D.componerImagen(tongue, 0, tongueBody.getHeight(null) / 2, tongueTip);


			Image mouthTongue = imagenPoligono2D.crearLienzoVacio(mouth.getWidth(null), mouth.getHeight(null) / 2 + tongue.getHeight(null) - mouth.getHeight(null) / 6, null);
			mouthTongue = imagenPoligono2D.componerImagen(mouthTongue, 0, 0, mouth);
			mouthTongue = imagenPoligono2D.componerImagen(mouthTongue, 
					mouthTongue.getWidth(null) / 2 - tongue.getWidth(null) / 2, 
					mouth.getHeight(null) / 3, 
					tongue);


			img = imagenPoligono2D.componerImagen(img, side - side / 3 - leftEye.getWidth(null) / 4, side / 6, leftEye);


			img = imagenPoligono2D.componerImagen(img, side / 6 + rigthEye.getWidth(null) / 4, side / 6, rigthEye);


			img = imagenPoligono2D.componerImagen(img, (img.getWidth(null) - mouthTongue.getWidth(null)) / 2, 
					img.getHeight(null) - (int)(1.05D * mouthTongue.getHeight(null)), 
					mouthTongue);
		}
		else if (l == 5)
		{
			// Fear

			int smileW = side - side / 3;
			int smileH = side / 9;

			Image mouth = imagenPoligono2D.crearLienzoVacio(smileW, smileH + 2 * thicknessBorder, fillColor);

			double stepSin = (4  * Math.PI ) / smileW;

			List<Integer> yVal = new ArrayList< Integer >();
			for (double xVal = 0.0D; xVal <= 4 * Math.PI; xVal += stepSin)
			{
				yVal.add( ( int )( smileH * ( Math.sin( xVal ) + 1 ) / 2 ) + thicknessBorder );
			}

			for (int i = 1; i < yVal.size(); i++)
			{
				mouth = imagenPoligono2D.crearImagenLinea(i - 1, ((Integer)yVal.get(i - 1)).intValue(), i, ((Integer)yVal.get(i)).intValue(), thicknessBorder, borderColor, mouth);
			}

			Image leftEyebrow = imagenPoligono2D.crearImagenLinea( 0, 0
					, eye.getWidth(null ), side/6
					, thicknessBorder, borderColor, null );

			Image rigthEyebrow = imagenPoligono2D.crearImagenLinea( 0, side/6
					, eye.getWidth(null ), 0 
					, thicknessBorder, borderColor, null );

			img = imagenPoligono2D.componerImagen( img, side/6, side/6 , eye );
			img = imagenPoligono2D.componerImagen( img, side/6 - eye.getWidth( null ) / 2, side/6 - rigthEyebrow.getHeight( null ) / 2, rigthEyebrow );

			img = imagenPoligono2D.componerImagen( img, side - side/3, side/6 , eye );
			img = imagenPoligono2D.componerImagen( img, side - side/3 + eye.getWidth( null ) / 2, side/6 - leftEyebrow.getHeight( null ) / 2, leftEyebrow );

			img = imagenPoligono2D.componerImagen( img, side/2 - mouth.getWidth( null ) / 2 , img.getHeight( null ) - img.getHeight( null ) / 3  , mouth );
		}
		else if (l == 6)
		{
			// Happiness

			img = getSAMValence(9, side, borderColor, fillColor).getImage();
			if (h > 0)
			{
				img = img.getScaledInstance(img.getWidth(null), h,  BufferedImage.SCALE_SMOOTH );
			}

		}
		else // Neutral
		{
			img = getSAMValence(5, side, borderColor, fillColor).getImage();
			if (h > 0)
			{
				img = img.getScaledInstance(img.getWidth(null), h,  BufferedImage.SCALE_SMOOTH );
			}
		}

		Image out = imagenPoligono2D.crearLienzoVacio(img.getWidth(null), img.getHeight(null) + hText, fillColor);
		imagenPoligono2D.componerImagen(out, 0, 0, img);

		if (imgText != null)
		{
			imagenPoligono2D.componerImagen(out, (img.getWidth(null) - imgText.getWidth(null)) / 2, img.getHeight(null), imgText);
		}

		return new ImageIcon(out);
	}
	*/
	/*
	public static ImageIcon getDoll(int width, int height, Color borderColor, Color fillColor, Color bgColor)
	{
		Image base = imagenPoligono2D.crearLienzoVacio(width, height, bgColor);

		double[] x = { 0.625D, 0.625D, 0.375D, 0.375D, 
				0.875D, 0.875D, 0.75D, 0.75D, 0.75D, 
				0.5D, 0.5D, 0.5D, 0.25D, 0.25D, 0.25D, 0.125D, 0.125D, 0.5D };

		double[] y = { 0.25D, 0.0D, 0.0D, 0.25D, 0.25D, 
				0.625D, 0.625D, 0.5D, 0.99D, 
				0.99D, 0.625D, 0.99D, 0.99D, 0.5D, 0.625D, 
				0.625D, 0.25D, 0.25D };

		int[] xs = new int[x.length];
		int[] ys = new int[y.length];

		for (int i = 0; i < xs.length; i++)
		{
			xs[i] = ((int)(x[i] * width));
			ys[i] = ((int)(y[i] * height));
		}


		imagenPoligono2D.crearImagenPoligonoRelleno(xs, ys, fillColor, base);
		imagenPoligono2D.crearImagenPoligonoPerfil(xs, ys, 2.0F, borderColor, base);

		return new ImageIcon(base);
	}
	*/
	
	public static ImageIcon getQuaver( int width, int height, Color fillColor, Color bgColor)
	{
		Image base = basicPainter2D.createEmptyImage( width, height, bgColor );
		
		int s = Math.min( width, height );
		
		int t = Math.max( width, height ) / 16;
		
		if( t < 1 )
		{
			t = 1;
		}
		
		int r = s / 4;
		int x = r / 2; 
		int x2 = width - x - r;
		int y = height - r - height / 8; 
		
		basicPainter2D.paintFillCircle( x, y, r, fillColor, base );
		basicPainter2D.paintFillCircle( x2, y, r, fillColor, base );
		
		basicPainter2D.paintLine( x + r - t / 2, ( 3 * height ) / 4, x + r - t / 2, height / 4, t, fillColor, base );
		basicPainter2D.paintLine( x2 + r - t / 2, ( 3 * height ) / 4, x2 + r - t / 2, height / 4, t, fillColor, base );
		
		basicPainter2D.paintLine( x + r, height / 4, x2 + r - t, height / 4, t * 2, fillColor, base );
		
		return new ImageIcon( base );
	}
	
	public static ImageIcon Sound( int width, int height, Color fillColor, Color bgColor)
	{
		Image base = basicPainter2D.createEmptyImage( width, height, bgColor );
		
		int x[]  = new int[] { width / 8, width / 4, width / 2, width / 2, width / 4, width / 8 };
		int y[]  = new int[] { ( 3 * height ) / 8, ( 3 * height ) / 8, height / 4, ( 3 * height ) / 4, ( 5 * height ) / 8, ( 5 * height ) / 8 };

		int h = y[ 3 ] - y[ 2 ];
		
		int t = Math.max( width, height ) / 32;
		
		if( t < 1 )
		{
			t = 1;
		}
		
		basicPainter2D.paintFillPolygon( x, y, fillColor, base ); 
		
		int w =  width / 8;
		
		basicPainter2D.paintArc( width / 2, height / 2 - h / 8 - t, w / 2, h / 4, -90, 180, t, fillColor, null, base );
		
		if( width >= 32 )
		{		
			basicPainter2D.paintArc( width / 2, height / 2 - h / 4 - t, w, h / 2, -90, 180, t, fillColor, null, base );
		}
		
		basicPainter2D.paintArc( width  / 2, height / 2 - ( ( 4 * h ) / 5 ) /2 - t, ( 3 * w ) / 2, ( 4 * h ) / 5, -90, 180, t, fillColor, null, base );
		
		return new ImageIcon( base );
	}
	
	public static ImageIcon InterleavedIcon( int width, int height, Color border, Color fill, Color background )
	{
		Image base = basicPainter2D.createEmptyImage( width, height, background );
		
		int thick = Math.min( width, height) / 16;
		if( thick < 1 )
		{
			thick = 1;
		}
		
		Color c = fill;
		if( c == null )
		{
			c = border;			
			
			if( c == null )
			{
				c = background;
			}
			
			if( c == null )
			{
				c = Color.RED;
			}
			else
			{			
				c = new Color( ~c.getRGB() );
			}
		}
					
		Image rect = basicPainter2D.paintRectangle( ( 3 * width ) / 10 - 1, ( 3 * height ) / 5, thick, border, c );
		
		int x = rect.getWidth( null ) / 3;
		int y = rect.getHeight( null ) / 3;
		basicPainter2D.compoundImages( base, x * 2 + thick + 1, thick, rect );
		basicPainter2D.compoundImages( base, x + thick + 1, y, rect );
		basicPainter2D.compoundImages( base, thick + 1, 2 * y - thick, rect );
		
		x = rect.getWidth( null ) / 3;
		y = rect.getHeight( null ) / 3;
		basicPainter2D.compoundImages( base, width / 2 + x * 2 - thick + 1, thick, rect );
		basicPainter2D.compoundImages( base, width / 2 + x - thick + 1, y, rect );
		basicPainter2D.compoundImages( base, width / 2 - thick + 1, 2 * y - thick, rect );
		
		if( fill == null )
		{
			Color n = new Color( c.getRed() / 255F, c.getGreen() / 255F , c.getBlue() / 255F, 0F );
			
			basicPainter2D.changeColorPixels( c, n, base );
		}
		
		
		return new ImageIcon( base );		
	}
}