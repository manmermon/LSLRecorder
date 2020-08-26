/*
 * Work based on IControlLevel1 of
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
package controls;

import controls.messages.EventInfo;

/**
 * A handler leader has to coordinate minion actions.  
 * 
 * @author Manuel Merino Monge
 *
 */
public abstract interface IHandlerSupervisor
{
	/**
	 * A subordinate indicates an event happened to the leader.
	 * 
	 * @param minion 	-> minion reference.
	 * @param event 	-> happened event.
	 */
	public abstract void eventNotification( IHandlerMinion minion, EventInfo event);
}