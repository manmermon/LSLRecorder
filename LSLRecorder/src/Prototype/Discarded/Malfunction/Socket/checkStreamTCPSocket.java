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

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;

import StoppableThread.AbstractStoppableThread;

public class checkStreamTCPSocket extends AbstractStoppableThread 
{
	private InputStream inputStreamSocket;
	private IClosedSocketMonitor monitorSocket;
	
	public checkStreamTCPSocket( Socket s, IClosedSocketMonitor monitor ) throws IOException 
	{
		if( s == null || monitor == null)
		{	
			throw new IllegalArgumentException( "Socket and/or monitor are null." );
		}
		
		this.inputStreamSocket = s.getInputStream();
		this.monitorSocket = monitor;
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
		if( this.inputStreamSocket != null )
		{
			super.wait( 1000L );
			try
			{
				if( this.inputStreamSocket.read() < 0 )
				{
					this.monitorSocket.closedConnection();
				}
			}
			catch( Exception e )
			{
				super.stopThread = true;
				this.monitorSocket.closedConnection();
			}
		}
		else
		{			
			super.stopThread = true;
			throw new IllegalStateException( "InputStream channel from socket is null." );
		}
	}

}
