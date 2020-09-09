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

package lslrec.dataStream;

public class StreamHeader
{	
	public static final String HEADER_BINARY_SEPARATOR = ";"; 
	public static final char HEADER_END = '\n';
	
	private String filePath;
	private String name;
	private int type;
	private int timeType;
	private int strLenType;
	private int nChs;
	private int chunckSize;
	private boolean interleave = false;
	private String xml;
	private String outFormat;
	private String outFolder;
	private boolean deleteBin = false;
	
	private String encryptKey = null;
	
	public StreamHeader( String path, String streamName
							, int dataType, int timeType
							, int strLenType
							, int channels, int chunksize
							, boolean interLeave, String desc
							, String outputFormat, String outputFolder
							, boolean delBin ) 
	{
		this.filePath = path;
		this.name = streamName;
		this.type = dataType;
		this.timeType = timeType;
		this.strLenType = strLenType;
		this.nChs = channels;
		this.chunckSize = chunksize;
		this.interleave = interLeave;
		this.xml = desc;
		this.outFormat = outputFormat;
		this.outFolder = outputFolder;
		this.deleteBin = delBin;
	}
	
	public String getFilePath( )
	{
		return this.filePath;
	}
	
	public void setFilePath( String file )
	{
		this.filePath = file;
	}
	
	public String getName( )
	{
		return this.name;
	}
	
	public void setName( String nam )
	{
		this.name = nam;
	}
	
	public int getType( ) 
	{
		return this.type;
	}
	
	public int getTimeType() 
	{
		return this.timeType;
	}
	
	public int getNumberOfChannels( )
	{
		return this.nChs;
	}
	
	public int getChunckSize() 
	{
		return this.chunckSize;
	}
	
	public boolean isInterleave()
	{
		return this.interleave;
	}
	
	public void setInterleave( boolean inter )
	{
		this.interleave = inter;
	}
	
	public String getXMLDescription( )
	{
		return this.xml;
	}
	
	public String getOutputFormat( )
	{
		return this.outFormat;
	}
	
	public void setOutputFormat( String format )
	{
		this.outFormat = format;
	}
	
	public String getOutputFolder( )
	{
		return this.outFolder;
	}
			
	public void setOutputFolder( String folder )
	{
		this.outFolder = folder;
	}
	
	public boolean deleteBinary()
	{
		return this.deleteBin;
	}
	
	public void setDeleteBinary( boolean del )
	{
		this.deleteBin = del;
	}
	
	public int getStringLengthType() 
	{
		return this.strLenType;
	}
	
	public void setStringLengthType( int type ) 
	{
		this.strLenType = type;
	}
	
	public String getStreamBinHeader()
	{
		String binHeader = this.name + HEADER_BINARY_SEPARATOR
							+ this.type + HEADER_BINARY_SEPARATOR
							+ this.nChs + HEADER_BINARY_SEPARATOR
							+ this.chunckSize + HEADER_BINARY_SEPARATOR
							+ this.timeType + HEADER_BINARY_SEPARATOR
							+ this.strLenType + HEADER_BINARY_SEPARATOR
							+ this.interleave + HEADER_BINARY_SEPARATOR
							+ this.xml;

		binHeader = binHeader.trim().replace( "\r", "" ).replace( "\n", "" ) + HEADER_END;
		
		return binHeader;
	}
	
	public void setEncryptKey( String key )
	{
		this.encryptKey = key; 
	}
	
	public String getEncryptKey()
	{
		return this.encryptKey;
	}
}