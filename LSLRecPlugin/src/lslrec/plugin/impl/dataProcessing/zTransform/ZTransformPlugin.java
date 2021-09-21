/**
 * 
 */
package lslrec.plugin.impl.dataProcessing.zTransform;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import lslrec.auxiliar.WarningMessage;
import lslrec.config.Parameter;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.processing.LSLRecPluginDataProcessing;

/**
 * @author Manuel Merino Monge
 *
 */
public class ZTransformPlugin implements ILSLRecPluginDataProcessing  
{

	@Override
	public WarningMessage checkSettings() 
	{
		return new WarningMessage();
	}

	@Override
	public JPanel getSettingPanel() 
	{
		return null;
	}

	@Override
	public List< Parameter< String > > getSettings() 
	{
		return new ArrayList< Parameter< String > >();
	}

	@Override
	public void loadSettings( List<Parameter<String>> arg0 ) 
	{	
	}

	@Override
	public PluginType getType() 
	{
		return PluginType.DATA_PROCESSING;
	}

	@Override
	public String getID() 
	{
		return "ZPlane";
	}

	@Override
	public int compareTo(ILSLRecPlugin o) 
	{
		return this.getID().compareTo( o.getID() );
	}

	@Override
	public LSLRecPluginDataProcessing getProcessing( IStreamSetting arg0, LSLRecPluginDataProcessing arg1 )
	{
		return new LTSSystem( arg0, arg1 );
	}
	
}
