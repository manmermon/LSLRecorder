/*-
 * #%L
 * MAT File Library
 * %%
 * Copyright (C) 2018 HEBI Robotics
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

//package us.hebi.matlab.mat.tests.serialization;
package lslrec.dataStream.outputDataFile.format.matlab;

import us.hebi.matlab.mat.format.Mat5;
import us.hebi.matlab.mat.format.Mat5Serializable;
import us.hebi.matlab.mat.format.Mat5Type;
import us.hebi.matlab.mat.format.Mat5WriteUtil;
import us.hebi.matlab.mat.types.AbstractArray;
import us.hebi.matlab.mat.types.MatlabType;
import us.hebi.matlab.mat.types.Sink;
import us.hebi.matlab.mat.types.Sinks;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteOrder;

import lslrec.exceptions.UnsupportedDataTypeException;
import us.hebi.matlab.mat.util.Bytes;
import us.hebi.matlab.mat.util.Preconditions;

//import static us.hebi.matlab.mat.format.Mat5.*;
//import static us.hebi.matlab.mat.format.Mat5WriteUtil.*;
//import static us.hebi.matlab.mat.util.Preconditions.*;

/**
 * 2D Matrix that has a fixed number of columns, and an expanding number
 * of rows. We've often encountered this as an issue when working with
 * synchronized time series from multiple sources.
 * <p>
 * The MAT file format is not well suited for this because the size
 * needs to be known beforehand, and because the data is in column-major
 * format.
 * <p>
 * This class is an example of how such a use case could be implemented
 * using custom serialization. There is one expanding file for each column
 * that contains all rows. Once the data gets written, all temporary storage
 * files get combined and written into the target sink.
 * <p>
 * This example that is not considered part of the stable API.
 *
 * @author Florian Enner
 * @since 08 May 2018
 * 
 * @author Modification by Manuel Merino Monge
 * @since 26 Jun 2020
 */
public final class StreamingMatrix2D extends AbstractArray implements Mat5Serializable 
{
	private Mat5Type datatype = Mat5Type.Double;
	private MatlabType mattype = MatlabType.Double;

	private int byteOfType = Bytes.SIZEOF_DOUBLE;

	private int col = 0;
	private final File[] tmpFiles;
	private final Sink[] columnSinks;
	private final String name;

	
	public static StreamingMatrix2D createRowMajor(File folder, String matrixName, long numCols, MatlabType datatype ) throws Exception 
	{
		return new StreamingMatrix2D(folder, matrixName, numCols, datatype );
	}

	protected StreamingMatrix2D(File folder, String name, long numCols, MatlabType dataType ) throws Exception 
	{
		super( Mat5.dims(0, (int)numCols));
		this.mattype = dataType;		
		
		this.name = name;
		Preconditions.checkNotNull(folder);
		Preconditions.checkState(folder.isDirectory(), "Invalid target directory: " + folder);

		// Create a temporary file for each column. The MAT file needs to be stored in column-major
		// order, so we would otherwise have to iterate through the entire file N times.
		tmpFiles = new File[(int)numCols ];
		columnSinks = new Sink[ (int)numCols ];
		
		for (int col = 0; col < numCols; col++) 
		{

			// Create new temporary file
			File file = new File(folder.getPath() + "/" + name + col + ".tmp");
			
			if (file.exists() && !file.delete()) 
			{
				for (Sink sink : columnSinks) 
				{
					sink.close();
				}
				
				String msg = "Failed to overwrite existing temporary storage: " + file.getAbsolutePath();
				throw new IOException(msg);
			}

			// Write buffer
			tmpFiles[col] = file;
			columnSinks[col] = Sinks.newStreamingFile(tmpFiles[col]);

			switch ( this.mattype ) 
			{
				case Double:
				{
					this.datatype = Mat5Type.Double;
					this.byteOfType = Bytes.SIZEOF_DOUBLE;
					break;
				}
				case Single:
				{
					this.datatype = Mat5Type.Single;
					this.byteOfType = Bytes.SIZEOF_FLOAT;
					break;
				}
				case Int64:
				{
					this.datatype = Mat5Type.Int64;
					this.byteOfType = Bytes.SIZEOF_LONG;
					break;
				}
				case Int32:
				{
					this.datatype = Mat5Type.Int32;
					this.byteOfType = Bytes.SIZEOF_INT;
					break;
				}
				case Int16:
				{
					this.datatype = Mat5Type.Int16;
					this.byteOfType = Bytes.SIZEOF_SHORT;
					break;
				}
				case Int8:
				{
					this.datatype = Mat5Type.Int8;
					this.byteOfType = Bytes.SIZEOF_BYTE;
					break;
				}
				case Character:
				{
					this.datatype = Mat5Type.Utf8;
					this.byteOfType = Bytes.SIZEOF_BYTE;
					break;
				}
				default:
				{
					throw new UnsupportedDataTypeException( "Matlab data type '" + this.mattype.name() + "' unsupported." );
				}
			}
		}
	}

	public String getName() 
	{
		return name;
	}

	@Override
	public int getMat5Size(String name) 
	{
		int header = Mat5WriteUtil.computeArrayHeaderSize(name, this);
		int data = this.datatype.computeSerializedSize( super.getNumElements() );;
		return Mat5.MATRIX_TAG_SIZE + header + data;
	}

	public void addValue( Number value) throws IOException
	{
		switch ( this.mattype ) 
		{
			case Single:
			{
				columnSinks[ col++ ].writeFloat( value.floatValue() );
				break;
			}
			case Int64:
			{
				columnSinks[ col++ ].writeLong( value.longValue() );
				break;
			}
			case Int32:
			{
				columnSinks[ col++ ].writeInt( value.intValue() );
				break;
			}
			case Int16:
			{
				columnSinks[ col++ ].writeShort( value.shortValue() );
				break;
			}
			case Int8:
			{
				columnSinks[ col++ ].writeByte( value.byteValue() );
				break;
			}
			default:
			{
				columnSinks[ col++ ].writeDouble( value.doubleValue() );
				break;
			}
		}
		
		if ( col == getNumCols() ) 
		{
			col = 0;
			dims[0]++;
		}
	}

	public void addValue( char c ) throws IOException
	{
		this.addValue( (byte)c );
	}
	
	@Override
	public void writeMat5(String name, boolean isGlobal, Sink sink) throws IOException 
	{
		int numElements = getNumElements();
		Mat5WriteUtil.writeMatrixTag(name, this, sink);
		Mat5WriteUtil.writeArrayHeader(name, isGlobal, this, sink);
		this.datatype.writeTag(numElements, sink);
		writeData(sink);
		this.datatype.writePadding(numElements, sink);
	}

	private void writeData(Sink sink) throws IOException 
	{
		if (getNumElements() == 0)
			return;

		if (sink.order() != ByteOrder.nativeOrder())
			throw new IOException("Expected sink to be in native order");

		for (int col = 0; col < getNumCols(); col++) 
		{
			// Make sure all data is on disk
			columnSinks[col].close();

			// Map each file and push data to sink
			FileInputStream input = new FileInputStream(tmpFiles[col]);
			try 
			{
				int numBytes = getNumRows() * this.byteOfType;
				sink.writeInputStream(input, numBytes);
			}
			finally 
			{
				input.close();
			}

		}

	}

	@Override
	public void close() throws IOException 
	{
		for (int col = 0; col < getNumCols(); col++) 
		{
			if (!tmpFiles[col].delete()) 
			{
				System.err.println("Unable to delete temporary file: " + tmpFiles[col]);
			}
		}
	}

	@Override
	protected int subHashCode() 
	{
		return System.identityHashCode(this);
	}

	@Override
	protected boolean subEqualsGuaranteedSameClass(Object otherGuaranteedSameClass) 
	{
		return false;
	}

	@Override
	public MatlabType getType() 
	{
		return this.mattype;
	}
}