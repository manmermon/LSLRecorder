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
package lslrec.plugin.impl.encoder.binary;

import java.util.List;

import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lslrec.auxiliar.WarningMessage;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.encoder.LSLRecPluginEncoder;

public class BinaryEncoderPlugin extends LSLRecPluginEncoder
{
	private Encoder enc = new BinaryEncoder(); 

	@Override
	public String getID() 
	{
		return enc.getID();
	}

	@Override
	public Encoder getEncoder() 
	{
		return enc;
	}

	@Override
	public WarningMessage checkSettings() 
	{
		return new WarningMessage();
	}

	@Override
	public PluginType getType() 
	{
		return PluginType.ENCODER;
	}

	@Override
	public int compareTo(ILSLRecPlugin arg0) 
	{
		int eq = 1;
		
		if( arg0 != null )
		{
			eq = arg0.getID().compareTo( this.getID() );
		}
			
		
		return eq;
	}

	@Override
	protected void setSettingPanel(JPanel arg0) 
	{
	}
	
	@Override
	public List<Parameter<String>> getSettings() 
	{
		ParameterList parlist = enc.getParameters();
		
		List< Parameter< String > > pars = super.getSettings();
		
		for( String parId : parlist.getParameterIDs() )
		{
			Parameter par = parlist.getParameter( parId );

			Parameter< String > parCopy = new Parameter<String>( parId, par.getValue().toString() );
			
			parCopy.addValueChangeListener( new ChangeListener() 
			{				
				@Override
				public void stateChanged(ChangeEvent e) {
					
					Object val = parCopy.getValue();
					
					if( par.getValue() instanceof Boolean )
					{
						par.setValue( Boolean.parseBoolean( val.toString() ) );
					}
					else if( par.getValue() instanceof Double )
					{
						par.setValue( Double.parseDouble( val.toString() ) );
					}
					else if( par.getValue() instanceof Float )
					{
						par.setValue( Float.parseFloat( val.toString() ) );
					}
					else if( par.getValue() instanceof Long )
					{
						par.setValue( Long.parseLong( val.toString() ) );
					}
					else if( par.getValue() instanceof Integer )
					{
						par.setValue( Integer.parseInt( val.toString() ) );
					}
					else if( par.getValue() instanceof Short )
					{
						par.setValue( Short.parseShort( val.toString() ) );
					}
					else if( par.getValue() instanceof Byte )
					{
						par.setValue( Byte.parseByte( val.toString() ) );
					}
					else
					{
						par.setValue( val.toString() );
					}
				}
			});
			
			super.pars.put( parId, parCopy );			
		}
		
		return super.getSettings();
	}
	
	@Override
	protected void postLoadSettings() 
	{
		ParameterList parlist = enc.getParameters();
		
		for( Parameter< String > par : super.getSettings() )
		{
			Parameter pEnc = parlist.getParameter( par.getID() );
			
			if( pEnc != null )
			{
				Object val = pEnc.getValue();
				String newValue = par.getValue();
				
				if( val instanceof Boolean )
				{
					pEnc.setValue( Boolean.parseBoolean( newValue ) );
				}
				else if( val instanceof Double )
				{
					pEnc.setValue( Double.parseDouble( newValue ) );
				}
				else if( val instanceof Float )
				{
					pEnc.setValue( Float.parseFloat( newValue ) );
				}
				else if( val instanceof Long )
				{
					pEnc.setValue( Long.parseLong( newValue ) );
				}
				else if( val instanceof Integer )
				{
					pEnc.setValue( Integer.parseInt( newValue ) );
				}
				else if(  val instanceof Short )
				{
					pEnc.setValue( Short.parseShort( newValue ) );
				}
				else if( val instanceof Byte )
				{
					pEnc.setValue( Byte.parseByte( newValue ) );
				}
				else
				{
					pEnc.setValue( newValue );
				}
			}
		}
	}
}
