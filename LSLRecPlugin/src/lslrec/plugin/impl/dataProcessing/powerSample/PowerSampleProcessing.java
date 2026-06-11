package lslrec.plugin.impl.dataProcessing.powerSample;

import java.util.List;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

public class PowerSampleProcessing extends LSLRecPluginDataProcessing 
{
	public static final String EXP = "Exponent";
	
	private ParameterList pars = new ParameterList();
	
	private double exp = 2D;
	
	public PowerSampleProcessing(IStreamSetting setting, LSLRecPluginDataProcessing prevProc) 
	{
		super(setting, prevProc);
		
		this.pars.addParameter( new Parameter< Double >(EXP, exp ) );
	}

	@Override
	public String getID() 
	{
		return "PowerSample";
	}

	@Override
	protected void finishProcess() 
	{		
	}

	@Override
	public int getBufferLength() 
	{
		return 1;
	}

	@Override
	public int getShiftOffset() 
	{	
		return 1;
	}

	@Override
	public void loadProcessingSettings(List< Parameter< String >> arg0) 
	{
		if( arg0 != null )
		{
			for( Parameter< String > p : arg0 )
			{
				String id = p.getID();
				String val = p.getValue();
				
				switch ( id ) 
				{
					case EXP:
					{	
						try
						{
							this.exp = Double.parseDouble( val );
							this.pars.getParameter( id ).setValue( exp );
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
			
			for( int i = 0; i < in.length; i++ )
			{
				out[ i ] = Math.pow( in[ i ], this.exp );
			}
		}
		
		return out;
	}

}
