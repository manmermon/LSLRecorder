package lslrec.testing.Auxiliar;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import lslrec.auxiliar.extra.Data2ChartImage;

public class testData2Graphic {

	public static void main(String[] args) 
	{
		int rows = 15;
		int cols = 10;
		
		double[][] d = new double[ rows ][ cols ];
		List< double[] > cat = new ArrayList<double[]>();
		
		for( int r = 0; r < rows; r++)
		{
			for( int c = 0; c < cols; c++ )
			{
				double v = Math.random();
				
				d[r][ c]  = v;
			}
			
			cat.add( d[ r ] );
		}
		
		Image img1 = Data2ChartImage.drawLineSerie( d, 1/5D, 0, new Dimension( 640, 480 ), "test 1", "tiempo", "magnitud" );
		Image img2 = Data2ChartImage.drawCategoryiesBoxplot( cat, new Dimension( 640, 480 ), "test 1", "tiempo", "magnitud" );
		
		try {
		    // retrieve image
		    File outputfile1 = new File("test1.png");
		    File outputfile2 = new File("test2.png");
		    ImageIO.write((BufferedImage)img1, "png", outputfile1);
		    ImageIO.write((BufferedImage)img2, "png", outputfile2);
		} catch (IOException e) 
		{
			e.printStackTrace();
		}
		
	}

}
