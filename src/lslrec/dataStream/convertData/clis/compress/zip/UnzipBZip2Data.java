package lslrec.dataStream.convertData.clis.compress.zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import lslrec.dataStream.convertData.clis.compress.UnzipDataFactory;
import lslrec.dataStream.convertData.clis.compress.UnzipDataTemplate;

public class UnzipBZip2Data extends UnzipDataTemplate 
{
	@Override
	public String getUnzipID() 
	{	
		return UnzipDataFactory.BZIP2_ID;
	}

	@Override
	protected byte[] uncompressData( byte[] data ) throws Exception 
	{	
		ByteArrayInputStream arInStream = new ByteArrayInputStream( data );
		BZip2CompressorInputStream bzip2 = new BZip2CompressorInputStream( arInStream );
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		
		IOUtils.copy( bzip2, buffer );
		
	    bzip2.close();
	    buffer.close();
	    		
	    byte[] uncompressData = buffer.toByteArray();
		
		return uncompressData;
	}
}
