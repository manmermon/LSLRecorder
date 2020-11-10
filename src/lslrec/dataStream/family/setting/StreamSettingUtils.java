/**
 * 
 */
package lslrec.dataStream.family.setting;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;

/**
 * @author Manuel Merino Monge
 *
 */
public class StreamSettingUtils 
{
	public enum StreamDataType { undefined, /** Can not be transmitted. */
		
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
	
	public static String addElementToXml( String xml, String nodeRoot, String name, String value )
	{			
		if( xml != null && nodeRoot != null && name != null )
		{
			Document doc = ConvertTo.Transform.xmlStringToXMLDocument( xml );

			if( doc != null )
			{
				NodeList nodes = doc.getElementsByTagName( nodeRoot );
				
				if( nodes.getLength() > 0 )
				{
					try 
				    {
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
				    }
				    catch( Exception e )
				    {		    	
				    }
				}
			}
		}
		
		return xml;
	}	
}
