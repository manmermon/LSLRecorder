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
package testing.DataStream.OutputDataFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import Auxiliar.extra.ConvertTo;
import DataStream.Binary.BinaryDataFormat;
import DataStream.Binary.reader.ReaderBinaryFile;
import DataStream.OutputDataFile.DataBlock.ByteBlock;
import dataStream.StreamHeader;
import edu.ucsd.sccn.LSL;
import edu.ucsd.sccn.LSLUtils;

public class testOutBinFileReader {

	public static void main(String[] args) 
	{
		try
		{
			String[] files = { "G:/test_Sint16A.temp0"
								,"G:/test_Sint8A.temp0"
								,"G:/test_SfloatA.temp0"								
								,"G:/test_SdoubleA.temp0"
								,"G:/test0_SfloatA.temp0"
								,"G:/test_Sfloat4A.temp0"
								,"G:/test_Sfloat45A.temp0"
							};
			
			List< List< BinaryDataFormat > > formatFiles = new ArrayList< List<BinaryDataFormat>>();
			
			List< BinaryDataFormat > formatInt16 = new ArrayList<BinaryDataFormat>();
			formatInt16.add( new BinaryDataFormat( LSLUtils.getDataTypeBytes( LSL.ChannelFormat.int16 ), 2,1 ) );
			formatInt16.add( new BinaryDataFormat( LSLUtils.getDataTypeBytes( LSL.ChannelFormat.double64 ), 8, 1 ) );
			formatFiles.add( formatInt16 );
			
			List< BinaryDataFormat > formatInt8 = new ArrayList<BinaryDataFormat>();
			formatInt8.add( new BinaryDataFormat( LSLUtils.getDataTypeBytes( LSL.ChannelFormat.int8 ), 1, 1 ) );
			formatInt8.add( new BinaryDataFormat( LSLUtils.getDataTypeBytes( LSL.ChannelFormat.double64 ), 8, 1 ) );
			formatFiles.add( formatInt8 );
			
			List< BinaryDataFormat > formatFloat = new ArrayList<BinaryDataFormat>();
			formatFloat.add( new BinaryDataFormat( LSLUtils.getDataTypeBytes( LSL.ChannelFormat.float32 ), 4, 1 ) );
			formatFloat.add( new BinaryDataFormat( LSLUtils.getDataTypeBytes( LSL.ChannelFormat.double64 ), 8, 1 ) );
			formatFiles.add( formatFloat );
			
			List< BinaryDataFormat > formatDouble = new ArrayList<BinaryDataFormat>();
			formatDouble.add( new BinaryDataFormat( LSLUtils.getDataTypeBytes( LSL.ChannelFormat.double64 ), 8, 1 ) );
			formatDouble.add( new BinaryDataFormat( LSLUtils.getDataTypeBytes( LSL.ChannelFormat.double64 ), 8, 1 ) );			
			formatFiles.add( formatDouble );
			
			List< BinaryDataFormat > formatFloat2 = new ArrayList<BinaryDataFormat>();
			formatFloat2.add( new BinaryDataFormat( LSLUtils.getDataTypeBytes( LSL.ChannelFormat.float32 ), 4, 2 ) );
			formatFloat2.add( new BinaryDataFormat( LSLUtils.getDataTypeBytes( LSL.ChannelFormat.double64 ), 8, 2 ) );
			formatFiles.add( formatFloat2 );
			
			List< BinaryDataFormat > formatFloat4 = new ArrayList<BinaryDataFormat>();
			formatFloat4.add( new BinaryDataFormat( LSLUtils.getDataTypeBytes( LSL.ChannelFormat.float32 ), 4, 4 ) );
			formatFloat4.add( new BinaryDataFormat( LSLUtils.getDataTypeBytes( LSL.ChannelFormat.double64 ), 8, 4 ) );
			formatFiles.add( formatFloat4 );
			
			List< BinaryDataFormat > formatFloat45 = new ArrayList<BinaryDataFormat>();
			formatFloat45.add( new BinaryDataFormat( LSLUtils.getDataTypeBytes( LSL.ChannelFormat.float32 ), 4, 4*5 ) );
			formatFloat45.add( new BinaryDataFormat( LSLUtils.getDataTypeBytes( LSL.ChannelFormat.double64 ), 8, 5 ) );
			formatFiles.add( formatFloat45 );
			
			int iFormat = 0;
			for( String file : files )
			{				
				List< BinaryDataFormat > formats = formatFiles.get( iFormat );				
								
				System.out.println( "Test reader file: " + file );
				
				ReaderBinaryFile reader = new ReaderBinaryFile( new File( file ), formats, StreamBinaryHeader.HEADER_END );
				
				List< ByteBlock > byteList = new ArrayList< ByteBlock >();
				
				do 
				{
					byteList = reader.readDataFromBinaryFile();
				
					if( !byteList.isEmpty() )
					{
						ByteBlock block0 = byteList.get( 0 );
						ByteBlock block1 = byteList.get( 1);
						
						switch ( iFormat) 
						{
							case 0:
							{
								short[] data = ConvertTo.ByteArray2ShortArray( ConvertTo.ByterArray2byteArray( block0.getData() ) );
								
								
								double[] time = ConvertTo.ByteArray2DoubleArray( ConvertTo.ByterArray2byteArray( block1.getData() ) );
											
								System.out.print(" <");
								for( int i = 0 ; i < data.length; i++ )
								{
									System.out.print( + data[ i ] +", " );
								}
								System.out.print( "> :: <" );
								
								for( int i = 0 ; i < time.length; i++ )
								{
									System.out.print( time[ i ] + ", ");
								}
								
								System.out.println(">");
								
								break;
							}
							case 1:
							{
								byte[] data = ConvertTo.ByterArray2byteArray( block0.getData() );
								
								
								double[] time = ConvertTo.ByteArray2DoubleArray( ConvertTo.ByterArray2byteArray( block1.getData() ) );
														
								System.out.print(" <");
								for( int i = 0 ; i < data.length; i++ )
								{
									System.out.print( + data[ i ] +", " );
								}
								System.out.print( "> :: <" );
								
								for( int i = 0 ; i < time.length; i++ )
								{
									System.out.print( time[ i ] + ", ");
								}
								
								System.out.println(">");
								
								break;
							}
							case 2:
							case 4:
							case 5:
							case 6:
							{
								float[] data = ConvertTo.ByteArray2FloatArray( ConvertTo.ByterArray2byteArray( block0.getData() ) );
																
								double[] time = ConvertTo.ByteArray2DoubleArray( ConvertTo.ByterArray2byteArray( block1.getData() ) );
											
								System.out.print(" <");
								for( int i = 0 ; i < data.length; i++ )
								{
									System.out.print( + data[ i ] +", " );
								}
								System.out.print( "> :: <" );
								
								for( int i = 0 ; i < time.length; i++ )
								{
									System.out.print( time[ i ] + ", ");
								}
								
								System.out.println(">");
								
								break;
							}
							case 3:
							{
								double[] data = ConvertTo.ByteArray2DoubleArray( ConvertTo.ByterArray2byteArray( block0.getData() ) );								
								
								double[] time = ConvertTo.ByteArray2DoubleArray( ConvertTo.ByterArray2byteArray( block1.getData() ) );
														
								System.out.print(" <");
								for( int i = 0 ; i < data.length; i++ )
								{
									System.out.print( + data[ i ] +", " );
								}
								System.out.print( "> :: <" );
								
								for( int i = 0 ; i < time.length; i++ )
								{
									System.out.print( time[ i ] + ", ");
								}
								
								System.out.println(">");
								
								break;
							}
							default:
							{
								break;
							}
						}
						
						
					}
				}
				while( !byteList.isEmpty() );
				
				System.out.println( "Test END\n" );
				
				iFormat++;
			}
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}

}
