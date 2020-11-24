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
package lslrec.plugin.lslrecPlugin.processing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.auxiliar.task.ITaskIdentity;
import lslrec.config.Parameter;
import lslrec.dataStream.family.setting.IStreamSetting;

public abstract class LSLRecPluginDataProcessing implements ITaskIdentity												
{		
	protected final IStreamSetting streamSetting;

	// Data buffers 
	private LinkedList< Number >[] dataBuffer = null;
	private int bufferCapacity = 1;
	
	private int overlapOffset = 1;
	private int[] overlapCounter = null;
	
	private ArrayList< Number > tempBuffer = new ArrayList<Number>();
	
	private LSLRecPluginDataProcessing prevProcess = null;
		
	public LSLRecPluginDataProcessing( IStreamSetting setting, LSLRecPluginDataProcessing prevProc )
	{
		if( setting == null )
		{
			throw new IllegalArgumentException( "Data stream setting null.");
		}
				
		this.prevProcess = prevProc;
		
		this.streamSetting = setting;
		
		this.overlapOffset = this.getOverlapOffset();
		
		if( this.overlapOffset <= 0 )
		{
			this.overlapOffset = 1;
		}
		
		this.overlapCounter = new int[ this.streamSetting.channel_count() ];
	}
	
	private void setBuffer()
	{
		int channels = this.streamSetting.channel_count();
		
		this.dataBuffer = new  LinkedList[ channels ];
		
		Number zero = ConvertTo.Casting.NumberTo( 0D, this.streamSetting.data_type() );
		
		this.bufferCapacity = this.getBufferLength();
		if( this.bufferCapacity < 1 )
		{
			this.bufferCapacity = 1;
		}
		
		for( int c = 0; c < channels; c++ )
		{
			LinkedList< Number > buffer = new LinkedList<Number>();
			
			for( int i = 0; i < bufferCapacity ; i++ )
			{
				buffer.add( zero );
			}
			
			this.dataBuffer[ c ] = buffer;
		}
	}
	
	/**
	 * 
	 * @param inputs -> interleaved array 
	 * @return interleaved array
	 */
	public final Number[] processDataBlock( Number[] inputs )
	{
		if( this.dataBuffer == null )
		{
			this.setBuffer();
		}
		
		List< Number > result = new ArrayList< Number >();

		if( this.prevProcess == null )
		{
			if( inputs != null && inputs.length > 0 )
			{
				//
				// Add data into buffer
				// 
				for( Number in : inputs )
				{
					this.tempBuffer.add( in );				
				}
				
				int from = 0;
				int to = this.tempBuffer.size() / ( this.streamSetting.channel_count() * this.streamSetting.getChunkSize() );
				
				int channels = this.streamSetting.channel_count();
				
				
				//
				// data block size = channels * chunkSize
				//
				if( to > 0 )
				{
					to *= ( channels * this.streamSetting.getChunkSize() );
					
					inputs = this.tempBuffer.subList( from, to ).toArray( new Number[ 0 ] );
					this.tempBuffer.subList( 0, to ).clear();
						
					this.putInBuffer( inputs, this.streamSetting.isInterleavedData() );
					
					result.addAll( this.applyProcess2DataBuffers() );
				}
				
			}
		}
		else
		{	
			Number[] processedData = this.prevProcess.processDataBlock( inputs ); 
			// no interleaved processed data, that is:
			// [PA0, PA1, .., PAn, PB0, PB1, ..., PBn, PC0, PC1, ..., PCn, ...]
			
			this.putInBuffer( processedData, false );
			
			result.addAll( this.applyProcess2DataBuffers() );			
			// no interleaved processed data, that is:
			// [PA0, PA1, .., PAn, PB0, PB1, ..., PBn, PC0, PC1, ..., PCn, ...]
						
		}

		return result.toArray( new Number[0]);
	}
	
	private void putInBuffer( Number[] inputs, boolean interleavedData )
	{
		if( inputs != null && inputs.length > 0)
		{
			int channels = this.streamSetting.channel_count();
			int chunk = this.streamSetting.getChunkSize();
			
			//
			// No interleaved data, that is:
			// [A0, B0, C0, .., A1, B1, C2, ..., An, Bn, Cn, ...] -> [A0, A1, .., An, B0, B1, ..., Bn, C0, C1, ..., Cn, ...] ->  
			//
			if( interleavedData )
			{
				inputs = ConvertTo.Transform.Interleaved( inputs												
															, chunk
															, channels
														  );
			}
			
			int ch = 0;
			for( int i = 0; i < inputs.length; i = i + chunk  )
			{
				for( int c = 0; c < chunk; c++ )
				{
					this.dataBuffer[ ch ].add( inputs[ i + c ] ); 
				}
				
				ch++;
				
				if( ch >= this.dataBuffer.length )
				{
					ch = 0;
				}
			}
		}
	}
	
	private List< Number > applyProcess2DataBuffers()
	{
		List< Number > result = new ArrayList< Number >();
		
		int channels = this.streamSetting.channel_count();
		
		// Remove oldest values
		for( int c = 0; c < channels; c++ )
		{
			LinkedList< Number > buffer = this.dataBuffer[ c ];
			while( buffer.size() > this.bufferCapacity )
			{
				buffer.remove( 0 );
				
				this.overlapCounter[ c ]++;
				
				if( this.overlapCounter[ c ] >= this.overlapOffset )
				{
					this.overlapCounter[ c ] = 0;
				
					//
					// Data processing
					//
					
					Number[] processedData = buffer.subList( 0, this.bufferCapacity ).toArray( new Number[ 0 ] );		
	
					processedData = this.processData( processedData );
					
					if( processedData != null && processedData.length > 0 )
					{
						for( Number dat : processedData )
						{
							result.add( dat );
							// no interleaved processed data, that is:
							// [PA0, PA1, .., PAn, PB0, PB1, ..., PBn, PC0, PC1, ..., PCn, ...]
						}
					}
				}
			}
		}	
		
		return result;
	}
	
	public IStreamSetting getDataStreamSetting()
	{
		return this.streamSetting;
	}

	public final int getMaxDataBufferLengthOfProcessingSequence()
	{
		int len = this.getBufferLength();
		
		if( this.prevProcess != null )
		{
			int prevLen = this.prevProcess.getMaxDataBufferLengthOfProcessingSequence();
			
			if( len < prevLen )
			{
				len = prevLen;
			}
		}
				
		return len;
	}
	
	public final List< Integer > getAllDataBufferLengths()
	{
		List< Integer > lens = new ArrayList< Integer >();
		
		if( this.prevProcess != null )
		{
			lens.addAll( this.prevProcess.getAllDataBufferLengths() );
		}
		
		lens.add( this.getBufferLength() );
		
		return lens;
	}
	
	public final List< String > getProcessesList()
	{
		List< String > lens = new ArrayList< String >();
		
		if( this.prevProcess != null )
		{
			lens.addAll( this.prevProcess.getProcessesList() );
		}
		
		lens.add( this.getID() );
		
		return lens;
	}
	
	public abstract void loadProcessingSettings( List< Parameter< String > > pars);
	
	public abstract int getBufferLength();
	
	public abstract int getOverlapOffset();
	
	protected abstract Number[] processData( Number[] inputs );
}
