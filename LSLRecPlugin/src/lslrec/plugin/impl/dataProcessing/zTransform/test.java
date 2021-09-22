/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.zTransform;

import java.util.ArrayList;
import java.util.List;

import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting.StreamLibrary;
import lslrec.dataStream.family.setting.SimpleStreamSetting;
import lslrec.dataStream.family.setting.StreamSettingUtils;
import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;
import lslrec.plugin.impl.dataProcessing.zTransform.Marker.Type;


/**
 * @author Manuel Merino Monge
 *
 */
public class test {

	/**
	 * @param args
	 */
	public static void main(String[] args) 
	{
		Filter f = new Filter( new SimpleStreamSetting( StreamLibrary.LSLREC, "test", StreamDataType.double64
														,1 , 1, IStreamSetting.IRREGULAR_RATE, "test1", "test2" ),null);
		Marker m1 = new Marker( 1, 0 );
		Marker m2 = new Marker( Math.cos( Math.PI / 4 ), Math.sin( Math.PI / 4 ) );
		Marker m2c = new Marker( Math.cos( Math.PI / 4 ), -Math.sin( Math.PI / 4 ) );
		
		Marker p2 = new Marker( 0.9*Math.cos( Math.PI / 4 ), 0.9*Math.sin( Math.PI / 4 ), Type.POLE );
		Marker p2c = new Marker( 0.9*Math.cos( Math.PI / 4 ), -0.9*Math.sin( Math.PI / 4 ), Type.POLE );
		
		List< Marker > markers = new ArrayList<Marker>();
		markers.add( m1 );
		markers.add( m2 );
		markers.add( m2c );
		markers.add( p2 );
		markers.add( p2c );
		
		f.setZeroPoles( markers );
		
		double[] xs = new double[] { 1, 2, 3, 4, 5, 4, 3, 2, 1 };
		List< Number > out = new ArrayList<Number>();
		for( double x : xs )
		{
			Number[] res = f.processData( new Number[] { x } );
			for( Number r : res )
			{
				out.add( r );
			}
		}		
	}

}
