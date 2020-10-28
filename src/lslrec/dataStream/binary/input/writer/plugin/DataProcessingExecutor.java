package lslrec.dataStream.binary.input.writer.plugin;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.auxiliar.extra.FileUtils;
import lslrec.auxiliar.tasks.ITaskIdentity;
import lslrec.dataStream.binary.input.writer.StreamBinaryHeader;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.stoppableThread.AbstractStoppableThread;

public class DataProcessingExecutor extends AbstractStoppableThread implements ITaskIdentity
{
	private ConcurrentLinkedQueue< Byte[] > inputs;
	private List< Number > data;
	private LSLRecPluginDataProcessing process;
	
	private int dataBlockSize = -1;
	
	private File outfile = null;	
	private BufferedOutputStream out;
	
	/**
	 * 
	 */
	public DataProcessingExecutor( LSLRecPluginDataProcessing processes, String outputFileName )  throws Exception 
	{
		this.inputs = new ConcurrentLinkedQueue< Byte[] >();
		this.data = new ArrayList< Number >();
		this.process = processes;
		
		if( outputFileName != null )
		{
			this.outfile =  new File( outputFileName );
		}
		
		if( this.process != null )
		{
			this.dataBlockSize = this.process.getDataStreamSetting().getChunkSize() * this.process.getDataStreamSetting().getStreamInfo().channel_count();
		}
		
		super.setName( this.getID() );
	}
	
	public File getOutputBinaryFile()
	{
		return this.outfile;
	}
	
	@Override
	public String getID() 
	{
		return this.getClass().getName() + "-" + this.process.getDataStreamSetting().getStreamName();
	}
	
	@Override
	protected void preStart() throws Exception 
	{	
		if( this.outfile != null )
		{
			FileUtils.CreateTemporalBinFile( this.outfile );
			
			this.out = new BufferedOutputStream( new FileOutputStream( this.outfile ) );
		}
		
		super.preStart();
	}
	
	@Override
	protected void startUp() throws Exception 
	{
		super.stopThread = ( this.process == null );
		
		if( !super.stopThread )
		{
			String binHeader = StreamBinaryHeader.getStreamBinHeader( this.process.getDataStreamSetting() );
			
			binHeader = binHeader.trim().replace( "\r", "" ).replace( "\n", "" ) + "\n";
			
			this.out.write( binHeader.getBytes() );
		}
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
									
					Number[] proccessedData = this.process.processDataBlock( d );
					
					if( this.out != null && proccessedData != null )
					{
						byte[] DAT = ConvertTo.NumberArray2ByteArray( proccessedData );
						
						this.out.write( DAT );
					}
				}
			}	
		}
	}
	
	@Override
	protected void runExceptionManager(Throwable e) 
	{
		if( !( e instanceof InterruptedException ) )
		{	
			super.runExceptionManager(e);
		}
	} 

	public final void processData( byte[] inputs )
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

	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{	
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{	
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
		
		if( this.out != null )
		{
			this.out.close();
		}
	}
}
