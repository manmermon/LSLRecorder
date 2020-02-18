package DataStream.OutputDataFile.Format.Clis;

import java.nio.charset.Charset;
import java.security.Key;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.spec.SecretKeySpec;

import Config.ConfigApp;
import DataStream.OutputDataFile.Compress.IOutZip;
import DataStream.OutputDataFile.Compress.OutputZipDataFactory;
import DataStream.OutputDataFile.Format.OutputFileFormatParameters;


public class CLISMetadata 
{
	private final String ENCRYPT_ALGORITHM = "AES";
    private final String ENCRYPT_TRANSFORMATION = "AES";
	
    private final int ENCRYPT_KEY_LEN = 16;
    private final String ENCRYPT_PASSWORD_PADDING = "_";
	
	private final String ID_LABEL_VER = "ver";
	private final String ID_LABEL_COMPRESS = "compress";
	private final String ID_LABEL_HEADERSIZE = "headerByteSize";
	private final String ID_LABEL_CHECKSUM = "checksum";
	private final String ID_LABEL_ENCRYPT = "encrypt";
	
	private final String HEADER_ASSIG_SYMBOL = "=";
	
	private String headerInfo = "";
	private String headerStreamInfo = "";
	private String headerInfoExtension = "";
	
	private String header = "";
	
	private final String endLine = "\n";

	private final String GrpSep = ";" ;
	private final String fieldSep = "," ;
 
	private String zip_text_id = "GZIP";
	private Integer zip_id = OutputZipDataFactory.GZIP;

	//private boolean addedStreamDataInfo = false;
	
	private Charset charCode;
	
	private byte[] padding = null;
	
	private long headerSize = 0;
	
	private double version = 2.1;
	
	private final String checkSumAlg_ID = "MD5";
	private MessageDigest checkSum = null;	
	private boolean checkSumGenerated = false;
	
	private Cipher cipher = null;
	
	public CLISMetadata( OutputFileFormatParameters pars  ) throws Exception 
	{	
		this.zip_id = pars.getCompressType();
		
		if( this.zip_id == null )
		{
			this.zip_id = OutputZipDataFactory.UNDEFINED;
		}
		
		IOutZip zipProcess = OutputZipDataFactory.createOuputZipStream( this.zip_id );
		
		if( zipProcess == null )
		{
			throw new IllegalArgumentException( "Compress technique unknown." );
		}
		
		this.zip_text_id = zipProcess.getZipID();
				
		this.charCode = pars.getCharset();
		if( this.charCode == null )
		{
			this.charCode = Charset.forName( "UTF-8" );
		}
		
		String encryptKey = pars.getEncryptKey();
		
		String encrpytMetadata = "";
		
		if( encryptKey != null && !encryptKey.isEmpty() )
		{
			String password = encryptKey;
			
			while( password.length() < ENCRYPT_KEY_LEN )
			{
				password += ENCRYPT_PASSWORD_PADDING;
			}
			
			Key secretKey = new SecretKeySpec( password.getBytes() , this.getEncryptID() );
	        this.cipher = Cipher.getInstance( this.getEncryptTransform() );	        	        
	        this.cipher.init( Cipher.ENCRYPT_MODE, secretKey );		        
	        
	        encrpytMetadata = new String( cipher.doFinal( encryptKey.getBytes() ) );		
		}
		
		Long numBlocks = pars.getNumerOfBlocks();
		if( numBlocks == null )
		{
			numBlocks = 2L;
		}

		String xml = pars.getDataInfo( );
		if( xml == null )
		{
			xml = "";
		}
		
		Integer chs = pars.getChannels();
		if( chs == null )
		{
			chs = 1;
		}
		
		String names = pars.getDataNames();
		if( names == null )
		{
			names = "";
		}

		int encryptBlockSize = 1;
		String encryptID = pars.getEncryptID();	
		if( encryptID != null )
		{
	        Cipher cipher = Cipher.getInstance( encryptID );
	        
	        encryptBlockSize = cipher.getBlockSize();
		}		

		Integer BLOCK_SIZE = pars.getBlockDataLength();
		
		if( BLOCK_SIZE == null )
		{
			BLOCK_SIZE = ConfigApp.DEFAULT_SEGMENTATION_BLOCK_SIZE;
		}
		
		BLOCK_SIZE = (int)( Math.ceil( BLOCK_SIZE / encryptBlockSize) * encryptBlockSize );
		
		int blockSizeStrLen = Integer.toString( BLOCK_SIZE ).length() + 1;
		
		
		this.headerSize = numBlocks * blockSizeStrLen;		
			
		this.headerSize += xml.toCharArray().length + cipher.getBlockSize();
		this.headerSize += ( names.length() + 10 ) * 4 ; // device info, binary and time data; 10 -> length of data type in string
		this.headerSize += Integer.toString( chs + 1 ).length() * 2; // channel numbers 
		
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		dfs.setGroupingSeparator(','); 
		
		DecimalFormat df = new DecimalFormat("#.0#", dfs);
						
		this.headerInfo = this.ID_LABEL_VER + this.HEADER_ASSIG_SYMBOL
								+ df.format( this.version ) + this.GrpSep 
							+ this.ID_LABEL_COMPRESS + this.HEADER_ASSIG_SYMBOL
								+ this.zip_text_id + this.GrpSep
							+ this.ID_LABEL_HEADERSIZE + this.HEADER_ASSIG_SYMBOL;
		
		this.headerInfoExtension = this.ID_LABEL_ENCRYPT + this.HEADER_ASSIG_SYMBOL 
										+ encrpytMetadata + this.GrpSep
										+ this.ID_LABEL_CHECKSUM + this.HEADER_ASSIG_SYMBOL;
							
		this.checkSum = MessageDigest.getInstance( this.checkSumAlg_ID );
		
		int checkSumLen = this.checkSum.getDigestLength();
		
		
		this.headerSize += ( this.headerInfo.length() + this.headerInfoExtension.length() + checkSumLen ) * 2;
				
		//byte[] padding = new byte[ Character.BYTES ];
		int charByteSize = this.headerInfo.getBytes( this.charCode ).length / this.headerInfo.length(); 
		this.padding = new byte[ charByteSize ];
		
		for( int i = 0; i < this.padding.length; i++ )
		{
			this.padding[ i ] = '\r';
		}
		
		this.headerInfo += ( this.headerSize * padding.length ) + this.GrpSep;
	}
	
	public long getHeaderSize() 
	{
		return this.headerSize;
	}
	
	public Cipher getEncrypt() 
	{
		return this.cipher;
	}
	
	public String getEncryptID()
	{
		return this.ENCRYPT_ALGORITHM;
	}
	
	public String getEncryptTransform()
	{
		return this.ENCRYPT_TRANSFORMATION;
	}
	
	public byte[] getPadding() 
	{
		return this.padding;
	}
	
	public void addMetadataProtocolInfo(String name, String type, int typeBytes, int col, List< Integer > blockSizes )
	{
		this.headerStreamInfo = this.headerStreamInfo + name + this.fieldSep + type + this.fieldSep + typeBytes 
							+ this.fieldSep + col;
		
		if( blockSizes.size() == 0 )
		{
			this.headerStreamInfo += this.fieldSep + 0;
		}	
		else
		{
			for( Integer size : blockSizes )
			{
				this.headerStreamInfo += this.fieldSep + size; 
			}
		}
							
		this.headerStreamInfo += this.GrpSep;
	}
	
	public void addMetadataHeader( String id, String text ) throws IllegalStateException
	{
		if( this.checkSumGenerated )
		{
			throw new IllegalStateException( "Checksum was generated." );
		}
		
		//this.addedStreamDataInfo = true;

		id = id.replace( "\n", "" ).replace( "\r", "" );
		text = text.replace( "\n", "" ).replace( "\r", "" );

		this.header += "<"+id +">" + text + "</" + id + ">";
	}
	
	public Charset getCharCode() 
	{
		return this.charCode;
	}
	
	public int getZipID()
	{
		return this.zip_id;
	}
	
	public int getDataStreamInfoLength()
	{
		return this.getDataStreamInfo().length();
	}
	
	public String getDataStreamInfo()
	{	
		String aux = this.header.trim();
		
		if( !aux.isEmpty() && this.cipher != null )
		{
			try 
			{
				aux = new String( this.cipher.doFinal( aux.getBytes() ) );
			}
			catch (IllegalBlockSizeException | BadPaddingException e)
			{
				e.printStackTrace();
			}
		}
		
		return aux + this.endLine;
	}
	
	private void generateCheckSum()
	{
		if( !this.checkSumGenerated )
		{
			byte[] bytes = this.checkSum.digest();
		
			this.checkSumGenerated = ( bytes != null && bytes.length > 0 );
			
			if( this.checkSumGenerated )
			{
				StringBuilder sb = new StringBuilder();
				for(int i=0; i< bytes.length ;i++)
			    {
			        sb.append( Integer.toString( (bytes[i] & 0xff ) + 0x100, 16 ).substring(1));
			    }
				
				this.headerInfoExtension += sb.toString() + this.GrpSep;
			}
		}
	}
	
	public String getMetaDataProtocol()
	{
		if( !this.checkSumGenerated )
		{
			String aux = this.headerInfo + this.headerInfoExtension +  this.headerStreamInfo 
							+ "header,"
							//+  this.addedStreamDataInfo
							+ this.getDataStreamInfoLength()
							+ this.GrpSep + this.endLine;
			
			String aux2 = this.getDataStreamInfo();
						
			aux += aux2;
			
			try 
			{
				this.updateCheckSum( aux.getBytes() );
			} 
			catch (Exception e) 
			{
			}
		
			this.generateCheckSum();
		}
		
		return this.headerInfo + this.headerInfoExtension +  this.headerStreamInfo 
				+ "header," 
				//+ this.addedStreamDataInfo
				+ this.getDataStreamInfoLength()
				+ this.GrpSep + this.endLine;
	}
	
	public void updateCheckSum( byte[] bytes ) throws Exception
	{
		if( this.checkSumGenerated )
		{
			throw new IllegalStateException( "Check sum was generated" ); 
		}
		
		this.checkSum.update( bytes );
	}
}
