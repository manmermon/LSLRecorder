/* 
 * Copyright 2019 by Manuel Merino Monge <manmermon@dte.us.es>
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

package lslrec.control.message;

public class AppState 
{
	public enum State { SAVED, SAVING, RUN, STOPPING, STOP, WAIT, PREPARING, NONE };
	
	/*
	public static final String SAVED = "Saved";
	public static final String SAVING = "Saving";
	public static final String RUN = "Run";
	public static final String STOPPING = "Stopping";
	public static final String STOP = "Stop";
	public static final String WAIT = "Wait";											
	public static final String PREPARING = "Initiating";
	public static final String NONE = "";
	//*/
}
