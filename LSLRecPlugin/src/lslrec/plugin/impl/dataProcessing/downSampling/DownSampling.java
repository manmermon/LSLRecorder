/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.plugin.impl.dataProcessing.downSampling;

import java.util.List;

import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

/**
 * @author Manuel Merino Monge
 *
 */
public class DownSampling extends LSLRecPluginDataProcessing 
{
	public static final String DECIMATION = "Decimation"; 

	private ParameterList pars = new ParameterList();
	
	private int D = 1;
	
	private Object lock = new Object();
	
	/**
	 * @param setting
	 * @param prevProc
	 */
	public DownSampling( IStreamSetting setting, LSLRecPluginDataProcessing prevProc) 
	{
		super(setting, prevProc);
		
		this.pars.addParameter( new Parameter< Integer >( DECIMATION, 1 ) );		
	}	
	
	@Override
	public String getID() 
	{
		return "Downsampling";
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
							synchronized ( this.lock )
							{
								this.D = Integer.parseInt( val );
							}
							
							this.pars.getParameter( id ).setValue( this.D );
							
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
			int s = arg0.length;
			synchronized ( this.lock )
			{
				s /= this.D;
			}			
			
			res = new Number[ s ];
			
			for( int i = 0; i < arg0.length; i = i + 2 )
			{
				res[ i / 2 ] = arg0[ i ];
			}
		}
		
		return res;
	}

	@Override
	protected void finishProcess() 
	{	
	}
}
