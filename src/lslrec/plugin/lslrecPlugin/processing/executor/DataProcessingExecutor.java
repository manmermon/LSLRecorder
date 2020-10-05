package lslrec.plugin.lslrecPlugin.processing.executor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.stoppableThread.AbstractStoppableThread;

public abstract class DataProcessingExecutor extends AbstractStoppableThread
{
	private ConcurrentLinkedQueue< Byte[] > inputs;
	private List< Number > data;
	private LSLRecPluginDataProcessing process;
	
	private int dataBlockSize = -1;
	
	/**
	 * 
	 */
	public DataProcessingExecutor( LSLRecPluginDataProcessing processes ) 
	{
		this.inputs = new ConcurrentLinkedQueue< Byte[] >();
		this.data = new ArrayList< Number >();
		this.process = processes;
		
		if( this.process != null )
		{
			this.dataBlockSize = this.process.getDataStreamSetting().getChunkSize() * this.process.getDataStreamSetting().getStreamInfo().channel_count();
		}
	}
	
	@Override
	protected void startUp() throws Exception 
	{
		this.stopThread = ( this.process == null );
	}
	
	@Override
	protected void runInLoop() throws Exception 
	{
		synchronized ( this )
		{
			if( this.inputs.isEmpty() )
			{
				super.wait();
			}
		}
		
		if( this.process != null )
		{
			while( !this.inputs.isEmpty() )
			{
				Number[] vals = ConvertTo.ByteArrayTo( this.inputs.poll(), this.process.getDataStreamSetting().getDataType() );
				
				synchronized ( this.data )
				{
					for( Number v : vals )
					{
						this.data.add( v );
					}
				}
				
				if( this.data.size() >= this.dataBlockSize )
				{
					Number[] d;
					synchronized ( this.data )
					{
						d = this.data.subList( 0, this.dataBlockSize ).toArray( new Number[0] );
						this.data.subList( 0, this.dataBlockSize ).clear();
					}
									
					this.process.processDataBlock( d );
				}
			}	
		}
	}

	public final void addData( byte[] inputs )
	{
		if( this.process != null && inputs != null )
		{
			this.inputs.add( ConvertTo.byteArray2ByteArray( inputs ) );
						
			synchronized ( this ) 
			{
				super.notify();
			}
		}
	}		
}
