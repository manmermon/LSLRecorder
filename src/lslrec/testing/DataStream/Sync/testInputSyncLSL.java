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
package testing.DataStream.Sync;

import testing.AppRunning.SyncStream.testSyncLSL;

public class testInputSyncLSL {

	public static void main(String[] args) 
	{
		try 
		{	
			long t = 100L;
			
			double f = 1000D / t;
			
			//testSyncLSL test = new testSyncLSL( 0, 1000L,3600, 1 );
			double tiempo = 60; // minutos
			tiempo = 20;
			//tiempo = 0.5;
			testSyncLSL test = new testSyncLSL( 0, t, (int)( tiempo * 60 * f ), 1 );
			
			test.startThread();
			test.join();
		}
		catch (Exception e) {
			// TODO: handle exception
		}
	}
}
