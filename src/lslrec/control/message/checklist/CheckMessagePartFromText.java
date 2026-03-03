/* 
 * Copyright 2026 by Manuel Merino Monge <manmermon@dte.us.es>
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

import java.util.regex.Pattern;

public class CheckMessagePartFromText implements ICheckMessagePart
{
	private String part = "";
 	private Pattern pattern = null; 
	private boolean partEditable = true;

	private String idLangCaption = null;
	
	public CheckMessagePartFromText( String tx, String regExp, boolean editable ) 
	{
		this.part = (tx != null) ? tx : "";
		this.partEditable = editable;
		
		regExp = ( regExp != null ) ? regExp : "";
		
		if( !regExp.isEmpty() )
		{
			this.pattern = Pattern.compile( regExp );
		}
	}
	
	@Override
	public void setIDLangCaption( String id )
	{
		this.idLangCaption = id;
	}
	
	@Override
	public boolean isPartEditable() 
	{
		return this.partEditable;
	}
	
	@Override
	public String getMessagePart() 
	{
		return this.part;
	}
	
	/**
	 * @param newTx
	 * @throws UnsupportedOperationException if is no editable.
	 */
	@Override
	public boolean setMessagePart( String newTx )
	{
		boolean res = true;
		String tx = (newTx == null ) ? "" : newTx;
		
		if( !this.partEditable )
		{
			throw new UnsupportedOperationException( "Message part is no editable." );
		}
				
		if( this.pattern != null )
		{
			 res = this.pattern.matcher( tx ).matches();
		}
		
		if( res )
		{
			this.part = tx;
		}
		
		return res;
	}
	
	@Override
	public String toString() 
	{
		String str = "<" 
				+ TypeMessagePart.Text.name() + toStrSeparator
				+ this.partEditable + this.toStrSeparator
				+ ( (this.idLangCaption == null ) ? "" : this.idLangCaption )+ this.toStrSeparator
				+ ( (this.pattern != null ) ? this.pattern.pattern() : "" ) + this.toStrSeparator;
		
		String unicode = String.format("\\u%04X", (int) this.toStrSeparator);
		str += this.part.replace( String.valueOf( this.toStrSeparator ), unicode);
		
		str += ">";
		
		return str;
	}
}
