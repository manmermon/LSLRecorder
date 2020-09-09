package testing.DataStream.Sync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import DataStream.Sync.SyncMarker;

public class testSortSyncMarkers {

	public static void main(String[] args) 
	{
		List< SyncMarker > lst = new ArrayList<SyncMarker>();
		lst.add( new SyncMarker( 5, 1D ) );
		lst.add( new SyncMarker( 5, 0.5D ) );
		lst.add( new SyncMarker( 5, 1.5D ) );
		lst.add( new SyncMarker( 4, 1D ) );
		
		System.out.println( "Orignal: " + lst );
		Collections.sort( lst );
		
		System.out.println( "Sorted: " + lst );
	}

}
