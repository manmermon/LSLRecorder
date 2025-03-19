/**
 * 
 */
package lslrec.plugin.impl.encoder.csv;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicBoolean;

import lslrec.auxiliar.extra.FileUtils;
import lslrec.auxiliar.task.ITaskMonitor;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlock;
import lslrec.dataStream.outputDataFile.format.IOutputDataFileWriter;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.exceptions.ReadInputDataException;

/**
 * @author Manuel Merino Monge
 *
 */
public class OutputCSVDataWriter implements IOutputDataFileWriter 
{		
	private String fileName = null;

	private String header = "";
	
	private RandomAccessFile FWriterMain;
	private RandomAccessFile FWriterToCopy;
	
	private AtomicBoolean savedDataBlock = new AtomicBoolean( true );
	
	private String SeparatorChar = ",";
	private char EOL = '\n';
	
	private int addCounter = 0;
	
	private int Padding = 2;
	
	private String currentVar = null;
	private Charset charCode = Charset.forName( "UTF-8" );
		
	private byte[] padding = null;
	
	private boolean separateVariables = false;
	private boolean newVar = false;
	private boolean copiedLine = false;
	private long endPadPointer = 0;	
		
	private File mainFile =  null;
	private File copyFile =  null;
	private long currentCols = 0;
	
	public OutputCSVDataWriter( String file, ITaskMonitor monitor, OutputFileFormatParameters pars, IStreamSetting settings ) throws Exception 
	{
		this.fileName = file;

		this.taskMonitor( monitor );
		
		this.separateVariables = (Boolean)pars.getParameter( CSVEncoder.SEPARATE_VARIABLE ).getValue();
		
		this.mainFile = FileUtils.CreateTemporalBinFile( file );
		this.FWriterMain = new RandomAccessFile( this.mainFile, "rw" );
		
		/*
		for( ; this.Padding > 0 ; this.Padding-- )
		{
			this.FWriterMain.write( EOL );
		}		
		//*/
				
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
		
		this.padding = new byte[ (int)headerSize + 1 + this.Padding ];

		for( int i = 0; i < this.padding.length - 1; i++ )
		{
			this.padding[ i ] = ' ';
		}
		this.padding[ this.padding.length - 1 ] = '\n';
		
		this.FWriterMain.write( this.padding );
		this.endPadPointer = this.FWriterMain.getFilePointer();
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

		String tx = "";
		
		if( id != null && !id.isEmpty() )
		{
			tx  += id + " = ";
		}
		
		if( text != null && !text.isEmpty() )
		{
			tx += text;
		}
		else if( !tx.isEmpty() )
		{
			tx += "[]";
		}
				
		this.header += "\"" + tx + "\"" + this.SeparatorChar;
	}

	@Override
	public void close() throws Exception 
	{
		/*
		if( this.currentVar != null )
		{
			String var = "</" + this.currentVar + ">";
			this.FWriter.write( var.getBytes() );
		}
		//*/
		
		if( !this.separateVariables )
		{
			this.transferRemainingRows();
		}
		
		
		
		this.FWriterMain.seek( 0 );
		this.header = ( this.header.isEmpty() ? this.header :  this.header.substring(0, this.header.length()-1 ) );
		this.header += "\n\n";
		this.FWriterMain.write( this.header.getBytes() );
		
		if( this.FWriterToCopy != null )
		{
			this.FWriterToCopy.close();
			this.copyFile.delete();	
		}
		
		this.FWriterMain.close();		
		
		this.mainFile.renameTo( new File( this.fileName ) );
	}

	@Override
	public boolean isFinished() 
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
				this.currentCols = nChn;
				
				String var = this.getNewVarHeader( varName, nChn );
								
				this.FWriterMain.write( var.getBytes() );
				this.FWriterMain.write( this.EOL );

				this.copiedLine = true;
			}
			else if( !this.currentVar.equals( varName ))
			{								
				this.currentVar = varName;
				this.newVar = true;
				
				this.copiedLine = false;
				
				/*
				String var = this.getNewVarHeader( varName, nChn );
				
				if( !this.separateVariables )
				{	
					this.FWriterMain.seek( this.endPadPointer );
					String cline = this.FWriterMain.readLine();
					this.insertValue( this.SeparatorChar + var, this.FWriterMain.getFilePointer()-1 );
					cline = this.FWriterMain.readLine();
				}
				else
				{
					this.FWriterMain.write( this.EOL );
					this.FWriterMain.write( this.EOL );
					
					this.FWriterMain.write( var.getBytes() );
					this.FWriterMain.write( this.EOL );
				}
				
				//*/
				
				String var = this.getNewVarHeader( varName, nChn ) + this.EOL;
				if( !this.separateVariables )
				{
					this.transferRemainingRows();
					
					this.copyFile = this.mainFile;
					this.FWriterToCopy = this.FWriterMain;
					this.FWriterToCopy.seek( this.endPadPointer );
									
					this.mainFile = FileUtils.CreateTemporalBinFile( ( this.mainFile.getName().endsWith("~" ) ? this.fileName : this.fileName + "~" ) );
					this.FWriterMain = new RandomAccessFile( this.mainFile, "rw" );
					this.FWriterMain.write( this.padding ); 
					
					String cline = this.FWriterToCopy.readLine();
					var = cline + this.SeparatorChar + var;	
				}
				else
				{
					var = this.EOL + var; 
				}
		
				this.currentCols = nChn;
				this.FWriterMain.write( var.getBytes() );
				
				this.addCounter = 0;
			}
			
			if( this.currentCols < nChn )
			{
				throw new ReadInputDataException( "The variable '" + varName + "', with number of channels = "+this.currentCols  
													+ "': error in data block (iseq=" + dataBlock.getSeqNum()  + "). Number of channels = " + nChn 
													+ " is greater."
													);
			}
			
			//String out = "";
			for( Object val : dataBlock.getData() )
			{
				if( this.newVar && !this.separateVariables && !this.copiedLine )
				{
					String emptyCols = this.getEmptyCols( this.currentCols - nChn );
					
					String cline = this.FWriterToCopy.readLine() + emptyCols + this.SeparatorChar;
					this.FWriterMain.write( cline.getBytes() );
					this.copiedLine = true;
				}
				
				String strVal = val.toString();
				
				if( this.addCounter > 0 )
				{
					strVal = this.SeparatorChar + strVal;
				}
				
				this.addCounter++;
				if( this.addCounter >= nChn )
				{
					strVal += this.EOL;
					this.addCounter = 0;
					this.copiedLine = false;
				}
				
				this.FWriterMain.write( strVal.getBytes() );
				
				/*
				out += val.toString();
				
				this.addCounter++;
				
				if( this.addCounter < nChn )
				{
					out += this.SeparatorChar;
				}
				else
				{
					out += this.EOL;
					
					if( !this.separateVariables && this.newVar )
					{	
						String emptyCols = this.getEmptyCols( this.currentCols - nChn );
						
						String cline = this.FWriterToCopy.readLine() + emptyCols + this.SeparatorChar + out;
						this.FWriterMain.write( cline.getBytes() );
						
						out = "";
					}
					
					this.addCounter = 0;
				}
				//*/
			}
			
			/*
			while( this.addCounter > 0 && this.addCounter < nChn )
			{
				out += this.SeparatorChar + " ";
				
				this.addCounter++;
				
				if( this.addCounter >= nChn )
				{
					out += this.EOL;
				}
			}
			//*/
			
			while( this.addCounter > 0 && this.addCounter < nChn )
			{
				String  missingVal = this.SeparatorChar + " ";
				
				this.addCounter++;
				if( this.addCounter >= nChn )
				{
					missingVal += this.EOL;
				}
				
				this.FWriterMain.write( missingVal.getBytes() );
			}
			 
			/*
			if( out.length() > 0 )
			{
				if( this.separateVariables || !this.newVar )
				{
					this.FWriterMain.write( out.getBytes() );
				}
				else
				{
					String emptyCols = this.getEmptyCols( this.currentCols - nChn );
					
					String cline = this.FWriterToCopy.readLine() + emptyCols + this.SeparatorChar + out ;
					this.FWriterMain.write( cline.getBytes() );
				}
			}
			//*/
		}
		
		this.savedDataBlock.set( true );
		
		return true;
	}
	
	private String getEmptyCols( long nCols )
	{
		String emptyCols = "";
		for( long iEmptyCols = nCols; iEmptyCols > 0; iEmptyCols-- )
		{
			emptyCols += this.SeparatorChar;
		}
		
		return emptyCols;
	}
	
	private String getNewVarHeader( String varName, long nChn ) throws IOException
	{
		String var = varName + "0";
		for( int i = 1; i < nChn; i++ )
		{
			var += this.SeparatorChar + this.currentVar + i;
		}

		return var;
	}
	
	private void transferRemainingRows() throws IOException
	{
		if( this.FWriterToCopy != null )
		{
			boolean moving = true;
			do
			{
				String cl = this.FWriterToCopy.readLine();

				moving = ( cl != null );

				if( moving )
				{
					String emptyCols = this.getEmptyCols( this.currentCols );

					cl += emptyCols + this.EOL;

					this.FWriterMain.write( cl.getBytes() );
				}
			}
			while( moving );

			this.FWriterToCopy.close();
			this.copyFile.delete();	
		}
	}
	
	private String getStringValue( String prefix, String newValues, int nCols )
	{
		String str = ( prefix != null ? prefix : "" );
		
		int cols = 0;
		
		if( !str.isEmpty() )
		{	
			cols += str.split( this.SeparatorChar ).length;
		}
		
		if( newValues != null && !newValues.isEmpty() )
		{
			cols += newValues.split( this.SeparatorChar ).length;
		}
		
		String emptyCols = "";
		
		for( int i = (nCols - cols ); i > 0; i-- )
		{
			emptyCols += this.SeparatorChar;
		}
		
		str = str + emptyCols + this.SeparatorChar + newValues;
		
		return str;
	}
	
	private void insertValue( String value, long insertPointer ) throws IOException
	{
		this.FWriterMain.seek( insertPointer );
		
		boolean end = false;
		String copy = "";
		String toWrite = value;
		long prevPointer = this.FWriterMain.getFilePointer();
		long endInsertPointer = prevPointer;
		while( !end )
		{						
			String read = this.FWriterMain.readLine();
			end = ( read == null );
			
			if( !end )
			{
				String suffix = this.EOL + "";
				if( read.endsWith( suffix ) )
				{
					suffix = "";
				}
				copy += read + suffix;				
				
				if( copy.length() >= toWrite.length() )
				{
					this.FWriterMain.seek( prevPointer );
					this.FWriterMain.write( toWrite.getBytes() );
					
					if( endInsertPointer == insertPointer )
					{
						endInsertPointer = this.FWriterMain.getFilePointer();
					}
					
					prevPointer = this.FWriterMain.getFilePointer();
					toWrite = copy;
					copy = "";
					
					
					String rl = "";
					for( String s : toWrite.split( "\n" ) )
					{
						if( s != null && !s.isEmpty() )
						{
							rl = this.FWriterMain.readLine(); // To seek to the begging of the uncopied next line
						}
					}
					
				}
			}
			else
			{
				this.FWriterMain.seek( prevPointer );
				this.FWriterMain.write( toWrite.getBytes() );
				if( copy != null && copy.length() > 0 )
				{
					this.FWriterMain.write( copy.getBytes() );
				}
			}
		}
		
		this.FWriterMain.seek( endInsertPointer );
		
		//return endInsertPointer;
	}

}
