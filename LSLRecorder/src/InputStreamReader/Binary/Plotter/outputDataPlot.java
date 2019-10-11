/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec and CLIS.
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

package InputStreamReader.Binary.Plotter;

import GUI.CanvasLSLDataPlot;
import InputStreamReader.Binary.readInputData;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLConfigParameters;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class outputDataPlot extends readInputData
{
	private CanvasLSLDataPlot plot;
	private int nByteData;
	private int minDataLengthToDraw = 1;
	private final List< List< Double > > dataBuffer = new ArrayList< List< Double > >();
	
	public outputDataPlot( CanvasLSLDataPlot Plot, LSL.StreamInfo info, LSLConfigParameters lslCfg ) throws Exception
	{
		super( info, lslCfg );

		if (Plot == null)
		{
			throw new IllegalArgumentException( "Plot is null." );
		}

		this.plot = Plot;
		this.plot.setPlotName( info.name() );
		
		super.setName( this.getClass().getSimpleName() + "-" + info.name() );

		this.plot.setVisible(true);
		
		this.minDataLengthToDraw = (int)( lslCfg.getSamplingRate() * 0.400D ); // 400 ms
		
		if( this.minDataLengthToDraw <= 0 )
		{
			this.minDataLengthToDraw = 1;
		}
	}

	protected int createArrayData() throws Exception
	{
		this.nByteData = super.createArrayData();

		return this.nByteData;
	}

	protected void managerData( byte[] data ) throws Exception
	{
		byte[] aux = new byte[ this.nByteData ];
		int numReadChunk = ( data.length / this.nByteData ) / super.lslChannelCounts;

		List< Double > DATA = new ArrayList< Double >();
		
		int channel = 0;
		if( !super.interleavedData )
		{		
			for (int i = 0; i < data.length; i += this.nByteData)
			{	
				if (data.length - i >= this.nByteData)
				{
					for (int j = 0; j < this.nByteData; j++)
					{
						aux[ j ] = data[ i + j ];
					}
					 
					DATA.add( this.getDataValue( aux ) );			
				}
	
				if( DATA.size() >= numReadChunk )
				{					
					if( this.dataBuffer.size() > channel )
					{
						List< Double > buf = this.dataBuffer.get( channel );
						buf.addAll( DATA );
					}
					else
					{
						this.dataBuffer.add( DATA );
					}
					
					channel++;
					DATA = new ArrayList< Double >();					
				}			
			}
		}
		else
		{
			int N = super.lslChannelCounts * this.nByteData;
			for( int c = 0; c <= ( N - this.nByteData ) ; c += this.nByteData )
			{
				for( int j = c; j < data.length; j += N )
				{
					for( int k = 0; k < this.nByteData; k++ )
					{
						aux[ k ] = data[ j + k ];
					}					
					
					DATA.add( this.getDataValue( aux ) );
				}
				
				if( this.dataBuffer.size() > channel )
				{
					List< Double > buf = this.dataBuffer.get( channel );
					buf.addAll( DATA );
				}
				else 
				{		
					this.dataBuffer.add( DATA );
				}
				
				channel++;
				DATA = new ArrayList< Double >();
			}
		}
		
		if( DATA.size() > 0 )
		{			
			if( this.dataBuffer.size() > channel )
			{
				List< Double > buf = this.dataBuffer.get( channel );
				buf.addAll( DATA );
			}
			else 
			{		
				this.dataBuffer.add( DATA );
			}
		}
		
		boolean draw = false;
		
		for( List< Double > buf : this.dataBuffer )
		{
			draw = buf.size() > this.minDataLengthToDraw;
			
			if( draw )
			{
				break;
			}
		}
		
		if( draw )
		{
			this.plot.addXYData( this.dataBuffer );
			this.dataBuffer.clear();
		}
	}
	
	private double getDataValue( byte[] aux )
	{
		Number out = this.getInValue( aux );
		
		/*
		switch( super.LSLFormatData ) 
		{
			case( LSL.ChannelFormat.double64 ):
			{
				out = ByteBuffer.wrap(aux).getDouble();
				break;
			}
			case( LSL.ChannelFormat.float32 ):
			{
				out = new Double( ByteBuffer.wrap(aux).getFloat() );
				break;
			}
			case( LSL.ChannelFormat.int8 ):
			{
				out = new Double( ByteBuffer.wrap(aux).get() );
				break;
			}
			case( LSL.ChannelFormat.int16 ):
			{
				out = new Double( ByteBuffer.wrap(aux).getShort() );
				break;
			}
			case( LSL.ChannelFormat.int32 ):
			{
				out = new Double( ByteBuffer.wrap(aux).getInt() );
				break;
			}
			case( LSL.ChannelFormat.int64 ):
			{
				out = new Double( ByteBuffer.wrap(aux).getLong() );
				break;
			}
			default: // String
			{					
				
			}
		}
		*/
		
		return out.doubleValue();
	}
	
	private Number getInValue( byte[] buf )
	{
		Number in;
		
		switch ( super.LSLFormatData  )
		{
			case LSL.ChannelFormat.float32: 
			{				
				in = ByteBuffer.wrap( buf ).getFloat();
				
				break;
			}
			case LSL.ChannelFormat.double64:
			{
				in = ByteBuffer.wrap( buf ).getDouble();
				
				break;
			}
			case LSL.ChannelFormat.int32:
			{
				in = ByteBuffer.wrap( buf ).getInt();
				
				break;
			}
			case LSL.ChannelFormat.int16:
			{
				in = ByteBuffer.wrap( buf ).getShort();
				
				break;
			}		
			case LSL.ChannelFormat.int64:
			{
				in = ByteBuffer.wrap( buf ).getLong();
				
				break;
			}
			case LSL.ChannelFormat.int8:
			{
				in = buf[ 0 ];
				
				break;
			}
			default:
			{
				in = 0F;
				
				break;
			}
		}
				
		return in;
	}

	protected void postCleanUp() throws Exception
	{}

	@Override
	public String getID() 
	{
		return this.getName();
	}
}