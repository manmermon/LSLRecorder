/* 
 * Copyright 2018-2026 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec. https://github.com/manmermon/LSLRecorder
 *	 
 *   LSLRec is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   LSLRec is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with LSLRec.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */
package lslrec.plugin.impl.dataProcessing.WeightInputs;

import java.util.List;

import lslrec.config.Parameter;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

public class WeightInputsProcessing extends LSLRecPluginDataProcessing
{
	public static final String MULT = "Multiplier";
	public static final String DIV = "Divisor";
	public static final String INV = "Inverse";
	
	private double mult = 1;
	private double div = 1;
	
	private double scale = 1;
	private boolean inv = false;	
	
	public WeightInputsProcessing(IStreamSetting setting, LSLRecPluginDataProcessing prevProc) 
	{
		super(setting, prevProc);
	}

	@Override
	public String getID() 
	{
		return "WeigthInputs";
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
	public void loadProcessingSettings(List<Parameter<String>> pars ) 
	{
		for( Parameter< String > par : pars )
		{
			String id = par.getID();
			String val = par.getValue();
			
			try
			{
				switch( id ) 
				{					
					case MULT:
					{		
						Double m = Double.parseDouble( val );
						this.mult = m;
						
						break;
					}
					case DIV:
					{
						Double d = Double.parseDouble( val );
						this.div = (d != 0 ) ? d : 1;
						
						break;
					}
					case INV:
					{
						this.inv = Boolean.parseBoolean( val );
						
						break;
					}
					default:
						break;
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		
		this.scale = this.mult / this.div;
	}

	@Override
	protected Number[] processData( Number[] in ) 
	{	
		Double[] res = new Double[0];
		
		if( in != null  )
		{
			res = new Double[ in.length ];
			
			for( int i = 0; i < in.length; i++ )
			{
				double val = in[ i ].doubleValue();
				
				val = ( this.inv ) ? 1 / val : val;
				
				res[ i ] = val * this.scale;
			}
		}
		
		return res;
	}
}
