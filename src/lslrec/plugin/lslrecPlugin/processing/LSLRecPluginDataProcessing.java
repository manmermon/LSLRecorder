package lslrec.plugin.lslrecPlugin.processing;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.config.Parameter;
import lslrec.dataStream.setting.DataStreamSetting;

public abstract class LSLRecPluginDataProcessing																
{		
	private DataStreamSetting streamSetting;

	// Interleaved Data buffer 
	private LinkedList< Number > dataBuffer = null;
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
		
		this.dataBuffer = new  LinkedList< Number >();
		
		int channels = setting.getStreamInfo().channel_count();
		
		Number zero = ConvertTo.NumberTo( 0D, setting.getDataType() );
		
		this.bufferCapacity = this.getBufferLength() * channels;
		if( this.bufferCapacity < 1 )
		{
			this.bufferCapacity = channels;
		}
		
		for( int i = 0; i < bufferCapacity ; i++ )
		{
			this.dataBuffer.add( zero );
		}
	}
	
	/**
	 * 
	 * @param inputs -> interleaved array 
	 * @return interleaved array
	 */
	public Number[] processDataBlock( Number[] inputs )
	{
		List< Number > result = new ArrayList< Number >();

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
			
			//
			// data block size = channels * chunkSize
			//
			if( to > 0 )
			{
				to *= ( this.streamSetting.getStreamInfo().channel_count() * this.streamSetting.getChunkSize() );
				
				inputs = this.tempBuffer.subList( 0, to ).toArray( new Number[ 0 ] );
				this.tempBuffer.subList( 0, to ).clear();
					
				//
				// No interleaved data, that is:
				//[A0, B0, C0, .., A1, B1, C2, ..., An, Bn, Cn, ...] -> [A0, A1, .., An, B0, B1, ..., Bn, C0, C1, ..., Cn, ...]
				//
				if( this.streamSetting.isInterleavedData() )
				{
					inputs = ConvertTo.Interleaved( inputs
													, this.streamSetting.getChunkSize()
													, this.streamSetting.getStreamInfo().channel_count() );
				}
				
				for( Number dat : inputs )
				{
					this.dataBuffer.add( dat ); 
				}
				
				do
				{
					// Remove oldest values
					// TODO
					// Eliminar los elementos más antiguos teniendo en cuenta que hemos ordenados las entradas como sigue:
					//[A0, A1, .., An, B0, B1, ..., Bn, C0, C1, ..., Cn, ...]
					for( int c = 0; c < this.streamSetting.getStreamInfo().channel_count(); c++ )
					{
						this.dataBuffer.remove( 0 );
					}
				
					
					//
					// Data processing
					//
					
					Number[] processedData = this.dataBuffer.subList( 0, this.bufferCapacity ).toArray( new Number[ 0 ] );
	
					if( this.prevProcess != null )
					{
						processedData = this.prevProcess.processDataBlock( processedData );
					}
	
					processedData = this.processData( processedData );
					
					for( Number val : processedData )
					{
						result.add( val );
					}
					
									
					this.channelIndex++;
				}
			}

		}

		return result.toArray( new Number[0]);
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
