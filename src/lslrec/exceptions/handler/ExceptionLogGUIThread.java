/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.  https://github.com/manmermon/LSLRecorder
 *
 *   LSLRec is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   LSLRec is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with LSLRec.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */
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
	
	private boolean flush = false;
	
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
					 try
					 {
						 Tuple< ExceptionMessage, Tuple< Boolean, Boolean > > in = queue.take();

						 buffer.add( in );

						 if( buffer.size() >= batchSize )
						 {
							 flushBatch( buffer );
						 }
					 }
					 catch (InterruptedException ignored) 
					 {
						 if( flush &&  buffer.size() >= batchSize )
						 {
							 flushBatch( buffer );
						 }
						 
						 flush = false;
					 }
				 }
			 } 
			 catch (Exception ignored) 
			 {	 
			 }
			 finally 
			 { 	 
				 if( buffer.size() >= batchSize )
				 {
					 flushBatch( buffer );
				 }
			 }
		 });

		 this.worker.setName( "Worker-" + this.getClass().getSimpleName() );
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
		this.flush = true;
		this.worker.interrupt();
	}
	
	public void close() 
	{
        running = false;
        worker.interrupt();
    }
}
