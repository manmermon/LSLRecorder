package lslrec.plugin.impl.test;

import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting.StreamLibrary;
import lslrec.dataStream.family.setting.SimpleStreamSetting;
import lslrec.dataStream.tools.StreamUtils.StreamDataType;
import lslrec.plugin.impl.dataProcessing.basicStatSummary.BasicStatSummaryPlugin;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.PluginDataProcessingSettings;

public class testBasicStatSummaryPlugin {

	public static void main(String[] args) 
	{
		IStreamSetting str = new SimpleStreamSetting( StreamLibrary.LSLREC, "test"
														, StreamDataType.float32, 1, 1, 100
														, 3, false
														, "ID"
														, "12345" );
		
		BasicStatSummaryPlugin plg = new BasicStatSummaryPlugin();
		
		PluginDataProcessingSettings setting = new PluginDataProcessingSettings( str );
		LSLRecPluginDataProcessing prc = plg.getProcessing( setting, null );
		
		Number[] dat = new Number[ 100 ];
		for( int i = 0; i < dat.length; i += 2 )
		{
			dat[ i ] = i;
		}
		
		for( int i = 1; i < dat.length; i += 2 )
		{
			dat[ i ] = 0;
		}
		
		/*
		dat[ 3 * dat.length /4 ] = 1;
		dat[ dat.length / 2 + 1 ] = 2;
		//*/
		
		prc.processDataBlock( dat );
		prc.finish();
	}

}
