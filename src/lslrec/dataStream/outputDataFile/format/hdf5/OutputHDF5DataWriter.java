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
package lslrec.dataStream.outputDataFile.format.hdf5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.auxiliar.tasks.ITaskMonitor;
import lslrec.dataStream.outputDataFile.dataBlock.ByteBlock;
import lslrec.dataStream.outputDataFile.dataBlock.CharBlock;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlock;
import lslrec.dataStream.outputDataFile.dataBlock.DoubleBlock;
import lslrec.dataStream.outputDataFile.dataBlock.FloatBlock;
import lslrec.dataStream.outputDataFile.dataBlock.IntegerBlock;
import lslrec.dataStream.outputDataFile.dataBlock.LongBlock;
import lslrec.dataStream.outputDataFile.dataBlock.ShortBlock;
import lslrec.dataStream.outputDataFile.dataBlock.StringBlock;
import lslrec.dataStream.outputDataFile.format.IOutputDataFileWriter;
import ch.systemsx.cisd.hdf5.HDF5Factory;
import ch.systemsx.cisd.hdf5.IHDF5Writer;

public class OutputHDF5DataWriter implements IOutputDataFileWriter 
{
	private IHDF5Writer writer = null;
	private Map< String, HDF5Data> dataWriters = null;
		
	private List< String > header = null ;
	
	private String fileName = null;
	
	private AtomicBoolean savedDataBlock = new AtomicBoolean( true );
	
	public OutputHDF5DataWriter( String file, ITaskMonitor monitor ) throws Exception 
	{
		this.fileName = file;
				
		this.dataWriters = new HashMap< String, HDF5Data >();		
		
		this.taskMonitor( monitor );
		
		this.writer = HDF5Factory.open( file );
				
		this.header = new ArrayList<String>();
	}

	@Override
	public void addMetadata( String id, String text ) throws Exception 
	{
		id = id.replace("\n", "").replace("\r", "");
		text = text.replace("\n", "").replace("\r", "");

		this.header.add( id + "=" + text );
	}

	@Override
	public boolean finished() 
	{
		return this.savedDataBlock.get();
	}

	@Override
	public boolean saveData( DataBlock dataBlock ) throws Exception
	{	
		this.savedDataBlock.set( false );
		
		if( dataBlock != null )
		{			
					
			HDF5Data wr = this.dataWriters.get( dataBlock.getName() );
			
			if( wr == null )
			{
				wr = new HDF5Data( this.writer, dataBlock.getName(), dataBlock.getDataType(), dataBlock.getNumCols() );
				this.dataWriters.put( dataBlock.getName(), wr );
			}
			
			if( dataBlock instanceof ByteBlock)
			{
				ByteBlock d = (ByteBlock)dataBlock;
				Byte[] dat = d.getData();

				wr.addData( dat );
			}
			else if(dataBlock instanceof ShortBlock)
			{
				ShortBlock d = (ShortBlock)dataBlock;
				Short[] dat = d.getData();

				wr.addData( dat );
			}
			else if(dataBlock instanceof IntegerBlock )
			{
				IntegerBlock d = (IntegerBlock)dataBlock;
				Integer[] dat = d.getData();
				
				wr.addData( dat );
			}
			else if(dataBlock instanceof LongBlock)
			{
				LongBlock d = (LongBlock)dataBlock;
				Long[] dat = d.getData();

				wr.addData( dat );
			}
			else if(dataBlock instanceof FloatBlock)
			{
				FloatBlock d = (FloatBlock)dataBlock;
				Float[] dat = d.getData();

				wr.addData( dat );
			}
			else if(dataBlock instanceof DoubleBlock)
			{
				DoubleBlock d = (DoubleBlock)dataBlock;
				Double[] dat = d.getData();

				wr.addData( dat );
			}
			else if(dataBlock instanceof CharBlock)
			{
				CharBlock d = (CharBlock)dataBlock;
				Character[] dat = d.getData();

				wr.addData( new String( ConvertTo.CharacterArray2charArray( dat ) ).split( "(?!^)" ) );
			}
			else if( dataBlock instanceof StringBlock )
			{
				StringBlock d = (StringBlock)dataBlock;
				Character[] dat = d.getData();

				wr.addData( new String[] { new String( ConvertTo.CharacterArray2charArray( dat ) ) } );
			}
			else
			{
				throw new Exception( "Data block type unknown." );
			}
			
		}
		
		this.savedDataBlock.set( true );
		
		return true;
	}


	@Override
	public void close() throws Exception 
	{
		this.writer.writeStringArray( "header", this.header.toArray( new String[0] ) );
		
		for( HDF5Data wr : this.dataWriters.values() )
		{
			wr.close();
		}
		
		this.writer.close();
	}

	@Override
	public void taskMonitor(ITaskMonitor monitor) 
	{
	}

	@Override
	public String getFileName() 
	{
		return this.fileName;
	}
}
