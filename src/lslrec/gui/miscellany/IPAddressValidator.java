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
package lslrec.gui.miscellany;

import java.util.regex.Pattern;
 
public class IPAddressValidator{

    private static final String IPADDRESS_PATTERN = 
		"^((\\s)*[01]?\\d\\d?|2[0-4]\\d|25[0-5])(\\s)*\\." +
		"(\\s)*([01]?\\d\\d?|2[0-4]\\d|25[0-5])(\\s)*\\." +
		"(\\s)*([01]?\\d\\d?|2[0-4]\\d|25[0-5])(\\s)*\\." +
		"(\\s)*([01]?\\d\\d?|2[0-4]\\d|25[0-5])(\\s)*$";
	  
   /**
    * Validate ip address with regular expression
    * @param ip ip address for validation
    * @return true valid ip address, false invalid ip address
    */
    public static boolean validate( final String ip)
    {
    	return Pattern.compile( IPADDRESS_PATTERN ).matcher( ip ).matches();	    	    
    }
}