/*
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
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

package lslrec.dataStream.outputDataFile.compress.zip;

import java.io.ByteArrayOutputStream;
import java.util.zip.GZIPOutputStream;

import lslrec.dataStream.outputDataFile.compress.ZipDataTemplate;

public class GZipData extends ZipDataTemplate 
{
	@Override
	protected byte[] compressData( byte[] data ) throws Exception
	{		
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream( data.length );
		GZIPOutputStream zipStream = new GZIPOutputStream( byteStream );

		zipStream.write( data );
		zipStream.close();
		byteStream.close();

		byte[] compressData = byteStream.toByteArray();
		
		return compressData;
	}

	@Override
	public String getID() 
	{
		return "GZIP";
	}

}
