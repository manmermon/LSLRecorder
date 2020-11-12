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
package lslrec.stoppableThread;

/**
 * @author Manuel Merino Monge
 *
 */
public class ThreadStopException extends Exception 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7542822226826638015L;

	public ThreadStopException( )
	{
		super( "Thread stop causes by an error." );
	}
	
	public ThreadStopException( String msg )
	{
		super( msg );
	}
}