package lslrec.dataStream.outputDataFile.compress.zip;

import java.io.ByteArrayOutputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import lslrec.dataStream.outputDataFile.compress.OutZipDataTemplate;

public class OutputBZip2Data extends OutZipDataTemplate 
{
	@Override
	public String getID() 
	{	
		return "BZIP2";
	}

	@Override
	protected byte[] compressData( byte[] data ) throws Exception 
	{		
	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( data.length );	    
	    BZip2CompressorOutputStream bZip2 = new BZip2CompressorOutputStream( byteArrayOutputStream );
	    
	    bZip2.write( data );
	    	    
	    bZip2.close();
	    byteArrayOutputStream.close();
	    
	    byte[] compressData = byteArrayOutputStream.toByteArray();
		
		return compressData;
	}
}
