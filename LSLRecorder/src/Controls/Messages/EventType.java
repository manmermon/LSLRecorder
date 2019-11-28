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

package Controls.Messages;

/**
 * Class with system events identifiers.
 * 
 * @author Manuel Merino Monge
 *
 */
public class EventType
{
  public static final String THREAD_STOP = "thread stop";
  public static final String SERVER_THREAD_STOP = "server stop";
  
  public static final String SOCKET_CHANNEL_CLOSE = "socket channel close";
  public static final String SOCKET_EVENTS = "socket events";
  public static final String SOCKET_INPUT_MSG = "input message";
  public static final String SOCKET_OUTPUT_MSG_OK = "output message ok";
  public static final String SOCKET_OUTPUT_MSG_SEND = "send message";
  public static final String SOCKET_CONNECTION_PROBLEM = "connection problem";
  public static final String SOCKET_PING_PROBLEM = "ping problem";
  public static final String SOCKET_OUTPUT_SOCKET_CLOSES = "Output socket closes";
  public static final String SOCKET_INOUT_CHANNEL_CREATED = "socket channel create";
  public static final String SOCKET_READER_THREAD_STOP = "socket reader thread closed";
  public static final String SOCKET_WRITER_THREAD_STOP = "socket writer thread closed";
  public static final String SOCKET_CONNECTION_DONE = "socket connect";
  public static final String SOCKET_PING_END = "ping end";
  public static final String SOCKET_SERVER_STOP = "stop server";
  
  
  public static final String PROBLEM = "problem";
  public static final String SAVED_OUTPUT_TEMPORAL_FILE = "saved output temporal file";
  public static final String CONVERT_OUTPUT_TEMPORAL_FILE = "convert output temporal file";
  public static final String SAVED_SYNCMARKER_TEMPORAL_FILE = "saved sync marker file";
  public static final String TEST_OUTPUT_TEMPORAL_FILE = "test output temporal file";
  public static final String TEST_WRITE_TIME= "test write time";
  public static final String WARNING = "warning";
  
  public static final String OUTPUT_DATA_FILE_SAVED = "output data file saved";
  public static final String ALL_OUTPUT_DATA_FILES_SAVED = "all output data files saved";
  public static final String INPUT_MARK_READY = "input mark ready";
  
  public static final String OUTPUT_FILE_WRITER_READY = "writer ready";  
}