package gui.miscellany;

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