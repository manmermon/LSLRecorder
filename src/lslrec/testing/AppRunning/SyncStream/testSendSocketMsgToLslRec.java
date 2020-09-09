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
package testing.AppRunning.SyncStream;

import java.util.ArrayList;
import java.util.List;

import Prototype.Socket.TCPClientSocketThread;
import Prototype.Socket.UDPClientSocketThread;
import Sockets.Info.SocketSetting;
import StoppableThread.AbstractStoppableThread;
import StoppableThread.IStoppableThread;
import Timers.ITimerMonitor;
import Timers.Timer;
import edu.ucsd.sccn.LSL;

public class testSendSocketMsgToLslRec extends AbstractStoppableThread implements ITimerMonitor
{
	private List< String > msgs = new ArrayList< String >();
	private int indMsg;

	private Timer timer = new Timer();

	private int cuenta = 0;

	private List< TCPClientSocketThread > tcpClients;
	private List< UDPClientSocketThread > udpClients;
	
	private LSL.StreamInfo info;
	
	private LSL.StreamOutlet out;

	private int numSocket;
	
	private int protocol, port;
	
	private String ip;
	
	private boolean socketCreate = false;
		
	public testSendSocketMsgToLslRec( int numSocket, long time, int count, int IdSocketProtocol, String IP, int port ) 
	{
		try 
		{					
			cuenta = count;
			
			this.numSocket = numSocket;
			
			this.protocol = IdSocketProtocol;
			this.port = port;
			this.ip = IP;
			
			timer.setTimerValue( time );
			timer.setTimerMonitor( this );

			info = new LSL.StreamInfo( this.getClass().getSimpleName()
									, "time"
									, 2
									, 0
									, LSL.ChannelFormat.double64
									, this.getClass().getSimpleName() );
			info.desc().append_child_value( "details", "ch1-time;ch2-mark" );
			
			this.out = new LSL.StreamOutlet( this.info );			
						
			tcpClients = new ArrayList< TCPClientSocketThread >();
			udpClients = new ArrayList< UDPClientSocketThread >();

			String end = "" + "\n";
			msgs.add( "__start" + end );
			msgs.add( "msg2" + end);
			msgs.add( "msg3" + end);
			msgs.add( "msg4" + end);
			msgs.add( "msg5" + end);
			msgs.add( "__stop" + end);

			timer.setName( "TIMER-testSendMsg2LSLRec");			
		} 
		catch (Exception e) 
		{
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
		
		double[] t =new double[] { System.nanoTime() / 1e9D, Math.pow( 2, indMsg ) };
		
		if( this.socketCreate )
		{
			indMsg++;
			
			try
			{
				System.out.println("testSendSocketMsgToLslRec.timeOver() " + msg );
				for( TCPClientSocketThread client : tcpClients )
				{
					client.sendMessage( msg );
				}
		
				for( UDPClientSocketThread client : udpClients )
				{
					client.sendMessage( msg );
				}
				
				if( this.out.have_consumers() )
				{
					this.out.push_sample( t );
				}
	
				cuenta--;
				if( cuenta <= 0 )
				{	
					for( TCPClientSocketThread client : tcpClients )
					{
						client.stopThread( IStoppableThread.FORCE_STOP );
					}
		
					for( UDPClientSocketThread client : udpClients )
					{
						client.stopThread( IStoppableThread.FORCE_STOP );
					}
		
					timer.stopThread( IStoppableThread.FORCE_STOP );
					
		
					this.out.close();
					
					System.out.println("FIN");
					
				}
				else
				{
					timer.restartTimer();
				}
			}
			catch (Exception e) 
			{
			}
		}
		else
		{
			this.createSocket();
			timer.restartTimer();
		}
		
	}

	@Override
	public void reportClockTime(long time) 
	{		
	}

	@Override
	protected void startUp() throws Exception 
	{
		super.startUp();
		
		Thread.sleep( 1000L );
		
		this.createSocket();
		
		timer.startThread();		
	}
	
	private void createSocket()
	{
		if( !this.socketCreate )
		{
			this.socketCreate = true;
			
			try					
			{
				for( int n = 0; n < numSocket; n++ )
				{
					SocketSetting info = new SocketSetting( this.protocol, this.ip, this.port + n );
					
					if( info.getProtocolType() == SocketSetting.TCP_PROTOCOL )
					{
						TCPClientSocketThread c = new TCPClientSocketThread( info.getSocketAddress() );
						tcpClients.add( c );
					}
					else
					{
						UDPClientSocketThread updClient = new UDPClientSocketThread( info.getSocketAddress() );
						udpClients.add( updClient );					
					}
				}
				
				for( TCPClientSocketThread c : this.tcpClients )
				{
					c.startThread();
				}
				
				for( UDPClientSocketThread c : this.udpClients )
				{
					c.startThread();
				}
			}
			catch (Exception e) 
			{
				this.socketCreate = false;
				
				for( TCPClientSocketThread c : this.tcpClients )
				{
					c.stopThread( IStoppableThread.FORCE_STOP );
				}
				
				for( UDPClientSocketThread c : this.udpClients )
				{
					c.stopThread( IStoppableThread.FORCE_STOP );
				}
			}
		}
	}
	
	@Override
	protected void targetDone() throws Exception 
	{
		super.stopThread = true;

		super.targetDone();
	}
	
	@Override
	protected void preStopThread(int friendliness) throws Exception 
	{	
	}

	@Override
	protected void postStopThread(int friendliness) throws Exception 
	{	
	}

	@Override
	protected void runInLoop() throws Exception 
	{	
	}

}
