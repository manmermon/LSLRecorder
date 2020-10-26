/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.test;

import java.util.Arrays;
import java.util.List;

import lslrec.config.Parameter;
import lslrec.dataStream.setting.DataStreamSetting;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

/**
 * @author Manuel Merino Monge
 *
 */
public class DataProcessingTest extends LSLRecPluginDataProcessing 
{
	/**
	 * 
	 */
	public DataProcessingTest( DataStreamSetting setting, LSLRecPluginDataProcessing prevProcess ) 
	{
		super( setting, prevProcess );
	}
	
	@Override
	public int getMinDataLength2Process() 
	{
		return 1;
	}

	@Override
	public void loadProcessingSettings(List<Parameter<String>> arg0) 
	{	
	}

	@Override
	protected Number[] processData( Number[] arg0 ) 
	{
		Number[] res = null;
		
		if( arg0 != null )
		{
			res = new Number[ arg0.length ];
			
			System.out.print( super.streamSetting.getStreamName() + " -- Inputs: " + Arrays.toString( arg0 ) );
			
			for( int i = 0; i < res.length; i++ )
			{	
				res[ i ] = arg0[ i ].doubleValue() + 1;
			}
			
			System.out.println( " Outputs: " + Arrays.toString( res ) );
		}
		
		return res;
	}

}
