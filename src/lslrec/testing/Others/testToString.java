/**
 * 
 */
package lslrec.testing.Others;

import lslrec.auxiliar.extra.Tuple;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin.PluginType;

/**
 * @author Manuel Merino Monge
 *
 */
public class testToString {
	
	public static void main(String[] args) 
	{
		Tuple< PluginType, String> t = new Tuple< PluginType, String>( PluginType.COMPRESSOR, "test" );
		
		System.out.println("testToString.main() " + t.toString() );
	}

}
