/*
 * Work based on controlOfSubordinateTemplate class of 
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

package lslrec.controls;

import java.util.ArrayList;
import java.util.List;

import lslrec.controls.messages.EventInfo;
import lslrec.stoppableThread.AbstractStoppableThread;
import lslrec.stoppableThread.IStoppableThread;

public abstract class HandlerMinionTemplate extends AbstractStoppableThread implements IHandlerMinion
{	
	protected boolean isWorking = false; // Indicate if it is working

	protected List< IStoppableThread > subordiateList = new ArrayList< IStoppableThread >( ); // Subordinate list

	protected IHandlerSupervisor supervisor = null; // 

	protected EventInfo event = null;

	protected boolean blocking = false;

	/**
	 * Delete subordinate threads based on friendliness level.
	 *  
	 * @param friendliness
	 * - if friendliness < 0: stop execution when task is done.
	 * - if friendliness = 0: stop execution before the next loop interaction.
	 * - if friendliness > 0: interrupt immediately task and then execution is stopped.     
	 */
	public synchronized void deleteSubordinates( int friendliness )
	{
		this.isWorking = false;

		for( IStoppableThread subordinate : this.subordiateList )
		{
			if( subordinate != null )
			{
				subordinate.stopThread( friendliness );
			}
		}

		this.subordiateList.clear();

		this.cleanUpSubordinates();
	}

	/**
	 * Addictional actions to delete subordinates. 
	 */
	protected abstract void cleanUpSubordinates();

	/**
	 * Create and add new subordinate using input parameters.
	 * 
	 * @param parameters
	 */
	public synchronized void addSubordinates( MinionParameters parameters ) throws Exception
	{
		if( !this.isWorking )
		{
			this.subordiateList.addAll( this.createSubordinates( parameters ) );
		}
	}

	/**
	 * Work orders to subordinates if subordinates are not working.
	 *  
	 * @param info 
	 * 	Necessary information to work. 
	 */
	public void toWorkSubordinates( Object info ) throws Exception
	{		
		this.preToWorkSubordinates();

		this.isWorking = true;
		this.startWorking( info );

		this.postToWorkSubordinates();
	}

	/**
	 * Actions before to startWorking.
	 * @throws Exception
	 */
	protected void preToWorkSubordinates() throws Exception
	{

	}

	/**
	 * Actions after to startWorking.
	 * @throws Exception
	 */
	protected void postToWorkSubordinates() throws Exception
	{

	}

	/**
	 * 
	 */
	public boolean isWorking()
	{
		return this.isWorking;
	}

	/*
	 * (non-Javadoc)
	 * @see Controls.IControlLevel2#setControlSupervisor(Controls.IControlLevel1)
	 */
	public void setControlSupervisor( IHandlerSupervisor leader )
	{
		this.supervisor = leader;
	}

	/**
	 * Start work.
	 * 
	 * @param info: Necessary information to work.
	 */
	protected void startWorking( Object info ) throws Exception
	{
		if( !this.getBlockingStartWorking() )
		{
			AbstractStoppableThread t = new AbstractStoppableThread() 
			{			
				@Override
				protected void runInLoop() throws Exception 
				{				
					super.stopThread = true;
					startWork( info );
				}

				@Override
				protected void preStopThread(int friendliness) throws Exception 
				{			
				}

				@Override
				protected void postStopThread(int friendliness) throws Exception 
				{				
				}
			};

			t.startThread();
		}
		else
		{		
			startWork( info );
		}
	}

	/**
	 * Auxiliary: Start work.
	 * 
	 * @param info: Necessary information to work.
	 */
	protected abstract void startWork( Object info ) throws Exception;

	/**
	 * Create subordinates using input parameters.
	 * 
	 * @param parameters
	 */
	protected abstract List< IStoppableThread > createSubordinates( MinionParameters parameters ) throws Exception;	

	/**
	 * Set if startWorking( ... ) must be blocking or not;
	 */
	public void setBlockingStartWorking( boolean block )
	{
		this.blocking = block;
	}

	/**
	 * Return if startWorking( ... ) must be blocking or not;
	 */
	public boolean getBlockingStartWorking()
	{
		return this.blocking;
	}
}
