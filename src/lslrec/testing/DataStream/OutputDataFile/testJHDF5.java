package testing.DataStream.OutputDataFile;

import java.util.Random;

import org.apache.commons.lang3.ArrayUtils;

import Auxiliar.extra.ConvertTo;
import ch.systemsx.cisd.base.mdarray.MDFloatArray;
import ch.systemsx.cisd.base.mdarray.MDIntArray;
import ch.systemsx.cisd.hdf5.HDF5ArrayBlockParamsBuilder;
import ch.systemsx.cisd.hdf5.HDF5DataSet;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.HDF5MDDataBlock;
import ch.systemsx.cisd.hdf5.IHDF5Reader;
import ch.systemsx.cisd.hdf5.IHDF5Writer;
import ch.systemsx.cisd.hdf5.MatrixUtils;

public class testJHDF5 
{
	public static void main(String[] args)
    {
        Random rng = new Random();
        MDIntArray mydata = new MDIntArray(MatrixUtils.dims(10, 10));

        // Write the integer matrix.
        try (IHDF5Writer writer = HDF5Factory.open("largeimatrix.h5"))
        {
        	writer.int32().createMatrix( "mydata", 1, 1, 1, 1 );
            // Define the block size as 10 x 10.
        	/*
            try (HDF5DataSet dataSet = writer.int32().createMDArrayAndOpen("mydata", MatrixUtils.dims(1, 1)))
            {
                // Write 5 x 7 blocks.
                for (int bx = 0; bx < 5; ++bx)
                {
                    for (int by = 0; by < 1; ++by)
                    {
                        fillMatrix( bx * 10, mydata);
                        writer.int32().writeMDArray(dataSet, mydata,HDF5ArrayBlockParamsBuilder.blockIndex(bx, 0));
                    }
                }
                
                mydata = new MDIntArray(MatrixUtils.dims(1, 10));
                fillMatrix( 50, mydata );
                writer.int32().writeMDArray(dataSet, mydata, HDF5ArrayBlockParamsBuilder.blockIndex(5, 0));
            }
            */
        	
        	
        	// Write 5 x 7 blocks.
        	int c = 0;
            for (int bx = 0; bx < 5; ++bx)
            {
                for (int by = 0; by < 1; ++by)
                {
                	int[][] data = new int[rng.nextInt( 9 ) + 1][10];
                	System.out.println("testJHDF5.main() " + data.length );
                    fillMatrix( c * 10, data);
                    writer.int32().writeMatrixBlockWithOffset( "mydata", data, c, 0 );
                    c += data.length;
                }
            }
            
            int[][] data = new int[1][10];
            fillMatrix( 50, data );
            writer.int32().writeMatrixBlockWithOffset( "mydata", data, c , 0 );
        }

        // Read the matrix in again, using the "natural" 10 x 10 blocks.
        /*
        try (IHDF5Reader reader = HDF5Factory.openForReading("largeimatrix.h5"))
        {
            for (HDF5MDDataBlock<MDIntArray> block : reader.int32().getMDArrayNaturalBlocks("mydata"))
            {
                System.out.println(ArrayUtils.toString(block.getIndex()) + " -> "
                        + block.getData().toString());
            }
    
            // Read a 1d sliced block of size 10 where the first index is fixed
            System.out.println(reader.int32().readMDArray("mydata", HDF5ArrayBlockParamsBuilder.slice(30, -1).block(10).index(4)));
        }
        */
    }

    static void fillMatrix(int val, MDIntArray mydata)
    {
        for (int i = 0; i < mydata.size(0); ++i)
        {
            for (int j = 0; j < mydata.size(1); ++j)
            {
                mydata.set(val + i, i, j);
            }
        }
    }
    
    static void fillMatrix(int val, int[][] mydata)
    {
        for (int i = 0; i < mydata.length; ++i)
        {
            for (int j = 0; j < mydata[0].length; ++j)
            {
                mydata[i][j] = val + i;
            }
        }
    }
    
	/*
	public static void main( String[] args )
	{
		String file = "test.h5";
		IHDF5Writer w =  HDF5Factory.open( file );
		
		String name = "t";
		w.float32().createMDArrayAndOpen( name, MatrixUtils.dims( 2, 4) );
		
		MDFloatArray mydata = new MDFloatArray( MatrixUtils.dims(2, 4));
		writer.int32().writeMDArray(dataSet, mydata, blockIndex(bx, by));
		w.float32().writeMatrixBlockWithOffset( name
				, new float[][] { {1,1,1,1 }, { 1,1,1,1} }
				, 0
				, 0 );
		
		w.float32().writeMatrixBlockWithOffset( name
				, new float[][] { {2,2,2,2 }, { 2,2,2,2} }
				, 0
				, 2 );
		
		w.float32().writeMatrixBlockWithOffset( name
				, new float[][] { {3,3,3,3 } }
				, 0
				, 3 );
	}
	
	private void fillArray( MDFloatArray mydata, float val )
	{
		 for (int i = 0; i < mydata.size(0); ++i)
	        {
	            for (int j = 0; j < mydata.size(1); ++j)
	            {
	                mydata.set( val, i, j);
	            }
	        }
	}
	*/
}
