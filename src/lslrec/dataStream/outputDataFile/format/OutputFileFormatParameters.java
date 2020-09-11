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

package lslrec.dataStream.outputDataFile.format;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import lslrec.config.ConfigApp;
import lslrec.dataStream.outputDataFile.compress.OutputZipDataFactory;

public class OutputFileFormatParameters
{
	private final String OUT_FILE_NAME = "OUT_FILE_NAME";
	private final String ZIP_ID = "ZIP_ID";
	private final String CHAR_CODING = "CHAR_CODING";
	private final String ENCRYPT_KEY = "ENCRYPT_KEY";	
	private final String PARALLELIZE = "PARALLELIZE";
	private final String OUT_FILE_FORMAT = "OUT_FILE_FORMAT";
	private final String NUM_BLOCKS= "NUM_BLOCKS";	
	private final String BLOCK_DATA_SIZE = "BLOCK_DATA_SIZE";
	private final String DATA_NAMES = "DATA_NAME";
	private final String RECORDING_INFO = "RECORDING_INFO";
	private final String DELETE_BIN = "DELETE_BIN";
	
	private Map< String, Object > pars = new HashMap< String, Object >();
	
	public OutputFileFormatParameters() 
	{
		this.setCompressType( OutputZipDataFactory.GZIP );
		this.setOutputFileFormat( DataFileFormat.CLIS );
		this.setCharset( Charset.forName( "UTF-8" )  );
		this.setDeleteBin( !ConfigApp.isTesting() );
		this.setOutputFileName( "./data" + DataFileFormat.getSupportedFileExtension().get( DataFileFormat.CLIS ) );
	}
	
	public OutputFileFormatParameters clone() 
	{
		OutputFileFormatParameters clon = new OutputFileFormatParameters();
		
		Object val = this.getBlockDataLength();
		if( val != null )
		{
			clon.setBlockDataLength( (Integer)val );
		}
		
		val = this.getCharset();
		if( val != null )
		{
			clon.setCharset( (Charset)this.getCharset() );
		}
		
		val = this.getCompressType();
		if( val != null )
		{
			clon.setCompressType( val.toString() );
		}
		
		val = this.getEncryptKey();		
		if( val != null )
		{
			clon.setEncryptKey( val.toString() );
		}
		
		val = this.getNumerOfBlocks();
		if( val != null )
		{
			clon.setNumerOfBlock( (Integer)val );
		}
		
		val = this.getOutputFileFormat();
		if( val != null )
		{
			clon.setOutputFileFormat( val.toString() );
		}
		
		val = this.getOutputFileName();
		if( val != null )
		{
			clon.setOutputFileName(  val.toString() );
		}
		
		val = this.isParallelize();
		if( val != null )
		{
			clon.setParallelize( (Boolean)val );
		}
		
		val = this.getDataNames();
		if( val != null )
		{
			clon.setDataNames( val.toString() );
		}
		
		val = this.getRecordingInfo();
		if( val != null )
		{
			Map< String, String > aux = (Map< String, String >)val;
			for( String id : aux.keySet() )
			{
				clon.addRecordingInfo( id, aux.get( id ) );
			}
		}
		
		return clon;
	}
	
	public void setCompressType( String type )
	{
		this.pars.put( this.ZIP_ID, type );
	}
	
	public void setOutputFileFormat( String format )
	{
		this.pars.put( this.OUT_FILE_FORMAT, format );
	}
	
	public void setCharset( Charset coding )
	{
		if( coding != null )
		{
			this.pars.put( this.CHAR_CODING, coding );
		}
	}
	
	public String getOutputFileFormat()
	{
		String format = null;
		
		Object z = this.pars.get( this.OUT_FILE_FORMAT );
		if( z != null )
		{
			format = z.toString();
		}
		
		return format;
	}
	
	public String getCompressType( )
	{
		String zp = null;
		
		Object z = this.pars.get( this.ZIP_ID );
		if( z != null )
		{
			zp = z.toString();
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
	
	public void setOutputFileName( String file )
	{
		if( file != null )
		{
			this.pars.put( this.OUT_FILE_NAME, file );
		}
	}
	
	public String getOutputFileName( )
	{
		String file = null;
		
		Object f = this.pars.get( this.OUT_FILE_NAME );
		if( f != null )
		{
			file = f.toString();
		}
		
		return file;
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
	
	public void addRecordingInfo( String id, String content )
	{
		if(id != null && content != null )
		{
			Map< String, String > info = this.getRecordingInfo();
			
			if( info == null )
			{
				info = new HashMap< String, String >();
				
				this.pars.put( this.RECORDING_INFO, info );
			}
			
			info.put( id, content );
		}
	}
	
	public Map< String, String > getRecordingInfo( )
	{
		Map< String, String > res = null;
		
		Object info = this.pars.get( this.RECORDING_INFO );
		if( info != null )
		{
			res = (Map< String, String >)info;
		}
		
		return res;
	}
	
	/*
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
	*/
	
	public void setParallelize( boolean parallel )
	{
		this.pars.put( this.PARALLELIZE , parallel );
	}
	
	public boolean isParallelize()
	{
		boolean parallel = false;
		
		Object e = this.pars.get( this.PARALLELIZE );
		
		if( e != null )
		{
			parallel= (Boolean) e;
		}
		
		return parallel;
	}
	
	public void setDeleteBin( boolean del )
	{
		this.pars.put( this.DELETE_BIN, del );
	}
	
	public boolean getDeleteBin()
	{
		boolean del = false;
		
		Object e = this.pars.get( this.DELETE_BIN );
		
		if( e != null )
		{
			del= (Boolean) e;
		}
		
		return del;
	}
}
