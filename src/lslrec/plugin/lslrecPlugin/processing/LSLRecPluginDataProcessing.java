package lslrec.plugin.lslrecPlugin.processing;

import java.util.ArrayList;
import java.util.List;

import lslrec.config.Parameter;
import lslrec.dataStream.setting.DataStreamSetting;

public abstract class LSLRecPluginDataProcessing																
{		
	protected DataStreamSetting streamSetting;
	
	protected List< Number > data = new ArrayList< Number >();
		
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
	
	public Number[] processDataBlock( Number[] inputs )
	{
		List< Number > result = new ArrayList< Number >();

		if( inputs != null && inputs.length > 0 )
		{
			for( Number v : inputs )
			{
				this.data.add( v );
			}

			while( this.data.size() >= this.getMinDataLength2Process() )
			{
				Number[] processedData = this.data.subList( 0, this.getMinDataLength2Process() ).toArray( new Number[0] );
				this.data.subList( 0, this.getMinDataLength2Process() ).clear();

				if( this.prevProcess != null )
				{
					processedData = this.prevProcess.processDataBlock( processedData );
				}

				processedData = this.processData( processedData );
			}
		}

		return result.toArray( new Number[0] );
	}
	
	public DataStreamSetting getDataStreamSetting()
	{
		return this.streamSetting;
	}

	public abstract void loadProcessingSettings( List< Parameter< String > > pars);
	
	public abstract int getMinDataLength2Process();
	
	protected abstract Number[] processData( Number[] inputs );
}
