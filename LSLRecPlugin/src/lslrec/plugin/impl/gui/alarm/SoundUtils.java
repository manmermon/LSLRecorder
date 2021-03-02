/*
 * Copyright 2011-2013 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of CLIS.
 *
 *   CLIS is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   CLIS is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with CLIS.  If not, see <http://www.gnu.org/licenses/>.
 *   
 */

package lslrec.plugin.impl.gui.alarm;

import java.io.ByteArrayInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class SoundUtils
{
	  public static float SAMPLE_RATE = 8000f;
	  
	  public static SourceDataLine sdl = null;

	  public static void tone(int hz, int msecs) throws LineUnavailableException 
	  {
	     tone(hz, msecs, 1.0);
	  }

	  public static void tone(int hz, int msecs, double vol) throws LineUnavailableException 
	  {
		  byte[] buf = createToneBuffer( hz, msecs, vol );
		  
		  AudioFormat af = createAudioFormat( );
					    
		  sdl = AudioSystem.getSourceDataLine( af );
		  sdl.open( af );
		  sdl.start();
		  
		  sdl.write( buf, 0, buf.length );
		  
		  sdl.drain();
		  sdl.stop();
		  sdl.close();
	  }

	  public static void stop( )
	  {
		  if( sdl != null )
		  {
			  sdl.flush();
			  sdl.stop();
			  sdl.close();
		  }		  
	  }
	  
	  public static AudioInputStream getTone( int hz, int msecs, double vol ) throws LineUnavailableException
	  {
		  byte[] buf = createToneBuffer( hz, msecs, vol );
		  
		  AudioFormat af = createAudioFormat();
		  
		  return new AudioInputStream( new ByteArrayInputStream( buf ), af, buf.length );
	  }
	  
	  private static AudioFormat createAudioFormat( )
	  {  
		  return new AudioFormat( SAMPLE_RATE, // sampleRate
					            	8,           // sampleSizeInBits
					            	1,           // channels
					            	true,        // signed
					            	false);      // bigEndian
	  }	  
	  
	 private static byte[] createToneBuffer( int hz, int msecs, double vol )
	 {		 
		byte[] buf = new byte[ 8 * msecs ];
		  
		for (int i=0; i<buf.length; i++)			  
		{
			 double angle = i / (SAMPLE_RATE / hz) * 2.0 * Math.PI;
			 buf[ i ] = (byte)(Math.sin(angle) * 80.0 * vol);
		}
		
		return buf;
	 }
}