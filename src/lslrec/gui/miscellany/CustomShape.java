package lslrec.gui.miscellany;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

/**
 * CustomShape
 * based on a Class from Andrew Thompson * 
 * Source: http://stackoverflow.com/questions/7052422/image-graphic-into-a-shape-in-java/7059497#7059497
 * @author Samuel Schneider, Andrew Thompson
 * 
 *
 */
public class CustomShape 
{
    private BufferedImage image = null;

    
    public CustomShape( BufferedImage img ) 
    {
    	this.image = img;
    }
    
    /**
     * Creates an Area with PixelPerfect precision
     * @param color The color that is draws the Custom Shape
     * @param tolerance The color tolerance
     * @return Area
     */
    public Area getArea(Color color, int tolerance) 
    {
        if(image==null) 
        {
        	return null;
        }
        
        Area area = new Area();
        
        for (int x=0; x<image.getWidth(); x++)
        {
            for (int y=0; y<image.getHeight(); y++) 
            {
                Color pixel = new Color(image.getRGB(x,y));
                
                if ( isIncluded(color, pixel, tolerance) ) 
                {
                    Rectangle r = new Rectangle(x,y,1,1);
                    area.add(new Area(r));
                }
            }
        }

        return area;
    }

    public Area getArea_FastHack( Color shapeColor ) 
    {
        //Assumes Black as Shape Color
        if( image == null ) 
        {
        	return null;
        }

        Area area = new Area();
        Rectangle r;
        int y1,y2;

        for (int x=0; x<image.getWidth(); x++) 
        {
            y1= this.image.getHeight() + 1;
            y2=-1;
            for (int y = 0; y < image.getHeight(); y++ ) 
            {
                Color pixel = new Color( image.getRGB(x,y) );
                
                //-16777216 entspricht RGB(0,0,0)
                if ( pixel.equals( shapeColor ) ) 
                {
                    if( y1==99 )
                    {
                    	y1=y;
                    	y2=y;
                    }
                    if( y > ( y2 + 1 ) ) 
                    {
                        r = new Rectangle( x, y1, 1, y2-y1 );
                        area.add( new Area( r ) ); 
                        y1 = y; 
                        y2 = y;
                    }
                    
                    y2= y;
                }               
            }
            if( ( y2 - y1 ) >= 0 )
            {
                r = new Rectangle( x, y1, 1, y2 - y1 );
                area.add( new Area( r ) ); 
            }
        }

        return area;
    }

    public Area getArea_FastOutline( Color shapeColor ) 
    {
        //Assumes Black as Shape Color
        if( this.image == null)
        {
        	return null;
        }
        
        Area area = new Area();
        
        int w = this.image.getWidth();
        int h = this.image.getHeight();
        int[][] matrixBinary = new int[ w ][ h ];

        for( int x = 0; x < w; x++ )
        {
        	for( int y = 0; y < h; y++ )
            {
        		if( shapeColor.equals( new Color( this.image.getRGB( x, y ) ) ) )
        		{
        			matrixBinary[ x ][ y ] = 1;
        		}
            }	
        }
       
        Point p = detectingStartPoint( shapeColor );
        
        area = this.getArea_FastOutlineAUX( matrixBinary, p.x, p.y, w, h );
        
        return area;
    }
    
    private Area getArea_FastOutlineAUX( int[][] matrix, int iniX, int iniY, int endX, int endY )
    {
    	Area a = new Area();
    	
    	if( endX - iniX < 2 || endY - iniY < 2 )
    	{
    		for( int i = iniX;  i < endX; i++ )
	    	{
	    		for( int j = iniY; j < endY; j++ )
	    		{
	    			if( matrix[ i ][ j ] == 1 )
	    			{
	    				a.add( new Area( new Rectangle( i, j, 1, 1 ) ) ); 
	    			}
	    		}
	    	}
    	}
    	else
    	{
    		int cont0 = 0;
    		
	    	for( int i = iniX;  i < endX; i++ )
	    	{
	    		for( int j = iniY; j < endY; j++ )
	    		{
	    			cont0++;
	    			if( matrix[ i ][ j ] == 1 )
	    			{
	    				cont0--;
	    			}
	    		}
	    	}
	    	
	    	if( cont0 == 0 )
	    	{
	    		a = new Area( new Rectangle( iniX, iniY, endX - iniX, endY - iniY ) ); 
	    	}
	    	else if( cont0 < (endX - iniX ) * ( endY - iniY) )
	    	{	
	    		int w = ( endX - iniX ) / 2;
	    		int h = ( endY - iniY ) / 2;
	    		Area a1 = this.getArea_FastOutlineAUX( matrix, iniX, iniY, iniX + w, iniY + h );
	    		Area a2 = this.getArea_FastOutlineAUX( matrix, iniX, iniY + h, iniX + w, endY  );
	    		Area a3 = this.getArea_FastOutlineAUX( matrix, iniX + w, iniY, endX, iniY + h );
	    		Area a4 = this.getArea_FastOutlineAUX( matrix, iniX + w, iniY + h, endX, endY );
	    		
	    		if( !a1.isEmpty() )
	    		{
	    			a.add( a1 );
	    		}
	    		
	    		if( !a2.isEmpty() )
	    		{
	    			a.add( a2 );
	    		}
	    		
	    		if( !a3.isEmpty() )
	    		{
	    			a.add( a3 );
	    		}
	    		
	    		if( !a4.isEmpty() )
	    		{
	    			a.add( a4 );
	    		}
	    	}
    	}
    	
    	return a;
    }
    
    public Area getArea_Fast( Color shapeColor) 
    {
        //Assumes Black as Shape Color
        if( this.image == null)
        {
        	return null;
        }

        Area area = new Area();
        Rectangle r;
        boolean enc = false;
                
        Point startPoint = this.detectingStartPoint( shapeColor );
        
        for (int x = startPoint.x ; x < this.image.getWidth(); x++ )
        {
            for (int y = startPoint.y; y < this.image.getHeight(); y++ ) 
            {
            	enc = shapeColor.equals( new Color( this.image.getRGB( x, y ) ) );
            	
            	if( enc )
            	{
	            	int incr = y;
	            	for( int iy = y + 1; iy < this.image.getHeight() && enc; iy++ )
	            	{
	            		enc = shapeColor.equals( new Color( this.image.getRGB( x, iy ) ) );
	            		if( enc )
	            		{
	            			incr++;
	            		}
	            	}
	            	
	            	if( incr >= this.image.getHeight() )
	            	{
	                    r = new Rectangle( x, y, 1, this.image.getHeight() - y  ); 
	            	}
	            	else
	            	{
	            		r = new Rectangle( x, y, 1, incr - y  ); 
	            	}
	            	
	            	area.add( new Area( r ) );
	            	y = incr + 1;
            	}
            }
        }
                
        return area;
    }
    
    public static boolean isIncluded(Color target, Color pixel, int tolerance) 
    {
        int rT = target.getRed();
        int gT = target.getGreen();
        int bT = target.getBlue();
        int rP = pixel.getRed();
        int gP = pixel.getGreen();
        int bP = pixel.getBlue();
        
        return (
        		(rP - tolerance <= rT) && (rT <= rP + tolerance) &&
        		(gP - tolerance <= gT) && (gT <= gP + tolerance) &&
        		(bP - tolerance <= bT) && (bT <= bP + tolerance) 
        	   );
    }

    
    private Point detectingStartPoint( Color shapeColor )
    {
    	Point p = new Point();
    	
    	boolean enc = false;
    	for( int y = 0; y < this.image.getHeight() && !enc; y++ )
        {
        	for( int x = 0; x  < this.image.getWidth() && !enc; x++)
        	{
        		enc = shapeColor.equals( new Color( this.image.getRGB( x, y ) ) );
        		if( enc )
        		{
        			p.y = y;
        		}
        	}
        }
        
        enc = false;
        for( int x = 0; x < this.image.getWidth() && !enc; x++ )
        {
        	for( int y = 0; y  < this.image.getHeight() && !enc; y++)
        	{
        		enc = shapeColor.equals( new Color( this.image.getRGB( x, y ) ) );
        		if( enc )
        		{
        			p.x = x;
        		}
        	}
        }
        
        return p;
    }
   
}