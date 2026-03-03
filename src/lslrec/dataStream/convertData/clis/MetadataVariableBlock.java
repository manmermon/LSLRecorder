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
package lslrec.dataStream.convertData.clis;

import java.util.ArrayList;
import java.util.List;

import lslrec.dataStream.tools.StreamUtils.StreamDataType;

public class MetadataVariableBlock
{	
	private int cols = 1;
	private StreamDataType dataType = StreamDataType.float32;
	private String name = "var";
	private List< Integer > blockDataSize = new ArrayList< Integer >();
	
	public MetadataVariableBlock( String var ) throws ClisMetadataException 
	{
		this.processVarBlock( var );
	}
	
	private void processVarBlock( String var ) throws ClisMetadataException
	{
		try
		{
			String[] parts = var.split( "," );
			
			this.name = parts[ 0 ];
			
			this.setDataTye( parts[ 1 ], Integer.valueOf( parts[ 2 ] ) );
			
			this.cols = Integer.valueOf( parts[ 3 ] );
			
			for( int i = 4; i < parts.length; i++ )
			{
				this.blockDataSize.add( Integer.valueOf( parts[ i ] ) );
			}
			
		}
		catch ( IndexOutOfBoundsException e) 
		{
			throw new ClisMetadataException( "Variable field length must be greater o equal than 5." ); 
		}	
		catch ( NumberFormatException e) 
		{
			throw new ClisMetadataException( "Variable fields are not a integer values." );
		}
		
	}
	
	private void setDataTye( String baseTye, int bytes ) throws ClisMetadataException
	{
		boolean err = false;
		
		if( baseTye.toLowerCase().equals( "int" ) )
		{
			switch ( bytes ) 
			{
				case 1:
				{
					this.dataType = StreamDataType.int8;
					break;
				}
				case 2:
				{
					this.dataType = StreamDataType.int16;
					break;
				}
				case 4:
				{
					this.dataType = StreamDataType.int32;
					break;
				}
				case 8:
				{
					this.dataType = StreamDataType.int64;
					break;
				}
				default:
				{
					err = true;
				}						
			}
		}
		else if( baseTye.toLowerCase().equals( "float" ) )
		{
			switch ( bytes ) 
			{
				case 4:
				{
					this.dataType = StreamDataType.float32;
					break;
				}
				case 8:
				{
					this.dataType = StreamDataType.double64;
					break;
				}
				default:
				{
					err = true;
				}						
			}
		}
		
		if( err )
		{
			throw new ClisMetadataException( "Unsupported data type." );
		}
	}

	public String getName()
	{
		return this.name;
	}
	
	public int getCols() 
	{
		return this.cols;
	}
	
	public StreamDataType getDataType()
	{
		return this.dataType;
	}
	
	public List<Integer> getBlockDataSize()
	{
		return this.blockDataSize;
	}
}
