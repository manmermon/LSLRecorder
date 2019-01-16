/*
 * Work based on CLIS by Manuel Merino Monge <https://github.com/manmermon/CLIS>
 * 
 * Copyright 2011-2018 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of CLIS and LSLRec.
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

package Auxiliar.Tasks;

import java.util.List;

import Controls.Messages.EventInfo;

/**
 * 
 * @author Manuel Merino Monge
 *
 */
public interface INotificationTask 
{
	/**
	 * Setting task monitor.
	 *  
	 * @param monitor: task monitor
	 */
	public void taskMonitor( ITaskMonitor monitor );
		
	
	/**
	 * To get the task result. 
	 * 
	 * @return task result.
	 */
	public List< EventInfo > getResult();
	
	/**
	 * Erase task result.
	 */
	public void clearResult();
	
	/**
	 * 
	 * @return task ID
	 */
	public String getID();

}
