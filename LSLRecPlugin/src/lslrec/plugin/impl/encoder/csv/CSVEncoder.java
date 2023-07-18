/**
 * 
 */
package lslrec.plugin.impl.encoder.csv;

import java.util.ArrayList;
import java.util.List;

import lslrec.auxiliar.task.ITaskMonitor;
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
public class CSVEncoder implements Encoder 
{
	public static final String SEPARATE_VARIABLE = "Separate variables";
	
	private ParameterList parlist = new ParameterList();
	
	public CSVEncoder() 
	{
		super();
		
		this.getParameters();
	}
	
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
		if( parlist.getParameterIDs().isEmpty() )
		{
			Parameter< Boolean > par = new Parameter< Boolean >( SEPARATE_VARIABLE, false );
			
			parlist.addParameter( par );
		}
		
		return parlist;
	}

	@Override
	public List< SettingOptions > getSettiongOptions() 
	{
		//return null;
		SettingOptions opt = new SettingOptions( SEPARATE_VARIABLE, Type.BOOLEAN, false, null, SEPARATE_VARIABLE );
		opt.addValue( "false" );
		opt.setSelectedValue(0);
		
		List< SettingOptions > setops = new ArrayList< SettingOptions >();
		
		setops.add( opt );
		
		return setops;
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
		
		return new OutputCSVDataWriter( (String)pars.getParameter( OutputFileFormatParameters.OUT_FILE_NAME ).getValue(), monitor, pars, strSet );
	}

	@Override
	public boolean isSupportedEncryption() 
	{
		return false;
	}
}
