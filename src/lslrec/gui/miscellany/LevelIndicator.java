/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.  https://github.com/manmermon/LSLRecorder
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


package lslrec.gui.miscellany;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;

public class LevelIndicator extends JComponent 
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final int HORIZONTAL = 0;
	public static final int VERTICAL = 1;
	
	private int numLevels = 2;
	private Color[] levelsColors = { Color.BLUE, new Color( 238, 238, 238)};
	private Insets inset = null;
	private int widtStick = 11;
	private int value = 0;
	
	private int max = 100;
	private int min = 0;
	
	private boolean inverted = false;
	private boolean editable = false;
	private boolean paintValue = true;
	
	private boolean gradientColor = false;
	
	private int orientation = HORIZONTAL;
	
	private int[] levels = null;
	
	private String fixedText = null; 
	
	private String text = "";
	
	public LevelIndicator( )
	{
		super();
		
		this.numLevels = 2;
		
		this.levels = new int[ this.numLevels - 1 ];
		int n = this.min;
		for( int i = 0; i < this.numLevels - 1; i++ )
		{
			this.levels[ i ] = n + ( this.max - this.min ) / this.numLevels;
		}
		//this.ins = new Insets( 20, 0, 20, 0);
				
		this.addInteractions();
	}
	
	public LevelIndicator( int levels )
	{
		super();
		
		if( levels < 2 )
		{
			throw new IllegalArgumentException( "Minimum value = 2.");
		}
		
		//this.ins = new Insets( 20, 0, 20, 0);
		
		this.numLevels = levels;
		this.levelsColors = new Color[ levels ];
		
		this.levels = new int[ levels - 1 ];
		int n = this.min;
		for( int i = 0; i < levels - 1; i++ )
		{
			n = n + ( this.max - this.min ) / levels;
			this.levels[ i ] = n;
		}
		int c = 0;
		for( int i = 0; i < levels; i++ )
		{	
			c = Color.HSBtoRGB( i * 1.0F / levels , 1, 1);
			this.levelsColors[ i ] = new Color( c );
		}		
		
		this.addInteractions();
	}	
	
	public LevelIndicator( int[] levels )
	{
		if( levels.length == 0 || levels == null )
		{
			throw new IllegalArgumentException( "Input null/empty.");
		}
			
		this.numLevels = levels.length;
		System.arraycopy( levels, 0, this.levels, 0, levels.length );
		this.levelsColors = new Color[ this.numLevels + 1];	
	
		int c = 0;
		for( int i = 0; i < this.numLevels; i++ )
		{	
			c = Color.HSBtoRGB( i * 1.0F / this.numLevels , 1, 1);
			this.levelsColors[ i ] = new Color( c );
		}		
		
		this.addInteractions();
	}
	
	public int[] getLevels( )
	{
		return this.levels;
	}
	
	public void setLevels( int[] levels )
	{
		if( levels.length != this.levels.length )
		{
			throw new IllegalArgumentException( "Array length incorrect. Input size must be = " + this.levels.length + ".");
		}
		
		System.arraycopy( levels, 0, this.levels, 0, levels.length );
		
		this.repaint();
	}
	
	public void setColorLevels( Color[] colors)
	{
		if( colors.length != this.levelsColors.length )
		{
			throw new IllegalArgumentException( "Input length is different to levels");
		}
		
		for( int i = 0; i < colors.length; i++)
		{
			Color c = colors[ i ];
			if( c == null )
			{
				throw new IllegalArgumentException( "Color Null");
			}
			
			this.levelsColors[ i ] = c;
		}
		
		this.repaint();
	}
		
	public void setOrientation( int orientation )
	{
		if( orientation != HORIZONTAL && orientation != VERTICAL )
		{
			this.orientation = HORIZONTAL;
		}
		else
		{
			this.orientation = orientation;
		}
		
		this.repaint();
	}
	
	public void setInverted( boolean invert)
	{
		this.inverted = invert;
		
		this.repaint();
	}
	
	public void setValue( int val )
	{
		int v = this.value;
		this.value = val;
		
		if( this.value < this.min )
		{
			this.value = this.min;
		}
		else if( this.value > this.max )
		{
			this.value = this.max;
		}
		
		if( v != this.value )
		{
			this.repaint();
		}
	}
	
	public void setMaximum( int max )
	{	
		if( (this.max - this.min) != 0 )
		{
			double escala = (1.0 * Math.abs( max - this.min ) / Math.abs( this.max - this.min));
			for( int i = 0; i < this.levels.length; i++ )
			{
				this.levels[ i ] = this.min + (int)( this.levels[ i ] * escala); 
			}
			
			this.setValue( this.min + (int)( this.value * escala ) );
		}
		
		this.max = max;		
		
		this.repaint();
	}
	
	public void setMinimum( int min )
	{
		if( (this.max - this.min) != 0 )
		{	
			double escala = (1.0 * Math.abs( this.max - min ) / Math.abs( this.max - this.min));
			
			for( int i = 0; i < this.levels.length; i++ )
			{
				this.levels[ i ] = min + (int)( this.levels[ i ] * escala);
			}
			
			this.setValue( min + (int)( this.value * escala ) );
		}
		
		this.min = min;	
		
		this.repaint();
	}
	
	public void setPaintedString( boolean s )
	{
		this.paintValue = s;
		
		this.repaint();
	}
	
	public void setGradientColor( boolean gr )
	{
		this.gradientColor = gr;
		
		this.repaint();
	}
	
	public void setLevelIndicatorWidth( int width )
	{
		this.widtStick = width;
		
		this.repaint();
	}
	
	public void setEditable( boolean edit )
	{
		this.editable = edit;
	}
	
	public void setString( String text )
	{
		this.fixedText = text;
		
		super.repaint();
	}
	
	public String getString()
	{
		return this.text;
	}
	
	public Insets getInsets()
	{
		return this.inset;
	}	
	
	public int getNumLevels()
	{
		return this.numLevels;
	}

	public int getValue( )
	{
		return this.value;
	}
	
	public int getMaximum()
	{
		return this.max;
	}
	
	public int getMinimum()
	{
		return this.min;
	}
	
	public boolean isEditable()
	{
		return this.editable;
	}
			
	public void repaint()
	{
		this.autoSetInsets();
		
		super.repaint();
	}
	
	protected void paintComponent( Graphics g )
	{
		super.paintComponents( g );
		//System.out.println("indicadorNivel.paintComponent(): size = "+super.getSize());
		this.autoSetInsets();		
	
		Point loc = new Point( this.inset.left, this.inset.top );
		int Width = getWidth();
		int Heigh = getHeight();
		
		if( super.isEnabled() )
		{
			if( this.orientation == HORIZONTAL )
			{
				int[] levels = new int[ this.levels.length ];
				
				int ini = 0;
				int fin = levels.length - 1;
				
				int pad = this.widtStick / this.numLevels;
				if( pad == 0 )
				{
					pad = 1;
				}
				int relleno = 0;
				
				if( this.inverted )
				{				
					for( int z = this.levels.length - 1; z >= 0; z-- )
					{					
						levels[ this.levels.length - 1 - z ] = this.levels[ z ];
					}
				}
				else
				{
					for( int z = 0; z < this.levels.length; z++ )
					{					
						levels[ z ] = this.levels[ z ];
					}
				}
				
				for( int i = ini; i <= fin; i++ )
				{
					Point p = this.valuePositionPixel( levels[ i ] * 1.0);					
					Color c = this.levelsColors[ i ];
					if( this.inverted )
					{
						c = this.levelsColors[ levelsColors.length - 1 - i ];
					}
					
					this.drawGradientRect(g, loc.x, loc.y, Math.abs( loc.x - (p.x + pad * ( i + 1)) ), Heigh - this.inset.top - this.inset.bottom, c, false);
					//System.out.println("indicadorNivel.paintComponent(): width = " + Math.abs( loc.x - (p.x + pad* ( i + 1))));
					
					loc.x = p.x + pad * ( i + 1);				
					loc.y = p.y;	
					
					relleno += pad;
					if( relleno >= this.widtStick )
					{
						pad = 0;
					}
				}
				
				Color c = this.levelsColors[ this.numLevels - 1 ];
				if( this.inverted )
				{
					c = this.levelsColors[ 0 ];
				}
				
				pad = this.widtStick - relleno;
				if( pad < 0 )
				{
					pad = 0;
				}			
				int w = Width - loc.x - this.inset.right;
				
				this.drawGradientRect(g, loc.x, loc.y, w , Heigh - this.inset.top - this.inset.bottom, c, false);
				//System.out.println("indicadorNivel.paintComponent(): width = "+w);
			}
			else
			{
				int[] levels = new int[ this.levels.length ];
				
				int ini = 0;
				int fin = levels.length - 1;
				
				int pad = this.widtStick / this.numLevels;
				if( pad == 0 )
				{
					pad = 1;
				}
				int relleno = 0;
				
				if( !this.inverted )
				{				
					for( int z = this.levels.length - 1; z >= 0; z-- )
					{					
						levels[ this.levels.length - 1 - z ] = this.levels[ z ];
					}
				}
				else
				{
					for( int z = 0; z < this.levels.length; z++ )
					{					
						levels[ z ] = this.levels[ z ];
					}
				}
				
				for( int i = ini; i <= fin; i++ )
				{
					Point p = this.valuePositionPixel( levels[ i ] * 1.0);
					Color c = this.levelsColors[ i ];
					if( this.inverted )
					{
						c = this.levelsColors[ levelsColors.length - 1 - i ];
					}
					
					this.drawGradientRect(g, loc.x, loc.y, Width - this.inset.left - this.inset.right, Math.abs( loc.y - (p.y + pad * ( i + 1)) ), c, true);
					//System.out.println("indicadorNivel.paintComponent(): width = " + Math.abs( loc.y - (p.y + pad* ( i + 1))));
					
					loc.x = p.x;
					loc.y = p.y + pad * ( i + 1);	
					
					relleno += pad;
					if( relleno >= this.widtStick )
					{
						pad = 0;
					}
				}
				
				Color c = this.levelsColors[ this.numLevels - 1 ];
				if( this.inverted )
				{
					c = this.levelsColors[ 0 ];
				}
				
				pad = this.widtStick - relleno;
				if( pad < 0 )
				{
					pad = 0;
				}			
				int h = Heigh - loc.y - this.inset.top;
				
				this.drawGradientRect(g, loc.x, loc.y, Width - this.inset.left - this.inset.right, h, c, true);
				//System.out.println("indicadorNivel.paintComponent(): width = " + h);
			}
		}
		else
		{
			boolean ver = false;
			if( this.orientation == VERTICAL )
			{
				ver = true;
			}
			this.drawGradientRect( g , this.inset.left, this.inset.top, Width - this.inset.left - this.inset.right, Heigh - this.inset.top - this.inset.bottom, Color.lightGray, ver);
		}
		
		Color c = new Color( 238, 238, 238 ).darker();
		if( !isEnabled() )
		{
			c = Color.gray;
		}		
		g.setColor( c );		
		//g.drawRect( this.inset.left, this.inset.top, getWidth() - this.inset.left - this.inset.right, getHeight() - this.inset.top - this.inset.bottom );
		int w = getWidth() - ( this.inset.left + this.inset.right)/2;
		int h = getHeight() - ( this.inset.top + this.inset.bottom ) /2 ;
		
		if( orientation == HORIZONTAL )
		{
			w -= 1;
		}
		else
		{
			h -= 1;
		}
		
		g.drawRect( 0, 0, w, h);
		
		this.drawStick( g );
		
		if( this.paintValue )
		{
			this.drawValue( g );
		}
	}
	
	private void addInteractions()
	{
		super.addMouseMotionListener( new MouseMotionListener()
		{
			public void mouseDragged(MouseEvent arg0) 
			{
				if( editable && isEnabled() )
				{
					if( orientation == HORIZONTAL )
					{
						int pxStick = arg0.getX();
						Insets i = getInsets();
						
						if( pxStick < i.left )
						{
							pxStick = i.left;
						}
						else if( pxStick > getWidth() - i.right - widtStick  )
						{
							pxStick = getWidth() - i.right - widtStick ;
						}
						
						setValue( getValuePx( pxStick ));					
					}
					else
					{
						int pyStick = arg0.getY();
						Insets i = getInsets();
						
						if( pyStick < i.top )
						{
							pyStick = i.top;
						}
						else if( pyStick > getHeight() - i.bottom - widtStick)
						{
							pyStick = getHeight() - i.bottom - widtStick;
						}
						
						setValue( getValuePx( pyStick ));
					}	
				}
			}

			public void mouseMoved(MouseEvent arg0) {
 
				
			}
			
		});
	}
		
	private void setInsets( int top, int left, int bottom, int right )
	{
		this.inset = new Insets( top, left, bottom, right);
	}
	
	private void autoSetInsets()
	{
		int w = this.getWidth(), h = super.getHeight();
		
		int top = ( int) ( h * 0.1);
		int bottom = top;
		int left = 0, right = 0;
		if( orientation == VERTICAL )
		{
			left = right = ( int) ( w * 0.1);
			top = bottom = 0;
		} 
		
		if( top > 20 )
		{
			top = bottom = 20;
		}
		
		if( left > 20 )
		{
			left = right = 20;
		}
		
		this.setInsets( top, left, bottom, right);
	}
	
	private void drawStick( Graphics g )
	{
		Point coord = this.indicatorPosition( this.value );
		
		int w = this.widtStick;
		int h = super.getHeight();
		boolean vertical = true;
		
		if( this.orientation == VERTICAL )
		{
			w = super.getWidth();
			h = this.widtStick;
			vertical = false;
		}
		
		Color c = Color.blue;
		if( !isEnabled() )
		{
			c = Color.lightGray;
		}
		this.drawGradientRect(g, coord.x, coord.y, w, h, c, vertical);
		
		c = Color.black;
		if( !isEnabled() )
		{
			c = Color.gray;
		}
		g.setColor( c );
		g.drawRect( coord.x, coord.y, w, h);

	}
	
	private void drawGradientRect( Graphics g, int x, int y, int width, int height, Color c, boolean vertical )	
	{ 
		if( isEnabled() && this.gradientColor )
		{
			float[] hsb = Color.RGBtoHSB( c.getRed(), c.getGreen(), c.getBlue(), null);
			
			float minSat = 0.2F; 
			if( vertical )
			{
				for( int i = 0; i < width; i++ )
				{
					float s = (i * ( 1.0F - minSat) / width) + minSat;
					g.setColor( new Color( Color.HSBtoRGB( hsb[ 0 ], s, hsb[ 2])));
					g.drawLine( x + i, y, x + i, y + height);
				}
			}
			else
			{
				for( int i = 0; i < height; i++ )
				{
					float s = (i * ( 1.0F - minSat) / height) + minSat;
					g.setColor( new Color( Color.HSBtoRGB( hsb[ 0 ], s, hsb[ 2])));
					g.drawLine( x, y + i, x + width, y + i);
				}
			}
		}
		else
		{
			g.setColor( c );
			g.fillRect( x, y, width, height);
		}
	}
	
	private void drawValue( Graphics g)
	{
		Color c = Color.black;		
		if( !isEnabled() )
		{
			c = Color.gray;
		}
		
		//g.setFont( new Font( Font.DIALOG, Font.BOLD, 12));
		g.setFont( super.getFont() );
		FontMetrics fm = g.getFontMetrics( g.getFont() );
				
		this.text = "" + this.value;
		if( this.fixedText != null )
		{
			this.text  = this.fixedText;
		}
		
		int xs = ( getWidth() - fm.stringWidth( this.text ) ) / 2;
		int ys = ( getHeight() - fm.getHeight()) / 2 + fm.getAscent();
		/*
		if( xs < 0 )
		{
			xs = 0;
		}
		
		if( ys < 0 )
		{
			ys = 0;
		}
		
		g.drawString( this.text , xs , ys);
		
		xs = xs + 2;
		
		g.drawString( this.text , xs , ys);
		
		ys = ys + 2;
		g.drawString( this.text , xs , ys);			
		
		xs = xs - 2;
		g.drawString( this.text , xs , ys);
		
		xs = xs + 1;
		ys = ys - 1;
		
		c = Color.white;
		if( !isEnabled() )
		{
			c = Color.lightGray;
		}
		*/
		
		g.setColor( c );						
		g.drawString( this.text , xs , ys);
	}
	
	private Point valuePositionPixel( double val )
	{
		Point loc = new Point(); 
		
		int stickWidth = this.widtStick;
				
		loc.x = this.inset.left + (int)(( super.getWidth() - this.inset.left - this.inset.right - stickWidth) * ( ( val - this.min ) / ( this.max - this.min)));
		loc.y = this.inset.top;
		
		if( this.inverted )
		{	
			loc.x = this.inset.left + (int)(( super.getWidth() - this.inset.left - this.inset.right - stickWidth) * ( ( this.max - val ) / ( this.max - this.min)));
		}
		
		if( loc.x < this.inset.left )
		{
			loc.x = this.inset.left;
		}
		else if( loc.x > super.getWidth() - this.inset.right - stickWidth )
		{
			  loc.x = super.getWidth() - this.inset.right - stickWidth;
		}
				 		
		if( this.orientation == VERTICAL )
		{
			loc.x = this.inset.left;
			loc.y = this.inset.top + (int)(( super.getHeight() - this.inset.top - this.inset.bottom - stickWidth) * ( (val - this.min) / ( this.max - this.min)));
			
			if( !this.inverted )
			{
				loc.y = this.inset.top + (int)(( super.getHeight() - this.inset.top - this.inset.bottom - stickWidth) * ( (this.max - val) / ( this.max - this.min)));
			}
			
			if( loc.y > super.getHeight() - this.inset.bottom - stickWidth )
			{
				loc.y = super.getHeight() - this.inset.bottom - stickWidth;
			}
			else if( loc.y < this.inset.top )
			{
				  loc.y = this.inset.top;
			}
		}
		
		return loc;
	}
	
	private Point indicatorPosition( double val )
	{	
		Point loc = this.valuePositionPixel( val );
		
		if( this.orientation == HORIZONTAL )
		{
			loc.y = 0;
		}
		else
		{
			loc.x = 0;
		}		
		
		return loc;
	}
	
	private int getValuePx( int coord )
	{
		int w = super.getWidth();
		int h = super.getHeight();
		
		double val =  1.0 * ( coord - this.inset.left) / ( w - this.inset.left - this.inset.right - this.widtStick ); 
		val = this.min + val * ( this.max - this.min ) ;		
				
		if( coord > w - this.inset.right )
		{
			val = this.max;
		}
		else if( coord < this.inset.left )
		{
			val = this.min;
		}
		
		if( this.inverted )
		{
			val =  this.max - (val - this.min);
			
			if( coord > w - this.inset.right - this.widtStick)
			{
				val = this.min;
			}
			else if( coord < this.inset.left )
			{
				val = this.max;
			}
		}
		
		if( this.orientation == VERTICAL )
		{
			val =  1.0 * ( coord - this.inset.top ) / ( h - this.inset.bottom - this.inset.top - this.widtStick ); 
			val = max - val * ( this.max - this.min );
			
			if( coord < this.inset.top )
			{
				val = this.max;
			}
			else if( coord > h - this.inset.bottom - this.widtStick )
			{
				val = this.min;
			}
			
			if( this.inverted )
			{
				val = min + max - val;
				
				if( coord < this.inset.top )
				{
					val = this.min;
				}
				else if( coord > h - this.inset.bottom - this.widtStick )
				{
					val = this.max;
				}
			}
		}
		
		return (int)Math.floor( val );
	}
}
