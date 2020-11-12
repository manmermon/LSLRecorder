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
package lslrec.dataStream.writingSystemTester;

import java.util.ArrayList;
import java.util.List;

import lslrec.auxiliar.extra.Tuple;
import lslrec.config.ConfigApp;
import lslrec.controls.messages.EventInfo;
import lslrec.controls.messages.EventType;
import lslrec.dataStream.binary.input.writer.TemporalOutDataFileWriter;
import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;

public class WritingTest extends TemporalOutDataFileWriter 
{
	private List< Long > times;
	private long initTime;
		
	public WritingTest( IStreamSetting lslCfg, OutputFileFormatParameters format, int Number) throws Exception 
	{
		super( lslCfg, format, Number);
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
		if( !ConfigApp.isTesting() && !super.file.delete() )
		{
			super.file.deleteOnExit();
		}
		
		EventInfo event = new EventInfo( super.getID(), EventType.TEST_WRITE_TIME, new Tuple< String, List< Long >>( super.streamSetting.name(), this.times ) );
		
		this.notifTask.addEvent( event );
		synchronized ( this.notifTask )
		{
			this.notifTask.notify();
		}
	}
	
}
