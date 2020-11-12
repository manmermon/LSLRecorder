/**
 * 
 */
package lslrec.plugin.impl.test;

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
		PluginArithmeticTest plg = new PluginArithmeticTest();
		
		LSLRecPluginTesting testing = new LSLRecPluginTesting( plg );
		
		testing.startTest();
	
	}
}
