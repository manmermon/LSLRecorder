/**
 * 
 */
package lslrec.auxiliar.thread;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.BooleanControl;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.stoppableThread.AbstractStoppableThread;

import javax.sound.sampled.LineEvent.Type;


/**
 * @author Manuel Merino Monge
 *
 */
public class BeepSound extends AbstractStoppableThread
{
	public static final float SAMPLE_RATE = 8e3F; 
	
	private final int NumberOfBeeps = 4; 
	private final int BeepDuration = 50; // 50ms
	
	private List< Clip > lines = new ArrayList< Clip >();
	
	private Iterator< Clip > itLines = null;
	
	private AudioFormat af = null;
	
	private AtomicBoolean waitLock = new AtomicBoolean( true );
	
	
	public BeepSound() throws LineUnavailableException, IOException 
	{
		this.af = this.createAudioFormat();
		
		for( Byte[] beep : this.createToneBuffer( 500, this.BeepDuration, 1 ) )
		{
			Clip line = AudioSystem.getClip();	
			
			line.open( this.af, ConvertTo.Casting.ByterArray2byteArray( beep ), 0, beep.length );
			line.addLineListener( new LineListener() {
				
				@Override
				public void update(LineEvent event) 
				{
					if( event.getType() == Type.STOP )
					{
						line.setMicrosecondPosition( 0 );
						
						synchronized( line )
						{
							line.notify();
						}
					}
				}
			});
			
			this.lines.add( line );
		}
		
		this.itLines = this.lines.iterator();
		
		super.setPriority( Thread.MAX_PRIORITY );
		super.setDaemon( true );
		
		super.setName( this.getClass().getSimpleName() );
	}

	private List< Byte[] > createToneBuffer( int hz, int msecs, double vol )
	{		 
		List< Byte[] > beeps = new ArrayList< Byte[] >();
		
		for( int j = 0; j < this.NumberOfBeeps; j++ )
		{
			Byte[] beep = new Byte[ 8 * msecs ];
	
			for( int i = 0; i < beep.length; i++ )			  
			{
				double angle = i / (SAMPLE_RATE / (hz * (j + 1))) * 2.0 * Math.PI;
				beep[ i ] = (byte)(Math.sin(angle) * 80.0 * vol);
			}
			
			beeps.add( beep );
		}
		
		return beeps;
	}
	
	private AudioFormat createAudioFormat( )
	{  
		return new AudioFormat( SAMPLE_RATE, // sampleRate
								8,           // sampleSizeInBits
								1,           // channels
								true,        // signed
								false);      // bigEndian
	}	  
	
	public void play()
	{
		synchronized ( this )
		{
			this.waitLock.set( false );
			super.notify();
		}
	}
	
	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{	
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{	
	}
	
	@Override
	protected void startUp() throws Exception 
	{
		super.startUp();
			
		this.soundOn( true );		
	}
	
	@Override
	protected void runInLoop() throws Exception 
	{
		try
        {
            synchronized ( this ) 
            {
            	if( this.waitLock.get() )
            	{
            		super.wait();            		
            	}
            	
            	this.waitLock.set( true );
			}
        }
        catch (InterruptedException eaten)
        {
        }
	
		this.soundOn( false );
	}

	private void soundOn( boolean mute ) throws InterruptedException
	{
		for( int i = 0; i < 2; i++ )
		{
			if( this.itLines.hasNext() )
			{
				Clip line = this.itLines.next();
				
				if( mute ) 
				{
					((BooleanControl) line.getControl( BooleanControl.Type.MUTE )).setValue( true );
				}
				
				synchronized( line )
				{
					line.start();
					//line.drain();
					line.wait();
					//Thread.sleep( 50L );
				}
				line.drain();

				if( mute ) 
				{
					((BooleanControl) line.getControl( BooleanControl.Type.MUTE )).setValue( false );
				}
			}
			else if( !this.itLines.hasNext() )
			{
				this.itLines = this.lines.iterator();
			}
		}
	}
	
	@Override
	protected void targetDone() throws Exception 
	{
		super.targetDone();
		
		if( !this.itLines.hasNext() )
		{
			this.itLines = this.lines.iterator();
		}
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{
		for( Clip line : this.lines )
		{
			line.drain();
			line.stop();
			line.close();
		}
		
		super.cleanUp();		
	}
}
