/**
 * 
 */
package lslrec.plugin.impl.encoder.csv;

import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

import lslrec.auxiliar.extra.FileUtils;
import lslrec.auxiliar.task.ITaskMonitor;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlock;
import lslrec.dataStream.outputDataFile.format.IOutputDataFileWriter;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;

/**
 * @author Manuel Merino Monge
 *
 */
public class OutputCSVDataWriter implements IOutputDataFileWriter 
{		
	private String fileName = null;

	private String header = "";
	
	private RandomAccessFile FWriter;
	
	private AtomicBoolean savedDataBlock = new AtomicBoolean( true );
	
	private String SeparatorChar = ",";
	private char EOL = '\n';
	
	private int addCounter = 0;
	
	private int Padding = 2;
	
	private String currentVar = null;
	private Charset charCode = Charset.forName( "UTF-8" );
	
	private byte[] padding = null;
	
	public OutputCSVDataWriter( String file, ITaskMonitor monitor, OutputFileFormatParameters pars, IStreamSetting settings ) throws Exception 
	{
		this.fileName = file;

		this.taskMonitor( monitor );
					
		this.FWriter = new RandomAccessFile( FileUtils.CreateTemporalBinFile( file ), "rw" );
		
		for( ; this.Padding > 0 ; this.Padding-- )
		{
			this.FWriter.write( EOL );
		}		
				
		long headerSize = 2;
		
		this.header = "";	
			
		if( pars != null )
		{
			this.charCode = (Charset)pars.getParameter( OutputFileFormatParameters.CHAR_CODING ).getValue();
			if( this.charCode == null )
			{
				this.charCode = Charset.forName( "UTF-8" );
			}		
		}

		if( settings != null )
		{
			String xml = settings.description();
			if( xml == null )
			{
				xml = "";
			}
						
			String names = "";
			Object aux = pars.getParameter( OutputFileFormatParameters.DATA_NAMES ).getValue();
			if( aux != null )
			{
				names = aux.toString();
			}
			
			headerSize += 2 * ( xml.toCharArray().length + ( Long.MAX_VALUE + "" ).length() );
			headerSize += names.length(); 			 
		}
		
		if( headerSize < 2 )
		{
			headerSize = 2;
		}
		
		this.padding = new byte[ (int)headerSize];

		for( int i = 0; i < this.padding.length; i++ )
		{
			this.padding[ i ] = ' ';
		}
		
		this.FWriter.write( this.padding );
	}
	
	@Override
	public void taskMonitor(ITaskMonitor arg0) 
	{	
	}

	@Override
	public void addMetadata( String id, String text ) throws Exception 
	{
		//id = id.replace("\n", "").replace("\r", "");
		//text = text.replace("\n", "").replace("\r", "");

		this.header += "\"" + id + " = " + text + "\""+ SeparatorChar;
	}

	@Override
	public void close() throws Exception 
	{
		this.FWriter.seek( 0 );
		this.header = this.header.substring(0, this.header.length()-1 );
		this.header += "\n";
		this.FWriter.write( this.header.getBytes() );
		
		this.FWriter.close();
	}

	@Override
	public boolean finished() 
	{
		return this.savedDataBlock.get();
	}

	@Override
	public String getFileName() 
	{
		return this.fileName;
	}

	@Override
	public boolean saveData( DataBlock dataBlock ) throws Exception 
	{
		this.savedDataBlock.set( false );
		
		if( dataBlock != null )
		{
			String varName = dataBlock.getName();
			long nChn = dataBlock.getNumCols();
			
			if( this.currentVar == null )
			{
				this.currentVar = varName;
				this.FWriter.write( this.currentVar.getBytes() );
				this.FWriter.write( this.EOL );
			}
			else if( !this.currentVar.equals( varName ))
			{
				this.currentVar = varName;
				
				this.FWriter.write( this.EOL );
				this.FWriter.write( this.EOL );
				this.FWriter.write( this.currentVar.getBytes()  );
				this.FWriter.write( this.EOL );
				
				this.addCounter = 0;
			}
			
			String out = "";
			for( Object val : dataBlock.getData() )
			{
				out += val.toString();
				
				this.addCounter++;
				
				if( this.addCounter < nChn )
				{
					out += this.SeparatorChar;
				}
				else
				{
					out += this.EOL;
					this.addCounter = 0;
				}
			}
			
			this.FWriter.write( out.getBytes() ); 
		}
		
		this.savedDataBlock.set( true );
		
		return true;
	}

}
