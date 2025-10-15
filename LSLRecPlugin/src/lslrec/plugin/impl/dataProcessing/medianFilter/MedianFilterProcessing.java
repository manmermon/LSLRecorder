package lslrec.plugin.impl.dataProcessing.medianFilter;

import java.util.Arrays;
import java.util.List;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

public class MedianFilterProcessing extends LSLRecPluginDataProcessing 
{
	public final static String MEDIAN_FILTER_LENGTH = "median filter lenght"; 

	private ParameterList pars = new ParameterList();
	
	private Object lock = new Object();
	
	private int len = 5;
			
	public MedianFilterProcessing(IStreamSetting setting, LSLRecPluginDataProcessing prevProc) 
	{
		super(setting, prevProc);
		
		this.pars.addParameter( new Parameter< Integer>( MEDIAN_FILTER_LENGTH, len ) );
	}

	@Override
	public String getID() 
	{
		return "Median filter";
	}

	@Override
	protected void finishProcess() 
	{		
	}

	@Override
	public int getBufferLength() 
	{
		return (Integer)this.pars.getParameter( MEDIAN_FILTER_LENGTH ).getValue();
	}

	@Override
	public int getOverlapOffset() 
	{
		return 1;
	}

	@Override
	public void loadProcessingSettings( List< Parameter< String >> arg0) 
	{
		if( arg0 != null )
		{
			for( Parameter< String > par : arg0 )
			{
				String id = par.getID();
				String val = par.getValue();
				
				switch ( par.getID() )
				{
					case MEDIAN_FILTER_LENGTH:
					{
						try
						{
							synchronized ( this.lock )
							{
								this.len = Integer.parseInt( val );
							}
							
							this.pars.getParameter( id ).setValue( this.len );
							
						}
						catch (Exception e) 
						{
						}
						
						break;
					}
					default:
					{
						break;
					}
				}
			}
		}
	}

	@Override
	protected Number[] processData(Number[] arg0) 
	{
		Number[] out = new Number[] { 0D };
		
		if( arg0 != null && arg0.length > 0 )
		{
			double[] in = ConvertTo.Casting.NumberArray2DoubleArray( arg0 );
			Arrays.sort( in );
			
			int L = arg0.length;
			
			int index = L / 2;
			double v = arg0[ index ].doubleValue();
			
			if( (L % 2) == 0)
			{			
				v = (v + arg0[ index -1 ].doubleValue() )/ 2;
			}
			
			out[ 0 ] = v;			 
		}
		
		return out;
	}

}
