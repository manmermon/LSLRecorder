package lslrec.testing.Others;

import lslrec.auxiliar.extra.ArrayTreeMap;

public class testArrayTree {

	public static void main(String[] args) 
	{
		ArrayTreeMap< String, Object >  test = new ArrayTreeMap<String, Object>();

		test.putElement( "test", null );
		System.out.println( "OK" );
		test.putElement( null, new Object());
		System.out.println( "OK" );
	} 

}
