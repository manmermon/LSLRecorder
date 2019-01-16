/*
 * Work based on Commands class of
 * CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.
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

package Controls.Messages;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import Config.Language.Language;

public class RegisterSyncMessages 
{
	public static final Integer NO_MARK = 0;
	
	public static String INPUT_STOP = "__stop";
	public static String INPUT_START = "__start";
	
	private static List< String > specialInputs = new ArrayList< String >();
	
	private static Map< String, SpecialInputMessage > outputDataFileMark = new LinkedHashMap< String, SpecialInputMessage >();
	
	static
	{
		specialInputs.add( INPUT_START.toLowerCase() );
		specialInputs.add( INPUT_STOP.toLowerCase() );
		
		clearSyncMessages();
	}
	
	/**
	 * 
	 * @param msg -> synchronization message
	 * @return True if input is special message; otherwise, False
	 */
	public static boolean isSpecialInputMessage( String msg )
	{
		boolean sp = false;
		
		SpecialInputMessage spMsg = outputDataFileMark.get( msg );
		if( spMsg != null )
		{
			sp = spMsg.isSpecial();
		}
		
		return sp;
	}
	
	/**
	 * 
	 * @return Number of special input messages.
	 */
	public static int getNumberOfSpecialInputMessages()
	{
		return outputDataFileMark.size();
	}
	
	/**
	 * 
	 * @return Special input messages and their meanings.
	 */
	public static Map< String, String > getInputSpecialMessageLengeds( boolean onlySpecial )
	{
		Map< String, String > msglegends = new LinkedHashMap< String, String >();
		
		for( String msg : outputDataFileMark.keySet() )
		{
			SpecialInputMessage in = outputDataFileMark.get( msg );
			if( !onlySpecial )
			{
				msglegends.put( msg, in.getMarkLegend() );
			}
			else if( in.isSpecial() )
			{
				msglegends.put( msg, in.getMarkLegend() );
			}
		}
		
		return msglegends;
	}
	
	/**
	 * Add a new synchronization message.
	 * 
	 * @param syncMsg	-> new message.
	 * 
	 * @return True if message is added, False if message is already there.
	 */
	public static boolean addSyncMessage( String syncMsg )
	{
		boolean added = false;
		
		if( !outputDataFileMark.containsKey( syncMsg ) )
		{
			added = true;
			int mark = 1 << outputDataFileMark.size();
			SpecialInputMessage in = new SpecialInputMessage( mark, syncMsg );
			outputDataFileMark.put( syncMsg, in );
		}
		
		return added;
	}
	
	/**
	 * Return mark of synchronization message.
	 * 
	 * @param syncMsg -> synchronization message.
	 * 
	 * @return synchronization mark.
	 */
	public static Integer getSyncMark( String syncMsg )
	{
		Integer markValue = new Integer( NO_MARK );
		
		SpecialInputMessage mark = outputDataFileMark.get( syncMsg );
		
		if( mark != null )
		{
			markValue = mark.getMarkValue();
		}
		
		return markValue;
	}		
	
	/**
	 * Return all marks and synchronization messages.
	 * 
	 * @return synchronization mark.
	 */
	public static Map< String, Integer > getSyncMessagesAndMarks( )
	{			
		Map< String, Integer > msgMarks = new LinkedHashMap< String, Integer >();
		
		for( String msg : outputDataFileMark.keySet() )
		{
			SpecialInputMessage in = outputDataFileMark.get( msg );
			msgMarks.put( msg, in.getMarkValue() );
		}
		
		return msgMarks;
	}		
	
	/**
	 * Remove message and its mark.
	 * 
	 * @param msg -> input message.
	 */
	public static void removeSyncMarks( String msg )
	{
		SpecialInputMessage inDel = outputDataFileMark.get( msg );

		if( inDel != null && !inDel.isSpecial() )
		{
			outputDataFileMark.remove( msg );
			
			int mark = inDel.getMarkValue();

			for( String m : outputDataFileMark.keySet() )
			{
				SpecialInputMessage in = outputDataFileMark.get( m );
				int val = in.getMarkValue();
				if( val > mark )
				{
					val = val >> 1;
					in.setMarkValue( val );
				}
			}
		}
	}
	
	/**
	 * A new message replace the previous one. If previous message not exist, the new input message is inserted.  
	 * 
	 * @param oldMsg -> Old message.
	 * @param newMsg -> new message.
	 * 
	 * @return True if new message is added.
	 */
	public static boolean updateSyncMessage( String oldMsg, String newMsg )
	{
		SpecialInputMessage spMsg = outputDataFileMark.get( oldMsg );
		
		boolean added = spMsg == null ;
		
		if( added )
		{			
			addSyncMessage( newMsg );
		}
		else
		{
			spMsg.setMarkText( newMsg );
			
			outputDataFileMark.remove( oldMsg );
			outputDataFileMark.put( newMsg, spMsg );
		}
		
		return added;
	}
	
	public static void clearSyncMessages()
	{
		outputDataFileMark.clear();
		
		int mark = 1;
		for( String msg : specialInputs )
		{
			SpecialInputMessage in = new SpecialInputMessage( mark, msg );
			if( mark == 1 )
			{
				in.setMarkLegendToken( Language.INPUT_START_LEGEND );
			}
			else if( mark == 2 )
			{
				in.setMarkLegendToken( Language.INPUT_STOP_LEGEND );
			}
			
			in.setSpecial( true );
			outputDataFileMark.put( msg, in );
			mark = mark << 1;
		}
	}
}
