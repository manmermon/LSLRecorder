package testing.Auxiliar;

import java.util.Arrays;

import Auxiliar.extra.ConvertTo;

public class testConvertToInterleaved 
{
	public static void main(String[] args) 
	{
		Integer[] array1 = new Integer[] { 1, 4, 7, 10, 2, 5, 8, 11, 3, 6, 9, 12};
		Integer[] array3 = new Integer[] { 1, 4, 7, 10, 2, 5, 8, 11, 3, 6, 9, 12, 13 };
		Integer[] array5 = new Integer[] { 1, 4, 7, 10, 2, 5, 8, 11, 3, 6, 9, 12, 13, 14 };
		Integer[] array7 = new Integer[] { 1, 4, 7, 10, 2, 5, 8, 11, 3, 6, 9, 12, 13, 14, 15 };
		Integer[] array9 = new Integer[] { 1, 4, 7, 10, 2, 5, 8, 11, 3, 6, 9, 12, 13, 14, 15, 16 };
		
		Integer[] array2 = new Integer[] { 1, 4, 7, 10, 2, 5, 8, 11, 3, 6, 9, 12, 13, 16, 19, 22, 14, 17, 20, 23, 15, 18, 21, 24 };
		Integer[] array4 = new Integer[] { 1, 4, 7, 10, 2, 5, 8, 11, 3, 6, 9, 12, 13, 16, 19, 22, 14, 17, 20, 23, 15, 18, 21, 24, 31 };
		Integer[] array6 = new Integer[] { 1, 4, 7, 10, 2, 5, 8, 11, 3, 6, 9, 12, 13, 16, 19, 22, 14, 17, 20, 23, 15, 18, 21, 24, 31, 32 };
		Integer[] array8 = new Integer[] { 1, 4, 7, 10, 2, 5, 8, 11, 3, 6, 9, 12, 13, 16, 19, 22, 14, 17, 20, 23, 15, 18, 21, 24, 31, 32, 33 };
		Integer[] array10 = new Integer[] { 1, 4, 7, 10, 2, 5, 8, 11, 3, 6, 9, 12, 13, 16, 19, 22, 14, 17, 20, 23, 15, 18, 21, 24, 31, 32, 33, 34 };
		Integer[] array12 = new Integer[] { 1, 4, 7, 10, 2, 5, 8, 11, 3, 6, 9, 12, 13, 16, 19, 22, 14, 17, 20, 23, 15, 18, 21, 24, 31, 32, 33, 34, 35 };
		Integer[] array14 = new Integer[] { 1, 4, 7, 10, 2, 5, 8, 11, 3, 6, 9, 12, 13, 16, 19, 22, 14, 17, 20, 23, 15, 18, 21, 24, 31, 32, 33, 34, 35, 36 };
		
		System.out.println("testConvertToInterleaved.main() 1 " + Arrays.toString( ConvertTo.Interleaved( array1, 4 ) ) );
		System.out.println("testConvertToInterleaved.main() 3 " + Arrays.toString( ConvertTo.Interleaved( array3, 4 ) ) );
		System.out.println("testConvertToInterleaved.main() 5 " + Arrays.toString( ConvertTo.Interleaved( array5, 4 ) ) );
		System.out.println("testConvertToInterleaved.main() 7 " + Arrays.toString( ConvertTo.Interleaved( array7, 4 ) ) );
		System.out.println("testConvertToInterleaved.main() 9 " + Arrays.toString( ConvertTo.Interleaved( array9, 4 ) ) );
		
		System.out.println("testConvertToInterleaved.main() 2 " + Arrays.toString( ConvertTo.Interleaved( array2, 3, 4 ) ) );
		System.out.println("testConvertToInterleaved.main() 4 " + Arrays.toString( ConvertTo.Interleaved( array4, 3, 4 ) ) );
		System.out.println("testConvertToInterleaved.main() 6 " + Arrays.toString( ConvertTo.Interleaved( array6, 3, 4 ) ) );
		System.out.println("testConvertToInterleaved.main() 8 " + Arrays.toString( ConvertTo.Interleaved( array8, 3, 4 ) ) );
		System.out.println("testConvertToInterleaved.main() 10 " + Arrays.toString( ConvertTo.Interleaved( array10, 3, 4 ) ) );
		System.out.println("testConvertToInterleaved.main() 12 " + Arrays.toString( ConvertTo.Interleaved( array12, 3, 4 ) ) );
		System.out.println("testConvertToInterleaved.main() 14 " + Arrays.toString( ConvertTo.Interleaved( array14, 3, 4 ) ) );
		
		System.out.println("testConvertToInterleaved.main() 14-14 " + Arrays.toString( ConvertTo.Interleaved( ConvertTo.Interleaved( array14, 3, 4 ), 4, 3 ) ) );
	}
}
