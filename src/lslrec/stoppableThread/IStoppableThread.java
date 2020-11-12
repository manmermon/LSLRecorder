/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
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

public interface IStoppableThread extends Runnable
{
	public final int ERROR_STOP = -2;
	public final int STOP_WITH_TASKDONE = -1;
	public final int STOP_IN_NEXT_LOOP = 0;
	public final int FORCE_STOP = 1;	
	
	
	/**
     * Stop thread execution. 
     * 
     * @param friendliness:
     * - if friendliness < -1: interrupt immediately task and then execution is stopped. An exception is thrown.
     * - if friendliness = -1: stop execution when task is done.
     * - if friendliness = 0: stop execution before the next loop interaction.
     * - if friendliness > 0: interrupt immediately task and then execution is stopped.     
     */
    public void stopThread( int friendliness );
    
    /**
     * Start execution.
     */
    public void startThread() throws Exception;    
}
