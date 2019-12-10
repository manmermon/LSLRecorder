/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018-2019 by Manuel Merino Monge <manmermon@dte.us.es>
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


package Config;

import Auxiliar.Extra.NumberRange;
import Auxiliar.Extra.Tuple;
import Config.Language.Language;
import Controls.Messages.RegisterSyncMessages;
import DataStream.OutputDataFile.Format.DataFileFormat;
import Excepciones.DefaultValueException;
import GUI.Miscellany.IPAddressValidator;
import Sockets.Info.SocketSetting;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLConfigParameters;
import edu.ucsd.sccn.LSL.StreamInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

public class ConfigApp
{
	// Language
	public static final String LANGUAGE = "LANGUAGE";
	
	public static final String SYNC_NONE = "None";
	public static final String SYNC_SOCKET = "Socket";
	public static final String SYNC_LSL = "LabStreaming Layer";

	public static final String fullNameApp = "LSL Recorder";
	public static final String shortNameApp = "LSLRec";
	public static final Calendar buildDate = new GregorianCalendar( 2019, 12 - 1, 10 );
	//public static final int buildNum = 33;
	
	public static final int WRITING_TEST_TIME = 1000 * 60; // 1 minute
	
	public static final String version = "Version 2" 
											//+ "." + buildNum
											+ "." + ( buildDate.get( Calendar.YEAR ) % 100 )											
											+ "." + ( buildDate.get( Calendar.DAY_OF_YEAR ) ) 
											;
	
	public static final String appDateRange = "2018-" + buildDate.get( Calendar.YEAR );
	public static final String defaultPathFile = System.getProperty("user.dir") + File.separatorChar;

	public static final String defaultNameFileConfigExtension = "cfg";
	
	public static String defaultNameFileConfig = "config." + defaultNameFileConfigExtension;
	
	public static final String defaultNameOutputDataFile = "data.clis";
	
	public static final String HEADER_SEPARATOR = ";" ;


	/**********************
	 * 
	 */
	
	public static final String SELECTED_SYNC_METHOD = "SYNC_METHOD";
	
	/***********
	 * 
	 * Socket	
	 *  
	 */
	//public static final String IS_SOCKET_SERVER_ACTIVE = "IS_SOCKET_SERVER_ACTIVE";
	
	public static final String IS_ACTIVE_SPECIAL_INPUTS = "IS_ACTIVE_SPECIAL_INPUTS";

	public static final String SERVER_SOCKET = "SERVER_SOCKET_TABLE";

	/****************
	 * 
	 * Lab Streaming Layer
	 * 
	 */

	public static final String LSL_ID_DEVICES = "LSL_ID_DEVICES";

	public static final String LSL_OUTPUT_FILE_NAME = "LSL_OUT_FILE_NAME";
	
	public static final String LSL_OUTPUT_FILE_FORMAT = "LSL_OUT_FILE_FORMAT";
	
	public static final String LSL_OUTPUT_FILE_DESCR = "LSL_OUTPUT_FILE_DESCR";
	
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

		//list_Key_Type.put( IS_SOCKET_SERVER_ACTIVE, Boolean.class);
		list_Key_Type.put( SELECTED_SYNC_METHOD, String.class );
		
		list_Key_Type.put( IS_ACTIVE_SPECIAL_INPUTS, Boolean.class );

		list_Key_Type.put( LSL_ID_DEVICES, HashSet.class);

		list_Key_Type.put( LSL_OUTPUT_FILE_FORMAT, String.class);
		list_Key_Type.put( LSL_OUTPUT_FILE_NAME, String.class);
		list_Key_Type.put( LSL_OUTPUT_FILE_DESCR, String.class );
	}
	
	private static void create_Key_RankValues()
	{
		list_Key_RankValues.clear();
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
			else if( key.equals( LSL_ID_DEVICES ) )
			{
				HashSet< LSLConfigParameters > lsl = (HashSet< LSLConfigParameters >)listConfig.get( key );
				HashSet< LSLConfigParameters > toSave = new HashSet< LSLConfigParameters >();
				
				for( LSLConfigParameters setting : lsl )
				{
					if( setting.isSelected() || setting.isSynchronationStream() )
					{
						toSave.add( setting );
					}
				}
				
				String p = toSave.toString();
				p = p.replace( ">, <", ">; <" );
				prop.setProperty( key, p );
			}
			else
			{
				prop.setProperty(key, listConfig.get(key).toString());
			}
		}

		FileOutputStream fOut = new FileOutputStream(f, false);

		prop.store(fOut, "");
		fOut.close();
	}

	public static boolean loadConfig( File f ) throws Exception
	{
		defaultNameFileConfig = f.getName();
		
		Properties prop = new Properties();
		FileInputStream propFileIn = null;
		
		boolean res = true;

		try
		{
			propFileIn = new FileInputStream( f );

			prop.load( propFileIn );
						
			String msg = checkProperties( prop );			
			
			if( !msg.isEmpty() )
			{
				res = false;
				
				throw new DefaultValueException( "Setting error in " + f + "\n" + msg );
			}
		}
		catch (DefaultValueException e)
		{
			res = false;
			
			throw new Exception(e.getMessage());
		}
		catch (Exception e)
		{
			loadDefaultProperties();
			
			res = false;
			
			throw new Exception(e.getMessage() + ": Default Parameters load.");
		}
		
		return res;
	}

	public static boolean setProperty(String propertyID, Object value)
	{
		boolean ok = true;

		if ( value.getClass().equals( listConfig.get( propertyID ).getClass() ) )
		{
			listConfig.put(propertyID, value);
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

		Iterator<Object> it = prop.keySet().iterator();

		while (it.hasNext())
		{
			String key = (String)it.next();
			Class clase = list_Key_Type.get(key);

			String p = prop.getProperty(key);

			if (clase != null)
			{
				if (clase.isInstance(new Boolean(false)))
				{
					if ((p == null) || ((!p.toLowerCase().equals( "true" )) && 
							(!p.toLowerCase().equals( "false" ))))
					{
						defaultValue = true;
						
						defaultMsg += key + "; ";
					}

					listConfig.put(key, new Boolean(p));
				}
				else if ( (clase.isInstance(new Integer(0))) || (clase.isInstance(new Long(0L))))
				{
					try
					{
						double v = new Double(p);

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
							else
							{
								listConfig.put(key, new Long( (long)v ) );
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

					if (p == null)
					{
						allOK = false;
					}
					else
					{
						String[] strings = p.replace( " ", "" ).replace( "}}", "}").split( "}" );

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

					p = p.replaceAll( "\\[", "");
					p = p.replaceAll( "\\]", "");
					p = p.replaceAll( "\\<", "");
					p = p.replaceAll( "\\>", "");

					String[] paths = p.split( "," );

					for (int i = 0; i < paths.length; i++)
					{
						String path = paths[i];
						if (!path.isEmpty())
						{
							values.add(path.trim());
						}
					}

					listConfig.put(key, values);
				}
				else if (clase.isInstance(new String()))
				{
					if (p == null)
					{
						loadDefaultValue(key);
						defaultValue = true;
						
						defaultMsg += key + "; ";
					}
					else
					{
						listConfig.put(key, p);

					}
				}
				else if ( clase.getCanonicalName().equals(Tuple.class.getCanonicalName() ) )
				{
					if (p.equals(new Tuple(null, null).toString()))
					{
						loadDefaultValue( key );
					}
					else
					{
						p = p.replace( "<", "").replace( ">", "").replace( " ", "");
						String[] values = p.split( ",");

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
					if( key.equals( LSL_ID_DEVICES ) )
					{
						loadDefaultLSLDeviceInfo();
						
						HashSet< LSLConfigParameters > lslDevs = (HashSet< LSLConfigParameters >)ConfigApp.getProperty( ConfigApp.LSL_ID_DEVICES );
						
						p = p.replace("[",  "").replace("]", "");
						if( !p.isEmpty() )
						{
							String[] devices = p.split( ">;");
							
							String msgDevNonFound = "";
													
							int countDevOff = 0;
							boolean allOk = true;
							
							for( String dev : devices )
							{
								dev = dev.replace( " ", "").replace( "<", "" ).replace( ">", "" );
								String[] devInfo = dev.split( "," );
								if( devInfo.length == 8 )
								{
									String sourceID = devInfo[ 0 ];
									String name = devInfo[ 1 ];
									String type = devInfo[ 2 ];
									String info = devInfo[ 3 ];
																		
									boolean select = false;
									
									try 
									{
										select = new Boolean( devInfo[ 4 ] );
									}
									catch (Exception e) 
									{
									}
									
									boolean interleaved = false;
									
									try 
									{
										interleaved = new Boolean( devInfo[ 6 ] );
									}
									catch (Exception e) 
									{
									}
									
									boolean isSync = false;
									
									try
									{
										isSync = new Boolean( devInfo[ 7 ] );
									}
									catch (Exception e) 
									{
									}
									
									int chunckSize = 1; 
									try
									{
										chunckSize = new Integer( devInfo[ 5 ] );
									}
									catch( Exception e)
									{}
									
									boolean found = false;
									
									for( LSLConfigParameters lslCfg : lslDevs )
									{
										found = lslCfg.getSourceID().equals( sourceID );
										
										if( !found )
										{	
											found = lslCfg.getDeviceType().equals( type ) && lslCfg.getDeviceName().equals( name );
										}
										
										if( found )
										{
											lslCfg.setAdditionalInfo( info );
											lslCfg.setSelected( select );
											lslCfg.setChunckSize( chunckSize );
											lslCfg.setInterleaveadData( interleaved );
											lslCfg.setSynchronizationStream( isSync );
											
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
			}
			else if( key.equals( SELECTED_SYNC_METHOD ) )
			{				
				loadDefaultSyncMethod();
				
				if( !p.equals( SYNC_NONE ) && !p.equals( SYNC_SOCKET ) && !p.equals( SYNC_LSL) )
				{
					defaultValue = true;
					defaultMsg +=  key + "; ";
				}
				else
				{
					listConfig.put( SELECTED_SYNC_METHOD, p );
				}
			}
			else 
			{
				System.out.println(key + ": non-defined parameter.");
			}
		}
		
		if( !prop.keySet().containsAll(list_Key_Type.keySet() ) )
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

	private static void loadDefaultValue(String prop)
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
			case LSL_ID_DEVICES:
			{
				loadDefaultLSLDeviceInfo();
				break;
			}
			case LSL_OUTPUT_FILE_FORMAT:
			{
				loadDefaultLSLOutputFileFormat();
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
			case LSL_OUTPUT_FILE_DESCR:
			{
				loadDefaultLSLOutputFileDescr();
				break;
			}
			case LSL_OUTPUT_FILE_NAME:
			{
				loadDefaultLSLOutputFileName();
				break;
			}
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

		loadDefaultLSLOutputFileFormat();
		loadDefaultLSLOutputFileDescr();		
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
		listConfig.put( SELECTED_SYNC_METHOD, SYNC_NONE );
	}
	
	/*
	private static void loadDefaultValueIsServerSocketActive()
	{
		listConfig.put( IS_SOCKET_SERVER_ACTIVE, false );
	}
	*/

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
		StreamInfo[] devices = null;

		LSL lsl = new LSL();

		LSL.StreamInfo[] deviceInfo = lsl.resolve_streams( );
		
		HashSet< LSLConfigParameters > settings = new HashSet< LSLConfigParameters >();
		
		for( StreamInfo info : deviceInfo )
		{
			LSLConfigParameters cfg = new LSLConfigParameters( info.uid()
																, info.name()
																, info.type()
																, info.source_id() );
			cfg.setSamplingRate( info.nominal_srate() );
			settings.add( cfg );
		}
		
		listConfig.put( LSL_ID_DEVICES, settings );
	}

	private static void loadDefaultLSLOutputFileName()
	{
		listConfig.put( LSL_OUTPUT_FILE_NAME, defaultPathFile + defaultNameOutputDataFile );
	}

	private static void loadDefaultLSLOutputFileFormat()
	{
		listConfig.put( LSL_OUTPUT_FILE_FORMAT, DataFileFormat.PCLIS );
		//listConfig.put( LSL_OUTPUT_FILE_FORMAT, DataFileFormat.CLIS );
	}
	
	private static void loadDefaultLSLOutputFileDescr()
	{
		listConfig.put( LSL_OUTPUT_FILE_DESCR, "" );
	}
}
