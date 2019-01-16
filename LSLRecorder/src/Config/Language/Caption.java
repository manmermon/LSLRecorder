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

package Config.Language;

import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

public class Caption implements ICaption 
{
	private final String defaultCaptionID;
		
	private HashMap< String, String > caption = null;
	
	private String ID;
	
	/**
	 * 
	 * @param captionID: caption ID
	 * @param defaultLanguage: default language id. It must not be in java.util.Locale.getISOLanguages(). 
	 * @param txt: default caption
	 * @throws IllegalArgumentException: If defaultLanguage or captionID is empty or null. 
	 */
	public Caption( String captionID, String defaultLanguage, String txt ) throws IllegalArgumentException
	{
		checkLanguage( captionID );
		checkLanguage( defaultLanguage );
		
		for( String lang : Locale.getISOLanguages() )
		{
			if( lang.equals( defaultLanguage ) )
			{
				throw new IllegalArgumentException( "Default language Id is in java.util.Locale.getISOLanguages()." );
			}			
		}
		
		this.caption = new HashMap< String, String >();
		
		this.ID = captionID;
		
		this.defaultCaptionID = defaultLanguage;		
		
		if( txt == null )
		{
			txt = "";
		}
		
		this.caption.put( defaultLanguage, txt );
	}
	
	/**
	 * 
	 * @param language: caption language.
	 * @return caption. If language is not registered, then default caption is returned. 
	 */
	public String getCaption( String language )
	{
		if( language == null )
		{
			language = this.defaultCaptionID;
		}
		
		language = language.trim();
		
		String lbl = this.caption.get( language );
		
		return lbl;
	}
	
	/**
	 * Set parameter caption for a language. This is added if there is not caption for this language.  
	 * @param language: parameter language
	 * @param Caption: parameter caption
	 * @throws IllegalArgumentException: If language is not in java.util.Locale.getISOLanguages() or is empty/null.  
	 */
	public void setCaption( String Language, String Caption ) throws IllegalArgumentException
	{
		Language = Language.toLowerCase();
		
		checkLanguage( Language );
		
		boolean ok = false;
		
		Language = Language.trim();
		
		for( Locale lang : Locale.getAvailableLocales() )
		{
			ok = lang.toString().toLowerCase().equals( Language );
			
			if( ok )
			{
				this.caption.put( Language, Caption );
				break;
			}
		}
		
		if( !ok )
		{
			throw new IllegalArgumentException( "Caption language (" + Language + ") is not in java.util.Locale.getAvailableLocales()." );
		}
	}
	
	/**
	 * Check language id
	 * 
	 * @param id String with parameter identifier
	 * @throws IllegalArgumentException: id is null or empty
	 */
	private void checkLanguage( String id ) throws IllegalArgumentException
	{
		if( id == null 
				|| id.trim().isEmpty() )
		{
			throw new IllegalArgumentException( "Input parameter is null or empty." );
		}
	}

	/**
	 * 
	 * @return parameter identifier
	 */
	public String getID()
	{
		return this.ID;
	}
	
	/**
	 * 
	 * @param id: caption identifier
	 */
	public void setID( String id )
	{
		this.ID = id;
	}
	
	/**
	 * 
	 * @return Set of identifiers of avaibled languages
	 */
	public Set< String > getLanguages()
	{
		return this.caption.keySet();
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() 
	{
		return this.caption.toString();
	}
}
