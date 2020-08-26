/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
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

package config.language;

import java.util.ArrayList;
import java.util.List;

public class CaptionIDList
{
	public static final String ACTION_PLAY = "ACTION_PLAY";
	public static final String ACTION_CONFIG = "ACTION_CONFIG";
	public static final String ACTION_ABOUT = "ACTION_ABOUT";
	public static final String ACTION_GNU_GPL = "ACTION_GNU_GPL";
	
	public static final String SETTING_MENU_FILE = "SETTING_MENU_FILE";
	public static final String SETTING_MENU_LOAD = "SETTING_MENU_LOAD";
	public static final String SETTING_MENU_SAVE = "SETTING_MENU_SAVE";
	
	public static final String SETTING_TASK_SOCKET_TAB = "SETTING_TASK_SOCKET_TAB";	
	public static final String SETTING_TASK_SOCKET_TAB_INPUT_IP_TABLE_ADDRESS = "SETTING_TASK_SOCKET_TAB_INPUT_IP_TABLE_ADDRESS";
	public static final String SETTING_TASK_SOCKET_TAB_INPUT_IP_TABLE_PORT = "SETTING_TASK_SOCKET_TAB_INPUT_IP_TABLE_PORT";
	
	public static final String WINDOW_CLOSE_MSG = "WINDOW_CLOSE";
	public static final String WINDOW_LOAD_CONFIG_MSG = "WINDOW_LOAD_CONFIG_MSG";
	

	public static List<String> getCaptionIDList(  )
	{
		List<String> capIDs = new ArrayList<String>(  );

		capIDs.add( ACTION_PLAY );
		capIDs.add( ACTION_CONFIG );
		capIDs.add( ACTION_ABOUT );
		capIDs.add( ACTION_GNU_GPL );

		capIDs.add( SETTING_MENU_FILE );
		capIDs.add( SETTING_MENU_LOAD );
		capIDs.add( SETTING_MENU_SAVE );

		capIDs.add( SETTING_TASK_SOCKET_TAB );

		capIDs.add( SETTING_TASK_SOCKET_TAB_INPUT_IP_TABLE_ADDRESS );
		capIDs.add( SETTING_TASK_SOCKET_TAB_INPUT_IP_TABLE_PORT );

		
		capIDs.add( WINDOW_CLOSE_MSG );
		capIDs.add( WINDOW_LOAD_CONFIG_MSG );

		return capIDs;
	}
}