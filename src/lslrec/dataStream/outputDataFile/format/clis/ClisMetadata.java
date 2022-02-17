/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.  https://github.com/manmermon/LSLRecorder
 *
 *   LSLRec is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   LSLRec is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with LSLRec.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */
package lslrec.dataStream.outputDataFile.format.clis;

import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import lslrec.dataStream.outputDataFile.compress.IOutZip;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.StreamSettingExtraLabels;
import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;
import lslrec.dataStream.outputDataFile.compress.CompressorDataFactory;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.config.ConfigApp;
import lslrec.config.Parameter;

public class ClisMetadata 
{
	private final String ENCRYPT_ALGORITHM = "AES";
    private final String ENCRYPT_TRANSFORMATION = "AES/CBC/PKCS5Padding";
		
	private final String ID_LABEL_VER = "ver";
	private final String ID_LABEL_COMPRESS = "compress";
	private final String ID_LABEL_HEADERSIZE = "headerByteSize";
	private final String ID_LABEL_CHECKSUM = "checksum";
	private final String ID_LABEL_ENCRYPT = "encrypt";
	private final String ID_LABEL_HEADER = "header";
	
	private final String HEADER_ASSIG_SYMBOL = "=";

	private final boolean MEDATADATA_EXTENSION = true;
	
	private String headerInfo = "";
	private String headerStreamInfo = "";
	private String headerInfoExtension = "";
	
	private String header = "";
	
	private final String endLine = "\n";

	private final String GrpSep = ";" ;
	private final String fieldSep = "," ;
 
	private String zip_text_id = "GZIP";
	private String zip_id = CompressorDataFactory.GZIP;

	//private boolean addedStreamDataInfo = false;
	
	private Charset charCode;
	
	private byte[] padding = null;
	
	private long headerSize = 0;
	
	private double version = 2.1;
	
	private final String checkSumAlg_ID = "MD5";
	private MessageDigest checkSum = null;	
	private boolean generatedChecksum = false;
	
	private String checkSumValue = "";
	
	private Cipher cipher = null;
	private IvParameterSpec cipherPars =  new IvParameterSpec( new byte[16] );
	
	private byte[] encrpytKeyMetadata = null;
	
	private IStreamSetting streamSetting;
	
	public ClisMetadata( OutputFileFormatParameters pars, IStreamSetting settings  ) throws Exception 
	{	
		this.zip_id = pars.getParameter( OutputFileFormatParameters.ZIP_ID ).getValue().toString();
		
		if( this.zip_id == null )
		{
			this.zip_id = CompressorDataFactory.UNDEFINED;
		}
		
		IOutZip zipProcess = CompressorDataFactory.createOuputZipStream( this.zip_id );
		
		if( zipProcess == null )
		{
			throw new IllegalArgumentException( "Compress technique unknown." );
		}
		
		this.zip_text_id = zipProcess.getID();
				
		this.charCode = (Charset)pars.getParameter( OutputFileFormatParameters.CHAR_CODING ).getValue();
		if( this.charCode == null )
		{
			this.charCode = Charset.forName( "UTF-8" );
		}
		
		Parameter< String > p = pars.getParameter( OutputFileFormatParameters.ENCRYPT_KEY );
		String encryptKey = null;
		
		if( p != null )
		{
			Object k = pars.getParameter( OutputFileFormatParameters.ENCRYPT_KEY ).getValue();
			
			if( k != null )
			{
				encryptKey = k.toString();
			}
		}
		
		
		int encryptBlockSize = 1;
		int cipherBlockSize  = 0;	
		
		if( encryptKey != null && !encryptKey.isEmpty() )
		{			
			SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
			KeySpec spec = new PBEKeySpec( encryptKey.toCharArray(), new byte[ 8 ], 10000, 128 );
			SecretKey tmp = skf.generateSecret( spec );
			SecretKeySpec skey = new SecretKeySpec(tmp.getEncoded(), this.getEncryptID() );
			
			this.cipher = Cipher.getInstance( this.getEncryptTransform() );
	        this.cipher.init( Cipher.ENCRYPT_MODE, skey, this.cipherPars );		        
	        
	        cipherBlockSize = this.cipher.getBlockSize();	        
	        encryptBlockSize = this.cipher.getBlockSize();
	        
	        this.encrpytKeyMetadata = this.cipher.doFinal( encryptKey.getBytes( this.charCode ) );		
		}
		
		Long numBlocks = (Long)pars.getParameter( OutputFileFormatParameters.NUM_BLOCKS ).getValue();
		if( numBlocks == null )
		{
			numBlocks = 2L;
		}

		this.streamSetting = settings;
		
		String xml = this.streamSetting.description();
		if( xml == null )
		{
			xml = "";
		}
		
		int chs = this.streamSetting.channel_count();
		if( chs < 1 )
		{
			chs = 1;
		}
		
		String names = "";
		Object aux = pars.getParameter( OutputFileFormatParameters.DATA_NAMES ).getValue();
		if( aux != null )
		{
			names = aux.toString();
		}

		Integer BLOCK_SIZE = (Integer)pars.getParameter(OutputFileFormatParameters.BLOCK_DATA_SIZE ).getValue();
		
		if( BLOCK_SIZE == null )
		{
			BLOCK_SIZE = (int)( Math.pow( 2, 20 ) * (Integer)ConfigApp.getProperty( ConfigApp.SEGMENT_BLOCK_SIZE ) );
		}
		
		BLOCK_SIZE = (int)( Math.ceil( BLOCK_SIZE / encryptBlockSize) * encryptBlockSize );
		
		int blockSizeStrLen = Integer.toString( BLOCK_SIZE ).length() + 1;
		
		
		this.headerSize = numBlocks * blockSizeStrLen;		
			
		this.headerSize += xml.toCharArray().length + cipherBlockSize;
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
		
		int encByteLen = 0;
		
		if( this.encrpytKeyMetadata != null )
		{
			encByteLen = this.encrpytKeyMetadata.length; 
		}
		
		this.headerInfoExtension = this.ID_LABEL_ENCRYPT + this.HEADER_ASSIG_SYMBOL 
										+ encByteLen + this.GrpSep
										+ this.ID_LABEL_CHECKSUM + this.HEADER_ASSIG_SYMBOL;
							
		this.checkSum = MessageDigest.getInstance( this.checkSumAlg_ID );
		
		int checkSumLen = this.checkSum.getDigestLength();
		
		
		this.headerSize += ( this.headerInfo.length() + this.headerInfoExtension.length() + checkSumLen + encByteLen ) * 2;
		this.headerSize += pars.getParameter( OutputFileFormatParameters.RECORDING_INFO ).getValue().toString().length();
		this.headerSize += pars.getParameter( StreamSettingExtraLabels.ID_RECORDED_SAMPLES_BY_CHANNELS ).getValue().toString().length();
		
		//byte[] padding = new byte[ Character.BYTES ];
		int charByteSize = this.headerInfo.getBytes( this.charCode ).length / this.headerInfo.length(); 
		this.padding = new byte[ charByteSize ];
		
		for( int i = 0; i < this.padding.length; i++ )
		{
			this.padding[ i ] = '\r';
		}
		
		this.headerInfo += ( this.headerSize * padding.length ) + this.GrpSep;
	}
		
	public int getDataTypeBytes( StreamDataType type )
	{
		return this.streamSetting.getDataTypeBytes( type );
	}
	
	public long getHeaderSize() 
	{
		return this.headerSize;
	}
	
	public Cipher getEncryptor() 
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
	
	public void addMetadataProtocolInfo(String name, String type, int typeBytes, long col, List< Integer > blockSizes )
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
		if( this.generatedChecksum )
		{
			throw new IllegalStateException( "Checksum was generated." );
		}
		
		//this.addedStreamDataInfo = true;

		id = id.replace( "\n", "" ).replace( "\r", "" ).replaceAll( "\\s+", "");
		//text = text.replace( "\n", "" ).replace( "\r", "" );

		this.header += "<"+id +">" + text + "</" + id + ">";
	}
	
	public Charset getCharCode() 
	{
		return this.charCode;
	}
	
	public String getZipID()
	{
		return this.zip_id;
	}
	
	private int getDataStreamInfoLength()
	{
		return this.getDataStreamInfo().length;
	}
	
	private byte[] getDataStreamInfo()
	{	
		byte[] aux = ( this.header.trim() + this.endLine ).getBytes( this.charCode );
		
		if( aux.length > 0 && this.cipher != null )
		{
			try 
			{
				aux = this.cipher.doFinal( aux );
			}
			catch (IllegalBlockSizeException | BadPaddingException e)
			{
				e.printStackTrace();
			}
		}
		
		return aux;
	}
	
	private byte[] getMetadataProtocolInfoExtension( boolean doChecksum )
	{
		if( !this.generatedChecksum && doChecksum )
		{
			byte[] bytes = this.checkSum.digest();
		
			this.generatedChecksum = ( bytes != null && bytes.length > 0 );
			
			if( this.generatedChecksum )
			{
				StringBuilder sb = new StringBuilder();
				for(int i=0; i< bytes.length ;i++)
			    {
			        sb.append( Integer.toString( (bytes[i] & 0xff ) + 0x100, 16 ).substring(1));
			    }
				
				this.checkSumValue = sb.toString();
			}
		}
		
		byte[] aux = ( this.headerInfoExtension + this.checkSumValue + this.GrpSep + this.endLine ).getBytes( this.charCode );
		
		int metaExtLen = aux.length;
		
		if( this.encrpytKeyMetadata != null )
		{
			metaExtLen += this.encrpytKeyMetadata.length;
		}
		
		byte[] out = new byte[ metaExtLen ];
		
		System.arraycopy( aux, 0, out, 0, aux.length );
		
		if( this.encrpytKeyMetadata != null)
		{
			System.arraycopy( this.encrpytKeyMetadata, 0, out, aux.length, this.encrpytKeyMetadata.length );
		}
		
		return  out;
				
	}
	
	public byte[] getMetaDataProtocol()
	{
		byte[] metaExtension = this.getMetadataProtocolInfoExtension( false );
		
		byte[] aux1 = ( this.headerInfo + this.headerStreamInfo 
						+ this.ID_LABEL_HEADER + this.fieldSep
						+ this.getDataStreamInfoLength()							
						+ this.GrpSep + this.MEDATADATA_EXTENSION + this.GrpSep  
						+ this.endLine ).getBytes( this.charCode );
		
		byte[] aux2 = this.getDataStreamInfo();
		
		byte[] aux3 = new byte[ metaExtension.length + aux1.length + aux2.length ];
		
		System.arraycopy( aux1, 0, aux3, 0, aux1.length );
		System.arraycopy( metaExtension, 0, aux3, aux1.length, metaExtension.length );
		System.arraycopy( aux2, 0, aux3, aux1.length + metaExtension.length, aux2.length );
		
		if( !this.generatedChecksum )
		{	
			try 
			{
				this.updateCheckSum( aux3 );
			} 
			catch (Exception e) 
			{
			}
			
			metaExtension = this.getMetadataProtocolInfoExtension( true );
			
			aux3 = new byte[ metaExtension.length + aux1.length + aux2.length ];
			
			System.arraycopy( aux1, 0, aux3, 0, aux1.length );
			System.arraycopy( metaExtension, 0, aux3, aux1.length, metaExtension.length );
			System.arraycopy( aux2, 0, aux3, aux1.length + metaExtension.length, aux2.length );
		}
		
		return aux3;
	}
	
	public void updateCheckSum( byte[] bytes ) throws Exception
	{
		if( this.generatedChecksum )
		{
			throw new IllegalStateException( "Check sum was generated" ); 
		}
		
		this.checkSum.update( bytes );
	}
}
