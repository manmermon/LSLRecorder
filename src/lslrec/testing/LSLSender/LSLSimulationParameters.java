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
package lslrec.testing.LSLSender;
import java.nio.ByteOrder;

import lslrec.dataStream.tools.StreamUtils.StreamDataType;


public class LSLSimulationParameters 
{
	public static final int SIN = 0;
	public static final int LINEAR = 1;
	public static final int RANDOM = 2;
	
	private int numOfThreads = 1;
	
	private String streamName = "Simulation";
	private double samplingRate = 0;
	private int channels = 1;
	private String streamType = "value";
	private StreamDataType outputDataType = StreamDataType.float32;
	private StreamDataType inputDataType = StreamDataType.float32;	
	private String separator = " ";
	private int blockSize = 1;
	private int NumOutBlocks = 0;
	private boolean interleavedData = false;
	private int functionType = 0;
	
	private String streamID = "TAIS022-LSL-Simulation";
	
	private ByteOrder inDataFormat = ByteOrder.BIG_ENDIAN; 
	
	public LSLSimulationParameters( ) 
	{
	}
	
	public void setNumOfThreads( int num )
	{ 
		this.numOfThreads = num;
	}
	
	public int getNumOfThreads()
	{
		return this.numOfThreads;
	}
	
	public void setStreamName( String name )
	{
		this.streamName = name;
	}
	
	public String getStreamName()
	{
		return this.streamName;
	}

	public void setSamplingRate( double sampling )
	{
		this.samplingRate = sampling;
	}
	
	public double getSamplingRate( )
	{
		return this.samplingRate;
	}
	
	public void setChannelNumber( int num )
	{
		this.channels = num;
	}
	
	public int getChannelNumber()
	{
		return this.channels;
	}
	
	public void setOutDataType( StreamDataType type )
	{
		this.outputDataType = type;
	}
	
	public StreamDataType getOutDataType()
	{
		return this.outputDataType;
	}
	
	public void setInDataType( StreamDataType type )
	{
		this.inputDataType = type;
	}
	
	public StreamDataType getInDataType()
	{
		return this.inputDataType;
	}
	
	public void setStringSeparator( String sep )
	{
		this.separator = sep;
	}
	
	public String getStringSeparator()
	{
		return this.separator;
	}
	
	public void setStreamType( String type )
	{
		this.streamType = type;
	}
	
	public String getStreamType()
	{
		return this.streamType;
	}
	
	public void setInDataFormat( String format )
	{
		try 
		{
			ByteOrder.class.getField( format ).get( this.inDataFormat );
		} 
		catch (IllegalArgumentException | IllegalAccessException
				| NoSuchFieldException | SecurityException e) 
		{
			e.printStackTrace();
		}	
	}
	
	public void setInDataFormat( ByteOrder format )
	{
		this.inDataFormat = format;
	}
	
	public ByteOrder getInDataFormat( )
	{
		return this.inDataFormat;
	}
	
	public int getBlockSize()
	{
		return this.blockSize;
	}
	
	public void setBlockSize( int size )
	{
		this.blockSize = size;
	}
	
	public int getNumberOutputBlocks()
	{
		return this.NumOutBlocks;
	}
	
	public void setNumberOutputBlocks( int n )
	{
		this.NumOutBlocks = n;
	}
	
	public boolean isInterleavedData()
	{
		return this.interleavedData;
	}
	
	public void setInterleavedData( boolean interleaved )
	{
		this.interleavedData = interleaved;
	}
	
	public void setOutputFunctionType( int t )
	{
		this.functionType = t;
	}
	
	public int getOutputFunctionType()
	{
		return this.functionType;
	}
	
	public void setStreamID( String ui )
	{
		if( ui != null && !ui.trim().isEmpty() )
		{
			this.streamID = ui.trim();
		}
	}
	
	public String getStreamID( )
	{
		return this.streamID;
	}
	
	@Override
	public String toString() 
	{
		return "< streamID=" + streamID 
				+ ", streamName=" + streamName
				+ ", streamType=" + streamType
				+ ", samplingRate=" + samplingRate
				+ ", channels=" + channels
				+ ", chunckSize=" + blockSize
				+ ", outDataType=" + outputDataType 
				+ ", functionType=" + functionType
				+ ", interleaved=" + interleavedData 
				+ ", inDataFormat=" + inDataFormat
				+ ", inputDataType=" + inputDataType
				+ ", numOfThreads=" + numOfThreads
				+ ", NumOutBlocks=" + NumOutBlocks
				+ " >";
	}
}

