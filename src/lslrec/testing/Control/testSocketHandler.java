/*
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.testing.Control;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lslrec.config.Parameter;
import lslrec.config.ParameterList;
import lslrec.controls.IHandlerMinion;
import lslrec.controls.IHandlerSupervisor;
import lslrec.controls.MinionParameters;
import lslrec.controls.SocketHandler;
import lslrec.controls.messages.EventInfo;
import lslrec.sockets.info.SocketParameters;
import lslrec.sockets.info.SocketSetting;
import lslrec.stoppableThread.IStoppableThread;
import lslrec.testing.Socket.TCPClientSocketThread;
import lslrec.testing.Socket.UDPClientSocketThread;
import lslrec.testing.timers.ITimerMonitor;
import lslrec.testing.timers.Timer;


public class testSocketHandler implements ITimerMonitor
{
	static SocketHandler ctrSocket;
	static List< SocketSetting > dest;

	static List< String > msgs = new ArrayList< String >();
	static int indMsg;
	
	static Timer timer = new Timer();
	
	static int cuenta = 0;
	
	static List< TCPClientSocketThread > clients;
	static List< UDPClientSocketThread > udpClients;
	
	public static void main(String[] args) 
	{
		try 
		{
			testSocketHandler main = new testSocketHandler();
			
			long time = 1000L;
						
			cuenta = 1000;//(int)( (time / 1000D) * 3600 );
			
			time = 1L;
			
			timer.setTimerValue( time );
			timer.setTimerMonitor( main );
			
			IHandlerSupervisor s = new IHandlerSupervisor()
			{				
				@Override
				public void eventNotification(IHandlerMinion minion, EventInfo event) 
				{
					System.out.println("test.eventNotification() " + minion + " - " + event );
				}
			};
						
			ctrSocket = SocketHandler.getInstance();
			ctrSocket.setControlSupervisor( s );		
			ctrSocket.startThread();
			
			SocketSetting info = new SocketSetting( SocketSetting.UDP_PROTOCOL, "127.0.0.1", 12345 );
			//SocketSetting info = new SocketSetting( SocketSetting.TCP_PROTOCOL, "127.0.0.1", 12345 );
			SocketParameters streamPars = new SocketParameters( info, SocketParameters.SOCKET_CHANNEL_IN );
			
			dest = new ArrayList< SocketSetting >();
			dest.add( info );
								
			Map< String, Object > sockets = new HashMap<String,Object>();
			sockets.put( SocketHandler.SERVER_SOCKET_STREAMING, streamPars );
			
			MinionParameters pars = new MinionParameters();
			
			ParameterList lst = new ParameterList();
			List< SocketParameters > l = new ArrayList<SocketParameters>();
			l.add( streamPars );
			Parameter p = new Parameter( SocketHandler.SERVER_SOCKET_STREAMING, l );
			lst.addParameter( p );
			
			pars.setMinionParameters( SocketHandler.ID, lst );
			
			ctrSocket.addSubordinates( pars );
			
			clients = new ArrayList< TCPClientSocketThread >();
			udpClients = new ArrayList< UDPClientSocketThread >();
			
			for( int n = 0; n < 100; n++ )
			{
				if( info.getProtocolType() == SocketSetting.TCP_PROTOCOL )
				{
					TCPClientSocketThread c = new TCPClientSocketThread( info.getSocketAddress() );
					clients.add( c );
					
					c.startThread();					
				}
				else
				{
					UDPClientSocketThread updClient = new UDPClientSocketThread( info.getSocketAddress() );
					udpClients.add( updClient );
					
					updClient.startThread();					
				}
			}
						
			String end = "" + "\n";
			msgs.add( "__start" + end );
			msgs.add( "msg2" + end);
			msgs.add( "msg3" + end);
			msgs.add( "msg4" + end);
			msgs.add( "msg5" + end);
			msgs.add( "msg6" + end);
			msgs.add( "msg7"+ end );
			msgs.add( "msg8"+ end );
			msgs.add( "msg9" + end);
			msgs.add( "msg10" + end);
			msgs.add( "msg11" + end);
			msgs.add( "__stop" + end);
			
			//main.timeOver( "main" );
			
			timer.setName( "TIMER");
			timer.startThread();
		} 
		catch (Exception e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void timeOver(String timerName) 
	{
		if( indMsg > 0 )
		{
			if( indMsg >= ( msgs.size() - 1 ))
			{
				indMsg = 1;
			}
		}		
		
		String msg = msgs.get( indMsg );
		
		if( cuenta <= 1 )
		{
			msg = msgs.get( msgs.size() - 1 );			
		}
		
		indMsg++;
			
		for( TCPClientSocketThread client : clients )
		{
			client.sendMessage( msg );
		}
		
		for( UDPClientSocketThread client : udpClients )
		{
			client.sendMessage( msg );
		}
		
		cuenta--;
		if( cuenta <= 0 )
		{			
			/*
			for( TCPClientSocketThread client : clients )
			{
				client.stopThread( IStoppableThread.FORCE_STOP );
			}
			
			for( UDPClientSocketThread client : udpClients )
			{
				client.stopThread( IStoppableThread.FORCE_STOP );
			}
			*/
			
			timer.stopThread( IStoppableThread.FORCE_STOP );
			
			ctrSocket.deleteSubordinates( IStoppableThread.FORCE_STOP );
			//ctrSocket.stopThread( IStoppableThread.FORCE_STOP );
		}
		else
		{
			timer.restartTimer();
		}
	}

	@Override
	public void reportClockTime(long time) 
	{		
	}
}
