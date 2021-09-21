/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.zTransform;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.complex.Complex;

import lslrec.auxiliar.extra.Tuple;
import lslrec.config.Parameter;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

/**
 * @author Manuel Merino Monge
 *
 */
public class LTSSystem extends LSLRecPluginDataProcessing 
{
	private ZPlanePluginWindow window = null;
			
	private int BufferLen = 1;
	
	private Object lock  = new Object();
	
	public LTSSystem(IStreamSetting setting, LSLRecPluginDataProcessing prevProc) 
	{
		super(setting, prevProc);
		
		this.window = new ZPlanePluginWindow();
		this.window.setVisible( true );
	}

	@Override
	public String getID() 
	{
		return this.getClass().getSimpleName();
	}

	@Override
	protected void finishProcess() 
	{	
	}

	@Override
	public int getBufferLength() 
	{
		synchronized ( this.lock )
		{
			return this.BufferLen;
		}		
	}

	@Override
	public int getOverlapOffset() 
	{
		return 1;
	}

	@Override
	public void loadProcessingSettings(List<Parameter<String>> arg0) 
	{		
	}

	@Override
	protected Number[] processData( Number[] x ) 
	{
		Number[] res = x;
		
		if( x != null && x.length > 0 )
		{
			synchronized( this.lock )
			{
				this.BufferLen = zeros.size();
				if( this.BufferLen < 1 )
				{
					this.BufferLen = 1;
				}
			}
		}
		
		return res;
	}

}
