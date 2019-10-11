package Prototype.Socket;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import StoppableThread.AbstractStoppableThread;

public class UDPClientSocketThread extends AbstractStoppableThread 
{
	private DatagramSocket udpClient;
	
	private int bufferSize = 65507;
	private List< DatagramPacket > sendPackets;
	
	private InetSocketAddress remote;
	
	public UDPClientSocketThread( InetSocketAddress address ) throws Exception
	{
		if( address == null )
		{
			throw new IllegalArgumentException( "Remote socket information null" );
		}
		
		this.remote = address;
		
		this.udpClient = new DatagramSocket( );	
		super.setName( "UPD-CLIENT>>" + this.udpClient.getLocalAddress() + ":" + this.udpClient.getLocalPort() );		
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
	protected void preStart() throws Exception 
	{
		super.preStart();
		
		this.sendPackets = new ArrayList< DatagramPacket >();
	}
	
	@Override
	protected void runInLoop() throws Exception 
	{
		synchronized( this )
		{
			try
			{
				this.wait();
			}
			catch( InterruptedException ex )
			{				
			}
		}
		
		synchronized( this.sendPackets )
		{
			for( DatagramPacket packet : this.sendPackets )
			{
				this.udpClient.send( packet );
			}
			
			this.sendPackets.clear();
		}
	}
	
	@Override
	protected void cleanUp() throws Exception 
	{
		super.cleanUp();
		
		if( !this.sendPackets.isEmpty() )
		{
			for( DatagramPacket packet : this.sendPackets )
			{
				this.udpClient.send( packet );
			}
			
			this.sendPackets.clear();
		}
		
		this.udpClient.close();
		
		this.sendPackets = null;
		this.udpClient = null;
	}
	
	public void sendMessage( String msg ) 
	{
		byte[] out = msg.getBytes();
		if( this.sendPackets != null )
		{
			synchronized ( this.sendPackets) 
			{
				this.sendPackets.add( new DatagramPacket( out, out.length, this.remote.getAddress(), this.remote.getPort() ) );
			}
			
			synchronized( this )
			{
				this.notify();
			}
		}
	}

}
