/**
 * 
 */
package lslrec.plugin.impl.gui.alarm;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.sound.sampled.LineUnavailableException;

import lslrec.stoppableThread.AbstractStoppableThread;

/**
 * @author Manuel Merino Monge
 *
 */
public class AlarmBeep extends AbstractStoppableThread 
{
	private int hz = 1500;
	private int msecs = 150;
	private int vol = 500;
	private int beep_rep = 1;
	
	private int wait_between_tone = 300;
	
	private ActionListener action = null;
	
	/**
	 * 
	 */
	public AlarmBeep( int freq, int time, int vol, int beepRep, int waitBetweenTone, ActionListener act ) 
	{
		this.hz = freq;
		this.msecs = time;
		this.vol = vol;
		this.beep_rep = beepRep;
		this.wait_between_tone = waitBetweenTone;
		
		this.action = act;
		
		super.setName( this.getClass().getSimpleName() );
	}
	
	@Override
	protected void postStopThread(int arg0) throws Exception 
	{	
	}

	@Override
	protected void preStopThread(int arg0) throws Exception 
	{	
	}

	@Override
	protected void runInLoop() throws Exception 
	{	
		synchronized( this )
		{
			super.wait();
		}
		
		try 
		{
			for( int count = beep_rep; count > 0; count-- )
			{
				SoundUtils.tone( hz, msecs, vol );
				
				super.wait( wait_between_tone );
			}
		}
		catch( InterruptedException e )
		{				
		}
		catch (LineUnavailableException e ) 
		{
			e.printStackTrace();
		}
		finally
		{
			if( this.action != null )
			{
				this.action.actionPerformed( new ActionEvent( this , ActionEvent.ACTION_FIRST, "Beep ended" ) );
			}
		}
	}
	
	@Override
	protected void runExceptionManager(Throwable e) 
	{
		if( !( e instanceof InterruptedException ) )
		{
			super.runExceptionManager( e );
		}
	}
}
