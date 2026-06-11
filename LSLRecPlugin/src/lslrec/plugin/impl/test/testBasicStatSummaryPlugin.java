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
