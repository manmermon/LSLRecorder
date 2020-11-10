/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.downSampling;

import java.util.List;

import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.dataStream.family.stream.lsl.DataStreamSetting;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

/**
 * @author Manuel Merino Monge
 *
 */
public class DownSampling extends LSLRecPluginDataProcessing 
{
	public static final String DECIMATION = "Decimation"; 

	private ParameterList pars = new ParameterList();
	
	/**
	 * @param setting
	 * @param prevProc
	 */
	public DownSampling(DataStreamSetting setting, LSLRecPluginDataProcessing prevProc) 
	{
		super(setting, prevProc);
		
		this.pars.addParameter( new Parameter< Integer >( DECIMATION, 1 ) );
	}	
	
	@Override
	public int getBufferLength() 
	{
		return (Integer)this.pars.getParameter( DECIMATION ).getValue();
	}

	@Override
	public int getOverlapOffset() 
	{
		return this.getBufferLength();
	}

	@Override
	public void loadProcessingSettings( List< Parameter< String > > arg0) 
	{
		if( arg0 != null )
		{
			for( Parameter< String > par : arg0 )
			{
				String id = par.getID();
				String val = par.getValue();
				
				switch ( par.getID() )
				{
					case DownSampling.DECIMATION:
					{
						try
						{
							this.pars.getParameter( id ).setValue( Integer.parseInt( val ) );
						}
						catch (Exception e) 
						{
						}
						
						break;
					}
					default:
						break;
				}
			}
		}
	}

	@Override
	protected Number[] processData(Number[] arg0) 
	{
		Number[] res = null;
		
		if( arg0 != null )
		{
			int D = (Integer)this.pars.getParameter( DownSampling.DECIMATION ).getValue();
			if( D <= 0 )
			{
				D = 1;				
			}
			
			int s = arg0.length / D;
			
			res = new Number[ s ];
			
			for( int i = 0; i < arg0.length; i = i + 2 )
			{
				res[ i / 2 ] = arg0[ i ];
			}
		}
		
		return res;
	}

}
