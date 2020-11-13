package lslrec.testing.Others;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class testSublist {

	public static void main(String[] args) 
	{
		List< Integer > dataBuffer = new ArrayList<Integer>();
		
		for( int i = 0; i < 100; i++ )
		{
			dataBuffer.add( i );
		}
		
		int from = 0;
		int to = 50;
		
		Object[] d = dataBuffer.subList( from, to ).toArray();
		
		dataBuffer.subList( from, to ).clear();
		
		System.out.println("testSublist.main() Array -> " + Arrays.toString( d ) );
		System.out.println("testSublist.main() List -> " + dataBuffer.toString() );
	}

}
