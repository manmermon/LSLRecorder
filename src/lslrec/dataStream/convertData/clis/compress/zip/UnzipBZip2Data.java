/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
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
