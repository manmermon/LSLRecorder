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
package lslrec.plugin.impl.encoder;

import org.apache.commons.lang3.ArrayUtils;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.auxiliar.extra.Tuple;
import lslrec.dataStream.family.stream.lsl.LSLUtils;
import lslrec.exceptions.UnsupportedTypeException;
import ch.systemsx.cisd.base.mdarray.MDArray;
//import ch.systemsx.cisd.hdf5.IHDF5Writer;
import ch.systemsx.cisd.hdf5.IHDF5Writer;


public class HDF5Data 
{	
	private IHDF5Writer writer = null;
				
	private long maxCol = 0;
	
	private int blockIndex = 0;
	
	private int dataType = LSLUtils.float32;
	
	private String name = "";
	
	private Number[] remainingData = new Number[0];
	private String[] remainingStringData = new String[0];
				
	public HDF5Data( IHDF5Writer wr, String varName, int dataformat, long numChannels ) throws Exception 
	{
		if( wr == null || varName == null )
		{
			throw new IllegalArgumentException( "Input(s) null." );
		}
		
		this.name = varName;
		
		this.maxCol = numChannels;
		
		this.dataType = dataformat;
		
		this.writer = wr;
				
		if( this.dataType == LSLUtils.undefined )
		{
			throw new UnsupportedTypeException( );
		}
		
		this.createMatrix( this.name );
	}
	
	private void createMatrix( String name )
	{
		switch ( this.dataType ) 
		{
			case LSLUtils.double64:
			{
				this.writer.float64().createMatrix( name, 1, 1, 1, 1 );
				break;
			}
			case LSLUtils.float32:
			{
				this.writer.float32().createMatrix( name, 1, 1, 1, 1 );
				break;
			}
			case LSLUtils.int8:
			{
				this.writer.int8().createMatrix( name, 1, 1, 1, 1 );
				break;
			}
			case LSLUtils.int16:
			{
				this.writer.int16().createMatrix( name, 1, 1, 1, 1 );
				break;
			}
			case LSLUtils.int32:
			{
				this.writer.int32().createMatrix( name, 1, 1, 1, 1 );
				break;
			}
			case LSLUtils.int64:
			{
				this.writer.int64().createMatrix( name, 1, 1, 1, 1 );
				break;
			}
			case LSLUtils.string:
			{
				//this.writer.int8().createMatrix( this.name, 1, 1, 1, 1 );
				this.writer.string().createMDArray( name, 1, new long[] { 1 }, new int[] { 1 } );
				break;
			}
		}
	}
		
	public void addData( Number[] values )
	{		
		values = ArrayUtils.addAll( this.remainingData, values );
		Tuple< Number[][], Number[] > data = ConvertTo.Transform.Array2Matrix( values, this.maxCol );
		
		this.writeData( this.name, data.t1, this.blockIndex );
		
		this.blockIndex += data.t1.length;
		
		this.remainingData = data.t2;
	}
			
	public void addData( String[] values )
	{		
		values = ArrayUtils.addAll( this.remainingStringData, values );
		Tuple< String[][], String[] > data = ConvertTo.Transform.StringArray2Matrix( values, this.maxCol );
		
		if( data.t1.length > 0 )
		{	
			this.writeStringData( this.name, this.StringMatrix2MDStringArray( data.t1 ), this.blockIndex );
			
			this.blockIndex += data.t1.length;
		}
		
		this.remainingStringData = data.t2;
	}
	
	private MDArray< String > StringMatrix2MDStringArray( String[][] data )
	{
		MDArray< String > strMatrix = new MDArray<String>( String.class , new long[] { data.length, this.maxCol } );
		for( int r = 0; r < data.length; r++ )
		{
			for( int c = 0; c < data[ 0 ].length; c++ )
			{
				strMatrix.set( data[ r ][ c ], r, c );
			}						
		}
		
		return strMatrix;
	}
	
	private void writeData( String name, Number[][] values, long blockIndex )
	{
		switch ( this.dataType ) 
		{
			case LSLUtils.double64:
			{
				this.writer.float64().writeMatrixBlockWithOffset( name, ConvertTo.Casting.NumberMatrix2doubleMatrix( values ), blockIndex, 0 );
				break;
			}
			case LSLUtils.float32:
			{
				this.writer.float32().writeMatrixBlockWithOffset( name, ConvertTo.Casting.NumberMatrix2floatMatrix( values ), blockIndex, 0 );
				break;
			}
			case LSLUtils.int8:
			{
				this.writer.int8().writeMatrixBlockWithOffset( name, ConvertTo.Casting.NumberMatrix2byteMatrix( values ), blockIndex, 0 );
				break;
			}
			case LSLUtils.int16:
			{
				this.writer.int16().writeMatrixBlockWithOffset( name, ConvertTo.Casting.NumberMatrix2shortMatrix( values ), blockIndex, 0 );
				break;
			}
			case LSLUtils.int32:
			{
				this.writer.int32().writeMatrixBlockWithOffset( name, ConvertTo.Casting.NumberMatrix2integerMatrix( values ), blockIndex, 0 );
				break;
			}
			case LSLUtils.int64:
			{
				this.writer.int64().writeMatrixBlockWithOffset( name, ConvertTo.Casting.NumberMatrix2longMatrix( values ), blockIndex, 0 );
				break;
			}
		}
	}
	
	private void writeStringData( String name, MDArray< String > values, long blockIndex )
	{
		this.writer.string().writeMDArrayBlockWithOffset( name, values, new long[] { blockIndex } );				
	}
	
	public void close() 
	{
		if( this.remainingData != null && this.remainingData.length > 0 )
		{
			String newName = this.name + "Remaining";
			this.createMatrix( newName );
			Number[][] d = ConvertTo.Transform.Array2Matrix( this.remainingData, this.remainingData.length ).t1;
			this.writeData( newName, d, 0L );
		}
		
		if( this.remainingStringData != null && this.remainingStringData.length > 0 )
		{
			String newName = this.name + "Remaining";
			this.createMatrix( newName );
			String[][] d = ConvertTo.Transform.StringArray2Matrix( this.remainingStringData, this.remainingStringData.length ).t1;
			this.writeStringData( newName, StringMatrix2MDStringArray( d ), 0 );
			
		}
			
		this.writer.close();
	}
}
