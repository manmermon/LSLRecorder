package lslrec.plugin.impl.dataProcessing.hr2hrv;

import java.util.LinkedList;
import java.util.List;

import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

public class HR2HRVProcessing  extends LSLRecPluginDataProcessing 
{
	//enum HrvType { SD, RMSSD };
	
	public static final String BUFFER_LEN = "Buffer samples";
	
	protected ParameterList pars;
	private LinkedList< Double > hrvBuffer = new LinkedList< Double >();
	private int hrvBufferLen = 20;
	
	private double HR_prev = -1;
	
	public HR2HRVProcessing( IStreamSetting setting, LSLRecPluginDataProcessing prevProc ) 
	{
		super( setting, prevProc );
		
		this.pars = new ParameterList();
		
		Parameter par = new Parameter< Integer >( BUFFER_LEN, this.hrvBufferLen );
		this.pars.addParameter( par );
	}

	@Override
	public String getID() 
	{
		return "HR2HRV";
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
	public int getOverlapOffset() 
	{
		return 1;
	}

	@Override
	public void loadProcessingSettings( List< Parameter< String > > arg0) 
	{	
		if( arg0 != null )
		{
			for( Parameter< String > p : arg0 )
			{
				String id = p.getID();
				String val = p.getValue();

				switch( id ) 
				{
					case BUFFER_LEN:
					{
						int L = Integer.parseInt( val );
						
						while( L < this.hrvBuffer.size() && !this.hrvBuffer.isEmpty() )
						{		
							this.hrvBuffer.pollFirst();
						}
						
						this.hrvBufferLen = L;
						
						this.pars.getParameter( id ).setValue( L );
						
						break;
					}
				}
			}
		}				
	}

	@Override
	protected Number[] processData(Number[] hrVector ) // HR input
	{
		LinkedList< Number > hrv = new LinkedList<Number>();
		
		if( hrVector != null && hrVector.length > 0 )
		{
			for( Number hr : hrVector )
			{
				double hrVal = hr.doubleValue();
				
				if( this.HR_prev < 0 )
				{
					this.HR_prev = hrVal;
				}
				else
				{
					double hrDif = hrVal - this.HR_prev;
					
					this.hrvBuffer.add( hrDif );
					
					if( this.hrvBuffer.size() > this.hrvBufferLen && !this.hrvBuffer.isEmpty() )
					{
						this.hrvBuffer.pollFirst();
					}
					
					//System.out.print( hrVal + "," + HR_prev + "," + this.hrvBuffer + ",");
					
					double hrvVal = this.calculeHRV();					
					
					hrv.add( hrvVal );
					
					this.HR_prev = hrVal;
				}
				
			}
		}
		
		return hrv.toArray( new Number[0] );
	}

	private double calculeHRV( )
	{
		return this.std();
	}
	
	private double std()
	{
		double hrvVal = 0;
		if( !this.hrvBuffer.isEmpty() )
		{
			double valSquare = 0;
			double mean = 0;
			for( Double v : this.hrvBuffer )
			{
				valSquare += v * v;
				mean += v;
			}
			
			mean /= this.hrvBuffer.size();
			
			hrvVal = Math.sqrt( valSquare / this.hrvBuffer.size() - mean * mean );
			
			//System.out.println( hrvVal );
		}
		
		return hrvVal;
	}
	
}
