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

package Auxiliar;

public class WarningMessage
{
  public static final int ERROR_MESSAGE = -1;
  public static final int OK_MESSAGE = 0;
  public static final int WARNING_MESSAGE = 1;
  
  private String msg;
  private int warningType;
    
  
  /**
   * Message with control information. Default OK_MESSAGE and empty information.
   */
  public WarningMessage()
  {
    this.msg = "";
    this.warningType = 0;
  }
  
  /**
   * 
   * @return Warning message.
   */
  public String getMessage()
  {
    return this.msg;
  }
  
  /**
   * Set warning message.
   * 
   * @param message -> information
   * @param type -> warning type: error (< 0), ok (=0), or warning (>0).
   * 		 
   */
  public void setMessage(String message, int type)
  {
    this.msg = message;
    this.warningType = type;    
  }
  
  /**
   * Add warning message.
   * 
   * @param message -> information
   * @param type -> warning type: error (< 0), ok (=0), or warning (>0). 
   * 		If previous type is not OK, the serious is preserved.
   * 		 
   */
  public void addMessage( String message, int type )
  {
	  if( !this.msg.isEmpty() )
	  {
		  this.msg += "\n"; 
	  }
	  
	  this.msg += message;
	  
	  if (this.warningType == OK_MESSAGE )
	  {
		  this.warningType = type;
	  }
	  else if ((this.warningType > OK_MESSAGE) && (type != OK_MESSAGE ) )
	  {
		  this.warningType = type;
	  }
  }

  /**
   * 
   * @return warning type. 
   */
  public int getWarningType()
  {
    return this.warningType;
  }
  
  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
	public String toString() 
  {	
		return "<" + this.warningType + ", " + this.msg + ">";
	}
}