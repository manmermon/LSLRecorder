/**
 * 
 */
package lslrec.plugin.impl.encoder.csv;

import java.util.List;

import lslrec.auxiliar.task.ITaskMonitor;
import lslrec.config.ParameterList;
import lslrec.config.SettingOptions;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.dataStream.outputDataFile.format.IOutputDataFileWriter;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;

/**
 * @author Manuel Merino Monge
 *
 */
public class CSVEncoder implements Encoder 
{
	@Override
	public String getID() 
	{
		return "CSV";
	}

	@Override
	public String getOutputFileExtension() 
	{
		return ".csv";
	}

	@Override
	public ParameterList getParameters() 
	{
		return null;
	}

	@Override
	public List<SettingOptions> getSettiongOptions() 
	{
		return null;
	}

	@Override
	public IOutputDataFileWriter getWriter(OutputFileFormatParameters pars, IStreamSetting strSet, ITaskMonitor monitor )
			throws Exception 
	{
		return new OutputCSVDataWriter( (String)pars.getParameter( OutputFileFormatParameters.OUT_FILE_NAME ).getValue(), monitor, pars, strSet );
	}

	@Override
	public boolean isSupportedEncryption() 
	{
		return false;
	}
}
