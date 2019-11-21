/*
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
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
package InputStreamReader.OutputDataFile.Format;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import InputStreamReader.OutputDataFile.Compress.IOutZip;
import InputStreamReader.OutputDataFile.Compress.OutputZipDataFactory;

public class OutputFileFormatParameters 
{
	private final String HEARDER_SIZE = "HEADER_SIZE";
	private final String ZIP_ID = "ZIP_ID";
	private final String CHAR_CODING = "CHAR_CODING";
	
	private Map< String, Object > pars = new HashMap< String, Object >();
	
	public OutputFileFormatParameters() 
	{
		this.setHeaderSize( 0 );
		this.setCompressType( OutputZipDataFactory.UNDEFINED );
		this.setCharset( Charset.forName( "UTF-8" )  );
	}
	
	public void setHeaderSize( long size )
	{
		this.pars.put( this.HEARDER_SIZE, size );
	}
	
	public void setCompressType( int type )
	{
		this.pars.put( this.ZIP_ID, type );
	}
	
	public void setCharset( Charset coding )
	{
		if( coding != null )
		{
			this.pars.put( this.CHAR_CODING, coding );
		}
	}
	
	public long getHeaderSize( )
	{
		return (long)this.pars.get( this.HEARDER_SIZE );
	}
	
	public int getCompressType( )
	{
		return (int)this.pars.get( this.ZIP_ID );
	}
	
	public Charset getCharset( )
	{
		return (Charset)this.pars.get( this.CHAR_CODING );
	}
}
