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

import java.util.LinkedHashMap;
import java.util.Map;

import Config.Language.Language;

public class RegisterSyncMessagesCopy 
{
	public static final Integer NO_MARK = 0;
	
	public static String INPUT_STOP = "__stop";
	public static String INPUT_START = "__start";
	
	private static Map< String, String > msglegends = new LinkedHashMap<String, String>();
	
	private static Map< String, Integer > outputDataFileMark = new LinkedHashMap< String, Integer >();
	
	static
	{
		msglegends.put( INPUT_START.toLowerCase(), Language.getLocalCaption( Language.INPUT_START_LEGEND ).toLowerCase() );
		msglegends.put( INPUT_STOP.toLowerCase(), Language.getLocalCaption( Language.INPUT_STOP_LEGEND ).toLowerCase() );
		
		clearSyncMessages();
	}
	
	/**
	 * 
	 * @param msg -> synchronization message
	 * @return True if input is special message; otherwise, False
	 */
	public static boolean isSpecialInputMessage( String msg )
	{
		return msglegends.keySet().contains( msg );
	}
	
	/**
	 * 
	 * @return Number of special input messages.
	 */
	public static int getNumberOfSpecialInputMessages()
	{
		return msglegends.size();
	}
	
	/**
	 * 
	 * @return Special input messages and their meanings.
	 */
	public static Map< String, String > getInputSpecialMessageLengeds( )
	{
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
			outputDataFileMark.put( syncMsg, mark );
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
		Integer mark = new Integer( NO_MARK );
		
		mark = outputDataFileMark.get( syncMsg );
		
		if( mark == null )
		{
			mark = NO_MARK;
		}
		
		return mark;
	}		
	
	/**
	 * Return all marks and synchronization messages.
	 * 
	 * @return synchronization mark.
	 */
	public static Map< String, Integer > getSyncMessagesAndMarks( )
	{			
		return outputDataFileMark;
	}		
	
	/**
	 * Remove message and its mark.
	 * 
	 * @param msg -> input message.
	 */
	public static void removeSyncMarks( String msg )
	{
		if( !msglegends.containsKey( msg.toLowerCase() ) )
		{
			Integer mark = outputDataFileMark.remove( msg );
			
			if( mark != null )
			{
				for( String m : outputDataFileMark.keySet() )
				{
					Integer val = outputDataFileMark.get( m );
					if( val > mark )
					{
						val = val >> 1;
						outputDataFileMark.put( m, val );
					}
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
		Integer v = outputDataFileMark.get( oldMsg );
		
		boolean added = addSyncMessage( newMsg );
		
		if( added && v != null )
		{			
			outputDataFileMark.remove( oldMsg );
			outputDataFileMark.put( newMsg, v );
		}
		
		return added;
	}
	
	public static void clearSyncMessages()
	{
		outputDataFileMark.clear();
		
		int mark = 1;
		for( String msg : msglegends.keySet() )
		{
			outputDataFileMark.put( msg, mark );
			mark = mark << 1;
		}
	}
}
