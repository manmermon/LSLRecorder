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

package InputStreamReader.OutputDataFile;

import InputStreamReader.OutputDataFile.DataBlock.DataBlock;
import StoppableThread.IStoppableThread;
import StoppableThread.Events.IStoppableThreadEventControl;

public abstract interface IOutputDataFileWriter extends IStoppableThread//, IStoppableThreadEventControl
{
	public void addHeader(String paramString1, String paramString2) throws Exception;

	/*
	public void saveData(String paramString, int[] paramArrayOfInt, int paramInt) throws Exception;

	public void saveData(String paramString, long[] paramArrayOfLong, int paramInt) throws Exception;

	public void saveData(String paramString, double[] paramArrayOfDouble, int paramInt) throws Exception;

	public void saveData(String paramString, float[] paramArrayOfFloat, int paramInt) throws Exception;

	public void saveData(String paramString, char[] paramArrayOfChar) throws Exception;

	public void saveData(String paramString1, String paramString2) throws Exception;
	*/
	
	public boolean isReady();
	
	public boolean saveData( DataBlock data )  throws Exception;

	public String getFileName();
	
	public boolean finished();

	//public void closeWriter() throws Exception;
}