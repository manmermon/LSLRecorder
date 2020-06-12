/*
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.
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

package ImportClisData.Java.Compress.Zip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPInputStream;

import org.apache.commons.compress.utils.IOUtils;

import ImportClisData.Java.Compress.UnzipDataFactory;
import ImportClisData.Java.Compress.UnzipDataTemplate;

public class UnzipGZipData extends UnzipDataTemplate
{
	@Override
	protected byte[] uncompressData( byte[] data ) throws Exception
	{		
		ByteArrayInputStream arInStream = new ByteArrayInputStream( data ); 
		GZIPInputStream gzip = new GZIPInputStream( arInStream );
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		IOUtils.copy( gzip,  buffer );
		gzip.close();
		
		byte[] uncompressData = buffer.toByteArray();
		
		arInStream.close();
		
		return uncompressData;
	}

	@Override
	public String getUnzipID() 
	{
		return UnzipDataFactory.GZIP_ID;
	}

}
