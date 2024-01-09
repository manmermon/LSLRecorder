package lslrec.testing.Auxiliar;


import lslrec.auxiliar.thread.BeepSound;

public class testBeep {

	public static void main(String[] args) 
	{
		try {
			BeepSound bs = new BeepSound();
			bs.startThread();
			bs.play();
			
			System.out.println("testBeep.main()");
			Thread.sleep( 1500L );
			bs.play();
			Thread.sleep( 1500L );
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
