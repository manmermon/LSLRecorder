package InputStreamReader.OutputDataFile.DataBlock;

public class StringBlock extends CharBlock 
{

	public StringBlock(int seqNum,String name, int nCols, String data) 
	{	
		super( seqNum, name, nCols, toCharacterArray( data ) );
	}
	
	private static Character[] toCharacterArray( String data )
	{
		char[] arr = data.toCharArray();
		Character[] d = new Character[ arr.length ];
		
		for( int i = 0; i < arr.length; i++ )
		{
			d[ i ] = arr[ i ];
		}
		
		return d;
	}

}
