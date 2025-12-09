package lslrec.exceptions.handler;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import lslrec.auxiliar.extra.Tuple;
import lslrec.gui.miscellany.TextAreaPrintStream;

public class ExceptionLogGUIThread 
{
	private List< TextAreaPrintStream > logs;
	
	private final BlockingQueue< Tuple< ExceptionMessage, Tuple< Boolean, Boolean > > >queue;
	
	private Object sync = new Object();
	
	private final Thread worker;
	
	private boolean running = true;
	
	public ExceptionLogGUIThread( final int batchSize ) 
	{
		this.logs = new ArrayList<TextAreaPrintStream>();
				
		this.queue = new LinkedBlockingQueue< Tuple< ExceptionMessage, Tuple< Boolean, Boolean > > >();
		
		 this.worker = new Thread(() -> 
		 {
			 List<  Tuple< ExceptionMessage, Tuple< Boolean, Boolean > > > buffer = new ArrayList<Tuple<ExceptionMessage,Tuple<Boolean,Boolean>>>();
			 try 
			 {
				 while (running || !queue.isEmpty()) 
				 {
					 Tuple< ExceptionMessage, Tuple< Boolean, Boolean > > in = queue.take();
					 
					 buffer.add( in );
					 
					 if( buffer.size() >= batchSize )
					 {
						flushBatch( buffer );
					 }
				 }
			 } 
			 catch (InterruptedException ignored) 
			 {				 
				 if( buffer.size() >= batchSize )
				 {
					flushBatch( buffer );
				 }
			 }
			 finally 
			 { 	 
			 }
		 });

		 this.worker.setDaemon(true);
		 this.worker.start();
	}	
	
	private void flushBatch( List< Tuple< ExceptionMessage, Tuple< Boolean, Boolean > > > buffer ) 
	{
		 for( Tuple< ExceptionMessage, Tuple< Boolean, Boolean > > t : buffer )
		 {							 
			 ExceptionMessage msg = t.t1;  // FIFO garantizado
			 Tuple< Boolean, Boolean > opts = t.t2;
			 
			 if( msg != null )
			 {
				 boolean concat = false, printTrace = false;
				 if( opts != null )
				 {
					 concat = opts.t1;
					 printTrace = opts.t2;
				 }
				 
				showException( msg, concat, printTrace );
			 }
		 }
		 
		 buffer.clear();
	} 
	
 	private void showException( ExceptionMessage msg, boolean concatMsg, boolean printExceptionTrace )
	{		
		Throwable ex = msg.getException();
		
		Color msgColor = Color.ORANGE;

		if( msg.getMessageType() == ExceptionMessage.ERROR_MESSAGE )
		{
			msgColor = Color.RED;
		}
		else if( msg.getMessageType() == ExceptionMessage.INFO_MESSAGE )
		{
			msgColor = Color.BLACK;
		}
		
		synchronized( sync )
		{
			for( TextAreaPrintStream log : this.logs )
			{
				if( !concatMsg )
				{
					log.flush();
				}

				if( !log.isSameTextColor( msgColor ) )
				{
					log.SetColorText( msgColor );
				}
					
				if( ex != null )
				{
					if( printExceptionTrace )
					{
						ex.printStackTrace( log );
					}
					else
					{
						log.println( ex.getMessage() );
					}
				}
			}
		}
	}
	
	public void addLog( TextAreaPrintStream log )
	{
		synchronized( this.sync )
		{
			this.logs.add( log );
		}
	}
	
	public void removeAll()
	{
		synchronized ( sync )
		{
			this.logs.clear();
		}
	}
	
	public void removeLog( TextAreaPrintStream log )
	{
		synchronized ( sync )
		{
			this.logs.remove( log );
		}
	}
	
	public void clearLog()
	{
		synchronized ( sync )
		{
			for( TextAreaPrintStream log : this.logs )
			{
				log.flush();
			}
		}		
	}

	public void write( ExceptionMessage ex, boolean concat, boolean printExTrace ) throws IllegalStateException
	{	
		 if ( !this.running)
		 {
			 throw new IllegalStateException("Logger cerrado");
		 }
	        
		 if( ex != null )
		 {
			 this.queue.add( new Tuple<ExceptionMessage, Tuple<Boolean,Boolean>>( ex,new Tuple<Boolean, Boolean>( concat, printExTrace ) ) );
		 }
	}
	
	public void flush()
	{
		this.worker.interrupt();
	}
	
	public void close() 
	{
        running = false;
        worker.interrupt();
    }
}
