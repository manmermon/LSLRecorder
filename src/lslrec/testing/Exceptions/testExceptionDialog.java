package lslrec.testing.Exceptions;

import lslrec.exceptions.handler.ExceptionDialog;
import lslrec.exceptions.handler.ExceptionDictionary;
import lslrec.exceptions.handler.ExceptionMessage;

public class testExceptionDialog {

	public static void main(String[] args) 
	{
		Thread t = new Thread()
		{
			public void run() 
			{
				ExceptionMessage msg = new ExceptionMessage( new Exception( "test" ), "prueba", ExceptionDictionary.WARNING_MESSAGE);
			
				ExceptionDialog.createExceptionDialog( null );
				
				ExceptionDialog.AppExitWhenWindowClosing();
				
				ExceptionDialog.showMessageDialog( msg, true, true );
				ExceptionDialog.showMessageDialog( msg, true, false );
				
				ExceptionMessage msg1 = new ExceptionMessage( new Exception("test3"), "prueba", ExceptionDictionary.ERROR_MESSAGE );
				ExceptionDialog.showMessageDialog( msg1, true, true );
								
				synchronized ( this )
				{
					try {
						this.wait();
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		};
		
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
