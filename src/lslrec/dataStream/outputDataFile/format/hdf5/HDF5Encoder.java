package lslrec.dataStream.outputDataFile.format.hdf5;

import java.util.List;

import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.config.SettingOptions;
import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.dataStream.outputDataFile.format.IOutputDataFileWriter;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.setting.DataStreamSetting;

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
		return new OutputHDF5DataWriter( pars.getOutputFileName(), monitor );
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

}
