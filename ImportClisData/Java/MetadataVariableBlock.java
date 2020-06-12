package ImportClisData.Java;

import java.util.ArrayList;
import java.util.List;

import ImportClisData.Java.ClisDataType.DataType;

public class MetadataVariableBlock
{	
	private int cols = 1;
	private DataType dataType = DataType.FLOAT32;
	private String name = "var";
	private List< Integer > blockDataSize = new ArrayList< Integer >();
	
	public MetadataVariableBlock( String var ) throws ClisMetadataException 
	{
		this.processVarBlock( var );
	}
	
	private void processVarBlock( String var ) throws ClisMetadataException
	{
		try
		{
			String[] parts = var.split( "," );
			
			this.name = parts[ 0 ];
			
			this.setDataTye( parts[ 1 ], Integer.valueOf( parts[ 2 ] ) );
			
			this.cols = Integer.valueOf( parts[ 3 ] );
			
			for( int i = 4; i < parts.length; i++ )
			{
				this.blockDataSize.add( Integer.valueOf( parts[ i ] ) );
			}
			
		}
		catch ( IndexOutOfBoundsException e) 
		{
			throw new ClisMetadataException( "Variable field length must be greater o equal than 5." ); 
		}	
		catch ( NumberFormatException e) 
		{
			throw new ClisMetadataException( "Variable fields are not a integer values." );
		}
		
	}
	
	private void setDataTye( String baseTye, int bytes ) throws ClisMetadataException
	{
		boolean err = false;
		
		if( baseTye.toLowerCase().equals( "int" ) )
		{
			switch ( bytes ) 
			{
				case 1:
				{
					this.dataType = DataType.INT8;
					break;
				}
				case 2:
				{
					this.dataType = DataType.INT16;
					break;
				}
				case 4:
				{
					this.dataType = DataType.INT32;
					break;
				}
				case 8:
				{
					this.dataType = DataType.INT64;
					break;
				}
				default:
				{
					err = true;
				}						
			}
		}
		else if( baseTye.toLowerCase().equals( "float" ) )
		{
			switch ( bytes ) 
			{
				case 4:
				{
					this.dataType = DataType.FLOAT32;
					break;
				}
				case 8:
				{
					this.dataType = DataType.FLOAT64;
					break;
				}
				default:
				{
					err = true;
				}						
			}
		}
		
		if( err )
		{
			throw new ClisMetadataException( "Unsupported data type." );
		}
	}

	public String getName()
	{
		return this.name;
	}
	
	public int getCols() 
	{
		return this.cols;
	}
	
	public DataType getDataType()
	{
		return this.dataType;
	}
	
	public List<Integer> getBlockDataSize()
	{
		return this.blockDataSize;
	}
}
