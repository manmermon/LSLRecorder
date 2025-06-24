/*
f * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
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


package lslrec.config.language;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import lslrec.config.ConfigApp;
import lslrec.dataStream.sync.SyncMethod;

public class Language 
{
	public static final String DefaultFolder = System.getProperty( "user.dir" ) + "/Lang/";

	public static final String defaultLanguage = "default";

	public static final String FILE_EXTENSION = "lang";

	private static Locale localLang = null;

	public static final String LANGUAGE = "LANGUAGE";

	public static final String ACTION_PLAY = "ACTION_PLAY";
	public static final String ACTION_STOP = "ACTION_STOP";

	public static final String MENU_CONFIG = "MENU_CONFIG";
	public static final String MENU_ABOUT = "MENU_ABOUT";
	public static final String MENU_GNU_GPL = "MENU_GNU_GPL";
	public static final String MENU_PREFERENCE = "MENU_PREFERENCE";
	public static final String MENU_SHOW_LOG = "MENU_SHOW_LOG";
	public static final String MENU_CONVERT_BIN = "MENU_CONVERT_BIN";
	public static final String MENU_CONVERT_CLIS = "MENU_CONVERT_CLIS";
	public static final String MENU_FILE = "MENU_FILE";
	public static final String MENU_LOAD = "MENU_LOAD";
	public static final String MENU_SAVE = "MENU_SAVE";
	public static final String MENU_EXIT = "MENU_EXIT";
	public static final String MENU_WRITE_TEST = "MENU_WRITE_TEST";
	public static final String MENU_LIBRARY = "MENU_LIBRARY";
	public static final String MENU_ADVANCED = "MENU_ADVANCED";
	public static final String MENU_THEME = "MENU_THEME";
	
	public static final String LOAD_TEXT = "LOAD_TEXT";
	
	public static final String OTHERS_TEXT = "OTHERS_TEXT";


	public static final String SETTING_SOCKET_TAB_INPUT_PANEL = "SETTING_SOCKET_TAB_INPUT_PANEL";
	public static final String SETTING_SOCKET_TAB_INPUT_IP_TABLE_ADDRESS = "SETTING_SOCKET_TAB_INPUT_IP_TABLE_ADDRESS";
	public static final String SETTING_SOCKET_TAB_INPUT_IP_TABLE_PORT = "SETTING_SOCKET_TAB_INPUT_IP_TABLE_PORT";
	
	public static final String SETTING_LSL_MARK = "SETTING_LSL_MARK";
	public static final String SETTING_LSL_FILE = "SETTING_LSL_FILE";
	public static final String SETTING_LSL_OUTPUT_FORMAT = "SETTING_LSL_OUTPUT_FORMAT";
	public static final String SETTING_COMPRESSOR = "SETTING_COMPRESSOR";
	public static final String SETTING_LSL_REFRESH = "SETTING_LSL_REFRESH";
	public static final String SETTING_LSL_DEVICES = "SETTING_LSL_DEVICES";
	public static final String SETTING_LSL_PLOT = "SETTING_LSL_PLOT";	
	public static final String SETTING_LSL_EXTRA = "SETTING_LSL_EXTRA";
	public static final String SETTING_LSL_STREAM_PLOT = "SETTING_LSL_STREAM_PLOT";
	public static final String SETTING_LSL_CHUNCK = "SETTING_LSL_CHUNCK";
	public static final String SETTING_LSL_INTERLEAVED = "SETTING_LSL_INTERLEAVED";
	public static final String SETTING_LSL_SYNC = "SETTING_LSL_SYNC";
	public static final String SETTING_LSL_NAME = "SETTING_LSL_NAME";	

	public static final String SETTING_LSL_SYNC_TOOLTIP = "SETTING_LSL_SYNC_TOOLTIP";
	public static final String SETTING_LSL_EXTRA_TOOLTIP = "SETTING_LSL_EXTRA_TOOLTIP";
	public static final String SETTING_LSL_STREAM_PLOT_TOOLTIP = "SETTING_LSL_STREAM_PLOT_TOOLTIP";
	public static final String SETTING_LSL_CHUNCK_TOOLTIP = "SETTING_LSL_CHUNCK_TOOLTIP";
	public static final String SETTING_LSL_INTERLEAVED_TOOLTIP = "SETTING_LSL_INTERLEAVED_TOOLTIP";
	public static final String SETTING_LSL_NAME_TOOLTIP = "SETTING_LSL_NAME_TOOLTIP";
	
	public static final String SETTING_LSL_SEARCHING_TIME = "SETTING_LSL_SEARCHING_TIME";
	public static final String SETTING_RECORDING_CHECKER_TIMER= "SETTING_RECORDING_CHECKER_TIMER";
	public static final String SETTING_SEGMENT_BLOCK_SIZE= "SETTING_SEGMENTATION_BLOCK_SIZE";

	public static final String LSL_PLOT_FILTERS = "LSL_PLOT_FILTERS";
	public static final String LSL_PLOT_FILTER_LEGEND = "LSL_PLOT_FILTER_LEGEND";
	public static final String LSL_BIN_DATA_FILES = "LSL_BIN_DATA_FILES";
	public static final String LSL_BIN_TIME_FILES = "LSL_BIN_TIME_FILES";
	public static final String LSL_DATA_TYPE = "LSL_DATA_TYPE";
	public static final String LSL_CHANNELS = "LSL_CHANNELS";
	public static final String LSL_CHANNEL = "LSL_CHANNEL";
	public static final String LSL_XML_DESCRIPTION = "LSL_XML_DESCRIPTION";
	public static final String LSL_DEL_BINS = "LSL_DEL_BINS";

	public static final String CHECK_SPECIAL_IN_WARNING_MSG = "CHECK_SPECIAL_IN_WARNING_MSG";
	public static final String CHECK_SYNC_METHOD_WARNING_MSG = "CHECK_SYNC_METHOD_WARNING_MSG";
	public static final String CHECK_SELECTED_STREAM_ERROR_MSG = "CHECK_NON_SELECT_LSL_ERROR_MSG";
	public static final String CHECK_SYNC_UNSELECTABLE_ERROR_MSG = "CHECK_SYNC_LSL_UNSELECTABLE_ERROR_MSG";
	public static final String CHECK_DEVICES_CHANGE_WARNING_MSG = "CHECK_LSL_DEVICES_CHANGE_WARNING_MSG";
	public static final String CHECK_SYNC_STREAM_WARNING_MSG = "CHECK_LSL_SYNC_STREAM_WARNING_MSG";
	public static final String CHECK_LSL_CHUNCKSIZE_WARNING_MSG = "CHECK_LSL_CHUNCKSIZE_WARNING_MSG";
	public static final String CHECK_SYNC_NO_SELECT_STREAM_WARNING_MSG = "CHECK_SYNC_UNSELECT_STREAM_WARNING_MSG";
	public static final String CHECK_SELECTED_DATA_STREAMS_MSG = "CHECK_SELECTED_STREAMS_MSG";
	public static final String CHECK_SELECTED_SYNC_STREAMS_MSG = "CHECK_SELECTED_SYNC_STREAMS_MSG";

	public static final String APPLY_TEXT = "APPLY_TEXT";
	public static final String INSERT_TEXT = "INSERT_TEXT";
	public static final String DELETE_TEXT = "DELETE_TEXT";
	public static final String PROCESS_TEXT = "PROCESS_TEXT";
	public static final String POST_PROCESS_TEXT = "POST_PROCESS_TEXT";
	public static final String SAVE_DATA_PROCESSING_TEXT = "DATA_PROCESSING_TEXT";
	public static final String TAKE_OFF_TEXT = "TAKE_OFF_TEXT";

	public static final String DIALOG_SAVE = "DIALOG_SAVE";
	public static final String DIALOG_ERROR = "DIALOG_ERROR";
	public static final String DIALOG_LOAD = "DIALOG_LOAD";
	public static final String DIALOG_SELECT_OPTS = "DIALOG_SELECT_OPTS";
	public static final String DIALOG_REPLACE_FILE_MESSAGE = "DIALOG_REPLACE_FILE_MESSAGE";
	public static final String DIALOG_SELECT_UESR_FILE = "DIALOG_SELECT_UESR_FILE";

	public static final String FILE_NOT_FOUND = "FILE_NOT_FOUND";

	public static final String MSG_WINDOW_CLOSE = "MSG_WINDOW_CLOSE";
	public static final String MSG_WINDOW_LOAD_CONFIG = "MSG_WINDOW_LOAD_CONFIG_MSG";
	public static final String MSG_INTERRUPT = "MSG_INTERRUPT";
	public static final String MSG_APP_STATE = "MSG_APP_STATE";
	public static final String MSG_WARNING = "MSG_WARNING";
	public static final String MSG_SELECTED_LSL_SYNC_STREAM_ERROR = "MSG_SELECTED_LSL_SYNC_STREAM_ERROR"; 
	public static final String MSG_LSL_PLOT_ERROR = "SETTING_LSL_PLOT_ERROR";
	public static final String MSG_WARNING_DATA_PROCESSING = "MSG_WARNING_DATA_PROCESSING";
	public static final String MSG_ILLEGAL_VALUE = "MSG_ILLEGAL_VALUE";
	public static final String MSG_ENCODER_PLUGIN_NO_FOUND = "MSG_ENCODER_PLUGIN_NO_FOUND";
	public static final String MSG_CANCEL_PROCESS = "MSG_CANCEL_PROCESS";
	public static final String MSG_ERROR_NUMBER_SELECTED_DATA_STREAMS = "MSG_ERROR_NUMBER_SELECTED_DATA_STREAMS";
	public static final String MSG_ERROR_NUMBER_SELECTED_SYNC_STREAMS = "MSG_ERROR_NUMBER_SELECTED_SYNC_STREAMS";
	
	public static final String INFO_STATE_LABEL = "INFO_STATE_LABEL";
	public static final String INFO_SESSION_TIME_LABEL = "INFO_SESSION_TIME_LABEL";

	public static final String ABOUT_WEB_LABEL = "ABOUT_WEB_LABEL";
	public static final String ABOUT_AUTHOR_LABEL = "ABOUT_AUTHOR_LABEL";
	public static final String ABOUT_EMAIL_LABEL = "ABOUT_EMAIL_LABEL";
	public static final String ABOUT_SOURCE_CODE_LABEL = "ABOUT_SOURCE_CODE_LABEL";

	public static final String GENERAL_WAIT_MSG = "WAIT_MSG";

	public static final String TOO_MUCH_TIME = "TOO_MUCH_TIME";
	public static final String FORCE_QUIT = "FORCE_QUIT";
	public static final String WAIT = "WAIT";
	public static final String WAITING = "WAITING";
	public static final String LOG = "LOG";
	public static final String APP_STATE_LOG = "APP_STATE_LOG";

	public static final String CLEAR = "CLEAR";
	public static final String INPUT_MSGS = "INPUT_MSGS";
	public static final String OUTPUT_MSGS = "OUTPUT_MSGS";
	public static final String OK_TEXT = "OK_TEXT";
	public static final String CANCEL_TEXT = "CANCEL_TEXT";
	public static final String SELECT_TEXT = "SELECT_TEXT";
	public static final String SELECTED_TEXT = "SELECTED_TEXT";
	public static final String SELECTED_PROCESSING_TEXT = "SELECTED_PROCESSING_TEXT";
	public static final String SELECTED_POST_PROCESSING_TEXT = "SELECTED_POST_PROCESSING_TEXT";
	public static final String UP_TEXT = "UP_TEXT";
	public static final String DOWN_TEXT = "DOWN_TEXT";
	public static final String REPEAT_TEXT = "REPEAT_TEXT";
	public static final String OPTIONS_TEXT = "OPTIONS_TEXT";

	public static final String INPUT_TEXT = "INPUT_TEXT";
	public static final String OUTPUT_TEXT = "OUTPUT_TEXT";
	public static final String MSG_TEXT = "MSG_TEXT";
	public static final String MSGS_TEXT = "MSGS_TEXT";
	public static final String CONTINUE_TEXT = "CONTINUE_TEXT";
	public static final String DESCRIPTION_TEXT = "DESCRIPTION_TEXT";
	public static final String ENCRYPT_KEY_TEXT = "ENCRYPT_KEY";
	public static final String PARALLELIZE_TEXT = "PARALLELIZE_TEXT";
	public static final String PASSWORD_TEXT = "PASSWORD_TEXT";
	public static final String DECRYPT_KEY_TEXT = "DECRYPT_KEY_TEXT";
	public static final String DATA_CHART_SUMMARY_TEXT = "DATA_CHART_SUMMARY_TEXT";
	public static final String FOLDER_TEXT = "FOLDER_TEXT";
	public static final String SUBJECT_ID_TEXT = "SUBJECT_ID_TEXT";
	public static final String TEST_ID_TEXT = "TEST_ID_TEXT";
	public static final String FILENAME_TEXT = "FILENAME_TEXT";
		
	public static final String SORT_TEXT = "SORT_TEXT";

	public static final String PROBLEM_TEXT = "PROBLEM_TEXT";
	
	public static final String CHECKLIST_TEXT = "CHECKLIST_TEXT";

	public static final String COPY = "COPY";
	public static final String COPY_ALL = "COPY_ALL";
	public static final String CUT = "CUT";

	public static final String AUTOSCROLL = "AUTOSCROLL";

	public static final String SETTING_SYNC_METHOD = "SETTING_SYNC_METHOD";
	public static final String SETTING_SPECIAL_IN_METHOD = "SETTING_SPECIAL_IN_METHOD";
	public static final String SETTING_SPECIAL_IN_METHOD_LEGEND = "SETTING_SPECIAL_IN_METHOD_LEGEND";

	public static final String INPUT_START_LEGEND = "INPUT_START_LEGEND";
	public static final String INPUT_STOP_LEGEND = "INPUT_STOP_LEGEND";
	public static final String LANGUAGE_TEXT = "LANGUAGE_TEXT";
	
	public static final String SETTING_PLUGIN = "SETTING_PLUGIN";	
	public static final String TRIALS_TEXT = "TEST_TEXT";
	public static final String ENCODER_TEXT = "ENCODER_TEXT";	
	
	public static final String MSG_DATA_PROCESSING_STREAMS_CHANGED = "MSG_DATA_PROCESSING_STREAMS_CHANGED";
	
	public static final String DEL_BINARY_FILES = "DEL_BINARY_FILES";
	public static final String FULLSCREEN = "FULLSCREEN";
	
	public static final String WIN_SIZE_TEXT = "WIN_SIZE_TEXT";
	public static final String WIDTH_TEXT = "WIDTH_TEXT";
	public static final String HEIGHT_TEXT = "HEIGHT_TEXT";
	public static final String WINDOW_TEXT = "WINDOW_TEXT";
	
	public static final String VARIABLE_TEXT = "VARIABLE_TEXT";
	public static final String VARIABLES_TEXT = "VARIABLE_TEXT";
	public static final String STEP_TEXT = "STEP_TEXT";
	public static final String ALL_TEXT = "ALL_TEXT";
	public static final String NONE_TEXT = "NONE_TEXT";
	public static final String XAXIS_TEXT = "XAXIS_TEXT";
	public static final String RELATIVE_TEXT = "RELATIVE_TEXT";
	
	private static Map<String, Caption> captions = new HashMap<String, Caption>();

	static 
	{		
		
		captions.put(LANGUAGE, new Caption(LANGUAGE, defaultLanguage, defaultLanguage));
		
		
		captions.put( MSG_ERROR_NUMBER_SELECTED_DATA_STREAMS, new Caption(MSG_ERROR_NUMBER_SELECTED_DATA_STREAMS, defaultLanguage, "Number of selected data streams is not equal to " ) );		
		captions.put( MSG_ERROR_NUMBER_SELECTED_SYNC_STREAMS, new Caption(MSG_ERROR_NUMBER_SELECTED_SYNC_STREAMS, defaultLanguage, "Number of selected sync streams is not equal to " ) );
		captions.put( CHECK_SELECTED_DATA_STREAMS_MSG, new Caption(CHECK_SELECTED_DATA_STREAMS_MSG, defaultLanguage, "Number of selected data streams equal to " ) );
		captions.put( CHECK_SELECTED_SYNC_STREAMS_MSG, new Caption(CHECK_SELECTED_SYNC_STREAMS_MSG, defaultLanguage, "Number of selected sync streams equal to " ) );
		
		captions.put( CHECKLIST_TEXT, new Caption(CHECKLIST_TEXT, defaultLanguage, "Checklist" ) );
		captions.put( FOLDER_TEXT, new Caption( FOLDER_TEXT, defaultLanguage, "Folder" ) );
		captions.put( SUBJECT_ID_TEXT, new Caption( SUBJECT_ID_TEXT, defaultLanguage, "Subject id" ) );
		captions.put( TEST_ID_TEXT, new Caption( TEST_ID_TEXT, defaultLanguage, "Test id" ) );
		captions.put( FILENAME_TEXT, new Caption( FILENAME_TEXT, defaultLanguage, "File name" ) );
		captions.put( TAKE_OFF_TEXT, new Caption( TAKE_OFF_TEXT, defaultLanguage, "Take off" ) );
		
		captions.put( DATA_CHART_SUMMARY_TEXT, new Caption( DATA_CHART_SUMMARY_TEXT, defaultLanguage, "Data chart summary" ) );

		captions.put( RELATIVE_TEXT, new Caption( RELATIVE_TEXT,  defaultLanguage, "Relative" ) );
		captions.put( XAXIS_TEXT, new Caption( XAXIS_TEXT,  defaultLanguage, "X axis" ) );
		captions.put( VARIABLE_TEXT, new Caption( VARIABLE_TEXT,  defaultLanguage, "Variable" ) );
		captions.put( VARIABLES_TEXT, new Caption( VARIABLE_TEXT,  defaultLanguage, "Variables" ) );
		captions.put( STEP_TEXT, new Caption( STEP_TEXT,  defaultLanguage, "Step" ) );
		captions.put( ALL_TEXT, new Caption( ALL_TEXT,  defaultLanguage, "All" ) );
		captions.put( NONE_TEXT, new Caption( NONE_TEXT,  defaultLanguage, "None" ) );

		captions.put( MSG_CANCEL_PROCESS, new Caption( MSG_CANCEL_PROCESS,  defaultLanguage, "Cancel process." ) );
		captions.put( MSG_ENCODER_PLUGIN_NO_FOUND, new Caption( MSG_ENCODER_PLUGIN_NO_FOUND,  defaultLanguage, "Encoder plugin no found" ) );
		captions.put( SETTING_SEGMENT_BLOCK_SIZE, new Caption( SETTING_SEGMENT_BLOCK_SIZE,  defaultLanguage, "Segment block size (MiB)" ) );
		captions.put( SETTING_RECORDING_CHECKER_TIMER, new Caption( SETTING_RECORDING_CHECKER_TIMER,  defaultLanguage, "Recording checking timer (sec or v / Fsampling)" ) );
		captions.put( MSG_ILLEGAL_VALUE, new Caption( MSG_ILLEGAL_VALUE,  defaultLanguage, "Illegal value" ) );
		captions.put( SETTING_LSL_SEARCHING_TIME, new Caption( SETTING_LSL_SEARCHING_TIME,  defaultLanguage, "Stream searching time (s)" ) );
		captions.put( MENU_ADVANCED, new Caption( MENU_ADVANCED,  defaultLanguage, "Advanced" ) );
		captions.put( MENU_THEME, new Caption( MENU_THEME,  defaultLanguage, "Theme" ) );
		
		captions.put(DEL_BINARY_FILES, new Caption(DEL_BINARY_FILES, defaultLanguage, "Delete binary files"));
		captions.put( FULLSCREEN, new Caption(FULLSCREEN, defaultLanguage, "fullscreen"));
		captions.put( WIN_SIZE_TEXT, new Caption(WIN_SIZE_TEXT, defaultLanguage, "Window size"));
		captions.put( WIDTH_TEXT, new Caption(WIDTH_TEXT, defaultLanguage, "Width"));
		captions.put( HEIGHT_TEXT, new Caption(HEIGHT_TEXT, defaultLanguage, "Height"));
		captions.put( WINDOW_TEXT, new Caption(WINDOW_TEXT, defaultLanguage, "Window"));
		
		captions.put( MENU_LIBRARY, new Caption( MENU_LIBRARY,  defaultLanguage, "Library" ) );		
		
		captions.put( MSG_WARNING_DATA_PROCESSING
						, new Caption( MSG_WARNING_DATA_PROCESSING, defaultLanguage
										, "Warning: data processing can reduce system efficiency."));
		
		captions.put(SETTING_SYNC_METHOD, new Caption(SETTING_SYNC_METHOD, defaultLanguage, "Sync Method"));
		captions.put(SETTING_SPECIAL_IN_METHOD,
				new Caption(SETTING_SPECIAL_IN_METHOD, defaultLanguage, "Special input messages"));
		captions.put(SETTING_SPECIAL_IN_METHOD_LEGEND,
				new Caption(SETTING_SPECIAL_IN_METHOD_LEGEND, defaultLanguage, "Reserved input (mark, message)"));

		captions.put(GENERAL_WAIT_MSG, new Caption(GENERAL_WAIT_MSG, defaultLanguage,
											"Wait to the process finishes. It may take several minutes."));

		captions.put(MSG_WINDOW_CLOSE,
				new Caption(MSG_WINDOW_CLOSE, defaultLanguage, "Processes are closing. Wait to exit..."));
		captions.put(MSG_WINDOW_LOAD_CONFIG, new Caption(MSG_WINDOW_LOAD_CONFIG, defaultLanguage, "Load setting..."));
		captions.put(MSG_INTERRUPT, new Caption(MSG_INTERRUPT, defaultLanguage, "Interrupt"));
		captions.put(MSG_APP_STATE, new Caption(MSG_APP_STATE, defaultLanguage, "App's state is "));
		captions.put(MSG_WARNING, new Caption(MSG_WARNING, defaultLanguage, "Warning"));
		captions.put(MSG_SELECTED_LSL_SYNC_STREAM_ERROR, new Caption(MSG_SELECTED_LSL_SYNC_STREAM_ERROR, defaultLanguage, "Option Sync for streams not enable. Select " + SyncMethod.SYNC_STREAM + " as sync method." ));

		captions.put(INFO_STATE_LABEL, new Caption(INFO_STATE_LABEL, defaultLanguage, "State"));
		captions.put(INFO_SESSION_TIME_LABEL, new Caption(INFO_SESSION_TIME_LABEL, defaultLanguage, "Session time"));

		captions.put(ABOUT_WEB_LABEL, new Caption(ABOUT_WEB_LABEL, defaultLanguage, "Web"));
		captions.put(ABOUT_AUTHOR_LABEL, new Caption(ABOUT_AUTHOR_LABEL, defaultLanguage, "Author"));
		captions.put(ABOUT_EMAIL_LABEL, new Caption(ABOUT_EMAIL_LABEL, defaultLanguage, "Email"));
		captions.put(ABOUT_SOURCE_CODE_LABEL,
				new Caption(ABOUT_SOURCE_CODE_LABEL, defaultLanguage, "Source code available on"));

		captions.put(DIALOG_SAVE, new Caption(DIALOG_SAVE, defaultLanguage, "Save"));
		captions.put(DIALOG_ERROR, new Caption(DIALOG_ERROR, defaultLanguage, "Error"));
		captions.put(DIALOG_LOAD, new Caption(DIALOG_LOAD, defaultLanguage, "Load"));
		captions.put(DIALOG_SELECT_OPTS, new Caption(DIALOG_SELECT_OPTS, defaultLanguage, "Select an option"));
		captions.put(DIALOG_REPLACE_FILE_MESSAGE,
				new Caption(DIALOG_REPLACE_FILE_MESSAGE, defaultLanguage, "Replace existing file?"));
		captions.put(DIALOG_SELECT_UESR_FILE, new Caption(DIALOG_SELECT_UESR_FILE, defaultLanguage, "Select file(s)"));

		captions.put(ACTION_PLAY, new Caption(ACTION_PLAY, defaultLanguage, "Play"));
		captions.put(ACTION_STOP, new Caption(ACTION_STOP, defaultLanguage, "Stop"));

		captions.put(MENU_CONFIG, new Caption(MENU_CONFIG, defaultLanguage, "Config"));
		captions.put(MENU_ABOUT, new Caption(MENU_ABOUT, defaultLanguage, "About " + ConfigApp.shortNameApp ));
		captions.put(MENU_GNU_GPL, new Caption(MENU_GNU_GPL, defaultLanguage, "GNU GLP"));
		captions.put(MENU_PREFERENCE, new Caption(MENU_PREFERENCE, defaultLanguage, "Preference"));

		captions.put(MENU_SHOW_LOG, new Caption(MENU_SHOW_LOG, defaultLanguage, "Show log" ) );
		captions.put(MENU_FILE, new Caption(MENU_FILE, defaultLanguage, "File"));
		captions.put(MENU_LOAD, new Caption(MENU_LOAD, defaultLanguage, "Load setting"));
		captions.put(MENU_SAVE, new Caption(MENU_SAVE, defaultLanguage, "Save setting"));
		captions.put(MENU_EXIT, new Caption(MENU_EXIT, defaultLanguage, "Exit"));
		captions.put(MENU_CONVERT_BIN, new Caption(MENU_CONVERT_BIN, defaultLanguage, "Convert bin to..."));
		captions.put(MENU_CONVERT_CLIS, new Caption(MENU_CONVERT_CLIS, defaultLanguage, "Convert clis file to..."));
		captions.put( MENU_WRITE_TEST, new Caption( MENU_WRITE_TEST,  defaultLanguage, "Writing test" ) );

		captions.put(LOAD_TEXT, new Caption(LOAD_TEXT, defaultLanguage, "Load"));

		// captions.put( SETTING_TASK, new Caption( SETTING_TASK, defaultLanguage,
		// "Task" ) );

		captions.put(SETTING_SOCKET_TAB_INPUT_PANEL,
				new Caption(SETTING_SOCKET_TAB_INPUT_PANEL, defaultLanguage, "Socket settings"));
		/*
		captions.put(SETTING_SOCKET_TAB_INPUT_ACTIVE,
				new Caption(SETTING_SOCKET_TAB_INPUT_ACTIVE, defaultLanguage, "Active"));
		*/
		captions.put(SETTING_SOCKET_TAB_INPUT_IP_TABLE_ADDRESS,
				new Caption(SETTING_SOCKET_TAB_INPUT_IP_TABLE_ADDRESS, defaultLanguage, "IP address"));
		captions.put(SETTING_SOCKET_TAB_INPUT_IP_TABLE_PORT,
				new Caption(SETTING_SOCKET_TAB_INPUT_IP_TABLE_PORT, defaultLanguage, "Port"));
		
		/*
		captions.put(SETTING_SOCKET_TAB_INPUT_COMMAND_ENABLE,
				new Caption(SETTING_SOCKET_TAB_INPUT_COMMAND_ENABLE, defaultLanguage, "Enable"));
		captions.put(SETTING_SOCKET_TAB_INPUT_COMMAND_EVENT,
				new Caption(SETTING_SOCKET_TAB_INPUT_COMMAND_EVENT, defaultLanguage, "Event"));
		captions.put(SETTING_SOCKET_TAB_INPUT_COMMAND_MESSAGE,
				new Caption(SETTING_SOCKET_TAB_INPUT_COMMAND_MESSAGE, defaultLanguage, "Message"));
		captions.put(SETTING_SOCKET_TAB_OUTPUT_PANEL,
				new Caption(SETTING_SOCKET_TAB_OUTPUT_PANEL, defaultLanguage, "Output messages"));
		captions.put(SETTING_SOCKET_TAB_OUTPUT_ACTIVE,
				new Caption(SETTING_SOCKET_TAB_OUTPUT_ACTIVE, defaultLanguage, "Active"));
		captions.put(SETTING_SOCKET_TAB_OUTPUT_IP_TABLE_ADDRESS,
				new Caption(SETTING_SOCKET_TAB_OUTPUT_IP_TABLE_ADDRESS, defaultLanguage, "IP Address"));
		captions.put(SETTING_SOCKET_TAB_OUTPUT_IP_TABLE_PORT,
				new Caption(SETTING_SOCKET_TAB_OUTPUT_IP_TABLE_PORT, defaultLanguage, "Port"));
		captions.put(SETTING_SOCKET_TAB_OUTPUT_COMMAND_ENABLE,
				new Caption(SETTING_SOCKET_TAB_OUTPUT_COMMAND_ENABLE, defaultLanguage, "Enable"));
		captions.put(SETTING_SOCKET_TAB_OUTPUT_COMMAND_EVENT,
				new Caption(SETTING_SOCKET_TAB_OUTPUT_COMMAND_EVENT, defaultLanguage, "Event"));
		captions.put(SETTING_SOCKET_TAB_OUTPUT_COMMAND_MESSAGE,
				new Caption(SETTING_SOCKET_TAB_OUTPUT_COMMAND_MESSAGE, defaultLanguage, "Message"));
		captions.put(SETTING_SOCKET_TAB_OUTPUT_NEW_SOCKET,
				new Caption(SETTING_SOCKET_TAB_OUTPUT_NEW_SOCKET, defaultLanguage, "New"));
		captions.put(SETTING_SOCKET_TAB_OUTPUT_DELETE_SOCKET,
				new Caption(SETTING_SOCKET_TAB_OUTPUT_DELETE_SOCKET, defaultLanguage, "Delete"));

		captions.put(SETTING_LSL_TAB, new Caption(SETTING_LSL_TAB, defaultLanguage, "Lab Streaming Layer"));
		captions.put(SETTING_LSL_EVENT, new Caption(SETTING_LSL_EVENT, defaultLanguage, "Event"));
		*/
		captions.put(SETTING_LSL_MARK, new Caption(SETTING_LSL_MARK, defaultLanguage, "Mark"));
		captions.put(SETTING_LSL_FILE, new Caption(SETTING_LSL_FILE, defaultLanguage, "File name"));
		captions.put(SETTING_COMPRESSOR, new Caption(SETTING_COMPRESSOR, defaultLanguage, "Compressor"));
		captions.put(SETTING_LSL_OUTPUT_FORMAT,
				new Caption(SETTING_LSL_OUTPUT_FORMAT, defaultLanguage, "Output file format"));
		captions.put(SETTING_LSL_REFRESH, new Caption(SETTING_LSL_REFRESH, defaultLanguage, "Refresh streams"));
		captions.put( MSG_LSL_PLOT_ERROR, new Caption( MSG_LSL_PLOT_ERROR, defaultLanguage, "LSL stream is not available." )  );
		
		captions.put(TOO_MUCH_TIME, new Caption(TOO_MUCH_TIME, defaultLanguage, "The operation is taking too long."));
		captions.put(FORCE_QUIT, new Caption(FORCE_QUIT, defaultLanguage, "Force exit"));
		captions.put(WAIT, new Caption(WAIT, defaultLanguage, "Wait"));
		captions.put(WAITING, new Caption(WAITING, defaultLanguage, "Waiting"));
		captions.put(LOG, new Caption(LOG, defaultLanguage, "Log"));
		captions.put(APP_STATE_LOG, new Caption(APP_STATE_LOG, defaultLanguage, "App-state log"));

		captions.put(CLEAR, new Caption(CLEAR, defaultLanguage, "Clear"));
		captions.put(INPUT_MSGS, new Caption(INPUT_MSGS, defaultLanguage, "Input messages"));
		captions.put(OUTPUT_MSGS, new Caption(OUTPUT_MSGS, defaultLanguage, "Output messages"));

		captions.put(COPY, new Caption(COPY, defaultLanguage, "Copy"));
		captions.put(COPY_ALL, new Caption(COPY_ALL, defaultLanguage, "Copy all"));
		captions.put(CUT, new Caption(CUT, defaultLanguage, "Cut"));

		captions.put(INPUT_TEXT, new Caption(INPUT_TEXT, defaultLanguage, "Input"));
		captions.put(OUTPUT_TEXT, new Caption(OUTPUT_TEXT, defaultLanguage, "Output"));
		captions.put(MSG_TEXT, new Caption(MSG_TEXT, defaultLanguage, "Message"));
		captions.put(MSGS_TEXT, new Caption(MSGS_TEXT, defaultLanguage, "Messages"));
		captions.put(DESCRIPTION_TEXT, new Caption(DESCRIPTION_TEXT, defaultLanguage, "Description"));
		captions.put( SORT_TEXT, new Caption( SORT_TEXT, defaultLanguage, "Sort" ) );
		captions.put( ENCRYPT_KEY_TEXT, new Caption( ENCRYPT_KEY_TEXT, defaultLanguage, "Encrypt" ) );
		captions.put( DECRYPT_KEY_TEXT, new Caption( DECRYPT_KEY_TEXT, defaultLanguage, "Decrypt" ) );
		captions.put( PARALLELIZE_TEXT, new Caption( PARALLELIZE_TEXT, defaultLanguage, "Parallelize" ) );

		captions.put(AUTOSCROLL, new Caption(AUTOSCROLL, defaultLanguage, "Autoscroll"));

		captions.put(SETTING_LSL_PLOT, new Caption(SETTING_LSL_PLOT, defaultLanguage, "Plot"));
		captions.put(SETTING_LSL_DEVICES, new Caption(SETTING_LSL_DEVICES, defaultLanguage, "Streams"));

		captions.put(SETTING_LSL_EXTRA, new Caption(SETTING_LSL_EXTRA, defaultLanguage, "Extra"));
		captions.put(SETTING_LSL_STREAM_PLOT, new Caption(SETTING_LSL_STREAM_PLOT, defaultLanguage, "Plot"));
		captions.put(SETTING_LSL_CHUNCK, new Caption(SETTING_LSL_CHUNCK, defaultLanguage, "Chunck size"));
		captions.put(SETTING_LSL_INTERLEAVED, new Caption(SETTING_LSL_INTERLEAVED, defaultLanguage, "Interleaved"));
		captions.put(SETTING_LSL_SYNC, new Caption(SETTING_LSL_SYNC, defaultLanguage, "Sync"));
		captions.put(SETTING_LSL_NAME, new Caption(SETTING_LSL_NAME, defaultLanguage, "Stream's name"));

		captions.put(SETTING_LSL_EXTRA_TOOLTIP,
				new Caption(SETTING_LSL_EXTRA_TOOLTIP, defaultLanguage, "Additional information"));
		captions.put(SETTING_LSL_STREAM_PLOT_TOOLTIP,
				new Caption(SETTING_LSL_STREAM_PLOT_TOOLTIP, defaultLanguage, "Plot"));
		captions.put(SETTING_LSL_CHUNCK_TOOLTIP,
				new Caption(SETTING_LSL_CHUNCK_TOOLTIP, defaultLanguage, "Chunck size"));
		captions.put(SETTING_LSL_INTERLEAVED_TOOLTIP,
				new Caption(SETTING_LSL_INTERLEAVED_TOOLTIP, defaultLanguage, "Interleaved LSL data"));
		captions.put(SETTING_LSL_SYNC_TOOLTIP,
				new Caption(SETTING_LSL_SYNC_TOOLTIP, defaultLanguage, "Get synchronization mark from this stream"));
		captions.put(SETTING_LSL_NAME_TOOLTIP, new Caption(SETTING_LSL_NAME_TOOLTIP, defaultLanguage, "Stream's name"));

		captions.put(LSL_PLOT_FILTERS, new Caption(LSL_PLOT_FILTERS, defaultLanguage, "Display range"));
		captions.put(LSL_BIN_DATA_FILES, new Caption(LSL_BIN_DATA_FILES, defaultLanguage, "Binary data files"));
		captions.put(LSL_BIN_TIME_FILES, new Caption(LSL_BIN_TIME_FILES, defaultLanguage, "Binary time files"));
		captions.put(LSL_DATA_TYPE, new Caption(LSL_DATA_TYPE, defaultLanguage, "Data type"));
		captions.put(LSL_CHANNELS, new Caption(LSL_CHANNELS, defaultLanguage, "Channels"));
		captions.put(LSL_CHANNEL, new Caption(LSL_CHANNEL, defaultLanguage, "Channel"));
		captions.put(LSL_XML_DESCRIPTION, new Caption(LSL_XML_DESCRIPTION, defaultLanguage, "XML description"));
		captions.put(LSL_DEL_BINS, new Caption(LSL_DEL_BINS, defaultLanguage, "Delete binaries"));

		captions.put(LSL_PLOT_FILTER_LEGEND,
				new Caption(LSL_PLOT_FILTER_LEGEND, defaultLanguage,
						"Display-range (DR) format: N1:(A1,B1)[C1,D1];N2:(A2,B2)[C2,D2];N3:(A3,B3)[C3,D3];...\n" + "where\n"
								+ ">> Nx is plot number where DR is applied. Plot numbering starts from 1."
								+ " The value 0 indicates that DR is applied to all plots.\n"
								+ ">> () indicates that only the values in the range will be passed, whereas"
								+ "  [] indicates that the display is limited to the range. At least one must be present, and the order must be respected.\n"
								+ ">> Ax, Bx, Cx and Dx are interval limits. Java number format is supported.\n"
								+ "   Espacial value (case sensity):\n"
								+ "    >> -Inf or Inf: -/+ infinity.\n"
								+ "    >> NaN (not a number) is ignored.\n"
								+ "\nIncorrect DR format are ignored.\n"
								+ "\nExamples: 1:(1,5);2:[-2,10];3:(1,5)[-7,8]"));
		captions.put(APPLY_TEXT, new Caption(APPLY_TEXT, defaultLanguage, "Apply"));
		captions.put(INSERT_TEXT, new Caption(INSERT_TEXT, defaultLanguage, "Insert"));
		captions.put(DELETE_TEXT, new Caption(DELETE_TEXT, defaultLanguage, "Delete"));
		captions.put(PROBLEM_TEXT, new Caption(PROBLEM_TEXT, defaultLanguage, "Problem"));
		captions.put(OK_TEXT, new Caption(OK_TEXT, defaultLanguage, "Ok"));
		captions.put(CANCEL_TEXT, new Caption(CANCEL_TEXT, defaultLanguage, "Cancel"));
		captions.put(SELECT_TEXT, new Caption(SELECT_TEXT, defaultLanguage, "Select"));
		captions.put(SELECTED_TEXT, new Caption(SELECTED_TEXT, defaultLanguage, "Selected"));
		captions.put(SELECTED_PROCESSING_TEXT, new Caption(SELECTED_PROCESSING_TEXT, defaultLanguage, "Selected processing"));
		captions.put(SELECTED_POST_PROCESSING_TEXT, new Caption(SELECTED_POST_PROCESSING_TEXT, defaultLanguage, "Selected post-processing"));
		captions.put(UP_TEXT, new Caption(UP_TEXT, defaultLanguage, "Up"));
		captions.put(DOWN_TEXT, new Caption(DOWN_TEXT, defaultLanguage, "Down"));
		captions.put(CONTINUE_TEXT, new Caption(CONTINUE_TEXT, defaultLanguage, "Continue"));
		captions.put(FILE_NOT_FOUND, new Caption(FILE_NOT_FOUND, defaultLanguage, "File(s) not found"));
		captions.put(LANGUAGE_TEXT, new Caption(LANGUAGE_TEXT, defaultLanguage, "Language"));
		captions.put( REPEAT_TEXT, new Caption( REPEAT_TEXT, defaultLanguage, "Repeat" ) );
		captions.put( OPTIONS_TEXT, new Caption( OPTIONS_TEXT, defaultLanguage, "Options" ) );
		captions.put( PASSWORD_TEXT, new Caption( PASSWORD_TEXT, defaultLanguage, "Password" ) );
		captions.put( PROCESS_TEXT,  new Caption( PROCESS_TEXT, defaultLanguage, "Process" ) );
		captions.put( POST_PROCESS_TEXT,  new Caption( POST_PROCESS_TEXT, defaultLanguage, "Post process" ) );

		captions.put(CHECK_DEVICES_CHANGE_WARNING_MSG, new Caption(CHECK_DEVICES_CHANGE_WARNING_MSG,
				defaultLanguage, "LSL devices changed. It is recommended to refresh before continuing."));
		captions.put(CHECK_SELECTED_STREAM_ERROR_MSG,
				new Caption(CHECK_SELECTED_STREAM_ERROR_MSG, defaultLanguage, "Selected streams changed."));
		captions.put(CHECK_SPECIAL_IN_WARNING_MSG,
				new Caption(CHECK_SPECIAL_IN_WARNING_MSG, defaultLanguage, "Special inputs are not selected."));
		captions.put(CHECK_SYNC_UNSELECTABLE_ERROR_MSG, new Caption(CHECK_SYNC_UNSELECTABLE_ERROR_MSG,
				defaultLanguage, "LSL sync stream not selected."));
		captions.put(CHECK_SYNC_METHOD_WARNING_MSG,
				new Caption(CHECK_SYNC_METHOD_WARNING_MSG, defaultLanguage, "Sync method is not selected."));
		captions.put(CHECK_SYNC_STREAM_WARNING_MSG,
				new Caption(CHECK_SYNC_STREAM_WARNING_MSG, defaultLanguage, "A LSL stream is set as sync, but its data will not be save because Data Stream is not sync method."));
		captions.put( CHECK_LSL_CHUNCKSIZE_WARNING_MSG
				, new Caption( CHECK_LSL_CHUNCKSIZE_WARNING_MSG, defaultLanguage, "Check if the next fields are correct: chunk size and interleaved." ) );
		captions.put(CHECK_SYNC_NO_SELECT_STREAM_WARNING_MSG,
				new Caption(CHECK_SYNC_NO_SELECT_STREAM_WARNING_MSG, defaultLanguage, "The Data Stream (sync method) is activated, but noone input data stream is not set as sync."));

		captions.put(INPUT_START_LEGEND,
				new Caption(INPUT_START_LEGEND, defaultLanguage, "system must start the recording."));
		captions.put(INPUT_STOP_LEGEND,
				new Caption(INPUT_STOP_LEGEND, defaultLanguage, "system must stop the recording."));
		
		captions.put(SETTING_PLUGIN,
				new Caption(SETTING_PLUGIN, defaultLanguage, "Plugin"));
		
		captions.put(OTHERS_TEXT,
				new Caption(OTHERS_TEXT, defaultLanguage, "Others"));
		
		captions.put(TRIALS_TEXT,
				new Caption(TRIALS_TEXT, defaultLanguage, "Trials"));
		
		captions.put(ENCODER_TEXT,
				new Caption(ENCODER_TEXT, defaultLanguage, "Encoder"));
		
		captions.put( MSG_DATA_PROCESSING_STREAMS_CHANGED
						, new Caption( MSG_DATA_PROCESSING_STREAMS_CHANGED, defaultLanguage, "The streams selected to be processed have changed. Check that everything is correct." ) );

		captions.put(SAVE_DATA_PROCESSING_TEXT,
				new Caption(SAVE_DATA_PROCESSING_TEXT, defaultLanguage, "Save output data processing."));
	}

	public static void loadLanguages() {
		try {
			File folder = new File(DefaultFolder);
			if (folder.exists() && folder.isDirectory()) {
				FileFilter filter = new FileFilter() {
					@Override
					public boolean accept(File pathname) {
						boolean ok = pathname.exists() && pathname.isFile()
								&& pathname.getAbsolutePath().endsWith(FILE_EXTENSION);

						return ok;
					}
				};

				File[] langFiles = folder.listFiles(filter);

				for (File lang : langFiles) {
					loadLanguageFile(lang);
				}
			}
		} catch (Exception ex) {
		}
	}

	private static void loadLanguageFile(File f) throws Exception {
		Properties prop = new Properties();
		FileInputStream propFileIn = null;

		try {
			propFileIn = new FileInputStream(f);

			prop.load(new InputStreamReader(propFileIn, Charset.forName("UTF-8")));
			
			Object idLang = prop.get(Language.LANGUAGE);

			if ( idLang != null ) 
			{
				prop.remove( Language.LANGUAGE );

				Caption cap = captions.get( Language.LANGUAGE );

				cap.setCaption( idLang.toString(), idLang.toString() );

				for ( Object key : prop.keySet() ) 
				{
					Object val = prop.get(key);
					if ( val != null && !val.toString().trim().isEmpty() ) 
					{
						Caption caption = captions.get( key );
						
						if ( caption != null )
						{
							if( key.toString().equalsIgnoreCase( MENU_ABOUT ) 
									&& !val.toString().toLowerCase().contains( ConfigApp.shortNameApp ) )
							{
								val = val.toString().trim() + " " + ConfigApp.shortNameApp;
							}
							
							caption.setCaption(idLang.toString(), val.toString());
						}
					}
				}
			}
		}
		catch (Exception ex) 
		{
			ex.printStackTrace();
		}
	}

	public static String getCaption(String captionID, String lang) {
		String txt = "";

		Caption cap = captions.get(captionID);

		if (cap != null) 
		{
			txt = cap.getCaption(lang.toLowerCase());

			if (txt == null) {
				txt = cap.getCaption(defaultLanguage);
			}
		}

		return txt;
	}

	public static void setDefaultLocalLanguage() {
		Locale lc = Locale.getDefault();
		changeLanguage(lc.toString());
	}

	public static String getLocalCaption(String captionID) {
		String idLng = defaultLanguage;
		if (localLang != null) {
			idLng = localLang.toString();
		}

		return getCaption(captionID, idLng);
	}

	public static List<String> getAvaibleLanguages() {
		List<String> LANGS = new ArrayList<String>();

		Caption lang = captions.get(LANGUAGE);
		LANGS.addAll(lang.getLanguages());
		Collections.sort(LANGS);

		return LANGS;
	}

	public static boolean changeLanguage(String lang) {
		Locale language = null;

		if (getAvaibleLanguages().contains(lang.toLowerCase())) {
			for (Locale lc : Locale.getAvailableLocales()) {
				if (lang.toLowerCase().equals(lc.toString().toLowerCase())) {
					language = lc;
				}
			}

			if (language != null) {
				localLang = language;
			} else if (lang.equals(defaultLanguage)) {
				localLang = null;
			}
		}

		return language != null;
	}

	public static String getCurrentLanguage() {
		String lng = defaultLanguage;

		if (localLang != null) {
			lng = localLang.toString();
		}

		return lng;
	}

	public static Map<String, Caption> getAllCaptions() {
		return captions;
	}
}