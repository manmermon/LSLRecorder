/* 
 * Copyright 2018-2020 by Manuel Merino Monge <manmermon@dte.us.es>
 *  
 *   This file is part of LSLRec.  https://github.com/manmermon/LSLRecorder
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
package lslrec.dataStream.family.stream.lslrec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lslrec.dataStream.family.setting.IStreamSetting;
import lslrec.dataStream.family.setting.IStreamSetting.StreamLibrary;
import lslrec.dataStream.family.stream.lslrec.streamgiver.ByteStreamGiver;

/**
 * @author Manuel Merino Monge
 *
 */
public class LSLRecStream 
{
	private static Map< String, IStreamSetting > lslrecStreamsList = new HashMap< String, IStreamSetting >();
	private static Map< String, ByteStreamGiver > lslrecDataGiver = new HashMap< String, ByteStreamGiver >();
	
	public static void addDataStream( IStreamSetting datStream )
	{
		if( datStream != null && datStream.getLibraryID() == StreamLibrary.LSLREC )
		{
			lslrecStreamsList.put( datStream.uid(), datStream );
		}		
	}
	
	public static boolean setDataStreamGiver( String streamId, ByteStreamGiver g )
	{
		boolean add = false;
		
		if( g != null && lslrecStreamsList.containsKey( streamId ) )
		{
			lslrecDataGiver.put( streamId, g );
		}
		
		return add;
	}
	
	public static void removeDataStream( IStreamSetting datStream )
	{
		if( datStream != null )
		{
			lslrecStreamsList.remove( datStream.uid() );
			lslrecDataGiver.remove( datStream.uid() );
		}
	}
	
	public static void clearDataStream()
	{
		lslrecStreamsList.clear();
		lslrecDataGiver.clear();
	}
	
	public static LSLRecSimpleDataStream createDataStream( IStreamSetting sst )
	{
		LSLRecSimpleDataStream datStr = null;
		
		if( sst != null )
		{
			IStreamSetting st = lslrecStreamsList.get( sst.uid() );
			
			if( st != null )
			{
				datStr = new LSLRecSimpleDataStream( st );
				datStr.setDataStreamGiver( lslrecDataGiver.get( st.uid() ) );
			}
		}
		
		return datStr;
	}
	
	public static List< IStreamSetting > getRegisteredStreamSettings()
	{
		List< IStreamSetting > ss = new ArrayList<IStreamSetting>( lslrecStreamsList.values() );
		
		return ss;
	}
}
