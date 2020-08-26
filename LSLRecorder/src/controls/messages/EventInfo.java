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

package controls.messages;

/**
 * 
 * @author Manuel Merino Monge
 *
 */
public class EventInfo
{
	private String eventType = "";
	private Object eventInformation;
	
	private String idSource = "";

	/**
	 * Event information.
	 * 
	 * @param type 	-> Event identification
	 * @param info	-> Event information 
	 */
	private EventInfo( String type, Object info )
	{
		this.eventType = type;
		this.eventInformation = info;
	}
	
	/**
	 * 
	 * @param source -> Source ID
	 * @param type 	-> Event identification
	 * @param info	-> Event information
	 */
	public EventInfo( String source, String type, Object info )
	{
		this( type, info );
		
		this.idSource = source;
	}

	/**
	 * 
	 * @return Source ID
	 */
	public String getIdSource() 
	{
		return idSource;
	}
	
	/**
	 * 
	 * @param idSource -> new Source ID
	 */
	public void setIdSource( String idSource ) 
	{
		this.idSource = idSource;
	}
	
	/**
	 * 
	 * @return event identification
	 */
	public String getEventType()
	{
		return this.eventType;
	}

	/**
	 * 
	 * @return a Object with event information.
	 */
	public Object getEventInformation()
	{
		return this.eventInformation;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals( Object obj )
	{
		boolean equal = obj instanceof EventInfo;

		if (equal)
		{
			EventInfo inObj = (EventInfo)obj;

			equal = (inObj.getEventType().equals(this.eventType)) 
					&& (inObj.equals(this.eventInformation));
		}

		return equal;
	}

	@Override
	public String toString()
	{
		return "[" + this.eventType + " -> " + this.eventInformation + "]";
	}
}