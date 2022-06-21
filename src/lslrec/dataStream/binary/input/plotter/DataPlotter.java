/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
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

package lslrec.dataStream.binary.input.plotter;

import java.util.ArrayList;
import java.util.List;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.dataStream.binary.input.InputDataStreamReceiverTemplate;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.tools.StreamUtils.StreamDataType;
import lslrec.gui.dataPlot.CanvasStreamDataPlot;

public class DataPlotter extends InputDataStreamReceiverTemplate
{
	private CanvasStreamDataPlot plot;
	private int nByteData;
	private int minDataLengthToDraw = 1;
	private final List< List< Double > > dataBuffer = new ArrayList< List< Double > >();
	
	public DataPlotter( CanvasStreamDataPlot Plot, IStreamSetting lslCfg ) throws Exception
	{
		super( lslCfg );

		if (Plot == null)
		{
			throw new IllegalArgumentException( "Plot is null." );
		}

		this.plot = Plot;
		this.plot.setPlotName( lslCfg.name() );
		
		super.setName( this.getClass().getSimpleName() + "-" + lslCfg.name() );

		this.plot.setVisible(true);
		
		this.minDataLengthToDraw = (int)( lslCfg.sampling_rate() * lslCfg.getChunkSize() * 0.400D ); // 400 ms
		
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

	protected void managerData( byte[] data, byte[] time ) throws Exception
	{	
		Number[] dat = null;
		
		if( super.streamSetting.data_type() != StreamDataType.string )
		{
			dat = ConvertTo.Transform.ByteArrayTo( ConvertTo.Casting.byteArray2ByteArray( data ), super.streamSetting.data_type() );
		}
		else
		{
			dat = ConvertTo.Casting.byteArray2ByteArray( data );
		}
		
		if( super.streamSetting.isInterleavedData() )
		{
			dat = ConvertTo.Transform.Interleaved( dat, super.chunckLength, super.streamSetting.channel_count() );
		}

		if( dat != null )
		{
			for( int index = 0; index < dat.length; index++ )
			{	
				int c = ( ( index / super.chunckLength ) % super.streamSetting.channel_count() );
	
				if( c >= this.dataBuffer.size() )
				{
					this.dataBuffer.add( new ArrayList< Double>() );
				}
	
				List< Double > buf = this.dataBuffer.get( c );
	
				buf.add( dat[ index ].doubleValue() );
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
	}
	
	/*
	private double getDataValue( byte[] aux )
	{
		Number out = this.getInValue( aux );
		
//		switch( super.LSLFormatData ) 
//		{
//			case( LSL.ChannelFormat.double64 ):
//			{
//				out = ByteBuffer.wrap(aux).getDouble();
//				break;
//			}
//			case( LSL.ChannelFormat.float32 ):
//			{
//				out = new Double( ByteBuffer.wrap(aux).getFloat() );
//				break;
//			}
//			case( LSL.ChannelFormat.int8 ):
//			{
//				out = new Double( ByteBuffer.wrap(aux).get() );
//				break;
//			}
//			case( LSL.ChannelFormat.int16 ):
//			{
//				out = new Double( ByteBuffer.wrap(aux).getShort() );
//				break;
//			}
//			case( LSL.ChannelFormat.int32 ):
//			{
//				out = new Double( ByteBuffer.wrap(aux).getInt() );
//				break;
//			}
//			case( LSL.ChannelFormat.int64 ):
//			{
//				out = new Double( ByteBuffer.wrap(aux).getLong() );
//				break;
//			}
//			default: // String
//			{					
//				
//			}
//		}
		
		return out.doubleValue();
	}
	*/
		
	/**
	 * @param buf
	 * @return
	 */
	/*
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
	*/

	protected void postCleanUp() throws Exception
	{
		if( this.plot != null )
		{
			if( this.plot.isUndock() )
			{
				this.plot.disposeUndockWindow();
			}
		}
	}

	@Override
	public String getID() 
	{
		return this.getName();
	}
	
	public String getStreamUID()
	{
		return super.streamSetting.uid();
	}
	
	@Override
	protected void startMonitor() throws Exception 
	{
	}
}