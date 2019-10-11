package testing;

import java.util.concurrent.atomic.AtomicInteger;

public class testThrearAtomicInteger 
{
	public static void main(String[] args) 
	{
		Thread t = new Thread()
				{
			public void run() 
			{
				AtomicInteger i = new AtomicInteger();
				
				System.out.println("testThrearAtomicInteger.main(...).new Thread() {...}.run()  SYNC" );
				synchronized ( i )
				{
					System.out.println("testThrearAtomicInteger.main(...).new Thread() {...}.run()  SYNC-PRE" );
					i.incrementAndGet();
					
					System.out.println("testThrearAtomicInteger.main(...).new Thread() {...}.run()  SYNC-POS" );
				}
				
				System.out.println("testThrearAtomicInteger.main(...).new Thread() {...}.run()  SYNC-PRE-POS-OUT" );
				
			}; 
				};
				
				t.start();
	}
}
