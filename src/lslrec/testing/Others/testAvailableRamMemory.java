package testing.Others;

public class testAvailableRamMemory {

	public static void main(String[] args) 
	{
		long totalMemory = Runtime.getRuntime().totalMemory();
		long freeMemory = Runtime.getRuntime().freeMemory();
		long maxMemory = Runtime.getRuntime().maxMemory();
		long allocatedMemory = totalMemory - freeMemory;		
		long presumableFreeMemory = maxMemory - allocatedMemory;
		
		System.out.println("Total memory: " + totalMemory / 1e6D + " MB");
		System.out.println("Free memory: " + freeMemory / 1e6D + " MB");
		System.out.println("Max memory: " + maxMemory / 1e6D + " MB");
		System.out.println("Allocated memory: " + allocatedMemory / 1e6D + " MB");
		System.out.println("Available memory: " + presumableFreeMemory / 1e6D + " MB" );
	}

}
