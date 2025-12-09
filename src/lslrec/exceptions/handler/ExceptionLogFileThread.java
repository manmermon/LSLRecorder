package lslrec.exceptions.handler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang3.StringUtils;

import lslrec.config.ConfigApp;

public class ExceptionLogFileThread  
{
	private final BlockingQueue<String> queue;
    private final BufferedWriter writer;
    private final Thread worker;
    private volatile boolean running = true;
    
    private String subjSession = "";
    
    private int batch_size = 1;
    
	public ExceptionLogFileThread( String subjID, String sessionID, int batchSize ) throws IOException 
	{		
		this.queue = new LinkedBlockingQueue<>();
		
		this.batch_size = ( batchSize > 0 ) ? batchSize : 1;
		
		String date = new SimpleDateFormat("yyyy-MM-dd").format( new Date());
		
		String subj = (subjID == null) ? "" : subjID;
		String session = (sessionID == null) ? "" : sessionID;
							
		this.subjSession = "";
		if( subj != null && !subj.isEmpty() )
		{
			this.subjSession += subj;
		}
		
		if( session != null && !session.isEmpty() )
		{
			this.subjSession = ( this.subjSession.isEmpty() ) ? "-" + session : this.subjSession + "-" + session;
		}
		
		String fileName = ConfigApp.defaultLogPathFile;
		fileName += ConfigApp.defaulLogFileNamePrefix;
		fileName += "_" + date + "_" + this.subjSession + "." + ConfigApp.defaulLogFileExtension;
		
		File errorWarningLog = new File( fileName );
		
		try 
		{
			errorWarningLog.getParentFile().mkdirs();
			errorWarningLog.createNewFile();
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
		
		this.writer = new BufferedWriter( new FileWriter( errorWarningLog, true ) );		
		
		 this.worker = new Thread(() -> 
		 {
			 List<String> buffer = new ArrayList< String >( this.batch_size );

			 try 
			 {
				 while (running || !queue.isEmpty()) 
				 {
					 String msg = queue.take();  // FIFO garantizado
					 
					 buffer.add( msg );
					 
					 if( buffer.size() >= this.batch_size )
					 {
						 flushBatch( buffer );
					 }
				 }
			 } 
			 catch (InterruptedException ignored) 
			 {
				 // Interrupción usada para cerrar
			 }
			 catch (IOException e) 
			 {
				 throw new RuntimeException("Error escribiendo en el log", e);
			 }
			 finally 
			 { 
				// Al cerrar, escribir lo que quede pendiente
				 if (!buffer.isEmpty()) 
				 {
					 try 
					 {
						 flushBatch( buffer );
					 } 
					 catch (IOException e) 
					 {
						 e.printStackTrace();
					 }
				 }

				 
				 try 
				 {
					 writer.flush();
					 writer.close();
				 } 
				 catch (IOException ignored) 
				 {}
			 }
		 });

		 this.worker.setDaemon(true);
		 this.worker.start();
	}
			 
	private void flushBatch( List<String> buffer ) throws IOException 
	{
		for (String s : buffer) 
		{
			this.writer.write(s);
			this.writer.newLine();
		}
		
		this.writer.flush();
		buffer.clear();
	} 
	
	public String getSubjectSessionID()
	{
		return this.subjSession;
	}
	
	public void write( String tx, int msgType ) throws IllegalStateException
	{	
		 if (!running)
		 {
			 throw new IllegalStateException("Logger cerrado");
		 }
	 
		 queue.add( getMessageHeader( msgType ) );
		 queue.add( tx );
	}
	
	private String getMessageHeader( int msgType )
	{
		String header = "\n"+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format( new Date());
		header += " (" + this.getSubjectSessionID() + "):";
		
		String type = "WARNING";
		if( msgType == ExceptionMessage.ERROR_MESSAGE )
		{
			type = "ERROR";
		}
		else if( msgType == ExceptionMessage.INFO_MESSAGE )
		{
			type = "INFO";
		}
																
		header += type + "\n";
		header += StringUtils.repeat( "-", header.length() );
		header += "\n";
		
		return header;
	}
	
	public void close() 
	{
        running = false;
        worker.interrupt();
    }
}
