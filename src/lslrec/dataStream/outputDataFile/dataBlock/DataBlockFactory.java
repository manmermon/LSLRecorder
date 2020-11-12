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
package lslrec.dataStream.outputDataFile.dataBlock;

import lslrec.dataStream.family.setting.StreamSettingUtils.StreamDataType;

public class DataBlockFactory 
{
	public static DataBlock getDataBlock( StreamDataType type, int seqNum, String name, int nCols, Object[] data )
	{
		DataBlock block = null;
		
		switch ( type )
		{
			case double64:
			{
				Double[] val = new Double[ data.length ];
				for( int i = 0; i < data.length; i++ )
				{
					val[ i ] = (Double)data[ i ];
				}
				
				block = new DoubleBlock(seqNum, name, nCols, val );
				
				break;
			}
			case float32:
			{
				Float[] val = new Float[ data.length ];
				for( int i = 0; i < data.length; i++ )
				{
					val[ i ] = (Float)data[ i ];
				}
				
				block = new FloatBlock(seqNum, name, nCols, val );
				
				break;
			}
			case int8:
			{
				Byte[] val = new Byte[ data.length ];
				for( int i = 0; i < data.length; i++ )
				{
					val[ i ] = (Byte)data[ i ];
				}
				
				block = new ByteBlock(seqNum, name, nCols, val );
				
				break;
			}
			case int16:
			{
				Short[] val = new Short[ data.length ];
				for( int i = 0; i < data.length; i++ )
				{
					val[ i ] = (Short)data[ i ];
				}
				
				block = new ShortBlock(seqNum, name, nCols, val );
				
				break;
			}
			case int32:
			{
				Integer[] val = new Integer[ data.length ];
				for( int i = 0; i < data.length; i++ )
				{
					val[ i ] = (Integer)data[ i ];
				}
				
				block = new IntegerBlock(seqNum, name, nCols, val );
				
				break;
			}
			case int64:
			{
				Long[] val = new Long[ data.length ];
				for( int i = 0; i < data.length; i++ )
				{
					val[ i ] = (Long)data[ i ];
				}
				
				block = new LongBlock(seqNum, name, nCols, val );
				
				break;
			}
			case string:
			{
				String str = "";
				for( Object st : data )
				{
					str += st.toString();
				}
				
				block = new StringBlock(seqNum, name, nCols, str );
				
				break;
			}
			default:
			{
				break;
			}
		}
		
		return block;
	}
}
