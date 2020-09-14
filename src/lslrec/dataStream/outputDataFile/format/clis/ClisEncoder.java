package lslrec.dataStream.outputDataFile.format.clis;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.config.ConfigApp;
import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.config.SettingOptions;
import lslrec.config.SettingOptions.Type;
import lslrec.config.language.Language;
import lslrec.dataStream.outputDataFile.compress.ZipDataFactory;
import lslrec.dataStream.outputDataFile.compress.zip.BZip2Data;
import lslrec.dataStream.outputDataFile.compress.zip.GZipData;
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
		addCompressor( ( new GZipData()).getID() );
		addCompressor(( new BZip2Data()).getID() );
	}
	
	public static void addCompressor( String idZip )
	{
		SupportedZip.add( idZip );
	}
	
	@Override
	public List<SettingOptions> getSettiongOptions() 
	{
		List< SettingOptions > opts = new ArrayList< SettingOptions >();
		
		SettingOptions zips = new SettingOptions( OutputFileFormatParameters.ZIP_ID, Type.STRING, true, ConfigApp.OUTPUT_COMPRESSOR );
		
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
		
		SettingOptions parall = new SettingOptions( OutputFileFormatParameters.PARALLELIZE
													, Type.BOOLEAN, false, ConfigApp.OUTPUT_PARALLELIZE );
		parall.addValue( ConfigApp.getProperty( ConfigApp.OUTPUT_PARALLELIZE ).toString() );
		
		opts.add( zips );
		opts.add( parall );
		
		return opts;
	}
	
	@Override
	public ParameterList getParameters() 
	{
		ParameterList pars = new ParameterList();
		
		Parameter< Boolean > par1 = new Parameter<Boolean>( ConfigApp.OUTPUT_PARALLELIZE, true );
		par1.setLangID( Language.PARALLELIZE_TEXT );
		pars.addParameter( par1 );
		
		Parameter< String > par2 = new Parameter<String>( ConfigApp.OUTPUT_COMPRESSOR, ZipDataFactory.GZIP );
		par2.setLangID( Language.SETTING_COMPRESSOR );
		pars.addParameter( par2 );
		
		return pars;
	}

	@Override
	public IOutputDataFileWriter getWriter( OutputFileFormatParameters pars, DataStreamSetting streamSettings, ITaskMonitor monitor )
			throws Exception 
	{
		IOutputDataFileWriter wr = null;
		
		Parameter< Boolean> p = pars.getParameter( OutputFileFormatParameters.PARALLELIZE );
		if( p != null && p.getValue() != null && !p.getValue() )
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
