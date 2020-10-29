package lslrec.plugin.lslrecPlugin.processing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.config.Parameter;
import lslrec.dataStream.setting.DataStreamSetting;

public abstract class LSLRecPluginDataProcessing																
{		
	protected final DataStreamSetting streamSetting;

	// Data buffers 
	private LinkedList< Number >[] dataBuffer = null;
	private int bufferCapacity = 1;
	
	private ArrayList< Number > tempBuffer = new ArrayList<Number>();
	
	private LSLRecPluginDataProcessing prevProcess = null;
		
	public LSLRecPluginDataProcessing( DataStreamSetting setting, LSLRecPluginDataProcessing prevProc )
	{
		if( setting == null )
		{
			throw new IllegalArgumentException( "Data stream setting null.");
		}
				
		this.prevProcess = prevProc;
		
		this.streamSetting = setting;
	}
	
	private void setBuffer()
	{
		int channels = this.streamSetting.getStreamInfo().channel_count();
		
		this.dataBuffer = new  LinkedList[ channels ];
		
		Number zero = ConvertTo.Casting.NumberTo( 0D, this.streamSetting.getDataType() );
		
		this.bufferCapacity = this.getBufferLength() * channels;
		if( this.bufferCapacity < 1 )
		{
			this.bufferCapacity = channels;
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
				int to = this.tempBuffer.size() / ( this.streamSetting.getStreamInfo().channel_count() * this.streamSetting.getChunkSize() );
				
				int channels = this.streamSetting.getStreamInfo().channel_count();
				
				//
				// data block size = channels * chunkSize
				//
				if( to > 0 )
				{
					to *= ( channels * this.streamSetting.getChunkSize() );
					
					inputs = this.tempBuffer.subList( from, to ).toArray( new Number[ 0 ] );
					this.tempBuffer.subList( 0, to ).clear();
						
					this.putInBuffer( inputs, this.streamSetting.isInterleavedData() );
					
								
				}
				
				result.addAll( this.applyProcess2DataBuffers() );	
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
			int channels = this.streamSetting.getStreamInfo().channel_count();
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
		
		int channels = this.streamSetting.getStreamInfo().channel_count();
		
		// Remove oldest values
		for( int c = 0; c < channels; c++ )
		{
			LinkedList< Number > buffer = this.dataBuffer[ c ];
			while( buffer.size() > this.bufferCapacity )
			{
				buffer.remove( 0 );
				
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
		
		return result;
	}
	
	public DataStreamSetting getDataStreamSetting()
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
	
	public abstract void loadProcessingSettings( List< Parameter< String > > pars);
	
	public abstract int getBufferLength();	
	
	protected abstract Number[] processData( Number[] inputs );
}
