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
package lslrec.testing.Others;

import java.io.*;
import java.net.*;

public class testJavaPing
{
	// Sends ping request to a provided IP address 
	  public static void sendPingRequest(String ipAddress) 
	              throws UnknownHostException, IOException 
	  { 
	    InetAddress geek = InetAddress.getByName(ipAddress); 
	    System.out.println("Sending Ping Request to " + ipAddress);
	    long t = System.nanoTime();
	    if (geek.isReachable(5000))
	    {
	    	System.out.println("testJavaPing.sendPingRequest() time " + (( System.nanoTime() - t)/1e6D) + " ms");
	      System.out.println("Host is reachable");
	    }
	    else
	      System.out.println("Sorry ! We can't reach to this host"); 
	  } 
	  
	  // Driver code 
	  public static void main(String[] args) 
	          throws UnknownHostException, IOException 
	  { 
		
	    String ipAddress = "127.0.0.1";
	    sendPingRequest(ipAddress); 
	  
	    ipAddress = "150.214.141.198"; 
	    sendPingRequest(ipAddress);
	    
	    ipAddress = "150.214.141.159"; 
	    sendPingRequest(ipAddress);
	  
	    ipAddress = "150.214.186.69"; 
	    sendPingRequest(ipAddress);
	    	    
	    ipAddress = "239.255.42.255";
	    sendPingRequest(ipAddress); 
	    
	    ipAddress = "239.255.172.255";
	    sendPingRequest(ipAddress);
	    
	    ipAddress = "239.192.172.255";
	    sendPingRequest(ipAddress);
	    
	  } 
}
