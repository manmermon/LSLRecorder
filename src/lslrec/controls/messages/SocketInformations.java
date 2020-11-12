/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
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

package lslrec.controls.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lslrec.sockets.info.SocketParameters;

public class SocketInformations 
{
	private List< SocketParameters > clientSocketInfos = null;
	private SocketParameters serverSocketInfo = null;
	private Map< String, Integer > inputCmds = null;
	private Map< String, String > outputCmds = null;
	
	/**
	 * Set socket information: setting and input/output messages. 
	 */
	public SocketInformations() 
	{
		this.clientSocketInfos = new ArrayList< SocketParameters >();
		this.inputCmds = new HashMap< String, Integer >();
		this.outputCmds = new HashMap< String, String >();
	}

	/**
	 * 
	 * @return List of input socket parameters.
	 */
	public List< SocketParameters > getInputSocketInformation()
	{
		return this.clientSocketInfos;
	}
	
	/**
	 * 
	 * @return Output socket parameters.
	 */
	public SocketParameters getOuputSocketInformation()
	{
		return this.serverSocketInfo;
	}
	
	/**
	 * 
	 * @return Map< K, V > with registered input messages:
	 * 			K -> String of message ID.
	 * 			V -> Integer of synchronization mark value.
	 */
	public Map< String, Integer > getInputCommands()
	{
		return this.inputCmds;
	}
	
	/**
	 * 
	 * @return Map< K, M > with output messages:
	 * 			K -> String of output event.
	 * 			M -> output message.
	 */
	public Map< String, String > getOutputCommands()
	{
		return this.outputCmds;
	}
	
	/**
	 * 
	 * @param info -> set socket parameters.
	 * @throws IllegalArgumentException -> info is null.
	 */
	public void setServerInformation( SocketParameters info ) throws IllegalArgumentException
	{
		if( info == null )
		{
			throw new IllegalArgumentException( "SocketParameter null." );
		}
		
		this.serverSocketInfo = info;
	}
	
	/**
	 * Replaces the SocketParameter at the specified position in this list with the specified element.
	 *  
	 * @param pos -> index of the element to replace.
	 * @param info -> Socket parameter
	 * 
	 * @throws UnsupportedOperationException - if the set operation is not supported by this list
	 * @throws ClassCastException - if the class of the specified element prevents it from being added to this list
	 * @throws NullPointerException - if the specified element is null and this list does not permit null elements
	 * @throws IllegalArgumentException - if some property of the specified element prevents it from being added to this list
	 * @throws IndexOutOfBoundsException - if the index is out of range(index < 0 || index >= size())
	 */
	public void setInputSocketInformations( int pos, SocketParameters info ) 
			throws UnsupportedOperationException, ClassCastException, NullPointerException
					, IllegalArgumentException, IndexOutOfBoundsException
													
	{
		this.clientSocketInfos.set( pos, info );
	}
	
	/**
	 * Add a SocketParameter.
	 *  
	 * @param info -> Socket parameter
	 * 
	 * @throws UnsupportedOperationException - if the set operation is not supported by this list
	 * @throws ClassCastException - if the class of the specified element prevents it from being added to this list
	 * @throws NullPointerException - if the specified element is null and this list does not permit null elements
	 * @throws IllegalArgumentException - if some property of the specified element prevents it from being added to this list
	 */
	public void addClientInformations( SocketParameters info ) 
			throws UnsupportedOperationException, ClassCastException
					, NullPointerException, IllegalArgumentException
	{
		this.clientSocketInfos.add( info );
	}
	
	/**
	 * Add an input message and its mark value.
	 *  
	 * @param textMsg 	-> message.
	 * @param mark		-> integer value.
	 * 
	 * @throws UnsupportedOperationException - if the set operation is not supported by this list
	 * @throws ClassCastException - if the class of the specified element prevents it from being added to this list
	 * @throws NullPointerException - if the specified element is null and this list does not permit null elements
	 * @throws IllegalArgumentException - if some property of the specified element prevents it from being added to this list
	 */
	public void addInputCommands( String textMsg, Integer mark ) 
			throws UnsupportedOperationException, ClassCastException
			, NullPointerException, IllegalArgumentException
	{
		this.inputCmds.put( textMsg, mark  );
	}
	
	/**
	 * Insert/replace output message.
	 *  
	 * @param messageType -> output message identifier.
	 * @param message -> output message text.
	 * 
	 * @throws UnsupportedOperationException - if the set operation is not supported by this list
	 * @throws ClassCastException - if the class of the specified element prevents it from being added to this list
	 * @throws NullPointerException - if the specified element is null and this list does not permit null elements
	 * @throws IllegalArgumentException - if some property of the specified element prevents it from being added to this list
	 */
	public void putOutputCommand( String messageType, String message )
			throws UnsupportedOperationException, ClassCastException
			, NullPointerException, IllegalArgumentException
	{		
		this.outputCmds.put( messageType, message );
	}
}
