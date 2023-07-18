/**
 * 
 */
package lslrec.plugin.impl.encoder.matlab;

import javax.swing.JPanel;

import lslrec.auxiliar.WarningMessage;
import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.encoder.LSLRecPluginEncoder;

/**
 * @author Manuel Merino Monge
 *
 */
public class MatlabEncoderPlugin extends LSLRecPluginEncoder 
{
	private MatlabEncoder enc = new MatlabEncoder();
	
	@Override
	public WarningMessage checkSettings() 
	{
		WarningMessage msg = new WarningMessage( );
		
		/*
		msg.setMessage( "Unstable encoder. It may fail, it is recommended "
						+ "to change the encoder or deselect the advanced "
						+ "option to delete temporary binary files. "
						, WarningMessage.WARNING_MESSAGE );
		//*/
		
		return msg;
	}

	@Override
	public PluginType getType() 
	{
		return PluginType.ENCODER;
	}

	@Override
	public String getID() 
	{
		return "Matlab";
	}

	@Override
	public int compareTo( ILSLRecPlugin o ) 
	{
		int eq = 1;
		
		if( o != null && o.getType() == this.getType() )
		{
			eq = o.getID().compareTo( this.getID() );
		}
		
		return eq;
	}

	@Override
	public Encoder getEncoder() 
	{
		return this.enc;
	}

	@Override
	protected void setSettingPanel( JPanel arg0 ) 
	{	
	}

	@Override
	protected void postLoadSettings() 
	{	
	}
}
