/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.zTransform;

import java.util.EventListener;


/**
 * @author Manuel Merino Monge
 *
 */
public interface ZeroPoleEventListener extends EventListener 
{
	public void ZeroPoleEvent( ZeroPoleEvent  ev);
}
