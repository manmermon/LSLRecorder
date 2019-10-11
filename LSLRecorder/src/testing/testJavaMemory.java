package testing;

import java.lang.management.ManagementFactory;

public class testJavaMemory 
{
	public static void main(String[] args) 
	{
		double MB = Math.pow( 2,20);
		
		/* Total number of processors or cores available to the JVM */
		System.out.println("Available processors (cores): " + 
				Runtime.getRuntime().availableProcessors());

		/* Total amount of free memory available to the JVM */
		System.out.println("Free memory (MB): " + 
				Runtime.getRuntime().freeMemory() / MB);

		/* This will return Long.MAX_VALUE if there is no preset limit */
		long maxMemory = Runtime.getRuntime().maxMemory();
		/* Maximum amount of memory the JVM will attempt to use */
		System.out.println("Maximum memory (MB): " + 
				(maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory / MB));

		long allocatedMemory = 
				  (Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
		System.out.println("Allocate memory (MB): " 
				  + allocatedMemory / MB );
		
		/* Total memory currently in use by the JVM */
		System.out.println("Total memory (MB): " +
				Runtime.getRuntime().totalMemory() / MB);
		
		long memorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory
		        			.getOperatingSystemMXBean()).getTotalPhysicalMemorySize();
		
		System.out.println("Total physical memory (MB): " +
				memorySize / MB);
		
		long freeMemorySize = ((com.sun.management.OperatingSystemMXBean) ManagementFactory
    			.getOperatingSystemMXBean()).getFreePhysicalMemorySize();
		
		System.out.println("Free physical memory (MB): " +
				freeMemorySize / MB);
	}
}
