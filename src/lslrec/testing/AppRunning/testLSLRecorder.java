package testing.AppRunning;
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

import Controls.Messages.RegisterSyncMessages;
import Controls.core.CoreControl;
import DataStream.OutputDataFile.Format.DataFileFormat;
import Exceptions.Handler.ExceptionDialog;
import Exceptions.Handler.ExceptionDictionary;
import Exceptions.Handler.ExceptionMessage;
import GUI.appUI;
import GUI.guiManager;
import GUI.Miscellany.ArrayTreeMap;
import GUI.Miscellany.GeneralAppIcon;
import GUI.Miscellany.OpeningDialog;
import GUI.Miscellany.TextAreaPrintStream;
import Sockets.Info.SocketSetting;
import StoppableThread.IStoppableThread;
import config.ConfigApp;
import controls.OutputDataFileHandler;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLConfigParameters;
import testing.AppRunning.LSLStream.LSLStream;
import testing.AppRunning.SyncStream.testSendSocketMsgToLslRec;
import testing.AppRunning.SyncStream.testSyncLSL;
import testing.LSLSender.LSLSimulationParameters;
import testing.Socket.testTCPSocket;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Point;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.UIManager;

import Auxiliar.extra.Tuple;
import Config.language.Language;

public class testLSLRecorder
{
	private static int testLSLSync = 0;
	private static int testSyncSocket = 1;
		
	private static ArrayTreeMap< Integer, Tuple< Integer[], LSLSimulationParameters > > testSettings = new ArrayTreeMap< Integer, Tuple< Integer[], LSLSimulationParameters > >();
	
	private static String outFilePath = "G:/LSLRecorderTests/data.clis";
	
	private static String IP =  "127.0.0.1";
	
	private static int port = 45678;
	
	private static String outFileFormat = DataFileFormat.CLIS_GZIP;
	
	static
	{
					
		/**
		 * 
		 * 
		 * Sync LSL stream
		 * 
		 */
		
		LSLSimulationParameters par = new LSLSimulationParameters();
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 100 );
		par.setNumOfThreads( 1 );
		par.setChannelNumber( 1 );
		par.setOutDataType( LSL.ChannelFormat.int32 );
		par.setOutputFunctionType( LSLSimulationParameters.LINEAR );
		
												// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		Integer[] lslrecSetting = new Integer[] { 0					,1						,1						,128				,5				,1			,0 };
/* 0 */ testSettings.put( testLSLSync, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );		
		
 
		par = new LSLSimulationParameters();
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 100 );
		par.setNumOfThreads( 1 );
		par.setChannelNumber( 1 );
		par.setOutDataType( LSL.ChannelFormat.int32 );
		par.setOutputFunctionType( LSLSimulationParameters.LINEAR );
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { 1					,1						,1						,128 				,5				,1			,0 };
/* 1 */ testSettings.put( testLSLSync, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );		
	
		
		par = new LSLSimulationParameters();
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 100 );
		par.setNumOfThreads( 1 );
		par.setChannelNumber( 1 );
		par.setOutDataType( LSL.ChannelFormat.int32 );
		par.setOutputFunctionType( LSLSimulationParameters.LINEAR );
		
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { -1					,1						,1						,128 				,5				,1			,0 };
/* 2 */ testSettings.put( testLSLSync, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );		
  
		par = new LSLSimulationParameters();
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 100 );
		par.setNumOfThreads( 1 );
		par.setChannelNumber( 1 );
		par.setOutDataType( LSL.ChannelFormat.int32 );
		par.setOutputFunctionType( LSLSimulationParameters.LINEAR );
		
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { 10					,1						,1						,128 				,5				,1			,0 };
/* 3 */ testSettings.put( testLSLSync, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );		

		par = new LSLSimulationParameters();
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 50 );
		par.setNumOfThreads( 1 );
		par.setChannelNumber( 1 );
		par.setOutDataType( LSL.ChannelFormat.int32 );
		par.setOutputFunctionType( LSLSimulationParameters.LINEAR );
		
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { 300					,1						,1						,128 				,5 				,1			,0 } ;
/* 4 */ testSettings.put( testLSLSync, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );

		par = new LSLSimulationParameters();
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 50 );
		par.setNumOfThreads( 2 );
		par.setChannelNumber( 1 );
		par.setOutDataType( LSL.ChannelFormat.int32 );
		par.setOutputFunctionType( LSLSimulationParameters.LINEAR );
		
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { 300					,1						,1						,128 			,5					,1			,0 };
/* 5 */ testSettings.put( testLSLSync, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );		
 		
		par = new LSLSimulationParameters();
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 50 );
		par.setNumOfThreads( 5 );
		par.setChannelNumber( 1 );
		par.setOutDataType( LSL.ChannelFormat.int32 );
		par.setOutputFunctionType( LSLSimulationParameters.LINEAR );
		
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { 300					,1						,1						,128 			,5					,1			,0 };
/* 6 */ testSettings.put( testLSLSync, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );		

		par = new LSLSimulationParameters();
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 50 );
		par.setNumOfThreads( 1 );
		par.setChannelNumber( 1 );
		par.setOutDataType( LSL.ChannelFormat.int32 );
		par.setOutputFunctionType( LSLSimulationParameters.LINEAR );
		
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { 300					,1						,2						,128 				,5 				, 1				,0 } ;
/* 7 */ testSettings.put( testLSLSync, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );
 
		par = new LSLSimulationParameters();
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 50 );
		par.setNumOfThreads( 5 );
		par.setChannelNumber( 1 );
		par.setOutDataType( LSL.ChannelFormat.int32 );
		par.setOutputFunctionType( LSLSimulationParameters.LINEAR );
		
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { 300					,1						,5						,128 				,5 				, 1				,0 } ;
/* 8 */ testSettings.put( testLSLSync, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );
		
		par = new LSLSimulationParameters();
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 50 );
		par.setNumOfThreads( 1 );
		par.setChannelNumber( 1 );
		par.setOutDataType( LSL.ChannelFormat.int32 );
		par.setOutputFunctionType( LSLSimulationParameters.LINEAR );
		
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize	6-Interleaved
		lslrecSetting = new Integer[] { 300					,1					,100 						,128				,5 				, 1				,0 } ;
/* 9 */ testSettings.put( testLSLSync, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );		 

		par = new LSLSimulationParameters();
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 50 );
		par.setNumOfThreads( 1 );
		par.setChannelNumber( 1 );
		par.setOutDataType( LSL.ChannelFormat.int32 );
		par.setOutputFunctionType( LSLSimulationParameters.LINEAR );
		
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize	6-Interleaved
		lslrecSetting = new Integer[] { 300					,1						,100 					,128				,1 				, 100				,0 } ;
/* 10 */ testSettings.put( testLSLSync, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );

		par = new LSLSimulationParameters();
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 0 );
		par.setNumOfThreads( 1 );
		par.setChannelNumber( 1 );
		par.setOutDataType( LSL.ChannelFormat.int32 );
		par.setOutputFunctionType( LSLSimulationParameters.LINEAR );
		
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize	6-Interleaved
		lslrecSetting = new Integer[] { 20					,1					,100 						,128				,5 				, 100				,0 } ;
/* 11 */ testSettings.put( testLSLSync, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );

		par = new LSLSimulationParameters();
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 0 );
		par.setNumOfThreads( 1 );
		par.setChannelNumber( 1 );
		par.setOutDataType( LSL.ChannelFormat.int32 );
		par.setOutputFunctionType( LSLSimulationParameters.LINEAR );
		
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize	6-Interleaved
		lslrecSetting = new Integer[] { 20					,1					,100 						,128				,5 				, 10				,1 } ;
/* 12 */ testSettings.put( testLSLSync, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );

	
		/**
		 * 
		 * 
		 * Sync Socket
		 * 
		 */

		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 100 ); // number of messages
		par.setInDataType( SocketSetting.TCP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { 0					,1						,1						,128				,5				,1			,0 };
/* 0 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );		

		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 100 ); // number of messages
		par.setInDataType( SocketSetting.UDP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER

								// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { 0					,1						,1						,128				,5				,1			,0 };
/* 1 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );


		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 100 ); // number of messages
		par.setInDataType( SocketSetting.TCP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER

										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { 1					,1						,1						,128 				,5				,1			,0 };
/* 2 */ testSettings.put( testLSLSync, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );		


		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 100 ); // number of messages
		par.setInDataType( SocketSetting.UDP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER

										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { 1					,1						,1						,128 				,5				,1			,0 };
/* 3 */ testSettings.put( testLSLSync, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );		

		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 100 ); // number of messages
		par.setInDataType( SocketSetting.TCP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER


								// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { -1					,1						,1						,128 				,5				,1			,0 };
/* 4 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );		

		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 100 ); // number of messages
		par.setInDataType( SocketSetting.UDP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER


										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { -1					,1						,1						,128 				,5				,1			,0 };
/* 5 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );		



		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 100 ); // number of messages
		par.setInDataType( SocketSetting.TCP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER


										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { 10					,1						,1						,128 				,5				,1			,0 };
/* 6 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );		


		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 100 ); // number of messages
		par.setInDataType( SocketSetting.UDP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER
		
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { 10					,1						,1						,128 				,5				,1			,0 };
/* 7 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );		


		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 50 ); // number of messages
		par.setInDataType( SocketSetting.TCP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER


										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { 300					,1						,1						,128 				,5 				,1			,0 } ;
/* 8 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );

		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 50 ); // number of messages
		par.setInDataType( SocketSetting.UDP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER
		
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { 300					,1						,1						,128 				,5 				,1			,0 } ;
/* 9 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );


		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 50 ); // number of messages
		par.setInDataType( SocketSetting.TCP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER

										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { 300					,1						,2						,128 				,5 				, 1				,0 } ;
/* 10 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );

		
		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 50 ); // number of messages
		par.setInDataType( SocketSetting.UDP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { 300					,1						,2						,128 				,5 				, 1				,0 } ;
/* 11 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );


		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 50 ); // number of messages
		par.setInDataType( SocketSetting.TCP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { 300					,1						,5						,128 				,5 				, 1				,0 } ;
/* 12 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );


		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 50 ); // number of messages
		par.setInDataType( SocketSetting.UDP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize 6-Interleaved
		lslrecSetting = new Integer[] { 300					,1						,5						,128 				,5 				, 1				,0 } ;
/* 13 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );


		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 50 ); // number of messages
		par.setInDataType( SocketSetting.TCP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER


										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize	6-Interleaved
		lslrecSetting = new Integer[] { 300					,1					,100 						,128				,5 				, 1				,0 } ;
/* 14 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );		 


		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 50 ); // number of messages
		par.setInDataType( SocketSetting.UDP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER
		
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize	6-Interleaved
		lslrecSetting = new Integer[] { 300					,1					,100 						,128				,5 				, 1				,0 } ;
/* 15 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );	


		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 50 ); // number of messages
		par.setInDataType( SocketSetting.TCP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER

										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize	6-Interleaved
		lslrecSetting = new Integer[] { 300					,1						,100 					,128				,1 				, 100				,0 } ;
/* 16 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );


		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( 50 ); // number of messages
		par.setInDataType( SocketSetting.UDP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize	6-Interleaved
		lslrecSetting = new Integer[] { 300					,1						,100 					,128				,1 				, 100				,0 } ;
/* 17 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );


		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( Integer.MAX_VALUE ); // number of messages
		par.setInDataType( SocketSetting.TCP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER


										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize	6-Interleaved
		lslrecSetting = new Integer[] { 20					,1					,100 						,128				,5 				, 100				,0 } ;
/* 18 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );


		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( Integer.MAX_VALUE ); // number of messages
		par.setInDataType( SocketSetting.UDP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER
		
										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize	6-Interleaved
		lslrecSetting = new Integer[] { 20					,1					,100 						,128				,5 				, 100				,0 } ;
/* 19 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );


		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( Integer.MAX_VALUE ); // number of messages
		par.setInDataType( SocketSetting.TCP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER

										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize	6-Interleaved
		lslrecSetting = new Integer[] { 20					,1					,100 						,128				,5 				, 10				,1 } ;
/* 20 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );

		
		par = new LSLSimulationParameters();
		par.setBlockSize( 1 ); // Number of socket thread
		par.setSamplingRate( 2 );
		par.setNumberOutputBlocks( Integer.MAX_VALUE ); // number of messages
		par.setInDataType( SocketSetting.UDP_PROTOCOL ); // TCP/UDP
		par.setStreamType( "127.0.0.1" ); // IP ADDRESS
		par.setChannelNumber( 45678 ); // PORT NUMBER

										// 0-timeData(s)	1-specialSyncMarker		2-DataStreamChannels	3-DataSamplingRate	4-No.DataStream	5-ChunckSize	6-Interleaved
		lslrecSetting = new Integer[] { 20					,1					,100 						,128				,5 				, 10				,1 } ;
/* 21 */ testSettings.put( testSyncSocket, new Tuple< Integer[], LSLSimulationParameters>( lslrecSetting, par ) );

	}
	
	private static int indexTest = -1;
	private static int step = 1;	
	
	/*
	 * @param args
	 */
	public static void main(String[] args)
	{	
		try 
		{	
			UIManager.setLookAndFeel( UIManager.getSystemLookAndFeelClassName() );
			//UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
		} 
		catch (Exception e) 
		{
			try 
			{
				// Set cross-platform Java L&F (also called "Metal")
				UIManager.setLookAndFeel( UIManager.getCrossPlatformLookAndFeelClassName() );
			}
			catch ( Exception e1) 
			{
			}
		}
						
		boolean launchApp = true;
		
		ConfigApp.setTesting( true );
		
		int lim = 100;
		
		if( lim == 0 )
		{
			Language.loadLanguages();
			Language.setDefaultLocalLanguage();

			try 
			{
				createApplication();
			}
			catch (Throwable e) 
			{
				e.printStackTrace();
			}
		}
		
		testSettings.remove( testLSLSync );
		
		for( int testType : testSettings.keySet() )
		{
			List< Tuple< Integer[], LSLSimulationParameters > > testSet = testSettings.get( testType );
									
			for( indexTest = 0; indexTest < testSet.size() && indexTest < lim; indexTest = indexTest + step )
			{	
				Tuple< Integer[], LSLSimulationParameters > setting = testSet.get( indexTest );
				
				Integer[] dataStreamCfg = setting.x;
				LSLSimulationParameters simPar = setting.y;
						
				try
				{				
					List< IStoppableThread > synchThreadList = new ArrayList< IStoppableThread>();

					try
					{	
						((HashSet)ConfigApp.getProperty( ConfigApp.LSL_ID_DEVICES )).clear();
						guiManager.getInstance().refreshLSLDevices();
						
						LSLStream.CreateDataLSLStreams( dataStreamCfg[ 0 ], LSL.ChannelFormat.double64, dataStreamCfg[ 2 ]
														, dataStreamCfg[ 3 ], dataStreamCfg[ 4 ], dataStreamCfg[ 5 ], dataStreamCfg[ 6 ]  );

						if( testType == testLSLSync )
						{
							for( int i = 0; i <simPar.getNumOfThreads(); i++ )
							{
								double a = (i+1);
								int t =  (int)( 1000D / simPar.getSamplingRate() ); 

								if( Math.random() < 0.5 )
								{
									a = 1 / a;
								}

								t =  (int)( t * a );

								if( t < 10 )
								{
									t = 10;
								}

								synchThreadList.add( new testSyncLSL( i, t , simPar.getNumberOutputBlocks(), simPar.isInterleavedData() ? 0 : 1 ) );
							}				
						}
						else
						{
							RegisterSyncMessages.clearSyncMessages();
							
							for( int i = 2; i < 6; i++ )
							{
								RegisterSyncMessages.addSyncMessage( "msg" + i );
							}
							
							String ipAddress = simPar.getStreamType();
							int port = simPar.getChannelNumber();
							int protocol = simPar.getInDataType();
														
							Set< String > map = ( Set< String > )ConfigApp.getProperty( ConfigApp.SERVER_SOCKET  );
						
							map.clear();

							String id = "TCP:";
							if( protocol == SocketSetting.UDP_PROTOCOL )
							{
								id = "UDP:";
							}
							
							id += ipAddress + ":" + port;
							
							Set< String > msgs = RegisterSyncMessages.getSyncMessagesAndMarks().keySet();
							msgs.remove( RegisterSyncMessages.INPUT_START );
							msgs.remove( RegisterSyncMessages.INPUT_STOP );
							
							String s = "{{" + id + "=" + msgs.toString() + "}}";
							
							ConfigApp.setProperty( ConfigApp.SERVER_SOCKET, s );
							
							for( int i = 0; i <simPar.getNumOfThreads(); i++ )
							{
								double a = (i+1);
								int t =  (int)( 1000D / simPar.getSamplingRate() ); 

								if( Math.random() < 0.5 )
								{
									a = 1 / a;
								}

								t =  (int)( t * a );

								if( t < 10 )
								{
									t = 10;
								}

								IStoppableThread sync = new testSendSocketMsgToLslRec( simPar.getBlockSize()
																						, (long)t
																						, simPar.getNumberOutputBlocks()
																						, protocol
																						, ipAddress
																						, port );
								
								synchThreadList.add( sync  );
							}							
						}

						Thread.sleep( 2000L );

						if( launchApp )
						{
							Language.loadLanguages();
							Language.setDefaultLocalLanguage();

							createApplication();

							launchApp  = false;
						}

						// load configuration
						try
						{
							if( args.length > 1 )
							{
								if( args[0].equals( "-c" ) )
								{
									guiManager.getInstance().getAppUI().getGlassPane().setVisible( true );

									ConfigApp.loadConfig( new File( args[ 1 ] ) );
								}
								else
								{	
									LoadSettings( testType, simPar.getNumOfThreads(), dataStreamCfg[ 5 ], dataStreamCfg[ 6 ], dataStreamCfg[ 1 ] );
								}
							}
							else
							{		
								LoadSettings( testType, simPar.getNumOfThreads(), dataStreamCfg[ 5 ], dataStreamCfg[ 6 ], dataStreamCfg[ 1 ] );
							}
						}
						catch( Throwable e)
						{
							new Thread() 
							{
								public void run() 
								{					
									showError( e, false );
								};
							}.start();
						}
						finally
						{
							guiManager.getInstance().getAppUI().loadConfigValues();
							guiManager.getInstance().refreshLSLDevices();
							guiManager.getInstance().getAppUI().getGlassPane().setVisible( false );						
						}

						boolean startTest = false;

						if( args.length > 2 )
						{
							startTest = args[ 2 ].equals( "-start" );
						}
						else if( args.length == 1 )
						{
							startTest = args[ 0 ].equals( "-start" );
						}

						if( startTest )
						{	
							guiManager.getInstance().addInputMessageLog( "Test index " + indexTest + ". Settings: " + simPar + "-" + Arrays.toString( dataStreamCfg ) + "\n" );

							guiManager.getInstance().startTest( false );

							Thread.sleep( 3000L );

							for( IStoppableThread t : synchThreadList )
							{
								t.startThread();
							}

							Thread.sleep( 3000L );
						}
					}
					catch (Throwable e2)
					{
						showError( e2, true );
					}
					finally
					{			
					}	


					try 
					{
						Thread.sleep( 5000L );
					}
					catch (InterruptedException e) 
					{
						e.printStackTrace();
					}


					while( OutputDataFileHandler.getInstance().isWorking() 
							|| OutputDataFileHandler.getInstance().isSavingData() 
							|| CoreControl.getInstance().isRecording() )
					{
						try 
						{
							Thread.sleep( 1000L );
						}
						catch (InterruptedException e) 
						{
						}
					}

					LSLStream.stopStreams();

					for( IStoppableThread t : synchThreadList )
					{
						t.stopThread( IStoppableThread.FORCE_STOP );
					}

					try 
					{
						Thread.sleep( 3000L );
					}
					catch (InterruptedException e) 
					{
					}
				}
				catch (Exception e) 
				{	
				}
				finally 
				{
					System.out.println("testLSLRecorder.main() TEST index " + indexTest + " END" );
				}
			}
		}
	}

	private static void LoadSettings( int testType, int numSynStream, int chunkSize, int interleave, int specialInputs  )
	{
		guiManager.getInstance().refreshLSLDevices();
		
		ConfigApp.setProperty( ConfigApp.LSL_OUTPUT_FILE_FORMAT, outFileFormat );
		ConfigApp.setProperty( ConfigApp.LSL_OUTPUT_FILE_NAME, outFilePath );
		
		HashSet< MutableDataStreamSetting > lslCfg = (HashSet< MutableDataStreamSetting >)ConfigApp.getProperty( ConfigApp.LSL_ID_DEVICES );
		
		if( testType == testLSLSync )
		{	
			for( int i = 0; i < numSynStream; i++ )
			{
				for( MutableDataStreamSetting cfg : lslCfg )
				{
					if( cfg.getStreamName().equals( testSyncLSL.PREFIX + i ) )
					{
						cfg.setSynchronizationStream( true );
						cfg.setSelected( false );
					}
				}
			}
			
			ConfigApp.setProperty( ConfigApp.SELECTED_SYNC_METHOD, ConfigApp.SYNC_LSL );			
			
		}
		else
		{
			ConfigApp.setProperty( ConfigApp.SELECTED_SYNC_METHOD, ConfigApp.SYNC_SOCKET );
		}
		
		ConfigApp.setProperty( ConfigApp.IS_ACTIVE_SPECIAL_INPUTS, false);
		
		if( specialInputs != 0 )
		{
			ConfigApp.setProperty( ConfigApp.IS_ACTIVE_SPECIAL_INPUTS, true);
		}
		
		for( MutableDataStreamSetting cfg : lslCfg )
		{
			if( !cfg.isSynchronationStream() )
			{
				cfg.setSelected( true );
				cfg.setChunckSize( chunkSize );
				cfg.setInterleaveadData( interleave != 0 );
			}
		}
		
	}

	public static void createApplication() throws Throwable
	{	
		createAppGUI();
		createAppCoreControl();
	}

	private static void createAppCoreControl()
	{
		try
		{
			CoreControl ctrl = CoreControl.getInstance();
			ctrl.start();
		}
		catch (Exception e)
		{
			showError( e, true );
		}
	}

	private static appUI createAppGUI() throws Exception
	{	
		Dimension openDim = new Dimension( 500, 200 );
		OpeningDialog open = new OpeningDialog( openDim 
												,  GeneralAppIcon.getIconoAplicacion( 128, 128).getImage()
												, ConfigApp.shortNameApp
												, "<html><center><h1>Opening " + ConfigApp.fullNameApp + ".<br>Wait please...</h1></center></html>" 
												, Color.WHITE );
		open.setVisible( true );
		open.setDefaultCloseOperation( OpeningDialog.DISPOSE_ON_CLOSE );
		
		Toolkit t = Toolkit.getDefaultToolkit();
		Dimension dm = t.getScreenSize();
		Insets pad = t.getScreenInsets( open.getGraphicsConfiguration() );

		
		open.setLocation( dm.width / 2 - openDim.width / 2, dm.height / 2 - openDim.height / 2 );		
		
	
		appUI ui = appUI.getInstance();
		
		ui.setIconImage(GeneralAppIcon.getIconoAplicacion(64, 64).getImage());
		ui.setTitle(  ConfigApp.fullNameApp );
		
		ui.setBackground(SystemColor.info);

		dm.width = (dm.width / 2 - (pad.left + pad.right));
		dm.height = (dm.height / 2 - (pad.top + pad.bottom));

		if( dm.width < 650 )
		{
			dm.width = 650;
		}
		
		if( dm.height < 650 )
		{
			dm.height = 650;
		}
		
		ui.setSize( dm );

		ui.toFront();
		Dimension d = new Dimension(dm);
		d.width /= 5;

		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		Insets insets = Toolkit.getDefaultToolkit().getScreenInsets(gd.getDefaultConfiguration());
		ui.setLocation( insets.left + 1, insets.top + 1 );

		ui.setVisible(true);
		
		open.dispose();

		return ui;
	}

	/*
	private static void showError( Throwable e, final boolean fatalError )
	{
		JTextArea jta = new JTextArea();
		jta.setAutoscrolls( true );
		jta.setEditable( false );
		jta.setLineWrap( true );
		jta.setTabSize( 0 );

		TextAreaPrintStream log = new TextAreaPrintStream( jta, new ByteArrayOutputStream() );

		e.printStackTrace( log );

		String[] lines = jta.getText().split( "\n" );
		int wd = Integer.MIN_VALUE;
		FontMetrics fm = jta.getFontMetrics( jta.getFont() );
		for (int i = 0; i < lines.length; i++)
		{
			if (wd < fm.stringWidth( lines[i] ) )
			{
				wd = fm.stringWidth( lines[i] );
			}
		}

		JDialog p = new JDialog();

		Icon icono = UIManager.getIcon( "OptionPane.warningIcon" );
		
		int w = icono.getIconWidth();
		int h = icono.getIconHeight();
		
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice gd = ge.getDefaultScreenDevice();
		GraphicsConfiguration gc = gd.getDefaultConfiguration();
		
		BufferedImage img = gc.createCompatibleImage( w, h, BufferedImage.TYPE_INT_ARGB ); 
		Graphics2D g = img.createGraphics();
		icono.paintIcon( null, g, 0, 0 );
		p.setIconImage( img );

		p.setTitle( "Problem" );
		
		if( fatalError )
		{
			p.setTitle( "Fatal Error" );
		}
		
		Dimension d = new Dimension( (int)( wd * 1.25D ), fm.getHeight() * 10 );
		p.setSize( d );

		Point pos = ge.getCenterPoint();
		pos.x -= d.width / 2;
		pos.y -= d.height / 2;
		p.setLocation(pos);

		p.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				if( fatalError )
				{
					System.exit( 0 );
				}
			}

		});
		
		JButton close = new JButton("Cerrar");
		close.addActionListener(new ActionListener()
		{
			public void actionPerformed( ActionEvent e )
			{
				if( fatalError )
				{
					System.exit( 0 );
				}
				else
				{
					p.dispose();
				}
			}

		});
		
		p.add( new JScrollPane( jta ), BorderLayout.CENTER );
		p.add( close, BorderLayout.SOUTH );
		p.toFront();
		p.setVisible( true );		
	}
	*/
	
	private static void showError( Throwable e, final boolean fatalError )
	{
		ExceptionDialog.createExceptionDialog( null );
		
		if( fatalError )
		{
			ExceptionDialog.AppExitWhenWindowClosing();
		}
		
		ExceptionMessage msg = new ExceptionMessage( e, Language.getLocalCaption( Language.DIALOG_ERROR), ExceptionDictionary.ERROR_MESSAGE );
		
		ExceptionDialog.showMessageDialog( msg, true, true );		
	}	

}
