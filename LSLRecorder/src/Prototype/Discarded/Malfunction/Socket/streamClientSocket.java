/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec and CLIS.
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

package Prototype.Discarded.Malfunction.Socket;

import java.nio.channels.DatagramChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

import Prototype.Discarded.Malfunction.Socket.Manager.ManagerInOutStreamSocket;
import Sockets.Info.SocketSetting;
import Sockets.Info.SocketParameters;

public class streamClientSocket   
{
	public static ManagerInOutStreamSocket createClientSocket( SocketParameters socketPars ) throws Exception
	{
		if( socketPars == null )
		{
			throw new IllegalArgumentException( "Null parameters." );
		}
		
		ManagerInOutStreamSocket ioss = null;
		
		AbstractSelectableChannel channel;
		if( socketPars.getSocketInfo().getProtocolType() == SocketSetting.TCP_PROTOCOL )
		{
			channel = SocketChannel.open();
			channel.configureBlocking( true );
			((SocketChannel) channel).socket().setReuseAddress( true );
			((SocketChannel) channel).connect( socketPars.getSocketInfo().getSocketAddress() );
		}
		else //if( socketInfo.getProtocolType() == streamingParameters.UDP_PROTOCOL )
		{
			channel = DatagramChannel.open();
			channel.configureBlocking( true );
			((DatagramChannel) channel).socket().setReuseAddress( true );
			
			((DatagramChannel) channel).connect( socketPars.getSocketInfo().getSocketAddress() );				
		}
		
		if( channel != null )
		{	
			ioss = new ManagerInOutStreamSocket( channel, socketPars.getDirectionSocketConnection() );
		}
		
		return ioss;
	}
}
