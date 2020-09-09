package testing.Compression;

import testing.Compression.testHNIS.HNIS.Codeword;

public class testHNIS {

	public static void main(String[] args) 
	{				
		HNIS hnis = new testHNIS.HNIS();
		
		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		byte v  = Byte.MIN_VALUE;
		
		while( true )
		{
			Codeword c = hnis.getNISCodeword( hnis.getBinaryFormat( v ) );
			System.out.println("testHNIS.main() " + v + "(" + hnis.getBinaryFormat( v ) + ") ->\t" + c );
			
			if( c.getNumberOfbits() < min )
			{
				min = c.getNumberOfbits();
			}
			
			if( c.getNumberOfbits() > max )
			{
				max = c.getNumberOfbits();
			}
			
			if( v == Byte.MAX_VALUE )
			{
				break;
			}
			else
			{
				v++;
			}
		}
		
		System.out.println("testHNIS.main() No. bits [" + ( min + 1 ) + ", " + ( max + 1 ) + "]" );
	}

	public static class HNIS
	{
		public String getBinaryFormat( byte v )
		{
			String format = "%"+ ( 8 * Byte.BYTES ) + "s";
			String f = String.format( format, Integer.toBinaryString( v & 0xFF )).replace(' ', '0');
			
			return f;
			//return Integer.toBinaryString( v );
		}
		
		public Codeword getNISCodeword( String binFormat )
		{
			Codeword codeword0 = NISTraversal( binFormat, true );
			Codeword codeword1 = NISTraversal( binFormat, false );
			
			Codeword res = codeword0;
			
			if( codeword0.getNumberOfbits() > codeword1.getNumberOfbits() )
			{
				res = codeword1;
			}
			
			
			return res;
		}
		
		private Codeword NISTraversal( String binFormat, boolean base0 )
		{
			String codeword = "";
			
			char base = '0';
			
			if( !base0 )
			{
				base = '1';
			}			
			
			if( binFormat != null && !binFormat.isEmpty() )
			{				
				byte count = 0;
				for( int i = 0; i < binFormat.length(); i++ )
				{
					char c = binFormat.charAt( i );

					if( c != base )
					{
						count++;
					}
					else
					{
						if( !codeword.isEmpty() )
						{
							codeword += ",";
						}
						
						codeword += Integer.toBinaryString( count );
						count = 0;
					}
				}
			}
			
			return new Codeword( base0, codeword );
		}
		
		public static class Codeword
		{
			private boolean base0 = true;
			private String code = "";
			private int nbits = 0;
			
			public Codeword( boolean base0, String codeword ) 
			{
				this.code = codeword;
				this.base0 = base0;
				
				String[] bits = codeword.split( "," );
				
				for( String st : bits )
				{
					if( !st.isEmpty() )
					{
						this.nbits += st.length();
					}
				}
			}
			
			public boolean isBase0()
			{
				return this.base0;
			}
			
			public String getCodeword() 
			{
				return code;
			}
			
			public int getNumberOfbits()
			{
				return this.nbits;
			}
			
			@Override
			public String toString() 
			{			
				String st = "0: ";
				
				if( !this.base0 )
				{
					st = "1: ";
				}
				
				return st + code + " - " + (nbits);
			}
		}		
	}
	
}
