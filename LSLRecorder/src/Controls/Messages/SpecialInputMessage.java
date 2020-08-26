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
package controls.messages;

import java.util.Comparator;

import config.language.Language;

public class SpecialInputMessage implements Comparable< SpecialInputMessage >, Comparator< SpecialInputMessage > 
{
	private int markValue;
	private String markText;
	
	private String markLegend = "";
	
	private boolean special = false;
	
	public SpecialInputMessage( int mark, String text )
	{
		this.markText = text;
		this.markValue = mark;
	}
	
	public void setSpecial(boolean special) 
	{
		this.special = special;
	}
	
	public boolean isSpecial() 
	{
		return special;
	}
	
	public int getMarkValue() 
	{
		return markValue;
	}
	
	public void setMarkValue( int markValue ) 
	{
		this.markValue = markValue;
	}
	
	public String getMarkText() 
	{
		return markText;
	}
	
	public void setMarkText( String markText ) 
	{
		this.markText = markText;
	}
	
	public void setMarkLegendToken( String langToken )
	{
		this.markLegend = langToken;
	}
	
	public String getMarkLegend() 
	{
		return Language.getLocalCaption( this.markLegend );
	}
	
	@Override
	public boolean equals( Object obj ) 
	{
		boolean eq = (obj instanceof SpecialInputMessage );
		
		if( !eq )
		{
			eq = this.markValue == ((SpecialInputMessage)obj).getMarkValue()
					&& this.markText.equals( ((SpecialInputMessage)obj).getMarkText() );
		}
		
		return eq;
	}

	@Override
	public int compareTo(SpecialInputMessage o) 
	{
		int pos = 0;
		
		if( !this.equals( o ) )
		{
			pos = this.markValue - o.getMarkValue();
			if( pos == 0 )
			{
				pos = this.markText.compareTo( o.getMarkText() );
			}
		}
		
		return 0;
	}
	
	@Override
	public int hashCode() 
	{
		return ( this.markText + this.markValue ).hashCode();
	}

	@Override
	public int compare(SpecialInputMessage o1, SpecialInputMessage o2) 
	{
		return o1.compareTo( o2 );
	}
}
