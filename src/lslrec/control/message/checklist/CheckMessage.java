/* 
 * Copyright 20026 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.control.message.checklist;

import java.util.ArrayList;
import java.util.List;

import lslrec.auxiliar.WarningMessage;

public class CheckMessage 
{
	public static final int ERROR = -1;
	public static final int INFO = 0;
	public static final int WARNING = 1;
	
	public static final int NO_REMOVABLE_MESSAGE = 0;
	public static final int REMOVABLE_MESSAGE = 1;
	
	public static final char toStrSeparator = ';';
	
	private boolean enable = true;
	private List< ICheckMessagePart > msgParts = new ArrayList< ICheckMessagePart >();
	private String idMessage = "";
		
	private MessageEvaluator evaluator = null;
	
	private int type = INFO;
	
	private int removalbeMsg = NO_REMOVABLE_MESSAGE;
	
	private boolean editable = false;
	
	private String description = null;
	
	private String alternativeMsg = "";
	
	public CheckMessage( String idMsg, int messageType, int removableMessage ) 
	{		
		this.idMessage = ( idMsg == null ) ? "" : idMsg;
		this.type = messageType;
		this.removalbeMsg = removableMessage;
	}
	
	public CheckMessage( String idMsg, int messageType ) 
	{		
		this( idMsg, messageType, NO_REMOVABLE_MESSAGE );
	}
	
	public void setAlternativeMessage( String alt )
	{
		this.alternativeMsg = alt;
	}
	
	public String getMessage2Show() 
	{
		String alt = this.alternativeMsg;
		
		if( alt == null || alt.trim().isEmpty() )
		{
			alt = this.getMessage();
		}
		
		return alt;
	}
	
	/**
	 * 
	 * @return 
	 */
	public boolean isRemovableMsg() 
	{
		return this.removalbeMsg != NO_REMOVABLE_MESSAGE;
	}
		
	public void addMessagePart( ICheckMessagePart part )
	{
		if( part != null )
		{
			this.msgParts.add( part );
			
			this.editable = this.editable || part.isPartEditable();
		}
	}	
	
	public List< ICheckMessagePart > getMessagePart( )
	{
		return this.msgParts;
	}
	
	public void setEnable( boolean enable ) 
	{
		this.enable = enable;
	}
	
	public boolean isEnable() 
	{
		return this.enable;
	}
	
	public String getMessage()
	{
		String msg = "";
		
		for( ICheckMessagePart part : msgParts )
		{
			msg += part.getMessagePart() + " ";
		}
		
		if( !msg.isEmpty() )
		{
			msg = msg.substring( 0, msg.length() - 1 );
		}
		
		return msg;
	}
	
	public String getMessageID() 
	{
		return this.idMessage;
	}
	
	public void setDescription( String desc )
	{
		this.description = desc;
	}

	public String getDescription()
	{
		return ( this.description == null || this.description.isEmpty() ) ? this.getMessage() : this.description;
	}
	
	public void setMessageEvaluator( MessageEvaluator eval )
	{
		this.evaluator = eval;
	}
	
	public WarningMessage evaluateMessage()
	{
		int type = WarningMessage.OK_MESSAGE;
		
		if( this.type == ERROR )
		{
			type = WarningMessage.ERROR_MESSAGE;
		}
		else if( this.type == WARNING )
		{
			type = WarningMessage.WARNING_MESSAGE;
		}
		
		WarningMessage msg = new WarningMessage( this.getMessage2Show(), type );
		
		if( this.evaluator != null )
		{
			if( !evaluator.evalue() )
			{	
				//msg.setMessage( this.getMessage(), type);
				msg.setMessage( this.getMessage2Show(), type);
			}
			else
			{
				msg = new WarningMessage();
			}
		}
		
		return msg;
	}
	
	public boolean isEditableMessage()
	{
		return this.editable;
	}
	
	@Override
	public String toString() 
	{
		String unicode = String.format("\\u%04X", (int)toStrSeparator);
		String str = "[" + this.idMessage.replace( String.valueOf( toStrSeparator ), unicode) 
							+ toStrSeparator
							+ this.type
							+ toStrSeparator
							+ this.enable 
						 	+ toStrSeparator
						 	+ (( this.description != null ) ? this.description : "")
						 	+ toStrSeparator	
						 	+ (( this.alternativeMsg != null ) ? this.alternativeMsg : "")
						 	+ toStrSeparator;

		if( this.msgParts != null )
		{
			for( ICheckMessagePart part : this.msgParts ) 
			{
				str += part.toString().replace( String.valueOf( toStrSeparator ), unicode ) 
						+ toStrSeparator;
			}
		}
		
		str = str.substring( 0, str.length() - 1 );
		
		str += "]";		
		
		return str;
	}
}
