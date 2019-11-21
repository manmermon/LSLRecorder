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
package InputStreamReader.OutputDataFile.DataBlock;

public abstract class DataBlock<T> 
{
	private int seqNum;
	
	private int type;
	
	private String name;
	
	private int numCols;
	
	private T[] Data;
		
	public DataBlock( int seqNumber, String name, int dataType, int nCols , T[] data) 
	{
		this.seqNum = seqNumber;
		this.name = name;
		this.type = dataType;
		this.numCols = nCols;		
		this.Data = data;
	}
	
	public String getName() 
	{
		return name;
	}
	
	public int getDataType() 
	{
		return type;
	}
	
	public int getNumCols() 
	{
		return numCols;
	}
	
	public T[] getData()
	{
		return this.Data;
	}
	
	public int getSeqNum() 
	{
		return seqNum;
	}
}
