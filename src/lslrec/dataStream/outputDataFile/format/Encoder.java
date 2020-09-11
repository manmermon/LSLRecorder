package lslrec.dataStream.outputDataFile.format;

import java.util.List;

import lslrec.auxiliar.tasks.ITaskIdentity;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.config.SettingOptions;
import lslrec.dataStream.setting.DataStreamSetting;

public interface Encoder extends ITaskIdentity 
{	
	/**
	 * 
	 * @return output file extension
	 */
	public String getOutputFileExtension();
	
	/**
	 * 	
	 * @return true if data encryptation is enabled. Otherwise, false
	 */
	public boolean isSupportedEncryption();	
	
	public IOutputDataFileWriter getWriter( OutputFileFormatParameters pars, DataStreamSetting streamSettings, ITaskMonitor monitor ) throws Exception;	
	
	public List< SettingOptions > getSettiongOptions();
}
