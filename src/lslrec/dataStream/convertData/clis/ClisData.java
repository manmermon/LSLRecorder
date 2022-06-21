package lslrec.dataStream.convertData.clis;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.crypto.Cipher;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.auxiliar.extra.Tuple;
import lslrec.dataStream.convertData.clis.compress.IUnzip;
import lslrec.dataStream.convertData.clis.compress.UnzipDataFactory;
import lslrec.dataStream.tools.StreamUtils.StreamDataType;

public class ClisData
{
	private ClisMetadataReader metadata = null;
	
	private Map< String, List< Number > > data = new HashMap< String, List< Number > >();
			
	private Tuple<Integer, String> currentVar = null;
	private int currentBlockIndex = 0;
	
	private int readData = 0;
	
	public ClisData( String filePath ) throws Exception
	{
		this.metadata = new ClisMetadataReader( new File( filePath ) );
	}
	
	public List< MetadataVariableBlock > getVarInfo()
	{
		List< MetadataVariableBlock > names = new ArrayList< MetadataVariableBlock >( this.metadata.getVariables() );
			
		return names;
	}	
	
	public StreamDataType getDataType( String var )
	{
		StreamDataType t = StreamDataType.undefined;
		
		List< MetadataVariableBlock > vars = this.getVarInfo();
		
		for( MetadataVariableBlock v : vars )
		{
			if( v.getName().equals( var ) )
			{
				t = v.getDataType();
				
				break;
			}
		}
		
		return t;
	}
	
	public String getHeader()
	{
		return this.metadata.getHeader();
	}
	
	public Number[][] importNextDataBlock( int varIndex, int chuckSize ) throws Exception
	{
		RandomAccessFile file = this.metadata.getFileReader();
		
		List< MetadataVariableBlock > vars = this.metadata.getVariables();
		
		Number[][] DATA = null;
		
		if( varIndex >= 0 && varIndex < vars.size() )
		{
			List< Number > _data = new ArrayList<Number>();
			
			MetadataVariableBlock varBlock = vars.get( varIndex );
			
			if( this.currentVar == null )
			{
				this.currentVar = new  Tuple<Integer, String>( varIndex, varBlock.getName() );
			}
			
			if( this.currentVar.t1 != varIndex )
			{
				this.currentVar = new Tuple<Integer, String>( varIndex, varBlock.getName() );
				this.currentBlockIndex = 0;
				
				int skip = this.metadata.getMetadataByteSize();
				
				for( int i = 0; i < varIndex; i++ )
				{
					MetadataVariableBlock var = vars.get( i );
					
					for( Integer size : var.getBlockDataSize() )
					{
						skip += size;
					}
				}
				
				file.seek( 0 );				
				file.skipBytes( skip );				
			}
			
			List< Integer > blockSize = varBlock.getBlockDataSize();
			
			if( this.currentBlockIndex >= 0 
					&& this.currentBlockIndex < blockSize.size() )
			{
				int size = blockSize.get( currentBlockIndex );
				
				List< Number > currentDataBlock = this.data.get( this.currentVar.t2 );
				boolean newBlock = ( currentDataBlock == null );
								
				if( newBlock )
				{
					currentDataBlock = new ArrayList< Number >();
					this.data.put( this.currentVar.t2, currentDataBlock );
					
					currentDataBlock.addAll( this.getNextDataBlock( file, size, varBlock.getDataType() ) );
				}
				
				int Len = chuckSize * varBlock.getCols();
				
				if( this.readData > currentDataBlock.size() )
				{	
					this.readData = 0;
					
					this.currentBlockIndex++;
					currentDataBlock.clear();
					
					if( this.currentBlockIndex >= 0  && this.currentBlockIndex < blockSize.size() )
					{						
						size = blockSize.get( this.currentBlockIndex );
						
						currentDataBlock.addAll( this.getNextDataBlock( file, size, varBlock.getDataType() ) );
					}
				}
				
				while( !currentDataBlock.isEmpty() 
						&& ( this.readData + Len ) > currentDataBlock.size() )
				{
					for( ; this.readData < currentDataBlock.size(); this.readData++ )
					{
						_data.add( currentDataBlock.get( this.readData ) );						
						
						Len--;
					}
					
					this.readData = 0;
					
					this.currentBlockIndex++;
					currentDataBlock.clear();
					
					if( this.currentBlockIndex >= 0  && this.currentBlockIndex < blockSize.size() )
					{						
						size = blockSize.get( this.currentBlockIndex );
						
						currentDataBlock.addAll( this.getNextDataBlock( file, size, varBlock.getDataType() ) );
					}
				}
				
				for( ; Len > 0 && this.readData < currentDataBlock.size(); this.readData++ )
				{
					_data.add( currentDataBlock.get( this.readData ) );						
					
					Len--;
				}
				
				
				int rows = (int)( Math.ceil( 1.0 * _data.size() / varBlock.getCols() ) );
				
				if( rows > 0 )
				{
					DATA = new Number[ rows ][ varBlock.getCols() ]; 
					
					int index = 0;
					for( int r = 0; r < rows && index < _data.size(); r++ )
					{
						for( int c = 0; c < varBlock.getCols()  && index < _data.size(); c++ )
						{
							DATA[ r ][ c ] = _data.get( index );
							
							index++;
						}
					}
				}
			}
			else
			{
				this.data.remove( varBlock.getName() );
			}
			
		}
		
		return DATA;
	}
	
	public Map< String, Number[][] > importAllData() throws Exception
	{
		Map< String, Number[][] > DATA = new HashMap<String, Number[][]>();
		
		List< MetadataVariableBlock > VARS = this.metadata.getVariables();
		
		int chuckSize = 1;
		
		for( int var = 0; var < VARS.size(); var++ )
		{
			MetadataVariableBlock v = VARS.get( var );
			
			Number[][] data = null;
			List< Number[][] > _datAux = new ArrayList< Number[][] >();
			int totalRows = 0;
			do
			{
				data = this.importNextDataBlock( var, chuckSize );
				
				if( data != null 
						&& data.length > 0 
						&& data[ 0 ].length > 0 )
				{
					totalRows += data.length;
					_datAux.add( data );
				}
			}
			while( data != null );		
			
			if( totalRows > 0 )
			{
				Number[][] D = new Number[ totalRows ][ v.getCols() ];
				
				int r = 0;
				for( Number[][] _dat : _datAux )
				{
					for( int i = 0; i < _dat.length; i++ )
					{
						for( int j = 0; j < _dat[ 0 ].length; j++ )
						{
							D[ r ][ j ] = _dat[ i ][ j ];
						}
						
						r++;
					}
				}
				
				DATA.put( v.getName(), D );
			}
		}
		
		return DATA;		
	}
	
	private List< Number > getNextDataBlock( RandomAccessFile file, int size, StreamDataType dataType ) throws IllegalArgumentException, IOException, Exception
	{
		List< Number > currentDataBlock = new ArrayList<Number>();		
		byte[] bytes = new byte[ size ];
		
		if( file.read( bytes ) > 0 )
		{
			IUnzip unzip = UnzipDataFactory.createUnzipStream( this.metadata.getCompressTechnique() );
			
			Cipher decrypt = this.metadata.getDecrypt();
			if( decrypt != null )
			{    
				bytes = decrypt.doFinal( bytes );		        
			}
			
			bytes = unzip.unzipData( bytes );		
						
			Number[] values = ConvertTo.Transform.ByteArray2ArrayOf( bytes, dataType );
			
			for( Number v : values )
			{
				currentDataBlock.add( v );
			}			
		}
		
		return currentDataBlock;
	}

	public void close() throws IOException
	{
		RandomAccessFile f = this.metadata.getFileReader();
		
		if( f != null )
		{
			f.close();
		}
	}
}
