/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
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
package lslrec.dataStream.tools;

import java.io.StringWriter;
import java.nio.charset.Charset;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting.StreamLibrary;
import lslrec.dataStream.family.stream.lsl.LSLStreamInfo;
import lslrec.dataStream.family.stream.lsl.LSL.StreamInlet;

/**
 * @author Manuel Merino Monge
 *
 */
public class StreamUtils 
{
	public enum StreamDataType 
	{ 
		
		undefined, /** Can not be transmitted. */
		
		float32,    /** For up to 24-bit precision measurements in the appropriate physical unit
		 			*  (e.g., microvolts). Integers from -16777216 to 16777216 are represented accurately. */

		double64,   /** For universal numeric data as long as permitted by network & disk budget.
		 			*  The largest representable integer is 53-bit. */
		
		string,		/** For variable-length ASCII strings or data blobs, such as video frames,
		 			*  complex event descriptions, etc. */
		
		int32,  	/** For high-rate digitized formats that require 32-bit precision. Depends critically on
		 			*  meta-data to represent meaningful units. Useful for application event codes or other coded data. */
		
		int16,      /** For very high rate signals (40KHz+) or consumer-grade audio
		 			*  (for professional audio float is recommended). */
		
		int8,      /** For binary signals or other coded data.
		 			*  Not recommended for encoding string data. */
		
		int64      /** For now only for future compatibility. Support for this type is not yet exposed in all languages.
		 			*  Also, some builds of liblsl will not be able to send or receive data of this type. */
	};

	public static StreamDataType getDataTypeByClass( Object classType )
	{
		StreamDataType dataType = StreamDataType.undefined;
		
		if( classType instanceof Double )
		{
			dataType = StreamDataType.double64;			
		}
		else if( classType instanceof Float )
		{
			dataType = StreamDataType.float32;
		}
		else if( classType instanceof Long )
		{
			dataType = StreamDataType.int64;
		}
		else if( classType instanceof Integer )
		{
			dataType = StreamDataType.int32;
		}
		else if( classType instanceof Short )
		{
			dataType = StreamDataType.int16;
		}
		else if( classType instanceof Byte )
		{
			dataType = StreamDataType.int8;
		}
		else if( classType instanceof String )
		{
			dataType = StreamDataType.string;
		}
		else if ( classType instanceof Character )
		{
			dataType = StreamDataType.string;
		}
		
		return dataType;
	}
	
	public static int getDataTypeBytes( StreamDataType type )
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
	
	public static String addElementToXmlStreamDescription( String xml, String nodeRoot, String name, String value )
	{			
		if( xml != null && nodeRoot != null && name != null )
		{
			Document doc = ConvertTo.Transform.xmlStringToXMLDocument( xml );

			if( doc != null )
			{
				NodeList nodes = doc.getElementsByTagName( nodeRoot );
				
				try 
			    {
					if( nodes.getLength() > 0 )
					{
						/*
						Element newElement = doc.createElement( name );
						newElement.appendChild( doc.createTextNode( value ) );

						Node node = nodes.item( 0 );
						node.appendChild( newElement );

						TransformerFactory tf = TransformerFactory.newInstance();
					    Transformer transformer;

				        transformer = tf.newTransformer();
				        transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes");

				        StringWriter writer = new StringWriter();

				        //transform document to string 
				        transformer.transform(new DOMSource( doc ), new StreamResult( writer ) );

				        xml = writer.getBuffer().toString();
						 */

						xml = createNode( doc, nodes, name, value );
					}
			    }
			    catch( Exception e )
			    {		    	
			    }
			}
		}
		
		return xml;
	}	
	
	private static String createNode( Document doc, NodeList nodes, String name, String value ) throws TransformerException
	{
		Element newElement = doc.createElement( name );
		newElement.appendChild( doc.createTextNode( value ) );
		
		Node node = nodes.item( 0 );
		node.appendChild( newElement );
	
		TransformerFactory tf = TransformerFactory.newInstance();
	    Transformer transformer;
    
        transformer = tf.newTransformer();
        transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty( OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", ""+( 4 * (nodes.getLength() + 1 ) ) );
        
        StringWriter writer = new StringWriter();
        
        //transform document to string 
        transformer.transform(new DOMSource( doc ), new StreamResult( writer ) );
 
        return writer.getBuffer().toString(); 
	}

	public static String getDeepXmlStreamDescription( IStreamSetting streamsetting )
	{
		String xml = "";
		
		if( streamsetting != null )
		{
			IStreamSetting stream = streamsetting;
			
			try 
			{
				if( streamsetting.getLibraryID() == StreamLibrary.LSL )
				{
					StreamInlet in = new StreamInlet( (LSLStreamInfo)stream );

					xml = in.info().description();
				}
				else if( streamsetting.getLibraryID() == StreamLibrary.LSLREC )
				{
					xml = streamsetting.description();
				}
			}
			catch ( Exception e) 
			{
				xml = streamsetting.description();
			}
		}
		
		return xml;
	}

}
