package lslrec.plugin.impl.dataProcessing.firFilter;

import java.util.List;

import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.dataStream.setting.DataStreamSetting;
import lslrec.plugin.impl.dataProcessing.firFilter.FilterWindow.WindowType;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

/**
 * @author Manuel Merino Monge
 *
 */
public class FIRFilter extends LSLRecPluginDataProcessing 
{
	enum FilterType { LOWPASS_FILTER, HIGHPASS_FILTER, BANDPASS_FILTER, NOTCH_FILTER };
	
	public static final String FILTER_TYPE = "filter type";	
	public static final String CUT_FREQ1 = "cut frequency 1";
	public static final String CUT_FREQ2 = "cut frequency 2";
	public static final String FILTER_LENGTH = "filter length";
	public static final String WINDOW_TYPE = "window type";
		
	protected ParameterList pars;
	
	protected Object lock = new Object();
	
	protected double[] h = null;
	
	/**
	 * @param setting
	 * @param prevProc
	 */
	public FIRFilter( DataStreamSetting setting, LSLRecPluginDataProcessing prevProc ) 
	{
		super( setting, prevProc );
		
		this.pars = new ParameterList();
		
		Parameter par = new Parameter< Integer >( FILTER_LENGTH, 100 );
		this.pars.addParameter( par );
		
		par = new Parameter< Integer >( CUT_FREQ1, 48 );
		this.pars.addParameter( par );
		
		par = new Parameter< Integer >( CUT_FREQ2, 52 );
		this.pars.addParameter( par );
		
		par = new Parameter< FilterWindow.WindowType >( WINDOW_TYPE, FilterWindow.WindowType.HAMMING );
		this.pars.addParameter( par );
		
		par = new Parameter< FilterType >( FILTER_TYPE, FilterType.LOWPASS_FILTER );
		this.pars.addParameter( par );
		
		this.calculateFilterCoefficients();
	}

	@Override
	public int getMinDataLength2Process() 
	{
		return 0;
	}

	@Override
	public void loadProcessingSettings(List<Parameter<String>> arg0) 
	{
		if( arg0 != null )
		{
			for( Parameter< String > p : arg0 )
			{
				String id = p.getID();
				String val = p.getValue();
				
				switch( id ) 
				{
					case FILTER_LENGTH:
					{
						int L = Integer.parseInt( val );
						
						this.pars.getParameter( id ).setValue( L );
						
						break;
					}
					case CUT_FREQ1:					
					case CUT_FREQ2:
					{
						double L = Double.parseDouble( val );
						
						this.pars.getParameter( id ).setValue( L );
						
						
						break;
					}
					case WINDOW_TYPE:
					{
						WindowType t = WindowType.valueOf( val );
						
						this.pars.getParameter( id ).setValue( t );
						
						break;
					}
					case FILTER_TYPE:
					{
						FilterType t = FilterType.valueOf( val );
						
						this.pars.getParameter( id ).setValue( t );
						
						break;
					}
					default:
					{
						break;
					}
				}
			}
			
			this.calculateFilterCoefficients();
		}				
	}
		
	@Override
	protected Number[] processData(Number[] x) 
	{
		Number[] res = null;
		
		if( x != null )
		{
			synchronized ( this.lock ) 
			{
				int L = x.length - this.h.length;
				
				
				if( L  < 0 )
				{
					res = new Number[ 1 ];
					
					double s = 0D;
					int offsetH = -L;
					for( int i = 0; i < x.length; i++ )
					{
						double x0 = x[ i ].doubleValue();
						double h0 = this.h[ i + offsetH ]; // h's order is inverted when it was created.
	
						s = s + x0 * h0;		
					}
				}
				else
				{
					res = new Number[ L + 1 ];
					
					for( int offset = 0; offset <= L; offset++ )
					{
						double s = 0D;
						for( int i = 0; i < this.h.length; i++ )
						{
							double x0 = x[ i + offset ].doubleValue();
							double h0 = this.h[ i ]; // h's order is inverted when it was created.
									
							s = s + x0 * h0;		
						}					
						
						res[ offset ] = s;
					}
				}
			}
		}
		
		return res;
	}
	
	private void calculateFilterCoefficients()
	{
		int L = (int)this.pars.getParameter( FILTER_LENGTH ).getValue();
		double Fm = super.streamSetting.getSamplingRate();
		double Fc1 = (double)this.pars.getParameter( CUT_FREQ1 ).getValue();
		double Fc2 = (double)this.pars.getParameter( CUT_FREQ2 ).getValue();
		WindowType wt = (WindowType)this.pars.getParameter( WINDOW_TYPE ).getValue();
		
		synchronized( this.lock )
		{
			if( Fm > 0 )
			{	
				FilterType ft = (FilterType)this.pars.getParameter( FILTER_TYPE ).getValue();
				
				switch ( ft ) 
				{
					case HIGHPASS_FILTER:
					{
						this.h = FIRFilterCoefficients.FIRHighpassFilterCoefficients(L, Fc1 / Fm, wt );
						
						break;
					}
					case BANDPASS_FILTER:
					{
						this.h = FIRFilterCoefficients.FIRBandpassFilterCoefficients(L, Fc1 / Fm, Fc2 / Fm, wt );
						
						break;
					}
					case NOTCH_FILTER:
					{
						this.h = FIRFilterCoefficients.FIRNotchFilterCoefficients(L, Fc1 / Fm, Fc2 / Fm, wt );
						
						break;
					}
					default:
					{
						this.h = FIRFilterCoefficients.FIRLowpassFilterCoefficients(L, Fc1 / Fm, wt );
						
						break;
					}
				}
				
			}
			else
			{
				this.h = new double[ 0 ];
			}
		}
	}
}