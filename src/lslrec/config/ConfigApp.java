/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018-2019 by Manuel Merino Monge <manmermon@dte.us.es>
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

import lslrec.dataStream.family.DataStreamFactory;
import lslrec.dataStream.family.setting.IMutableStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.MutableStreamSetting;
import lslrec.dataStream.family.setting.StreamExtraLabels;
import lslrec.dataStream.family.stream.IDataStream;
import lslrec.dataStream.outputDataFile.compress.CompressorDataFactory;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.dataStream.sync.SyncMethod;
import lslrec.exceptions.DefaultValueException;
import lslrec.gui.miscellany.IPAddressValidator;
import lslrec.plugin.loader.PluginLoader;
import lslrec.plugin.lslrecPlugin.ILSLRecConfigurablePlugin;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin;
import lslrec.plugin.lslrecPlugin.ILSLRecPlugin.PluginType;
import lslrec.plugin.lslrecPlugin.processing.ILSLRecPluginDataProcessing;
import lslrec.plugin.lslrecPlugin.trial.ILSLRecPluginTrial;
import lslrec.plugin.register.DataProcessingPluginRegistrar;
import lslrec.plugin.register.TrialPluginRegistrar;
import lslrec.sockets.SocketMessageDelayCalculator;
import lslrec.sockets.info.SocketSetting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.StringUtils;

import lslrec.auxiliar.WarningMessage;
import lslrec.auxiliar.extra.ArrayTreeMap;
import lslrec.auxiliar.extra.FileUtils;
import lslrec.auxiliar.extra.NumberRange;
import lslrec.auxiliar.extra.Tuple;
import lslrec.config.language.Language;
import lslrec.control.message.RegisterSyncMessages;

public class ConfigApp
{
	// Language
	public static final String LANGUAGE = "LANGUAGE";
	
	public static final String fullNameApp = "LSL Recorder";
	public static final String shortNameApp = "LSLRec";
	public static final Calendar buildDate = new GregorianCalendar( 2025, 2 - 1, 25 );
	//public static final int buildNum = 33;
	
	public static final int WRITING_TEST_TIME = 1000 * 60; // 1 minute
	
	public static final String version = "Version 3" 
											//+ "." + buildNum
											+ "." + ( buildDate.get( Calendar.YEAR ) % 100 )											
											+ "." + ( buildDate.get( Calendar.DAY_OF_YEAR ) ) 
											;
	
	public static final String appDateRange = "2018-" + buildDate.get( Calendar.YEAR );
	public static final String defaultPathFile = System.getProperty("user.dir") + File.separatorChar + "records" + File.separatorChar;

	public static final String defaultNameFileConfigExtension = "cfg";
	
	public static String defaultNameFileConfig = "config." + defaultNameFileConfigExtension;
	
	public static final String defaultNameOutputDataFile = "data"; //"data.clis";
	
	public static final String HEADER_SEPARATOR = ";" ;

	//public static final int DEFAULT_SEGMENTATION_BLOCK_SIZE = (int)( 10 * ( Math.pow( 2, 20 ) ) );
	
	public static final int DEFAULT_NUM_SOCKET_PING = SocketMessageDelayCalculator.DEFAULT_NUM_PINGS;
	
	public static final String SYSTEM_LIB_WIN_PATH = "systemLib/win/";
	public static final String SYSTEM_LIB_LINUX_PATH = "systemLib/linux/";
	public static final String SYSTEM_LIB_MACOS_PATH = "systemLib/macox/";
	//public static final String SYSTEM_LIB_PATH = System.getProperty( "user.dir" ) + "/systemLib/";
	
	
	/**********************
	 * 
	 */
	public static final String CHECKLIST_MSGS = "CHECKLIST_MSGS";
			
	public static final String SELECTED_SYNC_METHOD = "SYNC_METHOD";
	
	//public static final String DATA_CHART_SUMMARY = "DATA_CHART_SUMMARY";
	
	/***********
	 * 
	 * Socket	
	 *  
	 */
	//public static final String IS_SOCKET_SERVER_ACTIVE = "IS_SOCKET_SERVER_ACTIVE";
	
	public static final String IS_ACTIVE_SPECIAL_INPUTS = "IS_ACTIVE_SPECIAL_INPUTS";

	public static final String SERVER_SOCKET = "SERVER_SOCKET_TABLE";
	
	//public static final String STREAM_LIBRARY = "STREAM_LIBRARY";

	/****************
	 * 
	 * Lab Streaming Layer
	 * 
	 */

	public static final String ID_STREAMS = "LSL_ID_DEVICES";
	
	public static final String STREAM_SEARCHING_TIME = "STREAM_SEARCHING_TIME";
	
	/****
	 * 
	 * 
	 * Output format
	 * 
	 */
	
	public static final String OUTPUT_FILE_NAME = "OUTPUT_FILE_NAME";
	public static final String OUTPUT_FILE_FOLDER = "OUTPUT_FILE_FOLDER";
	public static final String OUTPUT_SUBJ_ID = "OUTPUT_SUBJ_ID";
	public static final String OUTPUT_TEST_ID = "OUTPUT_TEST_ID";
	
	public static final String OUTPUT_FILE_FORMAT = "OUTPUT_FILE_FORMAT";
	
	public static final String OUTPUT_COMPRESSOR = "OUTPUT_COMPRESSOR";
	
	public static final String OUTPUT_FILE_DESCR = "OUTPUT_FILE_DESCR";
	
	public static final String OUTPUT_ENCRYPT_DATA = "OUTPUT_ENCRYPT_DATA";
	
	public static final String OUTPUT_PARALLELIZE = "OUTPUT_PARALLELIZE";
	
	public static final String OUTPUT_SAVE_DATA_PROCESSING = "OUTPUT_SAVE_DATA_PROCESSING";
	
	public static final String DEL_BINARY_FILES = "DEL_BINARY_FILES";
		
	public static final String RECORDING_CHECKER_TIMER = "RECORDING_CHECKER_TIMER";
	
	public static final String SEGMENT_BLOCK_SIZE = "SEGMENTATION_BLOCK_SIZE";

	/****
	 * 
	 * 
	 * Plugin
	 * 
	 */
	
	public static final String PLUGINS = "PLUGINS";
	
	public static final String TRIAL_FULLSCREEN = "TRIAL_FULLSCREEN";
	public static final String TRIAL_WINDOW_WIDTH = "TRIAL_WINDOW_WIDTH";
	public static final String TRIAL_WINDOW_HEIGHT = "TRIAL_WINDOW_HEIGHT";
	
	////////////////////////
	
	private static boolean test = false;
	
	public static boolean isTesting()
	{
		//return true;
		return test;
	}
	
	public static void setTesting( boolean t )
	{
		test = t;
		loadDefaultChecklist();
	}
	
	
	////////////////////////
	
	private static Map< String, Object > listConfig = new HashMap< String, Object >();
	private static Map< String, Class > list_Key_Type = new HashMap< String, Class >();
	private static Map< String, NumberRange > list_Key_RankValues = new HashMap< String, NumberRange >();

	static
	{
		create_Key_Value();
		create_Key_Type_Set();
		create_Key_RankValues(); 
	}

	private static void create_Key_Value()
	{
		listConfig.clear();

		loadDefaultProperties();
	}

	private static void create_Key_Type_Set()
	{
		list_Key_Type.clear();

		list_Key_Type.put( LANGUAGE, String.class);
 		
		list_Key_Type.put( SERVER_SOCKET, Set.class);

		list_Key_Type.put( SELECTED_SYNC_METHOD, Set.class );
		
		list_Key_Type.put( IS_ACTIVE_SPECIAL_INPUTS, Boolean.class );

		list_Key_Type.put( ID_STREAMS, HashSet.class);

		list_Key_Type.put( OUTPUT_FILE_FORMAT, String.class);
		list_Key_Type.put( OUTPUT_FILE_NAME, String.class);
		list_Key_Type.put( OUTPUT_FILE_FOLDER, String.class);
		list_Key_Type.put( OUTPUT_SUBJ_ID, String.class);
		list_Key_Type.put( OUTPUT_TEST_ID, String.class);
		list_Key_Type.put( OUTPUT_FILE_DESCR, String.class );
		
		list_Key_Type.put( OUTPUT_ENCRYPT_DATA, Boolean.class );
		
		list_Key_Type.put( OUTPUT_COMPRESSOR, String.class );
		list_Key_Type.put( OUTPUT_PARALLELIZE, Boolean.class );
		
		list_Key_Type.put( OUTPUT_SAVE_DATA_PROCESSING, Boolean.class );
		
		list_Key_Type.put( DEL_BINARY_FILES, Boolean.class );
		
		list_Key_Type.put( TRIAL_FULLSCREEN, Boolean.class );
		list_Key_Type.put( TRIAL_WINDOW_WIDTH, Integer.class );
		list_Key_Type.put( TRIAL_WINDOW_HEIGHT, Integer.class );
		
		list_Key_Type.put( STREAM_SEARCHING_TIME, Double.class );
		
		list_Key_Type.put( RECORDING_CHECKER_TIMER, Integer.class );
		
		list_Key_Type.put( SEGMENT_BLOCK_SIZE, Integer.class );
		
		list_Key_Type.put( CHECKLIST_MSGS, List.class );
		
		//list_Key_Type.put( STREAM_LIBRARY, IStreamSetting.StreamLibrary.class );
		
		//list_Key_Type.put( DATA_CHART_SUMMARY, Boolean.class );
	}
	
	private static void create_Key_RankValues()
	{
		list_Key_RankValues.clear();
		
		list_Key_RankValues.put( TRIAL_WINDOW_HEIGHT, new NumberRange( 100,  8e3 ) );
		list_Key_RankValues.put( TRIAL_WINDOW_WIDTH, new NumberRange( 100,  8e3 ) );
		
		list_Key_RankValues.put( STREAM_SEARCHING_TIME, new NumberRange( 0,  IDataStream.TIME_FOREVER  ) );
		
		list_Key_RankValues.put( RECORDING_CHECKER_TIMER, new NumberRange( 0, Integer.MAX_VALUE ) );
		list_Key_RankValues.put( SEGMENT_BLOCK_SIZE, new NumberRange( 1, 100 ) );
	}
	
	public static NumberRange getPropertyRange( String id )
	{
		return list_Key_RankValues.get( id );
	}
	
	public static void saveConfig( File f ) throws Exception
	{		
		defaultNameFileConfig = f.getName();
		
		Properties prop = new Properties();

		Iterator< String > it = listConfig.keySet().iterator();

		while (it.hasNext())
		{
			String key = (String)it.next();

			if ( key.equals( SERVER_SOCKET ) )
			{				
				Set< String > table = (Set< String >)listConfig.get( key );
				
				Map< String, Integer > syncMsgs = RegisterSyncMessages.getSyncMessagesAndMarks();
				
				List< Integer > vals = new ArrayList< Integer>( syncMsgs.values() );
				Collections.sort( vals );
				
				List< String > msgs = new ArrayList< String >();
				for( Integer v : vals )
				{
					boolean foundValue = false;
					Iterator< String > itMsg = syncMsgs.keySet().iterator();
					
					while( itMsg.hasNext() && !foundValue )
					{
						String msg = itMsg.next();
						
						foundValue = syncMsgs.get( msg ) == v;
						
						if( foundValue && !RegisterSyncMessages.isSpecialInputMessage( msg ) )
						{
							msgs.add( msg );
						}
					}
				}
		
				String s = "{";
				for ( String socket : table )
				{
					s = s + "{" + socket + "=" + msgs + "}";
				}
				s = s + "}";

				prop.setProperty(key, s);
			}
			else if( key.equals( ID_STREAMS ) )
			{
				HashSet< IMutableStreamSetting > lsl = (HashSet< IMutableStreamSetting >)listConfig.get( key );
				//HashSet< IMutableStreamSetting > toSave = new HashSet< IMutableStreamSetting >();
				
				String p = "[";
				for( IMutableStreamSetting setting : lsl )
				{
					if( setting.isSelected() || setting.isSynchronationStream() )
					{
						//toSave.add( setting );
						p += setting.getStreamInfoToString() + ";";
					}
				}
				
				p += "]";
				
				//String p = toSave.toString();
				//p = p.replace( ">, <", ">; <" );
				prop.setProperty( key, p );
			}		
			else
			{
				prop.setProperty(key, listConfig.get(key).toString());
			}
		}
		
				
		/*
		 * Plugin setting format:
		 * 
		 * [<type,id,V,{<idParameter,value>,<idParameter,value>,..,<idParameter,value>};<type,id,V,{<idParameter,value>,<idParameter,value>,..,<idParameter,value>};...]
		 * 
		 * where:
		 * 	1. If TRIAL: V = true/false (selected/no selected)
		 *  2. If DATA_PROCESSING: V = stream.name@@@stream.source_id (Stream on which to apply it).
		 *  3. Others: V is empty.
		 */
		String p = "[";
		try
		{
			PluginLoader loader = PluginLoader.getInstance();
			
			for( PluginType pt : PluginType.values() )
			{
				String setPl = "";
				
				if( pt == PluginType.DATA_PROCESSING )
				{	
					/*
					for( ILSLRecPluginDataProcessing process : DataProcessingPluginRegistrar.getDataProcesses() )
					{
						String extra = "";
						
						for( IStreamSetting sst : DataProcessingPluginRegistrar.getDataStreams( process ) )
						{
							extra += sst.name() + "@@@" + sst.source_id() + "," ;
						}
								
						if( !extra.isEmpty() )
						{
							extra = extra.substring( 0, extra.length() - 1 );
						}
						
						extra = "(" + extra + ")";
						
						String setting = "";
						for( Parameter< String > par : process.getSettings() )
						{
							setting += par + ",";
						}
						
						if( !setting.isEmpty() )
						{
							setting = setting.substring( 0, setting.length() - 1 );
						}
						
						extra += ",{" + setting + "}" ;
						
						String plgHeader = getPluginPropertyFormatHeader( pt, process.getID(), extra );
						
						setPl += plgHeader + ";";
					}
					//*/
					
					for( IStreamSetting sst : DataProcessingPluginRegistrar.getAllDataStreams() )
					{
						String idStream = "(" + sst.name() + "@@@" + sst.source_id() + ",";
						
						int[] processLocs = new int[] { DataProcessingPluginRegistrar.PROCESSING, DataProcessingPluginRegistrar.POSTPROCESSING };
						
						for( int processLoc : processLocs )
						{
							int pluginOrder = 0;
							for( ILSLRecPluginDataProcessing process : DataProcessingPluginRegistrar.getDataProcessing( sst, processLoc ) )
							{
								String setting = "";
								
								for( Parameter< String > par : process.getSettings() )
								{
									setting += par + ",";
								}
								
								if( !setting.isEmpty() )
								{
									setting = setting.substring( 0, setting.length() - 1 );
								}
								
								String extra = idStream + processLoc + "," + pluginOrder + ")" + ",{" + setting + "}" ;
								
								String plgHeader = getPluginPropertyFormatHeader( pt, process.getID(), extra );
								
								setPl += plgHeader + ";";
								pluginOrder++;
							}
						}
					}
				}
				else 
				{				
					List< ILSLRecPlugin > plgs = loader.getPluginsByType( pt );
							
					if( plgs != null )
					{
						for( ILSLRecPlugin plg : plgs )
						{
							if( plg instanceof ILSLRecConfigurablePlugin )
							{
								ILSLRecConfigurablePlugin plcfg = (ILSLRecConfigurablePlugin) plg;
								
								String extra = "";
								
								if( pt == PluginType.TRIAL )
								{
									extra += "(" + TrialPluginRegistrar.isSelected( plcfg.getID() ) + ")";
								}
								
								String setting = "";
							
								List< Parameter< String > > pars = plcfg.getSettings();
								if( pars != null )
								{
									for( Parameter< String > par : pars )
									{
										String parValues = par.toString();
										int indexSep = parValues.indexOf( "," );
										if( indexSep > 0 )
										{
											String id = parValues.substring( 0, indexSep );
											String val = parValues.substring( indexSep+1, parValues.length() - 1 ).trim();
										
											parValues = id + ",{" + val +"}>";
										}
										setting += parValues + ",";
									}
								}
								
								if( !setting.isEmpty() )
								{
									setting = setting.substring( 0, setting.length() - 1 );
								}
								
								extra += ",{" + setting + "}" ;
								
								String plgHeader = getPluginPropertyFormatHeader( pt, plcfg.getID(), extra );
								
								setPl += plgHeader + ";";
								
							}
						}
					}
				}
				
				if( !setPl.isEmpty() )
				{
					setPl = setPl.substring( 0, setPl.length() - 1 );
					p += setPl + ";" ;
				}
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		
		p = p.substring( 0, p.length() - 1 ) + "]";
		
		prop.setProperty( ConfigApp.PLUGINS, p );
		//
		//
		//
		

		FileOutputStream fOut = new FileOutputStream(f, false);

		prop.store(fOut, "");
		fOut.close();
	}

	private static String getPluginPropertyFormatHeader( PluginType type, String id, String extra )
	{
		return "<" + type.name() + "," + id + "," + extra + ">";
	}
	
	public static WarningMessage loadConfig( File f ) throws Exception
	{
		defaultNameFileConfig = f.getName();
		
		Properties prop = new Properties();
		FileInputStream propFileIn = null;
		
		WarningMessage msg = new WarningMessage();

		try
		{
			propFileIn = new FileInputStream( f );

			prop.load( propFileIn );
						
			String msgCh = checkProperties( prop );			
			
			if( !msgCh.isEmpty() )
			{
				msg.setMessage( "Setting error in " + f + "\n" + msgCh, WarningMessage.WARNING_MESSAGE );				
				//throw new DefaultValueException( );
			}
		}
		catch (DefaultValueException e)
		{			
			//throw new Exception( e.getMessage() );
			msg.setMessage( e.getMessage(), WarningMessage.ERROR_MESSAGE );
		}
		catch (Exception e)
		{
			loadDefaultProperties();
			
			msg.setMessage( e.getMessage() + ": Default Parameters load.", WarningMessage.ERROR_MESSAGE );			
		}
		
		return msg;
	}

	public static boolean setProperty(String propertyID, Object value)
	{
		boolean ok = true;

		if ( value.getClass().equals( listConfig.get( propertyID ).getClass() ) )
		{
			ok = !list_Key_RankValues.containsKey( propertyID );
			if( !ok )
			{
				NumberRange nr = list_Key_RankValues.get( propertyID );
				ok = nr.within( (Number)value );
			}
			
			if( ok )
			{
				listConfig.put(propertyID, value);
			}
		}
		else
		{
			ok = false;
		}

		return ok;
	}

	public static Object getProperty(String propertyID)
	{
		Object ob = listConfig.get(propertyID);
		return ob;
	}

	private static String checkProperties(Properties prop) throws DefaultValueException
	{
		loadDefaultProperties();

		String checkPropErrorMsg = "";
		String defaultMsg = "Error in config file. Some parameters loaded to its default value:\n";
		
		boolean defaultValue = false;

		List< Object > idPars = new ArrayList<Object>( prop.keySet() );
		Collections.sort( idPars, new Comparator<Object>() 
		{
			@Override
			public int compare(Object o1, Object o2) 
			{
				return o1.toString().compareTo( o2.toString() );
			}
		});
		
		//Iterator<Object> it = prop.keySet().iterator();
		Iterator<Object> it = idPars.iterator();

		while( it.hasNext() )
		{
			Object id = it.next();
			if( id.toString().equals( ID_STREAMS ) ) 
			{
				it.remove();
				idPars.add( id );
				
				break;
			}
		}

		it = idPars.iterator();
		while( it.hasNext() )
		{
			Object id = it.next();
			if( id.toString().equals( PLUGINS ) ) 
			{
				it.remove();
				idPars.add( id );
				
				break;
			}
		}
		
		it = idPars.iterator();
		while( it.hasNext() )
		{
			String key = (String)it.next();
			Class clase = list_Key_Type.get(key);

			String value = prop.getProperty(key);

			if (clase != null)
			{
				if (clase.isInstance(new Boolean(false)))
				{
					if ((value == null) || ((!value.toLowerCase().equals( "true" )) && 
							(!value.toLowerCase().equals( "false" ))))
					{
						defaultValue = true;
						
						defaultMsg += key + "; ";
					}

					listConfig.put(key, new Boolean(value));
				}
				else if ( Number.class.isAssignableFrom( clase ) )//( (clase.isInstance(new Integer(0))) || (clase.isInstance(new Long(0L))))
				{
					try
					{
						double v = new Double(value);

						NumberRange rank = list_Key_RankValues.get(key);

						if (rank != null)
						{
							if (!rank.within( v ) )
							{
								loadDefaultValue(key);
								defaultValue = true;

								defaultMsg += key + "; ";
							}
							else if (clase.isInstance(new Integer(0)))
							{
								listConfig.put(key, new Integer( (int)v ) );
							}
							else if( clase.isInstance( new Long(0)))
							{
								listConfig.put(key, new Long( (long)v ) );
							}
							else
							{
								listConfig.put( key, v );
							}
						}
						else
						{
							loadDefaultValue(key);
							defaultValue = true;
							
							defaultMsg += key + "; ";
						}
					}
					catch (NumberFormatException e)
					{
						loadDefaultValue(key);
						defaultValue = true;
						
						defaultMsg += key + "; ";
					}
				}
				else if( key.equals( SERVER_SOCKET ) )
				{
					loadDefaultValue( key );					
					RegisterSyncMessages.clearSyncMessages();
					
					boolean allOK = true;

					if (value == null)
					{
						allOK = false;
					}
					else
					{
						String[] strings = value.replace( " ", "" ).replace( "}}", "}").split( "}" );

						Set< String > sockets = new TreeSet< String >();
						
						for (int j = 0; j < strings.length; j++)
						{
							String[] textTable = strings[ j ].replace( "{", "" ).split( "=" );

							if ( textTable.length != 2 )
							{
								allOK = false;
							}
							else
							{
								String socketInfo = checkSocket( textTable[ 0 ].split( ":" ) );
								if (socketInfo == null)
								{
									allOK = false;
								}
								else
								{
									sockets.add( socketInfo );
									
									String[] table = textTable[1].replace( "[", "" ).replace( "]", "" ).split( "," );

									for( String msg : table )
									{
										if( !msg.isEmpty() )
										{
											RegisterSyncMessages.addSyncMessage( msg );
										}
									}									
								}
							}
						}

						if ( !sockets.isEmpty() )
						{
							listConfig.put( key, sockets );
						}
					}

					if ( !allOK )
					{
						defaultValue = true;
						
						defaultMsg += key + "; ";
					}
				}
				else if( clase.getCanonicalName().equals( List.class.getCanonicalName() ) )
				{
					List<String> values = new ArrayList<String>();
					
					value = value.replaceAll( "\\[", "");
					value = value.replaceAll( "\\]", "");
					value = value.replaceAll( "\\<", "");
					value = value.replaceAll( "\\>", "");
					
					String[] parts = value.split( "," );
					
					switch (key) 
					{
						case CHECKLIST_MSGS:
						{
							boolean ok = ( parts.length % 2 == 0 );
							if( ok )
							{
								List< Tuple< Boolean, String > > checkList = (List< Tuple< Boolean, String > >)ConfigApp.getProperty( ConfigApp.CHECKLIST_MSGS);
								
								for( int i = 1; i < parts.length; i += 2 )
								{
									try
									{
										String msg = parts[ i ].trim();
										if( i < 2 && !checkList.isEmpty() )
										{
											msg = checkList.get( 0 ).t2;
											checkList.remove( 0 );
										}
										Boolean sel = Boolean.parseBoolean( parts[ i-1 ].trim() );

										Tuple< Boolean, String > tmsg = new Tuple<Boolean, String>( sel, msg );
										checkList.add( tmsg );										
									}
									catch( Exception e )
									{
										ok = false;
										break;
									}
								}
							}
							
							if( !ok )
							{
								defaultValue = true;
								
								defaultMsg += key + "; ";
								
								loadDefaultChecklist();
							}
							
							break;
						}
						default:
						{
							for (int i = 0; i < parts.length; i++)
							{
								String path = parts[i];
								if (!path.isEmpty())
								{
									values.add(path.trim());
								}
							}			
							
							listConfig.put(key, values);
							
							break;
						}
					}
				}
				else if (clase.isInstance(new String()))
				{
					if (value == null)
					{
						loadDefaultValue( key );
						defaultValue = true;
						
						defaultMsg += key + "; ";
					}
					else
					{
						switch ( key ) 
						{
							case OUTPUT_SUBJ_ID:
							case OUTPUT_TEST_ID:
							case OUTPUT_FILE_NAME:
							case OUTPUT_FILE_FOLDER:
							{	
								listConfig.put( key, value );
								
								//if( !FileUtils.checkOutputOutputFilePath() )
								if( FileUtils.getOutputCompletedFileNameFromConfig() == null )
								{
									loadDefaultValue( key );
									
									defaultValue = true;
									
									defaultMsg += key + "; ";
								}
								
								break;
							}
							default:
							{
								listConfig.put( key, value );
								break;
							}
						}
						
					}
				}
				else if ( clase.getCanonicalName().equals(Tuple.class.getCanonicalName() ) )
				{
					if (value.equals(new Tuple(null, null).toString()))
					{
						loadDefaultValue( key );
					}
					else
					{
						value = value.replace( "<", "").replace( ">", "").replace( " ", "");
						String[] values = value.split( ",");

						if (values.length != 2)
						{
							defaultValue = true;
							loadDefaultValue(key);
							
							defaultMsg += key + "; ";
						}
						else
						{
							listConfig.put(key, new Tuple< String, String >(values[0], values[1]));
						}            
					}
				}
				else if( clase.getCanonicalName().equals( HashSet.class.getCanonicalName() ) )
				{
					if( key.equals( ID_STREAMS ) )
					{
						loadDefaultLSLDeviceInfo();
						
						HashSet< IMutableStreamSetting > lslDevs = (HashSet< IMutableStreamSetting >)ConfigApp.getProperty( ConfigApp.ID_STREAMS );
						
						value = value.replace("[",  "").replace("]", "");
						if( !value.isEmpty() )
						{
							String[] devices = value.split( ">;");
							
							String msgDevNonFound = "";
													
							int countDevOff = 0;
							boolean allOk = true;
							
							
							Map< String, ArrayTreeMap< String, Tuple<String, IMutableStreamSetting>> > foundLslStreams =  new HashMap<String, ArrayTreeMap< String, Tuple<String, IMutableStreamSetting>>>();
							for( IMutableStreamSetting lslstr : lslDevs )
							{
								String name = lslstr.name();
								String type = lslstr.content_type();
								String source = lslstr.source_id();
								
								ArrayTreeMap< String, Tuple<String, IMutableStreamSetting>> ts = new ArrayTreeMap< String, Tuple<String, IMutableStreamSetting>>();
								if( foundLslStreams.containsKey( name ) )
								{
									ts = foundLslStreams.get( name );
								}
								else
								{	
									foundLslStreams.put( name, ts );
								}
								
								ts.putElement( type, new Tuple<String, IMutableStreamSetting>( source, lslstr) );
							}
							
							for( String dev : devices )
							{
								//dev = dev.replace( " ", "").replace( "<", "" ).replace( ">", "" );
								dev = dev.replace( "<", "" ).replace( ">", "" );
								String[] devInfo = dev.split( "," );
								
								if( devInfo.length == 9 )
								{
									String sourceID = devInfo[ 0 ].replace( " ", "" );
									String name = devInfo[ 1 ].replace( " ", "" );
									String type = devInfo[ 2 ].replace( " ", "" );
									String info = devInfo[ 3 ].trim();
									
									boolean select = false;									
									try 
									{
										select = new Boolean( devInfo[ 4 ].replace( " ", "" ) );
									}
									catch (Exception e) 
									{
									}
									
									
									int chunckSize = 1; 
									try
									{
										chunckSize = new Integer( devInfo[ 5 ].replace( " ", "" ) );
									}
									catch( Exception e)
									{}
									
									
									boolean interleaved = false;
									try 
									{
										interleaved = new Boolean( devInfo[ 6 ].replace( " ", "" )  );
									}
									catch (Exception e) 
									{
									}
									
									
									boolean isSync = false;									
									try
									{
										isSync = new Boolean( devInfo[ 7 ].replace( " ", "" ) );
									}
									catch (Exception e) 
									{
									}
									
									boolean enableRecordingCheckerTimer = false;									
									try
									{
										enableRecordingCheckerTimer = new Boolean( devInfo[ 8 ].replace( " ", "" ) );
									}
									catch (Exception e) 
									{
									}
									
									/*
									boolean found = false;
									
									for( IMutableStreamSetting lslCfg : lslDevs )
									{										
										found = lslCfg.source_id().equals( sourceID )
												&& lslCfg.content_type().equals( type ) 
												&& lslCfg.name().equals( name );
																			
										if( found )
										{
											lslCfg.setAdditionalInfo( StreamExtraLabels.ID_EXTRA_INFO_LABEL, info );
											lslCfg.setSelected( select );
											lslCfg.setChunckSize( chunckSize );
											lslCfg.setInterleaveadData( interleaved );
											lslCfg.setSynchronizationStream( isSync );
											lslCfg.enableRecordingCheckerTimer( enableRecordingCheckerTimer );
											
											break;
										}
									}
									
									if( !found )
									{			
										msgDevNonFound += "<" + sourceID + ", " + name + ", " + type + ">; ";
										countDevOff++;
										
										if( countDevOff > 4 )
										{
											msgDevNonFound += "\n";
											countDevOff = 0;
										}
										
										defaultValue = true;
										allOk = false;
									}
									//*/
									
									IMutableStreamSetting lslCfg = null;
									
								 	ArrayTreeMap< String, Tuple<String, IMutableStreamSetting>> typeSource = foundLslStreams.get( name );
									if( typeSource != null )
									{										
										List< Tuple<String, IMutableStreamSetting>> sources = typeSource.get( type );
										if( sources != null )
										{
											if( sources.size() == 1 )
											{
												lslCfg = sources.get( 0 ).t2;
											}
											else
											{
												for( Tuple<String, IMutableStreamSetting> source : sources )
												{
													if( source.t1.equals( sourceID ) )
													{
														lslCfg = source.t2;
														
														break;
													}
												}
											}
										}
									}
									
									if( lslCfg != null )
									{
										lslCfg.setAdditionalInfo( StreamExtraLabels.ID_EXTRA_INFO_LABEL, info );
										lslCfg.setSelected( select );
										lslCfg.setChunckSize( chunckSize );
										lslCfg.setInterleaveadData( interleaved );
										lslCfg.setSynchronizationStream( isSync );
										lslCfg.enableRecordingCheckerTimer( enableRecordingCheckerTimer );
									}
									else
									{			
										msgDevNonFound += "<" + sourceID + ", " + name + ", " + type + ">; ";
										countDevOff++;
										
										if( countDevOff > 4 )
										{
											msgDevNonFound += "\n";
											countDevOff = 0;
										}
										
										defaultValue = true;
										allOk = false;
									}
								}
								else
								{
									defaultValue = true;
									allOk = false;
								}
							}
							
							if( !msgDevNonFound.isEmpty() )
							{
								defaultMsg += "LSL devices non found:\n" + msgDevNonFound;
							}
							
							if( !allOk )
							{
								defaultMsg += key + "; ";
							}
						}
					}
				}
				else if( key.equals( SELECTED_SYNC_METHOD ) )
				{				
					loadDefaultSyncMethod();
					
					/*
					if( !value.equals( SyncMethod.SYNC_NONE ) 
							&& !value.equals( SyncMethod.SYNC_SOCKET ) 
							&& !value.equals( SyncMethod.SYNC_STREAM) )
					//*/
					
					
					value = value.replace("[",  "").replace("]", "");
					String[] values = value.split( "," );
					
					Set< String > meths = (Set< String >)listConfig.get( ConfigApp.SELECTED_SYNC_METHOD );
					meths.clear();
					
					for( String v : values )
					{
						if( SyncMethod.isSyncMethod( v.trim() ) )
						{
							meths.add( v.trim() );
						}
						else
						{
							defaultValue = true;
							defaultMsg += v.trim() + ": unknow sync method.\n";
						}
					}
					
					if( meths.isEmpty() )
					{
						loadDefaultSyncMethod();
					}
					
					/*
					if( !SyncMethod.isSyncMethod( value ) )
					{
						defaultValue = true;
						defaultMsg +=  key + "; ";
					}
					else
					{
						listConfig.put( SELECTED_SYNC_METHOD, value );
					}
					*/
				}			
			}			
			else if( key.equals( PLUGINS ) )
			{	
				try
				{
					/*
					 * Format data:
					 * 
					 * [<type,id,V,{<idParameter,value>,<idParameter,value>,..,<idParameter,value>};<type,id,V,{<idParameter,value>,<idParameter,value>,..,<idParameter,value>};...]
					 * 
					 * where:
					 * 	1. If TRIAL: V = (true/false) (selected/no selected)
					 *  2. If DATA_PROCESSING: V = stream.name@@@stream.source_id (Stream on which to apply it).
					 *  3. Others: V is empty.
					 */

					value = value.replaceAll( ">\\s+;\\s+<", ">;<" );

					String[] Plugins = value.split( ">;<" );
					if( Plugins.length > 0 )
					{
						Plugins[ 0 ] = Plugins[ 0 ].replace( "[", "" ).trim();
						Plugins[ Plugins.length - 1 ] = Plugins[ Plugins.length - 1 ].replace( "]", "" ).trim();

					}

					/*
					 * Plugins[] =
					 * 				<type,id,V,{<idParameter,value>,<idParameter,value>,..,<idParameter,value>}>
					 * 				<type,id,V,{<idParameter,value>,<idParameter,value>,..,<idParameter,value>}>
					 * 				<type,id,V,{<idParameter,value>,<idParameter,value>,..,<idParameter,value>}>
					 * 				<type,id,V,{<idParameter,value>,<idParameter,value>,..,<idParameter,value>}>
					 */

					PluginLoader loader = PluginLoader.getInstance();

					boolean parErrorFormat = false;
					
					ArrayTreeMap< Tuple< PluginType, String >, Tuple< String, ParameterList > > reg = new ArrayTreeMap< Tuple< PluginType, String >, Tuple< String, ParameterList > >(); 
					for( String plugin : Plugins )
					{

						// plugin = <type,id,V,{<idParameter,value>,<idParameter,value>,..,<idParameter,value>}>

						String[] pl = new String[] { "", "", "", "" };
						
						for( int indexCut = 0, ind = 0; indexCut >= 0 && ind < pl.length-1; )
						{
							if( ind < 2 )
							{
								indexCut = plugin.indexOf( "," );
								
								if( indexCut >= 0 )
								{
									pl[ ind ] = plugin.substring( 0, indexCut );
								
									
									plugin = plugin.substring( indexCut + 1 );
								}
							}
							else
							{
								pl[ ind ] = StringUtils.substringBetween( plugin, "(", ")" );
								plugin = plugin.replaceAll( "\\((.*?)\\)", "" ).replaceFirst( ",", "" );	
								
								if( pl[ ind ] == null )
								{
									pl[ ind ] = "";
								}
							}
							
							ind++;
							
							if( ind == pl.length - 1 )
							{
								pl[ ind ] = plugin;
							}
						}

						/*
						 * pl[] =
						 * 		<type
						 * 		,id
						 * 		,V
						 * 		,{<idParameter,value>,<idParameter,value>,..,<idParameter,value>}>
						 */

						String pType = pl[ 0 ].trim().replace("<", ""); // type
						String idPlugin = pl[ 1 ].trim(); // id
						String extra = pl[ 2 ].trim(); // V
						String pluginSetting = pl[ 3 ].trim(); //{<idParameter,value>,<idParameter,value>,..,<idParameter,value>}>

						PluginType plgType = null;
						try
						{
							plgType = PluginType.valueOf( pType.toUpperCase() );
						}
						catch( Exception e )
						{						
							defaultValue = true;
							defaultMsg += key + ": plugin type unknow (" + pType + ").\n";
						}
						
						if( plgType != null )
						{
							pluginSetting = pluginSetting.replaceAll( "\\{(.*?)\\}",  "$1");
							pluginSetting = pluginSetting.replaceFirst( "<", "" );
							pluginSetting = pluginSetting.replaceAll( ">\\s+,\\s+<", ">,<" );
							String[] settings = pluginSetting.split( ">,<" );
															
															
							if( settings.length > 0 )
							{
								settings[ settings.length - 1 ] = settings[ settings.length - 1 ].replaceAll( ">", "" ).trim();
							}
							
							//
							//settings[]= 
							//			idParameter,value
							//			idParameter,value
							//			...
							//			idParameter,value
							
							ParameterList pars = new ParameterList();

							for( String cfg : settings )
							{
								// cfg = idParameter,value

								if( !cfg.isEmpty() )
								{
									String[] par = new String[0];
									
									int sepIndex = cfg.indexOf( "," );
									
									if( sepIndex > 0 )
									{
										// 
										// par[] = 
										//			idParameter
										//			value
										par = new String[ 2 ];
										
										par[ 0 ] = cfg.substring( 0, sepIndex );
										par[ 1 ] = cfg.substring( sepIndex + 1 );
									}
										
									if( par.length == 2 )
									{
										String parID = par[ 0 ].trim(); // idParameter
										String val = par[ 1 ].trim().replaceFirst(",\\{", ""); // value
										
										if( val.charAt( 0 ) == '{' )
										{
											val = val.substring( 1 );
										}
										
										if( val.charAt( val.length() - 1 ) == '}' )
										{
											val = val.substring( 0, val.length() - 1 );
										}
	
										pars.addParameter( new Parameter<String>( parID, val ) );
									}
									else 
									{
										parErrorFormat = true;
									}
								}								
							}

							reg.putElement( new Tuple< PluginType, String >( plgType, idPlugin ), new Tuple<String, ParameterList>( extra, pars ) );
						}
					}
					
					ArrayTreeMap< Integer, Tuple< IStreamSetting, Tuple< Integer, ILSLRecPluginDataProcessing > > > dataprocessingOrder = new ArrayTreeMap<Integer, Tuple<IStreamSetting, Tuple< Integer, ILSLRecPluginDataProcessing >>>();
					
					for( Tuple< PluginType, String > plType : reg.keySet() )
					{
						PluginType pt = plType.t1;
						String idPlg = plType.t2;

						for( Tuple< String, ParameterList > plg : reg.get( plType ) )
						{
							String extra = plg.t1;
							ParameterList pars = plg.t2;

							List< Parameter< String > > parlist = new ArrayList< Parameter< String > >();

							for( String idPar : pars.getParameterIDs() )
							{
								parlist.add( pars.getParameter( idPar ) );
							}

							if( pt == PluginType.DATA_PROCESSING )
							{
								ILSLRecPluginDataProcessing newPr = (ILSLRecPluginDataProcessing)loader.createNewPluginInstance( pt, idPlg, false );									
								newPr.loadSettings( parlist );

								//DataProcessingPluginRegistrar.addDataProcessing( newPr );

								HashSet< IStreamSetting > dss = (HashSet< IStreamSetting >)ConfigApp.getProperty( ConfigApp.ID_STREAMS );

								//
								// extra = (streamName@@@sourceID,processLoc,order)
								//
								String[] stream = extra.replaceAll( "\\((.*)\\)", "$1" ).split( "," );

								//
								// streams =
								//			streamName@@@sourceID
								//			processLoc
								//			order
								
								if( stream.length == 3 )
								{
									try
									{
										int processLoc = Integer.parseInt( stream[ 1 ] );
										int order = Integer.parseInt( stream[ 2 ] );
										
										String[] stInfo = stream[0].split( "@@@" );
										//
										// stInfo =
										//			streamName
										//			sourceID

										String name = "";
										String sId = "";
										
										if( ( 2 - stInfo.length ) >= 0 )
										{
											name = stInfo[ 0 ].trim();
											
											if( stInfo.length == 2 )
											{
												sId = stInfo[ 1 ].trim();
											}

											for( IStreamSetting s : dss )
											{
												if( s.name().equalsIgnoreCase( name ) && s.source_id().equalsIgnoreCase( sId ) )
												{
													//DataProcessingPluginRegistrar.addDataStreamProcessing( newPr, s );

													dataprocessingOrder.putElement( order, new Tuple< IStreamSetting, Tuple< Integer, ILSLRecPluginDataProcessing > >( s, new Tuple< Integer, ILSLRecPluginDataProcessing>( processLoc, newPr ) ) );
													
													break;
												}
											}													
										}
										else
										{
											parErrorFormat = true;
										}
									}
									catch (Exception e) 
									{
										parErrorFormat = true;
									}
								}
								else
								{
									parErrorFormat = true;
								}
							}
							else if( pt == PluginType.TRIAL )
							{
								boolean sel = false;

								try
								{
									sel = Boolean.parseBoolean( extra );
								}
								catch (Exception e) 
								{
									parErrorFormat = true;
								}

								List< ILSLRecPlugin> pluginList = loader.getAllPlugins( pt, idPlg );
								
								if( pluginList != null && !pluginList.isEmpty() )
								{
									if( sel )
									{
										TrialPluginRegistrar.setTrialPlugin( (ILSLRecPluginTrial)pluginList.get( 0 ) );
									}
									
									for( ILSLRecPlugin rp : pluginList )
									{
										if( rp instanceof ILSLRecConfigurablePlugin )
										{
											((ILSLRecConfigurablePlugin) rp).loadSettings( parlist );
										}
									}
								}
							}
							else
							{										
								List< ILSLRecPlugin > pluginList = loader.getAllPlugins( pt, idPlg );

								if( pluginList != null )
								{
									for( ILSLRecPlugin rp : pluginList )
									{
										if( rp instanceof ILSLRecConfigurablePlugin )
										{
											((ILSLRecConfigurablePlugin) rp).loadSettings( parlist );
										}
									}
								}
							}
						}
					}	
					
					if( !dataprocessingOrder.isEmpty() )
					{
						List< Integer > order = new ArrayList<Integer>( dataprocessingOrder.keySet() );
						Collections.sort( order );
						
						for( Integer n : order )
						{
							List< Tuple< IStreamSetting, Tuple< Integer, ILSLRecPluginDataProcessing > > > plgs = dataprocessingOrder.get( n );
							
							for( Tuple< IStreamSetting, Tuple< Integer, ILSLRecPluginDataProcessing > > p : plgs )
							{
								IStreamSetting sst = p.t1;
								ILSLRecPluginDataProcessing proc = p.t2.t2;
								int procLoc = p.t2.t1;
								
								DataProcessingPluginRegistrar.addDataStreamProcessing( proc, sst, procLoc );
							}
						}
					}
					
					if( parErrorFormat )
					{
						defaultValue = true;
						defaultMsg += key + ": plugins' settings error.\n";	
					}
				}
				catch (Exception e) 
				{
					defaultValue = true;
					defaultMsg += key + ": plugins' settings error.\n"; 
				}				
			}
			/*
			else if( key.equalsIgnoreCase( STREAM_LIBRARY ) )
			{
				loadDefaultStreamLibrary();
				
				try
				{
					listConfig.put( STREAM_LIBRARY, IStreamSetting.StreamLibrary.valueOf( value ) );
				}
				catch( Exception e)
				{
					defaultValue = true;
					defaultMsg += key + ": the library " + value + " unsupported.\n";
				}
			}
			*/
			else 
			{
				System.out.println(key + ": non-defined parameter.");
			}
		}
		
		if( !prop.keySet().containsAll( list_Key_Type.keySet() ) )
		{
			defaultValue = true;
						
			for( Object k : list_Key_Type.keySet() )
			{
				if( !prop.keySet().contains( k ) )
				{
					defaultMsg += k.toString() + ";" ;
				}
			}
		}
		

		if (defaultValue)
		{
			checkPropErrorMsg = defaultMsg;			
		}
		
		return checkPropErrorMsg;
	}
	
	private static String checkSocket(String[] strs)
	{
		String socketInfo = null;
		if (strs.length == 3)
		{
			Object[] v = new Object[3];

			List<Integer> index = new ArrayList<Integer>();
			index.add( 0 );
			index.add( 1 );
			index.add( 2 );

			byte c = 7;
			for (Integer i : index)
			{
				String ipProp = strs[i.intValue()].replace( " " , "");

				if ((c & 0x4) > 0) /* TCP-UDP */	
				{
					if (ipProp.equals( "UDP" ) )
					{
						v[0] = SocketSetting.UDP_PROTOCOL;
						c = (byte)(c & 0x3);
					}
					else if (ipProp.equals( "TCP" ) )
					{
						v[0] = SocketSetting.TCP_PROTOCOL;
						c = (byte)(c & 0x3);
					}
				}

				if ((c & 0x2) > 0) /* IP */
				{
					if (IPAddressValidator.validate(ipProp))
					{
						v[1] = ipProp;

						c = (byte)(c & 0x5);
					}
				}

				if ((c & 0x1) > 0)
				{
					try
					{
						v[2] = new Integer(ipProp);

						c = (byte)(c & 0x6);
					}
					catch (Exception localException) {}
				}
			}


			if (c == 0)
			{
				socketInfo = SocketSetting.getSocketString( (Integer)v[0], v[1].toString(), (Integer)v[2] );
			}
		}

		return socketInfo;
	}

	private static void loadDefaultValue( String prop )
	{
		switch ( prop ) 
		{
			case LANGUAGE:
			{
				loadDefaultLanguage();
				break;
			}
			case SERVER_SOCKET:
			{
				loadDefaultValueServerSocketTable();
				break;
			}
			case ID_STREAMS:
			{
				loadDefaultLSLDeviceInfo();
				break;
			}
			case OUTPUT_FILE_FORMAT:
			{
				loadDefaultLSLOutputFileFormat();
				break;
			}
			case OUTPUT_COMPRESSOR:
			{
				loadDefaultOutputCompressor();
				break;
			}
			case SELECTED_SYNC_METHOD:
			{
				loadDefaultSyncMethod();
				break;
			}
			case IS_ACTIVE_SPECIAL_INPUTS:
			{
				loadDefaultValueIsServerSocketActiveInputSpecialMsg();
				break;
			}
			case OUTPUT_FILE_DESCR:
			{
				loadDefaultLSLOutputFileDescr();
				break;
			}
			case OUTPUT_FILE_NAME:
			{
				loadDefaultLSLOutputFileName();
				break;
			}
			case OUTPUT_FILE_FOLDER:
			{
				loadDefaultLSLOutputFileFolder();
				break;
			}
			case OUTPUT_SUBJ_ID:
			{
				loadDefaultLSLOutputSubjectID();
				break;
			}
			case OUTPUT_TEST_ID:
			{
				loadDefaultLSLOutputTestID();;
				break;
			}
			case OUTPUT_ENCRYPT_DATA:
			{
				loadDefaultLSLEncryptData();
				break;
			}
			case OUTPUT_PARALLELIZE:
			{
				loadDefaultLSLOutputParallelize();
				break;
			}
			case OUTPUT_SAVE_DATA_PROCESSING:
			{
				loadDefaultSaveOutputDataProcessing();
				break;
			}
			case PLUGINS:
			{
				loadDefaultPlugins();
				break;
			}
			case DEL_BINARY_FILES:
			{
				loadDefaultDeleteBinaryFiles();
				break;
			}
			case TRIAL_FULLSCREEN:
			{
				loadDefaultTrialFullScreen();
				break;
			}
			case TRIAL_WINDOW_WIDTH:
			{
				loadDefaultTrialWindowWidth();
				break;
			}
			case TRIAL_WINDOW_HEIGHT:
			{
				loadDefaultTrialWindowHeigh();
				break;
			}			
			case STREAM_SEARCHING_TIME:
			{
				loadDefaultStreamSearchingTime();
				
				break;
			}
			case RECORDING_CHECKER_TIMER:
			{
				loadDefaultRecorderTimer();
				
				break;
			}
			case SEGMENT_BLOCK_SIZE:
			{
				loadDefaultOutputSegmentBlockSize();
				
				break;
			}
			case CHECKLIST_MSGS:
			{
				loadDefaultChecklist();
				
				break;
			}
			/*
			case STREAM_LIBRARY:
			{
				loadDefaultStreamLibrary();
				
				break;
			}
			*/
			/*
			case DATA_CHART_SUMMARY:
			{
				loadDefaultDataChartSummary();
				
				break;
			}
			*/
		}
	}

	private static void loadDefaultProperties()
	{
		loadDefaultLanguage();

		loadDefaultValueServerSocketTable();

		//loadDefaultValueIsServerSocketActive();
		loadDefaultSyncMethod();
		loadDefaultValueIsServerSocketActiveInputSpecialMsg();
		
		loadDefaultLSLDeviceInfo();
		loadDefaultLSLOutputFileName();
		loadDefaultLSLOutputFileFolder();
		loadDefaultLSLOutputSubjectID();
		loadDefaultLSLOutputTestID();
		
		loadDefaultLSLOutputFileFormat();
		loadDefaultLSLOutputFileDescr();		
		
		loadDefaultLSLEncryptData();
		loadDefaultOutputCompressor();
		loadDefaultLSLOutputParallelize();
		
		loadDefaultSaveOutputDataProcessing();
		
		loadDefaultPlugins();
		loadDefaultDeleteBinaryFiles();
		
		loadDefaultTrialFullScreen();		
		loadDefaultTrialWindowWidth();
		loadDefaultTrialWindowHeigh();
		
		loadDefaultStreamSearchingTime();
		
		loadDefaultRecorderTimer();
		loadDefaultOutputSegmentBlockSize();
		
		loadDefaultChecklist();
		
		//loadDefaultStreamLibrary();
		
		//loadDefaultDataChartSummary();
	}

	private static void loadDefaultLanguage()
	{
		listConfig.put( LANGUAGE, Language.defaultLanguage );
	}
	
	private static void loadDefaultValueServerSocketTable()
	{
		Set< String > sockets = new TreeSet< String >();

		sockets.add( defaultSocket() );

		listConfig.put( SERVER_SOCKET, sockets);
	}

	private static void loadDefaultSyncMethod()
	{
		Set< String > syncMet = new TreeSet< String >();
		
		syncMet.add( SyncMethod.SYNC_NONE );
		
		listConfig.put( SELECTED_SYNC_METHOD, syncMet );
	}
	
	private static void loadDefaultValueIsServerSocketActiveInputSpecialMsg()
	{
		listConfig.put( IS_ACTIVE_SPECIAL_INPUTS, false );
	}
	
	private static String defaultSocket()
	{
		int port = 55555;

		ServerSocket localmachine = null;		
		try 
		{
			localmachine = new ServerSocket( 0 );
			localmachine.setReuseAddress( true );
			port = localmachine.getLocalPort();
			localmachine.close();
		} 
		catch (IOException e) 
		{}
		finally
		{
			if( localmachine != null )
			{
				try 
				{
					localmachine.close();
				}
				catch (IOException e) 
				{
					e.printStackTrace();
				}
			}

			localmachine = null;
		}

		return SocketSetting.getSocketString( SocketSetting.UDP_PROTOCOL,
				Inet4Address.getLoopbackAddress().getHostAddress(),
				port );
	}

	private static void loadDefaultLSLDeviceInfo()
	{
		/*
		LSL lsl = new LSL();

		IStreamSetting[] deviceInfo = lsl.resolve_streams( );
		*/
		
		//IStreamSetting[] deviceInfo = DataStreamFactory.getStreamSettings( StreamLibrary.LSL );		
		IStreamSetting[] deviceInfo = DataStreamFactory.getStreamSettings( );
		
		HashSet< IMutableStreamSetting > settings = new HashSet< IMutableStreamSetting >();
		
		for( IStreamSetting info : deviceInfo )
		{
			MutableStreamSetting cfg = new MutableStreamSetting( info );
			settings.add( cfg );
		}
		
		listConfig.put( ID_STREAMS, settings );
	}

	private static void loadDefaultLSLOutputFileName()
	{
		listConfig.put( OUTPUT_FILE_NAME, defaultNameOutputDataFile );
	}
	
	private static void loadDefaultLSLOutputFileFolder()
	{
		listConfig.put( OUTPUT_FILE_FOLDER, defaultPathFile );
	}
	
	private static void loadDefaultLSLOutputSubjectID()
	{
		listConfig.put( OUTPUT_SUBJ_ID, "" );
	}
	
	private static void loadDefaultLSLOutputTestID()
	{
		listConfig.put( OUTPUT_TEST_ID, "" );
	}

	private static void loadDefaultLSLOutputFileFormat()
	{
		listConfig.put( OUTPUT_FILE_FORMAT, DataFileFormat.CLIS );
		//listConfig.put( LSL_OUTPUT_FILE_FORMAT, DataFileFormat.CLIS );
	}
	
	private static void loadDefaultOutputCompressor()
	{
		listConfig.put( OUTPUT_COMPRESSOR, CompressorDataFactory.GZIP );
	}
	
	private static void loadDefaultLSLOutputFileDescr()
	{
		listConfig.put( OUTPUT_FILE_DESCR, "" );
	}
	
	private static void loadDefaultLSLEncryptData()
	{
		listConfig.put( OUTPUT_ENCRYPT_DATA,  false );
	}
	
	private static void loadDefaultLSLOutputParallelize()
	{
		listConfig.put( OUTPUT_PARALLELIZE, true );
	}
	
	private static void loadDefaultSaveOutputDataProcessing()
	{
		listConfig.put( OUTPUT_SAVE_DATA_PROCESSING, false );
	}
	
	private static void loadDefaultPlugins()
	{
		listConfig.put( PLUGINS, new ArrayTreeMap< Tuple< PluginType, String>, ParameterList >() );
	}
	
	private static void loadDefaultDeleteBinaryFiles()
	{				
		listConfig.put( DEL_BINARY_FILES, true );
	}
	
	private static void loadDefaultTrialFullScreen()
	{				
		listConfig.put( TRIAL_FULLSCREEN, false );
	}
		
	private static void loadDefaultTrialWindowWidth()
	{				
		listConfig.put( TRIAL_WINDOW_WIDTH, 500 );
	}	

	private static void loadDefaultTrialWindowHeigh()
	{				
		listConfig.put( TRIAL_WINDOW_HEIGHT, 500 );
	}
	
	private static void loadDefaultStreamSearchingTime()
	{				
		listConfig.put( STREAM_SEARCHING_TIME, 0D );
	}
	
	private static void loadDefaultRecorderTimer()
	{
		listConfig.put( RECORDING_CHECKER_TIMER, 3 );
	}
	
	private static void loadDefaultOutputSegmentBlockSize()
	{
		listConfig.put( SEGMENT_BLOCK_SIZE, 10 );
	}
	
	private static void loadDefaultChecklist()
	{
		List< Tuple< Boolean, String > > chlist = new ArrayList< Tuple< Boolean, String> >();
		
		chlist.add( new Tuple<Boolean, String>( !ConfigApp.isTesting(), ConfigApp.fullNameApp ) );
		
		listConfig.put( CHECKLIST_MSGS, chlist );
	}
	
	/*
	private static void loadDefaultStreamLibrary()
	{
		listConfig.put( STREAM_LIBRARY, IStreamSetting.StreamLibrary.LSL );
	}
	*/
	
	/*
	private static void loadDefaultDataChartSummary()
	{
		listConfig.put( DATA_CHART_SUMMARY, false );
	}
	//*/
}
