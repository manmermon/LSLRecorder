/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018-2026 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.config;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

import lslrec.sockets.SocketMessageDelayCalculator;

public class GeneralSettings 
{		
		public static final String fullNameApp = "LSL Recorder";
		public static final String shortNameApp = "LSLRec";
		public static final Calendar buildDate = new GregorianCalendar( 2026, 6 - 1, 11 );
		
		//WEB
		public static final String url = "http://grupo.us.es/grupotais/";//"http://matrix.dte.us.es/grupotais/";
		public static final String authorEmail = "manmermon@dte.us.es";
		public static final String sourceURL = "https://github.com/manmermon/LSLRecorder";
		public static final String authorName = "Manuel Merino Monge";
		
		public static final int WRITING_TEST_TIME = 1000 * 60; // 1 minute
		
		public static final String version = "Version 4" 
												//+ "." + buildNum
												+ "." + ( buildDate.get( Calendar.YEAR ) % 100 )											
												+ "." + ( buildDate.get( Calendar.DAY_OF_YEAR ) ) 
												;
		
		public static final String appDateRange = "2018-" + buildDate.get( Calendar.YEAR );
		public static final String defaultPathFile = System.getProperty("user.dir") + File.separatorChar + "records" + File.separatorChar;
		
		public static final String defaultLogPathFile = System.getProperty("user.dir") + File.separatorChar + "logs" + File.separatorChar;
		public static final String defaulLogFileNamePrefix = "log_errorsWarnings" ;
		public static final String defaulLogFileExtension = "txt" ;
		
		public static final String defaultNameFileConfigExtension = "cfg";
						
		public static final String defaultNameOutputDataFile = "data"; //"data.clis";
		
		public static final String HEADER_SEPARATOR = ";" ;

		//public static final int DEFAULT_SEGMENTATION_BLOCK_SIZE = (int)( 10 * ( Math.pow( 2, 20 ) ) );
		
		public static final int DEFAULT_NUM_SOCKET_PING = SocketMessageDelayCalculator.DEFAULT_NUM_PINGS;
		
		public static final String SYSTEM_LIB_WIN_PATH = "systemLib/win/";
		public static final String SYSTEM_LIB_LINUX_PATH = "systemLib/linux/";
		public static final String SYSTEM_LIB_MACOS_PATH = "systemLib/macox/";
		//public static final String SYSTEM_LIB_PATH = System.getProperty( "user.dir" ) + "/systemLib/";
		
		//public static final int CHECKLIST_DEFAULT_LEN = 5;
}
