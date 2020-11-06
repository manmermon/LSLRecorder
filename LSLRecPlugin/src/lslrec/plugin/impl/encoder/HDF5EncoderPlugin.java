package lslrec.plugin.impl.encoder;

import javax.swing.JPanel;

import lslrec.auxiliar.WarningMessage;
import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.encoder.LSLRecPluginEncoder;

public class HDF5EncoderPlugin extends LSLRecPluginEncoder
{
	private Encoder enc = new HDF5Encoder(); 

	@Override
	public String getID() 
	{
		return "HDF5 test";
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

}
