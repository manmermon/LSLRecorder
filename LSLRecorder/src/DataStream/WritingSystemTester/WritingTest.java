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
package DataStream.WritingSystemTester;

import java.util.ArrayList;
import java.util.List;

import Auxiliar.Extra.Tuple;
import Controls.Messages.EventInfo;
import Controls.Messages.EventType;
import DataStream.Binary.TemporalOutDataFileWriter;
import edu.ucsd.sccn.LSLConfigParameters;
import edu.ucsd.sccn.LSL.StreamInfo;

public class WritingTest extends TemporalOutDataFileWriter 
{
	private List< Long > times;
	private long initTime;
		
	public WritingTest(String filePath, StreamInfo info, LSLConfigParameters lslCfg, int Number) throws Exception 
	{
		super(filePath, info, lslCfg, Number);
	}	
	
	@Override
	protected void preStart() throws Exception 
	{
		super.preStart();
		
		this.times = new ArrayList< Long >();
	}

	@Override
	protected void startUp() throws Exception 
	{
		super.startUp();
	}
	
	@Override
	protected void managerData(byte[] data, byte[] times) throws Exception 
	{
		this.initTime = System.nanoTime();
		
		super.managerData( data, times );
		
		this.times.add( (System.nanoTime() - this.initTime ) );	
	}
		
	@Override
	protected void postCleanUp() throws Exception 
	{
		if( !super.file.delete() )
		{
			super.file.deleteOnExit();
		}
		
		EventInfo event = new EventInfo( EventType.TEST_WRITE_TIME, new Tuple< String, List< Long >>( super.LSLName, this.times ) );

		this.events.add(event);
		
		if (this.monitor != null)
		{
			this.monitor.taskDone(this);
		}
	}
	
}
