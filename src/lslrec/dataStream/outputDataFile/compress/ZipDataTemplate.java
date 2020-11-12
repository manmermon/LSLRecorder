/*
 * Copyright 2018 by Manuel Merino Monge <manmermon@dte.us.es>
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

package lslrec.dataStream.outputDataFile.compress;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

public abstract class ZipDataTemplate implements IOutZip
{	
	public byte[] zipData( byte[] data ) throws Exception
	{	
		byte[] compressData = this.compressData( data );
		
		return compressData;
	}
	
	public byte[] zipData( short[] data ) throws Exception
	{	
		int typeBytes = Short.BYTES;
		int nBytes = data.length * typeBytes;
		byte[] dataToSave = new byte[ nBytes ];

		ByteBuffer bb = ByteBuffer.wrap( dataToSave );
		ShortBuffer buf = bb.asShortBuffer();
		buf.put( data );
		
		byte[] compressData = this.compressData( dataToSave );
		
		return compressData;
	}
	
	public byte[] zipData( int[] data ) throws Exception
	{	
		int typeBytes = Integer.BYTES;
		int nBytes = data.length * typeBytes;
		byte[] dataToSave = new byte[nBytes];

		ByteBuffer bb = ByteBuffer.wrap(dataToSave);
		IntBuffer buf = bb.asIntBuffer();
		buf.put(data);

		byte[] compressData = this.compressData( dataToSave );
		
		return compressData;
	}

	public byte[] zipData( long[] data ) throws Exception
	{
		int typeBytes = Long.BYTES;
		int nBytes = data.length * typeBytes;
		byte[] dataToSave = new byte[ nBytes ];

		ByteBuffer bb = ByteBuffer.wrap(dataToSave);
		LongBuffer buf = bb.asLongBuffer();
		buf.put(data);

		byte[] compressData = this.compressData( dataToSave );

		return compressData;
	}

	public byte[] zipData( double[] data ) throws Exception
	{
		int typeBytes = Double.BYTES;
		int nBytes = data.length * typeBytes;
		byte[] dataToSave = new byte[nBytes];

		ByteBuffer bb = ByteBuffer.wrap(dataToSave);
		DoubleBuffer buf = bb.asDoubleBuffer();
		buf.put(data);

		byte[] compressData = this.compressData( dataToSave );
		
		return compressData;
	}

	public byte[] zipData( float[] data ) throws Exception
	{
		int typeBytes = Float.BYTES;
		int nBytes = data.length * typeBytes;
		byte[] dataToSave = new byte[nBytes];

		ByteBuffer bb = ByteBuffer.wrap(dataToSave);
		FloatBuffer buf = bb.asFloatBuffer();
		buf.put(data);

		byte[] compressData = this.compressData( dataToSave );
		
		return compressData;
	}

	public byte[] zipData( char[] data, Charset charset ) throws Exception
	{
		CharBuffer charBuffer = CharBuffer.wrap( data );
		ByteBuffer byteBuffer = charset.encode( charBuffer );
		byte[] dataToSave = Arrays.copyOfRange( byteBuffer.array(), byteBuffer.position(), byteBuffer.limit() );
		
		byte[] compressData = this.compressData( dataToSave );

		return compressData;
	}

	public byte[] zipData( String data, Charset charset ) throws Exception
	{
		return zipData( data.toCharArray(), charset );
	}
	
	protected abstract byte[] compressData( byte[] data) throws Exception;
}
