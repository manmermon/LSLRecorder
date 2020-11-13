package lslrec.testing.Compression;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;
import org.apache.commons.compress.utils.ByteUtils;

public class testCompressionApache 
{
	private static int bufferSize = 100;

	private static void compress( ByteArrayInputStream byteArrayInputStream, CompressorOutputStream compressorOutputStream) throws IOException 
	{
	    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
	    final byte[] buffer = new byte[ bufferSize ];
	    int n = 0;
	    while (-1 != (n = byteArrayInputStream.read(buffer)))
	    {
	        compressorOutputStream.write(buffer, 0, n);
	    }
	}
	
	private static void uncompress(CompressorInputStream compressorInputStream, ByteArrayOutputStream byteArrayOutputStream) throws IOException 
	{
	    final byte[] buffer = new byte[bufferSize];
	    int n = 0;
	    while (-1 != (n = compressorInputStream.read( buffer))) 
	    {
	        byteArrayOutputStream.write(buffer, 0, n);
	    }
	    
	    compressorInputStream.close();
	    byteArrayOutputStream.close();
	}
	
	static String compID = CompressorStreamFactory.BZIP2;

	public static byte[] compressBZIP2(byte[] inputBytes) throws Exception 
	{
	    ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream( inputBytes );
	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    CompressorOutputStream bZip2CompressorOutputStream = new CompressorStreamFactory().createCompressorOutputStream( compID,  byteArrayOutputStream);
	    bZip2CompressorOutputStream.write( inputBytes );
	    //compress(byteArrayInputStream, bZip2CompressorOutputStream);
	    bZip2CompressorOutputStream.close();
	    return byteArrayOutputStream.toByteArray();
	}
	
	public static byte[] bunzip2( byte[] input ) throws Exception 
	{
	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    ByteArrayInputStream inputStream = new ByteArrayInputStream( input );
	    CompressorInputStream bZip2CompressorInputStream = new CompressorStreamFactory().createCompressorInputStream( compID, inputStream ); // BZip2CompressorInputStream(  inputStream );
	    uncompress(bZip2CompressorInputStream, byteArrayOutputStream);
	    return byteArrayOutputStream.toByteArray();
	}
	
	public static void main(String[] args) 
	{
		try 
		{
			byte[] vals = new byte[ bufferSize ];
			
			for( int i = 1; i < vals.length; i++ )
			{
				vals[ i ] = (byte)( vals[ i - 1 ] + 1 );
			}
			
			byte[] zip = compressBZIP2( vals );
			
			System.out.println("testCompressionApache.main() " + Arrays.toString( vals ) );
			System.out.println("testCompressionApache.main() " + zip.length  + Arrays.toString( zip  ) );
			System.out.println("testCompressionApache.main() " + Arrays.toString( bunzip2( zip ) ) );
		}
		catch ( Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}
