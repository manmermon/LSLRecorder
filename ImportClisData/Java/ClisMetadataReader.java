package ImportClisData.Java;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClisMetadataReader 
{
	private final String VER = "ver";
	private final String COMPRESS = "compress";
	private final String HEADER_BYTE_SIZE = "headerByteSize";
	private final String HEADER = "header";
	private final String EXTENSION = "extension";
	private final String ENCRYPT = "encrypt";
	private final String CHECKSUM = "checksum";
	private final String VARIABLES = "variables";
	
	private Map< String, Object > fields = null; 
	
	private RandomAccessFile fileStreamReader = null;
	
	public ClisMetadataReader( File file ) throws IOException, ClisMetadataException 
	{
		this.fileStreamReader = new RandomAccessFile( file, "r" );
		 		
		String metadata = this.fileStreamReader.readLine();  		
		
		this.fields = new HashMap<String, Object>();		
		
		String[] parts  = metadata.split( ";" );
		
		this.getVersion( parts[ 0 ] );	
		
		if( ((Float)this.fields.get( VER ) ) == 2.1F )
		{
			this.processMetadataFields( Arrays.copyOfRange( parts, 1, parts.length ) );
		}
	}
	

	private void getVersion( String vers ) throws ClisMetadataException
	{
		String[] ver = vers.split( "=" );
		
		if( ver.length != 2 )
		{
			throw new ClisMetadataException( "Version field unknown." );
		}
		
		if( !ver[ 0 ].toLowerCase().equals( VER.toLowerCase() ) )
		{
			throw new ClisMetadataException( "Version field unknown." );				
		}
		
		try
		{
			Float verNum = Float.valueOf( ver[ 1 ] );			
			
			this.fields.put( VER, verNum );
		}
		catch ( Exception e) 
		{
			throw new ClisMetadataException( "Version number is not a number." );
		}		
	}

	private void processMetadataFields( String[] subfields ) throws ClisMetadataException, IOException
	{
		String[] compress = subfields[ 0 ].split( "=" );
		String[] headerSize = subfields[ 1 ].split( "=" );
		
		String extension = subfields[ subfields.length - 1 ];
		String[] dataInfo = subfields[ subfields.length - 2 ].split( "," );
		
		String[] vars = Arrays.copyOfRange( subfields, 2, subfields.length - 2 );
		
		if( compress.length != 2 || headerSize.length != 2 || dataInfo.length != 2 )
		{
			throw new ClisMetadataException();
		}
		
		if( !compress[ 0 ].toLowerCase().equals( COMPRESS.toLowerCase() ) )
		{
			throw new ClisMetadataException( "Compress ID incorrect." );
		}
		
		this.fields.put( COMPRESS, compress[ 1 ] );
		
		if( !headerSize[ 0 ].toLowerCase().equals( HEADER_BYTE_SIZE.toLowerCase() ) )
		{
			throw new ClisMetadataException( "HeaderByteSize ID incorrect." );
		}
		
		this.fields.put( HEADER_BYTE_SIZE, Integer.valueOf( headerSize[ 1 ] ) );
		
		try
		{
			Boolean ext = Boolean.valueOf( extension );
			
			this.fields.put( EXTENSION,  ext );
		}
		catch ( Exception e) 
		{
			throw new ClisMetadataException( "Metadata extension value is not a corrected boolean." );
		}
		
		if( !dataInfo[ 0 ].toLowerCase().equals( HEADER.toLowerCase() ) )
		{
			throw new ClisMetadataException( "Header ID incorrect" );
		}
		
		try
		{
			Integer headerLen = Integer.valueOf( dataInfo[ 1 ] );
			
			this.fields.put( HEADER, headerLen );
		}
		catch ( Exception e) 
		{
			throw new ClisMetadataException( "Header value is not a integer" );
		}
		
		List< MetadataVariableBlock > variables = new ArrayList< MetadataVariableBlock >();
		for( int i = 0; i < vars.length; i++ )
		{
			variables.add( new MetadataVariableBlock( vars[ i ] ) );
		}
		
		this.fields.put( VARIABLES, variables );
		
		if( (Boolean)this.fields.get( EXTENSION ) )
		{
			String metadataExt = this.fileStreamReader.readLine();
			
			this.processMetadataExtension( metadataExt );
		}
		
		Object encryptLen = this.fields.get( ENCRYPT );
		
		if( encryptLen != null )
		{
			int len = (Integer)encryptLen;
			
			if( len > 0 )
			{
				byte[] encryptKey = new byte[ len ];
				
				
	 			if( this.fileStreamReader.read( encryptKey ) > 0 )
				{
					this.fields.put( ENCRYPT, encryptKey );
				}
			}
		}
		
		int headerLen = (int)this.fields.get( HEADER );
		if( headerLen > 0 )
		{
			byte[] bytes = new byte[ headerLen ];
			
			if( this.fileStreamReader.read( bytes ) > 0 )
			{
				this.fields.put( HEADER, new String( bytes ) );
			}
		}

		int metadataSize = (Integer)this.fields.get( HEADER_BYTE_SIZE );
		
		this.fileStreamReader.seek( metadataSize );		
	}
		
	private void processMetadataExtension( String extension ) throws ClisMetadataException
	{
		String[] parts = extension.split( ";" );
		
		int counter = 0;
		for( String field : parts )
		{
			if( !field.isEmpty() )
			{
				String[] p = field.split( "=" );
				
				switch ( counter )
				{
					case 0:
					{
						if( p.length != 2 )							
						{
							throw new ClisMetadataException( "Encrypt badly formed" );
						}
						
						if( !p[ 0 ].toLowerCase().equals( ENCRYPT.toLowerCase() ) )
						{
							throw new ClisMetadataException( "Encrypt ID not found" );
						}
						
						this.fields.put( ENCRYPT, Integer.valueOf( p[ 1 ] ) );
						
						break;
					}
					case 1:
					{
						if( p.length != 2 )							
						{
							throw new ClisMetadataException( "Checksum badly formed" );
						}
						
						if( !p[ 0 ].toLowerCase().equals( CHECKSUM.toLowerCase() ) )
						{
							throw new ClisMetadataException( "Checksum ID not found" );
						}
						
						this.fields.put( CHECKSUM, p[ 1 ] );
						
						break;
					}
					default:
						break;
				}
				
				counter++;
			}
		}
	}
	
	public RandomAccessFile getFileReader()
	{
		return this.fileStreamReader;
	}
	
	public void closeFileReader() throws IOException
	{
		this.fileStreamReader.close();
	}
	
	public String getCompressTechnique()
	{
		return this.fields.get( COMPRESS ).toString();
	}
	
	public List< MetadataVariableBlock > getVariables()
	{
		return (List< MetadataVariableBlock >)this.fields.get( VARIABLES );
	}
	
	public int getMetadataByteSize()
	{
		return (Integer)this.fields.get( HEADER_BYTE_SIZE );
	}
}

