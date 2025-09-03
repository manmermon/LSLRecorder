/**
 * 
 */
package lslrec.plugin.impl.encoder.binary;

import java.util.ArrayList;
import java.util.List;

import lslrec.auxiliar.task.ITaskMonitor;
import lslrec.config.ConfigApp;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.config.SettingOptions;
import lslrec.config.SettingOptions.Type;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.dataStream.outputDataFile.format.IOutputDataFileWriter;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;

/**
 * @author Manuel Merino Monge
 *
 */
public class BinaryEncoder implements Encoder 
{
	private ParameterList parlist = new ParameterList();
	
	public BinaryEncoder() 
	{
		super();
		
		this.getParameters();
	}
	
	@Override
	public String getID() 
	{
		return ConfigApp.shortNameApp + "BIN";
	}

	@Override
	public String getOutputFileExtension() 
	{
		return ".temp";
	}

	@Override
	public ParameterList getParameters() 
	{		
		return parlist;
	}

	@Override
	public List< SettingOptions > getSettiongOptions() 
	{
		return null;
	}

	@Override
	public IOutputDataFileWriter getWriter(OutputFileFormatParameters pars, IStreamSetting strSet, ITaskMonitor monitor )
			throws Exception 
	{
		for( String parId : this.parlist.getParameterIDs() )
		{
			Parameter par = this.parlist.getParameter( parId );
				
			if( pars.getParameter( par.getID() ) == null )
			{
				pars.setParameter( parId, par.getValue() );
			}
		}
		
		return new OutputBinaryDataWriter( (String)pars.getParameter( OutputFileFormatParameters.OUT_FILE_NAME ).getValue(), monitor, pars, strSet );
	}

	@Override
	public boolean isSupportedEncryption() 
	{
		return false;
	}
}
