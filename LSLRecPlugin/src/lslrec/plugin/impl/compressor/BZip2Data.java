package lslrec.plugin.impl.compressor;

import java.io.ByteArrayOutputStream;
import java.util.List;

import javax.swing.JPanel;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

import lslrec.auxiliar.WarningMessage;
import lslrec.config.Parameter;
import lslrec.dataStream.outputDataFile.compress.IOutZip;
import lslrec.dataStream.outputDataFile.compress.ZipDataTemplate;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.compressor.LSLRecPluginCompressor;

public class BZip2Data extends LSLRecPluginCompressor 
{
	@Override
	public String getID() 
	{	
		return "BZIP2 test";
	}

	@Override
	public JPanel getSettingPanel() 
	{
		return new JPanel( );
	}

	@Override
	public List<Parameter<String>> getSettings() 
	{
		return null;
	}

	@Override
	public void loadSettings(List<Parameter<String>> arg0) 
	{	
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
}
