/**
 * 
 */
package lslrec.plugin.impl.test;

import java.util.List;

import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.config.ParameterList;
import lslrec.config.SettingOptions;
import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.dataStream.outputDataFile.format.IOutputDataFileWriter;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.setting.DataStreamSetting;

/**
 * @author Manuel Merino Monge
 *
 */

public class HDF5Encoder implements Encoder
{
	@Override
	public List<SettingOptions> getSettiongOptions() 
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IOutputDataFileWriter getWriter( OutputFileFormatParameters pars, DataStreamSetting streamSettings, ITaskMonitor monitor )
			throws Exception 
	{	
		return new OutputHDF5DataWriter( (String)pars.getParameter( OutputFileFormatParameters.OUT_FILE_NAME ).getValue(), monitor );
	}

	@Override
	public String getOutputFileExtension() 
	{
		return ".h5";
	}

	@Override
	public boolean isSupportedEncryption() 
	{
		return false;
	}

	@Override
	public String getID() 
	{
		return "HDF5";
	}

	@Override
	public ParameterList getParameters() 
	{
		return null;
	}
}
