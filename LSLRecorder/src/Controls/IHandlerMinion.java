/*
 * Work based on IControlLevle2 interface of 
 * CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
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

package Controls;

import Auxiliar.WarningMessage;
import java.util.Map;

/**
 * A hand
 * @author Manuel Merino Monge
 *
 */
public abstract interface IHandlerMinion
{  
	public final int STOP_WITH_TASKDONE = -1;
	public final int STOP_IN_NEXT_LOOP = 0;
	public final int FORCE_STOP = 1;
	
	/**
	 * Add a new subordinate. This is managed by handler minion.
	 * 
	 * @param pars	-> settings
	 * @throws Exception -> If an incorrect setting is detected. 
	 */
	public abstract void addSubordinates( MinionParameters pars ) throws Exception;

	/**
	 * Delete all subordinates.
	 * 
	 * @param friendliness:
	 * - if friendliness < 0: stop execution when task is done.
     * - if friendliness = 0: stop execution before the next loop interaction.
     * - if friendliness > 0: interrupt immediately task and then execution is stopped.     
	 */
	public abstract void deleteSubordinates( int friendliness );

	/*
	public abstract void setSubordinates( String paramString, Object paramObject ) throws Exception;
	 */
	
	/**
	 * Launch subordinates.
	 * 
	 * @param paramObject
	 * @throws Exception
	 */
	public abstract void toWorkSubordinates( Object paramObject ) throws Exception;

	/**
	 * Set handler supervisor to notify events.
	 * 
	 * @param leader
	 */
	public abstract void setControlSupervisor( IHandlerSupervisor leader );

	/**
	 * Indicate if the minion is working.
	 * @return boolean 
	 */
	public abstract boolean isWorking();

	/*
	public abstract void setBlockingStartWorking( boolean paramBoolean );
	 */

	/*
	public abstract boolean getBlockingStartWorking();
	*/

	/**
	 * Check if settings are corrects.
	 * 
	 * @return WarningMessage
	 */
	public abstract WarningMessage checkParameters();
}