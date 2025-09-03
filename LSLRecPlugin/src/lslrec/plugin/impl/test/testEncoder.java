package lslrec.plugin.impl.test;

import lslrec.auxiliar.extra.ConvertTo;
import lslrec.config.ConfigApp;
import lslrec.config.Parameter;
import lslrec.dataStream.family.setting.MutableStreamSetting;
import lslrec.dataStream.family.setting.SimpleStreamSetting;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lslrec.dataStream.convertData.clis.ClisData;
import lslrec.dataStream.convertData.clis.ClisMetadataReader;
import lslrec.dataStream.convertData.clis.MetadataVariableBlock;
import lslrec.dataStream.family.setting.IStreamSetting.StreamLibrary;
import lslrec.dataStream.outputDataFile.compress.CompressorDataFactory;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlock;
import lslrec.dataStream.outputDataFile.dataBlock.DataBlockFactory;
import lslrec.dataStream.outputDataFile.format.DataFileFormat;
import lslrec.dataStream.outputDataFile.format.OutputFileFormatParameters;
import lslrec.dataStream.tools.StreamUtils;
import lslrec.dataStream.tools.StreamUtils.StreamDataType;
import lslrec.plugin.impl.encoder.binary.OutputBinaryDataWriter;
import lslrec.plugin.impl.encoder.csv.CSVEncoder;
import lslrec.plugin.impl.encoder.csv.OutputCSVDataWriter;
import lslrec.plugin.impl.encoder.matlab.MatlabEncoder;
import lslrec.plugin.impl.encoder.matlab.MatlabEncoderPlugin;
import lslrec.plugin.impl.encoder.matlab.OutputMatDataWriter;

public class testEncoder 
{
	public static void main(String[] args) 
	{
		checkBin();
		//checkCSV_3();
		//checkCSV_2();
		//checkCSV();
	}
	
	private static void checkMatlab()
	{
		try
		{
			OutputMatDataWriter wr = new OutputMatDataWriter( "prueba.mat", null );
			for( int i = 0; i < 10; i++ )
			{
				Double[] d = new Double[ 10 ];
				
				for( int j = 0; j < d.length; j++ )
				{
					d[ j ] = j + i * 10D;
				}
			
				DataBlock data = DataBlockFactory.getDataBlock( StreamDataType.double64, 0, "var", 2, d );
				wr.saveData( data );
			}
			
			while( !wr.isFinished() )
			{
				Thread.sleep( 100L );
			}
			
			wr.close();			
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private static void checkCSV_2()
	{
		try 
		{
			ClisData clis = new ClisData( "D:\\Nextcloud\\WorkSpace\\GitHub\\LSLRecorder\\records\\data_OpenBCI_EEG.clis" );
			
			File f = new File( "prueba.test" );
			
			OutputFileFormatParameters pars = new OutputFileFormatParameters();

			pars.setParameter( OutputFileFormatParameters.OUT_FILE_FORMAT,  "CSV" );

			Parameter< String > p = pars.getParameter( OutputFileFormatParameters.OUT_FILE_FORMAT );
			pars.setParameter( OutputFileFormatParameters.OUT_FILE_NAME, "prueba.csv" );

			pars.setParameter( OutputFileFormatParameters.ZIP_ID, CompressorDataFactory.GZIP );
			pars.setParameter( OutputFileFormatParameters.CHAR_CODING,  Charset.forName( "UTF-8" )  );			
			pars.setParameter( OutputFileFormatParameters.PARALLELIZE, true );
			pars.setParameter( OutputFileFormatParameters.NUM_BLOCKS, 1L );
			pars.setParameter( OutputFileFormatParameters.BLOCK_DATA_SIZE, (int)( Math.pow( 2, 20 ) * 10 ) );			
			pars.setParameter( OutputFileFormatParameters.DATA_NAMES, "test" );			
			pars.setParameter( OutputFileFormatParameters.RECORDING_INFO, new HashMap< String, String >() );			
			pars.setParameter( OutputFileFormatParameters.DELETE_BIN, false );


			pars.setParameter( CSVEncoder.SEPARATE_VARIABLE, false);
			SimpleStreamSetting sss = new SimpleStreamSetting( StreamLibrary.LSL, "test", StreamDataType.double64, 1, 10, 10, 3,false, "testing", "1234" );

			if( !f.exists() )
			{
				f.createNewFile();
			}

			OutputCSVDataWriter wr = new OutputCSVDataWriter( f.getAbsolutePath(), null,  pars, sss );
			
			Map< String, Number[][] > data = clis.importAllData();
			
			List< MetadataVariableBlock > metaVars = clis.getVarInfo();
			
			for( String varName : data.keySet() )
			{
				Number[][] values = data.get( varName );
				
				int chn = 1;
				
				for( MetadataVariableBlock mv : metaVars )
				{
					if( mv.getName().equals( varName ) )
					{
						chn = mv.getCols();
						break;
					}
				}
				
				Float[] cval = null;
				int r = values.length;
				int c = values[0].length;
				
				cval = ConvertTo.Casting.floatArray2FloatArray( ConvertTo.Casting.NumberArray2FloatArray( ConvertTo.Transform.Matrix2Array( values )  ));
				
				int step = chn * 10; 
				int pos = step;
				int nSep = 0;
				while( pos <= cval.length )
				{
					Float[] subVals = Arrays.copyOfRange( cval, pos-step, pos);
					
					DataBlock valBlock = DataBlockFactory.getDataBlock( StreamDataType.float32, nSep, varName, chn, subVals );
					wr.saveData( valBlock );
					
					nSep++;
					pos += step;
				}
				
				if( pos < cval.length )
				{
					Float[] subVals = Arrays.copyOfRange( cval, pos-step, cval.length );
					
					DataBlock valBlock = DataBlockFactory.getDataBlock( StreamDataType.float32, 0, varName, chn, subVals );
					wr.saveData( valBlock );
				}
				
			}
			
			while( !wr.isFinished() )
			{
				Thread.sleep( 100L );
			}
			
			wr.close();	
					
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	private static void checkCSV_3()
	{
		try 
		{
			ClisData clis = new ClisData( "D:\\Desktop\\PA_DATA_OpenBCI\\ALBERTO\\Sesion14-16-Feb-2022\\data_OpenBCI_EEG.clis" );
			
			File f = new File( "prueba.csv" );
			
			OutputFileFormatParameters pars = new OutputFileFormatParameters();

			pars.setParameter( OutputFileFormatParameters.OUT_FILE_FORMAT,  "CSV" );

			Parameter< String > p = pars.getParameter( OutputFileFormatParameters.OUT_FILE_FORMAT );
			pars.setParameter( OutputFileFormatParameters.OUT_FILE_NAME, "prueba.csv" );

			pars.setParameter( OutputFileFormatParameters.ZIP_ID, CompressorDataFactory.GZIP );
			pars.setParameter( OutputFileFormatParameters.CHAR_CODING,  Charset.forName( "UTF-8" )  );			
			pars.setParameter( OutputFileFormatParameters.PARALLELIZE, true );
			pars.setParameter( OutputFileFormatParameters.NUM_BLOCKS, 1L );
			pars.setParameter( OutputFileFormatParameters.BLOCK_DATA_SIZE, (int)( Math.pow( 2, 20 ) * 10 ) );			
			pars.setParameter( OutputFileFormatParameters.DATA_NAMES, "test" );			
			pars.setParameter( OutputFileFormatParameters.RECORDING_INFO, new HashMap< String, String >() );			
			pars.setParameter( OutputFileFormatParameters.DELETE_BIN, false );


			pars.setParameter( CSVEncoder.SEPARATE_VARIABLE, false);
			SimpleStreamSetting sss = new SimpleStreamSetting( StreamLibrary.LSL, "test", StreamDataType.double64, 1, 10, 10, 3,false, "testing", "1234" );

			if( !f.exists() )
			{
				f.createNewFile();
			}

			List< MetadataVariableBlock > lmvb = clis.getVarInfo();
			
			List< String > varNames = new ArrayList< String >();
			String v = "";
			StreamDataType varType = StreamDataType.double64;
			int nChs = 0;
			for( MetadataVariableBlock vb : lmvb )
			{
				varNames.add( vb.getName() );
				nChs += vb.getCols();
				v += vb.getName();												
			}

			String header = clis.getHeader();
			
			SimpleStreamSetting sst = new SimpleStreamSetting( StreamLibrary.LSL
																, v
																, varType
																, nChs
																, 1
																, 0
																, 3
																, true
																, ""
																, ""
																, null );

			MutableStreamSetting msst = new MutableStreamSetting( sst );
			msst.setDescription( header );
			pars.setParameter( OutputFileFormatParameters.DATA_NAMES, varNames.toString() );
			
			OutputCSVDataWriter wr = new OutputCSVDataWriter( f.getAbsolutePath(), null,  pars, sss );
														
			int iSeq = 0;
			for( int iv = 0; iv < lmvb.size(); iv++ )
			{												
				MetadataVariableBlock vb = lmvb.get( iv );
				
				double memorySize = (Integer)ConfigApp.getProperty( ConfigApp.SEGMENT_BLOCK_SIZE ) * Math.pow( 2, 20 );
				int dataByteSize = StreamUtils.getDataTypeBytes( vb.getDataType() );
				
				int chunkSize = (int)( memorySize / dataByteSize );
				if( chunkSize < 1 )
				{
					chunkSize = 1;
				}
				
				nChs = vb.getCols();
				String var = vb.getName();
				varType = vb.getDataType();
					
				boolean cont = true;
				while( cont )
				{
					Number[][] vData = clis.importNextDataBlock( iv, chunkSize );
					
					cont = ( vData != null );
					
					if( cont )
					{
						DataBlock db = DataBlockFactory.getDataBlock( varType, iSeq, var, nChs, ConvertTo.Transform.Matrix2Array( vData ) );

						wr.saveData( db );

						iSeq++;
					}
				}
			}
			
			wr.addMetadata( "", header );
			while( !wr.isFinished() )
			{
				Thread.sleep( 100L );
			}
			
			wr.close();	
					
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
	
	
	private static void checkCSV()
	{
		File f = new File( "prueba.test" );
		
		try
		{
			OutputFileFormatParameters pars = new OutputFileFormatParameters();
			
			pars.setParameter( OutputFileFormatParameters.OUT_FILE_FORMAT,  "CSV" );
			
			Parameter< String > p = pars.getParameter( OutputFileFormatParameters.OUT_FILE_FORMAT );
			pars.setParameter( OutputFileFormatParameters.OUT_FILE_NAME, "prueba.csv" );
			
			pars.setParameter( OutputFileFormatParameters.ZIP_ID, CompressorDataFactory.GZIP );
			pars.setParameter( OutputFileFormatParameters.CHAR_CODING,  Charset.forName( "UTF-8" )  );			
			pars.setParameter( OutputFileFormatParameters.PARALLELIZE, true );
			pars.setParameter( OutputFileFormatParameters.NUM_BLOCKS, 1L );
			pars.setParameter( OutputFileFormatParameters.BLOCK_DATA_SIZE, (int)( Math.pow( 2, 20 ) * 10 ) );			
			pars.setParameter( OutputFileFormatParameters.DATA_NAMES, "test" );			
			pars.setParameter( OutputFileFormatParameters.RECORDING_INFO, new HashMap< String, String >() );			
			pars.setParameter( OutputFileFormatParameters.DELETE_BIN, false );
			
			
			pars.setParameter( CSVEncoder.SEPARATE_VARIABLE, false);
			SimpleStreamSetting sss = new SimpleStreamSetting( StreamLibrary.LSL, "test", StreamDataType.double64, 3, 10, 10, 3, false, "testing", "1234" );
			
			if( !f.exists() )
			{
				f.createNewFile();
			}
			
			OutputCSVDataWriter wr = new OutputCSVDataWriter( f.getAbsolutePath(), null,  pars, sss );
			for( int i = 0; i < 10; i++ )
			{
				Double[] d = new Double[ sss.channel_count() * sss.getChunkSize() ];
				
				for( int j = 0; j < d.length; j++ )
				{
					d[ j ] = j + i * 10D;
				}
			
				DataBlock data = DataBlockFactory.getDataBlock( StreamDataType.double64, i, "var1", sss.channel_count(), d );
				wr.saveData( data );
			}
			
			for( int i = 0; i < 8; i++ )
			{
				Double[] d = new Double[ 3 * sss.getChunkSize() ];
				
				for( int j = 0; j < d.length; j++ )
				{
					d[ j ] = -( j + i * 10D);
				}
			
				DataBlock data = DataBlockFactory.getDataBlock( StreamDataType.double64, i, "var2", 3, d );
				wr.saveData( data );
			}
			
			for( int i = 0; i < 10; i++ )
			{
				Double[] d = new Double[ 1 * sss.getChunkSize() ];
				
				for( int j = 0; j < d.length; j++ )
				{
					d[ j ] = -( j + i * 10D);
				}
			
				DataBlock data = DataBlockFactory.getDataBlock( StreamDataType.double64, i, "var3", 1, d );
				wr.saveData( data );
			}
			
			for( int i = 0; i < 7; i++ )
			{
				Double[] d = new Double[ 1 * sss.getChunkSize() ];
				
				for( int j = 0; j < d.length; j++ )
				{
					d[ j ] = -( j + i * 10D);
				}
			
				DataBlock data = DataBlockFactory.getDataBlock( StreamDataType.double64, i, "var4", 1, d );
				wr.saveData( data );
			}
			
			while( !wr.isFinished() )
			{
				Thread.sleep( 100L );
			}
			
			wr.close();			
		}
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally 
		{
			f.deleteOnExit();
		}
	}

	private static void checkBin()
	{
		try 
		{
			ClisData clis = new ClisData( "D:\\Nextcloud\\WorkSpace\\GitHub\\LSLRecorder\\records\\data_Simulation.clis" );
			
			File f = new File( "prueba.temp" );
			
			OutputFileFormatParameters pars = new OutputFileFormatParameters();

			pars.setParameter( OutputFileFormatParameters.OUT_FILE_FORMAT,  ConfigApp.shortNameApp + "BIN" );

			Parameter< String > p = pars.getParameter( OutputFileFormatParameters.OUT_FILE_FORMAT );
			pars.setParameter( OutputFileFormatParameters.OUT_FILE_NAME, "prueba.temp" );

			pars.setParameter( OutputFileFormatParameters.ZIP_ID, CompressorDataFactory.GZIP );
			pars.setParameter( OutputFileFormatParameters.CHAR_CODING,  Charset.forName( "UTF-8" )  );			
			pars.setParameter( OutputFileFormatParameters.PARALLELIZE, true );
			pars.setParameter( OutputFileFormatParameters.NUM_BLOCKS, 1L );
			pars.setParameter( OutputFileFormatParameters.BLOCK_DATA_SIZE, (int)( Math.pow( 2, 20 ) * 10 ) );			
			pars.setParameter( OutputFileFormatParameters.DATA_NAMES, "test" );			
			pars.setParameter( OutputFileFormatParameters.RECORDING_INFO, new HashMap< String, String >() );			
			pars.setParameter( OutputFileFormatParameters.DELETE_BIN, false );


			SimpleStreamSetting sss = new SimpleStreamSetting( StreamLibrary.LSL, "test", StreamDataType.double64, 1, 10, 10, 3,false, "testing", "1234" );

			if( !f.exists() )
			{
				f.createNewFile();
			}

			List< MetadataVariableBlock > lmvb = clis.getVarInfo();
			
			List< String > varNames = new ArrayList< String >();
			String v = "";
			StreamDataType varType = StreamDataType.double64;
			int nChs = 0;
			for( MetadataVariableBlock vb : lmvb )
			{
				varNames.add( vb.getName() );
				nChs += vb.getCols();
				v += vb.getName();												
			}

			String header = clis.getHeader();
			
			SimpleStreamSetting sst = new SimpleStreamSetting( StreamLibrary.LSL
																, v
																, varType
																, nChs
																, 1
																, 0
																, 3
																, true
																, ""
																, ""
																, null );

			MutableStreamSetting msst = new MutableStreamSetting( sst );
			msst.setDescription( header );
			pars.setParameter( OutputFileFormatParameters.DATA_NAMES, varNames.toString() );
			
			OutputBinaryDataWriter wr = new OutputBinaryDataWriter( f.getAbsolutePath(), null,  pars, sss );
														
			int iSeq = 0;
			for( int iv = 0; iv < lmvb.size(); iv++ )
			{												
				MetadataVariableBlock vb = lmvb.get( iv );
				
				double memorySize = (Integer)ConfigApp.getProperty( ConfigApp.SEGMENT_BLOCK_SIZE ) * Math.pow( 2, 20 );
				int dataByteSize = StreamUtils.getDataTypeBytes( vb.getDataType() );
				
				int chunkSize = (int)( memorySize / dataByteSize );
				if( chunkSize < 1 )
				{
					chunkSize = 1;
				}
				
				nChs = vb.getCols();
				String var = vb.getName();
				varType = vb.getDataType();
					
				boolean cont = true;
				while( cont )
				{
					Number[][] vData = clis.importNextDataBlock( iv, chunkSize );
					
					cont = ( vData != null );
					
					if( cont )
					{
						DataBlock db = DataBlockFactory.getDataBlock( varType, iSeq, var, nChs, ConvertTo.Transform.Matrix2Array( vData ) );

						wr.saveData( db );

						iSeq++;
					}
				}
			}
			
			//wr.addMetadata( "", header );
						
			wr.close();				
			
			while( !wr.isFinished() )
			{
				Thread.sleep( 100L );
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
		finally {
			System.out.println("testEncoder.checkBin() END");
		}
	}
}
