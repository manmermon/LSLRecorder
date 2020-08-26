package dataStream.binary.reader;

import java.io.BufferedInputStream;
import java.util.ArrayList;
import java.util.List;

import auxiliar.extra.ConvertTo;
import dataStream.binary.BinaryDataFormat;
import dataStream.outputDataFile.dataBlock.ByteBlock;

public class ReaderBinayData 
{
	public static List< ByteBlock > readDataFromBinaryFile( BufferedInputStream binStream
															, List< BinaryDataFormat > dataFormats  ) throws Exception
	{
		List< ByteBlock > out = new ArrayList< ByteBlock >();
		
		if( binStream != null && dataFormats != null )
		{
			for( BinaryDataFormat dataFormat : dataFormats )
			{
				for( ByteBlock bb : readDataFromBinaryFile( binStream, dataFormat ) )
				{
					out.add( bb );
				}
			}
		}
				
		return out;
	}
	
	public static List< ByteBlock > readDataFromBinaryFile( BufferedInputStream binStream
															, BinaryDataFormat dataFormat  ) throws Exception
	{
		List< ByteBlock > out = new ArrayList< ByteBlock >();

		if( dataFormat.getChunckSize() != BinaryDataFormat.UNKNOW_CHUCKSIZE )
		{
			ByteBlock bb = readDataFromBinaryFileAux( binStream, dataFormat );
			if( bb != null )
			{
				out.add( bb );
			}
		}
		else
		{
			ByteBlock bb = readDataFromBinaryFileAux( binStream, dataFormat.getLengthFormat() );
			
			if( bb != null )
			{
				Number[] lens = ConvertTo.ByteArrayTo( bb.getData(), dataFormat.getLengthFormat().getDataType() );
				
				long L = 0;
				for( Number l : lens )
				{
					L += l.longValue();
				}
				
				BinaryDataFormat bdf = new BinaryDataFormat( dataFormat.getDataType(), dataFormat.getDataByteSize(), L );
				ByteBlock dbb = readDataFromBinaryFileAux( binStream, bdf );
				
				if( dbb != null )
				{
					out.add( bb );
					out.add( dbb );
				}
			}
		}

		return out;
	}

	private static ByteBlock readDataFromBinaryFileAux( BufferedInputStream binStream
														, BinaryDataFormat dataFormat  ) throws Exception
	{
		ByteBlock out = null;

		List< Byte[] > data = new ArrayList< Byte[] >();

		byte[] aux = new byte[ dataFormat.getDataByteSize() ];

		int count = 0;

		while( count < dataFormat.getChunckSize()
				&& binStream.read( aux ) > 0 )
		{
			data.add( ConvertTo.byteArray2ByteArray( aux ) );				

			count++;
		}


		if( count > 0 )
		{
			Byte[] d = new Byte[ data.size() * dataFormat.getDataByteSize() ];

			int index = 0;

			for( int i = 0; i < count; i++ )
			{
				Byte[] aux2 = data.get( i );

				for( int j = 0; j < aux2.length && index < d.length; j++ )
				{
					d[ index ] = aux2[ j ];

					index++;
				}
			}


			out = new ByteBlock( 0, "", dataFormat.getChunckSize(), d );
		}

		return out;
	}
}
