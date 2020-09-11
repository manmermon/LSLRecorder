package lslrec.dataStream.outputDataFile.format.clis;

import java.util.ArrayList;
import java.util.List;

import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.config.ConfigApp;
import lslrec.config.SettingOptions;
import lslrec.dataStream.outputDataFile.compress.zip.OutputBZip2Data;
import lslrec.dataStream.outputDataFile.compress.zip.OutputGZipData;
import lslrec.dataStream.outputDataFile.format.Encoder;
import lslrec.dataStream.outputDataFile.format.IOutputDataFileWriter;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.outputDataFile.format.clis.parallel.OutputCLISDataParallelWriter;
import lslrec.dataStream.setting.DataStreamSetting;

public class ClisEncoder implements Encoder 
{	
	private static final List< String > SupportedZip = new ArrayList< String >();
	
	static
	{
		addCompressor( ( new OutputGZipData()).getID() );
		addCompressor(( new OutputBZip2Data()).getID() );
	}
	
	public static void addCompressor( String idZip )
	{
		SupportedZip.add( idZip );
	}
	
	@Override
	public List<SettingOptions> getSettiongOptions() 
	{
		List< SettingOptions > opts = new ArrayList< SettingOptions >();
		
		SettingOptions zips = new SettingOptions( "Compression", true, ConfigApp.OUTPUT_COMPRESSOR );
		
		String selZip = ConfigApp.getProperty( ConfigApp.OUTPUT_COMPRESSOR ).toString().toLowerCase();
		
		for( int i = 0; i <  SupportedZip.size(); i++ )
		{			
			String zip = SupportedZip.get( i );
					
			zips.addValue( zip );
			
			if( zip.toLowerCase().equals( selZip) )
			{
				zips.setSelectedValue( i );
			}
		}
		
		opts.add( zips );
		return opts;
	}

	@Override
	public IOutputDataFileWriter getWriter( OutputFileFormatParameters pars, DataStreamSetting streamSettings, ITaskMonitor monitor )
			throws Exception 
	{
		IOutputDataFileWriter wr = null;
		
		if( !pars.isParallelize() )
		{
			wr = new OutputCLISDataWriter( pars, streamSettings, monitor);
		}
		else
		{
			wr = new OutputCLISDataParallelWriter( pars, streamSettings, monitor );
		}
		
		return wr;
	}

	@Override
	public String getOutputFileExtension() 
	{
		return ".clis";
	}

	@Override
	public boolean isSupportedEncryption() 
	{
		return true;
	}

	@Override
	public String getID() 
	{
		return "CLIS";
	}

}
