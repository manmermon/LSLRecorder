package lslrec.testing.DataStream.OutputDataFile;

import java.util.Random;

import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlock;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlockFactory;
import lslrec.dataStream.outputDataFile.format.hdf5.OutputHDF5DataWriter;
import lslrec.dataStream.outputDataFile.format.matlab.OutputMatDataWriter;


public class testHDF5 {

	public static void main(String[] args) 
	{
		OutputHDF5DataWriter dw = null;
		OutputMatDataWriter mw = null;
		
		Random rng = new Random( 12345L );
		
		try
		{
			dw = new OutputHDF5DataWriter( "test.h5", null );
			mw = new OutputMatDataWriter( "test.mat", null );

			for( int ifile = 0; ifile < 2; ifile++ )
			{
				String name = "var" + ifile;
				int nCols = 10;
				int total = 0;
				
				Integer seqNum = 0;
				
				for( ; seqNum < 10; seqNum++ )
				{
					Number[] data = new Number[ nCols * rng.nextInt( 10 ) + nCols ];//+ rng.nextInt( 10 ) ];
					for( int i = 0; i < data.length; i++ )
					{
						data[ i ] = seqNum.floatValue();
					}

					DataBlock b = DataBlockFactory.getDataBlock( StreamDataType.float32, seqNum, name, nCols, data );

					dw.saveData( b );
					mw.saveData( b );

					total += data.length;
					System.out.println("testHDF5.main() " + total );
				}

				Number[] data = new Number[ nCols ];
				for( int i = 0; i < data.length; i++ )
				{
					data[ i ] = seqNum.floatValue();
				}

				DataBlock b = DataBlockFactory.getDataBlock( StreamDataType.float32, seqNum, name, nCols, data );

				dw.saveData( b );
				mw.saveData( b );
				total += data.length;
				System.out.println("testHDF5.main() " + total );

				System.out.println("testHDF5.main() "+ name + " " + total );
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally
		{
			if( dw != null )
			{
				try 
				{
					dw.close();
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}

			if( mw != null )
			{
				try 
				{
					mw.close();
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			}
		}

	}

}
