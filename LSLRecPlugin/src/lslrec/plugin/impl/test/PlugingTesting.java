/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec. https://github.com/manmermon/LSLRecorder
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

import lslrec.plugin.impl.dataProcessing.fftPlot.FFTPlugin;
import lslrec.plugin.impl.dataProcessing.firFilter.FIRFilterPlugin;
import lslrec.plugin.impl.dataProcessing.openposePlotter.OpenposePlotter;
import lslrec.plugin.impl.dataProcessing.openposePlotter.OpenposePlotterPlugin;
import lslrec.plugin.impl.dataProcessing.painter.DataDisplayPlugin;
import lslrec.plugin.impl.dataProcessing.zTransform.ZTransformPlugin;
import lslrec.plugin.impl.gui.alarm.PluginAlarmTest;
import lslrec.plugin.impl.gui.arithmetic.PluginArithmeticTest;
import lslrec.plugin.impl.gui.memory.PluginMemoryTest;
import lslrec.plugin.lslrecPlugin.test.LSLRecPluginTesting;

/**
 * @author Manuel Merino Monge
 *
 */
public class PlugingTesting 
{
	public static void main(String[] args) {
			
		//PluginMemoryTest plg = new PluginMemoryTest();
		//PluginArithmeticTest plg = new PluginArithmeticTest();
		//DataDisplayPlugin plg = new DataDisplayPlugin();
		//FIRFilterPlugin plg = new FIRFilterPlugin();
		//PluginAlarmTest plg = new PluginAlarmTest();
		//OpenposePlotterPlugin plg = new OpenposePlotterPlugin();
		//ZTransformPlugin plg = new ZTransformPlugin();
		FFTPlugin plg = new FFTPlugin();
		
		LSLRecPluginTesting testing = new LSLRecPluginTesting( plg );
		
		testing.startTest();
	
	}
}
