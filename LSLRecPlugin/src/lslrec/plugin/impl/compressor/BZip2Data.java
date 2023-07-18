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
package lslrec.plugin.impl.compressor;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import lslrec.auxiliar.WarningMessage;
import lslrec.config.Parameter;
import lslrec.dataStream.outputDataFile.compress.IOutZip;
import lslrec.dataStream.outputDataFile.compress.ZipDataTemplate;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.compressor.LSLRecPluginCompressor;

public class BZip2Data extends LSLRecPluginCompressor 
{	
	private String t = getID();
	
	@Override
	public String getID() 
	{	
		return "BZIP2 test";
	}

	@Override
	public JPanel getSettingPanel() 
	{
		JPanel p = new JPanel( );
		p.add( new JTextField(  t ) );
		return p;
	}

	@Override
	public List<Parameter<String>> getSettings() 
	{
		return null;
	}

	@Override
	public void loadSettings(List<Parameter<String>> arg0) 
	{	
		t = getID() + "-load";
		
		if( arg0!= null )
			t = arg0.toString();
	}

	@Override
	public IOutZip getCompressor() 
	{
		return new ZipDataTemplate() 
					{
						
						@Override
						public String getID() 
						{
							return getID();
						}
						
						@Override
						protected byte[] compressData( byte[] data ) throws Exception 
						{		
						    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream( data.length );	    
						    BZip2CompressorOutputStream bZip2 = new BZip2CompressorOutputStream( byteArrayOutputStream );
						    
						    bZip2.write( data );
						    	    
						    bZip2.close();
						    byteArrayOutputStream.close();
						    
						    byte[] compressData = byteArrayOutputStream.toByteArray();
							
							return compressData;
						}
					};
	}

	@Override
	public PluginType getType() 
	{
		return PluginType.COMPRESSOR;
	}

	@Override
	protected void setSettingPanel( JPanel arg0 ) 
	{		
	}
	
	@Override
	public int compareTo( ILSLRecPlugin o ) 
	{
		return o.getID().compareTo( this.getID() );
	}

	@Override
	public WarningMessage checkSettings() 
	{
		return new WarningMessage();
	}

	@Override
	protected void postLoadSettings() 
	{	
	}
}
