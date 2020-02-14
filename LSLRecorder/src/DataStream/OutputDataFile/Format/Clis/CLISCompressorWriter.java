package DataStream.OutputDataFile.Format.Clis;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import DataStream.OutputDataFile.IOutputDataFileWriter;
import edu.ucsd.sccn.LSLUtils;

public class CLISCompressorWriter 
{
	private String fileName;
	
	private RandomAccessFile fStream = null;
	
	private String currentVarName = "";
	private int currentType = IOutputDataFileWriter.FLOAT_TYPE;	
	private int currentNumCols = 0;

	private List< Integer > blockSizes = null;
	
	private CLISMetadata metadata;
	
	public CLISCompressorWriter( String file, CLISMetadata meta ) throws Exception 
	{
		this.metadata = meta;
		
		this.fileName = file;
		
		this.fStream = new RandomAccessFile( new File( file ), "rw" );
		
		long headersize = this.metadata.getHeaderSize();
		for( ; headersize > 0; headersize-- )
		{
			this.fStream.write( this.metadata.getPadding() );
		}
		
		this.blockSizes = new ArrayList< Integer >();
	}
	
	public void addMetadata( String id, String text )
	{
		this.metadata.addMetadataHeader( id, text );
	}
	
	public Charset getCharCode()
	{
		return this.metadata.getCharCode();
	}
	
	public String getFileName() 
	{
		return this.fileName;		
	}
	
	public String getSimpleFileName()
	{
		String simpleFileName = this.fileName;
		
		String sep = FileSystems.getDefault().getSeparator();
				
		int index = this.fileName.lastIndexOf( sep );
		
		if( index < 0 )
		{
			String[] opts = new String[] { "/", "\\" };
 			
			for( String sp : opts )
			{
				index = this.fileName.lastIndexOf( sp );
				
				if( index >= 0 )
				{
					break;
				}	
			}
		}
		
		if( index > 0 )
		{
			simpleFileName.substring( index );
		}
		
		return simpleFileName;
	}
	
	public void saveCompressedData( byte[] compressData, String varName, int dataType, int nCols ) throws Exception
	{
		if( !this.currentVarName.equals( varName ) )
		{
			if( !this.currentVarName.isEmpty() )
			{
				this.metadata.addMetadataProtocolInfo( this.currentVarName, this.getDataTypeIdentifier( this.currentType )
													, LSLUtils.getDataTypeBytes( this.currentType ) //this.getBytesCurrentDataType( this.currentType )
													, this.currentNumCols
													, this.blockSizes );
			}
			
			this.currentVarName = varName;
			this.currentType = dataType;
			this.currentNumCols = nCols;			
			
			this.blockSizes.clear();
		}
		else if( this.currentType != dataType )
		{			
				throw new IllegalStateException( "More than one type data for the same variable." );
		}
		else if( this.currentNumCols != nCols )
		{
			throw new IllegalStateException( "Number of columns changed for the same variable." );
		}
		
		this.blockSizes.add( compressData.length );
		
		if( this.fStream != null )
		{
			this.fStream.write( compressData );		
		}
	}
	
	public void saveMetadata() throws Exception
	{
		if( this.fStream != null )
		{
			this.metadata.addMetadataProtocolInfo(  this.currentVarName
												, this.getDataTypeIdentifier( this.currentType )
												, LSLUtils.getDataTypeBytes( this.currentType ) // this.getBytesCurrentDataType( this.currentType )
												, this.currentNumCols, this.blockSizes );
			
			String head = this.metadata.getMetaDataProtocol();
	
			CharBuffer charBuffer = CharBuffer.wrap( head.toCharArray() );
			ByteBuffer byteBuffer = this.metadata.getCharCode().encode( charBuffer );
			byte[] bytes = Arrays.copyOfRange( byteBuffer.array(), byteBuffer.position(), byteBuffer.limit() );
	
			this.fStream.seek( 0 );
			this.fStream.write( bytes );
	
			if ( this.metadata.hasHeader() )
			{
				charBuffer = CharBuffer.wrap( this.metadata.getHeader() );
				byteBuffer = this.metadata.getCharCode().encode(charBuffer);
				bytes = Arrays.copyOfRange( byteBuffer.array(), byteBuffer.position(), byteBuffer.limit() );
				this.fStream.write( bytes );
			}
		}
	}
	
	public void close() throws Exception
	{
		if( this.fStream != null )
		{
			this.fStream.close();
		}
	}
	
	private String getDataTypeIdentifier( int type )
	{
		String id = "float";
		
		switch ( type )
		{
			case( IOutputDataFileWriter.BYTE_TYPE ):
			{			
				id = "int";
				
				break;
			}
			case( IOutputDataFileWriter.SHORT_TYPE ):
			{
				id = "int";
				
				break;
			}
			case( IOutputDataFileWriter.INT_TYPE ):
			{
				id = "int";
				
				break;
			}
			case( IOutputDataFileWriter.LONG_TYPE ):
			{
				id = "int";
				
				break;
			}
			case( IOutputDataFileWriter.STRING_TYPE ):
			{
				id = "char";
				break;
			}
			default:
			{
				break;
			}
		}
		
		return id;
	}

}
