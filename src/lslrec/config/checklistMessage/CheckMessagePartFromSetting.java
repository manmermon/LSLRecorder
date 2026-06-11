/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2011-2026 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.config.checklistMessage;

import lslrec.config.ConfigApp;

public class CheckMessagePartFromSetting implements ICheckMessagePart
{
	private String idConfigParamenter = "";
	
	public CheckMessagePartFromSetting( String idCfgPar ) 
	{
		this.idConfigParamenter = idCfgPar;
	}

	@Override
	public void setIDLangCaption(String id) 
	{	
	}

	@Override
	public boolean isPartEditable() 
	{
		return false;
	}

	@Override
	public String getMessagePart() 
	{
		Object par = ConfigApp.getProperty( this.idConfigParamenter );
		
		String val = ( par != null ) ? par.toString() : "";
		
		return val;
	}

	@Override
	public boolean setMessagePart( String noTx ) 
	{		
		return true;
	}

	@Override
	public String toString() 
	{
		String str = "<" 
				+ TypeMessagePart.Setting.name() + toStrSeparator
				+ this.isPartEditable() + this.toStrSeparator
				+ "" + this.toStrSeparator
				+ "" + this.toStrSeparator;
		
		String unicode = String.format("\\u%04X", (int) this.toStrSeparator);
		str += this.idConfigParamenter.replace( String.valueOf( this.toStrSeparator ), unicode);
		
		str += ">";
		
		return str;
	}
}
