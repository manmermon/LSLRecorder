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
package dataStream.sync;

import edu.ucsd.sccn.LSL;

public class SyncMarker implements Comparable< SyncMarker >
{	
	public static final int MARK_DATA_TYPE = LSL.ChannelFormat.int32;
	public static final int MARK_TIME_TYPE = LSL.ChannelFormat.double64;
	
	public static final int NON_MARK = 0;
	
	
	private int markValue = NON_MARK;
	
	private double timeMarkValue = Double.NaN;
	
	public SyncMarker( int mark, double time )
	{
		this.markValue = mark;
		this.timeMarkValue = time;
	}
	
	public void addMarkValue( int val )
	{
		this.markValue = this.markValue | val;
	}
		
	public int getMarkValue() 
	{
		return markValue;
	}
	
	public double getTimeMarkValue() 
	{
		return timeMarkValue;
	}
	
	@Override
	public String toString() 
	{
		return "<" + this.markValue + ", " + this.timeMarkValue + ">";
	}

	@Override
	public int compareTo( SyncMarker o ) 
	{
		int eq = 0;
	
		if( this.timeMarkValue != Double.NaN && o.getTimeMarkValue() != Double.NaN )
		{
			if( this.timeMarkValue < o.getTimeMarkValue() )
			{
				eq = -1; 
			}
			else if( this.timeMarkValue > o.getTimeMarkValue() )
			{
				eq = 1;
			}
		}
		
		return eq;
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		boolean eq = ( obj != null ) && ( obj instanceof SyncMarker );
		
		if( eq )
		{
			SyncMarker mark = (SyncMarker) obj;
			
			eq = ( mark.getMarkValue() == this.markValue ) && ( mark.getTimeMarkValue() == this.timeMarkValue );
		}
		
		return eq;
	}
}
