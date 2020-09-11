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
package prototype.deprecated;

import auxiliar.extra.ConvertTo;
import exceptions.UnsupportedDataTypeException;
//import ch.systemsx.cisd.hdf5.IHDF5Writer;
import ch.systemsx.cisd.hdf5.IHDF5Writer;
import edu.ucsd.sccn.LSL;

public class HDF5Data 
{	
	private Number[] values = null;
	
	private IHDF5Writer writer = null;
			
	private int colIndex = 0;
	
	private int maxCol = 0;
	
	private int blockIndex = 0;
	
	private int dataType = LSL.ChannelFormat.float32;
	
	private String name = "";
				
	public HDF5Data( IHDF5Writer wr, String varName, int dataformat, int numChannels ) throws Exception 
	{
		if( wr == null || varName == null )
		{
			throw new IllegalArgumentException( "Input(s) null." );
		}
		
		this.name = varName;
		
		this.maxCol = numChannels;
		
		this.values = new Number[ this.maxCol ];
		
		this.dataType = dataformat;
		
		this.writer = wr;
				
		if( this.dataType == LSL.ChannelFormat.undefined )
		{
			throw new UnsupportedTypeException( );
		}
		
		this.createMatrix();
	}
	
	private void createMatrix()
	{
		switch ( this.dataType ) 
		{
			case LSL.ChannelFormat.double64:
			{
				this.writer.float64().createMatrix( this.name, this.maxCol,  1 );
				break;
			}
			case LSL.ChannelFormat.float32:
			{
				this.writer.float32().createMatrix( this.name, this.maxCol,  1 );
				break;
			}
			case LSL.ChannelFormat.int8:
			{
				this.writer.int8().createMatrix( this.name, this.maxCol,  1 );
				break;
			}
			case LSL.ChannelFormat.int16:
			{
				this.writer.int16().createMatrix( this.name, this.maxCol,  1 );
				break;
			}
			case LSL.ChannelFormat.int32:
			{
				this.writer.int32().createMatrix( this.name, this.maxCol,  1 );
				break;
			}
			case LSL.ChannelFormat.int64:
			{
				this.writer.int64().createMatrix( this.name, this.maxCol,  1 );
				break;
			}
			case LSL.ChannelFormat.string:
			{
				this.writer.int8().createMatrix( this.name, this.maxCol,  1 );
				break;
			}
		}
	}
		
	public void addData( Number value )
	{		
		this.values[ this.colIndex ] = value;
		
		this.colIndex++;
		
		if( this.colIndex >= this.maxCol )
		{
			this.colIndex = 0;
			
			this.writeData();
		}
	}
	
	public void addData( Number[] values )
	{		
		if( values != null )
		{
			for( Number val : values )
			{
				this.addData( val );
			}
		}
	}
	
	public void addData( String str )
	{
		if( str != null )
		{
			for( byte b : str.getBytes() )
			{
				this.addData( b );
			}
		}
	}
	
	public void addData( String[] values )
	{		
		if( values != null )
		{
			for( String str : values )
			{
				this.addData( str );
			}
		}
	}
	
	private void writeData()
	{
		switch ( this.dataType ) 
		{
			case LSL.ChannelFormat.double64:
			{
				this.writer.float64().writeMatrixBlockWithOffset( this.name, new double[][] { ConvertTo.NumberArray2DoubleArray( this.values ) }, 0, blockIndex );
				break;
			}
			case LSL.ChannelFormat.float32:
			{
				this.writer.float32().writeMatrixBlockWithOffset( this.name, new float[][] { ConvertTo.NumberArray2FloatArray( this.values ) }, 0, blockIndex );
				break;
			}
			case LSL.ChannelFormat.int8:
			{
				this.writer.int8().writeMatrixBlockWithOffset( this.name, new byte[][] { ConvertTo.NumberArray2ByteArray( this.values ) }, 0, blockIndex );
				break;
			}
			case LSL.ChannelFormat.int16:
			{
				this.writer.int16().writeMatrixBlockWithOffset( this.name, new short[][] { ConvertTo.NumberArray2ShortArray( this.values ) }, 0, blockIndex );
				break;
			}
			case LSL.ChannelFormat.int32:
			{
				this.writer.int32().writeMatrixBlockWithOffset( this.name, new int[][] { ConvertTo.NumberArray2IntegerArray( this.values ) }, 0, blockIndex );
				break;
			}
			case LSL.ChannelFormat.int64:
			{
				this.writer.int64().writeMatrixBlockWithOffset( this.name, new long[][] { ConvertTo.NumberArray2LongArray( this.values ) }, 0, blockIndex );
				break;
			}
			case LSL.ChannelFormat.string:
			{
				this.writer.int8().writeMatrixBlockWithOffset( this.name, new byte[][] { ConvertTo.NumberArray2ByteArray( this.values ) }, 0, blockIndex );
				break;
			}
		}
		
		this.blockIndex++;
	}
	
	public void close() 
	{
		this.writer.close();
	}
}
