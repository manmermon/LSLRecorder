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
 *   
 */
package lslrec.dataStream.family.lsl;

import java.nio.charset.Charset;
import java.util.List;

import lslrec.auxiliar.extra.StringTuple;
import lslrec.dataStream.family.lsl.LSL.StreamInfo;
import lslrec.dataStream.family.lsl.LSL.XMLElement;

public class LSLUtils 
{
	 /**
     * Data format of a channel (each transmitted sample holds an array of channels).
     */
	
	/*
	 * 
	 * Static variable from:
	 *  https://github.com/labstreaminglayer/liblsl-Java/blob/39799dae02edf34e138d2a67ae768dc38a0248a9/src/edu/ucsd/sccn/LSL.java
	 * 
	 */
	
	public static final int float32 = 1;    /** For up to 24-bit precision measurements in the appropriate physical unit
	 *  (e.g., microvolts). Integers from -16777216 to 16777216 are represented accurately. */
	public static final int double64 = 2;   /** For universal numeric data as long as permitted by network & disk budget.
	 *  The largest representable integer is 53-bit. */
	public static final int string = 3; /** For variable-length ASCII strings or data blobs, such as video frames,
	 *  complex event descriptions, etc. */
	public static final int int32 = 4;  /** For high-rate digitized formats that require 32-bit precision. Depends critically on
	 *  meta-data to represent meaningful units. Useful for application event codes or other coded data. */
	public static final int int16 = 5;      /** For very high rate signals (40KHz+) or consumer-grade audio
	 *  (for professional audio float is recommended). */
	public static final int int8 = 6;       /** For binary signals or other coded data.
	 *  Not recommended for encoding string data. */
	public static final int int64 = 7;      /** For now only for future compatibility. Support for this type is not yet exposed in all languages.
	 *  Also, some builds of liblsl will not be able to send or receive data of this type. */
	public static final int undefined = 0;  /** Can not be transmitted. */
	
	public static String getAdditionalInformationLabelInXml()
	{
		return "desc";
	}

	public static int getDataTypeByClass( Object classType )
	{
		int dataType = undefined;
		
		if( classType instanceof Double )
		{
			dataType = double64;			
		}
		else if( classType instanceof Float )
		{
			dataType = float32;
		}
		else if( classType instanceof Long )
		{
			dataType = int64;
		}
		else if( classType instanceof Integer )
		{
			dataType = int32;
		}
		else if( classType instanceof Short )
		{
			dataType = int16;
		}
		else if( classType instanceof Byte )
		{
			dataType = int8;
		}
		else if( classType instanceof String )
		{
			dataType = string;
		}
		else if ( classType instanceof Character )
		{
			dataType = string;
		}
		
		return dataType;
	}
	
	public static int getDataTypeBytes( int type )
	{
		int len = -1;
		
		switch ( type ) 
		{
			case double64:
			{
				len = Double.BYTES;
				break;
			}
			case float32:
			{
				len = Float.BYTES;
				break;
			}
			case string:
			{
				//len = Character.BYTES;
				
				Charset c = Charset.forName( "UTF-8" );
				
				len = ( "A" ).getBytes( c ).length;
				break;
			}
			case int8:
			{
				len = Byte.BYTES;
				break;
			}
			case int16:
			{
				len = Short.BYTES;
				break;
			}
			case int32:
			{
				len = Integer.BYTES;
				break;
			}			
			case int64:
			{
				len = Long.BYTES;
				break;
			}
			default: //undefined
			{
				len = -1;
				
				break;
			}
		}
		
		return len;
	}	

	public static int getTimeMarkBytes( )
	{
		return getDataTypeBytes( getTimeMarkType() );
	}
	
	public static int getTimeMarkType( )
	{
		return double64;
	}
	
	public static int numberOfRepeatedtNodoName( XMLElement child , String label )
	{		
		int countEq = 0;
		while( child != null && !child.name().isEmpty() )
		{
			String name = child.name().toLowerCase();
			if( name.equals( label ) )
			{
				countEq++;
			}
			
			child = child.next_sibling();
		}					
		
		return countEq;
	}
	
	public static XMLElement findFirstXMLNode( XMLElement desc, String NodeName )
	{		
		XMLElement child = desc.first_child();
		if( child != null )
		{
			String name = child.name().toLowerCase();
			
			if( !name.isEmpty() )
			{
				if( !name.equals( NodeName.toLowerCase() ) )
				{
					child = findFirstXMLNode( child.next_sibling(), NodeName );
				}
			}
		}
		
		return child;
	}
	
	public static void removeNode( StreamInfo stream, String childNode )
	{
		if( stream != null )
		{
			XMLElement parent = stream.desc();
										
			parent.remove_child( childNode );
		}
	}
	
	public static void addNode( StreamInfo stream, StringTuple child )
	{
		if( child != null && stream != null )
		{
			XMLElement parent = stream.desc();

			parent.append_child_value( child.t1, child.t2 );
		}
	}
	
	public static void addNodes( StreamInfo stream, List< StringTuple > childNodes )
	{
		if( childNodes != null && stream != null )
		{
			for( StringTuple child : childNodes )
			{
				addNode( stream, child );
			}
		}
	}
}
