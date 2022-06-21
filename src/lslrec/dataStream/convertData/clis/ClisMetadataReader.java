package lslrec.dataStream.convertData.clis;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.spec.KeySpec;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;

import lslrec.config.language.Language;
import lslrec.dataStream.outputDataFile.format.clis.ClisMetadata;
import lslrec.gui.GuiManager;

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
	
	private Cipher decrypt = null;
	private IvParameterSpec decryptPars =  new IvParameterSpec( new byte[16] );
		
	public ClisMetadataReader( File file ) throws Exception 
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
		
		if( this.isEncryptedData() )
		{	
			JPasswordField jpf = new JPasswordField( 16 );
		    JLabel jl = new JLabel( Language.getLocalCaption( Language.DECRYPT_KEY_TEXT ) + " - " +Language.getLocalCaption( Language.PASSWORD_TEXT ) + ": ");
		    Box box = Box.createHorizontalBox();
		    box.add(jl);
		    box.add(jpf);
			
			int ok = JOptionPane.showConfirmDialog( GuiManager.getInstance().getAppUI(), box, Language.getLocalCaption( Language.PASSWORD_TEXT ), JOptionPane.OK_CANCEL_OPTION );
			
			
			String password = "";
			
		    if ( ok == JOptionPane.OK_OPTION) 
		    {
		    	char[] pass = jpf.getPassword();
		    	
		    	if( pass != null )
		    	{
		    		password = new String( pass );
		    	}
		    }
			
			this.setDecrypt( password );
			
			if( !this.checkDecryptPassword( password ) )
			{
				throw new ClisMetadataException( Language.getLocalCaption( Language.PASSWORD_TEXT ) 
													+ ": " 
													+ Language.getLocalCaption( Language.DIALOG_ERROR )  );
			}
		}
	}

	private boolean isEncryptedData()
	{
		Object encrypt = this.fields.get( ENCRYPT );
		
		return ( encrypt != null && encrypt instanceof byte[] );
	}
	
	private boolean checkDecryptPassword( String password ) throws Exception
	{
		boolean ok = this.decrypt != null && password != null;
		
		if( ok )
		{
			Object encrypt = this.fields.get( ENCRYPT );
			
			if( encrypt != null && encrypt instanceof byte[] )
			{
				byte[] pass = (byte[]) encrypt;
				
				try
				{
					pass = this.decrypt.doFinal( pass );
					byte[] dec = password.getBytes( Charset.forName( "UTF-8" ) );
					
					ok = ( dec.length == pass.length );
							
					if( ok )
					{
						for( int ip = 0; ip < dec.length && ok; ip++ )
						{
							ok = ( dec[ ip ] == pass[ ip ] );
						}
					}
				}
				catch (Exception e) 
				{
					ok = false;
				}
			}
			else
			{ 
				ok = false;
			}
		}
		
		return ok;
	}
		
	private void setDecrypt( String password ) throws Exception
	{
		/*
		SecretKeyFactory sfk = SecretKeyFactory.getInstance( ClisMetadata.getSecretKeyAlgorithm()  );
		
		byte[] stat = new byte[ 8 ];
		PBEKeySpec spec = new PBEKeySpec( password.toCharArray(), stat, 10000, 128 );
		SecretKey sk = sfk.generateSecret( spec );
		
		IvParameterSpec ivpar = new IvParameterSpec( new byte[ 16 ] );
		
		this.decrypt = Cipher.getInstance( ClisMetadata.getEncryptTransform() );
		this.decrypt.init( Cipher.DECRYPT_MODE, new SecretKeySpec( sk.getEncoded(), ClisMetadata.getEncryptID() ), ivpar );
		*/
		
		SecretKeyFactory skf = SecretKeyFactory.getInstance( ClisMetadata.getSecretKeyAlgorithm() );
		KeySpec spec = new PBEKeySpec( password.toCharArray(), new byte[ 8 ], 10000, 128 );
		SecretKey tmp = skf.generateSecret( spec );
		SecretKeySpec skey = new SecretKeySpec(tmp.getEncoded(), ClisMetadata.getEncryptID() );
				
		this.decrypt = Cipher.getInstance( ClisMetadata.getEncryptTransform() );
        this.decrypt.init( Cipher.DECRYPT_MODE, skey, this.decryptPars );
	}
	
	public Cipher getDecrypt()
	{
		return this.decrypt;
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
				this.fields.put( HEADER, bytes );
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
	
	public String getHeader()
	{
		String h = "";
		
		Object hh = this.fields.get( HEADER );
		if( hh != null )
		{
			byte[] aux = (byte[])hh;
			if( this.decrypt != null )
			{
				try 
				{ 
					aux = this.decrypt.doFinal( aux );					
					//hh = new String( this.decrypt.doFinal( hh.toString().getBytes() )  );
				}
				catch (IllegalBlockSizeException | BadPaddingException e) 
				{
					e.printStackTrace();
				}
			}
			
			h = new String( aux, Charset.forName( "UTF-8")  );
		}
		
		return h;
	}
}

