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

package dataStream.outputDataFile.format;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import dataStream.outputDataFile.compress.OutputZipDataFactory;

public class OutputFileFormatParameters 
{
	//private final String HEARDER_SIZE = "HEADER_SIZE";
	private final String ZIP_ID = "ZIP_ID";
	private final String CHAR_CODING = "CHAR_CODING";
	private final String ENCRYPT_KEY = "ENCRYPT_KEY";
	private final String BLOCK_DATA_SIZE = "BLOCK_DATA_SIZE";
	private final String NUM_BLOCKS= "NUM_BLOCKS";
	private final String DATA_INFO = "DATA_INFO";
	private final String DATA_NAMES = "DATA_NAME";
	private final String CHANNELS = "CHANNELS";
	private final String ENCRYPT_ID = "ENCRYPT_ID";
	
	private Map< String, Object > pars = new HashMap< String, Object >();
	
	public OutputFileFormatParameters() 
	{
		this.setCompressType( OutputZipDataFactory.UNDEFINED );
		this.setCharset( Charset.forName( "UTF-8" )  );
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
	
	public Integer getCompressType( )
	{
		Integer zp = null;
		
		Object z = this.pars.get( this.ZIP_ID );
		if( z != null )
		{
			zp = (Integer)z;
		}
		
		return zp;
	}
	
	public Charset getCharset( )
	{
		Charset cs = null;
		
		Object c = this.pars.get( this.CHAR_CODING );
		if( c != null )
		{
			cs = (Charset)c;
		}
		
		return cs;
	}

	public void setEncryptKey( String key )
	{
		if( key != null )
		{
			this.pars.put( this.ENCRYPT_KEY, key );
		}
	}
	
	public String getEncryptKey()
	{
		String key = null;
		
		Object k = this.pars.get( this.ENCRYPT_KEY );
		
		if( k != null )
		{
			key = k.toString();
		}
		
		return key;
	}

	public void setBlockDataLength( int len )
	{
		this.pars.put( this.BLOCK_DATA_SIZE, len );
	}
	
	public Integer getBlockDataLength( )
	{
		Integer len = null;
		
		Object size = this.pars.get( this.BLOCK_DATA_SIZE );
		if( size != null ) 
		{
			len = (Integer)size;
		}
		return len;
	}
	
	public void setNumerOfBlock( long num )
	{
		this.pars.put( this.NUM_BLOCKS, num );
	}
	
	public Long getNumerOfBlocks( )
	{
		Long val = null;
		
		Object num = this.pars.get( this.NUM_BLOCKS );
		if( num != null )
		{
			val = (Long) num;
		}
		
		return val;
	}
	
	public void setDataInfo( String xml )
	{
		if( xml != null )
		{
			this.pars.put( this.DATA_INFO,  xml );
		}
	}
	
	public String getDataInfo( )
	{
		String res = null;
		
		Object info = this.pars.get( this.DATA_INFO );
		if( info != null )
		{
			res = info.toString();
		}
		
		return res;
	}
	
	public void setDataNames( String names )
	{
		if( names != null )
		{
			this.pars.put( this.DATA_NAMES, names );
		}
	}
	
	public String getDataNames( )
	{
		String names = "";
		
		Object n = this.pars.get( this.DATA_NAMES );
		if( n != null )
		{
			names = n.toString();
		}
				
		return names;
	}
	
	public void setChannels( int ch )
	{
		this.pars.put( this.CHANNELS, ch );
	}
	
	public Integer getChannels()
	{
		Integer ch = null;
		
		Object c = this.pars.get( this.CHANNELS );
		
		if( c != null )
		{
			ch = (Integer)c;
		}
		
		return ch;
	}
	
	public void setEncryptID( String encryptID )
	{
		this.pars.put( this.ENCRYPT_ID, encryptID );
	}
	
	public String getEncryptID( )
	{
		String id = null;
		
		Object e = this.pars.get( this.ENCRYPT_ID );
		
		if( e != null )
		{
			id = e.toString();
		}
		
		return id;
	}
}
